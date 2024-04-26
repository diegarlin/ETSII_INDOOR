import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.altbeacon.beacon.Beacon
import java.util.concurrent.ConcurrentHashMap
import android.os.Handler
import android.os.Looper
import org.altbeacon.beaconreference.BeaconReferenceApplication

class BeaconTracker{
    private val beaconRecords = ConcurrentHashMap<String, MutableList<Double>>()
    private val handler = Handler(Looper.getMainLooper())
    private var closestBeacon = "";
    private val RECORD_THRESHOLD = 6 //IMPORTANTE CAMBIAR CUANDO CAMBIE EL TIEMPO DE RANGEO Y DE UPDATEROOMRECODS

    fun addBeaconRecord(beacon: Beacon) {
        val beaconId = beacon.id1.toString()
        val distance = beacon.distance

        if (!beaconRecords.containsKey(beaconId)) {
            beaconRecords[beaconId] = mutableListOf()
        }
        beaconRecords[beaconId]?.add(distance)
        Log.d("BeaconTracker", "${beaconRecords[beaconId]?.size} records for beacon $beaconId with distance $distance")
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
                val deviceID = BeaconReferenceApplication.deviceID
                if (newclosestBeacon != closestBeacon) {
                    if(newclosestBeacon.equals("")){
                        //Anadir registro de salida
                        val registroSalida = Registro(beacon = closestBeacon, tipo = "salida", deviceID = deviceID)
                        val response = ApiClientRegistros.createRegistro(registroSalida)
                        if (response.isSuccessful) {
                            Log.d("BeaconTracker", "Registro de salida creado exitosamente")
                        } else {
                            Log.d("BeaconTracker", "Error al crear registro de salida")
                        }
                    }else{
                        // Si he entrado en la A0.12 depsués de estar en la A0.11 tengo que hacer el de salida de la A0.11
                        // y el de entrada en la A0.12
                        if(closestBeacon != ""){
                            val registroSalida = Registro(beacon = closestBeacon, tipo = "salida", deviceID = deviceID)
                            val response = ApiClientRegistros.createRegistro(registroSalida)
                            if (response.isSuccessful) {
                                Log.d("BeaconTracker", "Registro de salida creado exitosamente")
                            } else {
                                Log.d("BeaconTracker", "Error al crear registro de salida")
                            }
                        }
                        //Anadir registro de entrada
                        val registroEntrada = Registro(beacon = newclosestBeacon, tipo = "entrada", deviceID = deviceID)
                        val response = ApiClientRegistros.createRegistro(registroEntrada)
                        if (response.isSuccessful) {
                            Log.d("BeaconTracker", "Registro de entrada creado exitosamente")
                        } else {
                            Log.d("BeaconTracker", "Error al crear registro de entrada")
                        }
                    }
                    Log.d("BeaconTracker", "Antiguo: $closestBeacon, Nuevo: $newclosestBeacon")
                    closestBeacon = newclosestBeacon
                }else{
                    Log.d("BeaconTracker", "Sigues en la misma habitación con beaconID: $closestBeacon ")
                }

                beaconRecords.clear()
            } catch (e: Exception) {
                Log.e("BeaconTracker", "Error al actualizar los registros de la sala", e)
            }
        }
    }
}