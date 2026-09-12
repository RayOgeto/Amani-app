package com.refresh.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.refresh.pos.R
import com.refresh.pos.databinding.LayoutMainBinding
import com.refresh.pos.ui.inventory.InventoryFragment
import com.refresh.pos.ui.report.ReportFragment
import com.refresh.pos.ui.sale.SaleFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LayoutMainBinding

    private val saleFragment by lazy { SaleFragment() }
    private val inventoryFragment by lazy { InventoryFragment() }
    private val reportFragment by lazy { ReportFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupBackNavigation()

        if (savedInstanceState == null) {
            showFragment(saleFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_sale
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNavigation.selectedItemId != R.id.nav_sale) {
                    binding.bottomNavigation.selectedItemId = R.id.nav_sale
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_sale -> {
                    showFragment(saleFragment)
                    binding.topToolbar.subtitle = "Sale Terminal"
                    true
                }
                R.id.nav_inventory -> {
                    showFragment(inventoryFragment)
                    binding.topToolbar.subtitle = "Inventory Management"
                    true
                }
                R.id.nav_report -> {
                    showFragment(reportFragment)
                    binding.topToolbar.subtitle = "Sales & Reports"
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
