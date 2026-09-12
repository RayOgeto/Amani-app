package com.refresh.pos.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.databinding.ItemInventoryProductBinding
import com.refresh.pos.domain.engine.CurrencyUtils

class InventoryAdapter(
    private val onEditClicked: (ProductEntity) -> Unit,
    private val onAddStockClicked: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, InventoryAdapter.InventoryViewHolder>(ProductDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventoryViewHolder(private val binding: ItemInventoryProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) {
            binding.tvInvName.text = product.name
            binding.tvInvPrice.text = "${CurrencyUtils.formatKsh(product.unitPrice)} / ${product.unitOfMeasure}"
            val code = if (product.pluCode.isNotBlank()) "PLU: ${product.pluCode}" else "Barcode: ${product.barcode}"
            binding.tvInvBarcode.text = code
            binding.tvInvStock.text = "In Stock: ${CurrencyUtils.formatQuantity(product.stockQuantity, product.unitOfMeasure)}"

            binding.btnEdit.setOnClickListener { onEditClicked(product) }
            binding.btnAddLot.setOnClickListener { onAddStockClicked(product) }
        }
    }

    object ProductDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }
}
