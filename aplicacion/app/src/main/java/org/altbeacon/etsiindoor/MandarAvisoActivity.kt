package org.altbeacon.etsiindoor

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.apiUsers.ApiClientUsuarios
import org.altbeacon.utils.BaseActivity
import org.altbeacon.utils.SharedPreferencesManager

class MandarAvisoActivity : BaseActivity() {
    private lateinit var subjectEditText: EditText
    private lateinit var bodyEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mandar_aviso)
        setupToolbar(R.id.toolbar)
        subjectEditText = findViewById(R.id.subjectEditText)
        bodyEditText = findViewById(R.id.bodyEditText)
        sendButton = findViewById(R.id.sendButton)
        progressBar = findViewById(R.id.progressBar)

        sendButton.setOnClickListener {

            hideKeyboard(this@MandarAvisoActivity)

            val subject = subjectEditText.text.toString()
            val body = bodyEditText.text.toString()

            if (subject.isBlank() || body.isBlank()) {
                Toast.makeText(this, "Ambos campos deben estar llenos", Toast.LENGTH_SHORT).show()
            } else {
                sendEmail(subject, body)
            }
        }
    }

    private fun sendEmail(subject: String, body: String) {
        val token = SharedPreferencesManager.getTokenFromSharedPreferences(this)
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.VISIBLE
                }
                try {
                    val response =
                        ApiClientUsuarios.send_email(this@MandarAvisoActivity, subject, body, token)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@MandarAvisoActivity,
                                "Correo enviado con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MandarAvisoActivity,
                                "Error al enviar el correo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MandarAvisoActivity,
                            "Ha habido un problema con el servidor. Prueba en 1 minuto.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        } else {
            Toast.makeText(
                this@MandarAvisoActivity,
                "No se encontró el token. Logueate de nuevo o por primera vez",
                Toast.LENGTH_LONG
            ).show()
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
}