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

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.model.City;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;

/**
 * Shows information for one ticket.
 *
 * @author Michal Matl (matl)
 */
public class TicketFragment extends CardFragment {

    private static final String ARG_TICKET = "ticket";

    private Context mContext;

    public static TicketFragment create(City city) {
        TicketFragment ticketFragment = new TicketFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_TICKET, city);
        ticketFragment.setArguments(bundle);
        return ticketFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ticket, container, false);

        City city = (City)getArguments().getSerializable(ARG_TICKET);

        TextView txtValidity = (TextView)root.findViewById(R.id.txt_validity);
        TextView txtValidityTime = (TextView)root.findViewById(R.id.txt_validity_time);
        TextView txtPrice = (TextView)root.findViewById(R.id.txt_price);
        TextView txtPriceNote = (TextView)root.findViewById(R.id.txt_price_note);
        txtPrice.setText(FormatUtil.formatCurrency(Double.parseDouble(city.price), city.currency));

        String[] priceAndTime = FormatUtil.formatValidity(city.validity, mContext).split(" ");
        txtValidity.setText(priceAndTime[0]);
        txtValidityTime.setText(priceAndTime[1]);

        if (!TextUtils.isEmpty(city.priceNote)) {
            txtPriceNote.setVisibility(View.VISIBLE);
            txtPriceNote.setText(city.priceNote);
        } else {
            txtPriceNote.setVisibility(View.GONE);
        }

        return root;
    }
}
