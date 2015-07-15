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


import com.donellsandersjr.walmartbuddy.api.WBJsonUtils;
import com.donellsandersjr.walmartbuddy.api.WBLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;

import bolts.Task;

public final class AvalaraAPI extends WebAPI {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.AvalaraAPI";

    private static final String AVALARAPI_APIKEY = "[YOUR KEY GOES HERE]";
    private static final String AVALARAPI_SEARCHQUERY = "https://taxrates.api.avalara.com:443/postal";

    public static Task<Double> fetchTaxRate (String zipcode) {
        final HashMap<String, String> params = new HashMap<>();
        params.put("country", "usa");
        params.put("postal", zipcode);

        return Task.callInBackground(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                double taxRate = 0;
                URL url = buildUrl(AVALARAPI_SEARCHQUERY, params);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "AvalaraApiKey " + AVALARAPI_APIKEY);
                try {
                    String TOTALRATE = "totalRate";
                    JSONObject response = readFrom(connection.getInputStream());
                    if (response.has(TOTALRATE))
                        taxRate = WBJsonUtils.getDouble(response, TOTALRATE, 0);
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                    throw ex;
                } finally {
                    connection.disconnect();
                }
                return taxRate;
            }
        });
    }
}
