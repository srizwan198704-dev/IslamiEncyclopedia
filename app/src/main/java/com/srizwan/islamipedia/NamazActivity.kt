package com.srizwan.islamipedia

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.chrono.HijrahDate
class NamazActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var date: LinearLayout
    private lateinit var book1: LinearLayout
    private lateinit var book2: LinearLayout
    private lateinit var book3: LinearLayout
    private lateinit var book4: LinearLayout
    private lateinit var book5: LinearLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val locationRequestCode = 1000

    private lateinit var fajrTextView: TextView
    private lateinit var dhuhrTextView: TextView
    private lateinit var asrTextView: TextView
    private lateinit var maghribTextView: TextView
    private lateinit var ishaTextView: TextView

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var englishDateTextView: TextView
    private lateinit var bengaliDateTextView: TextView
    private lateinit var hijriDateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_namaz)
        AndroidThreeTen.init(this)
        try {
            // Initialize views
            fajrTextView = findViewById(R.id.fajrTime)
            dhuhrTextView = findViewById(R.id.dhuhrTime)
            asrTextView = findViewById(R.id.asrTime)
            maghribTextView = findViewById(R.id.maghribTime)
            ishaTextView = findViewById(R.id.ishaTime)

            book1 = findViewById(R.id.book1)
            book2 = findViewById(R.id.book2)
            book3 = findViewById(R.id.book3)
            book4 = findViewById(R.id.book4)
            book5 = findViewById(R.id.book5)
            date = findViewById(R.id.date)
            back = findViewById(R.id.back)
            back.setOnClickListener {
                finish()
            }

            englishDateTextView = findViewById(R.id.englishDate)
            bengaliDateTextView = findViewById(R.id.bengaliDate)
            hijriDateTextView = findViewById(R.id.hijriDate)

            sharedPreferences = getSharedPreferences("NamazTimes", MODE_PRIVATE)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.locations.forEach { location ->
                        getPrayerTimes(location.latitude, location.longitude)
                    }
                }
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationRequestCode
                )
            } else {
                startLocationUpdates()
            }

            loadSavedPrayerTimes()
            setDates()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "", Toast.LENGTH_LONG).show()
        }
    }

    private fun button(book: LinearLayout?) {
        val sketchU = GradientDrawable().apply {
            val d = applicationContext.resources.displayMetrics.density.toInt()
            setStroke(d, 0xFF01837A.toInt())
            cornerRadius = d * 5f
            setColor(0xFFFFFFFF.toInt())
        }

        book?.apply {
            elevation = 2f * resources.displayMetrics.density
            background = sketchU
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun loadSavedPrayerTimes() {
        val fajr = formatPrayerTimeTo12Hour(sharedPreferences.getString("Fajr", "N/A"))
        val dhuhr = formatPrayerTimeTo12Hour(sharedPreferences.getString("Dhuhr", "N/A"))
        val asr = formatPrayerTimeTo12Hour(sharedPreferences.getString("Asr", "N/A"))
        val maghrib = formatPrayerTimeTo12Hour(sharedPreferences.getString("Maghrib", "N/A"))
        val isha = formatPrayerTimeTo12Hour(sharedPreferences.getString("Isha", "N/A"))

        fajrTextView.text = fajr
        dhuhrTextView.text = dhuhr
        asrTextView.text = asr
        maghribTextView.text = maghrib
        ishaTextView.text = isha
    }

    private fun getPrayerTimes(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getPrayerTimes(latitude, longitude)

                if (response.isSuccessful && response.body() != null) {
                    val prayerTimes = response.body()?.data?.timings

                    with(sharedPreferences.edit()) {
                        putString("Fajr", prayerTimes?.Fajr)
                        putString("Dhuhr", prayerTimes?.Dhuhr)
                        putString("Asr", prayerTimes?.Asr)
                        putString("Maghrib", prayerTimes?.Maghrib)
                        putString("Isha", prayerTimes?.Isha)
                        apply()
                    }

                    withContext(Dispatchers.Main) {
                        fajrTextView.text = formatPrayerTimeTo12Hour(prayerTimes?.Fajr)
                        dhuhrTextView.text = formatPrayerTimeTo12Hour(prayerTimes?.Dhuhr)
                        asrTextView.text = formatPrayerTimeTo12Hour(prayerTimes?.Asr)
                        maghribTextView.text = formatPrayerTimeTo12Hour(prayerTimes?.Maghrib)
                        ishaTextView.text = formatPrayerTimeTo12Hour(prayerTimes?.Isha)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@NamazActivity, "", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NamazActivity, "", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setDates() {
        val currentDate = Calendar.getInstance()

        val englishDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        val englishDate = englishDateFormat.format(currentDate.time)
        englishDateTextView.text = englishDate

        val bengaliDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("bn", "BD"))
        val bengaliDate = bengaliDateFormat.format(currentDate.time)
        bengaliDateTextView.text = convertToBengaliNumerals(bengaliDate)
        val hijrahDate = HijrahDate.now() // Get the current Hijri date
        val hijriDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ar"))
        val hijriDate = hijrahDate.format(hijriDateFormatter)
        hijriDateTextView.text = convertToBengaliNumerals(hijriDate)
    }

    // Convert 24-hour format to 12-hour format and translate to Bengali numerals
    private fun formatPrayerTimeTo12Hour(time: String?): String {
        time?.let {
            val sdf = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val dateObj = sdf.parse(it)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            return convertToBengaliNumerals(outputFormat.format(dateObj!!))
        }
        return "N/A"
    }

    // Convert numbers in string to Bengali numerals
    private fun convertToBengaliNumerals(input: String): String {
        val englishNumbers = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val bengaliNumbers = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        var output = input
        englishNumbers.forEachIndexed { index, c ->
            output = output.replace(c, bengaliNumbers[index])
        }
        return output
    }
}
