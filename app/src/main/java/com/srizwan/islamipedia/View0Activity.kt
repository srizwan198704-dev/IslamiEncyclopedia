package com.srizwan.islamipedia

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.text.method.LinkMovementMethod
import androidx.activity.OnBackPressedCallback

class View0Activity : AppCompatActivity() {
    private lateinit var heading: TextView
    private lateinit var writer: TextView
    private lateinit var back: ImageView
    private lateinit var share: LinearLayout
    private lateinit var copy: LinearLayout
    private lateinit var high: LinearLayout
    private lateinit var low: LinearLayout
    private lateinit var get : SharedPreferences
    private var a = ""
    private var b = ""
    private var copyshare = ""
    private var click = 0.0

    @SuppressLint("NewApi", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view)
        click = 20.0

        heading = findViewById(R.id.name)
        writer = findViewById(R.id.writer)
        back = findViewById(R.id.back)
        back.setOnClickListener { finish() }
        share = findViewById(R.id.share)
        copy = findViewById(R.id.copy)
        high = findViewById(R.id.high)
        low = findViewById(R.id.low)
        get = getSharedPreferences("me", Activity.MODE_PRIVATE)
        share.setOnClickListener {
            Toast.makeText(this@View0Activity, "${heading.text} শেয়ার করা হয়েছে", Toast.LENGTH_SHORT).show()
            a = "এপ্সটি শেয়ার করুন"
            b = "${heading.text}\n____________________\n${writer.text}\n$copyshare\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_SUBJECT, a)
            i.putExtra(Intent.EXTRA_TEXT, b)
            startActivity(Intent.createChooser(i, "লেখা গুলো শেয়ার করুন"))
        }

        copy.setOnClickListener {
            Toast.makeText(this@View0Activity, "${heading.text} কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                ClipData.newPlainText("clipboard", "${heading.text}\n${writer.text}\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia")
            )
        }

        high.setOnClickListener {
            click++
            writer.textSize = click.toFloat()
        }

        low.setOnClickListener {
            click--
            writer.textSize = click.toFloat()
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.parseColor(getString(R.string.color))
                navigationBarColor = Color.parseColor(getString(R.string.color))
            }
        }

        if (writer.text.toString().trim().isEmpty()) {
            writer.text = "অধ্যায় বিস্তারিত সূচিতে দেখুন"
        }

        heading.text = intent.getStringExtra("name")

        // Check if "author" contains HTML
        val authorText =    get.getString("me", "")
        if (authorText != null && authorText.contains("<") && authorText.contains(">")) {
            writer.text = Html.fromHtml(authorText, Html.FROM_HTML_MODE_COMPACT)
        } else {
            writer.text = authorText
        }
        writer.movementMethod = LinkMovementMethod.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    finish()
            }
        })
    }

    override fun onPause() {
        super.onPause()
    }
}
