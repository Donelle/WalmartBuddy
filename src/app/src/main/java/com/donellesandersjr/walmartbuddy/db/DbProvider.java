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

import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;
import com.donellesandersjr.walmartbuddy.models.CartModel;
import com.donellesandersjr.walmartbuddy.models.CategoryModel;
import com.donellesandersjr.walmartbuddy.models.DataModel;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Query;

public final class DbProvider {

    public static void save (DataModel dataModel) throws NullPointerException {
        if (dataModel == null)
            throw new NullPointerException("model can not be null");

        if (dataModel instanceof ProductModel)
            new ProductDb().save((ProductModel)dataModel);
        else if (dataModel instanceof CategoryModel)
            new CategoryDb().save((CategoryModel) dataModel);
        else if (dataModel instanceof CartItemModel)
            new CartItemDb().save((CartItemModel) dataModel);
        else if (dataModel instanceof CartModel)
            new CartDb().save ((CartModel)dataModel);
    }

    public static int delete (DataModel dataModel) throws NullPointerException {
        if (dataModel == null)
            throw new NullPointerException("model can not be null");

        int result = -1;
        if (dataModel instanceof ProductModel)
            result = new ProductDb().delete((ProductModel) dataModel);
        else if (dataModel instanceof CategoryModel)
            result = new CategoryDb().delete((CategoryModel) dataModel);
        else if (dataModel instanceof CartItemModel)
            result = new CartItemDb().delete((CartItemModel) dataModel);
        else if (dataModel instanceof CartModel)
            result = new CartDb().delete((CartModel) dataModel);

        return result;
    }

    public static CartModel fetchCart () {
        Query whereQuery = Query.select().from(CartDb.TABLE);
        CartDb cartDb = BaseSpec.getDatabase().fetchByQuery(CartDb.class, whereQuery);
        if (cartDb == null) {
            // This means its the first time the app been run so lets create the default
            // shopping list and save it to the db.
            cartDb = new CartDb()
                    .setName("Default")
                    .setZipCode("")
                    .setTaxRate(0d);
            save(cartDb.getModel());
        }
        return cartDb.getModel();
    }

    public static WBList<CategoryModel> fetchCategories () {
        WBList<CategoryModel> categories = new WBList<>();
        SquidCursor<CategoryDb> cursor =
                BaseSpec.getDatabase().query(CategoryDb.class, Query.select().from(CategoryDb.TABLE));
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    CategoryDb db = new CategoryDb(cursor);
                    categories.add(db.getModel());
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return categories;
    }
}
