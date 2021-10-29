package com.skygolf.sggattserver

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var log: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        log = findViewById(R.id.log)
        Timber.plant(LogTree())

        Timber.tag(TAG).i("App started")

//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Timber.tag(TAG).e("App requires permissions. Please grant and restart")
//            return
//        }

        SgServer.startServer(this)
    }

    private inner class LogTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, tag, message, t)
            log.append("\r\n")
            log.append(message)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}