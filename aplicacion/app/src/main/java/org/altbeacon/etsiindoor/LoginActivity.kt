package org.altbeacon.etsiindoor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.apiUsers.ApiClientUsuarios
import org.altbeacon.utils.BaseActivity
import org.altbeacon.utils.SharedPreferencesManager

class LoginActivity : BaseActivity() {
    private lateinit var usernameOrEmailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupToolbar(R.id.toolbar)

        usernameOrEmailEditText = findViewById(R.id.usernameOrEmailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        errorTextView = findViewById(R.id.errorTextView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView.visibility = View.GONE

        loginButton.setOnClickListener {
            val usernameOrEmail = usernameOrEmailEditText.text.toString()
            val password = passwordEditText.text.toString()

            hideKeyboard(this@LoginActivity)

            if (usernameOrEmail.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                loginUser(usernameOrEmail, password)
            }
        }

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun loginUser(usernameOrEmail: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }
            try {
                // Realiza la llamada a la API para el inicio de sesión
                val deviceID = ETSIINDOOR.deviceID
                val response =
                    ApiClientUsuarios.login(this@LoginActivity, usernameOrEmail, password, deviceID)

                // Verifica si la respuesta es exitosa
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    // Verifica si la respuesta contiene un token
                    val token = responseBody?.get("access_token")?.asString
                    val admin = responseBody?.get("admin")?.asBoolean

                    if (!token.isNullOrBlank() && admin != null) {
                        SharedPreferencesManager.saveTokenAndAdminToSharedPreferences(
                            this@LoginActivity,
                            token,
                            admin
                        )
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("TOAST_MESSAGE", "Inicio de sesión exitoso")
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Log.d("API_ERROR", "Exception: ")
                    val errorBody = response.errorBody()?.string()
                    val jsonObject = JsonParser().parse(errorBody).asJsonObject

                    // Obtiene el mensaje del atributo .msg
                    val message = jsonObject.get("msg").asString

                    showError(message)
                }
            } catch (e: java.net.SocketTimeoutException) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Vuelve a probar dentro de 1 minuto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.d("api", "Exception: ", e)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error del servidor. Contacte con el administrador",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
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