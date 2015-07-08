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
import android.os.Bundle;

public final class CartItemModel extends DataModel {

    public static String KEY_NAME = "name";
    public static String KEY_PRICE = "price";
    public static String KEY_IMAGE_URL = "imageUrl";
    public static String KEY_QUANTITY = "quantity";
    private static String KEY_PRODUCT = "product";
    private static String KEY_CATEGORY = "category";

    private ProductModel _product = new ProductModel();
    private CategoryModel _category = new CategoryModel();

    public CartItemModel () {
        super();
    }

    CartItemModel (ContentValues values) {
        super(values);
    }

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelable(KEY_PRODUCT, _product);
        state.putParcelable(KEY_CATEGORY, _category);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        this.setProduct((ProductModel) state.getParcelable(KEY_PRODUCT));
        this.setCategory((CategoryModel) state.getParcelable(KEY_CATEGORY));
    }

    public ProductModel getProduct () {
        return _product;
    }

    public CartItemModel setProduct (ProductModel product) {
        boolean areEqual = product == null && _product == null;
        if (!areEqual) {
            _product = product;
            super.setStringValue(KEY_PRODUCT, _product.getUniversalId());
        }
        return this;
    }

    public boolean hasProductChanged () {
        return super.hasChanged(KEY_PRODUCT);
    }

    public CategoryModel getCategory () {
        return _category;
    }

    public CartItemModel setCategory (CategoryModel category) {
        boolean areEqual = category == null && _category == null;
        if (!areEqual) {
            _category = category;
            super.setStringValue(KEY_CATEGORY, _category.getUniversalId());
        }
        return this;
    }

    public boolean hasCategoryChanged () {
        return super.hasChanged(KEY_CATEGORY);
    }
}
