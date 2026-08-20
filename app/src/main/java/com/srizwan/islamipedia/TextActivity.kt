package com.srizwan.islamipedia

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class TextActivity : AppCompatActivity() {
    private var a = ""
    private var b = ""
    private lateinit var imageView: ImageView
    private lateinit var editText: EditText
    private lateinit var screenshot: Bitmap
    private lateinit var progress_bar: ProgressBar
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var refresh: ImageView
    private lateinit var copy: ImageView
    private lateinit var share: ImageView
    private val model by lazy {
        GeminiHelper.createModel(BuildConfig.textapi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_scan)

        refresh = findViewById(R.id.refresh)
        copy = findViewById(R.id.copy)
        share = findViewById(R.id.share)
        imageView = findViewById(R.id.screenshot_view)
        editText = findViewById(R.id.result_text)
        progress_bar = findViewById(R.id.progress_bar)

        // Check internet connection
        if (!isNetworkAvailable()) {
            showNoInternetDialog()
        }
        val byteArray = intent.getByteArrayExtra("screenshot")
        byteArray?.let {
            // FIX: BitmapFactory downsampling - Play Console warning fix
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(it, 0, it.size, options)
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            
            val inputStream = ByteArrayInputStream(it)
            // Downsampled bitmap load
            screenshot = BitmapFactory.decodeStream(inputStream, null, options) ?: BitmapFactory.decodeByteArray(it, 0, it.size)
            imageView.setImageBitmap(screenshot)

            // Extract text from screenshot
            extractTextFromScreenshot()
            copy.setOnClickListener {
                Toast.makeText(this@TextActivity, "কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                    ClipData.newPlainText("clipboard", "${editText.text}\n\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ গ্রবথ S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia")
                )
            }
            share.setOnClickListener {
                Toast.makeText(this@TextActivity, "শেয়ার করা হয়েছে", Toast.LENGTH_SHORT).show()
                a = "এপ্সটি শেয়ার করুন"
                b = "${editText.text}\n\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_SUBJECT, a)
                i.putExtra(Intent.EXTRA_TEXT, b)
                startActivity(Intent.createChooser(i, "লেখা গুলো শেয়ার করুন"))
            }
            refresh.setOnClickListener{
                extractTextFromScreenshot()
                editText.setText("")
                progress_bar.visibility = View.VISIBLE
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun extractTextFromScreenshot() {
        scope.launch {
            try {
                val response = model.generateContent(
                    com.google.ai.client.generativeai.type.content {
                        image(screenshot)
                        text("Extract Text From Image")
                    }
                )
                val extractedText = response.text

                // Combine paragraphs into a single string (if multiple paragraphs)
                val combinedText = extractedText!!.replace("\n", " ").trim()

                runOnUiThread {
                    progress_bar.visibility = View.GONE
                    editText.setText(combinedText)
                    Toast.makeText(applicationContext, "স্ক্রিনশট থেকে টেক্সট করা হয়েছে, ভুল থাকতে পারে সংশোধন করে নিবেন।", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    progress_bar.visibility = View.GONE
                    val errorMsg = e.message?.lowercase() ?: ""
                    if (errorMsg.contains("429") || errorMsg.contains("quota") || errorMsg.contains("resource_exhausted") || errorMsg.contains("limit") || errorMsg.contains("exceeded")) {
                        showDailyLimitDialogProgrammatically()
                    } else {
                        editText.setText("Failed to extract text")
                        Toast.makeText(applicationContext, "রিফ্রেশ ক্লিক করুন আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Daily Limit Popup - Programmatically UI No XML
    private fun showDailyLimitDialogProgrammatically() {
        val builder = AlertDialog.Builder(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 50, 60, 30)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val iconText = TextView(this).apply {
            text = "⏳"
            textSize = 48f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val title = TextView(this).apply {
            text = "আজকের লিমিট শেষ!"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#D32F2F"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 15)
        }
        val message = TextView(this).apply {
            text = "আজকের জন্য AI Text Scan এর ফ্রি কোটা শেষ হয়ে গেছে।\n\nGemini API এর দৈনিক লিমিট ২৪ ঘন্টা পর রিসেট হয়।\n\nআগামীকাল আবার চেষ্টা করুন, ইনশাআল্লাহ।"
            textSize = 14f
            setTextColor(Color.parseColor("#424242"))
            gravity = Gravity.CENTER
            setLineSpacing(8f, 1f)
        }
        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#EEEEEE"))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(0, 25, 0, 25)
            }
        }
        val okBtn = TextView(this).apply {
            text = "ঠিক আছে"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1976D2"))
            setPadding(0, 30, 0, 30)
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(20, 0, 20, 0)
            }
        }
        container.addView(iconText)
        container.addView(title)
        container.addView(message)
        container.addView(divider)
        container.addView(okBtn)
        builder.setView(container)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        okBtn.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Internet Connection")
        builder.setMessage("Please connect to the internet.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(this, "Please connect to the internet and try again.", Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)
        builder.show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }
}
