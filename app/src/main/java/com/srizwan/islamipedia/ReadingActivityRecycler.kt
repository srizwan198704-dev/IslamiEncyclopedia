package com.srizwan.islamipedia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ReadingActivityRecycler : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var name: Array<String>
    private lateinit var author: Array<String>
    private lateinit var searchtop: ImageView
    private lateinit var searchView: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var cancel: ImageView
    private lateinit var searchbox: EditText
    private lateinit var filteredItems: ArrayList<JSONObject>
    private lateinit var listItems: ArrayList<JSONObject>
    private lateinit var adapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.reading)

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
            searchView.visibility = if (searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        searchbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
        })

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val jsonArray = getJSonData(intent.getStringExtra("bookname") ?: "")
        listItems = getArrayListFromJSONArray(jsonArray)
        filteredItems = ArrayList(listItems)

        // Set RecyclerView adapter based on heading1.text
        adapter = if (heading1.text == "আসমাউল হুসনা") {
            RecyclerViewAdapterC(this, filteredItems)
        } else {
            RecyclerViewAdapterC(this, filteredItems)
        }
        
        recyclerView.adapter = adapter
// Handle item clicks for filtered list
        recyclerView.setOnClickListener { view ->
            val position = recyclerView.getChildAdapterPosition(view)
            if (filteredItems.isNotEmpty()) {
                val selectedItem = filteredItems[position]
                val author = selectedItem.getString("2") // Fetch the author value
                val copy = selectedItem.getString("1")
                if (author.isNullOrEmpty()) {
                    Toast.makeText(this@ReadingActivityRecycler, "$copy কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                        ClipData.newPlainText(
                            "clipboard",
                            "$copy\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
                        )
                    )
                } else {
                    val json = Intent(applicationContext, ViewActivity::class.java).apply {
                        putExtra("name", selectedItem.getString("1"))
                        putExtra("author", author)
                    }
                    startActivity(json)
                }
            }
        }

        // Handle item clicks for filtered list
        back.setOnClickListener { finish() }

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
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
        for (item in listItems) {
            val itemName = item.getString("1")
            val itemAuthor = item.getString("2")
            if (itemName.contains(query, ignoreCase = true) || itemAuthor.contains(query, ignoreCase = true)) {
                filteredItems.add(item)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun getJSonData(fileName: String): JSONArray? {
        return try {
            resources.assets.open(fileName).use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                JSONArray(String(buffer, Charsets.UTF_8))
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
