package com.arpitbandil.demo.blutoothchat.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.arpitbandil.demo.blutoothchat.R

fun Context?.hasAllPermissions(vararg permissions: String): Boolean {
    return permissions.map { permission ->
        this?.let {
            ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }.all { it }
}

fun Context?.hasFeatureAvailable(feature: String): Boolean {
    return this?.packageManager?.hasSystemFeature(feature) ?: false
}

fun Context?.toast(msg: Int) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Context?.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()