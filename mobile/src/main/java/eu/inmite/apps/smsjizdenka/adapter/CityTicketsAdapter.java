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

package eu.inmite.apps.smsjizdenka.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;

/**
 * Adapter for tickets in a city.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class CityTicketsAdapter extends ArrayAdapter<City> {

    Context c;
    List<City> list;

    public CityTicketsAdapter(Context c, List<City> list) {
        super(c, 0);
        this.c = c;
        this.list = list;
    }

    /**
     * It's reused in the dialog.
     */
    public static View setupCityView(City city, View convertView, ViewGroup parent, Context c) {
        if (convertView == null) {
            convertView = LayoutInflater.from(c).inflate(R.layout.item_city_ticket, parent, false);
            final ViewHolder h = new ViewHolder();
            h.vValidity = (TextView)convertView.findViewById(R.id.validity);
            h.vPrice = (TextView)convertView.findViewById(R.id.price);
            h.vPriceNote = (TextView)convertView.findViewById(R.id.price_note);
            h.vNote = (TextView)convertView.findViewById(R.id.note);
            convertView.setTag(h);
        }
        ViewHolder h = (ViewHolder)convertView.getTag();
        h.vValidity.setText(FormatUtil.formatValidity(city.validity, c));
        h.vPrice.setText(FormatUtil.formatCurrency(Double.parseDouble(city.price), city.currency));
        if (TextUtils.isEmpty(city.priceNote)) {
            h.vPriceNote.setVisibility(View.GONE);
        } else {
            h.vPriceNote.setVisibility(View.VISIBLE);
            h.vPriceNote.setText(deEscape(city.priceNote));
        }
        if (TextUtils.isEmpty(city.note)) {
            h.vNote.setVisibility(View.GONE);
        } else {
            h.vNote.setVisibility(View.VISIBLE);
            h.vNote.setText(deEscape(city.note));
        }
        return convertView;
    }

    private static String deEscape(String string) {
        return string.replace("\\'", "'");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        City item = list.get(position);
        return setupCityView(item, convertView, parent, c);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public City getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).id;
    }

    private static class ViewHolder {
        TextView vValidity;
        TextView vPrice;
        TextView vPriceNote;
        TextView vNote;
    }
}