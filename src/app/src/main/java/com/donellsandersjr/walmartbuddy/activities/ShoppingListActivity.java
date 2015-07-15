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


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
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


import com.donellsandersjr.walmartbuddy.R;
import com.donellsandersjr.walmartbuddy.api.WBStringUtils;
import com.donellsandersjr.walmartbuddy.db.CartItemDb;
import com.donellsandersjr.walmartbuddy.db.DbProvider;
import com.donellsandersjr.walmartbuddy.fragments.TaxRateDialogFragment;
import com.donellsandersjr.walmartbuddy.models.CartItemModel;
import com.donellsandersjr.walmartbuddy.models.CartModel;
import com.yahoo.squidb.data.SquidCursor;

import java.text.NumberFormat;


public class ShoppingListActivity extends BaseActivity implements
        View.OnClickListener, TaxRateDialogFragment.TaxRateDialogListener {

    private CartModel _cartModel;
    private ShoppingListAdapter _adapter;
    private TextView _totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        getSupportActionBar().setElevation(0);

        _cartModel = DbProvider.fetchCart();
        if (WBStringUtils.isNullOrEmpty(_cartModel.getZipCode())) {
            TaxRateDialogFragment dialog = TaxRateDialogFragment.newInstance(_cartModel);
            dialog.setDismissListener(this)
                  .show(getFragmentManager(), "ZipcodeDialog");
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.shopping_list_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(_adapter = new ShoppingListAdapter());

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.shopping_list_add_button);
        addButton.setOnClickListener(this);

        _totalTextView = (TextView) findViewById(R.id.shopping_list_total);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {

    }

    @Override /* TaxDialog.TaxDialogListener */
    public void onDismissed(CartModel model) {
        _cartModel = model;
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
            holder.bind(_cartModel.getCartItems().get(position));
        }

        @Override
        public int getItemCount() {
            return _cartModel.getCartItems().size();
        }


        public class ShoppingListItemView extends RecyclerView.ViewHolder implements View.OnClickListener {
            CheckBox _checkedOffChkbox;
            TextView _nameTextView, _quantityTextView, _priceTextView;
            View _crossedoutView;
            CartItemModel _model;

            public ShoppingListItemView (View itemView) {
                super(itemView);
                _checkedOffChkbox = (CheckBox) itemView.findViewById(R.id.shopping_list_item_checkedoff);
                _checkedOffChkbox.setOnClickListener(this);
                _nameTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_name);
                _quantityTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_quantity);
                _priceTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_price);
                _crossedoutView = itemView.findViewById(R.id.shopping_list_item_crossout);
            }

            public void bind (CartItemModel model) {
                _model = model;
                _checkedOffChkbox.setChecked(model.getCheckedOff());
                _nameTextView.setText(model.getName());
                _priceTextView.setText(NumberFormat.getCurrencyInstance().format(model.getPrice()));

                int quantity = model.getQuantity();
                _quantityTextView.setText(quantity > 0 ? String.valueOf(quantity) : "");

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) _crossedoutView.getLayoutParams();
                params.width = model.getCheckedOff() ? RelativeLayout.LayoutParams.MATCH_PARENT : 0;
                _crossedoutView.setLayoutParams(params);
            }

            @Override
            public void onClick(View v) {

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) _crossedoutView.getLayoutParams();
                params.width = !_model.getCheckedOff() ? RelativeLayout.LayoutParams.MATCH_PARENT : 0;
                _crossedoutView.setLayoutParams(params);

                _model.setCheckedOff(!_model.getCheckedOff());
                DbProvider.save(_model);
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Scrolling animation implementaion
     * @ref https://guides.codepath.com/android/Floating-Action-Buttons
     */
    public static class AddCartItemBehavior extends FloatingActionButton.Behavior {
        private static final android.view.animation.Interpolator INTERPOLATOR =
                new FastOutSlowInInterpolator();
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
                    .setInterpolator(INTERPOLATOR).withLayer()
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
                    .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                    .start();
        }
    }
}
