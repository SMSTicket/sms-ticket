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

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.framework.App;
import eu.inmite.apps.smsjizdenka.framework.util.IntentUtils;

/**
 * Fixme: remove this class and move functions to {@link eu.inmite.apps.smsjizdenka.fragment.AboutFragment}. This
 * class was part of about library and doesn't make sense doing base fragment for such simple class like
 * about fragment.
 * <p/>
 * About fragment which implements all logic. Extend it if you wish to customize it.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class BaseAboutFragment extends Fragment {

    public static final int PART_CUSTOM = 1;
    public static final int PART_APP_ICON = 2;
    public static final int PART_VERSION = 8;
    public static final int PART_OPENSOURCE = 16;
    public static final int PART_APP_NAME = 32;
    public Context mContext;

    public static void setupLink(TextView textView, View.OnClickListener listener) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textView.setTextColor(App.getInstance().getResources().getColorStateList(R.color.about_link_dark));
        textView.setOnClickListener(listener);
    }

    /**
     * Override this in the child to turn off some parts of the about screen. By default all parts are enabled.
     */
    public int getVisibleParts() {
        return PART_CUSTOM | PART_APP_ICON | PART_VERSION | PART_OPENSOURCE | PART_APP_NAME;
    }

    /**
     * Override this in the child to create custom part. By default there is no custom view.
     */
    public View getCustomPart() {
        return null;
    }

    /**
     * Override this in a child to add easter egg :) Easter egg is displayed after 5 taps on version in a toast. You
     * can somehow immortalize yourself in the app :) By default, no easter egg is shown.
     */
    public String getEasterEggText() {
        return null;
    }

    private boolean isVisible(int part) {
        return (getVisibleParts() & part) > 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FrameLayout vCustomPart = (FrameLayout)view.findViewById(R.id.part_custom);
        ImageView vAppIcon = (ImageView)view.findViewById(R.id.part_app_icon);
        TextView vVersion = (TextView)view.findViewById(R.id.part_version);
        TextView vOpensource = (TextView)view.findViewById(R.id.part_opensource);
        TextView vAppName = (TextView)view.findViewById(R.id.part_app_name);

        if (isVisible(PART_CUSTOM)) {
            View customView = getCustomPart();
            if (customView != null) {
                vCustomPart.setVisibility(View.VISIBLE);
                vCustomPart.addView(customView);
            }
        } else {
            vCustomPart.setVisibility(View.GONE);
        }

        if (isVisible(PART_APP_NAME)) {
            vAppName.setText(getActivity().getApplicationInfo().loadLabel(getActivity().getPackageManager()).toString
                ());
        } else {
            vAppName.setVisibility(View.GONE);
        }

        if (isVisible(PART_APP_ICON)) {
            if (getAppIcon() != 0) {
                vAppIcon.setImageResource(getAppIcon());
            }
            vAppIcon.setVisibility(View.VISIBLE);
            vAppIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IntentUtils.openBrowser(getActivity(), "http://www.avast.com");
                }
            });
        } else {
            vAppIcon.setVisibility(View.GONE);
        }


        if (isVisible(PART_VERSION)) {
            vVersion.setVisibility(View.VISIBLE);
            setupVersion(vVersion);
        } else {
            vVersion.setVisibility(View.GONE);
        }

        if (isVisible(PART_OPENSOURCE)) {
            vOpensource.setVisibility(View.VISIBLE);
            setupLink(vOpensource, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openOpenSourceDialog();
                }
            });
        } else {
            vOpensource.setVisibility(View.GONE);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
    }

    private void setupVersion(TextView vVersion) {
        vVersion.setText("v. " + App.getAppVersionName() + (App.isReleaseBuild() ? "" : "-debug"));
        if (!TextUtils.isEmpty(getEasterEggText())) {
            vVersion.setOnClickListener(new View.OnClickListener() {

                int clicks = 0;

                @Override
                public void onClick(View view) {
                    if (clicks++ > 4) {
                        SimpleDialogFragment.createBuilder(mContext, getFragmentManager()).setMessage(getEasterEggText()).show();
                    }

                }
            });
        }
    }

    protected int getAppIcon() {
        return 0;
    }

    protected void openOpenSourceDialog() {
        new OpenSourceDialogFragment().show(getFragmentManager(), OpenSourceDialogFragment.TAG);
    }

}
