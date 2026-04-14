package com.srizwan.islamipedia

import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class QuranActivity : AppCompatActivity() {
    private lateinit var jump: ImageView
    private lateinit var back: ImageView
    private lateinit var listView1: ListView
    private val json = Intent()
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var bookid: Array<String>
    private lateinit var ayanumber: Array<String>
    private lateinit var ayaarabic: Array<String>
    private lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject> // To hold the full list of books
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
            json.setClass(applicationContext, QuranviewActivity::class.java)
            json.putExtra("name", bookName)
            json.putExtra("booklist", "$bookAuthor.json")
            startActivity(json)
        }
        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "সুরা সার্চ করুন"
        searchbox.setHintTextColor(ColorStateList.valueOf(hintColor))
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
            override fun afterTextChanged(s: Editable?) {
                // Do nothing here
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing here
            }

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
        listItems = getArrayListFromJSONArray(jsonArray)  // Save the full list
        filteredItems = ArrayList(listItems) // Initially set the filtered items as all items
        val adapter = ListAdapterQ(this, R.layout.quran_list, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchView.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) {
                        searchView.visibility = View.GONE
                    } else {
                        searchbox.text.clear()
                    }
                } else {
                    finish()
                }
            }
        })
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
    // Filter list based on search query
    private fun filterList(query: String) {
        filteredItems.clear()  // Clear the previous filtered list
        for (i in name.indices) {
            if (name[i].contains(query, ignoreCase = true)) {
                filteredItems.add(JSONObject().apply {
                    put("name", name[i])
                    put("author", author[i])
                    put("bookid", bookid[i])
                    put("verses", ayanumber[i])
                    put("names", ayaarabic[i])
                })
                val nores: LinearLayout = findViewById(R.id.nores)
                nores.visibility = View.GONE
            }
        }

        if (filteredItems.isEmpty()) {
            val nores: LinearLayout = findViewById(R.id.nores)
            nores.visibility = View.VISIBLE
            //Toast.makeText(this, "কোন সুরা পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
        }

        // Update the adapter with the filtered list
        val adapter = ListAdapterQ(this, R.layout.quran_list, filteredItems)
        listView1.adapter = adapter
    }
    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        try {
            if (jsonArray != null) {
                name = Array(jsonArray.length()) { "" }
                author = Array(jsonArray.length()) { "" }
                bookid = Array(jsonArray.length()) { "" }
                ayanumber = Array(jsonArray.length()) { "" }
                ayaarabic = Array(jsonArray.length()) { "" }
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    aList.add(jsonObject)
                    name[i] = jsonObject.getString("name")
                    author[i] = jsonObject.getString("author")
                    bookid[i] = jsonObject.getString("bookid")
                    ayanumber[i] = jsonObject.getString("verses")
                    ayaarabic[i] = jsonObject.getString("names")
                }
            }
        } catch (je: JSONException) {
            je.printStackTrace()
        }
        return aList
    }
}
