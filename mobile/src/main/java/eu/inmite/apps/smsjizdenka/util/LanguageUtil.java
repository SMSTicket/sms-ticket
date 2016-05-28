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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * Helps with languages.
 */
public class LanguageUtil {

    private static String cachedCountry;

    private LanguageUtil() {}

    public static String getSimCountry(Context c) {
        if (cachedCountry != null) {
            return cachedCountry;
        } else {
            String country = ((TelephonyManager)c.getSystemService(Activity.TELEPHONY_SERVICE)).getSimCountryIso();
            if (TextUtils.isEmpty(country)) {
                country = "cz";
            }
            // running on emulator which always returns "us"
            if (Build.MODEL.equals("Android SDK built for x86") || Build.MODEL.equals("google_sdk") || Build.MODEL.equals("sdk")) {
                country = "cz";
            }
            cachedCountry = country;
            return country;
        }
    }
}
