package com.srizwan.islamipedia

import android.animation.*
import android.app.*
import android.content.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.*
import android.graphics.*
import android.graphics.drawable.*
import android.media.*
import android.net.*
import android.os.*
import android.text.*
import android.text.style.*
import android.util.*
import android.view.*
import android.view.View
import android.view.View.*
import android.view.animation.*
import android.webkit.*
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.AppBarLayout
import java.io.*
import java.text.*
import java.util.*
import java.util.regex.*
import org.json.*

class DonateActivity : AppCompatActivity() {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var contentLinearLayout: LinearLayout
    private lateinit var heartTextView: TextView
    private lateinit var appNameTextView: TextView
    private lateinit var dividerView: View
    private lateinit var commitmentTextView: TextView
    private lateinit var expandHeaderLayout: LinearLayout
    private lateinit var expandTitleTextView: TextView
    private lateinit var reasonsContentLayout: LinearLayout
    private lateinit var reasonsTextView: TextView
    private lateinit var numberContainerLayout: LinearLayout
    private lateinit var numberLabelTextView: TextView
    private lateinit var numberValueTextView: TextView
    private lateinit var copyHintTextView: TextView
    
    private var solaimanLipiTypeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Status Bar & Navigation Bar Fix
        setupWindowInsets()
        
        createUI()
        initializeLogic()
    }
    
    private fun setupWindowInsets() {
        // Make status bar transparent but with dark icons for better visibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                // Set status bar color to transparent
                statusBarColor = Color.TRANSPARENT
                
                // Set navigation bar color
                navigationBarColor = ContextCompat.getColor(this@DonateActivity, android.R.color.white)
                
                // Make status bar icons dark for better visibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility = decorView.systemUiVisibility or 
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                
                // For navigation bar icons
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    decorView.systemUiVisibility = decorView.systemUiVisibility or 
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
        }
        
        // Fix for edge-to-edge display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }
    
    private fun loadSolaimanLipiFont() {
        try {
            // Load font from res/font folder
            solaimanLipiTypeface = ResourcesCompat.getFont(this, R.font.solaimanlipi)
            if (solaimanLipiTypeface == null) {
                // Fallback: Try to load from assets if not found in res
                solaimanLipiTypeface = Typeface.createFromAsset(assets, "fonts/solaimanlipi.ttf")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default font
            solaimanLipiTypeface = Typeface.DEFAULT
        }
    }
    
    private fun applyFontToTextView(textView: TextView) {
        solaimanLipiTypeface?.let {
            textView.typeface = it
        }
    }

    private fun createUI() {
        // Load SolaimanLipi font from res/font
        loadSolaimanLipiFont()
        
        // Root CoordinatorLayout with background
        coordinatorLayout = CoordinatorLayout(this).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundResource(R.drawable.back1groundd)
            // Add padding for status bar and navigation bar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setPadding(0, getStatusBarHeight(), 0, getNavigationBarHeight())
            }
        }

        // AppBarLayout
        appBarLayout = AppBarLayout(this).apply {
            id = View.generateViewId()
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Toolbar
        toolbar = Toolbar(this).apply {
            id = View.generateViewId()
            layoutParams = AppBarLayout.LayoutParams(
                AppBarLayout.LayoutParams.MATCH_PARENT,
                getActionBarHeight()
            )
            setBackgroundColor(getColorFromAttr(android.R.attr.colorPrimary))
            setPopupTheme(R.style.Theme_MyApplication_AppBarOverlay)
        }
        appBarLayout.addView(toolbar)
        coordinatorLayout.addView(appBarLayout)

        // Build LayoutParams OUTSIDE apply to avoid val reassignment
        val contentParams = CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.MATCH_PARENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT
        )
        contentParams.behavior = AppBarLayout.ScrollingViewBehavior()

        contentLinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = contentParams
            setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
            setBackgroundColor(Color.WHITE)
        }

        // Heart Emoji TextView
        heartTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.CENTER_VERTICAL
            }
            text = "❤️"
            textSize = 100f
        }
        contentLinearLayout.addView(heartTextView)

        // App Name TextView
        appNameTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.CENTER_VERTICAL
            }
            text = "ইসলামী বিশ্বকোষ ও আল হাদিস S2"
            textSize = 25f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1A237E"))
            alpha = 0.8f
            applyFontToTextView(this)
        }
        contentLinearLayout.addView(appNameTextView)

        // Divider View
        dividerView = View(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(60),
                dpToPx(3)
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.CENTER_VERTICAL
                topMargin = dpToPx(16)
                bottomMargin = dpToPx(16)
            }
            setBackgroundColor(Color.parseColor("#F0F0F0"))
        }
        contentLinearLayout.addView(dividerView)

        // Commitment TextView
        commitmentTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.CENTER_VERTICAL
            }
            text = "আপনার সহযোগিতায় আমরা অ্যাপটিকে বিজ্ঞাপনমুক্ত ও উন্নত রাখতে চাই। এটি একটি সদকায়ে জারিয়া হিসেবে গণ্য হবে ইনশাআল্লাহ।"
            textSize = 14f
            setTextColor(Color.parseColor("#444444"))
            setLineSpacing(dpToPx(4).toFloat(), 1.0f)
            applyFontToTextView(this)
        }
        contentLinearLayout.addView(commitmentTextView)

        // Resolve TypedValue OUTSIDE apply to avoid val conflict with view property
        val expandBgOutValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, expandBgOutValue, true)

        // Expand Header Layout
        expandHeaderLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(20)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            isFocusable = true
            setPadding(0, dpToPx(8), 0, dpToPx(8))
            setBackgroundResource(expandBgOutValue.resourceId)
        }

        expandTitleTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            text = "কেন আপনার সহযোগিতা প্রয়োজন?"
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1A237E"))
            applyFontToTextView(this)
        }
        expandHeaderLayout.addView(expandTitleTextView)
        contentLinearLayout.addView(expandHeaderLayout)

        // Reasons Content Layout
        reasonsContentLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            orientation = LinearLayout.VERTICAL
        }

        reasonsTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "• সার্ভার ও রক্ষণাবেক্ষণ খরচ\n• বিজ্ঞাপনমুক্ত বিশুদ্ধ অভিজ্ঞতা\n• তাফসির সমগ্র সম্পূর্ণ করণ\n• নতুন আধুনিক ফিচার উন্নয়ন\n• দ্বীন প্রচারের প্রসার ও সদকায়ে জারিয়া"
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            setLineSpacing(dpToPx(6).toFloat(), 1.0f)
            applyFontToTextView(this)
        }
        reasonsContentLayout.addView(reasonsTextView)
        contentLinearLayout.addView(reasonsContentLayout)

        // Resolve bg resource id and foreground TypedValue OUTSIDE apply
        val bgResId = resources.getIdentifier("bg_payment_box", "drawable", packageName)
        val containerFgOutValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, containerFgOutValue, true)

        // Number Container Layout
        numberContainerLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            isFocusable = true
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))

            if (bgResId != 0) {
                setBackgroundResource(bgResId)
            } else {
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F8F9FA"))
                    cornerRadius = dpToPx(8).toFloat()
                    setStroke(1, Color.parseColor("#E0E0E0"))
                }
            }

            foreground = ContextCompat.getDrawable(this@DonateActivity, containerFgOutValue.resourceId)
        }

        // Inner LinearLayout for text
        val innerTextLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            orientation = LinearLayout.VERTICAL
        }

        numberLabelTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "বিকাশ (পার্সোনাল)"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#D81B60"))
            applyFontToTextView(this)
        }
        innerTextLayout.addView(numberLabelTextView)

        numberValueTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "01537-144153"
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#212121"))
            letterSpacing = 0.05f
            applyFontToTextView(this)
        }
        innerTextLayout.addView(numberValueTextView)

        numberContainerLayout.addView(innerTextLayout)
        contentLinearLayout.addView(numberContainerLayout)

        // Copy Hint TextView
        copyHintTextView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.CENTER_VERTICAL
            }
            text = "নম্বরের ওপর ট্যাপ করে কপি করুন"
            textSize = 11f
            setTextColor(Color.parseColor("#888888"))
            applyFontToTextView(this)
        }
        contentLinearLayout.addView(copyHintTextView)

        coordinatorLayout.addView(contentLinearLayout)
        setContentView(coordinatorLayout)

        // Setup Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set Click Listener for number container
        numberContainerLayout.setOnClickListener {
            Toast.makeText(this, "বিকাশ নাম্বার কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("clipboard", numberValueTextView.text.toString())
            clipboard.setPrimaryClip(clip)
        }
    }
    
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
    
    private fun getNavigationBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun initializeLogic() {
        title = "সাপোর্ট করুন"
        // Apply font to toolbar title
        solaimanLipiTypeface?.let {
            supportActionBar?.title?.let { title ->
                val spannableString = SpannableString(title)
                spannableString.setSpan(TypefaceSpan(it), 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                supportActionBar?.title = spannableString
            }
        }
    }

    private fun getActionBarHeight(): Int {
        val tv = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        } else {
            dpToPx(56)
        }
    }

    private fun getColorFromAttr(attr: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(attr, tv, true)
        return tv.data
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
