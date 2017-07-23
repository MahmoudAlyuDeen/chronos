package com.afterapps.chronos.places;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afterapps.chronos.BaseActivity;
import com.afterapps.chronos.R;

import butterknife.ButterKnife;

public class PlacesActivity
        extends BaseActivity<PlacesView, PlacesPresenter>
        implements PlacesView {

    @NonNull
    @Override
    public PlacesPresenter createPresenter() {
        return new PlacesPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        ButterKnife.bind(this);
    }
}
