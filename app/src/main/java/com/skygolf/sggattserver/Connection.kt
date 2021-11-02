package com.skygolf.sggattserver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import timber.log.Timber
import java.util.*

@SuppressLint("MissingPermission")
class Connection(private val device: BluetoothDevice) {
    fun run(context: Context){
        Timber.tag(TAG).i("Connecting to ${BluetoothDiagnostic.printDevice(device)}")
        device.connectGatt(context, true, gattCallback)
    }

    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Timber.tag(TAG).i(
                "onConnectionStateChange device %s. Status: %s, New state: %s",
                BluetoothDiagnostic.printDevice(device),
                BluetoothDiagnostic.printGattStatus(status),
                BluetoothDiagnostic.printConnectionState(newState)
            )
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            Timber.tag(TAG).i(
                "Discovered services. Status: %s",
                BluetoothDiagnostic.printGattStatus(status)
            )

            if (gatt == null){
                Timber.tag(TAG).w("Gatt is null")
                return
            }

            requestBatteryLevel(gatt)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)

            val value = characteristic?.value?.getOrNull(0)?.toString() ?: "empty"

            Timber.tag(TAG).i(
                "onCharacteristicRead. Status: %s, value: $value",
                BluetoothDiagnostic.printGattStatus(status)
            )
        }
    }

    private fun requestBatteryLevel(gatt: BluetoothGatt) {
        val service = gatt.getService(SERVICE_BATTERY)
        val chara = service.getCharacteristic(CHARACTERISTIC_BATTERY)

        Timber.tag(TAG).i("Reading battery level")

        if (!gatt.readCharacteristic(chara)) {
            Timber.tag(TAG).e("Battery level reading failed")
        }
    }

    companion object {
        private const val TAG = "Connection"

        private val SERVICE_BATTERY = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        private val CHARACTERISTIC_BATTERY = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    }
}