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


import com.donellsandersjr.walmartbuddy.models.ProductModel;
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
    public double salesPrice;
    public double msrp;
    public long taxonomyId;
    public long categoryTaxonomyId;
    public long createDate;


    @ModelMethod
    public static ProductModel getModel (ProductDb productDb) {
        ProductModel model = createModel(ProductModel.class);
        model.setLongValue(TableModel.DEFAULT_ID_COLUMN, productDb.getId());
        model.setStringValue(ProductModel.KEY_NAME, productDb.getName());
        model.setStringValue(ProductModel.KEY_DESC, productDb.getDescription());
        model.setStringValue(ProductModel.KEY_BRAND, productDb.getBrandName());
        model.setStringValue(ProductModel.KEY_UPC, productDb.getUpc());
        model.setStringValue(ProductModel.KEY_THUMBNAIL, productDb.getThumbnailUrl());
        model.setDoubleValue(ProductModel.KEY_PRICE, productDb.getSalesPrice());
        model.setDoubleValue(ProductModel.KEY_MSRP, productDb.getMsrp());
        model.setLongValue(ProductModel.KEY_ITEM_TAXONONY_ID, productDb.getTaxonomyId());
        model.setLongValue(ProductModel.KEY_CATEGORY_TAXONONY_ID, productDb.getCategoryTaxonomyId());

        return model;
    }

    @ModelMethod
    public static void save (ProductDb productDb, ProductModel model) {
        productDb.setId(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        productDb.setName(model.getStringValue(ProductModel.KEY_NAME));
        productDb.setDescription(model.getStringValue(ProductModel.KEY_DESC));
        productDb.setBrandName(model.getStringValue(ProductModel.KEY_BRAND));
        productDb.setUpc(model.getStringValue(ProductModel.KEY_UPC));
        productDb.setThumbnailUrl(model.getStringValue(ProductModel.KEY_THUMBNAIL));
        productDb.setSalesPrice(model.getDoubleValue(ProductModel.KEY_PRICE));
        productDb.setMsrp(model.getDoubleValue(ProductModel.KEY_MSRP));
        productDb.setTaxonomyId(model.getLongValue(ProductModel.KEY_ITEM_TAXONONY_ID));
        productDb.setCategoryTaxonomyId(model.getLongValue(ProductModel.KEY_CATEGORY_TAXONONY_ID));

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
