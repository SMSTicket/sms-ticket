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
import android.widget.Toast;

import eu.inmite.apps.smsjizdenka.BuildConfig;
import eu.inmite.apps.smsjizdenka.R;

/**
 * Receives system notifications about delivering sms to DPP server.
 *
 * @author ondra
 */
public class SmsDelivered extends BroadcastReceiver {

    public static final String INTENT_SMS_DELIVERED = BuildConfig.APPLICATION_ID + ".SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getText(R.string.msg_sms_delivered), Toast.LENGTH_LONG).show();
    }
}
