package com.afterapps.chronos.home;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afterapps.chronos.BaseActivity;
import com.afterapps.chronos.R;

import butterknife.ButterKnife;

public class HomeActivity
        extends BaseActivity<HomeView, HomePresenter>
        implements HomeView {

    @NonNull
    @Override
    public HomePresenter createPresenter() {
        return new HomePresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }
}
