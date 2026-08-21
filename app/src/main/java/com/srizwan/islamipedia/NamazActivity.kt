package com.srizwan.islamipedia

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.math.max

/**
 * NamazActivity - Islamic Encyclopedia & Al Hadith S2
 * Package: com.srizwan.islamipedia
 * No XML - 100% Programmatic UI
 * Data Source: jsDelivr CDN (PrayertimePedia)
 */
class NamazActivity : ComponentActivity() {

    companion object {
        const val CITIES_URL = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia/BangladeshCities.json"
        const val CITY_BASE = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia@main/BD/"
        const val PREF_CITY = "prayer_city"
        const val PREF_TRACKER = "salat_tracker_v2"
        const val PREF_NOTIF = "azan_notif"
        const val CHANNEL_ID = "azan_channel_s2"
    }

    data class City(val name_en: String, val name_bn: String, val division: String = "")
    data class PrayerInfo(val label_bn: String, val time: String, val time_bn: String, val label_en: String)
    data class TodayData(
        val city_en: String, val city_bn: String, val hijri: String, val bengaliDate: String,
        val prayerTimes: LinkedHashMap<String, PrayerInfo>,
        val forbiddenTimes: Map<String, JSONObject>,
        val naflTimes: Map<String, JSONObject>,
        val raw: JSONObject
    )

    private var selectedCity = City("Dhaka", "ঢাকা", "Dhaka")
    private var allCities = listOf<City>()
    private var todayData: TodayData? = null
    private var countdownJob: Job? = null
    private var notifEnabled = false
    private var lastNotifiedKey = ""

    // UI
    private lateinit var cityTv: TextView
    private lateinit var hijriTv: TextView
    private lateinit var bengTv: TextView
    private lateinit var countDownTv: TextView
    private lateinit var countSubTv: TextView
    private lateinit var bellBtn: TextView
    private lateinit var prayerBox: LinearLayout
    private lateinit var forbBox: LinearLayout
    private lateinit var naflBox: LinearLayout
    private lateinit var trackerBox: LinearLayout
    private lateinit var chartBox: LinearLayout
    private lateinit var chartSubTv: TextView
    private lateinit var loaderTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = getSharedPreferences("islamipedia", MODE_PRIVATE)
        sp.getString(PREF_CITY, null)?.let {
            try {
                val j = JSONObject(it)
                selectedCity = City(j.getString("name_en"), j.getString("name_bn"), j.optString("division"))
            } catch (_: Exception) {}
        }
        notifEnabled = sp.getBoolean(PREF_NOTIF, false)

