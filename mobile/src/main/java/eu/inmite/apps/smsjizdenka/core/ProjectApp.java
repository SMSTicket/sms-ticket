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

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import eu.inmite.apps.smsjizdenka.data.DatabaseHelper;
import eu.inmite.apps.smsjizdenka.data.model.CityManager;
import eu.inmite.apps.smsjizdenka.framework.App;

/**
 * Main application class.
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 * @version $Revision: 0$ ($Date: 13.6.2011 10:42:51$)
 */
public class ProjectApp extends App {

    private CityManager mCityManager;
    private DatabaseHelper mDatabaseHelper;

    @Override
    public synchronized Object getSystemService(String name) {
        if (CityManager.CITY_MANAGER_SERVICE.equals(name)) {
            if (mCityManager == null) {
                mCityManager = new CityManager();
            }

            return mCityManager;
        }

        if (DatabaseHelper.DATABASE_HELPER_SERVICE.equals(name)) {
            if (mDatabaseHelper == null) {
                mDatabaseHelper = new DatabaseHelper(this);
            }
            return mDatabaseHelper;
        }

        return super.getSystemService(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!isDebugBuild()) {
            Fabric.with(this, new Crashlytics());
        }
    }
}
