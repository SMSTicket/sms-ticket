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

package eu.inmite.apps.smsjizdenka.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

import eu.inmite.apps.smsjizdenka.data.CityProvider.Cities;
import eu.inmite.apps.smsjizdenka.data.TicketProvider.Tickets;
import eu.inmite.apps.smsjizdenka.framework.DebugLog;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_HELPER_SERVICE = "databaseHelperService";
    public static final String TICKET_TABLE_NAME = "ticket";
    public static final String CITY_TABLE_NAME = "city";
    static final String DATABASE_NAME = "smsjizdenka.db";
    static final int DATABASE_VERSION = 11;
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.mContext = context;
    }

    /**
     * Gets the {@link eu.inmite.apps.smsjizdenka.data.DatabaseHelper} from a {@link android.content.Context}.
     *
     * @throws IllegalStateException if the {@link android.app.Application} does not have an {@link eu.inmite.apps.smsjizdenka.data.DatabaseHelper}.
     * @see #DATABASE_HELPER_SERVICE
     */
    @SuppressWarnings("ResourceType")
    public static DatabaseHelper get(Context context) {
        DatabaseHelper loader = (DatabaseHelper)context.getSystemService(DATABASE_HELPER_SERVICE);
        if (loader == null) {
            context = context.getApplicationContext();
            loader = (DatabaseHelper)context.getSystemService(DATABASE_HELPER_SERVICE);
        }
        if (loader == null) {
            throw new IllegalStateException("DatabaseHelper not available");
        }

        return loader;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDB(db);
        // testovaci jizdenky
        /*long now = System.currentTimeMillis();
        int second = 1000;
		int minute = 60 * second;
		int hour = 60 * minute;
		int day = 24 * hour;
		addTestingTicket(db, 1, "Praha", "hash", "text jizdenky", now - 3 * day, now - 3 * day + hour, now - 3 * day + 2 * hour); // expired
		addTestingTicket(db, 18, "Hradec Králové", "hash", "text jizdenky", now - 2 * minute, now, now + 1 * hour); // valid
		addTestingTicket(db, 19, "Plzeň", "hash", "text jizdenky", now - 30 * minute, now - 28 * minute,
		now + 5 * minute); // expiring    */
    }

    private void addTestingTicket(SQLiteDatabase db, long cityId, String city, String hash, String message, long ordered, long validFrom, long validTo) {
        ContentValues values = new ContentValues();
        values.put(Tickets.CITY_ID, cityId);
        values.put(Tickets.CITY, city);
        values.put(Tickets.HASH, hash);
        values.put(Tickets.STATUS, Tickets.STATUS_DELIVERED);
        values.put(Tickets.TEXT, message);
        Time orderedTime = new Time();
        orderedTime.set(ordered);
        values.put(Tickets.ORDERED, orderedTime.format3339(false));
        Time validFromTime = new Time();
        validFromTime.set(validFrom);
        values.put(Tickets.VALID_FROM, validFromTime.format3339(false));
        Time validToTime = new Time();
        validToTime.set(validTo);
        values.put(Tickets.VALID_TO, validToTime.format3339(false));
        values.put(Tickets.VALID_TO_DATE, validTo);
        db.insert(TICKET_TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DebugLog.w("Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will preserve existing data");

        if (oldVersion == 8) {
            try {
                db.execSQL("ALTER TABLE " + TICKET_TABLE_NAME + " ADD " + TicketProvider.Tickets.SMS_URI + " TEXT;");
            } catch (SQLiteException sqle) {
                // Maybe the table was altered already... Shouldn't be an issue.
            }
            oldVersion = 9;
        }
        if (oldVersion == 9) {
            try {
                db.execSQL("ALTER TABLE " + TICKET_TABLE_NAME + " ADD " + TicketProvider.Tickets.CITY_ID + " INTEGER;");
                db.execSQL("ALTER TABLE " + TICKET_TABLE_NAME + " ADD " + TicketProvider.Tickets.TEXT + " TEXT;");
                db.execSQL("UPDATE " + TICKET_TABLE_NAME + " SET " + TicketProvider.Tickets.CITY_ID + " = 1");
            } catch (SQLiteException sqle) {
                // Maybe the table was altered already... Shouldn't be an issue.
            }
            oldVersion = 10;
        }
        if (oldVersion == 10) {
            try {
                db.execSQL("ALTER TABLE " + TICKET_TABLE_NAME + " ADD " + TicketProvider.Tickets.ORDERED + " TEXT;");
                db.execSQL("ALTER TABLE " + TICKET_TABLE_NAME + " ADD " + TicketProvider.Tickets.CITY + " TEXT;");
                db.execSQL("ALTER TABLE " + TICKET_TABLE_NAME + " ADD " + TicketProvider.Tickets.STATUS + " INTEGER;");
                db.execSQL("ALTER TABLE " + TICKET_TABLE_NAME + " ADD " + TicketProvider.Tickets.NOTIFICATION_ID + " INTEGER;");

                createTableCities(db);

                //update ticket description from 2.2 to 2.3
                final Time now = new Time();
                now.setToNow();
                now.switchTimezone(Time.getCurrentTimezone());

                final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");

                final ContentValues cv = new ContentValues();
                final Cursor c = db.query(TICKET_TABLE_NAME, null, null, null, null, null, null);
                while (c.moveToNext()) {
                    final Date validFrom = sdf.parse(c.getString(c.getColumnIndex(Tickets.VALID_FROM)));
                    final Time vf = new Time();
                    vf.set(validFrom.getTime());
                    cv.put(Tickets.VALID_FROM, vf.format3339(false));

                    final Date validTo = sdf.parse(c.getString(c.getColumnIndex(Tickets.VALID_TO)));
                    final Time vt = new Time();
                    vt.set(validTo.getTime());
                    cv.put(Tickets.VALID_TO, vt.format3339(false));


                    cv.put(Tickets.ORDERED, now.format3339(false));
                    cv.put(Tickets.STATUS, 7);
                    cv.put(Tickets.NOTIFICATION_ID, 0);

                    int cityId = c.getInt(c.getColumnIndex("city_id"));
                    if (cityId >= 3) {
                        cityId += 2;
                    }
                    cv.put(Tickets.CITY_ID, cityId);
                    final int stringId = mContext.getResources().getIdentifier("city_" + cityId, "string", mContext.getPackageName());
                    cv.put(Tickets.CITY, mContext.getString(stringId));

                    db.update(TICKET_TABLE_NAME, cv, Tickets._ID + " = ?", new String[]{c.getString(c.getColumnIndex(Tickets._ID))});
                }
                c.close();
            } catch (Exception e) {
                DebugLog.e("cannot update database. Uninstall and install the app once again");
            }
            oldVersion = 11;
        }
    }

    private void createDB(SQLiteDatabase db) {
        DebugLog.i("Creating database version " + DATABASE_VERSION);
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE ");
        sql.append(TICKET_TABLE_NAME);
        sql.append(" (");
        sql.append(Tickets._ID);
        sql.append(" integer primary key autoincrement,");
        sql.append(Tickets.ORDERED);
        sql.append(" text,");
        sql.append(Tickets.VALID_FROM);
        sql.append(" text,");
        sql.append(Tickets.VALID_TO);
        sql.append(" text,");
        sql.append(Tickets.VALID_TO_DATE);
        sql.append(" integer,");
        sql.append(Tickets.HASH);
        sql.append(" text,");
        sql.append(Tickets.CITY_ID);
        sql.append(" integer,");
        sql.append(Tickets.TEXT);
        sql.append(" text,");
        sql.append(Tickets.CITY);
        sql.append(" text,");
        sql.append(Tickets.STATUS);
        sql.append(" integer,");
        sql.append(Tickets.NOTIFICATION_ID);
        sql.append(" integer");
        sql.append(");");
        db.execSQL(sql.toString());

        createTableCities(db);
    }

    private void createTableCities(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql = new StringBuffer();
        sql.append("CREATE TABLE ");
        sql.append(CITY_TABLE_NAME);
        sql.append(" (");
        sql.append(Cities._ID);
        sql.append(" integer primary key,");
        sql.append(Cities.ADDITIONAL_NUMBER_1);
        sql.append(" text,");
        sql.append(Cities.ADDITIONAL_NUMBER_2);
        sql.append(" text,");
        sql.append(Cities.ADDITIONAL_NUMBER_3);
        sql.append(" text,");
        sql.append(Cities.CITY);
        sql.append(" text,");
        sql.append(Cities.CITY_PUBTRAN);
        sql.append(" text,");
        sql.append(Cities.COUNTRY);
        sql.append(" text,");
        sql.append(Cities.CURRENCY);
        sql.append(" text,");
        sql.append(Cities.IDENTIFICATION);
        sql.append(" text,");
        sql.append(Cities.LAT);
        sql.append(" double,");
        sql.append(Cities.LON);
        sql.append(" double,");
        sql.append(Cities.NOTE);
        sql.append(" text,");
        sql.append(Cities.NUMBER);
        sql.append(" text,");
        sql.append(Cities.P_DATE_FROM);
        sql.append(" text,");
        sql.append(Cities.P_DATE_TO);
        sql.append(" text,");
        sql.append(Cities.P_HASH);
        sql.append(" text,");
        sql.append(Cities.PRICE);
        sql.append(" text,");
        sql.append(Cities.PRICE_NOTE);
        sql.append(" text,");
        sql.append(Cities.REQUEST);
        sql.append(" text,");
        sql.append(Cities.VALIDITY);
        sql.append(" integer,");
        sql.append(Cities.DATE_FORMAT);
        sql.append(" text,");
        sql.append(Cities.CONFIRM_REQ);
        sql.append(" text,");
        sql.append(Cities.CONFIRM);
        sql.append(" text");
        sql.append(");");
        db.execSQL(sql.toString());
    }
}