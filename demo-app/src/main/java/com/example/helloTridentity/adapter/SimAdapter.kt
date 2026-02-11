package com.example.helloTridentity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.helloTridentity.data.SIMCardApp
import com.example.helloTridentity.databinding.SimLayoutMainBinding

class SimAdapter(
    private val simCard: ArrayList<SIMCardApp>,
    private val clickListener: ClickListener
) : RecyclerView.Adapter<SimAdapter.ViewHolder>() {

    private var mSelectedItem = -1

    inner class ViewHolder(val binding: SimLayoutMainBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(position: Int, selectedPosition: Int) {
            when {
                selectedPosition == -1 -> binding.radioButton.isChecked = false
                selectedPosition == position -> binding.radioButton.isChecked = true
                else -> binding.radioButton.isChecked = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SimLayoutMainBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(simCard[position]) {
                binding.textViewOperator.text = this.label
            }
            binding.radioButton.setOnClickListener {
                mSelectedItem = adapterPosition
                notifyDataSetChanged()
                clickListener.mOnitemClickListener(position, simCard[position].subscriptionId)
            }
            bindItems(position, mSelectedItem)
        }
    }

    override fun getItemCount(): Int = simCard.size

    interface ClickListener {
        fun mOnitemClickListener(position: Int, subsId: Int)
    }
}
