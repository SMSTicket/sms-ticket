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

package eu.inmite.apps.smsjizdenka.framework.services;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.SparseArray;

import eu.inmite.apps.smsjizdenka.framework.helper.BetterWeakReference;
import eu.inmite.apps.smsjizdenka.framework.interfaces.IService;


/**
 * Global one way asynchronous notification mechanism between parts of application based on Handler class.
 * For example from service to activities.
 * <p/>
 * <p>
 * Notification are always delivered to the UI thread.
 * </p>
 * <p/>
 * <p>
 * This class keep all listeners as weak reference so it is save to add whole activities as listeners, but do not use
 * anonymous listeners.
 * </p>
 * <p/>
 * <p>
 * Reference to this class should be obtained via <code>SL.get(getActivity(), GlobalObserverService.class)</code>
 * </p>
 * <p/>
 * <p>
 * <b>Usage:</b>
 * </p>
 * <p/>
 * <p>
 * Register listener:
 * <code>SL.get(getActivity(), GlobalObserverService.class).addListener(R.id.message_update_completed, this);</code>
 * </p>
 * <p>
 * Call listener:
 * <code>SL.get(getActivity(), GlobalObserverService.class).addListener(R.id.message_update_completed, this);</code>
 * </p>
 * <p/>
 * TODO: Use Otto instead of this.
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 */
public class GlobalHandlerService implements Callback, IService {

    Context mContext;

    private SparseArray<List<BetterWeakReference<Callback>>> mListenersSpecific = new SparseArray<List<BetterWeakReference<Callback>>>();
    private List<BetterWeakReference<Callback>> mListenersUniversal = new ArrayList<BetterWeakReference<Callback>>();

    public GlobalHandlerService(Context context) {
        super();
        this.mContext = context;
    }

    /**
     * Add listener for all messages.
     * <p/>
     * <p>Warning: Listener is referenced via weak reference, do not use annonymous class!</p>
     */
    public synchronized void addListener(Callback listener) {
        BetterWeakReference<Callback> r = new BetterWeakReference<Callback>(listener);
        if (!mListenersUniversal.contains(r)) {
            mListenersUniversal.add(r);
        }
    }

    /**
     * Remove listener for all messages.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeListener(Callback listener) {
        BetterWeakReference<Callback> r = new BetterWeakReference<Callback>(listener);
        if (mListenersUniversal.contains(r)) {
            mListenersUniversal.remove(r);
        }
    }

    /**
     * Remove all listeners for desired message ID.
     *
     * @param what The id of the message to stop listening to.
     */
    public synchronized void removeListeners(int what) {
        mListenersSpecific.delete(what);
    }


    /*
     * (non-Javadoc)
     *
     * @see android.os.Handler.Callback#handleMessage(android.os.Message)
     */
    @Override
    public boolean handleMessage(Message msg) {
        // proces listeners for specified type of message what
        synchronized (mListenersSpecific) {
            List<BetterWeakReference<Callback>> whatListofListeners = mListenersSpecific.get(msg.what);
            if (whatListofListeners != null) {
                handleListeners(whatListofListeners, msg);
                if (whatListofListeners.size() == 0) {
                    mListenersSpecific.remove(msg.what);
                }
            }
        }

        // process universal listeners
        handleListeners(mListenersUniversal, msg);

        return true;
    }

    private void handleListeners(List<BetterWeakReference<Callback>> whatListofListeners, Message msg) {
        synchronized (whatListofListeners) {
            for (BetterWeakReference<Callback> ComparableWeakReference : whatListofListeners) {
                if (ComparableWeakReference.get() != null) {
                    ComparableWeakReference.get().handleMessage(msg);
                } else {
                    whatListofListeners.remove(ComparableWeakReference);
                }
            }
        }
    }

}
