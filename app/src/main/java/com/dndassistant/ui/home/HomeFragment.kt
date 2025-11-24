package com.dndassistant.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.dndassistant.MainActivity
import com.dndassistant.R
import com.dndassistant.databinding.FragmentHomeBinding
import com.dndassistant.ui.processingAnimation

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val homeViewModel =
//            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hostingButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.hostingInfo.processingAnimation("Advertising", viewModel.connectingDone)
                viewModel.setHostButton(true)
            } else {
                viewModel.resetConnectingAnimation()
                viewModel.setHostButtonInfo("Not hosting")
                viewModel.setHostButton(false)
            }
        }

        binding.discoveryButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.discoveryInfo.processingAnimation("Connecting", viewModel.connectingDone)
                viewModel.setDiscoveryButton(true)
            } else {
                viewModel.resetConnectingAnimation()
                viewModel.setDiscoveryButtonInfo("Not discovering")
                viewModel.setDiscoveryButton(false)
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.hostingButton.isEnabled = state.hostButtonEnabled
            binding.hostingButton.isChecked = state.hostButtonState
            binding.hostingInfo.text = state.hostButtonInfo

            binding.discoveryButton.isEnabled = state.discoveryButtonEnabled
            binding.discoveryButton.isChecked = state.discoveryButtonState
            binding.discoveryInfo.text = state.discoveryButtonInfo

            binding.hostName.text = state.hostTagText
            binding.yourName.text = state.yourTagText
            binding.otherNames.text = state.otherTagsText
            binding.connectionInfo.text = state.infoText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}