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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cut down version of SmsParser from the app just for unit tests.
 * <p/>
 * TODO: Refactor the parser in the app so it can be used in tests and avoid this code duplication.
 */
public class SmsParser {

    private List<City> mCities;

    public SmsParser(String lang) {
        loadJson(lang);
    }

    private void loadJson(String lang) {
        InputStream is = null;
        try {
            is = ClassLoader.getSystemResourceAsStream("assets/tickets2.json");
            String json = readResult(is);

            JSONObject o = new JSONObject(json);
            JSONArray array = o.getJSONArray("tickets");

            mCities = new ArrayList<City>();
            for (int i = 0; i < array.length(); i++) {
                final JSONObject cityObject = array.getJSONObject(i);
                City city = new City();
                city.id = cityObject.getInt("id");
                city.city = getStringLocValue(cityObject, lang, "city");
                if (cityObject.has("city_pubtran")) {
                    city.cityPubtran = cityObject.getString("city_pubtran");
                }
                city.country = cityObject.getString("country");
                city.currency = cityObject.getString("currency");
                city.dateFormat = cityObject.getString("dateFormat");
                city.identification = cityObject.getString("identification");
                city.lat = cityObject.getDouble("lat");
                city.lon = cityObject.getDouble("lon");
                city.note = getStringLocValue(cityObject, lang, "note");
                city.number = cityObject.getString("number");
                city.pDateFrom = cityObject.getString("pDateFrom");
                city.pDateTo = cityObject.getString("pDateTo");
                city.pHash = cityObject.getString("pHash");
                city.price = cityObject.getDouble("price");
                city.priceNote = getStringLocValue(cityObject, lang, "priceNote");
                city.request = cityObject.getString("request");
                city.validity = cityObject.getInt("validity");
                if (cityObject.has("confirmReq")) {
                    city.confirmReq = cityObject.getString("confirmReq");
                }
                if (cityObject.has("confirm")) {
                    city.confirm = cityObject.getString("confirm");
                }

                final JSONArray additionalNumbers = cityObject.getJSONArray("additionalNumbers");
                city.additionalNumbers = new String[additionalNumbers.length()];
                for (int j = 0; j < additionalNumbers.length() && j < 3; j++) {
                    city.additionalNumbers[j] = additionalNumbers.getString(j);
                }
                mCities.add(city);
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException("JSON Parsing Error");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("JSON Parsing Error");
            }
        }
    }

    public Ticket parse(String number, String text) {
        List<City> resolvedCities = resolveCities(number, text);
        if (resolvedCities.size() == 0) {
            throw new RuntimeException("No city matched to this message");
        }
        return parseTicket(text, resolvedCities.get(0));
    }

    private Ticket parseTicket(String text, City city) {
        Ticket ticket = new Ticket();
        ticket.text = text;
        ticket.validFrom = parseDate(text, city.pDateFrom, city.getSdf(), city.getSdfTime(), ticket);
        ticket.validTo = parseDate(text, city.pDateTo, city.getSdf(), city.getSdfTime(), ticket);
        ticket.hash = parseHash(text, city.pHash);
        ticket.city = city.city;
        ticket.cityId = city.id;
        ticket.price = city.price;
        return ticket;
    }

    private Date parseDate(String text, String datePattern, SimpleDateFormat sdf, SimpleDateFormat sdfTime,
                           Ticket ticket) {
        Matcher m = Pattern.compile(datePattern).matcher(text);
        if (m.find()) {
            String d = m.group(1);
            if (!isEmpty(d)) {
                d = d.replaceAll(";", "");

                for (int i = 0; i < 2; i++) {
                    final Date date;
                    try {
                        if (i == 0) {
                            date = sdf.parse(d); // full date/time
                        } else if (i == 1 && sdfTime != null) {
                            date = sdfTime.parse(d); // only time
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    if (i == 1 && ticket != null && ticket.validFrom != null) {
                        final Date prevDate = ticket.validFrom;
                        date.setYear(prevDate.getYear());
                        date.setMonth(prevDate.getMonth());
                        date.setDate(prevDate.getDate());
                    }

                    return date;
                }
            }
        }

        throw new RuntimeException("Cannot parse date from the message " + text);
    }

    protected String parseHash(String text, String pHash) {
        Matcher m = Pattern.compile(pHash).matcher(text);
        if (m.find()) {
            return m.group(1);
        }

        throw new RuntimeException("Cannot parse hash from the message");
    }

    private boolean isEmpty(String text) {
        return text == null || text.trim().equals("");
    }

    private List<City> resolveCities(String number, String text) {
        List<City> resolvedCities = new ArrayList<City>();
        for (City city : mCities) {
            boolean matchesAdditionalNumber = false;
            for (String additionalNumber : city.additionalNumbers) {
                if (number.equals(additionalNumber)) {
                    matchesAdditionalNumber = true;
                }
            }
            if (city.number.equals(number) || matchesAdditionalNumber) {
                if (text.matches("^" + city.identification + ".*")) {
                    resolvedCities.add(city);
                }
            }
        }
        return resolvedCities;
    }

    private String readResult(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is, "utf-8"), 1000);
        String l = null;
        while ((l = r.readLine()) != null) {
            sb.append(l);
        }

        return sb.toString();
    }

    private String getStringLocValue(JSONObject json, String lang, String key) {
        String value = "";
        try {
            if (json.has(key + "_" + lang)) {
                value = json.getString(key + "_" + lang);
            } else {
                value = json.getString(key);
            }
        } catch (JSONException je) {
            throw new RuntimeException("JSON Parsing Error");
        }

        return value;
    }

}