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
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.mariux.teleport.lib.TeleportService;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.MainActivity;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.dialog.BuyTicketDialogFragment;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;

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
        long cityId = Long.parseLong(uri.getLastPathSegment());
        City city = CityManager.get(getApplicationContext()).getCity(getApplicationContext(), cityId);
        BuyTicketDialogFragment.orderNewTicket(city, getApplicationContext());

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
