package com.dndassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import com.dndassistant.databinding.ActivityMainBinding
import com.dndassistant.ui.CharacterCreationDialog
import com.dndassistant.ui.SerialMessage
import com.dndassistant.ui.characterCreation.CharacterCreationArgs
import com.dndassistant.ui.processingAnimation
import com.dndassistant.ui.showSnackbar
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity(), CharacterCreationDialog.CharacterCreationDialogListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    val TAG: String = "mainActivity"
    private val PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION: Int = 1001

    private val connectionsClient by lazy {
        Nearby.getConnectionsClient(this)
    }
    private val SERVICE_ID = "com.dndassistant.nearby"

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ){ permissions ->
            permissions.forEach { (permission, granted) ->
                Log.d("Permissions: ", "$permission granted: $granted")
            }
        }

    private val connectingDone = MutableStateFlow(false)

    private val connectedClients = mutableMapOf<String, String>()

    private var connectedHost = Pair<String, String>("", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_character, R.id.nav_battle
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val headerView = navView.getHeaderView(0)
//        val headerView = layoutInflater.inflate(R.layout.nav_header_main, navView, false)
//        navView.addView(headerView)
        val addButton = headerView.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.floatingActionButton)

        addButton.setOnClickListener {
            showCharacterCreationDialog()
//            drawerLayout.closeDrawer(GravityCompat.START)
        }

        val modeSwitch = findViewById<SwitchCompat>(R.id.accessSwitch)
        val hostButton = findViewById<ToggleButton>(R.id.hosting_button)
        val discoveryButton = findViewById<ToggleButton>(R.id.discovery_button)
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                permission()
                hostButton.setEnabled(true)
                discoveryButton.setEnabled(true)
            } else {
                //To do on switch off
                stopAll()
                hostButton.isChecked = false
                hostButton.setEnabled(false)
                discoveryButton.isChecked = false
                discoveryButton.setEnabled(false)
            }
        }

        hostButton.setOnCheckedChangeListener { _, isChecked ->
            val hostingInfoText = findViewById<TextView>(R.id.hosting_info)
            if (isChecked){
                hostingInfoText.processingAnimation("Advertising", connectingDone)
                startAdvertising()
            } else {


                connectingDone.value = true
                hostingInfoText.text = buildString { append("Not hosting") }
                stopAll()
            }
        }

        discoveryButton.setOnCheckedChangeListener { _, isChecked ->
            val discoveryInfoText = findViewById<TextView>(R.id.discovery_info)
            if (isChecked){
                discoveryInfoText.processingAnimation("Connecting", connectingDone)
                startDiscovery()
            } else {


                connectingDone.value = true
                discoveryInfoText.text = buildString { append("Not discovering") }
                stopAll()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showCharacterCreationDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_create_character_name, null)

        builder
            .setView(view)
//            .setTitle("Enter Name")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("OK", null)
            val dialog = builder.create()

            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                okButton.setOnClickListener {
                    val text = view.findViewById<EditText>(R.id.enter_name_field).text
                    if (text?.toString()?.trim().isNullOrEmpty() ){
                        Toast.makeText(
                            this,
                            "Provide the Name",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else {
                        dialog.dismiss()
                        CharacterCreationDialog(text.toString()).show(
                            supportFragmentManager,
                            "CharacterCreationDialog"
                        )
                    }
                }
            }
        dialog.show()
    }

    override fun CharacterCreationDialogSubmit(name: String, chLevel: Int, chClass: String, chSubclass: String){
        findNavController(R.id.nav_host_fragment_content_main).navigateToCharacterCreation(name, chLevel, chClass, chSubclass)
    }

    fun NavController.navigateToCharacterCreation(chName: String, chLevel: Int, chClass: String, chSubclass: String){
        val args = CharacterCreationArgs(chName, chLevel, chClass, chSubclass)
        val bundle = args.toBundle()
        navigate(R.id.character_creation, bundle)
    }

    fun closeDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
    }

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

        return if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                neededPermissions.toTypedArray(),
                3001
            )
            false
        } else {
            true
        }
    }

    private fun startWifiDirectService(){

    }

    private fun permission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        )
