package com.arpitbandil.demo.blutoothchat.module.bluetooth

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.arpitbandil.demo.blutoothchat.R
import com.arpitbandil.demo.blutoothchat.databinding.FragmentSearchDevicesListBinding
import com.arpitbandil.demo.blutoothchat.helpers.Constants.BLUETOOTH_SPP
import com.arpitbandil.demo.blutoothchat.helpers.toast
import java.io.IOException

@SuppressLint("MissingPermission")
class SearchDevicesFragment : Fragment() {

    private val binding by lazy { FragmentSearchDevicesListBinding.inflate(layoutInflater) }

    var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    private val listAdapter = MyBluetoothDeviceRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isBTAvailable()
        initRecyclerView()
        displayPairedDevices()
        searchBTDevices()
    }

    private fun isBTAvailable() {
        bluetoothManager = requireContext().getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        if (bluetoothAdapter == null || bluetoothAdapter?.isEnabled == false) {
            context.toast(R.string.bluetooth_not_supported)
            findNavController().navigateUp()
        }
    }

    private fun searchBTDevices() {
        binding.progressCircular.isVisible = true
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(requireContext(), receiver, filter, RECEIVER_EXPORTED)
        registerReceiver(requireContext(), receiver, filter2, RECEIVER_EXPORTED)
        bluetoothAdapter?.startDiscovery()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
            registerForContextMenu(this)
            isVisible = true
            binding.tvEmpty.isVisible = false
        }
    }

    private fun displayPairedDevices() {
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            listAdapter.addItemIfNotExists(device)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    listAdapter.addItemIfNotExists(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    binding.progressCircular.isVisible = false
                    binding.tvEmpty.isVisible = listAdapter.values.isEmpty()
                    binding.recyclerView.isVisible = listAdapter.values.isNotEmpty()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        bluetoothAdapter?.cancelDiscovery()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    //
    private inner class ConnectThread(val device: BluetoothDevice?, val message: String? = null) :
        Thread() {

        private var mmSocket: BluetoothSocket?

        init {
            mmSocket = device?.createRfcommSocketToServiceRecord(BLUETOOTH_SPP)
        }

        public override fun run() {
            bluetoothAdapter?.cancelDiscovery()
            for (i in 1..3) {
                if (mmSocket?.isConnected == true) {
                } else {
                    var isInException = false
                    try {
                        mmSocket?.connect()
                    } catch (e: IOException) {
                        try {
                            mmSocket =
                                device?.javaClass?.getMethod("createRfcommSocket", Int::class.java)
                                    ?.invoke(device, i) as BluetoothSocket
                            mmSocket?.connect()
                        } catch (ex: Exception) {
                            isInException = true
                            ex.printStackTrace()
                        }
                    }
                    if (!isInException) {
                        break

                        return
                    }
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("TAG", "Could not close the client socket", e)
            }
        }
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = listAdapter.longClickPosition
        binding.progressCircular.isVisible = false
        when (item.itemId) {
            1 -> { // SPP
                findNavController().navigate(
                    SearchDevicesFragmentDirections.terminalChat(
                        listAdapter.values[position]?.address.toString()
                    )
                )
            }

            2 -> {
                val input = EditText(context)
                val dialog = AlertDialog.Builder(context)
                dialog.setTitle("Enter Message to Send")
                dialog.setView(input)
                dialog.setPositiveButton("Send") { _, _ ->
                    ConnectThread(listAdapter.values[position], input.text.toString()).start()
                }
                dialog.setNegativeButton("Cancel") { _, _ -> }
                dialog.show()
            }
        }
        return super.onContextItemSelected(item)
    }
}