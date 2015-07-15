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

package com.donellesandersjr.walmartbuddy.api;

import android.util.Log;

import com.donellesandersjr.walmartbuddy.BuildConfig;

public final class WBLogger {
    public static void Error (String tag, Exception e) {
        Error(tag, "", e);
    }

    public static void Error (String tag, String description, Exception e) {
        String message = e.getMessage();
        if (WBStringUtils.isNullOrEmpty(message)) {
            message = e.toString();
        }

        if (WBStringUtils.isNullOrEmpty(description))
            description = "";

        StringBuilder stackTrace = new StringBuilder();
        String newline = System.getProperty("line.separator");
        for (StackTraceElement stack : e.getStackTrace())
            stackTrace.append(stack.toString() + newline);

        Log.e(tag, description + "-" + message);
        Log.e(tag, "stackTrace - " + stackTrace.toString());
    }

    public static void Error (String tag, String info) {
        Log.e(tag, info);
    }

    public static void Debug (String tag, String info) {
        if (BuildConfig.DEBUG)
            Log.d(tag, info);
    }
}
