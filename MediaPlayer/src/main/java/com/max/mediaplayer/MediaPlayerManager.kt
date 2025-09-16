package com.max.mediaplayer

// 设计关键点：
// 1、设计状态枚举
// 2、监听状态接口
// 3、简介的API

class MediaPlayerManager {

    /**
     * 播放状态枚举
     */
    enum class PlaybackState {
        STOPPED, PLAYING, PAUSED, ERROR
    }

    /**
     * 媒体播放状态回调
     */
    interface MediaStateListener {
        fun onPrepared(durationMs: Int)
        fun onProgressUpdate(positionMs: Int)
        fun onPlaybackStateChanged(state: PlaybackState)
        fun onPlaybackCompleted()
        fun onError(errorMsg: String)
        fun onBufferingUpdate(percent: Int)
    }

    /**
     * 设置url并播放视
     * @param uri 视频地址
     */
    fun setMediaURIAndPlay(uri: String) {
        // TODO: 设置url并播放视频
    }

    /**
     * 播放视频
     */
    fun play() {
        // TODO: 播放视频
    }

    /**
     * 暂停视频
     */
    fun pause() {
        // TODO: 暂停
    }

    /**
     * 停止播放
     */
    fun stop() {
        // TODO: 停止播放
    }

    /**
     * 释放播放器资源
     */
    fun release() {
        // TODO: 释放播放器资源
    }

    /**
     * 设置进度
     * @param positionMs 进度值，MS
     */
    fun seekTo(positionMs: Int) {
        // TODO: 设置进度
    }

    /**
     * 获取当前播放进度
     *
     * @return 当前播放进度
     */
    fun getCurrentPosition(): Int {
        // TODO: 获取当前播放进度
        return -1
    }

    /**
     * 获取视频总时长
     *
     * @return 视频总时长
     */
    fun getDuration(): Int {
        // TODO: 获取视频总时长
        return -1
    }
}