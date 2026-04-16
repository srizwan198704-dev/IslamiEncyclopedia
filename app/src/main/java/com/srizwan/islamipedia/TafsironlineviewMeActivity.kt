package com.srizwan.islamipedia

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TafsironlineviewMeActivity : AppCompatActivity() {

    private var getsearch: String = ""
    private var n: Int = 0

    private val map = ArrayList<HashMap<String, Any>>()
    private val chapter = ArrayList<HashMap<String, Any>>()
    private val listmap_cache = ArrayList<HashMap<String, Any>>()

    // Views - Toolbar
    private lateinit var toolbar: LinearLayout
    private lateinit var list: ImageView
    private lateinit var box: LinearLayout
    private lateinit var LinearLayout1: LinearLayout
    private lateinit var bookname: TextView
    private lateinit var author: TextView
    private lateinit var searchimg: ImageView

    // Views - Spin / No Internet
    private lateinit var spin: LinearLayout
    private lateinit var spinber: ProgressBar
    private lateinit var Nointernet: LinearLayout
    private lateinit var imageview3: ImageView
    private lateinit var textview1: TextView
    private lateinit var materialbutton1: MaterialButton

    // Views - Content
    private lateinit var content: LinearLayout
    private lateinit var searxhmain: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var imageview2: ImageView
    private lateinit var searchbox: EditText
    private lateinit var recyclerView1: RecyclerView
    private lateinit var nores: LinearLayout
    private lateinit var noresult: ImageView
    private lateinit var no_result: TextView

    private lateinit var book: RequestNetwork
    private val deleted = AlertDialog.Builder(this)
    private val onlineoffline = AlertDialog.Builder(this)

    private val bookRequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
            val cachePath = FileUtil.getPackageDataDir(applicationContext)
                .plus("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র")

            if (FileUtil.isExistFile(cachePath)) {
                try {
                    loadFromCache(cachePath)
                } catch (e: Exception) { /* ignore */ }
            } else {
                try {
                    if (!FileUtil.isExistFile(cachePath)) {
                        val parsed: ArrayList<HashMap<String, Any>> = Gson().fromJson(
                            response,
                            object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
                        )
                        n = 0
                        parsed.forEach { item ->
                            if (item["sura"].toString() == intent.getStringExtra("sura")) {
                                chapter.add(buildListMap(item))
                            }
                            n++
                        }
                        recyclerView1.adapter = TafsirAdapter(chapter)
                        getsearch = Gson().toJson(chapter)
                        searchimg.visibility = View.VISIBLE
                    }
                } catch (e: Exception) { /* ignore */ }
            }

            if (chapter.isEmpty()) {
                spin.visibility = View.VISIBLE
                content.visibility = View.GONE
                searchimg.visibility = View.GONE
            } else {
                spin.visibility = View.GONE
                content.visibility = View.VISIBLE
                searchimg.visibility = View.VISIBLE
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            val cachePath = FileUtil.getPackageDataDir(applicationContext)
                .plus("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র")

            if (FileUtil.isExistFile(cachePath)) {
                try {
                    loadFromCache(cachePath)
                } catch (e: Exception) { /* ignore */ }
            } else {
                if (!FileUtil.isExistFile(cachePath)) {
                    Toast.makeText(applicationContext, "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show()
                    spinber.visibility = View.GONE
                    Nointernet.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        initialize()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1000
            )
        } else {
            initializeLogic()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            initializeLogic()
        }
    }

    // ──────────────────────────────────────────────
    // Build entire UI programmatically (no XML)
    // ──────────────────────────────────────────────
    private fun buildUI() {
        val dp = resources.displayMetrics.density

        // ── Root ──────────────────────────────────
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundResource(R.drawable.back1ground)
            fitsSystemWindows = true
        }

        // ── Toolbar ───────────────────────────────
        toolbar = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#01837A"))
            elevation = 5 * dp
            setPadding((3 * dp).toInt(), (1 * dp).toInt(), (3 * dp).toInt(), (1 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (60 * dp).toInt()
            )
        }

        list = ImageView(this).apply {
            setImageResource(R.drawable.ic_arrow_back_white)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding((10 * dp).toInt(), (10 * dp).toInt(), (10 * dp).toInt(), (10 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams((40 * dp).toInt(), (40 * dp).toInt()).apply {
                marginEnd = (5 * dp).toInt()
            }
        }

        box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        LinearLayout1 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        bookname = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            setSingleLine(true)
            typeface = try { resources.getFont(R.font.solaimanlipi) } catch (e: Exception) { Typeface.DEFAULT }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { marginEnd = (5 * dp).toInt(); topMargin = (3 * dp).toInt() }
        }

        author = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            setSingleLine(true)
            typeface = try { resources.getFont(R.font.solaimanlipi) } catch (e: Exception) { Typeface.DEFAULT }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { marginEnd = (5 * dp).toInt(); topMargin = (3 * dp).toInt() }
        }

        searchimg = ImageView(this).apply {
            setImageResource(R.drawable.searchme)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams((30 * dp).toInt(), LinearLayout.LayoutParams.MATCH_PARENT)
        }

        LinearLayout1.addView(bookname)
        LinearLayout1.addView(author)
        box.addView(LinearLayout1)
        box.addView(searchimg)
        toolbar.addView(list)
        toolbar.addView(box)

        // ── Spin / Loading ────────────────────────
        spin = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        spinber = ProgressBar(this, null, android.R.attr.progressBarStyle).apply {
            isIndeterminate = true
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        Nointernet = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        imageview3 = ImageView(this).apply {
            setImageResource(R.drawable.nointernet)
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        textview1 = TextView(this).apply {
            text = "ইন্টারনেট সেটিং চেক করুন"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            typeface = try { resources.getFont(R.font.solaimanlipi) } catch (e: Exception) { Typeface.DEFAULT }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        materialbutton1 = MaterialButton(this).apply {
            text = "রিফ্রেশ করুন"
            textSize = 12f
            setTextColor(Color.WHITE)
            cornerRadius = (8 * dp).toInt()
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#6200EE"))
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        Nointernet.addView(imageview3)
        Nointernet.addView(textview1)
        Nointernet.addView(materialbutton1)
        spin.addView(spinber)
        spin.addView(Nointernet)

        // ── Content ───────────────────────────────
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Search bar
        searxhmain = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        boxofsearch = TextInputLayout(
            this, null,
            com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox
        ).apply {
            setPadding((5 * dp).toInt(), (5 * dp).toInt(), (5 * dp).toInt(), (5 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins((5 * dp).toInt(), (5 * dp).toInt(), (5 * dp).toInt(), (5 * dp).toInt())
            }
        }

        searchbox = EditText(this).apply {
            textSize = 14f
            setTextColor(Color.BLACK)
            hint = "শব্দ লিখে সার্চ করুন"
            setHintTextColor(Color.parseColor("#6200EE"))
            typeface = try { resources.getFont(R.font.solaimanlipi) } catch (e: Exception) { Typeface.DEFAULT }
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        imageview2 = ImageView(this).apply {
            setImageResource(R.drawable.cancel)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams((30 * dp).toInt(), LinearLayout.LayoutParams.MATCH_PARENT).apply {
                marginEnd = (5 * dp).toInt()
            }
        }

        boxofsearch.addView(searchbox)
        searxhmain.addView(boxofsearch)
        searxhmain.addView(imageview2)

        // RecyclerView
        recyclerView1 = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@TafsironlineviewMeActivity)
            setBackgroundResource(R.drawable.back1ground)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // No result
        nores = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        noresult = ImageView(this).apply {
            setImageResource(R.drawable.noresult)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams((100 * dp).toInt(), (100 * dp).toInt())
        }

        no_result = TextView(this).apply {
            text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            typeface = try { resources.getFont(R.font.solaimanlipi) } catch (e: Exception) { Typeface.DEFAULT }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        nores.addView(noresult)
        nores.addView(no_result)

        content.addView(searxhmain)
        content.addView(recyclerView1)
        content.addView(nores)

        // ── Assemble root ─────────────────────────
        root.addView(toolbar)
        root.addView(spin)
        root.addView(content)

        setContentView(root)
    }

    // ──────────────────────────────────────────────
    // Initialize listeners
    // ──────────────────────────────────────────────
    private fun initialize() {
        book = RequestNetwork(this)

        list.setOnClickListener { finish() }

        searchimg.setOnClickListener {
            searxhmain.visibility = if (searxhmain.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        materialbutton1.setOnClickListener {
            book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "", bookRequestListener)
            spinber.visibility = View.VISIBLE
            Nointernet.visibility = View.GONE
        }

        imageview2.setOnClickListener {
            if (searchbox.text.toString().isEmpty()) {
                searxhmain.visibility = View.GONE
            } else {
                searchbox.setText("")
            }
        }

        searchbox.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val charSeq = s.toString()
                jsonSearch(charSeq)
                if (chapter.isEmpty()) {
                    nores.visibility = View.VISIBLE
                    recyclerView1.visibility = View.GONE
                } else {
                    nores.visibility = View.GONE
                    recyclerView1.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ──────────────────────────────────────────────
    // Business logic
    // ──────────────────────────────────────────────
    private fun initializeLogic() {
        setStatusBarColor("#FF01837A", "#FF01837A")
        marquee(bookname, intent.getStringExtra("name") ?: "")
        marquee(author, intent.getStringExtra("author") ?: "")

        val dp = resources.displayMetrics.density
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()

        Nointernet.visibility = View.GONE
        searxhmain.visibility = View.GONE
        searchimg.visibility = View.GONE
        nores.visibility = View.GONE

        if (chapter.isEmpty()) {
            spin.visibility = View.VISIBLE
            content.visibility = View.GONE
            searchimg.visibility = View.GONE
        } else {
            content.visibility = View.VISIBLE
            spin.visibility = View.GONE
            searchimg.visibility = View.VISIBLE
        }

        val cachePath = FileUtil.getPackageDataDir(applicationContext)
            .plus("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র")

        if (FileUtil.isExistFile(cachePath)) {
            try {
                loadFromCache(cachePath)
                spin.visibility = View.GONE
                content.visibility = View.VISIBLE
                Nointernet.visibility = View.GONE
                searchimg.visibility = View.VISIBLE
            } catch (e: Exception) { /* ignore */ }
        } else {
            if (!FileUtil.isExistFile(cachePath)) {
                FileUtil.makeDir(
                    FileUtil.getPackageDataDir(applicationContext)
                        .plus("/" + "/ইসলামী বিশ্বকোষ/.অনলাইন বই ২/")
                )
                if (Rizwan.isConnected(applicationContext)) {
                    book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "", bookRequestListener)
                } else {
                    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork = cm.activeNetworkInfo
                    if (activeNetwork == null || !activeNetwork.isConnected) {
                        Nointernet.visibility = View.VISIBLE
                        Toast.makeText(applicationContext, "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show()
                    }
                    if (FileUtil.isExistFile(cachePath)) {
                        spin.visibility = View.GONE
                        content.visibility = View.VISIBLE
                        Nointernet.visibility = View.GONE
                    } else {
                        Toast.makeText(applicationContext, "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                        spin.visibility = View.VISIBLE
                        content.visibility = View.GONE
                        Nointernet.visibility = View.VISIBLE
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searxhmain.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) {
                        searxhmain.visibility = View.GONE
                    } else {
                        searchbox.setText("")
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun loadFromCache(cachePath: String) {
        val cached: ArrayList<HashMap<String, Any>> = Gson().fromJson(
            FileUtil.readFile(cachePath),
            object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
        )
        n = 0
        cached.forEach { item ->
            if (item["sura"].toString() == intent.getStringExtra("sura")) {
                chapter.add(buildListMap(item))
            }
            n++
        }
        recyclerView1.adapter = TafsirAdapter(chapter)
        getsearch = Gson().toJson(chapter)
        searchimg.visibility = View.VISIBLE
    }

    private fun buildListMap(item: HashMap<String, Any>): HashMap<String, Any> {
        return hashMapOf(
            "verses" to item["verses"].toString(),
            "names" to item["names"].toString(),
            "words" to item["words"].toString(),
            "name" to item["name"].toString(),
            "khazainul" to item["khazainul"].toString(),
            "irfanul" to item["irfanul"].toString(),
            "ibnabbas" to item["ibnabbas"].toString(),
            "majhari" to item["majhari"].toString(),
            "nurulirfan" to item["nurulirfan"].toString(),
            "tabari" to item["tabari"].toString(),
            "ibnkasir" to item["ibnkasir"].toString(),
            "rejviya" to item["rejviya"].toString(),
            "baizabi" to item["baizabi"].toString(),
            "kurtubi" to item["kurtubi"].toString()
        )
    }

    // ──────────────────────────────────────────────
    // Utility helpers
    // ──────────────────────────────────────────────
    fun marquee(text: TextView, texto: String) {
        text.text = texto
        text.ellipsize = TextUtils.TruncateAt.MARQUEE
        text.isSelected = true
        text.marqueeRepeatLimit = -1
        text.isSingleLine = true
        text.isFocusable = true
        text.isFocusableInTouchMode = true
    }

    fun setStatusBarColor(colour1: String, colour2: String) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val w: Window = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = Color.parseColor(colour1)
            w.navigationBarColor = Color.parseColor(colour2)
        }
    }

    fun jsonSearch(charSeq: String) {
        val tempChapter: ArrayList<HashMap<String, Any>> = Gson().fromJson(
            getsearch,
            object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
        )
        val length = tempChapter.size
        var r = length - 1
        repeat(length) {
            val value1 = tempChapter[r]["name"].toString()
            val value2 = tempChapter[r]["irfanul"].toString()
            val matchesName = charSeq.length <= value1.length && value1.lowercase().contains(charSeq.lowercase())
            val matchesIrfanul = charSeq.length <= value2.length && value2.lowercase().contains(charSeq.lowercase())
            if (!matchesName && !matchesIrfanul) {
                tempChapter.removeAt(r)
            }
            r--
        }
        chapter.clear()
        chapter.addAll(tempChapter)
        recyclerView1.adapter = TafsirAdapter(chapter)
    }

    fun replaceArabicNumber(n: String): String {
        return n.replace("1", "১").replace("2", "২").replace("3", "৩")
            .replace("4", "৪").replace("5", "৫").replace("6", "৬")
            .replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০")
    }

    fun enableCopyTextView(tv: TextView) {
        tv.setTextIsSelectable(true)
    }

    // ──────────────────────────────────────────────
    // RecyclerView Adapter (replaces ListView adapter)
    // ──────────────────────────────────────────────
    inner class TafsirAdapter(
        private val data: ArrayList<HashMap<String, Any>>
    ) : RecyclerView.Adapter<TafsirAdapter.TafsirViewHolder>() {

        inner class TafsirViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // Card container
            val main: LinearLayout = itemView.findViewById(android.R.id.content) as? LinearLayout
                ?: itemView as LinearLayout

            // Header row
            lateinit var linear2: LinearLayout
            lateinit var linear3: LinearLayout
            lateinit var number: TextView

            // Arabic + words
            lateinit var ayaarabic: TextView
            lateinit var words: TextView

            // Each tafsir section: header + body
            lateinit var maintafsiribnabbas: LinearLayout
            lateinit var headingtafsiribnabbas: TextView
            lateinit var texttafsiribnabbas: TextView

            lateinit var mainkanzulimaanlayout: LinearLayout
            lateinit var headingkanzulimaan: TextView
            lateinit var textkanzuliman: TextView

            lateinit var mainkhazainulirfan: LinearLayout
            lateinit var headingkhazainulirfan: TextView
            lateinit var texttafsirkhazainulirfan: TextView

            lateinit var maintafsirnurulirfan: LinearLayout
            lateinit var headingtafsirnurulirfan: TextView
            lateinit var texttafsirnurulirfan: TextView

            lateinit var mainirfanullayout: LinearLayout
            lateinit var headingirfanulkuran: TextView
            lateinit var textifranulkuran: TextView

            lateinit var maintafsirtabari: LinearLayout
            lateinit var headingtafsirtabari: TextView
            lateinit var texttafsirtabari: TextView

            lateinit var maintafsirmajhari: LinearLayout
            lateinit var headingtafsirmajhari: TextView
            lateinit var texttafsirmajhari: TextView

            lateinit var maintafsiribnkasir: LinearLayout
            lateinit var headingtafsiribnkasir: TextView
            lateinit var texttafsiribnkasir: TextView

            lateinit var maintafsirkurtubi: LinearLayout
            lateinit var headingtafsirkurtubi: TextView
            lateinit var texttafsirkurtubi: TextView

            lateinit var maintafsirbaizabi: LinearLayout
            lateinit var headingtafsirbaizabi: TextView
            lateinit var texttafsirbaizabi: TextView

            lateinit var maintafsirrezbiya: LinearLayout
            lateinit var headingtafsirrezbiya: TextView
            lateinit var texttafsirrezviya: TextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TafsirViewHolder {
            val itemView = buildItemView(parent.context)
            val holder = TafsirViewHolder(itemView)
            // Bind all sub-views by tag
            holder.linear2 = itemView.findViewWithTag("linear2")
            holder.linear3 = itemView.findViewWithTag("linear3")
            holder.number = itemView.findViewWithTag("number")
            holder.ayaarabic = itemView.findViewWithTag("ayaarabic")
            holder.words = itemView.findViewWithTag("words")
            holder.maintafsiribnabbas = itemView.findViewWithTag("maintafsiribnabbas")
            holder.headingtafsiribnabbas = itemView.findViewWithTag("headingtafsiribnabbas")
            holder.texttafsiribnabbas = itemView.findViewWithTag("texttafsiribnabbas")
            holder.mainkanzulimaanlayout = itemView.findViewWithTag("mainkanzulimaanlayout")
            holder.headingkanzulimaan = itemView.findViewWithTag("headingkanzulimaan")
            holder.textkanzuliman = itemView.findViewWithTag("textkanzuliman")
            holder.mainkhazainulirfan = itemView.findViewWithTag("mainkhazainulirfan")
            holder.headingkhazainulirfan = itemView.findViewWithTag("headingkhazainulirfan")
            holder.texttafsirkhazainulirfan = itemView.findViewWithTag("texttafsirkhazainulirfan")
            holder.maintafsirnurulirfan = itemView.findViewWithTag("maintafsirnurulirfan")
            holder.headingtafsirnurulirfan = itemView.findViewWithTag("headingtafsirnurulirfan")
            holder.texttafsirnurulirfan = itemView.findViewWithTag("texttafsirnurulirfan")
            holder.mainirfanullayout = itemView.findViewWithTag("mainirfanullayout")
            holder.headingirfanulkuran = itemView.findViewWithTag("headingirfanulkuran")
            holder.textifranulkuran = itemView.findViewWithTag("textifranulkuran")
            holder.maintafsirtabari = itemView.findViewWithTag("maintafsirtabari")
            holder.headingtafsirtabari = itemView.findViewWithTag("headingtafsirtabari")
            holder.texttafsirtabari = itemView.findViewWithTag("texttafsirtabari")
            holder.maintafsirmajhari = itemView.findViewWithTag("maintafsirmajhari")
            holder.headingtafsirmajhari = itemView.findViewWithTag("headingtafsirmajhari")
            holder.texttafsirmajhari = itemView.findViewWithTag("texttafsirmajhari")
            holder.maintafsiribnkasir = itemView.findViewWithTag("maintafsiribnkasir")
            holder.headingtafsiribnkasir = itemView.findViewWithTag("headingtafsiribnkasir")
            holder.texttafsiribnkasir = itemView.findViewWithTag("texttafsiribnkasir")
            holder.maintafsirkurtubi = itemView.findViewWithTag("maintafsirkurtubi")
            holder.headingtafsirkurtubi = itemView.findViewWithTag("headingtafsirkurtubi")
            holder.texttafsirkurtubi = itemView.findViewWithTag("texttafsirkurtubi")
            holder.maintafsirbaizabi = itemView.findViewWithTag("maintafsirbaizabi")
            holder.headingtafsirbaizabi = itemView.findViewWithTag("headingtafsirbaizabi")
            holder.texttafsirbaizabi = itemView.findViewWithTag("texttafsirbaizabi")
            holder.maintafsirrezbiya = itemView.findViewWithTag("maintafsirrezbiya")
            holder.headingtafsirrezbiya = itemView.findViewWithTag("headingtafsirrezbiya")
            holder.texttafsirrezviya = itemView.findViewWithTag("texttafsirrezviya")
            return holder
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: TafsirViewHolder, position: Int) {
            val item = data[position]
            val dp = holder.itemView.context.resources.displayMetrics.density

            // Card background
            val sketchUi = GradientDrawable().apply {
                setColor(0xFFFFFFFF.toInt())
                cornerRadius = dp * 20
                setStroke((dp * 1).toInt(), 0xFF01837A.toInt())
            }
            val main = holder.itemView as LinearLayout  // outer wrapper - apply to inner card
            // Apply to the scrollview's child (main LinearLayout tagged "main")
            val mainCard: LinearLayout = holder.itemView.findViewWithTag("main")
            mainCard.elevation = dp * 5
            val ripple = RippleDrawable(
                ColorStateList(arrayOf(intArrayOf()), intArrayOf(0xFF01837A.toInt())),
                sketchUi, null
            )
            mainCard.background = ripple

            try {
                if (item.containsKey("verses")) {
                    holder.number.text = replaceArabicNumber(item["verses"].toString())
                    holder.ayaarabic.text = item["names"].toString()
                    holder.words.text = item["words"].toString()
                    holder.textkanzuliman.text = replaceArabicNumber(
                        item["verses"].toString() + ". " + item["name"].toString()
                    )
                    holder.texttafsirkhazainulirfan.text = item["khazainul"].toString()
                    holder.textifranulkuran.text = item["irfanul"].toString()
                    holder.texttafsiribnabbas.text = item["ibnabbas"].toString()
                    holder.texttafsirmajhari.text = item["majhari"].toString()
                    holder.texttafsirnurulirfan.text = item["nurulirfan"].toString()
                    holder.texttafsirtabari.text = item["tabari"].toString()
                    holder.texttafsiribnkasir.text = item["ibnkasir"].toString()
                    holder.texttafsirkurtubi.text = item["kurtubi"].toString()
                    holder.texttafsirrezviya.text = item["rejviya"].toString()
                    holder.texttafsirbaizabi.text = item["baizabi"].toString()
                }
            } catch (e: Exception) { /* ignore */ }

            // Collapse all bodies initially
            holder.texttafsiribnabbas.visibility = View.GONE
            holder.texttafsirkhazainulirfan.visibility = View.GONE
            holder.texttafsirnurulirfan.visibility = View.GONE
            holder.texttafsirtabari.visibility = View.GONE
            holder.texttafsirmajhari.visibility = View.GONE
            holder.texttafsiribnkasir.visibility = View.GONE
            holder.texttafsirkurtubi.visibility = View.GONE
            holder.texttafsirbaizabi.visibility = View.GONE
            holder.texttafsirrezviya.visibility = View.GONE

            // Toggle listeners
            fun toggle(tv: TextView) {
                tv.visibility = if (tv.visibility == View.GONE) View.VISIBLE else View.GONE
            }
            holder.headingtafsiribnabbas.setOnClickListener { toggle(holder.texttafsiribnabbas) }
            holder.headingkhazainulirfan.setOnClickListener { toggle(holder.texttafsirkhazainulirfan) }
            holder.headingtafsirnurulirfan.setOnClickListener { toggle(holder.texttafsirnurulirfan) }
            holder.headingtafsirtabari.setOnClickListener { toggle(holder.texttafsirtabari) }
            holder.headingtafsirmajhari.setOnClickListener { toggle(holder.texttafsirmajhari) }
            holder.headingtafsirkurtubi.setOnClickListener { toggle(holder.texttafsirkurtubi) }
            holder.headingtafsiribnkasir.setOnClickListener { toggle(holder.texttafsiribnkasir) }
            holder.headingtafsirbaizabi.setOnClickListener { toggle(holder.texttafsirbaizabi) }
            holder.headingtafsirrezbiya.setOnClickListener { toggle(holder.texttafsirrezviya) }
            holder.headingkanzulimaan.setOnClickListener { toggle(holder.textkanzuliman) }
            holder.headingirfanulkuran.setOnClickListener { toggle(holder.textifranulkuran) }

            // Hide sections with placeholder text
            val NOT_ADDED = "তাফসির যুক্ত করা হয়নি"
            holder.maintafsiribnabbas.visibility = if (NOT_ADDED == holder.texttafsiribnabbas.text.toString()) View.GONE else View.VISIBLE
            holder.mainkhazainulirfan.visibility = if (NOT_ADDED == holder.texttafsirkhazainulirfan.text.toString()) View.GONE else View.VISIBLE
            holder.maintafsirnurulirfan.visibility = if (NOT_ADDED == holder.texttafsirnurulirfan.text.toString()) View.GONE else View.VISIBLE
            holder.maintafsirtabari.visibility = if (NOT_ADDED == holder.texttafsirtabari.text.toString()) View.GONE else View.VISIBLE
            holder.maintafsirmajhari.visibility = if (NOT_ADDED == holder.texttafsirmajhari.text.toString()) View.GONE else View.VISIBLE
            holder.maintafsiribnkasir.visibility = if (NOT_ADDED == holder.texttafsiribnkasir.text.toString()) View.GONE else View.VISIBLE
            holder.maintafsirkurtubi.visibility = if (NOT_ADDED == holder.texttafsirkurtubi.text.toString()) View.GONE else View.VISIBLE
            holder.maintafsirbaizabi.visibility = if (NOT_ADDED == holder.texttafsirbaizabi.text.toString()) View.GONE else View.VISIBLE
            holder.maintafsirrezbiya.visibility = if (NOT_ADDED == holder.texttafsirrezviya.text.toString()) View.GONE else View.VISIBLE
            holder.words.visibility = if ("শব্দার্থ যুক্ত করা হয়নি" == holder.words.text.toString()) View.GONE else View.VISIBLE

            // Enable text selection
            enableCopyTextView(holder.ayaarabic)
            enableCopyTextView(holder.words)
            enableCopyTextView(holder.texttafsiribnabbas)
            enableCopyTextView(holder.textkanzuliman)
            enableCopyTextView(holder.texttafsirkhazainulirfan)
            enableCopyTextView(holder.texttafsirnurulirfan)
            enableCopyTextView(holder.textifranulkuran)
            enableCopyTextView(holder.texttafsirtabari)
            enableCopyTextView(holder.texttafsirmajhari)
            enableCopyTextView(holder.texttafsiribnkasir)
            enableCopyTextView(holder.texttafsirkurtubi)
            enableCopyTextView(holder.texttafsirbaizabi)
            enableCopyTextView(holder.texttafsirrezviya)
        }

        /**
         * Build item view programmatically — mirrors tafsir.xml exactly.
         */
        private fun buildItemView(ctx: Context): View {
            val dp = ctx.resources.displayMetrics.density
            val banglaFont: Typeface = try { ctx.resources.getFont(R.font.solaimanlipi) } catch (e: Exception) { Typeface.DEFAULT }
            val boldFont: Typeface = try { ctx.resources.getFont(R.font.bold) } catch (e: Exception) { Typeface.DEFAULT_BOLD }

            fun lp(w: Int = LinearLayout.LayoutParams.MATCH_PARENT,
                   h: Int = LinearLayout.LayoutParams.WRAP_CONTENT,
                   weight: Float = 0f) =
                LinearLayout.LayoutParams(w, h, weight)

            fun sectionLayout(tag: String) = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                this.tag = tag
                layoutParams = lp().apply {
                    topMargin = (2 * dp).toInt()
                    bottomMargin = (2 * dp).toInt()
                }
            }

            fun headingTv(tag: String, text: String, bgColor: Int, textColor: Int) = TextView(ctx).apply {
                this.text = text
                this.tag = tag
                textSize = 14f
                setTextColor(textColor)
                setBackgroundColor(bgColor)
                typeface = banglaFont
                isFocusable = false
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
                layoutParams = lp()
            }

            fun bodyTv(tag: String) = TextView(ctx).apply {
                this.tag = tag
                textSize = 16f
                setTextColor(Color.BLACK)
                typeface = banglaFont
                isFocusable = false
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
                layoutParams = lp(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    topMargin = (5 * dp).toInt()
                }
            }

            // Root wrapper (ScrollView inside LinearLayout per item)
            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.back1ground)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val scrollView = ScrollView(ctx).apply {
                layoutParams = lp().apply { setMargins((10 * dp).toInt(), (10 * dp).toInt(), (10 * dp).toInt(), (10 * dp).toInt()) }
                setBackgroundColor(Color.WHITE)
            }

            val mainCard = LinearLayout(ctx).apply {
                tag = "main"
                orientation = LinearLayout.VERTICAL
                setPadding((5 * dp).toInt(), (5 * dp).toInt(), (5 * dp).toInt(), (5 * dp).toInt())
                layoutParams = lp()
            }

            // Header row: verse number badge
            val linear2 = LinearLayout(ctx).apply {
                tag = "linear2"
                orientation = LinearLayout.HORIZONTAL
                layoutParams = lp()
            }
            val linear3 = LinearLayout(ctx).apply {
                tag = "linear3"
                gravity = android.view.Gravity.CENTER
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
                setBackgroundResource(R.drawable.ic_1_4)
                layoutParams = LinearLayout.LayoutParams((50 * dp).toInt(), (50 * dp).toInt())
            }
            val number = TextView(ctx).apply {
                tag = "number"
                textSize = 10f
                setTextColor(Color.BLACK)
                isFocusable = false
                typeface = banglaFont
                layoutParams = lp(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            linear3.addView(number)
            linear2.addView(linear3)

            // Arabic ayah
            val ayaarabic = TextView(ctx).apply {
                tag = "ayaarabic"
                textSize = 20f
                setTypeface(boldFont, Typeface.BOLD)
                setTextColor(Color.BLACK)
                gravity = android.view.Gravity.END
                isFocusable = false
                textDirection = View.TEXT_DIRECTION_RTL
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    gravity = android.view.Gravity.END
                }
            }

            // Words
            val words = TextView(ctx).apply {
                tag = "words"
                textSize = 14f
                setTypeface(banglaFont, Typeface.BOLD)
                setTextColor(Color.BLACK)
                isFocusable = false
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            // ── Tafsir sections ──────────────────
            data class SectionDef(val mainTag: String, val headTag: String, val bodyTag: String, val headText: String, val bgColor: Int, val textColor: Int)

            val sections = listOf(
                SectionDef("maintafsiribnabbas", "headingtafsiribnabbas", "texttafsiribnabbas",
                    "তাফসিরে ইবনে আব্বাস (رضي الله عنه)", 0xFFE8F5E9.toInt(), 0xFF4CAF50.toInt()),
                SectionDef("mainkanzulimaanlayout", "headingkanzulimaan", "textkanzuliman",
                    "কানযুল ঈমান", 0xFFE0F2F1.toInt(), 0xFF009688.toInt()),
                SectionDef("mainkhazainulirfan", "headingkhazainulirfan", "texttafsirkhazainulirfan",
                    "তাফসিরে খাযাইনুল ইরফান", 0xFFE0F7FA.toInt(), 0xFF00BCD4.toInt()),
                SectionDef("maintafsirnurulirfan", "headingtafsirnurulirfan", "texttafsirnurulirfan",
                    "তাফসিরে নুরুল ইরফান", 0xFFE3F2FD.toInt(), 0xFF1E88E5.toInt()),
                SectionDef("mainirfanullayout", "headingirfanulkuran", "textifranulkuran",
                    "ইরফানুল কুরআন", 0xFFFBE9E7.toInt(), 0xFFFF5722.toInt()),
                SectionDef("maintafsirtabari", "headingtafsirtabari", "texttafsirtabari",
                    "তাফসিরে তাবারী", 0xFFF3E5F5.toInt(), 0xFF9C27B0.toInt()),
                SectionDef("maintafsirmajhari", "headingtafsirmajhari", "texttafsirmajhari",
                    "তাফসিরে মাযহারী", 0xFFFCE4EC.toInt(), 0xFFE91E63.toInt()),
                SectionDef("maintafsiribnkasir", "headingtafsiribnkasir", "texttafsiribnkasir",
                    "তাফসিরে ইবনে কাছীর", 0xFFFFEBEE.toInt(), 0xFFF44336.toInt()),
                SectionDef("maintafsirkurtubi", "headingtafsirkurtubi", "texttafsirkurtubi",
                    "তাফসিরে কুরতবী", 0xFFECEFF1.toInt(), 0xFF607D8B.toInt()),
                SectionDef("maintafsirbaizabi", "headingtafsirbaizabi", "texttafsirbaizabi",
                    "তাফসিরে বায়যাবী", 0xFFF1F8E9.toInt(), 0xFF8BC34A.toInt()),
                SectionDef("maintafsirrezbiya", "headingtafsirrezbiya", "texttafsirrezviya",
                    "তাফসিরে রেজভীয়া", 0xFFEFEBE9.toInt(), 0xFF795548.toInt())
            )

            // Assemble main card
            mainCard.addView(linear2)
            mainCard.addView(ayaarabic)
            mainCard.addView(words)

            sections.forEach { s ->
                val sec = sectionLayout(s.mainTag)
                sec.addView(headingTv(s.headTag, s.headText, s.bgColor, s.textColor))
                sec.addView(bodyTv(s.bodyTag))
                mainCard.addView(sec)
            }

            scrollView.addView(mainCard)
            root.addView(scrollView)
            return root
        }
    }
}
