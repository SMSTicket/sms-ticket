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

import java.lang.reflect.Method;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.framework.helper.BaseAsyncTask;

/**
 * Shows Apache licence text.
 *
 * @author Michal Mátl (matl)
 */
public class PlayServicesLicenceDialogFragment extends SimpleDialogFragment {

    public static final String TAG = PlayServicesLicenceDialogFragment.class.getName();

    public static void show(FragmentManager fragmentManager) {
        PlayServicesLicenceDialogFragment d = new PlayServicesLicenceDialogFragment();
        d.show(fragmentManager, TAG);
    }

    @Override
    protected Builder build(Builder builder) {
        builder.setView(getCustomView());
        return builder;
    }

    private View getCustomView() {
        View customView = LayoutInflater.from(getActivity()).inflate(R.layout.view_licence_dialog, null);
        final TextView txtEulaContent = (TextView)customView.findViewById(R.id.txt_content);
        final ProgressBar progress = (ProgressBar)customView.findViewById(R.id.progress_bar);

        new BaseAsyncTask() {
            String licence;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (isAdded()) {
                    progress.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void doInBackground() {
                licence = getPlayServicesLicense();
            }

            @Override
            public void onPostExecute() {
                super.onPostExecute();
                if (isAdded()) {
                    progress.setVisibility(View.GONE);
                    txtEulaContent.setText(licence);
                }

            }
        }.start();

        return customView;
    }

    private String getPlayServicesLicense() {
        try {
            Method method = Class.forName("com.google.android.gms.common.GooglePlayServicesUtil").getMethod("getOpenSourceSoftwareLicenseInfo", Context.class);
            return (String)method.invoke(null, getActivity().getApplicationContext());
        } catch (Exception e) {
            DebugLog.wtf("getPlayServicesLicense", e);
            return null;
        }
    }
}
