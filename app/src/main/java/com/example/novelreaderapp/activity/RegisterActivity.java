package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername;
    private EditText etPwd;
    private EditText etConfirmPwd;
    private Button btnRegister;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        dbManager = new DBManager(this);
        bindListener();
    }

    private void initView() {
        etUsername = findViewById(R.id.et_register_username);
        etPwd = findViewById(R.id.et_register_pwd);
        etConfirmPwd = findViewById(R.id.et_register_confirm_pwd);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void bindListener() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String pwd = etPwd.getText().toString().trim();
                String confirmPwd = etConfirmPwd.getText().toString().trim();

                // 校验管理员账户不能注册
                if (username.equals("admin")) {
                    Toast.makeText(RegisterActivity.this, "该用户名不可注册", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 校验输入
                if (username.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "请填写完整信息！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pwd.equals(confirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "两次密码不一致！", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 注册用户
                boolean isSuccess = dbManager.registerUser(username, pwd);
                if (isSuccess) {
                    Toast.makeText(RegisterActivity.this, R.string.toast_register_success, Toast.LENGTH_SHORT).show();
                    // 注册成功，跳回登录页面
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "用户名已存在！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
