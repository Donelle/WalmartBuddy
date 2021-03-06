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
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Criterion;
import com.yahoo.squidb.sql.Query;

import java.util.Date;

@TableModelSpec(className = "CartDb", tableName = "cart")
class CartSpec extends BaseSpec {

    public String name;
    public String zipCode;
    public double taxRate;
    public long createDate;

    @ModelMethod
    public static CartModel getModel (CartDb cartDb) {

        WBList<CartItemModel> cartItems = new WBList<>();
        Query query = Query.select().from(CartItemMapDb.TABLE).where(CartItemMapDb.CART_ID.eq(cartDb.getId()));
        SquidCursor<CartItemMapDb> cursor = getDatabase().query(CartItemMapDb.class, query);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                CartItemMapDb entry = new CartItemMapDb(cursor);
                CartItemModel cartItem =
                        getDatabase()
                                .fetch(CartItemDb.class, entry.getCartItemId())
                                .getModel();
                cartItems.add(cartItem);
            }while (cursor.moveToNext());
        }
        cursor.close();

        CartModel model = createModel(CartModel.class);
        model.setLongValue(TableModel.DEFAULT_ID_COLUMN, cartDb.getId());
        model.setName(cartDb.getName());
        model.setZipCode(cartDb.getZipCode());
        model.setTaxRate(cartDb.getTaxRate());
        model.setCartItems(cartItems);

        return model;
    }

    @ModelMethod
    public static void save (CartDb cartDb, CartModel model) {
        WalmartBuddyDatabase db =  getDatabase();

        cartDb.setId(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        cartDb.setName(model.getName());
        cartDb.setZipCode(model.getZipCode());
        cartDb.setTaxRate (model.getTaxRate());

        if (cartDb.getId() == 0)
            cartDb.setCreateDate(new Date().getTime());

        if (db.persist(cartDb))
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, cartDb.getId());
        //
        // Blow away the existing map items first before we insert our new collection
        //
        db.deleteWhere(CartItemMapDb.class, CartItemMapDb.CART_ID.eq(cartDb.getId()));
        for (CartItemModel cartItemModel : model.getCartItems()) {
            CartItemMapDb mapDb = new CartItemMapDb();
            mapDb.setCartId(cartDb.getId());
            mapDb.setCartItemId(cartItemModel.getLongValue(TableModel.DEFAULT_ID_COLUMN));
            db.persist(mapDb);
        }
    }

    @ModelMethod
    public static int delete (CartDb cartDb, CartModel model) {

        Criterion criterion = CartDb.ID.eq(model.getLongValue(TableModel.DEFAULT_ID_COLUMN));
        int result = getDatabase().deleteWhere(CartDb.class, criterion);
        if (result > 0)
            model.setLongValue(TableModel.DEFAULT_ID_COLUMN, 0L);

        return result;
    }
}
