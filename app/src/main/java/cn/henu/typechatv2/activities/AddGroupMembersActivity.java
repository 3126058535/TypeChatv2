package cn.henu.typechatv2.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import cn.henu.typechatv2.adapter.AddGroupMembersAdapter;
import cn.henu.typechatv2.databinding.ActivityAddGroupMembersBinding;
import cn.henu.typechatv2.listeners.OnMemberAddedListener;
import cn.henu.typechatv2.models.User;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class AddGroupMembersActivity extends AppCompatActivity implements OnMemberAddedListener {
    private ActivityAddGroupMembersBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<User> SelectMembers;
    private AddGroupMembersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddGroupMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SelectMembers = new ArrayList<>(); // 初始化 addedMembers
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setListeners();
        loadUsers();
    }
    private void loadUsers() {
        loading(true);
        database.collection(Constants.USERS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if (currentUserId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.USER_NAME);
                            user.email = documentSnapshot.getString(Constants.USER_EMAIL);
                            user.image = documentSnapshot.getString(Constants.USER_IMAGE);
                            user.token = documentSnapshot.getString(Constants.FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                        }
                        if (!users.isEmpty()){
                            binding.textNoUserFound.setVisibility(View.GONE);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                            adapter = new AddGroupMembersAdapter(users, AddGroupMembersActivity.this);
                            binding.userRecyclerView.setAdapter(adapter);
                        }
                    }
                });
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.confirmButton.setOnClickListener(v -> {
            if (!SelectMembers.isEmpty()){
                Intent intent = new Intent();
                intent.putExtra(Constants.GROUP_CHAT_MEMBERS, (CharSequence) SelectMembers);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Select at least one member", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onMemberAdded(List<User> users) {
        this.SelectMembers = users;
        adapter.notifyDataSetChanged();
    }
}
