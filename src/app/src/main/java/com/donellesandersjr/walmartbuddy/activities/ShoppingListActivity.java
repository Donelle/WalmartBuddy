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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.donellesandersjr.walmartbuddy.AppPreferences;
import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.domain.Cart;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.fragments.TaxRateDialogFragment;
import com.donellesandersjr.walmartbuddy.models.CartModel;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.text.NumberFormat;


public class ShoppingListActivity extends BaseActivity implements
        View.OnClickListener, TaxRateDialogFragment.TaxRateDialogListener {

    private final String TAG = "com.donellesandersjr.walmart.activities.ShoppingListActivity";

    private final String STATE_SHOPPING_CART = "ShoppingListActivity.STATE_SHOPPING_CART";
    private Cart _shoppingCart;

    private ShoppingListAdapter _adapter;
    private TextView _totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shopping_list);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");

        _shoppingCart = new Cart();
        if (savedInstanceState != null)
            _shoppingCart = savedInstanceState.getParcelable(STATE_SHOPPING_CART);
        //
        // Figure out if we've already shown this dialog before because
        // we don't want to "harass" the user with this. They can choose
        // to find out their tax rate or not.
        //
        if (!AppPreferences.getBooleanPreference(AppPreferences.PREFERENCE_DISPLAY_TAXRATE_SETUP))
           _displayTaxRateDialog();


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.shopping_list_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(_adapter = new ShoppingListAdapter());

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.shopping_list_add_button);
        addButton.setOnClickListener(this);

        _totalTextView = (TextView) findViewById(R.id.shopping_list_total);
        _totalTextView.setText(NumberFormat.getCurrencyInstance().format(_shoppingCart.getEstimatedTotal()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);

        IconDrawable iconDrawable =
                new IconDrawable(this, Iconify.IconValue.fa_barcode)
                    .color(Color.WHITE)
                    .actionBarSize();
        menu.findItem(R.id.action_scan_item).setIcon(iconDrawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan_item) {

        } else if (id == R.id.action_taxrate) {
            _displayTaxRateDialog();
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {

    }

    @Override /* TaxDialog.TaxDialogListener */
    public void onDismissed(CartModel model) {
        _shoppingCart = new Cart(model);
        _totalTextView.setText(NumberFormat.getCurrencyInstance().format(_shoppingCart.getEstimatedTotal()));
        //
        // Save that we've shown the tax rate setup atleast once
        //
        AppPreferences.savePreference(AppPreferences.PREFERENCE_DISPLAY_TAXRATE_SETUP, true);
    }


    private void _displayTaxRateDialog () {
        TaxRateDialogFragment dialog = TaxRateDialogFragment.newInstance(_shoppingCart.getModel());
        dialog.setDismissListener(this)
                .show(getFragmentManager(), "TaxRateDialogFragment");
    }


    private class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListItemView> {

        public ShoppingListAdapter () {
            super();
        }

        @Override
        public ShoppingListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(ShoppingListActivity.this).inflate(
                    R.layout.shopping_list_item, parent, false);
            return new ShoppingListItemView (itemView);
        }

        @Override
        public void onBindViewHolder(ShoppingListItemView holder, int position) {
            holder.bind(_shoppingCart.getCartItems().get(position));
        }

        @Override
        public int getItemCount() {
            return _shoppingCart.getCartItems().size();
        }


        public class ShoppingListItemView extends RecyclerView.ViewHolder implements View.OnClickListener {
            CheckBox _checkedOffChkbox;
            TextView _nameTextView, _quantityTextView, _priceTextView;
            View _crossedoutView;
            CartItem _cartItem;

            public ShoppingListItemView (View itemView) {
                super(itemView);
                _checkedOffChkbox = (CheckBox) itemView.findViewById(R.id.shopping_list_item_checkedoff);
                _checkedOffChkbox.setOnClickListener(this);
                _nameTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_name);
                _quantityTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_quantity);
                _priceTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_price);
                _crossedoutView = itemView.findViewById(R.id.shopping_list_item_crossout);
            }

            public void bind (CartItem cartItem) {
                _cartItem = cartItem;
                _checkedOffChkbox.setChecked(_cartItem.getCheckedOff());
                _nameTextView.setText(_cartItem.getName());
                _priceTextView.setText(NumberFormat.getCurrencyInstance().format(_cartItem.getPrice()));

                int quantity = _cartItem.getQuantity();
                _quantityTextView.setText(quantity > 0 ? String.valueOf(quantity) : "");

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) _crossedoutView.getLayoutParams();
                params.width = _cartItem.getCheckedOff() ? RelativeLayout.LayoutParams.MATCH_PARENT : 0;
                _crossedoutView.setLayoutParams(params);
            }

            @Override
            public void onClick(View v) {
                ViewCompat.animate(_crossedoutView)
                        .scaleX(!_cartItem.getCheckedOff() ? 1.0F : 0 )
                        .setDuration(500)
                        .setInterpolator(AppUI.DECELERATE_INTERPOLATOR)
                        .start();

                _cartItem.setCheckedOff(!_cartItem.getCheckedOff());
                try {
                    _cartItem.save();
                    notifyDataSetChanged();
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                    Snackbar.make(getWindow().getDecorView(), R.string.error_cartitem_save_failure, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }
    }

    /**
     * Scrolling animation implementaion
     * @ref https://guides.codepath.com/android/Floating-Action-Buttons
     */
    public static class AddCartItemBehavior extends FloatingActionButton.Behavior {
        private boolean mIsAnimatingOut = false;

        public AddCartItemBehavior (Context context, AttributeSet attrs) {
            super();
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                           FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                    super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                            nestedScrollAxes);
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                                   View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                    dyUnconsumed);

            if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
                animateOut(child);
            } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
                animateIn(child);
            }
        }

        // Same animation that FloatingActionButton.Behavior uses to
        // hide the FAB when the AppBarLayout exits
        private void animateOut(final FloatingActionButton button) {
            ViewCompat.animate(button).scaleX(0.0F).scaleY(0.0F).alpha(0.0F)
                    .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer()
                    .setListener(new ViewPropertyAnimatorListener() {
                        public void onAnimationStart(View view) {
                            AddCartItemBehavior.this.mIsAnimatingOut = true;
                        }

                        public void onAnimationCancel(View view) {
                            AddCartItemBehavior.this.mIsAnimatingOut = false;
                        }

                        public void onAnimationEnd(View view) {
                            AddCartItemBehavior.this.mIsAnimatingOut = false;
                            view.setVisibility(View.GONE);
                        }
                    }).start();
        }

        // Same animation that FloatingActionButton.Behavior
        // uses to show the FAB when the AppBarLayout enters
        private void animateIn(FloatingActionButton button) {
            button.setVisibility(View.VISIBLE);
            ViewCompat.animate(button).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                    .setInterpolator(AppUI.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer().setListener(null)
                    .start();
        }
    }
}
