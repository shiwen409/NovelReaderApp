package com.example.novelreaderapp.activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.R;
import com.example.novelreaderapp.entity.User;
import com.example.novelreaderapp.adapter.UserAdapter;
import com.example.novelreaderapp.db.DBManager;

import java.util.List;

public class UserManageActivity extends AppCompatActivity {
    private ListView lvUsers;
    private UserAdapter adapter;
    private DBManager dbManager;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);
        dbManager = new DBManager(this);
        lvUsers = findViewById(R.id.lv_users);

        loadUsers();

    }

    private void loadUsers() {
        new Thread(() -> {
            userList = dbManager.getAllUsers();
            runOnUiThread(() -> {
                // 初始化adapter
                adapter = new UserAdapter(this, userList);
                lvUsers.setAdapter(adapter);
                // 初始化后再设置监听器
                setListeners();
            });
        }).start();
    }

    private void setListeners() {
        adapter.setOnUserActionListener(new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(String username) {
                new Thread(() -> {
                    boolean isDeleted = dbManager.deleteUser(username);
                    runOnUiThread(() -> {
                        if (isDeleted) {
                            Toast.makeText(UserManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(UserManageActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }

            @Override
            public void onEdit(User user) {
                // 这里可以实现编辑用户密码的逻辑
                // 例如弹出对话框输入新密码
                String newPassword = "000000"; // 实际应用中应通过弹窗获取
                new Thread(() -> {
                    boolean isUpdated = dbManager.updateUserPassword(user.getUsername(), newPassword);
                    runOnUiThread(() -> {
                        if (isUpdated) {
                            Toast.makeText(UserManageActivity.this, "密码更新成功", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(UserManageActivity.this, "密码更新失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });
    }
}