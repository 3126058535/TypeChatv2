package cn.henu.typechatv2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import cn.henu.typechatv2.adapter.GroupAdapter;
import cn.henu.typechatv2.databinding.ActivityGroupBinding;
import cn.henu.typechatv2.listeners.GroupListener;
import cn.henu.typechatv2.models.Group;
import cn.henu.typechatv2.utilities.Constants;
import cn.henu.typechatv2.utilities.PreferenceManager;

public class GroupActivity extends AppCompatActivity implements GroupListener {

    ActivityGroupBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getGroup();
    }

    private void setListeners() {
        binding.buttonCreateGroupChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), CreateGroupChat.class)));
        binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
    }
    private void getGroup(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.GROUP_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<Group> groups = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if (currentUserId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            Group group = new Group();
                            group.groupname = documentSnapshot.getString(Constants.GROUP_CHAT_NAME);
                            group.image = documentSnapshot.getString(Constants.GROUP_CHAT_IMAGE);
                            group.id = documentSnapshot.getId();
                            groups.add(group);
                        }
                        if (!groups.isEmpty()){
                            GroupAdapter groupAdapter = new GroupAdapter(groups, this);
                            binding.groupRecyclerView.setAdapter(groupAdapter);
                            binding.groupRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
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
    public void onGroupClicked(Group group) {
        Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
        intent.putExtra(Constants.GROUP, group);
        startActivity(intent);

    }
}