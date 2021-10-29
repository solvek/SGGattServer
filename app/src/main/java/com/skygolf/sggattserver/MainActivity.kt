package com.skygolf.sggattserver

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var log: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        log = findViewById(R.id.log)
        Timber.plant(LogTree(), Timber.DebugTree())

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

    private inner class LogTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, tag, message, t)

            runOnUiThread {
                log.append("\r\n")
                log.append(message)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}