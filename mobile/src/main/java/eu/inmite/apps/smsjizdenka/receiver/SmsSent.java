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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.widget.Toast;

import eu.inmite.apps.smsjizdenka.BuildConfig;
import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;

/**
 * Receives broadcast about sending sms to DPP and tells user that message has been sent successfully.
 *
 * @author ondra
 */
public class SmsSent extends BroadcastReceiver {

    public static final String INTENT_SMS_SENT = BuildConfig.APPLICATION_ID + ".SMS_SENT";

    /**
     * Saves SMS to Sent in default Messaging app - using undocumented API, can break!
     */
    private static void saveMessageToSent(Context c, String number, String message) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            try {
                ContentValues values = new ContentValues();
                values.put("address", number);
                values.put("body", message);
                c.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
            } catch (Exception e) {
                // ignore because this can break on custom ROMs without Messaging app
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final int result = getResultCode();
        final Uri uri = (Uri)intent.getParcelableExtra("uri");

        boolean resultOk = false;
        switch (result) {
            case Activity.RESULT_OK:
                Toast.makeText(context, context.getText(R.string.msg_sms_sent), Toast.LENGTH_LONG).show();
                resultOk = true;
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(context, context.getText(R.string.msg_sms_sent_error_radio_off), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of RESULT_ERROR_RADIO_OFF");
                break;

            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(context, context.getText(R.string.msg_sms_sent_error_null_pdu), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of RESULT_ERROR_NULL_PDU");
                break;

            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(context, context.getText(R.string.msg_sms_sent_error_generic), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of RESULT_ERROR_GENERIC_FAILURE");
                break;

            default:
                Toast.makeText(context, context.getText(R.string.msg_sms_sent_error_generic), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of some other reason");
        }

        if (resultOk) {
            saveMessageToSent(context, intent.getStringExtra("NUMBER"), intent.getStringExtra("MESSAGE"));
        } else {
            if (uri != null) {
                context.getContentResolver().delete(uri, null, null);
            } else {
                DebugLog.w("Uri is null, something weird is going on");
            }
        }
    }
}