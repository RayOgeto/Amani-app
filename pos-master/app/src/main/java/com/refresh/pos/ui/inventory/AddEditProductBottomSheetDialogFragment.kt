package com.refresh.pos.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.databinding.BottomSheetAddProductBinding
import java.util.Locale

class AddEditProductBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by activityViewModels()
    private var editingProduct: ProductEntity? = null

    private val barcodeScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        val barcode = result.contents
        if (!barcode.isNullOrBlank()) {
            binding.etProductBarcode.setText(barcode)
        }
    }

    companion object {
        fun newInstance(product: ProductEntity? = null): AddEditProductBottomSheetDialogFragment {
            val fragment = AddEditProductBottomSheetDialogFragment()
            fragment.editingProduct = product
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingProduct?.let { product ->
            binding.tvProductDialogTitle.text = "Edit Grocery Item"
            binding.etProductName.setText(product.name)
            binding.etProductBarcode.setText(product.barcode)
            binding.etProductPrice.setText(String.format(Locale.US, "%.2f", product.unitPrice))
            binding.etProductCost.setText(String.format(Locale.US, "%.2f", product.costPrice))
            binding.etProductCategory.setText(product.category)
            binding.etProductUom.setText(product.unitOfMeasure)
            binding.etProductPlu.setText(product.pluCode)
            binding.cbIsWeighed.isChecked = product.isWeighed
            binding.etProductStock.setText(
                if (product.stockQuantity % 1.0 == 0.0) product.stockQuantity.toInt().toString()
                else product.stockQuantity.toString()
            )
        }

        binding.btnScanProductBarcode.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan barcode for this product")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            barcodeScannerLauncher.launch(options)
        }

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val barcode = binding.etProductBarcode.text.toString().trim()
            val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
            val cost = binding.etProductCost.text.toString().toDoubleOrNull() ?: 0.0
            val category = binding.etProductCategory.text.toString().trim().ifBlank { "General" }
            val uom = binding.etProductUom.text.toString().trim().ifBlank { "PCS" }
            val plu = binding.etProductPlu.text.toString().trim()
            val isWeighed = binding.cbIsWeighed.isChecked
            val stock = binding.etProductStock.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Product name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (price <= 0.0) {
                Toast.makeText(requireContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveProduct(
                name = name,
                barcode = barcode,
                price = price,
                cost = cost,
                category = category,
                unitOfMeasure = uom,
                isWeighed = isWeighed,
                pluCode = plu,
                initialStock = stock,
                existingId = editingProduct?.id ?: 0L
            )

            Toast.makeText(requireContext(), "Product saved successfully", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
