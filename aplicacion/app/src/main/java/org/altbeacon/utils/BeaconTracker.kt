package org.altbeacon.utils

import ApiClientRegistros
import ApiServiceAnyplace
import Registro
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.altbeacon.beacon.Beacon
import org.altbeacon.etsiindoor.ETSIINDOOR
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class BeaconTracker(private val etsiindoor: ETSIINDOOR) {
    private val beaconRecords = ConcurrentHashMap<String, MutableList<Double>>()
    private val handler = Handler(Looper.getMainLooper())
    private var closestBeacon = ""
    private val RECORD_THRESHOLD =
        6 //IMPORTANTE CAMBIAR CUANDO CAMBIE EL TIEMPO DE RANGEO Y DE UPDATEROOMRECODS

    fun addBeaconRecord(beacon: Beacon) {
        val beaconId = beacon.id1.toString()
        val distance = beacon.distance

        if (!beaconRecords.containsKey(beaconId)) {
            beaconRecords[beaconId] = mutableListOf()
        }
        beaconRecords[beaconId]?.add(distance)
        Log.d(
            "org.altbeacon.utils.BeaconTracker",
            "${beaconRecords[beaconId]?.size} records for beacon $beaconId with distance $distance"
        )
    }

    private fun calculateAverageDistance(beaconId: String): Double {
        val distances = beaconRecords[beaconId] ?: return Double.MAX_VALUE
        return distances.sum() / distances.size
    }

    private fun getClosestBeacon(): String {
        var closestBeaconId = ""
        var smallestAverageDistance = Double.MAX_VALUE
        for ((beaconId, _) in beaconRecords) {
            if ((beaconRecords[beaconId]?.size ?: 0) < RECORD_THRESHOLD) {
                beaconRecords.remove(beaconId)
                continue
            }

            val averageDistance = calculateAverageDistance(beaconId)
            if (averageDistance < smallestAverageDistance) {
                closestBeaconId = beaconId
                smallestAverageDistance = averageDistance
            }
        }
        return closestBeaconId
    }

    fun startUpdatingRoomRecords() {
        val runnable = object : Runnable {
            override fun run() {
                updateRoomRecords()
                //Lo hace cada 2 minutos
                handler.postDelayed(this, 2 * 60 * 1000) //IMPORTANTE CAMBIAR A 10 MINUTOS
            }
        }
        handler.post(runnable)
    }

    fun updateRoomRecords() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                var newclosestBeacon = getClosestBeacon()

                val deviceID = ETSIINDOOR.deviceID
                if (newclosestBeacon != closestBeacon) {
                    if (newclosestBeacon.equals("")) {
                        //Anadir registro de salida
                        val registroSalida =
                            Registro(beacon = closestBeacon, tipo = "salida", deviceID = deviceID)
                        val response = ApiClientRegistros.createRegistro(registroSalida)
                        if (response.isSuccessful) {
                            Log.d("org.altbeacon.utils.BeaconTracker", "Registro de salida creado exitosamente")
                            val sala = closestBeacon.takeLast(4).uppercase().replace(Regex("(\\w{2})(\\w{2})"), "$1.$2")
                            updateMap("salida", sala)
                        } else {
                            Log.d("org.altbeacon.utils.BeaconTracker", "Error al crear registro de salida")
                        }
                    } else {
                        // Si he entrado en la A0.12 depsués de estar en la A0.11 tengo que hacer el de salida de la A0.11
                        // y el de entrada en la A0.12
                        if (closestBeacon != "") {
                            val registroSalida = Registro(
                                beacon = closestBeacon,
                                tipo = "salida",
                                deviceID = deviceID
                            )
                            val response = ApiClientRegistros.createRegistro(registroSalida)
                            if (response.isSuccessful) {
                                Log.d("org.altbeacon.utils.BeaconTracker", "Registro de salida creado exitosamente")
                                val sala = closestBeacon.takeLast(4).uppercase().replace(Regex("(\\w{2})(\\w{2})"), "$1.$2")
                                updateMap("salida", sala)
                            } else {
                                Log.d("org.altbeacon.utils.BeaconTracker", "Error al crear registro de salida")
                            }
                        }
                        //Anadir registro de entrada
                        val registroEntrada = Registro(
                            beacon = newclosestBeacon,
                            tipo = "entrada",
                            deviceID = deviceID
                        )
                        val response = ApiClientRegistros.createRegistro(registroEntrada)
                        if (response.isSuccessful) {
                            Log.d("org.altbeacon.utils.BeaconTracker", "Registro de entrada creado exitosamente")
                            val sala = closestBeacon.takeLast(4).uppercase().replace(Regex("(\\w{2})(\\w{2})"), "$1.$2")
                            updateMap("entrada", sala)
                            if (needsSpecialPermission()) {
                                etsiindoor.sendNotification("Necesita permiso especial para entrar en la facultad")
                                Log.d(
                                    "org.altbeacon.utils.BeaconTracker",
                                    "Necesita permiso especial para entrar en la facultad"
                                )
                            }
                        } else {
                            Log.d("org.altbeacon.utils.BeaconTracker", "Error al crear registro de entrada")
                        }
                    }
                    Log.d("org.altbeacon.utils.BeaconTracker", "Antiguo: $closestBeacon, Nuevo: $newclosestBeacon")
                    closestBeacon = newclosestBeacon
                } else {
                    Log.d(
                        "org.altbeacon.utils.BeaconTracker",
                        "Sigues en la misma habitación con beaconID: $closestBeacon "
                    )
                }

                beaconRecords.clear()
            } catch (e: Exception) {
                Log.e("org.altbeacon.utils.BeaconTracker", "Error al actualizar los registros de la sala", e)
            }
        }
    }

    fun needsSpecialPermission(): Boolean {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val month = now.get(Calendar.MONTH)

        // Comprueba si es entre las 23 y las 8
        if (hour < 8 || hour >= 23) {
            return true
        }

        // Comprueba si es fin de semana
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return true
        }

        // Comprueba si es agosto (en Calendar, los meses empiezan desde 0, por lo que agosto es 7)
        if (month == Calendar.AUGUST) {
            return true
        }

        return false
    }



    fun updateMap(registroTipo: String, sala: String) {
        val url_base = "https://ap.cs.ucy.ac.cy:44"

        val search_poi_data = mapOf(
            "cuid" to "cuid_6b0a3e34-9997-462c-2dec-7f0e76cce1ca_1708272924486",
            "letters" to sala,
            "buid" to "building_3f91a8fd-ca7b-4d7e-bc04-626e2623d229_1704557723480",
            "greeklish" to "false"
        )

        val retrofit = Retrofit.Builder()
            .baseUrl(url_base)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiServiceAnyplace::class.java)

        val searchPoiCall = service.searchPoi(search_poi_data)
        val searchPoiResponse = searchPoiCall.execute()

        if (searchPoiResponse.isSuccessful) {
            val data = searchPoiResponse.body()
            val pois = data?.get("pois") as List<Map<String, Any>>
            val poi = pois[0]
            val current_number = (poi["description"] as String).split(":")[1].let { if (it.isNotBlank()) it.trim().toInt() else 0 }
            Log.d("org.altbeacon.utils.BeaconTracker", current_number.toString())
            Log.d("org.altbeacon.utils.BeaconTracker", poi["description"].toString())

            val new_number = if (registroTipo == "entrada") current_number + 1 else current_number - 1
            Log.d("org.altbeacon.utils.BeaconTracker", new_number.toString())

            val new_description = "Número de personas: $new_number"

            val update_poi_data = mapOf(
                "buid" to "building_3f91a8fd-ca7b-4d7e-bc04-626e2623d229_1704557723480",
                "puid" to "poi_9b57957a-332d-43c2-8ddd-4b01fabfc53d",
                "description" to new_description
            )

            val updatePoiCall = service.updatePoi(update_poi_data)
            val updatePoiResponse = updatePoiCall.execute()

            if (updatePoiResponse.isSuccessful) {
                val dataUpdate = updatePoiResponse.body()
                Log.d("org.altbeacon.utils.BeaconTracker", dataUpdate.toString())
            } else {
                Log.d("org.altbeacon.utils.BeaconTracker ", "ERROR EN EL UPDATE")
                Log.d("org.altbeacon.utils.BeaconTracker ", "Código de estado HTTP: ${updatePoiResponse.code()}")
                Log.d("org.altbeacon.utils.BeaconTracker ", "Mensaje de error HTTP: ${updatePoiResponse.errorBody()?.string()}")
            }
        } else {
            Log.d("org.altbeacon.utils.BeaconTracker ", "ERROR EN EL BUSCAR")
        }
    }

}