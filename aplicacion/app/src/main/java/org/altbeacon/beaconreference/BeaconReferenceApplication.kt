package org.altbeacon.beaconreference

import BeaconTracker
import MyMonitorNotifier
import SharedPreferencesManager
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.RegionViewModel
import android.provider.Settings


class BeaconReferenceApplication: Application() {
    // the region definition is a wildcard that matches all beacons regardless of identifiers.
    // if you only want to detect beacons with a specific UUID, change the id1 paremeter to
    // a UUID like Identifier.parse("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6")

    var region = Region("all-beacons", null, null, null)
    lateinit var beaconTracker: BeaconTracker // Debe ser private



    override fun onCreate() {
        super.onCreate()
        SharedPreferencesManager.init(this)
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        Log.d("deviceid", deviceID)
        BeaconManager.setDebug(true)
        beaconManager.setEnableScheduledScanJobs(false);
        //Escaneo cada 10 segundos cuando está en background y tarda en escanear 1 segundo
        beaconManager.foregroundBetweenScanPeriod = 5000; // 5 segundos
        beaconManager.foregroundScanPeriod = 1000; // 1 segundo
        beaconManager.backgroundBetweenScanPeriod=10000;
        beaconManager.backgroundScanPeriod = 1000L;
        // Solo busco iBeacons
        beaconManager.getBeaconParsers().clear();
        val parser = BeaconParser().
        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        beaconManager.getBeaconParsers().add(
            parser)

        //Empiezo a rangear y empiezo la actividad de actualizar el beacon más cercano cada minuto
        setupBeaconScanning()
        beaconTracker = BeaconTracker()
        beaconTracker.startUpdatingRoomRecords()

        //Dejo de monitorear todas las regiones que había anteriormente y comienzo a monitorear todas las definidas por si acaso
        for (region in beaconManager.monitoredRegions) {
            beaconManager.stopMonitoring(region)
            beaconManager.stopRangingBeacons(region)
        }

        beaconManager.startMonitoring(region)
        beaconManager.startRangingBeacons(region)


//        val myRangeNotifier = MyRangeNotifier(this)
//        val myMonitorNotifier = MyMonitorNotifier(this)

//        beaconManager.addRangeNotifier(myRangeNotifier)
//        beaconManager.addMonitorNotifier(myMonitorNotifier)
    }
    fun setupBeaconScanning() {
        val beaconManager = BeaconManager.getInstanceForApplication(this)


        try {
            setupForegroundService()
        }
        catch (e: SecurityException) {

            Log.d(TAG, "Not setting up foreground service scanning until location permission granted by user")
            return
        }

        // These two lines set up a Live Data observer so this Activity can get beacon data from the Application class
        val regionViewModel = BeaconManager.getInstanceForApplication(this).getRegionViewModel(region)
//        val regionViewModel1 = BeaconManager.getInstanceForApplication(this).getRegionViewModel(region1)
//        val regionViewModel2 = BeaconManager.getInstanceForApplication(this).getRegionViewModel(region2)
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observeForever( centralMonitoringObserver)
//        regionViewModel1.regionState.observeForever( centralMonitoringObserver)
//        regionViewModel2.regionState.observeForever( centralMonitoringObserver)
        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observeForever( centralRangingObserver)
//        regionViewModel1.rangedBeacons.observeForever( centralRangingObserver)
//        regionViewModel2.rangedBeacons.observeForever( centralRangingObserver)


    }

    fun setupForegroundService() {
        val builder = Notification.Builder(this, "BeaconReferenceApp")
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder.setContentTitle("Scanning for Beacons")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent);
        val channel =  NotificationChannel("beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setDescription("My Notification Channel Description")
        val notificationManager =  getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(channel.getId());
        Log.d(TAG, "Calling enableForegroundServiceScanning")
        BeaconManager.getInstanceForApplication(this).enableForegroundServiceScanning(builder.build(), 456);
        Log.d(TAG, "Back from  enableForegroundServiceScanning")
    }

    val centralMonitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.OUTSIDE) {
            Log.d(TAG, "outside beacon region: "+region)
        }
        else {
            Log.d(TAG, "inside beacon region: "+region)
//            sendNotification()
        }
    }

    val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
        val rangeAgeMillis = System.currentTimeMillis() - (beacons.firstOrNull()?.lastCycleDetectionTimestamp ?: 0)
        if (rangeAgeMillis < 10000) {
            Log.d("RangingObserver", "Ranged: ${beacons.count()} beacons")
            for (beacon: Beacon in beacons) {
                Log.d("RangingObserver", "$beacon about ${beacon.distance} meters away")
                beaconTracker.addBeaconRecord(beacon)
            }
        }
        else {
            Log.d(MainActivity.TAG, "Ignoring stale ranged beacons from $rangeAgeMillis millis ago")
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this, "beacon-ref-notification-id")
            .setContentTitle("Beacon Reference Application")
            .setContentText("A beacon is nearby.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(resultPendingIntent)
        val channel =  NotificationChannel("beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setDescription("My Notification Channel Description")
        val notificationManager =  getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(channel.getId());
        notificationManager.notify(1, builder.build())
    }

    companion object {
        val TAG = "BeaconReference"
        lateinit var deviceID: String
    }

}