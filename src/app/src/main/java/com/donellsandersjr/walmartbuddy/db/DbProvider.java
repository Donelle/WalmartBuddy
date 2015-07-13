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
import com.donellsandersjr.walmartbuddy.models.CartModel;
import com.donellsandersjr.walmartbuddy.models.CategoryModel;
import com.donellsandersjr.walmartbuddy.models.DataModel;
import com.donellsandersjr.walmartbuddy.models.ProductModel;

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

}