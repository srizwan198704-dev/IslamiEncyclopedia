package com.srizwan.islamipedia

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TafsironlineviewMeActivity : AppCompatActivity() {

    private var newName = ""
    private var click = 0.0
    private var a = ""
    private var b = ""
    private var search = ""
    private var length = 0.0
    private var r = 0.0
    private var value1 = ""
    private var value2 = ""
    private var value3 = ""
    private var getsearch = ""
    private var downloadDirectory = ""
    private var downloadzip = ""
    private var tap = false
    private var vUrl = ""
    private var vFilename = ""
    private var vResumePause = false
    private var download_progress = 0.0
    private var Current_Size = ""
    private var Total_Size = ""
    private var debug_string = ""
    private var n = 0.0
    private var ListMap = HashMap<String, Any>()

    private var map = ArrayList<HashMap<String, Any>>()
    private var chapter = ArrayList<HashMap<String, Any>>()
    private var listmap_cache = ArrayList<HashMap<String, Any>>()

    private lateinit var mainLayout: LinearLayout
    private lateinit var toolbar: LinearLayout
    private lateinit var spinLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout
    private lateinit var listIcon: ImageView
    private lateinit var boxLayout: LinearLayout
    private lateinit var titleLayout: LinearLayout
    private lateinit var searchIcon: ImageView
    private lateinit var bookNameTv: TextView
    private lateinit var authorTv: TextView
    private lateinit var spinProgress: ProgressBar
    private lateinit var noInternetLayout: LinearLayout
    private lateinit var noInternetIcon: ImageView
    private lateinit var noInternetText: TextView
    private lateinit var refreshButton: MaterialButton
    private lateinit var searchMainLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResultLayout: LinearLayout
    private lateinit var searchBoxLayout: TextInputLayout
    private lateinit var clearSearchIcon: ImageView
    private lateinit var searchBox: EditText
    private lateinit var noResultIcon: ImageView
    private lateinit var noResultText: TextView

    private lateinit var book: RequestNetwork
    private lateinit var _book_request_listener: RequestNetwork.RequestListener
    private val inIntent = Intent()
    private lateinit var deleted: AlertDialog.Builder
    private lateinit var onlineoffline: AlertDialog.Builder

    private lateinit var adapter: TafsirAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1000)
        }
        
        createUI()
        initializeLogic()
    }

    override fun onRequestPermissionsResult(requestCode: Int, companion: String[], permissions: IntArray, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            initializeLogic()
        }
    }

    private fun createUI() {
        // Main container
        mainLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
        setContentView(mainLayout)

        // Toolbar
        toolbar = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(60))
            setPadding(dpToPx(3), dpToPx(1), dpToPx(3), dpToPx(1))
            setBackgroundColor(Color.parseColor("#01837A"))
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            elevation = dpToPx(5).toFloat()
        }
        mainLayout.addView(toolbar)

        listIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)).apply {
                rightMargin = dpToPx(5)
            }
            setPadding(dpToPx(10))
            setImageResource(R.drawable.ic_arrow_back_white)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener { finish() }
        }
        toolbar.addView(listIcon)

        boxLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        toolbar.addView(boxLayout)

        titleLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(dpToPx(8))
            orientation = LinearLayout.VERTICAL
        }
        boxLayout.addView(titleLayout)

        bookNameTv = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                rightMargin = dpToPx(5)
            }
            setPadding(0, dpToPx(3), 0, 0)
            gravity = Gravity.CENTER_VERTICAL
            textSize = 16f
            setTextColor(Color.WHITE)
            setSingleLine(true)
            typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
        }
        titleLayout.addView(bookNameTv)

        authorTv = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                rightMargin = dpToPx(5)
            }
            setPadding(0, dpToPx(3), 0, 0)
            gravity = Gravity.CENTER_VERTICAL
            textSize = 14f
            setTextColor(Color.WHITE)
            setSingleLine(true)
            typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
        }
        titleLayout.addView(authorTv)

        searchIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(30), ViewGroup.LayoutParams.MATCH_PARENT)
            setImageResource(R.drawable.searchme)
            scaleType = ImageView.ScaleType.FIT_CENTER
            visibility = View.GONE
            setOnClickListener {
                searchMainLayout.visibility = if (searchMainLayout.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }
        boxLayout.addView(searchIcon)

        // Spin Layout (Loading)
        spinLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setPadding(dpToPx(8))
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
        }
        mainLayout.addView(spinLayout)

        spinProgress = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(8))
        }
        spinLayout.addView(spinProgress)

        noInternetLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(8))
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        spinLayout.addView(noInternetLayout)

        noInternetIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setImageResource(R.drawable.nointernet)
            scaleType = ImageView.ScaleType.CENTER
        }
        noInternetLayout.addView(noInternetIcon)

        noInternetText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(8))
            gravity = Gravity.CENTER
            text = "ইন্টারনেট সেটিং চেক করুন"
            textSize = 16f
            setTextColor(Color.BLACK)
            typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
        }
        noInternetLayout.addView(noInternetText)

        refreshButton = MaterialButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(8))
            gravity = Gravity.CENTER
            text = "রিফ্রেশ করুন"
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#01837A"))
            cornerRadius = dpToPx(8)
            setOnClickListener {
                book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "", _book_request_listener)
                spinProgress.visibility = View.VISIBLE
                noInternetLayout.visibility = View.GONE
            }
        }
        noInternetLayout.addView(refreshButton)

        // Content Layout
        contentLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.WHITE)
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        mainLayout.addView(contentLayout)

        // Search Main Layout
        searchMainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
        }
        contentLayout.addView(searchMainLayout)

        searchBoxLayout = TextInputLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dpToPx(5))
            }
            setPadding(dpToPx(8))
            boxCornerRadii(100f, 100f, 100f, 100f)
            boxBackgroundColor = Color.WHITE
        }
        searchMainLayout.addView(searchBoxLayout)

        searchBox = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(8))
            textSize = 14f
            setTextColor(Color.BLACK)
            hint = "শব্দ লিখে সার্চ করুন"
            setHintTextColor(Color.parseColor("#01837A"))
            typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
            addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    jsonSearch(charSeq.toString())
                    if (chapter.isEmpty()) {
                        noResultLayout.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        noResultLayout.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })
        }
        searchBoxLayout.addView(searchBox)

        clearSearchIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(30), ViewGroup.LayoutParams.MATCH_PARENT).apply {
                rightMargin = dpToPx(5)
            }
            setImageResource(R.drawable.cancel)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener {
                if (searchBox.text.toString().isEmpty()) {
                    searchMainLayout.visibility = View.GONE
                } else {
                    searchBox.setText("")
                }
            }
        }
        searchMainLayout.addView(clearSearchIcon)

        // RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            layoutManager = LinearLayoutManager(this@TafsironlineviewMeActivity)
        }
        contentLayout.addView(recyclerView)

        // No Result Layout
        noResultLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(8))
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        contentLayout.addView(noResultLayout)

        noResultIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(100), dpToPx(100))
            setImageResource(R.drawable.noresult)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        noResultLayout.addView(noResultIcon)

        noResultText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(8))
            gravity = Gravity.CENTER
            text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"
            textSize = 16f
            setTextColor(Color.BLACK)
            typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
        }
        noResultLayout.addView(noResultText)

        book = RequestNetwork(this)
        deleted = AlertDialog.Builder(this)
        onlineoffline = AlertDialog.Builder(this)

        // Adapter setup
        adapter = TafsirAdapter(chapter)
        recyclerView.adapter = adapter

        // Request Listener
        _book_request_listener = object : RequestNetwork.RequestListener {
            override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
                val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
                
                if (FileUtil.isExistFile(cachePath)) {
                    try {
                        listmap_cache = Gson().fromJson(FileUtil.readFile(cachePath), object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
                        processListMapCache()
                    } catch (e: Exception) {
                        // Handle exception
                    }
                } else {
                    try {
                        listmap_cache = Gson().fromJson(response, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
                        processListMapCache()
                    } catch (e: Exception) {
                        // Handle exception
                    }
                }
                
                updateUIVisibility()
            }

            override fun onErrorResponse(tag: String, message: String) {
                val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
                
                if (FileUtil.isExistFile(cachePath)) {
                    try {
                        listmap_cache = Gson().fromJson(FileUtil.readFile(cachePath), object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
                        processListMapCache()
                    } catch (e: Exception) {
                        // Handle exception
                    }
                } else {
                    Toast.makeText(applicationContext, "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show()
                    spinProgress.visibility = View.GONE
                    noInternetLayout.visibility = View.VISIBLE
                }
                updateUIVisibility()
            }
        }
    }

    private fun processListMapCache() {
        chapter.clear()
        n = 0.0
        val suraName = intent.getStringExtra("sura") ?: ""
        
        for (i in listmap_cache.indices) {
            if (listmap_cache[i.toInt()]["sura"].toString() == suraName) {
                ListMap = HashMap()
                ListMap["verses"] = listmap_cache[i.toInt()]["verses"].toString()
                ListMap["names"] = listmap_cache[i.toInt()]["names"].toString()
                ListMap["words"] = listmap_cache[i.toInt()]["words"].toString()
                ListMap["name"] = listmap_cache[i.toInt()]["name"].toString()
                ListMap["khazainul"] = listmap_cache[i.toInt()]["khazainul"].toString()
                ListMap["irfanul"] = listmap_cache[i.toInt()]["irfanul"].toString()
                ListMap["ibnabbas"] = listmap_cache[i.toInt()]["ibnabbas"].toString()
                ListMap["majhari"] = listmap_cache[i.toInt()]["majhari"].toString()
                ListMap["nurulirfan"] = listmap_cache[i.toInt()]["nurulirfan"].toString()
                ListMap["tabari"] = listmap_cache[i.toInt()]["tabari"].toString()
                ListMap["ibnkasir"] = listmap_cache[i.toInt()]["ibnkasir"].toString()
                ListMap["rejviya"] = listmap_cache[i.toInt()]["rejviya"].toString()
                ListMap["baizabi"] = listmap_cache[i.toInt()]["baizabi"].toString()
                ListMap["kurtubi"] = listmap_cache[i.toInt()]["kurtubi"].toString()
                chapter.add(ListMap)
            }
            n++
        }
        
        adapter.notifyDataSetChanged()
        getsearch = Gson().toJson(chapter)
        searchIcon.visibility = View.VISIBLE
    }

    private fun updateUIVisibility() {
        if (chapter.isEmpty()) {
            spinLayout.visibility = View.VISIBLE
            contentLayout.visibility = View.GONE
            searchIcon.visibility = View.GONE
        } else {
            spinLayout.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
            searchIcon.visibility = View.VISIBLE
        }
    }

    private fun initializeLogic() {
        statusBarColor("#FF01837A", "#FF01837A")
        marquee(bookNameTv, intent.getStringExtra("name") ?: "")
        marquee(authorTv, intent.getStringExtra("author") ?: "")
        click = 0.0
        noInternetLayout.visibility = View.GONE
        searchMainLayout.visibility = View.GONE
        searchIcon.visibility = View.GONE
        noResultLayout.visibility = View.GONE

        if (chapter.isEmpty()) {
            spinLayout.visibility = View.VISIBLE
            contentLayout.visibility = View.GONE
            searchIcon.visibility = View.GONE
        } else {
            contentLayout.visibility = View.VISIBLE
            spinLayout.visibility = View.GONE
            searchIcon.visibility = View.VISIBLE
        }

        val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
        
        if (FileUtil.isExistFile(cachePath)) {
            try {
                listmap_cache = Gson().fromJson(FileUtil.readFile(cachePath), object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
                processListMapCache()
                spinLayout.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE
                noInternetLayout.visibility = View.GONE
                searchIcon.visibility = View.VISIBLE
            } catch (e: Exception) {
                // Handle exception
            }
        } else {
            FileUtil.makeDir(FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/")
            
            if (Rizwan.isConnected(applicationContext)) {
                book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "", _book_request_listener)
            } else {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
                
                if (activeNetwork == null || !activeNetwork.isConnected) {
                    noInternetLayout.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show()
                }
                
                if (FileUtil.isExistFile(cachePath)) {
                    spinLayout.visibility = View.GONE
                    contentLayout.visibility = View.VISIBLE
                    noInternetLayout.visibility = View.GONE
                } else {
                    Toast.makeText(applicationContext, "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                    spinLayout.visibility = View.VISIBLE
                    contentLayout.visibility = View.GONE
                    noInternetLayout.visibility = View.VISIBLE
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchMainLayout.visibility == View.VISIBLE) {
                    if (searchBox.text.toString().isEmpty()) {
                        searchMainLayout.visibility = View.GONE
                    } else {
                        searchBox.setText("")
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun marquee(textView: TextView, text: String) {
        textView.text = text
        textView.ellipsize = TextUtils.TruncateAt.MARQUEE
        textView.isSelected = true
        textView.isHorizontallyScrolling = true
        textView.marqueeRepeatLimit = -1
        textView.isSingleLine = true
        textView.isFocusable = true
        textView.isFocusableInTouchMode = true
    }

    private fun statusBarColor(color1: String, color2: String) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor(color1)
            window.navigationBarColor = Color.parseColor(color2)
        }
    }

    private fun jsonSearch(charSeq: String) {
        chapter = Gson().fromJson(getsearch, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
        length = chapter.size.toDouble()
        r = length - 1
        
        for (i in 0 until length.toInt()) {
            value1 = chapter[r.toInt()]["name"].toString()
            value2 = chapter[r.toInt()]["irfanul"].toString()
            
            if (!(charSeq.length > value1.length) && value1.lowercase().contains(charSeq.lowercase())) {
                // Keep item
            } else if (!(charSeq.length > value2.length) && value2.lowercase().contains(charSeq.lowercase())) {
                // Keep item
            } else {
                chapter.removeAt(r.toInt())
            }
            r--
        }
        adapter.notifyDataSetChanged()
    }

    private fun replaceArabicNumber(n: String): String {
        return n.replace("1", "১")
            .replace("2", "২")
            .replace("3", "৩")
            .replace("4", "৪")
            .replace("5", "৫")
            .replace("6", "৬")
            .replace("7", "৭")
            .replace("8", "৮")
            .replace("9", "৯")
            .replace("0", "০")
    }

    private fun enableCopyTextView(tv: TextView) {
        tv.setTextIsSelectable(true)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    inner class TafsirAdapter(private val data: ArrayList<HashMap<String, Any>>) : RecyclerView.Adapter<TafsirAdapter.ViewHolder>() {

        inner class ViewHolder(val container: LinearLayout) : RecyclerView.ViewHolder(container) {
            val mainLayout: LinearLayout = container.getChildAt(0) as LinearLayout
            val numberTv: TextView = mainLayout.findViewById(1001)
            val ayaArabicTv: TextView = mainLayout.findViewById(1002)
            val wordsTv: TextView = mainLayout.findViewById(1003)
            
            val mainIbnAbbas: LinearLayout = mainLayout.findViewById(2001)
            val headingIbnAbbas: TextView = mainLayout.findViewById(2002)
            val textIbnAbbas: TextView = mainLayout.findViewById(2003)
            
            val mainKanzulIman: LinearLayout = mainLayout.findViewById(3001)
            val headingKanzulIman: TextView = mainLayout.findViewById(3002)
            val textKanzulIman: TextView = mainLayout.findViewById(3003)
            
            val mainKhazainul: LinearLayout = mainLayout.findViewById(4001)
            val headingKhazainul: TextView = mainLayout.findViewById(4002)
            val textKhazainul: TextView = mainLayout.findViewById(4003)
            
            val mainNurulIrfan: LinearLayout = mainLayout.findViewById(5001)
            val headingNurulIrfan: TextView = mainLayout.findViewById(5002)
            val textNurulIrfan: TextView = mainLayout.findViewById(5003)
            
            val mainIrfanul: LinearLayout = mainLayout.findViewById(6001)
            val headingIrfanul: TextView = mainLayout.findViewById(6002)
            val textIrfanul: TextView = mainLayout.findViewById(6003)
            
            val mainTabari: LinearLayout = mainLayout.findViewById(7001)
            val headingTabari: TextView = mainLayout.findViewById(7002)
            val textTabari: TextView = mainLayout.findViewById(7003)
            
            val mainMajhari: LinearLayout = mainLayout.findViewById(8001)
            val headingMajhari: TextView = mainLayout.findViewById(8002)
            val textMajhari: TextView = mainLayout.findViewById(8003)
            
            val mainIbnKasir: LinearLayout = mainLayout.findViewById(9001)
            val headingIbnKasir: TextView = mainLayout.findViewById(9002)
            val textIbnKasir: TextView = mainLayout.findViewById(9003)
            
            val mainKurtubi: LinearLayout = mainLayout.findViewById(10001)
            val headingKurtubi: TextView = mainLayout.findViewById(10002)
            val textKurtubi: TextView = mainLayout.findViewById(10003)
            
            val mainBaizabi: LinearLayout = mainLayout.findViewById(11001)
            val headingBaizabi: TextView = mainLayout.findViewById(11002)
            val textBaizabi: TextView = mainLayout.findViewById(11003)
            
            val mainRezviya: LinearLayout = mainLayout.findViewById(12001)
            val headingRezviya: TextView = mainLayout.findViewById(12002)
            val textRezviya: TextView = mainLayout.findViewById(12003)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val scrollView = ScrollView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setPadding(dpToPx(10))
                setBackgroundColor(Color.WHITE)
            }

            val mainLinear = LinearLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(dpToPx(5))
                orientation = LinearLayout.VERTICAL
                elevation = dpToPx(4).toFloat()
                
                val gradientDrawable = GradientDrawable()
                gradientDrawable.setColor(Color.WHITE)
                gradientDrawable.cornerRadius = dpToPx(20).toFloat()
                gradientDrawable.setStroke(dpToPx(1), Color.parseColor("#01837A"))
                
                val rippleDrawable = RippleDrawable(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#01837A")),
                    gradientDrawable,
                    null
                )
                background = rippleDrawable
            }

            // Number row layout
            val numberRow = LinearLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
            }
            mainLinear.addView(numberRow)

            val numberContainer = LinearLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(50), dpToPx(50))
                setPadding(dpToPx(8))
                setBackgroundResource(R.drawable.ic_1_4)
                gravity = Gravity.CENTER
            }
            numberRow.addView(numberContainer)

            val numberTv = TextView(parent.context).apply {
                id = 1001
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textSize = 10f
                setTextColor(Color.BLACK)
                isFocusable = false
                typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
            }
            numberContainer.addView(numberTv)

            // Arabic text
            val ayaArabicTv = TextView(parent.context).apply {
                id = 1002
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.RIGHT
                }
                setPadding(dpToPx(8))
                gravity = Gravity.RIGHT
                textSize = 20f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.BLACK)
                isFocusable = false
                textDirection = View.TEXT_DIRECTION_RTL
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                typeface = Typeface.createFromAsset(assets, "bold.ttf")
            }
            mainLinear.addView(ayaArabicTv)

            // Words text
            val wordsTv = TextView(parent.context).apply {
                id = 1003
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(dpToPx(8))
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.BLACK)
                isFocusable = false
                typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
            }
            mainLinear.addView(wordsTv)

            // Add all tafsir sections
            mainLinear.addView(createTafsirSection(parent.context, 2001, 2002, 2003, 
                "তাফসিরে ইবনে আব্বাস (رضي الله عنه)", "#E8F5E9", "#4CAF50"))
            mainLinear.addView(createTafsirSection(parent.context, 3001, 3002, 3003,
                "কানযুল ঈমান", "#E0F2F1", "#009688"))
            mainLinear.addView(createTafsirSection(parent.context, 4001, 4002, 4003,
                "তাফসিরে খাযাইনুল ইরফান", "#E0F7FA", "#00BCD4"))
            mainLinear.addView(createTafsirSection(parent.context, 5001, 5002, 5003,
                "তাফসিরে নুরুল ইরফান", "#E3F2FD", "#1E88E5"))
            mainLinear.addView(createTafsirSection(parent.context, 6001, 6002, 6003,
                "ইরফানুল কুরআন", "#FBE9E7", "#FF5722"))
            mainLinear.addView(createTafsirSection(parent.context, 7001, 7002, 7003,
                "তাফসিরে তাবারী", "#F3E5F5", "#9C27B0"))
            mainLinear.addView(createTafsirSection(parent.context, 8001, 8002, 8003,
                "তাফসিরে মাযহারী", "#FCE4EC", "#E91E63"))
            mainLinear.addView(createTafsirSection(parent.context, 9001, 9002, 9003,
                "তাফসিরে ইবনে কাছীর", "#FFEBEE", "#F44336"))
            mainLinear.addView(createTafsirSection(parent.context, 10001, 10002, 10003,
                "তাফসিরে কুরতবী", "#ECEFF1", "#607D8B"))
            mainLinear.addView(createTafsirSection(parent.context, 11001, 11002, 11003,
                "তাফসিরে বায়যাবী", "#F1F8E9", "#8BC34A"))
            mainLinear.addView(createTafsirSection(parent.context, 12001, 12002, 12003,
                "তাফসিরে রেজভীয়া", "#EFEBE9", "#795548"))

            scrollView.addView(mainLinear)
            
            val container = LinearLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.VERTICAL
            }
            container.addView(scrollView)
            
            return ViewHolder(container)
        }

        private fun createTafsirSection(context: Context, mainId: Int, headingId: Int, textId: Int,
                                        headingText: String, bgColor: String, textColor: String): LinearLayout {
            return LinearLayout(context).apply {
                id = mainId
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dpToPx(2)
                    bottomMargin = dpToPx(2)
                }
                orientation = LinearLayout.VERTICAL

                val heading = TextView(context).apply {
                    id = headingId
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    setPadding(dpToPx(8))
                    setBackgroundColor(Color.parseColor(bgColor))
                    text = headingText
                    textSize = 14f
                    setTextColor(Color.parseColor(textColor))
                    isFocusable = false
                    typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
                }
                addView(heading)

                val text = TextView(context).apply {
                    id = textId
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                        topMargin = dpToPx(5)
                    }
                    setPadding(dpToPx(8))
                    textSize = 16f
                    setTextColor(Color.BLACK)
                    isFocusable = false
                    visibility = View.GONE
                    typeface = Typeface.createFromAsset(assets, "solaimanlipi.ttf")
                }
                addView(text)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            
            holder.numberTv.text = replaceArabicNumber(item["verses"].toString())
            holder.ayaArabicTv.text = item["names"].toString()
            holder.wordsTv.text = item["words"].toString()
            holder.textKanzulIman.text = replaceArabicNumber(item["verses"].toString() + ". " + item["name"].toString())
            holder.textKhazainul.text = item["khazainul"].toString()
            holder.textIrfanul.text = item["irfanul"].toString()
            holder.textIbnAbbas.text = item["ibnabbas"].toString()
            holder.textMajhari.text = item["majhari"].toString()
            holder.textNurulIrfan.text = item["nurulirfan"].toString()
            holder.textTabari.text = item["tabari"].toString()
            holder.textIbnKasir.text = item["ibnkasir"].toString()
            holder.textKurtubi.text = item["kurtubi"].toString()
            holder.textRezviya.text = item["rejviya"].toString()
            holder.textBaizabi.text = item["baizabi"].toString()

            // Setup click listeners for expand/collapse
            setupExpandCollapse(holder.headingIbnAbbas, holder.textIbnAbbas)
            setupExpandCollapse(holder.headingKhazainul, holder.textKhazainul)
            setupExpandCollapse(holder.headingNurulIrfan, holder.textNurulIrfan)
            setupExpandCollapse(holder.headingTabari, holder.textTabari)
            setupExpandCollapse(holder.headingMajhari, holder.textMajhari)
            setupExpandCollapse(holder.headingKurtubi, holder.textKurtubi)
            setupExpandCollapse(holder.headingIbnKasir, holder.textIbnKasir)
            setupExpandCollapse(holder.headingBaizabi, holder.textBaizabi)
            setupExpandCollapse(holder.headingRezviya, holder.textRezviya)
            setupExpandCollapse(holder.headingKanzulIman, holder.textKanzulIman)
            setupExpandCollapse(holder.headingIrfanul, holder.textIrfanul)

            // Hide sections with no tafsir
            holder.mainIbnAbbas.visibility = if (holder.textIbnAbbas.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainKhazainul.visibility = if (holder.textKhazainul.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainNurulIrfan.visibility = if (holder.textNurulIrfan.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainTabari.visibility = if (holder.textTabari.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainMajhari.visibility = if (holder.textMajhari.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainIbnKasir.visibility = if (holder.textIbnKasir.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainKurtubi.visibility = if (holder.textKurtubi.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainBaizabi.visibility = if (holder.textBaizabi.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.mainRezviya.visibility = if (holder.textRezviya.text == "তাফসির যুক্ত করা হয়নি") View.GONE else View.VISIBLE
            holder.wordsTv.visibility = if (holder.wordsTv.text == "শব্দার্থ যুক্ত করা হয়নি") View.GONE else View.VISIBLE

            // Enable copy for all text views
            enableCopyTextView(holder.ayaArabicTv)
            enableCopyTextView(holder.wordsTv)
            enableCopyTextView(holder.textIbnAbbas)
            enableCopyTextView(holder.textKanzulIman)
            enableCopyTextView(holder.textKhazainul)
            enableCopyTextView(holder.textNurulIrfan)
            enableCopyTextView(holder.textIrfanul)
            enableCopyTextView(holder.textTabari)
            enableCopyTextView(holder.textMajhari)
            enableCopyTextView(holder.textIbnKasir)
            enableCopyTextView(holder.textKurtubi)
            enableCopyTextView(holder.textBaizabi)
            enableCopyTextView(holder.textRezviya)
        }

        private fun setupExpandCollapse(heading: TextView, text: TextView) {
            heading.setOnClickListener {
                text.visibility = if (text.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }

        override fun getItemCount(): Int = data.size
    }
}
