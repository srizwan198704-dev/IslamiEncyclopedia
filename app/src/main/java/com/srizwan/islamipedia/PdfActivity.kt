package com.srizwan.islamipedia

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
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
import android.os.Handler
import android.os.Looper
import java.io.ByteArrayOutputStream


class PdfActivity : AppCompatActivity() {

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
        spin.postDelayed({spin.visibility = View.GONE}, 1000)
        val pdfName = intent.getStringExtra("pdfname")
        val heading = intent.getStringExtra("heading")
        val inputStream = assets.open(pdfName ?: "")

        val lastPage = preferences.getInt("${heading}_lastPage", 0)

        if (lastPage > 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                AlertDialog.Builder(this)
                    .setTitle("শেষবার যেখান থেকে পড়েছিলেন সেখান থেকে আবারো পড়বেন?")
                    .setCancelable(false)
                    .setPositiveButton("হ্যাঁ") { _, _ -> openPdfAtPage(inputStream, lastPage) }
                    .setNegativeButton("না") { _, _ -> openPdfAtPage(inputStream, 0) }
                    .create()
                    .show()
            }, 900) // Delay in milliseconds
        } else {
            openPdfAtPage(inputStream, 0)
        }

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
        return when (item.itemId) {
            R.id.action_scan_text -> {
                try {
                    val screenshot = captureScreenshot()
                    val byteArray = convertBitmapToByteArray(screenshot)

                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("screenshot", byteArray)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to capture screenshot: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_avatar -> {
                showPageJumpDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun captureScreenshot(): Bitmap {
        val view = pdfView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
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
