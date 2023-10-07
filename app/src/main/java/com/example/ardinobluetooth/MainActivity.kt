package com.example.ardinobluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ardinobluetooth.databinding.ActivityMainBinding
import org.w3c.dom.Text
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
    private lateinit var hindiSpeech: TextToSpeech
    private var isEnglish = true
    private var isHindi = false
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
        hindiSpeech = TextToSpeech(
            applicationContext,
        ){ i->
            if(i!= TextToSpeech.ERROR){
                val hindi = Locale("hi", "IN")
                // To Choose language of speech
                hindiSpeech.language = hindi
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
            isHindi = false
            setLanguage()
        }
        binding.english.setOnClickListener {
            isEnglish = true
            isHindi = false
            setLanguage()
        }
        binding.hindi.setOnClickListener {
            isHindi = true
            isEnglish = false
            setLanguage()
        }
        binding.number.setText(getNumber())
        binding.number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                saveNumber(s.toString().trim())
            }
        })
        binding.number.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                // hide virtual keyboard
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.number.windowToken, 0)
                binding.number.clearFocus()
                return@OnEditorActionListener true
            }
            false
        })

    }
    private fun sendString( data : String){
        if (bluetoothSocket.isConnected) {
            val outputStream: OutputStream = bluetoothSocket.outputStream
            outputStream.write(data.toByteArray())
            //outputStream.close()
        } else {
            Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
        }
    }
    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode == Activity.RESULT_OK && result.data != null){
            val data = result.data
            val dataList = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (dataList != null && dataList.isNotEmpty()) {
                val convertedText = dataList[0]
                if(convertedText.isNotEmpty()){
                    sendString(convertedText.toString())
                } else{
                    Log.d("speech" , "Got no data")
                }
            }
            else{
                Log.d("speech" , "Got empth list")
            }
        }
        else{
            Log.d("speech" , "Action cancelled")
        }
    }
    private fun setLanguage(){

        val notSelected = ColorStateList.valueOf(Color.parseColor("#3F51B5"))
        val selected = ColorStateList.valueOf(Color.parseColor("#000000"))

        binding.telugu.backgroundTintList = notSelected
        binding.english.backgroundTintList = notSelected
        binding.hindi.backgroundTintList = notSelected
        if(isHindi){
            binding.hindi.backgroundTintList = selected
        }
        else if(isEnglish){
            binding.english.backgroundTintList  = selected
        }
        else{
            binding.telugu.backgroundTintList = selected
        }
    }

    private fun setSelection(mode: String) {
        val notSelected = ColorStateList.valueOf(Color.parseColor("#3F51B5"))
        val selected = ColorStateList.valueOf(Color.parseColor("#000000"))
        when (mode) {
            "H" -> {
                binding.first.backgroundTintList = selected
                binding.second.backgroundTintList = notSelected
                binding.third.backgroundTintList = notSelected
            }
            "F" -> {
                binding.first.backgroundTintList = notSelected
                binding.second.backgroundTintList = selected
                binding.third.backgroundTintList = notSelected
            }
            else -> {
                binding.first.backgroundTintList = notSelected
                binding.second.backgroundTintList = notSelected
                binding.third.backgroundTintList = selected
            }
        }
        if (bluetoothSocket.isConnected) {
            val outputStream: OutputStream = bluetoothSocket.outputStream
            outputStream.write(mode.toByteArray())
            //outputStream.close()
        } else {
            Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
        }

    }

    private fun speak(text: String) {

        if(isHindi){
            hindiSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        else if(isEnglish){
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

    private fun saveNumber(number : String){
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val myEdit: SharedPreferences.Editor = sharedPreferences.edit()
        myEdit.putString("number", number)
        myEdit.apply()
    }

    private fun getNumber() : String{
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MySharedPref", MODE_PRIVATE)
        return sharedPreferences.getString("number" , "").toString()
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
        val result =  when(value){
            'a' -> {
                "నాకు ఆహారం కావాలి"
            }

            'b' -> {
                "విశ్రాంతి గది"
            }
            'c' -> {
                "నాకు నీరు కావాలి"
            }
            'd' -> {
                "నేను సంతోషంగా ఉన్నాను"
            }

            'e' -> {
                "నేను విచారంగా ఉన్నాను"
            }

            'f' -> {
                "నాకు గాలి అనుభూతినివ్వండి"
            }
            'g' -> {
                "నాకు మందులు ఇవ్వండి"
            }
            'h' -> {
                "నాకు సహాయం కావాలి"
            }

            else -> {
                ""
            }
        }
        if(value == 'z'){
            makeCall()
        }
        return result
    }
    private fun makeCall(){
        val number: String = getNumber()
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number")
        startActivity(callIntent)
    }
    private fun getEnglishString(value : Char): String {
        val result = when (value) {
            'a' -> {
                "i need food"
            }

            'b' -> {
                "rest room"
            }
            'c' -> {
                "i need water"
            }
            'd' -> {
                "i am happy"
            }
            'e' -> {
                "i am sad"
            }

            'f' -> {
                "give me some space"
            }
            'g' -> {
                "give me medicines"
            }
            'h' -> {
                "need help"
            }
            'n' -> {
                "amma"
            }
            'o' -> {
                "nanna"
            }
            else -> {
                ""
            }
        }
        if(value == '%'){
            makeCall()
        }
        return result
    }

    private fun getHindiString(value : Char): String {
        val result = when (value) {
            'a' -> {
                "i need food"
            }

            'b' -> {
                "rest room"
            }
            'c' -> {
                "i need water"
            }
            'd' -> {
                "i am happy"
            }
            'e' -> {
                "i am sad"
            }

            'f' -> {
                "give me some space"
            }
            'g' -> {
                "give me medicines"
            }
            'h' -> {
                "need help"
            }
            'n' -> {
                "amma"
            }
            'o' -> {
                "nanna"
            }
            else -> {
                ""
            }
        }
        if(value == '%'){
            makeCall()
        }
        return result
    }

    private fun launchForSpeech(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        speechLauncher.launch(intent)
    }
    private fun updateData(data: String) {

        if (data.isEmpty()) return;

        val value = data[0]

        if(value == '%'){
            launchForSpeech()
            return
        }
        val displayText = if(isHindi) { getHindiString(value) } else if(isEnglish){
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

