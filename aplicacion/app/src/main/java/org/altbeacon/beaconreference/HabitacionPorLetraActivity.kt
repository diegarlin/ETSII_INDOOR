package org.altbeacon.beaconreference

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import ApiClientRegistros
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.utils.HabitacionAdapter

class HabitacionPorLetraActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var letraEditText: EditText
    private lateinit var buscarButton: Button
    private lateinit var emptyTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habitacion_por_letra)

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

            if (letra.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClientRegistros.getPersonasPorHabitacionPorLetra(letra)

                        if (response.isSuccessful && response.body() != null) {
                            val habitaciones = response.body()!!

                            runOnUiThread {
                                if (habitaciones.isEmpty()) {
                                    listView.visibility = View.GONE
                                    emptyTextView.visibility = View.VISIBLE
                                } else {
                                    listView.visibility = View.VISIBLE
                                    emptyTextView.visibility = View.GONE
                                    listView.adapter = HabitacionAdapter(this@HabitacionPorLetraActivity, habitaciones)
                                }
                            }
                        } else {
                            Log.d("api",response.toString() )
                            runOnUiThread {
                                Toast.makeText(this@HabitacionPorLetraActivity, "Error al obtener los registros", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        runOnUiThread {
                            Toast.makeText(this@HabitacionPorLetraActivity, "Vuelve a probar dentro de 1 minuto", Toast.LENGTH_SHORT).show()
                        }
                    }catch(e: Exception){
                        runOnUiThread {
                            Log.d("api","Exception: "+ e.toString())
                            Toast.makeText(
                                this@HabitacionPorLetraActivity,
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
            } else {
                Toast.makeText(this, "Por favor, introduce una letra", Toast.LENGTH_SHORT).show()
            }
        }
    }
}