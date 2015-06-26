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

package eu.inmite.apps.smsjizdenka.activity;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mariux.teleport.lib.TeleportClient;

import eu.inmite.apps.smsjizdenka.core.BusProvider;

/**
 * Michal Mátl (matl)
 */
public abstract class ProjectBaseActivity extends Activity {

    private TeleportClient mTeleportClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTeleportClient = new TeleportClient(this);
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTeleportClient.connect();
        Wearable.NodeApi.getConnectedNodes(mTeleportClient.getGoogleApiClient()).setResultCallback(new ResultCallback<NodeApi
            .GetConnectedNodesResult>() {


            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                if (getConnectedNodesResult.getNodes().size() > 0) {
                    startConnected();
                } else {
                    startDisconnected();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTeleportClient.disconnect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    /**
     * Override in child to handle disconnected state.
     */
    protected void startDisconnected() {

    }

    /**
     * Override in child to handle connected state.
     */
    protected void startConnected() {

    }

    public TeleportClient getTeleport() {
        return mTeleportClient;
    }

}
