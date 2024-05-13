package org.altbeacon.beaconreference

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.apiUsers.ApiClientUsuarios
import org.altbeacon.apiUsers.UpdateUserRequest
import org.altbeacon.utils.BaseActivity
import org.altbeacon.utils.SharedPreferencesManager

class EditUserActivity : BaseActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var adminSwitch: SwitchCompat
    private lateinit var profesorSwitch: SwitchCompat
    private lateinit var despachoEditText: TextInputEditText
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)
        setupToolbar(R.id.toolbar)

        progressBar = findViewById(R.id.progressBar)

        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            // Handle error
            return
        }

        progressBar.visibility = View.VISIBLE

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        adminSwitch = findViewById(R.id.adminSwitch)
        profesorSwitch = findViewById(R.id.profesorSwitch)
        despachoEditText = findViewById(R.id.despachoEditText)
        updateButton = findViewById(R.id.updateButton)

        despachoEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateButton.performClick()
                true
            } else {
                false
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClientUsuarios.getUser(this@EditUserActivity, userId)
                Log.d("api", response.toString())
                if (response.isSuccessful) {
                    val user = response.body()!!
                    withContext(Dispatchers.Main) {
                        usernameEditText.setText(user.username)
                        emailEditText.setText(user.email)
                        adminSwitch.isChecked = user.admin
                        profesorSwitch.isChecked = user.profesor
                        despachoEditText.setText(user.despacho?.toString())
                    }
                } else {
                    runOnUiThread {
                        SharedPreferencesManager.clearTokenAndAdminFromSharedPreferences(this@EditUserActivity)
                        Toast.makeText(this@EditUserActivity, "Tu token ha expirado vuelve a loguearte", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this@EditUserActivity, "Vuelve a probar dentro de 1 minuto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.d("api", "Exception: ", e)
                    Toast.makeText(
                        this@EditUserActivity,
                        "Error al obtener los datos. Contacte con el administrador",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                }
            }
        }

        updateButton.setOnClickListener {
            hideKeyboard(this@EditUserActivity)
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val admin = adminSwitch.isChecked
            val profesor = profesorSwitch.isChecked
            val despacho = despachoEditText.text.toString().trim()

            val request = UpdateUserRequest(username, email, admin, profesor, despacho)

            progressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiClientUsuarios.updateUser(this@EditUserActivity, userId, request)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            runOnUiThread {
                                Toast.makeText(this@EditUserActivity, "Se han actualizado los datos exitosamente", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                SharedPreferencesManager.clearTokenAndAdminFromSharedPreferences(this@EditUserActivity)
                                Toast.makeText(this@EditUserActivity, "Tu token ha expirado vuelve a loguearte", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    runOnUiThread {
                        Toast.makeText(this@EditUserActivity, "Vuelve a probar dentro de 1 minuto", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Log.d("api", "Exception: ", e)
                        Toast.makeText(
                            this@EditUserActivity,
                            "Error al actualizar los datos. Contacte con el administrador",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } finally {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}