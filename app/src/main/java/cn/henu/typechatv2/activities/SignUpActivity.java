package cn.henu.typechatv2.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.util.Random;

import cn.henu.typechatv2.databinding.ActivitySignUpBinding;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;
import cn.henu.typechatv2.utilities.SendMail;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;
    private String verificationCode;
    private CountDownTimer countDownTimer;

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
        binding.buttonsendcode.setOnClickListener(v ->{
            checkEmailValidity(new EmailValidationCallback() {
                @Override
                public void onCallback(boolean isEmailValid) {
                    if (isEmailValid) {
                        sendcode(binding.inputEmail.getText().toString());
                    } else {
                        showToast("邮箱已存在");
                    }
                }
            });

        });
    }

    @SuppressLint("DefaultLocale")
    private void sendcode(String mail){
        if (mail.isEmpty()) {
            showToast("Please enter your email");
            return;
        }
        verificationCode = String.format("%06d", new Random().nextInt(999999));
        new Thread(() -> SendMail.sendEmail(mail, verificationCode)).start();

        binding.buttonsendcode.setEnabled(false);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.buttonsendcode.setText(String.format("%ds", millisUntilFinished / 1000));
            }
            @Override
            public void onFinish() {
                binding.buttonsendcode.setEnabled(true);
                binding.buttonsendcode.setText("Send Code");
            }
        }.start();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    interface EmailValidationCallback {
        void onCallback(boolean isEmailValid);
    }

    private void checkEmailValidity(EmailValidationCallback callback) {
        String email = binding.inputEmail.getText().toString();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.USERS_COLLECTION)
                .whereEqualTo(Constants.USER_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().isEmpty()) {
                        // 电子邮件地址在数据库中不存在，可以进行注册
                        callback.onCallback(true);
                    } else {
                        // 电子邮件地址在数据库中已存在，提示用户选择其他的电子邮件地址
                        showToast("邮箱已存在");
                        callback.onCallback(false);
                    }
                });
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
        } else if (!binding.inputConfirmCode.getText().toString().equals(verificationCode)) {
            binding.inputConfirmCode.setError("Verification code does not match");
            binding.inputConfirmCode.requestFocus();
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