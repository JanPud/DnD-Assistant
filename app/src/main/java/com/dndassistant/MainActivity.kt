package com.dndassistant

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
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
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import com.dndassistant.databinding.ActivityMainBinding
import com.dndassistant.ui.CharacterCreationDialog
import com.dndassistant.ui.home.HomeFragment
import com.dndassistant.R
import com.dndassistant.ui.characterCreation.CharacterCreationArgs

class MainActivity : AppCompatActivity(), CharacterCreationDialog.CharacterCreationDialogListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_battle
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
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
//                startWifiDirectService()
            } else {
//                stopService(Intent(this, WifiDirectActivity::class.java))
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
        if (ensureLocationPermission()){
        }
    }
}