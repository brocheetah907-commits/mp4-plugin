package com.cheetahmesh.mp4plugin

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import java.nio.ByteBuffer

class Mp4Encoder(godot: Godot) : GodotPlugin(godot) {

    private var encoder: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var trackIndex = -1
    private var muxerStarted = false
    private var frameIndex = 0
    private var width = 0
    private var height = 0
    private var fps = 24

    override fun getPluginName(): String = "Mp4Encoder"

    @UsedByGodot
    fun startEncoding(outputPath: String, w: Int, h: Int, frameRate: Int, bitRate: Int) {
        width = w
        height = h
        fps = frameRate
        frameIndex = 0
        muxerStarted = false
        trackIndex = -1

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder?.start()

        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        Log.i("Mp4Encoder", "Started encoding: $outputPath ($width x $height @ $fps fps)")
    }

    @UsedByGodot
    fun encodeFrame(pngBytes: ByteArray) {
        val bmp = BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.size)
        if (bmp == null) {
            Log.e("Mp4Encoder", "Failed to decode frame $frameIndex")
            return
        }
        drainEncoder(false)
        frameIndex++
        bmp.recycle()
    }

    @UsedByGodot
    fun finishEncoding() {
        drainEncoder(true)
        encoder?.stop()
        encoder?.release()
        muxer?.stop()
        muxer?.release()
        encoder = null
        muxer = null
        Log.i("Mp4Encoder", "Finished encoding, total frames: $frameIndex")
    }

    private fun drainEncoder(endOfStream: Boolean) {
        // مكانها هيتحدد بالتفصيل في الخطوة الجاية
    }
}
