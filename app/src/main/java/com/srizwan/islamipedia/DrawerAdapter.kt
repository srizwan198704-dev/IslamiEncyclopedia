package com.srizwan.islamipedia

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DrawerAdapter(
    private val context: Context,
    private val list: ArrayList<HashMap<String, Any>>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lmain: LinearLayout = view.findViewById(R.id.linear1)
        val main: LinearLayout = view.findViewById(R.id.main)
        val name: TextView = view.findViewById(R.id.name)
        val number: TextView = view.findViewById(R.id.number)
        val box: LinearLayout = view.findViewById(R.id.linear5)

        init {
            lmain.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION && lmain.isEnabled) {
                    onClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.drawer_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val title = item["1"]?.toString() ?: "পৃষ্ঠা ${position + 1}"
        val subtitle = item["2"]?.toString() ?: ""

        holder.name.text = replaceArabicNumber(title)
        holder.number.text = replaceArabicNumber((position + 1).toString())

        val d = context.resources.displayMetrics.density.toInt()

        if (subtitle.isEmpty()) {
            // ফাঁকা subtitle - আলাদা ডিজাইন
            holder.box.visibility = View.GONE
            holder.name.gravity = Gravity.CENTER
            holder.lmain.gravity = Gravity.CENTER
            holder.name.setTextColor(Color.WHITE)
            holder.lmain.isEnabled = false
            holder.name.isEnabled = false
            holder.main.isEnabled = false

            val bg = GradientDrawable().apply {
                setStroke(d, Color.WHITE)
                setColor(Color.parseColor("#01837A"))
                cornerRadius = d * 12f
            }

            holder.lmain.elevation = d * 6f
            holder.lmain.background = RippleDrawable(
                ColorStateList.valueOf(Color.parseColor("#01837A")),
                bg,
                null
            )
        } else {
            // স্বাভাবিক ডিজাইন
            holder.box.visibility = View.VISIBLE
            holder.name.gravity = Gravity.CENTER_VERTICAL
            holder.lmain.gravity = Gravity.CENTER_VERTICAL
            holder.name.setTextColor(Color.BLACK)
            holder.lmain.isEnabled = true
            holder.name.isEnabled = true
            holder.main.isEnabled = true

            val bg = GradientDrawable().apply {
                setStroke(d, Color.parseColor("#01837A"))
                setColor(Color.WHITE)
                cornerRadius = d * 12f
            }

            holder.lmain.elevation = d * 6f
            holder.lmain.background = RippleDrawable(
                ColorStateList.valueOf(Color.parseColor("#01837A")),
                bg,
                null
            )
        }
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
    }
}
