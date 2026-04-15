package com.srizwan.islamipedia

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
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
import androidx.appcompat.app.AlertDialog
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
import java.io.File
import java.net.URL
import java.net.HttpURLConnection
import java.io.BufferedInputStream
import java.io.FileOutputStream

class QuranviewActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    private lateinit var jump: ImageView
    private lateinit var listView1: ListView
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
    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private var currentPlayingId: String? = null
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
        val playAudio: ImageView = findViewById(R.id.playAudio)
        val audiotab: LinearLayout = findViewById(R.id.audiotab)
        audiotab.visibility = View.VISIBLE
        /*
        val sketchUi = android.graphics.drawable.GradientDrawable()
        val d = applicationContext.resources.displayMetrics.density
        sketchUi.setColor(0xFF01837A.toInt())
        sketchUi.cornerRadius = d * 30
        audiotab.elevation = d * 5
        audiotab.background = sketchUi
         */
        val previous: LinearLayout = findViewById(R.id.previous)
        val next: LinearLayout = findViewById(R.id.next)
        val stop: LinearLayout = findViewById(R.id.stop)
        val playAudio1: LinearLayout = findViewById(R.id.playAudio1)
        playAudio1.setOnClickListener {
            playAudio.performClick()
        }
        previous.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
            }
            startPlayingFromIndex(currentIndex)
        }

        next.setOnClickListener {
            if (currentIndex < filteredItems.size - 1) {
                currentIndex++
            }
            startPlayingFromIndex(currentIndex)
        }

        stop.setOnClickListener {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    currentPlayingId = null
                    currentIndex = 0

                    // Reset AudioTab icon
                    val playAudio: ImageView = findViewById(R.id.playAudio)
                    playAudio.setImageResource(R.drawable.play)

                    // Reset ListView icons
                    notifyListView()

                    Toast.makeText(this, "অডিও প্লে বন্ধ হয়েছে।", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "এখন কোনো সূরা অডিও চলছে না", Toast.LENGTH_SHORT).show()
                }
            }
        }


        playAudio.setOnClickListener {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    notifyListView()
                    playAudio.setImageResource(R.drawable.play) // প্লে আইকন দেখাও
                } else {
                    // যদি সব অডিও শেষ হয়ে যায়, আবার শুরু করো
                    if (currentIndex >= filteredItems.size) {
                        currentIndex = 0
                        startPlayingFromIndex(currentIndex)
                        notifyListView()
                        playAudio.setImageResource(R.drawable.pause)
                    } else {
                        mediaPlayer?.start()
                        notifyListView()
                        playAudio.setImageResource(R.drawable.pause) // পজ আইকন দেখাও
                    }
                }
            } else {
                // যদি আগে কখনো প্লে না হয়ে থাকে, তাহলে প্রথম আয়াত থেকে শুরু করো
                if (filteredItems.isNotEmpty()) {
                    currentIndex = 0
                    notifyListView()
                    startPlayingFromIndex(currentIndex)
                    playAudio.setImageResource(R.drawable.pause)
                } else {
                    Toast.makeText(this, "প্লে করার মতো আয়াত নেই।", Toast.LENGTH_SHORT).show()
                }
            }
        }



        boxofsearch = findViewById(R.id.boxofsearch)
        boxofsearch.setBoxCornerRadii(100f, 100f, 100f, 100f)
        boxofsearch.boxBackgroundColor = 0xFFFFFFFF.toInt()
        val hintColor = ContextCompat.getColor(this, R.color.purple_500)
        boxofsearch.setHintTextColor(ColorStateList.valueOf(hintColor))
        searchbox = findViewById(R.id.searchbox)
        boxofsearch.hint = "আয়াত সার্চ করুন"
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

        listView1.setOnItemClickListener { _, _, position, _ ->
            val selectedBook = filteredItems[position]
            val headingText = heading1.text.toString()
            val selectedAyaNumber = selectedBook.getString("verses")
            val selectedAyaText = selectedBook.getString("names")
            val selectedName = selectedBook.getString("name")
            val selectedAuthor = selectedBook.getString("author")

            // Prepare text for clipboard and sharing
            val textToCopy =
                "$headingText আয়াত নং : ${replaceArabicNumber(selectedAyaNumber)}\n${replaceArabicNumber(selectedAyaText)}\n\nকানযুল ঈমান\n\n${replaceArabicNumber(selectedAyaNumber)}. ${replaceArabicNumber(selectedName)}\n\nইরফানুল কুরআন\n\n${replaceArabicNumber(selectedAuthor)}\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 :  https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"


            // Show a dialog with Copy and Share options
            val dialog = android.app.AlertDialog.Builder(this)
                .setMessage("নির্বাচন করুন")
                .setPositiveButton("শুনুন") { _, _ ->
                    val audioId = selectedBook.getString("_id")
                    val fileName = "$audioId.mp3"
                    val file = File(getExternalFilesDir(null), fileName)
                    val url = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$audioId.mp3"

                    // index বের করি যাতে সেটি থেকে ধারাবাহিকভাবে চালানো যায়
                    val startIndex = filteredItems.indexOfFirst { it.getString("_id") == audioId }

                    if (startIndex != -1) {
                        scrollToAya(audioId)
                        startPlayingFromIndex(startIndex)

                    } else {
                        Toast.makeText(this, "আয়াত পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
                    }
                }

                .setNegativeButton("কপি") { _, _ ->
                    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("clipboard", textToCopy))
                    val copiedText = replaceArabicNumber("${heading1.text} আয়াত নং : $selectedAyaNumber কপি করা হয়েছে")
                    Toast.makeText(this, copiedText, Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton("শেয়ার") { _, _ ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, textToCopy)
                    }
                    Toast.makeText(this, "শেয়ার উইন্ডো খুলছে...", Toast.LENGTH_SHORT).show()
                    startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                }
                .create()

            dialog.show()
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
        listItems = getArrayListFromJSONArray(jsonArray)  // Save the full list
        filteredItems = ArrayList(listItems) // Initially set the filtered items as all items
        val adapter = ListAdapterV(this, R.layout.quran_aya, filteredItems)
        listView1.adapter = adapter
        listView1.isFastScrollEnabled = true



        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                }
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


    // Filter list based on search query
    private fun filterList(query: String) {
        filteredItems.clear()  // Clear the previous filtered list
        for (i in name.indices) {
            if (name[i].contains(query, ignoreCase = true)) {
                filteredItems.add(JSONObject().apply {
                    put("_id", listItems[i].getString("_id"))
                    put("name", name[i])
                    put("author", author[i])
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
            //Toast.makeText(this, "কোন আয়াত পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
        }

        // Update the adapter with the filtered list
        val adapter = ListAdapterV(this, R.layout.quran_aya, filteredItems)
        listView1.adapter = adapter
    }
    // Show page jump dialog and handle user input
    private fun showPageJumpDialog() {
        val totalPages = listView1.adapter.count

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_page_jump, null)
        builder.setView(dialogView)

        val pageInput: EditText = dialogView.findViewById(R.id.pageInput)
        pageInput.hint = "0 - ${totalPages-1}"

        builder.setPositiveButton("হ্যাঁ") { dialog, _ ->
            val pageNumberStr = pageInput.text.toString().trim()
            if (pageNumberStr.isNotEmpty()) {
                try {
                    val pageNumber = pageNumberStr.toInt()
                    if (pageNumber in 0..totalPages) {
                        listView1.setSelection(pageNumber)
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
    fun playme(item: JSONObject) {
        val audioId = item.getString("_id")
        val fileName = "$audioId.mp3"
        val file = File(getExternalFilesDir(null), fileName)
        val url = "https://cdn.islamic.network/quran/audio/64/ar.alafasy/$audioId.mp3"

        // যদি আগে থেকেই প্লেয়ারে অডিও চালু থাকে, তবে পজ করুন
        if (audioId == currentPlayingId) {
            togglePlayPause()
            return
        }

        // নতুন অডিও প্লে হলে আগেরটি রিসেট করুন
        stopCurrentPlaying()

        // index বের করি যাতে সেটি থেকে ধারাবাহিকভাবে চালানো যায়
        val startIndex = filteredItems.indexOfFirst { it.getString("_id") == audioId }

        if (startIndex != -1) {
            currentPlayingId = audioId
            currentIndex = startIndex
            scrollToAya(audioId)
            startPlayingFromIndex(startIndex)

            // Update AudioTab icon
            val playAudio: ImageView = findViewById(R.id.playAudio)
            playAudio.setImageResource(R.drawable.pause)

            // Update ListView icons
            notifyListView()
        } else {
            Toast.makeText(this, "আয়াত পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
        }
    }


    // বর্তমান প্লেয়িং ID রিসেট করতে এই ফাংশনটি ব্যবহার করুন
    fun stopCurrentPlaying() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingId = null
        currentIndex = -1

        // Update AudioTab icon
        val playAudio: ImageView = findViewById(R.id.playAudio)
        playAudio.setImageResource(R.drawable.play)

        // Update ListView icons
        notifyListView()
    }


    fun getCurrentPlayingId(): String? = currentPlayingId
    fun isAudioPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                // Update AudioTab icon
                val playAudio: ImageView = findViewById(R.id.playAudio)
                playAudio.setImageResource(R.drawable.play)
            } else {
                it.start()
                // Update AudioTab icon
                val playAudio: ImageView = findViewById(R.id.playAudio)
                playAudio.setImageResource(R.drawable.pause)
            }

            // Update ListView icons
            notifyListView()
        }
    }


    fun notifyListView() {
        runOnUiThread {
            (listView1.adapter as? ListAdapterV)?.notifyDataSetChanged()
        }
    }

    fun copyme(item: JSONObject) {
        val verses = item.optString("verses")
        val names = item.optString("names")
        val name = item.optString("name")
        val author = item.optString("author")

        val textToCopy =
            "আয়াত নং : ${replaceArabicNumber(verses)}\n${replaceArabicNumber(names)}\n\nকানযুল ঈমান\n\n${replaceArabicNumber(verses)}. ${replaceArabicNumber(name)}\n\nইরফানুল কুরআন\n\n${replaceArabicNumber(author)}\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 :  https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"


        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", textToCopy)
        clipboard.setPrimaryClip(clip)

        val copiedText = replaceArabicNumber("আয়াত নং : $verses কপি করা হয়েছে")
        Toast.makeText(this, copiedText, Toast.LENGTH_SHORT).show()
    }

    fun shareme(item: JSONObject) {
        val verses = item.optString("verses")
        val names = item.optString("names")
        val name = item.optString("name")
        val author = item.optString("author")

        val textToShare =
            "আয়াত নং : ${replaceArabicNumber(verses)}\n${replaceArabicNumber(names)}\n\nকানযুল ঈমান\n\n${replaceArabicNumber(verses)}. ${replaceArabicNumber(name)}\n\nইরফানুল কুরআন\n\n${replaceArabicNumber(author)}\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 :  https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"


        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToShare)
            type = "text/plain"
        }
        Toast.makeText(this, "শেয়ার উইন্ডো খুলছে...", Toast.LENGTH_SHORT).show()
        startActivity(Intent.createChooser(intent, "শেয়ার করুন"))
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

    // Parse JSON data into an ArrayList of JSONObjects
    private fun getArrayListFromJSONArray(jsonArray: JSONArray?): ArrayList<JSONObject> {
        val aList = ArrayList<JSONObject>()
        jsonArray?.let {
            name = Array(it.length()) { "" }
            author = Array(it.length()) { "" }
            ayanumber = Array(it.length()) { "" }
            ayaarabic = Array(it.length()) { "" }

            for (i in 0 until it.length()) {
                val jsonObject = it.getJSONObject(i)
                aList.add(jsonObject)
                name[i] = jsonObject.getString("name")
                author[i] = jsonObject.getString("author")
                ayanumber[i] = jsonObject.getString("verses")
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
    private fun startPlayingFromIndex(index: Int) {
        // যদি শেষ আয়াত প্লে হয়ে যায়
        if (index >= filteredItems.size) {
            // সবকিছু রিসেট করুন
            stopCurrentPlaying()
            //Toast.makeText(this, "সব আয়াত প্লে হয়েছে।", Toast.LENGTH_SHORT).show()
            return
        }

        // Update current index and ID
        currentIndex = index
        val currentItem = filteredItems[currentIndex]
        val audioId = currentItem.getString("_id")
        val audioUrl = "https://cdn.islamic.network/quran/audio/64/ar.alafasy/$audioId.mp3"
        val fileName = "$audioId.mp3"
        val file = File(getExternalFilesDir(null), fileName)

        // Set the current playing ID
        currentPlayingId = audioId

        // Scroll to the playing aya
        scrollToAya(audioId)

        // Play the audio
        if (file.exists()) {
            playAudioFile(file)
            downloadNextAudios(currentIndex + 1)
        } else {
            downloadAndPlayFirstAudio(audioUrl, file)
        }

        // Update AudioTab icon
        val playAudio: ImageView = findViewById(R.id.playAudio)
        playAudio.setImageResource(R.drawable.pause)

        // Update ListView icons
        notifyListView()
    }

    // প্রথম অডিওর জন্য স্পেশাল ফাংশন
    private fun downloadAndPlayFirstAudio(url: String, file: File) {
        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(file)

                val data = ByteArray(1024)
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                runOnUiThread {
                    //Toast.makeText(this, "অডিও ডাউনলোড শেষ। চালু হচ্ছে...", Toast.LENGTH_SHORT).show()
                    playAudioFile(file)
                    downloadNextAudios(currentIndex + 1) // প্রথম ডাউনলোডের পর পরবর্তী ডাউনলোড শুরু
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "ডাউনলোডে সমস্যা হয়েছে।", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // পরবর্তী অডিওগুলো সিরিয়ালি ডাউনলোড করার জন্য
    private fun downloadNextAudios(startIndex: Int) {
        Thread {
            for (i in startIndex until filteredItems.size) {
                val currentItem = filteredItems[i]
                val audioId = currentItem.getString("_id")
                val audioUrl = "https://cdn.islamic.network/quran/audio/64/ar.alafasy/$audioId.mp3"
                val fileName = "$audioId.mp3"
                val file = File(getExternalFilesDir(null), fileName)

                if (!file.exists()) {
                    try {
                        val connection = URL(audioUrl).openConnection() as HttpURLConnection
                        connection.connect()
                        val input = BufferedInputStream(connection.inputStream)
                        val output = FileOutputStream(file)

                        val data = ByteArray(1024)
                        var count: Int
                        while (input.read(data).also { count = it } != -1) {
                            output.write(data, 0, count)
                        }

                        output.flush()
                        output.close()
                        input.close()

                        runOnUiThread {
                            //Toast.makeText(this, "${audioId} ডাউনলোড হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this, "${audioId} ডাউনলোডে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }.start()
    }

    private fun playAudioFile(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.setDataSource(file.absolutePath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            notifyListView()
            val playAudio: ImageView = findViewById(R.id.playAudio)
            playAudio.setImageResource(R.drawable.pause)
            mediaPlayer?.setOnCompletionListener {
                startPlayingFromIndex(currentIndex + 1) // শেষ হলে পরেরটা চালু হবে
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "অডিও প্লে করতে সমস্যা হয়েছে।", Toast.LENGTH_SHORT).show()
        }
    }


    private fun scrollToAya(id: String) {
        for (i in filteredItems.indices) {
            val itemId = filteredItems[i].getString("_id")
            if (itemId == id) {
                // প্রথমে সেট সিলেকশন, তারপর সামান্য স্ক্রল এডজাস্ট
                listView1.setSelection(i)
                listView1.postDelayed({
                    listView1.smoothScrollToPositionFromTop(i, 0)
                }, 50) // একটু ডিলে দিয়ে smooth scroll
                break
            }
        }
    }





}
