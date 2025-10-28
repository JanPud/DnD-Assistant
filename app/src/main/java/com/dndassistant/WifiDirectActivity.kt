package com.dndassistant

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket

class WifiDirectActivity : Service() {

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers.isNotEmpty()){
            for (device in refreshedPeers){
                Log.d("Wifi Direct", "Found peer: ${device.deviceName} - ${device.deviceAddress}")
            }
            Toast.makeText(this, "Found ${refreshedPeers.size} peers", Toast.LENGTH_SHORT).show()
        }else{
            Log.d("Wifi Direct", "No peers found")
        }
    }

    override fun onCreate() {
        super.onCreate()
//        setContentView(null)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

//        receiver = WifiDirectReceiver(manager, channel, this)
        receiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent?) {
                Log.d("WiFiDirect", "Received action: ${intent?.action}")
                when (intent?.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
//                        activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                        Log.d("WiFiDirect", "P2P state: $state")
                    }

                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.NEARBY_WIFI_DEVICES
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        Log.d("WiFiDirect", "Peers changed, requesting list")
                        manager.requestPeers(channel, peerListListener)
                    }

                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION ->{
                        Log.d("WiFiDirect", "Connection changed")
                        val info = intent.parcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                        if (info?.isConnected == true){
                            manager.requestConnectionInfo(channel){ connectionInfo ->
                                if (connectionInfo.groupFormed && connectionInfo.isGroupOwner){
                                    ServerAsyncTask().execute()
                                } else if (connectionInfo.groupFormed) {
                                    ClientAsyncTask(connectionInfo.groupOwnerAddress.hostAddress).execute()
                                }
                            }
                        }
                    }

                    WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                        Log.d("WiFiDirect", "This device changed")
//                (activity.supportFragmentManager.)
                    }
                }
            }
        }
        registerReceiver(receiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Handler(Looper.getMainLooper()).postDelayed({
            beginPeerDiscovery()
        }, 1000)
        return START_NOT_STICKY
    }

    private fun startPeerDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permission NEARBY_WIFI_DEVICES required", Toast.LENGTH_SHORT).show()
                return
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ){
            Toast.makeText(this, "Permission ACCESS_FINE_LOCATION required", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.requestP2pState(channel) { isAvailable ->
                if(isAvailable == 0){
                    Toast.makeText(this, "WiFi Direct is not supported or disabled", Toast.LENGTH_SHORT).show()
                    return@requestP2pState
                }
            }
        }
//        if (!isLocationEnabled(this)){
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(intent)
//        }
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@WifiDirectActivity, "Discovery started", Toast.LENGTH_SHORT).show()
                Log.d("WiFiDirect", "Discovery Started")
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@WifiDirectActivity, "Discovery failed: $reason", Toast.LENGTH_SHORT).show()
                Log.e("WiFiDirect", "Discovery failed: $reason")
            }
        })
    }

    private fun beginPeerDiscovery(){
        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) wifiManager.isWifiEnabled = true
        
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener{
            override fun onSuccess() {
                Log.d("WifiDirect", "Previous discovery stopped")
                startPeerDiscovery()
            }

            override fun onFailure(reason: Int) {
                Log.w("WifiDirect", "Failed to stop discovery. Reason: $reason")
                startPeerDiscovery()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureLocationPermission(): Boolean {
        val neededPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return neededPermissions.isEmpty()
    }

    fun connectTo(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
            return
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener{
            override fun onSuccess() {
                Log.d("P2P", "Connecting to ${device.deviceName}")
            }

            override fun onFailure(reason: Int) {
                Log.e("P2P", "Connection failed: $reason")
            }
        })
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key) as? T
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

class WifiDirectReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: WifiDirectActivity) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
//                activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                if (ActivityCompat.checkSelfPermission(
                        this.activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this.activity,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
//                    ensureLocationPermission()
                    return
                }
                manager.requestPeers(channel) { peers ->
                    peers.deviceList.forEach {
                            Log.d("P2P", "Found device: ${it.deviceName} (${it.deviceAddress})")
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION ->{
                val info = intent.parcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                if (info?.isConnected == true){
                    manager.requestConnectionInfo(channel){ connectionInfo ->
                        if (connectionInfo.groupFormed && connectionInfo.isGroupOwner){
                            ServerAsyncTask().execute()
                        } else if (connectionInfo.groupFormed) {
                            ClientAsyncTask(connectionInfo.groupOwnerAddress.hostAddress).execute()
                        }
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
//                (activity.supportFragmentManager.)
            }
        }
    }

    inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key) as? T
        }
    }

    fun discoverPeers(){
        if (ActivityCompat.checkSelfPermission(
                this.activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this.activity,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
//            ensureLocationPermission()
            return
        }
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener{
            override fun onSuccess() {
                Log.d("P2P", "Peer discovery started")
            }

            override fun onFailure(reason: Int) {
                Log.e("P2P", "Peer discovery failed: $reason")
            }
        })
    }

    fun connectTo(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        if (ActivityCompat.checkSelfPermission(
                this.activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this.activity,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
//            ensureLocationPermission()
            return
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener{
            override fun onSuccess() {
                Log.d("P2P", "Connecting to ${device.deviceName}")
            }

            override fun onFailure(reason: Int) {
                Log.e("P2P", "Connection failed: $reason")
            }
        })
    }

}

class ServerAsyncTask : AsyncTask<Void, Void, String>(){
    override fun doInBackground(vararg params: Void?): String {
        val serverSocket = ServerSocket(8888)
        val client = serverSocket.accept()
        val input = BufferedReader(InputStreamReader(client.inputStream))
        val message = input.readLine()
        Log.d("P2P", "Received: $message")
        serverSocket.close()
        return message
    }
}

class ClientAsyncTask(private val host: String?) : AsyncTask<Void, Void, Void>(){
    override fun doInBackground(vararg params: Void?): Void? {
        val socket = Socket(host, 8888)
        val writer = BufferedWriter(OutputStreamWriter(socket.outputStream))
        writer.write("Hello from client\n")
        writer.flush()
        socket.close()
        return null
    }
}