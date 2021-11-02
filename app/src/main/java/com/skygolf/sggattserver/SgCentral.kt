package com.skygolf.sggattserver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

object SgCentral {
    private const val TAG = "SgCentral"

    @SuppressLint("MissingPermission")
    fun start(context: Context){
        val bluetoothManager = context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

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

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val record = result.scanRecord
            Timber.tag(TAG).i("Nearby device found. Address ${BluetoothDiagnostic.printDevice(device)}, advertisement: ${record?.bytes}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Timber.tag(TAG).i("Scan failed $errorCode")
        }
    }
}