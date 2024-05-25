package cn.henu.typechatv2.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import cn.henu.typechatv2.databinding.ActivityShowAppBinding;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ShowAPP extends AppCompatActivity {

    ActivityShowAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        // 为邮箱文本添加点击事件
        binding.contactInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个意图，打开邮箱应用
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:mengtianyoo@gmail.com")); // 替换成你的邮箱地址
                startActivity(intent);
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
//                } else {
//                    // 处理没有找到相应应用的情况
//                    Toast.makeText(getApplicationContext(), "未找到邮箱应用", Toast.LENGTH_SHORT).show();
//                }
            }
        });

// 为 GitHub 文本添加点击事件
        binding.githubInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个意图，打开浏览器并跳转到 GitHub 链接
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/3126058535")); // 替换成你的 GitHub 链接
                startActivity(intent);
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
//                } else {
//                    // 处理没有找到相应应用的情况
//                    Toast.makeText(getApplicationContext(), "未找到浏览器应用", Toast.LENGTH_SHORT).show();
//                }
            }
        });


    }

}