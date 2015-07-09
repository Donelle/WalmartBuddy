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

import com.donellsandersjr.walmartbuddy.models.CartItemModel;
import com.donellsandersjr.walmartbuddy.models.CategoryModel;
import com.donellsandersjr.walmartbuddy.models.ProductModel;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Criterion;

import java.util.Date;

@TableModelSpec(className = "CartItemDb", tableName = "cartitem")
class CartItemSpec extends BaseSpec {

    public String name;
    public double price;
    public String thumbnailUrl;
    public int quantity;
    public int productId;
    public int categoryId;
    public long createDate;

    @ModelMethod
    public static CartItemModel getModel (CartItemDb cartItemDb) {
        CartItemModel model = createModel(CartItemModel.class);
        model.setLongValue(TableModel.DEFAULT_ID_COLUMN, cartItemDb.getId());
        model.setName(cartItemDb.getName());
        model.setPrice(cartItemDb.getPrice());
        model.setThumbnailUrl(cartItemDb.getThumbnailUrl());
        model.setQuantity(cartItemDb.getQuantity());

        int id = cartItemDb.getProductId();
        if (id > 0) {
            ProductDb productDb = getDatabase().fetch(ProductDb.class, id);
            model.setProduct(productDb.getModel());
        }

        id = cartItemDb.getCategoryId();
        if (id > 0) {
            CategoryDb categoryDb = getDatabase().fetch(CategoryDb.class, id);
            model.setCategory(categoryDb.getModel());
        }

        return model;
    }

    @ModelMethod
    public static void save (CartItemDb cartItemDb, CartItemModel model) {
        cartItemDb.setId(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        cartItemDb.setName(model.getName());
        cartItemDb.setPrice(model.getPrice());
        cartItemDb.setThumbnailUrl(model.getThumbnailUrl());
        cartItemDb.setQuantity (model.getQuantity());

        ProductModel productModel = model.getProduct();
        if (productModel != null)
            cartItemDb.setProductId(productModel.getIntValue(TableModel.DEFAULT_ID_COLUMN));

        CategoryModel categoryModel = model.getCategory();
        if (categoryModel != null)
            cartItemDb.setCategoryId(categoryModel.getIntValue(TableModel.DEFAULT_ID_COLUMN));

        if (cartItemDb.getId() == 0)
            cartItemDb.setCreateDate(new Date().getTime());

        if (getDatabase().persist(cartItemDb))
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, cartItemDb.getId());
    }

    @ModelMethod
    public static int delete (CartItemDb cartItemDb, CartItemModel model) {
        Criterion criterion = CartItemDb.ID.eq(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        int result = getDatabase().deleteWhere(CartItemDb.class, criterion);
        if (result > 0)
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, 0L);

        return result;
    }
}
