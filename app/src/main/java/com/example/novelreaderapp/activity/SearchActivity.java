package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.entity.Book;
import com.example.novelreaderapp.adapter.BookAdapter;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;
import com.example.novelreaderapp.utils.ApiClient;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearchInput;
    private Button btnSearch;
    private RecyclerView rvSearchResult;
    private BookAdapter bookAdapter;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        dbManager = new DBManager(this);
        initRecyclerView();
        bindListener();

        // 接收从书城传递的搜索关键词
        Intent intent = getIntent();
        String query = intent.getStringExtra("query");
        if (query != null && !query.isEmpty()) {
            etSearchInput.setText(query);
            performSearch(query); // 调用简化后的搜索方法
        }
    }

    private void initView() {
        etSearchInput = findViewById(R.id.et_search_title);
        btnSearch = findViewById(R.id.btn_search);
        rvSearchResult = findViewById(R.id.rv_search_result);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvSearchResult.setLayoutManager(layoutManager);
        bookAdapter = new BookAdapter(this, new ArrayList<>());
        rvSearchResult.setAdapter(bookAdapter);
        setupAdapterListeners();
    }

    // 提取搜索逻辑为独立方法，简化调用
    private void performSearch(String query) {
        new SearchBooksTask().execute(query);
    }

    // 简化后的AsyncTask实现
    private class SearchBooksTask extends AsyncTask<String, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(String... params) {
            // 直接调用ApiClient，异常已在ApiClient内部处理
            return ApiClient.searchBooks(params[0]);
        }

        @Override
        protected void onPostExecute(List<Book> result) {
            super.onPostExecute(result);

            // 简化UI更新逻辑
            if (result == null || result.isEmpty()) {
                Toast.makeText(SearchActivity.this, "未找到相关书籍或网络错误！", Toast.LENGTH_SHORT).show();
                bookAdapter.setBooks(new ArrayList<>());
            } else {
                bookAdapter.setBooks(result);
            }
        }
    }

    private void setupAdapterListeners() {
        bookAdapter.setOnAddBookshelfListener(book -> {
            if (!AppConfig.isLogin) {
                Toast.makeText(this, R.string.toast_login_required, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            boolean isSuccess = dbManager.addToBookshelf(
                    AppConfig.currentUsername,
                    book
            );
            Toast.makeText(SearchActivity.this,
                    isSuccess ? getString(R.string.toast_add_success) : "已在书架中！",
                    Toast.LENGTH_SHORT).show();
        });

        bookAdapter.setOnBookClickListener(book -> {
            Intent intent = new Intent(SearchActivity.this, BookDetailActivity.class);
            intent.putExtra("book", book);
            startActivity(intent);
        });
    }

    private void bindListener() {
        btnSearch.setOnClickListener(v -> {
            String query = etSearchInput.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "请输入搜索关键词！", Toast.LENGTH_SHORT).show();
                return;
            }
            performSearch(query); // 调用简化后的搜索方法
        });
    }
}