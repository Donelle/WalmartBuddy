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
    public static final String PREFERENCE_DISPLAY_TAXRATE_SETUP = "displayTaxRateSetup";

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
            if (!sharedPreferences.contains(PREFERENCE_DISPLAY_TAXRATE_SETUP))
                savePreference(PREFERENCE_DISPLAY_TAXRATE_SETUP, false);
        }
    }

    public static AppPreferences savePreference (String key, String value) {
        SharedPreferences.Editor editor = appPreferences.getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
        return appPreferences;
    }

    public static AppPreferences savePreference (String key, boolean val) {
        SharedPreferences.Editor editor = appPreferences.getSharedPreferences().edit();
        editor.putBoolean(key, val);
        editor.apply();
        return appPreferences;
    }

    public static String getStringPreference (String key) {
        return appPreferences.getSharedPreferences().getString(key, "");
    }

    public static boolean getBooleanPreference (String key) {
        return appPreferences.getSharedPreferences().getBoolean(key, false);
    }
}
