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

package eu.inmite.apps.smsjizdenka.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;

/**
 * Fragment displaying big action and label, based on Wear Design Guidelines.
 *
 * @author David Vávra (david@vavra.me)
 */
public class ActionFragment extends Fragment implements View.OnClickListener {

    private Listener mListener;

    public static ActionFragment create(int iconResId, int labelResId, Listener listener) {
        ActionFragment fragment = new ActionFragment();
        fragment.mListener = listener;
        Bundle args = new Bundle();
        args.putInt("ICON", iconResId);
        args.putInt("LABEL", labelResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CircledImageView vIcon = (CircledImageView)view.findViewById(R.id.icon);
        TextView vLabel = (TextView)view.findViewById(R.id.label);
        vIcon.setImageResource(getArguments().getInt("ICON"));
        vLabel.setText(getArguments().getInt("LABEL"));
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onActionPerformed();
    }

    public interface Listener {
        public void onActionPerformed();
    }
}