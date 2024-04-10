package cn.henu.typechatv2.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.henu.typechatv2.databinding.ActivityCreateGroupChatBinding;
import cn.henu.typechatv2.models.User;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class CreateGroupChat extends AppCompatActivity {
    ActivityCreateGroupChatBinding binding;
    PreferenceManager preferenceManager;
    private ArrayList<String> members;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCreateGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setlisteners();
    }

    private void setlisteners() {
        binding.buttonCreateGroupChat.setOnClickListener(v -> {
            if (isvialidGroupDetails()) {
                createGroupChat();
            }
        });
//        binding.buttonAddMembers.setOnClickListener(v -> openAddMembersActivity());
        binding.layouimage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickerImage.launch(intent);
        });
    }
//    private void openAddMembersActivity() {
//        Intent intent = new Intent(getApplicationContext(), AddGroupMembersActivity.class);
//        //intent.putExtra(Constants.GROUP_CHAT_ID, groupChatId);
//        startActivityForResult(intent, Constants.REQUEST_CODE_ADD_MEMBERS);
//    }
    private void createGroupChat() {
        loading(true);
        members = new ArrayList<>();
        members.add(preferenceManager.getString(Constants.USER_ID));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> group = new HashMap<>();
        group.put(Constants.GROUP_CHAT_NAME, binding.inputGroupName.getText().toString());
        group.put(Constants.GROUP_CHAT_IMAGE, encodedImage);
        group.put(Constants.GROUP_CHAT_MEMBERS, members);
        group.put(Constants.GROUP_CHAT_CREATED_BY, preferenceManager.getString(Constants.USER_ID));
        group.put(Constants.GROUP_CHAT_TIMESTAMP, new Date());
        database.collection(Constants.GROUP_COLLECTION)
                .add(group)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    showToast("Group chat created successfully");
                    Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
        Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
        startActivity(intent);
    }

    private boolean isvialidGroupDetails() {
        if (encodedImage == null) {
            showToast("Please select a profile picture");
            return false;
        } else if (binding.inputGroupName.getText().toString().trim().isEmpty()) {
            showToast("Enter group name");
            return false;
        } else if (binding.inputGroupDescription.getText().toString().trim().isEmpty()) {
            showToast("Enter group description");
            return false;
        }
//        else if (binding.addedMembers.getChildCount() < 2) { // 至少需要 3 个人(包括群主)
//            showToast("You need to add at least 2 members to create a group chat");
//            return false;
//        }
        else {
            return true;
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
                        binding.groupImage.setImageBitmap(bitmap);
                        binding.textUploadImage.setVisibility(View.INVISIBLE);
                        encodedImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );
    private void loading(boolean isLoading){
        if(isLoading){
            binding.buttonCreateGroupChat.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonCreateGroupChat.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}