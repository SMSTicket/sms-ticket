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

package eu.inmite.apps.smsjizdenka.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.data.CityProvider.Cities;
import eu.inmite.apps.smsjizdenka.data.DatabaseHelper;
import eu.inmite.apps.smsjizdenka.data.Preferences;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;
import eu.inmite.apps.smsjizdenka.util.NotificationUtil;

/**
 * Service which updates ticket database.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class UpdateService extends IntentService {

    private static final boolean LOCAL_DEFINITION_TESTING = false; // must be false in production

    private static final int URL_VERSION_ID = 1;
    private static final String URL_VERSION = "version2.json";
    private static final int URL_TICKETS_ID = 2;
    private static final String URL_TICKETS = "tickets2.json";
    private Handler mHandler;
    private Context c;

    public UpdateService() {
        super("UpdateService");
    }

    public static void call(Context context, boolean force) {
        final Intent i = new Intent(context, UpdateService.class);
        i.putExtra("force", force);
        context.startService(i);
    }

    public static void call(Context context, int serverVersion, String message) {
        final Intent i = new Intent(context, UpdateService.class);
        i.putExtra("serverVersion", serverVersion);
        i.putExtra("message", message);
        context.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
        c = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final boolean force = intent.getBooleanExtra("force", false);
        try {
            Locale loc = Locale.getDefault();
            String lang = loc.getISO3Language(); // http://www.loc.gov/standards/iso639-2/php/code_list.php; T-values if present both T and B
            if (lang == null || lang.length() == 0) {
                lang = "";
            }

            int serverVersion = intent.getIntExtra("serverVersion", -1);
            boolean fromPush = serverVersion != -1;
            JSONObject versionJson = null;
            if (!fromPush) {
                versionJson = getVersion(true);
                serverVersion = versionJson.getInt("version");
            }
            int localVersion = Preferences.getInt(c, Preferences.DATA_VERSION, -1);
            final String localLanguage = Preferences.getString(c, Preferences.DATA_LANGUAGE, "");

            if (serverVersion <= localVersion && !force && lang.equals(localLanguage) && !LOCAL_DEFINITION_TESTING) {
                // don't update
                DebugLog.i("Nothing new, not updating");
                return;
            }

            // update but don't notify about it.
            boolean firstLaunchNoUpdate = ((localVersion == -1 && getVersion(false).getInt("version") ==
                serverVersion) || !lang.equals(localLanguage));

            if (!firstLaunchNoUpdate) {
                DebugLog.i("There are new definitions available!");
            }

            handleAuthorMessage(versionJson, lang, intent, fromPush);

            InputStream is = getIS(URL_TICKETS_ID);
            try {
                String json = readResult(is);

                JSONObject o = new JSONObject(json);
                JSONArray array = o.getJSONArray("tickets");

                final SQLiteDatabase db = DatabaseHelper.get(this).getWritableDatabase();
                for (int i = 0; i < array.length(); i++) {
                    final JSONObject city = array.getJSONObject(i);
                    try {

                        final ContentValues cv = new ContentValues();
                        cv.put(Cities._ID, city.getInt("id"));
                        cv.put(Cities.CITY, getStringLocValue(city, lang, "city"));
                        if (city.has("city_pubtran")) {
                            cv.put(Cities.CITY_PUBTRAN, city.getString("city_pubtran"));
                        }
                        cv.put(Cities.COUNTRY, city.getString("country"));
                        cv.put(Cities.CURRENCY, city.getString("currency"));
                        cv.put(Cities.DATE_FORMAT, city.getString("dateFormat"));
                        cv.put(Cities.IDENTIFICATION, city.getString("identification"));
                        cv.put(Cities.LAT, city.getDouble("lat"));
                        cv.put(Cities.LON, city.getDouble("lon"));
                        cv.put(Cities.NOTE, getStringLocValue(city, lang, "note"));
                        cv.put(Cities.NUMBER, city.getString("number"));
                        cv.put(Cities.P_DATE_FROM, city.getString("pDateFrom"));
                        cv.put(Cities.P_DATE_TO, city.getString("pDateTo"));
                        cv.put(Cities.P_HASH, city.getString("pHash"));
                        cv.put(Cities.PRICE, city.getString("price"));
                        cv.put(Cities.PRICE_NOTE, getStringLocValue(city, lang, "priceNote"));
                        cv.put(Cities.REQUEST, city.getString("request"));
                        cv.put(Cities.VALIDITY, city.getInt("validity"));
                        if (city.has("confirmReq")) {
                            cv.put(Cities.CONFIRM_REQ, city.getString("confirmReq"));
                        }
                        if (city.has("confirm")) {
                            cv.put(Cities.CONFIRM, city.getString("confirm"));
                        }

                        final JSONArray additionalNumbers = city.getJSONArray("additionalNumbers");
                        for (int j = 0; j < additionalNumbers.length() && j < 3; j++) {
                            cv.put("ADDITIONAL_NUMBER_" + (j + 1), additionalNumbers.getString(j));
                        }

                        db.beginTransaction();
                        int count = db.update(DatabaseHelper.CITY_TABLE_NAME, cv, Cities._ID + " = " + cv.getAsInteger(Cities._ID), null);
                        if (count == 0) {
                            db.insert(DatabaseHelper.CITY_TABLE_NAME, null, cv);
                        }

                        db.setTransactionSuccessful();
                        getContentResolver().notifyChange(Cities.CONTENT_URI, null);
                    } finally {
                        if (db.inTransaction()) {
                            db.endTransaction();
                        }
                    }
                }
                Preferences.set(c, Preferences.DATA_VERSION, serverVersion);
                Preferences.set(c, Preferences.DATA_LANGUAGE, lang);
                if (!firstLaunchNoUpdate && !fromPush) {
                    final int finalServerVersion = serverVersion;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UpdateService.this, getString(R.string.cities_update_completed, finalServerVersion),
                                Toast.LENGTH_LONG).show();
                        }
                    });
                }
                if (LOCAL_DEFINITION_TESTING) {
                    DebugLog.w("Local definition testing - data updated from assets - must be removed in production!");
                }
            } finally {
                is.close();
            }
        } catch (IOException e) {
            DebugLog.e("IOException when calling update: " + e.getMessage(), e);
        } catch (JSONException e) {
            DebugLog.e("JSONException when calling update: " + e.getMessage(), e);
        }
    }

    private void handleAuthorMessage(JSONObject versionJson, String lang, Intent intent, boolean fromPush) {
        String intentMessage = intent.getStringExtra("message");
        if (fromPush && intentMessage != null) {
            NotificationUtil.notifyMessage(c, intentMessage);
        } else if (!fromPush) {
            String message = getStringLocValue(versionJson, lang, "msg");
            if (!TextUtils.isEmpty(message)) {
                NotificationUtil.notifyMessage(c, message);
            }
        }
    }

    private String getStringLocValue(JSONObject json, String lang, String key) {
        String value = "";

        try {
            if (json.has(key + "_" + lang)) {
                value = json.getString(key + "_" + lang);
            } else {
                value = json.getString(key);
            }
        } catch (JSONException je) {
            // failed
        }

        return value;
    }

    private JSONObject getVersion(boolean online) throws IOException, JSONException {
        InputStream is = null;
        try {
            is = getIS(URL_VERSION_ID, online);
        } catch (IOException e) {
            is = getResources().getAssets().open(URL_VERSION);
        }
        try {
            final String json = readResult(is);
            return new JSONObject(json);
        } finally {
            is.close();
        }
    }

    private String readResult(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is, "utf-8"), 1000);
        String l = null;
        while ((l = r.readLine()) != null) {
            sb.append(l);
        }

        return sb.toString();
    }

    private InputStream getIS(int addr) throws IOException {
        try {
            return getIS(addr, true);
        } catch (IOException e) {
            if (URL_VERSION_ID == addr) {
                return getAssets().open(URL_VERSION);
            } else {
                return getAssets().open(URL_TICKETS);
            }
        }
    }

    private InputStream getIS(int addr, boolean online) throws IOException {
        if (!online || LOCAL_DEFINITION_TESTING) {
            if (addr == URL_VERSION_ID) {
                return getResources().getAssets().open(URL_VERSION);
            } else if (addr == URL_TICKETS_ID) {
                return getResources().getAssets().open(URL_TICKETS);
            } else {
                throw new IOException("requested addr " + addr + " not found");
            }
        } else {
            URL url;

            if (addr == URL_VERSION_ID) {
                url = new URL(Constants.DATA_URL_BASE + URL_VERSION);
            } else if (addr == URL_TICKETS_ID) {
                url = new URL(Constants.DATA_URL_BASE + URL_TICKETS);
            } else {
                throw new IOException("requested addr " + addr + " not found");
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(30000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);

                if (connection.getResponseCode() != 200) {
                    connection.disconnect();

                    throw new IOException("status " + connection.getResponseCode() + " received");
                }

                if (addr == URL_VERSION_ID) {
                    DebugLog.i("Downloading definition version info...");
                } else if (addr == URL_TICKETS_ID) {
                    DebugLog.i("Downloading new tickets definition...");
                }

                return connection.getInputStream();
            } catch (SecurityException e) {
                throw new IOException("Internet access denied");
            }
        }
    }
}
