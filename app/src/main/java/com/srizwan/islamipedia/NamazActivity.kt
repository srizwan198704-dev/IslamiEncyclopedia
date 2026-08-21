package com.srizwan.islamipedia

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.max

class NamazActivity : AppCompatActivity() {

    // FIXED COLORS FOR READABILITY
    private val colBg = Color.parseColor("#FDFBF6")
    private val colGreen = Color.parseColor("#0E3B2E")
    private val colGold = Color.parseColor("#C9A227")
    private val colGoldL = Color.parseColor("#FFF6C8")
    private val colLine = Color.parseColor("#E8D9A8") // was #EFE5C8 - too light
    private val colMuted = Color.parseColor("#6B6B65") // was #9B9B93 - unreadable, now dark
    private val colText = Color.parseColor("#1D1D1B")
    private val colStatus = Color.parseColor("#102E26")
    private val colTextOnDark = Color.parseColor("#F5F3E8")

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
    private val prefs by lazy { getSharedPreferences("namaz_prefs", MODE_PRIVATE) }

    private val CITIES_URL = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia/BangladeshCities.json"
    private val CITY_BASE = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia@main/BD/"

    private var allCities = mutableListOf<City>()
    private var selectedCity = City("Dhaka", "ঢাকা", "Dhaka")
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
    data class PrayerInfo(val active: String?, val activeIdx: Int, val next: PrayerMeta?, val isTomorrow: Boolean)

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
          .connectTimeout(15, TimeUnit.SECONDS)
          .readTimeout(15, TimeUnit.SECONDS)
          .writeTimeout(15, TimeUnit.SECONDS)
          .build()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun dpF(v: Float) = v * resources.displayMetrics.density
    private fun toBn(n: String): String {
        val map = mapOf('0' to '০','1' to '১','2' to '২','3' to '৩','4' to '৪','5' to '৫','6' to '৬','7' to '৭','8' to '৮','9' to '৯')
        return n.map { map[it]?: it }.joinToString("")
    }
    private fun toBnNum(n: Int) = toBn(n.toString())
    private fun getInner(card: MaterialCardView) = card.getChildAt(0) as LinearLayout

    // FIXED FONT - file exists as solaimanlipi.ttf
    private fun getSolaimanLipiTypeface(): Typeface? {
        return try {
            ResourcesCompat.getFont(this, R.font.solaimanlipi)
        } catch (e: Exception) { null }
    }

