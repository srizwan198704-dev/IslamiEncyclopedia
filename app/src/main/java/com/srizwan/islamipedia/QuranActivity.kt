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
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

// ============== SINGLE FILE - ALL CLASSES ==============

class QuranActivity : AppCompatActivity() {
    private lateinit var jump: ImageView
    private lateinit var back: ImageView
    private lateinit var listView1: ListView
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var bookid: Array<String>
    private lateinit var ayanumber: Array<String>
    private lateinit var ayaarabic: Array<String>
    private lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject>
    private lateinit var searchtop: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var cancel: ImageView
    private lateinit var searchbox: EditText
    private lateinit var bookmarkViewBtn: TextView
    private lateinit var fabGlobalSearch: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createMainLayoutFirstScreen())

        val heading1: TextView = findViewById(R.id.heading1)
        heading1.text = intent.getStringExtra("sub") ?: "আল কুরআন"

        jump = findViewById(R.id.jump)
        jump.visibility = View.GONE
        back = findViewById(R.id.back)
        listView1 = findViewById(R.id.listview1)
        back.setOnClickListener { finish() }

        listView1.setOnItemClickListener { _, _, position, _ ->
            val selectedBook = filteredItems[position]
            val bookName = selectedBook.getString("name")
            val bookAuthor = selectedBook.getString("author")
            val intent = Intent(applicationContext, QuranviewActivity::class.java)
            intent.putExtra("name", bookName)
            intent.putExtra("booklist", "$bookAuthor.json")
            intent.putExtra("bookid", selectedBook.getString("bookid"))
            startActivity(intent)
        }

        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "সুরা সার্চ করুন"
        searchbox.setHintTextColor(ColorStateList.valueOf(hintColor))
        cancel = findViewById(R.id.cancelme)
        cancel.setOnClickListener {
            if (searchbox.text.toString() == "") {
                searchView.visibility = View.GONE
            } else {
                searchbox.text.clear()
            }
        }

        searchtop = findViewById(R.id.searchme)
        searchView = findViewById(R.id.searchView)
        searchtop.setOnClickListener {
            searchView.visibility = if (searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        bookmarkViewBtn = findViewById(R.id.bookmarkViewBtn)
        bookmarkViewBtn.setOnClickListener {
            startActivity(Intent(this, BookmarkActivity::class.java))
        }

        fabGlobalSearch = findViewById(R.id.fabGlobalSearch)
        fabGlobalSearch.setOnClickListener {
            startActivity(Intent(this, GlobalSearchActivity::class.java))
        }

        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
        })

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val jsonArray = getJSonData(intent.getStringExtra("booklist") ?: "sura.json")
        listItems = getArrayListFromJSONArray(jsonArray)
        filteredItems = ArrayList(listItems)
        val adapter = QuranAdapter(this, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchView.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) searchView.visibility = View.GONE else searchbox.text.clear()
                } else finish()
            }
        })
    }

    private fun createMainLayoutFirstScreen(): View {
        val d = resources.displayMetrics.density
        val root = ConstraintLayout(this).apply {
            id = R.id.main
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            try { background = ContextCompat.getDrawable(context, R.drawable.back1ground) } catch (e: Exception) { setBackgroundColor(Color.WHITE) }
            fitsSystemWindows = true
        }

        // Top bar linear1
        val topBar = LinearLayout(this).apply {
            id = R.id.linear1
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            elevation = 5f * d
            layoutParams = ConstraintLayout.LayoutParams(0, (65 * d).toInt()).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }

        val backIv = ImageView(this).apply {
            id = R.id.back
            layoutParams = LinearLayout.LayoutParams((56 * d).toInt(), (56 * d).toInt())
            setPadding((15 * d).toInt(), (15 * d).toInt(), (15 * d).toInt(), (15 * d).toInt())
            scaleType = ImageView.ScaleType.CENTER_CROP
            try { setImageResource(R.drawable.ic_arrow_back_white) } catch (e: Exception) {}
        }
        val heading = TextView(this).apply {
            id = R.id.heading1
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = (5 * d).toInt() }
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = try { ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) { null }
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isFocusable = true
            isFocusableInTouchMode = true
            setHorizontallyScrolling(true)
            gravity = Gravity.CENTER_VERTICAL
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            text = getString(R.string.app_name)
        }
        val bookmarkView = TextView(this).apply {
            id = R.id.bookmarkViewBtn
            layoutParams = LinearLayout.LayoutParams((40 * d).toInt(), (40 * d).toInt()).apply { rightMargin = (5 * d).toInt() }
            text = "⭐"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        val jumpIv = ImageView(this).apply {
            id = R.id.jump
            layoutParams = LinearLayout.LayoutParams((30 * d).toInt(), (30 * d).toInt()).apply { rightMargin = (10 * d).toInt() }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.ic_jump_page) } catch (e: Exception) {}
            visibility = View.GONE
        }
        val searchIv = ImageView(this).apply {
            id = R.id.searchme
            layoutParams = LinearLayout.LayoutParams((30 * d).toInt(), (30 * d).toInt()).apply { rightMargin = (5 * d).toInt() }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}
        }
        topBar.addView(backIv)
        topBar.addView(heading)
        topBar.addView(bookmarkView)
        topBar.addView(jumpIv)
        topBar.addView(searchIv)

        // SearchView
        val searchViewLL = LinearLayout(this).apply {
            id = R.id.searchView
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topToBottom = R.id.linear1
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = (10 * d).toInt()
            }
        }
        val til = TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox).apply {
            id = R.id.boxofsearch
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
        }
        val et = EditText(this).apply {
            id = R.id.searchbox
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            setTextColor(Color.BLACK)
            textSize = 14f
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        til.addView(et)
        val cancelIv = ImageView(this).apply {
            id = R.id.cancelme
            layoutParams = LinearLayout.LayoutParams((30*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { rightMargin = (5*d).toInt() }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.cancel) } catch (e: Exception) {}
        }
        searchViewLL.addView(til)
        searchViewLL.addView(cancelIv)

        // select gone
        val selectLL = LinearLayout(this).apply {
            id = R.id.select
            visibility = View.GONE
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topToBottom = R.id.searchView
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = (10*d).toInt()
            }
        }

        // nores
        val noresLL = LinearLayout(this).apply {
            id = R.id.nores
            visibility = View.GONE
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topToBottom = R.id.select
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
        val noresImg = ImageView(this).apply {
            id = R.id.noresult
            layoutParams = LinearLayout.LayoutParams((100*d).toInt(), (100*d).toInt()).apply { gravity = Gravity.CENTER }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.noresult) } catch (e: Exception) {}
        }
        val noresTv = TextView(this).apply {
            id = R.id.no_result
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
            text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
        }
        noresLL.addView(noresImg)
        noresLL.addView(noresTv)

        // ListView
        val lv = ListView(this).apply {
            id = R.id.listview1
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                topToBottom = R.id.nores
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = (10*d).toInt()
            }
            divider = null
            dividerHeight = 0
            setBackgroundColor(Color.WHITE)
            selector = android.graphics.drawable.ColorDrawable(Color.WHITE)
        }

        // audiotab gone for first screen but keep id
        val audioTab = LinearLayout(this).apply {
            id = R.id.audiotab
            visibility = View.GONE
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#01837A"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }

        // FAB
        val fab = FloatingActionButton(this).apply {
            id = R.id.fabGlobalSearch
            layoutParams = ConstraintLayout.LayoutParams((56*d).toInt(), (56*d).toInt()).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                bottomMargin = (16*d).toInt()
                rightMargin = (16*d).toInt()
                setMargins(0,0,(16*d).toInt(),(16*d).toInt())
            }
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
        }

        root.addView(topBar)
        root.addView(searchViewLL)
        root.addView(selectLL)
        root.addView(noresLL)
        root.addView(lv)
        root.addView(audioTab)
        root.addView(fab)
        return root
    }

    private fun getJSonData(fileName: String): JSONArray? {
        var jsonArray: JSONArray? = null
        try {
            val inputStream: InputStream = resources.assets.open(fileName)
            val size: Int = inputStream.available()
            val data = ByteArray(size)
            inputStream.read(data)
            inputStream.close()
            val json = String(data, Charsets.UTF_8)
            jsonArray = JSONArray(json)
        } catch (e: IOException) { e.printStackTrace() } catch (e: JSONException) { e.printStackTrace() }
        return jsonArray
    }

    private fun filterList(query: String) {
        filteredItems.clear()
        for (i in name.indices) {
            if (name[i].contains(query, ignoreCase = true)) {
                filteredItems.add(JSONObject().apply {
                    put("name", name[i])
                    put("author", author[i])
                    put("bookid", bookid[i])
                    put("verses", ayanumber[i])
                    put("names", ayaarabic[i])
                })
                findViewById<LinearLayout>(R.id.nores).visibility = View.GONE
            }
        }
        if (filteredItems.isEmpty()) {
            findViewById<LinearLayout>(R.id.nores).visibility = View.VISIBLE
        }
        val adapter = QuranAdapter(this, filteredItems)
        listView1.adapter = adapter
    }

    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        try {
            if (jsonArray != null) {
                name = Array(jsonArray.length()) { "" }
                author = Array(jsonArray.length()) { "" }
                bookid = Array(jsonArray.length()) { "" }
                ayanumber = Array(jsonArray.length()) { "" }
                ayaarabic = Array(jsonArray.length()) { "" }
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    aList.add(jsonObject)
                    name[i] = jsonObject.getString("name")
                    author[i] = jsonObject.getString("author")
                    bookid[i] = jsonObject.getString("bookid")
                    ayanumber[i] = jsonObject.getString("verses")
                    ayaarabic[i] = jsonObject.getString("names")
                }
            }
        } catch (je: JSONException) { je.printStackTrace() }
        return aList
    }
}

