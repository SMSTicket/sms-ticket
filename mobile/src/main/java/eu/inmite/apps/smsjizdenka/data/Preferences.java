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
package eu.inmite.apps.smsjizdenka.data;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Preferences helper for this app.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class Preferences {

    public static final String LAST_ORDER_TIME = "last_order_time";
    public static final String DATA_VERSION = "data_version";
    public static final String DATA_LANGUAGE = "data_language";
    public static final String NOTIFICATION_VIBRATE = "notification_vibrate";
    public static final String NOTIFICATION_RINGTONE = "notification_ringtone";
    public static final String NOTIFY_BEFORE_EXPIRATION = "notify_before_expiration";
    public static final String KEEP_NOTIFICATIONS = "keep_notifications";
    public static final String KEEP_IN_MESSAGING = "keep_in_messaging";
    public static final String PRIORITY_DIALOG_ENABLED = "priority_dialog_enabled";
    public static final String PREFILL_SMS = "prefill_sms";
    public static final String EULA_CONFIRMED = "eula";
    public static final String MESSAGE_READ = "message_read";
    public static final String DUALSIM_SIM = "sim";

    public static final int VALUE_DEFAULT_SIM = -1;

    private Preferences() {}

    public static int getInt(Context c, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(key, defaultValue);
    }

    public static long getLong(Context c, String key, long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(c).getLong(key, defaultValue);
    }

    public static String getString(Context c, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(key, defaultValue);
    }

    public static boolean getBoolean(Context c, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(key, defaultValue);
    }

    public static void set(Context c, String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putInt(key, value).commit();
    }

    public static void set(Context c, String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putLong(key, value).commit();
    }

    public static void set(Context c, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(key, value).commit();
    }

    public static void set(Context c, String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(key, value).commit();
    }
}
