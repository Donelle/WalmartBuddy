package com.donellesandersjr.walmartbuddy.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.donellesandersjr.walmartbuddy.domain.DomainObject;

public abstract class BaseFragment<T extends DomainObject> extends Fragment {
    private final String STATE_OBJECT = "BaseFragment.FRAGMENT_STATE";

    private T _object;
    protected void setDomainObject (T object) {
        _object = object;
    }

    public T getDomainObject ()  {
        return _object;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            _object = savedInstanceState.getParcelable(STATE_OBJECT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_OBJECT, _object);
    }
}
