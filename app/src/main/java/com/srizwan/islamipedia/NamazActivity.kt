package com.srizwan.islamipedia

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class NamazActivity : AppCompatActivity() {

    // ---------- Colors from CSS ----------
    private val colBg = Color.parseColor("#FDFBF6")
    private val colGreen = Color.parseColor("#0E3B2E")
    private val colGreen2 = Color.parseColor("#153E32")
    private val colGold = Color.parseColor("#C9A227")
    private val colGoldL = Color.parseColor("#FFF6C8")
    private val colLine = Color.parseColor("#EFE5C8")
    private val colMuted = Color.parseColor("#9B9B93")
    private val colText = Color.parseColor("#1D1D1B")
    private val colCard = Color.WHITE

    // ---------- UI Refs ----------
    private lateinit var tvCurrentCityBn: TextView
    private lateinit var tvHijri: TextView
    private lateinit var tvBengali: TextView
    private lateinit var todayBadge: TextView
    private lateinit var prayerListContainer: LinearLayout
    private lateinit var forbiddenListContainer: LinearLayout
    private lateinit var naflGrid: GridLayout
    private lateinit var trackerMonthBadge: TextView
    private lateinit var trackerCountBadge: TextView
    private lateinit var summaryBox: LinearLayout
    private lateinit var chartBars: LinearLayout
    private lateinit var chartSub: TextView
    private lateinit var chipContainer: LinearLayout
    private lateinit var dayListGrid: GridLayout
    private lateinit var countIcon: TextView
    private lateinit var countLabel: TextView
    private lateinit var countPray: TextView
    private lateinit var countCity: TextView
    private lateinit var tvH: TextView
    private lateinit var tvM: TextView
    private lateinit var tvS: TextView
    private lateinit var bellBtn: TextView
    private lateinit var modalOverlay: FrameLayout
    private lateinit var citySearchEt: EditText
    private lateinit var cityListContainer: LinearLayout
    private lateinit var toastView: LinearLayout
    private lateinit var toastMsg: TextView
    private lateinit var countCard: MaterialCardView
    private lateinit var prefs: SharedPreferences

    // ---------- Data ----------
    private val CITIES_URL = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia/BangladeshCities.json"
    private val CITY_BASE = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia@main/BD/"
    private var allCities = mutableListOf<City>()
    private var selectedCity: City = City("Dhaka", "ঢাকা", "Dhaka")
    private var tempSelected: City? = null
    private var allMonth = mutableListOf<JSONObject>()
    private var todayData: JSONObject? = null
    private var filter = "all"
    private val prayerOrder = listOf(
        PrayerMeta("fajr", "🌙"), PrayerMeta("dhuhr", "☀"),
        PrayerMeta("asr", "🌤"), PrayerMeta("maghrib", "🌇"), PrayerMeta("isha", "🌌")
    )
    private var countdownHandler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null
    private var notifEnabled = false
    private var lastNotifiedKey = ""

    data class City(val name_en: String, val name_bn: String, val division: String = "")
    data class PrayerMeta(val k: String, val ic: String)

    // ---------- Utils ----------
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun dpF(v: Float) = v * resources.displayMetrics.density
    private fun toBn(n: String): String {
        val map = mapOf('0' to '০','1' to '১','2' to '২','3' to '৩','4' to '৪','5' to '৫','6' to '৬','7' to '৭','8' to '৮','9' to '৯')
        return n.map { map[it]?: it }.joinToString("")
    }
    private fun toBnNum(n: Int) = toBn(n.toString())

    // --- Font Loading from Internet (Google Fonts) ---
// --- Font Loading from Internet (No R.array needed) ---
private fun loadGoogleFont(fontName: String, onResult: (Typeface?) -> Unit) {
    lifecycleScope.launch(Dispatchers.IO) {
        val url = if (fontName.contains("Hind", true)) {
            "https://github.com/google/fonts/raw/main/ofl/hindsiliguri/HindSiliguri-Regular.ttf"
        } else {
            "https://github.com/google/fonts/raw/main/ofl/anekbangla/AnekBangla-Regular.ttf"
        }
        val tf = downloadFontDirect(url)
        withContext(Dispatchers.Main) { onResult(tf) }
    }
}

private fun downloadFontDirect(urlStr: String): Typeface? {
    return try {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.connect()
        val input = conn.inputStream
        val file = java.io.File.createTempFile("font", ".ttf", cacheDir)
        file.outputStream().use { input.copyTo(it) }
        Typeface.createFromFile(file)
    } catch (e: Exception) { 
        e.printStackTrace()
        null 
    }
}
    // --- Network ---
    private suspend fun fetchJson(url: String): String = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.setRequestProperty("Cache-Control", "no-cache")
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.inputStream.bufferedReader().readText()
    }

    // --- Time Parsing (Same logic as HTML JS) ---
    private fun parseM(t: String?, k: String?): Int? {
        if (t == null) return null
        val parts = t.split(":"); if (parts.size < 2) return null
        var h = parts[0].toIntOrNull()?: return null
        val m = parts[1].toIntOrNull()?: 0
        if (k!= null) {
            if (k == "asr" || k == "maghrib" || k == "isha") { if (h < 12) h += 12 }
            else if (k == "fajr") { if (h >= 12) h -= 12 }
        }
        return h * 60 + m
    }
    private fun parseMEnd(t: String?, k: String?, startM: Int?): Int? {
        if (t == null) return null
        val parts = t.split(":"); if (parts.size < 2) return null
        var h = parts[0].toIntOrNull()?: return null
        var m = parts[1].toIntOrNull()?: 0
        var endM: Int
        if (k == "dhuhr" || k == "asr" || k == "maghrib" || k == "isha") { if (h < 12) h += 12 }
        endM = h * 60 + m
        if (startM!= null && endM <= startM) endM += 12 * 60
        return endM
    }
    private fun parseMGeneric(t: String?): Int? {
        if (t == null) return null
        val p = t.split(":"); val h = p[0].toIntOrNull()?: return null; val m = p.getOrNull(1)?.toIntOrNull()?: 0
        return h * 60 + m
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("namaz_prefs", MODE_PRIVATE)
        notifEnabled = prefs.getBoolean("azan_notif", false)
        // Load saved city
        prefs.getString("prayer_city_json", null)?.let {
            try {
                val o = JSONObject(it)
                selectedCity = City(o.getString("name_en"), o.getString("name_bn"), o.optString("division"))
            } catch (_: Exception) {}
        }
        tempSelected = selectedCity

        // Root Frame for overlay
        val rootFrame = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(colBg)
        }

        // ScrollView
        val scrollView = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            isVerticalScrollBarEnabled = false
        }
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(0,0,0, dp(130))
        }

        // ----- Build UI -----
        mainContainer.addView(buildHeroSection())
        mainContainer.addView(buildContentSection())

        scrollView.addView(mainContainer)
        rootFrame.addView(scrollView)

        // Count Card (Fixed Bottom)
        countCard = buildCountCard()
        val countParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
            setMargins(dp(12),0,dp(12),dp(12))
        }
        rootFrame.addView(countCard, countParams)

        // Toast
        toastView = buildToast()
        val toastParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = dp(16)
        }
        rootFrame.addView(toastView, toastParams)

        // Modal
        modalOverlay = buildCityModal()
        rootFrame.addView(modalOverlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        setContentView(rootFrame)

        // Load fonts from internet
        loadGoogleFont("Hind Siliguri") { tf ->
            tf?.let { applyFontToAll(rootFrame, it) }
        }

        // Init Notifications Channel
        createNotificationChannel()
        // Fetch Data
        lifecycleScope.launch {
            try { loadCities() } catch (_: Exception) {}
            try { loadCityData(selectedCity.name_en) } catch (e: Exception) {
                try { loadCityData("Dhaka") } catch (_: Exception) {}
            }
        }
    }

    private fun applyFontToAll(view: View, tf: Typeface) {
        if (view is TextView) { view.typeface = tf }
        if (view is ViewGroup) { for (i in 0 until view.childCount) applyFontToAll(view.getChildAt(i), tf) }
    }

    // ---------------- UI Builders ----------------
    private fun buildHeroSection(): LinearLayout {
        // Gradient background
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#102E26"), Color.parseColor("#0A201A"))).apply {
                cornerRadii = floatArrayOf(0f,0f,0f,0f, dpF(36f), dpF(36f), dpF(36f), dpF(36f))
            }
            setPadding(dp(16), dp(14), dp(16), dp(20))
        }

        // Top row
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
        }
        // Brand
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mark = TextView(this).apply {
            text = "﷽"
            gravity = Gravity.CENTER
            setTextColor(colGreen)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#D4A017"), Color.parseColor("#FFF1A0"))).apply {
                cornerRadius = dpF(14f)
            }
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        }
        val brandText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10),0,0,0)
        }
        val b1 = TextView(this).apply { text = "ইসলামী বিশ্বকোষ ও আল হাদিস S2"; setTextColor(Color.WHITE); textSize = 14f; setTypeface(null, Typeface.BOLD) }
        val b2 = TextView(this).apply { text = "নামাজের সময়সূচি • Masjid Edition"; setTextColor(Color.WHITE); alpha = 0.7f; textSize = 11f }
        brandText.addView(b1); brandText.addView(b2)
        brand.addView(mark); brand.addView(brandText)

        // City selector
        tvCurrentCityBn = TextView(this).apply {
            text = selectedCity.name_bn
            setTextColor(Color.WHITE); textSize = 13f
            setPadding(dp(13), dp(7), dp(13), dp(7))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1AFFFFFF")); cornerRadius = dpF(99f)
                setStroke(dp(1), Color.parseColor("#2DFFFFFF"))
            }
            setOnClickListener { openModal() }
        }
        // update text later with 📍 ▼
        tvCurrentCityBn.text = "📍 ${selectedCity.name_bn} ▼"

        topRow.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        topRow.addView(tvCurrentCityBn)

        // Center
        tvHijri = TextView(this).apply { text = "লোড হচ্ছে..."; setTextColor(Color.parseColor("#FFF6C8")); textSize = 15f; setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER; setPadding(0, dp(18),0,0) }
        tvBengali = TextView(this).apply { text = ""; setTextColor(Color.WHITE); alpha = 0.8f; textSize = 12f; gravity = Gravity.CENTER; setPadding(0, dp(4),0,0) }

        // Mosque line
        val mosqueLine = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply { topMargin = dp(18) }
        }
        mosqueLine.addView(createMinar())
        mosqueLine.addView(createMiniDome())
        mosqueLine.addView(createDome())
        mosqueLine.addView(createMiniDome())
        mosqueLine.addView(createMinar())

        val title = TextView(this).apply {
            text = "নামাজের সময়সূচি"
            setTextColor(Color.WHITE); textSize = 26f; setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(18),0,0)
        }

        hero.addView(topRow); hero.addView(tvHijri); hero.addView(tvBengali); hero.addView(mosqueLine); hero.addView(title)
        return hero
    }

    private fun createDome(): View {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(32)).apply { setMargins(dp(12),0,dp(12),0) }
            background = GradientDrawable().apply {
                setColors(intArrayOf(Color.parseColor("#FFE9A0"), colGold))
                cornerRadii = floatArrayOf(dpF(28f),dpF(28f),dpF(28f),dpF(28f),dpF(6f),dpF(6f),dpF(6f),dpF(6f))
            }
        }
    }
    private fun createMiniDome(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(28), dp(18)).apply { setMargins(dp(8),0,dp(8),0) }
            background = GradientDrawable().apply {
                setColors(intArrayOf(Color.parseColor("#FFE9A0"), colGold))
                cornerRadii = floatArrayOf(dpF(14f),dpF(14f),dpF(14f),dpF(14f),dpF(4f),dpF(4f),dpF(4f),dpF(4f))
                alpha = 230
            }
        }
    }
    private fun createMinar(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(6), dp(44)).apply { setMargins(dp(8),0,dp(8),0) }
            background = GradientDrawable().apply {
                setColors(intArrayOf(Color.parseColor("#E8E0C0"), Color.parseColor("#C9B78A")))
                cornerRadius = dpF(3f)
            }
        }
    }

    private fun buildContentSection(): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        // Prayer Card
        val prayerCard = createCard()
        prayerCard.addView(createCardHeader("আজকের ওয়াক্ত", todayBadgeText = "আজ").also { todayBadge = it.findViewWithTag("badge1") })
        prayerListContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(10)) }
        prayerListContainer.addView(TextView(this).apply { text = "লোড হচ্ছে..."; setTextColor(colMuted); setPadding(dp(10), dp(30), dp(10), dp(30)); gravity = Gravity.CENTER })
        prayerCard.addView(prayerListContainer)

        // Side Cards container
        val sideContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(16) } }

        // Forbidden
        val forbiddenCard = createCard()
        forbiddenCard.addView(createCardHeader("নামাজের নিষিদ্ধ সময়সূচী"))
        forbiddenListContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(8), dp(8), dp(8)) }
        forbiddenCard.addView(forbiddenListContainer)
        sideContainer.addView(forbiddenCard)

        // Nafl
        val naflCard = createCard().apply { layoutParams = (layoutParams as LinearLayout.LayoutParams).apply { topMargin = dp(16) } }
        naflCard.addView(createCardHeader("নফল নামাজের সময়সূচী"))
        naflGrid = GridLayout(this).apply { columnCount = 2; rowCount = 2; setPadding(dp(10), dp(10), dp(10), dp(10)) }
        naflCard.addView(naflGrid)
        sideContainer.addView(naflCard)

        // Tracker Card
        val trackerCard = createCard().apply { layoutParams = (layoutParams as LinearLayout.LayoutParams).apply { topMargin = dp(16) } }
        val trackerHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = GradientDrawable().apply { setColor(Color.TRANSPARENT) }
        }
        val trackerTitle = TextView(this).apply { text = "সম্পূর্ণ মাসের নামাজের ট্র্যাকার"; setTextColor(colText); textSize = 14f; setTypeface(null, Typeface.BOLD); layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        trackerMonthBadge = createBadge("—")
        trackerCountBadge = createBadge("0/0").apply {
            background = GradientDrawable().apply { setColor(Color.parseColor("#ECFDF5")); cornerRadius = dpF(99f); setStroke(dp(1), colLine) }
        }
        val badgeRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END }
        badgeRow.addView(trackerMonthBadge, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,dp(6),0) })
        badgeRow.addView(trackerCountBadge)
        trackerHeader.addView(trackerTitle); trackerHeader.addView(badgeRow)

        // Summary horizontal scroll
        val summaryScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        summaryBox = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(12), dp(10), dp(12), dp(10)) }
        summaryScroll.addView(summaryBox)

        // Chart
        val chartWrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); setBackgroundColor(Color.parseColor("#FFFEF6")) }
        val chartTitleRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        chartTitleRow.addView(TextView(this).apply { text = "📊 এই মাসের অগ্রগতি চার্ট"; textSize = 13f; setTypeface(null, Typeface.BOLD); layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
        chartSub = TextView(this).apply { text = "৫ ওয়াক্ত ভিত্তিক"; textSize = 10f; setTextColor(colMuted) }
        chartTitleRow.addView(chartSub)
        chartBars = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.BOTTOM; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(90)).apply { topMargin = dp(10) } }
        chartWrap.addView(chartTitleRow); chartWrap.addView(chartBars)

        // Chips
        val chipScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; setPadding(dp(12), dp(12), dp(12), dp(12)) }
        chipContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val chips = listOf("all" to "সব ওয়াক্ত","fajr" to "🌙 ফজর","dhuhr" to "☀ যুহর","asr" to "🌤 আসর","maghrib" to "🌇 মাগরিব","isha" to "🌌 ইশা")
        chips.forEachIndexed { idx, (k,lbl) ->
            val chip = createChip(lbl, k == "all")
            chip.setOnClickListener {
                for (i in 0 until chipContainer.childCount) (chipContainer.getChildAt(i) as TextView).isSelected = false
                chip.isSelected = true
                filter = k
                renderDays()
            }
            chip.tag = k
            chipContainer.addView(chip, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,dp(8),0) })
        }
        chipScroll.addView(chipContainer)

        // Day grid
        dayListGrid = GridLayout(this).apply {
            columnCount = 2
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        trackerCard.addView(trackerHeader)
        trackerCard.addView(summaryScroll)
        trackerCard.addView(chartWrap)
        trackerCard.addView(chipScroll)
        trackerCard.addView(dayListGrid)

        container.addView(prayerCard)
        container.addView(sideContainer)
        container.addView(trackerCard)

        return container
    }

    private fun createCard(): LinearLayout {
        val card = MaterialCardView(this).apply {
            radius = dpF(24f)
            cardElevation = dpF(12f)
            strokeWidth = dp(1)
            strokeColor = colLine
            setCardBackgroundColor(colCard)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) }
        card.addView(inner)
        // return inner but keep card wrapper - hack: use tag
        inner.tag = card
        // Actually we need wrapper; create a LinearLayout that contains card
        val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        wrapper.addView(card)
        // For ease, return inner and wrapper is parent. We'll return inner but add card elsewhere? Simpler: return inner and add wrapper to container via trick.
        // To keep API simple, we create a container LinearLayout that IS the card's inner
        return inner
    }

    private fun createCardHeader(title: String, todayBadgeText: String? = null): LinearLayout {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = GradientDrawable().apply { setColor(Color.TRANSPARENT) }
        }
        val tv = TextView(this).apply { text = title; textSize = 15f; setTypeface(null, Typeface.BOLD); setTextColor(colText); layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        header.addView(tv)
        if (todayBadgeText!= null) {
            val badge = createBadge(todayBadgeText)
            badge.tag = "badge1"
            header.addView(badge)
        }
        // bottom dashed line simulation
        header.setBackgroundColor(Color.TRANSPARENT)
        return header
    }

    private fun createBadge(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 10f
            setTextColor(colGreen)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = GradientDrawable().apply {
                setColor(colGoldL); cornerRadius = dpF(99f); setStroke(dp(1), Color.parseColor("#E8D89A"))
            }
        }
    }

    private fun createChip(text: String, active: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(12), dp(6), dp(12), dp(6))
            isSelected = active
            updateChipStyle()
            setOnClickListener { updateChipStyle() }
            addOnLayoutChangeListener { v,_,_,_,_,_,_,_,_ -> (v as TextView).updateChipStyle() }
        }
    }

    private fun TextView.updateChipStyle() {
        if (isSelected) {
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(colGreen); cornerRadius = dpF(99f) }
        } else {
            setTextColor(colText)
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(99f); setStroke(dp(1), colLine) }
        }
    }

    private fun buildCountCard(): MaterialCardView {
        val card = MaterialCardView(this).apply {
            radius = dpF(20f)
            cardElevation = dpF(18f)
            setCardBackgroundColor(Color.parseColor("#F20E3B2E")) // 97% opacity
            strokeWidth = dp(1); strokeColor = Color.parseColor("#24FFFFFF")
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)) }

        countIcon = TextView(this).apply {
            text = "🕌"; textSize = 20f; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#D4A017"), Color.parseColor("#FFF1A0"))).apply { cornerRadius = dpF(14f) }
        }
        val mid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(12),0,dp(12),0) } }
        countLabel = TextView(this).apply { text = "পরবর্তী নামাজ"; textSize = 9f; setTextColor(Color.WHITE); alpha = 0.7f }
        countPray = TextView(this).apply { text = "যুহর"; textSize = 15f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD) }
        countCity = TextView(this).apply { text = "ঢাকা"; textSize = 10f; setTextColor(Color.WHITE); alpha = 0.7f }
        mid.addView(countLabel); mid.addView(countPray); mid.addView(countCity)

        val timeRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        tvH = TextView(this).apply { text = "০০"; setTextColor(Color.WHITE); textSize = 18f; setTypeface(null, Typeface.BOLD) }
        tvM = TextView(this).apply { text = "০০"; setTextColor(Color.WHITE); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(dp(8),0,0,0) }
        tvS = TextView(this).apply { text = "০০"; setTextColor(Color.WHITE); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(dp(8),0,0,0) }
        timeRow.addView(tvH); timeRow.addView(TextView(this).apply { text = " ঘ"; setTextColor(Color.WHITE); textSize = 10f }); timeRow.addView(tvM); timeRow.addView(TextView(this).apply { text = " মি"; setTextColor(Color.WHITE); textSize = 10f }); timeRow.addView(tvS); timeRow.addView(TextView(this).apply { text = " সে"; setTextColor(Color.WHITE); textSize = 10f })

        bellBtn = TextView(this).apply {
            text = if (notifEnabled) "🔔" else "🔕"
            textSize = 16f; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36)).apply { setMargins(dp(8),0,0,0) }
            background = GradientDrawable().apply {
                cornerRadius = dpF(10f); setStroke(dp(1), Color.parseColor("#33FFFFFF")); setColor(Color.parseColor("#1AFFFFFF"))
            }
            setTextColor(Color.WHITE)
            setOnClickListener { toggleNotification() }
        }

        row.addView(countIcon); row.addView(mid); row.addView(timeRow); row.addView(bellBtn)
        card.addView(row)
        return card
    }

    private fun buildToast(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            visibility = View.GONE
            background = GradientDrawable().apply { setColor(colGreen); cornerRadius = dpF(99f) }
            elevation = dpF(12f)
        }.also {
            val ic = TextView(this).apply { text = "🔔"; setTextColor(Color.WHITE) }
            toastMsg = TextView(this).apply { text = "আজান"; setTextColor(Color.WHITE); textSize = 13f; setPadding(dp(10),0,0,0) }
            it.addView(ic); it.addView(toastMsg)
        }
    }

    private fun buildCityModal(): FrameLayout {
        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#8C0A1415"))
            visibility = View.GONE
        }
        val modal = MaterialCardView(this).apply {
            radius = dpF(22f); cardElevation = dpF(24f)
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER; setMargins(dp(14), dp(14), dp(14), dp(14))
            }
            setCardBackgroundColor(Color.parseColor("#FFFEFB"))
        }
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // Header
        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16),0) }
        header.addView(TextView(this).apply { text = "স্থান শনাক্ত করুন"; textSize = 17f; setTypeface(null, Typeface.BOLD) })
        header.addView(TextView(this).apply { text = "নামাজের সময় এবং চাঁদের তারিখ দেখাতে আপনার অবস্থান জানা জরুরি।"; textSize = 12f; setTextColor(colMuted); setPadding(0, dp(4),0,0) })

        // Search
        val searchWrap = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(12), dp(16),0) }
        val searchBox = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(12f); setStroke(dp(1), colLine) }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dp(11), dp(11), dp(11), dp(11))
        }
        citySearchEt = EditText(this).apply {
            hint = "শহর নির্বাচন করুন - যেমন ঢাকা"; textSize = 13f; background = null
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        searchBox.addView(TextView(this).apply { text = "🔍"; setPadding(0,0,dp(6),0) }); searchBox.addView(citySearchEt)
        searchWrap.addView(searchBox)

        // List
        val scroll = ScrollView(this).apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(300)).apply { topMargin = dp(8) } }
        cityListContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(8), dp(10), dp(8)) }
        scroll.addView(cityListContainer)

        // Footer
        val footer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(16), dp(10), dp(16), dp(10)) }
        val cancel = TextView(this).apply {
            text = "বাতিল"; gravity = Gravity.CENTER; textSize = 13f; setTypeface(null, Typeface.BOLD)
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(11f); setStroke(dp(1), colLine) }
            layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f).apply { setMargins(0,0,dp(8),0) }
            setPadding(dp(10),0,dp(10),0)
            setOnClickListener { closeModal() }
        }
        val save = TextView(this).apply {
            text = "সংরক্ষণ"; gravity = Gravity.CENTER; textSize = 13f; setTypeface(null, Typeface.BOLD); setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(colGreen); cornerRadius = dpF(11f) }
            layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f)
            setOnClickListener { saveCity() }
        }
        footer.addView(cancel); footer.addView(save)

        inner.addView(header); inner.addView(searchWrap); inner.addView(scroll); inner.addView(footer)
        modal.addView(inner)
        overlay.addView(modal)

        overlay.setOnClickListener { if (it == overlay) closeModal() }
        citySearchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { renderCityListFiltered() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        return overlay
    }

    // ---------- City Logic ----------
    private suspend fun loadCities() {
        try {
            val json = fetchJson(CITIES_URL)
            val arr = JSONArray(json)
            allCities.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                allCities.add(City(o.getString("name_en"), o.getString("name_bn"), o.optString("division","")))
            }
        } catch (_: Exception) {
            if (allCities.isEmpty()) allCities.add(City("Dhaka","ঢাকা","Dhaka"))
        }
        withContext(Dispatchers.Main) { renderCityListFiltered() }
    }

    private fun renderCityListFiltered() {
        val q = citySearchEt.text.toString().trim()
        val list = if (q.isEmpty()) allCities else allCities.filter { it.name_bn.contains(q) || it.name_en.lowercase().contains(q.lowercase()) }
        cityListContainer.removeAllViews()
        list.forEach { c ->
            val isSel = tempSelected?.name_en == c.name_en
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(11), dp(10), dp(11), dp(10))
                background = if (isSel) GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#FFF6C0"), Color.parseColor("#EAF7EF"))); cornerRadius = dpF(12f); setStroke(dp(1), Color.parseColor("#E6D688")) } else GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(12f) }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,dp(5)) }
                setOnClickListener { tempSelected = c; renderCityListFiltered() }
            }
            val left = TextView(this).apply { text = "${c.name_bn} ${c.division}"; textSize = 13f; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            val right = TextView(this).apply { text = if (isSel) "✓" else ""; textSize = 13f }
            row.addView(left); row.addView(right)
            cityListContainer.addView(row)
        }
    }

    private suspend fun loadCityData(en: String) {
        val url = CITY_BASE + en + ".json?v=" + System.currentTimeMillis()
        val json = fetchJson(url)
        val arr = JSONArray(json)
        allMonth.clear()
        for (i in 0 until arr.length()) allMonth.add(arr.getJSONObject(i))
        val today = Calendar.getInstance()
        val idx = max(0, minOf(today.get(Calendar.DAY_OF_MONTH)-1, allMonth.size-1))
        todayData = allMonth.getOrNull(idx)?: allMonth.firstOrNull()
        withContext(Dispatchers.Main) { renderAll() }
    }

    // ---------- Render ----------
    private fun renderAll() {
        val td = todayData?: return
        tvCurrentCityBn.text = "📍 ${td.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?: selectedCity.name_bn} ▼"
        val hijri = td.optJSONObject("date")?.optJSONObject("hijri")?.optString("bn")?: ""
        val fullBn = td.optJSONObject("date")?.optJSONObject("full")?.optString("bn")?: ""
        tvHijri.text = hijri.ifEmpty { fullBn.split("•").firstOrNull()?: "" }
        tvBengali.text = td.optJSONObject("date")?.optJSONObject("bengali")?.optString("bn")?: fullBn.split("•").getOrNull(1)?: ""
        todayBadge.text = td.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?: selectedCity.name_bn
        trackerMonthBadge.text = fullBn

        renderPrayerList()
        renderForbidden()
        renderNafl()
        renderDays()
        startCountdown()
    }

    private fun renderPrayerList() {
        prayerListContainer.removeAllViews()
        val td = todayData?: return
        val prayerTimes = td.optJSONObject("prayer_times")?: return
        val info = getNextPrayerInfo()
        prayerOrder.forEach { meta ->
            val pt = prayerTimes.optJSONObject(meta.k)?: return@forEach
            val isActive = info.active == meta.k
            val isNext = info.next?.k == meta.k &&!isActive
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,dp(8)) }
                background = if (isActive) GradientDrawable().apply { setColors(intArrayOf(colGreen, Color.parseColor("#1B4A3A"))); cornerRadius = dpF(16f) }
                else if (isNext) GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#FFFEF6"), Color.parseColor("#FFF3B8"))); cornerRadius = dpF(16f); setStroke(dp(1), colGold) }
                else GradientDrawable().apply { setColors(intArrayOf(Color.WHITE, Color.parseColor("#FFFCF0"))); cornerRadius = dpF(16f); setStroke(dp(1), colLine) }
            }
            val icon = TextView(this).apply {
                text = meta.ic; textSize = 22f; gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
                background = GradientDrawable().apply {
                    setColor(if (isActive) Color.parseColor("#1FFFFFFF") else Color.parseColor("#FAF6E8"))
                    cornerRadius = dpF(14f); setStroke(dp(1), if (isActive) Color.parseColor("#33FFFFFF") else colLine)
                }
            }
            val nameTv = TextView(this).apply {
                text = pt.optString("label_bn", meta.k); textSize = 15f; setTypeface(null, Typeface.BOLD)
                setTextColor(if (isActive) Color.WHITE else colText)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(12),0,0,0) }
            }
            val timeTv = TextView(this).apply {
                text = pt.optString("time_bn",""); textSize = 15f; setTypeface(null, Typeface.BOLD)
                setTextColor(if (isActive) colGoldL else colText)
            }
            item.addView(icon); item.addView(nameTv); item.addView(timeTv)
            prayerListContainer.addView(item)
        }
    }

    private fun renderForbidden() {
        forbiddenListContainer.removeAllViews()
        val ft = todayData?.optJSONObject("forbidden_times")?: return
        listOf("sunrise","noon","sunset").forEach { k ->
            val d = ft.optJSONObject(k)?: return@forEach
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                background = GradientDrawable().apply { setColor(Color.parseColor("#FFFBEB")); cornerRadius = dpF(12f); setStroke(dp(1), Color.parseColor("#F8E9B0")) }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,dp(6)) }
            }
            row.addView(TextView(this).apply { text = "🚫 ${d.optString("label_bn", k)}"; textSize = 12f; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
            row.addView(TextView(this).apply { text = d.optString("time_bn",""); textSize = 12f; setTypeface(null, Typeface.BOLD) })
            forbiddenListContainer.addView(row)
        }
    }

    private fun renderNafl() {
        naflGrid.removeAllViews()
        val nafl = todayData?.optJSONObject("nafl_times")?: JSONObject()
        val items = listOf(
            "তাহাজ্জুদ" to (nafl.optJSONObject("tahajjud")?.optString("time_bn")?: "-"),
            "সাহরী শেষ" to (nafl.optJSONObject("tahajjud")?.optString("time_bn")?: todayData?.optJSONObject("prayer_times")?.optJSONObject("fajr")?.optString("time_bn")?.split(" - ")?.firstOrNull()?: "-"),
            "ইশরাক" to (nafl.optJSONObject("ishraq")?.optString("time_bn")?: "-"),
            "চাশত" to (nafl.optJSONObject("chasht")?.optString("time_bn")?: "-")
        )
        items.forEach { (label, time) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(dp(11), dp(11), dp(11), dp(11))
                background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(14f); setStroke(dp(1), colLine) }
                layoutParams = GridLayout.LayoutParams().apply { width = 0; columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); setMargins(dp(4), dp(4), dp(4), dp(4)) }
            }
            card.addView(TextView(this).apply { text = label.uppercase(); textSize = 10f; setTextColor(colMuted) })
            card.addView(TextView(this).apply { text = time; textSize = 16f; setTypeface(null, Typeface.BOLD); setTextColor(colGreen); setPadding(0, dp(2),0,0) })
            naflGrid.addView(card)
        }
    }

    private fun renderDays() {
        dayListGrid.removeAllViews()
        val store = prefs.getString("salat_tracker_v2","{}")?.let { JSONObject(it) }?: JSONObject()
        val todayIdx = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)-1
        val keys = listOf("fajr","dhuhr","asr","maghrib","isha")
        val labels = mapOf("fajr" to Pair("🌙","ফজর"), "dhuhr" to Pair("☀","যুহর"), "asr" to Pair("🌤","আসর"), "maghrib" to Pair("🌇","মাগরিব"), "isha" to Pair("🌌","ইশা"))

        allMonth.forEachIndexed { i, d ->
            val base = (d.optJSONObject("meta")?.optJSONObject("location")?.optString("city")?: selectedCity.name_en) + "-$i"
            val doneCount = keys.count { store.optBoolean("$base-$it", false) }
            val dayCard = MaterialCardView(this).apply {
                radius = dpF(16f); strokeWidth = dp(1); strokeColor = if (i==todayIdx) colGold else colLine
                cardElevation = if (i==todayIdx) dpF(4f) else 0f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0; columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); setMargins(dp(5), dp(5), dp(5), dp(5))
                }
                setCardBackgroundColor(Color.WHITE)
            }
            val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(10)) }
            val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            head.addView(TextView(this).apply {
                text = toBnNum(i+1); textSize = 11f; setTypeface(null, Typeface.BOLD); setTextColor(Color.WHITE); gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(28), dp(28))
                background = GradientDrawable().apply { setColor(colGreen); cornerRadius = dpF(8f) }
            })
            head.addView(TextView(this).apply {
                text = "${toBnNum(doneCount)}/৫"; textSize = 10f; setTextColor(colMuted); gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            inner.addView(head)
            keys.forEach { k ->
                if (filter!= "all" && k!= filter) return@forEach
                val key = "$base-$k"
                val isDone = store.optBoolean(key, false)
                val pt = d.optJSONObject("prayer_times")?.optJSONObject(k)
                val pill = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(9), dp(7), dp(9), dp(7))
                    background = GradientDrawable().apply {
                        setColor(if (isDone) Color.parseColor("#E8F5E9") else Color.parseColor("#FAF6EB"))
                        cornerRadius = dpF(10f); setStroke(dp(1), if (isDone) Color.parseColor("#A7D8B0") else colLine)
                    }
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, dp(4),0,0) }
                    setOnClickListener {
                        val newStore = JSONObject(prefs.getString("salat_tracker_v2","{}")?: "{}")
                        if (newStore.optBoolean(key, false)) newStore.remove(key) else newStore.put(key, true)
                        prefs.edit().putString("salat_tracker_v2", newStore.toString()).apply()
                        renderDays()
                    }
                }
                pill.addView(TextView(this).apply { text = "${labels[k]?.first} ${labels[k]?.second}"; textSize = 11f; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
                pill.addView(TextView(this).apply {
                    text = if (isDone) "✓" else ""; textSize = 10f; gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
                    background = GradientDrawable().apply { setColor(if (isDone) colGreen else Color.WHITE); cornerRadius = dpF(6f); setStroke(dp(1), Color.parseColor("#D6D0BA")) }
                    setTextColor(if (isDone) Color.WHITE else colText)
                })
                inner.addView(pill)
            }
            dayCard.addView(inner)
            dayListGrid.addView(dayCard)
        }

        // Summary & chart
        var totalDone = 0; val per = mutableMapOf<String, Int>().apply { keys.forEach { put(it,0) } }
        val cityPrefix = (todayData?.optJSONObject("meta")?.optJSONObject("location")?.optString("city")?: selectedCity.name_en) + "-"
        val iter = store.keys()
        while (iter.hasNext()) { val k = iter.next(); if (k.startsWith(cityPrefix) && store.optBoolean(k,false)) { totalDone++; val wk = k.split("-").last(); per[wk] = (per[wk]?:0)+1 } }
        val totalWaqt = allMonth.size * 5
        trackerCountBadge.text = "${toBnNum(totalDone)}/${toBnNum(totalWaqt)}"
        // Summary
        summaryBox.removeAllViews()
        val sums = listOf("মোট আদায়" to totalDone, "ফজর" to (per["fajr"]?:0), "যুহর" to (per["dhuhr"]?:0), "আসর" to (per["asr"]?:0), "মাগরিব" to (per["maghrib"]?:0), "ইশা" to (per["isha"]?:0))
        sums.forEach { (label, value) ->
            val s = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(12f); setStroke(dp(1), colLine) }
                layoutParams = LinearLayout.LayoutParams(dp(88), ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,dp(8),0) }
            }
            s.addView(TextView(this).apply { text = toBnNum(value); textSize = 15f; setTypeface(null, Typeface.BOLD); setTextColor(colGreen); gravity = Gravity.CENTER })
            s.addView(TextView(this).apply { text = label; textSize = 9f; setTextColor(colMuted); gravity = Gravity.CENTER })
            summaryBox.addView(s)
        }
        val pct = if (totalWaqt>0) (totalDone*100/totalWaqt) else 0
        val pctView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(12f); setStroke(dp(1), colLine) }
        }
        pctView.addView(TextView(this).apply { text = "${toBnNum(pct)}%"; textSize = 15f; setTypeface(null, Typeface.BOLD); setTextColor(colGreen); gravity = Gravity.CENTER })
        pctView.addView(TextView(this).apply { text = "অগ্রগতি"; textSize = 9f; setTextColor(colMuted); gravity = Gravity.CENTER })
        summaryBox.addView(pctView)

        // Chart
        chartBars.removeAllViews()
        val maxVal = max(1, per.values.maxOrNull()?: 1)
        per.forEach { (k, v) ->
            val pctBar = if (allMonth.isNotEmpty()) (v*100/allMonth.size) else 0
            val barCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply { setMargins(dp(4),0,dp(4),0) } }
            val track = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(90))
                background = GradientDrawable().apply { setColor(Color.parseColor("#FEF3C7")); cornerRadii = floatArrayOf(dpF(10f),dpF(10f),dpF(4f),dpF(4f),dpF(4f),dpF(4f),dpF(10f),dpF(10f)) }
            }
            val fillHeight = max(dp(10), (v.toFloat()/maxVal*70).toInt())
            val fill = TextView(this).apply {
                text = if (v>0) toBnNum(v) else ""; textSize = 10f; setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
                setTextColor(if (pctBar<40) colGreen else Color.parseColor("#FFF6C8"))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fillHeight)
                background = GradientDrawable().apply {
                    setColors(if (pctBar<40) intArrayOf(Color.parseColor("#D4A017"), Color.parseColor("#FFF1A0")) else intArrayOf(colGreen, Color.parseColor("#1B4A3A")))
                    cornerRadii = floatArrayOf(dpF(10f),dpF(10f),dpF(4f),dpF(4f),dpF(4f),dpF(4f),dpF(10f),dpF(10f))
                }
            }
            track.addView(fill)
            barCol.addView(track)
            barCol.addView(TextView(this).apply { text = "${labels[k]?.first} ${labels[k]?.second}"; textSize = 11f; setTypeface(null, Typeface.BOLD); setTextColor(colGreen); gravity = Gravity.CENTER; setPadding(0, dp(6),0,0) })
            barCol.addView(TextView(this).apply { text = "${toBnNum(pctBar)}%"; textSize = 9f; setTextColor(colMuted); gravity = Gravity.CENTER })
            chartBars.addView(barCol)
        }
        chartSub.text = "${toBnNum(allMonth.size)} দিনে • ${toBnNum(totalDone)} ওয়াক্ত আদায়"
    }

    // ---------- Countdown & Notification ----------
    private fun getNextPrayerInfo(): PrayerInfo {
        val now = Calendar.getInstance()
        val nowM = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE)
        var active: String? = null; var activeIdx = -1
        for (i in prayerOrder.indices) {
            val k = prayerOrder[i].k
            val s = parseM(todayData?.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("start"), k)
            var e = parseMEnd(todayData?.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("end"), k, s)
            if (s!=null && e!=null && nowM>=s && nowM<e) { active=k; activeIdx=i; break }
        }
        var next: PrayerMeta? = null; var isTomorrow = false
        if (active!=null) { next = prayerOrder.getOrNull(activeIdx+1)?: prayerOrder[0]; if (activeIdx+1>=prayerOrder.size) isTomorrow=true }
        else { for (m in prayerOrder) { val s = parseM(todayData?.optJSONObject("prayer_times")?.optJSONObject(m.k)?.optString("start"), m.k); if (s!=null && s>nowM) { next=m; break } }; if (next==null) { next=prayerOrder[0]; isTomorrow=true } }
        return PrayerInfo(active, activeIdx, next, isTomorrow)
    }
    data class PrayerInfo(val active: String?, val activeIdx: Int, val next: PrayerMeta?, val isTomorrow: Boolean)

    private fun startCountdown() {
        countdownRunnable?.let { countdownHandler.removeCallbacks(it) }
        val runnable = object : Runnable {
            override fun run() {
                tickCountdown()
                countdownHandler.postDelayed(this, 1000)
            }
        }
        countdownRunnable = runnable
        countdownHandler.post(runnable)
    }

    private fun tickCountdown() {
        val td = todayData?: return
        val now = Calendar.getInstance()
        val info = getNextPrayerInfo()
        val k: String; val tStr: String?
        if (info.active!=null) { k=info.active; tStr=td.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("end") }
        else { k=info.next?.k?: "fajr"; tStr=td.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("start") }
        if (tStr==null) return
        var th = tStr.split(":")[0].toIntOrNull()?: 0
        var tm = tStr.split(":").getOrNull(1)?.toIntOrNull()?: 0
        if (k=="asr" || k=="maghrib" || k=="isha") { if (th<12) th+=12 }
        else if (k=="dhuhr" && info.active!=null) { if (th<12) th+=12 }

        val target = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, th); set(Calendar.MINUTE, tm); set(Calendar.SECOND, 0) }
        if (info.isTomorrow && info.active==null) target.add(Calendar.DAY_OF_YEAR, 1)
        if (target.before(now) && info.active==null) target.add(Calendar.DAY_OF_YEAR, 1)

        var diff = max(0, ((target.timeInMillis - now.timeInMillis)/1000).toInt())
        val h = diff/3600; diff%=3600; val m = diff/60; val s = diff%60

        tvH.text = toBn(String.format("%02d", h))
        tvM.text = toBn(String.format("%02d", m))
        tvS.text = toBn(String.format("%02d", s))

        // Forbidden check
        val forbiddenNow = getForbiddenNow()
        if (forbiddenNow!=null) {
            countCard.setCardBackgroundColor(Color.parseColor("#FFB91C1C"))
            countLabel.text = "🚫 ${forbiddenNow.optString("label_bn","নিষিদ্ধ সময়")}"
            countPray.text = forbiddenNow.optString("label_bn","নিষিদ্ধ")
            countIcon.text = "⛔"
        } else {
            countCard.setCardBackgroundColor(Color.parseColor("#F20E3B2E"))
            if (info.active!=null) {
                countLabel.text = "${td.optJSONObject("prayer_times")?.optJSONObject(info.active)?.optString("label_bn")?: ""} শেষ হতে"
                countPray.text = td.optJSONObject("prayer_times")?.optJSONObject(info.active)?.optString("label_bn")?: ""
                countIcon.text = "⏳"
                countCity.text = "${td.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?: selectedCity.name_bn} • চলছে"
            } else {
                countLabel.text = "পরবর্তী নামাজ"
                countPray.text = td.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("label_bn")?: k
                countIcon.text = info.next?.ic?: "🕌"
                countCity.text = "${td.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?: selectedCity.name_bn} • ${if (info.isTomorrow) "আগামীকাল" else ""} বাকি"
            }
        }

        // Notification logic
        checkAzanNotification(info, (target.timeInMillis - now.timeInMillis)/1000)

        if ((target.timeInMillis - now.timeInMillis)/1000 <=0) renderPrayerList()
    }

    private fun getForbiddenNow(): JSONObject? {
        val ft = todayData?.optJSONObject("forbidden_times")?: return null
        val now = Calendar.getInstance()
        val nowM = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE)
        for (k in listOf("sunrise","noon","sunset")) {
            val obj = ft.optJSONObject(k)?: continue
            val timeBn = obj.optString("time_bn","")
            val match = Regex("""(\d{1,2}):(\d{2}).*?(\d{1,2}):(\d{2})""").find(timeBn)
            if (match!=null) {
                var s = match.groupValues[1].toInt()*60 + match.groupValues[2].toInt()
                var e = match.groupValues[3].toInt()*60 + match.groupValues[4].toInt()
                if (k=="sunset" || k=="noon") { if (s<12*60) s+=12*60; if (e<12*60) e+=12*60 }
                if (nowM>=s && nowM<e) return obj
            }
        }
        return null
    }

    // ---------- Notification Handling ----------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("azan_channel", "Azan Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Prayer time alerts"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun toggleNotification() {
        if (!notifEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                    return
                }
            }
            notifEnabled = true
            prefs.edit().putBoolean("azan_notif", true).apply()
            bellBtn.text = "🔔"
            showToast("আজান নোটিফিকেশন চালু হয়েছে")
        } else {
            notifEnabled = false
            prefs.edit().putBoolean("azan_notif", false).apply()
            bellBtn.text = "🔕"
            showToast("নোটিফিকেশন বন্ধ")
        }
    }

    private fun checkAzanNotification(info: PrayerInfo, diffSec: Long) {
        if (!notifEnabled) return
        val now = Calendar.getInstance()
        val keyBase = "${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}-${now.get(Calendar.DAY_OF_MONTH)}"
        // 5 min before
        if (info.active==null && diffSec in 295..305) {
            val k = "pre-${info.next?.k}-${now.get(Calendar.DAY_OF_MONTH)}"
            if (lastNotifiedKey!=k) {
                lastNotifiedKey=k
                showSystemNotification("পরবর্তী নামাজ: ${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")}", "৫ মিনিট বাকি")
                showToast("${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")} ৫ মিনিট বাকি")
            }
        }
        if (info.active==null && diffSec<=2) {
            val k = "exact-${info.next?.k}-${now.get(Calendar.DAY_OF_MONTH)}-${now.get(Calendar.HOUR_OF_DAY)}"
            if (lastNotifiedKey!=k) {
                lastNotifiedKey=k
                showSystemNotification("${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")} এর সময় হয়েছে", "এখন ${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")} এর ওয়াক্ত")
                showToast("${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")} এর সময় হয়েছে")
            }
        }
    }

    private fun showSystemNotification(title: String, body: String) {
        try {
            val builder = NotificationCompat.Builder(this, "azan_channel")
               .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
               .setContentTitle(title)
               .setContentText(body)
               .setPriority(NotificationCompat.PRIORITY_HIGH)
               .setAutoCancel(true)
            NotificationManagerCompat.from(this).notify((System.currentTimeMillis()%10000).toInt(), builder.build())
        } catch (_: Exception) {}
    }

    private fun showToast(msg: String) {
        toastMsg.text = msg
        toastView.visibility = View.VISIBLE
        toastView.alpha = 0f
        toastView.animate().alpha(1f).setDuration(300).start()
        Handler(Looper.getMainLooper()).postDelayed({
            toastView.animate().alpha(0f).setDuration(300).withEndAction { toastView.visibility = View.GONE }.start()
        }, 3500)
    }

    // ---------- Modal ----------
    private fun openModal() { modalOverlay.visibility = View.VISIBLE; tempSelected = selectedCity; renderCityListFiltered() }
    private fun closeModal() { modalOverlay.visibility = View.GONE }
    private fun saveCity() {
        tempSelected?.let {
            selectedCity = it
            prefs.edit().putString("prayer_city_json", JSONObject().apply { put("name_en", it.name_en); put("name_bn", it.name_bn); put("division", it.division) }.toString()).apply()
            closeModal()
            prayerListContainer.removeAllViews()
            prayerListContainer.addView(TextView(this).apply { text="লোড হচ্ছে..."; gravity=Gravity.CENTER; setPadding(dp(10), dp(30), dp(10), dp(30)) })
            lifecycleScope.launch { try { loadCityData(selectedCity.name_en) } catch (_: Exception) {} }
        }
    }

    override fun onDestroy() {
        countdownRunnable?.let { countdownHandler.removeCallbacks(it) }
        super.onDestroy()
    }
}
