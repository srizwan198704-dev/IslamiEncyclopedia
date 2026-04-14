package com.srizwan.islamipedia

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class HadisviewActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var jump: ImageView
    private lateinit var listView1: ListView
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var bookid: Array<String>
    private lateinit var ayanumber: Array<String>
    private lateinit var ayaarabic: Array<String>
    private lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject> // Full list of items
    private lateinit var searchtop: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var cancel: ImageView
    private lateinit var searchbox: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        val heading1: TextView = findViewById(R.id.heading1)
        heading1.text = intent.getStringExtra("name")
        jump = findViewById(R.id.jump)
        back = findViewById(R.id.back)
        listView1 = findViewById(R.id.listview1)

        back.setOnClickListener { finish() }

        jump.setOnClickListener {
            showPageJumpDialog()
        }
        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()

        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "হাদিস সার্চ করুন"
        cancel = findViewById(R.id.cancelme)
        cancel.setOnClickListener {
            if (searchbox.text.toString() == "") {
                searchView.visibility = View.GONE
            } else {
                searchbox.text.clear()
            }
        }

        searchtop = findViewById(R.id.searchme)
        searchView = findViewById(R.id.searchView)
        searchtop.setOnClickListener {
            searchView.visibility = if (searchView.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // Search TextWatcher
        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                filterList(query)
            }
        })
        listView1.setOnItemClickListener { _, _, position, _ ->
            val name = name[position]
            val names = ayaarabic[position]
            val author = author[position]

            // Prepare the text to copy, converting Arabic numbers if needed
            val textToCopy = replaceArabicNumber(
                "$name\n\n$names\n\n$author\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
            )

            // Copy the text to clipboard
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText("clipboard", textToCopy))

            // Show a toast notification to indicate the copy action
            val copiedText = replaceArabicNumber("কপি করা হয়েছে")
            Toast.makeText(this, copiedText, Toast.LENGTH_SHORT).show()

            // Return true to indicate the long-click was handled
            true
        }

        listView1.setOnItemLongClickListener { _, _, position, _ ->
            // Get the item at the long-clicked position
            val name = name[position]
            val names = ayaarabic[position]
            val author = author[position]

            // Prepare the text to copy, converting Arabic numbers if needed
            val textToCopy = replaceArabicNumber(
                "$name\n\n$names\n\n$author\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://shorturl.at/RRUHP"
            )

            // Copy the text to clipboard
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText("clipboard", textToCopy))

            // Show a toast notification to indicate the copy action
            val copiedText = replaceArabicNumber("কপি করা হয়েছে")
            Toast.makeText(this, copiedText, Toast.LENGTH_SHORT).show()

            // Return true to indicate the long-click was handled
            true
        }

        // Customize the status and navigation bar colors if API level is higher than Lollipop
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor(getString(R.string.color))
            window.navigationBarColor = Color.parseColor(getString(R.string.color))
        }

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load and set the data in the list view
        val jsonArray = getJSonData(intent.getStringExtra("booklist") ?: "")
        listItems = getArrayListFromJSONArray(jsonArray) // Save full list
        filteredItems = ArrayList(listItems) // Initialize filtered list with full data
        val adapter = ListAdapterHV(this, R.layout.hadis_view, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true
    }

    // Show page jump dialog and handle user input
    private fun showPageJumpDialog() {
        val totalPages = listView1.adapter.count

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_page_jump, null)
        builder.setView(dialogView)
        val text: TextView = dialogView.findViewById(R.id.text)
        text.text = "কত নাম্বার হাদিসে যাবেন?"
        val pageInput: EditText = dialogView.findViewById(R.id.pageInput)
        pageInput.hint = "1 - $totalPages"

        builder.setPositiveButton("হ্যাঁ") { dialog, _ ->
            val pageNumberStr = pageInput.text.toString().trim()
            if (pageNumberStr.isNotEmpty()) {
                try {
                    val pageNumber = pageNumberStr.toInt()
                    if (pageNumber in 1..totalPages) {
                        listView1.setSelection(pageNumber - 1)
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

        builder.setNegativeButton("না") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    // Load JSON data from assets
    private fun getJSonData(fileName: String): JSONArray? {
        return try {
            val inputStream: InputStream = resources.assets.open(fileName)
            val size = inputStream.available()
            val data = ByteArray(size)
            inputStream.read(data)
            inputStream.close()
            JSONArray(String(data, Charsets.UTF_8))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }
    private fun filterList(query: String) {
        filteredItems.clear() // Clear previous filtered list
        for (item in listItems) {
            val name = item.getString("names")
            if (name.contains(query, ignoreCase = true)) {
                filteredItems.add(item) // Add matched items
            }
        }

        if (filteredItems.isEmpty()) {
            Toast.makeText(this, "কোন হাদিস পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
        }

        // Update adapter with filtered list
        val adapter = ListAdapterHV(this, R.layout.hadis_view, filteredItems)
        listView1.adapter = adapter
    }
    // Parse JSON data into an ArrayList of JSONObjects
    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        jsonArray?.let {
            name = Array(it.length()) { "" }
            author = Array(it.length()) { "" }
            ayaarabic = Array(it.length()) { "" }

            for (i in 0 until it.length()) {
                val jsonObject = it.getJSONObject(i)
                aList.add(jsonObject)
                name[i] = jsonObject.getString("name")
                author[i] = jsonObject.getString("author")
                ayaarabic[i] = jsonObject.getString("names")
            }
        }
        return aList
    }

    // Function to replace Arabic numerals with Bangla numerals and specific text replacements
    private fun replaceArabicNumber(n: String): String {
        return n.replace("1", "১")
            .replace("2", "২")
            .replace("3", "৩")
            .replace("4", "৪")
            .replace("5", "৫")
            .replace("6", "৬")
            .replace("7", "৭")
            .replace("8", "৮")
            .replace("9", "৯")
            .replace("0", "০")
            .replace("<b>", " ")
            .replace("</b>", " ")
            .replace("(রহঃ)", "(رحمة الله)")
            .replace("(রাঃ)", "(رضي الله عنه)")
            .replace("(সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace(" (সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace("('আঃ)", "(عليه السلام)")
            .replace("[১]", "")
            .replace("[২]", "")
            .replace("[৩]", "")
            .replace("(রহ)", "(رحمة الله)")
            .replace("(রা)", "(رضي الله عنه)")
            .replace("(সা)", "(ﷺ)")
            .replace("('আ)", "(عليه السلام)")
            .replace("(সাঃ)", "(ﷺ)")
            .replace("(স)", "(ﷺ)")
            .replace("বিবিন্‌ত", "বিন্‌ত")
            .replace("বিন্ত", "বিন্‌ত")
            .replace("(সা.)", "(ﷺ)")
            .replace("(স.)", "(ﷺ)")
            .replace("bookhozur14","bookhozur14")
            .replace("S2","S2")
    }
}
