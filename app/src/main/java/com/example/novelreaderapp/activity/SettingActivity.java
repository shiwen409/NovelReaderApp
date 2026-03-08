package com.example.novelreaderapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;
import com.example.novelreaderapp.utils.BDLocationUtils;


public class SettingActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView tvUsername; // 用户名显示
    private EditText etArea;     // 地区输入框
    private TextView tvCurrentLocation; // 当前位置显示
    private Button btnAutoLocation; // 自动获取位置按钮
    private Button btnLogout; // 退出登录按钮

    private BDLocationUtils locationUtils;

    private Button btnChangeUsername;   // 修改用户名按钮
    private Button btnChangePassword;   // 修改密码按钮
    private Button btnDeleteAccount;    // 注销账号按钮
    private DBManager dbManager;// 数据库管理实例
    private TextView tvNavBookstore, tvNavBookshelf, tvNavSetting;



    private static final int REQUEST_LOCATION_PERMISSION = 1001; // 定位权限请求码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // 初始化控件
        initView();
        // 初始化定位工具类
        locationUtils = new BDLocationUtils(this);
        // 根据登录状态控制按钮显示
        updateLogoutBtnVisibility();
        // 显示当前用户名
        updateUsernameDisplay();
        // 绑定点击事件
        bindListener();

        // 初始化数据库管理类
        dbManager = new DBManager(this);

        initBottomNav();
    }

    // 初始化底部导航
    private void initBottomNav() {
        tvNavBookstore = findViewById(R.id.tv_nav_bookstore);
        tvNavBookshelf = findViewById(R.id.tv_nav_bookshelf);
        tvNavSetting = findViewById(R.id.tv_nav_setting);

        // 设置当前页面导航高亮（可选）
        tvNavSetting.setTextColor(getResources().getColor(R.color.colorAccent));

        // 绑定导航事件
        bindBottomNavListener();
    }

    // 底部导航点击事件
    private void bindBottomNavListener() {
        tvNavBookstore.setOnClickListener(v -> {
            startActivity(new Intent(this, BookstoreActivity.class));
            finish();
        });

        tvNavBookshelf.setOnClickListener(v -> {
            startActivity(new Intent(this, BookshelfActivity.class));
            finish();
        });

        tvNavSetting.setOnClickListener(v -> {
            // 当前已是设置页，可忽略
        });
    }

    // 初始化控件
    private void initView() {
        // 位置相关控件
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        btnAutoLocation = findViewById(R.id.btn_auto_location);

        // 登录/退出相关控件
        btnLogout = findViewById(R.id.btn_logout);
        btnLogin = findViewById(R.id.btn_login);

        // 新增个人信息控件
        tvUsername = findViewById(R.id.tv_username);
        etArea = findViewById(R.id.et_area);

        // 新增三个功能按钮
        btnChangeUsername = findViewById(R.id.btn_change_username);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);
    }

    // 根据登录状态更新按钮可见性
    private void updateLogoutBtnVisibility() {
        if (AppConfig.isLogin) {
            // 登录状态：显示退出登录按钮和三个功能按钮，隐藏登录按钮
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            btnChangeUsername.setVisibility(View.VISIBLE);
            btnChangePassword.setVisibility(View.VISIBLE);
            btnDeleteAccount.setVisibility(View.VISIBLE);
        } else {
            // 未登录状态：隐藏所有功能按钮，显示登录按钮
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnChangeUsername.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);
            btnDeleteAccount.setVisibility(View.GONE);
        }
    }

    // 更新用户名显示
    private void updateUsernameDisplay() {
        if (AppConfig.isLogin && !AppConfig.currentUsername.isEmpty()) {
            tvUsername.setText("用户名：" + AppConfig.currentUsername);
        } else {
            tvUsername.setText("用户名：未登录");
        }
    }

    // 绑定点击事件
    private void bindListener() {
        // 1. 自动获取位置按钮
        btnAutoLocation.setOnClickListener(v -> {
            // 检查定位权限（Android 6.0+ 需动态申请）
            if (ActivityCompat.checkSelfPermission(SettingActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 未授权，申请权限
                ActivityCompat.requestPermissions(
                        SettingActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION
                );
                return;
            }
            // 已授权，开始定位
            startLocation();
        });

        // 2. 退出登录按钮
        btnLogout.setOnClickListener(v -> {
            // 更新全局登录状态
            AppConfig.isLogin = false;
            AppConfig.currentUsername = "";
            // 提示退出成功
            Toast.makeText(SettingActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
            // 跳转到登录页，关闭当前页
            startActivity(new Intent(SettingActivity.this, LoginActivity.class));
            finish();
        });

        // 3. 登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            // 跳转到登录页
            startActivity(new Intent(SettingActivity.this, LoginActivity.class));
            finish(); // 关闭当前设置页
        });

        // 4. 修改用户名按钮
        btnChangeUsername.setOnClickListener(v -> {
            showChangeUsernameDialog();
        });

        // 5. 修改密码按钮
        btnChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // 6. 注销账号按钮
        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
    }

    /**
     * 显示修改用户名对话框
     */
    private void showChangeUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改用户名");

        // 创建输入框
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("请输入新用户名");
        builder.setView(input);

        // 设置按钮
        builder.setPositiveButton("确认", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(SettingActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 调用DBManager修改用户名
            boolean success = dbManager.updateUsername(AppConfig.currentUsername, newUsername);
            if (success) {
                // 更新全局用户名
                AppConfig.currentUsername = newUsername;
                updateUsernameDisplay();
                Toast.makeText(SettingActivity.this, "用户名修改成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, "用户名已存在或修改失败", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改密码");

        // 创建布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        final EditText etOldPwd = view.findViewById(R.id.et_old_password);
        final EditText etNewPwd = view.findViewById(R.id.et_new_password);
        builder.setView(view);

        // 设置按钮
        builder.setPositiveButton("确认", (dialog, which) -> {
            String oldPwd = etOldPwd.getText().toString().trim();
            String newPwd = etNewPwd.getText().toString().trim();

            if (oldPwd.isEmpty() || newPwd.isEmpty()) {
                Toast.makeText(SettingActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 先验证旧密码是否正确
            if (dbManager.loginUser(AppConfig.currentUsername, oldPwd)) {
                // 旧密码正确，更新新密码
                boolean success = dbManager.updateUserPassword(AppConfig.currentUsername, newPwd);
                if (success) {
                    Toast.makeText(SettingActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SettingActivity.this, "旧密码不正确", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * 显示注销账号对话框
     */
    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        builder.setMessage("确定要注销账号吗？此操作不可恢复！");

        builder.setPositiveButton("确认", (dialog, which) -> {
            // 执行注销操作
            boolean success = dbManager.deleteUser(AppConfig.currentUsername);
            if (success) {
                // 注销成功，更新登录状态
                AppConfig.isLogin = false;
                AppConfig.currentUsername = "";
                Toast.makeText(SettingActivity.this, "账号注销成功", Toast.LENGTH_SHORT).show();
                // 跳转到登录页
                startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(SettingActivity.this, "账号注销失败", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // 开始定位（调用LocationUtils）
    private void startLocation() {
        tvCurrentLocation.setText("正在获取位置...");
        locationUtils.setOnLocationListener(new BDLocationUtils.OnLocationListener() {
            @Override
            public void onSuccess(String cityName) {
                tvCurrentLocation.setText(String.format("当前位置：%s", cityName));
                // 将定位到的城市自动填入地区输入框
                etArea.setText(cityName);
                Toast.makeText(SettingActivity.this, "定位成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMsg) {
                tvCurrentLocation.setText("定位失败");
                Toast.makeText(SettingActivity.this, errorMsg, Toast.LENGTH_SHORT).show();

            }
        });
        locationUtils.startLocation();
    }

    // 权限申请结果回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限申请通过，开始定位
                startLocation();
            } else {
                // 权限申请拒绝，提示用户
                Toast.makeText(this, "需要定位权限才能获取位置", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 页面销毁时停止定位，避免内存泄漏
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationUtils != null) {
            locationUtils.stopLocation();
            locationUtils.destroyLocation();
        }
    }
}