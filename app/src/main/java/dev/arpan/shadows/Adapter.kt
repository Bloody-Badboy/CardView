package dev.arpan.shadows

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.arpan.shadows.databinding.ItemCardBinding

class Adapter(private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<Adapter.IntViewHolder>() {

    var data: List<Int> = (1..500).toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntViewHolder {
        return IntViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: IntViewHolder, position: Int) {
        holder.bind(data[position], onItemClick)
    }

    override fun getItemCount(): Int = data.size

    class IntViewHolder private constructor(val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(parent: ViewGroup): IntViewHolder {
                return IntViewHolder(
                    binding = ItemCardBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        fun bind(item: Int, onItemClick: (Int) -> Unit) {
            itemView.setOnClickListener {
                onItemClick(item)
            }
            binding.count.text = item.toString()
        }
    }
}