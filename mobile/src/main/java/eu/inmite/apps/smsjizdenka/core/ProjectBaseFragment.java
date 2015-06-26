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

import android.app.Activity;
import android.content.Intent;

import eu.inmite.apps.smsjizdenka.framework.fragment.BaseFragment;


/**
 * Base for all fragments.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class ProjectBaseFragment extends BaseFragment {

    public ProjectBaseActivity c;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c = (ProjectBaseActivity)activity;
    }

    public void startActivity(Class clazz) {
        Intent intent = new Intent(c, clazz);
        c.startActivity(intent);
    }
}
