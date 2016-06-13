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

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.framework.SL;
import eu.inmite.apps.smsjizdenka.framework.activity.BaseSinglePaneActivity;
import eu.inmite.apps.smsjizdenka.framework.interfaces.IRefreshable;
import eu.inmite.apps.smsjizdenka.framework.services.GlobalHandlerService;
import eu.inmite.apps.smsjizdenka.framework.util.UIUtils;

/**
 * Parent fot all activities in project, put your project related customisation here.
 *
 * @author David Vávra (david@inmite.eu)
 */
public abstract class ProjectBaseActivity extends BaseSinglePaneActivity implements Callback {

    public ProjectBaseActivity c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugLog.v("ProjectBaseActivity.onCreate()");
        super.onCreate(savedInstanceState);

        printDebugInfo();

        // init handler that will enable progress indicator
        GlobalHandlerService gh = SL.get(getApplicationContext(), GlobalHandlerService.class);
        gh.addListener(this);
    }


    private void printDebugInfo() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        DebugLog.i("Device density: " + displaymetrics.densityDpi);
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            DebugLog.i("Device size is: LARGE");
        }
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            DebugLog.i("Device size is: XLARGE");
        }
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            DebugLog.i("Device size is: NORMAL");
        }
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            DebugLog.i("Device size is: SMALL");
        }
        DebugLog.i("Device is tablet: " + UIUtils.isHoneycombTablet(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see BaseSinglePaneActivity#onCreatePane()
     */
    @Override
    protected Fragment onCreatePane() {
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //SL.get(AnalyticsService.class).onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //SL.get(AnalyticsService.class).onStop();
    }

    @Override
    protected void onDestroy() {
        SL.get(this, GlobalHandlerService.class).removeListener(this);
        super.onDestroy();
    }

    /* (non-Javadoc)
     * @see apps.os.Handler.Callback#handleMessage(apps.os.Message)
     */
    @Override
    public boolean handleMessage(Message msg) {
        if (isFinishing()) {
            return false;
        }

        if (msg.what == R.id.message_connectivity_online) {
            Fragment f = getCurrentFragment();
            if (f != null && f instanceof IRefreshable) {
                ((IRefreshable)f).refresh();
            }
        }

        return true;
    }

    @Override
    public void onLowMemory() {
        DebugLog.wtf("ProjectBaseActivity.onLoweMemory()");

        //getSupportLoaderManager().destroyLoader(R.id.loader_...);
        System.gc();

        super.onLowMemory();
    }

}
