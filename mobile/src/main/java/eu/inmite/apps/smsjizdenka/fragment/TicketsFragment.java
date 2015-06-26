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

package eu.inmite.apps.smsjizdenka.fragment;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.activity.CitiesActivity;
import eu.inmite.apps.smsjizdenka.adapter.TicketsAdapter;
import eu.inmite.apps.smsjizdenka.core.Constants;
import eu.inmite.apps.smsjizdenka.core.ProjectBaseFragment;
import eu.inmite.apps.smsjizdenka.data.TicketProvider.Tickets;
import eu.inmite.apps.smsjizdenka.util.AnimationUtil;
import eu.inmite.apps.smsjizdenka.view.SwipeDismissList;

/**
 * Fragment displaying tickets.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class TicketsFragment extends ProjectBaseFragment implements
    LoaderManager.LoaderCallbacks<Cursor> {

    public static final long NONE = -1;
    static long sActiveId = NONE;
    static boolean sShowSms = false;
    View vTopHelp;
    View vBottomHelp;
    ListView vList;
    SwipeDismissList vSwipeDismissList;
    View vEmpty;
    View vProgress;
    Button vBuy;
    TicketsAdapter mAdapter;
    Timer mTimer;
    boolean mShowAnimation;
    int mLastScrollIndex, mLastScrollTop;
    // swipe to dismiss - undo pattern
    SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
        public SwipeDismissList.Undoable onDismiss(ListView listView, final int position) {
            setScrollPosition();
            final Pair<Long, Integer> previousState = mAdapter.archiveTicket(position);
            if (previousState == null) {
                // no undo
                return null;
            } else {
                return new SwipeDismissList.Undoable() {

                    @Override
                    public void undo() {

                        mAdapter.restoreTicket(previousState);
                    }
                };
            }
        }
    };

    public static TicketsFragment newInstance(long activeId, boolean showSms) {
        sActiveId = activeId;
        sShowSms = showSms;
        return new TicketsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tickets, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        vList = (ListView)view.findViewById(android.R.id.list);
        vTopHelp = view.findViewById(R.id.top_help);
        vBottomHelp = view.findViewById(R.id.bottom_help);
        vBuy = (Button)view.findViewById(R.id.buy);
        vEmpty = view.findViewById(android.R.id.empty);
        vProgress = view.findViewById(android.R.id.progress);
        vList.setEmptyView(vEmpty);
        showProgress();
        vList.setVisibility(View.GONE);
        if (mShowAnimation) {
            vList.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(c, android.R.anim
                .slide_in_left)));
        }
        vList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                collapseUncollapseTicket(id, false);
            }
        });
        vBuy.setOnClickListener(new

                                    View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            startActivity(CitiesActivity.class);
                                        }
                                    });
        vSwipeDismissList = new SwipeDismissList(vList, callback, SwipeDismissList.UndoMode.COLLAPSED_UNDO);
        vSwipeDismissList.setUndoString(c.getString(R.string.tickets_ticket_deleted));
        vSwipeDismissList.setUndoMultipleString(c.getString(R.string.tickets_tickets_deleted));
        // first launch animations
        int dpi = getResources().getDisplayMetrics().densityDpi;
        if (mShowAnimation && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT &&
            dpi != DisplayMetrics.DENSITY_LOW && dpi != DisplayMetrics.DENSITY_MEDIUM) {
            AnimationUtil.addAnimationToView(vTopHelp, R.anim.slide_in_top);
            AnimationUtil.addAnimationToView(vBottomHelp, R.anim.slide_in_bottom);
        }
        mShowAnimation = false;
        //SL.get(AnalyticsService.class).trackScreen("my-tickets");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mShowAnimation = true;
        } else {
            // disable animation on rotate
            mShowAnimation = savedInstanceState.getBoolean("showAnimation", false);
            mLastScrollIndex = savedInstanceState.getInt("scrollIndex", 0);
            mLastScrollTop = savedInstanceState.getInt("scrollTop", 0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("showAnimation", false);
        setScrollPosition();
        outState.putInt("scrollIndex", mLastScrollIndex);
        outState.putInt("scrollTop", mLastScrollTop);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // refresh list every full minute
        mTimer = new Timer();
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.SECOND, 0);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                c.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        c.getSupportLoaderManager().restartLoader(Constants.LOADER_TICKETS, null, TicketsFragment.this);
                    }
                });
            }
        }, new Date(calendar.getTimeInMillis()), 1000 * 60);
    }

    @Override
    public void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(c, Tickets.CONTENT_URI, null, Tickets.STATUS + " <> ?", new String[]{String.valueOf(Tickets.STATUS_DELETED)}, Tickets.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter = new TicketsAdapter(c, this, sActiveId, sShowSms);
        sShowSms = false;
        vList.setAdapter(mAdapter);
        mAdapter.swapCursor(cursor);
        if (mLastScrollIndex != 0) {
            vList.setSelectionFromTop(mLastScrollIndex, mLastScrollTop);
        }
        hideProgress();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onStop() {
        vSwipeDismissList.discardUndo();
        super.onStop();
    }

    public void collapseUncollapseTicket(long ticketId, boolean showSms) {
        //SL.get(AnalyticsService.class).trackEvent("collapse-uncollapse", "my-tickets");
        if (sActiveId == ticketId) {
            sActiveId = NONE;
        } else {
            sActiveId = ticketId;
        }
        sShowSms = showSms;
        setScrollPosition();
        c.getSupportLoaderManager().restartLoader(Constants.LOADER_TICKETS, null, TicketsFragment.this);
    }

    public void forceDismiss(int position) {
        setScrollPosition();
        vSwipeDismissList.forceDismiss(position);
    }

    private void showProgress() {
        vList.setVisibility(View.GONE);
        vEmpty.setVisibility(View.GONE);
        vProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        vList.setVisibility(View.VISIBLE);
        vProgress.setVisibility(View.GONE);
    }

    private void setScrollPosition() {
        mLastScrollIndex = vList.getFirstVisiblePosition();
        View v = vList.getChildAt(0);
        mLastScrollTop = (v == null) ? 0 : v.getTop();
    }
}
