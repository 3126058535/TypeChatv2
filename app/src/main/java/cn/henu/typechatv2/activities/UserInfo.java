package cn.henu.typechatv2.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import cn.henu.typechatv2.adapter.RecentConversionAdapter;
import cn.henu.typechatv2.databinding.ActivityUserInfoBinding;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class UserInfo extends AppCompatActivity {
    ActivityUserInfoBinding binding;
    PreferenceManager preferenceManager;
    private RecentConversionAdapter recentConversionAdapter;
    private FirebaseFirestore database;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Init();
        setlisteners();

        loadUserDetails();
    }

    private void loadUserDetails() {
        if (preferenceManager.getString(Constants.USER_ID).equals(userId)) {
            loadUserDetails2();
            binding.logout.setVisibility(View.VISIBLE);
        } else {
            loadUserDetails1();
            binding.logout.setVisibility(View.INVISIBLE);
        }
    }

    private void loadUserDetails1() {
        // Get the user details from the database
        if (userId == null) {
            return;
        }
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.nickname.setText(documentSnapshot.getString(Constants.USER_NAME));
                    binding.mail.setText(documentSnapshot.getString(Constants.USER_EMAIL));
                    binding.nickname2.setText(documentSnapshot.getString(Constants.USER_NAME));
                    binding.id.setText(documentSnapshot.getId());
                    byte[] bytes = Base64.decode(documentSnapshot.getString(Constants.USER_IMAGE), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.avatr.setImageBitmap(bitmap);
                });
    }
    private void loadUserDetails2() {
        // Get user details from PreferenceManager
        String userName = preferenceManager.getString(Constants.USER_NAME);
        String userEmail = preferenceManager.getString(Constants.USER_EMAIL);
        String userId = preferenceManager.getString(Constants.USER_ID);
        String userImageBase64 = preferenceManager.getString(Constants.USER_IMAGE);

        // Decode the user image from Base64 to Bitmap
        byte[] bytes = Base64.decode(userImageBase64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Set the user details to the views
        binding.nickname.setText(userName);
        binding.mail.setText(userEmail);
        binding.nickname2.setText(userName);
        binding.avatr.setImageBitmap(bitmap);
        binding.id.setText(userId);
    }

    private void setlisteners() {
        binding.logout.setOnClickListener(v -> signout());
    }

    private void signout(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.USERS_COLLECTION).document(
                        preferenceManager.getString(Constants.USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clearPreferences();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void Init() {
        userId = getIntent().getStringExtra(Constants.USER_ID);
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }


}