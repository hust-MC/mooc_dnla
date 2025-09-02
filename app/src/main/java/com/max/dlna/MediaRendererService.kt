package com.max.dlna

import android.util.Log
import org.fourthline.cling.binding.annotations.UpnpAction
import org.fourthline.cling.binding.annotations.UpnpInputArgument
import org.fourthline.cling.binding.annotations.UpnpOutputArgument
import org.fourthline.cling.binding.annotations.UpnpService
import org.fourthline.cling.binding.annotations.UpnpServiceId
import org.fourthline.cling.binding.annotations.UpnpServiceType
import org.fourthline.cling.binding.annotations.UpnpStateVariable
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes

@UpnpService(
    serviceId = UpnpServiceId("AVTransport"),
    serviceType = UpnpServiceType(value = "AVTransport", version = 1)
)
class MediaRendererService {

    /** 设备唯一标识符，通常是0 */
    @UpnpStateVariable(defaultValue = "0", sendEvents = false, name = "InstanceID")
    private var instanceId: UnsignedIntegerFourBytes? = null

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

    /** 视频播放状态 */
    @UpnpStateVariable(defaultValue = "STOPPED")
    private var currentTransportState = "STOPPED"

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
    }
}