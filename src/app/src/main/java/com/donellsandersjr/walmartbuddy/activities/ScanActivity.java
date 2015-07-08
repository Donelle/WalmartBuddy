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

import com.donellsandersjr.walmartbuddy.App;
import com.donellsandersjr.walmartbuddy.AppUI;
import com.donellsandersjr.walmartbuddy.BuildConfig;
import com.donellsandersjr.walmartbuddy.R;
import com.donellsandersjr.walmartbuddy.api.WBJsonUtils;
import com.donellsandersjr.walmartbuddy.api.WBLogger;
import com.donellsandersjr.walmartbuddy.models.ProductModel;
import com.google.zxing.Result;
import com.makeramen.roundedimageview.RoundedImageView;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public class ScanActivity extends BaseActivity implements
        BarCodeScannerFragment.IResultCallback,
        View.OnClickListener {

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

        findViewById(R.id.scanner_close_button).setOnClickListener(this);
        findViewById(R.id.scanner_add_to_cart_button).setOnClickListener(this);
    }

    @Override /* BarCodeScannerFragment.IResultCallback */
    public void result(Result lastResult) {
        WBLogger.Debug(TAG, "UPC: " + lastResult.toString());

        if (!_bIsSearching) {
            final ProgressDialog progressDialog =
                    AppUI.createProgressDialog(this, R.string.progress_message_generic_loading);
            progressDialog.show();

            _bIsSearching = true;
            final String apiCallUrl = String.format("http://api.walmartlabs.com/v1/items?apiKey=%1$s&upc=%2$s&format=json",
                    App.WALMART_APIKEY, lastResult.toString());
            Task.callInBackground(new Callable<JSONObject>() {
                @Override
                public JSONObject call() throws Exception {
                    JSONObject response = null;
                    URL url = new URL(apiCallUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    try {
                        JSONArray items = readFrom(connection.getInputStream()).getJSONArray("items");
                        response = items.getJSONObject(0);
                    } catch (Exception ex) {
                        response = readFrom(connection.getErrorStream());
                        if (response.has("errors")) {
                            if (BuildConfig.DEBUG) {
                                JSONArray errors = response.getJSONArray("errors");
                                for (int i = 0; i < errors.length(); i++) {
                                    JSONObject entry = errors.getJSONObject(i);
                                    WBLogger.Error(TAG, "Request Error - " + entry.getString("message"));
                                }
                            }
                        } else {
                            WBLogger.Error(TAG, ex);
                        }
                        throw ex;
                    } finally {
                        connection.disconnect();
                    }
                    return response;
                }
            }).continueWith(new Continuation<JSONObject, Object>() {
            @Override
            public Object then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    _productView.setVisibility(View.INVISIBLE);
                    Toast.makeText(ScanActivity.this, R.string.error_processing_request, Toast.LENGTH_LONG)
                            .show();
                } else {
                    _product = productFrom(task.getResult());
                    double price = _product.getDoubleValue(ProductModel.KEY_PRICE);

                    _productTitleTextView.setText(_product.getStringValue(ProductModel.KEY_NAME));
                    _productPriceTextView.setText(price > 0 ?
                            NumberFormat.getCurrencyInstance().format(price) :
                            "Price not available");
                    _productView.setVisibility(View.VISIBLE);

                    Task.callInBackground(new Callable<Bitmap>() {
                        @Override
                        public Bitmap call() throws Exception {
                            URL url = new URL(_product.getStringValue(ProductModel.KEY_THUMBNAIL));
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            InputStream inputStream = null;
                            Bitmap thumbnail = null;
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
                                    } catch (Exception ex) {
                                    }
                                connection.disconnect();
                            }
                            return thumbnail;
                        }
                    }).onSuccess(new Continuation<Bitmap, Void>() {
                        @Override
                        public Void then(Task<Bitmap> task) throws Exception {
                            final Bitmap thumbnail = task.getResult();
                            _productImageView.setImageBitmap(thumbnail);
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                }

                progressDialog.dismiss();
                _bIsSearching = false;
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
        }
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.scanner_add_to_cart_button) {

        } else if (id == R.id.scanner_close_button) {
            finish();
        }
    }

    private ProductModel productFrom (JSONObject jsonObject) {
        WBLogger.Debug(TAG, "JSON - " + jsonObject.toString());

        ProductModel model = new ProductModel();
        model.setStringValue(ProductModel.KEY_NAME,
                WBJsonUtils.getString(jsonObject, ProductModel.KEY_NAME, null));
        model.setStringValue(ProductModel.KEY_DESC,
                WBJsonUtils.getString(jsonObject, ProductModel.KEY_DESC, null));
        model.setStringValue(ProductModel.KEY_BRAND,
                WBJsonUtils.getString(jsonObject, ProductModel.KEY_BRAND, null));
        model.setStringValue(ProductModel.KEY_THUMBNAIL,
                WBJsonUtils.getString(jsonObject, ProductModel.KEY_THUMBNAIL, null));
        model.setStringValue(ProductModel.KEY_UPC,
                WBJsonUtils.getString(jsonObject, ProductModel.KEY_UPC, null));
        model.setDoubleValue(ProductModel.KEY_PRICE,
                WBJsonUtils.getDouble(jsonObject, ProductModel.KEY_PRICE, 0));
        model.setDoubleValue(ProductModel.KEY_MSRP,
                WBJsonUtils.getDouble(jsonObject, ProductModel.KEY_MSRP, 0));
        model.setStringValue(ProductModel.KEY_ITEM_TAXONONY_ID,
                WBJsonUtils.getString(jsonObject, ProductModel.KEY_ITEM_TAXONONY_ID, null));
        model.setStringValue(ProductModel.KEY_CATEGORY_TAXONONY_ID,
                WBJsonUtils.getString(jsonObject, ProductModel.KEY_CATEGORY_TAXONONY_ID, null));
        return model;
    }

    private JSONObject readFrom (InputStream stream) {
        JSONObject response = new JSONObject();
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            response = new JSONObject(responseStrBuilder.toString());
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        }
        return response;
    }
}
