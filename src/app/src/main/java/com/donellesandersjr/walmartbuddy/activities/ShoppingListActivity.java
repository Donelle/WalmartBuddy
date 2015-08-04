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

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import java.text.NumberFormat;


public class ShoppingListActivity extends BaseActivity<Cart> implements
        View.OnClickListener, TaxRateDialogFragment.TaxRateDialogListener {

    private final String TAG = "com.donellesandersjr.walmart.activities.ShoppingListActivity";
    private final int NEW_ITEM_RESULT = 100;

    private ShoppingCartAdapter _adapter;
    private TextView _totalTextView;
    private TextView _cartItemTotalTextView;
    private TextView _taxIncludedTextView;
    private TextView _subtotalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        if (savedInstanceState == null) {
            WBList<CartModel> models = DbProvider.fetchCarts(null);
            Cart shoppingCart = null;
            if (models.size() == 0) {
                try {
                    // This means its the first time the app been run so lets create the default
                    // shopping list and save it to the db.
                    shoppingCart = new Cart()
                            .setName("Default")
                            .setZipCode("")
                            .setTaxRate(0d);
                    shoppingCart.save();
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                }
            } else {
                shoppingCart = new Cart(models.first());
            }

            super.setDomainObject(shoppingCart);
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
        recyclerView.setAdapter(_adapter = new ShoppingCartAdapter());
        recyclerView.addItemDecoration(new ShoppingCartItemDecorator());

        ItemTouchHelper helper = new ItemTouchHelper(new ShoppingListItemCallback());
        helper.attachToRecyclerView(recyclerView);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.shopping_list_add_button);
        addButton.setOnClickListener(this);
        //
        // If we are running on kitkat devices we need to remove the margin so the icon
        // will lineup correctly with the recyclerview
        //
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) addButton.getLayoutParams();
            params.setMarginStart(0);
            addButton.setLayoutParams(params);
        }

        _cartItemTotalTextView = (TextView) findViewById(R.id.shopping_list_cartitem_total);
        _totalTextView = (TextView) findViewById(R.id.shopping_list_total);
        _taxIncludedTextView = (TextView) findViewById(R.id.shopping_list_tax_included);
        _subtotalTextView = (TextView) findViewById(R.id.shopping_list_subtotal);
        _setEstimatedTotal();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_taxrate) {
            _displayTaxRateDialog();
        } else if (id == R.id.action_clearall_item) {
            if (getDomainObject().getCartItems().size() > 0) {
                try {
                    //
                    // Remove and save
                    //
                    final WBList<CartItem> cartItems = _adapter.removeAll();
                    super.getDomainObject().save();
                    _setEstimatedTotal();
                    //
                    // Display the undo button
                    //
                    String notification = getString(R.string.notification_cart_items_removed, cartItems.size());
                    Snackbar.make(findViewById(R.id.coordinatorLayout), notification, Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        //
                                        // Reverse the changes and save
                                        //
                                        _adapter.insertAll(cartItems);
                                        getDomainObject().save();
                                        _setEstimatedTotal();
                                    } catch (Exception ex) {
                                        WBLogger.Error(TAG, ex);
                                        showMessage(getString(R.string.error_cart_save_failure));
                                    }
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.md_red_500))
                            .show();
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                    super.showMessage(getString(R.string.error_cart_save_failure));
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_ITEM_RESULT && resultCode == RESULT_OK) {
            CartModel model = data.getParcelableExtra(getString(R.string.bundle_key_cart));
            super.setDomainObject(new Cart(model));
            _adapter.notifyDataSetChanged();
            _setEstimatedTotal();
        }
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        Intent intent = new Intent(this, ScanItemActivity.class);
        intent.putExtra(getString(R.string.bundle_key_cart), super.getDomainObject().getModel());
        startActivityForResult(intent, NEW_ITEM_RESULT);
    }

    @Override /* TaxDialog.TaxDialogListener */
    public void onDismissed(CartModel model) {
        super.setDomainObject(new Cart(model));
        _setEstimatedTotal();
        //
        // Save that we've shown the tax rate setup atleast once
        //
        AppPreferences.savePreference(AppPreferences.PREFERENCE_DISPLAY_TAXRATE_SETUP, true);
    }

    /**
     * This method displays the tax rate dialog allowing the user to update the tax info
     * associated with this shopping list.
     */
    private void _displayTaxRateDialog () {
        TaxRateDialogFragment dialog = TaxRateDialogFragment.newInstance(super.getDomainObject().getModel());
        dialog.setDismissListener(this)
                .show(getFragmentManager(), "TaxRateDialogFragment");
    }

    /**
     * This method updates the area at the top that displays the total amount of money
     * and items associated with the shopping list.
     */
    private void _setEstimatedTotal () {
        //
        // Set the Total
        //
        double taxTotal = super.getDomainObject().getTaxTotal();
        boolean isValid = taxTotal > 0;
        _totalTextView.setText(isValid ? NumberFormat.getCurrencyInstance().format(taxTotal) : "$0");
        //
        // Set the subtotal
        //
        double subtotal = super.getDomainObject().getSubTotal();
        _subtotalTextView.setText(isValid ?  NumberFormat.getCurrencyInstance().format(subtotal) +  " subtotal" : "");
        //
        // Set the number of Items
        //
        int items = super.getDomainObject().getCartItems().size();
        isValid = items > 0;
        _cartItemTotalTextView.setText(isValid ? String.format("%d Items", items) : "");
        //
        // Set whether or not tax is included in the total
        //
        float taxRate = Double.valueOf(super.getDomainObject().getTaxRate()).floatValue();
        isValid = taxTotal > 0 && taxRate > 0f ;
        _taxIncludedTextView.setText(String.format("(%.3f%% Tax)", taxRate));
        _taxIncludedTextView.setVisibility(isValid ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * This class is responsible for managing the swipe events happening in the recyclerview
     */
    private class ShoppingListItemCallback extends ItemTouchHelper.Callback {
        private Drawable _backgroundDrawable, _bitmapDrawable, _defaultBackgroundDrawable;
        private final int IMAGE_SIZE_DP = 32, PADDING_DP = 16;

        public ShoppingListItemCallback () {
            super();
            _backgroundDrawable = new ColorDrawable(getResources().getColor(R.color.md_red_a700));
            _defaultBackgroundDrawable =  new ColorDrawable(getResources().getColor(R.color.md_grey_100));
            _bitmapDrawable = getResources().getDrawable(R.mipmap.ic_delete);
        }

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
            try {
                ShoppingCartAdapter.ShoppingCartItemView dataItem =
                        (ShoppingCartAdapter.ShoppingCartItemView) viewHolder;
                final CartItem item = dataItem.getCartItem();
                //
                // Remove and save
                //
                final int prevIndex = _adapter.removeItem(item);
                getDomainObject().save();
                _setEstimatedTotal();
                //
                // Display the undo button
                //
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.notification_cart_item_removed, Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //
                                    // Reverse the changes and save
                                    //
                                    _adapter.insertItem(prevIndex, item);
                                    getDomainObject().save();
                                    _setEstimatedTotal();
                                } catch (Exception ex) {
                                    WBLogger.Error(TAG, ex);
                                    showMessage(getString(R.string.error_cart_save_failure));
                                }
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.md_red_500))
                        .show();
            } catch (Exception ex) {
                WBLogger.Error(TAG, ex);
                showMessage(getString(R.string.error_cart_save_failure));
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View child = viewHolder.itemView;
            if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                Rect bounds = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                _defaultBackgroundDrawable.setBounds(bounds);
                _defaultBackgroundDrawable.draw(c);
            } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                Rect bounds = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                int dpImageSize = Double.valueOf(getDPUnits(IMAGE_SIZE_DP)).intValue();
                //
                // Draw the red background
                //
                _backgroundDrawable.setBounds(bounds);
                _backgroundDrawable.draw(c);
                //
                // We resize and center the icon vertically
                //
                bounds.right -= Double.valueOf(getDPUnits(PADDING_DP)).intValue();
                bounds.left = bounds.right - dpImageSize;
                bounds.top += ((bounds.height() - dpImageSize) / 2);
                bounds.bottom = bounds.top + dpImageSize;

                _bitmapDrawable.setBounds(bounds);
                _bitmapDrawable.draw(c);
            }
        }
    }

    /**
     * This class is responsible for drawing the divider below the item
     */
    private class ShoppingCartItemDecorator extends RecyclerView.ItemDecoration {
        private Drawable _backgroundDrawable;

        public ShoppingCartItemDecorator() {
            _backgroundDrawable = new ColorDrawable(getResources().getColor(R.color.md_grey_300));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

            int childCount = parent.getChildCount() - 1; // We skip drawing on the last item in the list
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                Rect bounds = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                bounds.top += bounds.height() - 2; // It was easier to read doing it this way
                //
                // Draw the divider
                //
                _backgroundDrawable.setBounds(bounds);
                _backgroundDrawable.draw(c);
            }
        }
    }

    /**
     * This class is responsible for managing the data associated with the recyclerview
     */
    private class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ShoppingCartItemView> {
        @Override
        public ShoppingCartItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(ShoppingListActivity.this).inflate(
                    R.layout.shopping_list_item, parent, false);
            return new ShoppingCartItemView (itemView);
        }

        @Override
        public void onBindViewHolder(ShoppingCartItemView holder, int position) {
            holder.bind(getDomainObject().getCartItems().get(position));
        }

        @Override
        public int getItemCount() {
            return getDomainObject().getCartItems().size();
        }

        public int removeItem (CartItem item) {
            WBList<CartItem> items = getDomainObject().getCartItems();
            int ndx = items.indexOf(item);
            items.remove(item);
            getDomainObject().setCartItems(items);
            notifyItemRemoved(ndx);
            return ndx;
        }

        public void insertItem (int index, CartItem item) {
            WBList<CartItem> items = getDomainObject().getCartItems();
            items.add(index, item);
            getDomainObject().setCartItems(items);
            notifyItemInserted(index);
        }

        public void insertAll (WBList<CartItem> items) {
            getDomainObject().setCartItems(items);
            notifyItemRangeInserted(0, items.size());
        }

        public WBList<CartItem> removeAll () {
            WBList<CartItem> items = getDomainObject().getCartItems();
            getDomainObject().setCartItems(new WBList<CartItem>());
            notifyItemRangeRemoved(0, items.size());
            return items;
        }

        public class ShoppingCartItemView extends RecyclerView.ViewHolder implements View.OnClickListener {

            private CheckBox _checkedOffChkbox;
            private TextView _nameTextView, _quantityTextView, _priceTextView;
            private CartItem _cartItem;

            public ShoppingCartItemView (View itemView) {
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

            public CartItem getCartItem () {
                return _cartItem;
            }
        }
    }
}
