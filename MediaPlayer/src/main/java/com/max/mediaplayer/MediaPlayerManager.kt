package com.max.mediaplayer

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

// 设计关键点：
// 1、设计状态枚举
// 2、监听状态接口
// 3、简介的API
@OptIn(UnstableApi::class)
class MediaPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var currentUri = ""
    private var shouldAutoPlay = false
    private var currentState = PlaybackState.STOPPED
    private var duration: Int = 0
    private var stateListener: MediaStateListener? = null

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
     * 设置url并自动播放视频
     * @param uri 视频地址
     */
    fun setMediaURIAndPlay(uri: String) {
        shouldAutoPlay = true
        setMediaURI(uri)
    }

    private fun setMediaURI(uri: String) {
        currentUri = uri
        Log.d(TAG, "设置媒体URI：$uri")
        // 播放之前释放播放器资源
        release()
        setupExoPlayer(uri)
    }

    private fun setupExoPlayer(uri: String) {

        val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(
            MediaCacheFactory.getCacheFactory(
                context
            )
        )
        exoPlayer = ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build()
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.d(TAG, "ExoPlayer播放错误：${error.message}")
                currentState = PlaybackState.ERROR
                stateListener?.onPlaybackStateChanged(currentState)
                stateListener?.onError("播放失败：${error.message}")
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    currentState = PlaybackState.PLAYING
                    stateListener?.onPlaybackStateChanged(currentState)
                } else {
                    // 播放器不在 播放状态，可能是暂停或者播放结束
                    // STATE_STOPPED会处理停止状态
                    if (exoPlayer?.playbackState == Player.STATE_READY) {
                        currentState = PlaybackState.PAUSED
                        stateListener?.onPlaybackStateChanged(currentState)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.d(TAG, "ExoPlayer状态：就绪")

                        // 获取视频时长
                        val durationMs = exoPlayer?.duration?.toInt() ?: 0
                        if (durationMs > 0) {
                            duration = durationMs
                            stateListener?.onPrepared(durationMs)
                        }

                        if (shouldAutoPlay) {
                            exoPlayer?.play()
                            shouldAutoPlay = false
                        }
                    }

                    Player.STATE_ENDED -> {
                        Log.d(TAG, "ExoPlayer状态：播放结束")
                        currentState = PlaybackState.STOPPED
                        stateListener?.onPlaybackStateChanged(currentState)
                        stateListener?.onPlaybackCompleted()
                    }

                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "ExoPlayer状态：缓冲中")
                        stateListener?.onBufferingUpdate(50)
                    }

                    Player.STATE_IDLE -> {
                        Log.d(TAG, "ExoPlayer状态：空闲")
                    }
                }
            }
        })
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

    companion object {
        const val TAG = "MediaPlayerManager"
    }
}