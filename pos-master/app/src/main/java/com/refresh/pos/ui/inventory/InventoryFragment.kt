package com.refresh.pos.ui.inventory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.refresh.pos.databinding.FragmentInventoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by activityViewModels()
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter(
            onEditClicked = { product ->
                AddEditProductBottomSheetDialogFragment.newInstance(product)
                    .show(parentFragmentManager, "EditProductSheet")
            },
            onAddStockClicked = { product ->
                AddStockLotDialogFragment.newInstance(product)
                    .show(parentFragmentManager, "AddStockDialog")
            }
        )
        binding.rvInventory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@InventoryFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etInventorySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchQuery.value = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFab() {
        binding.fabAddProduct.setOnClickListener {
            AddEditProductBottomSheetDialogFragment.newInstance(null)
                .show(parentFragmentManager, "AddProductSheet")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
