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
package com.donellesandersjr.walmartbuddy.models;


import android.content.ContentValues;

public final class StoreModel extends DataModel {

    private static String ADDRESS = "address";
    private static String CITY = "city";
    private static String STATE = "state";
    private static String ZIPCODE = "zip";

    public StoreModel () {
        super();
    }

    StoreModel (ContentValues values) {
        super(values);
    }

    public String getAddress () {
        return super.getStringValue(ADDRESS);
    }

    public StoreModel setAddress (String address) {
        super.setStringValue(ADDRESS, address);
        return this;
    }

    public String getCity () {
        return super.getStringValue(CITY);
    }

    public StoreModel setCity (String city) {
        super.setStringValue(CITY, city);
        return this;
    }

    public String getState () {
        return super.getStringValue(STATE);
    }

    public StoreModel setState (String state) {
        super.setStringValue(STATE, state);
        return this;
    }

    public String getZipCode () {
        return super.getStringValue(ZIPCODE);
    }

    public StoreModel setZipCode (String zipCode) {
        super.setStringValue(ZIPCODE, zipCode);
        return this;
    }
}