// ============== SECOND SCREEN ==============

class QuranviewActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var jump: ImageView
    lateinit var listView1: ListView
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var bookid: Array<String>
    private lateinit var ayanumber: Array<String>
    private lateinit var ayaarabic: Array<String>
    lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject>
    private lateinit var searchtop: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var cancel: ImageView
    private lateinit var searchbox: EditText
    private var mediaPlayer: MediaPlayer? = null
    var currentIndex = 0
    private var currentPlayingId: String? = null
    var currentSuraNumber: Int = 1
    var currentSuraAuthor: String = "Al-Fatihah"
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

        setContentView(createMainLayoutSecondScreen())

        val heading1: TextView = findViewById(R.id.heading1)
        heading1.text = intent.getStringExtra("name") ?: "সুরা"
        jump = findViewById(R.id.jump)
        back = findViewById(R.id.back)
        listView1 = findViewById(R.id.listview1)

        back.setOnClickListener { finish() }
        jump.setOnClickListener { showPageJumpDialog() }
        jump.visibility = View.VISIBLE

        val playAudio: ImageView = findViewById(R.id.playAudio)
        val audiotab: LinearLayout = findViewById(R.id.audiotab)
        audiotab.visibility = View.VISIBLE

        val previous: LinearLayout = findViewById(R.id.previous)
        val next: LinearLayout = findViewById(R.id.next)
        val stop: LinearLayout = findViewById(R.id.stop)
        val playAudio1: LinearLayout = findViewById(R.id.playAudio1)
        val qariSelector: TextView = findViewById(R.id.qariSelector)

        playAudio1.setOnClickListener { playAudio.performClick() }
        previous.setOnClickListener {
            if (currentIndex > 0) currentIndex--
            startPlayingFromIndex(currentIndex)
        }
        next.setOnClickListener {
            if (currentIndex < filteredItems.size - 1) currentIndex++
            startPlayingFromIndex(currentIndex)
        }
        stop.setOnClickListener {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null
                currentPlayingId = null; currentIndex = 0
                findViewById<ImageView>(R.id.playAudio).setImageResource(R.drawable.play)
                notifyListView()
                Toast.makeText(this, "অডিও প্লে বন্ধ হয়েছে।", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "এখন কোনো সূরা অডিও চলছে না", Toast.LENGTH_SHORT).show()
        }

        playAudio.setOnClickListener {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause(); notifyListView()
                    playAudio.setImageResource(R.drawable.play)
                } else {
                    if (currentIndex >= filteredItems.size) {
                        currentIndex = 0; startPlayingFromIndex(currentIndex); notifyListView()
                        playAudio.setImageResource(R.drawable.pause)
                    } else {
                        mediaPlayer?.start(); notifyListView()
                        playAudio.setImageResource(R.drawable.pause)
                    }
                }
            } else {
                if (filteredItems.isNotEmpty()) {
                    currentIndex = 0; notifyListView(); startPlayingFromIndex(currentIndex)
                    playAudio.setImageResource(R.drawable.pause)
                } else Toast.makeText(this, "প্লে করার মতো আয়াত নেই।", Toast.LENGTH_SHORT).show()
            }
        }

        qariSelector.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            qariMap.keys.forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item ->
                val banglaName = item.title.toString()
                val code = qariMap[banglaName] ?: "Alafasy_64kbps"
                selectedQariName = banglaName; selectedQariCode = code
                prefs.edit().putString("selected_qari_name", banglaName).putString("selected_qari_code", code).apply()
                Toast.makeText(this, "ক্বারী: $banglaName", Toast.LENGTH_SHORT).show()
                if (mediaPlayer != null) {
                    mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null
                    startPlayingFromIndex(currentIndex)
                }
                true
            }
            popup.show()
        }

        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "আয়াত সার্চ করুন"
        searchbox.setHintTextColor(ColorStateList.valueOf(hintColor))
        cancel = findViewById(R.id.cancelme)
        cancel.setOnClickListener {
            if (searchbox.text.toString() == "") searchView.visibility = View.GONE else searchbox.text.clear()
        }
        searchtop = findViewById(R.id.searchme)
        searchView = findViewById(R.id.searchView)
        searchtop.setOnClickListener {
            searchView.visibility = if (searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterList(s.toString()) }
        })

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val w: Window = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            try {
                w.statusBarColor = Color.parseColor(getString(R.string.color))
                w.navigationBarColor = Color.parseColor(getString(R.string.color))
            } catch (e: Exception) {}
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val booklistFile = intent.getStringExtra("booklist") ?: "Al-Fatihah.json"
        currentSuraAuthor = booklistFile.replace(".json", "")
        // figure sura number from sura.json
        currentSuraNumber = getSuraNumberFromAuthor(currentSuraAuthor)

        val jsonArray = getJSonData(booklistFile)
        listItems = getArrayListFromJSONArray(jsonArray)
        filteredItems = ArrayList(listItems)
        val adapter = QuranviewAdapter(this, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true

        // scroll to if from bookmark/global search
        val scrollTo = intent.getStringExtra("scrollToAyah")
        if (scrollTo != null) {
            for (i in filteredItems.indices) {
                if (filteredItems[i].optString("verses") == scrollTo) {
                    listView1.setSelection(i); break
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchView.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) searchView.visibility = View.GONE else searchbox.text.clear()
                } else finish()
            }
        })
    }

    private fun createMainLayoutSecondScreen(): View {
        val d = resources.displayMetrics.density
        val root = ConstraintLayout(this).apply {
            id = R.id.main
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            try { background = ContextCompat.getDrawable(context, R.drawable.back1ground) } catch (e: Exception) { setBackgroundColor(Color.WHITE) }
            fitsSystemWindows = true
        }
        val topBar = LinearLayout(this).apply {
            id = R.id.linear1
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            elevation = 5f * d
            layoutParams = ConstraintLayout.LayoutParams(0, (65 * d).toInt()).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
        val backIv = ImageView(this).apply {
            id = R.id.back
            layoutParams = LinearLayout.LayoutParams((56 * d).toInt(), (56 * d).toInt())
            setPadding((15 * d).toInt(), (15 * d).toInt(), (15 * d).toInt(), (15 * d).toInt())
            scaleType = ImageView.ScaleType.CENTER_CROP
            try { setImageResource(R.drawable.ic_arrow_back_white) } catch (e: Exception) {}
        }
        val heading = TextView(this).apply {
            id = R.id.heading1
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = (5 * d).toInt() }
            setTextColor(Color.WHITE); textSize = 18f
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            isSingleLine = true; ellipsize = android.text.TextUtils.TruncateAt.MARQUEE; marqueeRepeatLimit = -1
            isFocusable = true; isFocusableInTouchMode = true; setHorizontallyScrolling(true); gravity = Gravity.CENTER_VERTICAL
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val jumpIv = ImageView(this).apply {
            id = R.id.jump
            layoutParams = LinearLayout.LayoutParams((30 * d).toInt(), (30 * d).toInt()).apply { rightMargin = (10 * d).toInt() }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.ic_jump_page) } catch (e: Exception) {}
        }
        val searchIv = ImageView(this).apply {
            id = R.id.searchme
            layoutParams = LinearLayout.LayoutParams((30 * d).toInt(), (30 * d).toInt()).apply { rightMargin = (5 * d).toInt() }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}
        }
        topBar.addView(backIv); topBar.addView(heading); topBar.addView(jumpIv); topBar.addView(searchIv)

        val searchViewLL = LinearLayout(this).apply {
            id = R.id.searchView
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topToBottom = R.id.linear1; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; topMargin = (10 * d).toInt()
            }
        }
        val til = TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox).apply {
            id = R.id.boxofsearch
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
        }
        val et = EditText(this).apply {
            id = R.id.searchbox
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            setTextColor(Color.BLACK); textSize = 14f
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        til.addView(et)
        val cancelIv = ImageView(this).apply {
            id = R.id.cancelme
            layoutParams = LinearLayout.LayoutParams((30*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { rightMargin = (5*d).toInt() }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.cancel) } catch (e: Exception) {}
        }
        searchViewLL.addView(til); searchViewLL.addView(cancelIv)

        val selectLL = LinearLayout(this).apply {
            id = R.id.select; visibility = View.GONE; orientation = LinearLayout.HORIZONTAL
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topToBottom = R.id.searchView; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; topMargin = (10*d).toInt()
            }
        }
        val noresLL = LinearLayout(this).apply {
            id = R.id.nores; visibility = View.GONE; orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topToBottom = R.id.select; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
        val noresImg = ImageView(this).apply {
            id = R.id.noresult
            layoutParams = LinearLayout.LayoutParams((100*d).toInt(), (100*d).toInt()).apply { gravity = Gravity.CENTER }
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.noresult) } catch (e: Exception) {}
        }
        val noresTv = TextView(this).apply {
            id = R.id.no_result
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
            text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"; textSize = 16f; setTextColor(Color.BLACK); gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
        }
        noresLL.addView(noresImg); noresLL.addView(noresTv)

        val lv = ListView(this).apply {
            id = R.id.listview1
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                topToBottom = R.id.nores; bottomToTop = R.id.audiotab; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; topMargin = (10*d).toInt()
            }
            divider = null; dividerHeight = 0; setBackgroundColor(Color.WHITE)
            selector = android.graphics.drawable.ColorDrawable(Color.WHITE)
        }

        val audioTab = LinearLayout(this).apply {
            id = R.id.audiotab
            visibility = View.GONE
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#01837A"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
        val prevLL = LinearLayout(this).apply {
            id = R.id.previous
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { leftMargin = (10*d).toInt(); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
            orientation = LinearLayout.HORIZONTAL
        }
        val prevImg = ImageView(this).apply {
            id = R.id.previous_image
            layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.previous) } catch (e: Exception) {}
        }
        prevLL.addView(prevImg)
        val between0 = LinearLayout(this).apply {
            id = R.id.between0
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        }
        val playLL = LinearLayout(this).apply {
            id = R.id.playAudio1
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        }
        val playImg = ImageView(this).apply {
            id = R.id.playAudio
            layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.play) } catch (e: Exception) {}
        }
        playLL.addView(playImg)
        val between1 = LinearLayout(this).apply {
            id = R.id.between1
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        }
        val nextLL = LinearLayout(this).apply {
            id = R.id.next
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        }
        val nextImg = ImageView(this).apply {
            id = R.id.next_image
            layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            rotation = 180f
            try { setImageResource(R.drawable.previous) } catch (e: Exception) {}
        }
        nextLL.addView(nextImg)
        val between2 = LinearLayout(this).apply {
            id = R.id.between2
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        }
        val qariTv = TextView(this).apply {
            id = R.id.qariSelector
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), (40*d).toInt())
            text = "🎧"; textSize = 22f; gravity = Gravity.CENTER; setTextColor(Color.WHITE)
        }
        val stopLL = LinearLayout(this).apply {
            id = R.id.stop
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { rightMargin = (10*d).toInt(); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()) }
        }
        val stopImg = ImageView(this).apply {
            id = R.id.stop_image
            layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.stop) } catch (e: Exception) {}
        }
        stopLL.addView(stopImg)
        audioTab.addView(prevLL); audioTab.addView(between0); audioTab.addView(playLL); audioTab.addView(between1); audioTab.addView(nextLL); audioTab.addView(between2); audioTab.addView(qariTv); audioTab.addView(stopLL)

        root.addView(topBar); root.addView(searchViewLL); root.addView(selectLL); root.addView(noresLL); root.addView(lv); root.addView(audioTab)
        return root
    }

    private fun getSuraNumberFromAuthor(author: String): Int {
        try {
            val input = resources.assets.open("sura.json")
            val json = String(input.readBytes(), Charsets.UTF_8)
            input.close()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("author") == author) {
                    return obj.getString("bookid").toIntOrNull() ?: 1
                }
            }
        } catch (e: Exception) {}
        return 1
    }

    private fun getJSonData(fileName: String): JSONArray? {
        var jsonArray: JSONArray? = null
        try {
            val inputStream: InputStream = resources.assets.open(fileName)
            val size: Int = inputStream.available()
            val data = ByteArray(size)
            inputStream.read(data)
            inputStream.close()
            val json = String(data, Charsets.UTF_8)
            jsonArray = JSONArray(json)
        } catch (e: IOException) { e.printStackTrace() } catch (e: JSONException) { e.printStackTrace() }
        return jsonArray
    }

    private fun filterList(query: String) {
        filteredItems.clear()
        for (i in name.indices) {
            if (name[i].contains(query, ignoreCase = true) || ayaarabic[i].contains(query, ignoreCase = true)) {
                filteredItems.add(JSONObject().apply {
                    put("name", name[i]); put("author", author[i]); put("bookid", bookid[i]); put("verses", ayanumber[i]); put("names", ayaarabic[i])
                    try { put("_id", listItems[i].getString("_id")) } catch (e: Exception) {}
                })
                findViewById<LinearLayout>(R.id.nores).visibility = View.GONE
            }
        }
        if (filteredItems.isEmpty()) findViewById<LinearLayout>(R.id.nores).visibility = View.VISIBLE
        val adapter = QuranviewAdapter(this, filteredItems)
        listView1.adapter = adapter
    }

    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        try {
            if (jsonArray != null) {
                name = Array(jsonArray.length()) { "" }
                author = Array(jsonArray.length()) { "" }
                bookid = Array(jsonArray.length()) { "" }
                ayanumber = Array(jsonArray.length()) { "" }
                ayaarabic = Array(jsonArray.length()) { "" }
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    aList.add(jsonObject)
                    name[i] = jsonObject.optString("name", "")
                    author[i] = jsonObject.optString("author", "")
                    bookid[i] = jsonObject.optString("bookid", "")
                    ayanumber[i] = jsonObject.optString("verses", "")
                    ayaarabic[i] = jsonObject.optString("names", "")
                }
            }
        } catch (je: JSONException) { je.printStackTrace() }
        return aList
    }

    // EveryAyah logic
    fun getFormattedSSSAAA(suraNumber: Int, ayahNumber: Int): String {
        return String.format("%03d%03d", suraNumber, ayahNumber)
    }

    fun getEveryAyahUrl(qariCode: String, sssaaa: String): String {
        return "https://everyayah.com/data/$qariCode/$sssaaa.mp3"
    }

    fun startPlayingFromIndex(index: Int) {
        if (index >= filteredItems.size) { stopCurrentPlaying(); return }
        currentIndex = index
        val currentItem = filteredItems[currentIndex]
        val ayahNum = currentItem.optString("verses", "1").toIntOrNull() ?: (index+1)
        val sssaaa = getFormattedSSSAAA(currentSuraNumber, ayahNum)
        val audioUrl = getEveryAyahUrl(selectedQariCode, sssaaa)
        val fileName = "${selectedQariCode}_${sssaaa}.mp3"
        val file = File(getExternalFilesDir(null), fileName)
        val audioId = currentItem.optString("_id", sssaaa)
        currentPlayingId = audioId
        scrollToAya(audioId)
        if (file.exists()) {
            playAudioFile(file)
            downloadNextAudios(currentIndex + 1)
        } else {
            downloadAndPlayFirstAudio(audioUrl, file)
        }
        findViewById<ImageView>(R.id.playAudio).setImageResource(R.drawable.pause)
        notifyListView()
    }

    private fun downloadAndPlayFirstAudio(url: String, file: File) {
        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                // fallback 128kbps if 404
                if (connection.responseCode == 404) {
                    val fallbackUrl = url.replace("_64kbps", "_128kbps").replace("_40kbps", "_64kbps")
                    val conn2 = URL(fallbackUrl).openConnection() as HttpURLConnection
                    conn2.connect()
                    if (conn2.responseCode == 200) {
                        downloadStream(conn2, file)
                        runOnUiThread { playAudioFile(file); downloadNextAudios(currentIndex + 1) }
                        return@Thread
                    }
                }
                downloadStream(connection, file)
                runOnUiThread { playAudioFile(file); downloadNextAudios(currentIndex + 1) }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this, "ডাউনলোডে সমস্যা: $url", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun downloadStream(connection: HttpURLConnection, file: File) {
        val input = BufferedInputStream(connection.inputStream)
        val output = FileOutputStream(file)
        val data = ByteArray(1024)
        var count: Int
        while (input.read(data).also { count = it } != -1) output.write(data, 0, count)
        output.flush(); output.close(); input.close()
    }

    private fun downloadNextAudios(startIndex: Int) {
        Thread {
            for (i in startIndex until filteredItems.size) {
                val currentItem = filteredItems[i]
                val ayahNum = currentItem.optString("verses", "${i+1}").toIntOrNull() ?: (i+1)
                val sssaaa = getFormattedSSSAAA(currentSuraNumber, ayahNum)
                val audioUrl = getEveryAyahUrl(selectedQariCode, sssaaa)
                val fileName = "${selectedQariCode}_${sssaaa}.mp3"
                val file = File(getExternalFilesDir(null), fileName)
                if (!file.exists()) {
                    try {
                        val connection = URL(audioUrl).openConnection() as HttpURLConnection
                        connection.connect()
                        if (connection.responseCode == 200) downloadStream(connection, file)
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }.start()
    }

    private fun playAudioFile(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.setDataSource(file.absolutePath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            notifyListView()
            findViewById<ImageView>(R.id.playAudio).setImageResource(R.drawable.pause)
            mediaPlayer?.setOnCompletionListener { startPlayingFromIndex(currentIndex + 1) }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "অডিও প্লে করতে সমস্যা হয়েছে।", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentPlayingId(): String? = currentPlayingId
    fun isAudioPlaying(): Boolean = mediaPlayer?.isPlaying == true
    fun notifyListView() { (listView1.adapter as? QuranviewAdapter)?.notifyDataSetChanged() }
    private fun stopCurrentPlaying() {
        mediaPlayer?.release(); mediaPlayer = null; currentPlayingId = null; currentIndex = 0
        try { findViewById<ImageView>(R.id.playAudio).setImageResource(R.drawable.play) } catch (e: Exception) {}
        notifyListView()
    }
    private fun scrollToAya(id: String) {
        for (i in filteredItems.indices) {
            val itemId = filteredItems[i].optString("_id")
            if (itemId == id) {
                listView1.setSelection(i)
                listView1.postDelayed({ listView1.smoothScrollToPositionFromTop(i, 0) }, 50)
                break
            }
        }
    }

    fun playme(item: JSONObject) {
        val id = item.optString("_id")
        for (i in filteredItems.indices) if (filteredItems[i].optString("_id") == id) { currentIndex = i; break }
        val existingFileCheckAyah = item.optString("verses", "${currentIndex+1}").toIntOrNull() ?: (currentIndex+1)
        val sssaaa = getFormattedSSSAAA(currentSuraNumber, existingFileCheckAyah)
        val fileName = "${selectedQariCode}_${sssaaa}.mp3"
        val file = File(getExternalFilesDir(null), fileName)
        if (mediaPlayer != null && currentPlayingId == id) {
            if (mediaPlayer!!.isPlaying) { mediaPlayer?.pause() } else { mediaPlayer?.start() }
            notifyListView(); return
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
        val text = "${item.optString("names")}\n\n${item.optString("name")}\n${item.optString("author")}\n\n${intent.getStringExtra("name")}"
        val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(share, "শেয়ার করুন"))
    }

    // Bookmark
    fun toggleBookmark(item: JSONObject) {
        val prefsBm = getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val jsonStr = prefsBm.getString("bookmarks_json", "[]")
        val arr = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
        val _id = item.optString("_id")
        val suraAuth = currentSuraAuthor
        var foundIndex = -1
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("_id") == _id && o.optString("suraAuthor") == suraAuth) { foundIndex = i; break }
        }
        if (foundIndex >=0) {
            // remove
            val newArr = JSONArray()
            for (i in 0 until arr.length()) if (i!=foundIndex) newArr.put(arr.getJSONObject(i))
            prefsBm.edit().putString("bookmarks_json", newArr.toString()).apply()
            Toast.makeText(this, "বুকমার্ক মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
        } else {
            val bm = JSONObject()
            bm.put("suraNumber", currentSuraNumber)
            bm.put("suraName", intent.getStringExtra("name"))
            bm.put("suraAuthor", suraAuth)
            bm.put("ayahNumber", item.optString("verses"))
            bm.put("_id", _id)
            bm.put("name", item.optString("name"))
            bm.put("names", item.optString("names"))
            bm.put("author", item.optString("author"))
            bm.put("timestamp", System.currentTimeMillis())
            arr.put(bm)
            prefsBm.edit().putString("bookmarks_json", arr.toString()).apply()
            Toast.makeText(this, "বুকমার্ক যোগ হয়েছে", Toast.LENGTH_SHORT).show()
        }
        notifyListView()
    }
    fun isBookmarked(_id: String, suraAuthor: String): Boolean {
        val prefsBm = getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val jsonStr = prefsBm.getString("bookmarks_json", "[]")
        val arr = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("_id") == _id && o.optString("suraAuthor") == suraAuthor) return true
        }
        return false
    }

    private fun showPageJumpDialog() {
        val input = EditText(this).apply { hint = "আয়াত নম্বর লিখুন"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        AlertDialog.Builder(this).setTitle("আয়াতে যান").setView(input)
            .setPositiveButton("যান") { _, _ ->
                val num = input.text.toString().toIntOrNull()
                if (num != null) {
                    for (i in filteredItems.indices) if (filteredItems[i].optString("verses").toIntOrNull() == num) { listView1.setSelection(i); break }
                }
            }.setNegativeButton("বাতিল", null).show()
    }

    override fun onDestroy() { super.onDestroy(); mediaPlayer?.release(); mediaPlayer = null }
}

// ============== ADAPTERS ==============

class QuranAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val ctx = context
        val d = ctx.resources.displayMetrics.density
        val itemView = LinearLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            id = R.id.main
        }
        val lmain = LinearLayout(ctx).apply {
            id = R.id.linear1
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (60*d).toInt()).apply {
                setMargins((3*d).toInt(), (3*d).toInt(), (3*d).toInt(), (3*d).toInt())
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            elevation = 6f*d
        }
        val sketchUi = GradientDrawable().apply {
            val di = ctx.resources.displayMetrics.density.toInt()
            setStroke(di, Color.parseColor("#01837A"))
            setColor(Color.WHITE)
            cornerRadius = di * 12f
        }
        val ripple = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), sketchUi, null)
        lmain.background = ripple

        val linear5 = LinearLayout(ctx).apply {
            id = R.id.linear5
            layoutParams = LinearLayout.LayoutParams((46*d).toInt(), (46*d).toInt()).apply { leftMargin = (10*d).toInt() }
            gravity = Gravity.CENTER
            try { background = ContextCompat.getDrawable(ctx, R.drawable.quran) } catch (e: Exception) { setBackgroundColor(Color.LTGRAY) }
        }
        val number = TextView(ctx).apply {
            id = R.id.number
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textSize = 10f; setTextColor(Color.parseColor("#5A0202")); setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            setPadding(0,0,0,(10*d).toInt())
        }
        linear5.addView(number)
        val surabox = LinearLayout(ctx).apply {
            id = R.id.surabox
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { leftMargin = (4*d).toInt() }
        }
        val nameTv = TextView(ctx).apply {
            id = R.id.name
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textSize = 16f; setTextColor(Color.BLACK)
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        val ayaNumTv = TextView(ctx).apply {
            id = R.id.ayanumber
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textSize = 14f; setTextColor(Color.BLACK)
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        surabox.addView(nameTv); surabox.addView(ayaNumTv)
        val spacer = LinearLayout(ctx).apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        val arabicTv = TextView(ctx).apply {
            id = R.id.ayaarabic
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { rightMargin = (5*d).toInt() }
            textSize = 14f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        lmain.addView(linear5); lmain.addView(surabox); lmain.addView(spacer); lmain.addView(arabicTv)
        itemView.addView(lmain)
        val line = LinearLayout(ctx).apply {
            id = R.id.linear6
            layoutParams = LinearLayout.LayoutParams((100*d).toInt(), (1*d).toInt()).apply { setMargins((3*d).toInt(), (3*d).toInt(), (3*d).toInt(), (3*d).toInt()); gravity = Gravity.CENTER }
            setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
        }
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
    private fun replaceArabicNumber(n: String): String {
        return n.replace("1","১").replace("2","২").replace("3","৩").replace("4","৪").replace("5","৫").replace("6","৬").replace("7","৭").replace("8","৮").replace("9","৯").replace("0","০")
            .replace("<b>"," ").replace("</b>"," ").replace("(রহঃ)","(رحمة الله)").replace("(রাঃ)","(رضي الله عنه)")
            .replace("(সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)","(ﷺ)").replace(" (সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)","(ﷺ)")
            .replace("('আঃ)","(عليه السلام)").replace("[১]","").replace("[২]","").replace("[৩]","").replace("(রহ)","(رحمة الله)")
            .replace("(রা)","(رضي الله عنه)").replace("(সা)","(ﷺ)").replace("('আ)","(عليه السلام)").replace("(সাঃ)","(ﷺ)").replace("(স)","(ﷺ)")
            .replace("বিবিন্‌ত","বিন্‌ত").replace("বিন্ত","বিন্‌ত").replace("(সা.)","(ﷺ)").replace("(স.)","(ﷺ)")
    }
}

class QuranviewAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val ctx = context
        val d = ctx.resources.displayMetrics.density
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.WHITE)
            id = R.id.main
        }
        val lmain = LinearLayout(ctx).apply {
            id = R.id.linear1
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt())
            }
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            elevation = 4f*d
            setBackgroundColor(Color.WHITE)
        }

        // top row
        val linear4 = LinearLayout(ctx).apply {
            id = R.id.linear4
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (50*d).toInt())
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt())
        }
        val linear11 = LinearLayout(ctx).apply {
            id = R.id.linear11
            layoutParams = LinearLayout.LayoutParams((50*d).toInt(), (50*d).toInt())
            gravity = Gravity.CENTER
            try { background = ContextCompat.getDrawable(ctx, R.drawable.ic_1_4) } catch (e: Exception) {}
        }
        val number = TextView(ctx).apply {
            id = R.id.number
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textSize = 11f; setTextColor(Color.parseColor("#607D8B")); setTypeface(null, android.graphics.Typeface.BOLD)
        }
        linear11.addView(number)
        val spacer0 = LinearLayout(ctx).apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f) }

        // DRAWABLE LOCK: play, share, copy as ImageView with drawable
        val playBtn = ImageView(ctx).apply {
            id = R.id.playme
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            isFocusable = false
            try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {}
        }
        val shareBtn = ImageView(ctx).apply {
            id = R.id.shareme
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            isFocusable = false
            try { setImageResource(R.drawable.share_round) } catch (e: Exception) {}
        }
        val copyBtn = ImageView(ctx).apply {
            id = R.id.copyme
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt())
            scaleType = ImageView.ScaleType.FIT_CENTER
            isFocusable = false
            rotation = 180f; scaleX = -1f
            try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {}
        }
        // Bookmark as TextView
        val bookmarkBtn = TextView(ctx).apply {
            id = R.id.bookmarkViewBtn
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            text = "📑"; textSize = 20f; gravity = Gravity.CENTER
            isFocusable = false
        }
        val linear3 = LinearLayout(ctx).apply {
            id = R.id.linear3
            layoutParams = LinearLayout.LayoutParams(0, 0)
            try { background = ContextCompat.getDrawable(ctx, R.drawable.baseline_content_copy_24) } catch (e: Exception) {}
        }
        linear4.addView(linear11); linear4.addView(spacer0); linear4.addView(playBtn); linear4.addView(shareBtn); linear4.addView(copyBtn); linear4.addView(bookmarkBtn); linear4.addView(linear3)

        val ayaArabic = TextView(ctx).apply {
            id = R.id.ayaarabic
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            textSize = 28f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT
            textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL
            setTypeface(null, android.graphics.Typeface.BOLD)
            try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {}
        }
        val kanzul = TextView(ctx).apply {
            id = R.id.kanzul
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            text = "কানযুল ঈমান"; setBackgroundColor(Color.parseColor("#E0F2F1")); setTextColor(Color.parseColor("#009688")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
        }
        val nameTv = TextView(ctx).apply {
            id = R.id.name
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            textSize = 16f; setTextColor(Color.BLACK)
        }
        val irfan = TextView(ctx).apply {
            id = R.id.irfan
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            text = "ইরফানুল কুরআন"; setBackgroundColor(Color.parseColor("#E3F2FD")); setTextColor(Color.parseColor("#1E88E5")); textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
        }
        val ayaNumber = TextView(ctx).apply {
            id = R.id.ayanumber
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            textSize = 16f; setTextColor(Color.BLACK)
        }

        lmain.addView(linear4); lmain.addView(ayaArabic); lmain.addView(kanzul); lmain.addView(nameTv); lmain.addView(irfan); lmain.addView(ayaNumber)
        root.addView(lmain)

        val item = list[position]
        var currentId: String? = null; var isPlaying = false
        if (context is QuranviewActivity) {
            currentId = context.getCurrentPlayingId()
            isPlaying = context.isAudioPlaying()
        }
        val itemId = item.optString("_id")
        if (itemId == currentId) {
            if (isPlaying) playBtn.setImageResource(R.drawable.pause_circle) else playBtn.setImageResource(R.drawable.play_circle)
            // highlight
            val highlightDrawable = GradientDrawable().apply {
                setStroke((2*d).toInt(), Color.parseColor("#01837A"))
                setColor(Color.parseColor("#E0F7FA"))
                cornerRadius = 12f*d
            }
            lmain.background = highlightDrawable
            lmain.elevation = 8f*d
        } else {
            playBtn.setImageResource(R.drawable.play_circle)
            val normalDrawable = GradientDrawable().apply {
                setStroke(d.toInt(), Color.parseColor("#01837A"))
                setColor(Color.WHITE)
                cornerRadius = 12f*d
            }
            lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), normalDrawable, null)
            lmain.elevation = 6f*d
        }

        // bookmark state
        if (context is QuranviewActivity) {
            val bm = context.isBookmarked(itemId, context.currentSuraAuthor)
            bookmarkBtn.text = if (bm) "🔖" else "📑"
        }

        try {
            nameTv.text = replaceArabicNumber("${list[position].getString("verses")}. ${list[position].getString("name")}")
            ayaArabic.text = replaceArabicNumber(list[position].getString("names"))
            ayaNumber.text = replaceArabicNumber(list[position].getString("author"))
            number.text = replaceArabicNumber(list[position].getString("verses"))
        } catch (e: JSONException) { e.printStackTrace() }

        playBtn.setOnClickListener {
            if (context is QuranviewActivity) {
                context.playme(item)
                val act = context as QuranviewActivity
                val playAudio: ImageView = act.findViewById(R.id.playAudio)
                if (act.isAudioPlaying()) playAudio.setImageResource(R.drawable.pause) else playAudio.setImageResource(R.drawable.play)
                act.notifyListView()
            }
        }
        copyBtn.setOnClickListener { if (context is QuranviewActivity) (context as QuranviewActivity).copyme(item) }
        shareBtn.setOnClickListener { if (context is QuranviewActivity) (context as QuranviewActivity).shareme(item) }
        bookmarkBtn.setOnClickListener { if (context is QuranviewActivity) (context as QuranviewActivity).toggleBookmark(item) }

        return root
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

