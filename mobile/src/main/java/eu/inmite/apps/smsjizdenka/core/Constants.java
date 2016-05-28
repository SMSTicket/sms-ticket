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

package eu.inmite.apps.smsjizdenka.core;


/**
 * Various global constants/configuration.
 */
public class Constants {

    public static final int EXPIRING_MINUTES = 10;
    public static final int WARNING_REPEAT_PURCHASE_SECONDS = 60;

    public static final int BROADCAST_SMS_SENT = 1;
    public static final int BROADCAST_SMS_DELIVERED = 2;

    public static final String DATA_URL_BASE = "https://raw.githubusercontent.com/avast/sms-ticket/master/mobile/src/main/assets/";

    public static final int LOADER_TICKETS = 1;
    public static final int LOADER_CITIES = 2;
    public static final int LOADER_CITY_TICKETS = 3;
    public static final int LOADER_STATISTICS = 4;

    private Constants() {}
}