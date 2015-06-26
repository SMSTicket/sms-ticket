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

import android.os.Bundle;
import android.support.v4.app.Fragment;

import eu.inmite.apps.smsjizdenka.core.ProjectBaseActivity;
import eu.inmite.apps.smsjizdenka.fragment.CitiesFragment;

/**
 * Activity for selecting city of ticket.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class CitiesActivity extends ProjectBaseActivity {

    @Override
    protected Fragment onCreatePane() {
        return new CitiesFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
