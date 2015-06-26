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

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * Use this class instead of AsyncTasks everywhere by default.
 * <p/>
 * <p>It guarantees that it will run in parallel (Honeycomb and later) and will not be blocked by other AsyncTasks.</p>
 * <p/>
 * <p>It uses own executor for better and faster cleanup and for avoiding of interfering with other things like image loaders.</p>
 * <p/>
 * <p>It also removes Void parameters which are not necessary in most cases. Use constructor instead of input parameters and member
 * variables instead of return value from inBackground().</p>
 * <p/>
 * <p><b>Don't forget to start the task via start() and not execute().</b></p>
 *
 * @author David Vávra (david@inmite.eu)
 * @author carnero (carnero@inmite.eu)
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public abstract class BaseAsyncTask extends AsyncTask<Void, Integer, Void> {

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(r, "BaseAsyncTask #" + mCount.getAndIncrement());
            t.setPriority(Thread.NORM_PRIORITY - 3);
            return t;
        }
    };
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>(256);
    private static int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
        CPU_CORES,
        (CPU_CORES + 1) * 2,
        1,
        TimeUnit.SECONDS,
        sPoolWorkQueue,
        sThreadFactory
    );

    public void start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            executor.purge(); // remove cancelled tasks
            executeOnExecutor(executor);
        } else {
            execute();
        }
    }

    @Override
    protected final Void doInBackground(Void... params) {
        doInBackground();
        return null;
    }

    @Override
    protected final void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onPostExecute();
    }

    public abstract void doInBackground();

    public void onPostExecute() {

    }
}