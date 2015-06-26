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

import eu.inmite.apps.smsjizdenka.model.Ticket;
import eu.inmite.apps.smsjizdenka.service.ListenerService;

/**
 * Receives event that user pushed button for viewing chosen ticket in phone.
 * <p/>
 * Michal Mátl (matl)
 */
public class OpenPhoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Ticket t = (Ticket)intent.getExtras().getSerializable("ticket");
        Intent openIntent = new Intent(context, ListenerService.class);
        openIntent.setAction("open_ticket");
        openIntent.putExtra("ticket", t);
        context.startService(openIntent);
    }
}
