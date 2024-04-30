package org.altbeacon.beaconreference

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.apiUsers.ApiClientUsuarios
import android.widget.TextView
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText= findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        errorTextView = findViewById(R.id.errorTextView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView.visibility = View.GONE

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val email = emailEditText.text.toString()

            hideKeyboard(this@RegisterActivity)

            if (username.isBlank() || password.isBlank() || email.isBlank()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(username, email, password)
            }
        }

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerButton.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }
            try {
                // Realiza la llamada a la API para el inicio de sesión
                val deviceID = ETSIINDOOR.deviceID
                val response = ApiClientUsuarios.register(this@RegisterActivity, username,email, password, deviceID)

                // Verifica si la respuesta es exitosa
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    // Verifica si la respuesta contiene un token
                    val msg = responseBody?.get("msg")?.asString

                    if (msg.equals("Usuario registrado exitosamente")) {
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.putExtra("TOAST_MESSAGE", "Registro exitoso")
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
            } catch (e: java.net.SocketTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Vuelve a probar dentro de 1 minuto", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                runOnUiThread {
                    Log.d("api", "Exception: ", e)
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error del servidor. Contacte con el administrador",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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