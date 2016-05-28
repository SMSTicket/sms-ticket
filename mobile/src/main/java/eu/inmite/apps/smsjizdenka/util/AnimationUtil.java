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
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Animation Utils
 *
 * @author David Vávra (david@inmite.eu)
 */
public class AnimationUtil {

    public static int NO_ANIMATION = -1;

    private AnimationUtil() {}

    public static void addAnimationToView(final View view, final int animation) {
        if (view == null) {
            return;
        }
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), animation);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }
        });
        view.startAnimation(anim);
    }

    public static void setFlipAnimation(final ImageView view, final ObjectAnimator animator, final int firstImage,
                                        final int secondImage, final Context c) {
        if (secondImage == NO_ANIMATION) {
            view.setImageResource(firstImage);
            animator.end();
            ViewCompat.setHasTransientState(view, false);
        } else {
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setDuration(1300);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatMode(ValueAnimator.RESTART);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                final Drawable shape1 = c.getResources().getDrawable(firstImage);
                final Drawable shape2 = c.getResources().getDrawable(secondImage);
                Drawable currentDrawable = null;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float angle = (Float)animation.getAnimatedValue();
                    int quadrant = (int)(angle / 90) + 1;
                    if ((quadrant == 1 || quadrant == 4) && shape1 != currentDrawable) {
                        view.setImageDrawable(shape1);
                        currentDrawable = shape1;
                    } else if ((quadrant == 2 || quadrant == 3) && currentDrawable != shape2) {
                        view.setImageDrawable(shape2);
                        currentDrawable = shape2;
                    }
                }
            });
            animator.start();
            ViewCompat.setHasTransientState(view, true);
        }
    }
}
