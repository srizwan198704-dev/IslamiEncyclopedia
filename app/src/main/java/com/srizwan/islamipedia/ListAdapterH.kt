package com.srizwan.islamipedia

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONException
import org.json.JSONObject
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.animation.ScaleAnimation

class ListAdapterH(context: Context, private val vg: Int, private val list: ArrayList<JSONObject>) :
    ArrayAdapter<JSONObject>(context, vg, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(vg, parent, false)

        val lmain = itemView.findViewById<LinearLayout>(R.id.linear1)
        val main = itemView.findViewById<LinearLayout>(R.id.main)
        val txtName = itemView.findViewById<TextView>(R.id.name)
        val number = itemView.findViewById<TextView>(R.id.number)
        val ayanumber = itemView.findViewById<TextView>(R.id.ayanumber)
        val ayaarabic = itemView.findViewById<TextView>(R.id.ayaarabic)
        ayaarabic.visibility = View.GONE
        try {
            txtName.text = replaceArabicNumber(list[position].getString("name"))
            val bookid1 = replaceArabicNumber(list[position].getString("bookid"))
            number.text = if (bookid1.startsWith("০") || bookid1.startsWith("0")) {
                bookid1.drop(1)
            } else {
                bookid1
            }
            val verses = replaceArabicNumber(list[position].getString("verses"))
            ayanumber.text = "হাদিস : $verses"
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        txtName.setTextColor(Color.BLACK)
        lmain.isEnabled = true
        txtName.isEnabled = true
        main.isEnabled = true

        val sketchUi = GradientDrawable().apply {
            val d = context.applicationContext.resources.displayMetrics.density.toInt()
            setStroke(d, 0xFF01837A.toInt())
            setColor(0xFFFFFFFF.toInt())
            cornerRadius = d * 12f
        }
        lmain.elevation = context.applicationContext.resources.displayMetrics.density * 6

        val sketchuii = RippleDrawable(
            ColorStateList.valueOf(0xFF01837A.toInt()),
            sketchUi,
            null
        )
        lmain.background = sketchuii

        val animation = ScaleAnimation(
            0f, 1f, 0f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0f,
            ScaleAnimation.RELATIVE_TO_SELF, 1f
        ).apply {
            fillAfter = true
            duration = 300
        }
        lmain.startAnimation(animation)

        return itemView
    }

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
            .replace("</b>"," ")
            .replace("(রহঃ)", "(رحمة الله)")
            .replace("(রাঃ)", "(رضي الله عنه)")
            .replace("(সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)", "(ﷺ)")
            .replace(" (সাল্লাল্লাহু 'আলাইহি ওয়া সাল্লাম)","(ﷺ)")
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

