package com.dndassistant.ui.characterCreation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.dndassistant.Character_Basic
import com.dndassistant.Data_classes
import com.dndassistant.MainActivity
import com.dndassistant.R
import com.dndassistant.Stats
import com.dndassistant.databinding.FragmentCharacterCreationBinding
import com.dndassistant.ui.character.CharacterViewModel

class CharacterCreation : Fragment() {

    private var _binding: FragmentCharacterCreationBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<CharacterCreationArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        val characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)

        _binding = FragmentCharacterCreationBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
//        drawerLayout?.post {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        }

        val chBasic : Character_Basic = when(args.chLevel) {
            1 -> Character_Basic(args.chClass, args.chSubclass,0,args.chLevel,2)
            2 -> Character_Basic(args.chClass, args.chSubclass,300,args.chLevel,2)
            3 -> Character_Basic(args.chClass, args.chSubclass,900,args.chLevel,2)
            4 -> Character_Basic(args.chClass, args.chSubclass,2700,args.chLevel,2)
            5 -> Character_Basic(args.chClass, args.chSubclass,6500,args.chLevel,3)
            6 -> Character_Basic(args.chClass, args.chSubclass,14000,args.chLevel,3)
            7 -> Character_Basic(args.chClass, args.chSubclass,23000,args.chLevel,3)
            8 -> Character_Basic(args.chClass, args.chSubclass,34000,args.chLevel,3)
            9 -> Character_Basic(args.chClass, args.chSubclass,48000,args.chLevel,4)
            10 -> Character_Basic(args.chClass, args.chSubclass,64000,args.chLevel,4)
            11 -> Character_Basic(args.chClass, args.chSubclass,85000,args.chLevel,4)
            12 -> Character_Basic(args.chClass, args.chSubclass,10000,args.chLevel,4)
            13 -> Character_Basic(args.chClass, args.chSubclass,120000,args.chLevel,5)
            14 -> Character_Basic(args.chClass, args.chSubclass,140000,args.chLevel,5)
            15 -> Character_Basic(args.chClass, args.chSubclass,165000,args.chLevel,5)
            16 -> Character_Basic(args.chClass, args.chSubclass,195000,args.chLevel,5)
            17 -> Character_Basic(args.chClass, args.chSubclass,225000,args.chLevel,6)
            18 -> Character_Basic(args.chClass, args.chSubclass,265000,args.chLevel,6)
            19 -> Character_Basic(args.chClass, args.chSubclass,305000,args.chLevel,6)
            20 -> Character_Basic(args.chClass, args.chSubclass,355000,args.chLevel,6)
            else -> Character_Basic(args.chClass, args.chSubclass,0,1,2)
        }

        val chStats: Stats = when(args.chSubclass) {
            "Researcher" -> Stats(8, 14, 10, 15, 14, 10)
            "Implementer" -> Stats(11, 14, 10, 14, 14, 10)
            "Dax" -> Stats(15, 14, 13, 8, 12, 10)
            "Guard" -> Stats(14, 14, 14, 8, 10, 12)
            "Fundamentalist" -> Stats(8, 10, 13, 12, 15, 14)
            "Alchemist" -> Stats(8, 12, 13, 14, 15, 10)
            "Assassin" -> Stats(13, 15, 12, 10, 13, 10)
            "Outlaw" -> Stats(13, 15, 14, 10, 12, 8)
            "Admiral" -> Stats(10, 14, 14, 10, 13, 12)
            "Hot-Shot" -> Stats(12, 14, 14, 10, 10, 13)
            "Emissary" -> Stats(8, 10, 12, 13, 14, 15)
            "Virtuoso" -> Stats(10, 12,12,12, 12 , 15)
            else -> Stats(10, 10, 10, 10, 10, 10)
        }
        binding.textCharacterS.text = chStats.S.toString()
        binding.textCharacterD.text = chStats.D.toString()
        binding.textCharacterV.text = chStats.V.toString()
        binding.textCharacterI.text = chStats.I.toString()
        binding.textCharacterW.text = chStats.W.toString()
        binding.textCharacterC.text = chStats.C.toString()

        val chMod: Stats = Stats(((chStats.S-(chStats.S % 2))-10)/2, ((chStats.D-(chStats.D % 2))-10)/2, ((chStats.V-(chStats.V % 2))-10)/2,
            ((chStats.I-(chStats.I % 2))-10)/2,((chStats.W-(chStats.W % 2))-10)/2,((chStats.C-(chStats.C % 2))-10)/2)
        val chModToBind = mutableListOf<String>("","","","","","")
        var iter = 0

        for (modifier in chMod){
            if (modifier<0){
                chModToBind[iter] = modifier.toString()
            } else {
                chModToBind[iter] = """+${modifier.toString()}"""
            }
            iter++
        }

        binding.modTextS.text = chModToBind[0]
        binding.modTextD.text = chModToBind[1]
        binding.modTextV.text = chModToBind[2]
        binding.modTextI.text = chModToBind[3]
        binding.modTextW.text = chModToBind[4]
        binding.modTextC.text = chModToBind[5]

        binding.proficiencyValue.text = """+${chBasic.proficiency}"""
        binding.experienceValue.text = """ ${chBasic.experience}/"""
        binding.experienceProgressbar.progress = (chBasic.experience * 100/1000)
//        binding.experienceProgressbar.max = 1000

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        args = CharacterCreationArgs.fromBundle(arguments)
        binding.characterName.text = args.chName
        val chLevel = args.chLevel
        binding.characterLevel.text = chLevel.toString()
        binding.characterClass.text = args.chClass
        binding.characterSubclass.text = args.chSubclass

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
