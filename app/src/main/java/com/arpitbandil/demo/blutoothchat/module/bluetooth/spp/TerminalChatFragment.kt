package com.arpitbandil.demo.blutoothchat.module.bluetooth.spp

import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.arpitbandil.demo.blutoothchat.databinding.FragmentTerminalChatBinding
import com.arpitbandil.demo.blutoothchat.helpers.bt_spp.SerialListener
import com.arpitbandil.demo.blutoothchat.helpers.bt_spp.SerialService
import com.arpitbandil.demo.blutoothchat.helpers.bt_spp.SerialService.SerialBinder
import com.arpitbandil.demo.blutoothchat.helpers.bt_spp.SerialSocket
import com.arpitbandil.demo.blutoothchat.helpers.toast
import java.util.ArrayDeque

class TerminalChatFragment : Fragment(), ServiceConnection, SerialListener {

    private lateinit var deviceAddress: String
    private val binding by lazy { FragmentTerminalChatBinding.inflate(layoutInflater) }
    private var service: SerialService? = null
    private var connected = Connected.False
    private var initialStart = true

    private enum class Connected {
        False, Pending, True
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews() {
        val safeArgs: TerminalChatFragmentArgs by navArgs()
        deviceAddress = safeArgs.deviceAddress
        binding.receiveText.movementMethod = ScrollingMovementMethod.getInstance()
    }

    private fun initListeners() {
        binding.sendBtn.setOnClickListener { send(binding.sendText.text.toString()) }
    }

    /*
     * Serial + UI
     */
    private fun connect() {
        try {
            val bluetoothManager = requireContext().getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager?.adapter
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            status("connecting...")
            connected = Connected.Pending
            device?.let {
                service?.connect(SerialSocket(requireActivity().applicationContext, it))
            }
        } catch (e: Exception) {
            onSerialConnectError(e)
        }
    }

    private fun disconnect() {
        connected = Connected.False
        service?.disconnect()
    }

    private fun send(str: String) {
        if (connected != Connected.True) {
            context?.toast("not connected")
            return
        }
        try {
            val msg = "$str\n"
            binding.receiveText.append(msg)
            service?.write(msg.toByteArray())
        } catch (e: Exception) {
            onSerialIoError(e)
        }
    }

    private fun receive(byteArrays: ArrayDeque<ByteArray>) {
        binding.receiveText.append(byteArrays.joinToString("\n") { String(it) })
    }

    private fun status(str: String) = binding.receiveText.append("$str\n")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().bindService(
            Intent(activity, SerialService::class.java), this, Context.BIND_AUTO_CREATE
        )
    }

    override fun onStart() {
        super.onStart()
        if (service != null)
            service?.attach(this)
        else
            requireActivity().startService(
                Intent(activity, SerialService::class.java)
            ) // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    override fun onResume() {
        super.onResume()
        if (initialStart && service != null) {
            initialStart = false
            requireActivity().runOnUiThread { this.connect() }
        }
    }

    override fun onDetach() {
        try {
            requireActivity().unbindService(this)
        } catch (ignored: java.lang.Exception) {
        }
        super.onDetach()
    }

    override fun onStop() {
        if (!requireActivity().isChangingConfigurations) service?.detach()
        super.onStop()
    }

    override fun onDestroy() {
        if (connected != Connected.False) disconnect()
        requireActivity().stopService(Intent(activity, SerialService::class.java))
        super.onDestroy()
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        service = (binder as SerialBinder).service
        service?.attach(this)
        if (initialStart && isResumed) {
            initialStart = false
            requireActivity().runOnUiThread { connect() }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }

    override fun onSerialConnect() {
        status("connected")
        connected = Connected.True
    }

    override fun onSerialConnectError(e: Exception?) {
        status("connection failed: " + e!!.message)
        disconnect()
    }

    override fun onSerialRead(data: ByteArray?) {
        val temp = ArrayDeque<ByteArray>()
        temp.add(data)
        receive(temp)
    }

    override fun onSerialRead(datas: ArrayDeque<ByteArray>) {
        receive(datas)
    }

    override fun onSerialIoError(e: Exception?) {
        status("connection lost: ${e?.message}")
        disconnect()
    }
}