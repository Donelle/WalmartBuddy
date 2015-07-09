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
package com.donellsandersjr.walmartbuddy.activities;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.donellsandersjr.walmartbuddy.AppUI;
import com.donellsandersjr.walmartbuddy.R;
import com.donellsandersjr.walmartbuddy.WalmartAPI;
import com.donellsandersjr.walmartbuddy.api.WBList;
import com.donellsandersjr.walmartbuddy.api.WBLogger;
import com.donellsandersjr.walmartbuddy.models.ProductModel;
import com.google.zxing.Result;
import com.makeramen.roundedimageview.RoundedImageView;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public class ScanActivity extends BaseActivity implements
        BarCodeScannerFragment.IResultCallback,
        View.OnClickListener
{

    private final String TAG = "com.donellesandersjr.walmartbuddy.activities.ScanActivity";
    private final String FRAGMENT_TAG = "BarCodeScannerFragment";

    private RoundedImageView _productImageView;
    private View _productView;
    private TextView _productTitleTextView, _productPriceTextView;
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
                    .add(R.id.scanner_content, fragment, FRAGMENT_TAG)
                    .commit();
        }

        _productImageView = (RoundedImageView) findViewById(R.id.scanner_scan_pic);
        _productImageView.setCornerRadius(getDPUnits(20));
        _productImageView.setOval(true);
        _productImageView.mutateBackground(true);

        _productView = findViewById(R.id.scanner_scan_container);
        _productTitleTextView = (TextView) findViewById(R.id.scanner_scan_title);
        _productPriceTextView = (TextView) findViewById(R.id.scanner_scan_price);

        findViewById(R.id.scanner_add_to_cart_button).setOnClickListener(this);
    }

    @Override /* BarCodeScannerFragment.IResultCallback */
    public void result(Result lastResult) {
       if (_bIsSearching)
            return;

        final ProgressDialog progressDialog = AppUI.createProgressDialog(this, R.string.progress_message_generic_loading);
        progressDialog.show();

        _bIsSearching = true;
        WalmartAPI.fetchProductByUPC(lastResult.toString())
                .continueWith(new Continuation<WBList<ProductModel>, Bitmap>() {
                    @Override
                    public Bitmap then(Task<WBList<ProductModel>> task) throws Exception {
                        Bitmap thumbnail = null;
                        if (task.isFaulted()) {
                            Task.call(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    progressDialog.dismiss();
                                    _bIsSearching = false;
                                    _productView.setVisibility(View.INVISIBLE);
                                    Toast.makeText(ScanActivity.this, R.string.error_processing_request, Toast.LENGTH_LONG)
                                         .show();
                                    return null;
                                }
                            }, Task.UI_THREAD_EXECUTOR);
                        } else {
                            _product = task.getResult().first();
                            URL url = new URL(_product.getThumbnailUrl());
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            InputStream inputStream = null;
                            try {
                                inputStream = new BufferedInputStream(connection.getInputStream());
                                BufferedInputStream buffIn = new BufferedInputStream(inputStream);
                                thumbnail = BitmapFactory.decodeStream(buffIn);
                            } catch (IOException ex) {
                                WBLogger.Error(TAG, ex);
                                throw ex;
                            } finally {
                                if (inputStream != null)
                                    try {
                                        inputStream.close();
                                    } catch (Exception ex) {}
                                connection.disconnect();
                            }
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
                                    NumberFormat.getCurrencyInstance().format(price) :
                                    "Price not available");
                            _productView.setVisibility(View.VISIBLE);
                            _productImageView.setImageBitmap(thumbnail);

                            progressDialog.dismiss();
                            _bIsSearching = false;
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.scanner_add_to_cart_button) {

        }
    }
}
