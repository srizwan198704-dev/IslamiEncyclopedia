package com.srizwan.islamipedia;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.location.Priority;

public class ChangeLocationActivity extends AppCompatActivity {

    private Spinner divisionSpinner;
    private Spinner districtSpinner;
    private LinearLayout buttonChangeLocation;
    private List<DivisionClass> divisions;
    private List<DistrictClass> districts;

    private FusedLocationProviderClient fusedLocationClient;
    private MaterialButton buttonUseGPS;      // GPS দিয়ে লোকেশন
    private MaterialButton buttonUseNetwork;  // Network দিয়ে লোকেশন
    private ProgressDialog progressDialog;    // Progress dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_location);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        divisionSpinner = findViewById(R.id.divisionSpinner);
        districtSpinner = findViewById(R.id.districtSpinner);
        buttonChangeLocation = findViewById(R.id.buttonChangeLocation);

        // GPS Button
        buttonUseGPS = new MaterialButton(this);
        buttonUseGPS.setText("জিপিএস দিয়ে অবস্থান নির্ণয় করুন");
        buttonUseGPS.setAllCaps(false);
        ((LinearLayout) buttonChangeLocation.getParent()).addView(buttonUseGPS);

        // Network Button
        buttonUseNetwork = new MaterialButton(this);
        buttonUseNetwork.setText("নেটওয়ার্ক দিয়ে অবস্থান নির্ণয় করুন");
        buttonUseNetwork.setAllCaps(false);
        ((LinearLayout) buttonChangeLocation.getParent()).addView(buttonUseNetwork);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load division data
        String divisionJson = loadJSONFromAsset("division.json");
        if (divisionJson != null) {
            divisions = DivisionClass.parseDivisionJson(divisionJson);
            if (divisions.isEmpty()) {
                Toast.makeText(this, "No divisions found in the JSON data", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Error loading division data from JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<DivisionClass> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadDistrictsForDivision(divisions.get(position).getId());

                SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);
                String selectedDistrict = sharedPreferences.getString("selectedDistrict", null);

                int selectedIndex = getIndexFromText(districtSpinner, selectedDistrict);
                if (selectedIndex != -1) {
                    districtSpinner.setSelection(selectedIndex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        buttonChangeLocation.setOnClickListener(v -> {
            savePreferences();
            finish();
        });

        // GPS Button click
        buttonUseGPS.setOnClickListener(v -> {
            showProgressDialog();
            getCurrentLocationAndSave(Priority.PRIORITY_HIGH_ACCURACY);
        });

        // Network Button click
        buttonUseNetwork.setOnClickListener(v -> {
            showProgressDialog();
            getCurrentLocationAndSave(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
        });

        loadPreferences();
    }

    @Override
    protected void onResume() {
        loadPreferences();
        super.onResume();
    }

    private void loadDistrictsForDivision(String divisionId) {
        String districtJson = loadJSONFromAsset("district.json");
        if (districtJson != null) {
            districts = DistrictClass.parseDistrictJson(districtJson, divisionId);
            if (districts.isEmpty()) {
                Toast.makeText(this, "No districts found for the selected division", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayAdapter<DistrictClass> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            districtSpinner.setAdapter(districtAdapter);
        } else {
            Toast.makeText(this, "Error loading district data from JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private String loadJSONFromAsset(String fileName) {
        String json;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("ChangeLocationActivity", "Error loading JSON from asset: " + fileName);
            return null;
        }
        return json;
    }

    private void savePreferences() {
        String selectedDivision = divisionSpinner.getSelectedItem().toString();
        String selectedDistrict = districtSpinner.getSelectedItem().toString();

        int districtPosition = getIndexFromText(districtSpinner, selectedDistrict);

        String selectedLatitude = districts.get(districtPosition).getLatitude();
        String selectedLongitude = districts.get(districtPosition).getLongitude();

        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedDivision", selectedDivision);
        editor.putString("selectedDistrict", selectedDistrict);
        editor.putString("selectedLatitude", selectedLatitude);
        editor.putString("selectedLongitude", selectedLongitude);
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);

        String selectedDivision = sharedPreferences.getString("selectedDivision", null);
        String selectedDistrict = sharedPreferences.getString("selectedDistrict", null);

        int divisionPosition = getIndexFromText(divisionSpinner, selectedDivision);
        int districtPosition = getIndexFromText(districtSpinner, selectedDistrict);

        if (divisionPosition != -1) {
            divisionSpinner.setSelection(divisionPosition);
        }

        if (districtPosition != -1) {
            districtSpinner.setSelection(districtPosition);
        }
    }

    private int getIndexFromText(Spinner spinner, String text) {
        if (text == null) return -1;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(text)) {
                return i;
            }
        }
        return -1;
    }

    // লোকেশন নেওয়া
    private void getCurrentLocationAndSave(int priorityType) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            dismissProgressDialog();
            return;
        }

        fusedLocationClient.getCurrentLocation(priorityType, null)
                .addOnSuccessListener(location -> {
                    dismissProgressDialog();
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        String cityName = "Unknown City";
                        try {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);

                                if (address.getLocality() != null) {
                                    cityName = address.getLocality();
                                } else if (address.getSubAdminArea() != null) {
                                    cityName = address.getSubAdminArea();
                                } else if (address.getAdminArea() != null) {
                                    cityName = address.getAdminArea();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("selectedDivision", "Foreign");
                        editor.putString("selectedDistrict", cityName);
                        editor.putString("selectedLatitude", String.valueOf(latitude));
                        editor.putString("selectedLongitude", String.valueOf(longitude));
                        editor.apply();

                        Toast.makeText(this, "লোকেশন সেভ হয়েছে: " + cityName, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "লোকেশন পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    dismissProgressDialog();
                    Toast.makeText(this, "লোকেশন আনতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
                });
    }

    // Progress Dialog দেখানো
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("অবস্থান অনুসন্ধান করা হচ্ছে...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
