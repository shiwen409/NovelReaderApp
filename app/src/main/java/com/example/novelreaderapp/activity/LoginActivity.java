package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;
    private DBManager dbManager;
    // 主线程Handler，用于更新UI
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        dbManager = new DBManager(this);
        bindListener();
    }

    private void initView() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoRegister = findViewById(R.id.tv_go_register);
    }

    private void bindListener() {
        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 输入校验
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 子线程执行数据库操作（核心优化）
            new Thread(() -> {
                boolean isSuccess = dbManager.loginUser(username, password);
                boolean isAdmin = username.equals("admin") && password.equals("admin001");

                // 主线程更新UI
                mainHandler.post(() -> {
                    if (isSuccess) {
                        Log.d("LoginDebug", "登录成功，用户：" + username);
                        AppConfig.isLogin = true;
                        AppConfig.currentUsername = username;
                        startActivity(new Intent(LoginActivity.this, BookstoreActivity.class));
                        finish();
                    } else if (isAdmin) {
                        // 管理员登录
                        AppConfig.isLogin = true;
                        AppConfig.currentUsername = username;
                        startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "用户名或密码错误！", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        // 注册跳转
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 避免内存泄漏
        mainHandler.removeCallbacksAndMessages(null);
    }
}