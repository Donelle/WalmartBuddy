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
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.donellesandersjr.walmartbuddy.models.StoreModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import bolts.Task;

public final class WalmartAPI extends WebAPI {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.WalmartAPI";

    private static final String WALMART_APIKEY = "[YOUR KEY GOES HERE]";
    private static final String PRODUCT_SEARCH_QUERY = "http://api.walmartlabs.com/v1/items?apiKey=" + WALMART_APIKEY + "&format=json";
    private static final String STORE_QUERY =  "http://api.walmartlabs.com/v1/stores?apiKey=" + WALMART_APIKEY + "&format=json";

    public static Task<WBList<ProductModel>> fetchProductByUPC (String upc) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(QueryParams.UPC, upc);

        return Task.callInBackground(new Callable<WBList<ProductModel>>() {
            @Override
            public WBList<ProductModel> call() throws Exception {
                HttpURLConnection connection = null;
                WBList<ProductModel> products = new WBList<>();
                try {
                    URL url = buildUrl(PRODUCT_SEARCH_QUERY, params);
                    connection = (HttpURLConnection) url.openConnection();
                    JSONObject response = readFrom(connection.getInputStream(), JSONObject.class);
                    if (response != null && response.has(Response.ITEMS)) {
                        JSONArray items = response.getJSONArray(Response.ITEMS);
                        int len = items.length();
                        for (int i =0; i < len; i++) {
                            JSONObject item = WBJsonUtils.getObject(items, i, null);
                            if (item != null) {
                                ProductModel model =
                                    new ProductModel()
                                            .setName(WBJsonUtils.getString(item, FullItemResponse.NAME, null))
                                            .setDescription(WBJsonUtils.getString(item, FullItemResponse.SHORT_DESC, null))
                                            .setBrand(WBJsonUtils.getString(item, FullItemResponse.BRAND_NAME, null))
                                            .setThumbnailUrl(WBJsonUtils.getString(item, FullItemResponse.THUMBNAILURL, null))
                                            .setUPC(WBJsonUtils.getString(item, FullItemResponse.UPC, null))
                                            .setSalePrice(WBJsonUtils.getDouble(item, FullItemResponse.SALE_PRICE, 0))
                                            .setMSRP(WBJsonUtils.getDouble(item, FullItemResponse.MSRP, 0))
                                            .setProductId(WBJsonUtils.getLong(item, FullItemResponse.ITEMID, 0))
                                            .setProductCategoryId(WBJsonUtils.getString(item, FullItemResponse.CATEGORYID, null));
                                products.add(model);
                            }
                        }
                    }

                    if (products.size() == 0)
                        throw new WalmartAPIException(ErrorCodes.UPC_SEARCH_FAILED);

                } catch (Exception ex) {
                    if (connection != null) {
                        ArrayList<WalmartAPIException> exceptions = readErrorsFrom(connection.getErrorStream());
                        if (exceptions.size() > 0)
                            throw exceptions.get(0);
                    }
                    throw ex;
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
                return products;
            }
        });
    }

