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
package com.donellsandersjr.walmartbuddy;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


public final class AppUI {
    private static String TAG = "com.donellesandersjr.walmart.AppUI";

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

}
