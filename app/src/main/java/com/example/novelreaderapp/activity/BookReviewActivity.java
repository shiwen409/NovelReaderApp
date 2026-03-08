package com.example.novelreaderapp.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.entity.BookReview;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BookReviewActivity extends AppCompatActivity {
    private ListView lvReviews;
    private EditText etReviewContent;
    private Button btnSubmitReview, btnWriteReview;
    private DBManager dbManager;
    private List<BookReview> reviewList = new ArrayList<>();
    private ReviewAdapter adapter;
    private String bookId, bookTitle;
    private String currentUsername;
    private LinearLayout writeReviewContainer; // 成员变量，用于控制显示的容器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_review);

        initViews();    // 先初始化所有控件
        initData();
        initListeners(); // 统一在这个方法中设置监听器
        loadReviews();
    }

    private void initViews() {
        lvReviews = findViewById(R.id.lv_reviews);
        etReviewContent = findViewById(R.id.et_review_content);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        btnWriteReview = findViewById(R.id.btn_write_review);
        writeReviewContainer = findViewById(R.id.write_review_container); // 初始化容器
        adapter = new ReviewAdapter();
        lvReviews.setAdapter(adapter);
    }

    private void initData() {
        dbManager = new DBManager(this);
        bookId = getIntent().getStringExtra("bookId");
        bookTitle = getIntent().getStringExtra("bookTitle");
        currentUsername = AppConfig.currentUsername; // 从全局配置获取当前登录用户名
    }

    private void initListeners() {
        // 写书评按钮点击事件（控制容器显示/隐藏）
        btnWriteReview.setOnClickListener(v -> {
            if (writeReviewContainer.getVisibility() == View.GONE) {
                writeReviewContainer.setVisibility(View.VISIBLE); // 显示输入框容器
            } else {
                writeReviewContainer.setVisibility(View.GONE); // 隐藏输入框容器
            }
        });

        // 提交书评按钮点击事件
        btnSubmitReview.setOnClickListener(v -> {
            String content = etReviewContent.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "书评内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(currentUsername)) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建书评对象
            String reviewId = UUID.randomUUID().toString();
            long publishTime = System.currentTimeMillis();
            BookReview review = new BookReview(
                    reviewId,
                    bookId,
                    bookTitle,
                    currentUsername,
                    content,
                    publishTime
            );

            // 保存到数据库
            boolean isSuccess = dbManager.addBookReview(review);
            if (isSuccess) {
                Toast.makeText(this, "书评提交成功", Toast.LENGTH_SHORT).show();
                etReviewContent.setText("");
                reviewList.add(0, review); // 插入到列表头部
                adapter.notifyDataSetChanged();
                lvReviews.setSelection(0); // 滚动到顶部
                writeReviewContainer.setVisibility(View.GONE); // 隐藏输入框
            } else {
                Toast.makeText(this, "书评提交失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 加载书评列表
    private void loadReviews() {
        new Thread(() -> {
            reviewList = dbManager.getBookReviews(bookId);
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    // 书评列表适配器
    private class ReviewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return reviewList.size();
        }

        @Override
        public Object getItem(int position) {
            return reviewList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_book_review, parent, false);
                holder = new ViewHolder();
                holder.tvUsername = convertView.findViewById(R.id.tv_review_username);
                holder.tvTime = convertView.findViewById(R.id.tv_review_time);
                holder.tvContent = convertView.findViewById(R.id.tv_review_content);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BookReview review = reviewList.get(position);
            holder.tvUsername.setText(review.getUsername());
            holder.tvContent.setText(review.getContent());

            // 格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(review.getPublishTime())));

            return convertView;
        }

        class ViewHolder {
            TextView tvUsername, tvTime, tvContent;
        }
    }
}