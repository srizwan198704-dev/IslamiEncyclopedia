package com.srizwan.islamipedia

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.HashMap

class ErrorActivity : AppCompatActivity() {
    
    private lateinit var send: RequestNetwork
    private lateinit var errorMessage: String
    
    // Updated Bot Token and Chat ID
    private val BOT_TOKEN = "8513295796:AAEfeaGo-O29kSk-4zCxcUiB3eU-GRPbtGw"
    private val CHAT_ID = "7619923490"
    private val EMAIL_ADDRESS = "muhammodrizwan01@gmail.com"
    private val WHATSAPP_NUMBER = "8801714656343"
    
    private val sendRequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {
            runOnUiThread {
                Toast.makeText(this@ErrorActivity, "✓ Message sent successfully!", Toast.LENGTH_SHORT).show()
            }
        }
        
        override fun onErrorResponse(tag: String, errorMessage: String) {
            runOnUiThread {
                Toast.makeText(this@ErrorActivity, "✗ Failed to send: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        
        send = RequestNetwork(this)
        errorMessage = intent.getStringExtra("error_message") ?: "Unknown error occurred"
        
        val errorTextView = findViewById<TextView>(R.id.error_text)
        errorTextView.text = "😔 Sorry!\n\n$errorMessage"
        
        val retryButton = findViewById<Button>(R.id.retry)
        val developerButton = findViewById<Button>(R.id.developer)
        
        retryButton.setOnClickListener {
            val intent = Intent(this, Main0Activity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        
        developerButton.setOnClickListener { showReportDialog() }
    }
    
    private fun showReportDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report, null)
        
        val editTextMessage = dialogView.findViewById<EditText>(R.id.editText)
        val sendButton = dialogView.findViewById<Button>(R.id.sendButton)
        val emailButton = dialogView.findViewById<Button>(R.id.emailButton)
        val whatsappButton = dialogView.findViewById<Button>(R.id.whatsapp)
        val telegramButton = dialogView.findViewById<Button>(R.id.telegramButton)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Send to Telegram (via API)
        sendButton.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                val fullMessage = "📱 App Error Report\n━━━━━━━━━━━━━━━━\n👤 User Message: $message\n\n⚠️ Error: $errorMessage\n━━━━━━━━━━━━━━━━\n📅 Time: ${System.currentTimeMillis()}"
                val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage?chat_id=$CHAT_ID&text=${Uri.encode(fullMessage)}"
                send.startRequestNetwork(RequestNetworkController.POST, url, "Telegram", sendRequestListener)
                dialog.dismiss()
            } else {
                editTextMessage.error = "Please enter your message"
                Toast.makeText(this, "Please write something before sending", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Send to Email
        emailButton.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            val subject = "App Issue Report - Islamipedia"
            val body = """
                |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                |📱 ISLAMIPEDIA ERROR REPORT
                |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                |
                |📝 User Message:
                |$message
                |
                |⚠️ Error Details:
                |$errorMessage
                |
                |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                |📅 Date: ${java.util.Date()}
                |🆔 Device: ${android.os.Build.MODEL}
                |🤖 Android: ${android.os.Build.VERSION.RELEASE}
                |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            """.trimMargin()
            
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            
            try {
                startActivity(Intent.createChooser(emailIntent, "Send email via"))
                dialog.dismiss()
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Send to WhatsApp
        whatsappButton.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            val fullMessage = if (message.isNotEmpty()) {
                "*App Error Report*\n\n📝 *Message:* $message\n\n⚠️ *Error:* $errorMessage"
            } else {
                "*App Error Report*\n\n⚠️ *Error:* $errorMessage"
            }
            
            val url = "https://wa.me/$WHATSAPP_NUMBER?text=${Uri.encode(fullMessage)}"
            val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            
            try {
                startActivity(whatsappIntent)
                dialog.dismiss()
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Send to Telegram (Direct Chat)
        telegramButton.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            val fullMessage = if (message.isNotEmpty()) {
                "📱 App Error Report\n━━━━━━━━━━━━━━━━\n📝 User Message: $message\n\n⚠️ Error: $errorMessage"
            } else {
                "📱 App Error Report\n━━━━━━━━━━━━━━━━\n⚠️ Error: $errorMessage"
            }
            
            val telegramUrl = "https://t.me/share?url=&text=${Uri.encode(fullMessage)}"
            val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
            
            try {
                startActivity(telegramIntent)
                dialog.dismiss()
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(this, "Telegram is not installed", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
}