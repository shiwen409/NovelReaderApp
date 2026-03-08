package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.R;

public class AdminMainActivity extends AppCompatActivity {
    private Button btnUserManage;
    private Button btnBookshelfView;
    private Button btnLogout;
    private Button btnReviewManage;
    private Button btnStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        btnUserManage = findViewById(R.id.btn_user_manage);
        btnBookshelfView = findViewById(R.id.btn_bookshelf_view);
        btnReviewManage = findViewById(R.id.btn_review_manage);
        btnLogout = findViewById(R.id.btn_logout);
        btnStat = findViewById(R.id.btn_admin_stat);


        btnUserManage.setOnClickListener(v ->{
                Log.d("AdminMain", "用户管理按钮被点击");
                startActivity(new Intent(AdminMainActivity.this, UserManageActivity.class));
                });

        btnBookshelfView.setOnClickListener(v ->
                startActivity(new Intent(AdminMainActivity.this, UserBookshelfActivity.class)));

        btnLogout.setOnClickListener(v -> {
            AppConfig.isLogin = false;
            AppConfig.currentUsername = "";
            startActivity(new Intent(AdminMainActivity.this, LoginActivity.class));
            finish();
        });

        btnReviewManage.setOnClickListener(v ->
                startActivity(new Intent(AdminMainActivity.this, ReviewManageActivity.class)));

        btnStat.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminStatisticsActivity.class);
            startActivity(intent);
        });
    }
}