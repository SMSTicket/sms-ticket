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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseActivity;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseFragment;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.data.model.City;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.data.model.Ticket;
import eu.inmite.apps.smsjizdenka.dialog.BuyTicketConfirmationDialogFragment;
import eu.inmite.apps.smsjizdenka.dialog.EulaDialogFragment;
import eu.inmite.apps.smsjizdenka.dialog.IncompatibleDialogFragment;
import eu.inmite.apps.smsjizdenka.dialog.MessageDialogFragment;
import eu.inmite.apps.smsjizdenka.dialog.SimDialogFragment;
import eu.inmite.apps.smsjizdenka.fragment.AboutFragment;
import eu.inmite.apps.smsjizdenka.fragment.SettingsFragment;
import eu.inmite.apps.smsjizdenka.fragment.StatisticsFragment;
import eu.inmite.apps.smsjizdenka.fragment.TicketsFragment;
import eu.inmite.apps.smsjizdenka.framework.App;
import eu.inmite.apps.smsjizdenka.service.SmsReceiverService;
import eu.inmite.apps.smsjizdenka.service.UpdateService;
import eu.inmite.apps.smsjizdenka.util.LanguageUtil;

/**
 * Main application activity.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class MainActivity extends ProjectBaseActivity {

    public static final String EXTRA_TICKET_ID = "ticket_id";
    public static final String EXTRA_SHOW_SMS = "show_sms";
    public static final String EXTRA_REALLY_BUY_CITY_ID = "really_buy_ticket_id";
    public static final String EXTRA_AREA = "area";
    public static final String EXTRA_MINUTES = "minutes";
    public static final String EXTRA_MESSAGE = "message";
    public static final int TAB_TICKETS = 0;
    public static final int TAB_STATISTICS = 1;
    public static final String TAG_TICKETS = "tickets";
    public static final String TAG_STATS = "stats";
    public eu.inmite.apps.smsjizdenka.core.ProjectBaseFragment mCurrentFragment;
    private int mSelectedTab = TAB_TICKETS;
    ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mSelectedTab = tab.getPosition();
            String selectedTag = (mSelectedTab == TAB_TICKETS) ? TAG_TICKETS : TAG_STATS;
            ProjectBaseFragment preInitializedFragment = (ProjectBaseFragment)getSupportFragmentManager()
                .findFragmentByTag(selectedTag);
            if (preInitializedFragment == null) {
                switch (mSelectedTab) {
                    case TAB_TICKETS:
                        mCurrentFragment = TicketsFragment.newInstance(getIntent().getLongExtra(EXTRA_TICKET_ID,
                            TicketsFragment.NONE), getIntent().getBooleanExtra(EXTRA_SHOW_SMS, false));
                        break;
                    case TAB_STATISTICS:
                        mCurrentFragment = new StatisticsFragment();
                        break;
                }
                ft.add(android.R.id.content, mCurrentFragment, selectedTag);
            } else {
                mCurrentFragment = preInitializedFragment;
                ft.attach(preInitializedFragment);
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            String selectedTag = (tab.getPosition() == TAB_TICKETS) ? TAG_TICKETS : TAG_STATS;
            ProjectBaseFragment preInitializedFragment = (ProjectBaseFragment)getSupportFragmentManager()
                .findFragmentByTag(selectedTag);
            if (preInitializedFragment != null) {
                ft.detach(preInitializedFragment);
            } else if (mCurrentFragment != null) {
                ft.detach(mCurrentFragment);
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // ignore
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup actionbar tabs
        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.addTab(bar.newTab().setText(getString(R.string.tab_tickets)).setTabListener(mTabListener));
        bar.addTab(bar.newTab().setText(getString(R.string.tab_statistics)).setTabListener(mTabListener));
        if (savedInstanceState != null) {
            mSelectedTab = savedInstanceState.getInt("selectedTab", TAB_TICKETS);
        }
        bar.setSelectedNavigationItem(mSelectedTab);
        handleIntent(getIntent());
        UpdateService.call(this, false);
        checkForHigherPriorityReceivers();
        checkForSim();
        checkForEula();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        if (mCurrentFragment != null && getSupportActionBar().getSelectedNavigationIndex() == TAB_TICKETS) {
            ((TicketsFragment)mCurrentFragment).collapseUncollapseTicket(intent.getLongExtra(EXTRA_TICKET_ID,
                TicketsFragment.NONE), intent.getBooleanExtra(EXTRA_SHOW_SMS,
                false));
        } else {
            getSupportActionBar().setSelectedNavigationItem(TAB_TICKETS);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (App.isReleaseBuild()) {
            menu.removeItem(R.id.menu_add_testing_ticket);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                DetailActivity.call(this, SettingsFragment.class, null);
                return true;
            case R.id.menu_about:
                DetailActivity.call(this, AboutFragment.class, null);
                return true;
            case R.id.menu_add_testing_ticket:
                addTestingTicket();
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("selectedTab", mSelectedTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // called only when language was changed
        UpdateService.call(this, false);
        super.onConfigurationChanged(newConfig);
    }

    private void handleIntent(Intent intent) {
        //React to notification when you really want to buy another ticket in a short time.
        if (intent.hasExtra(EXTRA_REALLY_BUY_CITY_ID)) {
            long cityId = intent.getLongExtra(EXTRA_REALLY_BUY_CITY_ID, -1);
            // delete extras so no intent can be catched again
            intent.removeExtra(EXTRA_REALLY_BUY_CITY_ID);
            getIntent().removeExtra(EXTRA_REALLY_BUY_CITY_ID);
            BuyTicketConfirmationDialogFragment.newInstance(cityId).show(getSupportFragmentManager(),
                BuyTicketConfirmationDialogFragment.TAG);
        }
        // pubtran integration
        if (intent.hasExtra(EXTRA_AREA)) {
            City city = CityManager.get(c).getCityByPubtranCity(c, intent.getStringExtra(EXTRA_AREA));
            if (city != null) {
                int minutes = intent.getIntExtra(EXTRA_MINUTES, 0);
                //SL.get(AnalyticsService.class).trackEvent("pubtran", "on-start", "city", city.city, "minutes", String.valueOf(minutes));
                Intent i = new Intent(c, CityTicketsActivity.class);
                i.putExtra(CityTicketsActivity.EXTRA_CITY, city.city);
                i.putExtra(CityTicketsActivity.EXTRA_MINUTES, intent.getIntExtra(EXTRA_MINUTES, 0));
                startActivity(i);
            }
        }
        // message from inmite
        if (intent.hasExtra(EXTRA_MESSAGE) && !Preferences.getBoolean(c, Preferences.MESSAGE_READ, false)) {
            MessageDialogFragment.newInstance(intent.getStringExtra(EXTRA_MESSAGE)).show(getSupportFragmentManager(),
                MessageDialogFragment.TAG);
            Preferences.set(c, Preferences.MESSAGE_READ, true);
        }
    }

    private void addTestingTicket() {
        Ticket ticket = new Ticket();
        if (Locale.getDefault().toString().startsWith("en")) {
            ticket.setCity("Prague");
        } else {
            ticket.setCity("Praha");
        }
        ticket.setCityId(1);
        ticket.setHash("bhAJpWP9B / 861418");
        ticket.setStatus(TicketProvider.Tickets.STATUS_DELIVERED);
        ticket.setText("DP hl.m.Prahy, a.s., Jizdenka prestupni 32,- Kc, Platnost od: 29.8.11 8:09  do: 29.8.11 9:39. Pouze v pasmu P. WzL9n3JuQ /" +
            " " +
            "169605");
        int second = 1000;
        int minute = 60 * second;
        int hour = 60 * minute;
        int day = 24 * hour;
        Time time = new Time();
        time.setToNow();
        ticket.setOrdered(time);
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.SECOND, 0);
        long nowFullMinute = calendar.getTimeInMillis();
        time.set(nowFullMinute);
        ticket.setValidFrom(time);
        Time time2 = new Time();
        time2.set(nowFullMinute + 12 * minute);
        ticket.setValidTo(time2);
        SmsReceiverService.call(c, ticket);
    }

    private void checkForHigherPriorityReceivers() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && Preferences.getBoolean(c, Preferences.PRIORITY_DIALOG_ENABLED, true)) {
            final Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
            final List<ResolveInfo> activities = getPackageManager().queryBroadcastReceivers(intent, 0);
            for (ResolveInfo resolveInfo : activities) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if (activityInfo != null) {
                    // skip avast
                    if (activityInfo.name.startsWith("com.avast.android")) {
                        continue;
                    }
                    if (!activityInfo.name.startsWith("eu.inmite.apps.smsjizdenka")) {
                        String appName = activityInfo.loadLabel(getPackageManager()).toString();
                        //SL.get(AnalyticsService.class).trackEvent("higher-priority-receiver", "on-start", "app-name", appName);
                        IncompatibleDialogFragment.newInstance(appName).show
                            (getSupportFragmentManager(),
                                MessageDialogFragment.TAG);
                    }
                    break;
                }
            }
            Preferences.set(c, Preferences.PRIORITY_DIALOG_ENABLED, false);
        }
    }

    private void checkForSim() {
        if (!"cz".equals(LanguageUtil.getSimCountry(c)) && !"sk".equals(LanguageUtil.getSimCountry(c))) {
            //SL.get(AnalyticsService.class).trackEvent("wrong-sim", "on-start", "sim-language", LanguageUtil.getSimCountry(c));
            new SimDialogFragment().show(getSupportFragmentManager(), SimDialogFragment.TAG);
        }
    }

    private void checkForEula() {
        if (!Preferences.getBoolean(c, Preferences.EULA_CONFIRMED, false)) {
            EulaDialogFragment.show(this, null, 0, false);
        }
    }


}
