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

import java.io.IOException;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.framework.App;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.framework.helper.BaseAsyncTask;
import eu.inmite.apps.smsjizdenka.framework.util.StreamUtils;

/**
 * Shows Eula text.
 *
 * @author Michal Mátl (matl)
 */
public class EulaDialogFragment extends SimpleDialogFragment {

    public static final String TAG = EulaDialogFragment.class.getName();
    public static final String ARG_OPENED_FROM_ABOUT = "ARG_OPENED_FROM_ABOUT";

    public static void show(FragmentActivity activity, Fragment targetFragment, int requestCode,
                            boolean openedFromAbout) {
        EulaDialogFragment eulaDialogFragment = (EulaDialogFragment)activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (eulaDialogFragment != null) {
            eulaDialogFragment.dismiss();
        }

        EulaDialogFragment d = new EulaDialogFragment();
        d.setTargetFragment(targetFragment, requestCode);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_OPENED_FROM_ABOUT, openedFromAbout);
        d.setArguments(bundle);
        d.show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    protected Builder build(Builder builder) {
        builder.setTitle(R.string.about_eula_full);
        builder.setView(getCustomView());

        if (getArguments().getBoolean(ARG_OPENED_FROM_ABOUT)) {
            builder.setPositiveButton(R.string.dialog_ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        } else {
            builder.setPositiveButton(R.string.agree, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Preferences.set(App.getInstance(), Preferences.EULA_CONFIRMED, true);
                    dismiss();
                }
            });
            builder.setNegativeButton(R.string.disagree, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            setCancelable(false);
        }

        return builder;
    }

    private View getCustomView() {
        View customView = LayoutInflater.from(getActivity()).inflate(R.layout.view_eula_dialog, null);
        final TextView txtEulaContent = (TextView)customView.findViewById(R.id.txt_eula_content);
        final ProgressBar progressWheel = (ProgressBar)customView.findViewById(R.id.progress_bar);

        new BaseAsyncTask() {
            String eula;
            Spanned spannedEula;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressWheel.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                eula = getStringFromAssets(App.getInstance(), "eula.html");
                spannedEula = Html.fromHtml(eula);
            }

            @Override
            public void onPostExecute() {
                super.onPostExecute();
                progressWheel.setVisibility(View.GONE);
                txtEulaContent.setText(spannedEula);

            }
        }.start();

        return customView;
    }

    public String getStringFromAssets(@NonNull Context context, String data) {
        try {
            return StreamUtils.streamToString(context.getResources().getAssets().open(data));
        } catch (IOException e) {
            DebugLog.e("Read file from asset - failed", e);
        }

        return null;
    }
}
