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

import android.content.ContentValues;
import android.os.Bundle;

import java.util.Date;

public class WBObject  extends WBParcelable implements WBEquatable<WBObject> {
    private ContentValues _fieldValues;
    private final String FIELDVALUES = "fieldValues";

    public WBObject () {
        _fieldValues = new ContentValues();
    }
    public WBObject (ContentValues values) {
        _fieldValues = new ContentValues(values);
    }

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelable(FIELDVALUES, _fieldValues);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        _fieldValues = state.getParcelable(FIELDVALUES);
    }

    @Override
    public boolean equalsTo(WBObject other) {
        if (other == null) return false;
        return _fieldValues.equals(other._fieldValues);
    }

    @Override
    public int hashCode() {
        return _fieldValues.hashCode();
    }

    public final ContentValues getValues () {
        return new ContentValues(_fieldValues);
    }

    public final Object getValue (String fieldName)  {
        return _fieldValues.get(fieldName);
    }

    public String getStringValue (String fieldName) {
        return getStringValue(fieldName, null);
    }

    public String getStringValue (String fieldName, String defaultVal) {
        String val = _fieldValues.getAsString(fieldName);
        if (val == null) {
            setStringValue(fieldName, defaultVal);
            return defaultVal;
        }
        return val;
    }

    public void setStringValue (String fieldName, String value) {
        _fieldValues.put(fieldName, value);
    }

    public long getLongValue (String fieldName) {
        return getLongValue(fieldName, 0);
    }

    public long getLongValue (String fieldName, long defaultVal) {
        Long val = _fieldValues.getAsLong(fieldName);
        if (val == null) {
            setLongValue(fieldName, defaultVal);
            return defaultVal;
        }
        return val;
    }

    public void setLongValue (String fieldName, long value) {
        _fieldValues.put(fieldName, value);
    }

    public int getIntValue (String fieldName) {
        return getIntValue(fieldName, 0);
    }

    public int getIntValue (String fieldName, int defaultVal) {
        Integer val = _fieldValues.getAsInteger(fieldName);
        if (val == null) {
            setIntValue(fieldName, defaultVal);
            return defaultVal;
        }
        return val;
    }

    public void setIntValue (String fieldName, int value) {
        _fieldValues.put(fieldName, value);
    }

    public double getDoubleValue (String fieldName) {
        return getDoubleValue(fieldName, 0);
    }

    public double getDoubleValue (String fieldName, double defaultVal) {
        Double val = _fieldValues.getAsDouble(fieldName) ;
        if (val == null) {
            setDoubleValue(fieldName, defaultVal);
            return defaultVal;
        }
        return val;
    }

    public void setDoubleValue (String fieldName, double value) {
        _fieldValues.put(fieldName, value);
    }

    public float getFloatValue (String fieldName) {
        return getFloatValue(fieldName, 0);
    }

    public float getFloatValue (String fieldName, float defaultVal) {
        Float val = _fieldValues.getAsFloat(fieldName);
        if (val == null) {
            setFloatValue(fieldName, defaultVal);
            return defaultVal;
        }
        return val;
    }

    public void setFloatValue (String fieldName, float value) {
        _fieldValues.put(fieldName, value);
    }

    public boolean getBooleanValue (String fieldName) {
        return getBooleanValue(fieldName, false);
    }

    public boolean getBooleanValue (String fieldName, boolean defaultVal) {
        Boolean val = _fieldValues.getAsBoolean(fieldName);
        if (val == null)
            return defaultVal;

        return val;
    }

    public void setBooleanValue (String fieldName, boolean value) {
        _fieldValues.put(fieldName, value);
    }

    public byte [] getBytes (String fieldName) {
        return getBytes(fieldName, null);
    }

    public byte [] getBytes (String fieldName, byte [] defaultVal) {
        byte [] val = _fieldValues.getAsByteArray(fieldName);
        if (val == null)
            return defaultVal;
        return val;
    }

    public void setBytesValue (String fieldName, byte [] value) {
        _fieldValues.put(fieldName, value);
    }

    public Date getDate (String fieldName) {
        Long val = _fieldValues.getAsLong(fieldName);
        if (val != null)
            return new Date(val.longValue());
        return null;
    }

    public Date getDate (String fieldName, Date defaultVal) {
        Long val = _fieldValues.getAsLong(fieldName);
        if (val == null)
            return defaultVal;
        return new Date(val.longValue());
    }

    public void setDate (String fieldName, Date value) {
        if (value != null)
            _fieldValues.put(fieldName, value.getTime());
        else {
            _fieldValues.put(fieldName, 0);
        }
    }

    public boolean hasKey(String key) {
        return _fieldValues.keySet().contains(key);
    }

    public void removeKey (String key) {
        _fieldValues.remove(key);
    }
}
