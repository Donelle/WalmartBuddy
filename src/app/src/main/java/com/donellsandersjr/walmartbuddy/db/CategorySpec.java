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

package com.donellsandersjr.walmartbuddy.db;

import com.donellsandersjr.walmartbuddy.models.CategoryModel;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Criterion;

import java.util.Date;

@TableModelSpec(className = "CategoryDb", tableName = "category")
class CategorySpec extends BaseSpec {

    public String name;
    public long taxonomyId;
    public long createDate;

    @ModelMethod
    public static CategoryModel getModel (CategoryDb categoryDb) {
        CategoryModel model = createModel(CategoryModel.class);
        model.setLongValue(TableModel.DEFAULT_ID_COLUMN, categoryDb.getId());
        model.setStringValue(CategoryModel.KEY_NAME, categoryDb.getName());
        model.setLongValue(CategoryModel.KEY_TAXONONY_ID, categoryDb.getTaxonomyId());
        return model;
    }

    @ModelMethod
    public static void save (CategoryDb categoryDb, CategoryModel model) {
        categoryDb.setId(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        categoryDb.setName(model.getStringValue(CategoryModel.KEY_NAME));
        categoryDb.setTaxonomyId(model.getLongValue(CategoryModel.KEY_TAXONONY_ID));

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
}
