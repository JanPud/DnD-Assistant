package com.dndassistant.ui.battle

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dndassistant.R
import com.dndassistant.databinding.BattleFragmentBinding
import com.dndassistant.ui.AddParticipant
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.dndassistant.ui.battle.BattleViewModel.CardData
import com.google.android.material.chip.Chip
import kotlin.text.toInt

class BattleFragment : Fragment() {

    private val viewModel: BattleViewModel by activityViewModels()
    private lateinit var cardContainer: LinearLayout
    private var _binding: BattleFragmentBinding? = null
    private val binding get() = _binding!!
    var round: Int = 0
    private var lastSelectedCard: CardView? = null

    private var currentParticipant = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BattleFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        cardContainer = root.findViewById<LinearLayout>(R.id.card_container)
        round = viewModel.round
        binding.displayRound.text = getString(R.string.round_0_caption, round)

        for (card in viewModel.cardDataList.toList()){
//            val cardView = layoutInflater.inflate(R.layout.battle_participant_card_layout, cardContainer, false) as CardView
//            cardView.findViewById<TextView>(R.id.cardTitle).text = card.cardTitle
//            cardView.findViewById<TextView>(R.id.healthPool).text = card.healthPool.toString()
//            cardView.findViewById<TextView>(R.id.currentHealth).text = card.health.toString()
//            cardView.findViewById<TextView>(R.id.shieldPool).text = card.shieldPool.toString()
//            cardView.findViewById<TextView>(R.id.currentShield).text = card.shield.toString()
//            cardView.findViewById<TextView>(R.id.currentArmor).text = card.armor.toString()
//            cardView.findViewById<TextView>(R.id.currentInitiative).text = card.initiative.toString()
//
//            for (effect in card.effects){
//                val effectLayout =
//                    cardView.findViewById<LinearLayout>(R.id.participant_effect_layout)
//                val newEffect = layoutInflater.inflate(
//                    R.layout.battle_effect_layout,
//                    effectLayout,
//                    false
//                ) as Chip
//                if (card.effectsDuration[card.effects.indexOf(effect)] == "Indefinitely"){
//                    newEffect.text = getString(
//                        R.string.effect_chip_format_string,
//                        effect,
//                        card.effectsDuration[card.effects.indexOf(effect)]
//                    )
//                    addEffect(effectLayout, newEffect)
//                } else {
//                    newEffect.text = getString(
//                        R.string.effect_chip_format,
//                        effect,
//                        card.effectsDuration[card.effects.indexOf(effect)].toInt()
//                    )
//                    addEffect(effectLayout, newEffect)
//                }
//            }

            val cardView = createCardView(card, layoutInflater, cardContainer)

            addCard(cardView)
        }

//        lateinit var partName : String
        parentFragmentManager.setFragmentResultListener("part", viewLifecycleOwner){_, bundle ->
            val partName = bundle.getString("partName") ?: ""
            val partHP = bundle.getString("partHP")
            val partSH = bundle.getString("partSH")
            val partAR = bundle.getString("partAR")
            val partIN = bundle.getString("partIN")
            val newCard = layoutInflater.inflate(R.layout.battle_participant_card_layout, cardContainer, false) as CardView
            newCard.tag = partName
            newCard.findViewById<TextView>(R.id.cardTitle).text = partName
            newCard.findViewById<TextView>(R.id.healthPool).text = getString(R.string.health_pool, partHP?.toInt())
            newCard.findViewById<TextView>(R.id.currentHealth).text = getString(R.string.cur_health, partHP?.toInt())
            newCard.findViewById<TextView>(R.id.shieldPool).text = getString(R.string.health_pool, partSH?.toInt())
            newCard.findViewById<TextView>(R.id.currentShield).text = getString(R.string.cur_health, partSH?.toInt())
            newCard.findViewById<TextView>(R.id.currentArmor).text = getString(R.string.cur_health, partAR?.toInt())
            newCard.findViewById<TextView>(R.id.currentInitiative).text = getString(R.string.cur_health, partIN?.toInt())
            addCard(newCard)
        }

