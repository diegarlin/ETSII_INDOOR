package org.altbeacon.beaconreference

import ApiClientRegistros
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.utils.HabitacionAdapter

class PersonasPorHabitacionActivity : Activity() {
    private lateinit var listView: ListView
    private lateinit var emptyTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personas_por_habitacion)

        listView = findViewById(R.id.listView)
        emptyTextView = findViewById(R.id.emptyTextView)
        progressBar = findViewById(R.id.progressBar)

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }
            try {
                val response = ApiClientRegistros.getPersonasPorHabitacion()

                if (response.isSuccessful && response.body() != null) {
                    val habitaciones = response.body()!!
                    runOnUiThread {
                        if (habitaciones.isEmpty()) {
                            listView.visibility = View.GONE
                            emptyTextView.visibility = View.VISIBLE
                        } else {
                            listView.visibility = View.VISIBLE
                            emptyTextView.visibility = View.GONE
                            listView.adapter = HabitacionAdapter(this@PersonasPorHabitacionActivity, habitaciones)
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.d("api",response.toString() )
                        Toast.makeText(this@PersonasPorHabitacionActivity, "Error al obtener los registros", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this@PersonasPorHabitacionActivity, "Vuelve a probar dentro de 1 minuto", Toast.LENGTH_SHORT).show()
                }
            } catch(e: Exception){
                runOnUiThread {
                    Log.d("api","Exception: "+ e.toString())
                    Toast.makeText(
                        this@PersonasPorHabitacionActivity,
                        "Error al obtener los registros. Contacte con el administrador",
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
}