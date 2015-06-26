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

package eu.inmite.apps.smsjizdenka.activity;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.service.UpdateService;

/**
 * Activity with user settings.
 */
public class SettingsActivity extends PreferenceActivity {

    SettingsActivity c = this;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences
        .OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Preferences.NOTIFICATION_RINGTONE)) {
                updateRingtoneSummary();
            } else if (key.equals(Preferences.DATA_VERSION)) {
                updateDataVersion();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        findPreference("update_cities").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(c, R.string.pref_downloading_definitions, Toast.LENGTH_SHORT).show();
                UpdateService.call(c, true);
                return true;
            }
        });
        findPreference("restore_archived").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restoreArchived();
                return true;
            }
        });
        PreferenceManager.getDefaultSharedPreferences(c).registerOnSharedPreferenceChangeListener(listener);
        updateRingtoneSummary();
        updateDataVersion();
        //SL.get(AnalyticsService.class).trackScreen("settings");
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(c).unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateRingtoneSummary() {
        String name = c.getString(R.string.pref_ringtone_none);
        String value = Preferences.getString(c, Preferences.NOTIFICATION_RINGTONE, null);
        //SL.get(AnalyticsService.class).trackEvent("update-ringtone", "settings", "ringtone", value);
        if (!TextUtils.isEmpty(value)) {
            Uri ringtoneUri = Uri.parse(value);
            Ringtone ringtone = RingtoneManager.getRingtone(c, ringtoneUri);
            if (ringtone != null) {
                name = ringtone.getTitle(c);
            }
        }
        findPreference(Preferences.NOTIFICATION_RINGTONE).setSummary(name);
    }

    private void updateDataVersion() {
        //SL.get(AnalyticsService.class).trackEvent("update-data", "settings");
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findPreference("update_cities").setTitle(c.getString(R.string.pref_cities_update, Preferences.getInt(c,
                    Preferences.DATA_VERSION, -1)));
            }
        });
    }

    private void restoreArchived() {
        //SL.get(AnalyticsService.class).trackEvent("restore-archived", "settings");
        ContentValues values = new ContentValues();
        values.put(TicketProvider.Tickets.STATUS, TicketProvider.Tickets.STATUS_DELIVERED);
        c.getContentResolver().update(TicketProvider.Tickets.CONTENT_URI,
            values,
            TicketProvider.Tickets.STATUS + " = ?", new String[]{String.valueOf(TicketProvider.Tickets
                .STATUS_DELETED)});
        Toast.makeText(c, R.string.pref_archived_tickets_restored, Toast.LENGTH_SHORT).show();
    }
}