        val addButton = root.findViewById<FloatingActionButton>(R.id.add_participant_button)
        addButton.setOnClickListener {
            AddParticipant().show(parentFragmentManager, "AddParticipant")
//            val newCard = layoutInflater.inflate(R.layout.battle_participant_card_layout, cardContainer, false) as CardView
//            newCard.tag = "Robert"
//            newCard.findViewById<TextView>(R.id.currentHealth).text = 123.toString()
//            newCard.findViewById<TextView>(R.id.healthPool).text = 123.toString()
//            newCard.findViewById<TextView>(R.id.currentShield).text = 10.toString()
//            newCard.findViewById<TextView>(R.id.shieldPool).text = 10.toString()
//            newCard.findViewById<TextView>(R.id.currentInitiative).text = 10.toString()
//            newCard.findViewById<TextView>(R.id.currentArmor).text = 12.toString()
//            addCard(newCard)
        }

        val removeButton = root.findViewById<FloatingActionButton>(R.id.remove_participant_button)
        removeButton.setOnClickListener {
            if (lastSelectedCard == null){
                Toast.makeText(
                    requireContext(),
                    "Select a card first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                deleteCard(lastSelectedCard)
                lastSelectedCard = null
                currentParticipant = currentParticipant -1
                highlightNextCard()
            }
        }

        val damageButton = root.findViewById<FloatingActionButton>(R.id.deal_damage)
        damageButton.setOnClickListener {
            if (lastSelectedCard == null){
                Toast.makeText(
                    requireContext(),
                    "Select a card first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val builder = AlertDialog.Builder(requireContext())
                val view = layoutInflater.inflate(R.layout.change_participant_health, null)

                view.findViewById<EditText>(R.id.enter_damage_field).hint = "Amount to deal"
                val targetDropDown = view.findViewById<AutoCompleteTextView>(R.id.damage_to)
                val targetList = listOf("Health", "Shield", "AR", "Initiative")
                val adapterTarget = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, targetList)
                targetDropDown.setAdapter(adapterTarget)
                builder
                    .setView(view)
                    .setTitle("Deal damage")
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("OK", null)
                val dialog = builder.create()

                dialog.setOnShowListener {
                    val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    okButton.setOnClickListener {
                        val damage = view.findViewById<EditText>(R.id.enter_damage_field).text
                        if (damage?.toString()?.trim().isNullOrEmpty() ||
                            targetDropDown?.toString()?.trim().isNullOrEmpty()){
                            Toast.makeText(
                                requireContext(),
                                "Fill all boxes",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        } else {
                            val cardTitle = lastSelectedCard?.rootView?.findViewById<TextView>(R.id.cardTitle)?.text.toString()
                            val cardInitiative =  lastSelectedCard?.rootView?.findViewById<TextView>(R.id.currentInitiative)?.text.toString().toInt()
                            val cardData = viewModel.cardDataList.find { it.cardTitle == cardTitle && it.initiative == cardInitiative}
                            if (cardData == null){
                                return@setOnClickListener
                            }else {
                                when (targetDropDown.text.toString()) {
                                    "Health" -> {
                                        val health =
                                            lastSelectedCard?.findViewById<TextView>(R.id.currentHealth)?.text.toString()
                                                .toInt()
                                        val newHealth = health - damage.toString().toInt()
                                        cardData.health = newHealth
                                        lastSelectedCard?.findViewById<TextView>(R.id.currentHealth)?.text =
                                            (newHealth).toString()
                                    }

                                    "Shield" -> {
                                        val health =
                                            lastSelectedCard?.findViewById<TextView>(R.id.currentHealth)?.text.toString()
                                                .toInt()
                                        val shield =
                                            lastSelectedCard?.findViewById<TextView>(R.id.currentShield)?.text.toString()
                                                .toInt()
                                        if (shield >= damage.toString().toInt()) {
                                            val newShield = shield - damage.toString().toInt()
                                            cardData.shield = newShield
                                            lastSelectedCard?.findViewById<TextView>(R.id.currentShield)?.text =
                                                (newShield).toString()
                                        } else {
                                            val newHealth = health - damage.toString()
                                                .toInt() + shield
                                            cardData.shield = 0
                                            cardData.health = newHealth
                                            lastSelectedCard?.findViewById<TextView>(R.id.currentShield)?.text =
                                                "0"
                                            lastSelectedCard?.findViewById<TextView>(R.id.currentHealth)?.text =
                                                (newHealth).toString()
                                        }
                                    }

                                    "AR" -> {
                                        val armor =
                                            lastSelectedCard?.findViewById<TextView>(R.id.currentArmor)?.text.toString()
                                                .toInt()
                                        val newArmor = armor - damage.toString().toInt()
                                        cardData.armor = newArmor
                                        lastSelectedCard?.findViewById<TextView>(R.id.currentArmor)?.text =
                                            (newArmor).toString()
                                    }

                                    "Initiative" -> {
                                        val initiative = lastSelectedCard?.findViewById<TextView>(R.id.currentInitiative)?.text.toString().toInt()
                                        val newInit = initiative - damage.toString().toInt()
                                        cardData.initiative = newInit
                                        lastSelectedCard?.findViewById<TextView>(R.id.currentInitiative)?.text = newInit.toString()
                                    }
                                }
                            }
                            dialog.dismiss()
                        }
                    }
                }
                dialog.show()
                lastSelectedCard?.cardElevation = 8f
                lastSelectedCard?.setCardBackgroundColor(Color.WHITE)
            }
        }

        val healButton = root.findViewById<FloatingActionButton>(R.id.heal)
        healButton.setOnClickListener {
            if (lastSelectedCard == null){
                Toast.makeText(
                    requireContext(),
                    "Select a card first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val builder = AlertDialog.Builder(requireContext())
                val view = layoutInflater.inflate(R.layout.change_participant_health, null)

                view.findViewById<EditText>(R.id.enter_damage_field).hint = "Amount to regain"
                val targetDropDown = view.findViewById<AutoCompleteTextView>(R.id.damage_to)
                val targetList = listOf("Health", "Shield", "AR", "Initiative")
                val adapterTarget = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, targetList)
                targetDropDown.setAdapter(adapterTarget)
                builder
                    .setView(view)
                    .setTitle("Heal")
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("OK", null)
                val dialog = builder.create()

                dialog.setOnShowListener {
                    val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    okButton.setOnClickListener {
                        val damage = view.findViewById<EditText>(R.id.enter_damage_field).text
                        if (damage?.toString()?.trim().isNullOrEmpty() ||
                            targetDropDown?.toString()?.trim().isNullOrEmpty()){
                            Toast.makeText(
                                requireContext(),
                                "Fill all boxes",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        } else {
                            val cardTitle = lastSelectedCard?.rootView?.findViewById<TextView>(R.id.cardTitle)?.text.toString()
                            val cardInitiative =  lastSelectedCard?.rootView?.findViewById<TextView>(R.id.currentInitiative)?.text.toString().toInt()
                            val cardData = viewModel.cardDataList.find { it.cardTitle == cardTitle && it.initiative == cardInitiative}
                            if (cardData == null){
                                return@setOnClickListener
                            }else {
                                when (targetDropDown.text.toString()){
                                    "Health" -> {
                                        val health = lastSelectedCard?.findViewById<TextView>(R.id.currentHealth)?.text.toString().toInt()
                                        val newHealth = health+damage.toString().toInt()
                                        cardData.health = newHealth
                                        lastSelectedCard?.findViewById<TextView>(R.id.currentHealth)?.text = (newHealth).toString()
                                    }
                                    "Shield" -> {
                                        val shield = lastSelectedCard?.findViewById<TextView>(R.id.currentShield)?.text.toString().toInt()
                                        val newShield = shield + damage.toString().toInt()
                                        cardData.shield = newShield
                                        lastSelectedCard?.findViewById<TextView>(R.id.currentShield)?.text =
                                            (newShield).toString()
                                    }
                                    "AR" -> {
                                        val armor = lastSelectedCard?.findViewById<TextView>(R.id.currentArmor)?.text.toString().toInt()
                                        val newArmor = armor+damage.toString().toInt()
                                        cardData.armor = newArmor
                                        lastSelectedCard?.findViewById<TextView>(R.id.currentArmor)?.text = (newArmor).toString()
                                    }
                                    "Initiative" -> {
                                        val initiative = lastSelectedCard?.findViewById<TextView>(R.id.currentInitiative)?.text.toString().toInt()
                                        val newInit = initiative+damage.toString().toInt()
                                        cardData.initiative = newInit
                                        lastSelectedCard?.findViewById<TextView>(R.id.currentInitiative)?.text = newInit.toString()
                                    }
                                }
                            }
                            dialog.dismiss()
                        }
                    }
                }
                dialog.show()
                lastSelectedCard?.cardElevation = 8f
                lastSelectedCard?.setCardBackgroundColor(Color.WHITE)
            }
        }

        val nextButton = root.findViewById<Button>(R.id.next_round_button)
        nextButton.setOnClickListener {
            highlightNextCard()
            if (currentParticipant == 0) {
                nextRound()
            }
        }

        val addEffectButton = root.findViewById<FloatingActionButton>(R.id.add_effect)
        addEffectButton.setOnClickListener {
            if (lastSelectedCard == null){
                Toast.makeText(
                    requireContext(),
                    "Select a card first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                val builder = AlertDialog.Builder(requireContext())
                val view = layoutInflater.inflate(R.layout.add_effect_layout, null)

                val effectDropDown = view.findViewById<AutoCompleteTextView>(R.id.effect_type)
                val effectList = viewModel.effectList
                val adapterEffect = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, effectList)
                effectDropDown.setAdapter(adapterEffect)
                effectDropDown.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    val selectedItem = parent.getItemAtPosition(position).toString()

                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(effectDropDown.windowToken, 0)
                }

                val roundDropDown = view.findViewById<AutoCompleteTextView>(R.id.num_of_round_effect)
                val roundList = viewModel.roundList
                val adapterRound = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, roundList)
                roundDropDown.setAdapter(adapterRound)

                builder
                    .setView(view)
                    .setTitle("Effect")
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("OK", null)
                val dialog = builder.create()

                dialog.setOnShowListener {
                    val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    okButton.setOnClickListener {
                        val effect = effectDropDown.text
                        val rounds = roundDropDown.text
                        if (effect?.toString()?.trim().isNullOrEmpty() ||
                            rounds?.toString()?.trim().isNullOrEmpty()){
                            Toast.makeText(
                                requireContext(),
                                "Fill all boxes",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        } else {
                            val effectLayout =
                                lastSelectedCard?.findViewById<LinearLayout>(R.id.participant_effect_layout)
                            val newEffect = layoutInflater.inflate(
                                R.layout.battle_effect_layout,
                                effectLayout,
                                false
                            ) as Chip
                            if (rounds.toString() == "Indefinitely"){
                                newEffect.text = getString(
                                    R.string.effect_chip_format_string,
                                    effect.toString(),
                                    rounds.toString()
                                )
                                dialog.dismiss()
                                addEffect(effectLayout, newEffect)
                            } else {
                                newEffect.text = getString(
                                    R.string.effect_chip_format,
                                    effect.toString(),
                                    rounds.toString().substringBefore(" ").toInt()
                                )
                                dialog.dismiss()
                                addEffect(effectLayout, newEffect)
                            }
                        }
                    }
                }
                dialog.show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun AddParticipantDialogSubmit(name: String, chLevel: Int, chClass: String, chSubclass: String){
//
//    }

    private fun addCard(card: CardView){
        (card.parent as? ViewGroup)?.removeView(card)
        cardContainer.addView(card)

//        if (!viewModel.listOfParticipant.contains(card)){
//            viewModel.listOfParticipant.add(card)
//        }

//        val effectList = mutableListOf<String>()
//        val effectDuration = mutableListOf<String>()
//        for (index in 0 until card.findViewById<LinearLayout>(R.id.participant_effect_layout).childCount){
//            val effectChip = card.findViewById<LinearLayout>(R.id.participant_effect_layout).getChildAt(index) as Chip
//
//            val descriptor = effectChip.text.toString()
//            val label = descriptor.substringBefore(": ")
//            val value = descriptor.substringAfter(": ")
//            if (value == "Indefinitely"){
//                effectList.add(label)
//                effectDuration.add(value)
//            } else {
//                effectList.add(label)
//                effectDuration.add(value.replace(" rounds", ""))
//            }
//        }

//        val battleCardData = BattleViewModel.CardData(card.findViewById<TextView>(R.id.cardTitle).text.toString(),
//            card.findViewById<TextView>(R.id.currentHealth).text.toString().toInt(),
//            card.findViewById<TextView>(R.id.healthPool).text.toString().toInt(),
//            card.findViewById<TextView>(R.id.currentShield).text.toString().toInt(),
//            card.findViewById<TextView>(R.id.shieldPool).text.toString().toInt(),
//            card.findViewById<TextView>(R.id.currentArmor).text.toString().toInt(),
//            card.findViewById<TextView>(R.id.currentInitiative).text.toString().toInt(),
//            effectList, effectDuration)
        val battleCardData = viewModel.readCardData(card)
        if (battleCardData == null){
            return
        } else {
            if (!viewModel.cardDataList.contains(battleCardData)) {
                viewModel.cardDataList.add(battleCardData)
            }
        }
        sortCards()
    }

    private fun addEffect(cardLayout: LinearLayout?, effect: Chip){
        cardLayout?.addView(effect)
        effect.setOnClickListener { view ->
            val popupView = layoutInflater.inflate(R.layout.effect_description_layout, null)
            val popupText = popupView.findViewById<TextView>(R.id.chip_description)
            val deleteButton = popupView.findViewById<Button>(R.id.delete_effect)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.elevation = 12f
            popupWindow.showAsDropDown(view, 0, 10)

            deleteButton.setOnClickListener {
                cardLayout?.removeView(effect)
                popupWindow.dismiss()
            }
        }

        val cardTitle = cardLayout?.rootView?.findViewById<TextView>(R.id.cardTitle)?.text.toString()
        val cardInitiative =  cardLayout?.rootView?.findViewById<TextView>(R.id.currentInitiative)?.text.toString().toInt()
        val cardData = viewModel.cardDataList.find { it.cardTitle == cardTitle && it.initiative == cardInitiative}

        if (cardData == null){
            return
        }else {
            val (type, rounds) = getEffectData(effect)
            if (cardData.effects.contains(type)) {
                return
            } else {
                cardData.effects.add(type)
                if (rounds == "Indefinitely") {
                    cardData.effectsDuration.add(rounds)
                } else {
                    cardData.effectsDuration.add(rounds.substringBefore(" "))
                }
            }
        }
//        sortCards()
    }

    private fun getEffectData(effect: Chip?): Pair<String, String>{
        if (effect is Chip) {
            val descriptor = effect.text.toString()
            val label = descriptor.substringBefore(": ")
            val value = descriptor.substringAfter(": ")
            return Pair(label, value)
        } else {
            return Pair("", "")
        }
    }

    private fun deleteCard(card: CardView?){
        (card?.parent as? ViewGroup)?.removeView(card)

//        if (viewModel.listOfParticipant.contains(card)){
//            viewModel.listOfParticipant.remove(card)
//        }

//        val battleCardData = BattleCardData(card?.findViewById<TextView>(R.id.cardTitle)?.text.toString(),
//            card?.findViewById<TextView>(R.id.currentInitiative)?.text.toString().toInt(),card)
        val battleCardData = viewModel.readCardData(card)
        if (viewModel.cardDataList.contains(battleCardData)) {
            viewModel.cardDataList.remove(battleCardData)
        }
    }

    private fun nextRound(){
        viewModel.round = round+1
        round = viewModel.round
        view?.findViewById<TextView>(R.id.display_round)?.text = getString(R.string.round_0_caption, round)

//        var effectIndex = 0
        for (card in viewModel.cardDataList) {
            card.effectsDuration.forEach {
                if (it == "Indefinitely") {
                    it == it
                } else {
                    val index = card.effectsDuration.indexOf(it)
                    card.effectsDuration[index] = (it.toInt()-1).toString()
                }
            }
        }

        val cardLayout = view?.findViewById<LinearLayout>(R.id.card_container)
        if (cardLayout?.childCount == null){
            return
        } else {
            for (index in 0 until cardLayout.childCount) {
                val cardLayout =
                    view?.findViewById<LinearLayout>(R.id.card_container)?.getChildAt(index)
                val effectLayout =
                    cardLayout?.findViewById<LinearLayout>(R.id.participant_effect_layout)
                if (effectLayout == null) continue

                for (i in 0 until effectLayout.childCount) {
                    val effect = effectLayout.getChildAt(i)
                    if (effect is Chip) {
                        val descriptor = effect.text.toString()
                        val label = descriptor.substringBefore(": ")
                        val value = descriptor.substringAfter(": ")
                        if (value.toString() == "Indefinitely") {
                            return
                        } else {
                            val valueInt = value.replace(" rounds", "").toInt()
                            if (valueInt - 1 < 0) {
                                effectLayout.removeView(effect)
                            } else {
                                effect.text =
                                    getString(R.string.effect_chip_format, label, (valueInt - 1))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sortCards(){
        viewModel.cardDataList.sortByDescending { it.initiative }
        cardContainer.removeAllViews()
        viewModel.cardDataList.forEach {
            cardContainer.addView(createCardView(it, layoutInflater, cardContainer))
        }
    }

    private fun highlightNextCard(){
        val numOfCards = viewModel.cardDataList.size
        if (numOfCards == 0) {return}

        if (currentParticipant != -1){
            val previousCard = cardContainer.getChildAt(currentParticipant) as CardView
            previousCard.findViewById<LinearLayout>(R.id.overall_add_participant_layout).background = null
        }

        currentParticipant = (currentParticipant + 1) % numOfCards

        val newCard = cardContainer.getChildAt(currentParticipant) as CardView
        newCard.findViewById<LinearLayout>(R.id.overall_add_participant_layout).background =
            ResourcesCompat.getDrawable(resources, R.drawable.participant_frame, null)
    }

    fun createCardView(card: CardData, layoutInflater: LayoutInflater, cardContainer: LinearLayout): CardView{
        val cardView = layoutInflater.inflate(R.layout.battle_participant_card_layout, cardContainer, false) as CardView
        cardView.findViewById<TextView>(R.id.cardTitle).text = card.cardTitle
        cardView.findViewById<TextView>(R.id.healthPool).text = getString(R.string.health_pool,card.healthPool)
        cardView.findViewById<TextView>(R.id.currentHealth).text = card.health.toString()
        cardView.findViewById<TextView>(R.id.shieldPool).text = getString(R.string.health_pool,card.shieldPool)
        cardView.findViewById<TextView>(R.id.currentShield).text = card.shield.toString()
        cardView.findViewById<TextView>(R.id.currentArmor).text = card.armor.toString()
        cardView.findViewById<TextView>(R.id.currentInitiative).text = card.initiative.toString()

        for (effect in card.effects){
            val effectLayout =
                cardView.findViewById<LinearLayout>(R.id.participant_effect_layout)
            val newEffect = layoutInflater.inflate(
                R.layout.battle_effect_layout,
                effectLayout,
                false
            ) as Chip
            if (card.effectsDuration[card.effects.indexOf(effect)] == "Indefinitely"){
                newEffect.text = getString(
                    R.string.effect_chip_format_string,
                    effect,
                    card.effectsDuration[card.effects.indexOf(effect)]
                )
                addEffect(effectLayout, newEffect)
            } else {
                newEffect.text = getString(
                    R.string.effect_chip_format,
                    effect,
                    card.effectsDuration[card.effects.indexOf(effect)].toInt()
                )
                addEffect(effectLayout, newEffect)
            }
        }

        cardView.setOnClickListener {
            lastSelectedCard?.cardElevation = 8f
            lastSelectedCard?.setCardBackgroundColor(Color.WHITE)

            cardView.findViewById<LinearLayout>(R.id.overall_add_participant_layout)
            cardView.cardElevation = 24f
            cardView.setCardBackgroundColor(resources.getColor(R.color.void_light))
            lastSelectedCard = cardView
        }

        return cardView
    }

}