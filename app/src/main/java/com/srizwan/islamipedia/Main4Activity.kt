package com.srizwan.islamipedia

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class Main4Activity : AppCompatActivity() {

    private lateinit var jump: ImageView
    private lateinit var back: ImageView
    private lateinit var listView1: ListView
    private lateinit var searchtop: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var nores: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var cancel: ImageView
    private lateinit var searchbox: EditText
    private lateinit var select: LinearLayout
    private lateinit var boxofauthor: TextInputLayout
    private lateinit var boxofsubject: TextInputLayout
    private lateinit var searchboxauthor: EditText
    private lateinit var searchboxsubject: EditText
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var bookid: Array<String>
    private lateinit var subject: Array<String>
    private lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject> // To hold the full list of books

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        select = findViewById(R.id.select)
        nores = findViewById(R.id.nores)
        boxofsubject = findViewById(R.id.boxofsubject)
        boxofauthor = findViewById(R.id.boxofauthor)
        searchboxauthor = findViewById(R.id.searchboxauthor)
        searchboxsubject = findViewById(R.id.searchboxsubject)
        searchboxauthor.isEnabled = false
        searchboxsubject.isEnabled = false
        boxofauthor.setOnClickListener {
            val getauthor = Intent(applicationContext, HadisActivity::class.java).apply {
                putExtra("sub", "ইসলামী বই সমাহার")
                putExtra("booklist", "file.json")
            }
            startActivity(getauthor)
        }
        // Setup UI Elements
        val heading1: TextView = findViewById(R.id.heading1)
        heading1.text = intent.getStringExtra("sub")
        jump = findViewById(R.id.jump)
        jump.visibility = View.GONE
        back = findViewById(R.id.back)
        back.setOnClickListener { finish() }

        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "বইয়ের বা লেখকের নাম লিখে সার্চ করুন"
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
        Handler(Looper.getMainLooper()).postDelayed({
            if (intent?.hasExtra("get") == true) {
                searchbox.text = Editable.Factory.getInstance().newEditable(intent.getStringExtra("get"))
                if (heading1.text == "ইসলামী বই সমাহার") {
                    select.visibility = View.GONE
                    searchtop.visibility = View.VISIBLE
                } else {
                    select.visibility = View.GONE
                    searchtop.visibility = View.GONE
                }
            } else {
                searchbox.setText("")
                select.visibility = View.GONE
                searchtop.visibility = View.VISIBLE
                // Future handling for when the "get" extra is not present
            }
        }, 10)

        select.visibility = View.GONE

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
        // Set system UI colors for higher SDK versions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.parseColor(getString(R.string.color))
                navigationBarColor = Color.parseColor(getString(R.string.color))
            }
        }
        listView1 = findViewById(R.id.listview1)

        // Load data from JSON file
        val jsonArray = getJSonData(intent.getStringExtra("booklist") ?: "")
        listItems = getArrayListFromJSONArray(jsonArray)  // Save the full list
        filteredItems = ArrayList(listItems) // Initially set the filtered items as all items
        val adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true

        // Handle item click
        listView1.setOnItemClickListener { _, _, position, _ ->
            handleListItemClick(position)
        }


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

    // Load JSON data from assets
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

    // Convert JSONArray to ArrayList
    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        try {
            if (jsonArray != null) {
                name = Array(jsonArray.length()) { "" }
                author = Array(jsonArray.length()) { "" }
                bookid = Array(jsonArray.length()) { "" }
                subject = Array(jsonArray.length()) { "" }
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    jsonObject.put("original_index", i)
                    aList.add(jsonObject)
                    name[i] = jsonObject.getString("name")
                    author[i] = jsonObject.getString("author")
                    bookid[i] = jsonObject.getString("bookid")
                    subject[i] = jsonObject.getString("subject")
                }
            }
        } catch (je: JSONException) {
            je.printStackTrace()
        }
        return aList
    }

    // Filter list based on search query
    private fun filterList(query: String) {
        filteredItems.clear()  // Clear the previous filtered list
        for (i in name.indices) {
            if (name[i].contains(query, ignoreCase = true) || author[i].contains(query, ignoreCase = true) || subject[i].contains(query, ignoreCase = true)) {
                filteredItems.add(listItems[i])

                nores.visibility = View.GONE
            }
        }

        if (filteredItems.isEmpty()) {
            nores.visibility = View.VISIBLE
            //Toast.makeText(this, "কোন বই পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
        }

        // Update the adapter with the filtered list
        val adapter = ListAdapter(this, R.layout.list_layoutnew, filteredItems)
        listView1.adapter = adapter
    }

    // Handle ListView item click based on book name
    private fun handleListItemClick(position: Int) {
        // Retrieve the selected book from the filteredItems
        val selectedBook = filteredItems[position]
        val bookName = selectedBook.getString("name")
        val bookId = selectedBook.getString("bookid")
        val bookAuthor = selectedBook.getString("author")
        if (bookName == "সহীহ বুখারী") {
            val hadisjson = Intent(applicationContext, HadisActivity::class.java).apply {
                putExtra("sub", "সহীহ বুখারী")
                putExtra("booklist", "bukhari")
            }
            startActivity(hadisjson)
        } else {
            if (bookName == "সহীহ মুসলিম") {
                val hadisjson = Intent(applicationContext, HadisviewActivity::class.java).apply {
                    putExtra("name", "সহীহ মুসলিম")
                    putExtra("booklist", "muslim")
                }
                startActivity(hadisjson)
            } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("নির্বাচন করুন")
                    val options = arrayOf("অধ্যায় ভিত্তিক কিতাব", "সম্পূর্ণ কিতাব")
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                val json =
                                    Intent(applicationContext, ReadingActivity::class.java).apply {
                                        putExtra("name", bookName)
                                        putExtra("bookname", bookId)
                                        putExtra("author", bookAuthor)
                                    }
                                startActivity(json)
                            }

                            1 -> {
                                val json = Intent(
                                    applicationContext,
                                    FullbookreadActivity::class.java
                                ).apply {
                                    putExtra("name", bookName)
                                    putExtra("bookname", bookId)
                                    putExtra("author", bookAuthor)
                                }
                                startActivity(json)
                            }
                        }
                    }
                    builder.create().show()
                }
            }
        }
    }
