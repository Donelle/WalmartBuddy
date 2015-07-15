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


import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Criterion;

import java.util.Date;

@TableModelSpec(className = "ProductDb", tableName = "product")
class ProductSpec extends BaseSpec {

    public String name;
    public String description;
    public String brandName;
    public String upc;
    public String thumbnailUrl;
    public String largeImageUrl;
    public double salePrice;
    public double msrp;
    public long itemId;
    public String categoryId;
    public long createDate;


    @ModelMethod
    public static ProductModel getModel (ProductDb productDb) {
        ProductModel model = createModel(ProductModel.class);
        model.setLongValue(TableModel.DEFAULT_ID_COLUMN, productDb.getId());
        model.setName(productDb.getName());
        model.setDescription(productDb.getDescription());
        model.setBrand(productDb.getBrandName());
        model.setUPC(productDb.getUpc());
        model.setThumbnailUrl(productDb.getThumbnailUrl());
        model.setSalePrice(productDb.getSalePrice());
        model.setMSRP(productDb.getMsrp());
        model.setProductId(productDb.getItemId());
        model.setProductCategoryId(productDb.getCategoryId());

        return model;
    }

    @ModelMethod
    public static void save (ProductDb productDb, ProductModel model) {
        productDb.setId(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        productDb.setName(model.getName());
        productDb.setDescription(model.getDescription());
        productDb.setBrandName(model.getBrand());
        productDb.setUpc(model.getUPC());
        productDb.setThumbnailUrl(model.getThumbnailUrl());
        productDb.setSalePrice(model.getSalePrice());
        productDb.setMsrp(model.getMSRP());
        productDb.setItemId(model.getProductId());
        productDb.setCategoryId(model.getProductCategoryId());

        if (productDb.getId() == 0)
            productDb.setCreateDate(new Date().getTime());

        if (getDatabase().persist(productDb))
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, productDb.getId());
    }

    @ModelMethod
    public static int delete (ProductDb productDb, ProductModel model) {
        Criterion criterion = productDb.ID.eq(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        int result = getDatabase().deleteWhere(ProductDb.class, criterion);
        if (result > 0)
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, 0L);

        return result;
    }
}
