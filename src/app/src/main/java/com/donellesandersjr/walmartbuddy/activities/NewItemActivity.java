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
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.fragments.NewItemFragment;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.donellesandersjr.walmartbuddy.web.WalmartAPI;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;


import bolts.Continuation;
import bolts.Task;

public class NewItemActivity extends BaseActivity implements
        View.OnClickListener, ViewPager.OnPageChangeListener {

    private final String TAG = "com.donellesandersjr.walmart.activities.NewItemActivity";

    View _contentContainer,  _resultsContainer;
    EditText _itemNameEditText;
    TextView _resultsCounterTextView;
    SearchResultPagerAdapter _adapter;
    Button _searchButton;
    ProgressBar _progressbar;

    boolean _bIsShowingResults, _bIsSearching;

    private final String STATE_CARTITEM = "NewItemActivity.STATE_CARTITEM";
    CartItem _cartItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        _cartItem = new CartItem();
        if (savedInstanceState != null)
            _cartItem = savedInstanceState.getParcelable(STATE_CARTITEM);

        _contentContainer = findViewById(R.id.new_item_content_container);
        _resultsContainer = findViewById(R.id.new_item_results_container);
        _itemNameEditText = (EditText) findViewById(R.id.new_item_name);
        _resultsCounterTextView = (TextView) findViewById(R.id.new_item_results_counter);
        _progressbar = (ProgressBar) findViewById(R.id.progress_horizontal);
        _searchButton = (Button) findViewById(R.id.new_item_search);
        _searchButton.setOnClickListener(this);

        ViewPager pager = (ViewPager) findViewById(R.id.new_item_results_pager);
        pager.setPageTransformer(true, new DepthPageTransformer());
        pager.setAdapter(_adapter = new SearchResultPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(this);

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
            _processSearchRequest();
        } else if (id == R.id.new_item_action_close) {
            finish();
        } else if (id == R.id.new_item_results_hide) {
            _toggleSearchResults();
        }
    }

    @Override /*  ViewPager.OnPageChangeListener  */
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        String text = String.format("%1$d of %2$d", ++position, _adapter.getCount());
        _resultsCounterTextView.setText(text);
    }

    @Override /*  ViewPager.OnPageChangeListener */
    public void onPageSelected(int position) {

    }

    @Override /*  ViewPager.OnPageChangeListener */
    public void onPageScrollStateChanged(int state) {

    }

    private void _processSearchRequest () {
        //
        // Are we searching?
        //
        if (_bIsSearching || _bIsShowingResults)
            return;
        //
        // Do we have valid search criteria
        //
        String itemNameQuery = _itemNameEditText.getText().toString();
        if (WBStringUtils.isNullOrEmpty(itemNameQuery) || itemNameQuery.length() < 2) {
            _itemNameEditText.setError(getString(R.string.broken_rule_cartitem_query_invalid));
            return;
        }
        //
        // Close the keyboard if its visible
        //
        _hideKeyboard();
        //
        // Execute search
        //
        _bIsSearching = true;
        _progressbar.setVisibility(View.VISIBLE);
        WalmartAPI.search(itemNameQuery).continueWith(new Continuation<WBList<ProductModel>, Object>() {
            @Override
            public Object then(Task<WBList<ProductModel>> task) throws Exception {
                _bIsSearching = false;
                _progressbar.setVisibility(View.GONE);

                if (task.isFaulted()) {
                    _showMessage(getString(R.string.error_walmart_search_failure));
                } else {
                    _adapter.reload(task.getResult());
                    if (_adapter.getCount() > 0) {
                        _toggleSearchResults();
                    } else {
                        _showMessage(getString(R.string.notification_no_items_found));
                    }
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

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

    private void _hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    private void _showMessage (String message){
        Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_LONG)
                .show();
    }

    private class SearchResultPagerAdapter extends FragmentStatePagerAdapter {
        private WBList<ProductModel> _items;

        public SearchResultPagerAdapter(FragmentManager fm) {
            super(fm);
            _items = new WBList<>();
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

        public void reload (WBList<ProductModel> items) {
            _items = items;
            this.notifyDataSetChanged();
        }
    }

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
}
