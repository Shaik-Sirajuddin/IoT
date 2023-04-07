package com.example.ardinobluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ardinobluetooth.databinding.ActivityMainBinding
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var result: TextView
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var binding: ActivityMainBinding
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var teluguSpeech: TextToSpeech
    private var isEnglish = true
    var message = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        connectButton = this.findViewById(R.id.connectButton)
        result = this.findViewById(R.id.resultText)

        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            // if No error is found then only it will run
            if (i != TextToSpeech.ERROR) {
                // To Choose language of speech
                textToSpeech.language = Locale.UK
            }

        }

        teluguSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            // if No error is found then only it will run
            if (i != TextToSpeech.ERROR) {
                val telugu = Locale("te", "IN")
                // To Choose language of speech
                teluguSpeech.language = telugu
            }
        }

        connectButton.setOnClickListener {
            connectToBT()
        }

        binding.first.setOnClickListener {
            setSelection("H")
        }
        binding.second.setOnClickListener {
            setSelection("F")
        }
        binding.third.setOnClickListener {
            setSelection("M")
        }

        binding.telugu.setOnClickListener {
            isEnglish = false
            setLanguage()
        }
        binding.english.setOnClickListener {
            isEnglish = true
            setLanguage()
        }
    }


    private fun setLanguage(){

        val notSelected = ColorStateList.valueOf(Color.parseColor("#3F51B5"))
        val selected = ColorStateList.valueOf(Color.parseColor("#000000"))

        if(isEnglish){
            binding.english.backgroundTintList  = selected
            binding.telugu.backgroundTintList = notSelected
        }
        else{
            binding.english.backgroundTintList  = notSelected
            binding.telugu.backgroundTintList = selected
        }
    }

    private fun setSelection(mode: String) {

        if (bluetoothSocket.isConnected) {
            val outputStream: OutputStream = bluetoothSocket.outputStream
            outputStream.write(mode.toByteArray())
            //outputStream.close()
        } else {
            Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
        }

    }

    private fun speak(text: String) {
        if(isEnglish){
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        else{
            teluguSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
            bluetoothSocket =
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
        runOnUiThread {
            Toast.makeText(this, "Bluetooth Connected", Toast.LENGTH_SHORT).show()
        }
        val inputStream = socket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int
        while (true) {
            try {
                //  Log.d("hi")
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
    private fun getTeluguString(value : Char) : String{
        return   when(value){
            else -> {
                ""
            }
        }
    }
    private fun getEnglishString(value : Char): String {
        val result = when (value) {
            'a' -> {
                "hello"
            }
            'b' -> {
                "give me water"
            }
            'c' -> {
                "thank you"
            }
            'd' -> {
                "i want to play"
            }
            'e' -> {
                "after coming"
            }
            'f' -> {
                "give me some snacks"
            }
            'g' -> {
                "okay"
            }
            'h' -> {
                "yummy!"
            }
            'i' -> {
                "i will be back"
            }
            'j' -> {
                "after coming"
            }
            'k' -> {
                "it is delicious"
            }
            'l' -> {
                "after eating"
            }
            'm' -> {
                "sure"
            }
            'n' -> {
                "turn off lights"
            }
            'o' -> {
                "tomorrow is holiday"
            }
            'p' -> {
                "good friday"
            }
            'q' -> {
                "good night"
            }
            'r' -> {
                "sweet dreams"
            }
            's' -> {
                "i want food"
            }
            't' -> {
                "i am happy"
            }
            'u' -> {
                "i am sad"
            }
            'v' -> {
                "i am angry"
            }
            'w' -> {
                "emergency"
            }
            'A' -> {
                "hello"
            }
            'B' -> {
                "chill guys"
            }
            'C' -> {
                "let's play"
            }
            'D' -> {
                "let's go out"
            }
            'E' -> {
                "i will beat you"
            }
            'F' -> {
                "happy guys"
            }
            'G' -> {
                "good news guys"
            }
            'H' -> {
                "useless fellows "
            }
            'I' -> {
                "i won't come  "
            }
            'J' -> {
                "i am hungry"
            }
            'K' -> {
                "i will leave"
            }
            'L' -> {
                "excuse me"
            }
            'M' -> {
                "how much it costs?"
            }
            'N' -> {
                "can you decrease price"
            }
            'O' -> {
                "all fruits are fresh?"
            }
            'P' -> {
                "i won't like it"
            }
            else -> {
                ""
            }
        }
        return result
    }
    private fun updateData(data: String) {

        if (data.isEmpty()) return;

        val value = data[0]

        val displayText = if(isEnglish){
            getEnglishString(value)
        } else{
            getTeluguString(value)
        }

        runOnUiThread {
            speak(displayText)
            binding.resultText.text = displayText
        }
    }
}