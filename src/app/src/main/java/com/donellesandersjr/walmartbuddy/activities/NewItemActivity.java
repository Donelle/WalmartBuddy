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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.fragments.NewItemFragment;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.donellesandersjr.walmartbuddy.web.WalmartAPI;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import org.rocko.bpb.BounceProgressBar;

import bolts.Continuation;
import bolts.Task;

public class NewItemActivity extends BaseActivity implements
        View.OnClickListener {

    private final String TAG = "com.donellesandersjr.walmart.activities.NewItemActivity";

    ViewPager _resultsPager;
    View _contentContainer,  _resultsContainer;
    BounceProgressBar _progressbar;
    EditText _itemNameEditText;

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
        _resultsPager = (ViewPager) findViewById(R.id.new_item_results_pager);
        _resultsPager.setPageTransformer(true, new DepthPageTransformer());
        _progressbar = (BounceProgressBar) findViewById(R.id.new_item_search_progressbar);
        _itemNameEditText = (EditText) findViewById(R.id.new_item_name);

        Button button = (Button) findViewById(R.id.new_item_search);
        button.setOnClickListener(this);

        button = (Button) findViewById(R.id.new_item_add_to_cart);
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
            if (_bIsSearching) return;

            _bIsSearching = true;
            _progressbar.setVisibility(View.VISIBLE);
            WalmartAPI.search(_cartItem.getName()).continueWith(new Continuation<WBList<ProductModel>, Object>() {
                @Override
                public Object then(Task<WBList<ProductModel>> task) throws Exception {
                    _bIsSearching = false;
                    _progressbar.setVisibility(View.INVISIBLE);

                    if (task.isFaulted()) {
                        //
                        // TODO: Display snackbar with a failed search message
                        //
                    } else {
                        SearchResultPagerAdapter adapter =
                                new SearchResultPagerAdapter(task.getResult(), NewItemActivity.this.getSupportFragmentManager());
                        _resultsPager.setAdapter(adapter);

                        int height = _resultsContainer.getLayoutParams().height;
                        ViewCompat.animate(_contentContainer)
                                .yBy(_bIsShowingResults ? -height : height)
                                .setDuration(500)
                                .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR)
                                .withLayer()
                                .start();
                        _bIsShowingResults = !_bIsShowingResults;
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        } else if (id == R.id.new_item_action_close) {
            finish();
        }
    }


    private class SearchResultPagerAdapter extends FragmentStatePagerAdapter {
        private WBList<ProductModel> _items;

        public SearchResultPagerAdapter(WBList<ProductModel> items, FragmentManager fm) {
            super(fm);
            _items = items;
        }

        @Override
        public Fragment getItem(int position) {
            //
            // TODO: You need to transform the product model into a cart item
            //
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.bundle_key_cartitem), null);
            NewItemFragment fragment = new NewItemFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return _items.size();
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
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