        createChannel()
        buildUI()
        loadAll()
    }

    // ================= 100% PROGRAMMATIC UI =================
    private fun buildUI() {
        val root = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#FDFBF6"))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        val main = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // HERO - Masjid Design like web
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(28))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#102E26"), Color.parseColor("#0A201A"))).apply {
                cornerRadii = floatArrayOf(0f,0f,0f,0f, dp(36).toFloat(), dp(36).toFloat(), dp(36).toFloat(), dp(36).toFloat())
            }
        }

        val topBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val logoBox = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val mark = TextView(this).apply {
            text = "إ"; textSize = 18f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#0E3B2E"))
            background = GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#D4A017"), Color.parseColor("#FFF1A0"))); cornerRadius = dp(14).toFloat() }
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10),0,0,0) }
        brand.addView(TextView(this).apply { text = "ইসলামী বিশ্বকোষ"; setTextColor(Color.WHITE); textSize = 14f; typeface = Typeface.DEFAULT_BOLD })
        brand.addView(TextView(this).apply { text = "আল হাদিস S2 • Masjid Design"; setTextColor(Color.parseColor("#99FFFFFF")); textSize = 10f })
        logoBox.addView(mark); logoBox.addView(brand)

        cityTv = TextView(this).apply {
            text = "📍 ${selectedCity.name_bn}"; setTextColor(Color.WHITE); textSize = 12f
            setPadding(dp(13), dp(7), dp(13), dp(7))
            background = GradientDrawable().apply { setColor(Color.parseColor("#1AFFFFFF")); cornerRadius = dp(20).toFloat(); setStroke(1, Color.parseColor("#33FFFFFF")) }
            setOnClickListener { showCityPicker() }
        }
        topBar.addView(logoBox, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        topBar.addView(cityTv)

        hijriTv = TextView(this).apply { text = "হিজরি লোড হচ্ছে..."; setTextColor(Color.parseColor("#FFF6C8")); textSize = 15f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, dp(22),0,0) }
        bengTv = TextView(this).apply { text = ""; setTextColor(Color.parseColor("#AAFFFFFF")); textSize = 11f; gravity = Gravity.CENTER; setPadding(0, dp(4),0,0) }

        // Mosque line art (like web)
        val mosqueLine = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.BOTTOM; setPadding(dp(20), dp(18), dp(20),0) }
        fun minar(): View = View(this).apply { setBackgroundColor(Color.parseColor("#C9B78A")); layoutParams = LinearLayout.LayoutParams(dp(6), dp(44)).apply { setMargins(dp(8),0,dp(8),0) } }
        fun dome(w:Int,h:Int): View = View(this).apply { background = GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#FFE9A0"), Color.parseColor("#C9A227"))); cornerRadii = floatArrayOf(dp(w/2).toFloat(),dp(w/2).toFloat(),dp(w/2).toFloat(),dp(w/2).toFloat(), dp(4).toFloat(),dp(4).toFloat(),dp(4).toFloat(),dp(4).toFloat()) }; layoutParams = LinearLayout.LayoutParams(dp(w), dp(h)) }
        mosqueLine.addView(minar()); mosqueLine.addView(dome(28,18)); mosqueLine.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0,1,1f) }); mosqueLine.addView(dome(56,32)); mosqueLine.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0,1,1f) }); mosqueLine.addView(dome(28,18)); mosqueLine.addView(minar())

        val title = TextView(this).apply { text = "আজকের নামাজ"; setTextColor(Color.WHITE); textSize = 28f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, dp(12),0,0) }

        hero.addView(topBar); hero.addView(hijriTv); hero.addView(bengTv); hero.addView(mosqueLine); hero.addView(title)

        // COUNTDOWN CARD - Fixed bottom like web
        val countCard = card(Color.parseColor("#0E3B2E"), true)
        val countRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)) }
        val countIcon = TextView(this).apply {
            text = "⏰"; textSize = 20f; gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#D4A017"), Color.parseColor("#FFF1A0"))); cornerRadius = dp(14).toFloat() }
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        val countCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12),0,dp(8),0) }
        countDownTv = TextView(this).apply { text = "০০:০০:০০"; setTextColor(Color.WHITE); textSize = 22f; typeface = Typeface.DEFAULT_BOLD }
        countSubTv = TextView(this).apply { text = "লোড হচ্ছে..."; setTextColor(Color.parseColor("#A0D9C0")); textSize = 11f }
        bellBtn = TextView(this).apply {
            text = if(notifEnabled) "🔔" else "🔕"; textSize = 18f; gravity = Gravity.CENTER
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setColor(Color.parseColor("#1AFFFFFF")); cornerRadius = dp(12).toFloat() }
            setOnClickListener { toggleNotif() }
        }
        countCol.addView(countDownTv); countCol.addView(countSubTv)
        countRow.addView(countIcon); countRow.addView(countCol, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); countRow.addView(bellBtn)
        countCard.addView(countRow)

        // Cards
        val prayerCard = card(Color.WHITE)
        prayerCard.addView(header("🕌 আজকের ৫ ওয়াক্ত", "live"))
        loaderTv = TextView(this).apply { text = "CDN থেকে লোড হচ্ছে..."; setPadding(dp(16), dp(12), dp(16), dp(12)); textSize = 12f }
        prayerBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(6), dp(10), dp(10)) }
        prayerCard.addView(loaderTv); prayerCard.addView(prayerBox)

        val forbCard = card(Color.WHITE)
        forbCard.addView(header("⛔ নিষিদ্ধ সময়", "৩ টি"))
        forbBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10),0,dp(10),dp(10)) }
        forbCard.addView(forbBox)

        val naflCard = card(Color.WHITE)
        naflCard.addView(header("🌙 নফল ওয়াক্ত", ""))
        naflBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10),0,dp(10),dp(10)) }
        naflCard.addView(naflBox)

        val trackerCard = card(Color.WHITE)
        trackerCard.addView(header("✅ সালাত ট্র্যাকার", "local"))
        trackerBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(4), dp(8), dp(8)) }
        trackerCard.addView(trackerBox)

        val chartCard = card(Color.WHITE)
        chartCard.addView(header("📊 সাপ্তাহিক চার্ট", ""))
        chartSubTv = TextView(this).apply { text = ""; setPadding(dp(16),0,dp(16),dp(4)); textSize = 11f; setTextColor(Color.GRAY) }
        chartBox = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.BOTTOM; setPadding(dp(12), dp(10), dp(12), dp(12)) }
        chartCard.addView(chartSubTv); chartCard.addView(chartBox)

        main.addView(hero)
        main.addView(wrap(countCard))
        main.addView(wrap(prayerCard))
        main.addView(wrap(forbCard))
        main.addView(wrap(naflCard))
        main.addView(wrap(trackerCard))
        main.addView(wrap(chartCard))
        main.addView(View(this).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(100)) })

        root.addView(main)
        setContentView(root)
    }

    private fun wrap(v: View): View = LinearLayout(this).apply {
        setPadding(dp(14), dp(10), dp(14), 0)
        addView(v, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
    private fun card(bg: Int, dark: Boolean = false): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply {
            setColor(bg); cornerRadius = dp(24).toFloat()
            if(!dark) setStroke(1, Color.parseColor("#EFE5C8"))
        }
        if(!dark) elevation = dp(3).toFloat()
    }
    private fun header(title: String, badge: String): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(10))
        addView(TextView(context).apply { text = title; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.parseColor("#0E3B2E")) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        if(badge.isNotEmpty()) addView(TextView(context).apply {
            text = badge; textSize = 10f; setTextColor(Color.parseColor("#0E3B2E")); typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = GradientDrawable().apply { setColor(Color.parseColor("#FFF6C8")); cornerRadius = dp(12).toFloat(); setStroke(1, Color.parseColor("#E8D89A")) }
        })
    }
    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    // ================= DATA LOADING =================
    private fun loadAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val citiesText = URL(CITIES_URL).readText()
                val arr = org.json.JSONArray(citiesText)
                val list = mutableListOf<City>()
                for(i in 0 until arr.length()){
                    val o = arr.getJSONObject(i)
                    list.add(City(o.getString("name_en"), o.getString("name_bn"), o.optString("division","")))
                }
                allCities = list
                withContext(Dispatchers.Main){ cityTv.text = "📍 ${selectedCity.name_bn}" }
                loadCity(selectedCity.name_en)
            } catch (e: Exception){
                withContext(Dispatchers.Main){ loaderTv.text = "CDN Error: ${e.message}" }
            }
        }
    }

    private suspend fun loadCity(en: String){
        try {
            val jsonText = withContext(Dispatchers.IO){ URL(CITY_BASE + en + ".json").readText() }
            val root = JSONObject(jsonText)
            val todayObj = root.optJSONObject("today") ?: root

            val meta = todayObj.optJSONObject("meta") ?: JSONObject()
            val loc = meta.optJSONObject("location") ?: JSONObject()
            val hijri = todayObj.optString("hijri_date_bn", todayObj.optString("hijri",""))
            val beng = todayObj.optString("bengali_date_bn", todayObj.optString("date_bn",""))

            val prayers = LinkedHashMap<String, PrayerInfo>()
            val pt = todayObj.optJSONObject("prayer_times") ?: JSONObject()
            for(k in listOf("fajr","dhuhr","asr","maghrib","isha")){
                val o = pt.optJSONObject(k) ?: continue
                prayers[k] = PrayerInfo(o.optString("label_bn",k), o.optString("time", o.optString("time_24","")), o.optString("time_bn",""), o.optString("label_en",k))
            }

            val forb = mutableMapOf<String, JSONObject>()
            val ft = todayObj.optJSONObject("forbidden_times") ?: JSONObject()
            for(k in listOf("sunrise","noon","sunset")) ft.optJSONObject(k)?.let { forb[k]=it }

            val nafl = mutableMapOf<String, JSONObject>()
            val nt = todayObj.optJSONObject("nafl_times") ?: todayObj.optJSONObject("other_times") ?: JSONObject()
            val nKeys = nt.keys()
            while(nKeys.hasNext()){ val kk = nKeys.next(); nafl[kk]= nt.getJSONObject(kk) }

            todayData = TodayData(loc.optString("city",en), loc.optString("city_bn",selectedCity.name_bn), hijri, beng, prayers, forb, nafl, root)

            withContext(Dispatchers.Main){
                loaderTv.visibility = View.GONE
                render()
                startCountdown()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){ loaderTv.text = "ফাইল পাওয়া যায়নি: $en.json - ${e.message}" }
        }
    }

    // ================= RENDER =================
    private fun render(){
        val data = todayData ?: return
        hijriTv.text = data.hijri.ifEmpty { "আজকের তারিখ" }
        bengTv.text = data.bengaliDate

        // Prayer list
        prayerBox.removeAllViews()
        val (active, next) = getActiveNext()
        val icons = mapOf("fajr" to "🌙","dhuhr" to "☀️","asr" to "🌤️","maghrib" to "🌇","isha" to "🌌")
        data.prayerTimes.forEach { (k,v) ->
            val isActive = k==active
            val isNext = k==next
            prayerBox.addView(makePrayerRow(v.label_bn, v.time, v.time_bn, icons[k] ?: "🕌", isActive, isNext))
        }

        // Forbidden
        forbBox.removeAllViews()
        data.forbiddenTimes.forEach { (_, v) ->
            forbBox.addView(TextView(this).apply {
                text = "${v.optString("label_bn","নিষিদ্ধ")} • ${v.optString("time_bn", v.optString("time",""))} - ${v.optString("reason_bn","")}"
                setPadding(dp(12), dp(10), dp(12), dp(10))
                textSize = 11f
                background = GradientDrawable().apply { setColor(Color.parseColor("#FFFBEB")); cornerRadius = dp(12).toFloat(); setStroke(1, Color.parseColor("#F8E9B0")) }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, dp(4),0,dp(4)) }
            })
        }

        // Nafl
        naflBox.removeAllViews()
        val naflGrid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        data.naflTimes.entries.take(4).forEach { (_, v) ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(10))
                background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(14).toFloat(); setStroke(1, Color.parseColor("#EFE5C8")) }
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(4),0,dp(4),0) }
            }
            item.addView(TextView(this).apply { text = v.optString("label_bn","নফল").uppercase(); textSize = 9f; setTextColor(Color.GRAY) })
            item.addView(TextView(this).apply { text = v.optString("time_bn",""); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.parseColor("#0E3B2E")) })
            naflGrid.addView(item)
        }
        naflBox.addView(naflGrid)

        renderTracker(); renderChart()
    }

    private fun makePrayerRow(nameBn: String, time24: String, timeBn: String, icon: String, isActive: Boolean, isNext: Boolean): View {
        val bgColor = when { isActive -> Color.parseColor("#0E3B2E"); isNext -> Color.parseColor("#FFFEF6"); else -> Color.WHITE }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                setColor(bgColor); cornerRadius = dp(16).toFloat()
                setStroke(1, if(isNext) Color.parseColor("#C9A227") else Color.parseColor("#EFE5C8"))
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, dp(4),0,dp(4)) }
        }
        row.addView(TextView(this).apply { text = icon; textSize = 20f; setPadding(dp(4),0,dp(8),0) })
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply { text = nameBn; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(if(isActive) Color.WHITE else Color.BLACK) })
        col.addView(TextView(this).apply { text = timeBn; textSize = 10f; setTextColor(if(isActive) Color.parseColor("#A0D9C0") else Color.GRAY) })
        val timeTv = TextView(this).apply { text = time24; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(if(isActive) Color.parseColor("#FFF6C8") else Color.parseColor("#0E3B2E")) }

        val chk = CheckBox(this).apply {
            isChecked = isTracked(nameBn)
            setOnCheckedChangeListener { _, b -> toggleTrack(nameBn,b); renderTracker(); renderChart() }
        }

        row.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(timeTv)
        row.addView(chk)
        return row
    }

    // Tracker & Chart (same as web)
    private fun isTracked(bn: String): Boolean {
        val key = "${selectedCity.name_en}-${mapBnToEn(bn)}"
        val s = getSharedPreferences("islamipedia", MODE_PRIVATE).getString(PREF_TRACKER, "{}") ?: "{}"
        return try { JSONObject(s).has(key) } catch (_:Exception){ false }
    }
    private fun toggleTrack(bn: String, checked: Boolean){
        val sp = getSharedPreferences("islamipedia", MODE_PRIVATE)
        val obj = try { JSONObject(sp.getString(PREF_TRACKER, "{}") ?: "{}") } catch (_:Exception){ JSONObject() }
        val key = "${selectedCity.name_en}-${mapBnToEn(bn)}"
        if(checked) obj.put(key, System.currentTimeMillis()) else obj.remove(key)
        sp.edit().putString(PREF_TRACKER, obj.toString()).apply()
    }
    private fun mapBnToEn(bn: String) = when {
        bn.contains("ফজর") -> "fajr"
        bn.contains("যোহর") || bn.contains("যুহর") -> "dhuhr"
        bn.contains("আসর") -> "asr"
        bn.contains("মাগরিব") -> "maghrib"
        bn.contains("ইশা") -> "isha"
        else -> bn
    }
    private fun renderTracker(){
        trackerBox.removeAllViews()
        val s = getSharedPreferences("islamipedia", MODE_PRIVATE).getString(PREF_TRACKER, "{}") ?: "{}"
        val obj = try { JSONObject(s) } catch (_:Exception){ JSONObject() }
        var total = 0
        for(k in obj.keys()) if(k.toString().startsWith(selectedCity.name_en+"-")) total++
        trackerBox.addView(TextView(this).apply { text = "📿 এই মাসে ${toBn(total)} ওয়াক্ত আদায় • ${toBn(obj.length())} মোট"; setPadding(dp(12), dp(8), dp(12), dp(8)); textSize = 11f })
    }
    private fun renderChart(){
        chartBox.removeAllViews()
        val s = getSharedPreferences("islamipedia", MODE_PRIVATE).getString(PREF_TRACKER, "{}") ?: "{}"
        val obj = try { JSONObject(s) } catch (_:Exception){ JSONObject() }
        val per = mutableMapOf("fajr" to 0,"dhuhr" to 0,"asr" to 0,"maghrib" to 0,"isha" to 0)
        for(k in obj.keys()){
            val kk = k.toString()
            if(kk.startsWith(selectedCity.name_en+"-")){
                val wk = kk.substringAfterLast("-")
                if(per.containsKey(wk)) per[wk] = per[wk]!! + 1
            }
        }
        val maxV = per.values.maxOrNull() ?: 1
        chartSubTv.text = "${toBn(30)} দিনে • ${toBn(per.values.sum())} ওয়াক্ত"
        val labels = mapOf("fajr" to "ফজর","dhuhr" to "যুহর","asr" to "আসর","maghrib" to "মাগরিব","isha" to "ইশা")
        val icons = mapOf("fajr" to "🌙","dhuhr" to "☀️","asr" to "🌤️","maghrib" to "🌇","isha" to "🌌")
        per.forEach { (k,v) ->
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            val track = LinearLayout(this).apply { layoutParams = LinearLayout.LayoutParams(dp(32), dp(90)); background = GradientDrawable().apply { setColor(Color.parseColor("#FFF6C8")); cornerRadius = dp(8).toFloat() }; gravity = Gravity.BOTTOM }
            val h = max(10, v*70/maxV)
            val fill = View(this).apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(h)); background = GradientDrawable().apply { setColor(if(v*100/30 < 40) Color.parseColor("#E57373") else Color.parseColor("#0E3B2E")); cornerRadius = dp(8).toFloat() } }
            track.addView(fill)
            col.addView(track)
            col.addView(TextView(this).apply { text = "${icons[k]} ${labels[k]}"; textSize = 10f; gravity = Gravity.CENTER; setPadding(0, dp(6),0,0) })
            col.addView(TextView(this).apply { text = toBn(v).toString(); textSize = 11f; typeface = Typeface.DEFAULT_BOLD })
            chartBox.addView(col)
        }
    }

    // Countdown & Notification
    private fun getActiveNext(): Pair<String?, String?> {
        val data = todayData ?: return Pair(null,null)
        val cal = Calendar.getInstance()
        val nowM = cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE)
        var active: String? = null; var next: String? = null
        for(k in listOf("fajr","dhuhr","asr","maghrib","isha")){
            val t = data.prayerTimes[k]?.time ?: continue
            val m = parseMin(t)
            if(m <= nowM) active = k else if(next==null) next = k
        }
        if(active==null) next = data.prayerTimes.keys.firstOrNull()
        return Pair(active,next)
    }
    private fun startCountdown(){
        countdownJob?.cancel()
        countdownJob = lifecycleScope.launch {
            while(isActive){
                val data = todayData
                if(data != null){
                    val (active, next) = getActiveNext()
                    val cal = Calendar.getInstance()
                    val nowS = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + cal.get(Calendar.SECOND)
                    if(next != null){
                        val nextM = parseMin(data.prayerTimes[next]?.time ?: "00:00")
                        var diff = nextM*60 - nowS
                        if(diff<0) diff+= 24*3600
                        countDownTv.text = String.format("%02d:%02d:%02d", diff/3600, (diff%3600)/60, diff%60)
                        countSubTv.text = if(active!=null) "চলছে: ${data.prayerTimes[active]?.label_bn} • পরবর্তী: ${data.prayerTimes[next]?.label_bn}" else "পরবর্তী: ${data.prayerTimes[next]?.label_bn} - ${data.city_bn}"
                        checkAzan(active,next,diff)
                    }
                }
                delay(1000)
            }
        }
    }
    private fun checkAzan(active: String?, next: String?, diff: Int){
        if(!notifEnabled) return
        val data = todayData ?: return
        val cal = Calendar.getInstance()
        if(active==null && diff in 295..300){
            val k = "pre-$next-${cal.get(Calendar.DAY_OF_MONTH)}"
            if(lastNotifiedKey != k){ lastNotifiedKey = k; notify("পরবর্তী: ${data.prayerTimes[next]?.label_bn}", "৫ মিনিট বাকি • ${data.city_bn}"); beep() }
        }
        if(active==null && diff <= 2){
            val k = "exact-$next-${cal.get(Calendar.DAY_OF_MONTH)}-${cal.get(Calendar.HOUR_OF_DAY)}"
            if(lastNotifiedKey != k){ lastNotifiedKey = k; notify("${data.prayerTimes[next]?.label_bn} এর সময় হয়েছে", "${data.city_bn}"); beep() }
        }
    }
    private fun toggleNotif(){
        if(!notifEnabled){
            if(Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101); return
            }
            notifEnabled = true; bellBtn.text = "🔔"
        } else { notifEnabled = false; bellBtn.text = "🔕" }
        getSharedPreferences("islamipedia", MODE_PRIVATE).edit().putBoolean(PREF_NOTIF, notifEnabled).apply()
        Toast.makeText(this, if(notifEnabled) "আজান নোটিফিকেশন চালু" else "নোটিফিকেশন বন্ধ", Toast.LENGTH_SHORT).show()
    }
    private fun notify(t: String, b: String){
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(t).setContentText(b).setPriority(NotificationCompat.PRIORITY_HIGH).build()
        nm.notify((System.currentTimeMillis()%10000).toInt(), n)
    }
    private fun createChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val ch = NotificationChannel(CHANNEL_ID, "Azan S2", NotificationManager.IMPORTANCE_HIGH)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }
    private fun beep(){ try { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800) } catch(_:Exception){} }

    private fun showCityPicker(){
        if(allCities.isEmpty()){ Toast.makeText(this, "লিস্ট লোড হচ্ছে...", Toast.LENGTH_SHORT).show(); return }
        val names = allCities.map { "${it.name_bn} - ${it.division}" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("শহর নির্বাচন").setItems(names){ _, w ->
            selectedCity = allCities[w]
            getSharedPreferences("islamipedia", MODE_PRIVATE).edit().putString(PREF_CITY, JSONObject().apply { put("name_en", selectedCity.name_en); put("name_bn", selectedCity.name_bn); put("division", selectedCity.division) }.toString()).apply()
            cityTv.text = "📍 ${selectedCity.name_bn}"
            loaderTv.visibility = View.VISIBLE
            lifecycleScope.launch { loadCity(selectedCity.name_en) }
        }.show()
    }

    private fun parseMin(t: String): Int { return try { val p = t.trim().split(":"); p[0].toInt()*60 + p[1].substring(0,2).toInt() } catch(_:Exception){ 0 } }
    private fun toBn(n: Int): String { val en="0123456789"; val bn="০১২৩৪৫৬৭৮৯"; return n.toString().map { c -> if(c in '0'..'9') bn[en.indexOf(c)] else c }.joinToString("") }
}
