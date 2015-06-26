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

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import eu.inmite.apps.smsjizdenka.BuildConfig;

/**
 * Content provider for cities.
 */
public class CityProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".city";
    private static final UriMatcher sUriMatcher;
    private static final int CITIES = 1;
    private static final int CITY_ID = 2;
    private static HashMap<String, String> sProjectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "cities", CITIES);
        sUriMatcher.addURI(AUTHORITY, "cities/#", CITY_ID);

        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(Cities._ID, Cities._ID);
        sProjectionMap.put(Cities.ADDITIONAL_NUMBER_1, Cities.ADDITIONAL_NUMBER_1);
        sProjectionMap.put(Cities.ADDITIONAL_NUMBER_2, Cities.ADDITIONAL_NUMBER_2);
        sProjectionMap.put(Cities.ADDITIONAL_NUMBER_3, Cities.ADDITIONAL_NUMBER_3);
        sProjectionMap.put(Cities.CITY, Cities.CITY);
        sProjectionMap.put(Cities.CITY_PUBTRAN, Cities.CITY_PUBTRAN);
        sProjectionMap.put(Cities.COUNTRY, Cities.COUNTRY);
        sProjectionMap.put(Cities.CURRENCY, Cities.CURRENCY);
        sProjectionMap.put(Cities.LAT, Cities.LAT);
        sProjectionMap.put(Cities.LON, Cities.LON);
        sProjectionMap.put(Cities.NOTE, Cities.NOTE);
        sProjectionMap.put(Cities.NUMBER, Cities.NUMBER);
        sProjectionMap.put(Cities.P_DATE_FROM, Cities.P_DATE_FROM);
        sProjectionMap.put(Cities.P_DATE_TO, Cities.P_DATE_TO);
        sProjectionMap.put(Cities.P_HASH, Cities.P_HASH);
        sProjectionMap.put(Cities.PRICE, Cities.PRICE);
        sProjectionMap.put(Cities.PRICE_NOTE, Cities.PRICE_NOTE);
        sProjectionMap.put(Cities.REQUEST, Cities.REQUEST);
        sProjectionMap.put(Cities.VALIDITY, Cities.VALIDITY);
        sProjectionMap.put(Cities.DATE_FORMAT, Cities.DATE_FORMAT);
        sProjectionMap.put(Cities.IDENTIFICATION, Cities.IDENTIFICATION);
        sProjectionMap.put(Cities.CONFIRM_REQ, Cities.CONFIRM_REQ);
        sProjectionMap.put(Cities.CONFIRM, Cities.CONFIRM);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = DatabaseHelper.get(getContext()).getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case CITIES:
                count = db.delete(DatabaseHelper.CITY_TABLE_NAME, (!TextUtils.isEmpty(where) ? " (" + where + ')' : ""), whereArgs);
                break;

            case CITY_ID:
                String noteId = uri.getPathSegments().get(1);
                count = db.delete(DatabaseHelper.CITY_TABLE_NAME, Cities._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CITIES:
                return Cities.CONTENT_TYPE;

            case CITY_ID:
                return Cities.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != CITIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = DatabaseHelper.get(getContext()).getWritableDatabase();
        long rowId = db.insert(DatabaseHelper.CITY_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Cities.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case CITIES:
                qb.setTables(DatabaseHelper.CITY_TABLE_NAME);
                qb.setProjectionMap(sProjectionMap);
                break;
            case CITY_ID:
                qb.setTables(DatabaseHelper.CITY_TABLE_NAME);
                qb.setProjectionMap(sProjectionMap);
                qb.appendWhere(Cities._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Cities.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = DatabaseHelper.get(getContext()).getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = DatabaseHelper.get(getContext()).getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case CITIES:
                count = db.update(DatabaseHelper.CITY_TABLE_NAME, values, where, whereArgs);
                break;

            case CITY_ID:
                String noteId = uri.getPathSegments().get(1);
                count = db.update(DatabaseHelper.CITY_TABLE_NAME, values, Cities._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Notes table
     */
    public static final class Cities implements BaseColumns {
        // This class cannot be instantiated

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/cities");
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/smsjizdenka.city";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/smsjizdenka.city";
        public static final String COUNTRY = "COUNTRY";
        public static final String CITY = "CITY";
        public static final String CITY_PUBTRAN = "CITY_PUBTRAN";
        public static final String LAT = "LAT";
        public static final String LON = "LON";
        public static final String VALIDITY = "VALIDITY";
        public static final String NOTE = "NOTE";
        public static final String PRICE = "PRICE";
        public static final String CURRENCY = "CURRENCY";
        public static final String PRICE_NOTE = "PRICE_NOTE";
        public static final String REQUEST = "REQUEST";
        public static final String NUMBER = "NUMBER";
        public static final String ADDITIONAL_NUMBER_1 = "ADDITIONAL_NUMBER_1";
        public static final String ADDITIONAL_NUMBER_2 = "ADDITIONAL_NUMBER_2";
        public static final String ADDITIONAL_NUMBER_3 = "ADDITIONAL_NUMBER_3";
        public static final String IDENTIFICATION = "IDENTIFICATION";
        public static final String P_DATE_FROM = "P_DATE_FROM";
        public static final String P_DATE_TO = "P_DATE_TO";
        public static final String P_HASH = "P_HASH";
        public static final String DATE_FORMAT = "DATE_FORMAT";
        public static final String CONFIRM_REQ = "CONFIRM_REQ";
        public static final String CONFIRM = "CONFIRM";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = CITY + " ASC";

        private Cities() {
        }
    }
}
