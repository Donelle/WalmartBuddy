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
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBImageUtils;
import com.donellesandersjr.walmartbuddy.domain.Cart;
import com.donellesandersjr.walmartbuddy.models.CartModel;
import com.donellesandersjr.walmartbuddy.web.WalmartAPI;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.google.zxing.Result;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public class ScanItemActivity extends BaseActivity<Cart> implements
        BarCodeScannerFragment.IResultCallback,
        View.OnClickListener
{
    private final String TAG = "com.donellesandersjr.walmartbuddy.activities.ScanItemActivity";

    private ImageView _productImageView;
    private View _productView;
    private TextView _productTitleTextView, _productPriceTextView;
    private EditText _productQuantityEditText;

    private ProductModel _product;
    private boolean _bIsSearching, _bIsShowingResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);

        if (savedInstanceState == null) {
            CartModel model = getIntent().getParcelableExtra(getString(R.string.bundle_key_cart));
            super.setDomainObject(new Cart(model));

            BarCodeScannerFragment fragment = new BarCodeScannerFragment();
            fragment.setmCallBack(this);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.scanner_content, fragment, "BarCodeScannerFragment")
                    .commit();
        }

        _productImageView = (ImageView) findViewById(R.id.scan_item_scan_pic);
        _productView = findViewById(R.id.scan_item_scan_container);

        _productTitleTextView = (TextView) findViewById(R.id.scan_item_scan_title);
        _productPriceTextView = (TextView) findViewById(R.id.scan_item_scan_price);
        _productQuantityEditText = (EditText) findViewById(R.id.scan_item_scan_quantity);

        findViewById(R.id.scan_item_scan_hide).setOnClickListener(this);
        findViewById(R.id.scan_item_scan_add_to_cart).setOnClickListener(this);
        findViewById(R.id.scan_item_close).setOnClickListener(this);
        //
        // Close the keyboard if its visible
        //
        super.hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        _packupAndExit();
    }

    @Override /* BarCodeScannerFragment.IResultCallback */
    public void result(Result lastResult) {
        final String upc = lastResult.toString();
        if (!_canSearch(upc))
            return;

        final ProgressDialog progressDialog = AppUI.createProgressDialog(this, R.string.progress_message_searching);
        progressDialog.show();

        _bIsSearching = true;
        WalmartAPI.fetchProductByUPC(upc).continueWith(new Continuation<WBList<ProductModel>, Bitmap>() {
            @Override
            public Bitmap then(Task<WBList<ProductModel>> task) throws Exception {
                Bitmap thumbnail = null;
                if (task.isFaulted()) {
                    _product = null;
                    final Exception ex = task.getError();
                    Task.call(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            String message = ex instanceof WalmartAPI.WalmartAPIException ?
                                    ex.getMessage() :
                                    getString(R.string.error_processing_request);
                            showMessage(message);
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                } else {
                    ProductModel productModel = task.getResult().first();
                    thumbnail = WBImageUtils.bitmapFromURL(new URL(productModel.getThumbnailUrl()));
                    _product = productModel;
                }
                return thumbnail;
            }
        }).continueWith(new Continuation<Bitmap, Object>() {
            @Override
            public Object then(Task<Bitmap> task) throws Exception {
                Bitmap thumbnail = task.getResult();
                if (thumbnail != null) {
                    double price = _product.getSalePrice();
                    _productTitleTextView.setText(_product.getName());
                    _productPriceTextView.setText(price > 0 ?
                            "Price: " + NumberFormat.getCurrencyInstance().format(price) :
                            "Price not available");
                    _productImageView.setImageBitmap(thumbnail);
                    _productQuantityEditText.setText("");
                    //
                    // Show view
                    //
                    _toggleSearchResults();
                }

                progressDialog.dismiss();
                _bIsSearching = false;
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.scan_item_scan_add_to_cart) {
            final CartItem cartItem = new CartItem()
                    .setName(_product.getName())
                    .setPrice(_product.getSalePrice())
                    .setQuantity(_getIntValue(_productQuantityEditText.getText().toString(), 1))
                    .setProductModel(_product);
            if (cartItem.isValid()) {
                //
                // Close the keyboard if its visible
                //
                super.hideKeyboard();

                final ProgressDialog dialog = AppUI.createProgressDialog(this, R.string.progress_message_saving_new_cartitem);
                dialog.show();

                Task.callInBackground(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        //
                        // Check to make sure the item doesn't already exist in the list
                        // and if so we just skip the whole addition operation.
                        //
                        WBList<CartItem> cartItems = getDomainObject().getCartItems();
                        for (CartItem item : cartItems) {
                            if (item.getProductModel().getProductId() == cartItem.getProductModel().getProductId())
                                return null;
                        }
                        //
                        // Save the item to the shopping list
                        //
                        cartItems.add(cartItem);
                        getDomainObject()
                                .setCartItems(cartItems)
                                .save();
                        return null;
                    }
                }).continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        dialog.dismiss();
                        if (task.isFaulted()) {
                            WBLogger.Error(TAG, task.getError());
                            // Not sure why this would ever happen but we are going to
                            // do the right thing here and just report that shit went wrong.
                            showMessage(getString(R.string.error_cartitem_save_failure));
                        } else {
                            _product = null;
                            //
                            // Hide view
                            //
                            _toggleSearchResults();
                            //
                            // Show message
                            //
                            String message = getString(R.string.notification_cart_item_added);
                            showMessage(String.format(message, 1));
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            } else {
                for (Map.Entry<Integer, String> entry : cartItem.getBrokenRules().entrySet()) {
                    int ruleKey = entry.getKey();
                    if (ruleKey == CartItem.RULE_PRICE ) {
                        super.showMessage(getString(R.string.broken_rule_cartitem_price_invalid));
                    } else if (ruleKey == CartItem.RULE_QUANTITY) {
                        _productQuantityEditText.setError(entry.getValue());
                    }
                }
            }
        } else if (id == R.id.scan_item_close) {
            _packupAndExit ();
        } else if (id == R.id.scan_item_scan_hide) {
            //
            // Clear our search result
            //
            _product = null;
            //
            // Hide our item
            //
            _toggleSearchResults ();
            //
            // Close the keyboard if its visible
            //
            super.hideKeyboard();
        }
    }

    private void _packupAndExit () {
        Intent data = new Intent();
        data.putExtra(getString(R.string.bundle_key_cart), super.getDomainObject().getModel());
        setResult(RESULT_OK, data);
        finish();
    }

    private boolean _canSearch (String upc) {
        //
        // We can NOT search if:
        //  a.) We are currently searching for something OR
        //  b.) The new UPC match the UPC of the product we just performed a search on
        //
        return !_bIsSearching &&
               !_bIsShowingResults &&
               !(_product != null && WBStringUtils.areEqual(_product.getUPC(), upc));
    }

    private int _getIntValue (String stringVal, int defaultVal) {
        try {
            return Integer.valueOf(stringVal);
        } catch (NumberFormatException e2) {
            WBLogger.Error(TAG, "Cannot parse Integer value for " + stringVal);
        }
        return defaultVal;
    }

    private void _toggleSearchResults () {
        int distance = _productView.getMeasuredHeight() + 50;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) _productView.getLayoutParams();
        params.removeRule(RelativeLayout.ABOVE);
        _productView.setLayoutParams(params);

        ViewCompat.animate(_productView)
                .yBy(_bIsShowingResults ? -distance : distance)
                .setDuration(500)
                .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR)
                .withLayer()
                .start();
        _bIsShowingResults = !_bIsShowingResults;
    }
}
