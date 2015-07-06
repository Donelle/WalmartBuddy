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

package com.donellsandersjr.walmartbuddy.api;

/**
 * Defines a method that a type implements to compare two objects.
 * @param <T>
 */
public interface WBEqualityComparer<T extends WBObject> {
    /**
     * Determines whether the specified objects are equal.
     *
     * @param x The first object to compare.
     * @param y The second object to compare.
     * @return true if the specified objects are equal; otherwise, false.
     */
    boolean equals (T x, T y);
}