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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.util.CannotParseException;

/**
 * TODO: Model has important flaws: it should be refactored including json file with data.
 * Right now there are two entities - Cities and Tickets. But if there are more tickets in one city,
 * the data multiplies. That's bad practice, it complicates the code and creates confusion.
 * There should be three entities: Cities, Tickets in those cities and concrete instances of tickets in cities bought
 * by the user.
 */
public class City {

    private static final SimpleDateFormat format3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final SimpleDateFormat timezone = new SimpleDateFormat("Z");

    static {
        format3339.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"), new Locale("cs", "CZ")));
        timezone.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"), new Locale("cs", "CZ")));
    }

    public long id;
    public String country;
    public String city;
    public String cityPubtran;
    public double lat;
    public double lon;
    public int validity;
    public String note;
    public String price;
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
    SimpleDateFormat sdf;
    SimpleDateFormat sdfTime;

    public City(
        long id,
        String country,
        String city,
        String cityPubtran,
        double lat,
        double lon,
        int validity,
        String note,
        String price,
        String currency,
        String priceNote,
        String request,
        String number,
        String[] additionalNumbers,
        String identification,
        String pDateFrom,
        String pDateTo,
        String pHash,
        String dateFormat,
        String confirmReq,
        String confirm) {
        super();
        this.country = country;
        this.id = id;
        this.city = city;
        this.cityPubtran = cityPubtran;
        this.lat = lat;
        this.lon = lon;
        this.validity = validity;
        this.note = note;
        this.price = price;
        this.currency = currency;
        this.priceNote = priceNote;
        this.request = request;
        this.number = number;
        this.additionalNumbers = additionalNumbers;
        this.identification = identification;
        this.pDateFrom = pDateFrom;
        this.pDateTo = pDateTo;
        this.pHash = pHash;
        this.confirmReq = confirmReq;
        this.confirm = confirm;

        this.sdf = new SimpleDateFormat(dateFormat);
        sdf.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"), new Locale("cs", "CZ")));

        String[] dateFormatParts = dateFormat.split(" ");
        if (dateFormatParts.length > 1) {
            this.sdfTime = new SimpleDateFormat(dateFormatParts[1]);
            sdfTime.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"), new Locale("cs", "CZ")));
        }
    }

    @Override
    public String toString() {
        return "City{" +
            "id=" + id +
            ", country='" + country + '\'' +
            ", city='" + city + '\'' +
            ", cityPubtran='" + cityPubtran + '\'' +
            ", lat=" + lat +
            ", lon=" + lon +
            ", validity=" + validity +
            ", note='" + note + '\'' +
            ", price='" + price + '\'' +
            ", currency='" + currency + '\'' +
            ", priceNote='" + priceNote + '\'' +
            ", request='" + request + '\'' +
            ", number='" + number + '\'' +
            ", additionalNumbers=" + Arrays.toString(additionalNumbers) +
            ", identification='" + identification + '\'' +
            ", pDateFrom='" + pDateFrom + '\'' +
            ", pDateTo='" + pDateTo + '\'' +
            ", pHash='" + pHash + '\'' +
            ", confirmReq='" + confirmReq + '\'' +
            ", confirm='" + confirm + '\'' +
            ", sdf=" + sdf +
            ", sdfTime=" + sdfTime +
            '}';
    }

    public Ticket parseMessage(String message) throws ParseException {
        Ticket t = new Ticket();

        t.setText(message);
        t.setValidFrom(parseDate(message, pDateFrom, t));
        t.setValidTo(parseDate(message, pDateTo, t));
        t.setHash(parseHash(message));
        t.setCityId(id);
        t.setCity(city);
        t.setStatus(TicketProvider.Tickets.STATUS_DELIVERED);

        return t;
    }

    public boolean parseConfirm(Context context, String message, City city) throws ParseException {
        if (confirmReq == null || confirmReq.length() == 0) {
            return false;
        }

        final Matcher m = Pattern.compile(confirmReq).matcher(message);
        final int cnt = CityManager.get(context).getAvaitingTickets(context, city);

        if (m.find() && cnt > 0) {
            return true;
        }

        return false;
    }

    protected Time parseDate(String sms, String datePattern, Ticket ticket) throws ParseException {
        Matcher m = Pattern.compile(datePattern).matcher(sms);
        if (m.find()) {
            String d = m.group(1);
            if (!TextUtils.isEmpty(d)) {
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

                    if (i == 1 && ticket != null && ticket.getValidFrom() != null) {
                        final Date prevDate = new Date(ticket.getValidFrom().toMillis(true));
                        date.setYear(prevDate.getYear());
                        date.setMonth(prevDate.getMonth());
                        date.setDate(prevDate.getDate());
                    }

                    final Calendar c = Calendar.getInstance();
                    c.setTime(date);

                    final Time t = new Time();
                    synchronized (format3339) {
                        String zone = timezone.format(date);
                        zone = zone.substring(0, 3) + ":" + zone.substring(3);
                        String s = format3339.format(date) + zone;
                        t.parse3339(s);
                        t.switchTimezone(Time.getCurrentTimezone());
                    }

                    return t;
                }
            }
        }

        throw new ParseException("Cannot parse date from the message: " + sms, 0);
    }

    protected String parseHash(String sms) {
        Matcher m = Pattern.compile(pHash).matcher(sms);
        if (m.find()) {
            return m.group(1);
        }

        throw new CannotParseException("Cannot parse hash from the message: " + sms);
    }

    public boolean acceptMessage(String message) {
        return message.startsWith(identification);
    }

    public double getDistance(double lat, double lon) {
        return Math.sqrt((this.lon - lon) * (this.lon - lon) + (this.lat - lat) * (this.lat - lat));
    }
}