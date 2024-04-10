package cn.henu.typechatv2.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import cn.henu.typechatv2.adapter.GroupChatAdapter;
import cn.henu.typechatv2.databinding.ActivityGroupChatBinding;
import cn.henu.typechatv2.models.ChatMessage;
import cn.henu.typechatv2.models.Group;
import cn.henu.typechatv2.models.User;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class GroupChatActivity extends AppCompatActivity {
    ActivityGroupChatBinding binding;
    private Group receiverGroup;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private GroupChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setlisteners();
        loadReceiverDetails();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new GroupChatAdapter(
                chatMessages,
                preferenceManager.getString(Constants.USER_ID), database);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
        message.put(Constants.GROUP_CHAT_ID, receiverGroup.id);
        message.put(Constants.MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.TIMESTAMP, System.currentTimeMillis());
        database.collection(Constants.GROUP_CHAT_COLLECTION).add(message);
//        if (conversionId != null) {
//            updateConversion(binding.inputMessage.getText().toString());
//        } else {
//            HashMap<String, Object> conversion = new HashMap<>();
//            conversion.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
//            conversion.put(Constants.RECEIVER_ID, receiverUser.id);
//            conversion.put(Constants.SENDER_NAME, preferenceManager.getString(Constants.USER_NAME));
//            conversion.put(Constants.RECEIVER_NAME, receiverUser.name);
//            conversion.put(Constants.SENDER_IMAGE, preferenceManager.getString(Constants.USER_IMAGE));
//            conversion.put(Constants.RECEIVER_IMAGE, receiverUser.image);
//            conversion.put(Constants.LAST_MESSAGE, binding.inputMessage.getText().toString());
//            conversion.put(Constants.TIMESTAMP, new Date());
//            addConversion(conversion);
//        }
        binding.inputMessage.setText(null);
    }
    private Bitmap getmemberImage() {
        //再GROUP_CHAT_COLLECTION中查找receiverGroup.id对应的文档，将其中的senderId字段的值赋给receiverUser
        //receiverUser = new User();
        //receiverUser.id = database.collection(Constants.GROUP_CHAT_COLLECTION).document(receiverGroup.id).get().getResult().getString(Constants.SENDER_ID);
        //再通过receiverUser查找USER_COLLECTION中对应的文档，将其中的image字段的值赋给receiverUser
        //receiverUser.image = database.collection(Constants.USERS_COLLECTION).document(receiverUser.id).get().getResult().getString(Constants.USER_IMAGE);
        byte[] bytes = Base64.decode(receiverGroup.image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    private void listenMessages() {
        database.collection(Constants.GROUP_CHAT_COLLECTION)
                .whereEqualTo(Constants.GROUP_CHAT_ID, receiverGroup.id)
                .addSnapshotListener(eventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.GROUP_CHAT_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.MESSAGE);
                    Long timeStamp = documentChange.getDocument().getLong(Constants.TIMESTAMP);
                    if (timeStamp != null) {
                        chatMessage.dateObject = new Date(timeStamp);
                        chatMessage.dateTime = getReadableDateTime(chatMessage.dateObject);
                    }
                    chatMessages.add(chatMessage);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        showNotification(chatMessage.message);
//                    }

                }
            }
            chatMessages.sort(Comparator.comparing(obj -> obj.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
//        Log.d("ChatActivity", "************\nsendMessage2: " + conversionId);
//        if (conversionId == null) {
//            checkForConversion();
//        }
    };
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(date);
    }
    private void loadReceiverDetails() {
        receiverGroup = (Group) getIntent().getSerializableExtra(Constants.GROUP);
        assert receiverGroup != null;
        binding.textName.setText(receiverGroup.groupname);
    }

    private void setlisteners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutsend.setOnClickListener(v -> sendMessage());
    }
}