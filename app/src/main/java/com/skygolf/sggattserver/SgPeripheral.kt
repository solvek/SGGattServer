package com.skygolf.sggattserver

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import java.util.*

@SuppressLint("MissingPermission")
object SgPeripheral : BluetoothGattServerCallback() {
    private const val TAG = "SgPeripheral"
    private val SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val CHARA_RX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    private val CHARA_TX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val DESCRIPTOR_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val locationData: ByteArray = byteArrayOf(0x03, 0x54, 0x68)

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    private lateinit var gattServer: BluetoothGattServer
    private lateinit var characteristicRX: BluetoothGattCharacteristic

    fun start(context: Context) {
        bluetoothManager = context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager

        gattServer = bluetoothManager.openGattServer(context, this)
//        if (gattServer == null) {
//            Timber.tag(TAG)
//                .e("Unable to create GATT server")
//            return
//        }

        if (!gattServer.addService(createService())) {
            Timber.tag(TAG)
                .e("Failed to add GATT service")
            return
        }

        Timber.tag(TAG)
            .d("Gatt service started")

        initAdvertiser()
        startAdvertising()
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)

        Timber.tag(TAG).d(
            "onConnectionStateChange device %s. Status: %s, New state: %s",
            BluetoothDiagnostic.printDevice(device),
            BluetoothDiagnostic.printGattStatus(status),
            BluetoothDiagnostic.printConnectionState(newState)
        )

//        if (newState == BluetoothGatt.STATE_CONNECTED) {
//            stopAdvertising()
//        }
//
//        if (newState == BluetoothGatt.STATE_DISCONNECTED){
//            startAdvertising()
//        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        Timber.tag(TAG)
            .i("GattServer.onServiceAdded. status: %s", BluetoothDiagnostic.printGattStatus(status))
        super.onServiceAdded(status, service)
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?, requestId: Int, offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        super.onCharacteristicWriteRequest(
            device, requestId, characteristic, preparedWrite,
            responseNeeded, offset, value
        )
        Timber.tag(TAG)
            .d("GattServer.GetData from device: %s", Arrays.toString(value))
        if (responseNeeded) {
            gattServer.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
                null
            )
        }

        if (device != null) {
            sendLocation(device)
        }
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice?, requestId: Int, offset: Int,
        descriptor: BluetoothGattDescriptor
    ) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor)
        Timber.tag(TAG).d(
            "GattServer.onDescriptorReadRequest. Device tried to read descriptor: %s",
            descriptor.uuid
        )
        Timber.tag(TAG)
            .d("Value: %s", Arrays.toString(descriptor.value))
        if (offset != 0) {
            gattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_INVALID_OFFSET,
                offset,  /* value (optional) */
                null
            )
            return
        }
        gattServer.sendResponse(
            device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
            descriptor.value
        )
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?, requestId: Int,
        descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean,
        offset: Int, value: ByteArray?
    ) {
        Timber.tag(TAG)
            .d("GattServer.onDescriptorWriteRequest.")
        super.onDescriptorWriteRequest(
            device, requestId, descriptor, preparedWrite, responseNeeded,
            offset, value
        )
        //bleSyncServer.addIncomingBlock(value);
        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
    }

    private fun createService(): BluetoothGattService {
        val service = BluetoothGattService(SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY)

        characteristicRX = BluetoothGattCharacteristic(
            CHARA_RX,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ
                    or BluetoothGattDescriptor.PERMISSION_WRITE
                    or BluetoothGattCharacteristic.PROPERTY_NOTIFY
        )

        val notify = BluetoothGattDescriptor(
            DESCRIPTOR_NOTIFICATION,
            BluetoothGattDescriptor.PERMISSION_READ
                    or BluetoothGattDescriptor.PERMISSION_WRITE
                    or BluetoothGattCharacteristic.PROPERTY_NOTIFY
        )
        characteristicRX.addDescriptor(notify)

        service.addCharacteristic(characteristicRX)
        Timber.tag(TAG)
            .d("Created RX characteristic")

        val characteristicTX = BluetoothGattCharacteristic(
            CHARA_TX,  //Read-only characteristic, supports notifications
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristicTX)

        return service
    }

    private fun sendLocation(device: BluetoothDevice) {
        Timber.tag(TAG).i("Sending location to client")

        if (!characteristicRX.setValue(locationData)) {
            Timber.tag(TAG)
                .d("mCharacteristicTX setValue error")
            return
        }
        try {
            if (!gattServer.notifyCharacteristicChanged(
                    device, characteristicRX,
                    false
                )
            ) {
                Timber.tag(TAG)
                    .d("notifyCharacteristicChanged. Send data error")
                return
            }
        } catch (th: Throwable) {
            Timber.tag(TAG)
                .e(th, "Some internal error happen when trying to write data via ble")
            return
        }
    }

    @SuppressLint("HardwareIds")
    private fun initAdvertiser() {
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

//        if (bluetoothAdapter == null) {
//            Timber.tag(TAG)
//                .e("Bluetooth adapter is null")
//            return
//        }

        val name = bluetoothAdapter.name
        val address = bluetoothAdapter.address

        Timber.tag(TAG).i("Device name: $name, address: $address")

        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
    }

    private fun startAdvertising() {
//        if (bluetoothLeAdvertiser == null) {
//            Timber.tag(TAG)
//                .e("Failed to create advertiser")
//            return
//        }

        val advMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
        val txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM

        val settings = AdvertiseSettings.Builder().setAdvertiseMode(advMode)
            .setConnectable(true) //.setTimeout(0)
            .setTxPowerLevel(txPowerLevel)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE))
            .setIncludeTxPowerLevel(true)
            .build()

        val scanResponse = AdvertiseData.Builder().setIncludeDeviceName(true).build()

        Timber.tag(TAG).i("Starting advertisement")
        bluetoothLeAdvertiser.startAdvertising(settings, data, scanResponse, advertiseCallback)
    }

//    private fun stopAdvertising() {
//        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
//        Timber.tag(TAG).i("Advertisement stopped")
//    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Timber.tag(TAG).i("LE Advertise Started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.tag(TAG)
                .w("LE Advertise Failed: %s", BluetoothDiagnostic.printAdvError(errorCode))
        }
    }
}