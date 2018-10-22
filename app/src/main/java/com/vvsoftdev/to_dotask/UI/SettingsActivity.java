package com.vvsoftdev.to_dotask.UI;

import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import com.vvsoftdev.to_dotask.R;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupSharedPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = this.getSupportActionBar();

        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the VisualizerActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadColorFromPreferences(sharedPreferences);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    //Load the theme color picked in settings, by default red
    private void loadColorFromPreferences(SharedPreferences sharedPreferences) {
        if (sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value))
                .equals(getString(R.string.pref_color_red_value))) {
            this.setTheme(R.style.AppThemeRed);
        } else if (sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value))
                .equals(getString(R.string.pref_color_blue_value))) {
            this.setTheme(R.style.AppThemeBlue);
        } else if (sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value))
                .equals(getString(R.string.pref_color_green_value))) {
            this.setTheme(R.style.AppThemeGreen);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_color_key))) {
            loadColorFromPreferences(sharedPreferences);
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}
