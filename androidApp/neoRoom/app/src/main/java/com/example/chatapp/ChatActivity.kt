package com.example.chatapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.databinding.ActivityChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private val isConnected = AtomicBoolean(false)

    private lateinit var username: String
    private val colorMap = HashMap<String, Int>()
    private val colors = listOf(
        0xFFE57373.toInt(), // Red
        0xFF64B5F6.toInt(), // Blue
        0xFFAED581.toInt(), // Green
        0xFFFFD54F.toInt(), // Yellow
        0xFFBA68C8.toInt(), // Purple
        0xFF4DB6AC.toInt(), // Teal
        0xFFFFB74D.toInt(), // Orange
        0xFF9575CD.toInt()  // Deep Purple
    )
    private var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username") ?: "User"
        setupRecyclerView()
        setupUI()
        connectToServer()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages, username)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupUI() {
        binding.toolbar.title = "Chat Room"
        binding.toolbar.setNavigationOnClickListener {
            disconnect()
            finish()
        }

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.messageEditText.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun connectToServer() {
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = Socket("51.21.195.200", 6969) // use 10.0.2.2 for Android emulator to reach host loopback
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = PrintWriter(socket!!.getOutputStream(), true)

                writer?.println(username)

                isConnected.set(true)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.statusText.text = "Connected as $username"
                }

                receiveMessages()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.statusText.text = "Connection failed"
                    Toast.makeText(this@ChatActivity, "Failed to connect: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun receiveMessages() {
        while (isConnected.get()) {
            try {
                val message = reader?.readLine()
                if (message != null) {
                    val parsedMessage = parseMessage(message)
                    withContext(Dispatchers.Main) {
                        messages.add(parsedMessage)
                        adapter.notifyItemInserted(messages.size - 1)
                        binding.recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            } catch (e: Exception) {
                if (isConnected.get()) {
                    withContext(Dispatchers.Main) {
                        binding.statusText.text = "Connection lost"
                        Toast.makeText(this@ChatActivity, "Connection error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    disconnect()
                }
                break
            }
        }
    }

    private fun parseMessage(message: String): ChatMessage {
        val colonIndex = message.indexOf(':')
        return if (colonIndex > 0) {
            val sender = message.substring(0, colonIndex).trim()
            val content = message.substring(colonIndex + 1).trim()

            if (!colorMap.containsKey(sender)) {
                colorMap[sender] = colors[colorIndex % colors.size]
                colorIndex++
            }

            ChatMessage(sender, content, colorMap[sender] ?: colors[0])
        } else {
            ChatMessage("System", message, 0xFF757575.toInt())
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageEditText.text.toString().trim()
        if (messageText.isNotEmpty() && isConnected.get()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    writer?.println(messageText)
                    withContext(Dispatchers.Main) {
                        binding.messageEditText.text.clear()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun disconnect() {
        isConnected.set(false)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reader?.close()
                writer?.close()
                socket?.close()
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}