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
package eu.inmite.apps.smsjizdenka.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView in custom font.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class SegoePrintTextView extends TextView {

    public SegoePrintTextView(Context context) {
        super(context);
        setFont(context);
    }

    public SegoePrintTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont(context);
    }

    public SegoePrintTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context);
    }

    protected void setFont(Context context) {
        Typeface tfs = Typeface.createFromAsset(context.getAssets(), "SegoePrint.ttf");
        setTypeface(tfs);
    }


}