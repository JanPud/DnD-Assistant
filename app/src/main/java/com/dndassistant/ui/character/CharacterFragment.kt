package com.dndassistant.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.dndassistant.databinding.FragmentCharacterBinding

class CharacterFragment : Fragment() {

    private var _binding: FragmentCharacterBinding? = null
    private val binding get() = _binding!!
    val args: CharacterFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val characterViewModel = ViewModelProvider(this)[CharacterViewModel::class.java]
        characterViewModel.receiveCharacterCreationArgs(args)
        _binding = FragmentCharacterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        characterViewModel.attributes.observe(viewLifecycleOwner) { value ->
            binding.textCharacterS.text = getString(com.dndassistant.R.string.character_stat, value.S)
            binding.textCharacterD.text = getString(com.dndassistant.R.string.character_stat, value.D)
            binding.textCharacterV.text = getString(com.dndassistant.R.string.character_stat, value.V)
            binding.textCharacterI.text = getString(com.dndassistant.R.string.character_stat, value.I)
            binding.textCharacterW.text = getString(com.dndassistant.R.string.character_stat, value.W)
            binding.textCharacterC.text = getString(com.dndassistant.R.string.character_stat, value.C)
        }
        binding.characterName.text = characterViewModel.chName.value
        binding.characterClass.text = characterViewModel.character.value?.Ch_class
        binding.characterSubclass.text = characterViewModel.character.value?.Ch_subclass
        binding.characterLevel.text = characterViewModel.character.value?.Ch_level.toString()

        binding.proficiencyValue.text = characterViewModel.character.value?.proficiency.toString()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}