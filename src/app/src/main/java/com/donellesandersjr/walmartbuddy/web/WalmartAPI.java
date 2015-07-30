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
import com.donellesandersjr.walmartbuddy.models.CategoryModel;
import com.donellesandersjr.walmartbuddy.models.ProductModel;

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
    private static final String SEARCH_QUERY =  "http://api.walmartlabs.com/v1/search?apiKey=" + WALMART_APIKEY + "&format=json";
    private static final String CATEGORIES_QUERY = "http://api.walmartlabs.com/v1/taxonomy?apiKey="+ WALMART_APIKEY + "&format=json";;

    public static Task<WBList<ProductModel>> fetchProductByUPC (String upc) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(QueryParams.UPC, upc);

        return Task.callInBackground(new Callable<WBList<ProductModel>>() {
            @Override
            public WBList<ProductModel> call() throws Exception {
                URL url = buildUrl(PRODUCT_SEARCH_QUERY, params);
                return fetchResults(url, params);
            }
        });
    }

    public static Task<WBList<ProductModel>> search (String query, String categoryId) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(QueryParams.QUERY, query);
        params.put(QueryParams.CATEGORYID, categoryId);
        params.put(QueryParams.NUM_OF_ITEMS, "25");

        return Task.callInBackground(new Callable<WBList<ProductModel>>() {
            @Override
            public WBList<ProductModel> call() throws Exception {
                URL url = buildUrl(SEARCH_QUERY, params);
                return fetchResults(url, params);
            }
        });
    }

    public static Task<WBList<CategoryModel>> fetchCategories () {
        return Task.callInBackground(new Callable<WBList<CategoryModel>>() {
            @Override
            public WBList<CategoryModel> call() throws Exception {
                HttpURLConnection connection = null;
                WBList<CategoryModel> categories = new WBList<>();
                try {
                    URL url = new URL(CATEGORIES_QUERY);
                    connection = (HttpURLConnection) url.openConnection();
                    JSONObject response = readFrom(connection.getInputStream());
                    if (response.has(Response.CATEGORIES)) {
                        JSONArray items = response.getJSONArray(Response.CATEGORIES);
                        int len = items.length();
                        for (int i =0; i < len; i++) {
                            JSONObject item = WBJsonUtils.getObject(items, i, null);
                            if (item != null) {
                                CategoryModel model = categoryFrom(item);
                                categories.add(model);
                                populateSubcategories(model, item);
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
                return categories;
            }
        });
    }

    static void populateSubcategories (CategoryModel parent, JSONObject element) {
        if (element.has(TaxonomyResponse.CHILDREN)) {
            WBList<CategoryModel> categories = new WBList<>();
            JSONArray items = WBJsonUtils.getArray(element, TaxonomyResponse.CHILDREN);
            int len = items.length();
            for (int i =0; i < len; i++) {
                JSONObject item = WBJsonUtils.getObject(items, i, null);
                if (item != null) {
                    CategoryModel model = categoryFrom(item);
                    categories.add(model);
                    populateSubcategories(model, item);
                }
            }
            parent.setSubcategories(categories);
        }
    }

    static WBList<ProductModel> fetchResults (URL url, HashMap<String, String> params) throws Exception {
        HttpURLConnection connection = null;
        WBList<ProductModel> products = new WBList<>();
        try {
            connection = (HttpURLConnection) url.openConnection();
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

    static ProductModel productFrom (JSONObject jsonObject) {
        //WBLogger.Debug(TAG, "JSON - " + jsonObject.toString());
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

    static CategoryModel categoryFrom (JSONObject jsonObject) {
        return new CategoryModel()
                .setCategoryId(WBJsonUtils.getString(jsonObject, TaxonomyResponse.ID, null))
                .setName(WBJsonUtils.getString(jsonObject, TaxonomyResponse.NAME, null));
    }


    static ArrayList<WalmartAPIException> readErrorsFrom (InputStream stream) {
        ArrayList<WalmartAPIException> exceptions = new ArrayList<>();
        JSONObject response = readFrom(stream);
        if (response.has(Response.ERRORS)) {
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
                default:
                    return "Unexpected error occured";
            }
        }
    }

    /**
     * API Response entries
     */
    static class Response {
        static final String ITEMS = "items";
        static final String ERRORS = "errors";
        static final String CATEGORIES = "categories";

    }

    /**
     * API Response Codes
     */
    static class ErrorCodes {
        static final int UPC_NOTFOUND = 4023;
        static final int INVALID_REQUEST = 4001;
        static final int INVALID_ITEM = 4002;
        static final int INVALID_CATEGORY = 4003;
        static final int INVALID_START_PARAM = 4005;
        static final int INVALID_RESPONSE_FORMAT = 4007;
        static final int MISSING_ITEM_ID = 4008;
        static final int MISSING_SEARCH_QUERY = 4009;
        static final int INVALID_START_PARAM_GREATERTHAN_99 = 4010;
        static final int INTERNAL_SERVER_FAILURE = 5000;
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
         * Search text - whitespace separated sequence of keywords to search for
         */
        static final String QUERY = "query";
        /**
         * Number of matching items to be returned, max value 25. Default is 10.
         */
        static final String NUM_OF_ITEMS = "numItems";
        /**
         * Category id of the category for search within a category. This should
         * match the id field from Taxonomy API
         */
        static final String CATEGORYID = "categoryId";
        /**
         *
         */
        static final String TOTALRESULTS = "totalResults";
        /**
         * Starting point of the results within the matching set of items -
         * upto 10 items will be returned starting from this item
         */
        static final String START = "start";
    }

    /**
     * Taxonomy API exposes the category taxonomy used by walmart.com to categorize items.
     * It describes three levels - Departments, Categories and Sub-categories as available on Walmart.com.
     *
     * @reference https://developer.walmartlabs.com/docs/read/Taxonomy_API
     */
    static class TaxonomyResponse {
        /**
         * Category id for this category. These values are used as an input parameter to other APIs.
         */
        static final String ID = "id";
        /**
         * Name for this category as specified on Walmart.com
         */
        static final String NAME = "name";
        /**
         * List of categories that have the current category as a parent in the taxonomy.
         */
        static final String CHILDREN = "children";
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
