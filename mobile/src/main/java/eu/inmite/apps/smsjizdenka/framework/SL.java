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

import android.content.Context;

import eu.inmite.apps.smsjizdenka.framework.interfaces.IGetInstanceInit;


/**
 * Service locator class.
 * <p/>
 * Use call like <code>SL.get(contex, GlobalHandlerService.class)</code> to get singleton instance of desired
 * class or interface implementation.
 * <p/>
 * TODO: Use Dagger 2 instead of this.
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 */
public class SL {

    private SL() {}

    /**
     * Return instance of desired class or object that implement desired interface.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Context context, Class<T> clazz) {
        context = context.getApplicationContext();
        T instance = (T)context.getSystemService(clazz.getName());

        if (instance instanceof IGetInstanceInit) {
            ((IGetInstanceInit)instance).getInstanceInit();
        }

        return instance;
    }

    /**
     * Return instance of desired class or object that implement desired interface.
     * Context provided by App class will be used.
     */
    public static <T> T get(Class<T> clazz) {
        Context context = App.getInstance().getApplicationContext();
        return get(context, clazz);
    }
}
