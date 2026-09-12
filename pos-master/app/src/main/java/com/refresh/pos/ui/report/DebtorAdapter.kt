package com.refresh.pos.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refresh.pos.data.local.entity.CustomerEntity
import com.refresh.pos.databinding.ItemDebtorBinding
import com.refresh.pos.domain.engine.CurrencyUtils

class DebtorAdapter(
    private val onWhatsAppClick: (CustomerEntity) -> Unit,
    private val onPayDebtClick: (CustomerEntity) -> Unit
) : ListAdapter<CustomerEntity, DebtorAdapter.DebtorViewHolder>(DebtorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtorViewHolder {
        val binding = ItemDebtorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DebtorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DebtorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DebtorViewHolder(private val binding: ItemDebtorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: CustomerEntity) {
            binding.tvDebtorName.text = customer.name
            binding.tvDebtorPhone.text = if (customer.phone.isNotBlank()) customer.phone else "No phone"
            binding.tvDebtorAmount.text = CurrencyUtils.format(customer.currentDebt)

            binding.btnWhatsAppReminder.setOnClickListener {
                onWhatsAppClick(customer)
            }

            binding.btnPayDebt.setOnClickListener {
                onPayDebtClick(customer)
            }
        }
    }

    private class DebtorDiffCallback : DiffUtil.ItemCallback<CustomerEntity>() {
        override fun areItemsTheSame(oldItem: CustomerEntity, newItem: CustomerEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CustomerEntity, newItem: CustomerEntity): Boolean =
            oldItem == newItem
    }
}
