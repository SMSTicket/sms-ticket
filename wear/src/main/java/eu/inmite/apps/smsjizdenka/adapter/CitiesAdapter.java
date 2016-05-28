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

package eu.inmite.apps.smsjizdenka.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.model.City;

/**
 * Adapter for cities.
 *
 * @author Michal Matl (matl)
 */
public class CitiesAdapter extends WearableListView.Adapter {

    private int mDefaultCircleColor;
    private int mSelectedCircleColor;
    private Context mContext;
    private List<City> mCities;

    public CitiesAdapter(Context context, List<City> items) {
        mContext = context;
        this.mCities = items;
        mDefaultCircleColor = context.getResources().getColor(R.color.medium_gray);
        mSelectedCircleColor = context.getResources().getColor(R.color.yellow);
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WearableListView.ViewHolder(new ListItem(mContext));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
        City city = mCities.get(position);
        ListItem listItem = (ListItem)viewHolder.itemView;
        TextView text = (TextView)listItem.findViewById(R.id.text);
        text.setText(city.city);
        CircledImageView image = (CircledImageView)listItem.findViewById(R.id.image);
        image.setImageResource(R.drawable.list_ic_city);
        listItem.setTag(city.id);
    }

    @Override
    public int getItemCount() {
        return mCities.size();
    }

    class ListItem extends FrameLayout implements WearableListView.OnCenterProximityListener {

        @InjectView(R.id.image)
        CircledImageView image;
        @InjectView(R.id.text)
        TextView text;

        public ListItem(Context context) {
            super(context);
            View.inflate(context, R.layout.item_city, this);
            ButterKnife.inject(this, this);
        }

        @Override
        public void onCenterPosition(boolean b) {
            text.setAlpha(1f);
            image.setCircleColor(mSelectedCircleColor);
            image.setAlpha(1f);
            image.setScaleX(1f);
            image.setScaleY(1f);
        }

        @Override
        public void onNonCenterPosition(boolean b) {
            text.setAlpha(0.5f);
            image.setCircleColor(mDefaultCircleColor);
            image.setAlpha(0.5f);
            image.setScaleX(0.8f);
            image.setScaleY(0.8f);
        }
    }
}
