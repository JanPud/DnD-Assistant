package com.dndassistant.ui.character

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dndassistant.R
import com.dndassistant.database.CharacterTable
import com.dndassistant.databinding.FragmentCharacterOverviewBinding

class CharacterOverviewFragment : Fragment() {
    private var _binding: FragmentCharacterOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CharacterOverviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterOverviewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = CharactersListAdapter(emptyList(), object : CharactersListAdapter.OnItemClickListener{
            override fun onItemClick(character: CharacterTable) {
                val bundle = Bundle().apply {
                    putSerializable("characterData", character.character)
                }
                findNavController().navigate(R.id.nav_character_details, bundle)
            }

            override fun onItemLongClick(view: View, character: CharacterTable) {
                showPopupMenuCharacter(view, character)
            }
        })
        val recyclerView = binding.charactersDbLayout
        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        viewModel = ViewModelProvider(this)[CharacterOverviewViewModel::class.java]
        viewModel.characterData.observe(viewLifecycleOwner, Observer{ characterTables ->
            adapter.setData(characterTables)
        })



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showPopupMenuCharacter(view: View, character: CharacterTable){
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.character_popup_menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    val builder = AlertDialog.Builder(requireContext())
                    val view = layoutInflater.inflate(R.layout.change_participant_health, null)

                    val damageField = view.findViewById<EditText>(R.id.enter_damage_field)
                    damageField.isEnabled = false
                    damageField.isVisible = false
                    val targetDropDown = view.findViewById<AutoCompleteTextView>(R.id.damage_to)
//                    val targetList = listOf("Health", "Shield", "AR", "Initiative")
//                    val adapterTarget = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, targetList)
                    targetDropDown.isEnabled = false
                    targetDropDown.isVisible = false
                    builder
                        .setView(view)
                        .setTitle("Delete ${character.title}?")
                        .setMessage("Are you sure you want to delete this character?")
                        .setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
                        .setPositiveButton("YES") { _,_ ->
                            viewModel.deleteCharacter(character)
//                            findNavController().navigate(R.id.nav_character)

                        }
                    val dialog = builder.create()
                    dialog.show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}

class CharactersListAdapter(
    private var charactersList: List<CharacterTable>,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<CharactersListAdapter.CharactersViewHolder>(){

    interface OnItemClickListener {
        fun onItemClick(character: CharacterTable)
        fun onItemLongClick(view: View, character: CharacterTable)
    }

    inner class CharactersViewHolder(view: View): RecyclerView.ViewHolder(view){
        val characterName: TextView = view.findViewById<TextView>(R.id.character_name)
        val characterImage: ImageView = view.findViewById<ImageView>(R.id.character_image)

        fun bind(character: CharacterTable){
            itemView.setOnClickListener {
                listener.onItemClick(character)
            }

            itemView.setOnLongClickListener {
                listener.onItemLongClick(itemView, character)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.character_entry, parent, false)
        return CharactersViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int) {
        val currentEntry = charactersList[position]
        holder.characterName.text = currentEntry.title
        holder.characterImage.setImageURI(currentEntry.character.image?.toUri())
        holder.bind(charactersList[position])
    }

    override fun getItemCount(): Int {
        return charactersList.size
    }

    fun setData(characters: List<CharacterTable>){
        this.charactersList = characters
//        notifyItemRangeChanged(0, characters.size)
        notifyDataSetChanged()
    }
}
