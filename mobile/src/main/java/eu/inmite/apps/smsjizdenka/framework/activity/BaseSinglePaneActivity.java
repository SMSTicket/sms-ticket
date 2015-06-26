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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import eu.inmite.apps.smsjizdenka.R;

/**
 * A {@link BaseActivity} that simply contains a single fragment or only one dynamic fragment and several static.
 * <p/>
 * <p>The intent used to invoke this activity is forwarded to the fragment as arguments during
 * fragment instantiation. Derived activities should only need to implement {@link #onCreatePane()}.</p>
 * <p/>
 * <p>This should be used also fot tablet application where is only one dynamic fragment (content) and more static fragment like dasboard menu.<p>
 */
public abstract class BaseSinglePaneActivity extends BaseActivity {

    public static String ROOT_FRAGMENT_TAG = "eu.inmite.ROOT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResourceId());

        if (getIntent().hasExtra(Intent.EXTRA_TITLE)) {
            setTitle(getIntent().getStringExtra(Intent.EXTRA_TITLE));
        }

        if (savedInstanceState == null) {
            Fragment fragment = onCreatePane();
            if (fragment != null) {
                Bundle b = fragment.getArguments();
                if (b == null) {
                    b = new Bundle();
                }
                b.putAll(intentToFragmentArguments(getIntent()));
                fragment.setArguments(b);

                getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, ROOT_FRAGMENT_TAG).commit();
            }
        }
    }

    @Override
    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag(ROOT_FRAGMENT_TAG);
    }

    @Override
    public void removeCurrentFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment currentFrag = getSupportFragmentManager().findFragmentByTag(ROOT_FRAGMENT_TAG);
        if (currentFrag != null) {
            transaction.remove(currentFrag);
        }
        transaction.commit();
    }

    /**
     * Called just before a fragment replacement transaction is committed in response to an intent
     * being fired and substituted for a fragment.
     * <p/>
     * Here you can put custom animation or any other customization for fragment replacement.
     */
    protected void onBeforeCommitReplaceFragment(FragmentManager fm, FragmentTransaction ft, Fragment fragment) {
        //ft.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    }

    /**
     * Called in <code>onCreate</code> when the fragment constituting this
     * activity is needed. The returned fragment's arguments will be set to the
     * intent used to invoke this activity.
     */
    protected abstract Fragment onCreatePane();


    /**
     * Return activity layout resource. It should be overloaded in parents to change layout.
     *
     * @return layout resource ID
     */
    protected int getContentViewResourceId() {
        return R.layout.activity_singlepane_empty;
    }

}