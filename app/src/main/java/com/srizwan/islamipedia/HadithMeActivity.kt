package com.srizwan.islamipedia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.security.MessageDigest

// ─────────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────────
data class BookItem(
    val id: Int,
    val sequence: Int,
    val titleEn: String,
    val titleAr: String,
    val totalSection: Int,
    val totalHadith: Int,
    val originalPosition: Int = 0
)

data class SectionItem(
    val id: Int,
    val sequence: Int,
    val title: String,
    val titleAr: String,
    val totalHadith: Int,
    val rangeStart: Int,
    val rangeEnd: Int,
    val originalPosition: Int = 0
)

data class HadithItem(
    val hadithNumber: Int,
    val title: String,
    val descriptionAr: String,
    val description: String
)

// ─────────────────────────────────────────────────────────────────
// Page State
// ─────────────────────────────────────────────────────────────────
sealed class PageState {
    object Books : PageState()
    data class Sections(val bookId: Int, val bookTitle: String) : PageState()
    data class Hadith(
        val bookId: Int,
        val sectionId: Int,
        val bookTitle: String,
        val sectionTitle: String
    ) : PageState()
}

// ─────────────────────────────────────────────────────────────────
// Scroll position holder with better persistence
// ─────────────────────────────────────────────────────────────────
object ScrollState {
    var booksPosition: Int = 0
    var booksOffset: Int = 0
    val sectionsPositions = mutableMapOf<Int, Pair<Int, Int>>()
    val hadithPositions = mutableMapOf<String, Pair<Int, Int>>()
}

// ─────────────────────────────────────────────────────────────────
// In-memory Cache
// ─────────────────────────────────────────────────────────────────
object HadithCache {
    var books: List<BookItem>? = null
    var booksTimestamp: Long = 0
    val sections = mutableMapOf<Int, List<SectionItem>>()
    val sectionsTimestamp = mutableMapOf<Int, Long>()
    val hadith = mutableMapOf<String, List<HadithItem>>()
    val hadithTimestamp = mutableMapOf<String, Long>()

    fun clearAll() {
        books = null
        booksTimestamp = 0
        sections.clear()
        sectionsTimestamp.clear()
        hadith.clear()
        hadithTimestamp.clear()
    }
}

// ─────────────────────────────────────────────────────────────────
// Persisted "Downloaded Books" store (SharedPreferences)
// ─────────────────────────────────────────────────────────────────
object DownloadStore {
    private const val PREFS = "hadith_downloads_prefs"
    private const val KEY_DOWNLOADED = "downloaded_book_ids"

    fun getDownloaded(context: Context): MutableSet<Int> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getStringSet(KEY_DOWNLOADED, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toMutableSet()
    }

    private fun setDownloaded(context: Context, ids: Set<Int>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_DOWNLOADED, ids.map { it.toString() }.toSet()).apply()
    }

    fun markDownloaded(context: Context, bookId: Int) {
        val set = getDownloaded(context)
        set.add(bookId)
        setDownloaded(context, set)
    }

    fun unmarkDownloaded(context: Context, bookId: Int) {
        val set = getDownloaded(context)
        set.remove(bookId)
        setDownloaded(context, set)
    }
}

// ─────────────────────────────────────────────────────────────────
// Skeleton placeholder type (for shimmer loading effect)
// ─────────────────────────────────────────────────────────────────
enum class SkeletonType { BOOK, SECTION, HADITH }

// ─────────────────────────────────────────────────────────────────
// HTML / Plain-text helpers
// ─────────────────────────────────────────────────────────────────
fun String.toHtmlSpanned(): Spanned =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    else
        @Suppress("DEPRECATION") Html.fromHtml(this)

