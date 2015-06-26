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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.CityTicketsActivity;
import eu.inmite.apps.smsjizdenka.adapter.CitiesAdapter;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseFragment;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.framework.helper.WrappedAsyncTaskLoader;


/**
 * Fragment displaying cities.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class CitiesFragment extends ProjectBaseFragment implements LoaderManager.LoaderCallbacks<Object> {

    ListView vList;
    CitiesAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        vList = (ListView)view.findViewById(android.R.id.list);
        vList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String city = mAdapter.getItem(position).city;
                //SL.get(AnalyticsService.class).trackEvent("select-city", "cities", "city", city);
                Intent i = new Intent(c, CityTicketsActivity.class);
                i.putExtra(CityTicketsActivity.EXTRA_CITY, city);
                startActivity(i);
            }
        });
        c.getSupportLoaderManager().initLoader(Constants.LOADER_CITIES, null, this);
        //SL.get(AnalyticsService.class).trackScreen("cities");
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle arg1) {
        if (id == Constants.LOADER_CITIES) {
            return new WrappedAsyncTaskLoader<Object>(c) {

                @Override
                public Object loadInBackground() {
                    List<CitiesAdapter.Item> items = new ArrayList<CitiesAdapter.Item>();
                    CityManager cm = CityManager.get(c);
                    // one closest
                    LocationManager lm = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
                    Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        City closest = cm.getClosest(c, location.getLatitude(), location.getLongitude());
                        if (closest != null) {
                            items.add(new CitiesAdapter.Item(CitiesAdapter.Item.TYPE_HEADER, c.getString(R.string.cities_nearest)));
                            items.add(new CitiesAdapter.Item(CitiesAdapter.Item.TYPE_CITY, closest.city));
                        }
                    }
                    // two last
                    List<City> lastTwo = cm.getLastTwoUsed(c);
                    if (lastTwo.size() > 0) {
                        items.add(new CitiesAdapter.Item(CitiesAdapter.Item.TYPE_HEADER,
                            c.getString(R.string.cities_recently_purchased)));
                        items.add(new CitiesAdapter.Item(CitiesAdapter.Item.TYPE_CITY, lastTwo.get(0).city));
                        if (lastTwo.size() == 2) {
                            items.add(new CitiesAdapter.Item(CitiesAdapter.Item.TYPE_CITY, lastTwo.get(1).city));
                        }
                    }
                    // all alphabetically
                    List<City> all = cm.getUniqueCities(c);
                    items.add(new CitiesAdapter.Item(CitiesAdapter.Item.TYPE_HEADER, c.getString(R.string.cities_all_tickets)));
                    for (City city : all) {
                        items.add(new CitiesAdapter.Item(CitiesAdapter.Item.TYPE_CITY, city.city));
                    }
                    return items;
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
        if (loader.getId() == Constants.LOADER_CITIES) {
            mAdapter = new CitiesAdapter(c, (List<CitiesAdapter.Item>)data);
            vList.setAdapter(mAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        // ignore
    }
}
