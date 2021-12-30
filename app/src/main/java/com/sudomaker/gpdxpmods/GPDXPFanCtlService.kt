package com.sudomaker.gpdxpmods

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.graphics.drawable.RotateDrawable
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.lang.Exception
import java.util.*

class GPDXPFanCtlService: TileService() {
    private var currentFanSpeed: Int = -1
    private lateinit var timer: Timer

    private val fanCtlFile: File by lazy {
        val fileDir = applicationContext.filesDir
        File(fileDir, "sys_nodes/fan_ctrl")
    }

    private val rotatedDrawable: RotateDrawable by lazy {
        val fanEnabledDrawable = ContextCompat.getDrawable(this, R.drawable.ic_fan_enabled)
        val rotatedDrawable = RotateDrawable()
        rotatedDrawable.drawable = fanEnabledDrawable
        rotatedDrawable.fromDegrees = 0f
        rotatedDrawable.toDegrees = 360f
        rotatedDrawable.level = 0
        rotatedDrawable
    }

    private fun getRotatedBitmap(step: Int): Bitmap {
        rotatedDrawable.level += step
        if (rotatedDrawable.level > 10000) rotatedDrawable.level -= 10000
        return rotatedDrawable.toBitmap(96, 96)
    }

    private fun updateFanTile() {
        val tile = qsTile

        when (currentFanSpeed) {
            0 -> {
                tile.label = "Fan Off"
                tile.state = Tile.STATE_INACTIVE
                tile.icon = Icon.createWithResource(this, R.drawable.ic_fan_disabled)
            }

            in 1..9 -> {
                tile.label = "Fan Speed: $currentFanSpeed"
                tile.state = Tile.STATE_ACTIVE

                tile.icon = Icon.createWithBitmap(getRotatedBitmap(currentFanSpeed * 50))
            }

            else -> {
                tile.state = Tile.STATE_UNAVAILABLE
            }
        }

        tile.updateTile()
    }

    private fun mapActualFanSpeed(fanSpeed: Int): Int {
        var actualFanSpeed = fanSpeed
        if (actualFanSpeed in 1..8) actualFanSpeed = 9 - actualFanSpeed
        return actualFanSpeed
    }

    private fun getFanSpeed(): Int {
        return try {
            val inputStream = fanCtlFile.inputStream()
            val readByte = inputStream.read()
            inputStream.close()
            mapActualFanSpeed(Integer.parseInt(readByte.toChar().toString()))
        } catch (err: Exception) {
            -1
        }
    }

    private fun setFanSpeed(fanSpeed: Int) {
        currentFanSpeed = fanSpeed
        fanCtlFile.writeText(mapActualFanSpeed(fanSpeed).toString())
    }

    override fun onClick() {
        super.onClick()

        val currentFanSpeed = getFanSpeed()

        var nextFanSpeed = currentFanSpeed + 1
        if (nextFanSpeed == 10) nextFanSpeed = 0

        setFanSpeed(nextFanSpeed)
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        setFanSpeed(0)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        setFanSpeed(0)
    }

    override fun onStartListening() {
        super.onStartListening()
        currentFanSpeed = getFanSpeed()
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateFanTile()
            }
        }, 0, 33)
    }

    override fun onStopListening() {
        super.onStopListening()
        timer.cancel()
    }
}