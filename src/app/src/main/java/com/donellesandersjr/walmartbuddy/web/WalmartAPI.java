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

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;

import bolts.Task;

public final class WalmartAPI extends WebAPI {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.WalmartAPI";

    private static final String WALMART_APIKEY = "[YOUR KEY GOES HERE]";
    private static final String PRODUCT_SEARCH_QUERY = "http://api.walmartlabs.com/v1/items?apiKey=" + WALMART_APIKEY + "&format=json";


    public static Task<WBList<ProductModel>> fetchProductByUPC (String upc) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(ItemResponse.UPC, upc);

        return Task.callInBackground(new Callable<WBList<ProductModel>>() {
            @Override
            public WBList<ProductModel> call() throws Exception {
                WBList<ProductModel> products = new WBList<>();
                URL url = buildUrl(PRODUCT_SEARCH_QUERY, params);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    JSONObject response = readFrom(connection.getInputStream());
                    if (response.has(Response.ITEMS)) {
                        JSONArray items = response.getJSONArray(Response.ITEMS);
                        int len = items.length();
                        for (int i =0; i < len; i++) {
                            JSONObject item = WBJsonUtils.getObject(items, i, null);
                            if (item != null) products.add(productFrom(item));
                        }
                    }
                } catch (Exception ex) {
                    readErrorsFrom(connection.getErrorStream());
                    throw ex;
                } finally {
                    connection.disconnect();
                }
                return products;
            }
        });
    }

    static ProductModel productFrom (JSONObject jsonObject) {
        WBLogger.Debug(TAG, "JSON - " + jsonObject.toString());
        return new ProductModel()
                .setName(WBJsonUtils.getString(jsonObject, FullItemResponse.NAME, null))
                .setDescription(WBJsonUtils.getString(jsonObject, FullItemResponse.SHORT_DESC, null))
                .setBrand(WBJsonUtils.getString(jsonObject, FullItemResponse.BRAND_NAME, null))
                .setThumbnailUrl(WBJsonUtils.getString(jsonObject, FullItemResponse.THUMBNAILURL, null))
                .setUPC(WBJsonUtils.getString(jsonObject, FullItemResponse.UPC, null))
                .setSalePrice(WBJsonUtils.getDouble(jsonObject, FullItemResponse.SALE_PRICE, 0))
                .setMSRP(WBJsonUtils.getDouble(jsonObject, FullItemResponse.MSRP, 0))
                .setProductId(WBJsonUtils.getLong(jsonObject, FullItemResponse.ITEMID, 0))
                .setProductCategoryId(WBJsonUtils.getString(jsonObject, FullItemResponse.CATEGORYID, null));
    }


    static void readErrorsFrom (InputStream stream) {
        JSONObject response = readFrom(stream);
        if (response.has(Response.ERRORS)) {
            try {
                JSONArray errors = response.getJSONArray(Response.ERRORS);
                for (int i = 0; i < errors.length(); i++) {
                    JSONObject entry = errors.getJSONObject(i);
                    WBLogger.Error(TAG, "Request Error [code] - " + entry.getString("code") + " [message] - " + entry.getString("message"));
                }
            } catch (Exception ex) {
                WBLogger.Error(TAG, ex);
            }
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
