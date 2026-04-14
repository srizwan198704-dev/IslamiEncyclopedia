package com.srizwan.islamipedia


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import androidx.core.text.HtmlCompat
class AllinonemeKT : AppCompatActivity() {

    private var search = ""
    private var length = 0.0
    private var r = 0.0
    private var value1 = ""
    private var value2 = ""
    private var n = 0.0
    private var ListMap = HashMap<String, Any>()
    private var a = ""
    private var b = ""
    private val chapter = ArrayList<HashMap<String, Any>>()
    private var listmap_cache = ArrayList<HashMap<String, Any>>()

    private lateinit var toolbar: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var list: ImageView
    private lateinit var box: LinearLayout
    private lateinit var LinearLayout1: LinearLayout
    private lateinit var searchimg: ImageView
    private lateinit var bookname: TextView
    private lateinit var author: TextView
    private lateinit var searxhmain: LinearLayout
    private lateinit var recyclerview1: RecyclerView
    private lateinit var nores: LinearLayout
    private lateinit var boxofsearch: TextInputLayout
    private lateinit var imageview2: ImageView
    private lateinit var searchbox: EditText
    private lateinit var noresult: ImageView
    private lateinit var no_result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.allinoneme)
        initialize(savedInstanceState)
        initializeLogic()
    }

    private fun initialize(savedInstanceState: Bundle?) {
        toolbar = findViewById(R.id.toolbar)
        content = findViewById(R.id.content)
        list = findViewById(R.id.list)
        box = findViewById(R.id.box)
        LinearLayout1 = findViewById(R.id.LinearLayout1)
        searchimg = findViewById(R.id.searchimg)
        bookname = findViewById(R.id.bookname)
        author = findViewById(R.id.author)
        searxhmain = findViewById(R.id.searxhmain)
        recyclerview1 = findViewById(R.id.recyclerview1)
        nores = findViewById(R.id.nores)
        boxofsearch = findViewById(R.id.boxofsearch)
        imageview2 = findViewById(R.id.imageview2)
        searchbox = findViewById(R.id.searchbox)
        noresult = findViewById(R.id.noresult)
        no_result = findViewById(R.id.no_result)

        list.setOnClickListener {
            finish()
        }

        searchimg.setOnClickListener {
            if (searxhmain.visibility == View.GONE) {
                searxhmain.visibility = View.VISIBLE
            } else {
                searxhmain.visibility = View.GONE
            }
        }

        imageview2.setOnClickListener {
            if (searchbox.text.toString().isEmpty()) {
                searxhmain.visibility = View.GONE
            } else {
                searchbox.text.clear()
            }
        }

        searchbox.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSeq: CharSequence?, start: Int, before: Int, count: Int) {
                val _charSeq = charSeq.toString()
                jsonSearch(_charSeq)
                if (chapter.isEmpty()) {
                    nores.visibility = View.VISIBLE
                    recyclerview1.visibility = View.GONE
                } else {
                    nores.visibility = View.GONE
                    recyclerview1.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(charSeq: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun initializeLogic() {
        bookname.text = Arabic(intent.getStringExtra("name")!!)
        marquee(bookname, intent.getStringExtra("name")!!)
        author.text = intent.getStringExtra("author")

        try {
            val input = assets.open(intent.getStringExtra("file")!!)
            listmap_cache = Gson().fromJson(Rizwan.copyFromInputStream(input), object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type)
            n = 0.0
            for (index in listmap_cache.indices) {
                if (listmap_cache[index]["chapter_id"].toString() == intent.getStringExtra("chapter_id")) {
                    ListMap = HashMap()
                    ListMap["hadith_key"] = listmap_cache[index]["hadith_key"].toString()
                    ListMap["ar"] = listmap_cache[index]["ar"].toString()
                    ListMap["narrator"] = listmap_cache[index]["narrator"].toString()
                    ListMap["grade"] = listmap_cache[index]["grade"].toString()
                    chapter.add(ListMap)
                }
                n++
            }
            recyclerview1.adapter = Recyclerview1Adapter(chapter)
            recyclerview1.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        search = Gson().toJson(chapter)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searxhmain.visibility == View.VISIBLE) {
                    if (searchbox.text.toString().isEmpty()) {
                        searxhmain.visibility = View.GONE
                    } else {
                        searchbox.text.clear()
                    }
                } else {
                    finish()
                }
            }
        })
    }

    fun replaceArabicNumber(n: String): String {
        return n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪")
            .replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯")
            .replace("0", "০")
    }

    fun jsonSearch(charSeq: String) {
        chapter.clear()
        val listType = object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
        val updatedList = Gson().fromJson<ArrayList<HashMap<String, Any>>>(search, listType)
        chapter.addAll(updatedList)

        length = chapter.size.toDouble()
        r = length - 1
        for (index in (length - 1).toInt() downTo 0) {
            value1 = chapter[index]["hadith_key"].toString()
            value2 = chapter[index]["narrator"].toString()
            if (!(charSeq.length > value1.length) && value1.contains(charSeq, true)) {
                continue
            } else {
                if (!(charSeq.length > value2.length) && value2.contains(charSeq, true)) {
                    continue
                } else {
                    chapter.removeAt(index)
                }
            }
        }
        recyclerview1.adapter = Recyclerview1Adapter(chapter)
        recyclerview1.layoutManager = LinearLayoutManager(this)
    }

    fun marquee(text: TextView, texto: String) {
        text.text = texto
        text.isSelected = true
        text.ellipsize = TextUtils.TruncateAt.MARQUEE
        text.setHorizontallyScrolling(true)
        text.marqueeRepeatLimit = -1
        text.isSingleLine = true
        text.isFocusable = true
        text.isFocusableInTouchMode = true
    }

    fun Arabic(n: String): String {
        return n.replace("<b>", " ")
            .replace("</b>", " ")
            .replace("(রহঃ)", "(رحمة الله)")
            .replace("(রাঃ)", "(رضي الله عنه)")
            .replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লালাম)", "(ﷺ)")
            .replace("(‘আঃ)", "(عليه السلام)")
            .replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লালাম)", "(ﷺ)")
            .replace("(রাঃ)", "(رضي الله عنه)")
            .replace("[১]", "")
            .replace("[২]", "")
            .replace("[৩]", "")
            .replace("(রহ)", "(رحمة الله)")
            .replace("(রা)", "(رضي الله عنه)")
            .replace("(সা)", "(ﷺ)")
            .replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লালাম)", "(ﷺ)")
            .replace("(‘আ)", "(عليه السلام)")
            .replace("(সাঃ)", "(ﷺ)")
            .replace("(রাঃ)", "(رضي الله عنه)")
            .replace("(স)", "(ﷺ)")
            .replace("বিবিন্‌ত", "বিন্‌ত")
            .replace("বিন্ত", "বিন্‌ত")
            .replace("(সা.)", "(ﷺ)")
            .replace("(স.)", "(ﷺ)")
    }

    inner class Recyclerview1Adapter(private val data: ArrayList<HashMap<String, Any>>) : RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.hadisviewmore, parent, false)

            val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.layoutParams = lp
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView

            val linear1: LinearLayout = view.findViewById(R.id.linear1)
            val linear2: LinearLayout = view.findViewById(R.id.linear2)
            val book: TextView = view.findViewById(R.id.book)
            val title: TextView = view.findViewById(R.id.title)
            val description_ar: TextView = view.findViewById(R.id.description_ar)
            val description: TextView = view.findViewById(R.id.description)
            val hadith_number: TextView = view.findViewById(R.id.hadith_number)
            val linear3: LinearLayout = view.findViewById(R.id.linear3)
            val copy: ImageView = view.findViewById(R.id.copy)
            val share: ImageView = view.findViewById(R.id.share)

            val sketchUi = android.graphics.drawable.GradientDrawable()
            val d = resources.displayMetrics.density.toInt()

