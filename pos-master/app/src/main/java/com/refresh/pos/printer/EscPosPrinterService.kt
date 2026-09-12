package com.refresh.pos.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class EscPosPrinterService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    companion object {
        private const val TAG = "EscPosPrinter"
        // Standard SPP UUID for Bluetooth Serial communication
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    val isBluetoothAvailable: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(): List<BluetoothDevice> {
        if (!isBluetoothEnabled) return emptyList()
        return try {
            bluetoothAdapter?.bondedDevices?.filter { device ->
                val name = device.name?.lowercase() ?: ""
                name.contains("print") || name.contains("pos") || name.contains("mpt") ||
                        name.contains("rpp") || name.contains("bluetooth")
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting paired printers", e)
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun printBytes(device: BluetoothDevice, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter?.cancelDiscovery()
            socket.connect()

            outputStream = socket.outputStream
            outputStream.write(data)
            outputStream.flush()

            // Small delay to ensure printer buffer completes
            Thread.sleep(500)
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to print to ${device.name}", e)
            Result.failure(e)
        } finally {
            try {
                outputStream?.close()
                socket?.close()
            } catch (ignored: Exception) {}
        }
    }
}
