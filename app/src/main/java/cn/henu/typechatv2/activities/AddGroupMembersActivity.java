package cn.henu.typechatv2.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import cn.henu.typechatv2.adapter.AddGroupMembersAdapter;
import cn.henu.typechatv2.adapter.UsersAdapter;
import cn.henu.typechatv2.databinding.ActivityAddGroupMembersBinding;
import cn.henu.typechatv2.listeners.OnMemberAddedListener;
import cn.henu.typechatv2.models.User;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class AddGroupMembersActivity extends AppCompatActivity implements OnMemberAddedListener {
    private ActivityAddGroupMembersBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String groupChatId;
    private List<User> users;
    private AddGroupMembersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddGroupMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        groupChatId = getIntent().getStringExtra(Constants.GROUP_CHAT_ID);

        setListeners();
        loadUsers();

    }

    private void loadUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
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
                            binding.userRecyclerView.setAdapter(new AddGroupMembersAdapter(users, this));
                        }
                    }
                });
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMemberAdded(User user) {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.GROUP_CHAT_COLLECTION)
                .document(groupChatId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        List<String> members = (List<String>) documentSnapshot.get(Constants.GROUP_CHAT_MEMBERS);
                        if (members == null) {
                            members = new ArrayList<>();
                        }
                        members.add(user.id);
                        database.collection(Constants.GROUP_CHAT_COLLECTION)
                                .document(groupChatId)
                                .update(Constants.GROUP_CHAT_MEMBERS, members)
                                .addOnSuccessListener(unused -> {
                                    loading(false);
                                    showToast("Member added successfully");
                                })
                                .addOnFailureListener(e -> {
                                    loading(false);
                                    showToast(e.getMessage());
                                });
                    } else {
                        loading(false);
                        showToast("Something went wrong, please try again later");
                    }
                });

    }
}