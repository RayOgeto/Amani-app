package com.refresh.pos.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refresh.pos.data.local.relation.SaleWithLineItems
import com.refresh.pos.databinding.ItemSaleHistoryBinding
import com.refresh.pos.domain.engine.CurrencyUtils
import java.util.Locale

class SaleHistoryAdapter(
    private val onSaleClicked: (SaleWithLineItems) -> Unit
) : ListAdapter<SaleWithLineItems, SaleHistoryAdapter.SaleViewHolder>(SaleDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SaleViewHolder(private val binding: ItemSaleHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SaleWithLineItems) {
            val sale = item.sale
            binding.tvSaleReceiptId.text = String.format(Locale.US, "Receipt #%06d", sale.id)
            binding.tvSaleTimestamp.text = sale.endTime
            binding.tvSaleAmount.text = CurrencyUtils.formatKsh(sale.total)
            val customerText = if (sale.customerName.isNotBlank()) " • ${sale.customerName}" else ""
            binding.tvSaleItemsSummary.text = "${item.lineItems.size} items$customerText"
            binding.tvPaymentBadge.text = sale.paymentMethod

            binding.root.setOnClickListener { onSaleClicked(item) }
        }
    }

    object SaleDiffCallback : DiffUtil.ItemCallback<SaleWithLineItems>() {
        override fun areItemsTheSame(oldItem: SaleWithLineItems, newItem: SaleWithLineItems): Boolean {
            return oldItem.sale.id == newItem.sale.id
        }

        override fun areContentsTheSame(oldItem: SaleWithLineItems, newItem: SaleWithLineItems): Boolean {
            return oldItem == newItem
        }
    }
}
