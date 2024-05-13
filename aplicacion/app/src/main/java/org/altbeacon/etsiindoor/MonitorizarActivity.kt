package org.altbeacon.etsiindoor

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier

class MonitorizarActivity : AppCompatActivity() {
    lateinit var beaconListView: ListView
    lateinit var beaconCountTextView: TextView
    lateinit var monitoringButton: Button
    lateinit var rangingButton: Button
    lateinit var ETSIINDOOR: ETSIINDOOR
    var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ETSIINDOOR = application as ETSIINDOOR

        // Set up a Live Data observer for beacon data
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        val regionViewModel = beaconManager.getRegionViewModel(ETSIINDOOR.region)
//        val regionViewModel1 = beaconManager.getRegionViewModel(ETSIINDOOR.region1)
//        val regionViewModel2 = beaconManager.getRegionViewModel(ETSIINDOOR.region2)

        // Observer will be called each time the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observe(this, monitoringObserver)
//        regionViewModel1.regionState.observe(this, monitoringObserver)
//        regionViewModel2.regionState.observe(this, monitoringObserver)

        // Observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observe(this, rangingObserver)
//        regionViewModel1.rangedBeacons.observe(this, rangingObserver)
//        regionViewModel2.rangedBeacons.observe(this, rangingObserver)

        rangingButton = findViewById<Button>(R.id.rangingButton)
        monitoringButton = findViewById<Button>(R.id.monitoringButton)
        beaconListView = findViewById<ListView>(R.id.beaconList)
        beaconCountTextView = findViewById<TextView>(R.id.beaconCount)
        beaconCountTextView.text = "No beacons detected"
        beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }
    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        // You MUST make sure the following dynamic permissions are granted by the user to detect beacons
        //
        //    Manifest.permission.BLUETOOTH_SCAN
        //    Manifest.permission.BLUETOOTH_CONNECT
        //    Manifest.permission.ACCESS_FINE_LOCATION
        //    Manifest.permission.ACCESS_BACKGROUND_LOCATION // only needed to detect in background
        //
        // The code needed to get these permissions has become increasingly complex, so it is in
        // its own file so as not to clutter this file focussed on how to use the library.


    }

    val monitoringObserver = Observer<Int> { state ->
        var dialogTitle = "Beacons detected"
        var dialogMessage = "didEnterRegionEvent has fired"
        var stateString = "inside"
        when(state) {
            MonitorNotifier.OUTSIDE -> {
                dialogTitle = "No beacons detected"
                dialogMessage = "didExitRegionEvent has fired"
                stateString = "outside"
                beaconCountTextView.text = "Outside of the beacon region -- no beacons detected"
                beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
            }
            else -> {
                beaconCountTextView.text = "Inside the beacon region."
            }
        }
        Log.d(TAG, "monitoring state changed to : $stateString")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(android.R.string.ok, null)
        alertDialog?.dismiss()
        alertDialog = builder.create()
        alertDialog?.show()
    }


    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(TAG, "Ranged: ${beacons.count()} beacons")
        if (BeaconManager.getInstanceForApplication(this).rangedRegions.size > 0) {
            beaconCountTextView.text = "Ranging enabled: ${beacons.count()} beacon(s) detected"
            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                beacons
                    .sortedBy { it.distance }
                    .map { "UUID:${it.id1}\n MAJOR: ${it.id2} MINOR:${it.id3}  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray())

        }
    }

    fun rangingButtonTapped(view: View) {
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.rangedRegions.size == 0) {
            beaconManager.startRangingBeacons(ETSIINDOOR.region)
            rangingButton.text = "Stop Ranging"
            beaconCountTextView.text = "Ranging enabled -- awaiting first callback"
        }
        else {
            beaconManager.stopRangingBeacons(ETSIINDOOR.region)
            rangingButton.text = "Start Ranging"
            beaconCountTextView.text = "Ranging disabled -- no beacons detected"
            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }
    }

    fun monitoringButtonTapped(view: View) {
        var dialogTitle = ""
        var dialogMessage = ""
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.monitoredRegions.size == 0) {
            beaconManager.startMonitoring(ETSIINDOOR.region)
            dialogTitle = "Beacon monitoring started."
            dialogMessage = "You will see a dialog if a beacon is detected, and another if beacons then stop being detected."
            monitoringButton.text = "Stop Monitoring"

        }
        else {
            beaconManager.stopMonitoring(ETSIINDOOR.region)
            dialogTitle = "Beacon monitoring stopped."
            dialogMessage = "You will no longer see dialogs when beacons start/stop being detected."
            monitoringButton.text = "Start Monitoring"
        }
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(android.R.string.ok, null)
        alertDialog?.dismiss()
        alertDialog = builder.create()
        alertDialog?.show()

    }

    companion object {
        val TAG = "MonitorizarActivity"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }
}