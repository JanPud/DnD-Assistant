package com.dndassistant.ui.compendium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.dndassistant.database.CharacterTable
import com.dndassistant.databinding.FragmentCompendiumBinding
import com.dndassistant.ui.character.CharactersListAdapter
import com.dndassistant.ui.compendium.CompendiumViewModel

class CompendiumFragment : Fragment() {

    private var _binding: FragmentCompendiumBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val compendiumViewModel = ViewModelProvider(this)[CompendiumViewModel::class.java]

        _binding = FragmentCompendiumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = CharactersListAdapter(emptyList(), object : CharactersListAdapter.OnItemClickListener{
            override fun onItemClick(character: CharacterTable) {
                TODO("Not yet implemented")
            }

            override fun onItemLongClick(view: View, character: CharacterTable) {
                TODO("Not yet implemented")
            }
        })

        val recyclerView = binding.compendiumDbLayout
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        compendiumViewModel.compendiumData.observe(viewLifecycleOwner, Observer { entries ->
            adapter.setData(entries)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}