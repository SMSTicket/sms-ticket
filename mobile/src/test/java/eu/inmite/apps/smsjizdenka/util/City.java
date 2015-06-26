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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Simplified city object for tests.
 */
public class City {
    public long id;
    public String country;
    public String city;
    public String cityPubtran;
    public double lat;
    public double lon;
    public int validity;
    public String note;
    public double price;
    public String currency;
    public String priceNote;
    public String request;
    public String number;
    public String[] additionalNumbers;
    public String identification;
    public String pDateFrom;
    public String pDateTo;
    public String pHash;
    public String confirmReq;
    public String confirm;
    public String dateFormat;

    public SimpleDateFormat getSdf() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"), new Locale("cs", "CZ")));
        return sdf;
    }

    public SimpleDateFormat getSdfTime() {
        String[] dateFormatParts = dateFormat.split(" ");
        if (dateFormatParts.length > 1) {
            SimpleDateFormat sdfTime = new SimpleDateFormat(dateFormatParts[1]);
            sdfTime.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"), new Locale("cs", "CZ")));
            return sdfTime;
        }
        return null;
    }
}
