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

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreferences {
    public static final String PREFERENCE_NAME = "WalmartBuddy";
    public static final String PREFERENCE_ZIPCODE = "zipcode";

    private static AppPreferences appPreferences;
    private SharedPreferences _sharedPreferences;

    private AppPreferences(SharedPreferences preferences) {
        _sharedPreferences = preferences;
    }

    private SharedPreferences getSharedPreferences () {
        return _sharedPreferences;
    }

    public static void initialize (Context context) {
        if (appPreferences == null) {
            appPreferences = new AppPreferences(context.getSharedPreferences(PREFERENCE_NAME, 0));
            //
            // Set defaults
            //
            SharedPreferences sharedPreferences = appPreferences.getSharedPreferences();
            if (!sharedPreferences.contains(PREFERENCE_ZIPCODE))
                savePreference(PREFERENCE_ZIPCODE, "");
        }
    }

    public static void savePreference (String key, String value) {
        SharedPreferences.Editor editor = appPreferences.getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getStringPreference (String key, String defaultVal) {
        return appPreferences.getSharedPreferences().getString(key, defaultVal);
    }
}
