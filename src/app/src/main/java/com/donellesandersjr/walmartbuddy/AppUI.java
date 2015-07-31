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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.donellesandersjr.walmartbuddy.api.WBImageUtils;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;

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

        private boolean _isAnimating, _didFling;
        private final ViewPropertyAnimatorListener animateOut = new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(View view) {
                _isAnimating = true;
            }

            @Override
            public void onAnimationEnd(View view) {
                view.setVisibility(View.GONE);
                _isAnimating = false;
            }

            @Override
            public void onAnimationCancel(View view) {
                _isAnimating = false;
            }
        };

        private final ViewPropertyAnimatorListener animateIn = new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(View view) {
                _isAnimating = true;
            }

            @Override
            public void onAnimationEnd(View view) {
                _isAnimating = false;
            }

            @Override
            public void onAnimationCancel(View view) {
                _isAnimating = false;
            }
        };

        public FloatingActionButtonBehavior (Context context, AttributeSet attrs) {
            super();
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                           FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
            return  nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout,
                                   FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            if (!_isAnimating && child.getVisibility() == View.VISIBLE) {
                ViewCompat.animate(child).scaleX(0.0F).scaleY(0.0F).alpha(0.0F)
                        .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .withLayer()
                        .setListener(animateOut)
                        .start();
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target) {
            if (child.getVisibility() == View.GONE || _didFling) {
                _didFling = false;
                if(_isAnimating)
                    ViewCompat.animate(child).cancel();

                child.setVisibility(View.VISIBLE);
                ViewCompat.animate(child).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                        .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .withLayer()
                        .setListener(animateIn)
                        .start();
            }
        }

        @Override
        public boolean onNestedFling(CoordinatorLayout coordinatorLayout,
                                     FloatingActionButton child, View target, float velocityX, float velocityY, boolean consumed) {
            _didFling = true;
            return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
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
