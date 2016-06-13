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

package eu.inmite.apps.smsjizdenka.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseActivity;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.framework.fragment.PreferenceListFragment;
import eu.inmite.apps.smsjizdenka.service.UpdateService;

/**
 * Fragment for app settings.
 *
 * @author Michal Mátl (matl)
 */
public class SettingsFragment extends PreferenceListFragment {

    private Context mContext;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences
        .OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Preferences.NOTIFICATION_RINGTONE)) {
                updateRingtoneSummary(getPreferenceScreen());
            } else if (key.equals(Preferences.DATA_VERSION)) {
                updateDataVersion(getPreferenceScreen());
            } else if (key.equals(Preferences.DUALSIM_SIM)) {
                ListPreference preference = (ListPreference) getPreferenceScreen().findPreference(Preferences.DUALSIM_SIM);
                preference.setSummary(preference.getEntry());
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ProjectBaseActivity) getActivity()).getSupportActionBar().setTitle(R.string.ab_menu_settings);
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPreferenceAttached(PreferenceScreen preferenceScreen, int i) {

        findPreference("update_cities").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(mContext, R.string.pref_downloading_definitions, Toast.LENGTH_SHORT).show();
                UpdateService.call(mContext, true);
                return true;
            }
        });
        preferenceScreen.findPreference("restore_archived").setOnPreferenceClickListener(new Preference
            .OnPreferenceClickListener
            () {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restoreArchived();
                return true;
            }
        });

        updateRingtoneSummary(preferenceScreen);
        updateDataVersion(preferenceScreen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PreferenceCategory pref = (PreferenceCategory) preferenceScreen.findPreference("sms_category");
            pref.removePreference(pref.findPreference(Preferences.KEEP_IN_MESSAGING));
        }
        boolean dualSim = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && SubscriptionManager.from(getActivity()).getActiveSubscriptionInfoCount() >= 2;
        if (!dualSim) {
            PreferenceCategory pref = (PreferenceCategory) preferenceScreen.findPreference("sms_category");
            pref.removePreference(pref.findPreference(Preferences.DUALSIM_SIM));
        } else {
            fillDualSimList(preferenceScreen);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void fillDualSimList(PreferenceScreen preferenceScreen) {
        PreferenceCategory category = (PreferenceCategory) preferenceScreen.findPreference("sms_category");
        ListPreference preference = (ListPreference) category.findPreference(Preferences.DUALSIM_SIM);
        List<String> simIds = new ArrayList<>();
        List<String> simNames = new ArrayList<>();
        simIds.add(String.valueOf(Preferences.VALUE_DEFAULT_SIM));
        simNames.add(getString(R.string.sim_default));
        SubscriptionManager subscriptionManager = SubscriptionManager.from(getActivity());
        for (SubscriptionInfo subscriptionInfo : subscriptionManager.getActiveSubscriptionInfoList()) {
            simIds.add(String.valueOf(subscriptionInfo.getSubscriptionId()));
            simNames.add(getString(R.string.sim_name, subscriptionInfo.getSimSlotIndex() + 1, subscriptionInfo
                .getDisplayName()));
        }
        preference.setEntries(simNames.toArray(new String[simNames.size()]));
        preference.setEntryValues(simIds.toArray(new String[simIds.size()]));
        preference.setDefaultValue(String.valueOf(Preferences.VALUE_DEFAULT_SIM));
        preference.setSummary(preference.getEntry());
    }

    private void updateRingtoneSummary(PreferenceScreen preferenceScreen) {
        String name = getString(R.string.pref_ringtone_none);
        String value = Preferences.getString(mContext, Preferences.NOTIFICATION_RINGTONE, null);
        if (!TextUtils.isEmpty(value)) {
            Uri ringtoneUri = Uri.parse(value);
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, ringtoneUri);
            if (ringtone != null) {
                name = ringtone.getTitle(mContext);
            }
        }
        preferenceScreen.findPreference(Preferences.NOTIFICATION_RINGTONE).setSummary(name);
        preferenceScreen.findPreference(Preferences.NOTIFICATION_RINGTONE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String v = (String) newValue;
                preference.setSummary(v);
                return true;
            }
        });
    }

    private void updateDataVersion(final PreferenceScreen preferenceScreen) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                preferenceScreen.findPreference("update_cities").setTitle(mContext.getString(R.string
                        .pref_cities_update,
                    Preferences.getInt(mContext,
                        Preferences.DATA_VERSION, -1)));
            }
        });
    }

    private void restoreArchived() {
        ContentValues values = new ContentValues();
        values.put(TicketProvider.Tickets.STATUS, TicketProvider.Tickets.STATUS_DELIVERED);
        mContext.getContentResolver().update(TicketProvider.Tickets.CONTENT_URI,
            values,
            TicketProvider.Tickets.STATUS + " = ?", new String[]{String.valueOf(TicketProvider.Tickets
                .STATUS_DELETED)});
        Toast.makeText(mContext, R.string.pref_archived_tickets_restored, Toast.LENGTH_SHORT).show();
    }
}
