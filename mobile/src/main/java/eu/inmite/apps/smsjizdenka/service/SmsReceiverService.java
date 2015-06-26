/*
 * Copyright 2015 AVAST Software s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.inmite.apps.smsjizdenka.service;

import java.text.ParseException;
import java.util.Date;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.receiver.TicketAlarmReceiver;
import eu.inmite.apps.smsjizdenka.util.NotificationUtil;

//import eu.inmite.android.lib.analytics.AnalyticsService;

/**
 * Service which saves ticket in the database outside of main thread.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class SmsReceiverService extends IntentService {

    public static String EXTRA_TICKET = "TICKET";
    private Handler mHandler;
    private Context c;

    public SmsReceiverService() {
        super("SmsReceiverService");
    }

    public static void call(Context c, Ticket ticket) {
        final Intent i = new Intent(c, SmsReceiverService.class);
        i.putExtra(EXTRA_TICKET, ticket);
        c.startService(i);
    }

    /**
     * Sets alarm to notifyTicket <code>eu.inmite.apps.smsjizdenka.TICKET_ALARM</code> intent few minutes before ticket expires.
     *
     * @param c context
     * @param t ticket which alarm is set for
     * @throws java.text.ParseException if it is not possible to parse dates in given ticket instance
     */
    public static void postAlarm(Context c, Ticket t) throws ParseException {
        AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            DebugLog.e("Cannot obtain alarm service");
            return;
        }

        long alarmTime = t.getValidTo().toMillis(true) - Constants.EXPIRING_MINUTES * 60 * 1000;
        if (alarmTime > System.currentTimeMillis() && Preferences.getBoolean(c, Preferences.NOTIFY_BEFORE_EXPIRATION,
            true
        )) {
            final Intent i = new Intent(c, TicketAlarmReceiver.class);
            i.setAction(TicketAlarmReceiver.INTENT_TICKET_ALARM_EXPIRING);
            i.setData(Uri.withAppendedPath(TicketProvider.Tickets.CONTENT_URI, "" + t.getId()));
            i.putExtra(TicketAlarmReceiver.EXTRA_TICKET, t);

            final PendingIntent contentIntent = PendingIntent.getBroadcast(c, t.getNotificationId(), i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, alarmTime, contentIntent);
            DebugLog.i("Ticket alarm expiring set to " + new Date(alarmTime));
        }

        alarmTime = t.getValidTo().toMillis(true);
        if (alarmTime > System.currentTimeMillis()) {
            final Intent i = new Intent(c, TicketAlarmReceiver.class);
            i.setAction(TicketAlarmReceiver.INTENT_TICKET_ALARM_EXPIRED);
            i.setData(Uri.withAppendedPath(TicketProvider.Tickets.CONTENT_URI, "" + t.getId()));
            i.putExtra(TicketAlarmReceiver.EXTRA_TICKET, t);

            final PendingIntent contentIntent = PendingIntent.getBroadcast(c, t.getNotificationId(), i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, alarmTime, contentIntent);
            DebugLog.i("Ticket alarm expired set to " + new Date(alarmTime));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
        c = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Ticket ticket = (Ticket)intent.getParcelableExtra(EXTRA_TICKET);
        if (ticket == null) {
            return;
        }
        try {
            final Date now = new Date();
            ticket.setNotificationId(((now.getHours() * 100 + now.getMinutes()) * 100 + now.getSeconds()));
            ContentValues v = new ContentValues();
            v.put(TicketProvider.Tickets.VALID_FROM, ticket.getValidFrom().format3339(false));
            v.put(TicketProvider.Tickets.VALID_TO, ticket.getValidTo().format3339(false));
            v.put(TicketProvider.Tickets.VALID_TO_DATE, ticket.getValidTo().toMillis(false));
            v.put(TicketProvider.Tickets.HASH, ticket.getHash());
            v.put(TicketProvider.Tickets.CITY_ID, ticket.getCityId());
            v.put(TicketProvider.Tickets.TEXT, ticket.getText());
            v.put(TicketProvider.Tickets.CITY, ticket.getCity());
            v.put(TicketProvider.Tickets.STATUS, ticket.getStatus());
            v.put(TicketProvider.Tickets.NOTIFICATION_ID, ticket.getNotificationId());

            Uri u = c.getContentResolver().insert(TicketProvider.Tickets.CONTENT_URI, v);
            if (u == null) {
                toast(R.string.msg_insert_ticket_failed);
            } else {
                ticket.setId(Long.parseLong(u.getPathSegments().get(1)));
                long waitingTicketId = -1;

                Cursor ca = c.getContentResolver().query(
                    TicketProvider.Tickets.CONTENT_URI,
                    new String[]{TicketProvider.Tickets._ID},
                    TicketProvider.Tickets.STATUS + " = ? AND " + TicketProvider.Tickets.CITY_ID + " = ?",
                    new String[]{String.valueOf(TicketProvider.Tickets.STATUS_WAITING), String.valueOf(ticket.getCityId())},
                    TicketProvider.Tickets.ORDERED + " ASC"
                );

                try {
                    if (ca.moveToFirst()) {
                        waitingTicketId = ca.getLong(ca.getColumnIndex(TicketProvider.Tickets._ID));
                    }
                } finally {
                    ca.close();
                }

                if (waitingTicketId > 0) {
                    c.getContentResolver().delete(TicketProvider.Tickets.CONTENT_URI, TicketProvider.Tickets._ID + " = ?", new String[]{"" + waitingTicketId});
                }
            }

            DebugLog.i("New sms ticket inserted " + ticket);
            postAlarm(c, ticket);

            // send notification
            NotificationUtil.notifyTicket(c, ticket, Preferences.getBoolean(c, Preferences.KEEP_NOTIFICATIONS,
                true));
        } catch (ParseException e) {
            DebugLog.e("received sms cannot be parsed because of " + e.getMessage());
        }
    }

    private void toast(final int message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(c, c.getString(message), Toast.LENGTH_LONG).show();
            }
        });
    }

}
