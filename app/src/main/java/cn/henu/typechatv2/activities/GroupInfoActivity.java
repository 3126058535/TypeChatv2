package cn.henu.typechatv2.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import cn.henu.typechatv2.R;
import cn.henu.typechatv2.databinding.ActivityGroupInfoBinding;

public class GroupInfoActivity extends AppCompatActivity {

    ActivityGroupInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();


    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> finish());
        binding.enter.setOnClickListener(v -> finish());
        binding.deletegroup.setOnClickListener(v -> finish());
        binding.fabNewUser.setOnClickListener(v -> finish());
    }

}