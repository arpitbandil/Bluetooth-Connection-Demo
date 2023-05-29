package com.arpitbandil.demo.blutoothchat.module.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arpitbandil.demo.blutoothchat.R
import com.arpitbandil.demo.blutoothchat.databinding.ItemDeviceBinding
import com.arpitbandil.demo.blutoothchat.helpers.toast

class MyBluetoothDeviceRecyclerViewAdapter :
    RecyclerView.Adapter<MyBluetoothDeviceRecyclerViewAdapter.ViewHolder>() {

    var longClickPosition: Int = -1
    val values: MutableList<BluetoothDevice?> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("MissingPermission", "NewApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item?.name ?: item?.alias
        holder.addressView.text = item?.address
        holder.itemView.setOnLongClickListener {
            longClickPosition = position
            false
        }
        holder.itemView.setOnClickListener {
            holder.itemView.context.toast("Long Click to see options")
        }
    }

    override fun getItemCount(): Int = values.size

    @SuppressLint("MissingPermission")
    fun addItemIfNotExists(device: BluetoothDevice?) {
        if (device?.name.isNullOrBlank() || device?.type == BluetoothDevice.DEVICE_TYPE_LE) {
            return
        }
        values.filter { device?.address == it?.address }.ifEmpty {
            values.add( device)
            notifyItemInserted(itemCount-1)
        }
    }

    inner class ViewHolder(binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
        val contentView: TextView = binding.content
        val addressView: TextView = binding.address

        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(Menu.NONE, 1, Menu.NONE, R.string.connect_with_spp) // Bluetooth Serial Port Profile
            menu?.add(Menu.NONE, 2, Menu.NONE, R.string.connect)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

}