package com.skygolf.sggattserver

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open abstract class BleContext(protected val context: Context){
    protected val bluetoothManager = context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
    protected val adapter = bluetoothManager.adapter
}