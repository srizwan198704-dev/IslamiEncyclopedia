package com.srizwan.islamipedia

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONException
import org.json.JSONObject
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.animation.ScaleAnimation

class RecyclerViewAdapterC(
    private val context: Context,
    private val list: ArrayList<JSONObject>
) : RecyclerView.Adapter<RecyclerViewAdapterC.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lmain: LinearLayout = itemView.findViewById(R.id.linear1)
        val main: LinearLayout = itemView.findViewById(R.id.main)
        val txtName: TextView = itemView.findViewById(R.id.name)
        val number: TextView = itemView.findViewById(R.id.number)
        val box: LinearLayout = itemView.findViewById(R.id.linear5)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_layout0, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.txtName.text = replaceArabicNumber(list[position].getString("1"))
            holder.number.text = replaceArabicNumber((position + 1).toString())

            holder.box.visibility = View.VISIBLE
            holder.lmain.gravity = Gravity.CENTER_VERTICAL
            holder.txtName.gravity = Gravity.CENTER_VERTICAL
            holder.txtName.setTextColor(Color.BLACK)
            
            val sketchUi = GradientDrawable().apply {
                val d = context.resources.displayMetrics.density.toInt()
                setStroke(d, 0xFF01837A.toInt())
                setColor(0xFFFFFFFF.toInt())
                cornerRadius = d * 12f
            }

            holder.lmain.elevation = context.resources.displayMetrics.density * 6
            holder.lmain.background = RippleDrawable(
                ColorStateList.valueOf(0xFF01837A.toInt()),
                sketchUi,
                null
            )

            val animation = ScaleAnimation(
                0f, 1f, 0f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0f,
                ScaleAnimation.RELATIVE_TO_SELF, 1f
            ).apply {
                fillAfter = true
                duration = 300
            }
            holder.lmain.startAnimation(animation)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = list.size

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
            .replace("bookhozur14", "bookhozur14")
            .replace("S2", "S2")
    }
}
