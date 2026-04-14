package com.srizwan.islamipedia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.HighLatitudeRule;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SehriIftarActivity extends AppCompatActivity {

    private TextView textCurrentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sehri_iftar);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        View customLocationView = LayoutInflater.from(this).inflate(R.layout.layout_change_location, null);

        toolbar.addView(customLocationView);
        textCurrentLocation = customLocationView.findViewById(R.id.textCurrentLocation);

        customLocationView.setOnClickListener(v -> {
            Intent intent = new Intent(SehriIftarActivity.this, ChangeLocationActivity.class);
            startActivity(intent);
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        LoadSehriIftar();


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                    finish();
            }
        });
    }

    private void LoadSehriIftar() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);

                String savedCalcMethod = sharedPreferences.getString("selectedCalcMethod", null);
                String savedMadhab = sharedPreferences.getString("selectedMadhab", null);
                String selectedLatitude = sharedPreferences.getString("selectedLatitude", null);
                String selectedLongitude = sharedPreferences.getString("selectedLongitude", null);

                double latitude = 23.7104; // default Dhaka
                double longitude = 90.40744; // default Dhaka

                try {
                    if (selectedLatitude != null && selectedLongitude != null) {
                        latitude = Double.parseDouble(selectedLatitude);
                        longitude = Double.parseDouble(selectedLongitude);
                    }
                } catch (NumberFormatException e) {
                }

                final Coordinates coordinates = new Coordinates(latitude, longitude);

                CalculationParameters calculationMethod = null;

                if (savedCalcMethod == null || savedCalcMethod.isEmpty()) {
                    calculationMethod = CalculationMethod.KARACHI.getParameters();
                } else {
                    if (savedCalcMethod.equals("ইউনিভার্সিটি অফ ইসলামিক স্টাডিস, করাচী")) {
                        calculationMethod = CalculationMethod.KARACHI.getParameters();
                    } else if (savedCalcMethod.equals("ইসলামিক সোসাইটি অফ নর্থ আমেরিকা")) {
                        calculationMethod = CalculationMethod.NORTH_AMERICA.getParameters();
                    } else if (savedCalcMethod.equals("মুসলিম ওয়ার্ল্ড লীগ")) {
                        calculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters();
                    } else if (savedCalcMethod.equals("উম্মুল কুরা ইউনিভার্সিটি, মক্কা")) {
                        calculationMethod = CalculationMethod.UMM_AL_QURA.getParameters();
                    } else if (savedCalcMethod.equals("ইজিপ্সিয়ান জেনারেল অথরিটি অফ সার্ভে")) {
                        calculationMethod = CalculationMethod.EGYPTIAN.getParameters();
                    } else if (savedCalcMethod.equals("ইন্সটিটিউট অফ জিওফিজিক্স, তেহরান ইউনিভার্সিটি")) {
                        calculationMethod = CalculationMethod.QATAR.getParameters();
                    }
                }
                if (savedMadhab == null || savedMadhab.isEmpty()) {
                    calculationMethod.madhab = Madhab.HANAFI;
                } else {
                    if (savedMadhab.equals("হানাফি")) {
                        calculationMethod.madhab = Madhab.HANAFI;
                    } else if (savedMadhab.equals("শাফেয়ী, হাম্বলী, মালিকী")) {
                        calculationMethod.madhab = Madhab.SHAFI;
                    }
                }
                calculationMethod.highLatitudeRule = HighLatitudeRule.TWILIGHT_ANGLE;

                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", new Locale("bn"));
                formatter.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));

                CalculationParameters calculationParams = calculationMethod;

                Calendar calendar = Calendar.getInstance();
                int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                List<PrayerDayData> prayerDayDataList = new ArrayList<>();

                for (int day = 1; day <= daysInMonth; day++) {
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    DateComponents date = new DateComponents(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH));

                    PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, calculationParams);
                    String maghribTime = formatter.format(prayerTimes.maghrib);

                    Calendar now = Calendar.getInstance();
                    now.setTime(prayerTimes.fajr);
                    String sehriLastTime = formatter.format(now.getTime());

                    String bengaliDay = new SimpleDateFormat("EEEE", new Locale("bn")).format(calendar.getTime());
                    String bengaliDate = new SimpleDateFormat("d MMMM", new Locale("bn")).format(calendar.getTime());

                    prayerDayDataList.add(new PrayerDayData(toBengaliDigits(String.valueOf(day)),
                            bengaliDay,
                            bengaliDate,
                            sehriLastTime.toLowerCase(),
                            maghribTime.toLowerCase()));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayPrayerTimesRows(prayerDayDataList);
                    }
                });
            }
        });

        thread.start();
    }

    private void displayPrayerTimesRows(List<PrayerDayData> prayerDayDataList) {
        LinearLayout layoutParent = findViewById(R.id.layoutParent);
        layoutParent.removeAllViews();

        for (PrayerDayData data : prayerDayDataList) {
            View rowView = getLayoutInflater().inflate(R.layout.layout_sehri_iftar_row, null);

            TextView textKeyNumber = rowView.findViewById(R.id.textKeyNumber);
            TextView textDay = rowView.findViewById(R.id.textDay);
            TextView textDate = rowView.findViewById(R.id.textDate);
            TextView textSehriEndTime = rowView.findViewById(R.id.textSehriEndTime);
            TextView textIftarTime = rowView.findViewById(R.id.textIftarTime);

            textKeyNumber.setText(data.getDayNumber());
            textDay.setText(data.getBengaliDay());
            textDate.setText(data.getBengaliDate());
            textSehriEndTime.setText(data.getSehriLastTime());
            textIftarTime.setText(data.getMaghribTime());

            layoutParent.addView(rowView);
        }
    }

    public static class PrayerDayData {
        private String dayNumber;
        private String bengaliDay;
        private String bengaliDate;
        private String sehriLastTime;
        private String maghribTime;

        public PrayerDayData(String dayNumber, String bengaliDay, String bengaliDate, String sehriLastTime, String maghribTime) {
            this.dayNumber = dayNumber;
            this.bengaliDay = bengaliDay;
            this.bengaliDate = bengaliDate;
            this.sehriLastTime = sehriLastTime;
            this.maghribTime = maghribTime;
        }

        public String getDayNumber() {
            return dayNumber;
        }

        public String getBengaliDay() {
            return bengaliDay;
        }

        public String getBengaliDate() {
            return bengaliDate;
        }

        public String getSehriLastTime() {
            return sehriLastTime;
        }

        public String getMaghribTime() {
            return maghribTime;
        }
    }
    private String toBengaliDigits(String date) {
        String[] englishDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9","am","pm"};
        String[] bengaliDigits = {"০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯","AM","PM"};

        for (int i = 0; i < englishDigits.length; i++) {
            date = date.replace(englishDigits[i], bengaliDigits[i]);
        }

        return date;
    }

    @Override
    protected void onResume() {
        loadLocationFromPreferences();
        LoadSehriIftar();
        super.onResume();
    }

    private void loadLocationFromPreferences() {
        // Load Location from preferences
        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);
        String selectedDistrict = sharedPreferences.getString("selectedDistrict", null);

        if (selectedDistrict == null || selectedDistrict.isEmpty()) {
            textCurrentLocation.setText("ঢাকা");
        } else {
            textCurrentLocation.setText(selectedDistrict);
        }
    }
}
