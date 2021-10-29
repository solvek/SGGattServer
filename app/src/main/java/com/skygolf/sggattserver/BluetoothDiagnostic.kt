package com.skygolf.sggattserver

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.text.TextUtils

@SuppressLint("MissingPermission")
object BluetoothDiagnostic {
    private const val GATT_CONN_TIMEOUT = 8
    private const val GATT_CONN_TERMINATE_PEER_USER = 19 // Disconnect on Server side

    /*
  22 just means "connection terminated by local host" (defined by the Bluetooth specification). Some examples why the host terminates:
  1. You call disconnect().
  2. The GATT timer times out, which happens if the remote device doesn't respond to a request in 30 seconds.
  3. The SMP timer times out, which happens if the pairing process doesn't make any progress in 30 seconds.
   */
    private const val GATT_CONN_TERMINATE_LOCAL_HOST = 22 // Disconnect on Client side

    const val GATT_ERROR = 133

    /**
     * List of all error codes https://medium.com/@abrisad_it/ble-error-codes-a3c6675b29c1
     */
    fun printGattStatus(status: Int): String? {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> return "Success"
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> return "GATT_READ_NOT_PERMITTED"
            BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> return "GATT_WRITE_NOT_PERMITTED"
            BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> return "GATT_INSUFFICIENT_AUTHENTICATION"
            BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> return "GATT_REQUEST_NOT_SUPPORTED"
            BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> return "GATT_INSUFFICIENT_ENCRYPTION"
            BluetoothGatt.GATT_INVALID_OFFSET -> return "GATT_INVALID_OFFSET"
            BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> return "GATT_INVALID_ATTRIBUTE_LENGTH"
            BluetoothGatt.GATT_CONNECTION_CONGESTED -> return "GATT_CONNECTION_CONGESTED"
            BluetoothGatt.GATT_FAILURE -> return "GATT_FAILURE"
            GATT_ERROR -> return "GATT_ERROR"
            GATT_CONN_TERMINATE_LOCAL_HOST -> return "GATT_CONN_TERMINATE_LOCAL_HOST"
            GATT_CONN_TERMINATE_PEER_USER -> return "GATT_CONN_TERMINATE_PEER_USER"
            GATT_CONN_TIMEOUT -> return "GATT_CONN_TIMEOUT"
        }
        return "Unknown ($status)"
    }

    fun printBondState(device: BluetoothDevice): String? {
        val state = device.bondState
        return printBondState(state)
    }

    fun printBondState(state: Int): String {
        when (state) {
            BluetoothDevice.BOND_NONE -> return "none"
            BluetoothDevice.BOND_BONDING -> return "bonding"
            BluetoothDevice.BOND_BONDED -> return "bonded"
        }
        return "Unknown ($state)"
    }

    fun printConnectionState(state: Int): String? {
        when (state) {
            BluetoothProfile.STATE_CONNECTED -> return "connected"
            BluetoothProfile.STATE_CONNECTING -> return "connecting"
            BluetoothProfile.STATE_DISCONNECTED -> return "disconnected"
            BluetoothProfile.STATE_DISCONNECTING -> return "disconnecting"
        }
        return "unknown state $state"
    }

    fun printAdvError(error: Int): String? {
        when (error) {
            AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> return "Advertisement already started"
            AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> return "Advertisement data is too large"
            AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> return "This feature is not supported on this platform."
            AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> return "Operation failed due to an internal error."
            AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> return "Failed to start advertising because no advertising instance is available."
        }
        return "Unknown advertising error $error"
    }

    fun printDeviceName(device: BluetoothDevice): String? {
        val name = device.name
        return if (TextUtils.isEmpty(name)) {
            device.address
        } else name + "(" + device.address + ")"
    }

    fun printDevice(device: BluetoothDevice?): String {
        return if (device == null) {
            "{null device}"
        } else "{" +
                "address: "+
                device.address +
                ", name: "+
                device.name+
                ", bond: "+
                printBondState(device.bondState)+
                ", type: "+
                printDeviceType(device.type)+
                "}"
    }

    fun printDeviceType(type: Int): String {
        when (type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> return "Classic"
            BluetoothDevice.DEVICE_TYPE_LE -> return "BLE"
            BluetoothDevice.DEVICE_TYPE_DUAL -> return "Dual"
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> return "Unknown"
        }
        return "Unknown($type)"
    }

    fun printProfileName(profile: Int): String? {
        when (profile) {
            BluetoothProfile.GATT -> return "GATT"
            BluetoothProfile.GATT_SERVER -> return "GATT_SERVER"
        }
        return "Unknown($profile)"
    }

    fun printServices(gatt: BluetoothGatt): String? {
        val services = gatt.services
        if (services.size == 0) {
            return "No services"
        }
        val sb = StringBuilder("\n")
        for (service in services) {
            sb.append("Service ").append(service.uuid)
            val characteristics = service.characteristics
            sb.append("\n")
            if (characteristics.size == 0) {
                sb.append("\tNo characteristics.\n")
            }
            for (ch in characteristics) {
                sb.append("\t").append(ch.uuid).append("\n")
            }
        }
        return sb.toString()
    }

}