package com.arpitbandil.demo.blutoothchat.module.home

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.arpitbandil.demo.blutoothchat.R
import com.arpitbandil.demo.blutoothchat.databinding.FragmentHomeBinding
import com.arpitbandil.demo.blutoothchat.helpers.Utils.askAllBTPermissions
import com.arpitbandil.demo.blutoothchat.helpers.Utils.hasAllBTPermissions
import com.arpitbandil.demo.blutoothchat.helpers.Utils.isBTHardwareUnAvailable
import com.arpitbandil.demo.blutoothchat.helpers.toast

class HomeFragment : Fragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }

    private val btPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.filter { !it.value }.any()) {
                context.toast(R.string.need_permission_to_proceed)
            } else {
                enableBtWithPermissionCheck()
            }
        }

    private val enableBtResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                findNavController().navigate(HomeFragmentDirections.searchAction())
            } else {
                context.toast(R.string.open_bluetooth)
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForHardware()
        setOnClickListener()
    }

    private fun checkForHardware() {
        if (isBTHardwareUnAvailable(context)) {
            context.toast(R.string.bluetooth_not_supported)
            findNavController().navigateUp()
        }
    }

    private fun setOnClickListener() {
        binding.btnSearch.setOnClickListener {
            enableBtWithPermissionCheck()
        }
    }

    private fun enableBtWithPermissionCheck() {
        if (hasAllBTPermissions(context))
            enableBtResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        else
            askAllBTPermissions(btPermissions)
    }

}
