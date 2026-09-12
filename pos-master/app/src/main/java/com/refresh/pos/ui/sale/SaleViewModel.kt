package com.refresh.pos.ui.sale

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.refresh.pos.PosApplication
import com.refresh.pos.data.local.entity.CustomerEntity
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.data.local.entity.SaleEntity
import com.refresh.pos.data.local.entity.SaleLineItemEntity
import com.refresh.pos.domain.engine.TaxCalculator
import com.refresh.pos.domain.model.CartItem
import com.refresh.pos.domain.model.PaymentMethod
import com.refresh.pos.domain.model.Receipt
import com.refresh.pos.domain.model.ReceiptItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SaleViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<PosApplication>()
    private val productRepo = app.productRepository
    private val saleRepo = app.saleRepository
    private val customerRepo = app.customerRepository

    // Search query
    val searchQuery = MutableStateFlow("")

    // Category filter for Produce Speed-Grid
    val selectedCategory = MutableStateFlow("All")

    val categories: StateFlow<List<String>> = productRepo.categories
        .map { list -> listOf("All") + list }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Customers list for Credit Tab (Deni)
    val customers: StateFlow<List<CustomerEntity>> = customerRepo.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered available products
    val availableProducts: StateFlow<List<ProductEntity>> = combine(
        searchQuery,
        selectedCategory
    ) { query, category ->
        Pair(query.trim(), category)
    }.flatMapLatest { (query, category) ->
        when {
            query.isNotEmpty() -> productRepo.search(query)
            category != "All" -> productRepo.getByCategory(category)
            else -> productRepo.activeProducts
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart items state
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Subtotal
    val subtotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Tax calculation (inclusive by default)
    val taxBreakdown: StateFlow<TaxCalculator.TaxBreakdown> = subtotal.map { total ->
        TaxCalculator.calculateInclusive(total, 16.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaxCalculator.TaxBreakdown(0.0, 0.0, 0.0))

    val itemCount: StateFlow<Int> = _cartItems.map { items ->
        items.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _lastReceipt = MutableStateFlow<Receipt?>(null)
    val lastReceipt: StateFlow<Receipt?> = _lastReceipt.asStateFlow()

    fun addToCart(product: ProductEntity, qty: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + qty)
        } else {
            current.add(CartItem(product = product, quantity = qty))
        }
        _cartItems.value = current
    }

    fun increaseQuantity(productId: Long) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = current[index]
            val step = if (existing.product.isWeighed) 0.5 else 1.0
            current[index] = existing.copy(quantity = existing.quantity + step)
            _cartItems.value = current
        }
    }

    fun decreaseQuantity(productId: Long) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = current[index]
            val step = if (existing.product.isWeighed) 0.5 else 1.0
            if (existing.quantity > step) {
                current[index] = existing.copy(quantity = existing.quantity - step)
            } else {
                current.removeAt(index)
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        val current = _cartItems.value.toMutableList()
        current.removeAll { it.product.id == productId }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    /**
     * Looks up barcode or PLU code and immediately adds item to cart.
     */
    suspend fun onBarcodeOrPluScanned(code: String): ProductEntity? {
        val product = productRepo.getByBarcodeOrPlu(code.trim()) ?: return null
        if (!product.isWeighed) {
            addToCart(product, 1.0)
        }
        return product
    }

    /**
     * Complete checkout and save sale to Room.
     */
    suspend fun checkout(
        paymentMethod: PaymentMethod,
        amountTendered: Double,
        paymentReference: String = "",
        customerId: Long? = null,
        customerName: String = "",
        cashierName: String = "Cashier"
    ): Result<Receipt> {
        val items = _cartItems.value
        if (items.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty"))
        }

        val totalAmount = subtotal.value
        val taxInfo = taxBreakdown.value
        val now = System.currentTimeMillis()
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(now))
        val changeAmount = if (amountTendered >= totalAmount) amountTendered - totalAmount else 0.0

        val saleEntity = SaleEntity(
            startTime = dateString,
            endTime = dateString,
            status = "ENDED",
            paymentMethod = paymentMethod.name,
            subtotal = taxInfo.subtotalExclTax,
            tax = taxInfo.taxAmount,
            total = totalAmount,
            amountTendered = amountTendered,
            changeAmount = changeAmount,
            paymentReference = paymentReference,
            customerId = customerId,
            customerName = customerName,
            cashierName = cashierName,
            timestamp = now
        )

        val lineItems = items.map { cartItem ->
            SaleLineItemEntity(
                saleId = 0,
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                barcode = cartItem.product.barcode,
                quantity = cartItem.quantity,
                unitOfMeasure = cartItem.product.unitOfMeasure,
                unitPrice = cartItem.product.unitPrice,
                totalPrice = cartItem.subtotal
            )
        }

        val saleId = saleRepo.completeSale(saleEntity, lineItems)

        if (paymentMethod == PaymentMethod.STORE_CREDIT && customerId != null) {
            customerRepo.addDebt(customerId, totalAmount)
        }

        val receipt = Receipt(
            receiptNumber = "REC-${String.format(Locale.US, "%06d", saleId)}",
            cashierName = cashierName,
            dateString = dateString,
            customerName = customerName,
            items = items.map {
                ReceiptItem(
                    name = it.product.name,
                    quantity = it.quantity,
                    unitOfMeasure = it.product.unitOfMeasure,
                    unitPrice = it.product.unitPrice,
                    total = it.subtotal
                )
            },
            subtotal = taxInfo.subtotalExclTax,
            taxAmount = taxInfo.taxAmount,
            total = totalAmount,
            paymentMethod = paymentMethod,
            amountTendered = amountTendered,
            changeAmount = changeAmount,
            paymentReference = paymentReference
        )

        _lastReceipt.value = receipt
        clearCart()
        return Result.success(receipt)
    }
}
