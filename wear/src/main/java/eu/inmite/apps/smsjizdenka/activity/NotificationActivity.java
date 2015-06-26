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

package eu.inmite.apps.smsjizdenka.activity;

import android.os.Bundle;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.model.Ticket;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;

/**
 * Shows itself as custom notification in a stream of notifications.
 * <p/>
 * Michal Mátl (matl)
 */
public class NotificationActivity extends ProjectBaseActivity {

    public static final int STATUS_EXPIRED = 7;
    public static final int STATUS_VALID_EXPIRING = 9;
    public static final int STATUS_EXPIRING_EXPIRED = 10;

    @InjectView(R.id.txt_hash)
    TextView vTxtHash;
    @InjectView(R.id.txt_status)
    TextView vTxtStatus;
    @InjectView(R.id.txt_city)
    TextView vTxtCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.inject(this);

        Ticket t = (Ticket)getIntent().getExtras().getSerializable("ticket");
        vTxtHash.setText(t.getHash());
        vTxtCity.setText(t.getCity());

        if (t.getStatus() == STATUS_VALID_EXPIRING) {
            vTxtStatus.setText(getString(R.string.notif_valid_text, FormatUtil.formatDateTimeDifference(t.getValidTo())));
            vTxtStatus.setTextColor(getResources().getColor(R.color.status_yellow));
        } else if (t.getStatus() == STATUS_EXPIRING_EXPIRED) {
            vTxtStatus.setText(getString(R.string.notif_expiring_text, FormatUtil.formatTime(t.getValidTo())));
            vTxtStatus.setTextColor(getResources().getColor(R.color.status_red));
        } else if (t.getStatus() == STATUS_EXPIRED) {
            vTxtStatus.setText(getString(R.string.notif_expired_text, FormatUtil.formatTime(t.getValidTo())));
            vTxtStatus.setTextColor(getResources().getColor(R.color.status_gray));
        }
    }
}
