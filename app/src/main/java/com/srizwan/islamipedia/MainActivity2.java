package com.srizwan.islamipedia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.util.IslamicCalendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.HighLatitudeRule;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity {


    Intent targetActivity;
    String hijriDateStr = "";
    String banglaDateStr = "";
    private static final String PREFERENCE_NAME = "HijriDatePref";
    private static final String KEY_HIJRI_DATE = "HijriDate";
    TextView textTahajjudTime, textFazarTime, textIshrakTime, textSunriseTime, textJaharTime, textAshrTime, textSunsetTime, textMagribhTime, textEshaTime;
    String fajrTimeRange, dhuhrTimeRange, asrTimeRange, maghribTimeRange, ishaTimeRange;

    LinearLayout buttonChangeLocation;
    private LinearLayout Gone;
    private static final int REQUEST_CODE_CHANGE_LOCATION = 1;
    private TextView textEnglishDate, textHizriDate, textNextPrayerTimeRange, Donate;
    private TextView textCurrentPrayer, textCurrentPrayerTimeRange, textNextPrayer, textMorningForbiddenTime, textNoonForbiddenTime, textEveningForbiddenTime;

    private TextView textSehriEndTime, textIftarTime, textForbiddenPrayerTimeRange, textForbiddenPrayerTimeRemaining;
    private TextView textCurrentPrayerTimeRemaining, textAppVersion, textCheckForUpdate;

    private ProgressBar progress_CurrentTimeRemaining, progress_ForbiddenPrayerTimeRemaining;
    LinearLayout layoutCurrentPrayer, layoutNextPrayer, layoutForbiddenPrayer;
    BottomNavigationView bottomNavigationView;
    TextView textCurrentLocation;
    private ProgressDialog progressDialog;

    private static final int UPDATE_INTERVAL = 1000 * 60 * 1; // Update every 1 minutes
    private Handler handler;
    private Runnable runnable;

    private static final String CHANNEL_ID = "namaz_timer";
    private static final int NOTIFICATION_ID = 1;

    // private InAppUpdateManager inAppUpdateManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setBackgroundColor(Color.TRANSPARENT);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int app = item.getItemId();

                if (app == R.id.nav_new_chat) {
                    startActivity(new Intent(MainActivity2.this, SehriIftarActivity.class));

                    return true;

                }
                if (app == R.id.nav_home) {
                    startActivity(new Intent(MainActivity2.this, NamazTimeTableActivity.class));

                    return true;

                }

                if (app == R.id.nav_profile) {

                    startActivity(new Intent(MainActivity2.this, Main0Activity.class));

                    return true;

                }


                return false;
            }
        });

        //////////////////////////////////salat display////////////////////////////////////////

        textFazarTime = findViewById(R.id.textFazarTime);
        textJaharTime = findViewById(R.id.textJaharTime);
        textAshrTime = findViewById(R.id.textAshrTime);
        textMagribhTime = findViewById(R.id.textMagribhTime);
        textEshaTime = findViewById(R.id.textEshaTime);
        //textTahajjudTime = findViewById(R.id.textTahajjudTime);
        //textIshrakTime = findViewById(R.id.textIshrakTime);
        textSunsetTime = findViewById(R.id.textSunsetTime);
        textSunriseTime = findViewById(R.id.textSunriseTime);
        // Change Location Button
        textCurrentLocation = findViewById(R.id.textCurrentLocation);
        buttonChangeLocation = findViewById(R.id.buttonChangeLocation);

        buttonChangeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, ChangeLocationActivity.class);
                startActivity(intent);
            }
        });

        // Load Location from preferences
        loadLocationFromPreferences();

        // Print Dates
        //BanglaCalendar banglacalendar = new BanglaCalendar();

        // Get the current Bangla date
        //String banglaDate = banglacalendar.now(new Locale("bn"));

        textEnglishDate = findViewById(R.id.textEnglishDate);
        textHizriDate = findViewById(R.id.textHizriDate);

        String englishDate = getEnglishDate();
        //String hijriDate = getBengaliHijriDate();

        textEnglishDate.setText(englishDate);

        sharedPreferences = getSharedPreferences("datePrefs", MODE_PRIVATE);

