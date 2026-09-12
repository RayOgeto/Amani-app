package com.refresh.pos.ui.sale

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.refresh.pos.R
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.databinding.FragmentSaleBinding
import com.refresh.pos.domain.engine.CurrencyUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SaleFragment : Fragment() {

    private var _binding: FragmentSaleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SaleViewModel by activityViewModels()

    private lateinit var cartAdapter: CartAdapter
    private lateinit var catalogAdapter: ProductCatalogAdapter

    // Modern Barcode scanner launcher
    private val barcodeScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        val code = result.contents
        if (!code.isNullOrBlank()) {
            lifecycleScope.launch {
                val product = viewModel.onBarcodeOrPluScanned(code)
                if (product != null) {
                    if (product.isWeighed) {
                        promptWeightInput(product)
                    } else {
                        Toast.makeText(requireContext(), "Added ${product.name} (KSh ${product.unitPrice})", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No grocery item found for code: $code", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSearchAndScanner()
        setupTabSwitching()
        setupCategoryChips()
        setupCheckoutActions()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        cartAdapter = CartAdapter(
            onPlusClicked = { item -> viewModel.increaseQuantity(item.product.id) },
            onMinusClicked = { item -> viewModel.decreaseQuantity(item.product.id) },
            onRemoveClicked = { item -> viewModel.removeFromCart(item.product.id) }
        )
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        catalogAdapter = ProductCatalogAdapter(
            onProductSelected = { product ->
                if (product.isWeighed) {
                    promptWeightInput(product)
                } else {
                    viewModel.addToCart(product, 1.0)
                    Toast.makeText(requireContext(), "Added ${product.name}", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.rvCatalogProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = catalogAdapter
        }
    }

    private fun promptWeightInput(product: ProductEntity) {
        WeightInputDialogFragment.newInstance(product) { targetProduct, weight ->
            viewModel.addToCart(targetProduct, weight)
            val formatted = CurrencyUtils.formatQuantity(weight, targetProduct.unitOfMeasure)
            Toast.makeText(requireContext(), "Added $formatted of ${targetProduct.name}", Toast.LENGTH_SHORT).show()
        }.show(parentFragmentManager, "WeightInputDialog")
    }

    private fun setupSearchAndScanner() {
        binding.etSearchOrBarcode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.searchQuery.value = query
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                if (query.isNotEmpty()) {
                    showCatalogTab()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearchOrBarcode.text.clear()
            viewModel.searchQuery.value = ""
        }

        binding.btnScanBarcode.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan barcode or PLU code")
                setBeepEnabled(true)
                setOrientationLocked(false)
                setBarcodeImageEnabled(false)
            }
            barcodeScannerLauncher.launch(options)
        }
    }

    private fun setupCategoryChips() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest { categories ->
                binding.chipGroupCategories.removeAllViews()
                for (cat in categories) {
                    val chip = Chip(requireContext()).apply {
                        text = cat
                        isCheckable = true
                        isChecked = cat == viewModel.selectedCategory.value
                        setOnClickListener {
                            viewModel.selectedCategory.value = cat
                        }
                    }
                    binding.chipGroupCategories.addView(chip)
                }
            }
        }
    }

    private fun setupTabSwitching() {
        binding.tabCart.setOnClickListener { showCartTab() }
        binding.tabCatalog.setOnClickListener { showCatalogTab() }
    }

    private fun showCartTab() {
        binding.containerCart.visibility = View.VISIBLE
        binding.containerCatalog.visibility = View.GONE
        binding.tabCart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
        binding.tabCart.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary))
        binding.tabCatalog.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        binding.tabCatalog.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
    }

    private fun showCatalogTab() {
        binding.containerCart.visibility = View.GONE
        binding.containerCatalog.visibility = View.VISIBLE
        binding.tabCatalog.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
        binding.tabCatalog.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary))
        binding.tabCart.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        binding.tabCart.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
    }

    private fun setupCheckoutActions() {
        binding.btnClearCart.setOnClickListener {
            if (viewModel.cartItems.value.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is already empty", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.clearCart()
                Toast.makeText(requireContext(), "Cart cleared", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCheckout.setOnClickListener {
            if (viewModel.cartItems.value.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty. Add items first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CheckoutBottomSheetDialogFragment().show(parentFragmentManager, "CheckoutSheet")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cartItems.collectLatest { items ->
                cartAdapter.submitList(items)
                binding.layoutEmptyCart.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.rvCartItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                val count = items.size
                binding.tabCart.text = if (count > 0) "Active Cart ($count)" else "Active Cart"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.availableProducts.collectLatest { products ->
                catalogAdapter.submitList(products)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.subtotal.collectLatest { total ->
                binding.tvTotalDue.text = CurrencyUtils.formatKsh(total)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.taxBreakdown.collectLatest { tax ->
                binding.tvVatAmount.text = "VAT: ${CurrencyUtils.formatKsh(tax.taxAmount)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemCount.collectLatest { count ->
                binding.tvCartItemCount.text = "$count Items (VAT 16% incl.)"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
