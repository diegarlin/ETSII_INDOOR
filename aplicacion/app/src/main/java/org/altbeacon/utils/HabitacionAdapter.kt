package org.altbeacon.utils

import ApiServiceRegistros.Habitacion
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.altbeacon.etsiindoor.R

class HabitacionAdapter(private val context: Context, private val dataSource: List<Habitacion>) :
    BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.habitacion_adapter, parent, false)

        val habitacionTextView: TextView = rowView.findViewById(R.id.habitacionTextView)
        val numPersonasTextView: TextView = rowView.findViewById(R.id.numPersonasTextView)

        val habitacion = getItem(position) as Habitacion

        habitacionTextView.text = habitacion.habitacion
        numPersonasTextView.text = habitacion.numPersonas.toString()

        return rowView
    }
}