// ============== GLOBAL SEARCH ==============

class GlobalSearchActivity : AppCompatActivity() {
    private lateinit var listView1: ListView
    private lateinit var searchbox: EditText
    private lateinit var searchView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var nores: LinearLayout
    private var allSuraAuthors: ArrayList<String> = ArrayList()
    private var filteredItems: ArrayList<JSONObject> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createLayout())

        listView1 = findViewById(R.id.listview1)
        searchbox = findViewById(R.id.searchbox)
        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        nores = findViewById(R.id.nores)
        searchView.visibility = View.VISIBLE

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { finish() }
        findViewById<TextView>(R.id.heading1).text = "গ্লোবাল সার্চ"
        findViewById<ImageView>(R.id.jump).visibility = View.GONE

        findViewById<TextInputLayout>(R.id.boxofsearch).hint = "পুরো কুরআনে সার্চ করুন"
        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().trim()
                if (q.length >= 2) performGlobalSearch(q) else { filteredItems.clear(); listView1.adapter = GlobalSearchAdapter(this@GlobalSearchActivity, filteredItems) }
            }
        })

        // load sura authors
        try {
            val input = resources.assets.open("sura.json")
            val arr = JSONArray(String(input.readBytes(), Charsets.UTF_8))
            input.close()
            for (i in 0 until arr.length()) allSuraAuthors.add(arr.getJSONObject(i).getString("author"))
        } catch (e: Exception) {}
    }

    private fun createLayout(): View {
        val ctx = this
        val d = resources.displayMetrics.density
        val root = ConstraintLayout(ctx).apply {
            id = R.id.main
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            try { background = ContextCompat.getDrawable(ctx, R.drawable.back1ground) } catch (e: Exception) { setBackgroundColor(Color.WHITE) }
            fitsSystemWindows = true
        }
        val topBar = LinearLayout(ctx).apply {
            id = R.id.linear1
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.teal_200)); elevation = 5f*d
            layoutParams = ConstraintLayout.LayoutParams(0, (65*d).toInt()).apply { topToTop = ConstraintLayout.LayoutParams.PARENT_ID; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID }
        }
        val backIv = ImageView(ctx).apply {
            id = R.id.back; layoutParams = LinearLayout.LayoutParams((56*d).toInt(), (56*d).toInt()); setPadding((15*d).toInt(), (15*d).toInt(), (15*d).toInt(), (15*d).toInt()); scaleType = ImageView.ScaleType.CENTER_CROP
            try { setImageResource(R.drawable.ic_arrow_back_white) } catch (e: Exception) {}
        }
        val heading = TextView(ctx).apply {
            id = R.id.heading1; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = (5*d).toInt() }
            setTextColor(Color.WHITE); textSize = 18f; isSingleLine = true
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val jumpIv = ImageView(ctx).apply {
            id = R.id.jump; layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()).apply { rightMargin = (10*d).toInt() }; scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.ic_jump_page) } catch (e: Exception) {}
        }
        val searchIv = ImageView(ctx).apply {
            id = R.id.searchme; layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()).apply { rightMargin = (5*d).toInt() }; scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.searchme) } catch (e: Exception) {}; visibility = View.GONE
        }
        topBar.addView(backIv); topBar.addView(heading); topBar.addView(jumpIv); topBar.addView(searchIv)

        val searchViewLL = LinearLayout(ctx).apply {
            id = R.id.searchView; orientation = LinearLayout.HORIZONTAL; visibility = View.GONE
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topToBottom = R.id.linear1; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; topMargin = (10*d).toInt() }
        }
        val til = TextInputLayout(ctx, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox).apply {
            id = R.id.boxofsearch; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
        }
        val et = EditText(ctx).apply {
            id = R.id.searchbox; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt()); setTextColor(Color.BLACK); textSize = 14f
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        til.addView(et)
        val cancelIv = ImageView(ctx).apply {
            id = R.id.cancelme; layoutParams = LinearLayout.LayoutParams((30*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { rightMargin = (5*d).toInt() }; scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.cancel) } catch (e: Exception) {}
        }
        searchViewLL.addView(til); searchViewLL.addView(cancelIv)

        val progressContainer = LinearLayout(ctx).apply {
            id = R.id.progressContainer; orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topToBottom = R.id.searchView; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID }
        }
        val pb = ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal).apply {
            id = R.id.progressBar; layoutParams = LinearLayout.LayoutParams(0, (8*d).toInt(), 1f); max = 100
        }
        val pt = TextView(ctx).apply {
            id = R.id.progressText; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { leftMargin = (8*d).toInt() }
            text = "⏳ সার্চ চলছে... ০ টি পাওয়া গেছে"; textSize = 14f; setTextColor(Color.parseColor("#607D8B"))
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        progressContainer.addView(pb); progressContainer.addView(pt)

        val noresLL = LinearLayout(ctx).apply {
            id = R.id.nores; visibility = View.GONE; orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(Color.WHITE); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topToBottom = R.id.progressContainer; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID }
        }
        val noresImg = ImageView(ctx).apply {
            id = R.id.noresult; layoutParams = LinearLayout.LayoutParams((100*d).toInt(), (100*d).toInt()).apply { gravity = Gravity.CENTER }; scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.noresult) } catch (e: Exception) {}
        }
        val noresTv = TextView(ctx).apply {
            id = R.id.no_result; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
            text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"; textSize = 16f; setTextColor(Color.BLACK); gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        noresLL.addView(noresImg); noresLL.addView(noresTv)

        val lv = ListView(ctx).apply {
            id = R.id.listview1
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply { topToBottom = R.id.nores; bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; topMargin = (10*d).toInt() }
            divider = null; dividerHeight = 0; setBackgroundColor(Color.WHITE)
        }

        val audioTab = LinearLayout(ctx).apply { id = R.id.audiotab; visibility = View.GONE; layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID } }

        root.addView(topBar); root.addView(searchViewLL); root.addView(progressContainer); root.addView(noresLL); root.addView(lv); root.addView(audioTab)
        return root
    }

    private fun performGlobalSearch(query: String) {
        filteredItems.clear()
        progressBar.progress = 0
        progressText.text = "⏳ সার্চ চলছে... ০ টি পাওয়া গেছে"
        lifecycleScope.launch {
            var found = 0; var scanned = 0
            val total = allSuraAuthors.size
            val suraMap = mutableMapOf<String, JSONObject>() // author -> sura info
            try {
                val input = resources.assets.open("sura.json")
                val arr = JSONArray(String(input.readBytes(), Charsets.UTF_8)); input.close()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i); suraMap[o.getString("author")] = o
                }
            } catch (e: Exception) {}

            for (author in allSuraAuthors) {
                scanned++
                val matches = withContext(Dispatchers.IO) {
                    val list = ArrayList<JSONObject>()
                    try {
                        val f = resources.assets.open("$author.json")
                        val arr = JSONArray(String(f.readBytes(), Charsets.UTF_8)); f.close()
                        for (j in 0 until arr.length()) {
                            val obj = arr.getJSONObject(j)
                            val name = obj.optString("name", "")
                            val names = obj.optString("names", "")
                            val tafsir = obj.optString("author", "")
                            if (name.contains(query, true) || names.contains(query, true) || tafsir.contains(query, true)) {
                                val suraInfo = suraMap[author]
                                obj.put("suraName", suraInfo?.optString("name") ?: author)
                                obj.put("suraAuthor", author)
                                obj.put("suraNumber", suraInfo?.optString("bookid") ?: "1")
                                list.add(obj)
                            }
                        }
                    } catch (e: Exception) {}
                    list
                }
                found += matches.size
                filteredItems.addAll(matches)
                withContext(Dispatchers.Main) {
                    progressBar.progress = scanned * 100 / total
                    progressText.text = "⏳ সার্চ চলছে... $scanned/$total স্ক্যান - $found টি আয়াত পাওয়া গেছে"
                    if (filteredItems.isEmpty()) nores.visibility = View.VISIBLE else nores.visibility = View.GONE
                    listView1.adapter = GlobalSearchAdapter(this@GlobalSearchActivity, ArrayList(filteredItems))
                }
            }
            progressText.text = "✅ $found টি আয়াত পাওয়া গেছে"
        }
    }
}

class GlobalSearchAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val ctx = context
        val d = ctx.resources.displayMetrics.density
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); setBackgroundColor(Color.WHITE)
        }
        val suraHeader = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (5*d).toInt(), (10*d).toInt(), (2*d).toInt()) }
            textSize = 12f; setTextColor(Color.parseColor("#01837A")); setTypeface(null, android.graphics.Typeface.BOLD)
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        val lmain = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            elevation = 4f*d
            setBackgroundColor(Color.WHITE)
        }
        val topRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (50*d).toInt())
        }
        val num = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams((50*d).toInt(), (50*d).toInt()); gravity = Gravity.CENTER
            try { background = ContextCompat.getDrawable(ctx, R.drawable.ic_1_4) } catch (e: Exception) {}
            textSize = 11f; setTextColor(Color.parseColor("#607D8B")); setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val playBtn = ImageView(ctx).apply {
            id = R.id.playme; layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false
            try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {}
        }
        val copyBtn = ImageView(ctx).apply {
            id = R.id.copyme; layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; rotation = 180f; scaleX = -1f
            try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {}
        }
        val shareBtn = ImageView(ctx).apply {
            id = R.id.shareme; layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false
            try { setImageResource(R.drawable.share_round) } catch (e: Exception) {}
        }
        val bookmarkBtn = TextView(ctx).apply {
            id = R.id.bookmarkViewBtn; layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            text = "📑"; textSize = 20f; gravity = Gravity.CENTER; isFocusable = false
        }
        topRow.addView(num); topRow.addView(playBtn); topRow.addView(copyBtn); topRow.addView(shareBtn); topRow.addView(bookmarkBtn)

        val ayaArabic = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            textSize = 24f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL
            try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {}
        }
        val nameTv = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            textSize = 16f; setTextColor(Color.BLACK)
        }
        lmain.addView(topRow); lmain.addView(ayaArabic); lmain.addView(nameTv)
        root.addView(suraHeader); root.addView(lmain)

        val item = list[position]
        suraHeader.text = "${item.optString("suraName")} - আয়াত ${item.optString("verses")}"
        num.text = item.optString("verses")
        ayaArabic.text = item.optString("names")
        nameTv.text = item.optString("name")

        playBtn.setOnClickListener {
            val intent = Intent(ctx, QuranviewActivity::class.java)
            intent.putExtra("name", item.optString("suraName"))
            intent.putExtra("booklist", "${item.optString("suraAuthor")}.json")
            intent.putExtra("scrollToAyah", item.optString("verses"))
            ctx.startActivity(intent)
        }
        copyBtn.setOnClickListener {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("ayah", "${item.optString("names")}\n${item.optString("name")}"))
            Toast.makeText(ctx, "কপি হয়েছে", Toast.LENGTH_SHORT).show()
        }
        shareBtn.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, "${item.optString("names")}\n${item.optString("name")}\n${item.optString("suraName")}")
            ctx.startActivity(Intent.createChooser(share, "শেয়ার"))
        }
        // bookmark check
        val prefsBm = ctx.getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefsBm.getString("bookmarks_json","[]")) } catch (e: Exception) { JSONArray() }
        var isBm = false
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("_id")==item.optString("_id") && o.optString("suraAuthor")==item.optString("suraAuthor")) { isBm=true; break }
        }
        bookmarkBtn.text = if (isBm) "🔖" else "📑"
        bookmarkBtn.setOnClickListener {
            // toggle
            val jsonStr = prefsBm.getString("bookmarks_json","[]")
            val arr2 = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
            var foundIdx = -1
            for (i in 0 until arr2.length()) {
                val o = arr2.getJSONObject(i)
                if (o.optString("_id")==item.optString("_id") && o.optString("suraAuthor")==item.optString("suraAuthor")) { foundIdx=i; break }
            }
            if (foundIdx>=0) {
                val newArr = JSONArray()
                for (i in 0 until arr2.length()) if (i!=foundIdx) newArr.put(arr2.getJSONObject(i))
                prefsBm.edit().putString("bookmarks_json", newArr.toString()).apply()
                bookmarkBtn.text = "📑"
                Toast.makeText(ctx, "বুকমার্ক মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                val bm = JSONObject()
                bm.put("suraNumber", item.optString("suraNumber").toIntOrNull() ?: 1)
                bm.put("suraName", item.optString("suraName"))
                bm.put("suraAuthor", item.optString("suraAuthor"))
                bm.put("ayahNumber", item.optString("verses"))
                bm.put("_id", item.optString("_id"))
                bm.put("name", item.optString("name"))
                bm.put("names", item.optString("names"))
                bm.put("author", item.optString("author"))
                bm.put("timestamp", System.currentTimeMillis())
                arr2.put(bm)
                prefsBm.edit().putString("bookmarks_json", arr2.toString()).apply()
                bookmarkBtn.text = "🔖"
                Toast.makeText(ctx, "বুকমার্ক যোগ হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }

        val drawable = GradientDrawable().apply { setStroke(d.toInt(), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = 12f*d }
        lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), drawable, null)
        lmain.elevation = 6f*d
        return root
    }
}

// ============== BOOKMARK ==============

class BookmarkActivity : AppCompatActivity() {
    private lateinit var listView1: ListView
    private lateinit var nores: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createLayout())
        listView1 = findViewById(R.id.listview1)
        nores = findViewById(R.id.nores)
        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.heading1).text = "বুকমার্ক"
        findViewById<ImageView>(R.id.jump).visibility = View.GONE
        findViewById<ImageView>(R.id.searchme).visibility = View.GONE
        loadBookmarks()
    }

    private fun createLayout(): View {
        val ctx = this
        val d = resources.displayMetrics.density
        val root = ConstraintLayout(ctx).apply {
            id = R.id.main
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            try { background = ContextCompat.getDrawable(ctx, R.drawable.back1ground) } catch (e: Exception) { setBackgroundColor(Color.WHITE) }
            fitsSystemWindows = true
        }
        val topBar = LinearLayout(ctx).apply {
            id = R.id.linear1
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.teal_200)); elevation = 5f*d
            layoutParams = ConstraintLayout.LayoutParams(0, (65*d).toInt()).apply { topToTop = ConstraintLayout.LayoutParams.PARENT_ID; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID }
        }
        val backIv = ImageView(ctx).apply {
            id = R.id.back; layoutParams = LinearLayout.LayoutParams((56*d).toInt(), (56*d).toInt()); setPadding((15*d).toInt(), (15*d).toInt(), (15*d).toInt(), (15*d).toInt()); scaleType = ImageView.ScaleType.CENTER_CROP
            try { setImageResource(R.drawable.ic_arrow_back_white) } catch (e: Exception) {}
        }
        val heading = TextView(ctx).apply {
            id = R.id.heading1; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = (5*d).toInt() }
            setTextColor(Color.WHITE); textSize = 18f; isSingleLine = true
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val jumpIv = ImageView(ctx).apply { id = R.id.jump; layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()).apply { rightMargin = (10*d).toInt() }; scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.ic_jump_page) } catch (e: Exception) {} }
        val searchIv = ImageView(ctx).apply { id = R.id.searchme; layoutParams = LinearLayout.LayoutParams((30*d).toInt(), (30*d).toInt()).apply { rightMargin = (5*d).toInt() }; scaleType = ImageView.ScaleType.FIT_CENTER; try { setImageResource(R.drawable.searchme) } catch (e: Exception) {} }
        topBar.addView(backIv); topBar.addView(heading); topBar.addView(jumpIv); topBar.addView(searchIv)

        val noresLL = LinearLayout(ctx).apply {
            id = R.id.nores; visibility = View.GONE; orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(Color.WHITE); setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topToBottom = R.id.linear1; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID }
        }
        val noresImg = ImageView(ctx).apply {
            id = R.id.noresult; layoutParams = LinearLayout.LayoutParams((100*d).toInt(), (100*d).toInt()).apply { gravity = Gravity.CENTER }; scaleType = ImageView.ScaleType.FIT_CENTER
            try { setImageResource(R.drawable.noresult) } catch (e: Exception) {}
        }
        val noresTv = TextView(ctx).apply {
            id = R.id.no_result; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
            text = "কোন বুকমার্ক নেই"; textSize = 16f; setTextColor(Color.BLACK); gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        noresLL.addView(noresImg); noresLL.addView(noresTv)

        val lv = ListView(ctx).apply {
            id = R.id.listview1
            layoutParams = ConstraintLayout.LayoutParams(0, 0).apply { topToBottom = R.id.nores; bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; startToStart = ConstraintLayout.LayoutParams.PARENT_ID; endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; topMargin = (10*d).toInt() }
            divider = null; dividerHeight = 0; setBackgroundColor(Color.WHITE)
        }
        val audioTab = LinearLayout(ctx).apply { id = R.id.audiotab; visibility = View.GONE; layoutParams = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID } }

        root.addView(topBar); root.addView(noresLL); root.addView(lv); root.addView(audioTab)
        return root
    }

    private fun loadBookmarks() {
        val prefs = getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("bookmarks_json", "[]")
        val arr = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
        if (arr.length() == 0) { nores.visibility = View.VISIBLE; return } else nores.visibility = View.GONE
        val list = ArrayList<JSONObject>()
        for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
        listView1.adapter = BookmarkAdapter(this, list)
    }
}

