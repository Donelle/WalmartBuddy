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

package com.donellesandersjr.walmartbuddy.web;


import com.donellesandersjr.walmartbuddy.api.WBJsonUtils;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.models.StoreModel;

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

    public static Task<Double> fetchTaxRate (StoreModel storeModel) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(QueryParams.COUNTRY, "usa");
        params.put(QueryParams.STREET, storeModel.getAddress());
        params.put(QueryParams.CITY, storeModel.getCity());
        params.put(QueryParams.STATE, storeModel.getState());
        params.put(QueryParams.POSTAL, storeModel.getZipCode());
        return Task.callInBackground(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                double taxRate = 0;
                URL url = buildUrl(AVALARAPI_SEARCHQUERY, params);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "AvalaraApiKey " + AVALARAPI_APIKEY);
                try {
                    JSONObject response = readFrom(connection.getInputStream(), JSONObject.class);
                    if (response != null && response.has(Response.TOTALRATE)) {
                        //WBLogger.Debug(TAG, response.toString());
                        taxRate = WBJsonUtils.getDouble(response, Response.TOTALRATE, 0);
                    }
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

    /**
     * API Response entries
     *
     * Tax Rates returns a JSON object that contains a list of individual jurisdictions' tax rates,
     * as well as a rate summary.
     *
     * @reference http://taxratesapi.avalara.com/docs
     */
    static class Response {
        /**
         * double
           Contains the total tax rate for the location in question.
           Note that it is not a percentage; in the example above,
           the totalRate of 0.086 represents a tax rate of 8.6%.
         */
        static final String TOTALRATE = "totalRate";
    }

    /**
     * Query Parameters
     */
    public class QueryParams {
        /**
         * first line of the address
         */
        static final String STREET = "street";
        /**
         * city of the address
         */
        static final String CITY = "city";
        /**
         * state or region of the address
         */
        static final String STATE = "state";
        /**
         * country code in ISO 3166-1 alpha-3 format
         */
        static final String COUNTRY = "country";
        /**
         * postal code of the address
         */
        static final String POSTAL = "postal";
    }
}
