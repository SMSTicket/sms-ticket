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

package eu.inmite.apps.smsjizdenka.fragment;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.CityTicketsActivity;
import eu.inmite.apps.smsjizdenka.adapter.CityTicketsAdapter;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseFragment;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.dialog.BuyTicketDialogFragment;
import eu.inmite.apps.smsjizdenka.framework.helper.WrappedAsyncTaskLoader;


/**
 * Fragment displaying tickets in a city.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class CityTicketsFragment extends ProjectBaseFragment implements LoaderManager.LoaderCallbacks<Object> {

    ListView vList;
    CityTicketsAdapter mAdapter;
    boolean mDialogOpened = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        vList = (ListView) view.findViewById(android.R.id.list);
        vList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = mAdapter.getItem(position);
                //SL.get(AnalyticsService.class).trackEvent("select-ticket", "city-tickets", "city", city.city, "price", city.price);
                showBuyTicketDialog(city);
            }
        });
        c.getSupportLoaderManager().initLoader(Constants.LOADER_CITY_TICKETS, null, this);
        if (savedInstanceState != null) {
            mDialogOpened = savedInstanceState.getBoolean("dialogOpened", false);
        }
        //SL.get(AnalyticsService.class).trackScreen("city-tickets");
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle arg1) {
        if (id == Constants.LOADER_CITY_TICKETS) {
            return new WrappedAsyncTaskLoader<Object>(c) {

                @Override
                public Object loadInBackground() {
                    List<City> cities = CityManager.get(c).getTicketsInCity(getArguments().getString(CityTicketsActivity.EXTRA_CITY), c);
                    return cities;
                }
            };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        if (!isAdded()) {
            return;
        }
        if (loader.getId() == Constants.LOADER_CITY_TICKETS) {
            final List<City> cities = (List<City>) data;
            mAdapter = new CityTicketsAdapter(c, cities);
            vList.setAdapter(mAdapter);
            int minutes = getArguments().getInt(CityTicketsActivity.EXTRA_MINUTES, -1);
            if (cities.size() == 1 && !mDialogOpened) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        showBuyTicketDialog(cities.get(0));
                    }
                });
            } else if (minutes != -1) {
                resolvePubtranMinutes(cities, minutes);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAdapter != null && mAdapter.getCount() == 1) {
            outState.putBoolean("dialogOpened", true);
        }
        super.onSaveInstanceState(outState);
    }

    private void resolvePubtranMinutes(List<City> cities, int minutes) {
        for (int i = 0; i < cities.size(); i++) {
            final City city = cities.get(i);
            int previousValidity = Integer.MIN_VALUE;
            if (i > 0) {
                previousValidity = cities.get(i - 1).validity;
            }
            if (minutes > previousValidity && minutes <= city.validity) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        showBuyTicketDialog(city);
                    }
                });
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        // ignore
    }

    private void showBuyTicketDialog(City item) {
        if (isAdded()) {
            DialogFragment dialogFragment = BuyTicketDialogFragment.newInstance(item.id);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(dialogFragment, BuyTicketDialogFragment.TAG);
            ft.commitAllowingStateLoss();
        }
    }
}
