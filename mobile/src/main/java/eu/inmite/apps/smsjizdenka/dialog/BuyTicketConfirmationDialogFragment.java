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

package eu.inmite.apps.smsjizdenka.dialog;

import android.os.Bundle;
import android.view.View;

import com.avast.android.dialogs.core.BaseDialogFragment;
import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.framework.App;

/**
 * Confirmation Dialog for buying a ticket.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class BuyTicketConfirmationDialogFragment extends BaseDialogFragment {

    public static String TAG = "buy_ticket_confirmation";

    public static BuyTicketConfirmationDialogFragment newInstance(long cityId) {
        BuyTicketConfirmationDialogFragment dialog = new BuyTicketConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putLong("city_id", cityId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    protected Builder build(Builder builder) {
        final City city = getCity();
        builder.setTitle(city.city);
        builder.setMessage(R.string.cities_ticket_bought_again_verification);
        builder.setPositiveButton(R.string.cities_buy_ticket, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BuyTicketDialogFragment.orderNewTicket(city, getActivity());
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new

            View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

        return builder;
    }

    private City getCity() {
        return CityManager.get(App.getInstance()).getCity(App.getInstance(), getArguments().getLong("city_id"));
    }
}