class BookmarkAdapter(context: Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val ctx = context
        val d = ctx.resources.displayMetrics.density
        val root = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); setBackgroundColor(Color.WHITE) }
        val header = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (5*d).toInt(), (10*d).toInt(), (2*d).toInt()) }
            textSize = 12f; setTextColor(Color.parseColor("#01837A")); setTypeface(null, android.graphics.Typeface.BOLD)
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        val lmain = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
            elevation = 4f*d; setBackgroundColor(Color.WHITE)
        }
        val topRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (50*d).toInt())
        }
        // BOOKMARK CANCEL TEXTVIEW
        val cancelBtn = TextView(ctx).apply {
            id = R.id.bookmarkCancel
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            text = "❌"; textSize = 20f; gravity = Gravity.CENTER; isFocusable = false
        }
        val playBtn = ImageView(ctx).apply {
            id = R.id.playme
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false
            try { setImageResource(R.drawable.play_circle) } catch (e: Exception) {}
        }
        val copyBtn = ImageView(ctx).apply {
            id = R.id.copyme
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false; rotation = 180f; scaleX = -1f
            try { setImageResource(R.drawable.content_copy) } catch (e: Exception) {}
        }
        val shareBtn = ImageView(ctx).apply {
            id = R.id.shareme
            layoutParams = LinearLayout.LayoutParams((40*d).toInt(), ViewGroup.LayoutParams.MATCH_PARENT).apply { setMargins((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()) }
            setPadding((5*d).toInt(), (5*d).toInt(), (5*d).toInt(), (5*d).toInt()); scaleType = ImageView.ScaleType.FIT_CENTER; isFocusable = false
            try { setImageResource(R.drawable.share_round) } catch (e: Exception) {}
        }
        topRow.addView(cancelBtn); topRow.addView(playBtn); topRow.addView(copyBtn); topRow.addView(shareBtn)

        val ayaArabic = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            textSize = 24f; setTextColor(Color.BLACK); gravity = Gravity.RIGHT; textDirection = View.TEXT_DIRECTION_RTL; layoutDirection = View.LAYOUT_DIRECTION_RTL
            try { typeface = ResourcesCompat.getFont(ctx, R.font.noorehuda) } catch (e: Exception) {}
        }
        val nameTv = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins((10*d).toInt(), (10*d).toInt(), (10*d).toInt(), (10*d).toInt()) }
            textSize = 16f; setTextColor(Color.BLACK)
        }
        lmain.addView(topRow); lmain.addView(ayaArabic); lmain.addView(nameTv)
        root.addView(header); root.addView(lmain)

        val item = list[position]
        header.text = "${item.optString("suraName")} - আয়াত ${item.optString("ayahNumber")}"
        ayaArabic.text = item.optString("names")
        nameTv.text = item.optString("name")

        cancelBtn.setOnClickListener {
            val prefs = ctx.getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("bookmarks_json","[]")
            val arr = try { JSONArray(jsonStr) } catch (e: Exception) { JSONArray() }
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (!(o.optString("_id")==item.optString("_id") && o.optString("suraAuthor")==item.optString("suraAuthor"))) newArr.put(o)
            }
            prefs.edit().putString("bookmarks_json", newArr.toString()).apply()
            list.removeAt(position)
            notifyDataSetChanged()
            Toast.makeText(ctx, "বুকমার্ক থেকে বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
            if (list.isEmpty()) (ctx as? BookmarkActivity)?.findViewById<LinearLayout>(R.id.nores)?.visibility = View.VISIBLE
        }
        playBtn.setOnClickListener {
            val intent = Intent(ctx, QuranviewActivity::class.java)
            intent.putExtra("name", item.optString("suraName"))
            intent.putExtra("booklist", "${item.optString("suraAuthor")}.json")
            intent.putExtra("scrollToAyah", item.optString("ayahNumber"))
            ctx.startActivity(intent)
        }
        copyBtn.setOnClickListener {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("ayah", "${item.optString("names")}\n${item.optString("name")}"))
            Toast.makeText(ctx, "কপি হয়েছে", Toast.LENGTH_SHORT).show()
        }
        shareBtn.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND); share.type = "text/plain"; share.putExtra(Intent.EXTRA_TEXT, "${item.optString("names")}\n${item.optString("name")}\n${item.optString("suraName")}")
            ctx.startActivity(Intent.createChooser(share, "শেয়ার"))
        }

        val drawable = GradientDrawable().apply { setStroke(d.toInt(), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = 12f*d }
        lmain.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#01837A")), drawable, null)
        lmain.elevation = 6f*d
        return root
    }
}
