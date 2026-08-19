package com.srizwan.islamipedia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
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
    private var lastQuery = ""
    private lateinit var root: ConstraintLayout
    private lateinit var topBar: LinearLayout
    private lateinit var backIv: ImageView
    private lateinit var headingTv: TextView
    private lateinit var bookmarkViewBtn: TextView
    private lateinit var jumpIv: ImageView
    private lateinit var searchIv: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var searchIconInside: ImageView
    private lateinit var searchbox: EditText
    private lateinit var cancelIv: ImageView
    private lateinit var progressContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var nores: LinearLayout
    private lateinit var noresTv: TextView
    private lateinit var noresImg: ImageView
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
    private lateinit var suraType: Array<String>
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

    private fun dp(i: Int): Int { return (i * resources.displayMetrics.density).toInt() }
    private fun dpF(f: Float): Float { return f * resources.displayMetrics.density }

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
                w.navigationBarColor = Color.parseColor("#01837A")
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentMode == Mode.GLOBAL_SEARCH) {
                    if (searchView.visibility == View.VISIBLE && searchbox.text.toString().isEmpty()) {
                        switchMode(Mode.SURA_LIST)
                    } else if (searchView.visibility == View.VISIBLE) {
                        searchbox.text.clear()
                    } else {
                        switchMode(Mode.SURA_LIST)
                    }
                    return
                }
                if (searchView.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) searchView.visibility = View.GONE else searchbox.text.clear()
                } else if (currentMode != Mode.SURA_LIST) {
                    // Stop audio when exiting AYA_LIST
                    if (currentMode == Mode.AYA_LIST) {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        currentPlayingId = null
                        currentIndex = 0
                        try { playAudioIv.setImageResource(R.drawable.play) } catch (e: Exception) {}
                    }
                    switchMode(Mode.SURA_LIST)
                } else finish()
            }
        })
    }

    private fun createMainLayout(): ConstraintLayout {
        val rootLayout = ConstraintLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            try { background = ContextCompat.getDrawable(context, R.drawable.back1ground) } catch (e: Exception) { setBackgroundColor(Color.parseColor("#F5F5F5")) }
            fitsSystemWindows = true
        }

        topBar = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#01837A"))
            elevation = dpF(5f)
            val lp = ConstraintLayout.LayoutParams(0, dp(65))
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
        }
        backIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
            setPadding(dp(15), dp(15), dp(15), dp(15))
            scaleType = ImageView.ScaleType.CENTER_CROP
            try { setImageResource(R.drawable.ic_arrow_back_white) } catch (e: Exception) {}
            setColorFilter(Color.WHITE)
        }
        headingTv = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            lp.leftMargin = dp(5)
            layoutParams = lp
            setTextColor(Color.WHITE)
            textSize = 18f
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isFocusable = true
            isFocusableInTouchMode = true
            setHorizontallyScrolling(true)
            gravity = Gravity.CENTER_VERTICAL
            setTypeface(typeface, Typeface.BOLD)
            text = intent.getStringExtra("sub") ?: "আল কুরআন"
        }
        bookmarkViewBtn = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
            text = "⭐"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        jumpIv = ImageView(this).apply {
            val lp2 = LinearLayout.LayoutParams(dp(30), dp(30))
            lp2.rightMargin = dp(10)
            layoutParams = lp2
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.ic_jump_page) } catch (e: Exception) {}
            setColorFilter(Color.WHITE)
            visibility = View.GONE
        }
        searchIv = ImageView(this).apply {
            val lp2 = LinearLayout.LayoutParams(dp(30), dp(30))
            lp2.rightMargin = dp(8)
            layoutParams = lp2
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}
            setColorFilter(Color.WHITE)
        }
        topBar.addView(backIv)
        topBar.addView(headingTv)
        topBar.addView(bookmarkViewBtn)
        topBar.addView(jumpIv)
        topBar.addView(searchIv)

        searchView = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
            gravity = Gravity.CENTER_VERTICAL
            val lp = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topToBottom = topBar.id
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.topMargin = dp(10)
            lp.leftMargin = dp(10)
            lp.rightMargin = dp(10)
            layoutParams = lp
            val bg = GradientDrawable()
            bg.setColor(Color.WHITE)
            bg.cornerRadius = dpF(100f)
            bg.setStroke(dp(1), Color.parseColor("#01837A"))
            background = bg
            setPadding(dp(8), dp(4), dp(8), dp(4))
            elevation = dpF(2f)
        }
        searchIconInside = ImageView(this).apply {
            val lp2 = LinearLayout.LayoutParams(dp(24), dp(24))
            lp2.leftMargin = dp(6)
            layoutParams = lp2
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}
            setColorFilter(Color.parseColor("#01837A"))
            alpha = 0.7f
        }
        searchbox = EditText(this).apply {
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            lp.leftMargin = dp(6)
            layoutParams = lp
            setPadding(dp(8), dp(12), dp(8), dp(12))
            setTextColor(Color.BLACK)
            setHintTextColor(Color.parseColor("#9901837A"))
            textSize = 15f
            hint = "সুরা সার্চ করুন"
            background = null
            isSingleLine = true
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        cancelIv = ImageView(this).apply {
            val lp2 = LinearLayout.LayoutParams(dp(32), dp(32))
            lp2.rightMargin = dp(4)
            layoutParams = lp2
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.cancel) } catch (e: Exception) {}
            setColorFilter(Color.parseColor("#01837A"))
        }
        searchView.addView(searchIconInside)
        searchView.addView(searchbox)
        searchView.addView(cancelIv)

        progressContainer = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setPadding(dp(12), dp(8), dp(12), dp(8))
            val lp = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topToBottom = searchView.id
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
        }
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(8), 1f)
            max = 100
            progressTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
        }
        progressText = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.leftMargin = dp(8)
            layoutParams = lp
            text = "⏳ সার্চ চলছে..."
            textSize = 13f
            setTextColor(Color.parseColor("#607D8B"))
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        progressContainer.addView(progressBar)
        progressContainer.addView(progressText)

        nores = LinearLayout(this).apply {
            id = View.generateViewId()
            visibility = View.GONE
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(24), dp(16), dp(24))
            val lp = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topToBottom = progressContainer.id
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
            val bg = GradientDrawable()
            bg.setColor(Color.WHITE)
            bg.cornerRadius = dpF(12f)
            background = bg
            elevation = dpF(2f)
        }
        noresImg = ImageView(this).apply {
            val lp = LinearLayout.LayoutParams(dp(100), dp(100))
            lp.gravity = Gravity.CENTER
            layoutParams = lp
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.noresult) } catch (e: Exception) {}
        }
        noresTv = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.CENTER
            lp.topMargin = dp(8)
            layoutParams = lp
            text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        nores.addView(noresImg)
        nores.addView(noresTv)

        audiotab = LinearLayout(this).apply {
            id = View.generateViewId()
            visibility = View.GONE
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#01837A"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            val lp = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams = lp
            elevation = dpF(8f)
        }
        previousLL = LinearLayout(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.leftMargin = dp(10)
            layoutParams = lp
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        val prevImg = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(30), dp(30))
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.previous) } catch (e: Exception) {}
            setColorFilter(Color.WHITE)
        }
        previousLL.addView(prevImg)
        val between0 = LinearLayout(this).apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        playAudioLL = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        playAudioIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(30), dp(30))
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.play) } catch (e: Exception) {}
            setColorFilter(Color.WHITE)
        }
        playAudioLL.addView(playAudioIv)
        val between1 = LinearLayout(this).apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        nextLL = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        val nextImg = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(30), dp(30))
            scaleType = ImageView.ScaleType.FIT_CENTER
            rotation = 180f
            try { setImageResource(R.drawable.previous) } catch (e: Exception) {}
            setColorFilter(Color.WHITE)
        }
        nextLL.addView(nextImg)
        val between2 = LinearLayout(this).apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        qariSelectorTv = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(dp(110), ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.rightMargin = dp(4)
            layoutParams = lp
            text = selectedQariName
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(dp(4), dp(4), dp(4), dp(4))
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            val bg = GradientDrawable()
            bg.setColor(Color.parseColor("#00695C"))
            bg.cornerRadius = dpF(20f)
            background = bg
        }
        stopLL = LinearLayout(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.rightMargin = dp(10)
            layoutParams = lp
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        val stopImg = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(30), dp(30))
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.stop) } catch (e: Exception) {}
            setColorFilter(Color.WHITE)
        }
        stopLL.addView(stopImg)
        audiotab.addView(previousLL)
        audiotab.addView(between0)
        audiotab.addView(playAudioLL)
        audiotab.addView(between1)
        audiotab.addView(nextLL)
        audiotab.addView(between2)
        audiotab.addView(qariSelectorTv)
        audiotab.addView(stopLL)

        listView1 = ListView(this).apply {
            id = View.generateViewId()
            val lp = ConstraintLayout.LayoutParams(0, 0)
            lp.topToBottom = nores.id
            lp.bottomToTop = audiotab.id
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.topMargin = dp(6)
            layoutParams = lp
            divider = null
            dividerHeight = 0
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            selector = android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
            isFastScrollEnabled = true
            clipToPadding = false
            setPadding(0, 0, 0, dp(80))
        }

        fabGlobalSearch = FloatingActionButton(this).apply {
            val lp = ConstraintLayout.LayoutParams(dp(56), dp(56))
            lp.bottomToTop = audiotab.id
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            lp.bottomMargin = dp(16)
            lp.rightMargin = dp(16)
            layoutParams = lp
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) { setImageResource(android.R.drawable.ic_search_category_default) }
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
            imageTintList = ColorStateList.valueOf(Color.WHITE)
        }

        rootLayout.addView(topBar)
        rootLayout.addView(searchView)
        rootLayout.addView(progressContainer)
        rootLayout.addView(nores)
        rootLayout.addView(listView1)
        rootLayout.addView(audiotab)
        rootLayout.addView(fabGlobalSearch)
        return rootLayout
    }

    private fun setupListeners() {
        backIv.setOnClickListener { 
            if (currentMode == Mode.AYA_LIST) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                currentPlayingId = null
                currentIndex = 0
                try { playAudioIv.setImageResource(R.drawable.play) } catch (e: Exception) {}
            }
            if (currentMode != Mode.SURA_LIST) switchMode(Mode.SURA_LIST) else finish() 
        }
        bookmarkViewBtn.setOnClickListener { loadBookmarksAndSwitch() }
        searchIv.setOnClickListener { searchView.visibility = if (searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE; if (searchView.visibility == View.VISIBLE) searchbox.requestFocus() }
        cancelIv.setOnClickListener { if (searchbox.text.toString() == "") searchView.visibility = View.GONE else searchbox.text.clear() }
        jumpIv.setOnClickListener { showPageJumpDialog() }
        fabGlobalSearch.setOnClickListener { switchMode(Mode.GLOBAL_SEARCH); searchView.visibility = View.VISIBLE; searchbox.requestFocus(); searchbox.hint = "পুরো কুরআনে সার্চ করুন" }

        searchbox.addTextChangedListener { s ->
            val q = s.toString()
            lastQuery = q
            when (currentMode) {
                Mode.SURA_LIST -> filterSuraList(q)
                Mode.AYA_LIST -> filterAyaList(q)
                Mode.GLOBAL_SEARCH -> { if (q.length >= 2) performGlobalSearch(q) else { globalList.clear(); listView1.adapter = GlobalSearchAdapter(this, globalList); progressText.text = "কমপক্ষে ২ অক্ষর লিখুন" } }
                Mode.BOOKMARK -> filterBookmarkList(q)
                else -> {}
            }
        }

        listView1.setOnItemClickListener { _, _, position, _ ->
            when (currentMode) {
                Mode.SURA_LIST -> {
                    if (position < filteredSura.size) {
                        val selected = filteredSura[position]
                        currentSuraBangla = selected.getString("name")
                        currentSuraAuthor = selected.getString("author")
                        currentSuraNumber = getSuraNumberFromAuthor(currentSuraAuthor)
                        // Clear search box as requested
                        searchbox.text.clear()
                        searchView.visibility = View.GONE
                        loadAyaList("${currentSuraAuthor}.json")
                        switchMode(Mode.AYA_LIST)
                    }
                }
                Mode.BOOKMARK -> {
                    val item = (listView1.adapter as? BookmarkAdapter)?.getItemAt(position)
                    item?.let {
                        currentSuraAuthor = it.optString("suraAuthor")
                        currentSuraBangla = it.optString("suraName")
                        currentSuraNumber = it.optString("suraNumber").toIntOrNull() ?: getSuraNumberFromAuthor(currentSuraAuthor)
                        loadAyaList("${currentSuraAuthor}.json")
                        switchMode(Mode.AYA_LIST)
                        listView1.postDelayed({
                            val target = it.optString("ayahNumber")
                            for (i in filteredAya.indices) if (filteredAya[i].optString("verses") == target) { listView1.setSelection(i); break }
                        }, 300)
                    }
                }
                Mode.GLOBAL_SEARCH -> {
                    val item = (listView1.adapter as? GlobalSearchAdapter)?.getItemAt(position)
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
        playAudioLL.setOnClickListener { playAudioIv.performClick() }
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
                selectedQariName = banglaName
                qariSelectorTv.text = banglaName
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
                bookmarkViewBtn.visibility = View.VISIBLE
                fabGlobalSearch.visibility = View.VISIBLE
                searchView.visibility = View.GONE
                if (filteredSura.isEmpty()) loadSuraList()
                nores.visibility = if (filteredSura.isEmpty()) View.VISIBLE else View.GONE
                if (filteredSura.isEmpty()) noresTv.text = "কোন সুরা পাওয়া যায়নি"
                listView1.visibility = View.VISIBLE
                listView1.adapter = QuranAdapter(this, filteredSura)
            }
            Mode.AYA_LIST -> {
                headingTv.text = currentSuraBangla
                searchbox.hint = "আয়াত সার্চ করুন"
                audiotab.visibility = View.VISIBLE; progressContainer.visibility = View.GONE; jumpIv.visibility = View.VISIBLE; nores.visibility = View.GONE
                fabGlobalSearch.visibility = View.GONE
                bookmarkViewBtn.visibility = View.GONE
                searchView.visibility = View.GONE
                listView1.adapter = QuranviewAdapter(this, filteredAya)
            }
            Mode.GLOBAL_SEARCH -> {
                headingTv.text = "গ্লোবাল সার্চ"
                searchbox.hint = "পুরো কুরআনে সার্চ করুন"
                searchView.visibility = View.VISIBLE; audiotab.visibility = View.GONE; jumpIv.visibility = View.GONE; progressContainer.visibility = View.VISIBLE; nores.visibility = View.GONE
                bookmarkViewBtn.visibility = View.VISIBLE
                fabGlobalSearch.visibility = View.GONE
                globalList.clear()
                listView1.adapter = GlobalSearchAdapter(this, globalList)
                progressBar.progress = 0; progressText.text = "🔍 কমপক্ষে ২ অক্ষর লিখুন"
            }
            Mode.BOOKMARK -> {
                headingTv.text = "বুকমার্ক"
                searchbox.hint = "বুকমার্ক সার্চ"
                audiotab.visibility = View.GONE; progressContainer.visibility = View.GONE; jumpIv.visibility = View.GONE; searchView.visibility = View.GONE; fabGlobalSearch.visibility = View.GONE; bookmarkViewBtn.visibility = View.VISIBLE
                if (bookmarkList.isEmpty()) {
                    nores.visibility = View.VISIBLE
                    noresTv.text = "কোন বুকমার্ক নেই\n⭐ আইকনে ক্লিক করে বুকমার্ক যোগ করুন"
                } else nores.visibility = View.GONE
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
            suraName = Array(arr.length()) { "" }; suraAuthor = Array(arr.length()) { "" }; suraBookId = Array(arr.length()) { "" }; suraVerses = Array(arr.length()) { "" }; suraNamesAr = Array(arr.length()) { "" }; suraType = Array(arr.length()) { "" }
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i); suraList.add(o)
                suraName[i] = o.getString("name"); suraAuthor[i] = o.getString("author"); suraBookId[i] = o.getString("bookid"); suraVerses[i] = o.getString("verses"); suraNamesAr[i] = o.getString("names"); suraType[i] = o.optString("type", "")
                allSuraAuthors.add(o.getString("author")); suraInfoMap[o.getString("author")] = o
            }
            filteredSura = ArrayList(suraList)
        } catch (e: Exception) { e.printStackTrace(); suraList = ArrayList(); filteredSura = ArrayList() }
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
        lastQuery = query
        filteredSura.clear()
        if (query.isEmpty()) filteredSura.addAll(suraList)
        else { for (i in suraName.indices) if (suraName[i].contains(query, true) || suraNamesAr[i].contains(query, true) || suraBookId[i].contains(query, true) || suraType[i].contains(query, true) || getBanglaType(suraType[i]).contains(query, true)) { filteredSura.add(suraList[i]) } }
        nores.visibility = if (filteredSura.isEmpty()) View.VISIBLE else View.GONE
        if (filteredSura.isEmpty()) noresTv.text = "“$query” এর জন্য কোন সুরা পাওয়া যায়নি"
        listView1.adapter = QuranAdapter(this, filteredSura)
    }

    private fun filterAyaList(query: String) {
        lastQuery = query
        filteredAya.clear()
        if (query.isEmpty()) filteredAya.addAll(ayaList)
        else { for (i in ayaName.indices) if (ayaName[i].contains(query, true) || ayaNamesAr[i].contains(query, true) || ayaVerses[i].contains(query, true)) { filteredAya.add(ayaList[i]) } }
        nores.visibility = if (filteredAya.isEmpty()) View.VISIBLE else View.GONE
        if (filteredAya.isEmpty()) noresTv.text = "“$query” এর জন্য কোন আয়াত পাওয়া যায়নি"
        listView1.adapter = QuranviewAdapter(this, filteredAya)
    }

    private fun filterBookmarkList(query: String) {
        if (query.isEmpty()) { listView1.adapter = BookmarkAdapter(this, bookmarkList); nores.visibility = if (bookmarkList.isEmpty()) View.VISIBLE else View.GONE; return }
        val filtered = bookmarkList.filter { it.optString("name").contains(query, true) || it.optString("names").contains(query, true) }
        listView1.adapter = BookmarkAdapter(this, ArrayList(filtered))
        nores.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
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

    private fun loadBookmarksAndSwitch() {
        val prefsBm = getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefsBm.getString("bookmarks_json", "[]")) } catch (e: Exception) { JSONArray() }
        bookmarkList.clear()
        for (i in 0 until arr.length()) bookmarkList.add(arr.getJSONObject(i))
        switchMode(Mode.BOOKMARK)
    }

    private fun performGlobalSearch(query: String) {
        lastQuery = query
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
                            val newObj = JSONObject(obj.toString())
                            newObj.put("suraName", suraInfo?.optString("name") ?: author); newObj.put("suraAuthor", author); newObj.put("suraNumber", suraInfo?.optString("bookid") ?: "1")
                            matches.add(newObj)
                        }
                    }
                } catch (e: Exception) {}
                found += matches.size; globalList.addAll(matches)
                runOnUiThread {
                    progressBar.progress = scanned * 100 / total
                    progressText.text = "⏳ $scanned/$total স্ক্যান - $found টি আয়াত পাওয়া গেছে"
                    listView1.adapter = GlobalSearchAdapter(this, ArrayList(globalList))
                }
            }
            runOnUiThread {
                progressText.text = "✅ $found টি আয়াত পাওয়া গেছে"
                if (globalList.isEmpty()) { nores.visibility = View.VISIBLE; noresTv.text = "“$query” এর জন্য কোন আয়াত পাওয়া যায়নি" } else nores.visibility = View.GONE
            }
        }.start()
    }

    private fun showPageJumpDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
        }
        val header = TextView(this).apply {
            text = "কোন আয়াতে যাবেন?"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.parseColor("#01837A"))
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = dp(12)
            layoutParams = lp
        }
        val input = EditText(this).apply {
            hint = "আয়াত নম্বর লিখুন"
            setHintTextColor(Color.parseColor("#9901837A"))
            setTextColor(ContextCompat.getColor(context, R.color.purple_500))
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            try { background = ContextCompat.getDrawable(context, R.drawable.edit) } catch (e: Exception) {
                val bg = GradientDrawable(); bg.setColor(Color.WHITE); bg.setStroke(dp(1), Color.parseColor("#01837A")); bg.cornerRadius = dpF(8f); background = bg
            }
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48))
        }
        container.addView(header); container.addView(input)
        AlertDialog.Builder(this).setView(container)
            .setPositiveButton("যান") { _, _ ->
                val num = input.text.toString().toIntOrNull()
                if (num != null) {
                    var found = false
                    for (i in filteredAya.indices) if (filteredAya[i].optString("verses").toIntOrNull() == num) { listView1.setSelection(i); found = true; break }
                    if (!found) Toast.makeText(this, "আয়াত $num পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("বাতিল", null).show()
    }

    override fun onDestroy() { super.onDestroy(); mediaPlayer?.release(); mediaPlayer = null }

    private fun getHighlightedText(fullText: String, query: String): SpannableString {
        val spannable = SpannableString(fullText)
        if (query.isEmpty()) return spannable
        try {
            val lowerFull = fullText.lowercase()
            val lowerQuery = query.lowercase()
            var start = lowerFull.indexOf(lowerQuery)
            while (start >= 0) {
                spannable.setSpan(BackgroundColorSpan(Color.YELLOW), start, start + query.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = lowerFull.indexOf(lowerQuery, start + query.length)
            }
        } catch (e: Exception) {}
        return spannable
    }

    inner class QuranAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context
            val itemView = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT)
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(dp(3), dp(3), dp(3), dp(0))
            }
            val lmain = LinearLayout(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68))
                lp.setMargins(dp(3), dp(3), dp(3), dp(3))
                layoutParams = lp
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                elevation = dpF(6f)
            }
            val sketchUi = GradientDrawable().apply { setStroke(dp(1), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = dpF(12f) }
            lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), sketchUi, null)

            val linear5 = LinearLayout(ctx).apply {
                val lp = LinearLayout.LayoutParams(dp(46), dp(46))
                lp.leftMargin = dp(10)
                layoutParams = lp
                gravity = Gravity.CENTER
                try { background = ContextCompat.getDrawable(ctx, R.drawable.quran) } catch (e: Exception) { val g = GradientDrawable(); g.setColor(Color.parseColor("#E0F2F1")); g.cornerRadius = dpF(12f); background = g }
            }
            val number = TextView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textSize = 11f
                setTextColor(Color.parseColor("#5A0202"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
                setPadding(0, 0, 0, dp(10))
            }
            linear5.addView(number)
            val surabox = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.leftMargin = dp(4); layoutParams = lp }
            val nameTv = TextView(ctx).apply { textSize = 16f; setTextColor(Color.BLACK); try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {} }
            val ayaNumTv = TextView(ctx).apply { textSize = 13f; setTextColor(Color.parseColor("#607D8B")); try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {} }
            surabox.addView(nameTv); surabox.addView(ayaNumTv)
            val spacer = LinearLayout(ctx).apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            val arabicTv = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.rightMargin = dp(10)
                layoutParams = lp
                textSize = 16f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; setTypeface(null, Typeface.BOLD)
                textDirection = View.TEXT_DIRECTION_RTL
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {
                    try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (ee: Exception) {}
                }
            }
            lmain.addView(linear5); lmain.addView(surabox); lmain.addView(spacer); lmain.addView(arabicTv)
            itemView.addView(lmain)

            try {
                val obj = list[position]
                val rawName = obj.getString("name")
                val rawVerses = obj.getString("verses")
                val rawBookId = obj.getString("bookid")
                val rawArabic = obj.getString("names")
                val formattedName = replaceArabicNumber(rawName)
                val formattedArabic = replaceArabicNumber(rawArabic)
                val bookid1 = replaceArabicNumber(rawBookId)
                val displayId = if (bookid1.startsWith("০") || bookid1.startsWith("0")) bookid1.drop(1) else bookid1
                number.text = displayId
                val typeBangla = getBanglaType(list[position].optString("type", suraType.getOrNull(position) ?: ""))
                val typeSuffix = if (typeBangla.isNotEmpty()) " | $typeBangla" else ""
                val totalText = "মোট আয়াত : ${replaceArabicNumber(rawVerses)}$typeSuffix"
                if (lastQuery.isNotEmpty() && currentMode == Mode.SURA_LIST) {
                    nameTv.text = getHighlightedText(formattedName, replaceArabicNumber(lastQuery))
                    ayaNumTv.text = getHighlightedText(totalText, replaceArabicNumber(lastQuery))
                } else { nameTv.text = formattedName; ayaNumTv.text = totalText }
                arabicTv.text = formattedArabic
            } catch (e: JSONException) { e.printStackTrace() }
            return itemView
        }
    }

    inner class QuranviewAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context
            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT)
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(dp(6), dp(6), dp(6), dp(6))
            }
            val lmain = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(dp(8), dp(8), dp(8), dp(8))
                elevation = dpF(4f)
            }
            val linear4 = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)); setPadding(dp(5), dp(5), dp(5), dp(5)) }
            val linear11 = LinearLayout(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(50), dp(50))
                gravity = Gravity.CENTER
                try { background = ContextCompat.getDrawable(ctx, R.drawable.ic_1_4) } catch (e: Exception) { val g = GradientDrawable(); g.setColor(Color.parseColor("#E0F2F1")); g.cornerRadius = dpF(25f); background = g }
            }
            val number = TextView(ctx).apply { 
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textSize = 13f
                setTextColor(Color.parseColor("#004D40"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            linear11.addView(number)
            val playBtn = ImageView(ctx).apply { layoutParams = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins(dp(5), dp(5), dp(5), dp(5)) }; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {} }
            val shareBtn = ImageView(ctx).apply { layoutParams = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins(dp(5), dp(5), dp(5), dp(5)) }; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; try { setImageResource(R.drawable.share_round) } catch (e: Exception) {} }
            val copyBtn = ImageView(ctx).apply { layoutParams = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins(dp(5), dp(5), dp(5), dp(5)) }; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; rotation = 180f; scaleX = -1f; try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {} }
            val bookmarkBtn = TextView(ctx).apply { layoutParams = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins(dp(5), dp(5), dp(5), dp(5)) }; text = "📑"; textSize = 20f; gravity = Gravity.CENTER; isFocusable = false }
            linear4.addView(linear11); linear4.addView(playBtn); linear4.addView(shareBtn); linear4.addView(copyBtn); linear4.addView(bookmarkBtn)

            val ayaArabic = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(10), dp(10), dp(10), dp(10)) }
                textSize = 28f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL; setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) { try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (ee: Exception) {} }
            }
            val kanzul = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(10), dp(10), dp(10), dp(10)) }
                text = "কানযুল ঈমান"; setBackgroundColor(Color.parseColor("#E0F2F1")); setTextColor(Color.parseColor("#00695C")); textSize = 13f; setTypeface(null, Typeface.BOLD); setPadding(dp(10), dp(6), dp(10), dp(6))
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val nameTv = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(10), dp(4), dp(10), dp(10)) }
                textSize = 16f; setTextColor(Color.BLACK)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val irfan = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(10), dp(10), dp(10), dp(10)) }
                text = "ইরফানুল কুরআন"; setBackgroundColor(Color.parseColor("#E3F2FD")); setTextColor(Color.parseColor("#0D47A1")); textSize = 13f; setTypeface(null, Typeface.BOLD); setPadding(dp(10), dp(6), dp(10), dp(6))
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val ayaNumber = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(10), dp(4), dp(10), dp(10)) }
                textSize = 16f; setTextColor(Color.BLACK)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            lmain.addView(linear4); lmain.addView(ayaArabic); lmain.addView(kanzul); lmain.addView(nameTv); lmain.addView(irfan); lmain.addView(ayaNumber)
            root.addView(lmain)

            val item = list[position]
            val itemId = item.optString("_id")
            if (itemId == currentPlayingId) {
                playBtn.setImageResource(if (isAudioPlaying()) R.drawable.pause_circle else R.drawable.play_circle)
                val highlightDrawable = GradientDrawable().apply { setStroke(dp(2), Color.parseColor("#01837A")); setColor(Color.parseColor("#E0F7FA")); cornerRadius = dpF(12f) }
                lmain.background = highlightDrawable; lmain.elevation = dpF(8f)
            } else {
                try { playBtn.setImageResource(R.drawable.play_circle) } catch (e: Exception) {}
                val normalDrawable = GradientDrawable().apply { setStroke(dp(1), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = dpF(12f) }
                lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), normalDrawable, null); lmain.elevation = dpF(4f)
            }
            bookmarkBtn.text = if (isBookmarked(itemId, currentSuraAuthor)) "🔖" else "📑"
            try {
                val rawName = "${list[position].getString("verses")}. ${list[position].getString("name")}"
                val rawArabic = list[position].getString("names")
                val rawTafsir = list[position].getString("author")
                val rawVerses = list[position].getString("verses")
                number.text = replaceArabicNumber(rawVerses)
                if (lastQuery.isNotEmpty() && currentMode == Mode.AYA_LIST) {
                    ayaArabic.text = getHighlightedText(replaceArabicNumber(rawArabic), lastQuery)
                    nameTv.text = getHighlightedText(replaceArabicNumber(rawName), lastQuery)
                    ayaNumber.text = getHighlightedText(replaceArabicNumber(rawTafsir), lastQuery)
                } else {
                    ayaArabic.text = replaceArabicNumber(rawArabic)
                    nameTv.text = replaceArabicNumber(rawName)
                    ayaNumber.text = replaceArabicNumber(rawTafsir)
                }
            } catch (e: JSONException) { e.printStackTrace() }
            playBtn.setOnClickListener { playme(item) }
            copyBtn.setOnClickListener { copyme(item) }
            shareBtn.setOnClickListener { shareme(item) }
            bookmarkBtn.setOnClickListener { toggleBookmark(item); bookmarkBtn.text = if (isBookmarked(itemId, currentSuraAuthor)) "🔖" else "📑" }
            return root
        }
    }

    inner class GlobalSearchAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        fun getItemAt(pos: Int): JSONObject? = if (pos < list.size) list[pos] else null
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context
            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT)
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(dp(6), dp(6), dp(6), dp(6))
            }
            val suraHeader = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(12), dp(6), dp(10), dp(2))
                layoutParams = lp
                textSize = 13f; setTextColor(Color.parseColor("#01837A")); setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val lmain = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(6), dp(6), dp(6), dp(6))
                layoutParams = lp
                setPadding(dp(8), dp(8), dp(8), dp(8))
                elevation = dpF(4f)
                val normalDrawable = GradientDrawable().apply { setStroke(dp(1), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = dpF(12f) }
                background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), normalDrawable, null)
            }
            val topRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)) }
            val num = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(50), dp(50))
                gravity = Gravity.CENTER
                try { background = ContextCompat.getDrawable(ctx, R.drawable.ic_1_4) } catch (e: Exception) { val g = GradientDrawable(); g.setColor(Color.parseColor("#E0F2F1")); g.cornerRadius = dpF(25f); background = g }
                textSize = 12f; setTextColor(Color.parseColor("#607D8B")); setTypeface(null, Typeface.BOLD)
            }
            val playBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {} }
            val copyBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; rotation = 180f; scaleX = -1f; try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {} }
            val shareBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.share_round) } catch (e: Exception) {} }
            val bookmarkBtn = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; text = "📑"; textSize = 20f; gravity = Gravity.CENTER }
            topRow.addView(num); topRow.addView(playBtn); topRow.addView(copyBtn); topRow.addView(shareBtn); topRow.addView(bookmarkBtn)
            val ayaArabic = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(10), dp(10), dp(10))
                layoutParams = lp
                textSize = 26f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL; setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {}
            }
            val kanzul = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(10), dp(10), dp(4))
                layoutParams = lp
                text = "কানযুল ঈমান"
                setBackgroundColor(Color.parseColor("#E0F2F1"))
                setTextColor(Color.parseColor("#00695C"))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val nameTv = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(2), dp(10), dp(8))
                layoutParams = lp
                textSize = 15f; setTextColor(Color.BLACK)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val irfan = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(4), dp(10), dp(4))
                layoutParams = lp
                text = "ইরফানুল কুরআন"
                setBackgroundColor(Color.parseColor("#E3F2FD"))
                setTextColor(Color.parseColor("#0D47A1"))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val irfanTv = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(2), dp(10), dp(10))
                layoutParams = lp
                textSize = 15f; setTextColor(Color.BLACK)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            lmain.addView(topRow); lmain.addView(ayaArabic); lmain.addView(kanzul); lmain.addView(nameTv); lmain.addView(irfan); lmain.addView(irfanTv)
            root.addView(suraHeader); root.addView(lmain)
            val item = list[position]
            suraHeader.text = "${item.optString("suraName")} - আয়াত ${replaceArabicNumber(item.optString("verses"))}"
            num.text = replaceArabicNumber(item.optString("verses"))
            val rawArabic = item.optString("names")
            val rawName = item.optString("name")
            val rawIrfan = item.optString("author")
            if (lastQuery.isNotEmpty()) {
                ayaArabic.text = getHighlightedText(replaceArabicNumber(rawArabic), lastQuery)
                nameTv.text = getHighlightedText(replaceArabicNumber(rawName), lastQuery)
                irfanTv.text = getHighlightedText(replaceArabicNumber(rawIrfan), lastQuery)
            } else {
                ayaArabic.text = replaceArabicNumber(rawArabic)
                nameTv.text = replaceArabicNumber(rawName)
                irfanTv.text = replaceArabicNumber(rawIrfan)
            }
            playBtn.setOnClickListener {
                currentSuraAuthor = item.optString("suraAuthor"); currentSuraBangla = item.optString("suraName"); currentSuraNumber = item.optString("suraNumber").toIntOrNull() ?: getSuraNumberFromAuthor(currentSuraAuthor)
                loadAyaList("${currentSuraAuthor}.json"); switchMode(Mode.AYA_LIST)
                listView1.postDelayed({ val target = item.optString("verses"); for (i in filteredAya.indices) if (filteredAya[i].optString("verses") == target) { listView1.setSelection(i); break } }, 300)
            }
            copyBtn.setOnClickListener { val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; clipboard.setPrimaryClip(ClipData.newPlainText("ayah", "${item.optString("names")}\n${item.optString("name")}")); Toast.makeText(ctx, "কপি হয়েছে", Toast.LENGTH_SHORT).show() }
            shareBtn.setOnClickListener { val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, "${item.optString("names")}\n${item.optString("name")}\n${item.optString("suraName")}"); ctx.startActivity(Intent.createChooser(share, "শেয়ার")) }
            bookmarkBtn.setOnClickListener { toggleBookmark(item) }
            bookmarkBtn.text = if (isBookmarked(item.optString("_id"), item.optString("suraAuthor"))) "🔖" else "📑"
            return root
        }
    }

    inner class BookmarkAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        fun getItemAt(pos: Int): JSONObject? = if (pos < list.size) list[pos] else null
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context
            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT)
                setBackgroundColor(Color.TRANSPARENT); setPadding(dp(6), dp(6), dp(6), dp(6))
            }
            val header = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(12), dp(6), dp(10), dp(2))
                layoutParams = lp
                textSize = 13f; setTextColor(Color.parseColor("#01837A")); setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val lmain = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(6), dp(6), dp(6), dp(6))
                layoutParams = lp
                setPadding(dp(8), dp(8), dp(8), dp(8))
                elevation = dpF(4f)
                val d = GradientDrawable().apply { setStroke(dp(1), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = dpF(12f) }
                background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), d, null)
            }
            val topRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)) }
            val cancelBtn = TextView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; text = "❌"; textSize = 18f; gravity = Gravity.CENTER; isFocusable = false }
            val playBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {} }
            val copyBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; rotation = 180f; scaleX = -1f; try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {} }
            val shareBtn = ImageView(ctx).apply { val lp = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.MATCH_PARENT); lp.setMargins(dp(5), dp(5), dp(5), dp(5)); layoutParams = lp; setPadding(dp(5), dp(5), dp(5), dp(5)); scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.share_round) } catch (e: Exception) {} }
            topRow.addView(cancelBtn); topRow.addView(playBtn); topRow.addView(copyBtn); topRow.addView(shareBtn)
            val ayaArabic = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(10), dp(10), dp(10))
                layoutParams = lp
                textSize = 26f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL
                try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {
                    try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (ee: Exception) {}
                }
            }
            val kanzul = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(8), dp(10), dp(2))
                layoutParams = lp
                text = "কানযুল ঈমান"
                setBackgroundColor(Color.parseColor("#E0F2F1"))
                setTextColor(Color.parseColor("#00695C"))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val nameTv = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(2), dp(10), dp(8))
                layoutParams = lp
                textSize = 15f; setTextColor(Color.BLACK)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val irfan = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(4), dp(10), dp(2))
                layoutParams = lp
                text = "ইরফানুল কুরআন"
                setBackgroundColor(Color.parseColor("#E3F2FD"))
                setTextColor(Color.parseColor("#0D47A1"))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val irfanTv = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(2), dp(10), dp(10))
                layoutParams = lp
                textSize = 15f; setTextColor(Color.BLACK)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            lmain.addView(topRow); lmain.addView(ayaArabic); lmain.addView(kanzul); lmain.addView(nameTv); lmain.addView(irfan); lmain.addView(irfanTv)
            root.addView(header); root.addView(lmain)
            val item = list[position]
            header.text = "${item.optString("suraName")} - আয়াত ${replaceArabicNumber(item.optString("ayahNumber"))}"
            ayaArabic.text = replaceArabicNumber(item.optString("names"))
            nameTv.text = replaceArabicNumber(item.optString("name"))
            irfanTv.text = replaceArabicNumber(item.optString("author", item.optString("irfan", "")))
            cancelBtn.setOnClickListener {
                val prefs = ctx.getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
                val jsonStr = prefs.getString("bookmarks_json","[]")
                val arr = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
                val newArr = JSONArray(); for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); if (!(o.optString("_id")==item.optString("_id") && o.optString("suraAuthor")==item.optString("suraAuthor"))) newArr.put(o) }
                prefs.edit().putString("bookmarks_json", newArr.toString()).apply()
                list.removeAt(position); notifyDataSetChanged()
                Toast.makeText(ctx, "বুকমার্ক থেকে বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                if (list.isEmpty()) { nores.visibility = View.VISIBLE; noresTv.text = "কোন বুকমার্ক নেই\n⭐ আইকনে ক্লিক করে বুকমার্ক যোগ করুন" }
            }
            playBtn.setOnClickListener {
                currentSuraAuthor = item.optString("suraAuthor"); currentSuraBangla = item.optString("suraName"); currentSuraNumber = item.optString("suraNumber").toIntOrNull() ?: getSuraNumberFromAuthor(currentSuraAuthor)
                loadAyaList("${currentSuraAuthor}.json"); switchMode(Mode.AYA_LIST)
                listView1.postDelayed({ val target = item.optString("ayahNumber"); for (i in filteredAya.indices) if (filteredAya[i].optString("verses") == target) { listView1.setSelection(i); break } }, 300)
            }
            copyBtn.setOnClickListener { val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; clipboard.setPrimaryClip(ClipData.newPlainText("ayah", "${item.optString("names")}\n${item.optString("name")}")); Toast.makeText(ctx, "কপি হয়েছে", Toast.LENGTH_SHORT).show() }
            shareBtn.setOnClickListener { val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, "${item.optString("names")}\n${item.optString("name")}\n${item.optString("suraName")}"); ctx.startActivity(Intent.createChooser(share, "শেয়ার")) }
            return root
        }
    }

    private fun getBanglaType(type: String): String {
        return when (type.trim()) {
            "مکی", "مكی", "مكّي", "مكی" -> "মাক্কী"
            "مدنی", "مدنى", "مدني" -> "মাদানী"
            else -> {
                if (type.contains("مکی") || type.contains("مكی")) "মাক্কী"
                else if (type.contains("مدنی") || type.contains("مدنى")) "মাদানী"
                else type
            }
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
