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

package com.donellesandersjr.walmartbuddy.models;

import android.content.ContentValues;
import android.os.Bundle;

import com.donellesandersjr.walmartbuddy.api.WBList;

import java.util.Arrays;
import java.util.List;

public class CartModel extends DataModel {

    private static final String KEY_NAME = "name";
    private static final String KEY_ZIPCODE = "zipcode";
    private static final String KEY_TAXRATE = "taxrate";
    private static final String KEY_ITEMS = "items";

    private WBList<CartItemModel> _items = new WBList<>();

    public CartModel () {
        super();
    }

    CartModel (ContentValues values) {
        super(values);
    }

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelable(KEY_ITEMS, _items);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        _items = state.getParcelable(KEY_ITEMS);
    }

    @Override
    protected void onMergeChanges(DataModel model) {
        CartModel cartModel = (CartModel) model;
        if (cartModel != null) {
            _items.addAll(cartModel._items);
            _items.removeAll(cartModel._items.getDeletedList());
        }
    }

    @Override
    protected void onSaveChanges() {
        List items = Arrays.asList(_items.toArray());
        _items = new WBList<>(items);
    }

    public String getName () {
        return super.getStringValue(KEY_NAME);
    }

    public CartModel setName (String name) {
        super.setStringValue(KEY_NAME, name);
        return this;
    }

    public double getTaxRate () {
        return super.getDoubleValue(KEY_TAXRATE);
    }

    public CartModel setTaxRate (double taxRate) {
        super.setDoubleValue(KEY_TAXRATE, taxRate);
        return this;
    }

    public String getZipCode () {
        return super.getStringValue(KEY_ZIPCODE);
    }

    public CartModel setZipCode (String zipCode) {
        super.setStringValue(KEY_ZIPCODE, zipCode);
        return this;
    }

    public WBList<CartItemModel> getCartItems () {
        return _items;
    }

    public CartModel setCartItems (List<CartItemModel> cartItems) {
        boolean areEqual = cartItems == null && _items == null;
        if (!areEqual) {
            _items = cartItems != null ?
                    new WBList<>((List) cartItems) :
                    new WBList<CartItemModel>();
        }
        return this;
    }

    public boolean hasCartItemsChanged () {
        return _items.hasChanged();
    }
}
