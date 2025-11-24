package com.dndassistant.ui

import android.view.View
import android.widget.TextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dndassistant.ui.battle.BattleViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ViewExtensions {
}

fun View.showSnackbar(message: String, duration: Int, actionText: String, action: (View) -> Unit){
    Snackbar.make(this, message, duration)
        .setAction(actionText, action)
        .show()
}

fun TextView.processingAnimation(processText: String, stopWhen: Flow<Boolean>){
    val lifecycleOwner = findViewTreeLifecycleOwner()
        ?: throw IllegalStateException("TextView must be attached to a lifecycle owner")

    val dots = listOf("",".","..","...")

    lifecycleOwner.lifecycleScope.launch {
        var i = 0

        val animationJob = launch {
            while (isActive){
                this@processingAnimation.text = buildString {
                    append(processText)
                    append(dots[i % dots.size])
                }
                i++
                delay(500)
            }
        }

        stopWhen.first { it }

        animationJob.cancel()
        this@processingAnimation.text = "Done"
    }
}

@Serializable
sealed class SerialMessage {
    @Serializable
    @SerialName("connected_list")
    data class ConnectedList(val clients: List<String>) : SerialMessage()

    @Serializable
    @SerialName("request_list")
    object RequestList : SerialMessage()

    @Serializable
    @SerialName("battle_state")
    data class BattleState(val participants: List<BattleViewModel.CardData>) : SerialMessage()

    @Serializable
    @SerialName("request_battle_state")
    object RequestBattleState : SerialMessage()
}