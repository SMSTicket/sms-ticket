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

package eu.inmite.apps.smsjizdenka.core;

import java.util.HashMap;
import java.util.Map;

import eu.inmite.apps.smsjizdenka.R;

/**
 * @author Michal Matl (matl)
 */
public class Constants {

    public static final Map<Long, Integer> CITIES_BACKGROUND_MAP;

    static {
        CITIES_BACKGROUND_MAP = new HashMap<Long, Integer>();
        CITIES_BACKGROUND_MAP.put(27L, R.drawable.city_brno);
        CITIES_BACKGROUND_MAP.put(28L, R.drawable.city_brno);
        CITIES_BACKGROUND_MAP.put(31L, R.drawable.city_brno);
        CITIES_BACKGROUND_MAP.put(17L, R.drawable.city_hradeckralove);
        CITIES_BACKGROUND_MAP.put(18L, R.drawable.city_hradeckralove);
        CITIES_BACKGROUND_MAP.put(23L, R.drawable.city_karlovyvary);
        CITIES_BACKGROUND_MAP.put(24L, R.drawable.city_karlovyvary);
        CITIES_BACKGROUND_MAP.put(10L, R.drawable.city_liberec);
        CITIES_BACKGROUND_MAP.put(20L, R.drawable.city_olomouc);
        CITIES_BACKGROUND_MAP.put(6L, R.drawable.city_ostrava);
        CITIES_BACKGROUND_MAP.put(7L, R.drawable.city_ostrava);
        CITIES_BACKGROUND_MAP.put(25L, R.drawable.city_ostrava);
        CITIES_BACKGROUND_MAP.put(26L, R.drawable.city_ostrava);
        CITIES_BACKGROUND_MAP.put(21L, R.drawable.city_pardubice);
        CITIES_BACKGROUND_MAP.put(22L, R.drawable.city_pardubice);
        CITIES_BACKGROUND_MAP.put(11L, R.drawable.city_plzen);
        CITIES_BACKGROUND_MAP.put(19L, R.drawable.city_plzen);
        CITIES_BACKGROUND_MAP.put(29L, R.drawable.city_plzen);
        CITIES_BACKGROUND_MAP.put(30L, R.drawable.city_plzen);
        CITIES_BACKGROUND_MAP.put(1L, R.drawable.city_praha);
        CITIES_BACKGROUND_MAP.put(2L, R.drawable.city_praha);
        CITIES_BACKGROUND_MAP.put(3L, R.drawable.city_praha);
        CITIES_BACKGROUND_MAP.put(4L, R.drawable.city_praha);
        CITIES_BACKGROUND_MAP.put(34L, R.drawable.city_zlin);
        CITIES_BACKGROUND_MAP.put(35L, R.drawable.city_zlin);
        CITIES_BACKGROUND_MAP.put(36L, R.drawable.city_zlin);
        CITIES_BACKGROUND_MAP.put(5L, R.drawable.city_ustinadlabem);
        CITIES_BACKGROUND_MAP.put(32L, R.drawable.city_ustinadlabem);
        CITIES_BACKGROUND_MAP.put(33L, R.drawable.city_ustinadlabem);
        CITIES_BACKGROUND_MAP.put(8L, R.drawable.city_ceskebudejovice);
        CITIES_BACKGROUND_MAP.put(9L, R.drawable.city_ceskebudejovice);
    }

    private Constants() {}
}
