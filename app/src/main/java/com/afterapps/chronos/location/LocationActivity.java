package com.afterapps.chronos.location;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afterapps.chronos.BaseLocationActivity;
import com.afterapps.chronos.R;

import butterknife.ButterKnife;

public class LocationActivity
        extends BaseLocationActivity<LocationView, LocationPresenter>
        implements LocationView {

    @NonNull
    @Override
    public LocationPresenter createPresenter() {
        return new LocationPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
