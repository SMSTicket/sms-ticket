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

import java.util.List;

import android.app.Activity;
import android.graphics.Paint;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.framework.util.IntentUtils;

/**
 * Adapter for open-source libraries.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class OpenSourceAdapter extends ArrayAdapter<Constants.Library> {

    FragmentManager mFragmentManager;
    Activity mContext;
    int mLayoutResourceId;
    List<Constants.Library> mLibraries;
    LayoutInflater mInflater;

    public OpenSourceAdapter(Activity context, FragmentManager fragmentManager, int layoutResourceId, List<Constants.Library> libraries) {
        super(context, layoutResourceId, libraries);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mLibraries = libraries;
        mFragmentManager = fragmentManager;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder)view.getTag();
        } else {
            view = mInflater.inflate(mLayoutResourceId, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        Constants.Library library = mLibraries.get(position);
        holder.name.setText(library.name);
        holder.author.setText(mContext.getString(R.string.about_library_by, library.author));
        openWeb(holder.projectWebsite, library.projectWebsite);
        holder.license.setText(library.license.name);
        if (library.license.equals(Constants.APACHE_PLAY_SERVICES)) {
            openPlayServices(holder.license);
        } else {
            openWeb(holder.license, library.license.url);
        }

        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private void openPlayServices(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PlayServicesLicenceDialogFragment.show(mFragmentManager);
            }
        });
    }

    private void openWeb(TextView textView, final String url) {
        BaseAboutFragment.setupLink(textView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.openBrowser(mContext, url);
            }
        });
    }

    static class ViewHolder {
        TextView name;
        TextView author;
        TextView license;
        TextView projectWebsite;

        public ViewHolder(View view) {
            name = (TextView)view.findViewById(R.id.name);
            author = (TextView)view.findViewById(R.id.author);
            license = (TextView)view.findViewById(R.id.license);
            projectWebsite = (TextView)view.findViewById(R.id.project_website);
        }
    }
}
