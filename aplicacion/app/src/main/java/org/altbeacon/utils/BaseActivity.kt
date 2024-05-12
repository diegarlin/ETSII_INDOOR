package org.altbeacon.utils

import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.altbeacon.beaconreference.ETSIINDOOR
import org.altbeacon.beaconreference.EntradasPorLetraActivity
import org.altbeacon.beaconreference.EntradasPorLetraYFechaActivity
import org.altbeacon.beaconreference.LoginActivity
import org.altbeacon.beaconreference.MapaActivity
import org.altbeacon.beaconreference.MonitorizarActivity
import org.altbeacon.beaconreference.PersonasActualPorLetraActivity
import org.altbeacon.beaconreference.PersonasActualPorLetraYFechaActivity
import org.altbeacon.beaconreference.R
import org.altbeacon.beaconreference.RegisterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.apiUsers.ApiClientUsuarios
import org.altbeacon.beaconreference.MandarAvisoActivity

open class BaseActivity: AppCompatActivity() {
    private lateinit var toolbar: Toolbar

    protected fun setupToolbar(toolbarId: Int) {
        toolbar = findViewById(toolbarId)
        setSupportActionBar(toolbar)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val tokenExists = SharedPreferencesManager.existsToken(this)
        val esAdmin = SharedPreferencesManager.isAdmin(this)
        menu?.findItem(R.id.loginMenu)?.isVisible = !tokenExists
        menu?.findItem(R.id.registerMenu)?.isVisible = !tokenExists
        menu?.findItem(R.id.logoutMenu)?.isVisible = tokenExists
        menu?.findItem(R.id.cerrarEntradas)?.isVisible = esAdmin
        menu?.findItem(R.id.mandarAviso)?.isVisible = esAdmin
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.registerMenu -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.loginMenu -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.monitorizeMenu -> {
                val intent = Intent(this, MonitorizarActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.mapaMenu -> {
                val intent = Intent(this, MapaActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.entradasPorLetraMenu -> {
                val intent = Intent(this, EntradasPorLetraActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.entradasPorLetraYFechaMenu -> {
                val intent = Intent(this, EntradasPorLetraYFechaActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.personasActualPorLetraMenu -> {
                val intent = Intent(this, PersonasActualPorLetraActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.personasActualPorLetraYFechaMenu -> {
                val intent = Intent(this, PersonasActualPorLetraYFechaActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.closestBeaconMenu -> {
                val beaconTracker = (application as ETSIINDOOR).beaconTracker
                beaconTracker.updateRoomRecords()
                true
            }
            R.id.logoutMenu -> {
                SharedPreferencesManager.clearTokenAndAdminFromSharedPreferences(this@BaseActivity)
                Toast.makeText(this@BaseActivity, "Logout exitoso", Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu()
                true
            }
            R.id.cerrarEntradas -> {
                val token = SharedPreferencesManager.getTokenFromSharedPreferences(this)
                Log.d("api", token.toString())
                if (token != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = ApiClientUsuarios.cerrar_entradas(this@BaseActivity, token.toString())
                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@BaseActivity, "Has cerrado las entradas con éxito", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@BaseActivity, "Error al cerrar las entradas", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: java.net.SocketTimeoutException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@BaseActivity, "Ha habido un problema con el servidor. Prueba en 1 minuto.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@BaseActivity, "No se encontró el token. Logueate de nuevo o por primera vez", Toast.LENGTH_LONG).show()
                }
                true
            }
            R.id.mandarAviso -> {
                val intent = Intent(this, MandarAvisoActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}