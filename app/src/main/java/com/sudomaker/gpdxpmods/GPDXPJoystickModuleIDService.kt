package com.sudomaker.gpdxpmods

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import java.io.File
import java.lang.Exception
import java.util.*

class GPDXPJoystickModuleIDService : TileService() {
    private lateinit var timer: Timer
    private val rightIDFile: File by lazy {
        val fileDir = applicationContext.filesDir
        File(fileDir, "sys_nodes/right_id")
    }

    private fun updateTile(rightID: String) {
        val tile = qsTile

        tile.label = "Module: $rightID"
        tile.state = Tile.STATE_ACTIVE

        tile.updateTile()
    }

    private fun getRightID(): Int {
        return try {
            val inputStream = rightIDFile.inputStream()
            val idByte = inputStream.read()
            inputStream.close()
            Integer.parseInt(idByte.toChar().toString())
        } catch (err: Exception) {
            -1
        }
    }

    private fun refreshTile() {
        updateTile(getRightID().toString())
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
    }
}