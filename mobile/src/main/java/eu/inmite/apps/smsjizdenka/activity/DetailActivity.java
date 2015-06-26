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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseActivity;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;

/**
 * Holder for fragments.
 *
 * @author Michal Mátl (matl)
 */
public class DetailActivity extends ProjectBaseActivity {

    private static final String ARG_BUNDLE = "bundle";
    private static final String ARG_CLASS = "class";

    public static void call(Context context, Class<? extends Fragment> fragment, Bundle bundle) {
        Intent i = new Intent(context, DetailActivity.class);
        i.putExtra(ARG_BUNDLE, bundle);
        i.putExtra(ARG_CLASS, fragment);
        context.startActivity(i);
    }

    @Override
    protected Fragment onCreatePane() {
        Class<? extends Fragment> c = (Class<? extends Fragment>)getIntent().getSerializableExtra("class");
        try {
            Fragment fragment = c.newInstance();
            fragment.setArguments(getIntent().getBundleExtra(ARG_BUNDLE));
            getIntent().removeExtra(ARG_BUNDLE);
            getIntent().removeExtra(ARG_CLASS);
            return fragment;
        } catch (Exception e) {
            DebugLog.wtf("DetailActivity.onCreatePane() failed", e);
            finish();
        }

        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getCurrentFragment().onActivityResult(requestCode, resultCode, data);
    }
}
