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

package eu.inmite.apps.smsjizdenka.dialog;

import java.util.List;

import android.view.View;
import android.widget.AdapterView;

import com.avast.android.dialogs.core.BaseDialogFragment;
import eu.inmite.apps.smsjizdenka.adapter.ListAdapter;

/**
 * Dialog for selecting from list.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class ListDialogFragment extends BaseDialogFragment {

    public static String TAG = "list";
    // TODO: static fields for fragment arguments are bad practice
    // TODO: StyledDialogs library already has ListDialogFragment, replace
    private static List<String> sItems;
    private static SelectedListener sListener;


    public static ListDialogFragment newInstance(List<String> items, SelectedListener listener) {
        ListDialogFragment dialog = new ListDialogFragment();
        sItems = items;
        sListener = listener;
        return dialog;
    }

    @Override
    protected Builder build(Builder builder) {
        builder.setItems(new ListAdapter(getActivity(), sItems), -1, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sListener.onSelected(i);
                dismiss();
            }
        });
        return builder;
    }

    public interface SelectedListener {
        public void onSelected(int position);
    }
}
