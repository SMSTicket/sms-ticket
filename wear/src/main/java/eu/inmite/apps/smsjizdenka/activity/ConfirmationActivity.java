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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.BusProvider;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.events.FinishEvent;
import eu.inmite.apps.smsjizdenka.model.City;

/**
 * Shows view for canceling sending sms.
 *
 * @author Michal Matl (matl)
 */
public class ConfirmationActivity extends ProjectBaseActivity implements DelayedConfirmationView.DelayedConfirmationListener {

    private static final String ARG_CITY = "city";
    private static final String ARG_CITY_ID = "city_id";
    @InjectView(R.id.confirmation)
    DelayedConfirmationView vDelayedConfirmationView;
    @InjectView(R.id.image)
    ImageView vImage;
    private City mTicket;

    public static void call(Activity activity, City city, long cityId) {
        Intent intent = new Intent(activity, ConfirmationActivity.class);
        intent.putExtra(ARG_CITY, city);
        intent.putExtra(ARG_CITY_ID, cityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();

        mTicket = (City)bundle.getSerializable(ARG_CITY);
        vDelayedConfirmationView.setListener(this);
        vDelayedConfirmationView.setTotalTimeMs(3000);
        vDelayedConfirmationView.start();

        long cityId = bundle.getLong(ARG_CITY_ID);

        if (Constants.CITIES_BACKGROUND_MAP.get(cityId) != null) {
            vImage.setImageResource(Constants.CITIES_BACKGROUND_MAP.get(cityId));
        }
    }

    @Override
    public void onTimerFinished(View view) {
        if (!isFinishing()) {
            getTeleport().sendMessage("sentTicket/" + mTicket.id, null);
            showConfirmationDialog();
            BusProvider.getInstance().post(new FinishEvent());
        }

    }

    @Override
    public void onTimerSelected(View view) {
        finish();
    }

    private void showConfirmationDialog() {
        Intent intent = new Intent(this, android.support.wearable.activity.ConfirmationActivity.class);
        intent.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE,
            android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE,
            getString(R.string.message_sent));
        startActivity(intent);
    }

    @Subscribe
    public void onFinishActivity(FinishEvent event) {
        if (!isFinishing()) {
            finish();
        }
    }
}
