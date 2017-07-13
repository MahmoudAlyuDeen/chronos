package com.afterapps.chronos;

/*
 * Created by mahmoud on 1/10/17.
 */

import android.os.Bundle;

import com.hannesdorfmann.mosby3.mvp.MvpFragment;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;


@SuppressWarnings("unused")
public abstract class BaseFragment<V extends MvpView, P extends MvpPresenter<V>> extends MvpFragment<V, P> {

    protected boolean arabic;
    protected boolean landscape;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            if (getActivity() != null) {
//                attachToolbar();
//            }
//        }
//    }

    protected abstract void attachToolbar();
}
