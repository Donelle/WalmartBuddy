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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.domain.Cart;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.domain.DomainRuleObserver;
import com.donellesandersjr.walmartbuddy.models.CartModel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class NewItemDialogFragment extends DialogFragment implements View.OnClickListener {

    private final String TAG = "com.donellesandersjr.walmart.fragments.NewItemDialogFragment";
    private static final String BUNDLE_ARG = "model";

    private EditText _nameEditText;
    private EditText _priceEditText;
    private EditText _quantityEditText;
    private ProgressBar _progressbar;

    private CartItem _newCartItem;
    private NewItemDialogListener _dialogListener;
    private DialogInterface.OnCancelListener _cancelListener;

    public interface NewItemDialogListener {
        void onDismissed (CartModel model);
    }

    public static NewItemDialogFragment newInstance(CartModel model, NewItemDialogListener listener) {
        NewItemDialogFragment fragment = new NewItemDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_ARG, model);
        fragment.setArguments(args);
        fragment._dialogListener = listener;

        return fragment;
    }

    private interface OnTextChangedListener {
        void onTextChanged (String newText);
    }

    private static class EditTextWatcher implements TextWatcher {
        private OnTextChangedListener _listener;
        private String _previousText;

        public EditTextWatcher (OnTextChangedListener listener) {
            _listener = listener;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            _previousText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (!_previousText.contentEquals(s.toString()))
                _listener.onTextChanged(s.toString());
        }

        public static EditTextWatcher newInstance (OnTextChangedListener listener) {
            return new EditTextWatcher(listener);
        }
    }

    private EditTextWatcher _itemNameWatcher = EditTextWatcher.newInstance(new OnTextChangedListener() {
        @Override
        public void onTextChanged(String newText) {
            _newCartItem.setName(newText);
        }
    });

    private EditTextWatcher _itemPriceWatcher = EditTextWatcher.newInstance(new OnTextChangedListener() {
        @Override
        public void onTextChanged(String newText) {
            _newCartItem.setPrice(Double.parseDouble(newText));
        }
    });

    private EditTextWatcher _itemQtyWatcher = EditTextWatcher.newInstance(new OnTextChangedListener() {
        @Override
        public void onTextChanged(String newText) {
            _newCartItem.setQuantity(Integer.parseInt(newText));
        }
    });


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog = super.getDialog();
        dialog.setTitle(R.string.title_dialog_newitem);
        dialog.setCanceledOnTouchOutside(false);
        _createCartItem();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_newitem, container, false);

        _nameEditText = (EditText) rootView.findViewById(R.id.new_item_name);
        _nameEditText.addTextChangedListener(_itemNameWatcher);

        _priceEditText = (EditText) rootView.findViewById(R.id.new_item_price);
        _priceEditText.addTextChangedListener(_itemPriceWatcher);

        _quantityEditText = (EditText) rootView.findViewById(R.id.new_item_qty);
        _quantityEditText.addTextChangedListener(_itemQtyWatcher);

        _progressbar = (ProgressBar) rootView.findViewById(R.id.progressbar);

        rootView.findViewById(R.id.new_item_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.new_item_ok).setOnClickListener(this);

        return rootView;
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.new_item_ok) {
            _addItemToCart();
        } else if (id == R.id.new_item_cancel) {
            _dialogListener.onDismissed(null);
            super.getDialog().dismiss();
        }
    }

    private void _createCartItem () {
        _newCartItem = new CartItem();
        _newCartItem.subscribeToRule(CartItem.RULE_NAME, new DomainRuleObserver() {
            @Override
            public void onRuleChanged(boolean isBroken, String message) {
                _nameEditText.setError(isBroken ? message : null);
            }
        });
        _newCartItem.subscribeToRule(CartItem.RULE_PRICE, new DomainRuleObserver() {
            @Override
            public void onRuleChanged(boolean isBroken, String message) {
                _priceEditText.setError(isBroken ? message : null);
            }
        });
        _newCartItem.subscribeToRule(CartItem.RULE_QUANTITY, new DomainRuleObserver() {
            @Override
            public void onRuleChanged(boolean isBroken, String message) {
                _quantityEditText.setError(isBroken ? message : null);
            }
        });
    }

    private void _addItemToCart () {
        if (_newCartItem.isValid()) {
            _progressbar.setVisibility(View.VISIBLE);
            Task.callInBackground(new Callable<CartModel>() {
                @Override
                public CartModel call() throws Exception {
                    CartModel model = getArguments().getParcelable(BUNDLE_ARG);
                    Cart cart = new Cart(model);
                    //
                    // Save the item
                    //
                    WBList<CartItem> cartItems = cart.getCartItems();
                    cartItems.add(_newCartItem);
                    cart.setCartItems(cartItems).save();

                    return model;
                }
            }).continueWith(new Continuation<CartModel, Object>() {
                @Override
                public Object then(Task<CartModel> task) throws Exception {
                    _progressbar.setVisibility(View.INVISIBLE);
                    if(task.isFaulted()) {
                        WBLogger.Error(TAG, task.getError());
                        //
                        // Something went wrong so.. we should do something here
                        //
                        _nameEditText.setError(getString(R.string.error_cartitem_save_failure));
                    } else {
                        _dialogListener.onDismissed(task.getResult());
                        getDialog().dismiss();
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        } else {
            //
            // Trigger the visual display of errors in the UI
            //
            Set<Map.Entry<Integer, String>> brokenRules = _newCartItem.getBrokenRules().entrySet();
            for (Map.Entry<Integer, String> rule : brokenRules) {
                int id = rule.getKey();
                if (id == CartItem.RULE_NAME)
                    _nameEditText.setError(rule.getValue());
                else if (id == CartItem.RULE_PRICE)
                    _priceEditText.setError(rule.getValue());
                else if (id == CartItem.RULE_QUANTITY)
                    _quantityEditText.setError(rule.getValue());
            }
        }
    }
}
