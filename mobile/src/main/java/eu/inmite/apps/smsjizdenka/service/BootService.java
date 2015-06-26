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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import eu.inmite.apps.smsjizdenka.adapter.TicketsAdapter;
import eu.inmite.apps.smsjizdenka.data.TicketProvider.Tickets;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;

/**
 * Service which re-schedules alarms after boot.
 */
public class BootService extends IntentService {

    Context c;

    public BootService() {
        super("BootService");
    }

    public static void call(Context c) {
        final Intent i = new Intent(c, BootService.class);
        c.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        c = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent i) {
        final Cursor cursor = c.getContentResolver().query(Tickets.CONTENT_URI, null, Tickets.STATUS + " != ?",
            new String[]{"" + Tickets.STATUS_EXPIRED}, null);
        try {
            while (cursor.moveToNext()) {
                final Ticket t = CityManager.get(c).getTicket(cursor);
                final int status = TicketsAdapter.getValidityStatus(t.getStatus(), t.getValidTo());
                if (status == Tickets.STATUS_EXPIRING || status == Tickets.STATUS_VALID || status == Tickets
                    .STATUS_EXPIRING_EXPIRED || status == Tickets.STATUS_VALID_EXPIRING) {
                    try {
                        SmsReceiverService.postAlarm(this, t);
                    } catch (ParseException e) {
                        // ignore
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
