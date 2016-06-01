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
package eu.inmite.apps.smsjizdenka.framework.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Util class for launching other apps through Intents.
 * <p/>
 * <p>It will automatically add {@link Intent#FLAG_ACTIVITY_NEW_TASK}
 * if you will use application context instead of Activity for all methods in this class.</p>
 *
 * @author David Vávra (david@inmite.eu) and Tomáš Procházka (prochazka@avast.com)
 */
@SuppressWarnings("UnusedDeclaration")
public class IntentUtils {

    private IntentUtils() {}

    /**
     * Opens e-mail client, e.g. Gmail.
     *
     * @throws ActivityNotFoundException if no mail client
     */
    public static void sendEmail(Context context, String recipient) {
        sendEmail(context, new String[]{recipient}, null, null, null);
    }

    /**
     * Opens e-mail client e.g. Gmail.
     *
     * @throws ActivityNotFoundException if no mail client
     */
    public static void sendEmail(Context context, String[] recipients, String subject, String text, Uri stream) {
        Intent intent = createSendEmailIntent(recipients, subject, text, stream);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    /**
     * Creates intent for sending e-mail.
     */
    public static Intent createSendEmailIntent(String[] recipients, String subject, String text, Uri stream) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        if (recipients != null) {
            i.putExtra(Intent.EXTRA_EMAIL, recipients);
        }
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (!TextUtils.isEmpty(text)) {
            i.putExtra(Intent.EXTRA_TEXT, text);
        }
        if (stream != null) {
            i.putExtra(Intent.EXTRA_STREAM, stream);
        }
        return i;
    }

    /**
     * Open SMS client with prefilled email number and message.
     * <p/>
     * It automatically try also {@link Intent#ACTION_SENDTO} if {@link Intent#ACTION_VIEW} will fail.
     *
     * @throws ActivityNotFoundException if no SMS client
     */
    public static void sendSms(Context context, String phoneNumber, String message) {
        Intent smsIntent = createSmsIntent(phoneNumber, message);
        checkContext(context, smsIntent);
        try {
            context.startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            smsIntent.setAction(Intent.ACTION_SENDTO);
            context.startActivity(smsIntent);
        }
    }

    /**
     * Some android phones SMS clients doesn't support Intent.ACTION_VIEW, but Intent.ACTION_SENDTO only!
     * {@link #sendSms(Context, String, String)} will handle it.}
     */
    public static Intent createSmsIntent(String phoneNumber, String message) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + phoneNumber));
        smsIntent.putExtra("sms_body", message);
        return smsIntent;
    }

    /**
     * Opens external browser, e.g. Chrome.
     *
     * @throws ActivityNotFoundException if no broser available
     */
    public static void openBrowser(Context context, String url) {
        Intent intent = createOpenBrowserIntent(url);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    /**
     * Creates intent for opening browser.
     */
    public static Intent createOpenBrowserIntent(String url) {
        if (!url.contains("://")) {
            url = "http://" + url;
        }
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    /**
     * Pre-fills number in the dial screen, user needs to click to start calling.
     *
     * @throws ActivityNotFoundException
     */
    public static void callPhone(Context context, String phoneNumber) {
        Intent intent = createCallPhoneIntent(phoneNumber);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    /**
     * Creates Intent for calling a phone.
     */
    public static Intent createCallPhoneIntent(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        return callIntent;
    }

    /**
     * Opens Google Maps and centers to some location.
     *
     * @throws ActivityNotFoundException if no map application
     */
    public static void openMap(Context context, double latitude, double longitude) {
        Intent intent = createOpenMapIntent(latitude, longitude);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    /**
     * Opens Google Maps and searches for some place name or address.
     *
     * @throws ActivityNotFoundException if no map application
     */
    public static void openMapSearch(Context context, String addressOrPlaceName) {
        Intent intent = createOpenMapSearchIntent(addressOrPlaceName);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    /**
     * Opens Google Maps and searches for address or place name around some latitude, longitude.
     *
     * @throws ActivityNotFoundException if no map application
     */
    public static void openMapSearch(Context context, double latitude, double longitude, String addressOrPlaceName) {
        Intent intent = createOpenMapSearchIntent(latitude, longitude, addressOrPlaceName);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    /**
     * Creates Intent for centering to some location in Maps.
     */
    public static Intent createOpenMapIntent(double latitude, double longitude) {
        return createGeoUriIntent("geo:" + latitude + "," + longitude);
    }

    /**
     * Creates Intent for searching address or place name in Maps.
     */
    public static Intent createOpenMapSearchIntent(String addressOrPlaceName) {
        return createGeoUriIntent("geo:0,0?q=" + Uri.encode(addressOrPlaceName));
    }

    /**
     * Creates Intent for searching address or place name around some latitude, longitude in Maps.
     */
    public static Intent createOpenMapSearchIntent(double latitude, double longitude, String addressOrPlaceName) {
        final String uri;
        if (TextUtils.isEmpty(addressOrPlaceName)) {
            uri = "geo:" + latitude + "," + longitude;
        } else {
            uri = "geo:" + latitude + "," + longitude + "?q=" + Uri.encode(addressOrPlaceName);
        }
        return createGeoUriIntent(uri);
    }

    /**
     * Creates Intent for display exact location on the map with specified title and without searching.
     * <p/>
     * This may not work on different map that on google.
     */
    /*
    // This possibility was broken in the Google Maps v 7.x :-(
	   https://code.google.com/p/android/issues/detail?id=58507
	   http://stackoverflow.com/questions/17662814/new-update-of-google-maps-app-doesnt-display-a-mark-that-i-create-in-android

	public static Intent createOpenMapIntent(Activity activity, double latitude, double longitude, String placeTitle) {
		String uriBegin = "geo:" + latitude + "," + longitude;
		String query = latitude + "," + longitude + " (" + placeTitle.replaceAll("\\(.*?\\)", "") + ")";
		String encodedQuery = Uri.encode(query);
		String uriString = uriBegin + "?q=" + encodedQuery;
		return createGeoUriIntent(uriString);
	}
	*/
    private static Intent createGeoUriIntent(String uri) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

    /**
     * Launches navigation in Google Maps.
     *
     * @throws ActivityNotFoundException if no navigation application
     */
    public static void launchNavigation(Context context, double latitude, double longitude) {
        Intent intent = createLaunchNavigationIntent(latitude, longitude);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    public static Intent createLaunchNavigationIntent(double latitude, double longitude) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + latitude + "," + longitude));
    }

    /**
     * Ope Play with current application (for example for rating).
     *
     * @throws ActivityNotFoundException if no google play available
     */
    public static void openPlayStore(Context context) {
        openPlayStore(context, context.getPackageName());
    }

    /**
     * Ope Play with application specified by packageName
     *
     * @throws ActivityNotFoundException if no google play available
     */
    public static void openPlayStore(Context context, String packageName) {
        try {
            Intent i = createOldPlayStoreIntent(packageName);
            checkContext(context, i);
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Intent i = createPlayStoreIntent(packageName);
            checkContext(context, i);
            context.startActivity(i);
        }
    }

    /**
     * Run application by defined packageName.
     *
     * @param context     Activity or base context
     * @param packageName packageName of the app that will be started.
     */
    public static void openApplication(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        checkContext(context, intent);
        context.startActivity(intent);
    }

    /**
     * Create intent with old market: uri for compatibility with older Play.
     */
    public static Intent createOldPlayStoreIntent(String packageName) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("market://details?id=" + packageName));
        return i;
    }

    /**
     * Create intent with http://play.google.com/store/apps/details?id= uri.
     */
    public static Intent createPlayStoreIntent(final String packageName) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + packageName));
        return i;
    }


    /**
     * Will add {@link Intent#FLAG_ACTIVITY_NEW_TASK} to the intent if context is not activity.
     */
    private static void checkContext(Context context, Intent intent) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }
}
