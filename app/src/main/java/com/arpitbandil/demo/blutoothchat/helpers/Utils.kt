package com.arpitbandil.demo.blutoothchat.helpers

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.arpitbandil.demo.blutoothchat.R

object Utils {
    fun hasAllBTPermissions(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.hasAllPermissions(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            true
        }
    }

    fun askAllBTPermissions(resultHandler: ActivityResultLauncher<Array<String>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            resultHandler.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        }
    }

    fun isBTHardwareUnAvailable(context: Context?): Boolean {
        return when {
            !context.hasFeatureAvailable(PackageManager.FEATURE_BLUETOOTH) -> {
                context.toast(R.string.bluetooth_not_supported)
                true
            }
            !context.hasFeatureAvailable(PackageManager.FEATURE_BLUETOOTH_LE) -> {
                context.toast(R.string.ble_not_supported)
                true
            }
            context?.getSystemService(BluetoothManager::class.java)?.adapter == null -> {
                context.toast(R.string.bluetooth_not_supported)
                true
            }
            else -> false
        }
    }

}