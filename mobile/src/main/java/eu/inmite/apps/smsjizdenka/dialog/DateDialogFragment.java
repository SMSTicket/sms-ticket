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

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.avast.android.dialogs.core.BaseDialogFragment;
import eu.inmite.apps.smsjizdenka.R;

/**
 * Dialog for setting date.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class DateDialogFragment extends BaseDialogFragment {

    public static String TAG = "date";
    // TODO: static fields for fragment arguments are bad practice
    // TODO: StyledDialogs library already has DateDialogFragment, replace
    private static String sTitle;
    private static DateSelectedListener sListener;
    private static long sDate;
    private static boolean sBeginning;

    public static DateDialogFragment newInstance(String title, long date, boolean beginning, DateSelectedListener
        listener) {
        sTitle = title;
        sListener = listener;
        sBeginning = beginning;
        sDate = formatBeginningEnd(date);
        DateDialogFragment dialog = new DateDialogFragment();
        return dialog;
    }

    private static long formatBeginningEnd(long millis) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        if (sBeginning) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
        }
        return calendar.getTimeInMillis();
    }

    @Override
    protected Builder build(Builder builder) {
        final DatePicker datePicker = (DatePicker)LayoutInflater.from(getActivity()).inflate(R.layout.dialog_part_datepicker, null, false);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(sDate);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                    Calendar calendar = new GregorianCalendar(year, month, day);
                    sDate = formatBeginningEnd(calendar.getTimeInMillis());
                }
            });
        builder.setTitle(sTitle);
        builder.setView(datePicker);
        builder.setPositiveButton(R.string.set, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sListener.onDateSelected(sDate);
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

    public interface DateSelectedListener {
        public void onDateSelected(long millis);
    }
}
