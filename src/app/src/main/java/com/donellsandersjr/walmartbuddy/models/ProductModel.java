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

package com.donellsandersjr.walmartbuddy.models;

import android.content.ContentValues;

public final class ProductModel extends DataModel {

    private static String KEY_UPC = "upc";
    private static String KEY_PRICE = "salePrice";
    private static String KEY_NAME = "name";
    private static String KEY_BRAND = "brandName";
    private static String KEY_DESC = "shortDescription";
    private static String KEY_MSRP = "msrp";
    private static String KEY_THUMBNAIL = "thumbnailImage";
    private static String KEY_ITEM_ID = "itemId";
    private static String KEY_CATEGORY_ID = "categoryNode";

    public ProductModel () {
        super();
    }

    ProductModel (ContentValues values) {
        super(values);
    }

    public String getUPC () {
        return super.getStringValue(KEY_UPC, "");
    }

    public ProductModel setUPC (String upc) {
        super.setStringValue(KEY_UPC, upc);
        return this;
    }

    public double getSalePrice () {
        return super.getDoubleValue(KEY_PRICE, 0);
    }

    public ProductModel setSalePrice (double salesPrice) {
        super.setDoubleValue(KEY_PRICE, salesPrice);
        return this;
    }

    public String getName () {
        return super.getStringValue(KEY_NAME, "");
    }

    public ProductModel setName (String name) {
        super.setStringValue(KEY_NAME, name);
        return this;
    }

    public String getBrand () {
        return super.getStringValue(KEY_BRAND, "");
    }

    public ProductModel setBrand (String brandname) {
        super.setStringValue(KEY_BRAND, brandname);
        return this;
    }

    public String getDescription () {
        return super.getStringValue(KEY_DESC,  "");
    }

    public ProductModel setDescription (String description) {
        super.setStringValue(KEY_DESC, description);
        return this;
    }

    public double getMSRP () {
        return super.getDoubleValue(KEY_MSRP, 0);
    }

    public ProductModel setMSRP (double msrp) {
        super.setDoubleValue(KEY_MSRP, msrp);
        return this;
    }

    public String getThumbnailUrl () {
        return super.getStringValue(KEY_THUMBNAIL, "");
    }

    public ProductModel setThumbnailUrl (String thumbnailUrl) {
        super.setStringValue(KEY_THUMBNAIL, thumbnailUrl);
        return this;
    }

    public long getProductId () {
        return super.getLongValue(KEY_ITEM_ID, 0);
    }

    public ProductModel setProductId (long productId) {
        super.setLongValue(KEY_ITEM_ID, productId);
        return this;
    }

    public String getProductCategoryId () {
        return super.getStringValue(KEY_CATEGORY_ID, "");
    }

    public ProductModel setProductCategoryId (String categoryId) {
        super.getStringValue(KEY_CATEGORY_ID, categoryId);
        return this;
    }
}
