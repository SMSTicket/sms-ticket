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

package eu.inmite.apps.smsjizdenka.util;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Locale support utils
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 * @since 11.1.13 18:16
 */
public class LocaleUtils {

    public static NumberFormat sCurrencyFormatter = null;

    public static Currency sDefautCurrency = Currency.getInstance("CZK");
    public static Locale sDefautLocale = null;

    /**
     * Will replace 5,00 Kč to 5,- Kč
     */
    public static boolean sPostFormatCurrency = false;

    private LocaleUtils() {}

    /**
     * Format number like 1 123 Kč.
     */
    private static NumberFormat getCurrencyFormatter() {
        if (sCurrencyFormatter == null) {
            if (sDefautLocale == null) {
                sCurrencyFormatter = NumberFormat.getCurrencyInstance();
            } else {
                sCurrencyFormatter = NumberFormat.getCurrencyInstance(sDefautLocale);
            }
        }
        return sCurrencyFormatter;
    }

    /**
     * Format value in desired currency.
     *
     * @param value price
     * @return formatted price
     */
    public static String formatCurrency(double value, Currency currency) {
        NumberFormat f = getCurrencyFormatter();
        f.setCurrency(currency);
        return postFormatCurrency(f.format(value));
    }

    /**
     * Replace ,00 by ,-. Is this used in other countries?
     */
    private static String postFormatCurrency(String currency) {
        if (sPostFormatCurrency) {
            return currency.replace(",00", ",-").replace(".00", ".-");
        } else {
            return currency;
        }
    }

}
