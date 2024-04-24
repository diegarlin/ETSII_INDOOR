package org.altbeacon.beaconreference

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.api.ApiClient
import android.util.Log
import com.google.gson.JsonObject
import android.widget.TextView
import android.view.View
import android.widget.ProgressBar
import com.google.gson.JsonParser
import kotlinx.coroutines.withContext

class RegisterActivity : Activity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        errorTextView = findViewById(R.id.errorTextView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView.visibility = View.GONE

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            registerUser(username, password)
        }
    }

    private fun registerUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }
            try {
                // Realiza la llamada a la API para el inicio de sesión
                val deviceID = BeaconReferenceApplication.deviceID
                val response = ApiClient.register(this@RegisterActivity, username, password, deviceID)

                // Verifica si la respuesta es exitosa
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    // Verifica si la respuesta contiene un token
                    val msg = responseBody?.get("msg")?.asString

                    if (msg.equals("Usuario registrado exitosamente")) {

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
                showError("Error del servidor.\nInténtalo de nuevo.")
            }finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }


    private fun showError(message: String) {
        runOnUiThread {
            errorTextView.visibility = View.VISIBLE
            errorTextView.text = message
        }
    }
}