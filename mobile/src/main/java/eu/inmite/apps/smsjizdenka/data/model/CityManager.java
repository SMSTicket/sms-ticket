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

package eu.inmite.apps.smsjizdenka.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import eu.inmite.apps.smsjizdenka.data.CityProvider.Cities;
import eu.inmite.apps.smsjizdenka.data.TicketProvider.Tickets;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;
import eu.inmite.apps.smsjizdenka.util.LanguageUtil;

/**
 * Helper class for accessing data from database.
 */
public class CityManager {

    public static final String CITY_MANAGER_SERVICE = "cityManagerService";

    @SuppressLint("WrongConstant")
    public static CityManager get(Context context) {
        // TODO: It would be better to use Dagger 2 instead of this
        return (CityManager)((Application)context.getApplicationContext()).getSystemService(CITY_MANAGER_SERVICE);
    }

    public List<City> resolveCity(Context context, String number, String message) {
        ArrayList<City> cities = null;
        Cursor c = context.getContentResolver().query(Cities.CONTENT_URI, null, Cities.NUMBER + " = ? OR " + Cities.ADDITIONAL_NUMBER_1 + " = ? OR " + Cities.ADDITIONAL_NUMBER_2 + " = ? OR " + Cities.ADDITIONAL_NUMBER_3 + " = ?", new String[]{number, number, number, number}, null);

        try {
            final int indexNumber = c.getColumnIndex(Cities.NUMBER);
            final int indexNumber1 = c.getColumnIndex(Cities.ADDITIONAL_NUMBER_1);
            final int indexNumber2 = c.getColumnIndex(Cities.ADDITIONAL_NUMBER_2);
            final int indexNumber3 = c.getColumnIndex(Cities.ADDITIONAL_NUMBER_3);
            final int indexIdentification = c.getColumnIndex(Cities.IDENTIFICATION);

            while (c.moveToNext()) {
                if (number.equals(c.getString(indexNumber)) || number.equals(c.getString(indexNumber1)) || number.equals(c.getString(indexNumber2)) || number.equals(c.getString(indexNumber3))) {
                    if (message.matches("^" + c.getString(indexIdentification) + ".*")) {
                        if (cities == null) {
                            cities = new ArrayList<City>();
                        }
                        cities.add(getCity(c));
                    }
                }
            }
        } finally {
            c.close();
        }

        return cities;
    }

    public int getAvaitingTickets(Context context, City city) {
        StringBuilder where = new StringBuilder();
        where.append("(");
        where.append(Tickets.STATUS);
        where.append(" = ");
        where.append(Tickets.STATUS_WAITING);
        where.append(") AND ");
        where.append(Tickets.CITY_ID);
        where.append(" = ");
        where.append(city.id);

        Cursor c = context.getContentResolver().query(
            Tickets.CONTENT_URI,
            new String[]{Tickets._ID},
            where.toString(),
            null,
            null
        );

        int cnt = 0;

        try {
            cnt = c.getCount();
        } finally {
            c.close();
        }

        return cnt;
    }

