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

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.activity.ConfirmationActivity;

import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportService;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.NotificationActivity;
import eu.inmite.apps.smsjizdenka.core.BusProvider;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.events.CitiesEvent;
import eu.inmite.apps.smsjizdenka.events.ErrorEvent;
import eu.inmite.apps.smsjizdenka.events.TicketsEvent;
import eu.inmite.apps.smsjizdenka.model.City;
import eu.inmite.apps.smsjizdenka.model.Ticket;
import eu.inmite.apps.smsjizdenka.receiver.OpenPhoneReceiver;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;
import eu.inmite.apps.smsjizdenka.util.ImageUtil;

/**
 * Manages communication between app in wear and app in phone.
 *
 * @author Michal Matl (matl)
 */
public class ListenerService extends TeleportService {

    @Override
    public void onCreate() {
        super.onCreate();
        setOnSyncDataItemCallback(new OnSyncDataItemCallback() {
            @Override
            public void onDataSync(DataMap dataMap) {
                handleDataChanged(dataMap);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            if ("open_ticket".equals(intent.getAction())) {
                final Ticket t = (Ticket)intent.getExtras().getSerializable("ticket");
                sendMessage("openTicket/" + t.getId(), null);

                Intent confirmationIntent = new Intent(this, ConfirmationActivity.class);
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                confirmationIntent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
                startActivity(confirmationIntent);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Handles received data.
     */
    private void handleDataChanged(DataMap data) {
        if (data.containsKey("cities")) {
            processCityList(data);
        } else if (data.containsKey("tickets")) {
            processTicketsList(data);
        } else if (data.containsKey("notification")) {
            processNotification(data);
        } else if (data.containsKey("error_message")) {
            BusProvider.getInstance().post(new ErrorEvent(data.getString("error_message")));
        }
    }

    private void processCityList(DataMap data) {
        ArrayList<City> cities = new ArrayList<City>();
        ArrayList<DataMap> dataCities = data.getDataMapArrayList("cities");

        if (dataCities.size() > 0) {
            for (DataMap city : dataCities) {
                cities.add(new City(city.getLong("id"), city.getString("country"), city.getString("city")));
            }

            BusProvider.getInstance().post(new CitiesEvent(cities));
        } else {
            BusProvider.getInstance().post(new ErrorEvent(getString(R.string.error_zero_cities)));
        }

    }

    private void processTicketsList(DataMap data) {
        ArrayList<City> cities = new ArrayList<City>();
        ArrayList<DataMap> dataCities = data.getDataMapArrayList("tickets");

        for (DataMap city : dataCities) {
            cities.add(new City(city.getLong("id"), city.getString("country"), city.getString("city"),
                city.getInt("validity"), city.getString("note"), city.getString("price"), city.getString("currency"),
                city.getString("price_note")));
        }

        BusProvider.getInstance().post(new TicketsEvent(cities));
    }

    private void processNotification(DataMap dataMap) {
        DataMap dataMap1 = dataMap.getDataMap("notification");

        Ticket ticket = new Ticket();

        ticket.setId(dataMap1.getLong("id"));
        ticket.setCityId(dataMap1.getLong("city_id"));
        ticket.setCity(dataMap1.getString("city"));
        ticket.setStatus(dataMap1.getInt("status"));
        ticket.setHash(dataMap1.getString("hash"));
        ticket.setText(dataMap1.getString("text"));
        ticket.setValidFrom(dataMap1.getLong("valid_from"));
        ticket.setValidTo(dataMap1.getLong("valid_to"));
        ticket.setNotificationId(dataMap1.getLong("notification_id"));

        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.putExtra("ticket", ticket);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);

        int notificationColor = 0;

        if (ticket.getStatus() == NotificationActivity.STATUS_VALID_EXPIRING) {
            notificationColor = getResources().getColor(R.color.background_city_yellow);
        } else if (ticket.getStatus() == NotificationActivity.STATUS_EXPIRING_EXPIRED) {
            notificationColor = getResources().getColor(R.color.background_city_red);
        } else if (ticket.getStatus() == NotificationActivity.STATUS_EXPIRED) {
            notificationColor = getResources().getColor(R.color.background_city_transparent);
        }

        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle(getResources().getString(R.string.sms))
            .bigText(ticket.getText());

        Notification secondPageNotification =
            new NotificationCompat.Builder(this)
                .setStyle(secondPageStyle)
                .build();

        Intent intent = new Intent(this, OpenPhoneReceiver.class);
        intent.setAction("eu.inmite.apps.smsjizdenka.openphone");
        intent.putExtra("ticket", ticket);
        PendingIntent openPhonePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);

        int resourceIdForCity = Constants.CITIES_BACKGROUND_MAP.containsKey(ticket.getCityId()) ? Constants
            .CITIES_BACKGROUND_MAP.get(ticket.getCityId()) : 0;
        Notification notification = new Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentText(getStatusTitle(ticket))
            .extend(new Notification.WearableExtender()
                .setDisplayIntent(notificationPendingIntent)
                .setCustomContentHeight(getResources().getDimensionPixelSize(R.dimen.notification_size))
                .setStartScrollBottom(true)
                .setBackground(ImageUtil.combineTwoImages(this, resourceIdForCity, notificationColor))
                .addPage(secondPageNotification)
                .addAction(new Notification.Action(R.drawable.go_to_phone_00156, "Open on phone", openPhonePendingIntent)))
            .build();

        notification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationManager notificationManager =
            (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int)ticket.getNotificationId(), notification);

    }

    private String getStatusTitle(Ticket t) {
        if (t.getStatus() == NotificationActivity.STATUS_VALID_EXPIRING) {
            return getString(R.string.notif_valid_text, FormatUtil.formatDateTimeDifference(t.getValidTo()));
        } else if (t.getStatus() == NotificationActivity.STATUS_EXPIRING_EXPIRED) {
            return getString(R.string.notif_expiring_text, FormatUtil.formatTime(t.getValidTo()));
        } else if (t.getStatus() == NotificationActivity.STATUS_EXPIRED) {
            return getString(R.string.notif_expired_text, FormatUtil.formatTime(t.getValidTo()));
        }

        return getString(R.string.app_name);
    }
}
