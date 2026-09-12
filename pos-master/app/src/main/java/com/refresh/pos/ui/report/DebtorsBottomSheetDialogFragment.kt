package com.refresh.pos.ui.report

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.refresh.pos.data.local.entity.CustomerEntity
import com.refresh.pos.databinding.BottomSheetDebtorsBinding
import com.refresh.pos.domain.engine.CurrencyUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.URLEncoder

class DebtorsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDebtorsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by activityViewModels()
    private lateinit var debtorAdapter: DebtorAdapter

    companion object {
        fun newInstance(): DebtorsBottomSheetDialogFragment {
            return DebtorsBottomSheetDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDebtorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        binding.btnCloseDebtors.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        debtorAdapter = DebtorAdapter(
            onWhatsAppClick = { customer -> sendWhatsAppReminder(customer) },
            onPayDebtClick = { customer -> showPayDebtDialog(customer) }
        )

        binding.rvDebtorsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = debtorAdapter
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalDebt.collectLatest { debt ->
                binding.tvTotalDebtSummary.text = "Total Outstanding: ${CurrencyUtils.format(debt)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.debtors.collectLatest { debtorsList ->
                debtorAdapter.submitList(debtorsList)
                binding.tvEmptyDebtors.visibility = if (debtorsList.isEmpty()) View.VISIBLE else View.GONE
                binding.rvDebtorsList.visibility = if (debtorsList.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun sendWhatsAppReminder(customer: CustomerEntity) {
        if (customer.phone.isBlank()) {
            Toast.makeText(requireContext(), "No phone number registered for ${customer.name}", Toast.LENGTH_SHORT).show()
            return
        }

        val rawPhone = customer.phone.replace("[^0-9+]".toRegex(), "")
        val formattedPhone = when {
            rawPhone.startsWith("+") -> rawPhone.substring(1)
            rawPhone.startsWith("0") -> "254" + rawPhone.substring(1)
            rawPhone.startsWith("254") -> rawPhone
            else -> "254$rawPhone"
        }

        val message = "Habari ${customer.name}, hili ni kumbusho kutoka duka la Amani POS. Salio lako la deni ni ${CurrencyUtils.format(customer.currentDebt)}. Tafadhali lipa kupitia M-Pesa. Asante sana!"
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }

        val url = "https://wa.me/$formattedPhone?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPayDebtDialog(customer: CustomerEntity) {
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(String.format(java.util.Locale.US, "%.2f", customer.currentDebt))
            setSelection(text.length)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Record Debt Payment")
            .setMessage("Customer: ${customer.name}\nCurrent Debt: ${CurrencyUtils.format(customer.currentDebt)}\nEnter payment amount:")
            .setView(input)
            .setPositiveButton("Receive Payment") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                if (amount <= 0.0) {
                    Toast.makeText(requireContext(), "Invalid payment amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.recordCustomerDebtPayment(customer.id, amount)
                Toast.makeText(
                    requireContext(),
                    "Payment of ${CurrencyUtils.format(amount)} recorded for ${customer.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
