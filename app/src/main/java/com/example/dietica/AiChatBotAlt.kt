package com.example.dietica

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AiChatBotAlt : BaseActivity() {

    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_chat_bot_alt)

        functions = FirebaseFunctions.getInstance()

        // Decide whether to use the emulator
        if (false) {
            // Replace "10.0.2.2" with "localhost" if testing on a physical device
            functions.useEmulator("10.0.2.2", 5001)
            Log.d("FirebaseEmulator", "Using Firebase Emulator for Functions")
        }

        initSendButton()
        initChatbot()
    }

    private fun initSendButton() {
        val sendButton = findViewById<ImageButton>(R.id.sendBtn)
        val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        val chatContainer = findViewById<LinearLayout>(R.id.chat_container)
        val scrollView = findViewById<ScrollView>(R.id.chat_scroll)

        sendButton.setOnClickListener {
            val userMessage = editTextMessage.text.toString()
            if (userMessage.isNotBlank()) {
                val userMessageView = layoutInflater.inflate(R.layout.item_user_message, null)
                val textViewUserMessage = userMessageView.findViewById<TextView>(R.id.textViewUserMessage)
                textViewUserMessage.text = userMessage
                chatContainer.addView(userMessageView)

                lifecycleScope.launch {
                    val botMessageView = layoutInflater.inflate(R.layout.item_bot_message, null)
                    val textViewBotMessage = botMessageView.findViewById<TextView>(R.id.textViewBotMessage)
                    textViewBotMessage.text = "..."
                    chatContainer.addView(botMessageView)

                    val reply = getBotResponse(userMessage)
                    val markwon = Markwon.create(this@AiChatBotAlt)
                    markwon.setMarkdown(textViewBotMessage, reply)
                }
            }

            scrollView.post { scrollView.smoothScrollTo(0, scrollView.getChildAt(0).bottom) }
            editTextMessage.text.clear()
        }
    }

    private suspend fun getBotResponse(userMessage: String): String {
        val authId = getSharedPreferences("com.example.dietica", MODE_PRIVATE).getString("authId", null)
        val data = mapOf("authId" to authId, "message" to userMessage)
        return try {
            val result = functions.getHttpsCallable("sendChatbot").call(data).await()
            result.data.toString()
        } catch (e: FirebaseFunctionsException) {
            when (e.code) {
                FirebaseFunctionsException.Code.FAILED_PRECONDITION -> {
                    // Handle the specific "failed-precondition" error
                    Log.e("ChatbotError", "Failed precondition: ${e.message}")
                    "Sorry, but we cannot provide recommendation due to missing information. ${e.message}."
                }
                else -> {
                    // Handle other errors
                    Log.e("ChatbotError", "Unexpected error: ${e.message}")
                    "An error occurred. Please try again later."
                }
            }
        } catch (e: Exception) {
            // Handle generic exceptions
            Log.e("ChatbotError", "General error: ${e.message}")
            "An error occurred. Please try again later."
        }
    }

    private fun initChatbot() {
        val chatContainer = findViewById<LinearLayout>(R.id.chat_container)
        val scrollView = findViewById<ScrollView>(R.id.chat_scroll)

        lifecycleScope.launch {
            val authId = getSharedPreferences("com.example.dietica", MODE_PRIVATE).getString("authId", null)
            val data = mapOf("authId" to authId)

            val result = functions.getHttpsCallable("getChatbot").call(data).await()
            val messages = result.data as? List<*> ?: emptyList<Any>()
            if (messages.isEmpty()) {
                val botMessageView = layoutInflater.inflate(R.layout.item_bot_message, null)

                chatContainer.addView(botMessageView)
            } else {
                for (message in messages) {
                    val messageMap = message as? Map<*, *> ?: continue
                    val messageText = messageMap["message"] as? String ?: continue
                    val botText = messageMap["reply"] as? String ?: continue

                    val userMessageView = layoutInflater.inflate(R.layout.item_user_message, null)
                    val textViewUserMessage = userMessageView.findViewById<TextView>(R.id.textViewUserMessage)
                    textViewUserMessage.text = messageText

                    chatContainer.addView(userMessageView)

                    val botMessageView = layoutInflater.inflate(R.layout.item_bot_message, null)
                    val textViewBotMessage = botMessageView.findViewById<TextView>(R.id.textViewBotMessage)
                    val markwon = Markwon.create(this@AiChatBotAlt)
                    markwon.setMarkdown(textViewBotMessage, botText)

                    chatContainer.addView(botMessageView)
                }
            }

            scrollView.post { scrollView.smoothScrollTo(0, scrollView.getChildAt(0).bottom) }
        }
    }
}