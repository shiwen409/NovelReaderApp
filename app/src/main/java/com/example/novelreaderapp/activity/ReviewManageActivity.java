package com.example.novelreaderapp.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.entity.BookReview;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewManageActivity extends AppCompatActivity {
    private ListView lvAllReviews;
    private Spinner spUsers;
    private DBManager dbManager;
    private List<BookReview> allReviews = new ArrayList<>();
    private List<String> userList;
    private ReviewManageAdapter adapter;
    private String selectedUsername; // 当前选中的用户名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_manage);

        dbManager = new DBManager(this);
        lvAllReviews = findViewById(R.id.lv_all_reviews);
        spUsers = findViewById(R.id.sp_users);

        adapter = new ReviewManageAdapter();
        lvAllReviews.setAdapter(adapter);

        loadUsers(); // 先加载所有用户
        setSpinnerListener(); // 设置用户选择监听器
    }

    // 加载所有用户
    private void loadUsers() {
        new Thread(() -> {
            userList = dbManager.getAllUsernames();
            // 添加"所有用户"选项
            userList.add(0, "所有用户");
            runOnUiThread(() -> {
                ArrayAdapter<String> userAdapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, userList);
                userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spUsers.setAdapter(userAdapter);
            });
        }).start();
    }

    // 设置用户选择监听器
    private void setSpinnerListener() {
        spUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userList != null && !userList.isEmpty()) {
                    selectedUsername = userList.get(position);
                    // 根据选择的用户加载对应的书评
                    loadReviewsByUser(selectedUsername);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // 根据用户名加载书评（"所有用户"则加载全部）
    private void loadReviewsByUser(String username) {
        new Thread(() -> {
            if ("所有用户".equals(username)) {
                allReviews = dbManager.getAllBookReviews();
            } else {
                allReviews = dbManager.getReviewsByUser(username);
            }
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private class ReviewManageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allReviews.size();
        }

        @Override
        public Object getItem(int position) {
            return allReviews.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_review_manage, parent, false);
                holder = new ViewHolder();
                holder.tvBookTitle = convertView.findViewById(R.id.tv_book_title);
                holder.tvUsername = convertView.findViewById(R.id.tv_review_username);
                holder.tvTime = convertView.findViewById(R.id.tv_review_time);
                holder.tvContent = convertView.findViewById(R.id.tv_review_content);
                holder.btnDelete = convertView.findViewById(R.id.btn_delete_review);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BookReview review = allReviews.get(position);
            holder.tvBookTitle.setText("书籍: " + review.getBookTitle());
            holder.tvUsername.setText("用户: " + review.getUsername());
            holder.tvContent.setText(review.getContent());

            // 格式化发布时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(review.getPublishTime())));

            holder.btnDelete.setOnClickListener(v -> deleteReview(review.getId()));
            return convertView;
        }

        class ViewHolder {
            TextView tvBookTitle, tvUsername, tvTime, tvContent;
            Button btnDelete;
        }
    }

    private void deleteReview(String reviewId) {
        new Thread(() -> {
            boolean isDeleted = dbManager.deleteBookReview(reviewId);
            runOnUiThread(() -> {
                if (isDeleted) {
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    // 重新加载当前用户的书评
                    loadReviewsByUser(selectedUsername);
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}