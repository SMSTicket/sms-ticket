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

package eu.inmite.apps.smsjizdenka.framework.about;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.avast.android.dialogs.core.BaseDialogFragment;
import eu.inmite.apps.smsjizdenka.R;

/**
 * Shows used open-source libraries.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class OpenSourceDialogFragment extends BaseDialogFragment {

    public static final String TAG = "open_source";

    @Override
    protected Builder build(Builder builder) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_opensource, null);
        ListView vList = (ListView)view.findViewById(R.id.list);

        List<Constants.Library> usedLibraries = new ArrayList<Constants.Library>();

        for (Constants.Library library : Constants.KNOWN_LIBRARIES) {
            if (isClassAvailable(library.significantClass)) {
                usedLibraries.add(library);
            }
        }

        Collections.sort(usedLibraries, new Comparator<Constants.Library>() {
            @Override
            public int compare(Constants.Library lhs, Constants.Library rhs) {
                return lhs.name.compareToIgnoreCase(rhs.name);
            }
        });

        vList.setAdapter(new OpenSourceAdapter(getActivity(), getFragmentManager(), R.layout.item_opensource, usedLibraries));
        builder.setView(view);
        builder.setTitle(R.string.about_opensource_libraries);
        builder.setNegativeButton(android.R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ;
            }
        });
        return builder;
    }

    private boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
