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
 * Content provider for issued tickets.
 */
public class TicketProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".ticket";
    private static final UriMatcher sUriMatcher;
    private static final int TICKETS = 1;
    private static final int TICKET_ID = 2;
    private static HashMap<String, String> sProjectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "tickets", TICKETS);
        sUriMatcher.addURI(AUTHORITY, "tickets/#", TICKET_ID);

        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(Tickets._ID, Tickets._ID);
        sProjectionMap.put(Tickets.ORDERED, Tickets.ORDERED);
        sProjectionMap.put(Tickets.VALID_FROM, Tickets.VALID_FROM);
        sProjectionMap.put(Tickets.VALID_TO, Tickets.VALID_TO);
        sProjectionMap.put(Tickets.HASH, Tickets.HASH);
        sProjectionMap.put(Tickets.VALID_TO_DATE, Tickets.VALID_TO_DATE);
        sProjectionMap.put(Tickets.CITY_ID, Tickets.CITY_ID);
        sProjectionMap.put(Tickets.TEXT, Tickets.TEXT);
        sProjectionMap.put(Tickets.CITY, Tickets.CITY);
        sProjectionMap.put(Tickets.STATUS, Tickets.STATUS);
        sProjectionMap.put(Tickets.NOTIFICATION_ID, Tickets.NOTIFICATION_ID);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = DatabaseHelper.get(getContext()).getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case TICKETS:
                count = db.delete(DatabaseHelper.TICKET_TABLE_NAME, (!TextUtils.isEmpty(where) ? " (" + where + ')' : ""), whereArgs);
                break;

            case TICKET_ID:
                String noteId = uri.getPathSegments().get(1);
                count = db.delete(DatabaseHelper.TICKET_TABLE_NAME, Tickets._ID + "=" + noteId
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
            case TICKETS:
                return Tickets.CONTENT_TYPE;

            case TICKET_ID:
                return Tickets.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != TICKETS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = DatabaseHelper.get(getContext()).getWritableDatabase();
        long rowId = db.insert(DatabaseHelper.TICKET_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Tickets.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case TICKETS:
                qb.setTables(DatabaseHelper.TICKET_TABLE_NAME);
                qb.setProjectionMap(sProjectionMap);
                break;
            case TICKET_ID:
                qb.setTables(DatabaseHelper.TICKET_TABLE_NAME);
                qb.setProjectionMap(sProjectionMap);
                qb.appendWhere(Tickets._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Tickets.DEFAULT_SORT_ORDER;
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
            case TICKETS:
                count = db.update(DatabaseHelper.TICKET_TABLE_NAME, values, where, whereArgs);
                break;

            case TICKET_ID:
                String noteId = uri.getPathSegments().get(1);
                count = db.update(DatabaseHelper.TICKET_TABLE_NAME, values, Tickets._ID + "=" + noteId
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
    public static final class Tickets implements BaseColumns {
        // This class cannot be instantiated

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tickets");
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/smsjizdenka.ticket";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/smsjizdenka.ticket";
        public static final String ORDERED = "ordered";
        public static final String VALID_FROM = "valid_from";
        public static final String VALID_TO = "valid_to";
        public static final String HASH = "hash";
        public static final String VALID_TO_DATE = "valid_to_date";
        public static final String SMS_URI = "sms_uri";
        public static final String CITY_ID = "city_id";
        public static final String TEXT = "text";
        public static final String CITY = "city";
        public static final String STATUS = "status";
        public static final int STATUS_WAITING = 3;
        public static final int STATUS_VALID = 4;
        public static final int STATUS_EXPIRING = 6;
        public static final int STATUS_EXPIRED = 7;
        public static final int STATUS_DELETED = 5;
        // temporary status which is changed into VALID, EXPIRING or EXPIRED on the fly based on time
        public static final int STATUS_DELIVERED = 8;
        // new statuses for different animation
        public static final int STATUS_VALID_EXPIRING = 9;
        public static final int STATUS_EXPIRING_EXPIRED = 10;
        public static final String NOTIFICATION_ID = "notificationId";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = VALID_TO_DATE + " DESC";

        private Tickets() {
        }
    }
}
