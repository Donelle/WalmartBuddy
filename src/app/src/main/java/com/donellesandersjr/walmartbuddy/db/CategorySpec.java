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

package com.donellesandersjr.walmartbuddy.db;

import com.donellesandersjr.walmartbuddy.api.WBJsonUtils;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.models.CategoryModel;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Criterion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

@TableModelSpec(className = "CategoryDb", tableName = "category")
class CategorySpec extends BaseSpec {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.db.CategorySpec";
    private static final String CHILDREN = "children";

    public String name;
    public String categoryId;
    public String subcategories;
    public long createDate;

    @ModelMethod
    public static CategoryModel getModel (CategoryDb categoryDb) {
        CategoryModel model = createModel(CategoryModel.class);
        model.setLongValue(TableModel.DEFAULT_ID_COLUMN, categoryDb.getId());
        model.setName(categoryDb.getName());
        model.setCategoryId(categoryDb.getCategoryId());

        String json = categoryDb.getSubcategories ();
        if (!WBStringUtils.isNullOrEmpty(json)) {
            //
            // Now lets inflate the categories into model objects
            //
            WBList<CategoryModel> subcategories = new WBList<>();
            inflate(subcategories, WBJsonUtils.getArray(json));

            model.setSubcategories(subcategories);
        }
        return model;
    }

    @ModelMethod
    public static void save (CategoryDb categoryDb, CategoryModel model) {
        categoryDb.setId(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        categoryDb.setName(model.getName());
        categoryDb.setCategoryId(model.getCategoryId());

        List<CategoryModel> subcategories = model.getSubcategories();
        if (subcategories.size() > 0) {
            //
            // Now lets deflate the categories into json
            //
            JSONArray element = new JSONArray();
            deflate(element, subcategories);

            categoryDb.setSubcategories(element.toString());
        }

        if (categoryDb.getId() == 0)
            categoryDb.setCreateDate(new Date().getTime());

        if (getDatabase().persist(categoryDb))
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, categoryDb.getId());
    }

    @ModelMethod
    public static int delete (CategoryDb categoryDb, CategoryModel model) {
        Criterion criterion = categoryDb.ID.eq(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        int result = getDatabase().deleteWhere(CategoryDb.class, criterion);
        if (result > 0)
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, 0L);

        return result;
    }

    private static void inflate (List<CategoryModel> parent, JSONArray subcategories) {
        int len = subcategories.length();
        for (int i =0; i < len; i++) {
            JSONObject element = WBJsonUtils.getObject(subcategories, i, null);
            CategoryModel model = toModel(element);
            parent.add(model);
            if (element.has(CHILDREN)) {
                WBList<CategoryModel> children = new WBList<>();
                inflate(children, WBJsonUtils.getArray(element, CHILDREN));
                model.setSubcategories(children);
            }
        }
    }

    private static void deflate (JSONArray parent, List<CategoryModel> subcategories) {
        for (CategoryModel subcategory : subcategories) {
            JSONObject jscat = toJSON(subcategory);
            parent.put(jscat);
            if (subcategory.getSubcategories().size() > 0) {
                try {
                    JSONArray subparent = new JSONArray();
                    jscat.put(CHILDREN, subparent);
                    deflate(subparent, subcategory.getSubcategories());
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                }
            }
        }
    }

    private static JSONObject toJSON (CategoryModel model) {
        try {
            return new JSONObject()
                    .put(CategoryDb.NAME.getName(), model.getName())
                    .put(CategoryDb.CATEGORY_ID.getName(), model.getCategoryId());
        }catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        }
        return null;
    }

    static CategoryModel toModel (JSONObject jsonObject) {
        return new CategoryModel()
                .setCategoryId(WBJsonUtils.getString(jsonObject, CategoryDb.CATEGORY_ID.getName(), null))
                .setName(WBJsonUtils.getString(jsonObject, CategoryDb.NAME.getName(), null));
    }
}
