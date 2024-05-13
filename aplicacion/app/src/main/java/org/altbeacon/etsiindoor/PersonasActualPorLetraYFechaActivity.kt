package org.altbeacon.etsiindoor

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
import org.altbeacon.utils.BaseActivity
import org.joda.time.DateTime

class PersonasActualPorLetraYFechaActivity : BaseActivity() {
    private lateinit var listView: ListView
    private lateinit var letraEditText: EditText
    private lateinit var fechaTextView: TextView
    private lateinit var buscarButton: Button
    private lateinit var emptyTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personas_actual_letra_fecha)
        setupToolbar(R.id.toolbar)

        listView = findViewById(R.id.listView)
        letraEditText = findViewById(R.id.letraEditText)
        fechaTextView = findViewById(R.id.fechaTextView)
        buscarButton = findViewById(R.id.buscarButton)
        emptyTextView = findViewById(R.id.emptyTextView)
        progressBar = findViewById(R.id.progressBar)

        fechaTextView.setOnClickListener {
            showJodaDateTimePicker(fechaTextView)
        }


        buscarButton.setOnClickListener {
            val letra = letraEditText.text.toString()
            val fecha = fechaTextView.text.toString()

            if (emptyTextView.visibility == View.VISIBLE) {
                emptyTextView.visibility = View.GONE
            }

            if (fecha.isNotEmpty()) {

                progressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClientRegistros.getPersonasActualPorLetraYFecha(letra, fecha)

                        if (response.isSuccessful && response.body() != null) {
                            val habitaciones = response.body()!!

                            runOnUiThread {
                                if (habitaciones.isEmpty()) {
                                    listView.visibility = View.GONE
                                    emptyTextView.visibility = View.VISIBLE
                                } else {
                                    listView.visibility = View.VISIBLE
                                    emptyTextView.visibility = View.GONE
                                    listView.adapter = HabitacionAdapter(this@PersonasActualPorLetraYFechaActivity, habitaciones)
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@PersonasActualPorLetraYFechaActivity, "Error al obtener los registros", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }catch (e: java.net.SocketTimeoutException) {
                        runOnUiThread {
                            Toast.makeText(this@PersonasActualPorLetraYFechaActivity, "Vuelve a probar dentro de 1 minuto", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@PersonasActualPorLetraYFechaActivity,
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
                Toast.makeText(this, "Por favor, introduce la fecha", Toast.LENGTH_SHORT).show()
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

}