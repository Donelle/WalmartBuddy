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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.models.CartModel;
import com.donellesandersjr.walmartbuddy.web.AvalaraAPI;

import org.rocko.bpb.BounceProgressBar;

import bolts.Continuation;
import bolts.Task;

/**
 * Responsible for gathering the tax rate for this shopping list
 */
public class TaxRateDialogFragment extends DialogFragment implements View.OnClickListener {

    public interface TaxRateDialogListener {
        void onDismissed (CartModel model);
    }

    private static final String BUNDLE_ARG = "model";

    private EditText _zipcodeEditText;
    private TextInputLayout _zipcodeLayout;
    private BounceProgressBar _progressbar;
    private CartModel _model;
    private TaxRateDialogListener _dialogListener;

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
        _progressbar = (BounceProgressBar) rootView.findViewById(R.id.taxrate_progressbar);

        Button okButton = (Button) rootView.findViewById(R.id.taxrate_ok);
        okButton.setOnClickListener(this);

        Button skipButton = (Button) rootView.findViewById(R.id.taxrate_skip);
        skipButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog = super.getDialog();
        dialog.setTitle(R.string.title_dialog_taxrate);
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        final Button button = (Button) v;
        if (button.getId() == R.id.taxrate_skip) {
            _dialogListener.onDismissed(_model);
            getDialog().dismiss();
        } else {
            final String zipcode = _zipcodeEditText.getText().toString();
            if (!WBStringUtils.isNullOrEmpty(zipcode)) {
                _hideKeyboard();
                _zipcodeLayout.setError(null);
                _progressbar.setVisibility(View.VISIBLE);
                button.setEnabled(false);

                AvalaraAPI.fetchTaxRate(zipcode).continueWith(new Continuation<Double, Object>() {
                    @Override
                    public Object then(Task<Double> task) throws Exception {
                        if (task.isFaulted()) {
                            _zipcodeEditText.setError(getString(R.string.error_taxrate_search_failure));
                            _progressbar.setVisibility(View.INVISIBLE);
                            button.setEnabled(true);
                        } else {
                            _model.setZipCode(zipcode);
                            _model.setTaxRate(task.getResult());
                            DbProvider.save(_model);
                            _dialogListener.onDismissed(_model);
                            getDialog().dismiss();
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            } else {
                _zipcodeLayout.setError(getString(R.string.broken_rule_cart_zipcode_invalid));
            }
        }
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
}

