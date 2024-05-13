package org.altbeacon.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.apiUsers.ApiClientUsuarios
import org.altbeacon.utils.BeaconScanPermissionsActivity
import org.altbeacon.etsiindoor.R
import org.altbeacon.utils.BaseActivity
import org.altbeacon.utils.SharedPreferencesManager


class MainActivity : BaseActivity() {
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var mapaButton: Button
    private lateinit var entradasPorLetraButton: Button
    private lateinit var entradasPorLetraYFechaButton: Button
    private lateinit var personasActualPorLetraButton: Button
    private lateinit var personasActualPorLetraYFechaButton: Button
    private lateinit var monitorizeButton: Button
    private lateinit var cerrarEntradasButton: Button
    private lateinit var listaUsuariosButton: Button
    private lateinit var mandarAvisoButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar(R.id.toolbar)

        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        mapaButton = findViewById(R.id.mapaButton)
        entradasPorLetraButton = findViewById(R.id.entradasPorLetraButton)
        entradasPorLetraYFechaButton = findViewById(R.id.entradasPorLetraYFechaButton)
        personasActualPorLetraButton = findViewById(R.id.personasActualPorLetraButton)
        personasActualPorLetraYFechaButton = findViewById(R.id.personasActualPorLetraYFechaButton)
        monitorizeButton = findViewById(R.id.monitorizeButton)
        cerrarEntradasButton = findViewById(R.id.cerrarEntradasButton)
        listaUsuariosButton = findViewById(R.id.listaUsuariosButton)
        mandarAvisoButton = findViewById(R.id.mandarAvisoButton)
        logoutButton = findViewById(R.id.logoutButton)

        val tokenExists = SharedPreferencesManager.existsToken(this)
        val esAdmin = SharedPreferencesManager.isAdmin(this)
        loginButton.isVisible = !tokenExists
        registerButton.isVisible = !tokenExists
        logoutButton.isVisible = tokenExists
        cerrarEntradasButton.isVisible = esAdmin
        mandarAvisoButton.isVisible = esAdmin
        listaUsuariosButton.isVisible = esAdmin
        monitorizeButton.isVisible = esAdmin


        val toastMessage = intent.getStringExtra("TOAST_MESSAGE")
        if (!toastMessage.isNullOrEmpty()) {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

        checkBatteryOptimizations()

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        mapaButton.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
        }

        entradasPorLetraButton.setOnClickListener {
            val intent = Intent(this, EntradasPorLetraActivity::class.java)
            startActivity(intent)
        }

        entradasPorLetraYFechaButton.setOnClickListener {
            val intent = Intent(this, EntradasPorLetraYFechaActivity::class.java)
            startActivity(intent)
        }

        personasActualPorLetraButton.setOnClickListener {
            val intent = Intent(this, PersonasActualPorLetraActivity::class.java)
            startActivity(intent)
        }

        personasActualPorLetraYFechaButton.setOnClickListener {
            val intent = Intent(this, PersonasActualPorLetraYFechaActivity::class.java)
            startActivity(intent)
        }

        monitorizeButton.setOnClickListener {
            val intent = Intent(this, MonitorizarActivity::class.java)
            startActivity(intent)
        }

        cerrarEntradasButton.setOnClickListener {
            val token = SharedPreferencesManager.getTokenFromSharedPreferences(this)
            Log.d("api", token.toString())
            if (token != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClientUsuarios.cerrar_entradas(
                            this@MainActivity,
                            token.toString()
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Has cerrado las entradas con éxito",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error al cerrar las entradas",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Ha habido un problema con el servidor. Prueba en 1 minuto.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "No se encontró el token. Logueate de nuevo o por primera vez",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        listaUsuariosButton.setOnClickListener {
            val intent = Intent(this, ListaUsuariosActivity::class.java)
            startActivity(intent)
        }

        mandarAvisoButton.setOnClickListener {
            val intent = Intent(this, MandarAvisoActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            SharedPreferencesManager.clearTokenAndAdminFromSharedPreferences(this@MainActivity)
            Toast.makeText(this@MainActivity, "Logout exitoso", Toast.LENGTH_SHORT).show()
            recreate()
        }



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
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            ) {
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
