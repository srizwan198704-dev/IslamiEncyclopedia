package com.srizwan.islamipedia

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.widget.Toast

class BlogActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var errorLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)

        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        errorLayout = findViewById(R.id.errorLayout)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.parseColor("#FF01837A")
                navigationBarColor = Color.parseColor("#FF01837A")
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
                    return@setDownloadListener
                }
            }
            downloadFile(url, mimeType)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                progressBar.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                showErrorPage()
            }
        }

        loadWebPage()

        swipeRefreshLayout.setOnChildScrollUpCallback { _, _ ->
            webView.scrollY > 0
        }

        swipeRefreshLayout.setOnRefreshListener {
            if (!webView.canScrollVertically(-1)) {
                if (isNetworkAvailable()) {
                    webView.reload()
                } else {
                    swipeRefreshLayout.isRefreshing = false
                    showNoInternetDialog()
                }
            } else {
                swipeRefreshLayout.isRefreshing = false
            }
        }


        val refreshButton = findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            loadWebPage()
        }

        val internetButton = findViewById<Button>(R.id.internet)
        internetButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun downloadFile(url: String, mimeType: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.setTitle(URLUtil.guessFileName(url, null, mimeType))
            request.setDescription("Downloading file...")
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)

            // ফাইল ডাউনলোড লোকেশন সেট করা
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, null, mimeType))

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this, "ডাউনলোড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "ডাউনলোড করতে ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    private fun loadWebPage() {
        val url = intent.getStringExtra("url") ?: "https://www.sunni-encyclopedia.com/"

        if (isNetworkAvailable()) {
            errorLayout.visibility = View.GONE
            webView.visibility = View.VISIBLE
            webView.loadUrl(url)
        } else {
            showErrorPage()
        }
    }


    private fun showErrorPage() {
        webView.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)

        val titleTextView = TextView(this).apply {
            text = "কোন ইন্টারনেট সংযোগ নেই"
            textSize = 22f
            setTextColor(Color.BLACK)
            typeface = Typeface.createFromAsset(assets, "SolaimanLipi.ttf")
            setPadding(40, 20, 40, 10)
        }

        val messageTextView = TextView(this).apply {
            text = "অনুগ্রহ করে ইন্টারনেট সংযোগ চালু করুন"
            textSize = 18f
            setTextColor(Color.BLACK)
            typeface = Typeface.createFromAsset(assets, "SolaimanLipi.ttf")
            setPadding(40, 20, 40, 20)
        }

        builder.setCustomTitle(titleTextView)
            .setView(messageTextView)
            .setCancelable(false)
            .setPositiveButton("রিফ্রেশ") { dialog, id ->
                loadWebPage()
            }
            .setNegativeButton("ইন্টারনেট সেটিংস") { dialog, id ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        val alert = builder.create()
        alert.show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            val builder = AlertDialog.Builder(this)

            val messageTextView = TextView(this).apply {
                text = "আপনি কি ব্লগ থেকে বের হতে চান?"
                textSize = 18f
                setTextColor(Color.BLACK)
                typeface = Typeface.createFromAsset(assets, "SolaimanLipi.ttf")
                setPadding(40, 20, 40, 20)
            }

            builder.setView(messageTextView)
                .setCancelable(false)
                .setPositiveButton("হ্যাঁ") { dialog, id ->
                    super.onBackPressed()
                }
                .setNegativeButton("না") { dialog, id ->
                    dialog.dismiss()
                }

            val alert = builder.create()
            alert.show()
        }
    }
}
