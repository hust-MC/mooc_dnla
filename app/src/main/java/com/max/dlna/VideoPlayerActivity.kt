package com.max.dlna

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.max.mediaplayer.MediaPlayerManager
import com.max.mediaplayer.formatTime
import java.util.Locale

/**
 * 1、承载播放器的UI
 * 2、接收由视频软件发过来的视频URI
 * 3、将URI设置给播放器——VideoPlayerManager
 * 4、不断接收由播放器传递上来的状态
 * 5、注册VideoPlayerManager监听事件，拿到播放器的回调
 * 6、切后台自动暂停、前台自动恢复
 */
class VideoPlayerActivity : AppCompatActivity() {
    private var videoUri = ""

    private var mediaPlayerManager: MediaPlayerManager? = null

    private lateinit var surfaceView: SurfaceView
    private lateinit var seekBar: SeekBar
    private lateinit var tvDuration: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnBack: ImageButton

    private var isVideoInitialized = false
    private var wasPlayingBeforeBackground = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 1000)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        videoUri = intent.getStringExtra(EXTRA_VIDEO_URI) ?: ""

        initMediaPlayerManager()

        initView()

        surfaceView.holder.addCallback(VideoSurfaceCallback())
    }


    override fun onResume() {
        super.onResume()
        // 定时器开启
        handler.post(updateSeekBarRunnable)

        if (wasPlayingBeforeBackground) {
            mediaPlayerManager?.play()
            wasPlayingBeforeBackground = false
        }

        // 设置播放状态对应的按钮图标
        mediaPlayerManager?.let {
            val isPlaying = it.getCurrentState() == MediaPlayerManager.PlaybackState.PLAYING
            btnPlayPause.setImageResource(
                if (isPlaying) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // 定期器关闭
        handler.removeCallbacks(updateSeekBarRunnable)

        mediaPlayerManager?.let {
            wasPlayingBeforeBackground =
                it.getCurrentState() == MediaPlayerManager.PlaybackState.PLAYING
            if (wasPlayingBeforeBackground) {
                it.pause()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        mediaPlayerManager?.release()
        mediaPlayerManager = null

        handler.removeCallbacks(updateSeekBarRunnable)
    }

    private fun initMediaPlayerManager() {
        mediaPlayerManager = MediaPlayerManager(this).apply {
            setStateListener(object : MediaPlayerManager.MediaStateListener {
                override fun onPrepared(durationMs: Int) {
                    Log.d(TAG, "onPrepared")
                }

                override fun onProgressUpdate(positionMs: Int) {
                    Log.d(TAG, "onProgressUpdate")

                }

                override fun onPlaybackStateChanged(state: MediaPlayerManager.PlaybackState) {
                    Log.d(TAG, "onPlaybackStateChanged")

                    runOnUiThread {
                        when (state) {
                            MediaPlayerManager.PlaybackState.PLAYING -> {
                                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                            }

                            MediaPlayerManager.PlaybackState.PAUSED -> {
                                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)

                            }

                            else -> {
                                // 处理其他情况
                            }
                        }
                    }
                }

                override fun onPlaybackCompleted() {
                    Log.d(TAG, "onPlaybackCompleted")

                }

                override fun onError(errorMsg: String) {
                    Log.d(TAG, "onError: $errorMsg")

                }

                override fun onBufferingUpdate(percent: Int) {
                    Log.d(TAG, "onBufferingUpdate: $percent")
                }

            })
        }
    }

    private fun initView() {
        surfaceView = findViewById(R.id.playerSurfaceView)
        seekBar = findViewById(R.id.seekBar)
        tvDuration = findViewById(R.id.tvDuration)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnBack = findViewById(R.id.btnBack)

        // 播放暂停按钮：控制播放器的暂停和播放
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        // 返回按钮：点击之后关闭视频及页面
        btnBack.setOnClickListener {
            mediaPlayerManager?.release()
            finish()
        }

        // 进度条
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayerManager?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })


    }

    private fun togglePlayPause() {
        mediaPlayerManager?.let {
            if (it.getCurrentState() == MediaPlayerManager.PlaybackState.PLAYING) {
                it.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                it.play()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
    }

    /**
     * 更新进度条
     *
     * 不断查询播放进度，同步更新进度显示
     *
     */
    private fun updateProgress() {
        mediaPlayerManager?.let {
            val position = it.getCurrentPosition()
            val duration = it.getDuration()

            if (duration > 0) {
                seekBar.max = duration
                seekBar.progress = position

                val positionStr = formatTime(position)
                val durationStr = formatTime(duration)
                tvDuration.text = "$positionStr / $durationStr"
            }
        }
    }

    private inner class VideoSurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceCreated")

            // 当Surface创建后，将其设置给MediaPlayerManager
            mediaPlayerManager?.setSurface(holder.surface)

            // Created方法会被调用多次，导致每次都重新播放
            if (!isVideoInitialized) {
                mediaPlayerManager?.setMediaURIAndPlay(videoUri)
                isVideoInitialized = true
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            Log.d(TAG, "surfaceChanged")

            // Surface 尺寸或格式发生变化的时候，更新surface
            mediaPlayerManager?.setSurface(holder.surface)
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            Log.d(TAG, "surfaceChanged")

            // Surface销毁的时候，清除播放器的surface
            mediaPlayerManager?.setSurface(null)
        }

    }

    companion object {
        const val TAG = "VideoPlayerActivity"
        private const val EXTRA_VIDEO_URI = "extra_video_uri"

        /**
         * 唯一的启动入口，需要传入视频URI
         */
        fun start(context: Context, videoUri: String) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URI, videoUri)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}