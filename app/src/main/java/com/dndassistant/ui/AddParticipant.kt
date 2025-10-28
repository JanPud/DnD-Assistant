package com.dndassistant.ui

import android.app.Dialog
import androidx.fragment.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import com.dndassistant.R

class AddParticipant : DialogFragment(){

//    interface AddParticipantDialogListener {
//        fun AddParticipantDialogSubmit(name: String, chLevel: Int, chClass: String, chSubclass: String)
//    }
//    private var listener: AddParticipantDialogListener? = null
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        listener = context as? AddParticipantDialogListener
//            ?: throw ClassCastException("$context must implement InputDialogListener")
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.add_participant, null)

        val participantName = view.findViewById<EditText>(R.id.enter_name_field)
        val participantHP = view.findViewById<EditText>(R.id.enter_HP_field)
        val participantSH = view.findViewById<EditText>(R.id.enter_SH_field)
        val participantAR = view.findViewById<EditText>(R.id.enter_AR_field)
        val participantIN = view.findViewById<EditText>(R.id.enter_IN_field)

        builder.setView(view)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("OK", null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                okButton.setOnClickListener {
                    if (participantName.text?.toString()?.trim().isNullOrEmpty() ||
                        participantHP.text?.toString()?.trim().isNullOrEmpty() ||
                        participantSH.text?.toString()?.trim().isNullOrEmpty() ||
                        participantAR.text?.toString()?.trim().isNullOrEmpty() ||
                        participantIN.text?.toString()?.trim().isNullOrEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill all boxes",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else {
                        parentFragmentManager.setFragmentResult("part", bundleOf(
                            "partName" to participantName.text.toString(),
                            "partHP" to participantHP.text.toString(),
                            "partSH" to participantSH.text.toString(),
                            "partAR" to participantAR.text.toString(),
                            "partIN" to participantIN.text.toString()))
                        dismiss()
                    }
                }
            }
        return dialog
    }
}