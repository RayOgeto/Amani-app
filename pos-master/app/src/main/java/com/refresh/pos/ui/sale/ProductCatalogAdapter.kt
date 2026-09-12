package com.refresh.pos.ui.sale

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.databinding.ItemCatalogProductBinding
import com.refresh.pos.domain.engine.CurrencyUtils

class ProductCatalogAdapter(
    private val onProductSelected: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductCatalogAdapter.ProductViewHolder>(ProductDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemCatalogProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemCatalogProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) {
            binding.tvCatalogProductName.text = product.name
            val codeLabel = if (product.pluCode.isNotBlank()) "PLU: ${product.pluCode}" else product.barcode
            binding.tvCatalogBarcode.text = codeLabel
            binding.tvCatalogStock.text = "Stock: ${CurrencyUtils.formatQuantity(product.stockQuantity, product.unitOfMeasure)}"
            binding.tvCatalogPrice.text = "${CurrencyUtils.formatKsh(product.unitPrice)} / ${product.unitOfMeasure}"

            val buttonText = if (product.isWeighed) "Scale" else "+ Add"
            binding.btnQuickAdd.text = buttonText

            binding.root.setOnClickListener { onProductSelected(product) }
            binding.btnQuickAdd.setOnClickListener { onProductSelected(product) }
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
