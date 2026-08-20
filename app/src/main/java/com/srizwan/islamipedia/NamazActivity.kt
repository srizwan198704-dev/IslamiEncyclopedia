package com.srizwan.islamipedia

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

// Vector Masjid - pure Canvas, no drawable no res
class MasjidVectorView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {
    private val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#C9A227"); style = Paint.Style.FILL }
    private val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFF6C8"); style = Paint.Style.FILL; alpha = 230 }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2A5A4A"); style = Paint.Style.STROKE; strokeWidth = 2f }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat(); if (w==0f||h==0f) return
        val cx = w/2f
        val dome = Path()
        dome.moveTo(cx-w*0.18f, h*0.70f)
        dome.cubicTo(cx-w*0.15f, h*0.15f, cx+w*0.15f, h*0.15f, cx+w*0.18f, h*0.70f)
        dome.close(); canvas.drawPath(dome, gold)
        listOf(-0.32f to 0.10f, 0.32f to 0.10f).forEach { (off,sz) ->
            val px = cx+w*off; val p = Path()
            p.moveTo(px-w*sz, h*0.80f); p.cubicTo(px-w*sz*0.8f, h*0.45f, px+w*sz*0.8f, h*0.45f, px+w*sz, h*0.80f); p.close()
            canvas.drawPath(p, white)
        }
        val mw = w*0.045f
        listOf(cx-w*0.42f, cx+w*0.42f).forEach { mx ->
            canvas.drawRect(RectF(mx-mw/2, h*0.20f, mx+mw/2, h), white)
            canvas.drawCircle(mx, h*0.18f, mw, gold)
        }
        listOf(-0.20f to 0.12f, 0f to 0.14f, 0.20f to 0.12f).forEach { (off,aw) ->
            val ax = cx+w*off; val awPx = w*aw; val arch = Path()
            arch.moveTo(ax-awPx, h); arch.cubicTo(ax-awPx, h*0.70f, ax+awPx, h*0.70f, ax+awPx, h); arch.close()
            canvas.drawPath(arch, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A4536"); style=Paint.Style.STROKE; strokeWidth=2f })
        }
        canvas.drawText("\u263E", cx-10f, h*0.16f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.parseColor("#FFF6C8"); textSize=h*0.22f })
    }
    override fun onMeasure(wMS: Int, hMS: Int) {
        val w = MeasureSpec.getSize(wMS); setMeasuredDimension(w, (w*0.30f).toInt().coerceAtLeast(70))
    }
}

class NamazActivity : AppCompatActivity() {
    private val CITIES_URL = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia/BangladeshCities.json"
    private val CITY_BASE = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PrayertimePedia@main/BD/"
    private val FONT_HIND_URL = "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/hindsiliguri/HindSiliguri-Regular.ttf"
    private val FONT_HIND_BOLD_URL = "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/hindsiliguri/HindSiliguri-Bold.ttf"
    private val FONT_ANEK_BOLD_URL = "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/anekbangla/AnekBangla-Bold.ttf"

    private lateinit var prefs: SharedPreferences
    private var selectedCityEn = "Dhaka"
    private var selectedCityBn = "\u09A2\u09BE\u0995\u09BE"
    private var allCities = JSONArray()
    private var allMonth = JSONArray()
    private var todayData: JSONObject? = null
    private var filterWaqt = "all"

    private var tfHind: Typeface? = null
    private var tfHindBold: Typeface? = null
    private var tfAnekBold: Typeface? = null

    private lateinit var cityTv: TextView
    private lateinit var hijriTv: TextView
    private lateinit var bengaliTv: TextView
    private lateinit var todayBadge: TextView
    private lateinit var prayerContainer: LinearLayout
    private lateinit var forbiddenContainer: LinearLayout
    private lateinit var naflContainer: LinearLayout
    private lateinit var summaryContainer: LinearLayout
    private lateinit var trackerCountTv: TextView
    private lateinit var trackerMonthTv: TextView
    private lateinit var dayContainer: LinearLayout
    private lateinit var countIcon: TextView
    private lateinit var countLabel: TextView
    private lateinit var countPray: TextView
    private lateinit var countCity: TextView
    private lateinit var hTv: TextView
    private lateinit var mTv: TextView
    private lateinit var sTv: TextView
    private lateinit var sunriseTv: TextView
    private lateinit var sunsetTv: TextView
    private lateinit var rootContent: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private val order = listOf(
        Triple("fajr","\uD83C\uDF19","\u09AB\u099C\u09B0"),
        Triple("dhuhr","\u2600\uFE0F","\u09AF\u09C1\u09B9\u09B0"),
        Triple("asr","\uD83C\uDF24\uFE0F","\u0986\u09B8\u09B0"),
        Triple("maghrib","\uD83C\uDF07","\u09AE\u09BE\u0997\u09B0\u09BF\u09AC"),
        Triple("isha","\uD83C\uDF0C","\u0987\u09B6\u09BE")
    )

