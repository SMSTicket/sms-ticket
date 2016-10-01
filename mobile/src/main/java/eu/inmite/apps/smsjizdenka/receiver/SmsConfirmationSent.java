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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;

/**
 * Receives broadcast about sending sms to DPP and tells user that message has been sent successfully.
 *
 * @author ondra
 */
public class SmsConfirmationSent extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        final int result = getResultCode();
        final Uri uri = (Uri)intent.getParcelableExtra("uri");

        boolean resultOk = false;
        switch (result) {
            case Activity.RESULT_OK:
                Toast.makeText(c, R.string.msg_sms_confirm_sent, Toast.LENGTH_LONG).show();
                resultOk = true;
                break;

            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(c, c.getText(R.string.msg_sms_confirm_sent_error_radio_off), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of RESULT_ERROR_RADIO_OFF");
                break;

            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(c, c.getText(R.string.msg_sms_confirm_sent_error_null_pdu), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of RESULT_ERROR_NULL_PDU");
                break;

            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(c, c.getText(R.string.msg_sms_confirm_sent_error_generic), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of RESULT_ERROR_GENERIC_FAILURE");
                break;

            default:
                Toast.makeText(c, c.getText(R.string.msg_sms_confirm_sent_error_generic), Toast.LENGTH_LONG).show();
                DebugLog.e("sms not sent because of some other reason");
        }

        if (!resultOk) {
            if (uri != null) {
                c.getContentResolver().delete(uri, null, null);
            } else {
                DebugLog.w("Uri is null, something weird is going on");
            }
        }
    }
}