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

package eu.inmite.apps.smsjizdenka.framework;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.framework.interfaces.IService;
import eu.inmite.apps.smsjizdenka.framework.util.UIUtils;


/**
 * Intelligent parent for main application class which provide basic service initialization.
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 * @version $Revision: 0$ ($Date: 13.6.2011 10:42:51$)
 */
public abstract class App extends android.app.Application {

    private static boolean sIsMavenBuild;
    private static boolean sIsDebugBuild;
    private static int sAppVersionCode;
    private static String sAppVersionName;
    private static String sDeviceId;
    private static App sAppInstance;
    private Map<String, Object> mServicesInstances = new HashMap<String, Object>();
    private Map<String, Class> mServicesImplementationsMapping = new HashMap<String, Class>();

    /**
     * Return true if running build is builded by Maven or ANT, not by IDE.
     */
    public static boolean isMavenBuild() {
        return sIsMavenBuild;
    }

    public static boolean isIDEBuild() {
        return !sIsMavenBuild;
    }

    public static int getAppVersionCode() {
        return sAppVersionCode;
    }

    public static String getAppVersionName() {
        return sAppVersionName;
    }

    public static boolean isDebugBuild() {
        return sIsDebugBuild;
    }

    public static boolean isReleaseBuild() {
        return !sIsDebugBuild;
    }

    public static String getUniqueDeviceId() {
        return sDeviceId;
    }

    public static App getInstance() {
        return sAppInstance;
    }

    public static Intent getOpenMarketIntent(Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("market://details?id=" + context.getPackageName()));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    /**
     * Init app core constants, init debug logging and 3rd librarieds like Crittercism, Google Analytics, etc.
     *
     * @see DebugLog
     */
    @Override
    public void onCreate() {
        super.onCreate();

        sAppInstance = this;

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            sIsMavenBuild = pInfo.versionCode > 0 && !pInfo.versionName.equals("dev");
            sAppVersionCode = pInfo.versionCode;
            sAppVersionName = pInfo.versionName;
            sIsDebugBuild = ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
        } catch (NameNotFoundException e) {
            DebugLog.e(e.getMessage(), e);
        }

        try {
            sDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            DebugLog.e(e.getMessage(), e);
        }

        try {
            if (getResources().getBoolean(R.bool.config_fw_allowForceDebug)) {
                File f = new File(Environment.getExternalStorageDirectory(), "debug");
                if (f.exists()) {
                    sIsDebugBuild = true;
                }
            }
        } catch (Exception e) {
            DebugLog.e(e.getMessage(), e);
        }

        // enable debug logging to the logcat
        if (isDebugBuild()) {
            DebugLog.setDebugLoggingEnabled(true);
        } else {
            String levelS = getString(R.string.config_fw_logLevelForNonDebugBuilds);
            DebugLog.Level level = DebugLog.Level.valueOf(levelS);
            DebugLog.setLoggingLevel(level);
        }

        // set custom logcat TAG
        String logTag = getString(R.string.config_fw_logtag);
        if (!TextUtils.isEmpty(logTag)) {
            DebugLog.setDefaultLogTag(getString(R.string.config_fw_logtag));
        }

        // write startup information
        DebugLog.i("App started, release build: " + isReleaseBuild() + ", maven build: " + isMavenBuild());
        DebugLog.i("Device screen info: " + UIUtils.getScreenDensityAsString(this) + "/" + UIUtils.getScreenSizeAsString(this));

        if (isMavenBuild()) {
            initErrorReporting();
        }

        if (!isDebugBuild()) {
            initAnalytics();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !isMavenBuild()) {
            initStrictMode();
        }
    }

    /**
     * Override this to implement analytics initialisation.
     * It will be automatically called only for production builds.
     */
    protected void initAnalytics() {
    }

    /**
     * Override this to implement crash and error logging initialisation.
     * It will be automatically called only for non IDE build.
     */
    protected void initErrorReporting() {
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    protected void initStrictMode() {
        StrictMode.ThreadPolicy.Builder tpb = new StrictMode.ThreadPolicy.Builder();
        tpb.detectAll();
        tpb.penaltyLog();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            tpb.penaltyFlashScreen();
        }
        StrictMode.setThreadPolicy(tpb.build());

        StrictMode.VmPolicy.Builder vmpb = new StrictMode.VmPolicy.Builder();
        /* vmpb.detectActivityLeaks() - it doesn't work: http://stackoverflow.com/questions/5956132/android-strictmode-instancecountviolation */
        vmpb.detectLeakedSqlLiteObjects();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            vmpb.detectLeakedClosableObjects();
        }
        vmpb.penaltyLog();
        StrictMode.setVmPolicy(vmpb.build());
    }

    @Override
    public synchronized Object getSystemService(String name) {

        if (AlarmManager.class.getName().equals(name)) {
            return (AlarmManager)super.getSystemService(Context.ALARM_SERVICE);
        }

        if (NotificationManager.class.getName().equals(name)) {
            return (NotificationManager)super.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (TelephonyManager.class.getName().equals(name)) {
            return (TelephonyManager)super.getSystemService(Context.TELEPHONY_SERVICE);
        }

        if (InputMethodManager.class.getName().equals(name)) {
            return (InputMethodManager)super.getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        if (LocationManager.class.getName().equals(name)) {
            return (LocationManager)super.getSystemService(Context.LOCATION_SERVICE);
        }

        if (name.contains(".")) {
            // if name contains . it should be class path and we try to instantiate it.
            if (mServicesInstances.containsKey(name)) {
                return mServicesInstances.get(name);
            } else {
                try {
                    Class<?> clazz = null;
                    if (mServicesImplementationsMapping.containsKey(name)) {
                        clazz = mServicesImplementationsMapping.get(name);
                    } else {
                        clazz = Class.forName(name);
                    }

                    Object serviceInstance = null;
                    try {
                        Constructor<?> constructor = clazz.getConstructor(Context.class);
                        serviceInstance = constructor.newInstance(getApplicationContext());
                    } catch (NoSuchMethodException e) {
                        Constructor<?> constructor = clazz.getConstructor();
                        serviceInstance = constructor.newInstance();
                    }

                    if (!(serviceInstance instanceof IService)) {
                        throw new IllegalArgumentException("Requested service must implement IService interface");
                    }

                    mServicesInstances.put(name, serviceInstance);

                    DebugLog.v("App.getSystemService() - instantiate custom service " + name + " as object " + serviceInstance.toString());
                    return serviceInstance;

                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Requested service class was not found: " + name, e);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot initialize requested service: " + name, e);
                }
            }
        }

        return super.getSystemService(name);
    }

}