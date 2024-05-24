package cn.henu.typechatv2.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import android.view.View;
import android.widget.Toast;


import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import cn.henu.typechatv2.adapter.RecentConversionAdapter;
import cn.henu.typechatv2.databinding.ActivityMainBinding;
import cn.henu.typechatv2.listeners.ConversionListener;
import cn.henu.typechatv2.models.ChatMessage;
import cn.henu.typechatv2.models.Group;
import cn.henu.typechatv2.models.User;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class MainActivity extends BaseActivity implements ConversionListener {
    private ActivityMainBinding binding;
    PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversionAdapter recentConversionAdapter;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        Init();
        loadUserDetails();
        getToken();
        setlisteners();
        listenConversions();
    }

    private void Init() {
        conversations = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        recentConversionAdapter = new RecentConversionAdapter(conversations, this);
        binding.conversionRecyclerView.setAdapter(recentConversionAdapter);
    }

    private void setlisteners() {
        binding.imageLogout.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ShowAPP.class)));
        binding.fabNewUser.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UserActivity.class)));
        binding.buttonToGroupChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), GroupActivity.class)));
        Intent intent = new Intent(this, UserInfo.class);
        intent.putExtra(Constants.USER_ID, preferenceManager.getString(Constants.USER_ID)); // userId 是你要显示的用户的ID
        binding.imageProfile.setOnClickListener(v ->
                startActivity(intent));
    }


    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.USER_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.USER_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversions(){
        database.collection(Constants.COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if(preferenceManager.getString(Constants.USER_ID).equals(senderId)){
                        chatMessage.conversionId = receiverId;
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.RECEIVER_NAME);
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.RECEIVER_IMAGE);
                    } else {
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.SENDER_ID);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.SENDER_NAME);
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.SENDER_IMAGE);

                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    conversations.add(chatMessage);
                } else if( documentChange.getType() == DocumentChange.Type.MODIFIED){

                    for(int i = 0; i < conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) &&
                                conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConversionAdapter.notifyDataSetChanged();
            binding.conversionRecyclerView.smoothScrollToPosition(0);
            binding.conversionRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            
        }
    };
    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    // Log and toast
                    updateToken(token);
                });
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.USERS_COLLECTION).document(
                        preferenceManager.getString(Constants.USER_ID)
                );
        documentReference.update(Constants.FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

//    private void signout(){
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        DocumentReference documentReference =
//                database.collection(Constants.USERS_COLLECTION).document(
//                        preferenceManager.getString(Constants.USER_ID)
//                );
//        HashMap<String, Object> updates = new HashMap<>();
//        updates.put(Constants.FCM_TOKEN, FieldValue.delete());
//        documentReference.update(updates)
//                .addOnSuccessListener(unused -> {
//                    preferenceManager.clearPreferences();
//                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
//                    finish();
//                })
//                .addOnFailureListener(e -> showToast("Unable to sign out"));
//    }

    @Override
    public void onUserConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    public void onGroupConversionClicked(Group group) {
        Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
        intent.putExtra(Constants.GROUP, group);
        startActivity(intent);
    }
}