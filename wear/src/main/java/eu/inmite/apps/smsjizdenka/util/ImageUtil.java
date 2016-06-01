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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @author Michal Matl (matl)
 */
public class ImageUtil {

    private ImageUtil() {}

    public static Bitmap combineTwoImages(Context context, int backgroundResource, int color) {

        Bitmap bmpBackground = null;
        Bitmap big = null;
        if (backgroundResource == 0) {
            big = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        } else {
            bmpBackground = BitmapFactory.decodeResource(context.getResources(), backgroundResource);
            big = Bitmap.createBitmap(bmpBackground.getWidth(), bmpBackground.getHeight(), Bitmap.Config.ARGB_8888);
        }


        Canvas canvas = new Canvas(big);
        if (bmpBackground != null) {
            canvas.drawBitmap(bmpBackground, 0, 0, null);
        }
        canvas.drawColor(color);

        Drawable d = new BitmapDrawable(context.getResources(), big);

        return drawableToBitmap(d);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
