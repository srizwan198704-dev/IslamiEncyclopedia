package com.srizwan.islamipedia

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
class Main1Activity : AppCompatActivity() {
    private var a = ""
    private var b = ""
    private lateinit var button00: LinearLayout
    private lateinit var button1: LinearLayout
    private lateinit var button01: LinearLayout
    private lateinit var button001: LinearLayout
    private lateinit var button03: LinearLayout
    private lateinit var button04: LinearLayout
    private lateinit var imageView5: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var imageView7: ImageView
    private lateinit var back: ImageView
    private val gmail = Intent()
    private val share = Intent()
    private val rate = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main1)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.parseColor(getString(R.string.color))
                navigationBarColor = Color.parseColor(getString(R.string.color))
            }
        }
        button00 = findViewById(R.id.button00)
        button1 = findViewById(R.id.button1)
        button01 = findViewById(R.id.button01)
        button001 = findViewById(R.id.button001)
        button03 = findViewById(R.id.button03)
        button04 = findViewById(R.id.button04)
        imageView3 = findViewById(R.id.imageView3)
        imageView4 = findViewById(R.id.imageView4)
        imageView5 = findViewById(R.id.imageView5)
        imageView7 = findViewById(R.id.imageview7)

        back = findViewById(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this@Main1Activity, Drawer::class.java))
        }
        button04.setOnClickListener {
            startActivity(Intent(this@Main1Activity, Pdfonline14Activity::class.java))
        }
        button00.setOnClickListener {
showalquran()
        }
        button1.setOnClickListener {
            val json = Intent(applicationContext, Main4Activity::class.java).apply {
                putExtra("sub", "মুফতী আলাউদ্দিন জেহাদী গ্রন্থ সমগ্র")
                putExtra("booklist", "book.json")
            }
            startActivity(json)
        }
        button01.setOnClickListener {
            val json = Intent(applicationContext, Main4Activity::class.java).apply {
                putExtra("sub", "ইসলামী বই সমাহার")
                putExtra("booklist", "file.json")
            }
            startActivity(json)
        }
        button001.setOnClickListener {
            val json = Intent(applicationContext, Main4Activity::class.java).apply {
                putExtra("sub", "আল হাদিস")
                putExtra("booklist", "hadis.json")
            }
            startActivity(json)
        }
        button03.setOnClickListener {
            val json = Intent(applicationContext, ViewActivity::class.java).apply {
                putExtra("name", "মজার তথ্য")
                putExtra("author", "মজার তথ্যঃ\n" +
                        "১। এই এপ্সে কেন এড যুক্ত করা হয়েছে?\n" +
                        "এপ্সটা একজন সম্মানীত ব্যক্তির পুরাতন প্লে কনসোলে পাবলিশ করা হয়েছে। অথচ একটা পুরাতন একাউন্টে এপ্স পাবলিশ করতে ৪/৫ হাজার+ টাকা লাগে আর প্রতি সপ্তাহ ভাড়া দেওয়া লাগে ৩/৪ হাজার টাকা+ বিশ্বাস না হলে রিসার্চ করুন তথ্য পেয়ে যাবেন অনেকে এর থেকেও বেশী বা কম রাখে এটা যার যেমন ইচ্ছে।\n" +
                        "\n" +
                        "* নতুন একাউন্ট আর পুরাতন একাউন্টে পার্থক্য কোথায়? নতুন হলেই তো ভালো ফ্রেশ?\n" +
                        "পার্থক্য হলো নতুন একাউন্টে এপ্স পাবলিশ করতে এখন ১৪ দিন ক্লোজ টেষ্টিং করা লাগে ২০ জন ব্যবহারকারীকে দিয়ে ঠিকমতন ক্লোজ টেষ্টিং না হলে এপ্স পাবলিশ হয় না। অনেক ক্ষেত্রে একাউন্ট ও টারমিনেট হয়ে যাই।\n" +
                        "২। কেন ইসলামী বিশ্বকোষ এপ্সের ডেভেলপার মুছে গেলো?\n" +
                        "যখন ইসলামী বিশ্বকোষ এর ডেভেলপার এপ্স ডেভেলপ করা শুরু করল ইসলামী বিশ্বকোষ হয়ে উনি ১ টাকাও হাদিয়া নেননি। ইসলামী বিশ্বকোষ ও আল হাদিস পুরাতনটা যখন পাবলিশ হল একজন খুশি হয়ে উনাকে ১ হাজার টাকা দিয়েছিলো এটাই ছিলো তার হাদিয়া। ইসলামী বিশ্বকোষ ও আল হাদিস ডেভেলপ করে ডেভেলপার অনেক বড় ভুল করেছে? কি ভুল করেছে ডেভেলপার নাকি ডোনেট আর এড নামে অপশন নিজে যুক্ত করেছে যার কারনে এপ্সটা প্লে ষ্টোর থেকে নাকি রিজেক্ট করে দিয়েছে এটা বলেছে সম্মানীত ইসলামী বিশ্বকোষ এর ব্যবসায়ী গন। অথচ এগুলোর কারনে রিজেক্ট করা হয়নি করা হয়েছে Webview and Affiliate Marketing policy : গুগল প্লে কনসোলের নীতিমালা অনুযায়ী যদি কোন এপ্সে ওয়েবসাইট শো করানো হয় তাহলে সেটার জন্যেই তাদেরকে প্রুফ দেখাতে হয় যে ওয়েবসাইটটি আমাদের। এই বিষয়টা ব্যবসায়ীরা সমাধান করতে পারে নাই অথচ যিনি একাউন্ট ক্রয় করেছিলেন সে সমাধান করে ইসলামী বিশ্বকোষ ও আল হাদিস পুরাতন এপ্সটি ফেরত আনেন।\n" +
                        "\n" +
                        "৩। কেন ব্যবসায়ী?\n" +
                        "তারা ডেভেলপারকে দিয়ে ফ্রীতে এপ্স ডেভেলপ করে সেই পুরাতন প্লে কনসোলে একাউন্ট বিক্রি করেই দেই (৪ লাখ ২০ হাজার মার্কেট প্রাইজ তাদের বক্তব্য ৪২/৫০ হাজার) আর এই বিষয়ে কোন ধরনের পোষ্ট বা নোটিশ দেইনি উলটো ডেভেলপারকে বলা হয় কাউকে জানাতে না। তারা কিন্তু খাই না খাই না আবার বড় কুপ মারছে একাউন্ট বিক্রি করে।\n" +
                        "\n" +
                        "৪। ডেভেলপার কি ডোনেট আর এড অপশন নিজের বাড়ীঘর করার জন্যেই যুক্ত করেছিলো?\n" +
                        "না। ইসলামী বিশ্বকোষ এর যে ব্যবসায়ীরা বলেছিলো। তাই যুক্ত করা হয়েছিলো এব্যাপারে তারায় বলেছে তাদের কেন দান ভিক্ষা দিতে হবে।\n" +
                        "\n" +
                        "৫। ডেভেলপার কেন মুছে গেল?\n" +
                        "যখন সেই পুরাতন প্লে কনসোল একাউন্টটি বিক্রি করে দিলো আর যিনি এই একাউন্টটি ক্রয় করেছেন সে ইসলামী বিশ্বকোষ ও আল হাদিস এপ্স (পুরাতন) ফেরত আনে। প্রতিদিন ডেভেলপার প্লে ষ্টোরে সার্চ দিত ইসলামী বিশ্বকোষ ও আল হাদিস, ইসলামী তাবলীগ, শহিদুল্লাহ বাহাদুর গ্রন্থ সমগ্র, হাফেজ মাওলানা ওসমান গনি গ্রন্থ সমগ্র, উসীলা-ইস্তিগাসা ও আহকামুল মাযার। আগে ৪ টা এপ্স ডেভেলপার এর সার্চ দিলেই আসতো আর সে নিজের আত্মাকে শান্তি দিত যে ইসলামের জন্যেই কিছু করতে পেরেছি। এরপরে যখন সে দেখলো যারা একাউন্ট ক্রয় করেছিলো তারা পুরাতন এপ্সটি ফেরত আনে উনি সেই ইসলামী বিশ্বকোষ ব্যবসায়ীদের জানাই আর বাকী তারা কি করে তারা ওই লোককে বলে এপ্সটি যেন সরিয়ে ফেলে। যাই হোক এরপরে যখন দেখা গেলো এপ্সটি সহ উপরের এপ্স গুলোও নাই। ইসলামী বিশ্বকোষ এর ডেভেলপার মুছে গেলো।\n" +
                        "\n" +
                        "৬। এই এপ্সে কেন শুধুই এই ফিচার গুলো যেহেতু ইসলামী বিশ্বকোষ এর ব্যবসায়ীরা যা হাদিয়া দিয়েছে সে অনুযায়ী এগুলো পেয়েছেন । ডেভেলপার এর স্বপ্ন ছিলো সদকায়ে জারিয়া হিসেবেই ১০০০+ ইসলামী বই (অলরেডি ৫০০+ টেক্সট আছে রেডি করা),৮ টি তাফসির গ্রন্থ, ১৫টি হাদিস গ্রন্থ টেক্সট আকারে নিয়ে আসবে। এই স্বপ্ন হবেই পূরন কিন্তু ইসলামী বিশ্বকোষকে নিয়ে নয়।\n" +
                        "সারাটা দিন এই ইসলামী বিশ্বকোষ, ইসলামী বিশ্বকোষ কইরা আব্বা আম্মা তেজ্জোসন্তান করেছে আর কি চাই দুনিয়াতে? কারন এটাতে এতটা সময় দিছে উনার কাছে নেশার মতন ছিলো এই এপ্স ডেভেলপ করা।\n" +
                        "\n" +
                        "অনেক কিছুই কইতে মন চাইতাছিলো আজ আর নইঃ\n" +
                        "ইতি আপনাদের ডেভেলপার পক্ষ থেকে\n" +
                        "ডেভেলপার প্রতারক, ভন্ড। ডেভেলপারকে ভুলে যাবেন। (তারা কিন্তু তাকে অলী আউলিয়া বলেও প্রশংসা করেছিলো কি কইতাম এত সস্তা সব)\n" +
                        "আর ইসলামী বিশ্বকোষ এর ব্যবসায়ীরা থানায় কেছ করেন মাথা ঠান্ডা করেন। (হাসির ইমুজি)")
            }
            startActivity(json)
        }
        imageView3.setOnClickListener {
            gmail.apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("mailto:rajibhossain0684@gmail.com")
            }
            startActivity(gmail)
            Toast.makeText(this@Main1Activity, "Report us", Toast.LENGTH_SHORT).show()
        }

        imageView4.setOnClickListener {
            rate.apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.srizwan.islamipedia")
            }
            startActivity(rate)
            Toast.makeText(this@Main1Activity, "Rate us", Toast.LENGTH_SHORT).show()
        }

        imageView7.setOnClickListener {
            a = "Share app now"
            b = "আসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
            share.apply {
                type = "text/plain"
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, a)
                putExtra(Intent.EXTRA_TEXT, b)
            }
            startActivity(Intent.createChooser(share, "Share app now"))
            Toast.makeText(this@Main1Activity, "Share app", Toast.LENGTH_SHORT).show()
        }

        imageView5.setOnClickListener { showExitDialog() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("আপনি কি বের হতে চান?")
            setPositiveButton("হ্যাঁ") { _, _ -> finishAffinity() }
            setNegativeButton("না") { _, _ -> }
            create()
            show()
        }
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showExitDialog()
    }
}

private fun Main1Activity.showalquran() {
    AlertDialog.Builder(this).apply {
        setTitle("সিলেক্ট করুন")
        setMessage("ইন্শাআল্লাহ খুউব দ্রুত খাযাইনুল ইরফান ও তাফসিরে মাযহারী টেক্সট আকারে যুক্ত করা হবে।")
        setPositiveButton("কানযুল ঈমান ও তাফসিরে খাযাইনুল ইরফান") { _, _ ->             val json = Intent(applicationContext, PdfActivity::class.java).apply {
            putExtra("heading", "আল কুরআন")
            putExtra("pdfname", "alquran.pdf")
        }
            startActivity(json) }
        setNegativeButton("কানযুল ঈমান ও ইরফানুল কুরআন") { _, _ ->             val json = Intent(applicationContext, QuranActivity::class.java).apply {
            putExtra("sub", "আল কুরআন")
            putExtra("booklist", "sura.json")
        }
            startActivity(json)
        }
        create()
        show()
    }
}
