package org.altbeacon.beaconreference

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.utils.HabitacionAdapter
import ApiClientRegistros
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class HistoricoPersonas : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var habitacionEditText: EditText
    private lateinit var fechaInicioTextView: TextView
    private lateinit var fechaFinTextView: TextView
    private lateinit var buscarButton: Button
    private lateinit var emptyTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_personas)

        listView = findViewById(R.id.listView)
        habitacionEditText = findViewById(R.id.habitacionEditText)
        fechaInicioTextView = findViewById(R.id.fechaInicioTextView)
        fechaFinTextView = findViewById(R.id.fechaFinTextView)
        buscarButton = findViewById(R.id.buscarButton)
        emptyTextView = findViewById(R.id.emptyTextView)
        progressBar = findViewById(R.id.progressBar)

        fechaInicioTextView.setOnClickListener {
            showJodaDateTimePicker(fechaInicioTextView)
        }

        fechaFinTextView.setOnClickListener {
            showJodaDateTimePicker(fechaFinTextView)
        }

        buscarButton.setOnClickListener {
            val habitacion = habitacionEditText.text.toString()
            val fechaInicio = fechaInicioTextView.text.toString()
            val fechaFin = fechaFinTextView.text.toString()

            if (emptyTextView.visibility == View.VISIBLE) {
                emptyTextView.visibility = View.GONE
            }

            if (fechaInicio.isNotEmpty() && fechaFin.isNotEmpty()) {
                if (!isFechaInicioBeforeFechaFin(fechaInicio, fechaFin)) {
                    Toast.makeText(this, "La fecha de inicio debe ser anterior a la fecha de fin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isFechaNotInFuture(fechaInicio) || !isFechaNotInFuture(fechaFin)) {
                    Toast.makeText(this, "Las fechas no pueden ser posteriores a la fecha y hora actual", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                progressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClientRegistros.getPersonasPorHabitacionFecha(habitacion, fechaInicio, fechaFin)

                        if (response.isSuccessful && response.body() != null) {
                            val habitaciones = response.body()!!

                            runOnUiThread {
                                if (habitaciones.isEmpty()) {
                                    listView.visibility = View.GONE
                                    emptyTextView.visibility = View.VISIBLE
                                } else {
                                    listView.visibility = View.VISIBLE
                                    emptyTextView.visibility = View.GONE
                                    listView.adapter = HabitacionAdapter(this@HistoricoPersonas, habitaciones)
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@HistoricoPersonas, "Error al obtener los registros", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@HistoricoPersonas,
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
                Toast.makeText(this, "Por favor, introduce las fechas de inicio y fin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showJodaDateTimePicker(textView: TextView) {
        val now = DateTime()

        val datePickerDialog = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                val timePickerDialog = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
                    { _, hourOfDay, minute, _ ->
                        val selectedDateTime = DateTime(year, monthOfYear + 1, dayOfMonth, hourOfDay, minute)
                        textView.setText(selectedDateTime.toString("yyyy-MM-dd HH:mm"))
                    },
                    now.hourOfDay,
                    now.minuteOfHour,
                    true
                )
                timePickerDialog.show(supportFragmentManager, "TimePickerDialog")
            },
            now.year,
            now.monthOfYear - 1,
            now.dayOfMonth
        )
        datePickerDialog.show(supportFragmentManager, "DatePickerDialog")
    }

    private fun isFechaInicioBeforeFechaFin(fechaInicio: String, fechaFin: String): Boolean {
        val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
        val fechaInicioDateTime = format.parseDateTime(fechaInicio)
        val fechaFinDateTime = format.parseDateTime(fechaFin)

        return fechaInicioDateTime.isBefore(fechaFinDateTime)
    }

    private fun isFechaNotInFuture(fecha: String): Boolean {
        val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
        val fechaDateTime = format.parseDateTime(fecha)
        val now = DateTime()

        return !fechaDateTime.isAfter(now)
    }
}