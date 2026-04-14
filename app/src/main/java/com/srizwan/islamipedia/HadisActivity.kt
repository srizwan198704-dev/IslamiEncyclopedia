package com.srizwan.islamipedia

import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class HadisActivity : AppCompatActivity() {
    private lateinit var jump: ImageView
    private lateinit var back: ImageView
    private lateinit var listView1: ListView
    private val json = Intent()
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
        heading1.text = intent.getStringExtra("sub")
        jump = findViewById(R.id.jump)
        jump.visibility = View.GONE
        back = findViewById(R.id.back)
        listView1 = findViewById(R.id.listview1)
        back.setOnClickListener { finish() }

        listView1.setOnItemClickListener { _, _, position, _ ->
            val selectedBook = filteredItems[position]
            val bookName = selectedBook.getString("name")
            val bookAuthor = selectedBook.getString("author")
            json.setClass(applicationContext, HadisviewActivity::class.java)
            json.putExtra("name", bookName)
            json.putExtra("booklist", bookAuthor)
            startActivity(json)
        }

        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()

        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "অধ্যায় সার্চ করুন"
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val w: Window = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = Color.parseColor(getString(R.string.color))
            w.navigationBarColor = Color.parseColor(getString(R.string.color))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val jsonArray = getJSonData(intent.getStringExtra("booklist") ?: "")
        listItems = getArrayListFromJSONArray(jsonArray) // Save full list
        filteredItems = ArrayList(listItems) // Initialize filtered list with full data
        val adapter = ListAdapterH(this, R.layout.quran_list, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true
    }

    private fun getJSonData(fileName: String): JSONArray? {
        var jsonArray: JSONArray? = null
        try {
            val inputStream: InputStream = resources.assets.open(fileName)
            val size: Int = inputStream.available()
            val data = ByteArray(size)
            inputStream.read(data)
            inputStream.close()
            val json = String(data, Charsets.UTF_8)
            jsonArray = JSONArray(json)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonArray
    }

    private fun filterList(query: String) {
        filteredItems.clear() // Clear previous filtered list
        for (item in listItems) {
            val name = item.getString("name")
            if (name.contains(query, ignoreCase = true)) {
                filteredItems.add(item) // Add matched items
            }
        }

        if (filteredItems.isEmpty()) {
            Toast.makeText(this, "কোন আধ্যায় পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
        }

        // Update adapter with filtered list
        val adapter = ListAdapterH(this, R.layout.quran_list, filteredItems)
        listView1.adapter = adapter
    }

    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        try {
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    aList.add(jsonObject)
                }
            }
        } catch (je: JSONException) {
            je.printStackTrace()
        }
        return aList
    }
}
