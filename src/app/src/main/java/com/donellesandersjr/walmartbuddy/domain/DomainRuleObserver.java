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

import java.util.Observable;
import java.util.Observer;

public abstract class DomainRuleObserver implements Observer {
    public abstract void onRuleChanged (boolean isBroken, String message);

    @Override
    public void update(Observable observable, Object data) {
        DomainRuleResult result = (DomainRuleResult)data;
        if (result != null) {
            onRuleChanged(result.isBroken(), result.getMessage());
        } else {
            onRuleChanged(false, "");
        }
    }
}
