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

package eu.inmite.apps.smsjizdenka.framework.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import eu.inmite.apps.smsjizdenka.framework.App;
import eu.inmite.apps.smsjizdenka.framework.interfaces.IBackReceiver;

/**
 * Base fragment class.
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 */
public class BaseFragment extends Fragment implements IBackReceiver {

    protected Context mContext;
    protected App mApp;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        mApp = (App)activity.getApplication();
    }


    /**
     * Override this method to handle back button press or up from the actionbar.
     *
     * @param fromActionBar true if back action was introduced by up button in actionbar
     */
    @Override
    public boolean onBackPressed(boolean fromActionBar) {
        return false;
    }
}
