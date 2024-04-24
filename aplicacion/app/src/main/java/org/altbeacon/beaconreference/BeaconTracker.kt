import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.api.ApiClient
import org.altbeacon.beacon.Beacon
import java.util.concurrent.ConcurrentHashMap
import android.os.Handler
import android.os.Looper
class BeaconTracker{
    private val beaconRecords = ConcurrentHashMap<String, MutableList<Double>>()
    private val handler = Handler(Looper.getMainLooper())
    private var closestBeacon = "";
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
                //Lo hace cada minuto
                handler.postDelayed(this, 1 * 60 * 1000) //IMPORTANTE CAMBIAR A 10 MINUTOS
            }
        }
        handler.post(runnable)
    }
    fun updateRoomRecords() {// Debe ser private
        GlobalScope.launch(Dispatchers.IO) {
            var newclosestBeacon = getClosestBeacon()

            if (newclosestBeacon != closestBeacon) {
                if(newclosestBeacon.equals("")){
                    //tipo salida
                }else{
                    //tipo entrada
                }
                Log.d("BeaconTracker", "Antiguo: $closestBeacon, Nuevo: $newclosestBeacon")
                closestBeacon = newclosestBeacon
            }else{
                Log.d("BeaconTracker", "Beacon $closestBeacon is the closest beacon")
            }

            beaconRecords.clear()

        }
    }
}