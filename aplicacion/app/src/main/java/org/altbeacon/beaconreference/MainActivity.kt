package org.altbeacon.beaconreference

import SharedPreferencesManager
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.permissions.BeaconScanPermissionsActivity


class MainActivity : Activity() {
    private lateinit var loginButton: Button
    private lateinit var logoutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)
        loginButton = findViewById<Button>(R.id.loginButton)
        logoutButton = findViewById<Button>(R.id.logoutButton)
        val savedToken = SharedPreferencesManager.getTokenFromSharedPreferences(this@MainActivity)
        if (!savedToken.isNullOrBlank()) {
            loginButton.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
        } else {
            loginButton.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE
        }
//        val serviceIntent = Intent(this, BeaconForegroundService::class.java)
//        startForegroundService(serviceIntent)


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

    fun monitorizeButtonTapped(view: View){
        val intent = Intent(this, MonitorizarActivity::class.java)
        startActivity(intent)
    }
    fun mapButtonTapped(view: View) {
        val intent = Intent(this, MapaActivity::class.java)
        startActivity(intent)
    }

    fun loginButtonTapped(view: View) {

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun closestBeaconTapped(view: View) {
        val beaconTracker = (application as BeaconReferenceApplication).beaconTracker
        beaconTracker.updateRoomRecords()
    }
    fun logoutButtonTapped(view: View) {
        SharedPreferencesManager.clearTokenFromSharedPreferences(this@MainActivity)
        loginButton.visibility = View.VISIBLE
        logoutButton.visibility = View.GONE
    }
    companion object {
        val TAG = "MainActivity"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }

}