    public static Task<StoreModel> lookupStore (double longitude, double latitude) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(QueryParams.LONGITUDE, String.valueOf(longitude));
        params.put(QueryParams.LATITUDE, String.valueOf(latitude));
        return Task.callInBackground(new Callable<StoreModel>() {
            @Override
            public StoreModel call() throws Exception {
                return executeQuery(params);
            }
        });
    }

    public static Task<StoreModel> lookupStore (String zipCode) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(QueryParams.ZIP_CODE, zipCode);
        return Task.callInBackground(new Callable<StoreModel>() {
            @Override
            public StoreModel call() throws Exception {
                return executeQuery(params);
            }
        });
    }

    static StoreModel executeQuery (HashMap<String, String> params) throws Exception {
        HttpURLConnection connection = null;
        StoreModel storeModel = new StoreModel();
        try {
            URL url = buildUrl(STORE_QUERY, params);
            connection = (HttpURLConnection) url.openConnection();
            JSONArray response = readFrom(connection.getInputStream(), JSONArray.class);
            if (response != null) {
                int len = response.length();
                for (int i =0; i < len; i++) {
                    JSONObject item = WBJsonUtils.getObject(response, i, null);
                    if (item != null) {
                        storeModel = new StoreModel()
                                .setAddress(WBJsonUtils.getString(item, StoreResponse.ADDRESS, null))
                                .setCity(WBJsonUtils.getString(item, StoreResponse.CITY, null))
                                .setState(WBJsonUtils.getString(item, StoreResponse.STATE, null))
                                .setZipCode(WBJsonUtils.getString(item, StoreResponse.ZIP, null));
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            if (connection != null) {
                ArrayList<WalmartAPIException> exceptions = readErrorsFrom(connection.getErrorStream());
                if (exceptions.size() > 0)
                    throw exceptions.get(0);
            }
            throw ex;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return storeModel;
    }

    static ArrayList<WalmartAPIException> readErrorsFrom (InputStream stream) {
        ArrayList<WalmartAPIException> exceptions = new ArrayList<>();
        JSONObject response = readFrom(stream, JSONObject.class);
        if (response != null && response.has(Response.ERRORS)) {
            try {
                JSONArray errors = response.getJSONArray(Response.ERRORS);
                for (int i = 0; i < errors.length(); i++) {
                    JSONObject entry = errors.getJSONObject(i);
                    exceptions.add(new WalmartAPIException(WBJsonUtils.getInt(entry, "code", 0)));
                }
            } catch (Exception ex) {
                WBLogger.Error(TAG, ex);
            }
        }
        return exceptions;
    }

    public static class WalmartAPIException extends Exception {
        private int _errorCode;

        WalmartAPIException (int code) {
            super();
            _errorCode = code;
        }

        @Override
        public String getMessage() {
            switch (_errorCode) {
                case ErrorCodes.UPC_NOTFOUND:
                    return "Unable to find item by its barcode";
                case ErrorCodes.UPC_SEARCH_FAILED:
                    return "We lost connection searching for this item please rescan the item again";
                default:
                    return "Unexpected error occured";
            }
        }

        public int getErrorCode() {
            return _errorCode;
        }
    }

    /**
     * API Response entries
     */
    static class Response {
        static final String ITEMS = "items";
        static final String ERRORS = "errors";
    }

    /**
     * API Response Codes
     */
    public static class ErrorCodes {
        public static final int UPC_NOTFOUND = 4023;
        public static final int UPC_SEARCH_FAILED = 4024;
    }

    /**
     * Query Parameters
     */
    static class QueryParams {
        /**
         * upc of the item
         */
        static final String UPC = "upc";
        /**
         * zip
         */
        static final String ZIP_CODE = "zip";
        /**
         * longitude
         */
        static final String LONGITUDE = "lon";
        /**
         * latitude
         */
        static final String LATITUDE = "lat";
    }

    /**
     * Store Locator API helps locate nearest Walmart Stores via API.
     * It lets users search for stores by latitude and longitude, by zip code and by city.
     *
     *  @reference https://developer.walmartlabs.com/docs/read/Store_Locator_API
     */
    static class StoreResponse {
        static final String ADDRESS = "streetAddress";
        static final String CITY = "city";
        static final String STATE = "stateProvCode";
        static final String ZIP = "zip";
    }

    /**
     * Base Response is a smaller response that describes basic attributes for an item. It has been designed
     * for low latency and small size of the response. This is the default response type for Search API.
     * Fields returned by this response group are as below.
     *
     * @reference https://developer.walmartlabs.com/docs/read/Item_Field_Description
     */
    static class ItemResponse {
        /**
         * A positive integer that uniquely identifies an item
         */
        static final String ITEMID = "itemId";
        /**
         * Standard name of the item
         */
        static final String NAME = "name";
        /**
         * Manufacturer suggested retail price
         */
        static final String MSRP = "msrp";
        /**
         * Selling price for the item in USD
         */
        static final String SALE_PRICE = "salePrice";
        /**
         * Unique Product Code
         */
        static final String UPC = "upc";
        /**
         * Breadcrumb for the item. This string describes the category level hierarchy that the item falls under.
         */
        static final String CATEGORY_PATH = "categoryPath";

        /**
         * Long description for the item. Contains escaped html formatting tags.
         */
        static final String LONG_DESC = "longDescription";
        /**
         * Small size image for the item in jpeg format with dimensions 100 x 100 pixels
         */
        static final String THUMBNAILURL = "thumbnailImage";
    }

    /**
     * Full Response contains a large number of attributes that describe an item. This response group is used
     * for Product Lookup API and Data Feed API. The fields returned are as below.
     *
     * @reference https://developer.walmartlabs.com/docs/read/Item_Field_Description
     */
    static class FullItemResponse extends ItemResponse {
        /**
         * Category id for the category of this item. This value can be passed to APIs to pull this item's category level information.
         */
        static final String CATEGORYID = "categoryNode";
        /**
         * Short description for the item. Contains escaped html formatting tags.
         */
        static final String SHORT_DESC = "shortDescription";
        /**
         * Item's brand
         */
        static final String BRAND_NAME = "brandName";
        /**
         * Large size image for the item in jpeg format with dimensions 500 x 500 pixels
         */
        static final String LARGEIMAGEURL = "largeImage";
        /**
         * Whether the item is a Special Buy item on Walmart.com
         */
        static final String SPECIALBUY = "specialBuy";
    }
}
