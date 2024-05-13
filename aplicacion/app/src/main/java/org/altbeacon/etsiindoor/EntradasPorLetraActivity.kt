package org.altbeacon.etsiindoor

import ApiClientRegistros
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.utils.BaseActivity
import org.altbeacon.utils.HabitacionAdapter

class EntradasPorLetraActivity : BaseActivity() {
    private lateinit var listView: ListView
    private lateinit var letraEditText: EditText
    private lateinit var buscarButton: Button
    private lateinit var emptyTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entradas_letra)
        setupToolbar(R.id.toolbar)

        listView = findViewById(R.id.listView)
        letraEditText = findViewById(R.id.letraEditText)
        buscarButton = findViewById(R.id.buscarButton)
        emptyTextView = findViewById(R.id.emptyTextView)
        progressBar = findViewById(R.id.progressBar)

        buscarButton.setOnClickListener {
            val letra = letraEditText.text.toString()

            if (emptyTextView.visibility == View.VISIBLE) {
                emptyTextView.visibility = View.GONE
            }


            progressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiClientRegistros.getEntradasPorLetra(letra)

                    if (response.isSuccessful && response.body() != null) {
                        val habitaciones = response.body()!!

                        runOnUiThread {
                            if (habitaciones.isEmpty()) {
                                listView.visibility = View.GONE
                                emptyTextView.visibility = View.VISIBLE
                            } else {
                                listView.visibility = View.VISIBLE
                                emptyTextView.visibility = View.GONE
                                listView.adapter =
                                    HabitacionAdapter(this@EntradasPorLetraActivity, habitaciones)
                            }
                        }
                    } else {
                        Log.d("api", response.toString())
                        runOnUiThread {
                            Toast.makeText(
                                this@EntradasPorLetraActivity,
                                "Error al obtener los registros",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@EntradasPorLetraActivity,
                            "Vuelve a probar dentro de 1 minuto",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Log.d("api", "Exception: " + e.toString())
                        Toast.makeText(
                            this@EntradasPorLetraActivity,
                            "Error al obtener los registros. Contacte con el administrador",
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
}