package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.entity.Book;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BookDetailActivity extends AppCompatActivity {
    private static final String TAG = "BookDetailActivity";
    private String bookId;
    private ImageView ivBack, ivCover;
    private TextView tvTitle, tvAuthor, tvDesc, tvSerial, tvWordCount, tvReadCount;
    private Button btnAddToShelf;
    private Button btnRead;
    private DBManager dbManager;
    private Book currentBook;
    // 声明为final
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        initView();
        dbManager = new DBManager(this);
        receiveBookData();
        bindListener();
        checkIfInBookshelf();
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivCover = findViewById(R.id.iv_detail_cover);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvAuthor = findViewById(R.id.tv_detail_author);
        tvDesc = findViewById(R.id.tv_detail_desc);
        tvSerial = findViewById(R.id.tv_detail_serial);
        tvWordCount = findViewById(R.id.tv_detail_word_count);
        tvReadCount = findViewById(R.id.tv_detail_read_count);
        btnAddToShelf = findViewById(R.id.btn_detail_add);
        btnRead = findViewById(R.id.btn_detail_read);
    }

    private void receiveBookData() {
        Intent intent = getIntent();
        currentBook = (Book) intent.getSerializableExtra("book");

        if (currentBook == null) {
            Toast.makeText(this, "未获取到书籍信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookId = currentBook.getBookId();
        if (bookId == null || bookId.isEmpty()) {
            Toast.makeText(this, "书籍ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "当前书籍ID: " + bookId);
        updateBookInfo(currentBook);
    }

    private void updateBookInfo(Book book) {
        tvTitle.setText(book.getName());
        tvAuthor.setText("作者：" + book.getAuthor());
        tvDesc.setText(book.getBrief().isEmpty() ? "暂无简介" : book.getBrief());
        tvSerial.setText(book.getSerial() == 1 ? "连载中" : "已完结");
        tvWordCount.setText("字数：" + book.getWordNumber());
        tvReadCount.setText("阅读量：" + book.getReadCount());

        if (book.getCover() != null && !book.getCover().isEmpty()) {
            Picasso.get().load(book.getCover()).error(R.drawable.ic_book).into(ivCover);
        } else {
            ivCover.setImageResource(R.drawable.ic_book);
        }
    }

    private void bindListener() {
        ivBack.setOnClickListener(v -> finish());

        btnAddToShelf.setOnClickListener(v -> {
            if (!AppConfig.isLogin) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            boolean success = dbManager.addToBookshelf(AppConfig.currentUsername, currentBook);
            if (success) {
                Toast.makeText(this, "已加入书架", Toast.LENGTH_SHORT).show();
                btnAddToShelf.setText("已在书架");
                btnAddToShelf.setEnabled(false);
            } else {
                Toast.makeText(this, "添加失败或已在书架", Toast.LENGTH_SHORT).show();
            }
        });

        btnRead.setOnClickListener(v -> {
            if (bookId == null || bookId.isEmpty()) {
                Toast.makeText(this, "书籍ID无效，无法阅读", Toast.LENGTH_SHORT).show();
                return;
            }
            loadFirstChapter(bookId);
        });
    }

    // 加载第一章内容
    private void loadFirstChapter(String bookId) {
        btnRead.setEnabled(false);
        btnRead.setText("加载中...");

        new Thread(() -> {
            try {
                FormBody formBody = new FormBody.Builder()
                        .add("id", bookId)
                        .add("chapter", "1")
                        .add("key", AppConfig.API_KEY)
                        .add("type", "json")
                        .build();

                Request request = new Request.Builder()
                        .url("https://www.oiapi.net/api/FqRead")
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("请求失败，状态码: " + response.code());
                    }

                    String jsonData = response.body().string();
                    Log.d(TAG, "API响应数据: " + jsonData);

                    JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                    if (jsonObject.has("code") && jsonObject.get("code").getAsInt() == 1) {
                        String chapterContent = jsonObject.get("message").getAsString()
                                .replace("\\n", "<br>");

                        runOnUiThread(() -> {
                            Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
                            intent.putExtra("bookId", bookId);
                            intent.putExtra("chapterId", "1");
                            intent.putExtra("chapterName", "第一章");
                            intent.putExtra("content", chapterContent);
                            startActivity(intent);

                            btnRead.setEnabled(true);
                            btnRead.setText("阅读");
                        });
                    } else {
                        String errorMsg = jsonObject.get("message").getAsString();
                        throw new IOException("API错误: " + errorMsg);
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "加载第一章失败: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(BookDetailActivity.this,
                            "加载失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnRead.setEnabled(true);
                    btnRead.setText("阅读");
                });
            }
        }).start();
    }

    // 检查书籍是否已在书架
    private void checkIfInBookshelf() {
        if (AppConfig.isLogin && currentBook != null) {
            new Thread(() -> {
                boolean isExist = dbManager.isBookExistInShelf(AppConfig.currentUsername, currentBook.getBookId());
                runOnUiThread(() -> {
                    if (isExist) {
                        btnAddToShelf.setText("已在书架");
                        btnAddToShelf.setEnabled(false);
                    }
                });
            }).start();
        }
    }
}