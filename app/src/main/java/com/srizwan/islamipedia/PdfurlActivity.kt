package com.srizwan.islamipedia

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
class PdfurlActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var pdfView: PDFView
    private lateinit var preferences: SharedPreferences
    private var currentPage: Double = 0.0
    private var totalPages: Int = 0
    private lateinit var txtCurrentPage: TextView
    private lateinit var spin: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf)

        initialize()
        initializeLogic()
    }

    private fun initialize() {
        toolbar = findViewById(R.id._toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        title = intent.getStringExtra("heading")
        spin = findViewById(R.id.spinkit)
        txtCurrentPage = findViewById(R.id.txtCurrentPage)
        pdfView = findViewById(R.id.pdfview1)
        preferences = getSharedPreferences("page", Activity.MODE_PRIVATE)
    }

    private fun initializeLogic() {
        val pdfUrl = intent.getStringExtra("pdfname")
        val heading = intent.getStringExtra("heading")


        if (pdfUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show()
            return
        }

        val lastPage = preferences.getInt("${heading}_lastPage", 0)

        if (lastPage > 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                AlertDialog.Builder(this)
                    .setTitle("শেষবার যেখান থেকে পড়েছিলেন সেখান থেকে আবারো পড়বেন?")
                    .setCancelable(false)
                    .setPositiveButton("হ্যাঁ") { _, _ -> downloadAndOpenPdf(pdfUrl, lastPage) }
                    .setNegativeButton("না") { _, _ -> downloadAndOpenPdf(pdfUrl, 0) }
                    .create()
                    .show()
            }, 900)
        } else {
            downloadAndOpenPdf(pdfUrl, 0)
        }
    }

    private fun downloadAndOpenPdf(pdfUrl: String, page: Int) {
        spin.visibility = View.VISIBLE

        Thread {
            try {
                val url = java.net.URL(pdfUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()

                runOnUiThread {
                    spin.visibility = View.GONE
                    openPdfAtPage(inputStream, page)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to load PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }.start()
    }

    private fun openPdfAtPage(inputStream: java.io.InputStream, page: Int) {
        pdfView.fromStream(inputStream)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(page)
            .enableAnnotationRendering(false)
            .enableAntialiasing(true)
            .spacing(0)
            .scrollHandle(DefaultScrollHandle(this, true))
            .onPageChange { pageNum, pageCount ->
                currentPage = pageNum.toDouble()
                txtCurrentPage.text = "${pageNum + 1}/$pageCount"
                totalPages = pageCount
                Log.d("PDF_VIEWER", "Page changed to ${pageNum + 1}")
            }
            .onLoad { nbPages ->
                Log.d("PDF_VIEWER", "PDF Loaded with $nbPages pages")
                totalPages = nbPages
            }
            .load()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pdf_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_avatar) {
            showPageJumpDialog()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showPageJumpDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_page_jump, null)
        builder.setView(dialogView)
        val text: TextView = dialogView.findViewById(R.id.text)
        text.text = "কত নাম্বার পেইজে যাবেন?"
        val pageInput: EditText = dialogView.findViewById(R.id.pageInput)
        pageInput.hint = "1 - $totalPages"

        builder.setPositiveButton("হ্যাঁ") { _, _ ->
            val pageNumberStr = pageInput.text.toString().trim()
            if (pageNumberStr.isNotEmpty()) {
                try {
                    val pageNumber = pageNumberStr.toInt()
                    if (pageNumber in 1..totalPages) {
                        pdfView.jumpTo(pageNumber - 1, true)
                    } else {
                        Toast.makeText(applicationContext, "Invalid page number", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(applicationContext, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "Please enter a page number", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("না") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    override fun onPause() {
        super.onPause()
        val heading = intent.getStringExtra("heading")
        preferences.edit().putInt("${heading}_lastPage", currentPage.toInt()).apply()
    }
}
