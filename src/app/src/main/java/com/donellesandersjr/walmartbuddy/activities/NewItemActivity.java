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

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.IconTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.fragments.NewItemFragment;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;
import com.donellesandersjr.walmartbuddy.models.CategoryModel;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.donellesandersjr.walmartbuddy.web.WalmartAPI;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.yahoo.squidb.data.TableModel;


import bolts.Continuation;
import bolts.Task;

public class NewItemActivity extends BaseActivity implements
        View.OnClickListener, ViewPager.OnPageChangeListener, View.OnTouchListener {

    private final String TAG = "com.donellesandersjr.walmart.activities.NewItemActivity";

    View _contentContainer,  _resultsContainer;
    EditText _itemNameEditText;
    TextView _resultsCounterTextView;
    ViewPager _resultsPager;
    Button _searchButton;
    ProgressBar _progressbar;
    CategoryDataAdapter _categoryAdapter;
    Spinner _categoriesSpinner;

    boolean _bIsShowingResults, _bIsSearching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        _contentContainer = findViewById(R.id.new_item_content_container);
        _resultsContainer = findViewById(R.id.new_item_results_container);
        _itemNameEditText = (EditText) findViewById(R.id.new_item_name);
        _resultsCounterTextView = (TextView) findViewById(R.id.new_item_results_counter);
        _progressbar = (ProgressBar) findViewById(R.id.progress_horizontal);
        _searchButton = (Button) findViewById(R.id.new_item_search);
        _searchButton.setOnClickListener(this);

        _resultsPager = (ViewPager) findViewById(R.id.new_item_results_pager);
        _resultsPager.setPageTransformer(true, new DepthPageTransformer());
        _resultsPager.setAdapter(new SearchResultPagerAdapter(new WBList<ProductModel>(), getSupportFragmentManager()));
        _resultsPager.addOnPageChangeListener(this);

        _categoriesSpinner = (Spinner) findViewById(R.id.new_item_categories);
        _categoriesSpinner.setAdapter(_categoryAdapter = new CategoryDataAdapter(DbProvider.fetchCategories()));
        _categoriesSpinner.setOnTouchListener(this);
        if (_categoryAdapter.getCount() > 0) {
            int pos = _categoryAdapter.getFoodCategoryPos();
            if (pos != -1) _categoriesSpinner.setSelection(pos);
        }

        Button button = (Button) findViewById(R.id.new_item_add_to_cart);
        button.setOnClickListener(this);

        button = (Button) findViewById(R.id.new_item_results_hide);
        button.setOnClickListener(this);

        ImageView imageView = (ImageButton)findViewById(R.id.new_item_action_close);
        imageView.setOnClickListener(this);

        imageView = (ImageView)findViewById(R.id.new_item_photo);
        imageView.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_item, menu);
        IconDrawable iconDrawable =
                new IconDrawable(this, Iconify.IconValue.fa_barcode)
                        .color(Color.WHITE)
                        .actionBarSize();
        menu.findItem(R.id.action_scan_item).setIcon(iconDrawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan_item) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.new_item_search) {
            //
            // Check if we categories loaded and if not fetch'em first
            //
            if (_categoryAdapter.getCount() == 0) {
                _downloadCategories().onSuccess(new Continuation<Boolean, Object>() {
                    @Override
                    public Object then(Task<Boolean> task) throws Exception {
                        if (task.getResult()) {
                            //
                            // Now set the default search to Food category
                            //
                            int pos = _categoryAdapter.getFoodCategoryPos();
                            _categoriesSpinner.setSelection(pos != -1 ? pos : 0);
                            //
                            // Now process the request
                            //
                            _processSearchRequest();
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            } else {
                _processSearchRequest();
            }
        } else if (id == R.id.new_item_action_close) {
            finish();
        } else if (id == R.id.new_item_results_hide) {
            _toggleSearchResults();
        }
    }

    @Override /* View.OnTouchListener */
    public boolean onTouch(View v, MotionEvent event) {
        //
        // We check the view id for code readability purposes
        //
        if (v.getId() == R.id.new_item_categories && event.getAction() == MotionEvent.ACTION_UP) {
            if (_categoryAdapter.getCount() == 0) {
                _downloadCategories().onSuccess(new Continuation<Boolean, Object>() {
                    @Override
                    public Object then(Task<Boolean> task) throws Exception {
                        if (task.getResult())
                            _categoriesSpinner.performClick();
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
                return true;
            }
        }
        return false;
    }

    @Override /*  ViewPager.OnPageChangeListener  */
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        String text = String.format("%1$d of %2$d", ++position, _resultsPager.getAdapter().getCount());
        _resultsCounterTextView.setText(text);
    }

    @Override /*  ViewPager.OnPageChangeListener */
    public void onPageSelected(int position) {

    }

    @Override /*  ViewPager.OnPageChangeListener */
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * Responsible for executing search queries against walmart's api and then
     * displaying the results.
     */
    private void _processSearchRequest () {
        //
        // Are we searching?
        //
        if (_bIsSearching)
            return;
        //
        // Do we have valid search criteria
        //
        String itemName = _itemNameEditText.getText().toString();
        if (WBStringUtils.isNullOrEmpty(itemName) || itemName.length() < 2) {
            _itemNameEditText.setError(getString(R.string.broken_rule_cartitem_query_invalid));
            return;
        }
        //
        // Get Category Id
        //
        CategoryModel selCategory = (CategoryModel) _categoriesSpinner.getSelectedItem();
        String categoryId = selCategory.getCategoryId();
        //
        // Close the keyboard if its visible
        //
        _hideKeyboard();
        //
        // Hide current results if they are showing
        //
        if (_bIsShowingResults)
            _toggleSearchResults();
        //
        // Execute search
        //
        _bIsSearching = true;
        _progressbar.setVisibility(View.VISIBLE);
        WalmartAPI.search(itemName, categoryId).continueWith(new Continuation<WBList<ProductModel>, Object>() {
            @Override
            public Object then(Task<WBList<ProductModel>> task) throws Exception {
                _bIsSearching = false;
                _progressbar.setVisibility(View.GONE);

                if (task.isFaulted()) {
                    _showMessage(getString(R.string.error_walmart_search_failure));
                } else {
                    WBList<ProductModel> productModels = task.getResult();
                    if (productModels.size() > 0) {
                        _resultsPager.setAdapter(new SearchResultPagerAdapter(productModels, getSupportFragmentManager()));
                        _toggleSearchResults();
                    } else {
                        _showMessage(getString(R.string.notification_no_items_found));
                    }
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * This method is responsible for fetching the category listing from Walmart
     * @return
     *      The result of the operation, true if success otherwise false.
     */
    private Task<Boolean> _downloadCategories () {
        //
        // Let the user know we are fetching the categories first before
        // we execute their query
        //
        final ProgressDialog dialog = AppUI.createProgressDialog(this, R.string.progress_message_loading_categories);
        dialog.show();

        return WalmartAPI.fetchCategories().continueWith(new Continuation<WBList<CategoryModel>, Boolean>() {
            @Override
            public Boolean then(Task<WBList<CategoryModel>> task) throws Exception {
                dialog.dismiss();
                if (task.isFaulted()) {
                    Snackbar.make(
                                findViewById(R.id.coordinatorLayout),
                                getString(R.string.error_walmart_categories_download_failure),
                                Snackbar.LENGTH_INDEFINITE)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    _downloadCategories ();
                                }
                            })
                            .setActionTextColor(Color.RED)
                            .show();
                    return false;
                } else {
                    WBList<CategoryModel> categories = task.getResult();
                    //
                    // Persist the results to the database
                    //
                    for (CategoryModel categoryModel : categories)
                        DbProvider.save(categoryModel);
                    //
                    // Now populate our spinner
                    //
                    _categoryAdapter.addAll(categories);
                }
                return true;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * Responsible for showing/hiding the search results view
     */
    private void _toggleSearchResults () {
        int height = _resultsContainer.getMeasuredHeight();
        ViewCompat.animate(_contentContainer)
                .yBy(_bIsShowingResults ? -height : height)
                .setDuration(500)
                .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR)
                .withLayer()
                .start();
        _bIsShowingResults = !_bIsShowingResults;
    }

    /**
     * Should be pretty obvious here :-)
     */
    private void _hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Displays messages via the snackbar control
     * @param message
     */
    private void _showMessage (String message){
        Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_LONG)
                .show();
    }


    /**
     * This class is responsible for displaying the product cards after a successful search
     */
    private class SearchResultPagerAdapter extends FragmentStatePagerAdapter {
        private WBList<ProductModel> _items;

        public SearchResultPagerAdapter(WBList<ProductModel> items, FragmentManager fm) {
            super(fm);
            _items = items;
        }

        @Override
        public Fragment getItem(int position) {
            ProductModel productModel = _items.get(position);
            CartItemModel model = new CartItemModel()
                    .setName(productModel.getName())
                    .setPrice(productModel.getSalePrice())
                    .setQuantity(1)
                    .setThumbnailUrl(productModel.getThumbnailUrl())
                    .setProduct(productModel);

            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.bundle_key_cartitem), model);
            NewItemFragment fragment = new NewItemFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return _items.size();
        }
    }

    /**
     * This class is responsible for providing the smooth transitions between
     * product cards.
     */
    private class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    /**
     * This class is responsible for providing a listing of product categories
     */
    private class CategoryDataAdapter extends ArrayAdapter<CategoryModel> {
        private TypedValue _colorPrimaryValue =new TypedValue();

        public CategoryDataAdapter(WBList<CategoryModel> categories) {
            super(NewItemActivity.this, R.layout.new_item_list_item, R.id.new_item_list_text, categories);
            getTheme().resolveAttribute(android.R.attr.colorPrimary, _colorPrimaryValue, true);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = super.getView(position, convertView, parent);
            TextView textView = (TextView) rootView.findViewById(R.id.new_item_list_text);
            textView.setTextColor(_colorPrimaryValue.data);
            //
            // We need to capture the icon used for this item
            //
            TextView iconTextView = (TextView) rootView.findViewById(R.id.new_item_list_icon);
            _setIcon(iconTextView, getItem(position));

            return rootView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View rootView =  super.getDropDownView(position, convertView, parent);
            //
            // We need to capture the icon used for this item
            //
            IconTextView iconTextView = (IconTextView) rootView.findViewById(R.id.new_item_list_icon);
            _setIcon (iconTextView, getItem(position));

            return rootView;
        }

        public int getFoodCategoryPos () {
            int count = super.getCount();
            for (int i = 0; i < count; i++) {
                CategoryModel model = super.getItem(i);
                if (WBStringUtils.areEqual(model.getCategoryId(), "976759"))
                    return i;
            }
            return -1;
        }

        private void _setIcon (TextView textView, CategoryModel model) {
            Iconify.IconValue iconValue;
            switch (model.getCategoryId()) {
                case "91083":
                    //Auto & Tires
                    iconValue = Iconify.IconValue.fa_automobile;
                    break;
                case "3920":
                    // Books
                    iconValue = Iconify.IconValue.fa_book;
                    break;

                case "1105910":
                    //Cell Phones
                    iconValue = Iconify.IconValue.fa_mobile_phone;
                    break;

                case "3944":
                    // Electronics
                    iconValue = Iconify.IconValue.fa_laptop;
                    break;

                case "976759":
                    // Food
                    iconValue = Iconify.IconValue.fa_shopping_cart;
                    break;

                case "1094765":
                    // Gifts & Registry
                    iconValue = Iconify.IconValue.fa_gift;
                    break;

                case "976760":
                    // Health
                    iconValue = Iconify.IconValue.fa_heartbeat;
                    break;

                case "4044":
                case "1072864":
                case "1115193":
                    // HOME
                    iconValue = Iconify.IconValue.fa_home;
                    break;

                case "3891":
                    // Jewelry
                    iconValue = Iconify.IconValue.fa_diamond;
                    break;

                case "4096":
                    // Movies & TV
                    iconValue = Iconify.IconValue.fa_film;
                    break;

                case "4104":
                    // Music
                    iconValue = Iconify.IconValue.fa_music;
                    break;

                case "1229749":
                    // Office
                    iconValue = Iconify.IconValue.fa_fax;
                    break;

                case "5440":
                    // Pets
                    iconValue = Iconify.IconValue.fa_paw;
                    break;

                case "5426":
                    // Photo Center
                    iconValue = Iconify.IconValue.fa_camera;
                    break;

                case "4125":
                    // Sports & Outdoors
                    iconValue = Iconify.IconValue.fa_soccer_ball_o;
                    break;

                case "4171":
                    // Toys
                    iconValue = Iconify.IconValue.fa_rocket;
                    break;

                default:
                    iconValue = Iconify.IconValue.fa_tag;
            }

            Iconify.setIcon(textView, iconValue);
        }
    }
}
