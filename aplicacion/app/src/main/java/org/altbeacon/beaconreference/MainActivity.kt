package org.altbeacon.beaconreference

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.os.PowerManager
import android.os.Build
import android.widget.Toast
import org.altbeacon.beacon.permissions.BeaconScanPermissionsActivity
import org.altbeacon.utils.BaseActivity


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)
        setupToolbar(R.id.toolbar)


        val toastMessage = intent.getStringExtra("TOAST_MESSAGE")
        if (!toastMessage.isNullOrEmpty()) {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

        checkBatteryOptimizations()

    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }
    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        if (!BeaconScanPermissionsActivity.allPermissionsGranted(this, true)) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN) ||
                shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                // Los permisos fueron denegados, pero el usuario no seleccionó "No preguntar de nuevo", por lo que podemos solicitarlos de nuevo
                val intent = Intent(this, BeaconScanPermissionsActivity::class.java)
                intent.putExtra("backgroundAccessRequested", true)
                startActivity(intent)
            } else {
                // Los permisos fueron denegados con "No preguntar de nuevo". Debemos dirigir al usuario a la configuración de la aplicación
                AlertDialog.Builder(this)
                    .setMessage("Los permisos necesarios para esta aplicación han sido denegados. Por favor, ve a la configuración de la aplicación para habilitarlos.")
                    .setPositiveButton("Configuración de la aplicación") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }


    fun checkBatteryOptimizations() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }
    companion object {
        val TAG = "MainActivity"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }

}
