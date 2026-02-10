package com.dndassistant.ui.characterCreation

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dndassistant.MainActivity
import com.dndassistant.ProfState
import com.dndassistant.SkillListElement
import com.dndassistant.R
import com.dndassistant.databinding.FragmentCharacterCreationBinding
import kotlinx.coroutines.launch

class CharacterCreation : Fragment() {

    private val viewModel: CharacterCreationViewModel by viewModels()
    private var _binding: FragmentCharacterCreationBinding? = null
    private val binding get() = _binding!!

    private lateinit var skillAdapter: SkillListAdapter

    private val args by navArgs<CharacterCreationArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentCharacterCreationBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
//        drawerLayout?.post {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        }

        viewModel.receiveCharacterCreationArgs(args.chName, args.chLevel, args.chClass, args.chSubclass)

        val nameLayout = root.findViewById<LinearLayout>(R.id.name_layout)
        val proficiencyLayout = root.findViewById<LinearLayout>(R.id.proficiency_layout)
        val experienceLayout = root.findViewById<LinearLayout>(R.id.experience_points_layout)

        binding.characterName.text = viewModel.chName.value
        binding.characterLevel.text = viewModel.character.value?.Ch_level.toString()
        binding.characterClass.text = viewModel.character.value?.Ch_class
        binding.characterSubclass.text = viewModel.character.value?.Ch_subclass

        binding.proficiencyValue.text = "+${viewModel.character.value?.proficiency}"
//        binding.experienceValue.text = " ${viewModel.character.value?.experience.toString()}"
        if (viewModel.character.value != null) {
            binding.experienceValue.text =
                """ ${viewModel.character.value!!.experience}/${expLvl[viewModel.character.value!!.Ch_level - 1].second}"""
            binding.experienceProgressbar.max = expLvl[viewModel.character.value!!.Ch_level-1].second
            binding.experienceProgressbar.progress = viewModel.character.value!!.experience
        }

        binding.textCharacterS.text = viewModel.attributes.value?.S.toString()
        binding.textCharacterD.text = viewModel.attributes.value?.D.toString()
        binding.textCharacterV.text = viewModel.attributes.value?.V.toString()
        binding.textCharacterI.text = viewModel.attributes.value?.I.toString()
        binding.textCharacterW.text = viewModel.attributes.value?.W.toString()
        binding.textCharacterC.text = viewModel.attributes.value?.C.toString()

//        binding.modTextS.text = viewModel.modifiers.value?.S.toString()
//        binding.modTextD.text = viewModel.modifiers.value?.D.toString()
//        binding.modTextV.text = viewModel.modifiers.value?.V.toString()
//        binding.modTextI.text = viewModel.modifiers.value?.I.toString()
//        binding.modTextW.text = viewModel.modifiers.value?.W.toString()
//        binding.modTextC.text = viewModel.modifiers.value?.C.toString()

        val chModToBind = mutableListOf<String>("", "", "", "", "", "")
        if (viewModel.modifiers.value != null) {
            val chMod = listOf<Int>(
                viewModel.modifiers.value!!.S,
                viewModel.modifiers.value!!.D,
                viewModel.modifiers.value!!.V,
                viewModel.modifiers.value!!.I,
                viewModel.modifiers.value!!.W,
                viewModel.modifiers.value!!.C
            )
            var iter = 0
            for (modifier in chMod) {
                if (modifier < 0) {
                    chModToBind[iter] = modifier.toString()
                } else {
                    chModToBind[iter] = """+${modifier.toString()}"""
                }
                iter++
            }
        }

        binding.modTextS.text = chModToBind[0]
        binding.modTextD.text = chModToBind[1]
        binding.modTextV.text = chModToBind[2]
        binding.modTextI.text = chModToBind[3]
        binding.modTextW.text = chModToBind[4]
        binding.modTextC.text = chModToBind[5]

