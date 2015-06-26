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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.adapter.EmptyGridPagerAdapter;
import eu.inmite.apps.smsjizdenka.adapter.TicketsAdapter;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.events.ConfirmationTicketEvent;
import eu.inmite.apps.smsjizdenka.events.FinishEvent;
import eu.inmite.apps.smsjizdenka.events.TicketsEvent;
import eu.inmite.apps.smsjizdenka.model.City;

/**
 * Shows list of tickets which are available for the chosen city.
 *
 * @author Michal Matl (matl)
 */
public class TicketsActivity extends ProjectBaseActivity {

    private static final String ARG_CITY_NAME = "city_name";
    private static final String ARG_CITY_ID = "city_id";

    @InjectView(R.id.pager)
    GridViewPager vPager;
    @InjectView(R.id.page_indicator)
    DotsPageIndicator vDostDotsPageIndicator;
    @InjectView(R.id.image)
    ImageView vImage;
    @InjectView(R.id.txtError)
    TextView vTxtError;

    private String mCityName;
    private long mCityId;
    private List<City> mTickets;

    public static void call(Activity activity, String city, long cityId) {
        Intent intent = new Intent(activity, TicketsActivity.class);
        intent.putExtra(ARG_CITY_NAME, city);
        intent.putExtra(ARG_CITY_ID, cityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();
        mCityName = bundle.getString(ARG_CITY_NAME);
        mCityId = bundle.getLong(ARG_CITY_ID);

        setupViewPager();

        getTeleport().sendMessage("tickets/" + mCityName, null);
        vDostDotsPageIndicator.setPager(vPager);
    }

    private void setupViewPager() {
        vPager.setAdapter(new EmptyGridPagerAdapter()); // bug in the UI library
    }

    @Subscribe
    public void onTicketsList(TicketsEvent event) {
        mTickets = event.getCities();
        vPager.setAdapter(new TicketsAdapter(getFragmentManager(), getApplicationContext(), mTickets, mCityId));

        if (Constants.CITIES_BACKGROUND_MAP.get(mCityId) != null) {
            vImage.setImageResource(Constants.CITIES_BACKGROUND_MAP.get(mCityId));
        }

    }

    @Subscribe
    public void onCallTicketConfirmation(ConfirmationTicketEvent event) {
        ConfirmationActivity.call(this, event.getTicket(), mCityId);
    }

    @Subscribe
    public void onFinishActivity(FinishEvent event) {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void startConnected() {
        super.startConnected();
        vTxtError.setText("");
        vTxtError.setVisibility(View.GONE);
        vPager.setVisibility(View.VISIBLE);
        vImage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void startDisconnected() {
        super.startDisconnected();
        vTxtError.setText(getString(R.string.error_please_connect));
        vTxtError.setVisibility(View.VISIBLE);
        vPager.setVisibility(View.GONE);
        vImage.setVisibility(View.GONE);
    }

}
