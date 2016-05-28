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

package eu.inmite.apps.smsjizdenka.framework.about;

/**
 * Definition of open-source libs.
 */
public class Constants {

    /**
     * Known licenses:
     */
    public static final License APACHE = new License("Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0");
    public static final License MIT = new License("MIT", "http://opensource.org/licenses/mit-license.php");
    public static final License APACHE_PLAY_SERVICES = new License("Apache 2.0 + attribution", "GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo()"); // special handling
    public static final License ISC = new License("ORM Lite License", "http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_9.html#License");
    public static final License BSD3 = new License("BSD 3-Clause License", "http://opensource.org/licenses/BSD-3-Clause");

    /**
     * Add more libraries if you use more than it's in this list.
     */
    public static final Library[] KNOWN_LIBRARIES = {
        new Library("com.actionbarsherlock.app.ActionBar", "ActionBarSherlock", "Jake Wharton", APACHE, "https://github.com/JakeWharton/ActionBarSherlock"),
        new Library("com.viewpagerindicator.PagerIndicator", "ViewPagerIndicator", "Jake Wharton", APACHE, "https://github.com/JakeWharton/Android-ViewPagerIndicator"),
        new Library("com.nineoldandroids.view.ViewHelper", "NineOldAndroids", "Jake Wharton", APACHE, "https://github.com/JakeWharton/NineOldAndroids"),
        new Library("com.jakewharton.disklrucache.DiskLruCache", "DiskLruCache", "Jake Wharton", APACHE, "https://github.com/JakeWharton/DiskLruCache"),
        new Library("com.nostra13.universalimageloader.core.ImageLoader", "Universal Image Loader", "Sergey Tarasevich", APACHE, "https://github.com/nostra13/Android-Universal-Image-Loader"),
        new Library("com.github.kevinsawicki.http.HttpRequest", "Http Request", "Kewin Sawacki", MIT, "https://github.com/kevinsawicki/http-request"),
        new Library("com.fasterxml.jackson.core.JsonParser", "Jackson", "FasterXML, LLC", APACHE, "https://github.com/FasterXML/jackson"),
        new Library("com.integralblue.httpresponsecache.HttpResponseCache", "HttpResponseCache", "Craig", APACHE, "https://github.com/candrews/HttpResponseCache"),
        new Library("com.google.zxing.Reader", "zxing", "zxing team", APACHE, "https://code.google.com/p/zxing/"),
        new Library("butterknife.ButterKnife", "Butter Knife", "Square", APACHE, "https://github.com/JakeWharton/butterknife"),
        new Library("com.squareup.picasso.Picasso", "Picasso", "Square", APACHE, "https://github.com/square/picasso"),
        new Library("com.squareup.okhttp.OkHttpClient", "OK HTTP", "Square", APACHE, "https://github.com/square/okhttp"),
        new Library("retrofit.RestAdapter", "Retrofit", "Square", APACHE, "https://github.com/square/retrofit"),
        new Library("com.squareup.otto.Bus", "Otto", "Square", APACHE, "https://github.com/square/otto"),
        new Library("android.support.v4.app.Fragment", "Android Support Library", "Google", APACHE, "http://developer.android.com/tools/support-library/index.html"),
        new Library("android.support.v7.app.ActionBar", "Android Appcompat Library", "Google", APACHE, "https://developer.android.com/tools/support-library/features.html#v7-appcompat"),
        new Library("com.google.gson.Gson", "GSON", "Google", APACHE, "https://code.google.com/p/google-gson/"),
        new Library("com.google.android.gms.common.GooglePlayServicesUtil", "Play Services", "Google", APACHE_PLAY_SERVICES, "http://developer.android.com/google/play-services/index.html"),
        new Library("com.android.volley.Request", "Volley", "Google", APACHE, "https://android.googlesource.com/platform/frameworks/volley"),
        new Library("com.avast.android.dialogs.core.BaseDialogFragment", "StyledDialogs for Android", "Avast Sofware", APACHE, "https://github.com/inmite/android-styled-dialogs"),
        new Library("pl.mg6.android.maps.extensions.SupportMapFragment", "Android Maps Extensions", "Maciej Górski", APACHE, "https://code.google.com/p/android-maps-extensions/"),
        new Library("com.caldroid.CaldroidFragment", "CalDroid", "Roomorama", MIT, "https://github.com/roomorama/Caldroid"),
        new Library("org.simpleframework.xml.core.Persister", "Simple", "Niall Gallagher", APACHE, "http://simple.sourceforge.net/home.php"),
        new Library("com.j256.ormlite.dao.Dao", "ORM Lite", "Gray Watson", ISC, "http://ormlite.com/"),
        new Library("com.pnikosis.materialishprogress.ProgressWheel", "Material-ish Progress", "Nicolás Hormazál", APACHE, "htps://github.com/pnikosis/materialish-progress"),
        new Library("com.google.protobuf.Parser", "Protocol Buffers", "Google", BSD3, "https://code.google.com/p/protobuf"),
        new Library("com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView", "StickyGridHeaders", "Tonic Artos", APACHE, "https://github.com/TonicArtos/StickyGridHeaders"),
        new Library("com.tonicartos.superslim.GridSLM", "SuperSLiM", "Tonic Artos", APACHE, "https://github.com/TonicArtos/SuperSLiM"),
        new Library("com.hudomju.swipe.OnItemClickListener", "Swipe to dismiss", "Hugo Doménech Juárez", MIT, "https://github.com/hudomju/android-swipe-to-dismiss-undo"),
        new Library("de.keyboardsurfer.android.widget.crouton.Crouton", "Crouton", "Benjamin Weiss", APACHE, "https://github.com/keyboardsurfer/Crouton"),
        new Library("com.github.kovmarci86.android.secure.preferences.SecureSharedPreferences", "Secure Preferences", "Marcell Kovacs", APACHE, "https://github.com/kovmarci86/android-secure-preferences"),
    };

    private Constants() {}

    public static class Library {
        public String name;
        public String author;
        public String projectWebsite;
        public License license;
        public String significantClass;

        Library(String significantClass, String name, String author, License license, String projectWebsite) {
            this.name = name;
            this.author = author;
            this.projectWebsite = projectWebsite;
            this.license = license;
            this.significantClass = significantClass;
        }
    }

    public static class License {
        public String name;
        public String url;

        License(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
