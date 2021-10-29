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
        Timber.plant(LogTree())

        SgServer.startServer(this)
    }

    private inner class LogTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, tag, message, t)
            log.append("\r\n")
            log.append(message)
        }
    }
}