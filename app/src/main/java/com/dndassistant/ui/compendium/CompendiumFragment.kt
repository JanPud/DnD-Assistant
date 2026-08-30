package com.dndassistant.ui.compendium

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dndassistant.R
import com.dndassistant.compendium.CompendiumTable
import com.dndassistant.databinding.FragmentCompendiumBinding

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

        val adapter = CompendiumListAdapter(
            emptyList(),
            object : CompendiumListAdapter.OnItemClickListener{
            override fun onItemClick(character: CompendiumTable) {
                TODO("Not yet implemented")
            }

            override fun onItemLongClick(view: View, character: CompendiumTable) {
                TODO("Not yet implemented")
            }
        },
            compendiumViewModel::getBitmap)

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

class CompendiumListAdapter(
    private var charactersList: List<CompendiumTable>,
    private val listener: OnItemClickListener,
    private val imageLoader : (String, Int, Int) -> Bitmap?
): RecyclerView.Adapter<CompendiumListAdapter.CompendiumViewHolder>(){

    interface OnItemClickListener {
        fun onItemClick(character: CompendiumTable)
        fun onItemLongClick(view: View, character: CompendiumTable)
    }

    inner class CompendiumViewHolder(view: View): RecyclerView.ViewHolder(view){
        val entryName: TextView = view.findViewById<TextView>(R.id.entry_name)
        val entryImage: ImageView = view.findViewById<ImageView>(R.id.entry_image)

        fun bind(character: CompendiumTable){
            itemView.setOnClickListener {
                listener.onItemClick(character)
            }

            itemView.setOnLongClickListener {
                listener.onItemLongClick(itemView, character)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompendiumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.compendium_entry, parent, false)

        return CompendiumViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompendiumViewHolder, position: Int) {
        val currentEntry = charactersList[position]
        holder.entryName.text = currentEntry.name
        val bitmap = imageLoader(currentEntry.name, 128, 128)
//        holder.characterImage.setImageURI(currentEntry.character.image?.toUri())
        holder.entryImage.setImageBitmap(bitmap)
        holder.bind(charactersList[position])
    }

    override fun getItemCount(): Int {
        return charactersList.size
    }

    fun setData(characters: List<CompendiumTable>){
        this.charactersList = characters
//        notifyItemRangeChanged(0, characters.size)
        notifyDataSetChanged()
    }
}