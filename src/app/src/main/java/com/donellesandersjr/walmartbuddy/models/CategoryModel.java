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

public final class CategoryModel extends DataModel {

    private static String KEY_NAME = "name";
    private static String KEY_CATEGORY_ID = "categoryId";

    public CategoryModel () {
        super();
    }

    CategoryModel (ContentValues values) {
        super(values);
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
}
