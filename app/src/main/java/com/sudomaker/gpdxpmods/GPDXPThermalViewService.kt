package com.sudomaker.gpdxpmods

import android.graphics.Color
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.graphics.drawable.toBitmap
import com.amulyakhare.textdrawable.TextDrawable
import java.io.File
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

class GPDXPThermalViewService : TileService() {
    private val decimalFormat: DecimalFormat = DecimalFormat("0.## °C")
    private lateinit var timer: Timer
    private val temp5File: File by lazy {
        val fileDir = applicationContext.filesDir
        File(fileDir, "sys_nodes/temp5")
    }

    private fun updateTile(temperature: Int) {
        val tile = qsTile
        val temperatureFloat = temperature.toFloat() / 1000
        val temperatureString = decimalFormat.format(temperatureFloat)

        val temperatureDrawable = TextDrawable.builder()
            .beginConfig()
            .fontSize(80)
            .bold()
            .endConfig()
            .buildRound(temperatureFloat.roundToInt().toString(), Color.TRANSPARENT)

        tile.label = temperatureString
        tile.state = Tile.STATE_ACTIVE
        tile.icon = Icon.createWithBitmap(temperatureDrawable.toBitmap(96, 96))

        tile.updateTile()
    }

    private fun getCurrentTemp5(): Int {
        return try {
            val inputStream = temp5File.inputStream()
            val byteArr = ByteArray(5)
            inputStream.read(byteArr, 0, byteArr.size)
            inputStream.close()
            Integer.parseInt(String(byteArr))
        } catch (err: Exception) {
            return -1
        }
    }

    private fun refreshTile() {
        updateTile(getCurrentTemp5())
    }

    override fun onClick() {
        super.onClick()
        refreshTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    override fun onTileAdded() {
        super.onTileAdded()
    }

    override fun onStartListening() {
        super.onStartListening()

        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                refreshTile()
            }
        }, 0, 1000)
    }

    override fun onStopListening() {
        super.onStopListening()

        timer.cancel()
        // Called when the tile is no longer visible
    }
}