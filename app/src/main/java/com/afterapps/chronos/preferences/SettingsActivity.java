package com.afterapps.chronos.preferences;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.afterapps.chronos.R;
import com.afterapps.chronos.widget.PrayersWidget;
import com.mikepenz.aboutlibraries.LibsBuilder;

/*
 * Created by Mahmoud on 10/6/2016.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
        setTheme(R.style.SettingsTheme);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateHomeScreenWidget();
    }

    protected void updateHomeScreenWidget() {
        Intent invalidateWidget = new Intent(this, PrayersWidget.class);
        invalidateWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(invalidateWidget);
    }

    private void setupToolbar() {
        ViewGroup rootView = (ViewGroup) findViewById(R.id.action_bar_root);

        if (rootView != null) {
            View view = getLayoutInflater().inflate(R.layout.settings_toolbar, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
            setSupportActionBar(toolbar);
            setTitle(R.string.action_settings);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            setupAcknowledgementButtons();
        }

        private void setupAcknowledgementButtons() {
            Preference libraries = findPreference(getString(R.string.preference_key_libraries));
            libraries.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new LibsBuilder().withActivityTheme(R.style.AppTheme)
                            .withAutoDetect(true)
                            .withFields(R.string.class.getFields())
                            .start(getActivity());
                    return true;
                }
            });

            Preference api = findPreference(getString(R.string.preference_key_api));
            api.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.about_api_url))));
                    return true;
                }
            });
        }
    }
}