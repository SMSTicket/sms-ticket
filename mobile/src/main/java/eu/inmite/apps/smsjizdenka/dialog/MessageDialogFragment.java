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

import android.os.Bundle;
import android.view.View;

import com.avast.android.dialogs.core.BaseDialogFragment;
import eu.inmite.apps.smsjizdenka.R;

/**
 * Dialog for displaying message from evil empire.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class MessageDialogFragment extends BaseDialogFragment {

    public static String TAG = "message";

    public static MessageDialogFragment newInstance(String message) {
        MessageDialogFragment dialog = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    protected Builder build(Builder builder) {
        builder.setMessage(getMessage());
        builder.setPositiveButton(R.string.tickets_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return builder;
    }

    private String getMessage() {
        return getArguments().getString("message");
    }
}
