package com.srizwan.islamipedia


import android.content.*
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.*
import android.os.*
import android.text.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList
import java.util.HashMap
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.TextView
import java.io.IOException

class FullbookreadActivity : AppCompatActivity() {

    private var click = 0.0
    private var click0 = 0.0
    private var a = ""
    private var b = ""

    private var map = ArrayList<HashMap<String, Any>>()

    private lateinit var toolbar: LinearLayout
    private lateinit var viewpager1: ViewPager
    private lateinit var list: ImageView
    private lateinit var box: LinearLayout
    private lateinit var pagenumberbox: LinearLayout
    private lateinit var bookname: TextView
    private lateinit var authorname: TextView
    private lateinit var page: TextView
    private lateinit var pagenumber: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewDrawer: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fullbookread)
        initialize(savedInstanceState)
        initializeLogic()
    }

    private fun initialize(savedInstanceState: Bundle?) {
        toolbar = findViewById(R.id.toolbar)
        viewpager1 = findViewById(R.id.viewpager1)
        list = findViewById(R.id.list)
        box = findViewById(R.id.box)
        pagenumberbox = findViewById(R.id.pagenumberbox)
        bookname = findViewById(R.id.bookname)
        authorname = findViewById(R.id.authorname)
        page = findViewById(R.id.page)
        pagenumber = findViewById(R.id.pagenumber)

        viewpager1.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                page.text = _replaceArabicNumber((position + 1).toString())
                pagenumber.text = _replaceArabicNumber(" / ${map.size.toLong()}")
                saveLastPage(intent.getStringExtra("bookname") ?: "", position)
            }

            override fun onPageScrollStateChanged(scrollState: Int) {}
        })

        drawerLayout = findViewById(R.id.drawer_layout)
        recyclerViewDrawer = findViewById(R.id.recycler_view_drawer)

        list.setImageResource(R.drawable.menu)
        list.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        pagenumberbox.setOnClickListener {
            showPageJumpDialog()
        }
    }

    private fun initializeLogic() {
        _status_bar_color("#FF01837A", "#FF01837A")
        click = 16.0
        click0 = 14.0
        viewpager1.visibility = View.VISIBLE
        bookname.text = intent.getStringExtra("name")
        authorname.text = intent.getStringExtra("author")
        _marquue(bookname, intent.getStringExtra("name") ?: "")
        _marquue(authorname, intent.getStringExtra("author") ?: "")
        try {
    val bookName = intent.getStringExtra("bookname") ?: ""
    val inputStream1 = try {
        assets.open(bookName)
    } catch (e: IOException) {
        assets.open("books/$bookName")
    }
    val jsonString = inputStream1.bufferedReader().use { it.readText() }
    map = Gson().fromJson(
        jsonString,
        object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
    )
    viewpager1.adapter = Viewpager1Adapter(map)
    (viewpager1.adapter as PagerAdapter).notifyDataSetChanged()
} catch (e: Exception) {
    // Handle exception
    e.printStackTrace() // Optionally log the exception for debugging
        }
        page.text = "১"
        pagenumber.text = _replaceArabicNumber(" / ${map.size.toLong()}")
        askResumeLastPage(intent.getStringExtra("bookname") ?: "")
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    finish()
            }
        })

        recyclerViewDrawer.layoutManager = LinearLayoutManager(this)
        recyclerViewDrawer.adapter = DrawerAdapter(this, map) { position ->
            viewpager1.currentItem = position
            drawerLayout.closeDrawer(GravityCompat.START)
        }



    }

    private fun saveLastPage(bookname: String, page: Int) {
        val prefs = getSharedPreferences("last_page_pref", MODE_PRIVATE)
        prefs.edit().putInt(bookname, page).apply()
    }

    private fun askResumeLastPage(bookname: String) {
        val prefs = getSharedPreferences("last_page_pref", MODE_PRIVATE)
        val lastPage = prefs.getInt(bookname, -1)

        if (lastPage != -1) { // যদি আগের পেজ থাকে
            val builder = AlertDialog.Builder(this)
            builder.setMessage("শেষবার যেখান থেকে পড়েছিলেন সেখান থেকে আবার পড়বেন?")
            builder.setPositiveButton("হ্যাঁ") { _, _ ->
                viewpager1.currentItem = lastPage
            }
            builder.setNegativeButton("না") { _, _ ->
                viewpager1.currentItem = 0
            }
            builder.show()
        } else {
            viewpager1.currentItem = 0
        }
    }

    private fun _marquue(text: TextView, texto: String) {
        text.text = texto
        text.ellipsize = TextUtils.TruncateAt.MARQUEE
        text.isSelected = true
        text.marqueeRepeatLimit = -1
        text.isSingleLine = true
        text.isFocusable = true
        text.isFocusableInTouchMode = true
    }

    private fun _status_bar_color(colour1: String, colour2: String) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val w = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = Color.parseColor(colour1)
            w.navigationBarColor = Color.parseColor(colour2)
        }
    }

    private fun _replaceArabicNumber(n: String): String {
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
            .replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace("(‘আঃ)", "(عليه السلام)")
            .replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace("(রাঃ)", "(رضي الله عنه)")
            .replace("[১]", "")
            .replace("[২]", "")
            .replace("[৩]", "")
            .replace("(রহ)", "(رحمة الله)")
            .replace("(রা)", "(رضي الله عنه)")
            .replace("(সা)", "(ﷺ)")
            .replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace("(‘আ)", "(عليه السلام)")
            .replace("(সাঃ)", "(ﷺ)")
            .replace("(রাঃ)", "(رضي الله عنه)")
            .replace("(স)", "(ﷺ)")
            .replace("বিবিন্‌ত", "বিন্‌ত")
            .replace("বিন্ত", "বিন্‌ত")
            .replace("(সা.)", "(ﷺ)")
            .replace("(স.)", "(ﷺ)")
            .replace("স.", "(ﷺ)")
    }
    private fun showPageJumpDialog() {
        val totalPages = viewpager1.adapter?.count ?: 0

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_page_jump, null)
        builder.setView(dialogView)
        val text: TextView = dialogView.findViewById(R.id.text)
        text.text = "কত নাম্বার পেজে যাবেন?"
        val pageInput: EditText = dialogView.findViewById(R.id.pageInput)
        pageInput.hint = _replaceArabicNumber("১ - $totalPages")

        builder.setPositiveButton("হ্যাঁ") { dialog, _ ->
            val pageNumberStr = pageInput.text.toString().trim()
            if (pageNumberStr.isNotEmpty()) {
                try {
                    val pageNumber = pageNumberStr.toInt()
                    if (pageNumber in 1..totalPages) {
                        viewpager1.currentItem = pageNumber - 1
                    } else {
                        Toast.makeText(applicationContext, "Invalid page number", Toast.LENGTH_SHORT).show()
                    }
                } catch (_: NumberFormatException) {
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
    class DrawerAdapter(
        private val items: List<HashMap<String, Any>>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(android.R.id.text1)

            init {
                view.setOnClickListener {
                    onItemClick(adapterPosition)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val textView = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(textView)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val title = items[position]["1"]?.toString() ?: "পৃষ্ঠা ${position + 1}"
            holder.title.text = title
        }
    }


    inner class Viewpager1Adapter(private val data: ArrayList<HashMap<String, Any>>) : PagerAdapter() {

        private val context: Context = applicationContext

        override fun getCount(): Int {
            return data.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getItemPosition(`object`: Any): Int {
            return super.getItemPosition(`object`)
        }

        override fun getPageTitle(pos: Int): CharSequence {
            return "page $pos"
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(context).inflate(R.layout.fullbook, container, false)

            val main = view.findViewById<LinearLayout>(R.id.main)
            val Tab = view.findViewById<LinearLayout>(R.id.Tab)
            val name = view.findViewById<TextView>(R.id.name)
            val author = view.findViewById<TextView>(R.id.author)
            val back = view.findViewById<LinearLayout>(R.id.back)
            val share = view.findViewById<LinearLayout>(R.id.share)
            val copy = view.findViewById<LinearLayout>(R.id.copy)
            val high = view.findViewById<LinearLayout>(R.id.high)
            val low = view.findViewById<LinearLayout>(R.id.low)
            val next = view.findViewById<LinearLayout>(R.id.next)
            val hide = view.findViewById<TextView>(R.id.hide)
            val bookback = view.findViewById<LinearLayout>(R.id.bookback)
            val booknext0 = view.findViewById<LinearLayout>(R.id.booknext0)

            if (data[position].containsKey("1")) {
                name.text = _replaceArabicNumber(data[position]["1"].toString())
                author.text = _replaceArabicNumber(data[position]["2"].toString())
                hide.text = _replaceArabicNumber(data[position]["1"].toString())
            } else {
                Toast.makeText(
                    applicationContext,
                    "রিপোর্ট করুন",
                    Toast.LENGTH_SHORT
                ).show()

            }
            main.visibility = if (author.text.toString().isEmpty()) View.GONE else View.VISIBLE
            if (author.text.toString().contains("<") && author.text.toString().contains(">")) {
                author.text = Html.fromHtml(data[position]["2"].toString())
            }
            high.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            back.setOnClickListener {
                if (position > 0) {
                    viewpager1.currentItem = position - 1
                }
            }
            next.setOnClickListener {
                if (position < map.size - 1) {
                    viewpager1.currentItem = position + 1
                }
            }
            bookback.setOnClickListener {
                if (position > 0) {
                    viewpager1.currentItem = position - 1
                }
            }
            booknext0.setOnClickListener {
                if (position < map.size - 1) {
                    viewpager1.currentItem = position + 1
                }
            }
            // Adjust alpha for back and next buttons
            if (position == 0) {
                back.alpha = 0.0f
                bookback.alpha = 0.0f
            } else {
                back.alpha = 1.0f
                bookback.alpha = 1.0f
            }

            if (position == data.size - 1) {
                next.alpha = 0.0f
                booknext0.alpha = 0.0f
            } else {
                next.alpha = 1.0f
                booknext0.alpha = 1.0f
            }

            share.setOnClickListener {
                Toast.makeText(
                    applicationContext,
                    "${name.text} শেয়ার করা হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()
                a = "এপ্সটি শেয়ার করুন"
                b = "${name.text}\n____________________\n${author.text}\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ গ্রবথ S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, a)
                    putExtra(Intent.EXTRA_TEXT, b)
                }
                startActivity(Intent.createChooser(i, "লেখা গুলো শেয়ার করুন"))
            }
            copy.setOnClickListener {
                Toast.makeText(
                    applicationContext,
                    "${name.text} কপি করা হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("clipboard", "${name.text}\n${author.text}\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ গ্রবথ S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"))
            }
            high.setOnClickListener {
                click++
                author.textSize = click.toFloat()
            }
            low.setOnClickListener {
                click--
                author.textSize = click.toFloat()
            }
            name.setTextIsSelectable(true)
            author.setTextIsSelectable(true)
            Tab.visibility = View.VISIBLE

            container.addView(view)
            return view
        }
    }
}

