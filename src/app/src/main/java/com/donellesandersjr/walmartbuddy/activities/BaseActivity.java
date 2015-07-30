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

package com.donellesandersjr.walmartbuddy.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.domain.DomainObject;

public class BaseActivity<T extends DomainObject> extends AppCompatActivity {

    private final String STATE_OBJECT = "BaseActivity.STATE_OBJECT";

    private T _object;
    protected void setDomainObject (T object) {
        _object = object;
    }

    public T getDomainObject ()  {
        return _object;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            _object = savedInstanceState.getParcelable(STATE_OBJECT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_OBJECT, _object);
    }

    protected float getDPUnits (int unit) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            unit,
            getResources().getDisplayMetrics());
    }

    protected int getPixels (float dpUnits) {
        return Float.valueOf(dpUnits * (getResources().getDisplayMetrics().densityDpi / 160f))
                    .intValue();
    }

    /**
     * Displays a message using the Snackbar pending that the view contains a coordinator layout
     * @param message
     */
    protected void showMessage (String message){
        Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Should be pretty obvious here :-)
     */
    protected void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
