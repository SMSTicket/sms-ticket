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

import java.util.*;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.mariux.teleport.lib.TeleportService;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.MainActivity;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.receiver.SmsDelivered;
import eu.inmite.apps.smsjizdenka.receiver.SmsSent;

/**
 * Service for communication with wear device.
 *
 * @author Michal Matl (matl)
 */
public class WearableService extends TeleportService {

    @Override
    public void onCreate() {
        super.onCreate();
        setOnGetMessageCallback(new OnGetMessageCallback() {
            @Override
            public void onCallback(String path) {
                handleTeleportMessage(path);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            if ("sent_notification_to_wear".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                Ticket t = bundle.getParcelable("ticket");
                int status = bundle.getInt("status");
                sendSmsNotification(t, status);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Handle all messages here.
     */
    private void handleTeleportMessage(String path) {
        if (path.equals("/cities")) {
            syncCitiesToWear();
        } else if (path.startsWith("tickets")) {
            syncTicketsToWear(path);
        } else if (path.startsWith("sentTicket")) {
            sentTicket(path);
        } else if (path.startsWith("openTicket")) {
            openTicketInPhone(path);
        }
    }

    private void syncCitiesToWear() {
        CityManager cityManager = CityManager.get(getApplicationContext());
        List<City> cities = cityManager.getUniqueCities(getApplication());
        if (cities.size() > 0) {
            ArrayList<City> orderedCities = new ArrayList<>();
            LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                City closest = cityManager.getClosest(this, location.getLatitude(), location.getLongitude());
                if (closest != null) {
                    orderedCities.add(closest);

                    for (int i = 0; i < cities.size(); i++) {
                        if (closest.id != cities.get(i).id) {
                            orderedCities.add(cities.get(i));
                        }
                    }
                } else {
                    orderedCities.addAll(cities);
                }
            }

            final ArrayList<DataMap> dataCities = new ArrayList<DataMap>();

            for (final City city : orderedCities) {
                final DataMap dataMap = new DataMap();
                dataMap.putLong("id", city.id);
                dataMap.putString("city", city.city);
                dataMap.putString("country", city.country);

                dataCities.add(dataMap);
            }

            if (dataCities.size() == 0) {
                UpdateService.call(getApplicationContext(), false);
                sendError(getResources().getString(R.string.error_zero_cities));
            } else {
                PutDataMapRequest data = PutDataMapRequest.createWithAutoAppendedId("/cities");
                data.getDataMap().putDataMapArrayList("cities", dataCities);
                syncDataItem(data);
            }

        } else {
            UpdateService.call(getApplicationContext(), false);
            sendError(getResources().getString(R.string.error_zero_cities));
        }

    }

    /**
     * Sends error to wearable.
     */
    private void sendError(String message) {
        DebugLog.e(message);
        PutDataMapRequest data = PutDataMapRequest.createWithAutoAppendedId("/error");
        data.getDataMap().putString("error_message", message);
        syncDataItem(data);
    }

    private void syncTicketsToWear(String path) {
        Uri uri = Uri.parse(path);
        final ArrayList<DataMap> dataCities = new ArrayList<DataMap>();
        String cityName = uri.getLastPathSegment();

        List<City> cities = CityManager.get(getApplicationContext()).getTicketsInCity(cityName, getApplicationContext());

        for (final City city : cities) {
            final DataMap dataMap = new DataMap();
            dataMap.putLong("id", city.id);
            dataMap.putString("city", city.city);
            dataMap.putString("country", city.country);
            dataMap.putInt("validity", city.validity);
            dataMap.putString("price", city.price);
            dataMap.putString("currency", city.currency);
            dataMap.putString("price_note", city.priceNote);
            dataMap.putString("note", city.note);

            dataCities.add(dataMap);
        }
        PutDataMapRequest data = PutDataMapRequest.createWithAutoAppendedId("/tickets");
        data.getDataMap().putDataMapArrayList("tickets", dataCities);
        syncDataItem(data);
    }

    private void sentTicket(String path) {
        Uri uri = Uri.parse(path);
        final ArrayList<DataMap> dataCities = new ArrayList<DataMap>();
        long cityId = Long.parseLong(uri.getLastPathSegment());

        City city = CityManager.get(getApplicationContext()).getCity(getApplicationContext(), cityId);
        //addTestingTicket();
        orderNewTicket(city);

    }

    private void sendSmsNotification(Ticket t, int status) {
        DebugLog.d("Send ticket to wear" + t.toString());
        final DataMap dataMap = new DataMap();
        dataMap.putLong("id", t.getId());
        dataMap.putLong("city_id", t.getCityId());
        dataMap.putString("city", t.getCity());
        dataMap.putInt("status", status);
        dataMap.putString("hash", t.getHash());
        dataMap.putString("text", t.getText());
        dataMap.putLong("valid_from", t.getValidFrom().toMillis(true));
        dataMap.putLong("valid_to", t.getValidTo().toMillis(true));
        dataMap.putLong("notification_id", t.getNotificationId());

        PutDataMapRequest data = PutDataMapRequest.createWithAutoAppendedId("/notification");
        data.getDataMap().putDataMap("notification", dataMap);
        syncDataItem(data);
    }

    /**
     * Order ticket in new thread.
     */
    public synchronized void orderNewTicket(final City city) {
        if (city == null) {
            return;
        }
        // make sure sms cannot be saved twice
        Preferences.set(getApplication(), Preferences.LAST_ORDER_TIME, System.currentTimeMillis());
        //SL.get(AnalyticsService.class).trackEvent("order-ticket", analyticsSource, "city", city.city, "price", city.price);

        new Thread(new Runnable() {
            @Override
            public void run() {

                Time now = new Time();
                now.setToNow();
                now.switchTimezone(Time.getCurrentTimezone());

                final ContentValues cv = new ContentValues();
                cv.put(TicketProvider.Tickets.ORDERED, now.format3339(false));
                cv.put(TicketProvider.Tickets.VALID_TO, now.format3339(false));
                cv.put(TicketProvider.Tickets.VALID_TO_DATE, Long.MAX_VALUE);
                cv.put(TicketProvider.Tickets.CITY, city.city);
                cv.put(TicketProvider.Tickets.CITY_ID, city.id);
                cv.put(TicketProvider.Tickets.STATUS, TicketProvider.Tickets.STATUS_WAITING);
                final Uri uri = getApplicationContext().getContentResolver().insert(TicketProvider.Tickets.CONTENT_URI, cv);

                // send SMS directly
                final SmsManager sm = SmsManager.getDefault();
                final Intent sentIntent = new Intent(SmsSent.INTENT_SMS_SENT);
                sentIntent.putExtra("uri", uri);
                sentIntent.putExtra("NUMBER", city.number);
                sentIntent.putExtra("MESSAGE", city.request);
                final PendingIntent sent = PendingIntent.getBroadcast(getApplicationContext(), Constants.BROADCAST_SMS_SENT,
                    sentIntent, PendingIntent.FLAG_ONE_SHOT);

                final Intent deliveredIntent = new Intent(SmsDelivered.INTENT_SMS_DELIVERED);
                deliveredIntent.putExtra("uri", uri);
                final PendingIntent delivered = PendingIntent.getBroadcast(getApplicationContext(), Constants.BROADCAST_SMS_DELIVERED,
                    deliveredIntent,
                    PendingIntent.FLAG_ONE_SHOT);
                try {
                    // most sensitive line of the entire app:
                    sm.sendTextMessage(city.number, null, city.request, sent, delivered);
                } catch (SecurityException e) {
                    // LG Optimus Black needs READ_PHONE_STATE permission

                }

            }
        }).start();
    }

    private void addTestingTicket() {
        Ticket ticket = new Ticket();
        if (Locale.getDefault().toString().startsWith("en")) {
            ticket.setCity("Prague");
        } else {
            ticket.setCity("Praha");
        }
        ticket.setCityId(1);
        ticket.setHash("bhAJpWP9B / 861418");
        ticket.setStatus(TicketProvider.Tickets.STATUS_DELIVERED);
        ticket.setText("DP hl.m.Prahy, a.s., Jizdenka prestupni 32,- Kc, Platnost od: 29.8.11 8:09  do: 29.8.11 9:39. Pouze v pasmu P. WzL9n3JuQ /" +
            " " +
            "169605");
        int second = 1000;
        int minute = 60 * second;
        int hour = 60 * minute;
        int day = 24 * hour;
        Time time = new Time();
        time.setToNow();
        ticket.setOrdered(time);
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.SECOND, 0);
        long nowFullMinute = calendar.getTimeInMillis();
        time.set(nowFullMinute);
        ticket.setValidFrom(time);
        Time time2 = new Time();
        time2.set(nowFullMinute + 12 * minute);
        ticket.setValidTo(time2);
        SmsReceiverService.call(getApplicationContext(), ticket);
    }

    private void openTicketInPhone(String path) {
        DebugLog.e("test");
        Uri uri = Uri.parse(path);
        long ticketId = Long.parseLong(uri.getLastPathSegment());
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(MainActivity.EXTRA_TICKET_ID, ticketId);
        startActivity(i);
    }

}
