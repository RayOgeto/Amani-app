package com.refresh.pos.ui.inventory

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.databinding.DialogAddStockBinding
import java.util.Locale

class AddStockLotDialogFragment : DialogFragment() {

    private var _binding: DialogAddStockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by activityViewModels()
    private var product: ProductEntity? = null

    companion object {
        fun newInstance(product: ProductEntity): AddStockLotDialogFragment {
            val fragment = AddStockLotDialogFragment()
            fragment.product = product
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetProduct = product ?: return
        binding.tvStockLotProductName.text = "${targetProduct.name} (${targetProduct.unitOfMeasure})"
        binding.etStockLotCost.setText(String.format(Locale.US, "%.2f", targetProduct.costPrice))

        binding.btnCancelStockLot.setOnClickListener { dismiss() }

        binding.btnConfirmStockLot.setOnClickListener {
            val qty = binding.etStockLotQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val cost = binding.etStockLotCost.text.toString().toDoubleOrNull() ?: 0.0
            val expiry = binding.etStockLotExpiry.text.toString().trim()

            if (qty <= 0.0) {
                Toast.makeText(requireContext(), "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addStockLot(targetProduct.id, qty, cost, expiry)
            val qtyFormatted = if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
            Toast.makeText(requireContext(), "Added $qtyFormatted ${targetProduct.unitOfMeasure} to ${targetProduct.name}", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
