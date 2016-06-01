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

import android.util.Log;

/**
 * Simple debugging utils.
 *
 * @author David Vávra (david@vavra.me)
 */
public class DebugLog {
    private static final String DEFAULT_TAG = "SmsTicket";
    private static long startTime;
    private static long previousTime;

    private DebugLog() {}

    public static void d(Object text) {
        Log.d(DEFAULT_TAG, text.toString());
    }

    public static void e(Object text) {
        Log.e(DEFAULT_TAG, text.toString());
    }

    public static void i(Object text) {
        Log.i(DEFAULT_TAG, text.toString());
    }

    public static void t(String text) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            previousTime = startTime;
            d("------------------------------ starting time tracking: " + text);
        } else {
            long currentTime = System.currentTimeMillis();
            d(text + ": +" + (currentTime - previousTime) + " ms (total " + (currentTime - startTime) + " ms)");
            previousTime = currentTime;
        }
    }

    public static void w(Object text) {
        Log.w(DEFAULT_TAG, text.toString());
    }
}