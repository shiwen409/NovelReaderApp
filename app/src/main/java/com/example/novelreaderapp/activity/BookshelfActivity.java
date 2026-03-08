package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.entity.Book;
import com.example.novelreaderapp.adapter.BookshelfAdapter;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;

import java.util.ArrayList;
import java.util.List;

public class BookshelfActivity extends AppCompatActivity {
    private ListView lvBookshelf;
    private BookshelfAdapter adapter;
    private List<Book> bookList;
    private DBManager dbManager;
    private TextView tvNavBookstore, tvNavBookshelf, tvNavSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookshelf);
        dbManager = new DBManager(this);
        lvBookshelf = findViewById(R.id.lv_bookshelf);
        initBottomNav();
        loadBookshelfBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookshelfBooks(); // 每次回到书架页都刷新数据
    }

    private void loadBookshelfBooks() {
        if (!AppConfig.isLogin) {
            bookList = new ArrayList<>();
            if (adapter == null) {
                adapter = new BookshelfAdapter(this, bookList);
                lvBookshelf.setAdapter(adapter);
            } else {
                adapter.updateData(bookList);
                adapter.notifyDataSetChanged();
            }
            Toast.makeText(this, "请登录后查看书架内容", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Book> newBookList = dbManager.getBookshelfList(AppConfig.currentUsername);
        bookList = newBookList;

        if (adapter == null) {
            adapter = new BookshelfAdapter(this, bookList);
            lvBookshelf.setAdapter(adapter);
        } else {
            adapter.updateData(bookList);
            adapter.notifyDataSetChanged();
        }

        if (bookList.isEmpty()) {
            Toast.makeText(this, "书架为空，快去书城添加书籍吧！", Toast.LENGTH_SHORT).show();
        }
        setDeleteListener(); // 设置删除监听（包含确认弹窗）
        bindListener();
    }

    private void setDeleteListener() {
        adapter.setOnDeleteListener(book -> {
            // 校验书籍ID
            if (book.getBookId() == null || book.getBookId().isEmpty()) {
                Toast.makeText(this, "书籍信息错误，无法删除", Toast.LENGTH_SHORT).show();
                return;
            }

            // 弹出确认对话框
            new AlertDialog.Builder(this)
                    .setTitle("删除确认")
                    .setMessage("确定要从书架中删除《" + book.getName() + "》吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // 点击"删除"按钮，执行真正的删除操作
                        boolean isDeleted = dbManager.deleteFromBookshelf(
                                AppConfig.currentUsername,
                                book.getBookId()
                        );
                        if (isDeleted) {
                            Toast.makeText(this, "已从书架移除", Toast.LENGTH_SHORT).show();
                            loadBookshelfBooks(); // 刷新列表
                        } else {
                            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        // 点击"取消"按钮，关闭对话框
                        dialog.dismiss();
                    })
                    .show(); // 显示对话框
        });
    }

    private void bindListener() {
        lvBookshelf.setOnItemClickListener((parent, view, position, id) -> {
            if (bookList == null || position >= bookList.size()) {
                Toast.makeText(this, "无效的书籍位置", Toast.LENGTH_SHORT).show();
                return;
            }

            Book book = bookList.get(position);
            if (book.getBookId() == null || book.getBookId().isEmpty()) {
                Toast.makeText(this, "书籍信息不完整，无法查看详情", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(BookshelfActivity.this, BookDetailActivity.class);
            intent.putExtra("book", book);
            startActivity(intent);
        });
    }

    private void initBottomNav() {
        tvNavBookstore = findViewById(R.id.tv_nav_bookstore);
        tvNavBookshelf = findViewById(R.id.tv_nav_bookshelf);
        tvNavSetting = findViewById(R.id.tv_nav_setting);

        // 设置当前页面导航高亮（可选）
        tvNavBookshelf.setTextColor(getResources().getColor(R.color.colorAccent));

        // 绑定导航事件
        bindBottomNavListener();
    }

    // 底部导航点击事件
    private void bindBottomNavListener() {
        tvNavBookstore.setOnClickListener(v -> {
            startActivity(new Intent(this, BookstoreActivity.class));
            finish(); // 关闭当前页面
        });

        tvNavBookshelf.setOnClickListener(v -> {
            // 当前已是书架页，可忽略或刷新页面
            loadBookshelfBooks();
        });

        tvNavSetting.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));
            finish(); // 关闭当前页面
        });
    }
}