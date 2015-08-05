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
package com.donellesandersjr.walmartbuddy.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.models.CartModel;
import com.donellesandersjr.walmartbuddy.models.StoreModel;
import com.donellesandersjr.walmartbuddy.web.AvalaraAPI;
import com.donellesandersjr.walmartbuddy.web.WalmartAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Objects;

import bolts.Continuation;
import bolts.Task;

/**
 * Responsible for gathering the tax rate for this shopping list
 */
public class TaxRateDialogFragment extends DialogFragment implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public interface TaxRateDialogListener {
        void onDismissed (CartModel model);
    }

    private final String TAG = "com.donellesandersjr.walmart.fragments.TaxRateDialogFragment";
    private static final String BUNDLE_ARG = "model";

    private EditText _zipcodeEditText;
    private TextInputLayout _zipcodeLayout;
    private ProgressBar _progressbar;
    private Button _okButton;

    private CartModel _model;
    private TaxRateDialogListener _dialogListener;
    private GoogleApiClient _gacLocations;

    public static TaxRateDialogFragment newInstance(CartModel model) {
        TaxRateDialogFragment fragment = new TaxRateDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_ARG, model);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _model = getArguments().getParcelable(BUNDLE_ARG);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_taxrate, container, false);
        _zipcodeEditText = (EditText) rootView.findViewById(R.id.taxrate_zipcode);
        _zipcodeLayout = (TextInputLayout) rootView.findViewById(R.id.taxrate_inputLayout);
        _progressbar = (ProgressBar) rootView.findViewById(R.id.taxrate_progressbar);
        _progressbar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.md_orange_800), PorterDuff.Mode.SRC_IN);
        _okButton = (Button) rootView.findViewById(R.id.taxrate_ok);
        _okButton.setOnClickListener(this);

        rootView.findViewById(R.id.taxrate_skip).setOnClickListener(this);
        rootView.findViewById(R.id.taxrate_usecurrentlocation).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog = super.getDialog();
        dialog.setTitle(R.string.title_dialog_taxrate);
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.taxrate_skip) {
            _dialogListener.onDismissed(_model);
            getDialog().dismiss();
        } else if (id == R.id.taxrate_usecurrentlocation) {
            if (_gacLocations == null)
                _gacLocations =
                        new GoogleApiClient.Builder(this.getActivity())
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .build();
            //
            // Reset UI controls
            //
            _hideKeyboard();
            _zipcodeLayout.setError(null);
            _progressbar.setVisibility(View.VISIBLE);
            _okButton.setEnabled(false);
            //
            // Connect to the location services
            //
            _gacLocations.connect();
        } else {
            final String zipcode = _zipcodeEditText.getText().toString();
            if (!WBStringUtils.isNullOrEmpty(zipcode)) {
                //
                // Reset UI controls
                //
                _hideKeyboard();
                _zipcodeLayout.setError(null);
                _progressbar.setVisibility(View.VISIBLE);
                _okButton.setEnabled(false);
                //
                // Fetch all the walmarts located within the zip code
                //
                WalmartAPI.lookupStore(zipcode)
                        .continueWithTask(_fetchTaxRate())
                        .continueWith(_updateUI(), Task.UI_THREAD_EXECUTOR);
            } else {
                _zipcodeLayout.setError(getString(R.string.broken_rule_cart_zipcode_invalid));
            }
        }
    }

    @Override /* GoogleApiClient.ConnectionCallbacks */
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(_gacLocations);
        if (location != null) {
            //
            // Fetch all the walmarts located within the zip code
            //
            WalmartAPI.lookupStore(location.getLongitude(), location.getLatitude())
                    .continueWithTask(_fetchTaxRate())
                    .continueWith(_updateUI(), Task.UI_THREAD_EXECUTOR);
        } else {
            _displayGmsConnectionError();
        }
        _gacLocations.disconnect();
    }

    @Override /* GoogleApiClient.ConnectionCallbacks */
    public void onConnectionSuspended(int i) {
        _displayGmsConnectionError();
        _gacLocations.disconnect();
    }

    @Override /* GoogleApiClient.OnConnectionFailedListener */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        _displayGmsConnectionError();
        _gacLocations.disconnect();
    }

    public TaxRateDialogFragment setDismissListener (TaxRateDialogListener listener) {
        _dialogListener = listener;
        return this;
    }

    /**
     * Should be pretty obvious here :-)
     */
    private void _hideKeyboard() {
        // Check if no view has focus:
        View view = this.getDialog().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    private void _displayGmsConnectionError () {
        _zipcodeEditText.setError(getString(R.string.error_taxrate_location_services_failure));
        _okButton.setEnabled(true);
        _progressbar.setVisibility(View.INVISIBLE);
    }

    /**
     * Takes the result from the call to the Walmart API and uses it to fetch
     * the tax rate using the Avalara API
     * @return
     */
    private Continuation<StoreModel, Task<Double>> _fetchTaxRate () {
        return new Continuation<StoreModel, Task<Double>>() {
            @Override
            public Task<Double> then(Task<StoreModel> task) throws Exception {
                if (task.isFaulted())
                    throw task.getError();
                //
                // Now use the walmart store address to get the tax rate for
                // that area.
                //
                return AvalaraAPI.fetchTaxRate(task.getResult());
            }
        };
    }

    /**
     * Updates the user interface based on the result from the Avalara API
     * @return
     */
    private Continuation<Double, Object> _updateUI () {
        return new Continuation<Double, Object>() {
            @Override
            public Object then(Task<Double> task) throws Exception {
                if (task.isFaulted()) {
                    WBLogger.Error(TAG, task.getError());
                    _zipcodeEditText.setError(getString(R.string.error_taxrate_search_failure));
                    _okButton.setEnabled(true);
                    _progressbar.setVisibility(View.INVISIBLE);
                } else {
                    //
                    // Set the tax rate and then exit
                    //
                    _model.setTaxRate(task.getResult());
                    DbProvider.save(_model);
                    _dialogListener.onDismissed(_model);
                    getDialog().dismiss();
                }
                return null;
            }
        };
    }
}

