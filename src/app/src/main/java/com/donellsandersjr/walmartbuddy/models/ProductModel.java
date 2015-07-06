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

    public static String KEY_UPC = "upc";
    public static String KEY_PRICE = "salePrice";
    public static String KEY_NAME = "name";
    public static String KEY_BRAND = "brandName";
    public static String KEY_DESC = "shortDescription";
    public static String KEY_MSRP = "msrp";
    public static String KEY_THUMBNAIL = "thumbnailImage";
    public static String KEY_ITEM_TAXONONY_ID = "itemId";
    public static String KEY_CATEGORY_TAXONONY_ID = "categoryNode";
    public static String KEY_AVAILABLEONLINE= "availableOnline";

    public ProductModel () {
        super();
    }
    ProductModel (ContentValues values) {
        super(values);
    }
}