    public City getCity(Context c, long id) {
        Cursor cursor = c.getContentResolver().query(ContentUris.withAppendedId(Cities.CONTENT_URI, id), null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return getCity(cursor);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    public City getClosest(Context c, double latitude, double longitude) {
        Cursor cursor = c.getContentResolver().query(
            Cities.CONTENT_URI,
            new String[]{
                Cities._ID,
                "(abs(" + Cities.LAT + " - " + String.format((Locale)null, "%.7f", latitude) + ") + abs(" + Cities.LON + " - " + String.format((Locale)null, "%.7f", longitude) + ")) as dif"
            },
            Cities.COUNTRY + "= ?",
            new String[]{LanguageUtil.getSimCountry(c)},
            "dif"
        );

        try {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndex(Cities._ID));
                return getCity(c, id);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    public Ticket getTicket(Cursor c) {
        final Ticket t = new Ticket();

        t.setId(c.getLong(c.getColumnIndex(Tickets._ID)));

        String od = c.getString(c.getColumnIndex(Tickets.ORDERED));
        if (od != null) {
            t.setOrdered(FormatUtil.timeFrom3339(od));
        }

        String vf = c.getString(c.getColumnIndex(Tickets.VALID_FROM));
        if (vf != null) {
            t.setValidFrom(FormatUtil.timeFrom3339(vf));
        }

        String vt = c.getString(c.getColumnIndex(Tickets.VALID_TO));
        if (vt != null) {
            t.setValidTo(FormatUtil.timeFrom3339(vt));
        }

        t.setHash(c.getString(c.getColumnIndex(Tickets.HASH)));
        t.setCityId(c.getInt(c.getColumnIndex(Tickets.CITY_ID)));
        t.setText(c.getString(c.getColumnIndex(Tickets.TEXT)));
        t.setCity(c.getString(c.getColumnIndex(Tickets.CITY)));
        t.setStatus(c.getInt(c.getColumnIndex(Tickets.STATUS)));

        return t;
    }

    public City getCity(Cursor c) {
        if (c.isClosed() || c.isAfterLast() || c.isBeforeFirst()) {
            return null;
        }

        return new City(
            c.getLong(c.getColumnIndex(Cities._ID)),
            c.getString(c.getColumnIndex(Cities.COUNTRY)),
            c.getString(c.getColumnIndex(Cities.CITY)),
            c.getString(c.getColumnIndex(Cities.CITY_PUBTRAN)),
            c.getDouble(c.getColumnIndex(Cities.LAT)),
            c.getDouble(c.getColumnIndex(Cities.LON)),
            c.getInt(c.getColumnIndex(Cities.VALIDITY)),
            c.getString(c.getColumnIndex(Cities.NOTE)),
            c.getString(c.getColumnIndex(Cities.PRICE)),
            c.getString(c.getColumnIndex(Cities.CURRENCY)),
            c.getString(c.getColumnIndex(Cities.PRICE_NOTE)),
            c.getString(c.getColumnIndex(Cities.REQUEST)),
            c.getString(c.getColumnIndex(Cities.NUMBER)),
            new String[]{c.getString(c.getColumnIndex(Cities.ADDITIONAL_NUMBER_1))},
            c.getString(c.getColumnIndex(Cities.IDENTIFICATION)),
            c.getString(c.getColumnIndex(Cities.P_DATE_FROM)),
            c.getString(c.getColumnIndex(Cities.P_DATE_TO)),
            c.getString(c.getColumnIndex(Cities.P_HASH)),
            c.getString(c.getColumnIndex(Cities.DATE_FORMAT)),
            c.getString(c.getColumnIndex(Cities.CONFIRM_REQ)),
            c.getString(c.getColumnIndex(Cities.CONFIRM)));
    }

    public List<City> getUniqueCities(Context c) {
        List<City> cities = new ArrayList<City>();
        List<String> cityNames = new ArrayList<String>();
        Cursor cursor = c.getContentResolver().query(Cities.CONTENT_URI, null, Cities.COUNTRY + "= ?",
            new String[]{LanguageUtil.getSimCountry(c)}, Cities.DEFAULT_SORT_ORDER);
        try {
            while (cursor.moveToNext()) {
                City city = getCity(cursor);
                if (!cityNames.contains(city.city)) {
                    cities.add(city);
                    cityNames.add(city.city);
                }
            }
        } finally {
            cursor.close();
        }
        return cities;
    }

    public City getCityByPubtranCity(Context c, String cityPubTran) {
        Cursor cursor = c.getContentResolver().query(Cities.CONTENT_URI, null, Cities.CITY_PUBTRAN + "= ?",
            new String[]{cityPubTran}, null);
        try {
            while (cursor.moveToNext()) {
                return getCity(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public List<City> getLastTwoUsed(Context c) {
        List<City> cities = new ArrayList<City>();
        List<String> cityNames = new ArrayList<String>();
        Cursor cursor = c.getContentResolver().query(Tickets.CONTENT_URI, null, null, null,
            Tickets.DEFAULT_SORT_ORDER);
        try {
            int citiesFound = 0;
            while (cursor.moveToNext()) {
                String cityName = cursor.getString(cursor.getColumnIndex(Tickets.CITY));
                if (!cityNames.contains(cityName)) {
                    City city = getCity(c, cursor.getLong(cursor.getColumnIndex(Tickets.CITY_ID)));
                    if (city != null && city.country != null && !city.country.equals(LanguageUtil.getSimCountry(c))) {
                        continue;
                    }
                    cities.add(city);
                    cityNames.add(cityName);
                    citiesFound++;
                    if (citiesFound == 2) {
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return cities;
    }

    public List<City> getTicketsInCity(String cityName, Context c) {
        List<City> cities = new ArrayList<City>();
        Cursor cursor = c.getContentResolver().query(Cities.CONTENT_URI, null, Cities.CITY + " = ?", new String[]{cityName}, Cities.VALIDITY);
        try {
            while (cursor.moveToNext()) {
                cities.add(getCity(cursor));
            }
        } finally {
            cursor.close();
        }
        return cities;
    }

    public List<Ticket> getAllTickets(Context c) {
        List<Ticket> tickets = new ArrayList<Ticket>();
        Cursor cursor = c.getContentResolver().query(Tickets.CONTENT_URI, null, null, null,
            null);
        try {
            while (cursor.moveToNext()) {
                tickets.add(getTicket(cursor));
            }
        } finally {
            cursor.close();
        }
        return tickets;
    }
}