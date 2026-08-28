package com.srizwan.islamipedia

import android.content.Intent
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
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
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
import java.io.IOException
import java.io.InputStream

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

    private lateinit var progressContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var fabGlobalSearch: FloatingActionButton

    private fun dp(i: Int): Int { return (i * resources.displayMetrics.density).toInt() }
    private fun dpF(f: Float): Float { return f * resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        select = findViewById(R.id.select)
        nores = findViewById(R.id.nores)
        boxofsubject = findViewById(R.id.boxofsubject)
        boxofauthor = findViewById(R.id.boxofauthor)
        searchboxauthor = findViewById(R.id.searchboxauthor)
        searchboxsubject = findViewById(R.id.searchboxsubject)
        searchboxauthor.isEnabled = false
        searchboxsubject.isEnabled = false
        boxofauthor.setOnClickListener {
            val getauthor = Intent(applicationContext, HadisActivity::class.java).apply {
                putExtra("sub", "ইসলামী বই সমাহার")
                putExtra("booklist", "file.json")
            }
            startActivity(getauthor)
        }
        val heading1: TextView = findViewById(R.id.heading1)
        heading1.text = intent.getStringExtra("sub")
        jump = findViewById(R.id.jump)
        jump.visibility = View.GONE
        back = findViewById(R.id.back)
        back.setOnClickListener {
            if(currentMode == Mode.GLOBAL_SEARCH) switchMode(Mode.BOOK_LIST, heading1) else finish()
        }

        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "বইয়ের বা লেখকের নাম লিখে সার্চ করুন"
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
            if(searchView.visibility == View.VISIBLE) searchbox.requestFocus()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (intent?.hasExtra("get") == true) {
                searchbox.text = Editable.Factory.getInstance().newEditable(intent.getStringExtra("get"))
                select.visibility = View.GONE
                searchtop.visibility = View.VISIBLE
            } else {
                searchbox.setText("")
                select.visibility = View.GONE
                searchtop.visibility = View.VISIBLE
            }
        }, 10)
        select.visibility = View.GONE

        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                lastQuery = query
                if(currentMode == Mode.GLOBAL_SEARCH){
                    if(query.length >= 2) performGlobalSearch(query)
                    else {
                        globalSearchVersion++
                        globalList.clear()
                        listView1.adapter = GlobalSearchAdapter(this@Main4Activity, globalList)
                        progressBar.progress = 0
                        progressText.text = "🔍 কমপক্ষে ২ অক্ষর লিখুন"
                        nores.visibility = View.GONE
                    }
                } else {
                    filterList(query)
                }
            }
        })
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            try {
                window.statusBarColor = Color.parseColor(getString(R.string.color))
                window.navigationBarColor = Color.parseColor(getString(R.string.color))
            } catch (e: Exception) {
                window.statusBarColor = Color.parseColor("#01837A")
                window.navigationBarColor = Color.parseColor("#01837A")
            }
        }
        listView1 = findViewById(R.id.listview1)

        val jsonArray = getJSonData(intent.getStringExtra("booklist")?: "")
        listItems = getArrayListFromJSONArray(jsonArray)
        filteredItems = ArrayList(listItems)
        val adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true

        listView1.setOnItemClickListener { _, _, position, _ ->
            if(currentMode == Mode.BOOK_LIST) handleListItemClick(position)
        }

        addFabAndProgressOverlay()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(currentMode == Mode.GLOBAL_SEARCH){
                    if (searchView.visibility == View.VISIBLE && searchbox.text.toString().isEmpty()) {
                        switchMode(Mode.BOOK_LIST, heading1)
                    } else if (searchView.visibility == View.VISIBLE) {
                        searchbox.text.clear()
                    } else {
                        switchMode(Mode.BOOK_LIST, heading1)
                    }
                    return
                }
                if (searchView.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) {
                        searchView.visibility = View.GONE
                    } else {
                        searchbox.text.clear()
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun addFabAndProgressOverlay(){
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val frame = rootView.getChildAt(0) as? ViewGroup?: return

        progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setPadding(dp(12), dp(8), dp(12), dp(8))
            val bg = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dpF(20f); setStroke(dp(1), Color.parseColor("#01837A")) }
            background = bg
            elevation = dpF(4f)
        }
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(6), 1f)
            max = 100
            progressTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
        }
        progressText = TextView(this).apply {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.leftMargin = dp(8)
            layoutParams = lp
            text = "⏳ সার্চ চলছে..."
            textSize = 12f
            setTextColor(Color.parseColor("#607D8B"))
        }
        progressContainer.addView(progressBar)
        progressContainer.addView(progressText)
        val progressLp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(125); leftMargin = dp(12); rightMargin = dp(12); gravity = Gravity.TOP
        }
        frame.addView(progressContainer, progressLp)

        fabGlobalSearch = FloatingActionButton(this).apply {
            setImageResource(R.drawable.searchme)
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#01837A"))
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            size = FloatingActionButton.SIZE_NORMAL
        }
        val fabLp = FrameLayout.LayoutParams(dp(56), dp(56)).apply {
            gravity = Gravity.BOTTOM or Gravity.END; bottomMargin = dp(20); rightMargin = dp(16)
        }
        frame.addView(fabGlobalSearch, fabLp)

        fabGlobalSearch.setOnClickListener {
            val heading1: TextView = findViewById(R.id.heading1)
            switchMode(Mode.GLOBAL_SEARCH, heading1)
        }
    }

    private fun switchMode(mode: Mode, heading1: TextView? = null){
        currentMode = mode
        val heading = heading1?: findViewById<TextView>(R.id.heading1)
        when(mode){
            Mode.BOOK_LIST -> {
                heading.text = intent.getStringExtra("sub")?: "ইসলামী বই সমাহার"
                boxofsearch.hint = "বইয়ের বা লেখকের নাম লিখে সার্চ করুন"
                progressContainer.visibility = View.GONE
                fabGlobalSearch.visibility = View.VISIBLE
                nores.visibility = View.GONE
                searchView.visibility = View.GONE
                searchbox.text.clear()
                listView1.adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
            }
            Mode.GLOBAL_SEARCH -> {
                heading.text = "গ্লোবাল সার্চ"
                boxofsearch.hint = "সব কিতাবের ভিতরে সার্চ করুন"
                searchView.visibility = View.VISIBLE
                searchbox.requestFocus()
                fabGlobalSearch.visibility = View.GONE
                progressContainer.visibility = View.VISIBLE
                globalSearchVersion++
                globalList.clear()
                listView1.adapter = GlobalSearchAdapter(this, globalList)
                progressBar.progress = 0
                progressText.text = "🔍 কমপক্ষে ২ অক্ষর লিখুন"
            }
        }
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
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonArray
    }

    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        try {
            if (jsonArray!= null) {
                name = Array(jsonArray.length()) { "" }
                author = Array(jsonArray.length()) { "" }
                bookid = Array(jsonArray.length()) { "" }
                subject = Array(jsonArray.length()) { "" }
                allBookIds.clear()
                bookInfoMap.clear()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    jsonObject.put("original_index", i)
                    aList.add(jsonObject)
                    name[i] = jsonObject.getString("name")
                    author[i] = jsonObject.getString("author")
                    bookid[i] = jsonObject.getString("bookid")
                    subject[i] = jsonObject.getString("subject")
                    allBookIds.add(jsonObject.getString("bookid"))
                    bookInfoMap[jsonObject.getString("bookid")] = jsonObject
                }
            }
        } catch (je: JSONException) {
            je.printStackTrace()
        }
        return aList
    }

    private fun filterList(query: String) {
        filteredItems.clear()
        for (i in name.indices) {
            if (name[i].contains(query, ignoreCase = true) || author[i].contains(query, ignoreCase = true) || subject[i].contains(query, ignoreCase = true)) {
                filteredItems.add(listItems[i])
                nores.visibility = View.GONE
            }
        }
        if (filteredItems.isEmpty()) {
            nores.visibility = View.VISIBLE
        }
        val adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
        listView1.adapter = adapter
    }

    private fun readBookFile(bookId: String): String? {
        val paths = listOf("books/$bookId", bookId, "books/$bookId.json", "$bookId.json")
        for(p in paths){
            try{
                val input = assets.open(p)
                val str = String(input.readBytes(), Charsets.UTF_8)
                input.close()
                return str
            } catch (e: Exception){ continue }
        }
        return null
    }

    private fun performGlobalSearch(query: String){
        lastQuery = query
        globalSearchVersion++
        val myVersion = globalSearchVersion
        val searchQuery = query.trim()
        if(searchQuery.length < 2) return

        globalList.clear()
        listView1.adapter = GlobalSearchAdapter(this, ArrayList())
        progressBar.progress = 0
        progressText.text = "⏳ সার্চ চলছে..."
        nores.visibility = View.GONE

        Thread {
            val localResults = ArrayList<JSONObject>()
            var scanned = 0
            val total = allBookIds.size
            for (bId in allBookIds){
                if(myVersion!= globalSearchVersion) return@Thread
                scanned++
                try{
                    val jsonStr = readBookFile(bId)?: continue
                    val arr = JSONArray(jsonStr)
                    for(j in 0 until arr.length()){
                        if(myVersion!= globalSearchVersion) return@Thread
                        val obj = arr.getJSONObject(j)
                        val title = obj.optString("1","")
                        val content = obj.optString("2","")
                        if((title + " " + content).contains(searchQuery, true)){
                            val bookInfo = bookInfoMap[bId]
                            val newObj = JSONObject()
                            newObj.put("title", title)
                            newObj.put("content", content)
                            newObj.put("bookName", bookInfo?.optString("name")?: bId)
                            newObj.put("bookAuthor", bookInfo?.optString("author")?: "")
                            newObj.put("bookid", bId)
                            newObj.put("_unique_id", "${bId}_$j")
                            newObj.put("pos", j)
                            localResults.add(newObj)
                        }
                    }
                } catch (e: Exception){}
                val sc = scanned
                val fc = localResults.size
                runOnUiThread {
                    if(myVersion!= globalSearchVersion || lastQuery!= query) return@runOnUiThread
                    progressBar.progress = sc * 100 / total
                    progressText.text = "⏳ $sc/$total কিতাব - $fc টি পাওয়া গেছে"
                }
            }
            runOnUiThread {
                if(myVersion!= globalSearchVersion || lastQuery!= query) return@runOnUiThread
                globalList.clear()
                globalList.addAll(localResults)
                progressBar.progress = 100
                progressText.text = "✅ ${globalList.size} টি রেজাল্ট"
                nores.visibility = if(globalList.isEmpty()) View.VISIBLE else View.GONE
                listView1.adapter = GlobalSearchAdapter(this, ArrayList(globalList))
            }
        }.start()
    }

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

    private fun handleListItemClick(position: Int) {
        val selectedBook = filteredItems[position]
        val bookName = selectedBook.getString("name")
        val bookId = selectedBook.getString("bookid")
        val bookAuthor = selectedBook.getString("author")
        if (bookName == "সহীহ বুখারী") {
            val hadisjson = Intent(applicationContext, HadisActivity::class.java).apply {
                putExtra("sub", "সহীহ বুখারী")
                putExtra("booklist", "bukhari")
            }
            startActivity(hadisjson)
        } else {
            if (bookName == "সহীহ মুসলিম") {
                val hadisjson = Intent(applicationContext, HadisviewActivity::class.java).apply {
                    putExtra("name", "সহীহ মুসলিম")
                    putExtra("booklist", "muslim")
                }
                startActivity(hadisjson)
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("নির্বাচন করুন")
                val options = arrayOf("অধ্যায় ভিত্তিক কিতাব", "সম্পূর্ণ কিতাব")
                builder.setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val json = Intent(applicationContext, ReadingActivity::class.java).apply {
                                putExtra("name", bookName)
                                putExtra("bookname", bookId)
                                putExtra("author", bookAuthor)
                            }
                            startActivity(json)
                        }
                        1 -> {
                            val json = Intent(applicationContext, FullbookreadActivity::class.java).apply {
                                putExtra("name", bookName)
                                putExtra("bookname", bookId)
                                putExtra("author", bookAuthor)
                            }
                            startActivity(json)
                        }
                    }
                }
                builder.create().show()
            }
        }
    }

    inner class GlobalSearchAdapter(context: android.content.Context, private val list: ArrayList<JSONObject>) : android.widget.ArrayAdapter<JSONObject>(context, 0, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val ctx = context
            val item = list[position]
            val uid = item.optString("_unique_id", "$position")
            val isExpanded = expandedIds.contains(uid)

            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = android.widget.AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(dp(6), dp(6), dp(6), dp(6))
                setBackgroundColor(Color.TRANSPARENT)
            }

            val bookHeader = TextView(ctx).apply {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(10), dp(4), dp(10), dp(2))
                layoutParams = lp
                text = "📚 ${item.optString("bookName")}"
                textSize = 12f
                setTextColor(Color.parseColor("#01837A"))
                setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception){}
            }

            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(dp(4), dp(2), dp(4), dp(8))
                layoutParams = lp
                setPadding(dp(14), dp(12), dp(14), dp(12))
                elevation = dpF(4f)
                val drawable = GradientDrawable().apply { setStroke(dp(1), Color.parseColor("#01837A")); setColor(Color.WHITE); cornerRadius = dpF(12f) }
                background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#E0F2F1")), drawable, null)
            }

            val titleRaw = item.optString("title","")
            val contentRaw = item.optString("content","")
            val displayContent = if(!isExpanded && contentRaw.length > 150) contentRaw.take(150) + "..." else contentRaw

            val titleTv = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) }
                textSize = 16f; setTextColor(Color.BLACK); setTypeface(null, Typeface.BOLD)
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception){}
                text = if(lastQuery.isNotEmpty()) getHighlightedText(titleRaw, lastQuery) else titleRaw
            }

            val contentTv = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textSize = 14f; setTextColor(Color.parseColor("#333333"))
                try { typeface = ResourcesCompat.getFont(ctx, R.font.solaimanlipi) } catch (e: Exception){}
                text = if(lastQuery.isNotEmpty()) getHighlightedText(displayContent, lastQuery) else displayContent
            }

            val hintTv = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) }
                textSize = 11f; setTextColor(Color.parseColor("#FF5722")); gravity = Gravity.END
                text = if(isExpanded) "▲ ছোট করুন" else "▼ পুরো লেখা দেখুন"
            }

            card.addView(titleTv)
            card.addView(contentTv)
            if(contentRaw.length > 150) card.addView(hintTv)
            root.addView(bookHeader)
            root.addView(card)

            card.setOnClickListener {
                if(expandedIds.contains(uid)) expandedIds.remove(uid) else expandedIds.add(uid)
                notifyDataSetChanged()
            }

            root.setOnLongClickListener {
                val bName = item.optString("bookName")
                val bId = item.optString("bookid")
                val bAuthor = item.optString("bookAuthor")
                val pos = item.optInt("pos", 0)
                AlertDialog.Builder(ctx).setTitle(bName).setMessage("এই লেখায় যেতে চান?\nপজিশন: ${pos+1}")
                   .setPositiveButton("হ্যাঁ, যান"){_,_->
                        ctx.startActivity(Intent(ctx, ReadingActivity::class.java).apply {
                            putExtra("name", bName); putExtra("bookname", bId); putExtra("author", bAuthor)
                            putExtra("jumpTo", pos); putExtra("highlight_query", lastQuery)
                        })
                    }.setNegativeButton("বাতিল", null).show()
                true
            }
            return root
        }
    }
}
