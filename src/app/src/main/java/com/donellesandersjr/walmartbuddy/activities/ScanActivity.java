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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBImageUtils;
import com.donellesandersjr.walmartbuddy.web.WalmartAPI;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.google.zxing.Result;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public class ScanActivity extends BaseActivity implements
        BarCodeScannerFragment.IResultCallback,
        View.OnClickListener
{

    private final String TAG = "com.donellesandersjr.walmartbuddy.activities.ScanActivity";

    private ImageView _productImageView;
    private View _productView;
    private TextView _productTitleTextView, _productPriceTextView;
    private EditText _productQuantityEditText;

    private ProductModel _product;
    private boolean _bIsSearching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        if (savedInstanceState == null) {
            BarCodeScannerFragment fragment = new BarCodeScannerFragment();
            fragment.setmCallBack(this);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.scanner_content, fragment, "BarCodeScannerFragment")
                    .commit();
        }

        _productImageView = (ImageView) findViewById(R.id.scanner_scan_pic);
        _productView = findViewById(R.id.scanner_scan_container);

        _productTitleTextView = (TextView) findViewById(R.id.scanner_scan_title);
        _productPriceTextView = (TextView) findViewById(R.id.scanner_scan_price);
        _productQuantityEditText = (EditText) findViewById(R.id.scanner_scan_quantity);

        findViewById(R.id.scanner_add_to_cart_button).setOnClickListener(this);


        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
                        Task.call(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                _productView.setVisibility(View.INVISIBLE);
                                Toast.makeText(ScanActivity.this, R.string.error_processing_request, Toast.LENGTH_LONG)
                                        .show();
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
            })
            .continueWith(new Continuation<Bitmap, Object>() {
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
                        //
                        // Show view
                        //
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                                _productView.getLayoutParams();
                        _productView.animate().translationYBy(-params.height);
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
        if (id == R.id.scanner_add_to_cart_button) {
            CartItem cartItem = new CartItem()
                    .setName(_product.getName())
                    .setPrice(_product.getSalePrice())
                    .setQuantity(_getIntValue(_productQuantityEditText.getText().toString(), 1))
                    .setProductModel(_product);
            if (cartItem.isValid()) {
                try {
                    cartItem.save();
                    //
                    // Hide view
                    //
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                            _productView.getLayoutParams();
                    _productView.animate().translationYBy(params.height);
                    //
                    // Show message
                    //
                    String message = getString(R.string.notification_cartitem_item_added);
                    Toast.makeText(this, String.format(message, 1), Toast.LENGTH_LONG)
                            .show();
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                    // Not sure why this would ever happen but we are going to
                    // do the right thing here and just report that shit went wrong.
                    AppUI.createErrorAlert(this, getString(R.string.error_cartitem_save_failure))
                         .show();
                }
            } else {
                for (Map.Entry<Integer, String> entry : cartItem.getBrokenRules().entrySet()) {
                    int ruleKey = entry.getKey();
                    if (ruleKey == CartItem.RULE_PRICE ) {
                        AppUI.createErrorAlert(this, getString(R.string.broken_rule_cartitem_price_invalid))
                             .show();
                    } else if (ruleKey == CartItem.RULE_QUANTITY) {
                        _productQuantityEditText.setError(entry.getValue());
                    }
                }
            }
        }
    }

    private boolean _canSearch (String upc) {
        //
        // We can NOT search if:
        //  a.) We are currently searching for something OR
        //  b.) The new UPC match the UPC of the product we just performed a search on
        //
        return !_bIsSearching &&
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

}
