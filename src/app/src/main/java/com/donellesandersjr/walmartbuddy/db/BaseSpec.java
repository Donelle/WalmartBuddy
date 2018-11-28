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

import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.models.DataModel;


abstract class BaseSpec {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.db.BaseSpec";

    private static WalmartBuddyDatabase _dao;
    static WalmartBuddyDatabase getDatabase () {
        if (_dao == null)
            _dao = new WalmartBuddyDatabase();
        return _dao;
    }

    public static <T extends DataModel> T createModel (Class<T> classType) {
        try {
            final T instance = classType.newInstance();
            instance.setEqualityComparer(new DbEqualityComparer())
                    .setSortComparer(new DbSortComparer());
            return instance;
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        }
        return null;
    }
}
