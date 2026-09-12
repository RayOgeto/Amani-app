package com.refresh.pos.domain.engine

import com.refresh.pos.domain.model.Receipt
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.Locale

object ReceiptGenerator {

    /**
     * Formats receipt as readable formatted text for sharing.
     */
    fun formatPlainText(receipt: Receipt): String {
        val sb = StringBuilder()
        val line = "--------------------------------"
        val dline = "================================"

        sb.appendLine(dline)
        sb.appendLine(receipt.storeName.center(32))
        sb.appendLine(receipt.storeAddress.center(32))
        sb.appendLine(receipt.storePhone.center(32))
        if (receipt.tillNumber.isNotBlank()) {
            sb.appendLine(receipt.tillNumber.center(32))
        }
        sb.appendLine(dline)
        sb.appendLine("Receipt No: ${receipt.receiptNumber}")
        sb.appendLine("Date      : ${receipt.dateString}")
        sb.appendLine("Cashier   : ${receipt.cashierName}")
        if (receipt.customerName.isNotBlank()) {
            sb.appendLine("Customer  : ${receipt.customerName}")
        }
        sb.appendLine(line)
        sb.appendLine("ITEM             QTY   PRICE AMT")
        sb.appendLine(line)

        for (item in receipt.items) {
            val name = if (item.name.length > 14) item.name.substring(0, 14) else item.name
            val qtyStr = if (item.quantity % 1.0 == 0.0) {
                "${item.quantity.toInt()} ${item.unitOfMeasure}".take(6).padStart(6)
            } else {
                "${String.format(Locale.US, "%.2f", item.quantity)} ${item.unitOfMeasure}".take(6).padStart(6)
            }
            val price = String.format(Locale.US, "%.0f", item.unitPrice).padStart(4)
            val amt = String.format(Locale.US, "%.1f", item.total).padStart(6)
            sb.appendLine(String.format(Locale.US, "%-14s %s %s %s", name, qtyStr, price, amt))
        }

        sb.appendLine(line)
        sb.appendLine("Subtotal:".padEnd(16) + CurrencyUtils.formatKsh(receipt.subtotal).padStart(16))
        sb.appendLine("VAT (${receipt.taxRatePercent}%):".padEnd(16) + CurrencyUtils.formatKsh(receipt.taxAmount).padStart(16))
        if (receipt.discountAmount > 0.0) {
            sb.appendLine("Discount:".padEnd(16) + ("-" + CurrencyUtils.formatKsh(receipt.discountAmount)).padStart(16))
        }
        sb.appendLine(dline)
        sb.appendLine("TOTAL:".padEnd(14) + CurrencyUtils.formatKsh(receipt.total).padStart(18))
        sb.appendLine(dline)
        sb.appendLine("Payment: ${receipt.paymentMethod.displayName}")
        if (receipt.paymentReference.isNotBlank()) {
            sb.appendLine("Ref: ${receipt.paymentReference}")
        }
        sb.appendLine("Tendered:".padEnd(16) + CurrencyUtils.formatKsh(receipt.amountTendered).padStart(16))
        sb.appendLine("Change  :".padEnd(16) + CurrencyUtils.formatKsh(receipt.changeAmount).padStart(16))
        sb.appendLine(line)
        sb.appendLine("Asante kwa kununua nasi!".center(32))
        sb.appendLine("Karibu Tena (Come Again)".center(32))
        sb.appendLine(dline)

        return sb.toString()
    }

    /**
     * Generates ESC/POS standard byte command array for thermal printers (58mm/80mm).
     */
    fun generateEscPosBytes(receipt: Receipt): ByteArray {
        val out = ByteArrayOutputStream()

        // Init printer
        out.write(byteArrayOf(0x1B, 0x40))

        // Center align
        out.write(byteArrayOf(0x1B, 0x61, 0x01))

        // Double height & width bold title
        out.write(byteArrayOf(0x1B, 0x21, 0x38))
        out.write("${receipt.storeName}\n".toByteArray(Charset.forName("ASCII")))

        // Normal text
        out.write(byteArrayOf(0x1B, 0x21, 0x00))
        out.write("${receipt.storeAddress}\n".toByteArray(Charset.forName("ASCII")))
        out.write("${receipt.storePhone}\n".toByteArray(Charset.forName("ASCII")))
        if (receipt.tillNumber.isNotBlank()) {
            out.write("${receipt.tillNumber}\n\n".toByteArray(Charset.forName("ASCII")))
        } else {
            out.write("\n".toByteArray(Charset.forName("ASCII")))
        }

        // Left align
        out.write(byteArrayOf(0x1B, 0x61, 0x00))
        val body = formatPlainText(receipt)
        out.write(body.toByteArray(Charset.forName("ASCII")))

        // Center align footer
        out.write(byteArrayOf(0x1B, 0x61, 0x01))
        out.write("\n\n\n".toByteArray(Charset.forName("ASCII")))

        // Cut paper
        out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10))

        return out.toByteArray()
    }

    private fun String.center(length: Int): String {
        if (this.length >= length) return this
        val pad = (length - this.length) / 2
        return " ".repeat(pad) + this
    }
}
