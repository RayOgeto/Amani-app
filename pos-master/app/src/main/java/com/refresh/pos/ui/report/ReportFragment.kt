package com.refresh.pos.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.refresh.pos.R
import com.refresh.pos.databinding.FragmentReportBinding
import com.refresh.pos.domain.model.PaymentMethod
import com.refresh.pos.domain.model.PeriodFilter
import com.refresh.pos.domain.model.Receipt
import com.refresh.pos.domain.model.ReceiptItem
import com.refresh.pos.ui.sale.ReceiptDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by activityViewModels()
    private lateinit var adapter: SaleHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupPeriodFilter()
        setupZReport()
        setupDebtorsSheet()
        observeViewModel()
    }

    private fun setupDebtorsSheet() {
        val openDebtors = View.OnClickListener {
            DebtorsBottomSheetDialogFragment.newInstance()
                .show(parentFragmentManager, "DebtorsDialog")
        }
        binding.cardCustomerDebt.setOnClickListener(openDebtors)
        binding.btnViewDebtors.setOnClickListener(openDebtors)
    }

    private fun setupRecyclerView() {
        adapter = SaleHistoryAdapter { saleWithItems ->
            val sale = saleWithItems.sale
            val receipt = Receipt(
                receiptNumber = String.format(Locale.US, "REC-%06d", sale.id),
                cashierName = sale.cashierName,
                dateString = sale.endTime,
                items = saleWithItems.lineItems.map {
                    ReceiptItem(
                        name = it.productName,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        total = it.totalPrice,
                        unitOfMeasure = it.unitOfMeasure
                    )
                },
                subtotal = sale.subtotal,
                taxAmount = sale.tax,
                total = sale.total,
                paymentMethod = PaymentMethod.fromString(sale.paymentMethod),
                amountTendered = sale.amountTendered,
                changeAmount = sale.changeAmount,
                paymentReference = sale.paymentReference,
                customerName = sale.customerName
            )
            ReceiptDialogFragment.newInstance(receipt).show(parentFragmentManager, "ReceiptDialog")
        }

        binding.rvReportSales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ReportFragment.adapter
        }
    }

    private fun setupPeriodFilter() {
        binding.chipGroupPeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chipDaily) -> viewModel.setPeriod(PeriodFilter.DAILY)
                checkedIds.contains(R.id.chipWeekly) -> viewModel.setPeriod(PeriodFilter.WEEKLY)
                checkedIds.contains(R.id.chipMonthly) -> viewModel.setPeriod(PeriodFilter.MONTHLY)
                checkedIds.contains(R.id.chipYearly) -> viewModel.setPeriod(PeriodFilter.YEARLY)
            }
        }
    }

    private fun setupZReport() {
        binding.btnPrintZReport.setOnClickListener {
            val total = viewModel.totalRevenue.value
            val orders = viewModel.totalOrders.value
            val cash = viewModel.cashRevenue.value
            val digital = viewModel.digitalRevenue.value
            val period = viewModel.selectedPeriod.value.title

            val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
            val zReportReceipt = Receipt(
                receiptNumber = "Z-REPORT-${System.currentTimeMillis() % 100000}",
                storeName = "AMANI POS - Z-REPORT",
                cashierName = "Manager",
                dateString = "$now ($period)",
                items = listOf(
                    ReceiptItem(name = "Cash Tendered", quantity = orders.toDouble(), unitPrice = cash, total = cash),
                    ReceiptItem(name = "Digital / M-Pesa", quantity = orders.toDouble(), unitPrice = digital, total = digital)
                ),
                subtotal = total,
                taxAmount = 0.0,
                total = total,
                paymentMethod = PaymentMethod.SPLIT,
                amountTendered = total,
                changeAmount = 0.0
            )

            ReceiptDialogFragment.newInstance(zReportReceipt).show(parentFragmentManager, "ZReportDialog")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sales.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalRevenue.collectLatest { rev ->
                binding.tvReportRevenue.text = com.refresh.pos.domain.engine.CurrencyUtils.format(rev)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalOrders.collectLatest { count ->
                binding.tvReportOrders.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cashRevenue.collectLatest { cash ->
                binding.tvCashRevenue.text = "Cash: ${com.refresh.pos.domain.engine.CurrencyUtils.format(cash)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.digitalRevenue.collectLatest { digital ->
                binding.tvDigitalRevenue.text = "Digital: ${com.refresh.pos.domain.engine.CurrencyUtils.format(digital)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalDebt.collectLatest { debt ->
                binding.tvReportTotalDebt.text = com.refresh.pos.domain.engine.CurrencyUtils.format(debt)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.expiringLots.collectLatest { lots ->
                if (lots.isNotEmpty()) {
                    binding.bannerExpiringLots.visibility = View.VISIBLE
                    binding.tvExpiringLotsAlert.text = "⚠️ ${lots.size} batch(es) expiring soon within 7 days"
                } else {
                    binding.bannerExpiringLots.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
