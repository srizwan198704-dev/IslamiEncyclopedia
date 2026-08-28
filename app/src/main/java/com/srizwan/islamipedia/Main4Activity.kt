package com.srizwan.islamipedia

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors

class Main4Activity : AppCompatActivity() {

    enum class Mode { BOOK_LIST, GLOBAL_SEARCH }

    private lateinit var jump: ImageView
    private lateinit var back: ImageView
    private lateinit var listView1: ListView
    private lateinit var searchtop: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var nores: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var cancel: ImageView
    private lateinit var searchbox: EditText
    private lateinit var select: LinearLayout
    private lateinit var boxofauthor: TextInputLayout
    private lateinit var boxofsubject: TextInputLayout
    private lateinit var searchboxauthor: EditText
    private lateinit var searchboxsubject: EditText
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var bookid: Array<String>
    private lateinit var subject: Array<String>
    private lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject>

    private var currentMode = Mode.BOOK_LIST
    private var lastQuery = ""
    @Volatile private var globalSearchVersion = 0
    private var globalList: ArrayList<JSONObject> = ArrayList()
    private var allBookIds: ArrayList<String> = ArrayList()
    private var bookInfoMap: MutableMap<String, JSONObject> = mutableMapOf()
    private var expandedIds: MutableSet<String> = mutableSetOf()

    private var displayedList: ArrayList<JSONObject> = ArrayList()
    private var pageSize = 20
    private var currentPage = 0
    private var isSearching = false
    private var globalAdapter: GlobalSearchAdapter? = null