fun String.stripHtml(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString().trim()
    else
        @Suppress("DEPRECATION") Html.fromHtml(this).toString().trim()

fun String.containsHtml(): Boolean = contains(Regex("<[a-zA-Z][^>]*>"))

// ─────────────────────────────────────────────────────────────────
// JSON null-safe helper
// ─────────────────────────────────────────────────────────────────
fun JSONObject.safeString(key: String, fallback: String = ""): String {
    if (isNull(key)) return fallback
    val v = optString(key, fallback)
    return if (v == "null") fallback else v
}

// ─────────────────────────────────────────────────────────────────
// Main Activity
// ─────────────────────────────────────────────────────────────────
class HadithMeActivity : AppCompatActivity() {

    private val cacheDirName = "hadith_data"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var isNetworkAvailable = true
    private var isCurrentlyLoading = false
    private var currentRequestJob: Job? = null

    private lateinit var toolbarLayout: LinearLayout
    private lateinit var backButton: ImageView
    private lateinit var toolbarTitleView: TextView
    private lateinit var searchToggleBtn: ImageView
    private lateinit var searchContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var offlineIndicator: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var statusView: View
    private lateinit var statusText: TextView
    private lateinit var statusProgressBar: ProgressBar
    private lateinit var retryButton: Button
    private lateinit var fabSearchBtn: FrameLayout
    private lateinit var globalSearchOverlay: FrameLayout
    private lateinit var globalSearchInput: EditText
    private lateinit var globalSearchStatus: TextView
    private lateinit var globalSearchRecycler: RecyclerView
    private lateinit var globalSearchHint: TextView
    private lateinit var refreshButton: ImageView

    private var currentState: PageState = PageState.Books
    private var isSearchOpen = false
    private var isGlobalSearchOpen = false
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val globalSearchHandler = Handler(Looper.getMainLooper())
    private var globalSearchRunnable: Runnable? = null
    private val marqueeHandler = Handler(Looper.getMainLooper())

    private var currentBooks: List<BookItem> = emptyList()
    private var currentSections: List<SectionItem> = emptyList()
    private var currentHadithList: List<HadithItem> = emptyList()

    private var filteredBooks: List<BookItem> = emptyList()
    private var filteredSections: List<SectionItem> = emptyList()
    private var filteredHadith: List<HadithItem> = emptyList()

    private var isShowingCachedContent = false

    // ── Book download tracking ──────────────────────────────────
    private val downloadedBookIds = mutableSetOf<Int>()
    private val downloadingBookIds = mutableSetOf<Int>()
    private val downloadProgress = mutableMapOf<Int, Int>()
    private val downloadJobs = mutableMapOf<Int, Job>()

    // ── Guards against stale / out-of-order async results ───────
    private var loadGeneration = 0
    private var globalSearchGeneration = 0
    private val skeletonAnimators = mutableListOf<android.animation.Animator>()

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#01837A")
        window.navigationBarColor = Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = window.decorView.systemUiVisibility
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window.decorView.systemUiVisibility = flags
        }

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // ✅ FIX: buildUI() and setContentView() MUST come BEFORE
        //    checkNetworkState() and loadBooks() so all lateinit views
        //    are initialized before they are accessed.
        val rootLayout = buildUI()
        setContentView(rootLayout)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }

        File(filesDir, cacheDirName).mkdirs()

        downloadedBookIds.clear()
        downloadedBookIds.addAll(DownloadStore.getDownloaded(this))

        // ✅ These now run AFTER all views exist
        checkNetworkState()
        loadBooks()
    }

    private fun checkNetworkState() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        isNetworkAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }

        if (!isNetworkAvailable) {
            // ✅ offlineIndicator is guaranteed to exist here now
            offlineIndicator.visibility = View.VISIBLE
            offlineIndicator.text = "⚠️ অফলাইন মোড - ক্যাশে করা ডেটা দেখানো হচ্ছে"
        }
    }

    private fun buildUI(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // ── Toolbar ──────────────────────────────────────────────
        toolbarLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#01837A"))
            setPadding(dp(12), dp(14), dp(12), dp(14))
            elevation = dp(4).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        backButton = ImageView(this).apply {
            setImageResource(R.drawable.back)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24))
            setColorFilter(Color.WHITE)
            setOnClickListener { handleBackPress() }
        }
        toolbarTitleView = TextView(this).apply {
            text = "হাদিস সমগ্র"
            textSize = 19f
            setTextColor(Color.WHITE)
            typeface = getBengaliTypeface()
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { setMargins(dp(10), 0, dp(10), 0) }
        }
        searchToggleBtn = ImageView(this).apply {
            setImageResource(R.drawable.search)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24))
            setColorFilter(Color.WHITE)
            setOnClickListener { toggleSearch() }
        }
        refreshButton = ImageView(this).apply {
            setImageResource(R.drawable.refresh)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply { marginStart = dp(8) }
            setColorFilter(Color.WHITE)
            visibility = View.GONE
            setOnClickListener { refreshCurrentPage() }
        }
        toolbarLayout.addView(backButton)
        toolbarLayout.addView(toolbarTitleView)
        toolbarLayout.addView(searchToggleBtn)
        toolbarLayout.addView(refreshButton)
        root.addView(toolbarLayout)

        // ── Search Bar ────────────────────────────────────────────
        searchContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            elevation = dp(3).toFloat()
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        searchInput = EditText(this).apply {
            hint = "খুঁজুন..."
            typeface = getBengaliTypeface()
            textSize = 16f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.parseColor("#999999"))
            background = createRoundedBg(Color.WHITE, Color.parseColor("#01837A"), dp(2), dp(24))
            setPadding(dp(16), dp(10), dp(16), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
                    searchRunnable = Runnable { performSearch(s?.toString() ?: "") }
                    searchRunnable?.let { searchHandler.postDelayed(it, 300) }
                }
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            })
        }
        searchContainer.addView(searchInput)
        root.addView(searchContainer)

        // ── Offline Indicator ─────────────────────────────────────
        // ✅ offlineIndicator is initialized HERE inside buildUI()
        offlineIndicator = TextView(this).apply {
            text = "⚠️ অফলাইন মোড - ক্যাশে করা ডেটা দেখানো হচ্ছে"
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = getBengaliTypeface()
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#FF9800"))
            setPadding(dp(8), dp(5), dp(8), dp(5))
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        root.addView(offlineIndicator)

        // ── Content Frame ─────────────────────────────────────────
        val contentFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@HadithMeActivity)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(dp(12), dp(12), dp(12), dp(80))
            clipToPadding = false
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        saveScrollPosition()
                    }
                }
            })
        }
        contentFrame.addView(recyclerView)

        // ── Status Overlay (Loading / Error) ──────────────────────
        val statusOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        statusProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            indeterminateTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#01837A"))
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(56)).apply {
                bottomMargin = dp(16)
                gravity = Gravity.CENTER_HORIZONTAL
            }
            visibility = View.GONE
        }
        statusOverlay.addView(statusProgressBar)

        statusText = TextView(this).apply {
            textSize = 17f
            typeface = getBengaliTypeface()
            gravity = Gravity.CENTER
            setPadding(dp(24), 0, dp(24), 0)
        }
        retryButton = Button(this).apply {
            text = "আবার চেষ্টা করুন"
            typeface = getBengaliTypeface()
            setTextColor(Color.WHITE)
            background = createRoundedSolid(Color.parseColor("#01837A"), dp(24))
            setPadding(dp(20), dp(10), dp(20), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) }
            visibility = View.GONE
        }
        statusOverlay.addView(statusText)
        statusOverlay.addView(retryButton)
        statusView = statusOverlay
        contentFrame.addView(statusOverlay)

        // ── FAB Search Button ─────────────────────────────────────
        fabSearchBtn = FrameLayout(this).apply {
            val size = dp(52)
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.BOTTOM or Gravity.END)
                .apply { setMargins(0, 0, dp(20), dp(20)) }
            background = createRoundedSolid(Color.parseColor("#01837A"), size / 2)
            elevation = dp(6).toFloat()
            setOnClickListener { openGlobalSearch() }
        }
        fabSearchBtn.addView(ImageView(this).apply {
            setImageResource(R.drawable.search)
            setColorFilter(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER)
        })
        contentFrame.addView(fabSearchBtn)
        root.addView(contentFrame)

        // ── Global Search Overlay ─────────────────────────────────
        globalSearchOverlay = buildGlobalSearchOverlay()
        val decorFrame = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        decorFrame.addView(root)
        decorFrame.addView(globalSearchOverlay)
        return decorFrame
    }

    private fun buildGlobalSearchOverlay(): FrameLayout {
        val overlay = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            visibility = View.GONE
        }
        val popup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#01837A"))
            setPadding(dp(12), dp(14), dp(12), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        header.addView(ImageView(this).apply {
            setImageResource(R.drawable.back)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24))
            setOnClickListener { closeGlobalSearch() }
        })
        header.addView(TextView(this).apply {
            text = "সম্পূর্ণ হাদিস সার্চ"
            textSize = 17f
            setTextColor(Color.WHITE)
            typeface = getBengaliTypeface()
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { setMargins(dp(10), 0, 0, 0) }
        })

        val inputWrap = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#F0FFFE"))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        globalSearchInput = EditText(this).apply {
            hint = "হাদিস নম্বর, শিরোনাম বা বাংলা/আরবি লিখুন..."
            typeface = getBengaliTypeface()
            textSize = 16f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.parseColor("#999999"))
            background = createRoundedBg(Color.WHITE, Color.parseColor("#01837A"), dp(2), dp(24))
            setPadding(dp(16), dp(10), dp(16), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val query = s?.toString()?.trim() ?: ""
                    globalSearchRunnable?.let { globalSearchHandler.removeCallbacks(it) }
                    when {
                        query.length < 2 -> {
                            globalSearchGeneration++ // invalidate any in-flight search
                            globalSearchStatus.text = "কমপক্ষে ২টি অক্ষর লিখুন..."
                            showGlobalHint("🔍 ডাউনলোড করা হাদিস বই থেকে সার্চ করুন\n\nহাদিস নম্বর, বাংলা অনুবাদ বা আরবি টেক্সট দিয়ে সার্চ করা যাবে")
                        }
                        else -> {
                            globalSearchStatus.text = "⏳ টাইপ করা থামলে সার্চ শুরু হবে..."
                            globalSearchRunnable = Runnable { performGlobalSearchFromDownloaded(query) }
                            globalSearchRunnable?.let { globalSearchHandler.postDelayed(it, 600) }
                        }
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        inputWrap.addView(globalSearchInput)

        globalSearchStatus = TextView(this).apply {
            text = "সার্চ করতে টাইপ করুন..."
            textSize = 13f
            setTextColor(Color.parseColor("#666666"))
            typeface = getBengaliTypeface()
            setBackgroundColor(Color.parseColor("#F9F9F9"))
            setPadding(dp(15), dp(8), dp(15), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val resultsFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        globalSearchRecycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@HadithMeActivity)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(dp(12), dp(10), dp(12), dp(12))
            clipToPadding = false
            visibility = View.GONE
            addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: android.view.MotionEvent): Boolean {
                    if (e.action == android.view.MotionEvent.ACTION_DOWN) hideKeyboard(globalSearchInput)
                    return false
                }
            })
        }
        globalSearchHint = TextView(this).apply {
            text = "🔍 ডাউনলোড করা হাদিস বই থেকে সার্চ করুন\n\nহাদিস নম্বর, বাংলা অনুবাদ বা আরবি টেক্সট দিয়ে সার্চ করা যাবে"
            textSize = 15f
            typeface = getBengaliTypeface()
            setTextColor(Color.parseColor("#999999"))
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(40), dp(24), dp(40))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP
            )
        }
        resultsFrame.addView(globalSearchRecycler)
        resultsFrame.addView(globalSearchHint)
        popup.addView(header)
        popup.addView(inputWrap)
        popup.addView(globalSearchStatus)
        popup.addView(resultsFrame)
        overlay.addView(popup)
        return overlay
    }

    // ─────────────────────────────────────────────────────────────
    // Refresh current page
    // ─────────────────────────────────────────────────────────────
    private fun refreshCurrentPage() {
        if (!isNetworkAvailable) {
            Toast.makeText(this, "ইন্টারনেট সংযোগ নেই", Toast.LENGTH_SHORT).show()
            return
        }
        when (val state = currentState) {
            is PageState.Books -> {
                HadithCache.books = null
                loadBooks()
            }
            is PageState.Sections -> {
                HadithCache.sections.remove(state.bookId)
                loadSections(state.bookId, state.bookTitle)
            }
            is PageState.Hadith -> {
                HadithCache.hadith.remove("${state.bookId}_${state.sectionId}")
                loadHadith(state.bookId, state.sectionId, state.bookTitle, state.sectionTitle)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Keyboard helper
    // ─────────────────────────────────────────────────────────────
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateToolbar(title: String) {
        toolbarTitleView.text = title
    }

    // ─────────────────────────────────────────────────────────────
    // Status helpers
    // ─────────────────────────────────────────────────────────────
    private fun showLoading() {
        isCurrentlyLoading = true
        recyclerView.visibility = View.GONE
        statusView.visibility = View.VISIBLE
        statusProgressBar.visibility = View.VISIBLE
        statusText.text = "লোড হচ্ছে..."
        statusText.setTextColor(Color.parseColor("#01837A"))
        retryButton.visibility = View.GONE
        refreshButton.visibility = View.GONE
    }

    // ── Skeleton (shimmer placeholder) loading ──────────────────
    private fun clearSkeletonAnimators() {
        skeletonAnimators.forEach { it.cancel() }
        skeletonAnimators.clear()
    }

    private fun showSkeleton(type: SkeletonType) {
        isCurrentlyLoading = true
        statusView.visibility = View.GONE
        statusProgressBar.visibility = View.GONE
        retryButton.visibility = View.GONE
        refreshButton.visibility = View.GONE
        clearSkeletonAnimators()
        recyclerView.visibility = View.VISIBLE
        recyclerView.adapter = SkeletonAdapter(type)
    }

    private fun showError(message: String, retry: (() -> Unit)? = null) {
        isCurrentlyLoading = false
        clearSkeletonAnimators()
        recyclerView.visibility = View.GONE
        statusView.visibility = View.VISIBLE
        statusProgressBar.visibility = View.GONE
        statusText.text = "❌ $message"
        statusText.setTextColor(Color.parseColor("#E74C3C"))
        retryButton.visibility = if (retry != null) View.VISIBLE else View.GONE
        retry?.let { r -> retryButton.setOnClickListener { r() } }
        refreshButton.visibility = if (isShowingCachedContent) View.VISIBLE else View.GONE
    }

    private fun showContent() {
        isCurrentlyLoading = false
        clearSkeletonAnimators()
        statusView.visibility = View.GONE
        statusProgressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        refreshButton.visibility = if (!isNetworkAvailable || isShowingCachedContent) View.VISIBLE else View.GONE
    }

    // ─────────────────────────────────────────────────────────────
    // Cache helpers
    // ─────────────────────────────────────────────────────────────
    private fun cacheFileName(key: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(key.toByteArray()).joinToString("") { "%02x".format(it) } + ".json"
    }

    private fun getCachedData(key: String): String? {
        val f = File(File(filesDir, cacheDirName), cacheFileName(key))
        return if (f.exists()) f.readText() else null
    }

    private fun cacheData(key: String, data: String) {
        val dir = File(filesDir, cacheDirName)
        if (!dir.exists()) dir.mkdirs()
        File(dir, cacheFileName(key)).writeText(data)
    }

    private suspend fun fetchJson(url: String, cacheKey: String): String {
        getCachedData(cacheKey)?.let { cached ->
            withContext(Dispatchers.Main) {
                offlineIndicator.visibility = View.GONE
                isShowingCachedContent = true
            }
            return cached
        }
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                cacheData(cacheKey, text)
                withContext(Dispatchers.Main) {
                    offlineIndicator.visibility = View.GONE
                    isShowingCachedContent = false
                    isNetworkAvailable = true
                }
                text
            } catch (e: Exception) {
                val cached = getCachedData(cacheKey)
                if (cached != null) {
                    withContext(Dispatchers.Main) {
                        offlineIndicator.visibility = View.VISIBLE
                        offlineIndicator.text = "⚠️ অফলাইন মোড - ক্যাশে করা ডেটা দেখানো হচ্ছে"
                        isShowingCachedContent = true
                        isNetworkAvailable = false
                    }
                    return@withContext cached
                } else {
                    withContext(Dispatchers.Main) {
                        offlineIndicator.visibility = View.VISIBLE
                        offlineIndicator.text = "⚠️ নেটওয়ার্ক নেই এবং ক্যাশে ডেটা পাওয়া যায়নি"
                        isNetworkAvailable = false
                    }
                    throw e
                }
            }
        }
    }

    private fun toBangla(num: Int): String {
        val d = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return num.toString().map { if (it.isDigit()) d[it - '0'] else it }.joinToString("")
    }

    // ─────────────────────────────────────────────────────────────
    // Scroll helpers
    // ─────────────────────────────────────────────────────────────
    private fun saveScrollPosition() {
        if (!::recyclerView.isInitialized || isCurrentlyLoading) return
        val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val position = lm.findFirstVisibleItemPosition()
        if (position < 0) return
        val view = lm.findViewByPosition(position)
        val offset = view?.top ?: 0
        when (currentState) {
            is PageState.Books -> {
                ScrollState.booksPosition = position
                ScrollState.booksOffset = offset
            }
            is PageState.Sections -> {
                val s = currentState as PageState.Sections
                ScrollState.sectionsPositions[s.bookId] = Pair(position, offset)
            }
            is PageState.Hadith -> {
                val s = currentState as PageState.Hadith
                ScrollState.hadithPositions["${s.bookId}_${s.sectionId}"] = Pair(position, offset)
            }
        }
    }

    private fun restoreScrollPosition() {
        if (!::recyclerView.isInitialized) return
        val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val pair = when (currentState) {
            is PageState.Books -> Pair(ScrollState.booksPosition, ScrollState.booksOffset)
            is PageState.Sections -> {
                val s = currentState as PageState.Sections
                ScrollState.sectionsPositions[s.bookId] ?: Pair(0, 0)
            }
            is PageState.Hadith -> {
                val s = currentState as PageState.Hadith
                ScrollState.hadithPositions["${s.bookId}_${s.sectionId}"] ?: Pair(0, 0)
            }
        }
        if (pair.first > 0 && pair.first < (recyclerView.adapter?.itemCount ?: 0)) {
            lm.scrollToPositionWithOffset(pair.first, pair.second)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RecyclerView touch — hide keyboard on scroll tap
    // ─────────────────────────────────────────────────────────────
    private fun attachKeyboardHideOnTouch() {
        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: android.view.MotionEvent): Boolean {
                if (e.action == android.view.MotionEvent.ACTION_DOWN && isSearchOpen)
                    hideKeyboard(searchInput)
                return false
            }
        })
    }

    // ─────────────────────────────────────────────────────────────
    // Load Books
    // ─────────────────────────────────────────────────────────────
    private fun loadBooks() {
        saveScrollPosition()
        currentState = PageState.Books
        updateToolbar("হাদিস সমগ্র")
        closeSearchSilently()
        isShowingCachedContent = false
        val gen = ++loadGeneration

        val memBooks = HadithCache.books
        if (memBooks != null) {
            currentBooks = memBooks
            filteredBooks = memBooks
            showContent()
            recyclerView.adapter = BookAdapter(filteredBooks) { book ->
                saveScrollPosition()
                loadSections(book.id, book.titleEn)
            }
            attachKeyboardHideOnTouch()
            restoreScrollPosition()
            return
        }

        showSkeleton(SkeletonType.BOOK)
        currentRequestJob?.cancel()
        currentRequestJob = scope.launch {
            try {
                val json = fetchJson(
                    "https://cdn.jsdelivr.net/gh/SunniPedia/sunnipedia@main/hadith-books/book/book-title.json",
                    "hadith_books_list"
                )
                if (gen != loadGeneration) return@launch
                val books = parseBooks(json)
                HadithCache.books = books
                HadithCache.booksTimestamp = System.currentTimeMillis()
                currentBooks = books
                filteredBooks = books
                showContent()
                recyclerView.adapter = BookAdapter(filteredBooks) { book ->
                    saveScrollPosition()
                    loadSections(book.id, book.titleEn)
                }
                attachKeyboardHideOnTouch()
                restoreScrollPosition()
                isShowingCachedContent = false
            } catch (e: Exception) {
                if (gen != loadGeneration) return@launch
                val cachedJson = getCachedData("hadith_books_list")
                if (cachedJson != null) {
                    val books = parseBooks(cachedJson)
                    HadithCache.books = books
                    currentBooks = books
                    filteredBooks = books
                    showContent()
                    recyclerView.adapter = BookAdapter(filteredBooks) { book ->
                        saveScrollPosition()
                        loadSections(book.id, book.titleEn)
                    }
                    attachKeyboardHideOnTouch()
                    restoreScrollPosition()
                    isShowingCachedContent = true
                    offlineIndicator.visibility = View.VISIBLE
                } else {
                    showError("বই লোড করতে সমস্যা হয়েছে") { loadBooks() }
                }
            }
        }
    }

    private fun parseBooks(json: String): List<BookItem> {
        val arr = JSONArray(json)
        return (0 until arr.length()).mapIndexed { index, _ ->
            val o = arr.getJSONObject(index)
            BookItem(
                id               = o.optInt("id"),
                sequence         = o.optInt("sequence"),
                titleEn          = o.safeString("title_en", o.safeString("title", "")),
                titleAr          = o.safeString("title_ar"),
                totalSection     = o.optInt("total_section"),
                totalHadith      = o.optInt("total_hadith"),
                originalPosition = index
            )
        }.sortedBy { it.sequence }
    }

    // ─────────────────────────────────────────────────────────────
    // Load Sections
    // ─────────────────────────────────────────────────────────────
    private fun loadSections(bookId: Int, bookTitle: String) {
        saveScrollPosition()
        currentState = PageState.Sections(bookId, bookTitle)
        updateToolbar(bookTitle)
        closeSearchSilently()
        isShowingCachedContent = false
        val gen = ++loadGeneration

        val memSections = HadithCache.sections[bookId]
        if (memSections != null) {
            currentSections = memSections
            filteredSections = memSections
            showContent()
            recyclerView.adapter = SectionAdapter(filteredSections) { section ->
                saveScrollPosition()
                loadHadith(bookId, section.id, bookTitle, section.title)
            }
            attachKeyboardHideOnTouch()
            restoreScrollPosition()
            return
        }

        showSkeleton(SkeletonType.SECTION)
        currentRequestJob?.cancel()
        currentRequestJob = scope.launch {
            try {
                val json = fetchJson(
                    "https://cdn.jsdelivr.net/gh/SunniPedia/sunnipedia@main/hadith-books/book/$bookId/title.json",
                    "sections_$bookId"
                )
                if (gen != loadGeneration) return@launch
                val sections = parseSections(json)
                HadithCache.sections[bookId] = sections
                HadithCache.sectionsTimestamp[bookId] = System.currentTimeMillis()
                currentSections = sections
                filteredSections = sections
                showContent()
                recyclerView.adapter = SectionAdapter(filteredSections) { section ->
                    saveScrollPosition()
                    loadHadith(bookId, section.id, bookTitle, section.title)
                }
                attachKeyboardHideOnTouch()
                restoreScrollPosition()
                isShowingCachedContent = false
            } catch (e: Exception) {
                if (gen != loadGeneration) return@launch
                val cachedJson = getCachedData("sections_$bookId")
                if (cachedJson != null) {
                    val sections = parseSections(cachedJson)
                    HadithCache.sections[bookId] = sections
                    currentSections = sections
                    filteredSections = sections
                    showContent()
                    recyclerView.adapter = SectionAdapter(filteredSections) { section ->
                        saveScrollPosition()
                        loadHadith(bookId, section.id, bookTitle, section.title)
                    }
                    attachKeyboardHideOnTouch()
                    restoreScrollPosition()
                    isShowingCachedContent = true
                    offlineIndicator.visibility = View.VISIBLE
                } else {
                    showError("অধ্যায় লোড করতে সমস্যা হয়েছে") { loadSections(bookId, bookTitle) }
                }
            }
        }
    }

    private fun parseSections(json: String): List<SectionItem> {
        val arr = JSONArray(json)
        return (0 until arr.length()).mapIndexed { index, _ ->
            val o = arr.getJSONObject(index)
            SectionItem(
                id               = o.optInt("id"),
                sequence         = o.optInt("sequence"),
                title            = o.safeString("title", o.safeString("title_en", "")),
                titleAr          = o.safeString("title_ar"),
                totalHadith      = o.optInt("total_hadith"),
                rangeStart       = o.optInt("range_start"),
                rangeEnd         = o.optInt("range_end"),
                originalPosition = index
            )
        }.sortedBy { it.sequence }
    }

    // ─────────────────────────────────────────────────────────────
    // Load Hadith
    // ─────────────────────────────────────────────────────────────
    private fun loadHadith(bookId: Int, sectionId: Int, bookTitle: String, sectionTitle: String) {
        saveScrollPosition()
        currentState = PageState.Hadith(bookId, sectionId, bookTitle, sectionTitle)
        updateToolbar(sectionTitle)
        closeSearchSilently()
        isShowingCachedContent = false
        val key = "${bookId}_$sectionId"
        val gen = ++loadGeneration

        val memHadith = HadithCache.hadith[key]
        if (memHadith != null) {
            currentHadithList = memHadith
            filteredHadith = memHadith
            showContent()
            recyclerView.adapter = HadithAdapter(
                filteredHadith, bookTitle,
                onCopy  = { h -> copyHadith(h, bookTitle, sectionTitle) },
                onShare = { h -> shareHadith(h, bookTitle, sectionTitle) }
            )
            attachKeyboardHideOnTouch()
            restoreScrollPosition()
            return
        }

        showSkeleton(SkeletonType.HADITH)
        currentRequestJob?.cancel()
        currentRequestJob = scope.launch {
            try {
                val json = fetchJson(
                    "https://cdn.jsdelivr.net/gh/SunniPedia/sunnipedia@main/hadith-books/book/$bookId/hadith/$sectionId.json",
                    "hadith_${bookId}_$sectionId"
                )
                if (gen != loadGeneration) return@launch
                val hadithList = parseHadith(json)
                HadithCache.hadith[key] = hadithList
                HadithCache.hadithTimestamp[key] = System.currentTimeMillis()
                currentHadithList = hadithList
                filteredHadith = hadithList
                showContent()
                recyclerView.adapter = HadithAdapter(
                    filteredHadith, bookTitle,
                    onCopy  = { h -> copyHadith(h, bookTitle, sectionTitle) },
                    onShare = { h -> shareHadith(h, bookTitle, sectionTitle) }
                )
                attachKeyboardHideOnTouch()
                restoreScrollPosition()
                isShowingCachedContent = false
            } catch (e: Exception) {
                if (gen != loadGeneration) return@launch
                val cachedJson = getCachedData("hadith_${bookId}_$sectionId")
                if (cachedJson != null) {
                    val hadithList = parseHadith(cachedJson)
                    HadithCache.hadith[key] = hadithList
                    currentHadithList = hadithList
                    filteredHadith = hadithList
                    showContent()
                    recyclerView.adapter = HadithAdapter(
                        filteredHadith, bookTitle,
                        onCopy  = { h -> copyHadith(h, bookTitle, sectionTitle) },
                        onShare = { h -> shareHadith(h, bookTitle, sectionTitle) }
                    )
                    attachKeyboardHideOnTouch()
                    restoreScrollPosition()
                    isShowingCachedContent = true
                    offlineIndicator.visibility = View.VISIBLE
                } else {
                    showError("হাদিস লোড করতে সমস্যা হয়েছে") {
                        loadHadith(bookId, sectionId, bookTitle, sectionTitle)
                    }
                }
            }
        }
    }

    private fun parseHadith(json: String): List<HadithItem> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { arr.getJSONObject(it) }.map { o ->
            HadithItem(
                hadithNumber  = o.optInt("hadith_number"),
                title         = o.safeString("title"),
                descriptionAr = o.safeString("description_ar"),
                description   = o.safeString("description")
            )
        }.sortedBy { it.hadithNumber }
    }

    // ─────────────────────────────────────────────────────────────
    // Book Download Manager
    // Downloads a book's full section list + every section's hadith
    // and stores it in the on-disk cache + in-memory cache so the book
    // is fully available offline and searchable from global search.
    // ─────────────────────────────────────────────────────────────
    private fun startBookDownload(book: BookItem) {
        if (downloadingBookIds.contains(book.id) || downloadedBookIds.contains(book.id)) return
        if (!isNetworkAvailable) {
            Toast.makeText(this, "ডাউনলোড করতে ইন্টারনেট সংযোগ প্রয়োজন", Toast.LENGTH_SHORT).show()
            return
        }
        downloadingBookIds.add(book.id)
        downloadProgress[book.id] = 0
        notifyBookRowChanged(book.id)

        val job = scope.launch {
            try {
                val sectionsJson = fetchJson(
                    "https://cdn.jsdelivr.net/gh/SunniPedia/sunnipedia@main/hadith-books/book/${book.id}/title.json",
                    "sections_${book.id}"
                )
                val sections = parseSections(sectionsJson)
                HadithCache.sections[book.id] = sections
                HadithCache.sectionsTimestamp[book.id] = System.currentTimeMillis()

                val total = sections.size.coerceAtLeast(1)
                var done = 0
                downloadProgress[book.id] = 2
                notifyBookRowChanged(book.id)

                for (section in sections) {
                    ensureActive()
                    val hadithJson = fetchJson(
                        "https://cdn.jsdelivr.net/gh/SunniPedia/sunnipedia@main/hadith-books/book/${book.id}/hadith/${section.id}.json",
                        "hadith_${book.id}_${section.id}"
                    )
                    val list = parseHadith(hadithJson)
                    val k = "${book.id}_${section.id}"
                    HadithCache.hadith[k] = list
                    HadithCache.hadithTimestamp[k] = System.currentTimeMillis()
                    done++
                    downloadProgress[book.id] = ((done * 100) / total).coerceIn(0, 99)
                    notifyBookRowChanged(book.id)
                }

                downloadingBookIds.remove(book.id)
                downloadProgress.remove(book.id)
                downloadedBookIds.add(book.id)
                DownloadStore.markDownloaded(this@HadithMeActivity, book.id)
                notifyBookRowChanged(book.id)
                Toast.makeText(
                    this@HadithMeActivity,
                    "✅ \"${book.titleEn.ifBlank { book.titleAr }}\" সম্পূর্ণ ডাউনলোড হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: CancellationException) {
                // silently cancelled (activity destroyed) — nothing to clean up
            } catch (e: Exception) {
                downloadingBookIds.remove(book.id)
                downloadProgress.remove(book.id)
                notifyBookRowChanged(book.id)
                Toast.makeText(
                    this@HadithMeActivity,
                    "❌ ডাউনলোড ব্যর্থ হয়েছে, আবার চেষ্টা করুন",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                downloadJobs.remove(book.id)
            }
        }
        downloadJobs[book.id] = job
    }

    private fun notifyBookRowChanged(bookId: Int) {
        if (currentState !is PageState.Books) return
        val idx = filteredBooks.indexOfFirst { it.id == bookId }
        if (idx >= 0) recyclerView.adapter?.notifyItemChanged(idx)
    }

    // ─────────────────────────────────────────────────────────────
    // Search (inline — per page)
    // ─────────────────────────────────────────────────────────────
    private fun toggleSearch() {
        if (isSearchOpen) {
            closeSearch()
        } else {
            isSearchOpen = true
            searchContainer.visibility = View.VISIBLE
            searchInput.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun closeSearch() {
        isSearchOpen = false
        searchContainer.visibility = View.GONE
        searchInput.setText("")
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        searchRunnable = null
        hideKeyboard(searchInput)
        filteredBooks = currentBooks
        filteredSections = currentSections
        filteredHadith = currentHadithList
        restoreFullList()
    }

    private fun closeSearchSilently() {
        isSearchOpen = false
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        searchRunnable = null
        searchContainer.visibility = View.GONE
        searchInput.setText("")
        filteredBooks = currentBooks
        filteredSections = currentSections
        filteredHadith = currentHadithList
    }

    private fun restoreFullList() {
        showContent()
        when (val s = currentState) {
            is PageState.Books -> {
                recyclerView.adapter = BookAdapter(filteredBooks) { book ->
                    saveScrollPosition()
                    loadSections(book.id, book.titleEn)
                }
            }
            is PageState.Sections -> {
                recyclerView.adapter = SectionAdapter(filteredSections) { section ->
                    saveScrollPosition()
                    loadHadith(s.bookId, section.id, s.bookTitle, section.title)
                }
            }
            is PageState.Hadith -> {
                recyclerView.adapter = HadithAdapter(
                    filteredHadith, s.bookTitle,
                    onCopy  = { h -> copyHadith(h, s.bookTitle, s.sectionTitle) },
                    onShare = { h -> shareHadith(h, s.bookTitle, s.sectionTitle) }
                )
            }
        }
        restoreScrollPosition()
    }

    private fun performSearch(query: String) {
        showContent()
        val term = query.lowercase().trim()
        if (term.isBlank()) {
            filteredBooks = currentBooks
            filteredSections = currentSections
            filteredHadith = currentHadithList
            restoreFullList()
            return
        }
        when (val s = currentState) {
            is PageState.Books -> {
                filteredBooks = currentBooks.filter { b ->
                    b.titleEn.lowercase().contains(term) ||
                    b.titleAr.contains(term) ||
                    b.id.toString().contains(term)
                }
                recyclerView.adapter = BookAdapter(filteredBooks) { book ->
                    saveScrollPosition()
                    loadSections(book.id, book.titleEn)
                }
                if (filteredBooks.isEmpty()) showEmptySearchResult()
            }
            is PageState.Sections -> {
                filteredSections = currentSections.filter { sec ->
                    sec.title.lowercase().contains(term) ||
                    sec.titleAr.contains(term) ||
                    sec.id.toString().contains(term)
                }
                recyclerView.adapter = SectionAdapter(filteredSections) { section ->
                    saveScrollPosition()
                    loadHadith(s.bookId, section.id, s.bookTitle, section.title)
                }
                if (filteredSections.isEmpty()) showEmptySearchResult()
            }
            is PageState.Hadith -> {
                filteredHadith = currentHadithList.filter { h ->
                    h.hadithNumber.toString().contains(term) ||
                    h.title.stripHtml().lowercase().contains(term) ||
                    h.description.stripHtml().lowercase().contains(term) ||
                    h.descriptionAr.contains(term)
                }
                recyclerView.adapter = HadithAdapter(
                    filteredHadith, s.bookTitle,
                    onCopy  = { h -> copyHadith(h, s.bookTitle, s.sectionTitle) },
                    onShare = { h -> shareHadith(h, s.bookTitle, s.sectionTitle) }
                )
                if (filteredHadith.isEmpty()) showEmptySearchResult()
            }
        }
    }

    private fun showEmptySearchResult() {
        Toast.makeText(this, "কোনো ফলাফল পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
    }

    // ─────────────────────────────────────────────────────────────
    // Global Search
    // ─────────────────────────────────────────────────────────────
    private fun openGlobalSearch() {
        isGlobalSearchOpen = true
        globalSearchOverlay.visibility = View.VISIBLE
        globalSearchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(globalSearchInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun closeGlobalSearch() {
        isGlobalSearchOpen = false
        globalSearchRunnable?.let { globalSearchHandler.removeCallbacks(it) }
        globalSearchRunnable = null
        globalSearchGeneration++ // invalidate any in-flight search
        globalSearchOverlay.visibility = View.GONE
        globalSearchInput.setText("")
        globalSearchStatus.text = "সার্চ করতে টাইপ করুন..."
        showGlobalHint("🔍 ডাউনলোড করা হাদিস বই থেকে সার্চ করুন\n\nহাদিস নম্বর, বাংলা অনুবাদ বা আরবি টেক্সট দিয়ে সার্চ করা যাবে")
        hideKeyboard(globalSearchInput)
    }

    private fun showGlobalHint(msg: String) {
        globalSearchHint.text = msg
        globalSearchHint.visibility = View.VISIBLE
        globalSearchRecycler.visibility = View.GONE
        globalSearchRecycler.adapter = null
    }

    // Searches only the books the user has explicitly downloaded. Sections/hadith
    // are read from memory cache when available, otherwise lazily parsed from the
    // on-disk cache written during download — so a fresh app launch still works.
    private fun performGlobalSearchFromDownloaded(query: String) {
        if (downloadedBookIds.isEmpty()) {
            globalSearchGeneration++
            globalSearchStatus.text = "⚠️ কোনো বই ডাউনলোড করা হয়নি।"
            showGlobalHint("😔 এখনো কোনো বই ডাউনলোড করা হয়নি।\n\nবই তালিকা থেকে ডাউনলোড বাটনে চেপে একটি বই ডাউনলোড করুন, তারপর সার্চ করুন।")
            return
        }

        val gen = ++globalSearchGeneration
        globalSearchHint.visibility = View.GONE
        globalSearchRecycler.visibility = View.GONE
        globalSearchStatus.text = "🔍 ডাউনলোড করা বই থেকে অনুসন্ধান চলছে..."

        val booksSource = currentBooks.ifEmpty { HadithCache.books ?: emptyList() }
        val targetBooks = booksSource.filter { downloadedBookIds.contains(it.id) }

        scope.launch(Dispatchers.Default) {
            val results = mutableListOf<GlobalSearchResult>()
            val term = query.lowercase()
            var totalHadith = 0
            var booksSearched = 0

            for (book in targetBooks) {
                if (gen != globalSearchGeneration) return@launch

                val sections = HadithCache.sections[book.id] ?: run {
                    val cachedSectionsJson = getCachedData("sections_${book.id}")
                    if (cachedSectionsJson != null) {
                        val parsed = parseSections(cachedSectionsJson)
                        HadithCache.sections[book.id] = parsed
                        parsed
                    } else emptyList()
                }

                var bookHasData = false
                for (section in sections) {
                    if (gen != globalSearchGeneration) return@launch
                    val k = "${book.id}_${section.id}"
                    val hadithList = HadithCache.hadith[k] ?: run {
                        val cachedHadithJson = getCachedData("hadith_${book.id}_${section.id}")
                        if (cachedHadithJson != null) {
                            val parsed = parseHadith(cachedHadithJson)
                            HadithCache.hadith[k] = parsed
                            parsed
                        } else emptyList()
                    }
                    if (hadithList.isEmpty()) continue
                    bookHasData = true
                    totalHadith += hadithList.size
                    hadithList.filter { h ->
                        h.hadithNumber.toString().contains(term) ||
                        h.title.stripHtml().lowercase().contains(term) ||
                        h.description.stripHtml().lowercase().contains(term) ||
                        h.descriptionAr.contains(term)
                    }.forEach { h ->
                        results.add(
                            GlobalSearchResult(
                                h,
                                book.titleEn.ifBlank { book.titleAr },
                                book.id,
                                section.title,
                                section.id
                            )
                        )
                    }
                }
                if (bookHasData) {
                    booksSearched++
                    val snap = booksSearched
                    val rSnap = results.size
                    if (gen == globalSearchGeneration) {
                        withContext(Dispatchers.Main) {
                            if (gen == globalSearchGeneration) {
                                globalSearchStatus.text =
                                    "🔍 ${toBangla(snap)} টি ডাউনলোড করা বই দেখা হয়েছে — ${toBangla(rSnap)} টি ফলাফল"
                            }
                        }
                    }
                }
            }

            if (gen != globalSearchGeneration) return@launch
            withContext(Dispatchers.Main) {
                if (gen != globalSearchGeneration) return@withContext
                when {
                    totalHadith == 0 -> {
                        globalSearchStatus.text = "ডাউনলোড করা বইয়ে কোনো হাদিস ডাটা নেই"
                        showGlobalHint("😔 ডাউনলোড করা বইগুলোর ডাটা এখনো প্রস্তুত হয়নি।\nডাউনলোড সম্পূর্ণ হওয়া পর্যন্ত অপেক্ষা করুন।")
                    }
                    results.isEmpty() -> {
                        globalSearchStatus.text = "মোট ${toBangla(totalHadith)} টি হাদিস — কোনো ফলাফল নেই"
                        showGlobalHint("😔 \"$query\" এর জন্য কোনো হাদিস পাওয়া যায়নি।")
                    }
                    else -> {
                        globalSearchStatus.text = "✅ ${toBangla(results.size)} টি হাদিস পাওয়া গেছে"
                        globalSearchHint.visibility = View.GONE
                        globalSearchRecycler.visibility = View.VISIBLE
                        globalSearchRecycler.adapter = GlobalSearchAdapter(
                            results,
                            onCopy  = { r -> copyHadith(r.hadith, r.bookTitle, r.sectionTitle) },
                            onShare = { r -> shareHadith(r.hadith, r.bookTitle, r.sectionTitle) }
                        )
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Copy / Share
    // ─────────────────────────────────────────────────────────────
    private fun copyHadith(hadith: HadithItem, bookTitle: String, sectionTitle: String) {
        val text = buildPlainText(hadith, bookTitle, sectionTitle, withAppLink = false)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("হাদিস", text))
        Toast.makeText(this, "কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    private fun shareHadith(hadith: HadithItem, bookTitle: String, sectionTitle: String) {
        val text = buildPlainText(hadith, bookTitle, sectionTitle, withAppLink = true)
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                "শেয়ার করুন"
            )
        )
    }

    private fun buildPlainText(
        hadith: HadithItem,
        bookTitle: String,
        sectionTitle: String,
        withAppLink: Boolean
    ): String = listOfNotNull(
        bookTitle.ifBlank { null },
        sectionTitle.ifBlank { null },
        if (hadith.hadithNumber > 0) "হাদিস নং: ${toBangla(hadith.hadithNumber)}" else null,
        hadith.title.stripHtml().ifBlank { null },
        hadith.descriptionAr.stripHtml().ifBlank { null },
        hadith.description.stripHtml().ifBlank { null },
        if (withAppLink) "\nঅ্যাপ: ইসলামী বিশ্বকোষ ও আল হাদিস\nhttps://play.google.com/store/apps/details?id=com.srizwan.islamipedia" else null
    ).joinToString("\n")

    // ─────────────────────────────────────────────────────────────
    // Back press
    // ─────────────────────────────────────────────────────────────
    private fun handleBackPress() {
        when {
            isGlobalSearchOpen -> closeGlobalSearch()
            isSearchOpen -> closeSearch()
            currentState is PageState.Hadith -> {
                val s = currentState as PageState.Hadith
                saveScrollPosition()
                loadSections(s.bookId, s.bookTitle)
            }
            currentState is PageState.Sections -> {
                saveScrollPosition()
                loadBooks()
            }
            else -> finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearSkeletonAnimators()
        scope.cancel()
        marqueeHandler.removeCallbacksAndMessages(null)
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        globalSearchRunnable?.let { globalSearchHandler.removeCallbacks(it) }
        searchHandler.removeCallbacksAndMessages(null)
        globalSearchHandler.removeCallbacksAndMessages(null)
        currentRequestJob?.cancel()
    }

    // ─────────────────────────────────────────────────────────────
    // Drawing helpers
    // ─────────────────────────────────────────────────────────────
    private fun getBengaliTypeface() = try {
        android.graphics.Typeface.createFromAsset(assets, "fonts/SolaimanLipi.ttf")
    } catch (e: Exception) { android.graphics.Typeface.DEFAULT }

    private fun getArabicTypeface() = try {
        android.graphics.Typeface.createFromAsset(assets, "fonts/noorehuda.ttf")
    } catch (e: Exception) { android.graphics.Typeface.DEFAULT }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun createRoundedBg(
        fillColor: Int, strokeColor: Int, strokeWidth: Int, radius: Int
    ): android.graphics.drawable.Drawable =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(fillColor)
            setStroke(strokeWidth, strokeColor)
            cornerRadius = radius.toFloat()
        }

    private fun createRoundedSolid(fillColor: Int, radius: Int): android.graphics.drawable.Drawable =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(fillColor)
            cornerRadius = radius.toFloat()
        }

    private fun TextView.setSmartText(raw: String) {
        if (raw.containsHtml()) setText(raw.toHtmlSpanned()) else text = raw
        setTextIsSelectable(true)
    }

    // ─────────────────────────────────────────────────────────────
    // Download control widget (used inside each book card).
    // - Not downloaded → tappable "⬇" badge that starts the download
    // - Downloading    → animated percentage badge, updates live
    // - Downloaded     → hidden entirely (per requirement, the
    //                    download icon disappears once complete)
    // ─────────────────────────────────────────────────────────────
    private fun buildDownloadControl(book: BookItem): View {
        val size = dp(34)
        val box = FrameLayout(this@HadithMeActivity).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply { marginStart = dp(8) }
        }
        when {
            downloadedBookIds.contains(book.id) -> {
                box.visibility = View.GONE
            }
            downloadingBookIds.contains(book.id) -> {
                box.visibility = View.VISIBLE
                box.background = createRoundedBg(
                    Color.parseColor("#E8F8F7"), Color.parseColor("#01837A"), dp(1), size / 2
                )
                box.addView(TextView(this@HadithMeActivity).apply {
                    text = "${toBangla(downloadProgress[book.id] ?: 0)}%"
                    textSize = 9f
                    setTextColor(Color.parseColor("#01837A"))
                    typeface = getBengaliTypeface()
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                    )
                })
                box.isClickable = false
            }
            else -> {
                box.visibility = View.VISIBLE
                box.background = createRoundedBg(
                    Color.WHITE, Color.parseColor("#01837A"), dp(1), size / 2
                )
                box.addView(TextView(this@HadithMeActivity).apply {
                    text = "⬇"
                    textSize = 15f
                    setTextColor(Color.parseColor("#01837A"))
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                    )
                })
                box.setOnClickListener {
                    startBookDownload(book)
                }
            }
        }
        return box
    }

    // ─────────────────────────────────────────────────────────────
    // Skeleton Adapter — shimmering placeholder cards shown while a
    // page's real data is being fetched, so the list never appears
    // blank or shows a spinner-only screen.
    // ─────────────────────────────────────────────────────────────
    inner class SkeletonAdapter(private val type: SkeletonType) :
        RecyclerView.Adapter<SkeletonAdapter.VH>() {

        inner class VH(val card: LinearLayout) : RecyclerView.ViewHolder(card)

        private fun bar(widthDp: Int, heightDp: Int, topMarginDp: Int = 0): View =
            View(this@HadithMeActivity).apply {
                background = createRoundedSolid(Color.parseColor("#E4E4E4"), dp(6))
                layoutParams = LinearLayout.LayoutParams(dp(widthDp), dp(heightDp)).apply {
                    topMargin = dp(topMarginDp)
                }
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val card = LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(14) }
                background = createRoundedBg(Color.WHITE, Color.parseColor("#EEEEEE"), dp(2), dp(10))
                setPadding(dp(16), dp(14), dp(16), dp(14))
            }

            val headerRow = LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            headerRow.addView(bar(30, 24))
            headerRow.addView(bar(150, 18, 0).apply {
                (layoutParams as LinearLayout.LayoutParams).marginStart = dp(10)
            })
            card.addView(headerRow)

            card.addView(bar(220, 15, 12))
            if (type != SkeletonType.BOOK) {
                card.addView(bar(160, 13, 8))
            }
            if (type == SkeletonType.HADITH) {
                card.addView(bar(200, 13, 8))
            }

            val anim = android.animation.ObjectAnimator.ofFloat(card, "alpha", 1f, 0.45f, 1f).apply {
                duration = 900
                repeatCount = android.animation.ObjectAnimator.INFINITE
                startDelay = ((position_seed++ % 4) * 120).toLong()
                start()
            }
            skeletonAnimators.add(anim)

            return VH(card)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {}

        override fun getItemCount() = 8
    }

    private var position_seed = 0

    // ─────────────────────────────────────────────────────────────
    // Book Adapter
    // ─────────────────────────────────────────────────────────────
    inner class BookAdapter(
        private val items: List<BookItem>,
        private val onClick: (BookItem) -> Unit
    ) : RecyclerView.Adapter<BookAdapter.VH>() {

        inner class VH(val card: LinearLayout) : RecyclerView.ViewHolder(card)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(14) }
                background = createRoundedBg(Color.WHITE, Color.parseColor("#01837A"), dp(2), dp(10))
                elevation = dp(3).toFloat()
                setPadding(dp(16), dp(14), dp(16), dp(14))
            }
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val book = items[position]
            holder.card.removeAllViews()
            holder.card.setOnClickListener {
                hideKeyboard(searchInput)
                onClick(book)
            }

            val headerRow = LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            headerRow.addView(TextView(this@HadithMeActivity).apply {
                text = toBangla(book.originalPosition + 1)
                textSize = 13f
                setTextColor(Color.WHITE)
                typeface = getBengaliTypeface()
                background = createRoundedSolid(Color.parseColor("#01837A"), dp(16))
                gravity = Gravity.CENTER
                setPadding(dp(10), dp(4), dp(10), dp(4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(10) }
            })

            val displayTitle = book.titleEn.trim().ifBlank { book.titleAr.trim() }
            if (displayTitle.isNotBlank()) {
                headerRow.addView(TextView(this@HadithMeActivity).apply {
                    text = displayTitle
                    textSize = 17f
                    setTextColor(Color.parseColor("#01837A"))
                    typeface = getBengaliTypeface()
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
            }
            headerRow.addView(buildDownloadControl(book))
            holder.card.addView(headerRow)

            val arTitle = book.titleAr.trim()
            if (arTitle.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    text = arTitle
                    textSize = 18f
                    setTextColor(Color.parseColor("#333333"))
                    typeface = getArabicTypeface()
                    gravity = Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(10); bottomMargin = dp(10) }
                })
            }

            holder.card.addView(View(this@HadithMeActivity).apply {
                setBackgroundColor(Color.parseColor("#DDDDDD"))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1))
            })

            val hasSectionCount = book.totalSection > 0
            val hasHadithCount  = book.totalHadith > 0
            if (hasSectionCount || hasHadithCount) {
                val meta = LinearLayout(this@HadithMeActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(8) }
                }
                if (hasSectionCount) {
                    meta.addView(TextView(this@HadithMeActivity).apply {
                        text = "📚 ${toBangla(book.totalSection)} টি অধ্যায়"
                        textSize = 13f
                        setTextColor(Color.parseColor("#666666"))
                        typeface = getBengaliTypeface()
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                }
                if (hasHadithCount) {
                    meta.addView(TextView(this@HadithMeActivity).apply {
                        text = "📖 ${toBangla(book.totalHadith)} টি হাদিস"
                        textSize = 13f
                        setTextColor(Color.parseColor("#666666"))
                        typeface = getBengaliTypeface()
                    })
                }
                holder.card.addView(meta)
            }
        }

        override fun getItemCount() = items.size
    }

    // ─────────────────────────────────────────────────────────────
    // Section Adapter
    // ─────────────────────────────────────────────────────────────
    inner class SectionAdapter(
        private val items: List<SectionItem>,
        private val onClick: (SectionItem) -> Unit
    ) : RecyclerView.Adapter<SectionAdapter.VH>() {

        inner class VH(val card: LinearLayout) : RecyclerView.ViewHolder(card)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(14) }
                background = createRoundedBg(Color.WHITE, Color.parseColor("#01837A"), dp(2), dp(10))
                elevation = dp(3).toFloat()
                setPadding(dp(16), dp(14), dp(16), dp(14))
            }
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val section = items[position]
            holder.card.removeAllViews()
            holder.card.setOnClickListener {
                hideKeyboard(searchInput)
                onClick(section)
            }

            val headerRow = LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            headerRow.addView(TextView(this@HadithMeActivity).apply {
                text = toBangla(section.originalPosition + 1)
                textSize = 13f
                setTextColor(Color.WHITE)
                typeface = getBengaliTypeface()
                background = createRoundedSolid(Color.parseColor("#01837A"), dp(16))
                gravity = Gravity.CENTER
                setPadding(dp(10), dp(4), dp(10), dp(4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(10) }
            })

            val sectionTitle = section.title.trim()
            if (sectionTitle.isNotBlank()) {
                headerRow.addView(TextView(this@HadithMeActivity).apply {
                    text = sectionTitle
                    textSize = 17f
                    setTextColor(Color.parseColor("#01837A"))
                    typeface = getBengaliTypeface()
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
            }
            holder.card.addView(headerRow)

            val arTitle = section.titleAr.trim()
            if (arTitle.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    text = arTitle
                    textSize = 18f
                    setTextColor(Color.parseColor("#333333"))
                    typeface = getArabicTypeface()
                    gravity = Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(10); bottomMargin = dp(10) }
                })
            }

            holder.card.addView(View(this@HadithMeActivity).apply {
                setBackgroundColor(Color.parseColor("#DDDDDD"))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1))
            })

            val hasHadithCount = section.totalHadith > 0
            val hasRange       = section.rangeStart > 0 && section.rangeEnd > 0
            if (hasHadithCount || hasRange) {
                val meta = LinearLayout(this@HadithMeActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(8) }
                }
                if (hasHadithCount) {
                    meta.addView(TextView(this@HadithMeActivity).apply {
                        text = "📖 মোট ${toBangla(section.totalHadith)} টি হাদিস"
                        textSize = 13f
                        setTextColor(Color.parseColor("#666666"))
                        typeface = getBengaliTypeface()
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                }
                if (hasRange) {
                    meta.addView(TextView(this@HadithMeActivity).apply {
                        text = "🔢 ব্যাপ্তি: ${toBangla(section.rangeStart)}-${toBangla(section.rangeEnd)}"
                        textSize = 13f
                        setTextColor(Color.parseColor("#666666"))
                        typeface = getBengaliTypeface()
                    })
                }
                holder.card.addView(meta)
            }
        }

        override fun getItemCount() = items.size
    }

    // ─────────────────────────────────────────────────────────────
    // Hadith Adapter
    // ─────────────────────────────────────────────────────────────
    inner class HadithAdapter(
        private val items: List<HadithItem>,
        private val bookTitle: String,
        private val onCopy: (HadithItem) -> Unit,
        private val onShare: (HadithItem) -> Unit
    ) : RecyclerView.Adapter<HadithAdapter.VH>() {

        inner class VH(val card: LinearLayout) : RecyclerView.ViewHolder(card)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(14) }
                background = createRoundedBg(Color.WHITE, Color.parseColor("#01837A"), dp(2), dp(10))
                elevation = dp(3).toFloat()
                setPadding(dp(16), dp(14), dp(16), dp(14))
            }
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val hadith = items[position]
            holder.card.removeAllViews()
            holder.card.setOnClickListener { hideKeyboard(searchInput) }

            val headerRow = LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            if (hadith.hadithNumber > 0) {
                headerRow.addView(TextView(this@HadithMeActivity).apply {
                    text = "হাদিস নং: ${toBangla(hadith.hadithNumber)}"
                    textSize = 13f
                    setTextColor(Color.WHITE)
                    typeface = getBengaliTypeface()
                    background = createRoundedSolid(Color.parseColor("#01837A"), dp(20))
                    setPadding(dp(12), dp(5), dp(12), dp(5))
                })
            }
            val bookLabel = bookTitle.trim()
            if (bookLabel.isNotBlank()) {
                headerRow.addView(TextView(this@HadithMeActivity).apply {
                    text = bookLabel
                    textSize = 11f
                    setTextColor(Color.parseColor("#01837A"))
                    typeface = getBengaliTypeface()
                    background = createRoundedBg(
                        Color.parseColor("#E8F8F7"), Color.parseColor("#01837A"), dp(1), dp(12)
                    )
                    setPadding(dp(8), dp(3), dp(8), dp(3))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = dp(8) }
                    maxWidth = dp(120)
                    isSingleLine = true
                    ellipsize = android.text.TextUtils.TruncateAt.END
                })
            }
            headerRow.addView(View(this@HadithMeActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
            })
            headerRow.addView(ImageView(this@HadithMeActivity).apply {
                setImageResource(R.drawable.copy)
                setColorFilter(Color.parseColor("#01837A"))
                layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply { marginEnd = dp(10) }
                setOnClickListener { onCopy(hadith) }
            })
            headerRow.addView(ImageView(this@HadithMeActivity).apply {
                setImageResource(R.drawable.share)
                setColorFilter(Color.parseColor("#01837A"))
                layoutParams = LinearLayout.LayoutParams(dp(24), dp(24))
                setOnClickListener { onShare(hadith) }
            })
            holder.card.addView(headerRow)

            val titleText = hadith.title.trim()
            if (titleText.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    textSize = 18f
                    setTextColor(Color.parseColor("#01837A"))
                    typeface = getBengaliTypeface()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(10) }
                    setSmartText(titleText)
                })
            }

            val arText = hadith.descriptionAr.trim()
            if (arText.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    textSize = 20f
                    setTextColor(Color.parseColor("#333333"))
                    typeface = getArabicTypeface()
                    gravity = Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(12); bottomMargin = dp(6) }
                    setSmartText(arText)
                })
            }

            val bnText = hadith.description.trim()
            if (bnText.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    textSize = 18f
                    setTextColor(Color.parseColor("#444444"))
                    typeface = getBengaliTypeface()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(8) }
                    setSmartText(bnText)
                })
            }
        }

        override fun getItemCount() = items.size
    }

    // ─────────────────────────────────────────────────────────────
    // Global Search Result + Adapter
    // ─────────────────────────────────────────────────────────────
    data class GlobalSearchResult(
        val hadith: HadithItem,
        val bookTitle: String,
        val bookId: Int,
        val sectionTitle: String,
        val sectionId: Int
    )

    inner class GlobalSearchAdapter(
        private val items: List<GlobalSearchResult>,
        private val onCopy: (GlobalSearchResult) -> Unit,
        private val onShare: (GlobalSearchResult) -> Unit
    ) : RecyclerView.Adapter<GlobalSearchAdapter.VH>() {

        inner class VH(val card: LinearLayout) : RecyclerView.ViewHolder(card)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
                background = createRoundedBg(Color.WHITE, Color.parseColor("#01837A"), dp(2), dp(10))
                elevation = dp(3).toFloat()
                setPadding(dp(14), dp(12), dp(14), dp(12))
            }
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val result = items[position]
            val hadith = result.hadith
            holder.card.removeAllViews()
            holder.card.setOnClickListener { hideKeyboard(globalSearchInput) }

            val headerRow = LinearLayout(this@HadithMeActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            if (hadith.hadithNumber > 0) {
                headerRow.addView(TextView(this@HadithMeActivity).apply {
                    text = "হাদিস নং: ${toBangla(hadith.hadithNumber)}"
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    typeface = getBengaliTypeface()
                    background = createRoundedSolid(Color.parseColor("#01837A"), dp(20))
                    setPadding(dp(10), dp(4), dp(10), dp(4))
                })
            }
            val bookLabel = result.bookTitle.trim()
            if (bookLabel.isNotBlank()) {
                headerRow.addView(TextView(this@HadithMeActivity).apply {
                    text = bookLabel
                    textSize = 11f
                    setTextColor(Color.parseColor("#01837A"))
                    typeface = getBengaliTypeface()
                    background = createRoundedBg(
                        Color.parseColor("#E8F8F7"), Color.parseColor("#01837A"), dp(1), dp(12)
                    )
                    setPadding(dp(8), dp(3), dp(8), dp(3))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = dp(8) }
                    maxWidth = dp(130)
                    isSingleLine = true
                    ellipsize = android.text.TextUtils.TruncateAt.END
                })
            }
            headerRow.addView(View(this@HadithMeActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
            })
            headerRow.addView(ImageView(this@HadithMeActivity).apply {
                setImageResource(R.drawable.copy)
                setColorFilter(Color.parseColor("#01837A"))
                layoutParams = LinearLayout.LayoutParams(dp(22), dp(22)).apply { marginEnd = dp(8) }
                setOnClickListener { onCopy(result) }
            })
            headerRow.addView(ImageView(this@HadithMeActivity).apply {
                setImageResource(R.drawable.share)
                setColorFilter(Color.parseColor("#01837A"))
                layoutParams = LinearLayout.LayoutParams(dp(22), dp(22))
                setOnClickListener { onShare(result) }
            })
            holder.card.addView(headerRow)

            val titleText = hadith.title.trim()
            if (titleText.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    textSize = 15f
                    setTextColor(Color.parseColor("#01837A"))
                    typeface = getBengaliTypeface()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(8) }
                    setSmartText(titleText)
                })
            }

            val arText = hadith.descriptionAr.trim()
            if (arText.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    textSize = 18f
                    setTextColor(Color.parseColor("#333333"))
                    typeface = getArabicTypeface()
                    gravity = Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(10); bottomMargin = dp(6) }
                    setSmartText(arText)
                })
            }

            val bnText = hadith.description.trim()
            if (bnText.isNotBlank()) {
                holder.card.addView(TextView(this@HadithMeActivity).apply {
                    textSize = 14f
                    setTextColor(Color.parseColor("#444444"))
                    typeface = getBengaliTypeface()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(8) }
                    setSmartText(bnText)
                })
            }
        }

        override fun getItemCount() = items.size
    }
}
