package cn.henu.typechatv2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import cn.henu.typechatv2.adapter.UsersAdapter;
import cn.henu.typechatv2.databinding.ActivityUserBinding;
import cn.henu.typechatv2.models.User;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;
import cn.henu.typechatv2.listeners.UserListener;

public class UserActivity extends BaseActivity implements UserListener{
    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setlisteners();
        getUsers();
    }
    private void setlisteners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
    private void getUsers(){
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
                            binding.userRecyclerView.setAdapter(new UsersAdapter(users, this));
                        }else{
                            showErrorMessage();
                        }
                    }else{
                        showErrorMessage();
                    }
                });
    }
        private void showErrorMessage(){
            binding.textNoUserFound.setVisibility(View.VISIBLE);
        }
        private void loading (Boolean isLoading){
            if (isLoading){
                binding.progressBar.setVisibility(View.VISIBLE);
            }else{
                binding.progressBar.setVisibility(View.GONE);
            }

        }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.USER, user);

        startActivity(intent);
        finish();
    }
}