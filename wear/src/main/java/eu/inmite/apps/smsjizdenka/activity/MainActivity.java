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

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.otto.Subscribe;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.adapter.CitiesAdapter;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.events.CitiesEvent;
import eu.inmite.apps.smsjizdenka.events.ErrorEvent;
import eu.inmite.apps.smsjizdenka.events.FinishEvent;
import eu.inmite.apps.smsjizdenka.model.City;

public class MainActivity extends ProjectBaseActivity {

    @InjectView(R.id.list)
    WearableListView vList;
    @InjectView(R.id.image)
    ImageView vImage;
    @InjectView(R.id.txtError)
    TextView vTxtError;

    private List<City> mCities;
    private CitiesAdapter mCitiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        getTeleport().sendMessage("/cities", null);

        vList.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                City city = mCities.get(viewHolder.getPosition());
                TicketsActivity.call(MainActivity.this, city.city, city.id);
            }

            @Override
            public void onTopEmptyRegionClick() {

            }
        });

        vList.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {

            }

            @Override
            public void onAbsoluteScrollChange(int i) {

            }

            @Override
            public void onScrollStateChanged(int i) {

            }

            @Override
            public void onCentralPositionChanged(int i) {
                if (mCities != null) {
                    loadBackground(i);
                }
            }
        });

    }

    private void loadBackground(int position) {
        if (mCities != null && mCities.size() > 0) {
            final City city = mCities.get(position);
            if (Constants.CITIES_BACKGROUND_MAP.get(city.id) != null) {
                String imageUri = "drawable://" + Constants.CITIES_BACKGROUND_MAP.get(city.id);
                ImageLoader.getInstance().cancelDisplayTask(vImage);
                ImageLoader.getInstance().displayImage(imageUri, vImage);
            } else {
                vList.setBackgroundResource(R.drawable.background_tickets);
            }
        } else {
            vTxtError.setVisibility(View.VISIBLE);
            vTxtError.setText(R.string.error_general);
        }

    }

    @Subscribe
    public void onCitiesList(CitiesEvent event) {
        mCities = event.getCities();
        if (mCities != null && mCities.size() > 0) {
            mCitiesAdapter = new CitiesAdapter(MainActivity.this, mCities);
            vList.setAdapter(mCitiesAdapter);
            loadBackground(0);
        } else {
            vTxtError.setVisibility(View.VISIBLE);
            vTxtError.setText(R.string.error_general);
        }

    }

    @Subscribe
    public void onError(ErrorEvent event) {
        vTxtError.setVisibility(View.VISIBLE);
        vTxtError.setText(event.getErrorMessage());
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
        vList.setVisibility(View.VISIBLE);
        vImage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void startDisconnected() {
        super.startDisconnected();
        vTxtError.setText(getString(R.string.error_please_connect));
        vTxtError.setVisibility(View.VISIBLE);
        vList.setVisibility(View.GONE);
        vImage.setVisibility(View.GONE);
    }
}
