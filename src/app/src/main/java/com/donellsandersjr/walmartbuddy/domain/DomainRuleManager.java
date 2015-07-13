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
package com.donellsandersjr.walmartbuddy.domain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class DomainRuleManager implements Iterable<Map.Entry<Integer,DomainRuleSet>> {
    private HashMap<Integer, DomainRuleSet> _ruleSets = new HashMap<>();

    @Override
    protected void finalize() throws Throwable {
        _ruleSets.clear();
        super.finalize();
    }

    @Override
    public Iterator<Map.Entry<Integer, DomainRuleSet>> iterator() {
        return _ruleSets.entrySet().iterator();
    }

    public DomainRuleManager addRuleSet(Integer key, DomainRuleSet ruleSet) {
        _ruleSets.put(key, ruleSet);
        return this;
    }

    public DomainRuleSet getRuleSet (Integer key) {
        return _ruleSets.get(key);
    }
}
