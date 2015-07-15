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
package com.donellsandersjr.walmartbuddy.web;

import android.net.Uri;

import com.donellsandersjr.walmartbuddy.api.WBLogger;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

abstract class WebAPI {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.web.WebAPI";

    static URL buildUrl (String query, HashMap<String, String> parameters) throws MalformedURLException {
        Uri.Builder uriBuilder = Uri.parse(query).buildUpon();
        for (Map.Entry<String, String> entry : parameters.entrySet())
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        return new URL(uriBuilder.toString());
    }

    static JSONObject readFrom (InputStream stream) {
        JSONObject response = new JSONObject();
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            response = new JSONObject(responseStrBuilder.toString());
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        }
        return response;
    }
}
