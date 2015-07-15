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

package com.donellesandersjr.walmartbuddy.models;

import android.content.ContentValues;
import android.os.Bundle;

import com.donellesandersjr.walmartbuddy.api.WBEqualityComparer;
import com.donellesandersjr.walmartbuddy.api.WBEquatable;
import com.donellesandersjr.walmartbuddy.api.WBLogger;
import com.donellesandersjr.walmartbuddy.api.WBObject;
import com.donellesandersjr.walmartbuddy.api.WBSortComparer;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DataModel extends WBObject implements Comparable<DataModel> {
    private final String TAG = "com.donellesandersjr.walmartbuddy.model.WBDataModel";

    private final String KEY_UNIVERSAL_ID = "universal_id";
    private final String ORIGINALVALUES = "originalValues";
    private final String CHANGE_TIMESTAMPS = "changeTimestamps";
    private final String LOCK_STATUS = "lockStatus";
    private final String IS_CAST_MODEL = "isCastModel";
    private final String EQCOMPARE_HANDLER = "eqCompareHandler";
    private final String SRTCOMPARE_HANDLER = "sortCompareHandler";

    private WBEqualityComparer<DataModel> _eqComparer;
    private WBSortComparer<DataModel> _sortComparer;
    private ContentValues _originalValues = new ContentValues();
    private ContentValues _changeTimestamps = new ContentValues();
    private boolean _isLocked;

    public DataModel() {
        super();
        this.setUniversalId(UUID.randomUUID().toString().replace("-", "").trim());
    }

    public DataModel(ContentValues values) {
        super(values);
    }

    protected void onSaveChanges () {}
    protected void onMergeChanges (DataModel model) {}

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelable(ORIGINALVALUES, _originalValues);
        state.putParcelable(CHANGE_TIMESTAMPS, _changeTimestamps);
        state.putBoolean(LOCK_STATUS, _isLocked);

        if (_eqComparer != null)
            state.putString(EQCOMPARE_HANDLER, _eqComparer.getClass().getName());

        if (_sortComparer != null)
            state.putString(SRTCOMPARE_HANDLER, _sortComparer.getClass().getName());
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        _originalValues = state.getParcelable(ORIGINALVALUES);
        _changeTimestamps = state.getParcelable(CHANGE_TIMESTAMPS);
        _isLocked = state.getBoolean(LOCK_STATUS);

        String classname = state.getString(EQCOMPARE_HANDLER);
        if (!WBStringUtils.isNullOrEmpty(classname)) {
            try {
                Class classType = Class.forName(classname);
                _eqComparer = (WBEqualityComparer<DataModel>) classType.newInstance();
            } catch (Exception ex) {
                WBLogger.Error(TAG, ex);
            }
        }

        classname = state.getString(SRTCOMPARE_HANDLER);
        if (!WBStringUtils.isNullOrEmpty(classname)) {
            try {
                Class classType = Class.forName(classname);
                _sortComparer = (WBSortComparer<DataModel>) classType.newInstance();
            } catch (Exception ex) {
                WBLogger.Error(TAG, ex);
            }
        }
    }

    @Override
    public boolean equalsTo(WBObject other) {
        DataModel model = (DataModel) other;
        if (model == null)
            return false;

        if (_eqComparer != null)
            return _eqComparer.equals(this, model);

        return hashCode() == other.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return equalsTo((WBObject) o);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 17 + _originalValues.hashCode();
        hash = hash * 31 + _changeTimestamps.hashCode();
        hash = hash * 13 + (_isLocked ? 1 : 0);
        return hash;
    }

    @Override
    public int compareTo(DataModel another) {
        if (another == null)
            return 1;

        if (_sortComparer != null)
            return _sortComparer.compare(this, another);

        return 0;
    }

    @Override
    public void setIntValue(String fieldName, int value) {
        super.setIntValue(fieldName, value);
        _changeTimestamps.put(fieldName, new Date().getTime());
        if (!_isLocked)
            _originalValues.put(fieldName, value);
    }

    @Override
    public void setStringValue(String fieldName, String value) {
        super.setStringValue(fieldName, value);
        _changeTimestamps.put(fieldName, new Date().getTime());
        if (!_isLocked)
            _originalValues.put(fieldName, value);
    }

    @Override
    public void setLongValue(String fieldName, long value) {
        super.setLongValue(fieldName, value);
        _changeTimestamps.put(fieldName, new Date().getTime());
        if (!_isLocked)
            _originalValues.put(fieldName, value);
    }

    @Override
    public void setDoubleValue(String fieldName, double value) {
        super.setDoubleValue(fieldName, value);
        _changeTimestamps.put(fieldName, new Date().getTime());
        if (!_isLocked)
            _originalValues.put(fieldName, value);
    }

    @Override
    public void setBooleanValue(String fieldName, boolean value) {
        super.setBooleanValue(fieldName, value);
        _changeTimestamps.put(fieldName, new Date().getTime());
        if (!_isLocked)
            _originalValues.put(fieldName, value);
    }

    @Override
    public void setBytesValue(String fieldName, byte[] value) {
        super.setBytesValue(fieldName, value);
        _changeTimestamps.put(fieldName, new Date().getTime());
        if (!_isLocked)
            _originalValues.put(fieldName, value);
    }

    @Override
    public void setDate(String fieldName, Date value) {
        super.setDate(fieldName, value);
        _changeTimestamps.put(fieldName, new Date().getTime());
        if (value != null && !_isLocked)
            _originalValues.put(fieldName, value.getTime());
    }

    @Override
    public void removeKey(String key) {
        super.removeKey(key);
        if (_changeTimestamps.containsKey(key))
            _changeTimestamps.remove(key);
        if (!_isLocked && _originalValues.containsKey(key))
            _originalValues.remove(key);
    }

    public DataModel setSortComparer (WBSortComparer<DataModel> comparer) {
        _sortComparer = comparer;
        return this;
    }

    public DataModel setEqualityComparer (WBEqualityComparer<DataModel> comparer) {
        _eqComparer = comparer;
        return this;
    }

    public String getUniversalId () {
        return super.getStringValue(KEY_UNIVERSAL_ID);
    }

    private void setUniversalId (String universalId) {
        this.setStringValue(KEY_UNIVERSAL_ID, universalId);
    }

    public DataModel lockValues () {
        _isLocked = true;
        return this;
    }

    public boolean hasChanged (String fieldName) {
        if (!_originalValues.containsKey(fieldName)) {
            return false;
        } else if (_isLocked) {
            Object orgVal = _originalValues.get(fieldName),
                   currVal = super.getValue(fieldName);

            if (orgVal == null && currVal == null || orgVal == null)
                return false;

            else if (orgVal instanceof WBEquatable)
                return !((WBEquatable)orgVal).equalsTo(currVal);

            else
                return !orgVal.equals(currVal);
        } else {
            return true;
        }
    }


    public DataModel mergeChanges (DataModel model) {
        for (Map.Entry<String,Object> entry : model.getValues().valueSet()) {
            String fieldname = entry.getKey();
            if (this.hasKey(fieldname) &&  model.hasChanged(fieldname)) {
                long timestamp = model._changeTimestamps.getAsLong(fieldname);
                long myTimestamp = _changeTimestamps.getAsLong(fieldname);
                if (myTimestamp < timestamp) {
                    Class<?> classType = entry.getValue().getClass();
                    if (classType.isAssignableFrom(String.class)) {
                        this.setStringValue(fieldname, (String)entry.getValue());
                    } else if (classType.isAssignableFrom(Integer.class)) {
                        this.setIntValue(fieldname, (Integer)entry.getValue());
                    } else if (classType.isAssignableFrom(Long.class)) {
                        this.setLongValue(fieldname, (Long)entry.getValue());
                    } else if (classType.isAssignableFrom(Double.class)) {
                        this.setDoubleValue(fieldname, (Double)entry.getValue());
                    } else if (classType.isAssignableFrom(Boolean.class)) {
                        this.setBooleanValue(fieldname, (Boolean)entry.getValue());
                    } else if (classType.isAssignableFrom(byte[].class)) {
                        this.setBytesValue(fieldname, (byte [])entry.getValue());
                    } else {
                        WBLogger.Error(TAG, "Unable to merge changes for field : " + fieldname);
                    }
                }
            }
        }
        onMergeChanges(model);
        return this;
    }

    public DataModel saveChanges () {
        onSaveChanges();
        _originalValues = super.getValues();
        _changeTimestamps.clear();
        _isLocked = true;
        return this;
    }

    public final <T extends DataModel> T cast(Class<T> tClass) {
        try {
            T model = tClass.getDeclaredConstructor(ContentValues.class).newInstance(this.getValues());
            model.setBooleanValue(IS_CAST_MODEL, true);
            return model;
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        }
        return null;
    }

    public final boolean isCastModel () {
        return getBooleanValue(IS_CAST_MODEL, false);
    }
}
