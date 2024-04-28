package org.altbeacon.beaconreference

import ApiClientRegistros
import android.app.Activity
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.utils.HabitacionAdapter

class PersonasPorHabitacionActivity : Activity() {
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personas_por_habitacion)

        listView = findViewById(R.id.listView)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClientRegistros.getPersonasPorHabitacion()

            if (response.isSuccessful && response.body() != null) {
                val habitaciones = response.body()!!
                runOnUiThread {
                    listView.adapter = HabitacionAdapter(this@PersonasPorHabitacionActivity, habitaciones)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@PersonasPorHabitacionActivity, "Error al obtener los registros", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}