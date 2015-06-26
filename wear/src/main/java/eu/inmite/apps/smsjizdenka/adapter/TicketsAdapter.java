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

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.FragmentGridPagerAdapter;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.BusProvider;
import eu.inmite.apps.smsjizdenka.events.ConfirmationTicketEvent;
import eu.inmite.apps.smsjizdenka.fragment.ActionFragment;
import eu.inmite.apps.smsjizdenka.fragment.TicketFragment;
import eu.inmite.apps.smsjizdenka.model.City;

/**
 * Adapter for tickets.
 *
 * @author Michal Matl (matl)
 */
public class TicketsAdapter extends FragmentGridPagerAdapter {

    private Context mContext;
    private List<City> mTickets;
    private long mCityId;

    public TicketsAdapter(FragmentManager fm, Context context, List<City> tickets, long cityId) {
        super(fm);
        this.mContext = context;
        this.mTickets = tickets;
        this.mCityId = cityId;
    }

    @Override
    public Fragment getFragment(final int row, int col) {
        if (col == 0) {
            City ticket = mTickets.get(row);
            TicketFragment ticketFragment = TicketFragment.create(ticket);
            ticketFragment.setExpansionEnabled(false);
            return ticketFragment;
        } else if (col == 1) {
            return ActionFragment.create(R.drawable.aprove_ic_ticket
                , R.string.action_buy, new ActionFragment.Listener() {
                @Override
                public void onActionPerformed() {
                    City ticket = mTickets.get(row);
                    BusProvider.getInstance().post(new ConfirmationTicketEvent(ticket));
                }
            });
        }

        return null;
    }

    @Override
    public int getRowCount() {
        return mTickets.size();
    }

    @Override
    public int getColumnCount(int i) {
        return 2;
    }

}
