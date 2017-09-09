package com.afterapps.chronos.location;

import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.afterapps.chronos.BaseLocationActivity;
import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Location;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.afterapps.chronos.Constants.VIEW_STATE_PROGRESS;
import static com.afterapps.chronos.Utilities.updateHomeScreenWidget;

public class LocationActivity
        extends BaseLocationActivity<LocationView, LocationPresenter>
        implements LocationView,
        MaterialDialog.SingleButtonCallback,
        EasyPermissions.PermissionCallbacks {

    private static final int RC_PLACES_AUTO_COMPLETE_OVERLAY = 1;

    @BindView(R.id.places_toolbar)
    Toolbar mPlacesToolbar;
    @BindView(R.id.location_add)
    LinearLayout mLocationAdd;
    @BindView(R.id.locations_recycler)
    RecyclerView mLocationsRecycler;
    @BindView(R.id.location_progress)
    LinearLayout mLocationProgress;

    private Realm mRealm;

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
        setSupportActionBar(mPlacesToolbar);
        mLocationsRecycler.setNestedScrollingEnabled(false);
        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra(Constants.FROM_ON_BOARDING, false)) {
                showLocationMethodDialog();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRealm = Realm.getDefaultInstance();
        displayLocations();
        EventBus.getDefault().register(this);
    }

    private void displayLocations() {
        final OrderedRealmCollection<Location> locations = mRealm.where(Location.class).findAll();
        final LocationsAdapter adapter = new LocationsAdapter(locations);
        mLocationsRecycler.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRealm.close();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.location_add)
    public void onViewClicked() {
        showLocationMethodDialog();
    }

    private void showLocationMethodDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_location_method)
                .content(R.string.dialog_content_location_method)
                .positiveText(R.string.dialog_positive_location_method)
                .negativeText(R.string.dialog_negative_location_method)
                .stackingBehavior(StackingBehavior.ALWAYS)
                .onPositive(this)
                .onNegative(this)
                .show();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        switch (which) {
            case POSITIVE:
                askedForLocationPermission = false;
                connectLocationClient();
                break;
            case NEGATIVE:
                startAutoCompleteOverlay();
                break;
        }
    }

    private void startAutoCompleteOverlay() {
        try {
            final AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .build();

            final Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(autocompleteFilter)
                    .build(LocationActivity.this);

            startActivityForResult(intent, RC_PLACES_AUTO_COMPLETE_OVERLAY);

        } catch (Exception e) {
            Snackbar.make(mLocationsRecycler, R.string.error_play_services, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_play_services, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent =
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PLAY_SERVICES_LINK));
                            startActivity(browserIntent);
                        }
                    }).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PLACES_AUTO_COMPLETE_OVERLAY && resultCode == RESULT_OK) {
            final Place place = PlaceAutocomplete.getPlace(this, data);
            final android.location.Location location = new android.location.Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(place.getLatLng().latitude);
            location.setLongitude(place.getLatLng().longitude);
            onLocationChanged(location);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onChangeSettingsDenied() {
        showLocationMethodDialog();
    }

    @Override
    public void onLocationChanged(android.location.Location geoLocation) {
        presenter.onLocationDetected(geoLocation);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationSelected(LocationsAdapter.LocationClickEvent event) {
        presenter.onLocationSelected(event.getTimezoneId());
    }

    @Override
    public void onLocationHandled() {
        updateHomeScreenWidget(this);
        finish();
    }

    @Override
    protected void showLocationDetectionError() {
        Snackbar.make(mLocationsRecycler, R.string.error_location_detection, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_location_manual, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startAutoCompleteOverlay();
                    }
                }).show();
    }

    @Override
    public void onReverseGeolocationError() {
        Snackbar.make(mLocationsRecycler, R.string.location_error_reverse_geolocation, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_time_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                    }
                }).show();
    }

    @Override
    public void showConnectionError() {
        Snackbar.make(mLocationsRecycler, R.string.location_error_connection, Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        checkLocationPermissionAndRequestLocation();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        showLocationMethodDialog();
    }

    @Override
    protected void displayViewState() {
        mLocationProgress.setVisibility(viewState == VIEW_STATE_PROGRESS ? VISIBLE : GONE);
    }
}
