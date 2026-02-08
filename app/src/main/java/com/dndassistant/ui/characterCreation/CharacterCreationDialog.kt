package com.dndassistant.ui.characterCreation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dndassistant.R

class CharacterCreationDialog(Name: String) : DialogFragment() {

    private val chName = Name
    interface CharacterCreationDialogListener {
        fun CharacterCreationDialogSubmit(name: String, chLevel: Int, chClass: String, chSubclass: String)
    }

    private var listener: CharacterCreationDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? CharacterCreationDialogListener
            ?: throw ClassCastException("$context must implement InputDialogListener")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_create_character, null)

        // Access views inside the dialog
        val levelDropDown = view.findViewById<AutoCompleteTextView>(R.id.character_level)
        val classDropdown = view.findViewById<AutoCompleteTextView>(R.id.character_class)
        val subclassDropDown = view.findViewById<AutoCompleteTextView>(R.id.character_subclass)

        // Setup dropdown adapter
//        val levelList = List(20) { "Level ${it + 1}" }
        val levelList = List(20) { getString(R.string.level) + " ${it + 1}" }
        val adapterLevel = ArrayAdapter(activity, android.R.layout.simple_list_item_1, levelList)
        levelDropDown.setAdapter(adapterLevel)

        val classList = listOf(getString(R.string.class_Scientist), getString(R.string.class_Soldier), getString(
            R.string.class_Cleric),
            getString(R.string.class_Rogue), getString(R.string.class_Pilot), getString(R.string.class_Envoy))
        val adapterClass = ArrayAdapter(activity, android.R.layout.simple_list_item_1, classList)
        classDropdown.setAdapter(adapterClass)

        classDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedClass = parent.getItemAtPosition(position).toString()

            val subclassList = when(selectedClass) {
                "Scientist" -> listOf("Researcher", "Implementer")
                "Soldier" -> listOf("Dax", "Guard")
                "Cleric" -> listOf("Fundamentalist", "Alchemist")
                "Rogue" -> listOf("Assassin", "Outlaw")
                "Pilot" -> listOf("Admiral", "Hot-Shot")
                "Envoy" -> listOf("Emissary", "Virtuoso")
                else -> emptyList()
            }
            val subclassAdapter =
                ArrayAdapter(activity, android.R.layout.simple_list_item_1, subclassList)
            subclassDropDown.setAdapter(subclassAdapter)

            subclassDropDown.setText("",false)
        }

        // Build dialog
        builder.setView(view)
//            .setTitle("Sign in")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("OK", null)

            val dialog = builder.create()

            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                okButton.setOnClickListener {
                    if (levelDropDown.text?.toString()?.trim().isNullOrEmpty() ||
                        classDropdown.text?.toString()?.trim().isNullOrEmpty() ||
                        subclassDropDown.text?.toString()?.trim().isNullOrEmpty()
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill all boxes",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else {
                        val chLevel =
                            levelDropDown.text.toString()
                                .replace(getString(R.string.level) + " ", "")
                        val chClass = classDropdown.text.toString()
                        val chSubclass = subclassDropDown.text.toString()
                        listener?.CharacterCreationDialogSubmit(
                            chName,
                            chLevel.toInt(),
                            chClass,
                            chSubclass
                        )
                        dialog.dismiss()
                    }
                }
            }

        return dialog
    }
}