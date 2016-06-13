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
package eu.inmite.apps.smsjizdenka.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.dialog.ListDialogFragment;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;

/**
 * Custom view for filtering ticket types
 *
 * @author David Vávra (david@inmite.eu)
 */
public class TypeFilterView extends LinearLayout {

    public static final long FILTER_ALL = -1;
    Context c;
    private List<Long> mKeys;
    private List<String> mValues;
    private SelectedChangedListener mListener;
    private long mSelected = FILTER_ALL;
    private String mCity = CityFilterView.FILTER_ALL;
    private TextView vSelected;

    public TypeFilterView(Context context) {
        super(context);
        c = context;
        init();
    }

    public TypeFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TypeFilterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        c = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(c).inflate(R.layout.view_filter, this, true);
        vSelected = (TextView)view.findViewById(R.id.selected);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        load();
    }

    public long getSelected() {
        return mSelected;
    }

    public void setSelected(String city, long selected) {
        mSelected = selected;
        mCity = city;
        load();
    }

    public void setOnSelectedChangedListener(SelectedChangedListener listener) {
        mListener = listener;
    }

    public void setCity(String city) {
        mCity = city;
        mSelected = FILTER_ALL;
        load();
    }

    private void load() {
        if (mCity.equals(CityFilterView.FILTER_ALL)) {
            setEnabled(false);
            findViewById(R.id.dropdown).setEnabled(false);
            vSelected.setEnabled(false);
        } else {
            setEnabled(true);
            findViewById(R.id.dropdown).setEnabled(true);
            vSelected.setEnabled(true);
        }
        new FillTask().execute();
    }

    private void update() {
        for (int i = 0; i < mKeys.size(); i++) {
            long key = mKeys.get(i);
            if (key == mSelected) {
                vSelected.setText(mValues.get(i));
                break;
            }
        }
    }

    private void showDialog() {
        final ListDialogFragment dialogFragment = ListDialogFragment.newInstance(mValues, new ListDialogFragment.SelectedListener() {
            @Override
            public void onSelected(int position) {
                long current = mKeys.get(position);
                if (mListener != null && mSelected != current) {
                    mListener.onSelectedChanged(current);
                }
                mSelected = current;
                update();
            }
        });
        dialogFragment.show(((FragmentActivity)c).getSupportFragmentManager(), ListDialogFragment.TAG);
    }

    public interface SelectedChangedListener {
        public void onSelectedChanged(long selected);
    }

    class FillTask extends AsyncTask<Void, Void, Result> {

        @Override
        protected Result doInBackground(Void... voids) {
            CityManager cm = CityManager.get(c);
            Result result = new Result();
            result.keys = new ArrayList<Long>();
            result.values = new ArrayList<String>();
            result.keys.add(FILTER_ALL);
            result.values.add(c.getString(R.string.stats_all_types));
            if (!mCity.equals(CityFilterView.FILTER_ALL)) {
                List<City> cities = cm.getTicketsInCity(mCity, c);
                for (City city : cities) {
                    result.keys.add(city.id);
                    result.values.add(FormatUtil.formatValidity(city.validity, c) + " - " + FormatUtil.formatCurrency
                        (Double.parseDouble(city.price), city.currency));
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            mKeys = result.keys;
            mValues = result.values;
            update();
        }
    }

    class Result {
        List<Long> keys;
        List<String> values;
    }

}