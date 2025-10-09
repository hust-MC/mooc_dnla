package com.max.dlna

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.max.mediaplayer.MediaPlayerManager
import com.max.mediaplayer.formatTime
import org.fourthline.cling.binding.annotations.UpnpAction
import org.fourthline.cling.binding.annotations.UpnpInputArgument
import org.fourthline.cling.binding.annotations.UpnpOutputArgument
import org.fourthline.cling.binding.annotations.UpnpService
import org.fourthline.cling.binding.annotations.UpnpServiceId
import org.fourthline.cling.binding.annotations.UpnpServiceType
import org.fourthline.cling.binding.annotations.UpnpStateVariable
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.lastchange.LastChange
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask

@UpnpService(
    serviceId = UpnpServiceId("AVTransport"),
    serviceType = UpnpServiceType(value = "AVTransport", version = 1)
)
class MediaRendererService {

    /** 设备唯一标识符，通常是0 */
    @UpnpStateVariable(defaultValue = "0", sendEvents = false, name = "InstanceID")
    private var instanceId: UnsignedIntegerFourBytes? = null

    @UpnpStateVariable(sendEvents = true)
    private var lastChange = ""

    @UpnpStateVariable(defaultValue = "1")
    private var speed: String = "1"

    /** 视频URI */
    @UpnpStateVariable(defaultValue = "")
    private var currentURI: String = ""

    /** 视频META DATA */
    @UpnpStateVariable(defaultValue = "")
    private var currentURIMetaData = ""

    /** 视频进度 */
    @UpnpStateVariable(defaultValue = "00:00:00")
    private var currentTrackDuration = "00:00:00"

    /** 视频进度 */
    @UpnpStateVariable(defaultValue = "00:00:00")
    private var currentMediaDuration = "00:00:00"

    /** 视频播放状态 */
    @UpnpStateVariable(defaultValue = "STOPPED")
    private var currentTransportState = "STOPPED"

    @UpnpStateVariable(defaultValue = "00:00:00")
    private var absTime: String = "00:00:00"

    @UpnpStateVariable(defaultValue = "00:00:00")
    private var relTime: String = "00:00:00"

    /**
     * 用于设置要播放的媒体URI
     */
    @UpnpAction
    fun setAVTransportURI(
        @UpnpInputArgument(name = "InstanceID") instanceId: UnsignedIntegerFourBytes,
        @UpnpInputArgument(name = "CurrentURI") uri: String,
        @UpnpInputArgument(name = "CurrentURIMetaData") metadata: String
    ) {
        Log.d(TAG, "收到设置视频URI的请求：$uri")
        currentURI = uri
        currentURIMetaData = metadata
        currentTransportState = STOPPED
    }

    /**
     * 接收到播放请求
     */
    @UpnpAction
    fun play(
        @UpnpInputArgument(name = "InstanceID") instanceId: UnsignedIntegerFourBytes,
        @UpnpInputArgument(name = "Speed") speed: String
    ) {
        Log.d(TAG, "收到播放请求，URI：$currentURI; Speed: $speed")

        currentTransportState = PLAYING

        this.speed = speed

        Handler(Looper.getMainLooper()).post {
            try {
                // 如果当前未打卡播放页面，则启动播放
                if (currentURI.isNotEmpty() && appContext != null) {
                    getContext()?.let {
                        VideoPlayerActivity.start(it, currentURI)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "播放失败：${e.message}", e)
            }
        }
    }

    @UpnpAction
    fun pause(
        @UpnpInputArgument(name = "InstanceID") instanceId: UnsignedIntegerFourBytes,
    ) {
        Log.d(TAG, "收到暂停请求，URI：$currentURI")
        currentTransportState = PAUSED
    }

    @UpnpAction
    fun stop(@UpnpInputArgument(name = "InstanceID") instanceId: UnsignedIntegerFourBytes) {
        Log.d(TAG, "收到停止请求，URI：$currentURI")
        currentTransportState = STOPPED

    }

    /**
     * 获取播放状态
     */
    @UpnpAction(out = [UpnpOutputArgument(name = "CurrentTransportState")])
    fun getTransportInfo(@UpnpInputArgument(name = "InstanceID") instanceID: UnsignedIntegerFourBytes): String {
        return currentTransportState
    }

    /**
     * 获取视频URI
     */
    /** 获取媒体信息 */
    @UpnpAction(out = [UpnpOutputArgument(name = "CurrentURI")])
    fun getMediaInfo(@UpnpInputArgument(name = "InstanceID") instanceID: UnsignedIntegerFourBytes): String {
        return currentURI
    }

    companion object {
        const val TAG = "MediaRendererService"

        const val STOPPED = "STOPPED"
        const val PAUSED = "PAUSED_PLAYBACK"
        const val PLAYING = "PLAYING"

        private var appContext: WeakReference<Context>? = null
        private var statusUpdateTimer: Timer? = null
        private var mediaPlayerManagerRef: WeakReference<MediaPlayerManager>? = null
        private var serviceInstance: WeakReference<MediaRendererService>? = null
        private var lastChange: LastChange? = null

        /**
         * 初始化服务
         * 设置应用上下文对象，并初始化播放器
         *
         * @param context 应用上下文
         */
        fun initialize(context: Context) {
            appContext = WeakReference(context.applicationContext)
            lastChange = LastChange(CustomAVTransportLastChangeParser())
        }

        /**
         * 获取Context
         */
        fun getContext(): Context? {
            return appContext?.get()
        }


        /**
         * 设置MediaPlayerManager实例引用，内部为弱引用
         */
        fun setMediaPlayerManager(manager: MediaPlayerManager) {
            mediaPlayerManagerRef = WeakReference(manager)
            Log.d(TAG, "MediaPlayerManager 设置成功")
        }

        private fun startStatueUpdateTimer() {
            statusUpdateTimer?.cancel()
            statusUpdateTimer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        updatePlaybackStatus()
                    }
                }, 0, 1000)
            }
        }

        private fun updatePlaybackStatus() {
            val player = mediaPlayerManagerRef?.get() ?: return
            val service = serviceInstance?.get() ?: return
            val state = player.getCurrentState()

            // 获取播放状态
            when (state) {
                MediaPlayerManager.PlaybackState.STOPPED -> {
                    service.currentTransportState = "STOPPED"
                }

                MediaPlayerManager.PlaybackState.PLAYING -> {
                    service.currentTransportState = "PLAYING"

                }

                MediaPlayerManager.PlaybackState.PAUSED -> {
                    service.currentTransportState = "PAUSED_PLAYBACK"

                }

                MediaPlayerManager.PlaybackState.ERROR -> {
                    service.currentTransportState = "STOPPED"
                }

                else -> {
                    // 仅做兜底
                    service.currentTransportState = "STOPPED"
                    Log.w(TAG, "updatePlaybackStatus 分支异常")
                }
            }

            service.lastChange = lastChange.toString()

            Log.d(TAG, "播放状态：$state")

            // 获取播放进度
            val position = player.getCurrentPosition()
            val duration = player.getDuration()

            if (duration > 0) {
                val formattedDuration = formatTime(duration)
                service.currentTrackDuration = formattedDuration
                service.currentMediaDuration = formattedDuration

                val formattedPosition = formatTime(position)
                service.absTime = formattedPosition
                service.relTime = formattedPosition
                Log.d(TAG, "播放进度：$formattedPosition; 播放总时长：$formattedDuration")
            }
        }
    }

}