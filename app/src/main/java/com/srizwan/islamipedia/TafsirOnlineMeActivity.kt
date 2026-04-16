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
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.CornerFamily
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TafsirOnlineMeActivity : AppCompatActivity() {

    private var timer: Timer? = Timer()
    private var click = 0.0
    private var getsearch = ""
    private var saveMe = ""
    private var n = 0.0

    private val listMap: HashMap<String, Any> = HashMap()
    private var map: ArrayList<HashMap<String, Any>> = ArrayList()
    private var listmapCache: ArrayList<HashMap<String, Any>> = ArrayList()

    // UI Components
    private lateinit var rootLayout: LinearLayout
    private lateinit var toolbar: LinearLayout
    private lateinit var spinLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout
    private lateinit var bookname: TextView
    private lateinit var refresh: ImageView
    private lateinit var progressBar1: ProgressBar
    private lateinit var searchImg: ImageView
    private lateinit var version: TextView
    private lateinit var spinBer: ProgressBar
    private lateinit var noInternetLayout: LinearLayout
    private lateinit var materialButton1: MaterialButton
    private lateinit var searchMainLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResLayout: LinearLayout
    private lateinit var boxOfSearch: TextInputLayout
    private lateinit var searchBox: EditText

    private lateinit var book: RequestNetwork
    private lateinit var bookUpdate: RequestNetwork
    private val intent = Intent()
    private lateinit var adapter: TafsirAdapter

    private val bookRequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
            handleBookResponse(response)
        }

        override fun onErrorResponse(tag: String, message: String) {
            handleBookError()
        }
    }

    private val bookUpdateRequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
            try {
                val updateBook = Gson().fromJson<ArrayList<HashMap<String, Any>>>(
                    response,
                    object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
                )
                if (updateBook.isNotEmpty()) {
                    val currentVersion = version.text.toString().toDoubleOrNull() ?: 0.0
                    val newVersion = (updateBook[0]["version"] as? String)?.toDoubleOrNull() ?: 0.0
                    
                    if (currentVersion < newVersion) {
                        if (!isFinishing) {
                            showUpdateDialog(updateBook)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            // Handle error silently
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createProgrammaticUI()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1000)
        } else {
            initializeLogic()
        }
    }

    private fun createProgrammaticUI() {
        // Root Layout
        rootLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            setBackgroundColor(Color.WHITE)
        }

        // Toolbar
        toolbar = createToolbar()
        rootLayout.addView(toolbar)

        // Spin Layout (Loading)
        spinLayout = createSpinLayout()
        rootLayout.addView(spinLayout)

        // Content Layout
        contentLayout = createContentLayout()
        rootLayout.addView(contentLayout)

        setContentView(rootLayout)

        book = RequestNetwork(this)
        bookUpdate = RequestNetwork(this)
    }

    private fun createToolbar(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                60.dpToPx()
            ).apply {
                setPadding(3.dpToPx(), 1.dpToPx(), 3.dpToPx(), 1.dpToPx())
            }
            setBackgroundColor(Color.parseColor("#01837A"))
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            elevation = 5.dpToPx().toFloat()

            // Back Button
            val backBtn = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
                    marginEnd = 5.dpToPx()
                }
                setPadding(10.dpToPx(), 10.dpToPx(), 10.dpToPx(), 10.dpToPx())
                setImageResource(R.drawable.ic_arrow_back_white)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setOnClickListener { finish() }
            }
            addView(backBtn)

            // Title Box
            val titleBox = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
            }

            bookname = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginEnd = 5.dpToPx()
                }
                text = "তাফসির সমগ্র"
                textSize = 20f
                setTextColor(Color.WHITE)
                setSingleLine(true)
                typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi)
                marqueeText("তাফসির সমগ্র")
            }
            titleBox.addView(bookname)

            refresh = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(30.dpToPx(), LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    marginEnd = 10.dpToPx()
                }
                setImageResource(R.drawable.refresh)
                scaleType = ImageView.ScaleType.FIT_CENTER
                visibility = View.GONE
                setOnClickListener {
                    if (Rizwan.isConnected(applicationContext)) {
                        refreshData()
                    } else {
                        Toast.makeText(applicationContext, "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            titleBox.addView(refresh)

            progressBar1 = ProgressBar(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                visibility = View.GONE
                indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            }
            titleBox.addView(progressBar1)

            searchImg = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(30.dpToPx(), LinearLayout.LayoutParams.MATCH_PARENT)
                setImageResource(R.drawable.searchme)
                scaleType = ImageView.ScaleType.FIT_CENTER
                visibility = View.GONE
                setOnClickListener {
                    searchMainLayout.visibility = if (searchMainLayout.visibility == View.GONE) View.VISIBLE else View.GONE
                }
            }
            titleBox.addView(searchImg)

            addView(titleBox)
        }
    }

    private fun createSpinLayout(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            version = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                textSize = 12f
                setTextColor(Color.BLACK)
            }
            addView(version)

            spinBer = ProgressBar(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            }
            addView(spinBer)

            noInternetLayout = createNoInternetLayout()
            addView(noInternetLayout)
        }
    }

    private fun createNoInternetLayout(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE

            val noInternetImg = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setImageResource(R.drawable.nointernet)
                scaleType = ImageView.ScaleType.CENTER
            }
            addView(noInternetImg)

            val noInternetText = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                gravity = Gravity.CENTER
                text = "ইন্টারনেট সেটিং চেক করুন"
                textSize = 16f
                setTextColor(Color.BLACK)
                typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi)
            }
            addView(noInternetText)

            materialButton1 = MaterialButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                gravity = Gravity.CENTER
                text = "রিফ্রেশ করুন"
                textSize = 12f
                setTextColor(Color.WHITE)
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF01837A"))
                cornerRadius = 8.dpToPx()
                setOnClickListener {
                    book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "", bookRequestListener)
                    spinBer.visibility = View.VISIBLE
                    noInternetLayout.visibility = View.GONE
                }
            }
            addView(materialButton1)
        }
    }

    private fun createContentLayout(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE

            // Search Layout
            searchMainLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                visibility = View.GONE

                boxOfSearch = TextInputLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        setMargins(5.dpToPx(), 5.dpToPx(), 5.dpToPx(), 5.dpToPx())
                    }
                    setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                    boxBackgroundColor = Color.WHITE
                    
                    // Fix: Use ShapeAppearanceModel instead of boxCornerRadii
                    shapeAppearanceModel = ShapeAppearanceModel.builder()
                        .setAllCornerSizes(ShapeAppearanceModel.PILL)
                        .build()

                    searchBox = EditText(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                        textSize = 14f
                        setTextColor(Color.BLACK)
                        hint = "সূরার নাম লিখে সার্চ করুন"
                        setHintTextColor(Color.parseColor("#FF01837A"))
                        typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi)
                        
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                jsonSearch(s.toString())
                                noResLayout.visibility = if (map.isEmpty()) View.VISIBLE else View.GONE
                                recyclerView.visibility = if (map.isEmpty()) View.GONE else View.VISIBLE
                            }
                            override fun afterTextChanged(s: Editable?) {}
                        })
                    }
                    addView(searchBox)
                }
                addView(boxOfSearch)

                val cancelImg = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(30.dpToPx(), LinearLayout.LayoutParams.MATCH_PARENT).apply {
                        marginEnd = 5.dpToPx()
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
                addView(cancelImg)
            }
            addView(searchMainLayout)

            // RecyclerView
            recyclerView = RecyclerView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutManager = LinearLayoutManager(context)
                adapter = TafsirAdapter(map)
                this.adapter = adapter
            }
            addView(recyclerView)

            // No Results Layout
            noResLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                setBackgroundColor(Color.WHITE)
                gravity = Gravity.CENTER
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE

                val noResultImg = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(100.dpToPx(), 100.dpToPx())
                    setImageResource(R.drawable.noresult)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                addView(noResultImg)

                val noResultText = TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                    gravity = Gravity.CENTER
                    text = "কোন সার্চ রেজাল্ট পাওয়া যায়নি"
                    textSize = 16f
                    setTextColor(Color.BLACK)
                    typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi)
                }
                addView(noResultText)
            }
            addView(noResLayout)
        }
    }

    private fun initializeLogic() {
        bookname.text = "তাফসির সমগ্র"
        bookname.marqueeText("তাফসির সমগ্র")
        click = 0.0
        noInternetLayout.visibility = View.GONE
        searchMainLayout.visibility = View.GONE
        searchImg.visibility = View.GONE
        noResLayout.visibility = View.GONE
        refresh.visibility = View.GONE

        updateVisibility()

        val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
        
        if (FileUtil.isExistFile(cachePath)) {
            loadCachedData(cachePath)
            if (Rizwan.isConnected(applicationContext)) {
                bookUpdate.startRequestNetwork(
                    RequestNetworkController.GET,
                    "https://www.dropbox.com/scl/fi/b40r0083jqpipelv820rq/tafsirupdate.json?rlkey=km0ipzqn3wfwna8lx0uyt801v&st=nhsvmjeq&dl=1",
                    "Rizwan",
                    bookUpdateRequestListener
                )
            }
        } else {
            FileUtil.makeDir(FileUtil.getPackageDataDir(applicationContext) + "/ইসলামী বিশ্বকোষ/.অনলাইন বই ২/")
            if (Rizwan.isConnected(applicationContext)) {
                book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "Rizwan", bookRequestListener)
            } else {
                handleNoInternet()
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

    private fun handleBookResponse(response: String) {
        val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
        
        if (!FileUtil.isExistFile(cachePath)) {
            processResponseData(response)
            saveMe = Gson().toJson(listmapCache)
            FileUtil.writeFile(cachePath, saveMe)
        }
        
        updateUIAfterLoad()
    }

    private fun loadCachedData(cachePath: String) {
        try {
            val cachedJson = FileUtil.readFile(cachePath)
            listmapCache = Gson().fromJson(
                cachedJson,
                object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
            )
            processCachedData()
            updateUIAfterLoad()
        } catch (e: Exception) {
            e.printStackTrace()
            handleBookError()
        }
    }

    private fun processResponseData(response: String) {
        try {
            listmapCache = Gson().fromJson(response, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
            processCachedData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processCachedData() {
        n = 0.0
        map.clear()
        listmapCache.forEach { item ->
            if (n == 0.0) {
                addToMap(item)
            } else {
                if (item["sura"].toString() != listmapCache[n.toInt() - 1]["sura"].toString()) {
                    addToMap(item)
                }
            }
            n++
        }
        getsearch = Gson().toJson(map)
    }

    private fun addToMap(item: HashMap<String, Any>) {
        val newMap = HashMap<String, Any>()
        newMap["sura"] = item["sura"]?.toString() ?: ""
        newMap["suraName"] = item["suraName"]?.toString() ?: ""
        newMap["type"] = item["type"]?.toString() ?: ""
        newMap["versess"] = item["versess"]?.toString() ?: ""
        newMap["suraArabic"] = item["suraArabic"]?.toString() ?: ""
        newMap["version"] = item["version"]?.toString() ?: "1.0"
        map.add(newMap)
    }

    private fun updateUIAfterLoad() {
        runOnUiThread {
            adapter.updateData(map)
            refresh.visibility = View.VISIBLE
            spinLayout.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
            noInternetLayout.visibility = View.GONE
            searchImg.visibility = View.VISIBLE
            progressBar1.visibility = View.GONE
            
            if (map.isNotEmpty()) {
                version.text = map[0]["version"]?.toString() ?: "1.0"
            }
            
            updateVisibility()
        }
    }

    private fun handleBookError() {
        runOnUiThread {
            val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
            
            if (FileUtil.isExistFile(cachePath)) {
                loadCachedData(cachePath)
            } else {
                refresh.visibility = View.VISIBLE
                Toast.makeText(applicationContext, "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show()
                spinBer.visibility = View.GONE
                noInternetLayout.visibility = View.VISIBLE
            }
            updateVisibility()
        }
    }

    private fun handleNoInternet() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!isConnected) {
            noInternetLayout.visibility = View.VISIBLE
            Toast.makeText(applicationContext, "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show()
        }

        val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
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

    private fun refreshData() {
        val cachePath = FileUtil.getPackageDataDir(applicationContext) + "//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"
        
        if (FileUtil.isExistFile(cachePath)) {
            refresh.visibility = View.GONE
            progressBar1.visibility = View.VISIBLE
            searchImg.visibility = View.GONE
            FileUtil.deleteFile(cachePath)
            
            Handler(Looper.getMainLooper()).postDelayed({
                materialButton1.performClick()
                adapter.notifyDataSetChanged()
            }, 50)
            
            map.clear()
            Toast.makeText(applicationContext, "আপডেট হচ্ছে....।", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateVisibility() {
        if (map.isEmpty()) {
            spinLayout.visibility = View.VISIBLE
            contentLayout.visibility = View.GONE
            searchImg.visibility = View.GONE
        } else {
            spinLayout.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
            searchImg.visibility = View.VISIBLE
        }
    }

    private fun jsonSearch(charSeq: String) {
        try {
            val originalMap: ArrayList<HashMap<String, Any>> = Gson().fromJson(getsearch, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
            map.clear()
            
            originalMap.forEach { item ->
                val value1 = item["suraName"]?.toString() ?: ""
                if (charSeq.isEmpty() || (charSeq.length <= value1.length && value1.lowercase(Locale.getDefault()).contains(charSeq.lowercase(Locale.getDefault())))) {
                    map.add(item)
                }
            }
            adapter.updateData(map)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun showUpdateDialog(updateBook: ArrayList<HashMap<String, Any>>) {
        val dialogView = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(20.dpToPx(), 20.dpToPx(), 20.dpToPx(), 20.dpToPx())
            setBackgroundColor(Color.WHITE)
            
            rippleRoundStroke(this, "#FFFFFF", "#000000", 15.0, 0.0, "#000000")

            val versionText = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = updateBook[0]["title"]?.toString() ?: "নতুন আপডেট"
                textSize = 18f
                setTextColor(Color.BLACK)
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 0, 0, 10.dpToPx())
            }
            addView(versionText)

            val whatsNewText = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = updateBook[0]["new"]?.toString() ?: "নতুন ফিচার যোগ করা হয়েছে"
                textSize = 14f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 20.dpToPx())
            }
            addView(whatsNewText)

            val updateBtn = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "রিফ্রেশ করুন"
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 12.dpToPx(), 0, 12.dpToPx())
                rippleRoundStroke(this, "#FF9800", "#40FFFFFF", 15.0, 0.0, "#000000")
            }
            addView(updateBtn)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        (dialogView.getChildAt(2) as TextView).setOnClickListener {
            refresh.performClick()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun TextView.marqueeText(text: String) {
        this.text = text
        ellipsize = TextUtils.TruncateAt.MARQUEE
        isSelected = true
        isHorizontalScrollBarEnabled = true
        marqueeRepeatLimit = -1
        isSingleLine = true
        isFocusable = true
        isFocusableInTouchMode = true
    }

    private fun rippleRoundStroke(view: View, focus: String, pressed: String, round: Double, stroke: Double, strokeClr: String) {
        val gradientDrawable = GradientDrawable().apply {
            setColor(Color.parseColor(focus))
            cornerRadius = round.toFloat()
            setStroke(stroke.toInt(), Color.parseColor(strokeClr.replace("#", "#")))
        }
        val rippleDrawable = RippleDrawable(
            android.content.res.ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.parseColor(pressed))),
            gradientDrawable,
            null
        )
        view.background = rippleDrawable
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    inner class TafsirAdapter(private var data: ArrayList<HashMap<String, Any>>) : 
        RecyclerView.Adapter<TafsirAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val linear1: LinearLayout = itemView.findViewById(itemView.context.resources.getIdentifier("linear1", "id", itemView.context.packageName))
            val suraArabic: TextView = itemView.findViewById(itemView.context.resources.getIdentifier("suraArabic", "id", itemView.context.packageName))
            val number: TextView = itemView.findViewById(itemView.context.resources.getIdentifier("number", "id", itemView.context.packageName))
            val suraName: TextView = itemView.findViewById(itemView.context.resources.getIdentifier("suraName", "id", itemView.context.packageName))
            val verses: TextView = itemView.findViewById(itemView.context.resources.getIdentifier("verses", "id", itemView.context.packageName))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(createProgrammaticItemView(parent))
        }

        private fun createProgrammaticItemView(parent: ViewGroup): View {
            val context = parent.context
            val rootLayout = LinearLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.WHITE)
                orientation = LinearLayout.VERTICAL
            }

            val linear1 = LinearLayout(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(5.dpToPx(), 5.dpToPx(), 5.dpToPx(), 5.dpToPx())
                }
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                setBackgroundColor(Color.WHITE)
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
                
                val gradientDrawable = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 20.dpToPx().toFloat()
                    setStroke(1.dpToPx(), Color.parseColor("#01837A"))
                }
                elevation = 5.dpToPx().toFloat()
                val rippleDrawable = RippleDrawable(
                    android.content.res.ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.parseColor("#01837A"))),
                    gradientDrawable,
                    null
                )
                background = rippleDrawable
                isClickable = true
            }

            val bookPic = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(50.dpToPx(), 50.dpToPx())
                setBackgroundResource(R.drawable.quran)
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL
            }

            val number = TextView(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                gravity = Gravity.CENTER
                textSize = 10f
                setTextColor(Color.BLACK)
                translationY = -5f
                typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi)
            }
            bookPic.addView(number)
            linear1.addView(bookPic)

            val boxOfContent = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                orientation = LinearLayout.VERTICAL
            }

            val suraName = TextView(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 16f
                setTextColor(Color.BLACK)
                typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi)
            }
            boxOfContent.addView(suraName)

            val verses = TextView(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 12f
                setTextColor(Color.BLACK)
                typeface = ResourcesCompat.getFont(context, R.font.solaimanlipi)
            }
            boxOfContent.addView(verses)
            linear1.addView(boxOfContent)

            val suraArabic = TextView(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 14f
                setTextColor(Color.BLACK)
                typeface = Typeface.DEFAULT_BOLD
            }
            linear1.addView(suraArabic)

            rootLayout.addView(linear1)
            
            // Store IDs as tags for later retrieval
            linear1.tag = "linear1"
            suraArabic.tag = "suraArabic"
            number.tag = "number"
            suraName.tag = "suraName"
            verses.tag = "verses"
            
            return rootLayout
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]

            if (item.containsKey("suraName")) {
                holder.suraName.text = item["suraName"]?.toString() ?: ""
                holder.verses.text = "${item["versess"]} | ${item["type"]}"
                holder.number.text = replaceArabicNumber(item["sura"]?.toString() ?: "")
                holder.suraArabic.text = item["suraArabic"]?.toString() ?: ""
            }

            holder.linear1.setOnClickListener {
                if (item["suraName"]?.toString() == "none") {
                    Toast.makeText(applicationContext, "বই যুক্ত করা হয়নি", Toast.LENGTH_SHORT).show()
                } else {
                    intent.setClass(applicationContext, TafsironlineviewActivity::class.java)
                    intent.putExtra("name", item["suraName"]?.toString() ?: "")
                    intent.putExtra("author", "${item["versess"]} | ${item["type"]}")
                    intent.putExtra("sura", item["sura"]?.toString() ?: "")
                    intent.putExtra("bookname", bookname.text.toString())
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int = data.size

        fun updateData(newData: ArrayList<HashMap<String, Any>>) {
            data = newData
            notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            initializeLogic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
    }
}
