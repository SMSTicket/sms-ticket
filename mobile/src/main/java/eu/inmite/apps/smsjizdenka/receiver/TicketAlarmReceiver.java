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

package eu.inmite.apps.smsjizdenka.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BadParcelableException;

import eu.inmite.apps.smsjizdenka.BuildConfig;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.util.NotificationUtil;


/**
 * Receives broadcasts of <code>eu.inmite.dpp.smsjizdenky.TICKET_ALARM</code> intent fired by alarm few minutes before
 * ticket expires.
 * <p/>
 * <p>After receiving broadcast, posts notification about expiring ticket.</p>
 *
 * @author ondra
 */
public class TicketAlarmReceiver extends BroadcastReceiver {

    public static final String INTENT_TICKET_ALARM_EXPIRING = BuildConfig.APPLICATION_ID + ".TICKET_ALARM_EXPIRING";
    public static final String INTENT_TICKET_ALARM_EXPIRED = BuildConfig.APPLICATION_ID + ".TICKET_ALARM_EXPIRED";
    public static final String EXTRA_TICKET = "TICKET";

    @Override
    public void onReceive(Context c, Intent intent) {
        try {
            final Ticket t = intent.getParcelableExtra(EXTRA_TICKET);

            if (t == null) {
                DebugLog.w("Scheduled ticket is null");
            } else {
                if (INTENT_TICKET_ALARM_EXPIRING.equals(intent.getAction())) {
                    NotificationUtil.notifyTicket(c, t, Preferences.getBoolean(c, Preferences.KEEP_NOTIFICATIONS,
                        true));
                    DebugLog.i("Receiving alarm for expiring ticket " + t);
                }
                if (INTENT_TICKET_ALARM_EXPIRED.equals(intent.getAction())) {
                    NotificationUtil.notifyTicket(c, t, false);
                    DebugLog.i("Receiving alarm for expired ticket " + t);
                }
            }
        } catch (BadParcelableException e) {
            DebugLog.i("ticket was bought in the old version and alarm received in the new version, ignoring");
        }
    }
}