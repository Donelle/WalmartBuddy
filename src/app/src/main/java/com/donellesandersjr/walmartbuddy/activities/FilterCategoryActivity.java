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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.models.CategoryModel;
import com.donellesandersjr.walmartbuddy.widgets.CategoryCompletionView;
import com.donellesandersjr.walmartbuddy.widgets.CategoryDataAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.Stack;

public class FilterCategoryActivity extends BaseActivity implements
        TokenCompleteTextView.TokenListener<CategoryModel> {

    private CategoryCompletionView _categoryView;
    private CategoryModel _currentCategory;
    private CategoryDataAdapter _adapter;

    private final String STATE_BREADCRUMBS = "FilterCategoryActivity.STATE_BREADCRUMBS";
    private Stack<CategoryModel> _breadCrumbs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_category);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (savedInstanceState != null) {
            _breadCrumbs = (Stack) savedInstanceState.getSerializable(STATE_BREADCRUMBS);
            _adapter  = new CategoryDataAdapter(this, _breadCrumbs);
        } else {
            _breadCrumbs = new Stack<>();
            _adapter = new CategoryDataAdapter(this, _currentCategory.getSubcategories());
        }

        _currentCategory = getIntent().getParcelableExtra(getString(R.string.bundle_key_category));
        _categoryView = (CategoryCompletionView) findViewById(R.id.filter_category_subcategories);
        _categoryView.setTokenListener(this);
        _categoryView.setAdapter(_adapter);
        _categoryView.setCompletionHint("Example: " + _currentCategory.getSubcategories().get(0).getName());
        _categoryView.setThreshold(1);

        TextView categoryTextView = (TextView) findViewById(R.id.filter_category_text);
        categoryTextView.setText(_currentCategory.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.getMenuInflater().inflate(R.menu.menu_filter_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select_item && _breadCrumbs.size() > 0) {
            Intent data = new Intent();
            data.putExtra(getString(R.string.bundle_key_category), _breadCrumbs.peek());
            setResult(RESULT_OK, data);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override /* TokenCompleteTextView.TokenListener */
    public void onTokenAdded(CategoryModel categoryModel) {
        _breadCrumbs.push(categoryModel);
        if (categoryModel.getSubcategories().size() > 0) {
            _adapter.clear();
            _adapter.addAll(categoryModel.getSubcategories());
        } else {
            _adapter.clear();
        }
    }

    @Override /* TokenCompleteTextView.TokenListener */
    public void onTokenRemoved(CategoryModel categoryModel) {
        _breadCrumbs.pop();
        _adapter.clear();
        _adapter.addAll(_breadCrumbs.peek().getSubcategories());
    }


}
