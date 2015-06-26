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

import android.support.wearable.view.GridPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Needed because of a bug in support library:
 * http://stackoverflow.com/questions/24752716/nullpointerexception-while-using-gridviewpager-class-on-android-wear
 *
 * @author David Vávra (david@vavra.me)
 */
public class EmptyGridPagerAdapter extends GridPagerAdapter {
    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return 1;
    }

    @Override
    protected Object instantiateItem(ViewGroup viewGroup, int i, int i2) {
        return null;
    }

    @Override
    protected void destroyItem(ViewGroup viewGroup, int i, int i2, Object o) {

    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return false;
    }
}