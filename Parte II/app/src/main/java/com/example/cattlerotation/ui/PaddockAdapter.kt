package com.example.cattlerotation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cattlerotation.R
import com.example.cattlerotation.data.Paddock
import com.example.cattlerotation.util.toDateString

class PaddockAdapter(
    private val onClick: (Paddock) -> Unit,
    private val onLongClick: (Paddock) -> Unit
) : RecyclerView.Adapter<PaddockAdapter.VH>() {

    private var items: List<Paddock> = emptyList()

    fun submit(list: List<Paddock>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paddock, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.name.text = p.name
        holder.area.text = " ${"%,.0f".format(p.areaM2)} m²"
        holder.date.text = " Creado: ${p.creationDate.toDateString()}"
        holder.itemView.setOnClickListener { onClick(p) }
        holder.itemView.setOnLongClickListener { onLongClick(p); true }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.textName)
        val area: TextView = v.findViewById(R.id.textArea)
        val date: TextView = v.findViewById(R.id.textDate)
    }
}