    private fun dp(v: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("islamipedia_prefs", Context.MODE_PRIVATE)
        selectedCityEn = prefs.getString("city_en","Dhaka")!!
        selectedCityBn = prefs.getString("city_bn","\u09A2\u09BE\u0995\u09BE")!!
        setContentView(buildUi())
        loadFontsFromInternet()
        loadCities()
        loadCityData(selectedCityEn)
        startAutoRefresh()
    }

    private fun loadFontsFromInternet(){
        thread{
            try{
                tfHind = downloadTypeface(FONT_HIND_URL,"HindSiliguri-Regular.ttf")
                tfHindBold = downloadTypeface(FONT_HIND_BOLD_URL,"HindSiliguri-Bold.ttf")?:tfHind
                tfAnekBold = downloadTypeface(FONT_ANEK_BOLD_URL,"AnekBangla-Bold.ttf")?:tfHindBold
                handler.post{ applyFonts() }
            }catch(_:Exception){}
        }
    }
    private fun downloadTypeface(urlStr:String,fileName:String):Typeface?{
        return try{
            val file = File(cacheDir,fileName)
            if(!file.exists()||file.length()<1000){
                val conn = URL(urlStr).openConnection() as HttpURLConnection
                conn.connectTimeout=15000; conn.readTimeout=15000; conn.connect()
                if(conn.responseCode==200){ conn.inputStream.use{ input-> FileOutputStream(file).use{ out-> input.copyTo(out) } } }
            }
            if(file.exists()&&file.length()>1000) Typeface.createFromFile(file) else null
        }catch(_:Exception){ null }
    }
    private fun applyFonts(){
        if(tfHind==null) return
        try{
            fun rec(v:View){
                if(v is TextView){
                    val txt=v.text.toString()
                    val isTitle=txt.contains("নামাজের সময়সূচি")||txt.contains("ইসলামী বিশ্বকোষ")
                    v.typeface = if(isTitle) tfAnekBold else if(v.typeface?.isBold==true) tfHindBold else tfHind
                }else if(v is ViewGroup){ for(i in 0 until v.childCount) rec(v.getChildAt(i)) }
            }
            rec(rootContent)
        }catch(_:Exception){}
    }

