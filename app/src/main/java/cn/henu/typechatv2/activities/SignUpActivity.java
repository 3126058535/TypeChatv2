package cn.henu.typechatv2.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import java.io.InputStream;
import java.util.HashMap;

import cn.henu.typechatv2.databinding.ActivitySignUpBinding;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setlisteners();
    }
    private void setlisteners() {
        binding.textSignIn.setOnClickListener(v ->onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.layouimage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickerImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.USER_NAME, binding.inputName.getText().toString());
        user.put(Constants.USER_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.USER_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.USER_IMAGE, encodedImage);
        database.collection(Constants.USERS_COLLECTION)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    showToast("User added successfully");
                    preferenceManager.putBoolean(Constants.USER_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.USER_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.USER_EMAIL, binding.inputEmail.getText().toString());
                    preferenceManager.putString(Constants.USER_IMAGE, encodedImage);
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast(e.getMessage());
                });

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
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textUploadImage.setVisibility(View.INVISIBLE);
                        encodedImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if (encodedImage == null){
            showToast("Please select a profile picture");
            return false;
        }else if (binding.inputName.getText().toString().trim().isEmpty()) {
            binding.inputName.setError("Please enter your name");
            binding.inputName.requestFocus();
            //showToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.inputEmail.setError("Please enter your email");
            binding.inputEmail.requestFocus();
            //showToast("Please enter your email");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            binding.inputEmail.setError("Please enter a valid email address");
            binding.inputEmail.requestFocus();
            //showToast("Please enter a valid email address");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            binding.inputPassword.setError("Please enter your password");
            binding.inputPassword.requestFocus();
            //showToast("Please enter your password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            binding.inputConfirmPassword.setError("Please confirm your password");
            binding.inputConfirmPassword.requestFocus();
            //showToast("Please confirm your password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            binding.inputConfirmPassword.setError("Passwords do not match");
            binding.inputConfirmPassword.requestFocus();
            //showToast("Passwords do not match");
            return false;
        } else {
            return true;
        }
    }
    private void loading(boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}