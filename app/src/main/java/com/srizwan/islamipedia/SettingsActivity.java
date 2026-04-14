package com.srizwan.islamipedia;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private Spinner calculationMethodSpinner, madhabSpinner;
    private Switch switchWaqtNotification;
    private TextView textChangeLanguage;
    private MaterialToolbar toolbar;

    private Switch themeSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the theme based on the user's preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                onBackPressed();
            }
        });

        calculationMethodSpinner = findViewById(R.id.calculationMethodSpinner);
        madhabSpinner = findViewById(R.id.madhabSpinner);
        switchWaqtNotification = findViewById(R.id.switchWaqtNotification);

        loadSpinnerData();
        loadPreferences();

        // Theme Switch
        themeSwitch = findViewById(R.id.themeSwitch);
        themeSwitch.setChecked(isDarkModeEnabled);
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setDarkModeEnabled(isChecked);
            }
        });
    }
    private void setDarkModeEnabled(boolean darkModeEnabled) {
        sharedPreferences.edit().putBoolean("isDarkModeEnabled", darkModeEnabled).apply();
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate();
    }

    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
    private void loadSpinnerData() {
        ArrayAdapter<String> madhabAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        madhabAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> calculationMethodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        calculationMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        madhabSpinner.setAdapter(madhabAdapter);
        calculationMethodSpinner.setAdapter(calculationMethodAdapter);

        // Clear the adapters if needed (optional)
        madhabAdapter.clear();
        calculationMethodAdapter.clear();

        // Add items to the madhabSpinner
        madhabAdapter.add("হানাফি");
        madhabAdapter.add("শাফেয়ী, হাম্বলী, মালিকী");

        // Add items to the calculationMethodSpinner
        calculationMethodAdapter.add("ইউনিভার্সিটি অফ ইসলামিক স্টাডিস, করাচী");
        calculationMethodAdapter.add("ইসলামিক সোসাইটি অফ নর্থ আমেরিকা");
        calculationMethodAdapter.add("মুসলিম ওয়ার্ল্ড লীগ");
        calculationMethodAdapter.add("উম্মুল কুরা ইউনিভার্সিটি, মক্কা");
        calculationMethodAdapter.add("ইজিপ্সিয়ান জেনারেল অথরিটি অফ সার্ভে");
        calculationMethodAdapter.add("ইন্সটিটিউট অফ জিওফিজিক্স, তেহরান ইউনিভার্সিটি");

        // Notify the adapters about the data changes
        madhabAdapter.notifyDataSetChanged();
        calculationMethodAdapter.notifyDataSetChanged();
    }

    private void savePreferences() {
        String selectedMadhab = madhabSpinner.getSelectedItem().toString();
        String selectedCalcMethod = calculationMethodSpinner.getSelectedItem().toString();
        boolean ischeckWaqtNotification = switchWaqtNotification.isChecked();

        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedMadhab", selectedMadhab);
        editor.putString("selectedCalcMethod", selectedCalcMethod);
        editor.putString("ischeckWaqtNotification", String.valueOf(ischeckWaqtNotification));
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);

        String savedMadhab = sharedPreferences.getString("selectedMadhab", null);
        String savedCalcMethod = sharedPreferences.getString("selectedCalcMethod", null);
        String ischeckWaqtNotificationStr = sharedPreferences.getString("ischeckWaqtNotification", null);

        // Get the positions of the saved selections in the spinners' data
        int madhabPosition = getIndexFromText(madhabSpinner, savedMadhab);
        int calcMethodPosition = getIndexFromText(calculationMethodSpinner, savedCalcMethod);

        // Set the selected items in the spinners
        if (madhabPosition != -1) {
            madhabSpinner.setSelection(madhabPosition);
        }

        if (calcMethodPosition != -1) {
            calculationMethodSpinner.setSelection(calcMethodPosition);
        }

        boolean ischeckWaqtNotification = Boolean.parseBoolean(ischeckWaqtNotificationStr);
        switchWaqtNotification.setChecked(ischeckWaqtNotification);

    }

    private int getIndexFromText(Spinner spinner, String text) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(text)) {
                return i;
            }
        }
        return -1; // Return -1 if not found
    }

    @Override
    public void onBackPressed() {
        savePreferences();
        super.onBackPressed();
    }
    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}
