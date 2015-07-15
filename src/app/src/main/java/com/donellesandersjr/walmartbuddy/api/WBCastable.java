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

package com.donellesandersjr.walmartbuddy.api;

import android.os.Parcelable;

/**
 * Defines a type capable of casting its self safely from one type to another
 */
public interface WBCastable<T> {
    /**
     * Casts its self to the expected type W.
     *
     * @param item
     *           An object to cast from
     * @return a valid object of type W otherwise null.
     */
    <W extends Parcelable & WBEquatable> W cast (T item);
}