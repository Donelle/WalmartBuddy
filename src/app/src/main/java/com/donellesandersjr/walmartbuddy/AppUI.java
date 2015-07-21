/*
 * Copyright (C) 2015 Donelle Sanders Jr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.donellesandersjr.walmartbuddy;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.donellesandersjr.walmartbuddy.api.WBImageUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public final class AppUI {
    private static String TAG = "com.donellesandersjr.walmart.AppUI";

    public static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    public static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    public static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    public static AlertDialog createAlert (Context ctx, Integer resourceMessageId) {
        return createAlert(ctx, resourceMessageId, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
    }

    public static android.app.AlertDialog createErrorAlert (Context ctx, String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ctx);
        builder.setTitle (R.string.app_name);
        builder.setIcon(R.mipmap.ic_error);
        builder.setMessage (message);
        builder.setPositiveButton (R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick (DialogInterface dialog, int id) {
                dialog.dismiss ();
            }
        });
        return builder.create();
    }

    public static AlertDialog createAlert (Context ctx, Integer resourceMessageId, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.app_name);
        builder.setMessage(resourceMessageId);
        builder.setPositiveButton(R.string.button_ok, listener);

        return builder.create();
    }


    public static ProgressDialog createProgressDialog (Activity activity, int resourceMessageId) {
        ProgressDialog dialog = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(activity.getString(resourceMessageId));
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static void loadImage  (final String filePath, final ImageView imageView) {
        Task.callInBackground(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
               return WBImageUtils.bitmapFromUri(URI.create(filePath));
            }
        }).onSuccess(new Continuation<Bitmap, Void>() {
            @Override
            public Void then(Task<Bitmap> task) throws Exception {
                final Bitmap thumbnail = task.getResult();
                imageView.setImageBitmap(thumbnail);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public static void loadImageUrl (final String urlPath,  final ImageView imageView) {
        Task.callInBackground(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return WBImageUtils.bitmapFromURL(new URL(urlPath));
            }
        }).onSuccess(new Continuation<Bitmap, Void>() {
            @Override
            public Void then(Task<Bitmap> task) throws Exception {
                final Bitmap thumbnail = task.getResult();
                imageView.setImageBitmap(thumbnail);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }



    /**
     * Scrolling animation implementaion
     * @ref https://guides.codepath.com/android/Floating-Action-Buttons
     */
    public static class FloatingActionButtonBehavior extends FloatingActionButton.Behavior {
        private boolean mIsAnimatingOut = false;

        public FloatingActionButtonBehavior (Context context, AttributeSet attrs) {
            super();
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                           FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                    super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                            nestedScrollAxes);
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                                   View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                    dyUnconsumed);

            if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
                animateOut(child);
            } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
                animateIn(child);
            }
        }

        // Same animation that FloatingActionButton.Behavior uses to
        // hide the FAB when the AppBarLayout exits
        private void animateOut(final FloatingActionButton button) {
            ViewCompat.animate(button).scaleX(0.0F).scaleY(0.0F).alpha(0.0F)
                    .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer()
                    .setListener(new ViewPropertyAnimatorListener() {
                        public void onAnimationStart(View view) {
                            FloatingActionButtonBehavior.this.mIsAnimatingOut = true;
                        }

                        public void onAnimationCancel(View view) {
                            FloatingActionButtonBehavior.this.mIsAnimatingOut = false;
                        }

                        public void onAnimationEnd(View view) {
                            FloatingActionButtonBehavior.this.mIsAnimatingOut = false;
                            view.setVisibility(View.GONE);
                        }
                    }).start();
        }

        // Same animation that FloatingActionButton.Behavior
        // uses to show the FAB when the AppBarLayout enters
        private void animateIn(FloatingActionButton button) {
            button.setVisibility(View.VISIBLE);
            ViewCompat.animate(button).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                    .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer().setListener(null)
                    .start();
        }
    }

    /**
     * Animates the main view to show the snackbar
     * @ref https://lab.getbase.com/introduction-to-coordinator-layout-on-android/
     */
    public static class SnackbarBehavior extends CoordinatorLayout.Behavior {
        public SnackbarBehavior (Context context, AttributeSet attrs) {
            super (context, attrs);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            child.setTranslationY(translationY);
            return true;
        }
    }
}
