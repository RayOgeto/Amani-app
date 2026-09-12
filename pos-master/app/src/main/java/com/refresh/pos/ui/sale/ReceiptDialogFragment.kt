package com.refresh.pos.ui.sale

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.refresh.pos.PosApplication
import com.refresh.pos.databinding.DialogReceiptBinding
import com.refresh.pos.domain.engine.ReceiptGenerator
import com.refresh.pos.domain.model.Receipt
import kotlinx.coroutines.launch

class ReceiptDialogFragment : DialogFragment() {

    private var _binding: DialogReceiptBinding? = null
    private val binding get() = _binding!!

    private var receipt: Receipt? = null

    companion object {
        fun newInstance(receipt: Receipt): ReceiptDialogFragment {
            val fragment = ReceiptDialogFragment()
            fragment.receipt = receipt
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
        _binding = DialogReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentReceipt = receipt ?: return
        val formattedText = ReceiptGenerator.formatPlainText(currentReceipt)
        binding.tvReceiptBody.text = formattedText

        val printerService = PosApplication.instance.printerService
        val pairedPrinters = printerService.getPairedPrinters()
        if (pairedPrinters.isNotEmpty()) {
            binding.tvPrinterStatus.text = "Connected Printer: ${pairedPrinters.first().name}"
        } else {
            binding.tvPrinterStatus.text = "No Bluetooth POS printer paired."
        }

        binding.btnShareReceipt.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Receipt #${currentReceipt.receiptNumber}")
                putExtra(Intent.EXTRA_TEXT, formattedText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Receipt via"))
        }

        binding.btnPrintReceipt.setOnClickListener {
            val printers = printerService.getPairedPrinters()
            if (printers.isEmpty()) {
                Toast.makeText(requireContext(), "Please pair a Bluetooth thermal printer in device settings.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                binding.btnPrintReceipt.isEnabled = false
                val bytes = ReceiptGenerator.generateEscPosBytes(currentReceipt)
                val result = printerService.printBytes(printers.first(), bytes)
                binding.btnPrintReceipt.isEnabled = true

                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Receipt sent to printer!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Print failed: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDoneReceipt.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
