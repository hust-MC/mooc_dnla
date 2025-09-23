package com.max.mediaplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object MediaCacheFactory {

    private const val TAG = "MediaCacheFactory"
    private var cacheFactory: DataSource.Factory? = null

    @Synchronized
    fun getCacheFactory(ctx: Context): DataSource.Factory {
        if (cacheFactory == null) {
            var downDirectory = File(ctx.cacheDir, "videos")
            val databaseProvider = StandaloneDatabaseProvider(ctx)
            var cache =
                SimpleCache(
                    downDirectory,
                    LeastRecentlyUsedCacheEvictor(1024 * 1024 * 512),
                    databaseProvider
                )

            cacheFactory = CacheDataSource.Factory().setCache(cache)
                .setCacheReadDataSourceFactory(
                    DefaultDataSource.Factory(
                        ctx, DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(false)
                            .setConnectTimeoutMs(8000)
                            .setReadTimeoutMs(8000)
                            .setUserAgent("MY_ExoPlayer")
                    )
                ).setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(false)
                        .setConnectTimeoutMs(8000)
                        .setReadTimeoutMs(8000)
                        .setUserAgent("MY_Exoplayer")
                ).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        }
        return cacheFactory!!
    }

}
