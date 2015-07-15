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


import android.database.sqlite.SQLiteDatabase;

import com.donellesandersjr.walmartbuddy.App;
import com.yahoo.squidb.data.AbstractDatabase;
import com.yahoo.squidb.sql.Table;

class WalmartBuddyDatabase extends AbstractDatabase {
    private static final int VERSION = 1;

    public WalmartBuddyDatabase() {
        super(App.getInstance());
    }

    @Override
    protected String getName() {
        return "walmartbuddy.db";
    }

    @Override
    protected Table[] getTables() {
        return new Table[]{
            CartDb.TABLE,
            CartItemDb.TABLE,
            ProductDb.TABLE,
            CategoryDb.TABLE,
            CartItemMapDb.TABLE
        };
    }

    @Override
    protected int getVersion() {
        return VERSION;
    }

    @Override
    protected boolean onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        return false;
    }

}