    private fun buildUi(): View {
        val act = this
        val outer = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#FDFBF6")) }
        val scroll = ScrollView(act).apply{ layoutParams=LinearLayout.LayoutParams(-1,0,1f) }
        rootContent = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(14),dp(14),dp(14),dp(100)) }

        val header = LinearLayout(act).apply{
            orientation=LinearLayout.VERTICAL
            val gd = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#102E26"), Color.parseColor("#0A201A")))
            gd.cornerRadii = floatArrayOf(0f,0f,0f,0f, dp(32).toFloat(), dp(32).toFloat(), dp(32).toFloat(), dp(32).toFloat())
            background=gd; setPadding(dp(16),dp(16),dp(16),dp(8))
        }
        val topRow = LinearLayout(act).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL }
        val appName = TextView(act).apply{ text="ইসলামী বিশ্বকোষ ও আল হাদিস S2"; setTextColor(Color.WHITE); setTextSize(TypedValue.COMPLEX_UNIT_SP,15f); typeface=Typeface.DEFAULT_BOLD }
        val appSub = TextView(act).apply{ text="নামাজের সময়সূচি • Masjid Edition"; setTextColor(Color.parseColor("#B0C4B8")); setTextSize(TypedValue.COMPLEX_UNIT_SP,11f) }
        brand.addView(appName); brand.addView(appSub)
        cityTv = TextView(act).apply{
            text="📍 $selectedCityBn ▼"; setTextColor(Color.WHITE); setPadding(dp(12),dp(7),dp(12),dp(7))
            val bg=GradientDrawable(); bg.setColor(Color.parseColor("#1A4536")); bg.cornerRadius=dp(20).toFloat(); bg.setStroke(dp(1), Color.parseColor("#2A5A4A")); background=bg
            setOnClickListener{ showCityDialog() }
        }
        topRow.addView(brand, LinearLayout.LayoutParams(0,-2,1f)); topRow.addView(cityTv)
        hijriTv = TextView(act).apply{ text="লোড হচ্ছে..."; setTextColor(Color.parseColor("#FFF6C8")); setTextSize(TypedValue.COMPLEX_UNIT_SP,15f); typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER; setPadding(0,dp(18),0,0) }
        bengaliTv = TextView(act).apply{ setTextColor(Color.parseColor("#C8DDD6")); setTextSize(TypedValue.COMPLEX_UNIT_SP,12f); gravity=Gravity.CENTER }
        val title = TextView(act).apply{ text="নামাজের সময়সূচি"; setTextColor(Color.WHITE); setTextSize(TypedValue.COMPLEX_UNIT_SP,28f); typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER; setPadding(0,dp(8),0,0) }
        val sunRow = LinearLayout(act).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
        sunriseTv = TextView(act).apply{ text="সূর্যোদয় --"; setTextColor(Color.parseColor("#FFF6C8")); setTextSize(TypedValue.COMPLEX_UNIT_SP,11f) }
        sunsetTv = TextView(act).apply{ text="সূর্যাস্ত --"; setTextColor(Color.parseColor("#FFF6C8")); setTextSize(TypedValue.COMPLEX_UNIT_SP,11f); setPadding(dp(20),0,0,0) }
        sunRow.addView(sunriseTv); sunRow.addView(sunsetTv)
        val masjidView = MasjidVectorView(act).apply{ layoutParams=LinearLayout.LayoutParams(-1, dp(70)) }
        header.addView(topRow); header.addView(hijriTv); header.addView(bengaliTv); header.addView(title); header.addView(sunRow); header.addView(masjidView)

        val prayerCard = createCard()
        val prayerHead = createCardHead("আজকের ওয়াক্ত","আজ"); todayBadge=prayerHead.second
        prayerCard.addView(prayerHead.first)
        prayerContainer = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }
        prayerCard.addView(prayerContainer)

        val sideRow = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(0,dp(14),0,0) }
        val forbCard = createCard(); forbCard.addView(createCardHead("নামাজের নিষিদ্ধ সময়সূচী",null).first)
        forbiddenContainer = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }; forbCard.addView(forbiddenContainer)
        val naflCard = createCard(); naflCard.addView(createCardHead("নফল নামাজের সময়সূচী",null).first)
        naflContainer = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }; naflCard.addView(naflContainer)
        sideRow.addView(forbCard); sideRow.addView(View(act).apply{ layoutParams=LinearLayout.LayoutParams(-1,dp(12)) }); sideRow.addView(naflCard)

        val trackerCard = createCard()
        val trackerHead = createCardHead("সম্পূর্ণ মাসের নামাজের ট্র্যাকার",null)
        trackerMonthTv = TextView(act).apply{ text="—"; setTextSize(TypedValue.COMPLEX_UNIT_SP,10f); val bg=GradientDrawable(); bg.setColor(Color.parseColor("#FFF6C8")); bg.cornerRadius=dp(10).toFloat(); background=bg; setPadding(dp(8),dp(4),dp(8),dp(4)) }
        trackerCountTv = TextView(act).apply{ text="0/0"; setTextSize(TypedValue.COMPLEX_UNIT_SP,10f); val bg=GradientDrawable(); bg.setColor(Color.parseColor("#ECFDF5")); bg.cornerRadius=dp(10).toFloat(); background=bg; setPadding(dp(8),dp(4),dp(8),dp(4)) }
        val badgeRow = LinearLayout(act).apply{ addView(trackerMonthTv, LinearLayout.LayoutParams(-2,-2).apply{ setMargins(0,0,dp(6),0) }); addView(trackerCountTv) }
        (trackerHead.first as LinearLayout).addView(badgeRow)
        trackerCard.addView(trackerHead.first)
        summaryContainer = LinearLayout(act).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }
        trackerCard.addView(HorizontalScrollView(act).apply{ addView(summaryContainer) })
        val filterRow = LinearLayout(act).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }
        listOf("all" to "সব","fajr" to "🌙 ফজর","dhuhr" to "☀️ যুহর","asr" to "🌤️ আসর","maghrib" to "🌇 মাগরিব","isha" to "🌌 ইশা").forEach{ (k,lbl)->
            val chip = TextView(act).apply{
                text=lbl; tag=k; setPadding(dp(10),dp(6),dp(10),dp(6)); setTextSize(TypedValue.COMPLEX_UNIT_SP,11f)
                val bg=GradientDrawable(); bg.cornerRadius=dp(20).toFloat()
                if(k==filterWaqt){ bg.setColor(Color.parseColor("#0E3B2E")); setTextColor(Color.WHITE) } else { bg.setColor(Color.WHITE); bg.setStroke(1, Color.parseColor("#EFE5C8")); setTextColor(Color.BLACK) }
                background=bg
                setOnClickListener{
                    filterWaqt=k
                    for(i in 0 until filterRow.childCount){
                        val c=filterRow.getChildAt(i) as TextView
                        val b=c.background as GradientDrawable
                        if(c.tag==filterWaqt){ b.setColor(Color.parseColor("#0E3B2E")); c.setTextColor(Color.WHITE) } else { b.setColor(Color.WHITE); c.setTextColor(Color.BLACK) }
                    }
                    renderTracker()
                }
            }
            filterRow.addView(chip, LinearLayout.LayoutParams(-2,-2).apply{ setMargins(dp(4),0,dp(4),0) })
        }
        trackerCard.addView(filterRow)
        dayContainer = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(8),dp(8),dp(8),dp(8)) }
        trackerCard.addView(dayContainer)

        rootContent.addView(header)
        rootContent.addView(prayerCard, LinearLayout.LayoutParams(-1,-2).apply{ topMargin=dp(14) })
        rootContent.addView(sideRow)
        rootContent.addView(trackerCard, LinearLayout.LayoutParams(-1,-2).apply{ topMargin=dp(14) })
        scroll.addView(rootContent)
        outer.addView(scroll)

        val countBox = LinearLayout(act).apply{
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
            val bg=GradientDrawable(); bg.setColor(Color.parseColor("#0E3B2E")); bg.cornerRadius=dp(20).toFloat(); bg.setStroke(1, Color.parseColor("#2A5A4A")); background=bg
            setPadding(dp(12),dp(12),dp(12),dp(12)); elevation=dp(12).toFloat()
        }
        countIcon = TextView(act).apply{ text="🕌"; setTextSize(TypedValue.COMPLEX_UNIT_SP,18f); val bg=GradientDrawable(); bg.setColor(Color.parseColor("#FFF1A0")); bg.cornerRadius=dp(12).toFloat(); background=bg; setPadding(dp(10),dp(8),dp(10),dp(8)) }
        val countMid = LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(10),0,0,0) }
        countLabel = TextView(act).apply{ text="পরবর্তী নামাজ"; setTextColor(Color.parseColor("#A0C4B8")); setTextSize(TypedValue.COMPLEX_UNIT_SP,9f) }
        countPray = TextView(act).apply{ text="যুহর"; setTextColor(Color.WHITE); setTextSize(TypedValue.COMPLEX_UNIT_SP,15f); typeface=Typeface.DEFAULT_BOLD }
        countCity = TextView(act).apply{ text=selectedCityBn; setTextColor(Color.parseColor("#8AB0A0")); setTextSize(TypedValue.COMPLEX_UNIT_SP,10f) }
        countMid.addView(countLabel); countMid.addView(countPray); countMid.addView(countCity)
        val timeRow = LinearLayout(act).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        hTv = createTimeTv(); mTv = createTimeTv(); sTv = createTimeTv()
        timeRow.addView(hTv); timeRow.addView(createSmallTv(" ঘ ")); timeRow.addView(mTv); timeRow.addView(createSmallTv(" মি ")); timeRow.addView(sTv); timeRow.addView(createSmallTv(" সে"))
        countBox.addView(countIcon); countBox.addView(countMid, LinearLayout.LayoutParams(0,-2,1f)); countBox.addView(timeRow)
        outer.addView(countBox, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(dp(12),dp(8),dp(12),dp(12)) })
        return outer
    }

    private fun createCard(): LinearLayout = LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL
        val bg=GradientDrawable(); bg.setColor(Color.WHITE); bg.cornerRadius=dp(24).toFloat(); bg.setStroke(1, Color.parseColor("#EFE5C8")); background=bg; elevation=dp(2).toFloat()
    }
    private fun createCardHead(title:String,badgeText:String?):Pair<LinearLayout,TextView>{
        val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(14),dp(12),dp(14),dp(10)) }
        val tv=TextView(this).apply{ text=title; setTextSize(TypedValue.COMPLEX_UNIT_SP,15f); typeface=Typeface.DEFAULT_BOLD }
        row.addView(tv, LinearLayout.LayoutParams(0,-2,1f))
        val badge=TextView(this).apply{
            text=badgeText?:""; visibility=if(badgeText!=null) View.VISIBLE else View.GONE
            setPadding(dp(8),dp(4),dp(8),dp(4)); setTextSize(TypedValue.COMPLEX_UNIT_SP,10f)
            val bg=GradientDrawable(); bg.setColor(Color.parseColor("#FFF6C8")); bg.cornerRadius=dp(10).toFloat(); background=bg
        }
        row.addView(badge)
        val line=View(this).apply{ setBackgroundColor(Color.parseColor("#EFE5C8")); layoutParams=LinearLayout.LayoutParams(-1,dp(1)) }
        val wrapper=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; addView(row); addView(line) }
        return Pair(wrapper,badge)
    }
    private fun createTimeTv(): TextView = TextView(this).apply{ text="00"; setTextColor(Color.WHITE); setTextSize(TypedValue.COMPLEX_UNIT_SP,20f); typeface=Typeface.DEFAULT_BOLD }
    private fun createSmallTv(t:String): TextView = TextView(this).apply{ text=t; setTextColor(Color.parseColor("#A0C4B8")); setTextSize(TypedValue.COMPLEX_UNIT_SP,10f) }

    private fun httpGet(urlStr:String):String{
        val url=URL(urlStr); val conn=url.openConnection() as HttpURLConnection
        conn.requestMethod="GET"; conn.setRequestProperty("Cache-Control","no-cache")
        conn.connectTimeout=15000; conn.readTimeout=15000; conn.connect()
        if(conn.responseCode!=200) throw Exception("HTTP ${conn.responseCode}")
        return conn.inputStream.bufferedReader().readText()
    }
    private fun loadCities(){
        thread{
            try{
                val txt=httpGet("$CITIES_URL?v=${System.currentTimeMillis()}")
                allCities=JSONArray(txt)
            }catch(_:Exception){ allCities=JSONArray("[{\"name_en\":\"Dhaka\",\"name_bn\":\"ঢাকা\",\"division\":\"Dhaka\"}]") }
        }
    }
    private fun loadCityData(en:String){
        thread{
            try{
                val url="$CITY_BASE${en}.json?v=${System.currentTimeMillis()}"
                val txt=httpGet(url)
                val arr=JSONArray(txt)
                val newStamp=arr.optJSONObject(0)?.optJSONObject("meta")?.optString("scraped_at")?:""
                prefs.edit().putString("last_scraped_$en",newStamp).putLong("last_fetch_$en",System.currentTimeMillis()).apply()
                allMonth=arr
                val cal=Calendar.getInstance()
                val idx=(cal.get(Calendar.DAY_OF_MONTH)-1).coerceAtMost(arr.length()-1).coerceAtLeast(0)
                todayData=arr.getJSONObject(idx)
                handler.post{ renderAll() }
            }catch(e:Exception){ handler.post{ Toast.makeText(this@NamazActivity,"লোড ব্যর্থ: ${e.message}",Toast.LENGTH_SHORT).show() } }
        }
    }
    private fun startAutoRefresh(){
        handler.postDelayed(object:Runnable{
            override fun run(){
                val last=prefs.getLong("last_fetch_$selectedCityEn",0)
                if(System.currentTimeMillis()-last>6*60*60*1000) loadCityData(selectedCityEn)
                handler.postDelayed(this,60*60*1000)
            }
        },60*60*1000)
    }

    private fun renderAll(){
        val data=todayData?:return
        try{
            val hijri=data.optJSONObject("date")?.optJSONObject("hijri")?.optString("bn")?:data.optJSONObject("date")?.optJSONObject("full")?.optString("bn")?.split("•")?.getOrNull(0)?:""
            val bengali=data.optJSONObject("date")?.optJSONObject("bengali")?.optString("bn")?:data.optJSONObject("date")?.optJSONObject("full")?.optString("bn")?.split("•")?.getOrNull(1)?:""
            hijriTv.text=hijri; bengaliTv.text=bengali
            todayBadge.text=data.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?:selectedCityBn
            cityTv.text="📍 ${data.optJSONObject("meta")?.optJSONObject("location")?.optString("city_bn")?:selectedCityBn} ▼"
            trackerMonthTv.text=data.optJSONObject("date")?.optJSONObject("full")?.optString("bn")?.take(42)?:""
            val forb=data.optJSONObject("forbidden_times")
            sunriseTv.text="সূর্যোদয় ${forb?.optJSONObject("sunrise")?.optString("time_bn")?.split(" - ")?.getOrNull(0)?:""}"
            sunsetTv.text="সূর্যাস্ত ${forb?.optJSONObject("sunset")?.optString("time_bn")?.split(" - ")?.getOrNull(1)?:""}"
            val prayerTimes=data.optJSONObject("prayer_times")
            val nowM=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60+Calendar.getInstance().get(Calendar.MINUTE)
            var activeKey:String?=null; var activeIdx=-1
            order.forEachIndexed{i,(k,_,_)-> val s=parseM(prayerTimes?.optJSONObject(k)?.optString("start")); val e=parseM(prayerTimes?.optJSONObject(k)?.optString("end")); if(s!=null&&e!=null&&nowM>=s&&nowM<e){ activeKey=k; activeIdx=i } }
            var nextKey:String?=null; var nextTriple:Triple<String,String,String>?=null
            if(activeKey==null){
                for(o in order){ val s=parseM(prayerTimes?.optJSONObject(o.first)?.optString("start")); if(s!=null&&s>nowM){ nextKey=o.first; nextTriple=o; break } }
                if(nextKey==null){ nextKey=order[0].first; nextTriple=order[0] }
            }else{ val nextIdx=(activeIdx+1)%order.size; nextKey=order[nextIdx].first; nextTriple=order[nextIdx] }

            prayerContainer.removeAllViews()
            order.forEach{ (k,ic,_)->
                val pt=prayerTimes?.optJSONObject(k)?:return@forEach
                val label=pt.optString("label_bn",k)
                val timeBn=pt.optString("time_bn","")
                val isActive=k==activeKey; val isNext=k==nextKey&&!isActive
                val row=LinearLayout(this).apply{
                    orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(12),dp(12),dp(12),dp(12))
                    val bg=GradientDrawable(); bg.cornerRadius=dp(14).toFloat()
                    when{ isActive->{ bg.setColor(Color.parseColor("#0E3B2E")) } isNext->{ bg.setColor(Color.parseColor("#FFF3B8")); bg.setStroke(1, Color.parseColor("#C9A227")) } else->{ bg.setColor(Color.parseColor("#FFFCF0")); bg.setStroke(1, Color.parseColor("#EFE5C8")) }
                    background=bg
                }
                val icon=TextView(this).apply{ text=ic; setTextSize(TypedValue.COMPLEX_UNIT_SP,20f); val b=GradientDrawable(); b.setColor(Color.parseColor("#FAF6E8")); b.cornerRadius=dp(12).toFloat(); b.setStroke(1, Color.parseColor("#EFE5C8")); background=b; setPadding(dp(8),dp(6),dp(8),dp(6)) }
                val name=TextView(this).apply{ text=label; setTextSize(TypedValue.COMPLEX_UNIT_SP,15f); typeface=tfHindBold?:Typeface.DEFAULT_BOLD; if(isActive) setTextColor(Color.WHITE); setPadding(dp(10),0,0,0) }
                val time=TextView(this).apply{ text=timeBn; setTextSize(TypedValue.COMPLEX_UNIT_SP,14f); typeface=tfHindBold?:Typeface.DEFAULT_BOLD; if(isActive) setTextColor(Color.parseColor("#FFF6C8")) else setTextColor(Color.BLACK); gravity=Gravity.END }
                row.addView(icon); row.addView(name, LinearLayout.LayoutParams(0,-2,1f)); row.addView(time)
                prayerContainer.addView(row, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,dp(4),0,dp(4)) })
            }
            forbiddenContainer.removeAllViews()
            listOf("sunrise","noon","sunset").forEach{ fk->
                val d=forb?.optJSONObject(fk)?:return@forEach
                val row=TextView(this).apply{
                    text="🚫 ${d.optString("label_bn")} : ${d.optString("time_bn")}"
                    setPadding(dp(10),dp(10),dp(10),dp(10))
                    val bg=GradientDrawable(); bg.setColor(Color.parseColor("#FFFBEB")); bg.setStroke(1, Color.parseColor("#F8E9B0")); bg.cornerRadius=dp(12).toFloat(); background=bg
                    setTextSize(TypedValue.COMPLEX_UNIT_SP,12f); typeface=tfHind
                }
                forbiddenContainer.addView(row, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,dp(4),0,dp(4)) })
            }
            naflContainer.removeAllViews()
            val nafl=data.optJSONObject("nafl_times")
            val naflItems=listOf(
                "তাহাজ্জুদ" to (nafl?.optJSONObject("tahajjud")?.optString("time_bn")?:"-"),
                "সাহরী শেষ" to (nafl?.optJSONObject("tahajjud")?.optString("time_bn")?:prayerTimes?.optJSONObject("fajr")?.optString("time_bn")?.split(" - ")?.getOrNull(0)?:"-"),
                "ইশরাক" to (nafl?.optJSONObject("ishraq")?.optString("time_bn")?:"-"),
                "চাশত" to (nafl?.optJSONObject("chasht")?.optString("time_bn")?:"-")
            )
            naflItems.forEach{ (lbl,time)->
                val card=LinearLayout(this).apply{
                    orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(10),dp(10),dp(10))
                    val bg=GradientDrawable(); bg.setColor(Color.WHITE); bg.setStroke(1, Color.parseColor("#EFE5C8")); bg.cornerRadius=dp(14).toFloat(); background=bg
                }
                val l=TextView(this).apply{ text=lbl; setTextSize(TypedValue.COMPLEX_UNIT_SP,10f); setTextColor(Color.GRAY); typeface=tfHind }
                val t=TextView(this).apply{ text=time; setTextSize(TypedValue.COMPLEX_UNIT_SP,16f); typeface=tfHindBold?:Typeface.DEFAULT_BOLD; setTextColor(Color.parseColor("#0E3B2E")) }
                card.addView(l); card.addView(t)
                naflContainer.addView(card, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,dp(4),0,dp(4)) })
            }
            renderTracker()
            startCountdown(nextTriple, activeKey)
            applyFonts()
        }catch(e:Exception){ e.printStackTrace() }
    }

    private fun renderTracker(){
        dayContainer.removeAllViews(); summaryContainer.removeAllViews()
        if(allMonth.length()==0) return
        val store=JSONObject(prefs.getString("salat_tracker_v2","{}")?:"{}")
        val todayIdx=Calendar.getInstance().get(Calendar.DAY_OF_MONTH)-1
        var totalDone=0; val per=mutableMapOf("fajr" to 0,"dhuhr" to 0,"asr" to 0,"maghrib" to 0,"isha" to 0)
        val keys=store.keys(); while(keys.hasNext()){ val k=keys.next(); if(k.startsWith("${selectedCityEn}-")&&store.optBoolean(k,false)){ totalDone++; val wk=k.split("-").last(); per[wk]=(per[wk]?:0)+1 } }
        val totalWaqt=allMonth.length()*5
        trackerCountTv.text="${toBn(totalDone)}/${toBn(totalWaqt)}"
        listOf("মোট ${toBn(totalDone)}","ফজর ${toBn(per["fajr"]?:0)}","যুহর ${toBn(per["dhuhr"]?:0)}","আসর ${toBn(per["asr"]?:0)}","মাগরিব ${toBn(per["maghrib"]?:0)}","ইশা ${toBn(per["isha"]?:0)}","${if(totalWaqt>0) toBn((totalDone*100/totalWaqt))+"%" else "০%"} অগ্রগতি").forEach{ txt->
            val tv=TextView(this).apply{ text=txt; setPadding(dp(10),dp(6),dp(10),dp(6)); setTextSize(TypedValue.COMPLEX_UNIT_SP,11f); typeface=tfHind; val bg=GradientDrawable(); bg.setColor(Color.WHITE); bg.setStroke(1, Color.parseColor("#EFE5C8")); bg.cornerRadius=dp(12).toFloat(); background=bg }
            summaryContainer.addView(tv, LinearLayout.LayoutParams(-2,-2).apply{ setMargins(dp(4),0,dp(4),0) })
        }
        for(i in 0 until allMonth.length()){
            val d=allMonth.getJSONObject(i)
            val base="${selectedCityEn}-$i"
            val doneCount=order.count{ (k,_,_)-> store.optBoolean("$base-$k", false) }
            val dayCard=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(10),dp(10),dp(10))
                val bg=GradientDrawable(); bg.setColor(Color.WHITE); bg.setStroke(1, if(i==todayIdx) Color.parseColor("#C9A227") else Color.parseColor("#EFE5C8")); bg.cornerRadius=dp(16).toFloat(); background=bg
                if(i==todayIdx) elevation=dp(4).toFloat()
            }
            val head=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
            val num=TextView(this).apply{ text=toBn(i+1); setTextColor(Color.WHITE); gravity=Gravity.CENTER; val bg=GradientDrawable(); bg.setColor(Color.parseColor("#0E3B2E")); bg.cornerRadius=dp(8).toFloat(); background=bg; setPadding(dp(6),dp(4),dp(6),dp(4)); typeface=tfHindBold }
            val hijri=TextView(this).apply{ text="${d.optJSONObject("date")?.optJSONObject("hijri")?.optString("bn")?.split(",")?.getOrNull(0)?:""} • ${toBn(doneCount)}/৫"; setTextSize(TypedValue.COMPLEX_UNIT_SP,10f); setTextColor(Color.GRAY); gravity=Gravity.END; typeface=tfHind }
            head.addView(num); head.addView(hijri, LinearLayout.LayoutParams(0,-2,1f)); dayCard.addView(head)
            order.forEach{ (k,ic,bn)->
                if(filterWaqt!="all"&&k!=filterWaqt) return@forEach
                val key="$base-$k"; val isDone=store.optBoolean(key,false)
                val pt=d.optJSONObject("prayer_times")?.optJSONObject(k)
                val time=pt?.optString("time_bn")?.split(" - ")?.getOrNull(0)?:""
                val pill=LinearLayout(this).apply{
                    orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(8),dp(6),dp(8),dp(6))
                    val bg=GradientDrawable(); bg.cornerRadius=dp(10).toFloat()
                    if(isDone){ bg.setColor(Color.parseColor("#E8F5E9")); bg.setStroke(1, Color.parseColor("#A7D8B0")) } else { bg.setColor(Color.parseColor("#FAF6EB")); bg.setStroke(1, Color.parseColor("#EFE5C8")) }
                    background=bg
                    setOnClickListener{
                        val newStore=JSONObject(prefs.getString("salat_tracker_v2","{}")?:"{}")
                        if(newStore.optBoolean(key,false)) newStore.remove(key) else newStore.put(key,true)
                        prefs.edit().putString("salat_tracker_v2",newStore.toString()).apply()
                        renderTracker()
                    }
                }
                val lbl=TextView(this).apply{ text="$ic $bn $time"; setTextSize(TypedValue.COMPLEX_UNIT_SP,11f); typeface=tfHind; if(isDone) typeface=tfHindBold }
                val chk=TextView(this).apply{
                    text=if(isDone) "✓" else ""; gravity=Gravity.CENTER
                    val bg=GradientDrawable(); bg.cornerRadius=dp(6).toFloat()
                    if(isDone){ bg.setColor(Color.parseColor("#0E3B2E")); setTextColor(Color.WHITE) } else { bg.setColor(Color.WHITE); bg.setStroke(1, Color.parseColor("#D6D0BA")) }
                    background=bg; setPadding(dp(4),dp(2),dp(4),dp(2))
                }
                pill.addView(lbl, LinearLayout.LayoutParams(0,-2,1f)); pill.addView(chk)
                dayCard.addView(pill, LinearLayout.LayoutParams(-1,-2).apply{ topMargin=dp(4) })
            }
            dayContainer.addView(dayCard, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,dp(6),0,dp(6)) })
        }
    }

    private var countdownRunnable:Runnable?=null
    private fun startCountdown(nextTriple:Triple<String,String,String>?,activeKey:String?){
        countdownRunnable?.let{ handler.removeCallbacks(it) }
        val runnable=object:Runnable{
            override fun run(){
                val data=todayData?:return
                val prayerTimes=data.optJSONObject("prayer_times")
                val targetStr=if(activeKey!=null) prayerTimes?.optJSONObject(activeKey)?.optString("end") else prayerTimes?.optJSONObject(nextTriple?.first)?.optString("start")
                if(targetStr.isNullOrEmpty()) return
                val parts=targetStr.split(":"); if(parts.size<2) return
                val th=parts[0].toIntOrNull()?:0; val tm=parts[1].toIntOrNull()?:0
                val now=Calendar.getInstance()
                val target=Calendar.getInstance().apply{ set(Calendar.HOUR_OF_DAY,th); set(Calendar.MINUTE,tm); set(Calendar.SECOND,0) }
                if(target.before(now)&&activeKey==null) target.add(Calendar.DAY_OF_YEAR,1)
                val diff=(target.timeInMillis-now.timeInMillis)/1000
                val h=(diff/3600).coerceAtLeast(0); val m=((diff%3600)/60).coerceAtLeast(0); val s=(diff%60).coerceAtLeast(0)
                hTv.text=toBn(h.toInt().toString().padStart(2,'0')); mTv.text=toBn(m.toInt().toString().padStart(2,'0')); sTv.text=toBn(s.toInt().toString().padStart(2,'0'))
                if(activeKey!=null){
                    countLabel.text="${prayerTimes?.optJSONObject(activeKey)?.optString("label_bn")?:""} শেষ হতে"
                    countPray.text=prayerTimes?.optJSONObject(activeKey)?.optString("label_bn")?:""
                    countIcon.text="⏳"
                }else{
                    countLabel.text="পরবর্তী নামাজ"
                    countPray.text= prayerTimes?.optJSONObject(nextTriple?.first?:"")?.optString("label_bn")?:nextTriple?.third?:""
                    countIcon.text= nextTriple?.second?:"🕌"
                }
                countCity.text="${prefs.getString("city_bn","ঢাকা")} • ${if(activeKey!=null) "চলছে" else "বাকি"}"
                handler.postDelayed(this,1000)
            }
        }
        countdownRunnable=runnable; handler.post(runnable)
    }

    private fun parseM(t:String?):Int?{
        if(t.isNullOrEmpty()) return null
        val p=t.split(":"); if(p.size<2) return null
        return (p[0].toIntOrNull()?:0)*60+(p[1].toIntOrNull()?:0)
    }
    private fun toBn(n:Any):String = n.toString().replace(Regex("\\d")){ m-> "০১২৩৪৫৬৭৮৯"[m.value.toInt()].toString() }

    private fun showCityDialog(){
        val act=this
        val dialogView=LinearLayout(act).apply{ orientation=LinearLayout.VERTICAL; setPadding(dp(16),dp(16),dp(16),dp(16)) }
        val title=TextView(act).apply{ text="স্থান শনাক্ত করুন"; setTextSize(TypedValue.COMPLEX_UNIT_SP,17f); typeface=Typeface.DEFAULT_BOLD }
        val sub=TextView(act).apply{ text="নামাজের সময় এবং চাঁদের তারিখ দেখাতে আপনার অবস্থান জানা জরুরি।"; setTextSize(TypedValue.COMPLEX_UNIT_SP,12f); setTextColor(Color.GRAY) }
        val searchEt=EditText(act).apply{
            hint="শহর নির্বাচন করুন - যেমন ঢাকা"; setPadding(dp(12),dp(10),dp(12),dp(10))
            val bg=GradientDrawable(); bg.setColor(Color.WHITE); bg.setStroke(1, Color.parseColor("#EFE5C8")); bg.cornerRadius=dp(12).toFloat(); background=bg
        }
        val listView=ListView(act).apply{ layoutParams=LinearLayout.LayoutParams(-1,dp(300)) }
        dialogView.addView(title); dialogView.addView(sub, LinearLayout.LayoutParams(-1,-2).apply{ topMargin=dp(6) })
        dialogView.addView(searchEt, LinearLayout.LayoutParams(-1,-2).apply{ topMargin=dp(12) })
        dialogView.addView(listView, LinearLayout.LayoutParams(-1,-2).apply{ topMargin=dp(8) })
        val adapterList=mutableListOf<JSONObject>(); for(i in 0 until allCities.length()) adapterList.add(allCities.getJSONObject(i))
        var filtered=adapterList.toList()
        fun updateList(q:String){
            filtered=if(q.isEmpty()) adapterList else adapterList.filter{ it.optString("name_bn").contains(q)||it.optString("name_en").lowercase().contains(q.lowercase()) }
            val arr=filtered.map{ "${it.optString("name_bn")} - ${it.optString("name_en")} (${it.optString("division")})" }.toTypedArray()
            listView.adapter=ArrayAdapter(act, android.R.layout.simple_list_item_1, arr)
        }
        updateList("")
        searchEt.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s:Editable?){}
            override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
            override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){ updateList(s.toString()) }
        })
        val dialog=android.app.AlertDialog.Builder(act).setView(dialogView).create()
        listView.setOnItemClickListener{ _,_,pos,_->
            val obj=filtered[pos]; selectedCityEn=obj.optString("name_en"); selectedCityBn=obj.optString("name_bn")
            prefs.edit().putString("city_en",selectedCityEn).putString("city_bn",selectedCityBn).apply()
            cityTv.text="📍 $selectedCityBn ▼"; dialog.dismiss(); loadCityData(selectedCityEn)
        }
        dialog.show()
    }
}
