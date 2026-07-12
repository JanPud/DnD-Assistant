package com.dndassistant.ui.compendium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dndassistant.databinding.FragmentCompendiumBinding
import com.dndassistant.ui.compendium.CompendiumViewModel

class CompendiumFragment : Fragment() {

    private var _binding: FragmentCompendiumBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val compendiumViewModel = ViewModelProvider(this).get(CompendiumViewModel::class.java)

        _binding = FragmentCompendiumBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        characterViewModel.stats.observe(viewLifecycleOwner) { value ->
//            binding.textCharacterS.text = getString(com.dndassistant.R.string.character_stat, value.S)
//            binding.textCharacterD.text = getString(com.dndassistant.R.string.character_stat, value.D)
//            binding.textCharacterV.text = getString(com.dndassistant.R.string.character_stat, value.V)
//            binding.textCharacterI.text = getString(com.dndassistant.R.string.character_stat, value.I)
//            binding.textCharacterW.text = getString(com.dndassistant.R.string.character_stat, value.W)
//            binding.textCharacterC.text = getString(com.dndassistant.R.string.character_stat, value.C)
//        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}