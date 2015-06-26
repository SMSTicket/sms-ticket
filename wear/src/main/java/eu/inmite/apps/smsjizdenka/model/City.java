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

package eu.inmite.apps.smsjizdenka.model;

import java.io.Serializable;

/**
 * @author Michal Matl (matl)
 */
public class City implements Serializable {

    public long id;
    public String country;
    public String city;
    public int validity;
    public String note;
    public String price;
    public String currency;
    public String priceNote;

    public City(long id, String country, String city) {
        this.id = id;
        this.country = country;
        this.city = city;
    }

    public City(long id, String country, String city, int validity, String note, String price, String currency, String priceNote) {
        this.id = id;
        this.country = country;
        this.city = city;
        this.validity = validity;
        this.note = note;
        this.price = price;
        this.currency = currency;
        this.priceNote = priceNote;
    }
}
