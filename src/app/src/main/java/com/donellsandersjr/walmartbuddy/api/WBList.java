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

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class WBList<T extends Parcelable & WBEquatable> extends WBParcelable implements List<T> {

    private final String STATE_LIST = "WBList.STATE_LIST";
    private final String STATE_DELETED_LIST = "WBList.STATE_DELETED_LIST";
    private final String STATE_ADDED_LIST = "WBList.STATE_ADDED_LIST";

    private ArrayList<T> _list;
    private ArrayList<T> _deletedElements = new ArrayList<T>();
    private ArrayList<T> _addedElements = new ArrayList<T>();

    public WBList () {
        _list = new ArrayList<>();
    }
    public WBList (int capacity) {
        _list = new ArrayList<>(capacity);
    }
    public WBList (List<T> items) {
        _list = new ArrayList<T>(items);
        if (items instanceof WBList) {
            _deletedElements.addAll(((WBList) items).getDeletedList());
            _addedElements.addAll(((WBList) items).getAddedList());
        }
    }

    @Override
    protected void onSaveState(Bundle state) {
        super.onSaveState(state);
        state.putParcelableArrayList(STATE_LIST, _list);
        state.putParcelableArrayList(STATE_ADDED_LIST, _addedElements);
        state.putParcelableArrayList(STATE_DELETED_LIST, _deletedElements);
    }

    @Override
    protected void onRestoreState(Bundle state) {
        super.onRestoreState(state);
        _list = state.getParcelableArrayList(STATE_LIST);
        _addedElements = state.getParcelableArrayList(STATE_ADDED_LIST);
        _deletedElements = state.getParcelableArrayList(STATE_DELETED_LIST);
    }

    @Override
    public boolean add(T object) {
        if (object == null)
            return false;

        int ndx = this.indexOf(object);
        if (ndx == -1) {
            _list.add(object);
            _addedElements.add(object);

            ndx = WBList.indexOf(_deletedElements, object);
            if (ndx != -1) _deletedElements.remove(ndx);
        } else {
            this.set(ndx, object);
        }

        return true;
    }

    @Override
    public void add(int index, T object) {
        if (object == null)
            throw new IllegalArgumentException("object parameter can not be null");

        int ndx = WBList.indexOf(_list, object);
        //
        // check to see if the object already existed in the list in
        // case we are just moving the object around in the list
        //
        if (ndx != -1) {
            _list.remove(ndx);
            _list.add(index, object);
        } else {
            _list.add(index, object);
            ndx = WBList.indexOf(_addedElements, object);
            if (ndx == -1) _addedElements.add(object);

            ndx = WBList.indexOf(_deletedElements, object);
            if (ndx != -1) _deletedElements.remove(ndx);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        for (T item : collection)
            this.add(item);
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        for (T item : collection)
            this.add(index++, item);
        return true;
    }

    @Override
    public boolean remove(Object object) {
        if (!(object instanceof WBEquatable))
            return false;

        T otherEl = (T)object;
        int len = _list.size();
        for (int i =0; i < len; i++) {
            WBEquatable<T> el = _list.get(i);
            if (el.equalsTo(otherEl)) {
                _list.remove(i);

                int ndx = WBList.indexOf(_addedElements, otherEl);
                if (ndx != -1) _addedElements.remove(ndx);

                ndx = WBList.indexOf(_deletedElements, otherEl);
                if (ndx == -1) _deletedElements.add(otherEl);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object object) {
        if (!(object instanceof WBEquatable))
            return false;

        for (WBEquatable<T> el : _list) {
            if (el.equalsTo((T)object))
                return true;
        }
        return false;
    }

    @Override
    public int indexOf(Object object) {
        if (!(object instanceof WBEquatable))
            return -1;

        return WBList.indexOf(_list, (T)object);
    }

    @Override
    public int lastIndexOf(Object object) {
        if (!(object instanceof WBEquatable))
            return -1;

        return WBList.lastIndexOf(_list, (T) object);
    }

    @Override
    public T set(int index, T object) {
        if (!(object instanceof WBEquatable))
            return null;

        T el = _list.set(index, object);

        int ndx = WBList.indexOf(_addedElements, object);
        if (ndx == -1)
            _addedElements.add(object);
        else
            _addedElements.set(ndx, object);

        ndx = WBList.indexOf(_deletedElements, object);
        if (ndx != -1) _deletedElements.remove(object);

        if (!object.equalsTo(el)) {
            ndx = WBList.indexOf(_addedElements, el);
            if (ndx != -1) _addedElements.remove(el);

            ndx = WBList.indexOf(_deletedElements, el);
            if (ndx == -1) _deletedElements.add(el);
        }

        return el;
    }

    @Override
    public void clear() {
        _deletedElements.clear();
        _addedElements.clear();
        _list.clear();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return _list.containsAll(collection);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof WBList)) {
            return false;
        }

        WBList el = (WBList)object;
        return _list.equals(el._list);
    }

    @Override
    public T get(int location) {
        return _list.get(location);
    }

    @Override
    public int hashCode() {
        return _list.hashCode() +
                _deletedElements.hashCode() +
                _addedElements.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return _list.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return _list.iterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return _list.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        return _list.listIterator(location);
    }

    @Override
    public T remove(int location) {
        T el = _list.remove(location);
        if (el != null) {
            int ndx = WBList.indexOf(_addedElements, el);
            if (ndx != -1) _addedElements.remove(ndx);

            ndx = WBList.indexOf(_deletedElements, el);
            if (ndx == -1) _deletedElements.add(el);
        }
        return el;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            T el = (T) it.next();
            int ndx = WBList.indexOf(_list, el);
            if (ndx != -1) {
                _list.remove(ndx);

                ndx = WBList.indexOf(_addedElements, el);
                if (ndx != -1) _addedElements.remove(ndx);

                ndx = WBList.indexOf(_deletedElements, el);
                if (ndx == -1) _deletedElements.add(el);
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        Iterator<T> it = this.iterator();
        while (it.hasNext()) {
            T el = it.next();
            if (!collection.contains(el))
                this.remove(el);
        }
        return true;
    }

    @Override
    public int size() {
        return _list.size();
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
        return _list.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return _list.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(T1[] array) {
        return _list.toArray(array);
    }

    public boolean indexExist (int index) {
        return _list.size() > index && index > -1;
    }

    public WBList<T> getDeletedList () {
        return new WBList<>(_deletedElements);
    }

    public WBList<T> getAddedList () {
        return new WBList<>(_addedElements);
    }

    public boolean hasChanged () {
        return _deletedElements.size() > 0 || _addedElements.size() > 0;
    }

    public <W extends Parcelable & WBEquatable> WBList<W> castTo (WBCastable<T> enumerable) {
        WBList<W> results = new WBList<>();
        for (T item : _list) {
            W el = enumerable.cast(item);
            results._list.add(el);
        }

        for (T item : _addedElements) {
            W el = enumerable.cast(item);
            results._addedElements.add(el);
        }

        for (T item : _deletedElements) {
            W el = enumerable.cast(item);
            results._deletedElements.add(el);
        }
        return results;
    }

    private static <T extends WBEquatable> int indexOf (List<T> list, T object) {
        int len = list.size();
        for (int i =0; i < len; i++) {
            WBEquatable<T> el = list.get(i);
            if (el.equalsTo(object))
                return i;
        }
        return -1;
    }

    private static <T extends WBEquatable> int lastIndexOf(List<T> list, T object) {
        for (int len = list.size() - 1 ; 0 < len; len--) {
            WBEquatable<T> el = list.get(len);
            if (el.equalsTo(object)) {
                return len;
            }
        }
        return -1;
    }
}
