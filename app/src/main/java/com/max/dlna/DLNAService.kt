package com.max.dlna

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.max.dlna.MediaRendererService.Companion.initialize
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.DeviceDetails
import org.fourthline.cling.model.meta.DeviceIdentity
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.LocalService
import org.fourthline.cling.model.meta.ManufacturerDetails
import org.fourthline.cling.model.meta.ModelDetails
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import java.util.UUID

class DLNAService : Service() {
    private var upnpService: AndroidUpnpService? = null
    private val binder = LocalBinder()

    private var serviceConnection: ServiceConnection? = null

    inner class LocalBinder : Binder() {
        fun getService(): DLNAService = this@DLNAService
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        initialize(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                upnpService = service as AndroidUpnpService
                createDevice()
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                upnpService = null
            }

        }

        applicationContext.bindService(
            Intent(this, AndroidUpnpServiceImpl::class.java),
            serviceConnection!!,
            Context.BIND_AUTO_CREATE
        )

    }

    private fun createDevice() {
        try {
            // 填写设备描述
            val udn = UDN(UUID.randomUUID())
            val identity = DeviceIdentity(udn)
            val type = UDADeviceType("MediaRenderer", 1)
            val details = DeviceDetails(
                "MCPlayer",
                ManufacturerDetails("Max"),
                ModelDetails("MaxCarPlayer", "车载DLNA播放器", "v1.0")
            )

            val binder = AnnotationLocalServiceBinder()

            @Suppress("UNCHECKED_CAST")
            val mediaRendererService =
                binder.read(MediaRendererService::class.java) as LocalService<MediaRendererService>
            mediaRendererService.manager =
                DefaultServiceManager(mediaRendererService, MediaRendererService::class.java)
            initialize(this@DLNAService)
            // 根据描述创建设备
            val localDevice = LocalDevice(identity, type, details, arrayOf(mediaRendererService))

            // 注册到网络
            upnpService?.registry?.addDevice(localDevice)

            Log.d(TAG, "设备创建成功，名称：${details.friendlyName}")
        } catch (e: Exception) {
            Log.e(TAG, "设备创建失败", e)
        }
    }

    /**
     * 创建通知通道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "DLNA服务通道", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DLNA服务通道"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DLNA投屏服务")
            .setContentText("正在运行中...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val TAG = "DLNAService"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "DLNA_SERVICE_CHANNEL"
    }
}