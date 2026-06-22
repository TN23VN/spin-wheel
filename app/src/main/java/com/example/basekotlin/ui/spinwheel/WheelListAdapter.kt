package com.example.spinwheel.ui.spinwheel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spinwheel.databinding.ItemWheelBinding
import com.example.spinwheel.model.WheelModel

class WheelListAdapter(
    private val onOpen: (WheelModel) -> Unit,
    private val onEdit: (WheelModel) -> Unit,
    private val onDelete: (WheelModel) -> Unit,
    private val onDuplicate: (WheelModel) -> Unit,
) : RecyclerView.Adapter<WheelListAdapter.WheelViewHolder>() {

    private val data = mutableListOf<WheelModel>()

    fun submit(items: List<WheelModel>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WheelViewHolder {
        return WheelViewHolder(
            ItemWheelBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: WheelViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class WheelViewHolder(private val binding: ItemWheelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WheelModel) {
            binding.tvName.text = item.name
            binding.root.setOnClickListener { onOpen(item) }
            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
            binding.btnDuplicate.setOnClickListener { onDuplicate(item) }
        }
    }
}
