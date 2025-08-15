package com.max.dlna

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d(TAG, "DLNA服务启动成功")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d(TAG, "DLNA服务断开")

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启动DLNA服务
        val intent = Intent(this, DLNAService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    companion object {
        const val TAG = "MainActivity"
    }

}