package com.example.dietica

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AIChatBot : BaseActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_aichat_bot)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewChat)
        val editTextMessage: EditText = findViewById(R.id.editTextMessage)
        val buttonSend: ImageButton = findViewById(R.id.sendBtn)

        chatAdapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        buttonSend.setOnClickListener {
            val userMessage = editTextMessage.text.toString()
            if (userMessage.isNotBlank()) {
                messages.add(Message(userMessage, isUser = true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                editTextMessage.text.clear()

                // Placeholder for bot response
                messages.add(Message("This is a bot response.", isUser = false))
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }
}
