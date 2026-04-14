package com.srizwan.islamipedia

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReportActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private val url = "https://srizwan198704-dev.github.io/islamipediaChat/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatreport)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        setupWebView()
        webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings = webView.settings

        // JavaScript enabled
        webSettings.javaScriptEnabled = true

        // Zoom controls
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(true)

        // DOM storage for website data
        webSettings.domStorageEnabled = true

        // Load images and content properly
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        // WebViewClient to handle page navigation
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        view?.loadUrl(url)
                        return true
                    } else {
                        // Handle external links like tel:, mailto:
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                }
                return false
            }
        }

        // WebChromeClient for progress bar
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                supportActionBar?.title = title
            }
        }

        // Handle file downloads
        webView.setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
            request.setMimeType(mimetype)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("ডাউনলোড হচ্ছে...")
            request.setTitle(Uri.parse(downloadUrl).lastPathSegment)
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Uri.parse(downloadUrl).lastPathSegment)

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this@ReportActivity, "ডাউনলোড শুরু হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle back button for WebView navigation
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webView.clearHistory()
        webView.destroy()
        super.onDestroy()
    }
}
