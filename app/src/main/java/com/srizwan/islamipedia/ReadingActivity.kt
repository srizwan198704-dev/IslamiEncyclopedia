package com.srizwan.islamipedia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ReadingActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var listView1: ListView
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var searchtop: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var cancel: ImageView
    private lateinit var searchbox: EditText
    private lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reading)

        // Initialize UI elements
        back = findViewById(R.id.back)
        val heading1: TextView = findViewById(R.id.heading)
        val writer1: TextView = findViewById(R.id.writer)
        heading1.text = intent.getStringExtra("name")
        writer1.text = intent.getStringExtra("author")

        heading1.isFocusable = true
        heading1.isFocusableInTouchMode = true
        heading1.requestFocus()
        heading1.setSelected(true)
        writer1.isFocusable = true
        writer1.isFocusableInTouchMode = true
        writer1.requestFocus()
        writer1.setSelected(true)
        // Set up search box and search view
        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        cancel = findViewById(R.id.cancelme)
        searchtop = findViewById(R.id.searchme)
        searchView = findViewById(R.id.searchView)
        searchbox.setHintTextColor(ColorStateList.valueOf(hintColor))
        cancel.setOnClickListener {
            if (searchbox.text.toString().isEmpty()) {
                searchView.visibility = View.GONE
            } else {
                searchbox.text.clear()
            }
        }

        searchtop.setOnClickListener {
            searchView.visibility =
                if (searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
        })

        listView1 = findViewById(R.id.listview1)
        listView1.isFastScrollEnabled = true

        listView1.setOnItemClickListener { _, _, position, _ ->
            if (heading1.text == "আল কুরআনের শব্দাবলী") {
                val selectedItem = filteredItems[position]
                val author = selectedItem.getString("2") // Fetch the author value
                val copy = selectedItem.getString("1")
                val name = selectedItem.getString("1") // Assuming name is stored in the same field
                try {
                    val intent = Intent(applicationContext, ViewActivity1::class.java)

                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("content", author)
                    editor.apply()

                    intent.putExtra("name", name)
                    startActivity(intent)

                } catch (e: Exception) {
                    val error = e.toString()
                    Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
                }

            } else {
            if (filteredItems.isNotEmpty()) {
                val selectedItem = filteredItems[position]
                val author = selectedItem.getString("2") // Fetch the author value
                val copy = selectedItem.getString("1")
                val name = selectedItem.getString("1") // Assuming name is stored in the same field

                if (author.isNullOrEmpty()) {
                    Toast.makeText(this, "$copy কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                        ClipData.newPlainText(
                            "clipboard",
                            "$copy\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
                        )
                    )
                } else {
                    // Save the name and author directly to SharedPreferences
                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("name", name)
                        putString("author", author) // Save author to SharedPreferences
                        apply()
                    }

                    // Show a Toast to inform that data has been saved
                    //Toast.makeText(this, "Name and author saved to SharedPreferences", Toast.LENGTH_SHORT).show()

                    // Instead of passing via Intent, you can directly open ViewActivity
                    val intent = Intent(applicationContext, ViewActivity::class.java)
                    startActivity(intent)
                }
            }
            }
        }



        back.setOnClickListener { finish() }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.parseColor(getString(R.string.color))
                navigationBarColor = Color.parseColor(getString(R.string.color))
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val jsonArray = getJSonData(intent.getStringExtra("bookname") ?: "")
        listItems = getArrayListFromJSONArray(jsonArray)
        filteredItems = ArrayList(listItems)

        if (heading1.text == "আসমাউল হুসনা") {
            val adapter = ListAdapterC(this, R.layout.list_layout1, filteredItems)
            listView1.adapter = adapter
        } else {
            //if (heading1.text == "আসমাউল নভবী") {
                //val adapter = ListAdapterC(this, R.layout.list_layout1, filteredItems)
                //listView1.adapter = adapter
            //} else {
                val adapter = ListAdapterA(this, R.layout.list_layout1, filteredItems)
                listView1.adapter = adapter
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

    private fun filterList(query: String) {
        filteredItems.clear()
        for (i in listItems.indices) {
            val item = listItems[i]
            val itemName = item.getString("1")
            val itemAuthor = item.getString("1")
            if (itemName.contains(query, ignoreCase = true) || itemAuthor.contains(query, ignoreCase = true)) {
                filteredItems.add(listItems[i])
                val nores: LinearLayout = findViewById(R.id.nores)
                nores.visibility = View.GONE
            }
        }

        if (filteredItems.isEmpty()) {
            val nores: LinearLayout = findViewById(R.id.nores)
            nores.visibility = View.VISIBLE
            //Toast.makeText(this, "কোন অধ্যায় বা অ্যায়ের মধ্যে পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
        }

        val adapter = ListAdapterA(this, R.layout.list_layout1, filteredItems)
        listView1.adapter = adapter
    }

    private fun getJSonData(fileName: String): JSONArray? {
    return try {
        val inputStream = try {
            resources.assets.open(fileName)
        } catch (e: IOException) {
            resources.assets.open("books/$fileName")
        }

        inputStream.use {
            val size = it.available()
            val buffer = ByteArray(size)
            it.read(buffer)
            val json = String(buffer, Charsets.UTF_8)
            JSONArray(json)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } catch (e: JSONException) {
        e.printStackTrace()
        null
    }
}

    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        jsonArray?.let {
            name = Array(it.length()) { "" }
            author = Array(it.length()) { "" }
            for (i in 0 until it.length()) {
                try {
                    val jsonObject = it.getJSONObject(i)
                    jsonObject.put("original_index", i)
                    aList.add(jsonObject)
                    name[i] = jsonObject.getString("1")
                    author[i] = jsonObject.getString("2")
                } catch (je: JSONException) {
                    je.printStackTrace()
                }
            }
        }
        return aList
    }
}
