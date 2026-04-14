package com.srizwan.islamipedia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.IslamicCalendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.HighLatitudeRule;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NamazTimeTableActivity extends AppCompatActivity {

    TextView textTahajjudTime, textFazarTime, textIshrakTime, textSunriseTime, textJaharTime, textAshrTime, textSunsetTime, textMagribhTime, textDuhaTime, textAwwabeenTime, textEshaTime;
    String fajrTimeRange, dhuhrTimeRange, asrTimeRange, maghribTimeRange, ishaTimeRange;
    TextView textCurrentLocation, textDhuhrName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_namaz_time_table);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
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
            Intent intent = new Intent(NamazTimeTableActivity.this, ChangeLocationActivity.class);
            startActivity(intent);
        });

        TextView textTodayDate = findViewById(R.id.textTodayDate);

        String englishDate = getEnglishDate();
        String hijriDate = getBengaliHijriDate();

        textTodayDate.setText(englishDate);

        // All Textview
        textTahajjudTime = findViewById(R.id.textTahajjudTime);
        textFazarTime = findViewById(R.id.textFazarTime);
        textIshrakTime = findViewById(R.id.textIshrakTime);
        textSunriseTime = findViewById(R.id.textSunriseTime);
        textJaharTime = findViewById(R.id.textJaharTime);
        textAshrTime = findViewById(R.id.textAshrTime);
        textSunsetTime = findViewById(R.id.textSunsetTime);
        textMagribhTime = findViewById(R.id.textMagribhTime);
        textEshaTime = findViewById(R.id.textEshaTime);

        textDhuhrName = findViewById(R.id.textDhuhrName);
        textDuhaTime = findViewById(R.id.textDuhaTime);
        textAwwabeenTime = findViewById(R.id.textAwwabeenTime);
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            textDhuhrName.setText("জুম'আ");
        } else {
            textDhuhrName.setText("যোহর");
        }

        calculateAndDisplayPrayerTimes();
        loadLocationFromPreferences();
    }

    @Override
    protected void onResume() {
        calculateAndDisplayPrayerTimes();
        loadLocationFromPreferences();
        super.onResume();
    }

    public static String calculateOneThirdTimes(String maghribTime, String fajrTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aaa");

        try {
            Date maghrib = sdf.parse(maghribTime);
            Date fajr = sdf.parse(fajrTime);

            long difference = fajr.getTime() - maghrib.getTime();

            if (difference < 0) {
                difference += 24 * 60 * 60 * 1000;
            }

            long thirdOfTheDifference = difference / 3;

            Date lastThirdOfTheNight = new Date(fajr.getTime() - thirdOfTheDifference);

            String lastThirdOfTheNightFormatted = sdf.format(lastThirdOfTheNight);

            return lastThirdOfTheNightFormatted;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ""; // Return an empty string in case of error
    }

    private void calculateAndDisplayPrayerTimes() {
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
        final DateComponents dateComponents = DateComponents.from(new Date());
        //CalculationParameters calculationMethod = CalculationMethod.KARACHI.getParameters();

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
        PrayerTimes prayerTimes = new PrayerTimes(coordinates, dateComponents, calculationParams);

        String fajrTime = formatter.format(prayerTimes.fajr);
        String dhuhrTime = formatter.format(prayerTimes.dhuhr);
        String asrTimeString = formatter.format(prayerTimes.asr);
        String maghribTime = formatter.format(prayerTimes.maghrib);
        String ishaTime = formatter.format(prayerTimes.isha);
        String sunriseTime = formatter.format(prayerTimes.sunrise);

        // Tahazzud
        String tahazzudStartTime = toBengaliDigits(calculateOneThirdTimes(maghribTime, fajrTime));
        Calendar tahazzudEnd = Calendar.getInstance();
        tahazzudEnd.setTime(prayerTimes.fajr);
        tahazzudEnd.add(Calendar.MINUTE, -1);
        String tahazzudEndTime = formatter.format(tahazzudEnd.getTime());

        String tahazzudTimeRange = tahazzudStartTime + " - " + tahazzudEndTime;
        textTahajjudTime.setText(tahazzudTimeRange.toLowerCase());

        // FAJR
        Calendar fajrStartTime = Calendar.getInstance();
        fajrStartTime.setTime(prayerTimes.sunrise);
        fajrStartTime.add(Calendar.MINUTE, -5);
        String fajrEndTime = formatter.format(fajrStartTime.getTime());

        fajrTimeRange = fajrTime + " - " + fajrEndTime;
        textFazarTime.setText(fajrTimeRange.toLowerCase());

        // DHUHR
        Calendar dhuhurTime = Calendar.getInstance();
        dhuhurTime.setTime(prayerTimes.asr);
        dhuhurTime.add(Calendar.MINUTE, -1);
        String DhuhurEndTime = formatter.format(dhuhurTime.getTime());

        dhuhrTimeRange = dhuhrTime + " - " + DhuhurEndTime;
        textJaharTime.setText(dhuhrTimeRange.toLowerCase());

        // ASR
        Calendar asrTime = Calendar.getInstance();
        asrTime.setTime(prayerTimes.maghrib);
        asrTime.add(Calendar.MINUTE, -15);
        String AsrEndTime = formatter.format(asrTime.getTime());

        asrTimeRange = asrTimeString + " - " + AsrEndTime;
        textAshrTime.setText(asrTimeRange.toLowerCase());

        // MAGHRIB
        Calendar maghribEnd = Calendar.getInstance();
        maghribEnd.setTime(prayerTimes.isha);
        maghribEnd.add(Calendar.MINUTE, -1);
        String maghribStartTime = maghribTime;
        String maghribEndTime = formatter.format(maghribEnd.getTime());

        maghribTimeRange = maghribStartTime + " - " + maghribEndTime;
        textMagribhTime.setText(maghribTimeRange.toLowerCase());

        // ISHA
        Calendar ishaTimeEnd = Calendar.getInstance();
        ishaTimeEnd.setTime(prayerTimes.isha);
        ishaTimeEnd.add(Calendar.HOUR, 4);
        String IshaEndTime = formatter.format(ishaTimeEnd.getTime());

        ishaTimeRange = ishaTime + " - " + IshaEndTime;
        textEshaTime.setText(ishaTimeRange.toLowerCase());

        // Sunrise and Sunset
        textSunriseTime.setText(sunriseTime.toLowerCase());
        textSunsetTime.setText(maghribTime.toLowerCase());

        // Ishrak Time
        Calendar ishrakTime = Calendar.getInstance();
        ishrakTime.setTime(prayerTimes.sunrise);
        ishrakTime.add(Calendar.MINUTE, 20);
        String IshrakStartTime = formatter.format(ishrakTime.getTime());
        Calendar ishrakEnd = Calendar.getInstance();
        ishrakEnd.setTime(ishrakTime.getTime());
        ishrakEnd.add(Calendar.HOUR, 2);
        String IshrakEndTime = formatter.format(ishrakEnd.getTime());

        String ishrakPrayerTime = IshrakStartTime + " - " + IshrakEndTime;
        textIshrakTime.setText(ishrakPrayerTime.toLowerCase());
    }


    private String getEnglishDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM, yyyy", new Locale("bn"));
        String bengaliDate = dateFormat.format(calendar.getTime());
        return bengaliDate;
    }

    private String getBengaliHijriDate() {
        Calendar calendar = Calendar.getInstance();
        IslamicCalendar islamicCalendar = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            islamicCalendar = new IslamicCalendar();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            islamicCalendar.setTimeInMillis(calendar.getTimeInMillis());
        }
        int hijriYear = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hijriYear = islamicCalendar.get(IslamicCalendar.YEAR);
        }
        int hijriMonth = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hijriMonth = islamicCalendar.get(IslamicCalendar.MONTH);
        }
        int hijriDay = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hijriDay = islamicCalendar.get(IslamicCalendar.DAY_OF_MONTH);
        }

        String[] bengaliMonths = {
                "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জুমাদিউল আউয়াল", "জুমাদিউস সানি",
                "রজব", "শা'বান", "রমজান", "শাওয়াল", "জ্বিলক্বদ", "জ্বিলহজ্জ"
        };

        String bengaliHijriMonth = bengaliMonths[hijriMonth];

        return toBengaliDigits(String.valueOf(hijriDay)) + " " + bengaliHijriMonth + ", " + toBengaliDigits(String.valueOf(hijriYear));
    }

    private String toBengaliDigits(String date) {
        String[] englishDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9","AM","PM"};
        String[] bengaliDigits = {"০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯","AM","PM"};

        for (int i = 0; i < englishDigits.length; i++) {
            date = date.replace(englishDigits[i], bengaliDigits[i]);
        }

        return date;
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