    private lateinit var progressContainer: LinearLayout
    private lateinit var progressBarCircle: ProgressBar
    private lateinit var progressBarH: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var fabGlobalSearch: FloatingActionButton
    private lateinit var prefs: SharedPreferences

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun dp(i: Int): Int { return (i * resources.displayMetrics.density).toInt() }
    private fun dpF(f: Float): Float { return f * resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        prefs = getSharedPreferences("book_cache", Context.MODE_PRIVATE)

        select = findViewById(R.id.select)
        nores = findViewById(R.id.nores)
        boxofsubject = findViewById(R.id.boxofsubject)
        boxofauthor = findViewById(R.id.boxofauthor)
        searchboxauthor = findViewById(R.id.searchboxauthor)
        searchboxsubject = findViewById(R.id.searchboxsubject)
        searchboxauthor.isEnabled = false
        searchboxsubject.isEnabled = false
        boxofauthor.setOnClickListener {
            startActivity(Intent(applicationContext, HadisActivity::class.java).apply {
                putExtra("sub", "ইসলামী বই সমাহার"); putExtra("booklist", "file.json")
            })
        }

        val heading1: TextView = findViewById(R.id.heading1)
        heading1.text = intent.getStringExtra("sub")
        jump = findViewById(R.id.jump); jump.visibility = View.GONE
        back = findViewById(R.id.back)
        back.setOnClickListener { if (currentMode == Mode.GLOBAL_SEARCH) switchMode(Mode.BOOK_LIST, heading1) else finish() }

        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "বইয়ের বা লেখকের নাম লিখে সার্চ করুন"
        searchbox.setHintTextColor(ColorStateList.valueOf(hintColor))
        cancel = findViewById(R.id.cancelme)
        cancel.setOnClickListener { if (searchbox.text.toString() == "") searchView.visibility = View.GONE else searchbox.text.clear() }

        searchtop = findViewById(R.id.searchme)
        searchView = findViewById(R.id.searchView)
        searchtop.setOnClickListener {
            searchView.visibility = if (searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            if (searchView.visibility == View.VISIBLE) searchbox.requestFocus()
        }

        mainHandler.postDelayed({
            if (intent?.hasExtra("get") == true) searchbox.text = Editable.Factory.getInstance().newEditable(intent.getStringExtra("get"))
            else searchbox.setText("")
            select.visibility = View.GONE; searchtop.visibility = View.VISIBLE
        }, 10)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            try {
                window.statusBarColor = Color.parseColor(getString(R.string.color))
                window.navigationBarColor = Color.parseColor(getString(R.string.color))
            } catch (e: Exception) {
                window.statusBarColor = Color.parseColor("#01837A"); window.navigationBarColor = Color.parseColor("#01837A")
            }
        }

        listView1 = findViewById(R.id.listview1)
        val jsonArray = getCachedFileJson()
        listItems = getArrayListFromJSONArray(jsonArray)
        filteredItems = ArrayList(listItems)
        listView1.adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
        listView1.isFastScrollEnabled = true
        listView1.setOnItemClickListener { _, _, position, _ -> if (currentMode == Mode.BOOK_LIST) handleListItemClick(position) }

        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString(); lastQuery = query
                if (currentMode == Mode.GLOBAL_SEARCH) {
                    if (query.length >= 2) performGlobalSearch(query, allBookIds)
                    else {
                        globalSearchVersion++; globalList.clear(); displayedList.clear()
                        globalAdapter = GlobalSearchAdapter(this@Main4Activity, displayedList)
                        listView1.adapter = globalAdapter
                        progressContainer.visibility = View.GONE; nores.visibility = View.GONE
                    }
                } else filterList(query)
            }
        })

        addFabAndProgressOverlay()
        setupPaginationScroll()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentMode == Mode.GLOBAL_SEARCH) {
                    if (searchView.visibility == View.VISIBLE && searchbox.text.toString().isEmpty()) switchMode(Mode.BOOK_LIST, heading1)
                    else if (searchView.visibility == View.VISIBLE) searchbox.text.clear()
                    else switchMode(Mode.BOOK_LIST, heading1); return
                }
                if (searchView.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) searchView.visibility = View.GONE else searchbox.text.clear()
                } else finish()
            }
        })
    }

    private fun getCachedFileJson(): JSONArray? {
        return try {
            val assetJson = assets.open("file.json").use { String(it.readBytes(), Charsets.UTF_8) }
            val cached = prefs.getString("cached_file_json", null)
            val cachedHash = prefs.getString("cached_hash", "")
            val newHash = assetJson.hashCode().toString() + "_" + assetJson.length
            if (cached == null || cachedHash!= newHash) {
                prefs.edit().putString("cached_file_json", assetJson).putString("cached_hash", newHash).apply()
                JSONArray(assetJson)
            } else {
                JSONArray(cached)
            }
        } catch (e: Exception) { getJSonData("file.json") }
    }

    private fun addFabAndProgressOverlay() {
        val rootContent = findViewById<ViewGroup>(android.R.id.content) as ViewGroup

        progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; visibility = View.GONE
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(16f); setStroke(dp(1), Color.parseColor("#01837A")) }
            elevation = dpF(12f)
        }
        progressBarCircle = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48)).apply { gravity = Gravity.CENTER }
            indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
        }
        progressBarH = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(dp(200), dp(6)).apply { topMargin = dp(12); gravity = Gravity.CENTER }
            max = 100; progressTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
        }
        progressText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8); gravity = Gravity.CENTER }
            text = "⏳ সার্চ চলছে..."; textSize = 13f; setTextColor(Color.BLACK); gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi) } catch (e: Exception) {}
        }
        progressContainer.addView(progressBarCircle); progressContainer.addView(progressBarH); progressContainer.addView(progressText)
        rootContent.addView(progressContainer, FrameLayout.LayoutParams(dp(240), ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER })
        progressContainer.bringToFront()

        fabGlobalSearch = FloatingActionButton(this).apply {
            setImageResource(R.drawable.searchme)
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            elevation = dpF(8f)
        }
        rootContent.addView(fabGlobalSearch, FrameLayout.LayoutParams(dp(56), dp(56)).apply {
            gravity = Gravity.BOTTOM or Gravity.END; bottomMargin = dp(90); rightMargin = dp(16)
        })
        fabGlobalSearch.bringToFront()
        fabGlobalSearch.setOnClickListener { showCustomGlobalSearchDialog() }
    }

    private fun showCustomGlobalSearchDialog() {
        val ctx = this
        val whiteBg = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(16f) }

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL; background = whiteBg
            setPadding(dp(16), dp(16), dp(16), dp(12))
        }

        val titleTv = TextView(ctx).apply {
            text = "সকল কিতাব সার্চ করুন"; textSize = 17f; setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK); gravity = Gravity.CENTER
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            setPadding(0, 0, 0, dp(12))
        }

        val inputLayout = TextInputLayout(ctx).apply {
            hint = "সার্চ করুন..."; boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxCornerRadii(dpF(12f), dpF(12f), dpF(12f), dpF(12f))
            boxStrokeColor = Color.parseColor("#01837A")
            hintTextColor = ColorStateList.valueOf(Color.BLACK)
        }
        val editText = EditText(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            inputType = InputType.TYPE_CLASS_TEXT; textSize = 14f; setTextColor(Color.BLACK); setHintTextColor(Color.GRAY)
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            setText(lastQuery); setSelection(text.length)
        }
        inputLayout.addView(editText)

        val optionLabel = TextView(ctx).apply {
            text = "কিতাব নির্বাচন করুন"; textSize = 13f; setTextColor(Color.BLACK); setTypeface(null, Typeface.BOLD)
            try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            setPadding(0, dp(12), 0, dp(6))
        }

        val loadingTv = TextView(ctx).apply {
            text = "⏳ কিতাব লোড হচ্ছে..."; setTextColor(Color.GRAY); gravity = Gravity.CENTER; textSize = 12f
            setPadding(0, dp(20), 0, dp(20))
        }

        val optionBox = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply { setColor(Color.parseColor("#F5F5F5")); cornerRadius = dpF(12f); setStroke(dp(1), Color.parseColor("#E0E0E0")) }
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        val scroll = ScrollView(ctx).apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(240)) }
        val checkContainer = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        checkContainer.addView(loadingTv)
        scroll.addView(checkContainer)
        optionBox.addView(scroll)
        root.addView(titleTv); root.addView(inputLayout); root.addView(optionLabel); root.addView(optionBox)

        val dialog = AlertDialog.Builder(ctx).setView(root)
         .setPositiveButton("সার্চ করুন", null)
         .setNegativeButton("বাতিল করুন") { d, _ -> d.dismiss() }
         .create()

        dialog.show()
        dialog.window?.setBackgroundDrawable(GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(16f) })

        // Executor দিয়ে non-deprecated background load
        executor.execute {
            val ids = ArrayList(allBookIds)
            val infos = HashMap(bookInfoMap)
            mainHandler.post {
                checkContainer.removeAllViews()
                val selectedIds = mutableSetOf<String>()
                selectedIds.addAll(ids)

                val allCheck = CheckBox(ctx).apply {
                    text = "★ সকল কিতাব"; textSize = 14f; setTextColor(Color.BLACK); isChecked = true
                    try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
                    buttonTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
                }
                checkContainer.addView(allCheck)
                val bookChecks = ArrayList<CheckBox>()

                for (bId in ids) {
                    val bName = infos[bId]?.optString("name")?: bId
                    val cb = CheckBox(ctx).apply {
                        text = bName; textSize = 13f; setTextColor(Color.BLACK); isChecked = true
                        try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
                        buttonTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
                    }
                    bookChecks.add(cb); checkContainer.addView(cb)
                }

                var isProgrammatic = false
                allCheck.setOnCheckedChangeListener { _, isChecked ->
                    if (isProgrammatic) return@setOnCheckedChangeListener
                    isProgrammatic = true
                    if (isChecked) {
                        selectedIds.clear(); selectedIds.addAll(ids)
                        for (c in bookChecks) c.isChecked = true
                    } else {
                        selectedIds.clear()
                        for (c in bookChecks) c.isChecked = false
                    }
                    isProgrammatic = false
                }

                for ((index, cb) in bookChecks.withIndex()) {
                    cb.setOnCheckedChangeListener { _, isChecked ->
                        if (isProgrammatic) return@setOnCheckedChangeListener
                        val bId = ids[index]
                        if (isChecked) selectedIds.add(bId) else selectedIds.remove(bId)
                        isProgrammatic = true
                        allCheck.isChecked = selectedIds.size == ids.size
                        isProgrammatic = false
                    }
                }

                val btnSearch = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                btnSearch.setTextColor(Color.parseColor("#01837A"))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY)
                btnSearch.setOnClickListener {
                    val q = editText.text.toString().trim()
                    if (q.length < 2) { editText.error = "কমপক্ষে ২ অক্ষর"; return@setOnClickListener }
                    if (selectedIds.isEmpty()) {
                        Toast.makeText(ctx, "অন্তত ১ টি কিতাব সিলেক্ট করুন", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val heading1: TextView = findViewById(R.id.heading1)
                    switchMode(Mode.GLOBAL_SEARCH, heading1)
                    searchView.visibility = View.VISIBLE
                    searchbox.setText(q); searchbox.setSelection(q.length)
                    performGlobalSearch(q, ArrayList(selectedIds))
                    dialog.dismiss()
                }
            }
        }

        editText.requestFocus()
        mainHandler.postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun setupPaginationScroll() {
        listView1.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, state: Int) {}
            override fun onScroll(view: AbsListView?, firstVisible: Int, visibleCount: Int, totalCount: Int) {
                if (currentMode!= Mode.GLOBAL_SEARCH) return
                if (totalCount == 0) return
                if (isSearching) return
                if (firstVisible + visibleCount >= totalCount - 2) {
                    if (displayedList.size < globalList.size) loadNextPage()
                }
            }
        })
    }

    private fun loadNextPage() {
        if (displayedList.size >= globalList.size) return
        val nextPage = currentPage + 1
        val start = nextPage * pageSize
        var end = start + pageSize
        if (end > globalList.size) end = globalList.size
        if (start >= end) return
        currentPage = nextPage
        for (i in start until end) displayedList.add(globalList[i])
        globalAdapter?.notifyDataSetChanged()
    }

    private fun switchMode(mode: Mode, heading1: TextView? = null) {
        currentMode = mode
        val heading = heading1?: findViewById<TextView>(R.id.heading1)
        when (mode) {
            Mode.BOOK_LIST -> {
                heading.text = intent.getStringExtra("sub")?: "ইসলামী বই সমাহার"
                boxofsearch.hint = "বইয়ের বা লেখকের নাম লিখে সার্চ করুন"
                progressContainer.visibility = View.GONE
                fabGlobalSearch.visibility = View.VISIBLE
                fabGlobalSearch.bringToFront()
                nores.visibility = View.GONE
                searchView.visibility = View.GONE
                searchbox.text.clear()
                listView1.adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
            }
            Mode.GLOBAL_SEARCH -> {
                heading.text = "গ্লোবাল সার্চ"
                boxofsearch.hint = "সব কিতাবের ভিতরে সার্চ করুন"
                searchView.visibility = View.VISIBLE
                fabGlobalSearch.visibility = View.GONE
                globalSearchVersion++
                globalList.clear(); displayedList.clear(); currentPage = 0
                globalAdapter = GlobalSearchAdapter(this, displayedList)
                listView1.adapter = globalAdapter
            }
        }
    }

    private fun getJSonData(fileName: String): JSONArray? {
        return try {
            val input = assets.open(fileName)
            val data = ByteArray(input.available()); input.read(data); input.close()
            JSONArray(String(data, Charsets.UTF_8))
        } catch (e: Exception) { null }
    }

    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        try {
            if (jsonArray!= null) {
                name = Array(jsonArray.length()) { "" }; author = Array(jsonArray.length()) { "" }
                bookid = Array(jsonArray.length()) { "" }; subject = Array(jsonArray.length()) { "" }
                allBookIds.clear(); bookInfoMap.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    obj.put("original_index", i); aList.add(obj)
                    name[i] = obj.optString("name", ""); author[i] = obj.optString("author", "")
                    bookid[i] = obj.optString("bookid", ""); subject[i] = obj.optString("subject", "")
                    val bId = obj.optString("bookid", "").trim()
                    if (bId.isNotEmpty()) { allBookIds.add(bId); bookInfoMap[bId] = obj }
                }
            }
        } catch (je: JSONException) { je.printStackTrace() }
        return aList
    }

    private fun filterList(query: String) {
        filteredItems.clear()
        for (i in name.indices) {
            if (name[i].contains(query, true) || author[i].contains(query, true) || subject[i].contains(query, true)) {
                filteredItems.add(listItems[i]); nores.visibility = View.GONE
            }
        }
        if (filteredItems.isEmpty()) nores.visibility = View.VISIBLE
        listView1.adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
    }

    private fun readBookFile(bookId: String): String? {
        val tries = listOf("books/$bookId", bookId)
        for (p in tries) {
            try {
                assets.open(p).use { input ->
                    val bytes = input.readBytes()
                    if (bytes.size > 4 * 1024 * 1024) continue
                    val str = String(bytes, Charsets.UTF_8)
                    if (str.trim().isNotEmpty()) return str
                }
            } catch (e: Exception) { continue }
        }
        return null
    }

    private fun performGlobalSearch(query: String, targetBookIds: ArrayList<String>) {
        lastQuery = query; globalSearchVersion++; val myVersion = globalSearchVersion
        val searchQuery = query.trim(); if (searchQuery.length < 2) return
        isSearching = true; globalList.clear(); displayedList.clear(); currentPage = 0
        globalAdapter = GlobalSearchAdapter(this, displayedList)
        listView1.adapter = globalAdapter
        progressContainer.visibility = View.VISIBLE
        progressBarCircle.visibility = View.VISIBLE
        progressBarH.visibility = View.VISIBLE
        progressBarH.progress = 0
        progressText.text = "⏳ ${targetBookIds.size} টি কিতাবে সার্চ চলছে..."
        nores.visibility = View.GONE
        progressContainer.bringToFront()

        executor.execute {
            var scanned = 0; val total = targetBookIds.size
            val batch = ArrayList<JSONObject>()
            for (bId in targetBookIds) {
                if (myVersion!= globalSearchVersion) return@execute
                scanned++
                try {
                    val jsonStr = readBookFile(bId)?: continue
                    val arr = JSONArray(jsonStr)
                    for (j in 0 until arr.length()) {
                        if (myVersion!= globalSearchVersion) return@execute
                        val obj = arr.getJSONObject(j)
                        val title = obj.optString("1", "")
                        val content = obj.optString("2", "")
                        if ((title + " " + content).contains(searchQuery, true)) {
                            val info = bookInfoMap[bId]
                            val newObj = JSONObject()
                            newObj.put("title", title); newObj.put("content", content)
                            newObj.put("bookName", info?.optString("name")?: bId)
                            newObj.put("bookAuthor", info?.optString("author")?: "")
                            newObj.put("bookid", bId); newObj.put("_unique_id", "${bId}_$j"); newObj.put("pos", j)
                            batch.add(newObj)
                            if (batch.size >= 30) {
                                val copy = ArrayList(batch); batch.clear()
                                mainHandler.post {
                                    if (myVersion!= globalSearchVersion) return@post
                                    globalList.addAll(copy)
                                    if (displayedList.isEmpty()) {
                                        val end = minOf(pageSize, globalList.size)
                                        for (k in 0 until end) displayedList.add(globalList[k])
                                        globalAdapter?.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {}
                val sc = scanned
                mainHandler.post {
                    if (myVersion!= globalSearchVersion || lastQuery!= query) return@post
                    progressBarH.progress = sc * 100 / total
                    progressText.text = "⏳ $sc/$total - ${globalList.size + batch.size} টি"
                }
            }
            if (batch.isNotEmpty()) globalList.addAll(batch)
            mainHandler.post {
                if (myVersion!= globalSearchVersion || lastQuery!= query) return@post
                isSearching = false
                if (globalList.isEmpty()) {
                    progressBarCircle.visibility = View.GONE; progressBarH.visibility = View.GONE
                    progressText.text = "❌ কোনো রেজাল্ট পাওয়া যায়নি"
                    nores.visibility = View.VISIBLE
                    mainHandler.postDelayed({ progressContainer.visibility = View.GONE }, 2000)
                } else {
                    progressBarCircle.visibility = View.GONE
                    progressBarH.progress = 100
                    progressText.text = "✅ ${globalList.size} টি পাওয়া গেছে"
                    if (displayedList.isEmpty()) {
                        val end = minOf(pageSize, globalList.size)
                        for (k in 0 until end) displayedList.add(globalList[k])
                        globalAdapter?.notifyDataSetChanged()
                    }
                    mainHandler.postDelayed({ progressContainer.visibility = View.GONE }, 2500)
                }
            }
        }
    }

    private fun getHighlightedText(fullText: String, query: String): SpannableString {
        val spannable = SpannableString(fullText)
        if (query.isEmpty()) return spannable
        try {
            var start = fullText.lowercase().indexOf(query.lowercase())
            while (start >= 0) {
                spannable.setSpan(BackgroundColorSpan(Color.YELLOW), start, start + query.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = fullText.lowercase().indexOf(query.lowercase(), start + query.length)
            }
        } catch (e: Exception) {}
        return spannable
    }

    private fun handleListItemClick(position: Int) {
        val selectedBook = filteredItems[position]
        val bookName = selectedBook.optString("name", "")
        val bookId = selectedBook.optString("bookid", "")
        val bookAuthor = selectedBook.optString("author", "")
        if (bookName == "সহীহ বুখারী") {
            startActivity(Intent(applicationContext, HadisActivity::class.java).apply { putExtra("sub", "সহীহ বুখারী"); putExtra("booklist", "bukhari") })
        } else if (bookName == "সহীহ মুসলিম") {
            startActivity(Intent(applicationContext, HadisviewActivity::class.java).apply { putExtra("name", "সহীহ মুসলিম"); putExtra("booklist", "muslim") })
        } else {
            AlertDialog.Builder(this).setTitle("নির্বাচন করুন").setItems(arrayOf("অধ্যায় ভিত্তিক কিতাব", "সম্পূর্ণ কিতাব")) { _, which ->
                val cls = if (which == 0) ReadingActivity::class.java else FullbookreadActivity::class.java
                startActivity(Intent(applicationContext, cls).apply { putExtra("name", bookName); putExtra("bookname", bookId); putExtra("author", bookAuthor) })
            }.show()
        }
    }

    inner class GlobalSearchAdapter(context: android.content.Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        override fun getCount(): Int { return list.size }
        override fun getItem(position: Int): JSONObject? { return list[position] }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context
            val item = list[position]
            val uid = item.optString("_unique_id", "$position")
            val isExpanded = expandedIds.contains(uid)
            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(dp(6), dp(6), dp(6), dp(6))
                isLongClickable = true
            }
            val bookHeader = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(10), dp(4), dp(10), dp(2)) }
                text = "📚 ${item.optString("bookName")}"
                textSize = 12f; setTextColor(Color.parseColor("#01837A")); setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(4), dp(2), dp(4), dp(8)) }
                setPadding(dp(14), dp(12), dp(14), dp(12)); elevation = dpF(4f); isLongClickable = true
                background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#E0F2F1")),
                    GradientDrawable().apply { setStroke(dp(1), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = dpF(12f) }, null)
            }
            val titleRaw = item.optString("title", ""); val contentRaw = item.optString("content", "")
            val displayContent = if (!isExpanded && contentRaw.length > 150) contentRaw.take(150) + "..." else contentRaw
            val titleTv = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) }
                textSize = 16f; setTextColor(Color.BLACK); setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
                text = if (lastQuery.isNotEmpty()) getHighlightedText(titleRaw, lastQuery) else titleRaw
            }
            val contentTv = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textSize = 14f; setTextColor(Color.BLACK)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
                text = if (lastQuery.isNotEmpty()) getHighlightedText(displayContent, lastQuery) else displayContent
            }
            val hintTv = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) }
                textSize = 11f; setTextColor(Color.parseColor("#FF5722")); gravity = Gravity.END
                text = if (isExpanded) "▲ ছোট করুন" else "▼ পুরো লেখা দেখুন"
            }
            val toolbar = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) }
            }
            val copyBtn = TextView(ctx).apply {
                text = "📋 কপি"; textSize = 11f; setTextColor(Color.parseColor("#01837A"))
                setPadding(dp(8), dp(4), dp(8), dp(4))
                background = GradientDrawable().apply { setColor(Color.parseColor("#E0F2F1")); cornerRadius = dpF(8f) }
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception) {}
            }
            val shareBtn = TextView(ctx).apply {
                text = "📤 শেয়ার"; textSize = 11f; setTextColor(Color.parseColor("#01837A"))
                setPadding(dp(8), dp(4), dp(8), dp(4))
                background = GradientDrawable().apply { setColor(Color.parseColor("#E0F2F1")); cornerRadius = dpF(8f) }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { leftMargin = dp(8) }
            }
            copyBtn.setOnClickListener {
                val full = "${item.optString("title")}\n\n${item.optString("content")}\n\n- ${item.optString("bookName")}"
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.text = full
                Toast.makeText(ctx, "কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
            shareBtn.setOnClickListener {
                val full = "${item.optString("title")}\n\n${item.optString("content")}\n\n- ${item.optString("bookName")}"
                val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, full) }
                ctx.startActivity(Intent.createChooser(share, "শেয়ার করুন"))
            }
            toolbar.addView(copyBtn); toolbar.addView(shareBtn)

            card.addView(titleTv); card.addView(contentTv); if (contentRaw.length > 150) card.addView(hintTv)
            card.addView(toolbar)
            root.addView(bookHeader); root.addView(card)

            card.setOnClickListener {
                if (expandedIds.contains(uid)) expandedIds.remove(uid) else expandedIds.add(uid); notifyDataSetChanged()
            }

            val longClickAction = View.OnLongClickListener {
                val bName = item.optString("bookName"); val bId = item.optString("bookid")
                val bAuthor = item.optString("bookAuthor"); val pos = item.optInt("pos", 0)
                AlertDialog.Builder(ctx).setTitle(bName).setMessage("এই লেখায় যেতে চান? পজিশন: ${pos + 1}")
               .setPositiveButton("হ্যাঁ, যান") { _, _ ->
                        ctx.startActivity(Intent(ctx, ReadingActivity::class.java).apply {
                            putExtra("name", bName); putExtra("bookname", bId); putExtra("author", bAuthor); putExtra("jumpTo", pos); putExtra("highlight_query", lastQuery)
                        })
                    }.setNegativeButton("বাতিল", null).show()
                true
            }
            card.setOnLongClickListener(longClickAction)
            root.setOnLongClickListener(longClickAction)

            return root
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
