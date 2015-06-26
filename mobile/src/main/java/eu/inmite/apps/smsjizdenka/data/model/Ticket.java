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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

/**
 * Domain object for issued sms ticket.
 *
 * @author ondra
 */
public class Ticket implements Parcelable {

    @SuppressWarnings({"rawtypes"})
    public static final Creator CREATOR = new Creator() {
        public Ticket createFromParcel(Parcel in) {
            return new Ticket(in);
        }

        public Ticket[] newArray(int size) {
            return new Ticket[size];
        }
    };
    private long id;
    private long cityId;
    private Time ordered;
    private Time validFrom;
    private Time validTo;
    private String hash;
    private String text;
    private String city;
    private int status;
    private int notificationId;

    public Ticket() {
        super();
    }

    private Ticket(Parcel p) {
        id = p.readLong();

        Time t = new Time();
        t.parse3339(p.readString());
        validFrom = t;

        t = new Time();
        t.parse3339(p.readString());
        validTo = t;

        hash = p.readString();
        cityId = p.readLong();
        text = p.readString();
        city = p.readString();
        status = p.readInt();
        notificationId = p.readInt();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("id:");
        sb.append(id);
        sb.append(", ");

        sb.append("cityId:");
        sb.append(cityId);
        sb.append(", ");

        sb.append("from:");
        sb.append(validFrom);
        sb.append(", ");

        sb.append("to:");
        sb.append(validTo);
        sb.append(", ");

        sb.append("hash:");
        sb.append(hash);
        sb.append(", ");

        sb.append("city:");
        sb.append(city);
        sb.append(", ");

        sb.append("status:");
        sb.append(status);

        sb.append("}");

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeLong(id);
        if (validFrom != null) {
            p.writeString(validFrom.format3339(false));
        } else {
            p.writeString(null);
        }
        if (validTo != null) {
            p.writeString(validTo.format3339(false));
        } else {
            p.writeString(null);
        }
        p.writeString(hash);
        p.writeLong(cityId);
        p.writeString(text);
        p.writeString(city);
        p.writeInt(status);
        p.writeInt(notificationId);
    }

    public long getId() {
        return id;
    }

    public void setId(long _id) {
        this.id = _id;
    }

    public Time getOrdered() {
        return ordered;
    }

    public void setOrdered(Time ordered) {
        this.ordered = ordered;
    }

    public Time getValidFrom() {
        if (validFrom != null) {
            validFrom.switchTimezone(Time.getCurrentTimezone());
        }

        return validFrom;
    }

    public void setValidFrom(Time validFrom) {
        this.validFrom = validFrom;
    }

    public Time getValidTo() {
        if (validTo != null) {
            validTo.switchTimezone(Time.getCurrentTimezone());
        }

        return validTo;
    }

    public void setValidTo(Time validTo) {
        this.validTo = validTo;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getCityId() {
        return cityId;
    }

    public void setCityId(long cityId) {
        this.cityId = cityId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(id ^ (id >>> 32));
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ticket other = (Ticket)obj;
        if (id != other.id)
            return false;
        return true;
    }
}