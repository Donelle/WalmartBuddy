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

import com.donellesandersjr.walmartbuddy.api.WBEqualityComparer;
import com.donellesandersjr.walmartbuddy.models.DataModel;
import com.yahoo.squidb.data.TableModel;

public final class DbEqualityComparer implements WBEqualityComparer<DataModel> {

    @Override
    public boolean equals(DataModel x, DataModel y) {
        long id1 = x.getLongValue(TableModel.DEFAULT_ID_COLUMN, 0L);
        long id2 = y.getLongValue(TableModel.DEFAULT_ID_COLUMN, 0L);

        if (id1 == 0 && id2 == 0) {
            // These are new records so lets compare the objects themselves
            return x.hashCode() == y.hashCode();
        }

        return id1 == id2;
    }
}
