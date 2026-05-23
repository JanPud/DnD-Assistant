package com.dndassistant.ui.character

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dndassistant.R
import com.dndassistant.databinding.FragmentCharacterCreationBinding
import com.dndassistant.utilities.ProfState
import com.dndassistant.utilities.SkillListElement
import com.dndassistant.utilities.allSkills
import com.dndassistant.utilities.expLvl
import com.dndassistant.utilities.toPx

class CharacterFragment : Fragment() {

    private var _binding: FragmentCharacterCreationBinding? = null
    private val binding get() = _binding!!
    val args: CharacterFragmentArgs by navArgs()
    private lateinit var skillAdapter: SkillListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val characterViewModel = ViewModelProvider(this)[CharacterViewModel::class.java]
        characterViewModel.receiveCharacterCreationArgs(args)
        _binding = FragmentCharacterCreationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        characterViewModel.character.observe(viewLifecycleOwner) {
            binding.characterName.text = " ${characterViewModel.chName.value}"
            binding.characterClass.text = characterViewModel.character.value?.Ch_class
            binding.characterSubclass.text = characterViewModel.character.value?.Ch_subclass
            binding.characterLevel.text = characterViewModel.character.value?.Ch_level.toString()

            characterViewModel.character.value?.proficiency?.let {
                if (it >= 0)
                    binding.proficiencyValue.text = "+${characterViewModel.character.value?.proficiency}"
                else
                    binding.proficiencyValue.text = "-${characterViewModel.character.value?.proficiency}"
            }

            if (characterViewModel.character.value != null) {
                binding.experienceValue.text =
                    """ ${characterViewModel.character.value!!.experience}/${expLvl[characterViewModel.character.value!!.Ch_level - 1].second}"""
                binding.experienceProgressbar.max = expLvl[characterViewModel.character.value!!.Ch_level-1].second
                binding.experienceProgressbar.progress = characterViewModel.character.value!!.experience
            }
        }

        characterViewModel.attributes.observe(viewLifecycleOwner) { value ->
            binding.textCharacterS.text = getString(com.dndassistant.R.string.character_stat, value.S)
            binding.textCharacterD.text = getString(com.dndassistant.R.string.character_stat, value.D)
            binding.textCharacterV.text = getString(com.dndassistant.R.string.character_stat, value.V)
            binding.textCharacterI.text = getString(com.dndassistant.R.string.character_stat, value.I)
            binding.textCharacterW.text = getString(com.dndassistant.R.string.character_stat, value.W)
            binding.textCharacterC.text = getString(com.dndassistant.R.string.character_stat, value.C)

            val chModToBind = mutableListOf<String>("", "", "", "", "", "")
            if (characterViewModel.modifiers.value != null) {
                val chMod = listOf<Int>(
                    characterViewModel.modifiers.value!!.S,
                    characterViewModel.modifiers.value!!.D,
                    characterViewModel.modifiers.value!!.V,
                    characterViewModel.modifiers.value!!.I,
                    characterViewModel.modifiers.value!!.W,
                    characterViewModel.modifiers.value!!.C
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

            if (characterViewModel.skillProficiencies.value == null){
                Log.d("ChCreation", "No proficiencies data")
            } else {
                val skillsLayout = root.findViewById<RecyclerView>(R.id.skills_layout)
                skillAdapter = SkillListAdapter(characterViewModel.skillProficiencies.value!!)

                skillsLayout.layoutManager = LinearLayoutManager(this.context)
                skillsLayout.adapter = skillAdapter

                val skillData = characterViewModel.skillProficiencies.value!!.toList()
                skillAdapter.updateData(skillData)
            }
        }

        characterViewModel.highCostEvent.observe(viewLifecycleOwner){
            it.get()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        val imageLayout = binding.characterImage
        imageLayout.setImageURI(characterViewModel.imageUri.value)
        imageLayout.layoutParams.width = requireContext().toPx(70).toInt()
        imageLayout.layoutParams.height = requireContext().toPx(70).toInt()
        imageLayout.requestLayout()

        characterViewModel.survivability.observe(viewLifecycleOwner) {
            binding.ARValue.text = characterViewModel.survivability.value?.AR.toString()
            binding.HPValue.text = buildString {
                append(characterViewModel.survivability.value?.Cur_HP.toString())
                append("/")
                append(characterViewModel.survivability.value?.HP.toString())
            }
            binding.SHValue.text = buildString {
                append(characterViewModel.survivability.value?.Cur_Sh.toString())
                append("/")
                append(characterViewModel.survivability.value?.Shield.toString())
            }
            binding.ENValue.text = buildString {
                append(characterViewModel.survivability.value?.Cur_En.toString())
                append("/")
                append(characterViewModel.survivability.value?.Energy.toString())
            }
        }

        val levelUpButton = binding.reassignStatsButton
        levelUpButton.text = "LEVEL UP"

        val changeImageButton = binding.addImageButton
        changeImageButton.text = "Change\nImage"

        val saveCharacterButton = binding.saveCharacterButton

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SkillListAdapter(
    private val skills: MutableList<SkillListElement>
): RecyclerView.Adapter<SkillListAdapter.ItemViewHolder>(){

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
    }

    override fun getItemCount(): Int = skills.size

    fun updateData(newSkills: List<SkillListElement>){
        skills.clear()
        skills.addAll(newSkills)
        notifyDataSetChanged()
    }

}