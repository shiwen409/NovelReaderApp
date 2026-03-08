package com.example.novelreaderapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.entity.Book;
import com.example.novelreaderapp.adapter.BookshelfAdapter;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBManager;

import java.util.List;

public class UserBookshelfActivity extends AppCompatActivity {
    private Spinner spUsers;
    private ListView lvBooks;
    private BookshelfAdapter adapter;
    private DBManager dbManager;
    private List<String> userList;
    private List<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bookshelf);
        dbManager = new DBManager(this);
        spUsers = findViewById(R.id.sp_users);
        lvBooks = findViewById(R.id.lv_user_books);

        loadUsers();
        setSpinnerListener();
    }

    private void loadUsers() {
        new Thread(() -> {
            userList = dbManager.getAllUsernames();
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, userList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spUsers.setAdapter(adapter);
            });
        }).start();
    }

    private void setSpinnerListener() {
        spUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userList != null && !userList.isEmpty()) {
                    String username = userList.get(position);
                    loadUserBooks(username);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadUserBooks(String username) {
        new Thread(() -> {
            bookList = dbManager.getBookshelfList(username);
            runOnUiThread(() -> {
                adapter = new BookshelfAdapter(UserBookshelfActivity.this, bookList);
                lvBooks.setAdapter(adapter);
            });
        }).start();
    }
}