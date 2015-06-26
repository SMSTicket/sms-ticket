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

import java.text.ParseException;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsMessage;

import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.service.SmsReceiverService;

/**
 * Receives system broadcasts about receiving sms <code>android.provider.Telephony.SMS_RECEIVED</code> and checks if
 * such sms message has been sent from DPP. And process message in that case.
 *
 * @author ondra
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String[] projection = new String[]{TicketProvider.Tickets.HASH};

    /**
     * Checks if given sms ticket is already stored in the database.
     * <p/>
     * <p>Ticket is identified by its hash.</p>
     *
     * @param context context to be used to get content resolver
     * @param t       ticket to be checked
     * @return true if ticket already exists, false otherwise
     */
    private static boolean alreadyProcessed(Context context, Ticket t) {
        boolean processed = false;

        Cursor c = context.getContentResolver().query(TicketProvider.Tickets.CONTENT_URI, projection, TicketProvider.Tickets.HASH + " = '" + t.getHash() + "'", null, null);
        try {
            if (c.moveToFirst()) {
                processed = true;
            }
        } finally {
            c.close();
        }

        return processed;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        if (!Preferences.getBoolean(c, Preferences.EULA_CONFIRMED, false)) {
            DebugLog.w("EULA not confirmed");
            return;
        }
        SmsMessage[] messages = getMessages(intent);
        try {
            for (int n = 0; n < messages.length; n++) {
                try {
                    DebugLog.i("receiving sms from " + messages[n].getDisplayOriginatingAddress());
                } catch (Exception e) {
                    // ignore
                }

                processTicket(c, messages[n]);
            }
        } catch (ParseException e) {
            DebugLog.e("received sms cannot be parsed because of " + e.getMessage());
        }
    }

    /**
     * Creates array of <code>SmsMessage</code> from given intent.
     *
     * @param intent extracts messages from this intent
     * @return array of extracted messages. If no messages received, empty array is returned
     */
    public synchronized SmsMessage[] getMessages(Intent intent) {
        Bundle bundle = intent.getExtras();

        Object messages[] = (Object[])bundle.get("pdus");
        if (messages != null) {
            SmsMessage[] smsMessages = new SmsMessage[messages.length];
            for (int i = 0; i < messages.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[])messages[i]);
            }
            return smsMessages;
        } else {
            return new SmsMessage[0];
        }
    }

    /**
     * Parses given sms message and if it contains valid sms ticket, stores ticket in the database and schedules alarm.
     *
     * @param m sms message to parse
     * @return parsed new ticket or null if ticket already exists in database
     * @throws java.text.ParseException
     */
    public synchronized void processTicket(Context c, SmsMessage m) throws ParseException {
        final String message = m.getDisplayMessageBody().replaceAll("[\n\r]", " ");

        DebugLog.i("receiving sms from " + m.getOriginatingAddress() + " with text " + message);

        final ArrayList<City> cities = CityManager.get(c).resolveCity(c, m.getOriginatingAddress(), message);
        if (cities == null || cities.isEmpty()) {
            DebugLog.w("no city recognized");
            return;
        }

		/*
        Commented out because service provider wants expensive SMS to confirm manually.

		for (City city : cities) {
			final boolean sendConfirm = city.parseConfirm(c, message, city);
			if (sendConfirm) {
				DebugLog.i("sending confirmation sms");

				final Intent sentIntent = new Intent(SmsConfirmationSent.INTENT_SMS_CONFIRM_SENT);
				final PendingIntent sent = PendingIntent.getBroadcast(c, Constants.BROADCAST_SMS_CONFIRM_SENT, sentIntent, PendingIntent.FLAG_ONE_SHOT);

				final SmsManager sm = SmsManager.getDefault();
				ArrayList<String> sText = sm.divideMessage(city.confirm);
				ArrayList<PendingIntent> sIntentsSent = new ArrayList<PendingIntent>();
				sIntentsSent.add(sent);

				sm.sendMultipartTextMessage(city.number, null, sText, sIntentsSent, null);

				abortBroadcast();

				return;
			}
		} */

        for (City city : cities) {
            final Ticket t = city.parseMessage(message);
            if (!alreadyProcessed(c, t)) {
                SmsReceiverService.call(c, t);
                if (!Preferences.getBoolean(c, Preferences.KEEP_IN_MESSAGING, false)) {
                    abortBroadcast();
                }
                return;
            }

            DebugLog.i("New sms ticket received but already present in the database. Not doing anything. Ticket " + t);
        }
    }
}