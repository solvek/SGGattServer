package com.skygolf.sggattserver

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import timber.log.Timber

private const val TAG = "SgCentral"

class SgCentral(context: Context): BleContext(context) {
    private lateinit var connection: Connection
    private val touched = HashSet<String>()

    @SuppressLint("MissingPermission")
    fun start(){
        val scanner = adapter.bluetoothLeScanner

//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_SCAN
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Timber.tag(TAG).w("Please give permission for bluetooth scanning")
//            return
//        }
        scanner.startScan(leScanCallback)
        Timber.tag(TAG).w("Scanning for peripherals")
    }

    private fun onDevice(result: ScanResult) {
        val device = result.device

        val address = device.address

        if (touched.contains(address)){
            return
        }

        touched.add(address)

        val record = result.scanRecord
        Timber.tag(TAG).i("Nearby device found. Address ${BluetoothDiagnostic.printDevice(device)}, advertisement: ${record?.bytes}")

        if (address == "00:80:E1:26:79:3D" && !this@SgCentral::connection.isInitialized){
            connection = Connection(device)
            connection.run(context)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            onDevice(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Timber.tag(TAG).i("Scan failed $errorCode")
        }
    }
}