// Explicitly pass the color as an Int
            sketchUi.setColor(0xFFFFFFFF.toInt()) // This should work correctly

            sketchUi.cornerRadius = (d * 24).toFloat()
            sketchUi.setStroke(d, 0xFF01837A.toInt())
            linear1.elevation = (d * 7).toFloat()

            val colorStateList = android.content.res.ColorStateList(
                arrayOf(intArrayOf()), // No state array
                intArrayOf(0xFF01837A.toInt()) // Color array with a single color
            )

            val rippleUi = android.graphics.drawable.RippleDrawable(
                colorStateList, // Use the corrected ColorStateList
                sketchUi, null
            )

            linear1.background = rippleUi



            val currentItem = chapter[position]
            hadith_number.text = HtmlCompat.fromHtml(Arabic(currentItem["hadith_key"].toString()), HtmlCompat.FROM_HTML_MODE_LEGACY)
            book.text = author.text.toString()
            title.text = HtmlCompat.fromHtml(Arabic(currentItem["ar"].toString()), HtmlCompat.FROM_HTML_MODE_LEGACY)
            description_ar.text = HtmlCompat.fromHtml(Arabic(currentItem["narrator"].toString()), HtmlCompat.FROM_HTML_MODE_LEGACY)
            description.text = HtmlCompat.fromHtml(Arabic(currentItem["grade"].toString()), HtmlCompat.FROM_HTML_MODE_LEGACY)

            if (currentItem["ar"].toString().isEmpty()) {
                linear1.visibility = View.GONE
            } else {
                linear1.visibility = View.VISIBLE
            }

            copy.setOnClickListener {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                    ClipData.newPlainText(
                        "clipboard",
                        "${hadith_number.text}\n${title.text}\n${description_ar.text}\n${description.text}\n${book.text}"
                    )
                )
                Toast.makeText(applicationContext, "${hadith_number.text} কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
            }

            share.setOnClickListener {
                a = "হাদিসটি শেয়ার করুন"
                b = "${hadith_number.text}\n${title.text}\n${description_ar.text}\n${description.text}\n${book.text}"
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, a)
                    putExtra(Intent.EXTRA_TEXT, b)
                }
                startActivity(Intent.createChooser(i, "হাদিসটি শেয়ার করুন"))
                Toast.makeText(applicationContext, "${hadith_number.text} শেয়ার করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int = data.size

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v)
    }
}
