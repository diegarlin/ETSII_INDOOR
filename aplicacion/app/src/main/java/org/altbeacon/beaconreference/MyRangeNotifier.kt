package org.altbeacon.beaconreference

import android.content.Context
import android.util.Log
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region

class MyRangeNotifier(private val context: Context) : RangeNotifier {
    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        val beaconCount = beacons?.size ?: 0
        Log.d("RANGE", "Number of beacons detected: $beaconCount")

        beacons?.forEach { beacon ->
            val distance = beacon.distance
            if (distance < 0.3) {
                Log.d("RANGE", "${beacon.id1} distance: ${beacon.distance}")
            }
        }
    }
}
