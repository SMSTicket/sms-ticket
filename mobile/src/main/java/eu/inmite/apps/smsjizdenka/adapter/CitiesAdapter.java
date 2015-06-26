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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;

/**
 * Adapter for cities
 *
 * @author David Vávra (david@inmite.eu)
 */
public class CitiesAdapter extends ArrayAdapter<CitiesAdapter.Item> {

    Context c;
    List<Item> list;

    public CitiesAdapter(Context c, List<Item> list) {
        super(c, 0);
        this.c = c;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = list.get(position);
        if (convertView == null) {
            if (item.type == Item.TYPE_HEADER) {
                convertView = LayoutInflater.from(c).inflate(R.layout.item_list_header, parent, false);
            } else {
                convertView = LayoutInflater.from(c).inflate(R.layout.item_city, parent, false);
            }
            convertView.setTag(convertView.findViewById(R.id.list_item_text));
        }
        ((TextView)convertView.getTag()).setText(item.city);
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return list.get(position).type != Item.TYPE_HEADER;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Item getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type;
    }

    public static class Item {
        public static int TYPE_HEADER = 0;
        public static int TYPE_CITY = 1;
        public int type;
        public String city;

        public Item(int type, String city) {
            this.type = type;
            this.city = city;
        }
    }

}