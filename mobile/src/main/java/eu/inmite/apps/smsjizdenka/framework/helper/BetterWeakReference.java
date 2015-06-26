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

package eu.inmite.apps.smsjizdenka.framework.helper;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Add WeakReference that refer equals() and hashCode() methods to reference object.
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 */
public class BetterWeakReference<T> extends WeakReference<T> {

    /**
     * Constructs a new weak reference to the given referent. The newly created
     * reference is not registered with any reference queue.
     *
     * @param r the referent to track
     */
    public BetterWeakReference(T r) {
        super(r);
    }

    /**
     * Constructs a new weak reference to the given referent. The newly created
     * reference is registered with the given reference queue.
     *
     * @param r the referent to track
     * @param q the queue to register to the reference object with. A null value
     *          results in a weak reference that is not associated with any
     *          queue.
     */
    public BetterWeakReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }

    @Override
    public boolean equals(Object o) {
        T value = get();
        if (o instanceof WeakReference<?> && value != null) {
            return value.equals(((WeakReference<?>)o).get());
        }
        return false;
    }

    @Override
    public int hashCode() {
        T value = get();
        if (value != null) {
            return value.hashCode();
        }
        return super.hashCode();
    }

}
