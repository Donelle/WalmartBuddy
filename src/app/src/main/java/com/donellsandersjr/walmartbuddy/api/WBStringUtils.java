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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WBStringUtils {

    public static String toString (String [] arrayOfStrings) {
        return Arrays.toString(arrayOfStrings).replaceAll("(\\[+)|(\\]+)", "");
    }

    public static String toString  (List<String> listOfStrings) {
        String [] arrayOfStrings = new String[listOfStrings.size()];
        for (int i = 0; i < arrayOfStrings.length; i++)
            arrayOfStrings[i] = listOfStrings.get(i);

        return toString(arrayOfStrings);
    }

    public static boolean isNullOrEmpty (String val) {
        return val == null || val.isEmpty();
    }

    public static ArrayList<String> splitString (String val, int partLength) {
        ArrayList<String> parts = new ArrayList<String>();
        while (val.length() > 0) {
            if (val.length() > partLength) {
                String temp = val.substring(0, partLength);
                parts.add(temp);
                val = val.substring(temp.length());
            } else {
                parts.add(val);
                val = "";
            }
        }
        return parts;
    }

    public static boolean areEqual (String val, String valOther) {
        return (val == null ? valOther == null : val.contentEquals(valOther));
    }
}
