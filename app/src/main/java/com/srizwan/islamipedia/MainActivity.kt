package com.srizwan.islamipedia

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var textview1: TextView
    private lateinit var linear1: LinearLayout
    private lateinit var hozur: ImageView
    private var index = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.setBackgroundColor(Color.parseColor(getString(R.string.color)))
        setContentView(R.layout.activity_main)
        textview1 = findViewById(R.id.textview1)
        linear1 = findViewById(R.id.linear1)
        hozur = findViewById(R.id.hozur)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.parseColor(getString(R.string.color))
                navigationBarColor = Color.parseColor(getString(R.string.color))
            }
        }
        typeWriterEffect()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun typeWriterEffect() {
        val text = "ইসলামী বিশ্বকোষ ও আল হাদিস S2"
        if (index < text.length) {
            textview1.append(text[index].toString())
            index++
            handler.postDelayed({ typeWriterEffect() }, 100)
        } else {
            openMain2Activity()
        }
    }

    private fun openMain2Activity() {
        val intent = Intent(this@MainActivity, Main0Activity::class.java)
        startActivity(intent)
        finish()
    }
}

