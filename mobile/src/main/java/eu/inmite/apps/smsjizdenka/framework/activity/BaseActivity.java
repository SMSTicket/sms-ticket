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

package eu.inmite.apps.smsjizdenka.framework.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import eu.inmite.apps.smsjizdenka.framework.interfaces.IBackReceiver;

/**
 * A base activity.
 * This class shouldn't be used directly; instead, activities should
 * inherit from {@link BaseSinglePaneActivity}.
 * <p/>
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Converts an intent into a {@link android.os.Bundle} suitable for use as fragment arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }
        final String action = intent.getAction();
        if (action != null) {
            arguments.putString("_action", action);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onHomePressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This allow to react on home up button pressed in AB.
     * <p/>
     * <p>Default implementation will call {@link #onBackPressed()} on current fragment and if return false, then {@link #goToBack()}.</p>
     * <p>You can also implement {@link IBackReceiver} in the fragment and you will decide about back operation in the fragment.</p>
     */
    protected void onHomePressed() {
        // then we delegate the back to fragment (which can also cancel it)
        final Fragment fragment = getCurrentFragment();
        if (fragment != null && fragment instanceof IBackReceiver) {
            if (((IBackReceiver)fragment).onBackPressed(true)) {
                return;
            }
        }

        goToBack();
    }

    @Override
    public void onBackPressed() {
        // then we delegate the back to fragment (which can also cancel it)
        final Fragment fragment = getCurrentFragment();
        if (fragment != null && fragment instanceof IBackReceiver) {
            if (((IBackReceiver)fragment).onBackPressed(false)) {
                return;
            }
        }

        super.onBackPressed();
    }

    /**
     * <p>It will go back fragment if any exist, otherwise call finish(), but not if activity is root activity.</p>
     *
     * @return true if back operation was successful proceed.
     */
    public boolean goToBack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            removeCurrentFragment();
            getSupportFragmentManager().popBackStack();
            return true;
        } else {
            if (!isTaskRoot()) {
                finish();
                return true;
            }
        }
        return false;
    }

    public Fragment getCurrentFragment() {
        return null;
    }

    public void removeCurrentFragment() {
    }

}
