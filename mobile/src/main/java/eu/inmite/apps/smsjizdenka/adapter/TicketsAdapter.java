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

package eu.inmite.apps.smsjizdenka.adapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.data.TicketProvider;
import eu.inmite.apps.smsjizdenka.data.TicketProvider.Tickets;
import eu.inmite.apps.smsjizdenka.dialog.ShowSmsDialogFragment;
import eu.inmite.apps.smsjizdenka.fragment.TicketsFragment;
import eu.inmite.apps.smsjizdenka.util.AnimationUtil;
import eu.inmite.apps.smsjizdenka.util.FormatUtil;

/**
 * Adapter for bought tickets.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class TicketsAdapter extends CursorAdapter {

    int iValidTo;
    int iCity;
    int iCityId;
    int iStatus;
    int iId;
    int iValidFrom;
    int iHash;
    int iText;
    private Context c;
    private TicketsFragment mFragment;
    private long mActiveId;
    private boolean mShowSms;

    public TicketsAdapter(Context c, TicketsFragment fragment, long activeId, boolean showSms) {
        super(c, null, 0);
        this.c = c;
        mFragment = fragment;
        mActiveId = activeId;
        mShowSms = showSms;
    }

    /**
     * Gets status of the ticket depending on its validity in {@code minutes}.
     */
    public static int getValidityStatus(int status, Time validTo) {
        if (status == Tickets.STATUS_WAITING || status == Tickets.STATUS_DELETED) {
            return status;
        } else {
            final int minutes = getValidityMinutes(validTo);
            if (minutes <= 0) {
                return Tickets.STATUS_EXPIRED;
            } else if (minutes == Constants.EXPIRING_MINUTES + 1) {
                return Tickets.STATUS_VALID_EXPIRING;
            } else if (minutes == 1) {
                return Tickets.STATUS_EXPIRING_EXPIRED;
            } else if (minutes <= Constants.EXPIRING_MINUTES) {
                return Tickets.STATUS_EXPIRING;
            } else {
                return Tickets.STATUS_VALID;
            }
        }
    }

    /**
     * Gets validity of ticket with time {@code t} in minutes.
     */
    public static int getValidityMinutes(Time t) {
        if (t == null) {
            return -1;
        }

        final Time now = new Time();
        now.setToNow();
        now.switchTimezone(Time.getCurrentTimezone());
        return (int)Math.ceil((t.toMillis(true) - now.toMillis(true)) / 1000d / 60d);
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        if (cursor != null) {
            iValidTo = cursor.getColumnIndex(Tickets.VALID_TO);
            iCity = cursor.getColumnIndex(Tickets.CITY);
            iCityId = cursor.getColumnIndex(Tickets.CITY_ID);
            iStatus = cursor.getColumnIndex(Tickets.STATUS);
            iId = cursor.getColumnIndex(Tickets._ID);
            iValidFrom = cursor.getColumnIndex(Tickets.VALID_FROM);
            iHash = cursor.getColumnIndex(Tickets.HASH);
            iText = cursor.getColumnIndex(Tickets.TEXT);
        }
        return super.swapCursor(cursor);
    }

    @Override
    public long getItemId(int position) {
        Cursor cursor = getCursor();
        if (cursor == null || cursor == null) {
            return -1;
        }
        try {
            boolean success = cursor.moveToPosition(position);
            if (!success) {
                return -1;
            }
        } catch (IllegalStateException e) {
            return -1;
        }
        return cursor.getLong(iId);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemId(position) == mActiveId) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder h = (ViewHolder)view.getTag();

        final Time validTo = FormatUtil.timeFrom3339(cursor.getString(iValidTo));

        final int status = getValidityStatus(cursor.getInt(iStatus), validTo);
        view.setTag(R.id.swipe_enabled, (status == Tickets.STATUS_EXPIRED));
        AnimationUtil.setFlipAnimation(h.vIndicator, h.animator, getPrimaryIndicatorImage(status),
            getSecondaryIndicatorImage(status), c);
        h.vStatus.setText(getValidityString(status, validTo));
        h.vStatus.setTextColor(getValidityColor(status));
        h.vCity.setText(cursor.getString(iCity));
        h.vTicket.setBackgroundResource(getTicketBackground(status));
        if (cursor.getLong(iId) == mActiveId) {
            final int position = cursor.getPosition();
            if (status != Tickets.STATUS_WAITING) {
                h.vValidFrom.setText(FormatUtil.formatDate3339(cursor.getString(iValidFrom)));
                h.vValidFrom.setTextColor(getActiveDetailColor(status));
                h.vValidTo.setText(FormatUtil.formatDate3339(cursor.getString(iValidTo)));
                h.vValidTo.setTextColor(getActiveDetailColor(status));
                h.vValidToLabel.setTextColor(getActiveDetailColor(status));
                h.vCode.setText(cursor.getString(iHash));
                h.vCode.setTextColor(getActiveDetailColor(status));
                h.vCodeLabel.setTextColor(getActiveDetailColor(status));
                h.vShowSms.setTextColor(getShowSmsColor(status));
                h.vValidFromLabel.setText(R.string.tickets_valid_from);
                h.vValidFromLabel.setVisibility(View.VISIBLE);
                h.vCodeLabel.setVisibility(View.VISIBLE);
                h.vShowSms.setVisibility(View.VISIBLE);
            } else {
                h.vValidFromLabel.setText(R.string.tickets_waiting_info);
                h.vValidToLabel.setVisibility(View.INVISIBLE);
                h.vCodeLabel.setVisibility(View.INVISIBLE);
                h.vShowSms.setVisibility(View.INVISIBLE);
            }
            if (status == Tickets.STATUS_EXPIRED || status == Tickets.STATUS_WAITING) {
                h.vDelete.setImageResource(getDeleteImage(status));
                h.vDelete.setContentDescription(c.getString(R.string.desc_delete_ticket));
                if (status == Tickets.STATUS_EXPIRED) {
                    h.vDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFragment.forceDismiss(position);
                        }
                    });
                } else {
                    // waiting gets deleted, not archived
                    h.vDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteTicket(position);
                        }
                    });
                }
            } else {
                h.vDelete.setImageResource(getCollapseImage(status));
                h.vDelete.setContentDescription(c.getString(R.string.desc_collapse_ticket));
                h.vDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFragment.collapseUncollapseTicket(mActiveId, mShowSms);
                    }
                });
            }
            h.vValidFromLabel.setTextColor(getActiveDetailColor(status));
            h.vCollapse.setImageResource(getCollapseImage(status));
            h.vSeparator1.setBackgroundResource(getSeparatorImage(status));
            h.vSeparator2.setBackgroundResource(getSeparatorImage(status));
            h.vSeparator3.setBackgroundResource(getSeparatorImage(status));
        } else {
            h.vWhenExpired.setText(getValidToText(status, validTo.toMillis(false)));
            h.vWhenExpired.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater li = LayoutInflater.from(context);
        final Time validTo = FormatUtil.timeFrom3339(cursor.getString(iValidTo));
        View v;
        final ViewHolder h = new ViewHolder();
        if (cursor.getLong(iId) == mActiveId) {
            final int position = cursor.getPosition();
            v = li.inflate(R.layout.item_ticket_active, parent, false);
            h.vValidTo = (TextView)v.findViewById(R.id.ticket_valid_to);
            h.vValidFrom = (TextView)v.findViewById(R.id.ticket_valid_from);
            h.vCode = (TextView)v.findViewById(R.id.ticket_code);
            h.vValidToLabel = (TextView)v.findViewById(R.id.ticket_valid_to_label);
            h.vValidFromLabel = (TextView)v.findViewById(R.id.ticket_valid_from_label);
            h.vCodeLabel = (TextView)v.findViewById(R.id.ticket_code_label);
            h.vDelete = (ImageButton)v.findViewById(R.id.delete_button);
            h.vCollapse = (ImageButton)v.findViewById(R.id.collapse_button);
            h.vShowSms = (Button)v.findViewById(R.id.sms_button);
            h.vSeparator1 = v.findViewById(R.id.separator1);
            h.vSeparator2 = v.findViewById(R.id.separator2);
            h.vSeparator3 = v.findViewById(R.id.separator3);
            h.vTopInfo = v.findViewById(R.id.top_info);
            //listeners
            h.vCollapse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.collapseUncollapseTicket(mActiveId, mShowSms);
                }
            });
            h.vTopInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.collapseUncollapseTicket(mActiveId, mShowSms);
                }
            });
            h.vShowSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSms(position);
                }
            });
            if (mShowSms) {
                showSms(position);
                mShowSms = false;
            }
        } else {
            v = li.inflate(R.layout.item_ticket, parent, false);
            h.vWhenExpired = (TextView)v.findViewById(R.id.ticket_when_expired);
        }
        h.vIndicator = (ImageView)v.findViewById(R.id.ticket_indicator);
        h.vStatus = (TextView)v.findViewById(R.id.ticket_status);
        h.vCity = (TextView)v.findViewById(R.id.ticket_city);
        h.vTicket = v.findViewById(R.id.ticket);
        h.animator = ObjectAnimator.ofFloat(h.vIndicator, "rotationX", 0, 360);
        v.setTag(h);

        return v;
    }

    private int getTicketBackground(int status) {
        switch (status) {
            case Tickets.STATUS_WAITING:
            case Tickets.STATUS_EXPIRED:
                return R.drawable.content_ticket_bg_expired;
            default:
                return R.drawable.content_ticket_bg_active;
        }
    }

    private String getValidToText(int status, long date) {
        if (status == Tickets.STATUS_EXPIRED) {
            return DateUtils.getRelativeTimeSpanString(date, System.currentTimeMillis(), 0).toString();
        } else {
            return "";
        }
    }

    private int getPrimaryIndicatorImage(int status) {
        switch (status) {
            case TicketProvider.Tickets.STATUS_WAITING:
            case TicketProvider.Tickets.STATUS_VALID:
            case Tickets.STATUS_VALID_EXPIRING:
                return R.drawable.content_ticket_indicator_circle_ready;
            case TicketProvider.Tickets.STATUS_EXPIRING:
            case Tickets.STATUS_EXPIRING_EXPIRED:
                return R.drawable.content_ticket_indicator_circle_warning;
            default:
                return R.drawable.content_ticket_indicator_circle_expired;
        }
    }

    private int getSecondaryIndicatorImage(int status) {
        switch (status) {
            case TicketProvider.Tickets.STATUS_WAITING:
                return R.drawable.content_ticket_indicator_circle_expired;
            case Tickets.STATUS_VALID_EXPIRING:
                return R.drawable.content_ticket_indicator_circle_warning;
            case Tickets.STATUS_EXPIRING_EXPIRED:
                return R.drawable.content_ticket_indicator_circle_expired;
            default:
                return AnimationUtil.NO_ANIMATION;
        }
    }

    private String getValidityString(int status, Time validTo) {
        final int validMinutes = getValidityMinutes(validTo);
        switch (status) {
            case Tickets.STATUS_WAITING:
                return c.getString(R.string.tickets_waiting);
            default:
                return FormatUtil.formatValidity(validMinutes, c);
        }

    }

    private int getDeleteImage(int status) {
        switch (status) {
            case Tickets.STATUS_WAITING:
            case Tickets.STATUS_EXPIRED:
                return R.drawable.content_ticket_ic_delete_dark;
            default:
                return R.drawable.content_ticket_ic_delete_light;
        }
    }

    private int getCollapseImage(int status) {
        switch (status) {
            case Tickets.STATUS_WAITING:
            case Tickets.STATUS_EXPIRED:
                return R.drawable.content_ticket_ic_wrap_dark;
            default:
                return R.drawable.content_ticket_ic_wrap_light;
        }
    }

    private int getSeparatorImage(int status) {
        switch (status) {
            case Tickets.STATUS_WAITING:
            case Tickets.STATUS_EXPIRED:
                return R.drawable.content_ticket_separator_dark;
            default:
                return R.drawable.content_ticket_separator_light;
        }
    }

    private int getShowSmsColor(int status) {
        final Resources resources = c.getResources();
        switch (status) {
            case Tickets.STATUS_WAITING:
            case Tickets.STATUS_EXPIRED:
                return resources.getColor(R.color.gray_4);
            default:
                return resources.getColor(R.color.gray_2);
        }
    }

    private int getActiveDetailColor(int status) {
        final Resources resources = c.getResources();
        switch (status) {
            case Tickets.STATUS_WAITING:
            case Tickets.STATUS_EXPIRED:
                return resources.getColor(R.color.gray_2);
            default:
                return resources.getColor(R.color.gray_1);
        }
    }

    private int getValidityColor(int status) {
        final Resources resources = c.getResources();
        switch (status) {
            case Tickets.STATUS_VALID:
            case Tickets.STATUS_VALID_EXPIRING:
                return resources.getColor(R.color.orange_1);
            case Tickets.STATUS_EXPIRING:
            case Tickets.STATUS_EXPIRING_EXPIRED:
                return resources.getColor(R.color.red);
            default:
                return resources.getColor(R.color.gray_3);
        }
    }

    /**
     * Tries to delete ticket either from swipe-to-dismiss or button.
     *
     * @return Pair of ticket id and previous status or null if it wasn't deleted.
     */
    public Pair<Long, Integer> archiveTicket(int position) {
        Cursor cursor = getCursor();
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        cursor.moveToPosition(position);
        long id;
        try {
            id = cursor.getLong(iId);
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }
        final Time validTo = FormatUtil.timeFrom3339(cursor.getString(iValidTo));
        int status = getValidityStatus(cursor.getInt(iStatus), validTo);
        if (status == Tickets.STATUS_EXPIRED) {
            ContentValues values = new ContentValues();
            values.put(TicketProvider.Tickets.STATUS, TicketProvider.Tickets.STATUS_DELETED);
            c.getContentResolver().update(ContentUris.withAppendedId(TicketProvider.Tickets.CONTENT_URI, id), values,
                null, null);
            // I had to call this deprecated method, because it needs to run synchronously. In asynchronous case, previous tickets blinks during swipe to dismiss.
            //noinspection deprecation
            getCursor().requery();
            return new Pair<Long, Integer>(id, status);
        } else {
            return null;
        }
    }

    private void deleteTicket(int position) {
        //SL.get(AnalyticsService.class).trackEvent("delete", "my-tickets");
        Cursor cursor = getCursor();
        if (cursor == null || cursor.isClosed()) {
            return;
        }
        cursor.moveToPosition(position);
        long id;
        try {
            id = cursor.getLong(iId);
        } catch (CursorIndexOutOfBoundsException e) {
            return;
        }
        c.getContentResolver().delete(ContentUris.withAppendedId(Tickets.CONTENT_URI, id), null, null);
        getCursor().requery();
    }

    public void restoreTicket(Pair<Long, Integer> previous) {
        //SL.get(AnalyticsService.class).trackEvent("restore-ticket", "my-tickets");
        ContentValues values = new ContentValues();
        values.put(TicketProvider.Tickets.STATUS, previous.second);
        c.getContentResolver().update(ContentUris.withAppendedId(TicketProvider.Tickets.CONTENT_URI, previous.first),
            values,
            null, null);
        // I had to call this deprecated method, because it needs to run synchronously. In asynchronous case, previous tickets blinks during swipe to dismiss.
        //noinspection deprecation
        getCursor().requery();
    }

    public void showSms(int position) {
        //SL.get(AnalyticsService.class).trackEvent("show-sms", "my-tickets");
        Cursor cursor = getCursor();
        if (cursor == null || cursor.isClosed()) {
            return;
        }
        cursor.moveToPosition(position);
        String text = cursor.getString(iText);
        long cityId = cursor.getLong(iCityId);
        ShowSmsDialogFragment.newInstance(text, cityId).show(mFragment.getFragmentManager(),
            ShowSmsDialogFragment.TAG);
    }

    public int getPositionForId(long id) {
        if (id == TicketsFragment.NONE) {
            return NO_SELECTION;
        }
        Cursor cursor = getCursor();
        if (cursor.isClosed()) {
            return NO_SELECTION;
        }
        while (cursor.moveToNext()) {
            long ticketId = cursor.getLong(iId);
            if (ticketId == id) {
                return cursor.getPosition();
            }
        }
        return NO_SELECTION;
    }

    private class ViewHolder {
        View vTicket;
        ImageView vIndicator;
        TextView vStatus;
        TextView vCity;
        TextView vWhenExpired;
        TextView vValidTo;
        TextView vValidFrom;
        TextView vCode;
        TextView vValidToLabel;
        TextView vValidFromLabel;
        TextView vCodeLabel;
        ImageButton vDelete;
        ImageButton vCollapse;
        Button vShowSms;
        View vSeparator1;
        View vSeparator2;
        View vSeparator3;
        View vTopInfo;
        ObjectAnimator animator;
    }
}