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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.avast.android.dialogs.core.BaseDialogFragment;
import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.CityTicketsActivity;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;

/**
 * Dialog for showing text of SMS.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class ShowSmsDialogFragment extends BaseDialogFragment {

    public static String TAG = "show_sms";

    public static ShowSmsDialogFragment newInstance(String text, long cityId) {
        ShowSmsDialogFragment dialog = new ShowSmsDialogFragment();
        Bundle args = new Bundle();
        args.putString("text", text);
        args.putLong("cityId", cityId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    protected Builder build(Builder builder) {
        builder.setTitle(R.string.tickets_sms_text);
        builder.setMessage(getText());
        builder.setPositiveButton(R.string.tickets_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.tickets_buy_new, new

            View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), CityTicketsActivity.class);
                    i.putExtra(CityTicketsActivity.EXTRA_CITY, getCity());
                    startActivity(i);
                    dismiss();
                }
            });
        return builder;
    }

    private String getText() {
        return getArguments().getString("text");
    }

    private String getCity() {
        long cityId = getArguments().getLong("cityId");
        City city = CityManager.get(getActivity()).getCity(getActivity(), cityId);
        return city.city;
    }
}
