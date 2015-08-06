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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.App;
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
import com.joanzapata.android.iconify.Iconify;
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
    private TextView _productTitleTextView;
    private EditText _productPriceEditText;
    private TextView _shoppingListTotalTextView;
    private EditText _productQuantityEditText;

    private ProductModel _product;
    private boolean _bIsSearching, _bIsShowingResults;
    private int _resultsOverlayDistance;

    private BarCodeScannerFragment _scannerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);

        if (savedInstanceState == null) {
            CartModel model = getIntent().getParcelableExtra(getString(R.string.bundle_key_cart));
            super.setDomainObject(new Cart(model));

            _scannerFragment  = new BarCodeScannerFragment();
            _scannerFragment.setmCallBack(this);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.scanner_content, _scannerFragment, "BarCodeScannerFragment")
                    .commit();
        }

        _productImageView = (ImageView) findViewById(R.id.scan_item_scan_pic);
        _productView = findViewById(R.id.scan_item_scan_container);
        final GestureDetectorCompat compat = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                //
                // This lets the detector know we are interested in the rest of the gesture
                //
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (velocityY < 0) {
                    _hideResultsAndStartScan();
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

        });
        _productView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return compat.onTouchEvent(event);
            }
        });

        _productTitleTextView = (TextView) findViewById(R.id.scan_item_scan_title);
        _productPriceEditText = (EditText) findViewById(R.id.scan_item_scan_price);
        _shoppingListTotalTextView = (TextView) findViewById(R.id.scan_item_total);
        _productQuantityEditText = (EditText) findViewById(R.id.scan_item_scan_quantity);

        findViewById(R.id.scan_item_scan_hide).setOnClickListener(this);
        findViewById(R.id.scan_item_scan_add_to_cart).setOnClickListener(this);
        findViewById(R.id.scan_item_close).setOnClickListener(this);

        _resultsOverlayDistance = Double.valueOf(super.getDPUnits(50)).intValue();
        //
        // Calculate the total
        //
        _setEstimatedTotal();
        //
        // Close the keyboard if its visible
        //
        super.hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        _packupAndExit();
        super.onBackPressed();
    }

    @Override /* BarCodeScannerFragment.IResultCallback */
    public void result(Result lastResult) {
        //
        // See if we can search because this method rapid fires if the
        // user still has the lazer pointed at the upc. We don't want
        // to keep trying to search if one is already in progress.
        //
        final String upc = lastResult.toString();
        if (!_canSearch(upc))
            return;
        //
        // Make sure we have network connectivity before even going throug
        // the trouble.
        //
        if (!_hasNetworkConnectivity()) {
            showMessage(getString(R.string.error_no_network_connectivity));
            return;
        }
        //
        // If we already have results showing then just hide it
        //
        if (_bIsShowingResults)
            _toggleSearchResults();

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
                    try {
                        thumbnail = WBImageUtils.bitmapFromURL(new URL(productModel.getThumbnailUrl()));
                    } catch (Exception ex){
                        //
                        // We continue on even if we don't have a image to display
                        //
                    }
                    _product = productModel;
                }
                return thumbnail;
            }
        }).continueWith(new Continuation<Bitmap, Object>() {
            @Override
            public Object then(Task<Bitmap> task) throws Exception {
                if (_product != null) {
                    double price = _product.getSalePrice();
                    _productTitleTextView.setText(_product.getName());
                    _productPriceEditText.setText(String.format("%.2f", Double.valueOf(_product.getSalePrice()).floatValue()));
                    _productImageView.setImageBitmap(task.getResult());
                    _productQuantityEditText.setText("");
                    //
                    // Show view
                    //
                    _toggleSearchResults();
                    //
                    // Turn off scanning
                    //
                    _scannerFragment.stopScan();
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
                    .setPrice(_getDoubleValue(_productPriceEditText.getText().toString(), 0))
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
                        boolean bfound = false;
                        //
                        // Check to make sure the item doesn't already exist in the list
                        // and if so we just skip the whole addition operation.
                        //
                        WBList<CartItem> cartItems = getDomainObject().getCartItems();
                        for (CartItem item : cartItems) {
                            if (item.getProductModel().getProductId() == _product.getProductId()) {
                                item.setQuantity(item.getQuantity() + cartItem.getQuantity());
                                item.setPrice(cartItem.getPrice());
                                bfound = true;
                                break;
                            }
                        }
                        if (!bfound)
                            cartItems.add(cartItem);
                        //
                        // Save the item to the shopping list
                        //
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
                            // Show the new total
                            //
                            _setEstimatedTotal();
                            //
                            // Show message
                            //
                            String message = getString(R.string.notification_cart_item_added);
                            showMessage(message);
                        }
                        //
                        // Start scanning
                        //
                        _scannerFragment.startScan();
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            } else {
                for (Map.Entry<Integer, String> entry : cartItem.getBrokenRules().entrySet()) {
                    int ruleKey = entry.getKey();
                    if (ruleKey == CartItem.RULE_PRICE ) {
                        _productPriceEditText.setError(entry.getValue());
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
            _hideResultsAndStartScan();
        }
    }

    /**
     * Seems pretty obvious :)
     */
    private void _packupAndExit () {
        Intent data = new Intent();
        data.putExtra(getString(R.string.bundle_key_cart), super.getDomainObject().getModel());
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * Determines whether or not search can be carried out on the UPC
     * @param upc
     * @return
     */
    private boolean _canSearch (String upc) {
        //
        // We can NOT search if:
        //  a.) We are currently searching for something OR
        //  b.) The new UPC match the UPC of the product we just performed a search on
        //
        return !_bIsSearching &&
               !(_product != null && WBStringUtils.areEqual(_product.getUPC(), upc));
    }

    /**
     * Again pretty obvious what this does :)
     * @return
     */
    private boolean _hasNetworkConnectivity () {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        //
        // Check wifi
        //
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected())
            return true;
        //
        // Check data
        //
        NetworkInfo data = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return data.isConnected();
    }

    /**
     * Utility method for getting an int value
     * @param stringVal
     * @param defaultVal
     * @return
     */
    private int _getIntValue (String stringVal, int defaultVal) {
        try {
            return Integer.valueOf(stringVal);
        } catch (NumberFormatException e2) {
            WBLogger.Error(TAG, "Cannot parse Integer value for " + stringVal);
        }
        return defaultVal;
    }

    /**
     * Utility method for getting an double value
     * @param stringVal
     * @param defaultVal
     * @return
     */
    private double _getDoubleValue (String stringVal, double defaultVal) {
        try {
            return Double.valueOf(stringVal);
        } catch (NumberFormatException e2) {
            WBLogger.Error(TAG, "Cannot parse Double value for " + stringVal);
        }
        return defaultVal;
    }

    /**
     * Toggles the visiblity of the result made by searching walmart's inventory
     */
    private void _toggleSearchResults () {
        int distance = _productView.getMeasuredHeight() + _resultsOverlayDistance;

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

    /**
     * This method updates the area at the top that displays the total amount of money
     * for the shopping list.
     */
    private void _setEstimatedTotal () {
        //
        // Set the Total
        //
        double taxTotal = super.getDomainObject().getTaxTotal();
        String total = taxTotal > 0 ? NumberFormat.getCurrencyInstance().format(taxTotal) : "$0";
        CharSequence displayText = Iconify.compute(String.format("{fa-shopping-cart} %s", total));
        _shoppingListTotalTextView.setText(displayText);
    }

    /**
     * :-)
     */
    private void _hideResultsAndStartScan () {
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
        hideKeyboard();
        //
        // Start scanning
        //
        _scannerFragment.startScan();
    }
}
