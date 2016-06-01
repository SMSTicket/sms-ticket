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
package eu.inmite.apps.smsjizdenka.util;

import java.sql.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.TimeFormatException;

import eu.inmite.apps.smsjizdenka.R;

/**
 * Format Utils
 *
 * @author David Vávra (david@inmite.eu)
 */
public class FormatUtil {

    private FormatUtil() {}

    public static Time timeFrom3339(String s3339) {
        final Time time = new Time();
        if (TextUtils.isEmpty(s3339)) {
            return time;
        }
        try {
            time.parse3339(s3339);
        } catch (TimeFormatException e) {
            return time;
        }
        time.switchTimezone(Time.getCurrentTimezone());
        return time;
    }

    public static String formatTime(long millis) {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(millis));
    }

    public static String formatDateTime(long millis) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(millis));
    }

    public static String formatDateTimeDifference(long millis) {
        Calendar calendar = new GregorianCalendar();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(millis);
        int targetDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (currentDay == targetDay) {
            return formatTime(millis);
        } else {
            return formatDateTime(millis);
        }
    }

    public static String formatValidity(int validMinutes, Context c) {
        if (validMinutes <= 0) {
            return c.getString(R.string.tickets_expired);
        } else if (validMinutes == 1) {
            return c.getString(R.string.tickets_valid_one_minute, validMinutes);
        } else if (validMinutes <= 4) {
            return c.getString(R.string.tickets_valid_few_minutes, validMinutes);
        } else if (validMinutes <= 120) {
            return c.getString(R.string.tickets_valid_many_minutes, validMinutes);
        } else if (validMinutes <= 120) {
            return c.getString(R.string.tickets_valid_few_hours, (int)Math.floor(validMinutes / 60));
        } else {
            return c.getString(R.string.tickets_valid_many_hours, (int)Math.floor(validMinutes / 60));
        }
    }

    public static String formatCurrency(double amount, String currencyCode) {
        // I don't know why this is happening but sometimes currencyCode is Kč instead of CZK
        currencyCode = currencyCode.replace("Kč", "CZK");
        Currency currency = Currency.getInstance(currencyCode);
        String formatted = LocaleUtils.formatCurrency(amount, currency);
        //hack for Czech crowns - there is no better solution because Android doesn't allow different settings for
        // locale and language
        return formatted.replace("CZK", "CZK ").replace("Kč", "Kč ");
    }
}