//        when {
//            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED -> {
//                Log.d(TAG, "Bluetooth permission granted")
//            }
//
//            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH) -> {
//                //Additional rationale should be displayed
//                binding.root.showSnackbar("BLUETOOTH", Snackbar.LENGTH_INDEFINITE, "OK"){
//                    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
//                }
//            }
//            else -> {
//                //Permission has not be asked yet
//                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
//            }
//        }
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isEmpty()){
            Log.d("Permission: ", "All permissions already granted")
            return
        }
        val needRationale = toRequest.any(){
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
        if (needRationale){
            binding.root.showSnackbar(
                "Permission are required for Nearby Connections",
                Snackbar.LENGTH_INDEFINITE,
                "OK"
            ){
                requestPermissionLauncher.launch(toRequest.toTypedArray())
            }
        }else{
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Log.e(TAG, "Permission is not granted: $permissions")
                    //To do on permission denied
                }
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {      //Universal connection callback
        lateinit var endpointInfo: ConnectionInfo

        override fun onConnectionInitiated(endpoint: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated from ${info.endpointName}")
            connectionsClient.acceptConnection(endpoint, payloadCallback)
            endpointInfo = info

        }

        override fun onConnectionResult(endpoint: String, result: ConnectionResolution) {
            connectingDone.value = true

            when (result.status.statusCode){
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "Connected to $endpoint")
                    findViewById<TextView>(R.id.connection_info).text = "Connection established"
                    if (connectedHost == Pair("","")){
                        connectedClients[endpoint] = endpointInfo.endpointName
                        writeConnectedClients(connectedClients.values.toList())
                    } else {
                        requestConnectedClients()
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "Rejected")
                    findViewById<TextView>(R.id.connection_info).text = "Connection rejected"
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e(TAG, "Error connecting")
                    findViewById<TextView>(R.id.connection_info).text = "Connection error"
                }
            }
        }

        override fun onDisconnected(endpoint: String) {
            Log.d(TAG, "Disconnected from $endpoint")

            if (connectedHost == Pair("","")){
                findViewById<TextView>(R.id.connection_info).text = "Disconnected"
                connectedClients.remove(endpoint)
                writeConnectedClients(connectedClients.values.toList())
            } else {
                findViewById<TextView>(R.id.connection_info).text = "Client disconnected"
            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback(){   //Client searching for host
        override fun onEndpointFound(endpoint: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Found endpoint ${info.endpointName}")
            connectedHost = Pair(endpoint, info.endpointName)
            findViewById<TextView>(R.id.host_name).text = info.endpointName
            val name = "Player"
            connectionsClient.requestConnection(name, endpoint, connectionLifecycleCallback)
            connectingDone.value = true
            findViewById<TextView>(R.id.your_name).text = name
        }

        override fun onEndpointLost(endpoint: String) {
            Log.d(TAG, "Lost endpoint $endpoint")
            findViewById<TextView>(R.id.connection_info).text = "Connection lost"
            if (connectedHost == Pair("","")){
                connectedClients.remove(endpoint)
                writeConnectedClients(connectedClients.values.toList())
            }
        }
    }

    private val payloadCallback = object : PayloadCallback(){   //Universal payload receiver
        override fun onPayloadReceived(endpoint: String, payload: Payload) {
            val text = payload.asBytes()?.toString(Charsets.UTF_8) ?: return
            val temp = payload
            val temp1 = payload.asBytes()
            Log.d(TAG, "Received data: $text")
            findViewById<TextView>(R.id.connection_info).text = "Data received"

            if (text.isBlank() || !text.trimStart().startsWith("{") || text == "{}"){
                Log.w(TAG, "Ignoring non-JSON payload")
                return
            } else {
                val msg = Json.decodeFromString<SerialMessage>(text)
                when (msg) {
                    is SerialMessage.RequestList -> broadcastConnectedList()
                    is SerialMessage.ConnectedList -> writeConnectedClients(msg.clients)
                    is SerialMessage.RequestBattleState -> {}
                    is SerialMessage.BattleState -> {}
                }
            }
        }

        override fun onPayloadTransferUpdate(endpoint: String, update: PayloadTransferUpdate) {

        }
    }

    fun startAdvertising() {
        val options = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_STAR)
            .build()
        val name = "HostDevice"

        connectionsClient.startAdvertising(name, SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener {
                Log.d(TAG, "Advertising started")
                findViewById<TextView>(R.id.host_name).text = name
                findViewById<TextView>(R.id.your_name).text = name
            }.addOnFailureListener {
                Log.e(TAG, "Advertising failed: ${it.message}")
                findViewById<TextView>(R.id.connection_info).text = "Advertising failed: ${it.message}"
            }
    }

    fun startDiscovery(){
        val options = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_STAR)
            .build()

        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnSuccessListener {
                Log.d(TAG, "Discovery started")

            }.addOnFailureListener {
                Log.e(TAG, "Discovery failed: ${it.message}")
                findViewById<TextView>(R.id.connection_info).text = "Discovery failed: ${it.message}"
            }
    }

    fun sendMessage(endpoint: String, message: String){
        val payload = Payload.fromBytes(message.toByteArray())
        connectionsClient.sendPayload(endpoint, payload)
    }

    fun broadcastConnectedList(){
        val data = connectedClients.values.toList()
//        val bytes = Json.encodeToString(data).toByteArray()
        val payload = Json.encodeToString<SerialMessage>(SerialMessage.ConnectedList(data)).toByteArray()

        connectionsClient.sendPayload(connectedClients.keys.toList(), Payload.fromBytes(payload))
    }

    fun requestConnectedClients() {
//        val message = "REQUEST_CONNECTED_LIST".toByteArray()
        val json = Json.encodeToString<SerialMessage>(SerialMessage.RequestList)
        connectionsClient.sendPayload(connectedHost.first, Payload.fromBytes(json.toByteArray()))
    }

    fun stopAll(){
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        findViewById<TextView>(R.id.host_name).text = "---"
        findViewById<TextView>(R.id.your_name).text = "---"
        findViewById<TextView>(R.id.other_names).text = "---"
        findViewById<TextView>(R.id.connection_info).text = "Connectivity off"
        connectingDone.value = false
    }

    fun writeConnectedClients(connectedList: List<String>){
        findViewById<TextView>(R.id.other_names).text = connectedList.joinToString("\n")
    }
}