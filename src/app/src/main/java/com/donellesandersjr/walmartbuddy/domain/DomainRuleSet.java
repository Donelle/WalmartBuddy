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

import java.util.ArrayList;
import java.util.Observable;

class DomainRuleSet extends Observable {
    ArrayList<DomainRule> _rules;
    boolean _isValid;

    public DomainRuleSet () {
        _rules = new ArrayList<>();
    }

    public boolean isValid () { return _isValid; }

    public DomainRuleSet addRule (DomainRule rule) {
        if (rule != null)
            _rules.add(rule);
        return this;
    }

    public DomainRuleSet clearRules () {
        _rules.clear();
        return this;
    }

    public <T> void validate(T data) {
        super.setChanged();

        for (DomainRule rule : _rules) {
            data = rule.validate(data);
            if (rule.isBroken()) {
                _isValid = false;

                DomainRuleResult result =
                        new DomainRuleResult(rule.getMessage(), true, 0);

                super.notifyObservers(result);
                super.clearChanged();
                return;
            }
        }

        _isValid = true;
        super.notifyObservers(null);
        super.clearChanged();
    }

    public static class NullOrEmptyRule extends DomainRule {

        public NullOrEmptyRule (int resourceId) {
            super(resourceId);
        }

        @Override
        public <T> T validate(T data) {
            setBroken(false);
            String value = (String)data;

            if (value == null) {
                setBroken(true);
            } else {
                value = value.trim();
                if (value.length() == 0)
                    setBroken(true);
            }

            return (T)value;
        }
    }

    public static class NullRule extends DomainRule {

        public NullRule (int resourceId) {
            super (resourceId);
        }

        @Override
        public <T> T validate(T data) {
            setBroken(data == null);
            return data;
        }
    }

    public static class ExpressionRule extends DomainRule {
        DomainRulePredicate _predicate;
        public ExpressionRule(int resourceId, DomainRulePredicate predicate) {
            super(resourceId);
            _predicate = predicate;
        }

        @Override
        public <T> T validate(T data) {
            setBroken(!_predicate.execute(data));
            return data;
        }
    }

    public static class GreaterThanZeroRule extends DomainRule {

        public GreaterThanZeroRule (int resourceId) {
            super (resourceId);
        }

        @Override
        public <T> T validate(T data) {
            Integer val = (Integer) data;
            setBroken(val <= 0);
            return data;
        }
    }


}
