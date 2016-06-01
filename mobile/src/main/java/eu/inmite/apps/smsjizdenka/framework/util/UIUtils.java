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

package eu.inmite.apps.smsjizdenka.framework.util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {

    private UIUtils() {}

    public static boolean isHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Return true for device with LARGE screen.
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK)
            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return isHoneycomb() && isTablet(context);
    }

    public static String getScreenSizeAsString(Context context) {
        String size = null;
        int screenLayout = context.getResources().getConfiguration().screenLayout;
        if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            size = "large";
        }
        if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            size = "xlarge";
        }
        if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            size = "normal";
        }
        if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            size = "small";
        }
        if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
            size = "undefined";
        }
        return size;
    }

    public static String getScreenDensityAsString(Context context) {
        String size = null;
        int density = context.getResources().getDisplayMetrics().densityDpi;
        if (density == DisplayMetrics.DENSITY_LOW) {
            size = "ldpi";
        }
        if (density == DisplayMetrics.DENSITY_MEDIUM) {
            size = "mdpi";
        }
        if (density == DisplayMetrics.DENSITY_TV) {
            size = "tvdpi";
        }
        if (density == DisplayMetrics.DENSITY_HIGH) {
            size = "hdpi";
        }
        if (density == DisplayMetrics.DENSITY_XHIGH) {
            size = "xhdpi";
        }
        if (density == DisplayMetrics.DENSITY_XXHIGH) {
            size = "xxhdpi";
        }
        return size;
    }

}