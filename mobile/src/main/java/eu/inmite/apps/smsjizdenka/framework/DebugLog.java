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

import java.util.LinkedHashSet;
import java.util.Set;

import android.os.Build;
import android.util.Log;

/**
 * Convenient encapsulation of default Log implementation.
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 *         Ondra Zahradník &lt;<a href="mailto:ondra.zahradnik@inmite.eu">ondra.zahradnik@inmite.eu</a>&gt;
 */
public final class DebugLog {

    private static String defaultLogTag = "avast-app";
    private static int sLoggingLevel = Log.WARN;
    private static Set<IEventCallback> sEventCallback = new LinkedHashSet<>();

    private DebugLog() {

    }

    public static boolean isDebugLoggingEnabled() {
        return sLoggingLevel < Log.ERROR;
    }

    /**
     * Will set error logging to all if enabled or to ERROR and ASSETS if not.
     */
    public static void setDebugLoggingEnabled(boolean enabled) {
        if (enabled) {
            setLoggingLevel(Log.VERBOSE);
        } else {
            setLoggingLevel(Log.WARN);
        }
    }

    /**
     * Selected and all higher levels will be logged to  the logcat.
     */
    public static void setLoggingLevel(Level level) {
        sLoggingLevel = level.value;
    }

    /**
     * Selected and all higher levels will be logged to the logcat.
     *
     * @param level {@link Log#VERBOSE}, {@link Log#DEBUG}, {@link Log#INFO}, {@link Log#WARN}, {@link Log#ERROR}, {@link Log#ASSERT} or 0
     */
    public static void setLoggingLevel(int level) {
        if (level == 0) {
            level = Level.NONE.value;
        }
        sLoggingLevel = level;
    }


    public static void setDefaultLogTag(String tag) {
        defaultLogTag = tag;
    }

    public static int v(String msg) {
        return v(defaultLogTag, msg);
    }

    public static int v(String tag, String msg) {
        return v(tag, msg, null);
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        if (sLoggingLevel <= Log.VERBOSE) {
            result = Log.v(tag, msg, tr);
        }
        callEvent(Level.VERBOSE, tag, msg, tr);
        return result;
    }

    public static int d(String msg) {
        return d(defaultLogTag, msg);
    }

    public static int d(String tag, String msg) {
        return d(tag, msg, null);
    }

    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        if (sLoggingLevel <= Log.DEBUG) {
            result = Log.d(tag, msg, tr);
        }
        callEvent(Level.DEBUG, tag, msg, tr);
        return result;
    }

    public static int i(String tag, String msg) {
        return i(tag, msg, null);
    }

    public static int i(String msg) {
        return i(defaultLogTag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        int result = 0;
        if (sLoggingLevel <= Log.INFO) {
            result = Log.i(tag, msg, tr);
        }
        callEvent(Level.INFO, tag, msg, tr);
        return result;
    }

    public static int w(String tag, String msg) {
        return w(tag, msg, null);
    }

    public static int w(String msg) {
        return w(defaultLogTag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        if (sLoggingLevel <= Log.WARN) {
            result = Log.w(tag, msg, tr);
        }
        callEvent(Level.WARN, tag, msg, tr);
        return result;
    }

    public static int w(String msg, Throwable tr) {
        return w(defaultLogTag, msg, tr);
    }

    public static int e(String msg) {
        return e(defaultLogTag, msg);
    }

    public static int e(String tag, String msg) {
        return e(tag, msg, null);
    }

    public static int e(String msg, Throwable tr) {
        return e(defaultLogTag, msg, tr);
    }

    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        if (sLoggingLevel <= Log.ERROR) {
            result = Log.e(tag, msg, tr);
        }

        callEvent(Level.ERROR, tag, msg, tr);
        return result;
    }

    /**
     * Use this in situation where you think that an error should never happen.
     * It will be logged as handled exception.
     */
    public static int wtf(String msg) {
        return wtf(defaultLogTag, msg, null);
    }

    /**
     * Use this in situation where you think that an error should never happen.
     * It will be logged as handled exception.
     */
    public static int wtf(String tag, String msg) {
        return wtf(tag, msg, null);
    }

    /**
     * Use this in situation where you think that an error should never happen.
     * It will be logged as handled exception.
     */
    public static int wtf(String msg, Throwable tr) {
        return wtf(defaultLogTag, msg, tr);
    }

    /**
     * Use this in situation where you think that an error should never happen.
     * It will be logged as handled exception
     */
    public static int wtf(String tag, String msg, Throwable tr) {

		/*
        try {
			if (tr != null) {
				GAHelper.sendException("handledError: " + msg + "(" + tr.getMessage() + ")", tr, false);
			} else {
				GAHelper.sendException("handledError: " + msg, false);
			}
		} catch (Throwable ex) {
			DebugLog.w("GoogleAnalytics fail");
		}

		try {
			if (tr != null) {
				FlurryHelper.onError("handledError: " + msg, tr.getMessage(), tr.getClass().getName());
			} else {
				FlurryHelper.onError("handledError: " + msg, msg, null);
			}
		} catch (Throwable ex) {
			DebugLog.w("Flurry fail");
		}
		*/


        int result = 0;
        if (sLoggingLevel <= Log.ASSERT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                result = Log.wtf(tag, msg, tr);
            } else {
                result = Log.e(tag, msg, tr);
            }
        }

        // log handled exception
        if (tr != null) {
            callHandledException(tag, msg, tr);
        }
        // log as event
        callEvent(Level.ASSERT, tag, msg, tr);

        return result;
    }

    /**
     * Add new event callback to process logging od handled exception.
     */
    public static void addEventCallback(IEventCallback callback) {
        sEventCallback.add(callback);
    }

    public static void removeAllEventCallbacks() {
        sEventCallback.clear();
    }

    private static void callHandledException(String tag, String message, Throwable exception) {
        HandledException enhancedException = new HandledException(message, exception);
        try {
            for (IEventCallback callback : sEventCallback) {
                callback.onHandledException(tag, message, enhancedException, exception);
            }
        } catch (Exception ex) {
            Log.wtf(defaultLogTag, "DebugLog call callback failed ", ex);
        }
    }

    private static void callEvent(Level level, String tag, String message, Throwable throwable) {
        if (throwable != null) {
            message += throwable.getClass().getSimpleName() + " - " + throwable.getMessage();
        }

        try {
            for (IEventCallback callback : sEventCallback) {
                callback.onEvent(level, tag, message);
            }
        } catch (Exception ex) {
            Log.wtf(defaultLogTag, "DebugLog call callback failed ", ex);
        }
    }

    public static enum Level {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT),
        NONE(10);

        int value;

        private Level(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static interface IEventCallback {
        public void onEvent(Level level, String tag, String message);

        /**
         * Will report exception logged by DebugLog.wtf();
         *
         * @param message           original message
         * @param enhancedException original exception wrapped to {@link HandledException} which contain also message.
         * @param originalException original exception
         */
        public void onHandledException(String tag, String message, HandledException enhancedException, Throwable originalException);
    }

    public static class HandledException extends Exception {
        public HandledException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }

}