/*
        if (isNetworkAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fetchHijriDateFromAPI();
            }
        } else {
            loadDateFromPrefs();
        }
*/
        if (isNetworkAvailable()) {
            // If internet is available, fetch new date and update SharedPreferences
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                String result = fetchDataFromAPI();
                runOnUiThread(() -> {
                    if (result != null && !result.isEmpty()) {
                        updateUI(result); // update UI and save to SharedPreferences
                    } else {
                        loadHijriDateFromPreferences(); // fallback if result is empty
                    }
                });
            });
        } else {// No internet: show previously saved Hijri date
            loadHijriDateFromPreferences();
        }

        //LinearLayout buttonGoFacebook = findViewById(R.id.buttonGoFacebook);

        // Initialize the progress dialog
        progressDialog = new ProgressDialog(MainActivity2.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false); // Prevent the user from canceling the progress dialog


        // Get Current Prayer
        textCurrentPrayer = findViewById(R.id.textCurrentPrayer);
        textCurrentPrayerTimeRange = findViewById(R.id.textCurrentPrayerTimeRange);
        textNextPrayer = findViewById(R.id.textNextPrayer);
        textNextPrayerTimeRange = findViewById(R.id.textNextPrayerTimeRange);
        textForbiddenPrayerTimeRange = findViewById(R.id.textForbiddenPrayerTimeRange);

        textCurrentPrayerTimeRemaining = findViewById(R.id.textCurrentPrayerTimeRemaining);
        progress_CurrentTimeRemaining = findViewById(R.id.progress_CurrentTimeRemaining);

        textMorningForbiddenTime = findViewById(R.id.textMorningForbiddenTime);
        textNoonForbiddenTime = findViewById(R.id.textNoonForbiddenTime);
        textEveningForbiddenTime = findViewById(R.id.textEveningForbiddenTime);

        layoutCurrentPrayer = findViewById(R.id.layoutCurrentPrayer);
        layoutNextPrayer = findViewById(R.id.layoutNextPrayer);
        layoutForbiddenPrayer = findViewById(R.id.layoutForbiddenPrayer);
        textForbiddenPrayerTimeRemaining = findViewById(R.id.textForbiddenPrayerTimeRemaining);

        textSehriEndTime = findViewById(R.id.textSehriEndTime);
        textIftarTime = findViewById(R.id.textIftarTime);

        startPrayerTimeUpdates();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                    finish();
            }
        });
    }

    public static String getBanglaDate(LocalDate gregorianDate) {
        LocalDate banglaNewYear = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            banglaNewYear = LocalDate.of(gregorianDate.getYear(), 4, 14);
        }
        int banglaYear = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            banglaYear = gregorianDate.getYear() - 593;
        }

        long daysBetween = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            daysBetween = ChronoUnit.DAYS.between(banglaNewYear, gregorianDate);
        }
        if (daysBetween < 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                banglaNewYear = banglaNewYear.minusYears(1);
            }
            banglaYear -= 1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                daysBetween = ChronoUnit.DAYS.between(banglaNewYear, gregorianDate);
            }
        }

        // মাস ও তারিখ ক্যালকুলেশন
        String[] banglaMonths = {
                "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
                "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
        };
        int[] daysInMonth = {31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29, 30};

        int monthIndex = 0;
        while (daysBetween >= daysInMonth[monthIndex]) {
            daysBetween -= daysInMonth[monthIndex];
            monthIndex++;
        }

        int banglaDay = (int)daysBetween + 1;
        String banglaMonth = banglaMonths[monthIndex];

        return banglaDay + " " + banglaMonth + ", " + banglaYear;
    }

    private void loadDateFromPrefs() {
        String hijri = sharedPreferences.getString("hijri", "হিজরি তারিখ পাওয়া যায়নি");
        String bangla = sharedPreferences.getString("bangla", "বঙ্গাব্দ তারিখ পাওয়া যায়নি");
        textHizriDate.setText(hijri + " | " + bangla);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void fetchHijriDateFromAPI() {
        new Thread(() -> {
            try {
                // ⬇️ Step 1: Get offset from external JSON API
                int offset = 0; // default
                try {
                    URL offsetUrl = new URL("https://www.dropbox.com/scl/fi/p1rrq2j9u0ttjas0a82xd/hijri.json?rlkey=wow5mv5j9z2vu3lk568gx0fpx&st=ikx18de2&dl=1"); // Replace with your actual URL
                    HttpURLConnection offsetConn = (HttpURLConnection) offsetUrl.openConnection();
                    offsetConn.setRequestMethod("GET");
                    InputStream offsetInput = new BufferedInputStream(offsetConn.getInputStream());

                    BufferedReader offsetReader = new BufferedReader(new InputStreamReader(offsetInput));
                    StringBuilder offsetResult = new StringBuilder();
                    String offsetLine;

                    while ((offsetLine = offsetReader.readLine()) != null) {
                        offsetResult.append(offsetLine);
                    }

                    JSONObject offsetJson = new JSONObject(offsetResult.toString());
                    offset = offsetJson.getInt("hijri_offset");

                } catch (Exception e) {
                    e.printStackTrace(); // fallback to 0
                }

                // ⬇️ Step 2: Get Hijri date from Aladhan API
                URL url = new URL("https://api.aladhan.com/v1/gToH");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                InputStream input = new BufferedInputStream(conn.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                JSONObject hijri = json.getJSONObject("data").getJSONObject("hijri");

                int day = hijri.getInt("day");
                int month = hijri.getJSONObject("month").getInt("number");
                int year = hijri.getInt("year");

                // ⬇️ Step 3: Apply offset
                LocalDate hijriDate = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    hijriDate = LocalDate.of(year, month, day);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    hijriDate = hijriDate.plusDays(offset);
                }

                int correctedDay = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    correctedDay = hijriDate.getDayOfMonth();
                }
                int correctedMonth = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    correctedMonth = hijriDate.getMonthValue();
                }
                int correctedYear = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    correctedYear = hijriDate.getYear();
                }

                // ⬇️ Step 4: Convert to Bangla text
                String monthEn = hijri.getJSONObject("month").getString("en");
                String monthBn = getHijriMonthBN(monthEn); // new method using number
                hijriDateStr = toBanglaNumber(correctedDay) + " " + monthBn + ", " + toBanglaNumber(correctedYear) + " হিজরি";

                // ⬇️ Step 5: Get Bangla date
                Calendar calendar = Calendar.getInstance();
                banglaDateStr = getBanglaBongDate(calendar);
                String banglaDate;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    banglaDate = getBanglaDate(LocalDate.now());
                } else {
                    banglaDate = banglaDateStr;
                }

                // Save to SharedPreferences
                sharedPreferences.edit()
                        .putString("hijri", hijriDateStr)
                        .putString("bangla", banglaDate)
                        .apply();

                runOnUiThread(() -> textHizriDate.setText(toBengaliDigits(hijriDateStr + " | " + banglaDate)));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> textHizriDate.setText("তারিখ লোড করা যায়নি"));
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getHijriMonthBN(String monthEn) {
        Map<String, String> months = new HashMap<>();
        months.put("Muḥarram", "মুহাররম");
        months.put("Ṣafar", "সফর");
        months.put("Rabīʿ al-awwal", "রবিউল আউয়াল");
        months.put("Rabīʿ al-thānī", "রবিউস সানি");
        months.put("Jumādá al-ūlá", "জুমাদাল উলা");
        months.put("Jumādá al-ākhirah", "জুমাদাস সানি");
        months.put("Rajab", "রজব");
        months.put("Shaʿbān", "শাবান");
        months.put("Ramaḍān", "রমজান");
        months.put("Shawwāl", "শাওয়াল");
        months.put("Dhū al-Qaʿdah", "যিলকদ্ব");
        months.put("Dhū al-Ḥijjah", "যিলহজ্ব");
        return months.getOrDefault(monthEn, monthEn);
    }

    private String toBanglaNumber(int number) {
        String[] banglaDigits = {"০","১","২","৩","৪","৫","৬","৭","৮","৯"};
        String numStr = String.valueOf(number);
        StringBuilder banglaDate = new StringBuilder();
        for (char c : numStr.toCharArray()) {
            if (Character.isDigit(c)) {
                banglaDate.append(banglaDigits[c - '0']);
            } else {
                banglaDate.append(c);
            }
        }
        return banglaDate.toString();
    }

    private String getBanglaBongDate(Calendar date) {
        int year = date.get(Calendar.YEAR) - 593;
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        String[] banglaMonthsBong = {
                "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
                "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
        };

        if (month < 3 || (month == 3 && day < 14)) {
            year--;
            month = month + 9;
        } else {
            month = month - 3;
        }

        month %= 12;

        return toBanglaNumber(day) + " " + banglaMonthsBong[month] + ", " + toBanglaNumber(year) + " বঙ্গাব্দ";
    }

    // Method to check network connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Method to load Hijri date from SharedPreferences
    private void loadHijriDateFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        String hijriDate = sharedPreferences.getString(KEY_HIJRI_DATE, "Date not available");
        textHizriDate.setText(hijriDate);
    }
    private String fetchDataFromAPI() {
        String apiUrl = "https://www.dropbox.com/scl/fi/xq6324fzd2at0wqo7fhvn/hijridate.json?rlkey=a8ymsdtr3554sdnrxstpoc4wk&st=i739zrrs&dl=1";
        String result = "";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
            int data = reader.read();
            StringBuilder stringBuilder = new StringBuilder();

            while (data != -1) {
                char current = (char) data;
                stringBuilder.append(current);
                data = reader.read();
            }

            result = stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void updateUI(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String hijriDate = jsonObject.getString("hijridate");

            // Save date to SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_HIJRI_DATE, hijriDate);
            editor.apply();

            // Show in TextView
            textHizriDate.setText(hijriDate);

        } catch (Exception e) {
            e.printStackTrace();
            // Show previous date if JSON parsing fails
            loadHijriDateFromPreferences();
            Toast.makeText(MainActivity2.this, "হিজরি তারিখ লোড করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
        }
    }


    private void startPrayerTimeUpdates() {
        calculateAndDisplayPrayerTimes();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                calculateAndDisplayPrayerTimes();

                // Schedule the next update after the defined interval
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };

        // Start the initial update
        handler.post(runnable);
    }

    private void stopPrayerTimeUpdates() {
        // Stop the updates when needed (for example, in onPause or onDestroy)
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPrayerTimeUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPrayerTimeUpdates();
        calculateAndDisplayPrayerTimes();
        loadLocationFromPreferences();
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

    private void checkTimeAndNotifyWaqt() {
        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);

        String savedCalcMethod = sharedPreferences.getString("selectedCalcMethod", null);
        String savedMadhab = sharedPreferences.getString("selectedMadhab", null);
        String selectedLatitude = sharedPreferences.getString("selectedLatitude", null);
        String selectedLongitude = sharedPreferences.getString("selectedLongitude", null);

        double latitude = 23.7104;
        double longitude = 90.40744;

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
            calculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters();
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

        //calculationMethod.madhab = Madhab.SHAFI;
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

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa", new Locale("bn"));
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));

        CalculationParameters calculationParams = calculationMethod;
        PrayerTimes prayerTimes = new PrayerTimes(coordinates, dateComponents, calculationParams);

        String fajrTime = formatter.format(prayerTimes.fajr);
        String dhuhrTime = formatter.format(prayerTimes.dhuhr);
        String asrTimeString = formatter.format(prayerTimes.asr);
        String maghribTime = formatter.format(prayerTimes.maghrib);
        String ishaTime = formatter.format(prayerTimes.isha);


        // New Code
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String currentTime = sdf.format(new Date());

        String currentPrayerName = "";
        String currentPrayerTimeRange = "";
        String prayerTimeRemaining = "";

        // Ishrak Time
        Calendar ishrakTime = Calendar.getInstance();
        ishrakTime.setTime(prayerTimes.sunrise);
        ishrakTime.add(Calendar.MINUTE, 20);
        String ishrakStartTime = formatter.format(ishrakTime.getTime());
        Calendar ishrakEnd = Calendar.getInstance();
        ishrakEnd.setTime(ishrakTime.getTime());
        ishrakEnd.add(Calendar.HOUR, 2);
        String ishrakEndTime = formatter.format(ishrakEnd.getTime());
        String[] ishrakRange = {ishrakStartTime, ishrakEndTime};
        // Fajr Time
        Calendar fajrEnd = Calendar.getInstance();
        fajrEnd.setTime(prayerTimes.sunrise);
        fajrEnd.add(Calendar.MINUTE, -5);
        String fajrEndTime = formatter.format(fajrEnd.getTime());
        String[] fajrRange = {fajrTime, fajrEndTime};
        // Dhuhr Time
        Calendar dhuhrEnd = Calendar.getInstance();
        dhuhrEnd.setTime(prayerTimes.asr);
        dhuhrEnd.add(Calendar.MINUTE, -1);
        String dhuhrEndTime = formatter.format(dhuhrEnd.getTime());
        String[] dhuhrRange = {dhuhrTime, dhuhrEndTime};
        // Asr Time
        Calendar asrEnd = Calendar.getInstance();
        asrEnd.setTime(prayerTimes.maghrib);
        asrEnd.add(Calendar.MINUTE, -15);
        String asrEndTime = formatter.format(asrEnd.getTime());
        String[] asrRange = {asrTimeString, asrEndTime};
        // Maghrib Time
        Calendar maghribEn = Calendar.getInstance();
        maghribEn.setTime(prayerTimes.isha);
        maghribEn.add(Calendar.MINUTE, -1);
        String maghribEndTim = formatter.format(maghribEn.getTime());
        String[] maghribRange = {maghribTime, maghribEndTim};
        // Isha Time
        Calendar ishaEnd = Calendar.getInstance();
        ishaEnd.setTime(prayerTimes.isha);
        ishaEnd.add(Calendar.HOUR, 4);
        String ishaEndTime = formatter.format(ishaEnd.getTime());
        String[] ishaRange = {ishaTime, ishaEndTime};
        // Tahazzud Time
        String tahazzudStartTime = toBengaliDigits(calculateOneThirdTimes(maghribTime, fajrTime));
        Calendar tahazzudEnd = Calendar.getInstance();
        tahazzudEnd.setTime(prayerTimes.fajr);
        tahazzudEnd.add(Calendar.MINUTE, -1);
        String tahazzudEndTime = formatter.format(tahazzudEnd.getTime());
        String[] tahazzudRange = {tahazzudStartTime, tahazzudEndTime};


        if (isTimeWithinRange(ishrakRange[0], ishrakRange[1], currentTime)) {
            // layoutCurrentPrayer.setVisibility(View.VISIBLE);
            currentPrayerName = "ইশরাক";
            currentPrayerTimeRange = ishrakRange[0] + " - " + ishrakRange[1];
            calculatePercentageAndSetProgressBar(ishrakRange[0], ishrakRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(ishrakRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(fajrRange[0], fajrRange[1], currentTime)) {
            currentPrayerName = "ফজর";
            currentPrayerTimeRange = fajrRange[0] + " - " + fajrRange[1];
            calculatePercentageAndSetProgressBar(fajrRange[0], fajrRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(fajrRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(dhuhrRange[0], dhuhrRange[1], currentTime)) {
            Calendar now = Calendar.getInstance();
            if (now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                currentPrayerName = "জুম'আ";
            } else {
                currentPrayerName = "যোহর";
            }
            currentPrayerTimeRange = dhuhrRange[0] + " - " + dhuhrRange[1];
            calculatePercentageAndSetProgressBar(dhuhrRange[0], dhuhrRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(dhuhrRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(asrRange[0], asrRange[1], currentTime)) {
            currentPrayerName = "আসর";
            currentPrayerTimeRange = asrRange[0] + " - " + asrRange[1];
            calculatePercentageAndSetProgressBar(asrRange[0], asrRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(asrRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(maghribRange[0], maghribRange[1], currentTime)) {
            currentPrayerName = "মাগরিব";
            currentPrayerTimeRange = maghribRange[0] + " - " + maghribRange[1];
            calculatePercentageAndSetProgressBar(maghribRange[0], maghribRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(maghribRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(ishaRange[0], ishaRange[1], currentTime)) {
            currentPrayerName = "ইশা";
            currentPrayerTimeRange = ishaRange[0] + " - " + ishaRange[1];
            calculatePercentageAndSetProgressBar(ishaRange[0], ishaRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(ishaRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endTimeDate);
                if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                    calendar.add(Calendar.DATE, 1);
                }
                endTimeDate = calendar.getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(tahazzudRange[0], tahazzudRange[1], currentTime)) {
            currentPrayerName = "তাহাজ্জুদ";
            currentPrayerTimeRange = tahazzudRange[0] + " - " + tahazzudRange[1];
            calculatePercentageAndSetProgressBar(tahazzudRange[0], tahazzudRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(tahazzudRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    public void calculateAndDisplayPrayerTimes() {
        SharedPreferences sharedPreferences = getSharedPreferences("NamazTimerPreferences", MODE_PRIVATE);

        String savedCalcMethod = sharedPreferences.getString("selectedCalcMethod", null);
        String savedMadhab = sharedPreferences.getString("selectedMadhab", null);
        String selectedLatitude = sharedPreferences.getString("selectedLatitude", null);
        String selectedLongitude = sharedPreferences.getString("selectedLongitude", null);

        double latitude = 23.7104;
        double longitude = 90.40744;

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

        //calculationMethod.madhab = Madhab.SHAFI;
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

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa", new Locale("bn"));
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));

        CalculationParameters calculationParams = calculationMethod;
        PrayerTimes prayerTimes = new PrayerTimes(coordinates, dateComponents, calculationParams);

        String fajrTime = formatter.format(prayerTimes.fajr);
        String dhuhrTime = formatter.format(prayerTimes.dhuhr);
        String asrTimeString = formatter.format(prayerTimes.asr);
        String maghribTime = formatter.format(prayerTimes.maghrib);
        String ishaTime = formatter.format(prayerTimes.isha);
        String sunriseTime = formatter.format(prayerTimes.sunrise);


        // Adding 15 minutes to the sunrise time to create the end of the forbidden time range in the morning
        Calendar forbiddenMorningStart = Calendar.getInstance();
        forbiddenMorningStart.setTime(prayerTimes.sunrise);
        forbiddenMorningStart.add(Calendar.MINUTE, 1);
        Calendar forbiddenMorningEnd = Calendar.getInstance();
        forbiddenMorningEnd.setTime(prayerTimes.sunrise);
        forbiddenMorningEnd.add(Calendar.MINUTE, 15);
        String forbiddenMorningStartTime = formatter.format(forbiddenMorningStart.getTime());
        String forbiddenMorningEndTime = formatter.format(forbiddenMorningEnd.getTime());
        boolean isForbiddenMorning = Calendar.getInstance().getTime().after(forbiddenMorningStart.getTime()) && Calendar.getInstance().getTime().before(forbiddenMorningEnd.getTime());

        // Adding 15 minutes before Dhuhr to create the start of the forbidden noon time range
        Calendar forbiddenNoonStart = Calendar.getInstance();
        forbiddenNoonStart.setTime(prayerTimes.dhuhr);
        forbiddenNoonStart.add(Calendar.MINUTE, -15);
        String forbiddenNoonStartTime = formatter.format(forbiddenNoonStart.getTime());
        Calendar forbiddenNoonEnd = Calendar.getInstance();
        forbiddenNoonEnd.setTime(prayerTimes.dhuhr);
        forbiddenNoonEnd.add(Calendar.MINUTE, -1);
        String forbiddenNoonEndTime = formatter.format(forbiddenNoonEnd.getTime());
        boolean isForbiddenNoon = Calendar.getInstance().getTime().after(forbiddenNoonStart.getTime()) && Calendar.getInstance().getTime().before(forbiddenNoonEnd.getTime());

        // Adding 15 minutes before Magribh to create the start of the forbidden noon time range
        Calendar forbiddenEveningStart = Calendar.getInstance();
        forbiddenEveningStart.setTime(prayerTimes.maghrib);
        forbiddenEveningStart.add(Calendar.MINUTE, -15);
        String forbiddenEveningStartTime = formatter.format(forbiddenEveningStart.getTime());
        Calendar forbiddenEveningEnd = Calendar.getInstance();
        forbiddenEveningEnd.setTime(prayerTimes.maghrib);
        forbiddenEveningEnd.add(Calendar.MINUTE, -1);
        String forbiddenEveningEndTime = formatter.format(forbiddenEveningEnd.getTime());
        boolean isForbiddenEvening = Calendar.getInstance().getTime().after(forbiddenEveningStart.getTime()) && Calendar.getInstance().getTime().before(forbiddenEveningEnd.getTime());


        // Forbidden Layout

        String forbiddenRange = "";
        String forbiddenPrayerTimeRemaining = "";
        if (isForbiddenMorning) {
            forbiddenRange = forbiddenMorningStartTime + " - " + forbiddenMorningEndTime;
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            calculatePercentageAndSetProgressBar(forbiddenMorningStartTime, forbiddenMorningEndTime);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(forbiddenMorningEndTime);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                String forbiddenRangeTimeRange = getTimeFormatted(timeRemainingInMillis);
                forbiddenPrayerTimeRemaining = forbiddenRangeTimeRange;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isForbiddenNoon) {
            forbiddenRange = forbiddenNoonStartTime + " - " + forbiddenNoonEndTime;
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            calculatePercentageAndSetProgressBar(forbiddenNoonStartTime, forbiddenNoonEndTime);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(forbiddenNoonEndTime);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                String forbiddenRangeTimeRange = getTimeFormatted(timeRemainingInMillis);
                forbiddenPrayerTimeRemaining = forbiddenRangeTimeRange;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isForbiddenEvening) {
            forbiddenRange = forbiddenEveningStartTime + " - " + forbiddenEveningEndTime;
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            calculatePercentageAndSetProgressBar(forbiddenEveningStartTime, forbiddenEveningEndTime);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(forbiddenEveningEndTime);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                String forbiddenRangeTimeRange = getTimeFormatted(timeRemainingInMillis);
                forbiddenPrayerTimeRemaining = forbiddenRangeTimeRange;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (isForbiddenMorning || isForbiddenNoon || isForbiddenEvening) {
            layoutForbiddenPrayer.setVisibility(View.VISIBLE);
            textForbiddenPrayerTimeRange.setText(forbiddenRange.toLowerCase());
        } else {
            layoutForbiddenPrayer.setVisibility(View.GONE);
            textForbiddenPrayerTimeRange.setText("");
        }

        // Change the next prayer to Jummah on Friday
        Calendar now = Calendar.getInstance();
        Prayer nextPrayer = prayerTimes.nextPrayer(now.getTime());
        String nextPrayerName = "";
        if (nextPrayer == Prayer.DHUHR && now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            nextPrayerName = "জুম'আ";
        } else {
            nextPrayerName = nextPrayer.name();
        }

        String nextPrayerTimeRange = "";
        if (nextPrayer == Prayer.FAJR) {
            Calendar fajrStartTim = Calendar.getInstance();
            fajrStartTim.setTime(prayerTimes.sunrise);
            fajrStartTim.add(Calendar.MINUTE, -5);
            String fajrEndTime = formatter.format(fajrStartTim.getTime());
            nextPrayerTimeRange = fajrTime + " - " + fajrEndTime;


        } else if (nextPrayer == Prayer.DHUHR) {
            Calendar timeNow1 = Calendar.getInstance();
            timeNow1.setTime(prayerTimes.asr);
            timeNow1.add(Calendar.MINUTE, -1);
            String DhuhurEndTime = formatter.format(timeNow1.getTime());

            nextPrayerTimeRange = dhuhrTime + " - " + DhuhurEndTime;


        } else if (nextPrayer == Prayer.ASR) {
            Calendar timeNow2 = Calendar.getInstance();
            timeNow2.setTime(prayerTimes.maghrib);
            timeNow2.add(Calendar.MINUTE, -15);
            String AsrEndTime = formatter.format(timeNow2.getTime());

            nextPrayerTimeRange = asrTimeString + " - " + AsrEndTime;


        } else if (nextPrayer == Prayer.MAGHRIB) {
            Calendar maghribEnd = Calendar.getInstance();
            maghribEnd.setTime(prayerTimes.isha);
            maghribEnd.add(Calendar.MINUTE, -1);
            String maghribStartTime = maghribTime;
            String maghribEndTime = formatter.format(maghribEnd.getTime());

            nextPrayerTimeRange = maghribStartTime + " - " + maghribEndTime;


        } else if (nextPrayer == Prayer.ISHA) {
            Calendar timeNow4 = Calendar.getInstance();
            timeNow4.setTime(prayerTimes.isha);
            timeNow4.add(Calendar.HOUR, 4);
            String IshaEndTime = formatter.format(timeNow4.getTime());

            nextPrayerTimeRange = ishaTime + " - " + IshaEndTime;


        } else if (nextPrayer == Prayer.NONE) {
            // Hide Next Prayer Layer
            //layoutNextPrayer.setVisibility(View.GONE);

            // Turn into Tahazzud
            String tahazzudStartTime = toBengaliDigits(calculateOneThirdTimes(maghribTime, fajrTime));
            Calendar tahazzudEnd = Calendar.getInstance();
            tahazzudEnd.setTime(prayerTimes.fajr);
            tahazzudEnd.add(Calendar.MINUTE, -1);
            String tahazzudEndTime = formatter.format(tahazzudEnd.getTime());

            nextPrayerTimeRange = tahazzudStartTime + " - " + tahazzudEndTime;
            nextPrayerName = "তাহাজ্জুদ";
        }


        // New Code
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String currentTime = sdf.format(new Date());

        String currentPrayerName = "";
        String currentPrayerTimeRange = "";
        String prayerTimeRemaining = "";

        // Ishrak Time
        Calendar ishrakTime = Calendar.getInstance();
        ishrakTime.setTime(prayerTimes.sunrise);
        ishrakTime.add(Calendar.MINUTE, 20);
        String ishrakStartTime = formatter.format(ishrakTime.getTime());
        Calendar ishrakEnd = Calendar.getInstance();
        ishrakEnd.setTime(ishrakTime.getTime());
        ishrakEnd.add(Calendar.HOUR, 2);
        String ishrakEndTime = formatter.format(ishrakEnd.getTime());
        String[] ishrakRange = {ishrakStartTime, ishrakEndTime};


        // Fajr Time
        Calendar fajrEnd = Calendar.getInstance();
        fajrEnd.setTime(prayerTimes.sunrise);
        fajrEnd.add(Calendar.MINUTE, -5);
        String fajrEndTime = formatter.format(fajrEnd.getTime());
        String[] fajrRange = {fajrTime, fajrEndTime};
        fajrTimeRange = fajrTime + " - " + fajrEndTime;
        textFazarTime.setText(fajrTimeRange.toLowerCase());


        // Dhuhr Time
        Calendar dhuhrEnd = Calendar.getInstance();
        dhuhrEnd.setTime(prayerTimes.asr);
        dhuhrEnd.add(Calendar.MINUTE, -1);
        String dhuhrEndTime = formatter.format(dhuhrEnd.getTime());
        String[] dhuhrRange = {dhuhrTime, dhuhrEndTime};
        dhuhrTimeRange = dhuhrTime + " - " + dhuhrEndTime;
        textJaharTime.setText(dhuhrTimeRange.toLowerCase());

        // Asr Time
        Calendar asrEnd = Calendar.getInstance();
        asrEnd.setTime(prayerTimes.maghrib);
        asrEnd.add(Calendar.MINUTE, -15);
        String asrEndTime = formatter.format(asrEnd.getTime());
        String[] asrRange = {asrTimeString, asrEndTime};

        asrTimeRange = asrTimeString + " - " + asrEndTime;
        textAshrTime.setText(asrTimeRange.toLowerCase());

        // Maghrib Time
        Calendar maghribEnd = Calendar.getInstance();
        maghribEnd.setTime(prayerTimes.isha);
        maghribEnd.add(Calendar.MINUTE, -1);
        String maghribEndTime = formatter.format(maghribEnd.getTime());
        String[] maghribRange = {maghribTime, maghribEndTime};

        maghribTimeRange = maghribTime + " - " + maghribEndTime;
        textMagribhTime.setText(maghribTimeRange.toLowerCase());

        // Isha Time
        Calendar ishaEnd = Calendar.getInstance();
        ishaEnd.setTime(prayerTimes.isha);
        ishaEnd.add(Calendar.HOUR, 4);
        String ishaEndTime = formatter.format(ishaEnd.getTime());
        String[] ishaRange = {ishaTime, ishaEndTime};

        ishaTimeRange = ishaTime + " - " + ishaEndTime;
        textEshaTime.setText(ishaTimeRange.toLowerCase());


        textSunriseTime.setText(sunriseTime.toLowerCase());
        textSunsetTime.setText(maghribTime.toLowerCase());


        // Tahazzud Time
        String tahazzudStartTime = toBengaliDigits(calculateOneThirdTimes(maghribTime, fajrTime));
        Calendar tahazzudEnd = Calendar.getInstance();
        tahazzudEnd.setTime(prayerTimes.fajr);
        tahazzudEnd.add(Calendar.MINUTE, -1);
        String tahazzudEndTime = formatter.format(tahazzudEnd.getTime());
        String[] tahazzudRange = {tahazzudStartTime, tahazzudEndTime};


        if (isTimeWithinRange(ishrakRange[0], ishrakRange[1], currentTime)) {
            layoutCurrentPrayer.setVisibility(View.VISIBLE);
            currentPrayerName = "ইশরাক";
            currentPrayerTimeRange = ishrakRange[0] + " - " + ishrakRange[1];
            calculatePercentageAndSetProgressBar(ishrakRange[0], ishrakRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(ishrakRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                prayerTimeRemaining = getTimeFormatted(timeRemainingInMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(fajrRange[0], fajrRange[1], currentTime)) {
            layoutCurrentPrayer.setVisibility(View.VISIBLE);
            currentPrayerName = "ফজর";
            currentPrayerTimeRange = fajrRange[0] + " - " + fajrRange[1];
            calculatePercentageAndSetProgressBar(fajrRange[0], fajrRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(fajrRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                prayerTimeRemaining = getTimeFormatted(timeRemainingInMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(dhuhrRange[0], dhuhrRange[1], currentTime)) {
            layoutCurrentPrayer.setVisibility(View.VISIBLE);
            if (now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                currentPrayerName = "জুম'আ";
            } else {
                currentPrayerName = "যোহর";
            }
            currentPrayerTimeRange = dhuhrRange[0] + " - " + dhuhrRange[1];
            calculatePercentageAndSetProgressBar(dhuhrRange[0], dhuhrRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(dhuhrRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                prayerTimeRemaining = getTimeFormatted(timeRemainingInMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(asrRange[0], asrRange[1], currentTime)) {
            layoutCurrentPrayer.setVisibility(View.VISIBLE);
            currentPrayerName = "আসর";
            currentPrayerTimeRange = asrRange[0] + " - " + asrRange[1];
            calculatePercentageAndSetProgressBar(asrRange[0], asrRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(asrRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                prayerTimeRemaining = getTimeFormatted(timeRemainingInMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(maghribRange[0], maghribRange[1], currentTime)) {
            layoutCurrentPrayer.setVisibility(View.VISIBLE);
            currentPrayerName = "মাগরিব";
            currentPrayerTimeRange = maghribRange[0] + " - " + maghribRange[1];
            calculatePercentageAndSetProgressBar(maghribRange[0], maghribRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(maghribRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                prayerTimeRemaining = getTimeFormatted(timeRemainingInMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(ishaRange[0], ishaRange[1], currentTime)) {
            layoutCurrentPrayer.setVisibility(View.VISIBLE);
            currentPrayerName = "ইশা";
            currentPrayerTimeRange = ishaRange[0] + " - " + ishaRange[1];
            calculatePercentageAndSetProgressBar(ishaRange[0], ishaRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(ishaRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endTimeDate);
                if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                    calendar.add(Calendar.DATE, 1);
                }
                endTimeDate = calendar.getTime();

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                prayerTimeRemaining = getTimeFormatted(timeRemainingInMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (isTimeWithinRange(tahazzudRange[0], tahazzudRange[1], currentTime)) {
            layoutCurrentPrayer.setVisibility(View.VISIBLE);
            currentPrayerName = "তাহাজ্জুদ";
            currentPrayerTimeRange = tahazzudRange[0] + " - " + tahazzudRange[1];
            calculatePercentageAndSetProgressBar(tahazzudRange[0], tahazzudRange[1]);
            try {
                String currentTimeString = sdf.format(new Date());

                Date endTimeDate = sdf.parse(tahazzudRange[1]);
                Date currentTimeDate = sdf.parse(currentTimeString);

                long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();

                prayerTimeRemaining = getTimeFormatted(timeRemainingInMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            layoutCurrentPrayer.setVisibility(View.GONE);
        }


        // Print the prayers to the layouts
        textCurrentPrayer.setText(convertToBengaliPrayer(currentPrayerName));
        textCurrentPrayerTimeRange.setText(convertToBengaliPrayer(currentPrayerTimeRange).toLowerCase());
        textNextPrayer.setText(convertToBengaliPrayer(nextPrayerName));
        textNextPrayerTimeRange.setText(convertToBengaliPrayer(nextPrayerTimeRange).toLowerCase());
        textMorningForbiddenTime.setText(forbiddenMorningStartTime.toLowerCase() + " - " + forbiddenMorningEndTime.toLowerCase());
        textNoonForbiddenTime.setText(forbiddenNoonStartTime.toLowerCase() + " - " + forbiddenNoonEndTime.toLowerCase());
        textEveningForbiddenTime.setText(forbiddenEveningStartTime.toLowerCase() + " - " + forbiddenEveningEndTime.toLowerCase());
        textSehriEndTime.setText(fajrTime.toLowerCase());
        textIftarTime.setText(maghribTime.toLowerCase());

        textCurrentPrayerTimeRemaining.setText(prayerTimeRemaining);
        textForbiddenPrayerTimeRemaining.setText(forbiddenPrayerTimeRemaining);
    }


    private void calculatePercentageAndSetProgressBar(String timeStart, String timeEnd) {
        String currentTime = getCurrentTime();

        ProgressBar progressBar = findViewById(R.id.progress_CurrentTimeRemaining);
        ProgressBar progressBar1 = findViewById(R.id.progress_ForbiddenPrayerTimeRemaining);
        double percentageLeft = calculatePercentageLeft(timeStart, timeEnd, currentTime);
        int progress = 100 - (int) percentageLeft;
        progressBar.setProgress(progress);
        progressBar1.setProgress(progress);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(new Date());
    }

    private double calculatePercentageLeft(String startTime, String endTime, String currentTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

            Date startTimeDate = sdf.parse(startTime);
            Date endTimeDate = sdf.parse(endTime);
            Date currentTimeDate = sdf.parse(currentTime);

            long timeRemainingInMillis = endTimeDate.getTime() - currentTimeDate.getTime();
            long totalTimeInMillis = endTimeDate.getTime() - startTimeDate.getTime();

            return (double) timeRemainingInMillis / totalTimeInMillis * 100;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private static boolean isTimeWithinRange(String startTime, String endTime, String currentTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        try {
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            Date currentTimeDate = sdf.parse(currentTime);

            // Parse "12:00 am" as the end of the day (midnight)
            if (endTime.equals("12:00 am")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.add(Calendar.DATE, 1);
                endDate = calendar.getTime();
            }

            // Check if the current time is within the range, considering the adjusted end time
            if (endDate.after(startDate)) {
                // Normal case: end time is after the start time on the same day
                return currentTimeDate.after(startDate) && currentTimeDate.before(endDate);
            } else {
                // Special case: end time is on the next day
                return currentTimeDate.after(startDate) || currentTimeDate.before(endDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    private static String getTimeFormatted(long timeInMillis) {
        long minutes = timeInMillis / (60 * 1000);
        long hours = minutes / 60;
        minutes %= 60;

        // If hours equal 12 and minutes equal 0, it means it's midnight (12:00 am)
        // In this case, we consider it as the start of the next day
        if (hours == 12 && minutes == 0) {
            hours = 0; // Reset the hours to 0
        }

        String remainingTime;
        if (hours > 0) {
            remainingTime = String.format(Locale.getDefault(), "%02d ঘণ্টা %02d মিনিট", hours, minutes);
        } else {
            remainingTime = String.format(Locale.getDefault(), "%02d মিনিট", minutes);
        }
        return "সময় বাকিঃ " + toBengaliDigits(remainingTime);
    }


    public static String convertToBengaliPrayer(String prayerName) {
        String[] englishPrayerNames = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
        String[] bengaliPrayerNames = {"ফজর", "যোহর", "আসর", "মাগরিব", "ইশা"};

        for (int i = 0; i < englishPrayerNames.length; i++) {
            if (prayerName.equalsIgnoreCase(englishPrayerNames[i])) {
                return bengaliPrayerNames[i];
            }
        }

        return prayerName;
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
                "রজব", "শা'বান", "রমজান", "শাওয়াল", "জ্বিলক্বদ", "জ্বিলহজ্জ", "অজানা"
        };

        String bengaliHijriMonth = bengaliMonths[hijriMonth];

        return toBengaliDigits(String.valueOf(hijriDay)) + " " + bengaliHijriMonth + ", " + toBengaliDigits(String.valueOf(hijriYear));
    }

    private static String toBengaliDigits(String date) {
        String[] englishDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "am", "pm"};
        String[] bengaliDigits = {"০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "AM", "PM"};

        for (int i = 0; i < englishDigits.length; i++) {
            date = date.replace(englishDigits[i], bengaliDigits[i]);
        }

        return date;
    }

}
