package com.refresh.pos.ui.sale

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.databinding.DialogWeightInputBinding
import com.refresh.pos.domain.engine.CurrencyUtils

class WeightInputDialogFragment : DialogFragment() {

    private var _binding: DialogWeightInputBinding? = null
    private val binding get() = _binding!!

    private var product: ProductEntity? = null
    private var onWeightConfirmed: ((ProductEntity, Double) -> Unit)? = null

    companion object {
        fun newInstance(
            product: ProductEntity,
            onConfirmed: (ProductEntity, Double) -> Unit
        ): WeightInputDialogFragment {
            val fragment = WeightInputDialogFragment()
            fragment.product = product
            fragment.onWeightConfirmed = onConfirmed
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWeightInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = product ?: return
        binding.tvWeightProductName.text = item.name
        binding.tvWeightUnitPrice.text = "@ ${CurrencyUtils.formatKsh(item.unitPrice)} / ${item.unitOfMeasure}"

        fun updatePreview(weight: Double) {
            val subtotal = weight * item.unitPrice
            binding.tvWeightSubtotal.text = CurrencyUtils.formatKsh(subtotal)
        }

        updatePreview(1.0)

        binding.etWeightQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val weight = s?.toString()?.toDoubleOrNull() ?: 0.0
                updatePreview(weight)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnWeightQuarter.setOnClickListener { binding.etWeightQuantity.setText("0.25") }
        binding.btnWeightHalf.setOnClickListener { binding.etWeightQuantity.setText("0.50") }
        binding.btnWeight1Kg.setOnClickListener { binding.etWeightQuantity.setText("1.00") }
        binding.btnWeightOneHalf.setOnClickListener { binding.etWeightQuantity.setText("1.50") }
        binding.btnWeight2Kg.setOnClickListener { binding.etWeightQuantity.setText("2.00") }

        binding.btnCancelWeight.setOnClickListener { dismiss() }

        binding.btnAddWeightToCart.setOnClickListener {
            val weight = binding.etWeightQuantity.text.toString().toDoubleOrNull() ?: 0.0
            if (weight > 0.0) {
                onWeightConfirmed?.invoke(item, weight)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
