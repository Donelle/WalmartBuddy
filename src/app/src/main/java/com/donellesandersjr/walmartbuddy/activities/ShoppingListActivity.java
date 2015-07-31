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
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;


import com.donellesandersjr.walmartbuddy.AppPreferences;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.domain.Cart;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.fragments.TaxRateDialogFragment;
import com.donellesandersjr.walmartbuddy.models.CartModel;
import com.joanzapata.android.iconify.Iconify;

import java.text.NumberFormat;


public class ShoppingListActivity extends BaseActivity implements
        View.OnClickListener, TaxRateDialogFragment.TaxRateDialogListener {

    private final String TAG = "com.donellesandersjr.walmart.activities.ShoppingListActivity";

    private final String STATE_SHOPPING_CART = "ShoppingListActivity.STATE_SHOPPING_CART";
    private Cart _shoppingCart;

    private ShoppingListAdapter _adapter;
    private TextView _totalTextView, _cartItemTotalTextView, _taxIncludedTextView;

    private final int NEW_ITEM_RESULT = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        if (savedInstanceState != null)
            _shoppingCart = savedInstanceState.getParcelable(STATE_SHOPPING_CART);
        else {
            WBList<CartModel> models = DbProvider.fetchCarts(null);
            if (models.size() == 0) {
                try {
                    // This means its the first time the app been run so lets create the default
                    // shopping list and save it to the db.
                    _shoppingCart = new Cart()
                            .setName("Default")
                            .setZipCode("")
                            .setTaxRate(0d);
                    _shoppingCart.save();
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                }
            } else {
                _shoppingCart = new Cart(models.first());
            }
        }
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
        recyclerView.addItemDecoration(new ShoppingListItemDecorator());

        ItemTouchHelper helper = new ItemTouchHelper(new ShoppingListItemCallback());
        helper.attachToRecyclerView(recyclerView);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.shopping_list_add_button);
        addButton.setOnClickListener(this);

        _cartItemTotalTextView = (TextView) findViewById(R.id.shopping_list_cartitem_total);
        _totalTextView = (TextView) findViewById(R.id.shopping_list_total);
        _taxIncludedTextView = (TextView) findViewById(R.id.shopping_list_tax_included);
        _setEstimatedTotal();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_taxrate) {
            _displayTaxRateDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_ITEM_RESULT && resultCode == RESULT_OK) {
            CartModel model = data.getParcelableExtra(getString(R.string.bundle_key_cart));
            _shoppingCart = new Cart(model);
            _adapter.notifyDataSetChanged();
            _setEstimatedTotal();
        }
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        Intent intent = new Intent(this, NewItemActivity.class);
        intent.putExtra(getString(R.string.bundle_key_cart), _shoppingCart.getModel());
        startActivityForResult(intent, NEW_ITEM_RESULT);
    }

    @Override /* TaxDialog.TaxDialogListener */
    public void onDismissed(CartModel model) {
        _shoppingCart = new Cart(model);
        _setEstimatedTotal();
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

    private void _setEstimatedTotal () {
        //
        // Set the Total
        //
        double estimatedTotal = _shoppingCart.getEstimatedTotal();
        boolean isValid = estimatedTotal > 0;
        _totalTextView.setText(isValid ? NumberFormat.getCurrencyInstance().format(estimatedTotal) : "$0");
        //
        // Set the number of Items
        //
        int items = _shoppingCart.getCartItems().size();
        isValid = items > 0;
        _cartItemTotalTextView.setText(isValid ? String.format("%d Items", items) : "");
        //
        // Set whether or not tax is included in the total
        //
        isValid = estimatedTotal > 0 && _shoppingCart.getTaxRate() > 0d ;
        _taxIncludedTextView.setVisibility(isValid ? View.VISIBLE : View.INVISIBLE);
    }


    private void _removeCartItem (int position) {
        _adapter.removeItem(position);
        final CartItem item = _shoppingCart.getCartItems().get(position);
        Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.notification_cart_item_removed, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       _adapter.addItem(item);
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.md_red_500))
                .show();
    }


    private class ShoppingListItemCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.START);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            _removeCartItem(viewHolder.getAdapterPosition());
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

    }

    private class ShoppingListItemDecorator extends RecyclerView.ItemDecoration {
        private Drawable _drawable;

        public ShoppingListItemDecorator() {
            _drawable = getDrawable(R.drawable.shopping_list_item_delete);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                _drawable.setBounds(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                _drawable.draw(c);
            }
        }
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

        public void removeItem (int position) {
            WBList<CartItem> items = _shoppingCart.getCartItems();
            items.remove(position);
            _shoppingCart.setCartItems(items);
            notifyItemRemoved(position);
        }

        public void addItem (CartItem item) {
            WBList<CartItem> items = _shoppingCart.getCartItems();
            items.add(item);
            _shoppingCart.setCartItems(items);
            notifyDataSetChanged();
        }

        public class ShoppingListItemView extends RecyclerView.ViewHolder implements View.OnClickListener {

            private CheckBox _checkedOffChkbox;
            private TextView _nameTextView, _quantityTextView, _priceTextView;
            private CartItem _cartItem;


            public ShoppingListItemView (View itemView) {
                super(itemView);
                _checkedOffChkbox = (CheckBox) itemView.findViewById(R.id.shopping_list_item_checkedoff);
                _checkedOffChkbox.setOnClickListener(this);
                _nameTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_name);
                _quantityTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_quantity);
                _priceTextView = (TextView) itemView.findViewById(R.id.shopping_list_item_price);
            }

            @Override /* View.OnClickListener */
            public void onClick(View v) {
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


            public void bind (CartItem cartItem) {
                _cartItem = cartItem;
                _checkedOffChkbox.setChecked(_cartItem.getCheckedOff());
                _nameTextView.setText(_cartItem.getName());
                _nameTextView.setPaintFlags(_cartItem.getCheckedOff() ?
                        _nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG :
                        _nameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                _priceTextView.setText(formatter.format(_cartItem.getTotalAmount()));
                _quantityTextView.setText(String.valueOf(_cartItem.getQuantity()) + " x " + formatter.format(_cartItem.getPrice()) + " each");
            }

        }
    }
}
