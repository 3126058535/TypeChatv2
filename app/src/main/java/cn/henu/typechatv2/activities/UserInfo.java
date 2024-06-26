package cn.henu.typechatv2.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import cn.henu.typechatv2.adapter.RecentConversionAdapter;
import cn.henu.typechatv2.databinding.ActivityUserInfoBinding;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;



public class UserInfo extends AppCompatActivity {
    ActivityUserInfoBinding binding;
    PreferenceManager preferenceManager;

    private String encodedImage;
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
        loadUserDetails1();
        if (preferenceManager.getString(Constants.USER_ID).equals(userId)) {
            binding.logout.setVisibility(View.VISIBLE);
        } else {
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
                    binding.instruction.setText(documentSnapshot.getString(Constants.USER_ABOUT));
                    binding.age1.setText(documentSnapshot.getString(Constants.USER_AGE));
                    binding.gender1.setText(documentSnapshot.getString(Constants.USER_GENDER));
                    binding.dob1.setText(documentSnapshot.getString(Constants.USER_BIRTHDAY));
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
        if (preferenceManager.getString(Constants.USER_ID).equals(userId)) {
            binding.logout.setOnClickListener(v -> signout());
            binding.avatr.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickerImage.launch(intent);
            } );
            binding.username.setOnClickListener(v -> showUpdateDialog(Constants.USER_NAME, binding.nickname.getText().toString()));
            binding.about.setOnClickListener(v -> showUpdateDialog(Constants.USER_ABOUT, binding.instruction.getText().toString()));
            binding.age.setOnClickListener(v -> showUpdateDialog(Constants.USER_AGE, binding.age1.getText().toString()));
            binding.gender.setOnClickListener(v -> showUpdateDialog(Constants.USER_GENDER, binding.gender1.getText().toString()));
            binding.dob.setOnClickListener(v -> showUpdateDialog(Constants.USER_BIRTHDAY, binding.dob1.getText().toString()));
            binding.logout.setVisibility(View.VISIBLE);
        } else {
            binding.logout.setVisibility(View.INVISIBLE);
        }
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap preview = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        preview.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickerImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        assert imageUri != null;
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.avatr.setImageBitmap(bitmap);
                        saveImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );
    private void saveImage(Bitmap bitmap) {
        // Step 1: Convert the image to Base64 string
        String encodedImage = encodeImage(bitmap);

        // Step 2: Save the Base64 string to local storage
        preferenceManager.putString(Constants.USER_IMAGE, encodedImage);

        // Step 3: Save the Base64 string to Firebase database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.USERS_COLLECTION).document(
                        preferenceManager.getString(Constants.USER_ID)
                );
        documentReference.update(Constants.USER_IMAGE, encodedImage)
                .addOnSuccessListener(unused -> showToast("Image saved successfully"))
                .addOnFailureListener(e -> showToast("Unable to save image"));
    }
    private void showUpdateDialog(String key, String oldValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update " + key);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(oldValue);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> updateUserInfo(key, input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void updateUserInfo(String key, String newValue) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.USERS_COLLECTION).document(
                        preferenceManager.getString(Constants.USER_ID)
                );
        documentReference.update(key, newValue)
                .addOnSuccessListener(unused -> {
                    // 更新成功，重新加载用户信息
                    loadUserDetails();
                })
                .addOnFailureListener(e -> showToast("Unable to update " + key));
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