    private suspend fun fetchJson(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).header("Cache-Control", "no-cache").header("Pragma", "no-cache").build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code} for $url")
            response.body?.string()?: throw IOException("Empty body for $url")
        }
    }

    private fun parseM(t: String?, k: String?): Int? {
        if (t == null) return null; val p = t.split(":"); if (p.size<2) return null
        var h = p[0].toIntOrNull()?: return null; val m = p[1].toIntOrNull()?:0
        if (k!=null && (k=="asr"||k=="maghrib"||k=="isha") && h<12) h+=12
        return h*60+m
    }
    private fun parseMEnd(t: String?, k: String?, s: Int?): Int? {
        if (t==null) return null; val p=t.split(":"); var h=p[0].toIntOrNull()?:return null; var m=p.getOrNull(1)?.toIntOrNull()?:0
        if (k=="dhuhr"||k=="asr"||k=="maghrib"||k=="isha" && h<12) h+=12
        var e=h*60+m; if (s!=null && e<=s) e+=12*60; return e
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIX: STATUSBAR / NAVBAR OVERLAY OFF + COLOR MATCHING
        window.statusBarColor = colStatus
        window.navigationBarColor = colBg
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        notifEnabled = prefs.getBoolean("azan_notif", false)
        prefs.getString("prayer_city_json", null)?.let {
            try { val o=JSONObject(it); selectedCity=City(o.getString("name_en"),o.getString("name_bn"),o.optString("division")) } catch (_:Exception){}
        }
        tempSelected = selectedCity

        val rootFrame = FrameLayout(this).apply { layoutParams = FrameLayout.LayoutParams(-1,-1); setBackgroundColor(colBg) }
        val scrollView = ScrollView(this).apply { layoutParams = FrameLayout.LayoutParams(-1,-1); isVerticalScrollBarEnabled=false }
        val mainContainer = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; layoutParams = ViewGroup.LayoutParams(-1,-2); setPadding(0,0,0,dp(130)) }
        mainContainer.addView(buildHeroSection())
        mainContainer.addView(buildContentSection())
        scrollView.addView(mainContainer)
        rootFrame.addView(scrollView)

        countCard = buildCountCard()
        rootFrame.addView(countCard, FrameLayout.LayoutParams(-1,-2).apply { gravity=Gravity.BOTTOM; setMargins(dp(12),0,dp(12),dp(12)) })
        toastView = buildToast()
        rootFrame.addView(toastView, FrameLayout.LayoutParams(-2,-2).apply { gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL; topMargin=dp(16) })
        modalOverlay = buildCityModal()
        rootFrame.addView(modalOverlay, FrameLayout.LayoutParams(-1,-1))

        setContentView(rootFrame)
        getSolaimanLipiTypeface()?.let { tf -> applyFontToAll(rootFrame, tf) }
        createNotificationChannel()
        lifecycleScope.launch {
            try { loadCities() } catch (_:Exception){}
            try { loadCityData(selectedCity.name_en) } catch (_:Exception){ try{ loadCityData("Dhaka") } catch (_:Exception){} }
        }
    }

    private fun applyFontToAll(v: View, tf: Typeface){
        if(v is TextView) {
            val style = if(v.typeface?.isBold == true) Typeface.BOLD else Typeface.NORMAL
            v.typeface = Typeface.create(tf, style)
        }
        if(v is ViewGroup) for(i in 0 until v.childCount) applyFontToAll(v.getChildAt(i),tf)
    }

    private fun createCard(): MaterialCardView {
        val inner = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; layoutParams=ViewGroup.LayoutParams(-1,-2) }
        return MaterialCardView(this).apply {
            radius=dpF(24f); cardElevation=dpF(12f); strokeWidth=dp(1); strokeColor=colLine
            setCardBackgroundColor(Color.WHITE)
            layoutParams=LinearLayout.LayoutParams(-1,-2)
            addView(inner)
        }
    }
    private fun createCardHeader(title: String): LinearLayout {
        val header = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(16),dp(14),dp(16),dp(14)) }
        header.addView(TextView(this).apply { text=title; textSize=15f; setTypeface(null,Typeface.BOLD); setTextColor(colText); layoutParams=LinearLayout.LayoutParams(0,-2,1f) })
        return header
    }
    private fun createBadge(text: String) = TextView(this).apply {
        this.text=text; textSize=10f; setTypeface(null,Typeface.BOLD); setTextColor(colGreen)
        setPadding(dp(10),dp(5),dp(10),dp(5))
        background=GradientDrawable().apply { setColor(colGoldL); cornerRadius=dpF(99f); setStroke(dp(1),Color.parseColor("#E8D89A")) }
    }
    private fun createChip(text: String, active: Boolean) = TextView(this).apply {
        this.text=text; textSize=11f; setTypeface(null,Typeface.BOLD); setPadding(dp(12),dp(6),dp(12),dp(6))
        isSelected=active; updateChip()
    }
    private fun TextView.updateChip(){
        background = if(isSelected) GradientDrawable().apply { setColor(colGreen); cornerRadius=dpF(99f) }
        else GradientDrawable().apply { setColor(Color.WHITE); cornerRadius=dpF(99f); setStroke(dp(1),colLine) }
        setTextColor(if(isSelected) Color.WHITE else colText)
    }

    private fun buildHeroSection(): LinearLayout {
        val hero = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#102E26"),Color.parseColor("#0A201A"))).apply { cornerRadii=floatArrayOf(0f,0f,0f,0f,dpF(36f),dpF(36f),dpF(36f),dpF(36f)) }
            setPadding(dp(16),dp(14),dp(16),dp(20))
        }
        val topRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        val mark = TextView(this).apply { text="﷽"; gravity=Gravity.CENTER; setTextColor(colGreen); textSize=18f; setTypeface(null,Typeface.BOLD); background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(Color.parseColor("#D4A017"),Color.parseColor("#FFF1A0"))).apply { cornerRadius=dpF(14f) }; layoutParams=LinearLayout.LayoutParams(dp(40),dp(40)) }
        val brandText = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(10),0,0,0) }
        brandText.addView(TextView(this).apply { text="ইসলামী বিশ্বকোষ ও আল হাদিস S2"; setTextColor(Color.WHITE); textSize=14f; setTypeface(null,Typeface.BOLD) })
        brandText.addView(TextView(this).apply { text="নামাজের সময়সূচি • Masjid Edition"; setTextColor(colTextOnDark); alpha=0.85f; textSize=11f })
        brand.addView(mark); brand.addView(brandText)
        tvCurrentCityBn = TextView(this).apply { text="📍 ${selectedCity.name_bn} ▼"; setTextColor(Color.WHITE); textSize=13f; setPadding(dp(13),dp(7),dp(13),dp(7)); background=GradientDrawable().apply { setColor(Color.parseColor("#1AFFFFFF")); cornerRadius=dpF(99f); setStroke(dp(1),Color.parseColor("#2DFFFFFF")) }; setOnClickListener { openModal() } }
        topRow.addView(brand, LinearLayout.LayoutParams(0,-2,1f)); topRow.addView(tvCurrentCityBn)
        tvHijri = TextView(this).apply { text="লোড হচ্ছে..."; setTextColor(colGoldL); textSize=15f; setTypeface(null,Typeface.BOLD); gravity=Gravity.CENTER; setPadding(0,dp(18),0,0) }
        tvBengali = TextView(this).apply { text=""; setTextColor(Color.WHITE); alpha=0.95f; textSize=12f; gravity=Gravity.CENTER; setPadding(0,dp(4),0,0) }
        val mosqueLine = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; layoutParams=LinearLayout.LayoutParams(-1,dp(56)).apply { topMargin=dp(18) } }
        mosqueLine.addView(View(this).apply { layoutParams=LinearLayout.LayoutParams(dp(6),dp(44)).apply{setMargins(dp(8),0,dp(8),0)}; background=GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#E8E0C0"),Color.parseColor("#C9B78A"))); cornerRadius=dpF(3f) } })
        mosqueLine.addView(View(this).apply { layoutParams=LinearLayout.LayoutParams(dp(28),dp(18)).apply{setMargins(dp(8),0,dp(8),0)}; background=GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#FFE9A0"),colGold)); cornerRadii=floatArrayOf(dpF(14f),dpF(14f),dpF(14f),dpF(14f),dpF(4f),dpF(4f),dpF(4f),dpF(4f)) } })
        mosqueLine.addView(View(this).apply { layoutParams=LinearLayout.LayoutParams(dp(56),dp(32)).apply{setMargins(dp(12),0,dp(12),0)}; background=GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#FFE9A0"),colGold)); cornerRadii=floatArrayOf(dpF(28f),dpF(28f),dpF(28f),dpF(28f),dpF(6f),dpF(6f),dpF(6f),dpF(6f)) } })
        mosqueLine.addView(View(this).apply { layoutParams=LinearLayout.LayoutParams(dp(28),dp(18)).apply{setMargins(dp(8),0,dp(8),0)}; background=GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#FFE9A0"),colGold)); cornerRadii=floatArrayOf(dpF(14f),dpF(14f),dpF(14f),dpF(14f),dpF(4f),dpF(4f),dpF(4f),dpF(4f)) } })
        mosqueLine.addView(View(this).apply { layoutParams=LinearLayout.LayoutParams(dp(6),dp(44)).apply{setMargins(dp(8),0,dp(8),0)}; background=GradientDrawable().apply { setColors(intArrayOf(Color.parseColor("#E8E0C0"),Color.parseColor("#C9B78A"))); cornerRadius=dpF(3f) } })
        hero.addView(topRow); hero.addView(tvHijri); hero.addView(tvBengali); hero.addView(mosqueLine)
        hero.addView(TextView(this).apply { text="নামাজের সময়সূচি"; setTextColor(Color.WHITE); textSize=26f; setTypeface(null,Typeface.BOLD); gravity=Gravity.CENTER; setPadding(0,dp(18),0,0) })
        return hero
    }

    private fun buildContentSection(): LinearLayout {
        val container = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(14),dp(12),dp(14),dp(12)) }
        val prayerCard = createCard()
        val pInner = getInner(prayerCard)
        val header1 = createCardHeader("আজকের ওয়াক্ত")
        todayBadge = createBadge("আজ")
        header1.addView(todayBadge)
        pInner.addView(header1)
        prayerListContainer = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(10),dp(10),dp(10)) }
        prayerListContainer.addView(TextView(this).apply { text="লোড হচ্ছে..."; setTextColor(colMuted); gravity=Gravity.CENTER; setPadding(dp(10),dp(30),dp(10),dp(30)) })
        pInner.addView(prayerListContainer)

        val forbiddenCard = createCard().apply { layoutParams=(layoutParams as LinearLayout.LayoutParams).apply{topMargin=dp(16)} }
        val fInner = getInner(forbiddenCard)
        fInner.addView(createCardHeader("নামাজের নিষিদ্ধ সময়সূচী"))
        forbiddenListContainer = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }
        fInner.addView(forbiddenListContainer)

        val naflCard = createCard().apply { layoutParams=(layoutParams as LinearLayout.LayoutParams).apply{topMargin=dp(16)} }
        val nInner = getInner(naflCard)
        nInner.addView(createCardHeader("নফল নামাজের সময়সূচী"))
        naflGrid = GridLayout(this).apply { columnCount=2; setPadding(dp(10),dp(10),dp(10),dp(10)) }
        nInner.addView(naflGrid)

        val trackerCard = createCard().apply { layoutParams=(layoutParams as LinearLayout.LayoutParams).apply{topMargin=dp(16)} }
        val tInner = getInner(trackerCard)
        val trackerHeader = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(16),dp(14),dp(16),dp(14)) }
        trackerHeader.addView(TextView(this).apply { text="সম্পূর্ণ মাসের নামাজের ট্র্যাকার"; textSize=14f; setTypeface(null,Typeface.BOLD); setTextColor(colText); layoutParams=LinearLayout.LayoutParams(0,-2,1f) })
        trackerMonthBadge = createBadge("—")
        trackerCountBadge = createBadge("0/0").apply { background=GradientDrawable().apply { setColor(Color.parseColor("#ECFDF5")); cornerRadius=dpF(99f); setStroke(dp(1),colLine) } }
        val badgeRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        badgeRow.addView(trackerMonthBadge, LinearLayout.LayoutParams(-2,-2).apply{setMargins(0,0,dp(6),0)}); badgeRow.addView(trackerCountBadge)
        trackerHeader.addView(badgeRow)

        val summaryScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled=false }
        summaryBox = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(dp(12),dp(10),dp(12),dp(10)) }
        summaryScroll.addView(summaryBox)

        val chartWrap = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(12),dp(12),dp(12),dp(12)); setBackgroundColor(Color.parseColor("#FFFEF6")) }
        val chartTitleRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        chartTitleRow.addView(TextView(this).apply { text="📊 এই মাসের অগ্রগতি চার্ট"; textSize=13f; setTypeface(null,Typeface.BOLD); setTextColor(colText); layoutParams=LinearLayout.LayoutParams(0,-2,1f) })
        chartSub = TextView(this).apply { text="৫ ওয়াক্ত ভিত্তিক"; textSize=10f; setTextColor(colMuted) }
        chartTitleRow.addView(chartSub)
        chartBars = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.BOTTOM; layoutParams=LinearLayout.LayoutParams(-1,dp(90)).apply{topMargin=dp(10)} }
        chartWrap.addView(chartTitleRow); chartWrap.addView(chartBars)

        val chipScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled=false; setPadding(dp(12),dp(12),dp(12),dp(12)) }
        chipContainer = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        listOf("all" to "সব ওয়াক্ত","fajr" to "🌙 ফজর","dhuhr" to "☀ যুহর","asr" to "🌤 আসর","maghrib" to "🌇 মাগরিব","isha" to "🌌 ইশা").forEach { pair ->
            val (k,lbl)=pair; val chip=createChip(lbl,k=="all"); chip.tag=k
            chip.setOnClickListener { for(i in 0 until chipContainer.childCount) (chipContainer.getChildAt(i) as TextView).let{it.isSelected=false; it.updateChip()}; chip.isSelected=true; chip.updateChip(); filter=k; renderDays() }
            chipContainer.addView(chip, LinearLayout.LayoutParams(-2,-2).apply{setMargins(0,0,dp(8),0)})
        }
        chipScroll.addView(chipContainer)
        dayListGrid = GridLayout(this).apply { columnCount=2; setPadding(dp(12),dp(12),dp(12),dp(12)) }

        tInner.addView(trackerHeader); tInner.addView(summaryScroll); tInner.addView(chartWrap); tInner.addView(chipScroll); tInner.addView(dayListGrid)
        container.addView(prayerCard); container.addView(forbiddenCard); container.addView(naflCard); container.addView(trackerCard)
        return container
    }

    private fun buildCountCard(): MaterialCardView {
        val card = MaterialCardView(this).apply { radius=dpF(20f); cardElevation=dpF(18f); setCardBackgroundColor(Color.parseColor("#F20E3B2E")); strokeWidth=dp(1); strokeColor=Color.parseColor("#24FFFFFF") }
        val row = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(14),dp(12),dp(14),dp(12)) }
        countIcon = TextView(this).apply { text="🕌"; textSize=20f; gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(dp(46),dp(46)); background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(Color.parseColor("#D4A017"),Color.parseColor("#FFF1A0"))).apply{cornerRadius=dpF(14f)} }
        val mid = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(dp(12),0,dp(12),0)} }
        countLabel=TextView(this).apply{ text="পরবর্তী নামাজ"; textSize=10f; setTextColor(Color.WHITE); alpha=0.95f }
        countPray=TextView(this).apply{ text="যুহর"; textSize=15f; setTextColor(Color.WHITE); setTypeface(null,Typeface.BOLD) }
        countCity=TextView(this).apply{ text="ঢাকা"; textSize=10f; setTextColor(colTextOnDark); alpha=0.9f }
        mid.addView(countLabel); mid.addView(countPray); mid.addView(countCity)
        val timeRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL }
        tvH=TextView(this).apply{ text="০০"; setTextColor(Color.WHITE); textSize=18f; setTypeface(null,Typeface.BOLD) }
        tvM=TextView(this).apply{ text="০০"; setTextColor(Color.WHITE); textSize=18f; setTypeface(null,Typeface.BOLD); setPadding(dp(8),0,0,0) }
        tvS=TextView(this).apply{ text="০০"; setTextColor(Color.WHITE); textSize=18f; setTypeface(null,Typeface.BOLD); setPadding(dp(8),0,0,0) }
        timeRow.addView(tvH); timeRow.addView(tvM); timeRow.addView(tvS)
        bellBtn=TextView(this).apply{ text=if(notifEnabled)"🔔" else "🔕"; textSize=16f; gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(dp(36),dp(36)).apply{setMargins(dp(8),0,0,0)}; background=GradientDrawable().apply{ cornerRadius=dpF(10f); setStroke(dp(1),Color.parseColor("#33FFFFFF")); setColor(Color.parseColor("#1AFFFFFF")) }; setTextColor(Color.WHITE); setOnClickListener{ toggleNotification() } }
        row.addView(countIcon); row.addView(mid); row.addView(timeRow); row.addView(bellBtn)
        card.addView(row); return card
    }
    private fun buildToast()=LinearLayout(this).apply{
        orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(18),dp(12),dp(18),dp(12)); visibility=View.GONE
        background=GradientDrawable().apply{ setColor(colGreen); cornerRadius=dpF(99f) }; elevation=dpF(12f)
    }.also{ it.addView(TextView(this).apply{ text="🔔"; setTextColor(Color.WHITE) }); toastMsg=TextView(this).apply{ text="আজান"; setTextColor(Color.WHITE); textSize=13f; setPadding(dp(10),0,0,0) }; it.addView(toastMsg) }

    private fun buildCityModal(): FrameLayout {
        val overlay=FrameLayout(this).apply{ setBackgroundColor(Color.parseColor("#990A1415")); visibility=View.GONE }
        val modal=MaterialCardView(this).apply{ radius=dpF(22f); cardElevation=dpF(24f); layoutParams=FrameLayout.LayoutParams(-1,-2).apply{gravity=Gravity.CENTER; setMargins(dp(14),dp(14),dp(14),dp(14))}; setCardBackgroundColor(Color.parseColor("#FFFEFB")) }
        val inner=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL }
        val header=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(16),dp(16),dp(16),0) }
        header.addView(TextView(this).apply{ text="স্থান শনাক্ত করুন"; textSize=17f; setTypeface(null,Typeface.BOLD); setTextColor(colText) })
        header.addView(TextView(this).apply{ text="নামাজের সময় এবং চাঁদের তারিখ দেখাতে আপনার অবস্থান জানা জরুরি।"; textSize=12f; setTextColor(colMuted); setPadding(0,dp(4),0,0) })
        val searchWrap=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(16),dp(12),dp(16),0) }
        val searchBox=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; background=GradientDrawable().apply{ setColor(Color.WHITE); cornerRadius=dpF(12f); setStroke(dp(1),Color.parseColor("#D6C99E"))}; layoutParams=LinearLayout.LayoutParams(-1,-2); setPadding(dp(11),dp(11),dp(11),dp(11)) }
        citySearchEt=EditText(this).apply{ hint="শহর নির্বাচন করুন - যেমন ঢাকা"; textSize=13f; background=null; setTextColor(colText); setHintTextColor(Color.parseColor("#8A8A86")); layoutParams=LinearLayout.LayoutParams(0,-2,1f) }
        searchBox.addView(TextView(this).apply{ text="🔍"; setPadding(0,0,dp(6),0) }); searchBox.addView(citySearchEt); searchWrap.addView(searchBox)
        val scroll=ScrollView(this).apply{ layoutParams=LinearLayout.LayoutParams(-1,dp(300)).apply{topMargin=dp(8)} }
        cityListContainer=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(8),dp(10),dp(8)) }; scroll.addView(cityListContainer)
        val footer=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(dp(16),dp(10),dp(16),dp(10)) }
        val cancel=TextView(this).apply{ text="বাতিল"; gravity=Gravity.CENTER; textSize=13f; setTypeface(null,Typeface.BOLD); setTextColor(colText); background=GradientDrawable().apply{ setColor(Color.WHITE); cornerRadius=dpF(11f); setStroke(dp(1),Color.parseColor("#D6C99E"))}; layoutParams=LinearLayout.LayoutParams(0,dp(42),1f).apply{setMargins(0,0,dp(8),0)}; setOnClickListener{ closeModal() } }
        val save=TextView(this).apply{ text="সংরক্ষণ"; gravity=Gravity.CENTER; textSize=13f; setTypeface(null,Typeface.BOLD); setTextColor(Color.WHITE); background=GradientDrawable().apply{ setColor(colGreen); cornerRadius=dpF(11f)}; layoutParams=LinearLayout.LayoutParams(0,dp(42),1f); setOnClickListener{ saveCity() } }
        footer.addView(cancel); footer.addView(save)
        inner.addView(header); inner.addView(searchWrap); inner.addView(scroll); inner.addView(footer)
        modal.addView(inner); overlay.addView(modal)
        overlay.setOnClickListener{ if(it==overlay) closeModal() }
        citySearchEt.addTextChangedListener(object: TextWatcher{ override fun afterTextChanged(s: Editable?){ renderCityListFiltered() } override fun beforeTextChanged(s: CharSequence?,start:Int,count:Int,after:Int){} override fun onTextChanged(s: CharSequence?,start:Int,before:Int,count:Int){} })
        return overlay
    }

    private suspend fun loadCities(){ try{ val json=fetchJson(CITIES_URL); val arr=JSONArray(json); allCities.clear(); for(i in 0 until arr.length()){ val o=arr.getJSONObject(i); allCities.add(City(o.getString("name_en"),o.getString("name_bn"),o.optString("division",""))) } } catch(_:Exception){ if(allCities.isEmpty()) allCities.add(City("Dhaka","ঢাকা","Dhaka")) }; withContext(Dispatchers.Main){ renderCityListFiltered() } }
    private fun renderCityListFiltered(){
        val q=citySearchEt.text.toString().trim(); val list=if(q.isEmpty()) allCities else allCities.filter{ it.name_bn.contains(q)||it.name_en.lowercase().contains(q.lowercase()) }
        cityListContainer.removeAllViews()
        list.forEach{ c ->
            val isSel=tempSelected?.name_en==c.name_en
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                setPadding(dp(12),dp(11),dp(12),dp(11))
                background=if(isSel) GradientDrawable().apply{ setColor(Color.parseColor("#EAF7EF")); cornerRadius=dpF(12f); setStroke(dp(1),colGreen)}
                else GradientDrawable().apply{ setColor(Color.WHITE); cornerRadius=dpF(12f); setStroke(dp(1),colLine)}
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,dp(6))}
                setOnClickListener{ tempSelected=c; renderCityListFiltered() }
            }
            row.addView(TextView(this).apply{
                text="${c.name_bn} - ${c.division}"; textSize=14f; setTypeface(null, if(isSel) Typeface.BOLD else Typeface.NORMAL)
                setTextColor(if(isSel) colGreen else colText); layoutParams=LinearLayout.LayoutParams(0,-2,1f)
            })
            row.addView(TextView(this).apply{ text=if(isSel)"✓" else ""; textSize=14f; setTypeface(null,Typeface.BOLD); setTextColor(colGreen) })
            cityListContainer.addView(row)
        }
    }
    private suspend fun loadCityData(en: String){ val url=CITY_BASE+en+".json?v="+System.currentTimeMillis(); val json=fetchJson(url); val arr=JSONArray(json); allMonth.clear(); for(i in 0 until arr.length()) allMonth.add(arr.getJSONObject(i)); val today=java.util.Calendar.getInstance(); val idx=max(0,minOf(today.get(java.util.Calendar.DAY_OF_MONTH)-1,allMonth.size-1)); todayData=allMonth.getOrNull(idx)?:allMonth.firstOrNull(); withContext(Dispatchers.Main){ renderAll() } }
    private fun renderAll(){ val td=todayData?:return; tvCurrentCityBn.text="📍 ${td.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?:selectedCity.name_bn} ▼"; val hijri=td.optJSONObject("date")?.optJSONObject("hijri")?.optString("bn")?:""; val fullBn=td.optJSONObject("date")?.optJSONObject("full")?.optString("bn")?:""; tvHijri.text=hijri.ifEmpty{fullBn.split("•").firstOrNull()?:""}; tvBengali.text=td.optJSONObject("date")?.optJSONObject("bengali")?.optString("bn")?:fullBn.split("•").getOrNull(1)?:""; todayBadge.text=td.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?:selectedCity.name_bn; trackerMonthBadge.text=fullBn; renderPrayerList(); renderForbidden(); renderNafl(); renderDays(); startCountdown() }
    private fun renderPrayerList(){ prayerListContainer.removeAllViews(); val td=todayData?:return; val pt=td.optJSONObject("prayer_times")?:return; val info=getNextPrayerInfo(); prayerOrder.forEach{ meta -> val o=pt.optJSONObject(meta.k)?:return@forEach; val isA=info.active==meta.k; val isN=info.next?.k==meta.k&&!isA; val item=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(12),dp(12),dp(12),dp(12)); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,dp(8))}; background=if(isA) GradientDrawable().apply{ setColors(intArrayOf(colGreen,Color.parseColor("#1B4A3A"))); cornerRadius=dpF(16f)} else if(isN) GradientDrawable().apply{ setColors(intArrayOf(Color.parseColor("#FFFEF6"),Color.parseColor("#FFF3B8"))); cornerRadius=dpF(16f); setStroke(dp(1),colGold)} else GradientDrawable().apply{ setColors(intArrayOf(Color.WHITE,Color.parseColor("#FFFCF0"))); cornerRadius=dpF(16f); setStroke(dp(1),colLine)} }; val icon=TextView(this).apply{ text=meta.ic; textSize=22f; gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(dp(48),dp(48)); background=GradientDrawable().apply{ setColor(if(isA) Color.parseColor("#1FFFFFFF") else Color.parseColor("#FAF6E8")); cornerRadius=dpF(14f)} }; val nameTv=TextView(this).apply{ text=o.optString("label_bn",meta.k); textSize=15f; setTypeface(null,Typeface.BOLD); setTextColor(if(isA) Color.WHITE else colText); layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(dp(12),0,0,0)} }; val timeTv=TextView(this).apply{ text=o.optString("time_bn",""); textSize=15f; setTypeface(null,Typeface.BOLD); setTextColor(if(isA) colGoldL else colText)}; item.addView(icon); item.addView(nameTv); item.addView(timeTv); prayerListContainer.addView(item) } }
    private fun renderForbidden(){ forbiddenListContainer.removeAllViews(); val ft=todayData?.optJSONObject("forbidden_times")?:return; listOf("sunrise","noon","sunset").forEach{ k -> val d=ft.optJSONObject(k)?:return@forEach; val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(12),dp(10),dp(12),dp(10)); background=GradientDrawable().apply{ setColor(Color.parseColor("#FFFBEB")); cornerRadius=dpF(12f); setStroke(dp(1),Color.parseColor("#F8E9B0"))}; layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,dp(6))}}; row.addView(TextView(this).apply{ text="🚫 ${d.optString("label_bn",k)}"; textSize=12f; setTextColor(colText); layoutParams=LinearLayout.LayoutParams(0,-2,1f)}); row.addView(TextView(this).apply{ text=d.optString("time_bn",""); textSize=12f; setTypeface(null,Typeface.BOLD); setTextColor(colText)}); forbiddenListContainer.addView(row) } }
    private fun renderNafl(){ naflGrid.removeAllViews(); val nafl=todayData?.optJSONObject("nafl_times")?:JSONObject(); val items=listOf("তাহাজ্জুদ" to (nafl.optJSONObject("tahajjud")?.optString("time_bn")?:"-"), "সাহরী শেষ" to (nafl.optJSONObject("tahajjud")?.optString("time_bn")?:todayData?.optJSONObject("prayer_times")?.optJSONObject("fajr")?.optString("time_bn")?.split(" - ")?.firstOrNull()?:"-"), "ইশরাক" to (nafl.optJSONObject("ishraq")?.optString("time_bn")?:"-"), "চাশত" to (nafl.optJSONObject("chasht")?.optString("time_bn")?:"-")); items.forEach{ (l,t)-> val card=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(11),dp(11),dp(11),dp(11)); background=GradientDrawable().apply{ setColor(Color.WHITE); cornerRadius=dpF(14f); setStroke(dp(1),colLine)}; layoutParams=GridLayout.LayoutParams().apply{ width=0; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(dp(4),dp(4),dp(4),dp(4))} }; card.addView(TextView(this).apply{ text=l.uppercase(); textSize=10f; setTextColor(colMuted)}); card.addView(TextView(this).apply{ text=t; textSize=16f; setTypeface(null,Typeface.BOLD); setTextColor(colGreen); setPadding(0,dp(2),0,0)}); naflGrid.addView(card)} }
    private fun renderDays(){ dayListGrid.removeAllViews(); val store=prefs.getString("salat_tracker_v2","{}")?.let{JSONObject(it)}?:JSONObject(); val todayIdx=java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)-1; val keys=listOf("fajr","dhuhr","asr","maghrib","isha"); val labels=mapOf("fajr" to Pair("🌙","ফজর"),"dhuhr" to Pair("☀","যুহর"),"asr" to Pair("🌤","আসর"),"maghrib" to Pair("🌇","মাগরিব"),"isha" to Pair("🌌","ইশা")); allMonth.forEachIndexed{ i,d -> val base=(d.optJSONObject("meta")?.optJSONObject("location")?.optString("city")?:selectedCity.name_en)+"-$i"; val done=keys.count{store.optBoolean("$base-$it",false)}; val dayCard=MaterialCardView(this).apply{ radius=dpF(16f); strokeWidth=dp(1); strokeColor=if(i==todayIdx)colGold else colLine; cardElevation=if(i==todayIdx)dpF(4f) else 0f; layoutParams=GridLayout.LayoutParams().apply{ width=0; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(dp(5),dp(5),dp(5),dp(5))}; setCardBackgroundColor(Color.WHITE)}; val inner=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(10),dp(10),dp(10))}; val head=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL}; head.addView(TextView(this).apply{ text=toBnNum(i+1); textSize=11f; setTypeface(null,Typeface.BOLD); setTextColor(Color.WHITE); gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(dp(28),dp(28)); background=GradientDrawable().apply{ setColor(colGreen); cornerRadius=dpF(8f)}}); head.addView(TextView(this).apply{ text="${toBnNum(done)}/৫"; textSize=10f; setTextColor(colMuted); gravity=Gravity.END; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}); inner.addView(head); keys.forEach{ k-> if(filter!="all"&&k!=filter) return@forEach; val key="$base-$k"; val isDone=store.optBoolean(key,false); val pill=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(9),dp(7),dp(9),dp(7)); background=GradientDrawable().apply{ setColor(if(isDone)Color.parseColor("#E8F5E9") else Color.parseColor("#FAF6EB")); cornerRadius=dpF(10f); setStroke(dp(1),if(isDone)Color.parseColor("#A7D8B0") else colLine)}; layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,dp(4),0,0)}; setOnClickListener{ val ns=JSONObject(prefs.getString("salat_tracker_v2","{}")?:"{}"); if(ns.optBoolean(key,false)) ns.remove(key) else ns.put(key,true); prefs.edit().putString("salat_tracker_v2",ns.toString()).apply(); renderDays() } }; pill.addView(TextView(this).apply{ text="${labels[k]?.first} ${labels[k]?.second}"; textSize=11f; setTextColor(colText); layoutParams=LinearLayout.LayoutParams(0,-2,1f)}); pill.addView(TextView(this).apply{ text=if(isDone)"✓" else ""; textSize=10f; gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(dp(18),dp(18)); background=GradientDrawable().apply{ setColor(if(isDone)colGreen else Color.WHITE); cornerRadius=dpF(6f)}; setTextColor(if(isDone)Color.WHITE else colText)}); inner.addView(pill)}; dayCard.addView(inner); dayListGrid.addView(dayCard)}; var totalDone=0; val per=mutableMapOf<String,Int>().apply{ keys.forEach{put(it,0)} }; val cityPrefix=(todayData?.optJSONObject("meta")?.optJSONObject("location")?.optString("city")?:selectedCity.name_en)+"-"; val it=store.keys(); while(it.hasNext()){ val k=it.next(); if(k.startsWith(cityPrefix)&&store.optBoolean(k,false)){ totalDone++; val wk=k.split("-").last(); per[wk]=(per[wk]?:0)+1 } }; trackerCountBadge.text="${toBnNum(totalDone)}/${toBnNum(allMonth.size*5)}"; summaryBox.removeAllViews(); listOf("মোট আদায়" to totalDone,"ফজর" to (per["fajr"]?:0),"যুহর" to (per["dhuhr"]?:0),"আসর" to (per["asr"]?:0),"মাগরিব" to (per["maghrib"]?:0),"ইশা" to (per["isha"]?:0)).forEach{ (lb,v)-> val s=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(dp(8),dp(8),dp(8),dp(8)); background=GradientDrawable().apply{ setColor(Color.WHITE); cornerRadius=dpF(12f); setStroke(dp(1),colLine)}; layoutParams=LinearLayout.LayoutParams(dp(88),-2).apply{setMargins(0,0,dp(8),0)}}; s.addView(TextView(this).apply{ text=toBnNum(v); textSize=15f; setTypeface(null,Typeface.BOLD); setTextColor(colGreen); gravity=Gravity.CENTER}); s.addView(TextView(this).apply{ text=lb; textSize=9f; setTextColor(colMuted); gravity=Gravity.CENTER}); summaryBox.addView(s)}; chartBars.removeAllViews(); val maxV=max(1,per.values.maxOrNull()?:1); per.forEach{ (k,v)-> val pct=if(allMonth.isNotEmpty()) v*100/allMonth.size else 0; val barCol=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL; layoutParams=LinearLayout.LayoutParams(0,-1,1f).apply{setMargins(dp(4),0,dp(4),0)}}; val track=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; gravity=Gravity.BOTTOM; layoutParams=LinearLayout.LayoutParams(-1,dp(90)); background=GradientDrawable().apply{ setColor(Color.parseColor("#FEF3C7")); cornerRadii=floatArrayOf(dpF(10f),dpF(10f),dpF(4f),dpF(4f),dpF(4f),dpF(4f),dpF(10f),dpF(10f))}}; val fillH=max(dp(10),(v.toFloat()/maxV*70).toInt()); val fill=TextView(this).apply{ text=if(v>0)toBnNum(v) else ""; textSize=10f; setTypeface(null,Typeface.BOLD); gravity=Gravity.CENTER; setTextColor(if(pct<40)colGreen else Color.parseColor("#FFF6C8")); layoutParams=LinearLayout.LayoutParams(-1,fillH); background=GradientDrawable().apply{ setColors(if(pct<40) intArrayOf(Color.parseColor("#D4A017"),Color.parseColor("#FFF1A0")) else intArrayOf(colGreen,Color.parseColor("#1B4A3A"))); cornerRadii=floatArrayOf(dpF(10f),dpF(10f),dpF(4f),dpF(4f),dpF(4f),dpF(4f),dpF(10f),dpF(10f))}}; track.addView(fill); barCol.addView(track); barCol.addView(TextView(this).apply{ text="${labels[k]?.first} ${labels[k]?.second}"; textSize=11f; setTypeface(null,Typeface.BOLD); setTextColor(colGreen); gravity=Gravity.CENTER; setPadding(0,dp(6),0,0)}); barCol.addView(TextView(this).apply{ text="${toBnNum(pct)}%"; textSize=9f; setTextColor(colMuted); gravity=Gravity.CENTER}); chartBars.addView(barCol)}; chartSub.text="${toBnNum(allMonth.size)} দিনে • ${toBnNum(totalDone)} ওয়াক্ত আদায়" }
    private fun getNextPrayerInfo(): PrayerInfo { val now=java.util.Calendar.getInstance(); val nowM=now.get(java.util.Calendar.HOUR_OF_DAY)*60+now.get(java.util.Calendar.MINUTE); var active:String?=null; var activeIdx=-1; for(i in prayerOrder.indices){ val k=prayerOrder[i].k; val s=parseM(todayData?.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("start"),k); var e=parseMEnd(todayData?.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("end"),k,s); if(s!=null&&e!=null&&nowM>=s&&nowM<e){ active=k; activeIdx=i; break } }; var next:PrayerMeta?=null; var isTomorrow=false; if(active!=null){ next=prayerOrder.getOrNull(activeIdx+1)?:prayerOrder[0]; if(activeIdx+1>=prayerOrder.size) isTomorrow=true } else { for(m in prayerOrder){ val s=parseM(todayData?.optJSONObject("prayer_times")?.optJSONObject(m.k)?.optString("start"),m.k); if(s!=null&&s>nowM){ next=m; break } }; if(next==null){ next=prayerOrder[0]; isTomorrow=true } }; return PrayerInfo(active,activeIdx,next,isTomorrow) }
    private fun startCountdown(){ countdownRunnable?.let{countdownHandler.removeCallbacks(it)}; val r=object:Runnable{ override fun run(){ tickCountdown(); countdownHandler.postDelayed(this,1000) } }; countdownRunnable=r; countdownHandler.post(r) }
    private fun tickCountdown(){ val td=todayData?:return; val now=java.util.Calendar.getInstance(); val info=getNextPrayerInfo(); val k:String; val tStr:String?; if(info.active!=null){ k=info.active; tStr=td.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("end") } else { k=info.next?.k?:"fajr"; tStr=td.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("start") }; if(tStr==null) return; var th=tStr.split(":")[0].toIntOrNull()?:0; var tm=tStr.split(":").getOrNull(1)?.toIntOrNull()?:0; if(k=="asr"||k=="maghrib"||k=="isha"&&th<12) th+=12; val target=java.util.Calendar.getInstance().apply{ set(java.util.Calendar.HOUR_OF_DAY,th); set(java.util.Calendar.MINUTE,tm); set(java.util.Calendar.SECOND,0) }; if(info.isTomorrow&&info.active==null) target.add(java.util.Calendar.DAY_OF_YEAR,1); if(target.before(now)&&info.active==null) target.add(java.util.Calendar.DAY_OF_YEAR,1); var diff=max(0,((target.timeInMillis-now.timeInMillis)/1000).toInt()); val h=diff/3600; diff%=3600; val m=diff/60; val s=diff%60; tvH.text=toBn(String.format("%02d",h)); tvM.text=toBn(String.format("%02d",m)); tvS.text=toBn(String.format("%02d",s)); if(info.active!=null){ countLabel.text="${td.optJSONObject("prayer_times")?.optJSONObject(info.active)?.optString("label_bn")?:""} শেষ হতে"; countPray.text=td.optJSONObject("prayer_times")?.optJSONObject(info.active)?.optString("label_bn")?:""; countIcon.text="⏳" } else { countLabel.text="পরবর্তী নামাজ"; countPray.text=td.optJSONObject("prayer_times")?.optJSONObject(k)?.optString("label_bn")?:k; countIcon.text=info.next?.ic?:"🕌" }; checkAzan(info, (target.timeInMillis-now.timeInMillis)/1000) }
    private fun createNotificationChannel(){ if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){ val ch=NotificationChannel("azan_channel","Azan",NotificationManager.IMPORTANCE_HIGH); getSystemService(NotificationManager::class.java).createNotificationChannel(ch) } }
    private fun toggleNotification(){ if(!notifEnabled){ if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){ ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),101); return }; notifEnabled=true; prefs.edit().putBoolean("azan_notif",true).apply(); bellBtn.text="🔔"; showToast("আজান নোটিফিকেশন চালু") } else { notifEnabled=false; prefs.edit().putBoolean("azan_notif",false).apply(); bellBtn.text="🔕"; showToast("বন্ধ") } }
    private fun checkAzan(info: PrayerInfo, diff: Long){ if(!notifEnabled) return; val now=java.util.Calendar.getInstance(); if(info.active==null&&diff in 295..305){ val k="pre-${info.next?.k}-${now.get(java.util.Calendar.DAY_OF_MONTH)}"; if(lastNotifiedKey!=k){ lastNotifiedKey=k; showSystemNoti("পরবর্তী নামাজ: ${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")}","৫ মিনিট বাকি"); showToast("${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")} ৫ মিনিট বাকি") } }; if(info.active==null&&diff<=2){ val k="exact-${info.next?.k}-${now.get(java.util.Calendar.DAY_OF_MONTH)}-${now.get(java.util.Calendar.HOUR_OF_DAY)}"; if(lastNotifiedKey!=k){ lastNotifiedKey=k; showSystemNoti("${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")} এর সময় হয়েছে","এখন ওয়াক্ত"); showToast("${todayData?.optJSONObject("prayer_times")?.optJSONObject(info.next?.k)?.optString("label_bn")} এর সময়") } } }
    private fun showSystemNoti(t:String,b:String){ try{ val builder=NotificationCompat.Builder(this,"azan_channel").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(t).setContentText(b).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true); NotificationManagerCompat.from(this).notify((System.currentTimeMillis()%10000).toInt(),builder.build()) } catch(_:Exception){} }
    private fun showToast(msg:String){ toastMsg.text=msg; toastView.visibility=View.VISIBLE; toastView.alpha=0f; toastView.animate().alpha(1f).setDuration(300).start(); Handler(Looper.getMainLooper()).postDelayed({ toastView.animate().alpha(0f).setDuration(300).withEndAction{ toastView.visibility=View.GONE }.start() },3500) }
    private fun openModal(){ modalOverlay.visibility=View.VISIBLE; tempSelected=selectedCity; renderCityListFiltered() }
    private fun closeModal(){ modalOverlay.visibility=View.GONE }
    private fun saveCity(){ tempSelected?.let{ selectedCity=it; prefs.edit().putString("prayer_city_json",JSONObject().apply{ put("name_en",it.name_en); put("name_bn",it.name_bn); put("division",it.division) }.toString()).apply(); closeModal(); prayerListContainer.removeAllViews(); prayerListContainer.addView(TextView(this).apply{ text="লোড হচ্ছে..."; gravity=Gravity.CENTER; setTextColor(colMuted); setPadding(dp(10),dp(30),dp(10),dp(30)) }); lifecycleScope.launch{ try{ loadCityData(selectedCity.name_en) } catch(_:Exception){} } } }
    override fun onDestroy(){ countdownRunnable?.let{countdownHandler.removeCallbacks(it)}; super.onDestroy() }
}
