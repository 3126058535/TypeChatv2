package cn.henu.typechatv2.chatwithai;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
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
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;
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
    private String userId;
    private PreferenceManager preferenceManager;
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
                .baseUrl("http://123.57.181.11:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create service interface instance
        apiService = retrofit.create(ApiServiceai.class);

        preferenceManager = new PreferenceManager(getApplicationContext());
        userId = preferenceManager.getString(Constants.USER_ID);
        Log.d("chatai", "onCreate: "+userId);
        messageDatabaseHelper = new MessageDatabaseHelper(this);
        messageDatabaseHelper.createUserTable(userId);

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
        loadMessagesFromDatabase();
    }
    private void loadMessagesFromDatabase() {
        messages.clear();
        List<String> savedMessages = messageDatabaseHelper.getAllMessages(userId);
        boolean isCurrentUserMessage = true;
        for (String message : savedMessages) {
            messages.add(new Messageai(message, isCurrentUserMessage, currentTime));
            isCurrentUserMessage = !isCurrentUserMessage;
        }
        chatAdapter.notifyDataSetChanged();
        recyclerViewChat.scrollToPosition(messages.size() - 1);
    }
    public void onSendMessageClick(View view) {
        String messageContent = userInput.getText().toString();
        Messageai userMessage = new Messageai(messageContent, true, currentTime);
        messages.add(userMessage);
        messageDatabaseHelper.addMessage(userId,messageContent);
        chatAdapter.notifyDataSetChanged();
        recyclerViewChat.scrollToPosition(messages.size() - 1);
        userInput.setText("");
        ChatRequest request = new ChatRequest(messageContent);
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
                    messageDatabaseHelper.addMessage(userId, serverResponse);
                    chatAdapter.notifyDataSetChanged();
                    recyclerViewChat.scrollToPosition(messages.size() - 1);
                } else {
                    String errorMessage = "服务器未响应，请稍后再试";
                    Log.e("Retrofit Request", errorMessage);
                    Messageai errorServerMessage = new Messageai(errorMessage, false, currentTime);
                    messages.add(errorServerMessage);
                    messageDatabaseHelper.addMessage(userId, errorMessage);
                    chatAdapter.notifyDataSetChanged();
                    recyclerViewChat.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {

                String errorMessage = "Request Failed: " + t.getMessage();

                Messageai errorServerMessage = new Messageai(errorMessage, false, currentTime);
                messages.add(errorServerMessage);
                messageDatabaseHelper.addMessage(userId, errorMessage);
                chatAdapter.notifyDataSetChanged();

                // Scroll to the last message
                recyclerViewChat.scrollToPosition(messages.size() - 1);
            }
        });
    }
}

