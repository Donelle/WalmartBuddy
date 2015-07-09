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

package com.donellsandersjr.walmartbuddy.api;

import org.json.JSONArray;
import org.json.JSONObject;

public final class WBJsonUtils {

    public static String getString (JSONObject json, String field, String defaultVal) {
        try {
            String val = json.getString(field);
            if (!WBStringUtils.isNullOrEmpty(val))
                return val;
        } catch (Exception e) {}
        return defaultVal;
    }

    public static long getLong (JSONObject json, String field, long defaultVal) {
        try {
            return json.getLong(field);
        } catch (Exception e) {}
        return defaultVal;
    }

    public static boolean getBool (JSONObject json, String field, boolean defaultVal) {
        try {
            return json.getBoolean(field);
        } catch (Exception e) {}
        return defaultVal;
    }

    public static double getDouble (JSONObject json, String field, double defaultVal) {
        try {
            return Double.valueOf(json.getDouble(field)).floatValue();
        } catch (Exception ex) {}
        return defaultVal;
    }

    public static JSONObject getObject (JSONArray jsonArray, int pos, JSONObject defaultVal) {
        try {
            JSONObject val = jsonArray.getJSONObject(pos);
            if (val != null) return val;
        } catch (Exception e) {}
        return defaultVal;
    }
}
