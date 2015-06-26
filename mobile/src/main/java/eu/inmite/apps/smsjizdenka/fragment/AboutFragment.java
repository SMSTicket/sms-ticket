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

import android.os.Bundle;
import android.view.View;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseActivity;
import eu.inmite.apps.smsjizdenka.dialog.EulaDialogFragment;
import eu.inmite.apps.smsjizdenka.framework.about.BaseAboutFragment;


/**
 * About screen fragment.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class AboutFragment extends BaseAboutFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ProjectBaseActivity)getActivity()).getSupportActionBar().setTitle(R.string.ab_menu_about);
    }

    /**
     * Override this in the child to turn off some parts of the about screen. By default all parts are enabled.
     */
    @Override
    public int getVisibleParts() {
        return PART_CUSTOM | PART_VERSION | PART_APP_ICON | PART_APP_NAME;
    }

    @Override
    protected int getAppIcon() {
        return R.drawable.about_obr;
    }

    @Override
    public View getCustomPart() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_custom_about, null);
        view.findViewById(R.id.layout_open_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOpenSourceDialog();
            }
        });

        view.findViewById(R.id.layout_eula).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EulaDialogFragment.show(getActivity(), AboutFragment.this, 0, true);
            }
        });
        return view;
    }

    @Override
    public String getEasterEggText() {
        return "Remember Inmite? https://www.youtube.com/watch?v=DlS0sMrHTtA";
    }
}
