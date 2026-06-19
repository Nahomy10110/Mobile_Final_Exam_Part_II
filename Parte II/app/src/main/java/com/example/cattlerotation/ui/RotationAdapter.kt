package com.example.cattlerotation.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cattlerotation.R
import com.example.cattlerotation.data.Paddock
import com.example.cattlerotation.data.Rotation
import com.example.cattlerotation.domain.PaddockState
import com.example.cattlerotation.domain.RotationCalculator
import com.example.cattlerotation.util.toDateString

data class RotationRow(
    val paddock: Paddock,
    val state: PaddockState,
    val active: Rotation?
)

class RotationAdapter : RecyclerView.Adapter<RotationAdapter.VH>() {

    private var rows: List<RotationRow> = emptyList()
    private val selected = mutableSetOf<Long>()
    private val expanded = mutableSetOf<Long>()

    fun submit(newRows: List<RotationRow>) { rows = newRows; notifyDataSetChanged() }
    fun selectedIds(): List<Long> = selected.toList()
    fun clearSelection() { selected.clear(); notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotation, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = rows[position]
        val ctx = holder.itemView.context

        holder.name.text = row.paddock.name

        val (colorRes, label) = when (row.state) {
            PaddockState.GREEN  -> R.color.state_green  to ctx.getString(R.string.state_green)
            PaddockState.RED    -> R.color.state_red    to ctx.getString(R.string.state_red)
            PaddockState.ORANGE -> R.color.state_orange to ctx.getString(R.string.state_orange)
        }
        val color = ContextCompat.getColor(ctx, colorRes)
        holder.stateBar.setBackgroundColor(color)
        holder.stateText.text = label
        holder.stateText.setTextColor(color)

        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = selected.contains(row.paddock.id)
        holder.check.isEnabled = row.state == PaddockState.GREEN
        holder.check.alpha = if (row.state == PaddockState.GREEN) 1f else 0.3f
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selected.add(row.paddock.id)
            else selected.remove(row.paddock.id)
        }

        val isExpanded = expanded.contains(row.paddock.id)
        holder.layoutDetail.visibility = if (isExpanded) View.VISIBLE else View.GONE
        if (isExpanded) bindDetail(ctx, holder, row)

        holder.itemView.setOnLongClickListener {
            if (isExpanded) expanded.remove(row.paddock.id)
            else expanded.add(row.paddock.id)
            notifyItemChanged(position)
            true
        }
    }

    private fun bindDetail(ctx: Context, holder: VH, row: RotationRow) {
        val a = row.active
        if (a == null) {
            holder.textDetailRed.visibility = View.GONE
            holder.textDetailOrange.visibility = View.GONE
            holder.textDetailNone.visibility = View.VISIBLE
        } else {
            holder.textDetailNone.visibility = View.GONE
            holder.textDetailRed.visibility = View.VISIBLE
            holder.textDetailOrange.visibility = View.VISIBLE
            holder.textDetailRed.text = ctx.getString(
                R.string.rotation_dates_red,
                a.startDate.toDateString(),
                RotationCalculator.redEnd(a).toDateString()
            )
            holder.textDetailOrange.text = ctx.getString(
                R.string.rotation_dates_orange,
                RotationCalculator.redEnd(a).toDateString(),
                RotationCalculator.orangeEnd(a).toDateString()
            )
        }
    }

    override fun getItemCount() = rows.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val stateBar: View = v.findViewById(R.id.stateBar)
        val name: TextView = v.findViewById(R.id.textName)
        val stateText: TextView = v.findViewById(R.id.textState)
        val check: CheckBox = v.findViewById(R.id.checkSelect)
        val layoutDetail: View = v.findViewById(R.id.layoutDetail)
        val textDetailRed: TextView = v.findViewById(R.id.textDetailRed)
        val textDetailOrange: TextView = v.findViewById(R.id.textDetailOrange)
        val textDetailNone: TextView = v.findViewById(R.id.textDetailNone)
    }
}