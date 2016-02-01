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

package eu.inmite.apps.smsjizdenka.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.view.View;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.MainActivity;
import eu.inmite.apps.smsjizdenka.adapter.CityTicketsAdapter;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider.Tickets;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.framework.App;
import eu.inmite.apps.smsjizdenka.receiver.SmsDelivered;
import eu.inmite.apps.smsjizdenka.receiver.SmsSent;
import eu.inmite.apps.smsjizdenka.util.NotificationUtil;

/**
 * Dialog for buying a ticket.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class BuyTicketDialogFragment extends BaseDialogFragment {

    public static String TAG = "buy_ticket";

    public static BuyTicketDialogFragment newInstance(long cityId) {
        BuyTicketDialogFragment dialog = new BuyTicketDialogFragment();
        Bundle args = new Bundle();
        args.putLong("city_id", cityId);
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Order ticket in new thread.
     */
    public static synchronized void orderNewTicket(final City city, final Context context) {
        if (city == null) {
            return;
        }
        // make sure sms cannot be saved twice
        Preferences.set(context, Preferences.LAST_ORDER_TIME, System.currentTimeMillis());

        if (Preferences.getBoolean(context, Preferences.PREFILL_SMS, false)) {
            // prefill SMS
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + city.number));
            i.putExtra("sms_body", city.request);
            if (!(context instanceof Activity)) {
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(i);
        } else {
            new Thread(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {

                    Time now = new Time();
                    now.setToNow();
                    now.switchTimezone(Time.getCurrentTimezone());

                    final ContentValues cv = new ContentValues();
                    cv.put(Tickets.ORDERED, now.format3339(false));
                    cv.put(Tickets.VALID_TO, now.format3339(false));
                    cv.put(Tickets.VALID_TO_DATE, Long.MAX_VALUE);
                    cv.put(Tickets.CITY, city.city);
                    cv.put(Tickets.CITY_ID, city.id);
                    cv.put(Tickets.STATUS, Tickets.STATUS_WAITING);
                    final Uri uri = context.getContentResolver().insert(Tickets.CONTENT_URI, cv);

                    // send SMS directly
                    SmsManager sm;
                    int simId = Integer.parseInt(Preferences.getString(App.getInstance(), Preferences.DUALSIM_SIM, String.valueOf(Preferences
                        .VALUE_DEFAULT_SIM)));
                    if (simId == Preferences.VALUE_DEFAULT_SIM) {
                        sm = SmsManager.getDefault();
                    } else {
                        sm = SmsManager.getSmsManagerForSubscriptionId(simId);
                    }
                    final Intent sentIntent = new Intent(SmsSent.INTENT_SMS_SENT);
                    sentIntent.putExtra("uri", uri);
                    sentIntent.putExtra("NUMBER", city.number);
                    sentIntent.putExtra("MESSAGE", city.request);
                    final PendingIntent sent = PendingIntent.getBroadcast(context, Constants.BROADCAST_SMS_SENT, sentIntent, PendingIntent.FLAG_ONE_SHOT);

                    final Intent deliveredIntent = new Intent(SmsDelivered.INTENT_SMS_DELIVERED);
                    deliveredIntent.putExtra("uri", uri);
                    final PendingIntent delivered = PendingIntent.getBroadcast(context, Constants.BROADCAST_SMS_DELIVERED, deliveredIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                    try {
                        // most sensitive line of the entire app:
                        sm.sendTextMessage(city.number, null, city.request, sent, delivered);
                    } catch (SecurityException e) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.msg_sms_sent_error_generic, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }).start();
        }
    }

    @Override
    protected Builder build(Builder builder) {

        final City city = getCity();
        builder.setTitle(city.city);
        builder.setView(CityTicketsAdapter.setupCityView(city, null, null, App.getInstance()));
        builder.setPositiveButton(R.string.cities_buy_ticket, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBoughtRecently()) {
                    NotificationUtil.notifyVerification(App.getInstance(), city);
                } else {
                    orderNewTicket(city, getActivity());
                }
                if (!Preferences.getBoolean(App.getInstance(), Preferences.PREFILL_SMS, false)) {
                    startBackActivity(MainActivity.class);
                } else {
                    dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new

            View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        return builder;
    }

    private City getCity() {
        return CityManager.get(App.getInstance()).getCity(App.getInstance(), getArguments().getLong("city_id"));
    }

    private boolean isBoughtRecently() {
        long diff = System.currentTimeMillis() - Preferences.getLong(App.getInstance(), Preferences.LAST_ORDER_TIME, 0);
        return (diff < Constants.WARNING_REPEAT_PURCHASE_SECONDS * 1000);
    }

    protected void startBackActivity(Class<?> activityClass) {
        Intent intent = new Intent(getActivity(), activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getActivity().startActivity(intent);
    }
}
