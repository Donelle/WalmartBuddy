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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.App;
import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBImageUtils;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.db.CartItemDb;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.db.ProductDb;
import com.donellesandersjr.walmartbuddy.domain.Cart;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.domain.DomainRuleObserver;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;
import com.donellesandersjr.walmartbuddy.models.CartModel;
import com.donellesandersjr.walmartbuddy.models.ProductModel;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public class NewItemActivity extends BaseActivity implements
        View.OnClickListener, AdapterView.OnItemClickListener {

    private final String TAG = "com.donellesandersjr.walmart.activities.NewItemActivity";

    private AutoCompleteTextView _itemNameEditText;
    private EditText _priceEditText;
    private EditText _quantityEditText;
    private ImageView _snapshotImageView;

    private final String STATE_CART = "NewItemActivity.STATE_CART";
    private Cart _cart;
    private CartItem _newCartItem;

    private final int REQUEST_CODE_CAPTURE_IMAGE = 100;
    private Uri _snapshotUri;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _createCartItem();
        if (savedInstanceState != null) {
            _cart = savedInstanceState.getParcelable(STATE_CART);
        } else {
            CartModel model = getIntent().getParcelableExtra(getString(R.string.bundle_key_cart));
            _cart = new Cart(model);
        }

        setContentView(R.layout.activity_new_item);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        _itemNameEditText = (AutoCompleteTextView) findViewById(R.id.new_item_name);
        _itemNameEditText.addTextChangedListener(_itemNameWatcher);
        _itemNameEditText.setOnItemClickListener(this);
        _itemNameEditText.setAdapter(new ProductDataAdapter());
        _itemNameEditText.setThreshold(2);

        _priceEditText =(EditText) findViewById(R.id.new_item_price);
        _priceEditText.addTextChangedListener(_itemPriceWatcher);

        _quantityEditText = (EditText) findViewById(R.id.new_item_qty);
        _quantityEditText.addTextChangedListener(_itemQtyWatcher);

        _snapshotImageView = (ImageView)findViewById(R.id.new_item_photo);
        _snapshotImageView.setOnClickListener(this);

        findViewById(R.id.new_item_add_to_cart).setOnClickListener(this);
        findViewById(R.id.new_item_close).setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable(STATE_CART, _cart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_item, menu);
        IconDrawable iconDrawable =
                new IconDrawable(this, Iconify.IconValue.fa_barcode)
                        .color(Color.WHITE)
                        .actionBarSize();
        menu.findItem(R.id.action_scan_item).setIcon(iconDrawable);
        iconDrawable =
                new IconDrawable(this, Iconify.IconValue.fa_search)
                        .color(Color.WHITE)
                        .actionBarSize();
        menu.findItem(R.id.action_search_item).setIcon(iconDrawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan_item) {
            Intent intent = new Intent(this, ScanItemActivity.class);
            intent.putExtra(getString(R.string.bundle_key_cart), _cart.getModel());
            startActivity(intent);
        } else if (id == R.id.action_search_item) {
            Intent intent = new Intent(this, SearchItemActivity.class);
            intent.putExtra(getString(R.string.bundle_key_cart), _cart.getModel());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            final Point displayDims = _calculateARC();
            Task.callInBackground(new Callable<Bitmap>() {
                @Override
                public Bitmap call() throws Exception {
                    FileInputStream stream = new FileInputStream(_snapshotUri.getPath());
                    Bitmap photo = null;
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(stream, null, options);
                        stream.close();

                        options.inSampleSize = WBImageUtils.calculateInSampleSize(
                                options, displayDims.x, displayDims.y);
                        options.inJustDecodeBounds = false;

                        stream = new FileInputStream(_snapshotUri.getPath());
                        photo = BitmapFactory.decodeStream(stream, null, options);
                    } finally {
                        if (stream != null)
                            try {
                                stream.close();
                            } catch (Exception ex) {}
                    }
                    return photo;
                }
            }).onSuccess(new Continuation<Bitmap, Void>() {
                @Override
                public Void then(Task<Bitmap> task) throws Exception {
                    _snapshotImageView.setImageBitmap(task.getResult());
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        }
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.new_item_close) {
            finish();
        } else if (id == R.id.new_item_add_to_cart) {
            _addItemToCart();
        } else if (id == R.id.new_item_photo) {
            File dir = super.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            _snapshotUri = Uri.fromFile(new File(dir, "Snapshot.jpeg"));
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, _snapshotUri);
            startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
        }
    }

    @Override /* AdapterView.OnItemClickListener */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private Point _calculateARC() {
        /* http://andrew.hedges.name/experiments/aspect_ratio/  */
        Display display = super.getWindowManager().getDefaultDisplay();
        Point displayDims = new Point();
        display.getSize(displayDims);
        int newWidth = super.getPixels(super.getDPUnits(100));

        displayDims.y = displayDims.y / displayDims.x * newWidth;
        displayDims.x = newWidth;
        return displayDims;
    }

    private void _createCartItem () {
        _newCartItem = new CartItem();
        _newCartItem.subscribeToRule(CartItem.RULE_NAME, new DomainRuleObserver() {
            @Override
            public void onRuleChanged(boolean isBroken, String message) {
                _itemNameEditText.setError(isBroken ? message : null);
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
            final ProgressDialog dialog = AppUI.createProgressDialog(this, R.string.progress_message_saving_new_cartitem);
            dialog.show();

            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    //
                    // Did we take a photo?
                    //
                    if (_snapshotUri != null) {
                        Bitmap bitmap = WBImageUtils.bitmapFromUri(URI.create(_snapshotUri.getPath()));
                        Uri fileUri = Uri.fromFile(App.createSnapshotFile());
                        WBImageUtils.compressToFile(bitmap, URI.create(fileUri.getPath()));
                        _newCartItem.setThumbnailUrl(fileUri.getPath());
                    }
                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    if (task.isFaulted()) {
                        WBLogger.Error(TAG, task.getError());
                        Task.call(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                //
                                // Let the user know that we couldn't save the pic to disk
                                //
                                _showMessage(getString(R.string.error_photo_save_failure));
                                return null;
                            }
                        }, Task.UI_THREAD_EXECUTOR);
                    }
                    //
                    // Save the item
                    //
                    WBList<CartItem> cartItems = _cart.getCartItems();
                    cartItems.add(_newCartItem);
                    _cart.setCartItems(cartItems).save();
                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    dialog.dismiss();
                    if(task.isFaulted()) {
                        WBLogger.Error(TAG, task.getError());
                        //
                        // Something went wrong so let the user know
                        //
                        _showMessage(getString(R.string.error_cartitem_save_failure));
                    } else {
                        //
                        // Reset our UI
                        //
                        _resetUI();
                        //
                        // Create empty cart item
                        //
                        _createCartItem();
                        //
                        // Notify the user item added
                        //
                        _showMessage(getString(R.string.notification_cartitem_item_added));
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
                   _itemNameEditText.setError(rule.getValue());
                else if (id == CartItem.RULE_PRICE)
                    _priceEditText.setError(rule.getValue());
                else if (id == CartItem.RULE_QUANTITY)
                    _quantityEditText.setError(rule.getValue());
            }

            _showMessage(getString(R.string.error_cartitem_validation_failure));
        }
    }


    private void _resetUI () {
        _itemNameEditText.removeTextChangedListener(_itemNameWatcher);
        _itemNameEditText.setText("");
        _itemNameEditText.addTextChangedListener(_itemNameWatcher);

        _priceEditText.removeTextChangedListener(_itemPriceWatcher);
        _priceEditText.setText("");
        _priceEditText.addTextChangedListener(_itemPriceWatcher);

        _quantityEditText.removeTextChangedListener(_itemQtyWatcher);
        _quantityEditText.setText("");
        _quantityEditText.addTextChangedListener(_itemQtyWatcher);

        _snapshotImageView.setImageBitmap(null);
        _snapshotUri = null;
    }

    private void _showMessage (String message){
        Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_LONG)
                .show();
    }


    private class ProductDataAdapter extends BaseAdapter implements Filterable {
        private WBList<CartItemModel> _inventory;

        @Override
        public int getCount() {
            return _inventory.size();
        }

        @Override
        public Object getItem(int position) {
            return _inventory.get(position);
        }

        @Override
        public long getItemId(int position) {
            return _inventory.get(position).getIntValue(CartItemDb.DEFAULT_ID_COLUMN);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.new_item_list_item, parent, false);

            TextView textView = (TextView) convertView.findViewById(R.id.new_item_list_text);
            textView.setText(_inventory.get(position).getName());

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    if (constraint != null) {
                        _inventory = DbProvider.fetchCartItems(CartItemDb.NAME.like(constraint));
                        FilterResults results = new FilterResults();
                        results.values = _inventory;
                        results.count = _inventory.size();
                        return results;
                    }
                    return null;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    /* NOOP */
                }
            };
        }


    }
}
