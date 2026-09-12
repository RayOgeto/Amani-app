package com.refresh.pos.ui.sale

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.refresh.pos.R
import com.refresh.pos.data.local.entity.CustomerEntity
import com.refresh.pos.databinding.BottomSheetCheckoutBinding
import com.refresh.pos.domain.engine.CurrencyUtils
import com.refresh.pos.domain.model.PaymentMethod
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class CheckoutBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCheckoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SaleViewModel by activityViewModels()
    private var selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH
    private var selectedCustomer: CustomerEntity? = null
    private var customerList: List<CustomerEntity> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalDue = viewModel.subtotal.value
        binding.tvCheckoutTotal.text = CurrencyUtils.formatKsh(totalDue)

        setupPaymentMethodSelector()
        setupCashCalculator(totalDue)
        setupCustomerCreditSelector()

        binding.btnConfirmPayment.setOnClickListener {
            val amountTenderedStr = binding.etAmountTendered.text.toString().trim()
            val amountTendered = if (selectedPaymentMethod == PaymentMethod.CASH) {
                amountTenderedStr.toDoubleOrNull() ?: 0.0
            } else {
                totalDue
            }

            if (selectedPaymentMethod == PaymentMethod.CASH && amountTendered < totalDue) {
                Toast.makeText(requireContext(), "Amount tendered must be at least ${CurrencyUtils.formatKsh(totalDue)}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPaymentMethod == PaymentMethod.STORE_CREDIT && selectedCustomer == null) {
                Toast.makeText(requireContext(), "Please select a customer for store credit (Deni)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentRef = binding.etPaymentRef.text.toString().trim()

            lifecycleScope.launch {
                val result = viewModel.checkout(
                    paymentMethod = selectedPaymentMethod,
                    amountTendered = amountTendered,
                    paymentReference = paymentRef,
                    customerId = selectedCustomer?.id,
                    customerName = selectedCustomer?.name ?: ""
                )

                if (result.isSuccess) {
                    val receipt = result.getOrNull()
                    dismiss()
                    if (receipt != null) {
                        ReceiptDialogFragment.newInstance(receipt)
                            .show(parentFragmentManager, "ReceiptDialog")
                    }
                } else {
                    Toast.makeText(requireContext(), "Checkout failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupPaymentMethodSelector() {
        binding.chipGroupPaymentMethod.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chipCash) -> {
                    selectedPaymentMethod = PaymentMethod.CASH
                    binding.layoutCashSection.visibility = View.VISIBLE
                    binding.layoutDigitalSection.visibility = View.GONE
                    binding.layoutCreditSection.visibility = View.GONE
                }
                checkedIds.contains(R.id.chipMpesaTill) -> {
                    selectedPaymentMethod = PaymentMethod.MPESA_TILL
                    binding.layoutCashSection.visibility = View.GONE
                    binding.layoutDigitalSection.visibility = View.VISIBLE
                    binding.layoutCreditSection.visibility = View.GONE
                }
                checkedIds.contains(R.id.chipMpesaPaybill) -> {
                    selectedPaymentMethod = PaymentMethod.MPESA_PAYBILL
                    binding.layoutCashSection.visibility = View.GONE
                    binding.layoutDigitalSection.visibility = View.VISIBLE
                    binding.layoutCreditSection.visibility = View.GONE
                }
                checkedIds.contains(R.id.chipStoreCredit) -> {
                    selectedPaymentMethod = PaymentMethod.STORE_CREDIT
                    binding.layoutCashSection.visibility = View.GONE
                    binding.layoutDigitalSection.visibility = View.GONE
                    binding.layoutCreditSection.visibility = View.VISIBLE
                }
                checkedIds.contains(R.id.chipCard) -> {
                    selectedPaymentMethod = PaymentMethod.CARD
                    binding.layoutCashSection.visibility = View.GONE
                    binding.layoutDigitalSection.visibility = View.VISIBLE
                    binding.layoutCreditSection.visibility = View.GONE
                }
            }
        }
    }

    private fun setupCashCalculator(totalDue: Double) {
        fun updateChange(tendered: Double) {
            val change = if (tendered >= totalDue) tendered - totalDue else 0.0
            binding.tvChangeAmount.text = CurrencyUtils.formatKsh(change)
        }

        binding.etAmountTendered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val tendered = s?.toString()?.toDoubleOrNull() ?: 0.0
                updateChange(tendered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fun setTendered(amount: Double) {
            binding.etAmountTendered.setText(String.format(Locale.US, "%.0f", amount))
        }

        binding.btnQuickExact.setOnClickListener { setTendered(totalDue) }
        binding.btnQuick50.setOnClickListener { setTendered(Math.max(50.0, totalDue)) }
        binding.btnQuick100.setOnClickListener { setTendered(Math.max(100.0, totalDue)) }
        binding.btnQuick200.setOnClickListener { setTendered(Math.max(200.0, totalDue)) }
        binding.btnQuick500.setOnClickListener { setTendered(Math.max(500.0, totalDue)) }
        binding.btnQuick1000.setOnClickListener { setTendered(Math.max(1000.0, totalDue)) }
    }

    private fun setupCustomerCreditSelector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customers.collectLatest { customers ->
                customerList = customers
                val names = customers.map { "${it.name} (${it.phone})" }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                binding.actvCustomer.setAdapter(adapter)

                binding.actvCustomer.setOnItemClickListener { _, _, position, _ ->
                    val customer = customerList.getOrNull(position)
                    selectedCustomer = customer
                    if (customer != null) {
                        binding.tvCustomerDebtStatus.text = "Current Balance / Deni: ${CurrencyUtils.formatKsh(customer.currentDebt)}"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
