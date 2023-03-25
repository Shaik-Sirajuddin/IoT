package com.example.ardinobluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ardinobluetooth.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var connectButton: Button
    private lateinit var result: TextView
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var binding: ActivityMainBinding
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var message = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        connectButton = this.findViewById(R.id.connectButton)
        result = this.findViewById(R.id.resultText)
        connectButton.setOnClickListener {
            connectToBT()
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToBT() {
        initData()
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.d("device", deviceName)
            if (deviceName == "HC-05") {
                initializeConnection(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeConnection(device: BluetoothDevice) {
        try {
            val bluetoothSocket =
                device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            bluetoothSocket.connect()
            val executor: ExecutorService = Executors.newSingleThreadExecutor()

            executor.execute {
                readData(bluetoothSocket)
            }


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Read data from Bluetooth device
    private fun readData(socket: BluetoothSocket) {
        Log.d("bt", "connected")
        val inputStream = socket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int
        while (true) {
            try {
                bytes = inputStream.read(buffer)
                val data = String(buffer, 0, bytes)
                if (data.isNotEmpty()) {
                    Log.d("testData", data)
                    updateData(data)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                break
            }
        }
    }

    private fun initData() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            return
        }
    }

    private fun updateData(data: String) {
        val value: Int? = data.toIntOrNull() ?: return
        var displayText = ""
        displayText = when (value) {
            0 -> {
                "I am from hr"
            }
            1 -> {
                "I need help"
            }
            else -> {
                "I don't know"
            }
        }
        runOnUiThread {
            binding.resultText.text = displayText
        }
    }


}