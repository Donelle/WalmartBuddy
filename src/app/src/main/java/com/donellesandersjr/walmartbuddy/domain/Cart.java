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
package com.donellesandersjr.walmartbuddy.domain;

import android.os.Bundle;
import android.os.Parcelable;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBCastable;
import com.donellesandersjr.walmartbuddy.api.WBEquatable;
import com.donellesandersjr.walmartbuddy.api.WBList;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;
import com.donellesandersjr.walmartbuddy.models.CartModel;

import java.util.ArrayList;
import java.util.List;

public final class Cart extends  DomainObject<CartModel> {
    public static final Integer RULE_NAME = 100;
    public static final Integer RULE_TAXRATE = 101;

    private static final String STATE_CART_ITEMS = "Cart.STATE_CART_ITEMS";
    private WBList<CartItem> _cartItems;
    private double _estimatedTotal = 0d;

    private static final WBCastable<CartItem> CARTITEM_MODEL_CASTABLE = new WBCastable<CartItem>() {
        @Override
        public <W extends Parcelable & WBEquatable> W cast(CartItem item) {
            return (W)item.getModel();
        }
    };

    public Cart ()  {
        super();
        setName(null);
        setTaxRate(-1d);
    }

    public Cart (CartModel model) {
        super(model);
        _validateModel();
        _calculateTotal();
    }

    @Override
    protected void onInit() {
        DomainRuleSet ruleSet = new DomainRuleSet();
        ruleSet.addRule(new DomainRuleSet.NullOrEmptyRule(R.string.broken_rule_cart_name_invalid));
        super.getRuleManager().addRuleSet(RULE_NAME, ruleSet);

        ruleSet = new DomainRuleSet();
        ruleSet.addRule(new DomainRuleSet.ExpressionRule(R.string.broken_rule_cart_taxrate_invalid,
                new DomainRulePredicate() {
                    @Override
                    public <T> boolean execute(T data) {
                        Double val = (Double) data;
                        return val >= 0d;
                    }
                }));
        super.getRuleManager().addRuleSet(RULE_TAXRATE, ruleSet);
    }

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelable(STATE_CART_ITEMS, _cartItems);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        _cartItems = state.getParcelable(STATE_CART_ITEMS);
        _validateModel();
        _calculateTotal();
    }

    @Override
    protected boolean onValidate() {
        for (CartItem cartItem : this.getCartItems())
            if (!cartItem.isValid())
                return false;

        return true;
    }

    @Override
    protected void onSave() throws Exception {
        for (CartItem cartItem : this.getCartItems())
            cartItem.save();
    }

    private void _validateModel () {
        DomainRuleSet ruleSet = super.getRuleManager().getRuleSet(RULE_NAME);
        ruleSet.validate(super.getModel().getName());

        ruleSet = super.getRuleManager().getRuleSet(RULE_TAXRATE);
        ruleSet.validate(super.getModel().getTaxRate());
    }

    private void _calculateTotal () {
        double total = 0d;
        for (CartItem cartItem : this.getCartItems())
            total += cartItem.getTotalAmount();

        double taxRate = this.getTaxRate();
        if (taxRate > 0d)
            total *= (.1d * taxRate);

        _estimatedTotal = total;
    }

    public String getName () {
        String name = super.getModel().getName();
        return WBStringUtils.isNullOrEmpty(name) ? "" : name;
    }

    public Cart setName (String name) {
        DomainRuleSet ruleSet = super.getRuleManager().getRuleSet(RULE_NAME);
        ruleSet.validate (name);

        if (ruleSet.isValid())
            super.getModel().setName(name);

        return this;
    }

    public WBList<CartItem> getCartItems () {
        if (_cartItems != null)
            return _cartItems;

        List<CartItemModel> models = super.getModel().getCartItems();
        if (models != null) {
            ArrayList<CartItem> cartItems = new ArrayList<>(models.size());
            for (CartItemModel model : models)
                cartItems.add(new CartItem(model));
            // Adds cart items to list without adding to the internal "added" list
            _cartItems = new WBList<>(cartItems);
        } else {
            _cartItems = new WBList<>();
        }

        return _cartItems;
    }

    public Cart setCartItems (WBList<CartItem> cartItems) {
        if (cartItems != null) {
            _cartItems = cartItems;

            WBList<CartItemModel> models = this.getCartItems().castTo(CARTITEM_MODEL_CASTABLE);
            super.getModel().setCartItems(models);

            _calculateTotal();
        }
        return this;
    }

    public String getZipCode () {
        String zipCode = super.getModel().getZipCode();
        return WBStringUtils.isNullOrEmpty(zipCode) ? "" : zipCode;
    }

    public Cart setZipCode (String zipCode) {
        super.getModel().setZipCode(zipCode);
        return this;
    }

    public double getTaxRate () {
        return super.getModel().getTaxRate();
    }

    public Cart setTaxRate (double taxRate) {
        DomainRuleSet ruleSet = super.getRuleManager().getRuleSet(RULE_TAXRATE);
        ruleSet.validate (taxRate);

        if (ruleSet.isValid()) {
            super.getModel().setTaxRate(taxRate);
            _calculateTotal();
        }

        return this;
    }

    public double getEstimatedTotal () {
        return _estimatedTotal;
    }
}
