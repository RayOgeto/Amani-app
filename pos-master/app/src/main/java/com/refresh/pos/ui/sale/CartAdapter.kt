package com.refresh.pos.ui.sale

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refresh.pos.databinding.ItemCartBinding
import com.refresh.pos.domain.engine.CurrencyUtils
import com.refresh.pos.domain.model.CartItem

class CartAdapter(
    private val onPlusClicked: (CartItem) -> Unit,
    private val onMinusClicked: (CartItem) -> Unit,
    private val onRemoveClicked: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvProductName.text = item.product.name
            binding.tvUnitPrice.text = "@ ${CurrencyUtils.formatKsh(item.product.unitPrice)} / ${item.product.unitOfMeasure}"
            binding.tvQuantity.text = CurrencyUtils.formatQuantity(item.quantity, item.product.unitOfMeasure)
            binding.tvItemTotal.text = CurrencyUtils.formatKsh(item.subtotal)

            binding.btnPlus.setOnClickListener { onPlusClicked(item) }
            binding.btnMinus.setOnClickListener { onMinusClicked(item) }
            binding.btnRemove.setOnClickListener { onRemoveClicked(item) }
        }
    }

    object CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
