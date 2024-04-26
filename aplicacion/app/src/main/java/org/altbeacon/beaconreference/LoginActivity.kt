package org.altbeacon.beaconreference

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.apiUsers.ApiClientUsuarios
import android.widget.TextView
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import com.google.gson.JsonParser
import kotlinx.coroutines.withContext

class LoginActivity : Activity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        errorTextView = findViewById(R.id.errorTextView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView.visibility = View.GONE

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            hideKeyboard(this@LoginActivity)

            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }
            try {
                // Realiza la llamada a la API para el inicio de sesión
                val deviceID = BeaconReferenceApplication.deviceID
                val response = ApiClientUsuarios.login(this@LoginActivity, username, password, deviceID)

                // Verifica si la respuesta es exitosa
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    // Verifica si la respuesta contiene un token
                    val token = responseBody?.get("access_token")?.asString

                    if (!token.isNullOrBlank()) {
                        SharedPreferencesManager.saveTokenToSharedPreferences(this@LoginActivity, token)
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("TOAST_MESSAGE", "Inicio de sesión exitoso")
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val jsonObject = JsonParser().parse(errorBody).asJsonObject

                    // Obtiene el mensaje del atributo .msg
                    val message = jsonObject.get("msg").asString

                    showError(message)
                }
            } catch (e: Exception) {
                showError("Error del servidor.\nInténtalo de nuevo en 1 minuto") //CAMBIAR IMPORTANTE ?
            }finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        // Verifica si no hay vista enfocada, ya que en ese caso el teclado se ocultará
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun showError(message: String) {
        runOnUiThread {
            errorTextView.visibility = View.VISIBLE
            errorTextView.text = message
        }
    }
}