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

import java.util.HashMap;
import java.util.List;

import android.database.ContentObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseFragment;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.framework.helper.WrappedAsyncTaskLoader;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;
import eu.inmite.apps.smsjizdenka.view.BaseFilterView;
import eu.inmite.apps.smsjizdenka.view.CityFilterView;
import eu.inmite.apps.smsjizdenka.view.FromFilterView;
import eu.inmite.apps.smsjizdenka.view.TypeFilterView;
import eu.inmite.apps.smsjizdenka.view.UntilFilterView;

/**
 * Fragment displaying statistics.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class StatisticsFragment extends ProjectBaseFragment implements LoaderManager.LoaderCallbacks<Object> {

    TextView vTotalPrice;
    TextView vNumberTickets;
    FromFilterView vFromFilterView;
    UntilFilterView vUntilFilterView;
    CityFilterView vCityFilterView;
    TypeFilterView vTypeFilterView;
    View vContent;
    View vProgress;
    ContentObserver mObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            restartLoader();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vTotalPrice = (TextView)view.findViewById(R.id.total_price);
        vNumberTickets = (TextView)view.findViewById(R.id.number_tickets);
        vContent = view.findViewById(R.id.content);
        vProgress = view.findViewById(android.R.id.progress);
        vFromFilterView = (FromFilterView)view.findViewById(R.id.from_filter);
        vFromFilterView.setOnSelectedChangedListener(new FromFilterView.SelectedChangedListener() {
            @Override
            public void onSelectedChanged(long selected) {
                restartLoader();
            }
        });
        vUntilFilterView = (UntilFilterView)view.findViewById(R.id.until_filter);
        vUntilFilterView.setOnSelectedChangedListener(new BaseFilterView.SelectedChangedListener() {
            @Override
            public void onSelectedChanged(long selected) {
                restartLoader();
            }
        });
        vTypeFilterView = (TypeFilterView)view.findViewById(R.id.type_filter);
        vTypeFilterView.setOnSelectedChangedListener(new
                                                         TypeFilterView.SelectedChangedListener() {
                                                             @Override
                                                             public void onSelectedChanged(long selected) { //    String.valueOf(vTypeFilterView.getSelected()));
                                                                 restartLoader();
                                                             }
                                                         });
        vCityFilterView = (CityFilterView)view.findViewById(R.id.city_filter);
        vCityFilterView.setOnSelectedChangedListener(new
                                                         CityFilterView.SelectedChangedListener() {
                                                             @Override
                                                             public void onSelectedChanged(String selected) {
                                                                 restartLoader();
                                                                 vTypeFilterView.setCity(selected);
                                                             }
                                                         });
        showProgress();
        // recover from rotation
        if (savedInstanceState != null) {
            vFromFilterView.setSelected(savedInstanceState.getLong("from", FromFilterView.FILTER_ALL));
            vUntilFilterView.setSelected(savedInstanceState.getLong("to", UntilFilterView.FILTER_ALL));
            String city = savedInstanceState.getString("city");
            if (city == null) {
                city = CityFilterView.FILTER_ALL;
            }
            vCityFilterView.setSelected(city);
            vTypeFilterView.setSelected(city, savedInstanceState.getLong("type", TypeFilterView.FILTER_ALL));
        }
        c.getSupportLoaderManager().restartLoader(Constants.LOADER_STATISTICS, null, this);
        c.getContentResolver().registerContentObserver(TicketProvider.Tickets.CONTENT_URI, true, mObserver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (vFromFilterView == null) {
            return;
        }
        outState.putLong("from", vFromFilterView.getSelected());
        outState.putLong("to", vUntilFilterView.getSelected());
        outState.putLong("type", vTypeFilterView.getSelected());
        outState.putString("city", vCityFilterView.getSelected());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        c.getContentResolver().unregisterContentObserver(mObserver);
        super.onDestroyView();
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle bundle) {
        if (id == Constants.LOADER_STATISTICS) {
            return new WrappedAsyncTaskLoader<Object>(c) {

                @Override
                public Object loadInBackground() {
                    CityManager cm = CityManager.get(c);
                    LoaderResult result = new LoaderResult();

                    // compute statistics based on filters
                    double totalPrice = 0;
                    String currency = "CZK";
                    int numberOfTickets = 0;
                    HashMap<Long, City> cachedCities = new HashMap<Long, City>();
                    List<Ticket> tickets = cm.getAllTickets(c);
                    for (Ticket ticket : tickets) {
                        City city = cachedCities.get(ticket.getCityId());
                        if (city == null) {
                            city = cm.getCity(c, ticket.getCityId());
                            cachedCities.put(ticket.getCityId(), city);
                        }
                        if (city == null) {
                            continue;
                        }
                        if (numberOfTickets == tickets.size() - 1) {
                            // currency is taken from the last ticket
                            currency = city.currency;
                        }
                        // filters
                        long fromFilter = vFromFilterView.getSelected();
                        if (ticket.getValidFrom() != null && fromFilter > ticket.getValidFrom().toMillis(true) && fromFilter != FromFilterView
                            .FILTER_ALL) {
                            continue;
                        }
                        long untilFilter = vUntilFilterView.getSelected();
                        if (ticket.getValidFrom() != null && untilFilter < ticket.getValidFrom().toMillis(true) && untilFilter != UntilFilterView
                            .FILTER_ALL) {
                            continue;
                        }
                        String cityFilter = vCityFilterView.getSelected();
                        if (ticket.getCity() != null && !ticket.getCity().equals(cityFilter) && !cityFilter.equals(CityFilterView.FILTER_ALL)) {
                            continue;
                        }
                        long typeFilter = vTypeFilterView.getSelected();
                        if (ticket.getCityId() != typeFilter && typeFilter != TypeFilterView.FILTER_ALL) {
                            continue;
                        }
                        totalPrice += Double.parseDouble(city.price);
                        numberOfTickets++;
                    }
                    result.totalPrice = FormatUtil.formatCurrency(totalPrice, currency);
                    if (numberOfTickets == 1) {
                        result.numTickets = c.getString(R.string.stats_num_tickets_value_one, numberOfTickets);
                    } else if (numberOfTickets > 1 && numberOfTickets < 5) {
                        result.numTickets = c.getString(R.string.stats_num_tickets_value_few, numberOfTickets);
                    } else {
                        result.numTickets = c.getString(R.string.stats_num_tickets_value_many, numberOfTickets);
                    }
                    return result;
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
        if (loader.getId() == Constants.LOADER_STATISTICS) {
            LoaderResult result = (LoaderResult)data;
            vTotalPrice.setText(result.totalPrice);
            vNumberTickets.setText(result.numTickets);
            hideProgress();
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> objectLoader) {
        // ignore
    }

    private void showProgress() {
        vContent.setVisibility(View.GONE);
        vProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        vContent.setVisibility(View.VISIBLE);
        vProgress.setVisibility(View.GONE);
    }

    private void restartLoader() {
        c.getSupportLoaderManager().restartLoader(Constants.LOADER_STATISTICS, null, this);
    }

    class LoaderResult {
        public String totalPrice;
        public String numTickets;
    }
}
