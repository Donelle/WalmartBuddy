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

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.IconTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.donellsandersjr.walmartbuddy.R;
import com.joanzapata.android.iconify.Iconify;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;


public class ShoppingListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.shopping_list_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        _buildFloatingMenu ();
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

    private void _buildFloatingMenu () {
        TextView textView = new TextView(this);
        textView.setTextSize(30f);
        textView.setTextColor(Color.WHITE);
        textView.setText("+");

        FloatingActionButton addButton =
                new FloatingActionButton.Builder(this)
                        .setContentView(textView)
                        .setPosition(FloatingActionButton.POSITION_BOTTOM_RIGHT)
                        .setBackgroundDrawable(R.drawable.round_button)
                        .build();

        textView = new IconTextView(this);
        textView.setLayoutParams(new RelativeLayout.LayoutParams(24, 24));
        textView.setTextSize(15f);
        textView.setTextColor(Color.WHITE);
        Iconify.setIcon(textView, Iconify.IconValue.fa_pencil);

        SubActionButton.Builder builder = new SubActionButton.Builder(this);
        SubActionButton newItemButton =
                builder.setContentView(textView)
                        .setBackgroundDrawable(getDrawable(R.drawable.small_round_button))
                        .build();

        textView = new IconTextView(this);
        textView.setLayoutParams(new RelativeLayout.LayoutParams(24, 24));
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(15f);
        Iconify.setIcon(textView, Iconify.IconValue.fa_barcode);

        SubActionButton scanButton =
                builder.setContentView(textView)
                        .setBackgroundDrawable(getDrawable(R.drawable.small_round_button))
                        .build();
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShoppingListActivity.this, ScanActivity.class));
            }
        });

        new FloatingActionMenu.Builder(this)
                .addSubActionView(newItemButton)
                .addSubActionView(scanButton)
                .attachTo(addButton)
                .build();
    }
}
