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

package com.donellesandersjr.walmartbuddy.api;

import android.os.Bundle;
import android.os.Parcel;

import com.donellesandersjr.walmartbuddy.App;

public abstract class WBParcelable implements android.os.Parcelable {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.api.Parcelable";
    private static final String CLASSNAME = "className";

    protected void onRestoreState(Bundle state) { /*NO OP*/ }
    protected void onSaveState(Bundle state) { /*NO OP*/ }

    public static final Creator<WBParcelable> CREATOR = new Creator<WBParcelable>() {
        @Override
        public WBParcelable createFromParcel(Parcel parcel) {
            try {
                Bundle state = parcel.readBundle();
                state.setClassLoader(App.getInstance().getClassLoader());
                Class classType = Class.forName(state.getString (CLASSNAME));
                WBParcelable parcelable = (WBParcelable) classType.newInstance();
                parcelable.onRestoreState(state);
                return parcelable;
            } catch (Exception ex) {
                WBLogger.Error(TAG, ex);
            }

            return null;
        }

        @Override
        public WBParcelable[] newArray(int i) {
            return new WBParcelable[i];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        Bundle state = new Bundle();
        state.putString (CLASSNAME, this.getClass().getName());
        onSaveState(state);
        parcel.writeBundle(state);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static  <T extends WBParcelable> T clone (T clonable) {
        try {
            Bundle state = new Bundle();
            clonable.onSaveState(state);
            T instance =(T) clonable.getClass().newInstance();
            instance.onRestoreState(state);
            return instance;
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        }
        return null;
    }
}
