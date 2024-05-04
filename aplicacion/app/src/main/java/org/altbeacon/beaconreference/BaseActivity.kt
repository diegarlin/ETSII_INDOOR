package org.altbeacon.beaconreference

import SharedPreferencesManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

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
        menu?.findItem(R.id.loginMenu)?.isVisible = !tokenExists
        menu?.findItem(R.id.registerMenu)?.isVisible = !tokenExists
        menu?.findItem(R.id.logoutMenu)?.isVisible = tokenExists
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
                SharedPreferencesManager.clearTokenFromSharedPreferences(this@BaseActivity)
                Toast.makeText(this@BaseActivity, "Logout exitoso", Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}