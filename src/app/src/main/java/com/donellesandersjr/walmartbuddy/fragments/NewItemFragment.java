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


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.AppUI;
import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.domain.CartItem;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;


import java.text.NumberFormat;

public class NewItemFragment extends BaseFragment<CartItem> implements View.OnClickListener {
    private final String TAG = "com.donellesandersjr.walmartbuddy.fragments.NewItemFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            CartItemModel model = getArguments().getParcelable(getString(R.string.bundle_key_cartitem));
            super.setDomainObject(new CartItem(model));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_item, container, false);

        CartItem cartItem = super.getDomainObject();

        TextView textView = (TextView) rootView.findViewById(R.id.item_search_result_name);
        textView.setText(cartItem.getName());

        textView = (TextView) rootView.findViewById(R.id.item_search_result_price);
        textView.setText(cartItem.getPrice() > 0 ?
                 NumberFormat.getCurrencyInstance().format(cartItem.getPrice()) :
                "Price not available");

        ImageView imageView = (ImageView) rootView.findViewById(R.id.item_search_result_photo);
        AppUI.loadImage(cartItem.getThumbnailUrl(), imageView);

        Button selectButton = (Button) rootView.findViewById(R.id.item_search_result_select);
        selectButton.setOnClickListener(this);
        selectButton.setEnabled(cartItem.isValid());

        return rootView;
    }

    @Override /* View.OnClickListener */
    public void onClick(View v) {

    }
}
