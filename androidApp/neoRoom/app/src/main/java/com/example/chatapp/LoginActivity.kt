package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.connectButton.setOnClickListener {
            val username = binding.nameEditText.text.toString().trim()
            if (validateUsername(username)) {
                startChatActivity(username)
            }
        }
    }

    private fun validateUsername(username: String): Boolean {
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        }

        if (username.equals("SERVER", ignoreCase = true)) {
            Toast.makeText(this, "The name SERVER is not allowed", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun startChatActivity(username: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}