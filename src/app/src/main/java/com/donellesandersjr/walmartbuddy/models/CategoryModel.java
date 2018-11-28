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

import java.util.Collections;
import java.util.List;

public final class CategoryModel extends DataModel {

    private static String KEY_NAME = "name";
    private static String KEY_CATEGORY_ID = "categoryId";
    private static String KEY_SUBCATEGORIES = "subcategories";

    private WBList<CategoryModel> _subcategories = new WBList<>();

    public CategoryModel () {
        super();
    }

    CategoryModel (ContentValues values) {
        super(values);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        _subcategories = state.getParcelable(KEY_SUBCATEGORIES);
    }

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelable(KEY_SUBCATEGORIES, _subcategories);
    }

    public String getName () {
        return super.getStringValue(KEY_NAME, "");
    }

    public CategoryModel setName (String name) {
        super.setStringValue(KEY_NAME, name);
        return this;
    }

    public String getCategoryId () {
        return super.getStringValue(KEY_CATEGORY_ID, "");
    }

    public CategoryModel setCategoryId (String categoryId) {
        super.getStringValue(KEY_CATEGORY_ID, categoryId);
        return this;
    }

    public List<CategoryModel> getSubcategories () {
        return Collections.unmodifiableList(_subcategories);
    }

    public CategoryModel setSubcategories (List<CategoryModel> subcategories) {
        if (!(subcategories == null && _subcategories == null)) {
            _subcategories = subcategories != null ?
                    new WBList<>((List) subcategories) :
                    new WBList<>();
        }
        return this;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
