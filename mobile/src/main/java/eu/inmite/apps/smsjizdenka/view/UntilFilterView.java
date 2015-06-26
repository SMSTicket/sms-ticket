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
package eu.inmite.apps.smsjizdenka.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.dialog.DateDialogFragment;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;

/**
 * Custom view for filtering according to from date.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class UntilFilterView extends LinearLayout {

    public static final long FILTER_ALL = -1;
    Context c;
    private SelectedChangedListener mListener;
    private long mSelected = FILTER_ALL;
    private TextView vSelected;

    public UntilFilterView(Context context) {
        super(context);
        c = context;
        init();
    }

    public UntilFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public UntilFilterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        c = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(c).inflate(R.layout.view_filter, this, true);
        vSelected = (TextView)view.findViewById(R.id.selected);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        update();
    }

    public long getSelected() {
        return mSelected;

    }

    public void setSelected(long selected) {
        mSelected = selected;
        update();
    }

    public void setOnSelectedChangedListener(SelectedChangedListener listener) {
        mListener = listener;
    }

    private void update() {
        if (mSelected == FILTER_ALL) {
            vSelected.setText(R.string.stats_until_today);
        } else {
            vSelected.setText(c.getString(R.string.stats_until_date, FormatUtil.formatDate(mSelected)));
        }
    }

    private void showDialog() {
        long millis = mSelected;
        if (mSelected == FILTER_ALL) {
            millis = System.currentTimeMillis();
        }
        final DateDialogFragment dialogFragment = DateDialogFragment.newInstance(c.getString(R.string.stats_until),
            millis, false, new DateDialogFragment.DateSelectedListener() {
                @Override
                public void onDateSelected(long millis) {
                    if (mListener != null && mSelected != millis) {
                        mListener.onSelectedChanged(millis);
                    }
                    mSelected = millis;
                    update();
                }
            });
        dialogFragment.show(((FragmentActivity)c).getSupportFragmentManager(), DateDialogFragment.TAG);
    }

    public interface SelectedChangedListener {
        public void onSelectedChanged(long selected);
    }

}