package com.srizwan.islamipedia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class QuranActivity : AppCompatActivity() {

    enum class Mode { SURA_LIST, AYA_LIST, GLOBAL_SEARCH, BOOKMARK }

    private var currentMode = Mode.SURA_LIST
    private lateinit var root: ConstraintLayout
    private lateinit var topBar: LinearLayout
    private lateinit var backIv: ImageView
    private lateinit var headingTv: TextView
    private lateinit var bookmarkViewBtn: TextView
    private lateinit var jumpIv: ImageView
    private lateinit var searchIv: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var searchbox: EditText
    private lateinit var cancelIv: ImageView
    private lateinit var progressContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var nores: LinearLayout
    private lateinit var listView1: ListView
    private lateinit var audiotab: LinearLayout
    private lateinit var previousLL: LinearLayout
    private lateinit var nextLL: LinearLayout
    private lateinit var stopLL: LinearLayout
    private lateinit var playAudioLL: LinearLayout
    private lateinit var playAudioIv: ImageView
    private lateinit var qariSelectorTv: TextView
    private lateinit var fabGlobalSearch: FloatingActionButton

    private lateinit var suraName: Array<String>
    private lateinit var suraAuthor: Array<String>
    private lateinit var suraBookId: Array<String>
    private lateinit var suraVerses: Array<String>
    private lateinit var suraNamesAr: Array<String>
    private var suraList: ArrayList<JSONObject> = ArrayList()
    private var filteredSura: ArrayList<JSONObject> = ArrayList()

    private lateinit var ayaName: Array<String>
    private lateinit var ayaAuthor: Array<String>
    private lateinit var ayaBookId: Array<String>
    private lateinit var ayaVerses: Array<String>
    private lateinit var ayaNamesAr: Array<String>
    private var ayaList: ArrayList<JSONObject> = ArrayList()
    private var filteredAya: ArrayList<JSONObject> = ArrayList()

    private var globalList: ArrayList<JSONObject> = ArrayList()
    private var bookmarkList: ArrayList<JSONObject> = ArrayList()
    private var allSuraAuthors: ArrayList<String> = ArrayList()
    private var suraInfoMap: MutableMap<String, JSONObject> = mutableMapOf()

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private var currentPlayingId: String? = null
    var currentSuraNumber: Int = 1
    var currentSuraAuthor: String = "Al-Fatihah"
    var currentSuraBangla: String = "আল-ফাতিহা"
    lateinit var qariMap: LinkedHashMap<String, String>
    lateinit var prefs: SharedPreferences
    var selectedQariCode: String = "Alafasy_64kbps"
    var selectedQariName: String = "মিশারী রাশিদ আল-আফাসী"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("quran_pref", Context.MODE_PRIVATE)
        selectedQariName = prefs.getString("selected_qari_name", "মিশারী রাশিদ আল-আফাসী") ?: "মিশারী রাশিদ আল-আফাসী"
        selectedQariCode = prefs.getString("selected_qari_code", "Alafasy_64kbps") ?: "Alafasy_64kbps"
        qariMap = linkedMapOf(
            "মিশারী রাশিদ আল-আফাসী" to "Alafasy_64kbps",
            "আব্দুর রহমান আস-সুদাইস" to "Abdurrahmaan_As-Sudais_64kbps",
            "সা'দ আল-গামেদী" to "Ghamadi_40kbps",
            "আবু বকর আশ-শাতরী" to "Abu_Bakr_Ash-Shaatree_64kbps",
            "আব্দুল বাসিত (মুরাত্তাল)" to "Abdul_Basit_Murattal_64kbps",
            "মাহের আল-মুয়াইকলি" to "Maher_AlMuaiqly_64kbps"
        )
        root = createMainLayout()
        setContentView(root)
        setupListeners()
        loadSuraList()
        switchMode(Mode.SURA_LIST)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val w: Window = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            try {
                w.statusBarColor = Color.parseColor(getString(R.string.color))
                w.navigationBarColor = Color.parseColor(getString(R.string.color))
            } catch (e: Exception) {
                w.statusBarColor = Color.parseColor("#01837A")
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchView.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) searchView.visibility = View.GONE else searchbox.text.clear()
                } else if (currentMode != Mode.SURA_LIST) {
                    switchMode(Mode.SURA_LIST)
                } else finish()
            }
        })
    }

    private fun createMainLayout(): ConstraintLayout {
        val d = resources.displayMetrics.density
        val rootLayout = ConstraintLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            try { background = ContextCompat.getDrawable(context, R.drawable.back1ground) } catch (e: Exception) { setBackgroundColor(Color.WHITE) }
            fitsSystemWindows = true
        }

        topBar = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            elevation = 5f * d
            val lp = ConstraintLayout.LayoutParams(0, (65 * d).toInt())
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
        }
        backIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams((56 * d).toInt(), (56 * d).toInt())
            setPadding((15 * d).toInt(), (15 * d).toInt(), (15 * d).toInt(), (15 * d).toInt())
            scaleType = ImageView.ScaleType.CENTER_CROP
            try { setImageResource(R.drawable.ic_arrow_back_white) } catch (e: Exception) {}
        }
        headingTv = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            lp.leftMargin = (5 * d).toInt()
            layoutParams = lp
            setTextColor(Color.WHITE); textSize = 18f
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            isSingleLine = true; ellipsize = android.text.TextUtils.TruncateAt.MARQUEE; marqueeRepeatLimit = -1; isFocusable = true; isFocusableInTouchMode = true; setHorizontallyScrolling(true); gravity = Gravity.CENTER_VERTICAL; setTypeface(typeface, android.graphics.Typeface.BOLD)
            text = intent.getStringExtra("sub") ?: "আল কুরআন"
        }
        bookmarkViewBtn = TextView(this).apply {
            val lp = LinearLayout.LayoutParams((40 * d).toInt(), (40 * d).toInt())
            lp.rightMargin = (5 * d).toInt()
            layoutParams = lp
            text = "⭐"; textSize = 20f; gravity = Gravity.CENTER; setTextColor(Color.WHITE)
        }
        jumpIv = ImageView(this).apply {
            val lp = LinearLayout.LayoutParams((30 * d).toInt(), (30 * d).toInt())
            lp.rightMargin = (10 * d).toInt()
            layoutParams = lp
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.ic_jump_page) } catch (e: Exception) {}
            visibility = View.GONE
        }
        searchIv = ImageView(this).apply {
            val lp = LinearLayout.LayoutParams((30 * d).toInt(), (30 * d).toInt())
            lp.rightMargin = (5 * d).toInt()
            layoutParams = lp
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}
        }
        topBar.addView(backIv); topBar.addView(headingTv); topBar.addView(bookmarkViewBtn); topBar.addView(jumpIv); topBar.addView(searchIv)

        searchView = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL; visibility = View.GONE
            val lp = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topToBottom = topBar.id
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.topMargin = (10 * d).toInt()
            layoutParams = lp
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt())
            val bg = GradientDrawable(); bg.setColor(Color.WHITE); bg.cornerRadius = 100f; bg.setStroke((1*d).toInt(), Color.parseColor("#01837A")); background = bg
        }
        searchbox = EditText(this).apply {
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams = lp
            setPadding((16*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()); setTextColor(Color.BLACK); textSize = 14f; hint = "সুরা সার্চ করুন"; background = null
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        cancelIv = ImageView(this).apply {
            val lp = LinearLayout.LayoutParams((30*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
            lp.rightMargin = (5*d).toInt()
            layoutParams = lp
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.cancel) } catch (e: Exception) {}
        }
        searchView.addView(searchbox); searchView.addView(cancelIv)

        progressContainer = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; visibility = View.GONE
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            val lp = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topToBottom = searchView.id
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
        }
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            val lp = LinearLayout.LayoutParams(0, (8*d).toInt(), 1f)
            layoutParams = lp
            max = 100
        }
        progressText = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.leftMargin = (8*d).toInt()
            layoutParams = lp
            text = "⏳ সার্চ চলছে... ০ টি পাওয়া গেছে"; textSize = 14f; setTextColor(Color.parseColor("#607D8B"))
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        progressContainer.addView(progressBar); progressContainer.addView(progressText)

        nores = LinearLayout(this).apply {
            id = View.generateViewId()
            visibility = View.GONE; orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(Color.WHITE); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            val lp = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topToBottom = progressContainer.id
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
        }
        val noresImg = ImageView(this).apply {
            val lp = LinearLayout.LayoutParams((100*d).toInt(), (100*d).toInt())
            lp.gravity = Gravity.CENTER
            layoutParams = lp
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.noresult) } catch (e: Exception) {}
        }
        val noresTv = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.CENTER
            layoutParams = lp
            text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"; textSize = 16f; setTextColor(Color.BLACK); gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        nores.addView(noresImg); nores.addView(noresTv)

        listView1 = ListView(this).apply {
            id = View.generateViewId()
            val lp = ConstraintLayout.LayoutParams(0, 0)
            lp.topToBottom = nores.id
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.topMargin = (10*d).toInt()
            lp.bottomMargin = (80*d).toInt()
            layoutParams = lp
            divider = null; dividerHeight = 0; setBackgroundColor(Color.WHITE); selector = android.graphics.drawable.ColorDrawable(Color.WHITE); isFastScrollEnabled = true
        }

        audiotab = LinearLayout(this).apply {
            id = View.generateViewId()
            visibility = View.GONE; orientation = LinearLayout.HORIZONTAL; setBackgroundColor(Color.parseColor("#01837A")); gravity = Gravity.CENTER_VERTICAL
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            val lp = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
        }
        previousLL = LinearLayout(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.leftMargin = (10*d).toInt()
            layoutParams = lp
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            orientation = LinearLayout.HORIZONTAL
        }
        val prevImg = ImageView(this).apply { val lp = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()); layoutParams = lp; scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.previous) } catch (e: Exception) {} }
        previousLL.addView(prevImg)
        val between0 = LinearLayout(this).apply { val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); layoutParams = lp }
        playAudioLL = LinearLayout(this).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,0); layoutParams = lp; setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        playAudioIv = ImageView(this).apply { val lp = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()); layoutParams = lp; scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.play) } catch (e: Exception) {} }
        playAudioLL.addView(playAudioIv)
        val between1 = LinearLayout(this).apply { val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); layoutParams = lp }
        nextLL = LinearLayout(this).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); layoutParams = lp; setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        val nextImg = ImageView(this).apply { val lp = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()); layoutParams = lp; scaleType = ImageView.ScaleType.FIT_CENTER; rotation = 180f; try { setImageResource(R.drawable.previous) } catch (e: Exception) {} }
        nextLL.addView(nextImg)
        val between2 = LinearLayout(this).apply { val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); layoutParams = lp }
        qariSelectorTv = TextView(this).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), (40*d).toInt()); layoutParams = lp; text = "🎧"; textSize = 22f; gravity = Gravity.CENTER; setTextColor(Color.WHITE) }
        stopLL = LinearLayout(this).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.rightMargin = (10*d).toInt(); layoutParams = lp; setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        val stopImg = ImageView(this).apply { val lp = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()); layoutParams = lp; scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.stop) } catch (e: Exception) {} }
        stopLL.addView(stopImg)
        audiotab.addView(previousLL); audiotab.addView(between0); audiotab.addView(playAudioLL); audiotab.addView(between1); audiotab.addView(nextLL); audiotab.addView(between2); audiotab.addView(qariSelectorTv); audiotab.addView(stopLL)

        fabGlobalSearch = FloatingActionButton(this).apply {
            val lp = ConstraintLayout.LayoutParams((56*d).toInt(), (56*d).toInt())
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.bottomMargin = (16*d).toInt()
            lp.rightMargin = (16*d).toInt()
            lp.setMargins(0,0,(16*d).toInt(),(16*d).toInt())
            layoutParams = lp
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
        }

        rootLayout.addView(topBar); rootLayout.addView(searchView); rootLayout.addView(progressContainer); rootLayout.addView(nores); rootLayout.addView(listView1); rootLayout.addView(audiotab); rootLayout.addView(fabGlobalSearch)
        return rootLayout
    }

    private fun setupListeners() {
        backIv.setOnClickListener { if (currentMode != Mode.SURA_LIST) switchMode(Mode.SURA_LIST) else finish() }
        bookmarkViewBtn.setOnClickListener { loadBookmarksAndSwitch() }
        searchIv.setOnClickListener { searchView.visibility = if (searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE }
        cancelIv.setOnClickListener { if (searchbox.text.toString() == "") searchView.visibility = View.GONE else searchbox.text.clear() }
        jumpIv.setOnClickListener { showPageJumpDialog() }
        fabGlobalSearch.setOnClickListener { switchMode(Mode.GLOBAL_SEARCH) }

        searchbox.addTextChangedListener { s ->
            val q = s.toString()
            when (currentMode) {
                Mode.SURA_LIST -> filterSuraList(q)
                Mode.AYA_LIST -> filterAyaList(q)
                Mode.GLOBAL_SEARCH -> { if (q.length >= 2) performGlobalSearch(q) }
                else -> {}
            }
        }

        listView1.setOnItemClickListener { _, _, position, _ ->
            when (currentMode) {
                Mode.SURA_LIST -> {
                    if (position < filteredSura.size) {
                        val selected = filteredSura[position]
                        val bookAuthor = selected.getString("author")
                        currentSuraBangla = selected.getString("name")
                        currentSuraAuthor = bookAuthor
                        currentSuraNumber = getSuraNumberFromAuthor(bookAuthor)
                        loadAyaList("${bookAuthor}.json")
                        switchMode(Mode.AYA_LIST)
                    }
                }
                Mode.BOOKMARK -> {
                    val item = if (listView1.adapter is BookmarkAdapter) (listView1.adapter as BookmarkAdapter).getItemAt(position) else null
                    item?.let {
                        currentSuraAuthor = it.optString("suraAuthor")
                        currentSuraBangla = it.optString("suraName")
                        currentSuraNumber = it.optString("suraNumber").toIntOrNull() ?: getSuraNumberFromAuthor(currentSuraAuthor)
                        loadAyaList("${currentSuraAuthor}.json")
                        switchMode(Mode.AYA_LIST)
                        listView1.postDelayed({
                            val targetVerses = it.optString("ayahNumber")
                            for (i in filteredAya.indices) if (filteredAya[i].optString("verses") == targetVerses) { listView1.setSelection(i); break }
                        }, 300)
                    }
                }
                Mode.GLOBAL_SEARCH -> {
                    val item = if (listView1.adapter is GlobalSearchAdapter) (listView1.adapter as GlobalSearchAdapter).getItemAt(position) else null
                    item?.let {
                        currentSuraAuthor = it.optString("suraAuthor")
                        currentSuraBangla = it.optString("suraName")
                        currentSuraNumber = it.optString("suraNumber").toIntOrNull() ?: getSuraNumberFromAuthor(currentSuraAuthor)
                        loadAyaList("${currentSuraAuthor}.json")
                        switchMode(Mode.AYA_LIST)
                        listView1.postDelayed({
                            val target = it.optString("verses")
                            for (i in filteredAya.indices) if (filteredAya[i].optString("verses") == target) { listView1.setSelection(i); break }
                        }, 300)
                    }
                }
                else -> {}
            }
        }

        previousLL.setOnClickListener { if (currentIndex > 0) currentIndex--; startPlayingFromIndex(currentIndex) }
        nextLL.setOnClickListener { if (currentIndex < filteredAya.size - 1) currentIndex++; startPlayingFromIndex(currentIndex) }
        stopLL.setOnClickListener {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null; currentPlayingId = null; currentIndex = 0
                playAudioIv.setImageResource(R.drawable.play); notifyAyaList()
                Toast.makeText(this, "অডিও প্লে বন্ধ হয়েছে।", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "এখন কোনো সূরা অডিও চলছে না", Toast.LENGTH_SHORT).show()
        }
        playAudioIv.setOnClickListener {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) { mediaPlayer?.pause(); notifyAyaList(); playAudioIv.setImageResource(R.drawable.play) }
                else {
                    if (currentIndex >= filteredAya.size) { currentIndex = 0; startPlayingFromIndex(currentIndex); notifyAyaList(); playAudioIv.setImageResource(R.drawable.pause) }
                    else { mediaPlayer?.start(); notifyAyaList(); playAudioIv.setImageResource(R.drawable.pause) }
                }
            } else {
                if (filteredAya.isNotEmpty()) { currentIndex = 0; notifyAyaList(); startPlayingFromIndex(currentIndex); playAudioIv.setImageResource(R.drawable.pause) }
                else Toast.makeText(this, "প্লে করার মতো আয়াত নেই।", Toast.LENGTH_SHORT).show()
            }
        }
        qariSelectorTv.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            qariMap.keys.forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item ->
                val banglaName = item.title.toString()
                val code = qariMap[banglaName] ?: "Alafasy_64kbps"
                selectedQariName = banglaName; selectedQariCode = code
                prefs.edit().putString("selected_qari_name", banglaName).putString("selected_qari_code", code).apply()
                Toast.makeText(this, "ক্বারী: $banglaName", Toast.LENGTH_SHORT).show()
                if (mediaPlayer != null) { mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null; startPlayingFromIndex(currentIndex) }
                true
            }
            popup.show()
        }
    }

    fun switchMode(mode: Mode) {
        currentMode = mode
        when (mode) {
            Mode.SURA_LIST -> {
                headingTv.text = intent.getStringExtra("sub") ?: "আল কুরআন"
                searchbox.hint = "সুরা সার্চ করুন"
                audiotab.visibility = View.GONE; progressContainer.visibility = View.GONE; jumpIv.visibility = View.GONE
                fabGlobalSearch.visibility = View.VISIBLE
                searchView.visibility = View.GONE
                if (filteredSura.isEmpty()) loadSuraList()
                nores.visibility = if (filteredSura.isEmpty()) View.VISIBLE else View.GONE
                listView1.visibility = View.VISIBLE
                listView1.adapter = QuranAdapter(this, filteredSura)
            }
            Mode.AYA_LIST -> {
                headingTv.text = currentSuraBangla
                searchbox.hint = "আয়াত সার্চ করুন"
                audiotab.visibility = View.VISIBLE; progressContainer.visibility = View.GONE; jumpIv.visibility = View.VISIBLE; nores.visibility = View.GONE
                fabGlobalSearch.visibility = View.GONE
                searchView.visibility = View.GONE
                listView1.adapter = QuranviewAdapter(this, filteredAya)
            }
            Mode.GLOBAL_SEARCH -> {
                headingTv.text = "গ্লোবাল সার্চ"
                searchbox.hint = "পুরো কুরআনে সার্চ করুন"
                searchView.visibility = View.VISIBLE; audiotab.visibility = View.GONE; jumpIv.visibility = View.GONE; progressContainer.visibility = View.VISIBLE; nores.visibility = View.GONE
                fabGlobalSearch.visibility = View.GONE
                globalList.clear()
                listView1.adapter = GlobalSearchAdapter(this, globalList)
                progressBar.progress = 0; progressText.text = "⏳ সার্চ চলছে... ০ টি পাওয়া গেছে"
            }
            Mode.BOOKMARK -> {
                headingTv.text = "বুকমার্ক"
                searchbox.hint = "বুকমার্ক সার্চ"; audiotab.visibility = View.GONE; progressContainer.visibility = View.GONE; jumpIv.visibility = View.GONE; searchView.visibility = View.GONE; fabGlobalSearch.visibility = View.GONE
                if (bookmarkList.isEmpty()) nores.visibility = View.VISIBLE else nores.visibility = View.GONE
                listView1.adapter = BookmarkAdapter(this, bookmarkList)
            }
        }
    }

    private fun loadSuraList() {
        val fileName = intent.getStringExtra("booklist")?.takeIf { it.isNotEmpty() } ?: "sura.json"
        try {
            val input = resources.assets.open(fileName)
            val arr = JSONArray(String(input.readBytes(), Charsets.UTF_8)); input.close()
            suraList = ArrayList()
            allSuraAuthors.clear(); suraInfoMap.clear()
            suraName = Array(arr.length()) { "" }; suraAuthor = Array(arr.length()) { "" }; suraBookId = Array(arr.length()) { "" }; suraVerses = Array(arr.length()) { "" }; suraNamesAr = Array(arr.length()) { "" }
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i); suraList.add(o)
                suraName[i] = o.getString("name"); suraAuthor[i] = o.getString("author"); suraBookId[i] = o.getString("bookid"); suraVerses[i] = o.getString("verses"); suraNamesAr[i] = o.getString("names")
                allSuraAuthors.add(o.getString("author")); suraInfoMap[o.getString("author")] = o
            }
            filteredSura = ArrayList(suraList)
            copyBlankReasonToClipboard("Loaded ${suraList.size} suras from $fileName")
        } catch (e: Exception) {
            e.printStackTrace()
            copyBlankReasonToClipboard("Failed to load $fileName: ${e.message} - Check assets/$fileName exists")
            suraList = ArrayList(); filteredSura = ArrayList()
        }
    }

    private fun loadAyaList(fileName: String) {
        try {
            val input = resources.assets.open(fileName)
            val arr = JSONArray(String(input.readBytes(), Charsets.UTF_8)); input.close()
            ayaList = ArrayList(); ayaName = Array(arr.length()) { "" }; ayaAuthor = Array(arr.length()) { "" }; ayaBookId = Array(arr.length()) { "" }; ayaVerses = Array(arr.length()) { "" }; ayaNamesAr = Array(arr.length()) { "" }
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i); ayaList.add(o)
                ayaName[i] = o.optString("name",""); ayaAuthor[i] = o.optString("author",""); ayaBookId[i] = o.optString("bookid",""); ayaVerses[i] = o.optString("verses",""); ayaNamesAr[i] = o.optString("names","")
            }
            filteredAya = ArrayList(ayaList); currentIndex = 0; currentPlayingId = null
        } catch (e: Exception) { e.printStackTrace(); ayaList = ArrayList(); filteredAya = ArrayList() }
    }

    private fun filterSuraList(query: String) {
        filteredSura.clear()
        if (query.isEmpty()) filteredSura.addAll(suraList)
        else {
            for (i in suraName.indices) if (suraName[i].contains(query, true)) {
                filteredSura.add(JSONObject().apply { put("name", suraName[i]); put("author", suraAuthor[i]); put("bookid", suraBookId[i]); put("verses", suraVerses[i]); put("names", suraNamesAr[i]) })
            }
        }
        nores.visibility = if (filteredSura.isEmpty()) View.VISIBLE else View.GONE
        listView1.adapter = QuranAdapter(this, filteredSura)
    }

    private fun filterAyaList(query: String) {
        filteredAya.clear()
        if (query.isEmpty()) filteredAya.addAll(ayaList)
        else {
            for (i in ayaName.indices) if (ayaName[i].contains(query, true) || ayaNamesAr[i].contains(query, true)) {
                filteredAya.add(ayaList[i])
            }
        }
        nores.visibility = if (filteredAya.isEmpty()) View.VISIBLE else View.GONE
        listView1.adapter = QuranviewAdapter(this, filteredAya)
    }

    private fun getSuraNumberFromAuthor(author: String): Int {
        return suraInfoMap[author]?.optString("bookid")?.toIntOrNull() ?: try {
            val input = resources.assets.open("sura.json")
            val arr = JSONArray(String(input.readBytes(), Charsets.UTF_8)); input.close()
            for (i in 0 until arr.length()) if (arr.getJSONObject(i).getString("author") == author) return arr.getJSONObject(i).getString("bookid").toInt()
            1
        } catch (e: Exception) { 1 }
    }

    fun getFormattedSSSAAA(suraNumber: Int, ayahNumber: Int): String = String.format("%03d%03d", suraNumber, ayahNumber)
    fun getEveryAyahUrl(qariCode: String, sssaaa: String): String = "https://everyayah.com/data/$qariCode/$sssaaa.mp3"

    fun startPlayingFromIndex(index: Int) {
        if (index >= filteredAya.size) { stopCurrentPlaying(); return }
        currentIndex = index
        val currentItem = filteredAya[currentIndex]
        val ayahNum = currentItem.optString("verses", "${index+1}").toIntOrNull() ?: (index+1)
        val sssaaa = getFormattedSSSAAA(currentSuraNumber, ayahNum)
        val audioUrl = getEveryAyahUrl(selectedQariCode, sssaaa)
        val fileName = "${selectedQariCode}_${sssaaa}.mp3"
        val file = File(getExternalFilesDir(null), fileName)
        val audioId = currentItem.optString("_id", sssaaa)
        currentPlayingId = audioId
        scrollToAya(audioId)
        if (file.exists()) { playAudioFile(file); downloadNextAudios(currentIndex + 1) }
        else downloadAndPlayFirstAudio(audioUrl, file)
        playAudioIv.setImageResource(R.drawable.pause)
        notifyAyaList()
    }

    private fun downloadAndPlayFirstAudio(url: String, file: File) {
        Thread {
            try {
                var connection = URL(url).openConnection() as HttpURLConnection; connection.connect()
                if (connection.responseCode == 404) {
                    val fallbackUrl = url.replace("_64kbps", "_128kbps").replace("_40kbps", "_64kbps")
                    val conn2 = URL(fallbackUrl).openConnection() as HttpURLConnection; conn2.connect()
                    if (conn2.responseCode == 200) { downloadStream(conn2, file); runOnUiThread { playAudioFile(file); downloadNextAudios(currentIndex + 1) }; return@Thread }
                }
                downloadStream(connection, file)
                runOnUiThread { playAudioFile(file); downloadNextAudios(currentIndex + 1) }
            } catch (e: Exception) { e.printStackTrace(); runOnUiThread { Toast.makeText(this, "ডাউনলোডে সমস্যা", Toast.LENGTH_SHORT).show() } }
        }.start()
    }

    private fun downloadStream(connection: HttpURLConnection, file: File) {
        val input = BufferedInputStream(connection.inputStream)
        val output = FileOutputStream(file)
        val data = ByteArray(1024); var count: Int
        while (input.read(data).also { count = it } != -1) output.write(data, 0, count)
        output.flush(); output.close(); input.close()
    }

    private fun downloadNextAudios(startIndex: Int) {
        Thread {
            for (i in startIndex until filteredAya.size) {
                val currentItem = filteredAya[i]
                val ayahNum = currentItem.optString("verses", "${i+1}").toIntOrNull() ?: (i+1)
                val sssaaa = getFormattedSSSAAA(currentSuraNumber, ayahNum)
                val audioUrl = getEveryAyahUrl(selectedQariCode, sssaaa)
                val fileName = "${selectedQariCode}_${sssaaa}.mp3"
                val file = File(getExternalFilesDir(null), fileName)
                if (!file.exists()) {
                    try { val connection = URL(audioUrl).openConnection() as HttpURLConnection; connection.connect(); if (connection.responseCode == 200) downloadStream(connection, file) } catch (e: Exception) {}
                }
            }
        }.start()
    }

    private fun playAudioFile(file: File) {
        mediaPlayer?.release(); mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.setDataSource(file.absolutePath); mediaPlayer?.prepare(); mediaPlayer?.start()
            notifyAyaList(); playAudioIv.setImageResource(R.drawable.pause)
            mediaPlayer?.setOnCompletionListener { startPlayingFromIndex(currentIndex + 1) }
        } catch (e: IOException) { e.printStackTrace(); Toast.makeText(this, "অডিও প্লে করতে সমস্যা", Toast.LENGTH_SHORT).show() }
    }

    fun getCurrentPlayingId(): String? = currentPlayingId
    fun isAudioPlaying(): Boolean = mediaPlayer?.isPlaying == true
    fun notifyAyaList() { if (currentMode == Mode.AYA_LIST) (listView1.adapter as? QuranviewAdapter)?.notifyDataSetChanged() }
    private fun stopCurrentPlaying() {
        mediaPlayer?.release(); mediaPlayer = null; currentPlayingId = null; currentIndex = 0
        try { playAudioIv.setImageResource(R.drawable.play) } catch (e: Exception) {}
        notifyAyaList()
    }
    private fun scrollToAya(id: String) {
        for (i in filteredAya.indices) if (filteredAya[i].optString("_id") == id) { listView1.setSelection(i); listView1.postDelayed({ listView1.smoothScrollToPositionFromTop(i, 0) }, 50); break }
    }

    fun playme(item: JSONObject) {
        val id = item.optString("_id")
        for (i in filteredAya.indices) if (filteredAya[i].optString("_id") == id) { currentIndex = i; break }
        if (mediaPlayer != null && currentPlayingId == id) {
            if (mediaPlayer!!.isPlaying) { mediaPlayer?.pause(); playAudioIv.setImageResource(R.drawable.play) } else { mediaPlayer?.start(); playAudioIv.setImageResource(R.drawable.pause) }
            notifyAyaList(); return
        }
        startPlayingFromIndex(currentIndex)
    }

    fun copyme(item: JSONObject) {
        val text = "${item.optString("names")}\n\n${item.optString("name")}\n${item.optString("author")}"
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ayah", text))
        Toast.makeText(this, "কপি হয়েছে", Toast.LENGTH_SHORT).show()
    }
    fun shareme(item: JSONObject) {
        val text = "${item.optString("names")}\n\n${item.optString("name")}\n${item.optString("author")}\n\n$currentSuraBangla"
        val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(share, "শেয়ার করুন"))
    }

    fun toggleBookmark(item: JSONObject) {
        val prefsBm = getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val jsonStr = prefsBm.getString("bookmarks_json", "[]")
        val arr = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
        val _id = item.optString("_id")
        var foundIndex = -1
        for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); if (o.optString("_id") == _id && o.optString("suraAuthor") == currentSuraAuthor) { foundIndex = i; break } }
        if (foundIndex >=0) {
            val newArr = JSONArray(); for (i in 0 until arr.length()) if (i!=foundIndex) newArr.put(arr.getJSONObject(i))
            prefsBm.edit().putString("bookmarks_json", newArr.toString()).apply()
            Toast.makeText(this, "বুকমার্ক মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
        } else {
            val bm = JSONObject()
            bm.put("suraNumber", currentSuraNumber); bm.put("suraName", currentSuraBangla); bm.put("suraAuthor", currentSuraAuthor)
            bm.put("ayahNumber", item.optString("verses")); bm.put("_id", _id); bm.put("name", item.optString("name")); bm.put("names", item.optString("names")); bm.put("author", item.optString("author")); bm.put("timestamp", System.currentTimeMillis())
            arr.put(bm); prefsBm.edit().putString("bookmarks_json", arr.toString()).apply()
            Toast.makeText(this, "বুকমার্ক যোগ হয়েছে", Toast.LENGTH_SHORT).show()
        }
        notifyAyaList()
        if (currentMode == Mode.GLOBAL_SEARCH) (listView1.adapter as? GlobalSearchAdapter)?.notifyDataSetChanged()
    }
    fun isBookmarked(_id: String, suraAuthor: String): Boolean {
        val prefsBm = getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefsBm.getString("bookmarks_json", "[]")) } catch (e: Exception) { JSONArray() }
        for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); if (o.optString("_id") == _id && o.optString("suraAuthor") == suraAuthor) return true }
        return false
    }
    private fun copyBlankReasonToClipboard(reason: String) {
        try {
            val fullReason = "Blank Reason: $reason | Mode: $currentMode | SuraList: ${suraList.size} | Filtered: ${filteredSura.size} | Assets sura.json exists: ${checkAssetExists("sura.json")}"
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("blank_reason", fullReason))
            android.util.Log.e("QuranActivity", fullReason)
        } catch (e: Exception) {}
    }
    private fun checkAssetExists(fileName: String): Boolean {
        return try { resources.assets.open(fileName).close(); true } catch (e: Exception) { false }
    }
    private fun loadBookmarksAndSwitch() {
        val prefsBm = getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefsBm.getString("bookmarks_json", "[]")) } catch (e: Exception) { JSONArray() }
        bookmarkList.clear()
        for (i in 0 until arr.length()) bookmarkList.add(arr.getJSONObject(i))
        switchMode(Mode.BOOKMARK)
    }

    private fun performGlobalSearch(query: String) {
        globalList.clear(); progressBar.progress = 0; progressText.text = "⏳ সার্চ চলছে... ০ টি পাওয়া গেছে"
        Thread {
            var found = 0; var scanned = 0; val total = allSuraAuthors.size
            for (author in allSuraAuthors) {
                scanned++
                val matches = ArrayList<JSONObject>()
                try {
                    val f = resources.assets.open("$author.json")
                    val arr = JSONArray(String(f.readBytes(), Charsets.UTF_8)); f.close()
                    for (j in 0 until arr.length()) {
                        val obj = arr.getJSONObject(j)
                        val name = obj.optString("name",""); val names = obj.optString("names",""); val tafsir = obj.optString("author","")
                        if (name.contains(query, true) || names.contains(query, true) || tafsir.contains(query, true)) {
                            val suraInfo = suraInfoMap[author]
                            obj.put("suraName", suraInfo?.optString("name") ?: author); obj.put("suraAuthor", author); obj.put("suraNumber", suraInfo?.optString("bookid") ?: "1")
                            matches.add(obj)
                        }
                    }
                } catch (e: Exception) {}
                found += matches.size; globalList.addAll(matches)
                runOnUiThread {
                    progressBar.progress = scanned * 100 / total
                    progressText.text = "⏳ সার্চ চলছে... $scanned/$total স্ক্যান - $found টি আয়াত পাওয়া গেছে"
                    nores.visibility = if (globalList.isEmpty()) View.VISIBLE else View.GONE
                    listView1.adapter = GlobalSearchAdapter(this, ArrayList(globalList))
                }
            }
            runOnUiThread { progressText.text = "✅ $found টি আয়াত পাওয়া গেছে" }
        }.start()
    }

    private fun showPageJumpDialog() {
        val input = EditText(this).apply { hint = "আয়াত নম্বর লিখুন"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        AlertDialog.Builder(this).setTitle("আয়াতে যান").setView(input)
            .setPositiveButton("যান") { _, _ ->
                val num = input.text.toString().toIntOrNull()
                if (num != null) for (i in filteredAya.indices) if (filteredAya[i].optString("verses").toIntOrNull() == num) { listView1.setSelection(i); break }
            }.setNegativeButton("বাতিল", null).show()
    }

    override fun onDestroy() { super.onDestroy(); mediaPlayer?.release(); mediaPlayer = null }

    inner class QuranAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context; val d = ctx.resources.displayMetrics.density
            val itemView = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT); setBackgroundColor(Color.WHITE) }
            val lmain = LinearLayout(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (60*d).toInt())
                lp.setMargins((3*d).toInt(), (3*d).toInt(), (3*d).toInt(), (3*d).toInt())
                layoutParams = lp
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200)); elevation = 6f*d
            }
            val sketchUi = GradientDrawable().apply { val di = ctx.resources.displayMetrics.density.toInt(); setStroke(di, Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = di * 12f }
            val ripple = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), sketchUi, null); lmain.background = ripple
            val linear5 = LinearLayout(ctx).apply {
                val lp = LinearLayout.LayoutParams((46*d).toInt(), (46*d).toInt())
                lp.leftMargin = (10*d).toInt()
                layoutParams = lp
                gravity = Gravity.CENTER
                try { background = ContextCompat.getDrawable(ctx, R.drawable.quran) } catch (e: Exception) { setBackgroundColor(Color.LTGRAY) }
            }
            val number = TextView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); textSize = 10f; setTextColor(Color.parseColor("#5A0202")); setTypeface(null, android.graphics.Typeface.BOLD); gravity = Gravity.CENTER
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}; setPadding(0,0,0,(10*d).toInt())
            }
            linear5.addView(number)
            val surabox = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.leftMargin = (4*d).toInt(); layoutParams = lp }
            val nameTv = TextView(ctx).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); textSize = 16f; setTextColor(Color.BLACK); try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {} }
            val ayaNumTv = TextView(ctx).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); textSize = 14f; setTextColor(Color.BLACK); try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {} }
            surabox.addView(nameTv); surabox.addView(ayaNumTv)
            val spacer = LinearLayout(ctx).apply { val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); layoutParams = lp }
            val arabicTv = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.rightMargin = (5*d).toInt(); layoutParams = lp; textSize = 14f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; setTypeface(null, android.graphics.Typeface.BOLD) }
            lmain.addView(linear5); lmain.addView(surabox); lmain.addView(spacer); lmain.addView(arabicTv)
            itemView.addView(lmain)
            val line = LinearLayout(ctx).apply { val lp = LinearLayout.LayoutParams((100*d).toInt(), (1*d).toInt()); lp.setMargins((3*d).toInt(), (3*d).toInt(), (3*d).toInt(), (3*d).toInt()); lp.gravity = Gravity.CENTER; layoutParams = lp; setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200)) }
            itemView.addView(line)
            try {
                nameTv.text = replaceArabicNumber(list[position].getString("name"))
                arabicTv.text = replaceArabicNumber(list[position].getString("names"))
                val bookid1 = replaceArabicNumber(list[position].getString("bookid"))
                number.text = if (bookid1.startsWith("০") || bookid1.startsWith("0")) bookid1.drop(1) else bookid1
                ayaNumTv.text = "মোট আয়াত : ${replaceArabicNumber(list[position].getString("verses"))}"
            } catch (e: JSONException) { e.printStackTrace() }
            val animation = android.view.animation.ScaleAnimation(0f,1f,0f,1f, android.view.animation.ScaleAnimation.RELATIVE_TO_SELF,0f, android.view.animation.ScaleAnimation.RELATIVE_TO_SELF,1f).apply { fillAfter=true; duration=300 }
            lmain.startAnimation(animation)
            return itemView
        }
    }

    inner class QuranviewAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context; val d = ctx.resources.displayMetrics.density
            val root = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT); setBackgroundColor(Color.WHITE) }
            val lmain = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt())
                layoutParams = lp
                setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()); elevation = 4f*d; setBackgroundColor(Color.WHITE)
            }
            val linear4 = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (50*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            val linear11 = LinearLayout(ctx).apply { val lp = LinearLayout.LayoutParams((50*d).toInt(), (50*d).toInt()); layoutParams = lp; gravity = Gravity.CENTER; try { background = ContextCompat.getDrawable(ctx, R.drawable.ic_1_4) } catch (e: Exception) {} }
            val number = TextView(ctx).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); textSize = 11f; setTextColor(Color.parseColor("#607D8B")); setTypeface(null, android.graphics.Typeface.BOLD) }
            linear11.addView(number)
            val spacer0 = LinearLayout(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f); layoutParams = lp }
            val playBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {} }
            val shareBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.share_round) } catch (e: Exception) {} }
            val copyBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; rotation = 180f; scaleX = -1f; try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {} }
            val bookmarkBtn = TextView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; text = "📑"; textSize = 20f; gravity = Gravity.CENTER; isFocusable = false }
            val linear3 = LinearLayout(ctx).apply { val lp = LinearLayout.LayoutParams(0, 0); layoutParams = lp; try { background = ContextCompat.getDrawable(ctx, R.drawable.baseline_content_copy_24) } catch (e: Exception) {} }
            linear4.addView(linear11); linear4.addView(spacer0); linear4.addView(playBtn); linear4.addView(shareBtn); linear4.addView(copyBtn); linear4.addView(bookmarkBtn); linear4.addView(linear3)
            val ayaArabic = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; textSize = 28f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL; setTypeface(null, android.graphics.Typeface.BOLD); try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {} }
            val kanzul = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; text = "কানযুল ঈমান"; setBackgroundColor(Color.parseColor("#E0F2F1")); setTextColor(Color.parseColor("#009688")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
            val nameTv = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; textSize = 16f; setTextColor(Color.BLACK) }
            val irfan = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; text = "ইরফানুল কুরআন"; setBackgroundColor(Color.parseColor("#E3F2FD")); setTextColor(Color.parseColor("#1E88E5")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
            val ayaNumber = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; textSize = 16f; setTextColor(Color.BLACK) }
            lmain.addView(linear4); lmain.addView(ayaArabic); lmain.addView(kanzul); lmain.addView(nameTv); lmain.addView(irfan); lmain.addView(ayaNumber)
            root.addView(lmain)
            val item = list[position]
            val itemId = item.optString("_id")
            if (itemId == currentPlayingId) {
                if (isAudioPlaying()) playBtn.setImageResource(R.drawable.pause_circle) else playBtn.setImageResource(R.drawable.play_circle)
                val highlightDrawable = GradientDrawable().apply { setStroke((2*d).toInt(), Color.parseColor("#01837A")); setColor(Color.parseColor("#E0F7FA")); cornerRadius = 12f*d }
                lmain.background = highlightDrawable; lmain.elevation = 8f*d
            } else {
                playBtn.setImageResource(R.drawable.play_circle)
                val normalDrawable = GradientDrawable().apply { setStroke(d.toInt(), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = 12f*d }
                lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), normalDrawable, null); lmain.elevation = 6f*d
            }
            val bm = isBookmarked(itemId, currentSuraAuthor)
            bookmarkBtn.text = if (bm) "🔖" else "📑"
            try {
                nameTv.text = replaceArabicNumber("${list[position].getString("verses")}. ${list[position].getString("name")}")
                ayaArabic.text = replaceArabicNumber(list[position].getString("names"))
                ayaNumber.text = replaceArabicNumber(list[position].getString("author"))
                number.text = replaceArabicNumber(list[position].getString("verses"))
            } catch (e: JSONException) { e.printStackTrace() }
            playBtn.setOnClickListener { playme(item); if (isAudioPlaying()) playAudioIv.setImageResource(R.drawable.pause) else playAudioIv.setImageResource(R.drawable.play) }
            copyBtn.setOnClickListener { copyme(item) }
            shareBtn.setOnClickListener { shareme(item) }
            bookmarkBtn.setOnClickListener { toggleBookmark(item) }
            return root
        }
    }

    inner class GlobalSearchAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        fun getItemAt(pos: Int): JSONObject? = if (pos < list.size) list[pos] else null
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context; val d = ctx.resources.displayMetrics.density
            val root = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT); setBackgroundColor(Color.WHITE) }
            val suraHeader = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (5*d).toInt(), (10*d).toInt(), (2*d).toInt()); layoutParams = lp; textSize = 12f; setTextColor(Color.parseColor("#01837A")); setTypeface(null, android.graphics.Typeface.BOLD); try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {} }
            val lmain = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()); elevation = 4f*d; setBackgroundColor(Color.WHITE) }
            val topRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (50*d).toInt()); layoutParams = lp }
            val num = TextView(ctx).apply { val lp = LinearLayout.LayoutParams((50*d).toInt(), (50*d).toInt()); layoutParams = lp; gravity = Gravity.CENTER; try { background = ContextCompat.getDrawable(ctx, R.drawable.ic_1_4) } catch (e: Exception) {}; textSize = 11f; setTextColor(Color.parseColor("#607D8B")); setTypeface(null, android.graphics.Typeface.BOLD) }
            val playBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {} }
            val copyBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; rotation = 180f; scaleX = -1f; try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {} }
            val shareBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.share_round) } catch (e: Exception) {} }
            val bookmarkBtn = TextView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; text = "📑"; textSize = 20f; gravity = Gravity.CENTER; isFocusable = false }
            topRow.addView(num); topRow.addView(playBtn); topRow.addView(copyBtn); topRow.addView(shareBtn); topRow.addView(bookmarkBtn)
            val ayaArabic = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; textSize = 24f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL; try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {} }
            val nameTv = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; textSize = 16f; setTextColor(Color.BLACK) }
            lmain.addView(topRow); lmain.addView(ayaArabic); lmain.addView(nameTv)
            root.addView(suraHeader); root.addView(lmain)
            val item = list[position]
            suraHeader.text = "${item.optString("suraName")} - আয়াত ${item.optString("verses")}"
            num.text = item.optString("verses"); ayaArabic.text = replaceArabicNumber(item.optString("names")); nameTv.text = replaceArabicNumber(item.optString("name"))
            playBtn.setOnClickListener {
                currentSuraAuthor = item.optString("suraAuthor"); currentSuraBangla = item.optString("suraName"); currentSuraNumber = item.optString("suraNumber").toIntOrNull() ?: getSuraNumberFromAuthor(currentSuraAuthor)
                loadAyaList("${currentSuraAuthor}.json"); switchMode(Mode.AYA_LIST)
                listView1.postDelayed({ val target = item.optString("verses"); for (i in filteredAya.indices) if (filteredAya[i].optString("verses") == target) { listView1.setSelection(i); break } }, 300)
            }
            copyBtn.setOnClickListener { val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; clipboard.setPrimaryClip(ClipData.newPlainText("ayah", "${item.optString("names")}\n${item.optString("name")}")); Toast.makeText(ctx, "কপি হয়েছে", Toast.LENGTH_SHORT).show() }
            shareBtn.setOnClickListener { val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, "${item.optString("names")}\n${item.optString("name")}\n${item.optString("suraName")}"); ctx.startActivity(Intent.createChooser(share, "শেয়ার")) }
            bookmarkBtn.setOnClickListener { toggleBookmark(item) }
            val drawable = GradientDrawable().apply { setStroke(d.toInt(), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = 12f*d }
            lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), drawable, null); lmain.elevation = 6f*d
            return root
        }
    }

    inner class BookmarkAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        fun getItemAt(pos: Int): JSONObject? = if (pos < list.size) list[pos] else null
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context; val d = ctx.resources.displayMetrics.density
            val root = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT); setBackgroundColor(Color.WHITE) }
            val header = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (5*d).toInt(), (10*d).toInt(), (2*d).toInt()); layoutParams = lp; textSize = 12f; setTextColor(Color.parseColor("#01837A")); setTypeface(null, android.graphics.Typeface.BOLD); try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {} }
            val lmain = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()); elevation = 4f*d; setBackgroundColor(Color.WHITE) }
            val topRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (50*d).toInt()); layoutParams = lp }
            val cancelBtn = TextView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; text = "❌"; textSize = 20f; gravity = Gravity.CENTER; isFocusable = false }
            val playBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {} }
            val copyBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; rotation = 180f; scaleX = -1f; try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {} }
            val shareBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); layoutParams = lp; setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.share_round) } catch (e: Exception) {} }
            topRow.addView(cancelBtn); topRow.addView(playBtn); topRow.addView(copyBtn); topRow.addView(shareBtn)
            val ayaArabic = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; textSize = 24f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL; try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {} }
            val nameTv = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()); layoutParams = lp; textSize = 16f; setTextColor(Color.BLACK) }
            lmain.addView(topRow); lmain.addView(ayaArabic); lmain.addView(nameTv)
            root.addView(header); root.addView(lmain)
            val item = list[position]
            header.text = "${item.optString("suraName")} - আয়াত ${item.optString("ayahNumber")}"
            ayaArabic.text = replaceArabicNumber(item.optString("names")); nameTv.text = replaceArabicNumber(item.optString("name"))
            cancelBtn.setOnClickListener {
                val prefs = ctx.getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
                val jsonStr = prefs.getString("bookmarks_json","[]")
                val arr = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
                val newArr = JSONArray(); for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); if (!(o.optString("_id")==item.optString("_id") && o.optString("suraAuthor")==item.optString("suraAuthor"))) newArr.put(o) }
                prefs.edit().putString("bookmarks_json", newArr.toString()).apply()
                list.removeAt(position); notifyDataSetChanged()
                Toast.makeText(ctx, "বুকমার্ক থেকে বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                if (list.isEmpty()) nores.visibility = View.VISIBLE
            }
            playBtn.setOnClickListener {
                currentSuraAuthor = item.optString("suraAuthor"); currentSuraBangla = item.optString("suraName"); currentSuraNumber = item.optString("suraNumber").toIntOrNull() ?: getSuraNumberFromAuthor(currentSuraAuthor)
                loadAyaList("${currentSuraAuthor}.json"); switchMode(Mode.AYA_LIST)
                listView1.postDelayed({ val target = item.optString("ayahNumber"); for (i in filteredAya.indices) if (filteredAya[i].optString("verses") == target) { listView1.setSelection(i); break } }, 300)
            }
            copyBtn.setOnClickListener { val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; clipboard.setPrimaryClip(ClipData.newPlainText("ayah", "${item.optString("names")}\n${item.optString("name")}")); Toast.makeText(ctx, "কপি হয়েছে", Toast.LENGTH_SHORT).show() }
            shareBtn.setOnClickListener { val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, "${item.optString("names")}\n${item.optString("name")}\n${item.optString("suraName")}"); ctx.startActivity(Intent.createChooser(share, "শেয়ার")) }
            val drawable = GradientDrawable().apply { setStroke(d.toInt(), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = 12f*d }
            lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), drawable, null); lmain.elevation = 6f*d
            return root
        }
    }

    private fun replaceArabicNumber(n: String): String {
        return n.replace("1","১").replace("2","২").replace("3","৩").replace("4","৪").replace("5","৫").replace("6","৬").replace("7","৭").replace("8","৮").replace("9","৯").replace("0","০")
            .replace("<b>"," ").replace("</b>"," ").replace("(রহঃ)","(رحمة الله)").replace("(রাঃ)","(رضي الله عنه)")
            .replace("(সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)","(ﷺ)").replace(" (সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)","(ﷺ)")
            .replace("('আঃ)","(عليه السلام)").replace("[১]","").replace("[২]","").replace("[৩]","").replace("(রহ)","(رحمة الله)")
            .replace("(রা)","(رضي الله عنه)").replace("(সা)","(ﷺ)").replace("('আ)","(عليه السلام)").replace("(সাঃ)","(ﷺ)").replace("(স)","(ﷺ)")
            .replace("বিবিন্‌ত","বিন্‌ত").replace("বিন্ত","বিন্‌ত").replace("(সা.)","(ﷺ)").replace("(স.)","(ﷺ)")
    }
}
