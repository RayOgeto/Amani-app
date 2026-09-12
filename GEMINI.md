# Project: Amani-app (Modern Mobile Point of Sale)

## Project Overview

Amani POS is a modern, high-performance Android Point of Sale (POS) and inventory management system designed for retail, grocery, and mobile merchant operations. Built with modern Android best practices (Kotlin, Material Design 3, Room Persistence, Coroutines & Flow, MVVM architecture).

### Core Features

*   **Fast POS Register & Interactive Cart:**
    *   One-tap catalog selection with instant search and category filtering.
    *   Integrated Barcode Scanning using device camera (ZXing Embedded) and handheld hardware scanners.
    *   Interactive Cart with quantity steppers (+ / -), item subtotals, and swipe/tap removal.
*   **Multi-Tender Payment Processing:**
    *   **Cash:** Quick-cash denomination buttons ($10, $20, $50, $100, Exact) and real-time change calculation.
    *   **Digital / Mobile Money:** Support for M-Pesa / Mobile Money transaction reference recording.
    *   **Card:** Credit/Debit transaction handling.
*   **Thermal Receipt Printing & Sharing:**
    *   **Bluetooth ESC/POS Printing:** Connects to standard 58mm / 80mm wireless thermal POS receipt printers.
    *   **Digital Receipt Sharing:** Formats receipts as digital invoices shareable via WhatsApp, SMS, or Email.
*   **Inventory & Stock Lot Management:**
    *   Add/Edit products with barcode, selling price, cost price, and category.
    *   Stock lot receiving (batch quantity & cost tracking).
    *   Automatic stock deduction upon sale completion.
    *   Initial product catalog preloaded automatically from `res/raw/products.csv`.
*   **Analytics, Reporting & Cashier Shifts:**
    *   Real-time revenue, total order counts, and cash vs. digital breakdown.
    *   Period filtering: Daily, Weekly, Monthly, Yearly.
    *   Historical transaction review with instant receipt re-printing.
    *   End-of-day **Z-Report** reconciliation generation.

---

## Architecture & Technology Stack

*   **Platform:** Android (API 24+ / Android 7.0 Nougat to Android 14+)
*   **Language:** 100% Kotlin with Coroutines & StateFlow
*   **UI Framework:** Material 3 Design System (`Theme.Material3.DayNight.NoActionBar`), ViewBinding, ListAdapter with DiffUtil
*   **Architecture Pattern:** Clean Architecture + MVVM (Model-View-ViewModel)
*   **Database:** AndroidX Room Database 2.6.1 with KSP (Kotlin Symbol Processing)
*   **Hardware Peripherals:**
    *   Bluetooth SPP (`00001101-0000-1000-8000-00805F9B34FB`) for ESC/POS Thermal Printers
    *   CameraX / ZXing Barcode Scanner
*   **Build System:** Gradle 8.4.2, AGP 8.4.2, Java 17/21 compatible

---

## Directory Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/refresh/pos/
│   ├── PosApplication.kt               # Application singleton & dependency provider
│   ├── data/
│   │   ├── local/
│   │   │   ├── PosDatabase.kt          # Room Database definition
│   │   │   ├── DatabaseSeeder.kt       # Preloads CSV products on first launch
│   │   │   ├── entity/                 # ProductEntity, SaleEntity, SaleLineItemEntity, StockLotEntity, CashierShiftEntity
│   │   │   ├── relation/               # SaleWithLineItems
│   │   │   └── dao/                    # ProductDao, SaleDao, StockDao, CashierShiftDao
│   │   └── repository/                 # ProductRepository, SaleRepository, ShiftRepository
│   ├── domain/
│   │   ├── model/                      # CartItem, Receipt, Enums (PaymentMethod, PeriodFilter)
│   │   └── engine/                     # TaxCalculator (inclusive VAT), ReceiptGenerator (ESC/POS & PlainText)
│   ├── printer/
│   │   └── EscPosPrinterService.kt     # Bluetooth SPP wireless thermal printing
│   └── ui/
│       ├── MainActivity.kt             # Navigation host with Material 3 BottomNavigationView
│       ├── sale/                       # SaleFragment, SaleViewModel, CartAdapter, ProductCatalogAdapter, CheckoutBottomSheet, ReceiptDialog
│       ├── inventory/                  # InventoryFragment, InventoryViewModel, InventoryAdapter, AddEditProductSheet, AddStockDialog
│       └── report/                     # ReportFragment, ReportViewModel, SaleHistoryAdapter
└── res/                                # Material 3 themes, modern color palette, vector assets, and responsive layouts
```

---

## Building and Running

1. **Prerequisites:**
   * JDK 17 or JDK 21
   * Android SDK (API 34)

2. **Build Debug APK:**
   ```bash
   cd pos-master
   ./gradlew assembleDebug
   ```
   Output APK: `pos-master/app/build/outputs/apk/debug/app-debug.apk`

3. **Install on Device or Emulator:**
   ```bash
   adb install pos-master/app/build/outputs/apk/debug/app-debug.apk
   ```
