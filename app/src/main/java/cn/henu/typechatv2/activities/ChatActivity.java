package cn.henu.typechatv2.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

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
import java.util.Objects;

import cn.henu.typechatv2.R;
import cn.henu.typechatv2.adapter.ChatAdapter;
import cn.henu.typechatv2.databinding.ActivityChatBinding;
import cn.henu.typechatv2.models.ChatMessage;
import cn.henu.typechatv2.models.User;
import cn.henu.typechatv2.network.ApiClient;
import cn.henu.typechatv2.network.ApiService;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init() {
        Log.d("ChatActivity", "************\nsendMessage1: " + conversionId);
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getReceiverProfileImage(),
                preferenceManager.getString(Constants.USER_ID));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
        message.put(Constants.RECEIVER_ID, receiverUser.id);
        message.put(Constants.MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.TIMESTAMP, System.currentTimeMillis());
        database.collection(Constants.CHAT_COLLECTION).add(message);
        if (conversionId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
            conversion.put(Constants.RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.SENDER_NAME, preferenceManager.getString(Constants.USER_NAME));
            conversion.put(Constants.RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.SENDER_IMAGE, preferenceManager.getString(Constants.USER_IMAGE));
            conversion.put(Constants.RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.TIMESTAMP, new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);
    }

    private void listenAvailability() {
        database.collection(Constants.USERS_COLLECTION)
                .document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null && value.exists()) {
                        if (value.getLong(Constants.USER_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(value.getLong(Constants.USER_AVAILABILITY)).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.token = value.getString(Constants.FCM_TOKEN);
                    }
                    if (isReceiverAvailable) {
                        binding.textAvailability.setVisibility(View.VISIBLE);
                    } else {
                        binding.textAvailability.setVisibility(View.GONE);
                    }


                });
    }

    private void listenMessages() {
        database.collection(Constants.CHAT_COLLECTION)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.CHAT_COLLECTION)
                .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
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
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
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
        Log.d("ChatActivity", "************\nsendMessage2: " + conversionId);
        if (conversionId == null) {
            checkForConversion();
        }
    };
    private Bitmap getReceiverProfileImage() {
        byte[] bytes = Base64.decode(receiverUser.image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra("user");
        assert receiverUser != null;
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutsend.setOnClickListener(v -> sendMessage());

    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(date);
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

    private void checkForConversion() {
        if (!chatMessages.isEmpty()) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.SENDER_ID, senderId)
                .whereEqualTo(Constants.RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void showNotification(String message) {
        // 创建一个通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ChatMessages", "Chat Messages", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // 创建一个点击通知后的动作
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // 解码发送者的头像
        byte[] decodedString = Base64.decode(preferenceManager.getString(Constants.USER_IMAGE), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // 创建一个通知
        @SuppressLint("RestrictedApi") NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ChatMessages")
                .setSmallIcon(Objects.requireNonNull(IconCompat.createFromIcon(Icon.createWithBitmap(decodedByte)))) // 设置应用的通知图标为发送者的头像
                .setContentTitle("New Message") // 通知的标题
                .setContentText(message) // 通知的内容
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 通知的优先级
                .setContentIntent(pendingIntent) // 设置点击通知后的动作
                .setAutoCancel(true); // 设置通知在点击后自动消失

        // 显示通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有，请求这个权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        notificationManager.notify(1, builder.build());
    }
    @Override
    protected void onResume() {
        super.onResume();
        listenAvailability();
    }

}