package cn.henu.typechatv2.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
        String inputMessage = binding.inputMessage.getText().toString();
        if (inputMessage.trim().isEmpty()) {
            Toast.makeText(GroupChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (!isCurrentUserGroupMember()) {
//            Toast.makeText(GroupChatActivity.this, "You are not a member of this group", Toast.LENGTH_SHORT).show();
//            return;
//        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
        message.put(Constants.GROUP_CHAT_ID, receiverGroup.id);
        message.put(Constants.MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.TIMESTAMP, System.currentTimeMillis());
        //message.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TEXT);
        message.put(Constants.IS_GROUP_CONVERSATION, true);
        database.collection(Constants.GROUP_CHAT_COLLECTION).add(message);
//        if (conversionId != null) {
//            updateConversion(binding.inputMessage.getText().toString());
//        } else {
//            HashMap<String, Object> conversion = new HashMap<>();
//            conversion.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
//            conversion.put(Constants.GROUP_CHAT_ID, receiverGroup.id);
//            conversion.put(Constants.SENDER_NAME, preferenceManager.getString(Constants.USER_NAME));
//            conversion.put(Constants.RECEIVER_NAME, receiverGroup.groupname);
//            conversion.put(Constants.SENDER_IMAGE, preferenceManager.getString(Constants.USER_IMAGE));
//            conversion.put(Constants.RECEIVER_IMAGE, receiverGroup.image);
//            conversion.put(Constants.LAST_MESSAGE, binding.inputMessage.getText().toString());
//            conversion.put(Constants.IS_GROUP_CONVERSATION, true);
//            conversion.put(Constants.TIMESTAMP, new Date());
//            addConversion(conversion);
//        }
        binding.inputMessage.setText(null);
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
        if (conversionId == null) {
            checkForConversion();
        }
    };
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(date);
    }
    private void loadReceiverDetails() {
        receiverGroup = (Group) getIntent().getSerializableExtra(Constants.GROUP);
        assert receiverGroup != null;
        binding.textName.setText(receiverGroup.groupname);
    }
//    private boolean isCurrentUserGroupMember() {
//        List<String> members = (List<String>) database.collection(Constants.GROUP_COLLECTION)
//                .document(receiverGroup.id)
//                .get()
//                .getResult()
//                .get(Constants.GROUP_CHAT_MEMBERS);
//        String currentUserId = preferenceManager.getString(Constants.USER_ID);
//        return members.contains(currentUserId);
//    }
    private void checkForConversion() {
        if (!chatMessages.isEmpty()) {
            checkForConversionRemotely(
                    receiverGroup.id
            );
        }
    }
    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.COLLECTION_CONVERSIONS)
                .add(conversion)
                .addOnSuccessListener(
                        documentReference -> {
                            conversionId = documentReference.getId();
                        }
                );
    }

    private void updateConversion(String message) {
        DocumentReference documentReference =
                database.collection(Constants.COLLECTION_CONVERSIONS)
                        .document(conversionId);
        documentReference.update(
                Constants.LAST_MESSAGE, message,
                Constants.TIMESTAMP, new Date()
        );

    }
    private void checkForConversionRemotely(String receiverId) {
        database.collection(Constants.COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.GROUP_CHAT_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };
    private void setlisteners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutsend.setOnClickListener(v -> sendMessage());
    }
}