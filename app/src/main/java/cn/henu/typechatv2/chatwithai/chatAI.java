package cn.henu.typechatv2.chatwithai;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.henu.typechatv2.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class chatAI extends AppCompatActivity {

    private EditText userInput;
    private RecyclerView recyclerViewChat;
    private ChatAdapterai chatAdapter;
    private List<Messageai> messages;
    private ApiServiceai apiService;
    private Button send;
    private MessageDatabaseHelper messageDatabaseHelper;
    String currentTime = timeUtils.getCurrentTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatai);

        userInput = findViewById(R.id.editTextUserInput);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapterai(messages);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://123.57.181.11:5000")  // 修改为你的 Flask 服务器 IP
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create service interface instance
        apiService = retrofit.create(ApiServiceai.class);

        // Initialize MessageDatabaseHelper
        messageDatabaseHelper = new MessageDatabaseHelper(this);

        send = findViewById(R.id.buttonSendMessage);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMessageClick(v);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Load messages from SQLite database
        loadMessagesFromDatabase();
    }
    private void loadMessagesFromDatabase() {
        // Clear existing messages
        messages.clear();

        // Load messages from SQLite database
        List<String> savedMessages = messageDatabaseHelper.getAllMessages();
        boolean isCurrentUserMessage = true; // Track whether the current message is sent by the user or the server
        for (String message : savedMessages) {
            // Create Message objects from saved messages and add to the list
            messages.add(new Messageai(message, isCurrentUserMessage, currentTime));
            // Toggle between user and server messages
            isCurrentUserMessage = !isCurrentUserMessage;
        }

        // Refresh RecyclerView
        chatAdapter.notifyDataSetChanged();

        // Scroll to the last message
        recyclerViewChat.scrollToPosition(messages.size() - 1);
    }
    public void onSendMessageClick(View view) {
        String messageContent = userInput.getText().toString();
        Messageai userMessage = new Messageai(messageContent, true, currentTime);
        messages.add(userMessage);

        // Save message to SQLite database
        messageDatabaseHelper.addMessage(messageContent);

        // Refresh RecyclerView
        chatAdapter.notifyDataSetChanged();

        // Scroll to the last message
        recyclerViewChat.scrollToPosition(messages.size() - 1);

        // Clear the input box
        userInput.setText("");

        // Create a request object
        ChatRequest request = new ChatRequest(messageContent);

        // Send the request
        Call<ChatResponse> call = apiService.sendMessage(request);
        call.enqueue(new Callback<ChatResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle the server response
                    String serverResponse = response.body().getAnswer();
                    Messageai serverMessage = new Messageai(serverResponse, false, currentTime);
                    messages.add(serverMessage);

                    // Save server message to SQLite database
                    messageDatabaseHelper.addMessage(serverResponse);

                    // Refresh RecyclerView
                    chatAdapter.notifyDataSetChanged();

                    // Scroll to the last message
                    recyclerViewChat.scrollToPosition(messages.size() - 1);
                } else {
                    // Handle the failed request
                    String errorMessage = "Server Error";
                    Messageai errorServerMessage = new Messageai(errorMessage, false, currentTime);
                    messages.add(errorServerMessage);

                    // Save error message to SQLite database
                    messageDatabaseHelper.addMessage(errorMessage);

                    // Refresh RecyclerView
                    chatAdapter.notifyDataSetChanged();

                    // Scroll to the last message
                    recyclerViewChat.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                // Handle request failure
                String errorMessage = "Request Failed: " + t.getMessage();

                Messageai errorServerMessage = new Messageai(errorMessage, false, currentTime);
                messages.add(errorServerMessage);

                // Save error message to SQLite database
                messageDatabaseHelper.addMessage(errorMessage);

                // Refresh RecyclerView
                chatAdapter.notifyDataSetChanged();

                // Scroll to the last message
                recyclerViewChat.scrollToPosition(messages.size() - 1);
            }
        });
    }
}