        val reassignButton = root.findViewById<Button>(R.id.reassign_stats_button)
        reassignButton.setOnClickListener {
            val activity = requireActivity()
            val builder = AlertDialog.Builder(activity)
            val dialogView = inflater.inflate(R.layout.dialog_reassign_stats, null)

            builder.setView(dialogView)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("OK", null)

                val dialog = builder.create()
                dialog.setOnShowListener {
                    val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    val valueSView = dialog.findViewById<TextView>(R.id.S_value)
                    val buttonPlusS = dialog.findViewById<Button>(R.id.S_plus)
                    val buttonMinusS = dialog.findViewById<Button>(R.id.S_minus)
                    val valueSCost = dialog.findViewById<TextView>(R.id.S_cost)

                    buttonPlusS!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.S
                            viewModel.changeAttribute("S", attributeValue+1)
                            if (viewModel.designateAttrCost()) {
                                valueSView!!.text = viewModel.attributes.value!!.S.toString()
                                valueSCost!!.text = viewModel.skillCost.value!!.S.toString()
                            } else {
                                viewModel.changeAttribute("S", attributeValue)
                            }
                        }
                    }
                    buttonMinusS!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.S
                            viewModel.changeAttribute("S", attributeValue-1)
                            if (viewModel.designateAttrCost()) {
                                valueSView!!.text = viewModel.attributes.value!!.S.toString()
                                valueSCost!!.text = viewModel.skillCost.value!!.S.toString()
                            } else {
                                viewModel.changeAttribute("S", attributeValue)
                            }
                        }
                    }

                    val valueDView = dialog.findViewById<TextView>(R.id.D_value)
                    val buttonPlusD = dialog.findViewById<Button>(R.id.D_plus)
                    val buttonMinusD = dialog.findViewById<Button>(R.id.D_minus)
                    val valueDCost = dialog.findViewById<TextView>(R.id.D_cost)

                    buttonPlusD!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.D
                            viewModel.changeAttribute("D", attributeValue+1)
                            if (viewModel.designateAttrCost()) {
                                valueDView!!.text = viewModel.attributes.value!!.D.toString()
                                valueDCost!!.text = viewModel.skillCost.value!!.D.toString()
                            } else {
                                viewModel.changeAttribute("D", attributeValue)
                            }
                        }
                    }
                    buttonMinusD!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.D
                            viewModel.changeAttribute("D", attributeValue-1)
                            if (viewModel.designateAttrCost()) {
                                valueDView!!.text = viewModel.attributes.value!!.D.toString()
                                valueDCost!!.text = viewModel.skillCost.value!!.D.toString()
                            } else {
                                viewModel.changeAttribute("D", attributeValue)
                            }
                        }
                    }

                    val valueVView = dialog.findViewById<TextView>(R.id.V_value)
                    val buttonPlusV = dialog.findViewById<Button>(R.id.V_plus)
                    val buttonMinusV = dialog.findViewById<Button>(R.id.V_minus)
                    val valueVCost = dialog.findViewById<TextView>(R.id.V_cost)

                    buttonPlusV!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.V
                            viewModel.changeAttribute("V", attributeValue+1)
                            if (viewModel.designateAttrCost()) {
                                valueVView!!.text = viewModel.attributes.value!!.V.toString()
                                valueVCost!!.text = viewModel.skillCost.value!!.V.toString()
                            } else {
                                viewModel.changeAttribute("V", attributeValue)
                            }
                        }
                    }
                    buttonMinusV!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.V
                            viewModel.changeAttribute("V", attributeValue-1)
                            if (viewModel.designateAttrCost()) {
                                valueVView!!.text = viewModel.attributes.value!!.V.toString()
                                valueVCost!!.text = viewModel.skillCost.value!!.V.toString()
                            } else {
                                viewModel.changeAttribute("V", attributeValue)
                            }
                        }
                    }

                    val valueIView = dialog.findViewById<TextView>(R.id.I_value)
                    val buttonPlusI = dialog.findViewById<Button>(R.id.I_plus)
                    val buttonMinusI = dialog.findViewById<Button>(R.id.I_minus)
                    val valueICost = dialog.findViewById<TextView>(R.id.I_cost)

                    buttonPlusI!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.I
                            viewModel.changeAttribute("I", attributeValue+1)
                            if (viewModel.designateAttrCost()) {
                                valueIView!!.text = viewModel.attributes.value!!.I.toString()
                                valueICost!!.text = viewModel.skillCost.value!!.I.toString()
                            } else {
                                viewModel.changeAttribute("I", attributeValue)
                            }
                        }
                    }
                    buttonMinusI!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.I
                            viewModel.changeAttribute("I", attributeValue-1)
                            if (viewModel.designateAttrCost()) {
                                valueIView!!.text = viewModel.attributes.value!!.I.toString()
                                valueICost!!.text = viewModel.skillCost.value!!.I.toString()
                            } else {
                                viewModel.changeAttribute("I", attributeValue)
                            }
                        }
                    }

                    val valueWView = dialog.findViewById<TextView>(R.id.W_value)
                    val buttonPlusW = dialog.findViewById<Button>(R.id.W_plus)
                    val buttonMinusW = dialog.findViewById<Button>(R.id.W_minus)
                    val valueWCost = dialog.findViewById<TextView>(R.id.W_cost)

                    buttonPlusW!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.W
                            viewModel.changeAttribute("W", attributeValue+1)
                            if (viewModel.designateAttrCost()) {
                                valueWView!!.text = viewModel.attributes.value!!.W.toString()
                                valueWCost!!.text = viewModel.skillCost.value!!.W.toString()
                            } else {
                                viewModel.changeAttribute("W", attributeValue)
                            }
                        }
                    }
                    buttonMinusW!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.W
                            viewModel.changeAttribute("W", attributeValue-1)
                            if (viewModel.designateAttrCost()) {
                                valueWView!!.text = viewModel.attributes.value!!.W.toString()
                                valueWCost!!.text = viewModel.skillCost.value!!.W.toString()
                            } else {
                                viewModel.changeAttribute("W", attributeValue)
                            }
                        }
                    }

                    val valueCView = dialog.findViewById<TextView>(R.id.C_value)
                    val buttonPlusC = dialog.findViewById<Button>(R.id.C_plus)
                    val buttonMinusC = dialog.findViewById<Button>(R.id.C_minus)
                    val valueCCost = dialog.findViewById<TextView>(R.id.C_cost)

                    buttonPlusC!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.C
                            viewModel.changeAttribute("C", attributeValue+1)
                            if (viewModel.designateAttrCost()) {
                                valueCView!!.text = viewModel.attributes.value!!.C.toString()
                                valueCCost!!.text = viewModel.skillCost.value!!.C.toString()
                            } else {
                                viewModel.changeAttribute("C", attributeValue)
                            }
                        }
                    }
                    buttonMinusC!!.setOnClickListener {
                        if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                            val attributeValue = viewModel.attributes.value!!.C
                            viewModel.changeAttribute("C", attributeValue-1)
                            if (viewModel.designateAttrCost()) {
                                valueCView!!.text = viewModel.attributes.value!!.C.toString()
                                valueCCost!!.text = viewModel.skillCost.value!!.C.toString()
                            } else {
                                viewModel.changeAttribute("C", attributeValue)
                            }
                        }
                    }

                    val resetButton = dialog.findViewById<Button>(R.id.reset_attributes_button)

                    resetButton!!.setOnClickListener {
                        viewModel.resetAttributes()
                        if (viewModel.attributes.value != null && viewModel.designateAttrCost()) {
                            valueSView!!.text = viewModel.attributes.value!!.S.toString()
                            valueSCost!!.text = viewModel.skillCost.value!!.S.toString()
                            valueDView!!.text = viewModel.attributes.value!!.D.toString()
                            valueDCost!!.text = viewModel.skillCost.value!!.D.toString()
                            valueVView!!.text = viewModel.attributes.value!!.V.toString()
                            valueVCost!!.text = viewModel.skillCost.value!!.V.toString()
                            valueIView!!.text = viewModel.attributes.value!!.I.toString()
                            valueICost!!.text = viewModel.skillCost.value!!.I.toString()
                            valueWView!!.text = viewModel.attributes.value!!.W.toString()
                            valueWCost!!.text = viewModel.skillCost.value!!.W.toString()
                            valueCView!!.text = viewModel.attributes.value!!.C.toString()
                            valueCCost!!.text = viewModel.skillCost.value!!.C.toString()
                        }
                    }

                    if (viewModel.attributes.value != null && viewModel.skillCost.value != null) {
                        valueSView!!.text = viewModel.attributes.value!!.S.toString()
                        valueSCost!!.text = viewModel.skillCost.value!!.S.toString()

                        valueDView!!.text = viewModel.attributes.value!!.D.toString()
                        valueDCost!!.text = viewModel.skillCost.value!!.D.toString()

                        valueVView!!.text = viewModel.attributes.value!!.V.toString()
                        valueVCost!!.text = viewModel.skillCost.value!!.V.toString()

                        valueIView!!.text = viewModel.attributes.value!!.I.toString()
                        valueICost!!.text = viewModel.skillCost.value!!.I.toString()

                        valueWView!!.text = viewModel.attributes.value!!.W.toString()
                        valueWCost!!.text = viewModel.skillCost.value!!.W.toString()

                        valueCView!!.text = viewModel.attributes.value!!.C.toString()
                        valueCCost!!.text = viewModel.skillCost.value!!.C.toString()
                    }

                    okButton.setOnClickListener {
                        binding.textCharacterS.text = viewModel.attributes.value?.S.toString()
                        binding.textCharacterD.text = viewModel.attributes.value?.D.toString()
                        binding.textCharacterV.text = viewModel.attributes.value?.V.toString()
                        binding.textCharacterI.text = viewModel.attributes.value?.I.toString()
                        binding.textCharacterW.text = viewModel.attributes.value?.W.toString()
                        binding.textCharacterC.text = viewModel.attributes.value?.C.toString()
                        viewModel.updateModifiers()
                        val chModToBind = mutableListOf<String>("", "", "", "", "", "")
                        if (viewModel.modifiers.value != null) {
                            val chMod = listOf<Int>(
                                viewModel.modifiers.value!!.S,
                                viewModel.modifiers.value!!.D,
                                viewModel.modifiers.value!!.V,
                                viewModel.modifiers.value!!.I,
                                viewModel.modifiers.value!!.W,
                                viewModel.modifiers.value!!.C
                            )
                            var iter = 0
                            for (modifier in chMod) {
                                if (modifier < 0) {
                                    chModToBind[iter] = modifier.toString()
                                } else {
                                    chModToBind[iter] = """+${modifier.toString()}"""
                                }
                                iter++
                            }
                        }
                        binding.modTextS.text = chModToBind[0]
                        binding.modTextD.text = chModToBind[1]
                        binding.modTextV.text = chModToBind[2]
                        binding.modTextI.text = chModToBind[3]
                        binding.modTextW.text = chModToBind[4]
                        binding.modTextC.text = chModToBind[5]
                        viewModel.updateSkillProf()
                        val skillData = viewModel.skillProficiencies.value!!.toList()
                        skillAdapter.updateData(skillData)
                        dialog.dismiss()
                    }
                }

            dialog.show()
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val saveButton = root.findViewById<Button>(R.id.save_character_button)
        saveButton.setOnClickListener {


            when (viewModel.saveCharacter()) {
                0 -> {
                    Toast.makeText(
                        requireContext(),
                        "Character saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                1 -> {
                    Toast.makeText(
                        requireContext(),
                        "Character could not be saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                2 -> {
                    Toast.makeText(
                        requireContext(),
                        "Character with the same name already exists",
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Unknown resolution",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        experienceLayout.setOnClickListener {
            val activity = requireActivity()
            val expBuilder = AlertDialog.Builder(activity)
            val expView = inflater.inflate(R.layout.dialog_create_character_name, null)
            val expViewValue = expView.findViewById<TextView>(R.id.enter_name_field)
            expViewValue.inputType = InputType.TYPE_CLASS_NUMBER
            expViewValue.hint = "Input Exp value"
            expBuilder.setView(expView)
//            .setTitle("Sign in")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("OK", null)

                val dialog = expBuilder.create()
                dialog.setOnShowListener {
                    val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    okButton.setOnClickListener {
                        val expViewValue = expViewValue.text.toString()
                        if (expViewValue.trim().isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "Please fill all boxes",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        } else {
                            viewModel.changeExp(expViewValue.toInt())
//                            binding.experienceValue.text = """ ${chBasic.experience}/${expLvl[chBasic.Ch_level-1].second}"""
//                            binding.experienceProgressbar.setProgress(expViewValue.toInt(),true)
                            binding.experienceValue.text =
                                """ ${viewModel.character.value!!.experience}/${expLvl[viewModel.character.value!!.Ch_level - 1].second}"""
                            binding.experienceProgressbar.progress = viewModel.character.value!!.experience
                            dialog.dismiss()
                        }
                    }
                }
            dialog.show()
        }

        val survivabilityLayout = root.findViewById<LinearLayout>(R.id.life_stats_layout)
        val AR_layout = survivabilityLayout.findViewById<LinearLayout>(R.id.AR_layout)
//        AR_layout.findViewById<TextView>(R.id.AR_value).text = "${10+chModToBind[1].toInt()}"
        AR_layout.setOnClickListener {
            val activity = requireActivity()
            val expBuilder = AlertDialog.Builder(activity)
            val numView = inflater.inflate(R.layout.dialog_create_character_name, null)
            val numViewValue = numView.findViewById<TextView>(R.id.enter_name_field)
            numViewValue.inputType = InputType.TYPE_CLASS_NUMBER
            numViewValue.hint = "Input AR value"
            expBuilder.setView(numView)
//            .setTitle("Sign in")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("OK", null)

            val dialog = expBuilder.create()
            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                okButton.setOnClickListener {
                    val numViewValueS = numViewValue.text.toString()
                    if (numViewValueS.trim().isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill all boxes",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else {
                        viewModel.changeAR(numViewValueS.toInt())
                        binding.ARValue.text = viewModel.survivability.value?.AR.toString()
                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
        }

        val HP_layout = survivabilityLayout.findViewById<LinearLayout>(R.id.HP_layout)
//        HP_layout.findViewById<TextView>(R.id.HP_value).text = "10"
        HP_layout.setOnClickListener {
            val activity = requireActivity()
            val expBuilder = AlertDialog.Builder(activity)
            val numView = inflater.inflate(R.layout.dialog_create_character_name, null)
            val numViewValue = numView.findViewById<TextView>(R.id.enter_name_field)
            numViewValue.inputType = InputType.TYPE_CLASS_NUMBER
            numViewValue.hint = "Input HP value"
            expBuilder.setView(numView)
//            .setTitle("Sign in")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("OK", null)

            val dialog = expBuilder.create()
            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                okButton.setOnClickListener {
                    val numViewValueS = numViewValue.text.toString()
                    if (numViewValueS.trim().isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill all boxes",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else {
                        viewModel.changeHP(numViewValueS.toInt())
                        binding.HPValue.text =
                            "${viewModel.survivability.value?.Cur_HP}/${viewModel.survivability.value?.HP}"
                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
        }

        val SH_layout = survivabilityLayout.findViewById<LinearLayout>(R.id.SH_layout)
//        SH_layout.findViewById<TextView>(R.id.SH_value).text = "10"
        SH_layout.setOnClickListener {
            val activity = requireActivity()
            val expBuilder = AlertDialog.Builder(activity)
            val numView = inflater.inflate(R.layout.dialog_create_character_name, null)
            val numViewValue = numView.findViewById<TextView>(R.id.enter_name_field)
            numViewValue.inputType = InputType.TYPE_CLASS_NUMBER
            numViewValue.hint = "Input SH value"
            expBuilder.setView(numView)
//            .setTitle("Sign in")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("OK", null)

            val dialog = expBuilder.create()
            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                okButton.setOnClickListener {
                    val numViewValueS = numViewValue.text.toString()
                    if (numViewValueS.trim().isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill all boxes",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else {
                        viewModel.changeSH(numViewValueS.toInt())
                        binding.SHValue.text =
                            "${viewModel.survivability.value?.Cur_Sh}/${viewModel.survivability.value?.Shield}"
                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
        }

        val EN_layout = survivabilityLayout.findViewById<LinearLayout>(R.id.EN_layout)
//        EN_layout.findViewById<TextView>(R.id.EN_value).text = "${5+5*chBasic.Ch_level}"
        viewModel.changeEN()
        binding.ENValue.text =
            "${viewModel.survivability.value?.Cur_En}/${viewModel.survivability.value?.Energy}"

        val nameField = nameLayout.findViewById<TextView>(R.id.character_name)
        val editField = nameLayout.findViewById<EditText>(R.id.edit_character_name)
        nameField.setOnClickListener {
            editField.setText(nameField.text)
            nameField.visibility = View.GONE
            editField.visibility = View.VISIBLE
            editField.requestFocus()
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editField, 0)
        }
        editField.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                nameField.text = editField.text
                editField.visibility = View.GONE
                nameField.visibility = View.VISIBLE
                viewModel.changeName(nameField.text.toString())
                nameField.textSize = 20f
                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editField.windowToken, 0)
            }
        }

//        val skillData = mutableListOf<SkillListElement>()
//        var modifier = 0
//        if (viewModel.character.value != null){
//            for (skill in allSkills) {
//                when (skill.second) {
//                    "S" -> {modifier = chModToBind[0].toInt()}
//                    "D" -> {modifier = chModToBind[1].toInt()}
//                    "V" -> {modifier = chModToBind[2].toInt()}
//                    "I" -> {modifier = chModToBind[3].toInt()}
//                    "W" -> {modifier = chModToBind[4].toInt()}
//                    "C" -> {modifier = chModToBind[5].toInt()}
//
//                }
//                skillData.add(SkillListElement(modifier, ProfState.ZERO, skill.first + " (" + skill.second + ")", viewModel.character.value!!.proficiency))
//            }
//        }
//        skillData.sortBy { it.name }

        if (viewModel.skillProficiencies.value == null){
            Log.d("ChCreation", "No proficiencies data")
        } else {
            val skillsLayout = root.findViewById<RecyclerView>(R.id.skills_layout)
            skillAdapter = SkillListAdapter(viewModel.skillProficiencies.value!!, viewModel)

            skillsLayout.layoutManager = LinearLayoutManager(this.context)
            skillsLayout.adapter = skillAdapter

            val skillData = viewModel.skillProficiencies.value!!.toList()
            skillAdapter.updateData(skillData)
        }

        viewModel.highCostEvent.observe(viewLifecycleOwner){
            it.get()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        args = CharacterCreationArgs.fromBundle(arguments)
//        if (viewModel.character.value != null) {
//            binding.characterName.text = viewModel.chName.value
//            val chLevel = viewModel.character.value!!.Ch_level
//            binding.characterLevel.text = chLevel.toString()
//            binding.characterClass.text = viewModel.character.value!!.Ch_class
//            binding.characterSubclass.text = viewModel.character.value!!.Ch_subclass
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.closeDrawer()
    }
}

class SkillListAdapter(private val skills: MutableList<SkillListElement>, val viewModel: CharacterCreationViewModel): RecyclerView.Adapter<SkillListAdapter.ItemViewHolder>(){

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view){
        val skill_modifier: TextView = view.findViewById<TextView>(R.id.skill_modifier)
        val skill_checkbox: Button = view.findViewById<Button>(R.id.skill_checkbox)
        val skill_name: TextView = view.findViewById<TextView>(R.id.skill_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.skill_layout_element, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val skill = skills[position]
        if (skill.modifier<0){
            holder.skill_modifier.text = skill.modifier.toString()
        } else {
            holder.skill_modifier.text = """+${skill.modifier.toString()}"""
        }
//        holder.skill_modifier.text = skill.modifier.toString()
        holder.skill_checkbox.tag = skill.profState.toString()
        val scalingSign = allSkills.find { it.first==skill.name }?.second
        holder.skill_name.text = skill.name+" ("+scalingSign+")"

        val modifierToDisplay = skill.modifier + skill.proficiency*skill.profState.code
        if (modifierToDisplay<0){
            holder.skill_modifier.text = modifierToDisplay.toString()
        } else {
            holder.skill_modifier.text = """+${modifierToDisplay.toString()}"""
        }
        when(skill.profState){
            ProfState.ZERO -> holder.skill_checkbox.setBackgroundResource(R.drawable.base_skill_icon)
            ProfState.ONE -> holder.skill_checkbox.setBackgroundResource(R.drawable.proficiency_icon)
            ProfState.TWO -> holder.skill_checkbox.setBackgroundResource(R.drawable.expertise_icon)
        }

        holder.skill_checkbox.setOnClickListener {
            it.tag = skill.profState.next()
            skill.profState = skill.profState.next()
            viewModel.changeSkillProf(skill.name)

            val modifierToDisplay = skill.modifier + skill.proficiency*skill.profState.code
            if (modifierToDisplay<0){
                holder.skill_modifier.text = modifierToDisplay.toString()
            } else {
                holder.skill_modifier.text = """+${modifierToDisplay.toString()}"""
            }
//            holder.skill_modifier.text = modifierToDisplay.toString()
            when(skill.profState){
                ProfState.ZERO -> holder.skill_checkbox.setBackgroundResource(R.drawable.base_skill_icon)
                ProfState.ONE -> holder.skill_checkbox.setBackgroundResource(R.drawable.proficiency_icon)
                ProfState.TWO -> holder.skill_checkbox.setBackgroundResource(R.drawable.expertise_icon)
            }
        }
    }

    override fun getItemCount(): Int = skills.size

    fun updateData(newSkills: List<SkillListElement>){
        skills.clear()
        skills.addAll(newSkills)
        notifyDataSetChanged()
    }

}
