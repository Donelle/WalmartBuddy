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

import com.donellesandersjr.walmartbuddy.api.WBEquatable;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBParcelable;
import com.donellesandersjr.walmartbuddy.db.DbProvider;
import com.donellesandersjr.walmartbuddy.models.DataModel;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

abstract class DomainObject<TModel extends DataModel> extends WBParcelable implements  WBEquatable<DomainObject> {
    public static final String TAG = "com.donellesandersjr.walmartbuddy.domain.DomainObject";

    private final String KEY_MODEL = "DomainObject.KEY_MODEL";
    private TModel _model;
    private DomainRuleManager _ruleManager = new DomainRuleManager();

    protected DomainObject() {
        Class<TModel> classType = (Class<TModel>)
                ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        try {
            _model = classType.newInstance();
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        }
        onInit();
    }

    protected DomainObject(TModel existingObject) {
        _model = existingObject;
        onInit();
    }

    @Override
    protected void onSaveState(Bundle state) {
        state.putParcelable(KEY_MODEL, _model);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        _model = state.getParcelable(KEY_MODEL);
        this.onInit();
    }

    @Override
    public boolean equalsTo(DomainObject other) {
        return _model.equalsTo(other.getModel());
    }

    protected DomainRuleManager getRuleManager () { return _ruleManager; }

    protected void setModel (TModel model) {
        _model = model;
    }

    public TModel getModel () {
        return _model;
    }

    public void subscribeToRule (Integer rule, DomainRuleObserver observer) {
        DomainRuleSet ruleSet = _ruleManager.getRuleSet(rule);
        ruleSet.addObserver(observer);
    }

    public void unsubscribeFromRule (Integer rule, DomainRuleObserver observer) {
        DomainRuleSet ruleSet = _ruleManager.getRuleSet(rule);
        ruleSet.deleteObserver(observer);
    }

    public final boolean isValid() {
        boolean isValid = onValidate();
        if (isValid) {
            for (Map.Entry<Integer, DomainRuleSet> entry : _ruleManager) {
                if (!entry.getValue().isValid())
                    return false;
            }
        }
        return isValid;
    }

    public final void save () throws Exception {
        if (!isValid())
            throw new IllegalStateException();
        //
        // Perform any last minute changes
        //
        onSave();
        //
        // Perform the actual save to the db
        //
        DbProvider.save(_model);
        //
        // Lock the changes
        //
        _model.saveChanges();
    }

    public final void delete () throws Exception {
        //
        // Perform any last minute changes
        //
        onDelete();
        //
        // Perform the actual delete to the db
        //
        DbProvider.delete(_model);
    }

    public Map<Integer, String> getBrokenRules () {
        Map<Integer, String> brokenRules = new HashMap<>();
        for (Map.Entry<Integer, DomainRuleSet> entry : _ruleManager) {
            DomainRuleSet ruleSet = entry.getValue();
            if (!ruleSet.isValid()) {
                for (DomainRule rule : ruleSet._rules) {
                    if (rule.isBroken()) {
                        brokenRules.put(entry.getKey(), rule.getMessage());
                        break;
                    }
                }
            }
        }
        return brokenRules;
    }


    protected void onSave () throws Exception { /* NO OP */ }
    protected void onDelete () throws Exception { /* NO OP */ }
    protected void onInit () { /* NO OP */ }
    protected boolean onValidate () { return true; }
}
