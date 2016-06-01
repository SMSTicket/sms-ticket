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
package eu.inmite.apps.smsjizdenka.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.MainActivity;
import eu.inmite.apps.smsjizdenka.adapter.TicketsAdapter;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.service.WearableService;

/**
 * Notification Utils
 *
 * @author David Vávra (david@inmite.eu)
 */
public class NotificationUtil {

    public static final int NOTIFICATION_VERIFY = 42;
    public static final int NOTIFICATION_MESSAGE = 43;

    private NotificationUtil() {}

    /**
     * Posts notification about new sms ticket.
     *
     * @param c context to post notification
     * @param t new ticket
     */
    public static void notifyTicket(Context c, @NonNull Ticket t, boolean keepNotification) {
        String text;
        String ticker;
        int smallIcon;
        int largeIcon;
        int status;
        switch (TicketsAdapter.getValidityStatus(t.getStatus(), t.getValidTo())) {
            case TicketProvider.Tickets.STATUS_VALID:
            case TicketProvider.Tickets.STATUS_VALID_EXPIRING:
                text = c.getString(R.string.notif_valid_text, FormatUtil.formatDateTimeDifference(t.getValidTo()));
                ticker = c.getString(R.string.notif_valid_ticker);
                smallIcon = R.drawable.notification_small_ready;
                largeIcon = R.drawable.notification_big_ready;
                status = TicketProvider.Tickets.STATUS_VALID_EXPIRING;
                break;
            case TicketProvider.Tickets.STATUS_EXPIRING:
            case TicketProvider.Tickets.STATUS_EXPIRING_EXPIRED:
                text = c.getString(R.string.notif_expiring_text, FormatUtil.formatTime(t.getValidTo()));
                ticker = c.getString(R.string.notif_expiring_ticker);
                smallIcon = R.drawable.notification_small_warning;
                largeIcon = R.drawable.notification_big_warning;
                status = TicketProvider.Tickets.STATUS_EXPIRING_EXPIRED;
                break;
            case TicketProvider.Tickets.STATUS_EXPIRED:
                text = c.getString(R.string.notif_expired_text, FormatUtil.formatTime(t.getValidTo()));
                ticker = c.getString(R.string.notif_expired_ticker);
                smallIcon = R.drawable.notification_small_expired;
                largeIcon = R.drawable.notification_big_expired;
                status = TicketProvider.Tickets.STATUS_EXPIRED;
                break;
            default:
                return;
        }

        Intent intent = new Intent(c, WearableService.class);
        intent.setAction("sent_notification_to_wear");
        intent.putExtra("ticket", t);
        intent.putExtra("status", status);
        c.startService(intent);

        Intent i = new Intent(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(MainActivity.EXTRA_TICKET_ID, t.getId());
        PendingIntent openIntent = PendingIntent.getActivity(c, t.getNotificationId(), i, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent i2 = new Intent(c, MainActivity.class);
        i2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i2.putExtra(MainActivity.EXTRA_TICKET_ID, t.getId());
        i2.putExtra(MainActivity.EXTRA_SHOW_SMS, true);
        PendingIntent showSmsIntent = PendingIntent.getActivity(c, t.getNotificationId() + 1000, i2,
            PendingIntent.FLAG_CANCEL_CURRENT);
        List<Action> actions = new ArrayList<Action>();
        actions.add(new Action(R.drawable.notification_show_sms, R.string.notif_show_sms, showSmsIntent));
        List<String> rows = new ArrayList<String>();
        rows.add(text);
        rows.add(c.getString(R.string.tickets_valid_from) + ": " + FormatUtil.formatDateTime(t.getValidFrom()));
        rows.add(c.getString(R.string.tickets_code) + ": " + t.getHash());
        fireNotification(c, t.getNotificationId(), openIntent, c.getString(R.string
            .application_name), text, rows, t.getCity(), ticker, smallIcon, largeIcon, actions, keepNotification);
    }

    /**
     * Verification if tickets is bought 1 minute after another.
     */
    public static void notifyVerification(Context c, City city) {
        Intent i = new Intent(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(MainActivity.EXTRA_REALLY_BUY_CITY_ID, city.id);
        PendingIntent contentIntent = PendingIntent.getActivity(c, NOTIFICATION_VERIFY, i, PendingIntent.FLAG_CANCEL_CURRENT);
        String title = c.getString(R.string.cities_ticket_bought_again_verification);
        String text = c.getString(R.string.cities_ticket_bought_again_action);
        fireNotification(c, NOTIFICATION_VERIFY, contentIntent, title, text, null, null, title,
            R.drawable.notification_small_warning,
            R.drawable.notification_big_warning, null, false);
    }

    public static void notifyMessage(Context c, String message) {
        Intent i = new Intent(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(MainActivity.EXTRA_MESSAGE, message);
        PendingIntent contentIntent = PendingIntent.getActivity(c, NOTIFICATION_MESSAGE, i,
            PendingIntent.FLAG_CANCEL_CURRENT);
        String title = c.getString(R.string.application_name);
        String text = message;
        fireNotification(c, NOTIFICATION_VERIFY, contentIntent, title, text, null, null, title,
            R.drawable.notification_small_ready,
            R.drawable.notification_big_ready, null, false);
        Preferences.set(c, Preferences.MESSAGE_READ, false);
    }

    private static void fireNotification(Context c, int notificationId, PendingIntent contentIntent,
                                         String title, String text,
                                         List<String> rows, String summary, String ticker, int smallIcon,
                                         int largeIcon,
                                         List<Action> actions, boolean keepNotification) {
        int defaults = Notification.DEFAULT_LIGHTS;
        if (Preferences.getBoolean(c, Preferences.NOTIFICATION_VIBRATE, true)) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c).setContentTitle(title).setSmallIcon
            (smallIcon).setLargeIcon(BitmapFactory.decodeResource(c.getResources(), largeIcon))
            .setTicker(ticker).setContentText(text)
            .setLocalOnly(true)
            .setContentIntent(contentIntent).setWhen(System.currentTimeMillis()).setAutoCancel(!keepNotification)
            .setDefaults
                (defaults);

        String soundUri = Preferences.getString(c, Preferences.NOTIFICATION_RINGTONE, null);
        if (!TextUtils.isEmpty(soundUri)) {
            builder.setSound(Uri.parse(soundUri));
        }

        if (actions != null) {
            for (Action action : actions) {
                builder.addAction(action.drawable, c.getString(action.text), action.intent);
            }
        }

        Notification notification;
        if (rows == null) {
            notification = builder.build();
        } else {
            NotificationCompat.InboxStyle styled = new NotificationCompat.InboxStyle(builder);
            for (String row : rows) {
                styled.addLine(row);
            }
            styled.setSummaryText(summary);
            notification = styled.build();
        }

        NotificationManager nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            DebugLog.e("Cannot obtain notification manager");
            return;
        }

        nm.notify(notificationId, notification);
    }

    static class Action {
        public int drawable;
        public int text;
        public PendingIntent intent;

        Action(int drawable, int text, PendingIntent intent) {
            this.drawable = drawable;
            this.text = text;
            this.intent = intent;
        }
    }
}
