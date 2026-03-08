package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class BookstoreActivity extends AppCompatActivity {
    private RecyclerView rvBookstore;
    private EditText etSearch;
    private Button btnSearch;
    // 补全底部导航所有控件声明（包含书城本身）
    private TextView tvNavBookstore, tvNavBookshelf, tvNavSetting;
    private BookAdapter bookAdapter; // 适配器实例
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookstore);
        initView();
        dbManager = new DBManager(this);
        initRecyclerView(); // 初始化RecyclerView和适配器
        loadHotBooks(); // 加载热门书籍
        bindListener();
        // 设置当前页面（书城）导航高亮
        setCurrentNavHighlight();
    }

    private void initView() {
        rvBookstore = findViewById(R.id.rv_bookstore);
        etSearch = findViewById(R.id.et_bookstore_search);
        btnSearch = findViewById(R.id.btn_bookstore_search);
        // 初始化所有底部导航控件
        tvNavBookstore = findViewById(R.id.tv_nav_bookstore);
        tvNavBookshelf = findViewById(R.id.tv_nav_bookshelf);
        tvNavSetting = findViewById(R.id.tv_nav_setting);
    }

    // 新增：设置当前页面导航高亮
    private void setCurrentNavHighlight() {
        // 书城按钮高亮（使用项目中定义的colorAccent颜色）
        tvNavBookstore.setTextColor(getResources().getColor(R.color.colorAccent));
        // 其他按钮恢复默认白色（与布局中默认颜色一致）
        tvNavBookshelf.setTextColor(getResources().getColor(android.R.color.white));
        tvNavSetting.setTextColor(getResources().getColor(android.R.color.white));
    }

    // 初始化RecyclerView和适配器
    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBookstore.setLayoutManager(layoutManager);
        // 初始化适配器并绑定到RecyclerView
        bookAdapter = new BookAdapter(this, new ArrayList<>());
        rvBookstore.setAdapter(bookAdapter);
    }

    // 加载热门书籍（修复线程问题）
    private void loadHotBooks() {
        new Thread(() -> {
            // 1. 子线程执行网络请求
            List<Book> hotBooks = ApiClient.searchBooks("经典");
            Log.d("BookstoreActivity", "热门书籍数量：" + hotBooks.size());

            // 2. 切换到主线程更新UI
            runOnUiThread(() -> {
                if (hotBooks != null && !hotBooks.isEmpty()) {
                    Book firstBook = hotBooks.get(0);
                    Log.d("Bookstore", "解析后书籍：id=" + firstBook.getBookId()
                            + ", name=" + firstBook.getName()
                            + ", cover=" + firstBook.getCover());

                    bookAdapter.setBooks(hotBooks); // 更新适配器数据
                    setAddBookshelfListener(); // 设置加入书架事件
                    setBookClickListener(); // 设置书籍点击事件
                } else {
                    // 显示空状态提示（可在布局中添加一个TextView用于提示）
                    Toast.makeText(this, "未找到热门书籍，请尝试其他关键词", Toast.LENGTH_LONG).show();
                    // 清空适配器数据，避免显示旧数据
                    bookAdapter.setBooks(new ArrayList<>());
                    Toast.makeText(BookstoreActivity.this,
                            "未加载到热门书籍",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // 设置加入书架点击事件
    private void setAddBookshelfListener() {
        bookAdapter.setOnAddBookshelfListener(book -> {
            if (!AppConfig.isLogin) {
                Toast.makeText(this,
                        "请先登录再添加书籍到书架",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            // 添加到书架数据库
            boolean isSuccess = dbManager.addToBookshelf(
                    AppConfig.currentUsername,
                    book // 直接传递Book对象
            );

            Toast.makeText(this,
                    isSuccess ? "添加成功" : "该书籍已在书架中！",
                    Toast.LENGTH_SHORT).show();
        });
    }

    // 设置书籍点击事件（跳转到详情页）
    private void setBookClickListener() {
        bookAdapter.setOnBookClickListener(book -> {
            Intent intent = new Intent(BookstoreActivity.this, BookDetailActivity.class);
            intent.putExtra("book", book); // 传递书籍对象
            startActivity(intent);
        });
    }

    // 绑定导航和搜索按钮事件
    private void bindListener() {
        // 搜索按钮点击事件
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "请输入书名！", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到搜索结果页
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("query", query);
            startActivity(intent);
        });

        // 跳转到书架
        tvNavBookshelf.setOnClickListener(v ->
                startActivity(new Intent(this, BookshelfActivity.class))
        );

        // 跳转到设置
        tvNavSetting.setOnClickListener(v ->
                startActivity(new Intent(this, SettingActivity.class))
        );

        // 新增：书城按钮点击事件（刷新当前页面）
        tvNavBookstore.setOnClickListener(v -> {
            loadHotBooks(); // 重新加载热门书籍
            setCurrentNavHighlight(); // 重新高亮（防止跳转后返回样式错乱）
        });
    }
}