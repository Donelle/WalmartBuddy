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
package com.donellesandersjr.walmartbuddy.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.db.CategoryDb;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.models.CategoryModel;
import com.joanzapata.android.iconify.Iconify;
import com.tokenautocomplete.TokenCompleteTextView;

import java.io.Serializable;
import java.util.ArrayList;

public final class CategoryCompletionView extends TokenCompleteTextView<CategoryModel> {

    public CategoryCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setTokenClickStyle(TokenClickStyle.Delete);
        super.setSplitChar(new char[]{',', ';', ' '});
    }

    @Override
    protected View getViewForObject(CategoryModel categoryModel) {
        TextView textView = (TextView)
            LayoutInflater.from(getContext()).inflate(
                    R.layout.category_filter_item, (ViewGroup)getParent(), false);
        Iconify.setIcon(textView, Iconify.IconValue.fa_remove);
        return textView;
    }

    @Override
    protected CategoryModel defaultObject(String s) {
        return null;
    }

    @Override
    protected ArrayList<CategoryModel> convertSerializableArrayToObjectArray(ArrayList<Serializable> s) {
        ArrayList<CategoryModel> models = new ArrayList<>();
        for (Serializable item : s) {
            String categoryId = (String)item;
            models.add(DbProvider.fetchCategories(CategoryDb.CATEGORY_ID.eq(categoryId)).first());
        }
        return models;
    }

    @Override
    protected ArrayList<Serializable> getSerializableObjects() {
        ArrayList<Serializable> items = new ArrayList<>();
        for (CategoryModel model : getObjects())
            items.add(model.getCategoryId());

        return items;
    }
}
