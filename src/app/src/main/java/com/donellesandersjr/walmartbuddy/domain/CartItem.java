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

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.models.CartItemModel;
import com.donellesandersjr.walmartbuddy.models.ProductModel;

public final class CartItem extends DomainObject<CartItemModel> {
    public static final Integer RULE_NAME = 100;
    public static final Integer RULE_PRICE = 101;
    public static final Integer RULE_QUANTITY = 102;

    private static String STATE_PRODUCT_MODEL = "CartItem.STATE_PRODUCT_MODEL";
    private ProductModel _productModel;

    public CartItem () {
        super();
        setPrice(0);
        setQuantity(0);
        setName(null);
    }

    public CartItem (CartItemModel model) {
        super(model);
        _validateModel();
    }

    @Override
    protected void onInit() {
        DomainRuleSet ruleSet = new DomainRuleSet();
        ruleSet.addRule(new DomainRuleSet.NullOrEmptyRule(R.string.broken_rule_cartitem_name_invalid));
        super.getRuleManager().addRuleSet(RULE_NAME, ruleSet);

        ruleSet = new DomainRuleSet();
        ruleSet.addRule(new DomainRuleSet.GreaterThanZeroRule(R.string.broken_rule_cartitem_quantity_invalid));
        super.getRuleManager().addRuleSet(RULE_QUANTITY, ruleSet);

        ruleSet = new DomainRuleSet();
        ruleSet.addRule(new DomainRuleSet.ExpressionRule(R.string.broken_rule_cartitem_price_invalid, new DomainRulePredicate() {
            @Override
            public <T> boolean execute(T data) {
                Double price = (Double) data;
                return price >= .01;
            }
        }));
        super.getRuleManager().addRuleSet(RULE_PRICE, ruleSet);
    }

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelable(STATE_PRODUCT_MODEL, _productModel);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        _productModel = state.getParcelable(STATE_PRODUCT_MODEL);
        _validateModel();
    }

    @Override
    protected void onSave() throws Exception {
        if (_productModel != null)
            DbProvider.save(_productModel);
    }

    private void _validateModel () {
        DomainRuleSet ruleSet = super.getRuleManager().getRuleSet(RULE_NAME);
        ruleSet.validate (super.getModel().getName());

        ruleSet = getRuleManager().getRuleSet(RULE_PRICE);
        ruleSet.validate(super.getModel().getPrice());

        ruleSet = getRuleManager().getRuleSet(RULE_QUANTITY);
        ruleSet.validate(super.getModel().getQuantity());
    }

    public String getName () {
        String name = super.getModel().getName();
        return name == null ? "" : name;
    }

    public CartItem setName (String name) {
        DomainRuleSet ruleSet = super.getRuleManager().getRuleSet(RULE_NAME);
        ruleSet.validate (name);

        if (ruleSet.isValid())
            super.getModel().setName(name);

        return this;
    }

    public int getQuantity () {
        return super.getModel().getQuantity();
    }

    public CartItem setQuantity (int quantity) {
        DomainRuleSet ruleSet = getRuleManager().getRuleSet(RULE_QUANTITY);
        ruleSet.validate(quantity);

        if (ruleSet.isValid())
            super.getModel().setQuantity(quantity);

        return this;
    }

    public double getPrice () {
        return super.getModel().getPrice();
    }

    public CartItem setPrice (double price) {
        DomainRuleSet ruleSet = super.getRuleManager().getRuleSet(RULE_PRICE);
        ruleSet.validate(price);

        if (ruleSet.isValid())
            super.getModel().setPrice(price);

        return this;
    }

    public String getThumbnailUrl () {
        return super.getModel().getThumbnailUrl();
    }

    public CartItem setThumbnailUrl (String url) {
        super.getModel().setThumbnailUrl(url);
        return this;
    }

    public ProductModel getProductModel () {
        return _productModel;
    }

    public CartItem setProductModel (ProductModel productModel) {
        _productModel = productModel;
        return this;
    }
}
