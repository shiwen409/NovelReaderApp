package com.example.novelreaderapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.novelreaderapp.entity.Book;
import com.example.novelreaderapp.entity.BookReview;
import com.example.novelreaderapp.entity.User;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static final String TAG = "DBManager";
    private DBHelper dbHelper;

    // 构造方法：初始化DBHelper
    public DBManager(Context context) {
        dbHelper = DBHelper.getInstance(context); // 使用单例方法
    }

    // 1. 用户注册（插入用户数据）
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.USER_NAME, username);
        values.put(DBHelper.USER_PWD, password);
        // 插入数据（返回行号，-1表示失败）
        long result = db.insert(DBHelper.TABLE_USER, null, values);
        return result != -1;
    }

    // 2. 用户登录（验证用户名和密码）
    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 查询条件：用户名和密码匹配
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(DBHelper.TABLE_USER, null,
                DBHelper.USER_NAME + "=? AND " + DBHelper.USER_PWD + "=?",
                selectionArgs, null, null, null);
        boolean isSuccess = cursor.getCount() > 0;
        cursor.close();
        return isSuccess;
    }

    /**
     * 3. 书架添加书籍（已优化：避免重复添加）
     */
    public boolean addToBookshelf(String username, Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;

        try {
            // 先检查书籍是否已存在（通过bookId）
            if (!isBookExistInShelf(username, book.getBookId())) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.BOOK_ID, book.getBookId());
                values.put(DBHelper.BOOK_TITLE, book.getName());
                values.put(DBHelper.BOOK_AUTHOR, book.getAuthor());
                values.put(DBHelper.BOOK_COVER, book.getCover());
                values.put(DBHelper.BOOK_DESC, book.getBrief());
                values.put(DBHelper.BOOK_SERIAL, book.getSerial());
                values.put(DBHelper.BOOK_WORD_NUMBER, book.getWordNumber());
                values.put(DBHelper.BOOK_READ_COUNT, book.getReadCount());
                values.put(DBHelper.USERNAME_FK, username);
                result = db.insert(DBHelper.TABLE_BOOKSHELF, null, values);
                Log.d(TAG, "Book '" + book.getName() + "' added to shelf for user '" + username + "'");
            } else {
                Log.d(TAG, "Book '" + book.getName() + "' already exists in shelf for user '" + username + "'. Skipping.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding book to shelf: " + e.getMessage(), e);
        } finally {
            // 移除db.close()，由系统管理连接
        }

        return result != -1;
    }

    /**
     * 批量添加书籍到书架（使用事务）
     */
    public boolean batchAddToBookshelf(String username, List<Book> books) {
        if (books == null || books.isEmpty()) {
            Log.w(TAG, "No books to add in batch.");
            return true; // 空列表视为成功
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction(); // 开启事务

        try {
            for (Book book : books) {
                // 对每本书都进行存在性检查（通过bookId）
                if (!isBookExistInShelf(username, book.getBookId())) {
                    ContentValues values = new ContentValues();
                    values.put(DBHelper.BOOK_ID, book.getBookId());
                    values.put(DBHelper.BOOK_TITLE, book.getName());
                    values.put(DBHelper.BOOK_AUTHOR, book.getAuthor());
                    values.put(DBHelper.BOOK_COVER, book.getCover());
                    values.put(DBHelper.BOOK_DESC, book.getBrief());
                    values.put(DBHelper.BOOK_SERIAL, book.getSerial());
                    values.put(DBHelper.BOOK_WORD_NUMBER, book.getWordNumber());
                    values.put(DBHelper.BOOK_READ_COUNT, book.getReadCount());
                    values.put(DBHelper.USERNAME_FK, username);

                    long result = db.insert(DBHelper.TABLE_BOOKSHELF, null, values);
                    if (result == -1) {
                        Log.e(TAG, "Failed to add book '" + book.getName() + "' in batch.");
                    } else {
                        Log.d(TAG, "Book '" + book.getName() + "' added in batch.");
                    }
                } else {
                    Log.d(TAG, "Book '" + book.getName() + "' already exists. Skipping in batch.");
                }
            }

            db.setTransactionSuccessful(); // 标记事务成功
            Log.d(TAG, "Batch add operation completed successfully.");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error during batch add operation. Transaction will rollback.", e);
            return false;

        } finally {
            db.endTransaction(); // 结束事务（提交或回滚）
        }
    }

    // 5. 查询用户书架所有书籍 - 修复bookId赋值问题
    public List<Book> getBookshelfList(String username) {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {username};
        // 明确指定查询的列，确保获取bookId
        String[] columns = {
                DBHelper.BOOK_ID,
                DBHelper.BOOK_TITLE,
                DBHelper.BOOK_AUTHOR,
                DBHelper.BOOK_COVER,
                DBHelper.BOOK_DESC,
                DBHelper.BOOK_SERIAL,
                DBHelper.BOOK_WORD_NUMBER,
                DBHelper.BOOK_READ_COUNT
        };

        Cursor cursor = db.query(DBHelper.TABLE_BOOKSHELF, columns,
                DBHelper.USERNAME_FK + "=?", selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            // 正确获取bookId列的值
            String bookId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.BOOK_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.BOOK_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.BOOK_AUTHOR));
            String cover = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.BOOK_COVER));
            String brief = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.BOOK_DESC));
            int serial = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.BOOK_SERIAL));
            String wordNumber = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.BOOK_WORD_NUMBER));
            String readCount = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.BOOK_READ_COUNT));

            // 确保bookId被正确传入构造函数
            Book book = new Book(bookId, title, author, cover, brief, serial, wordNumber, readCount);
            bookList.add(book);
        }
        cursor.close();
        return bookList;
    }

    // 6. 从书架删除书籍
    public boolean deleteFromBookshelf(String username, String bookId) {
        Log.d(TAG, "删除参数：username=" + username + ", bookId=" + bookId);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] selectionArgs = {username, bookId};
        int result = db.delete(DBHelper.TABLE_BOOKSHELF,
                DBHelper.USERNAME_FK + "=? AND " + DBHelper.BOOK_ID + "=?",
                selectionArgs);
        return result > 0;
    }

    /**
     * 辅助方法：检查某本书是否已存在于用户的书架中
     */
    public boolean isBookExistInShelf(String username, String bookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String selection = DBHelper.USERNAME_FK + " = ? AND " + DBHelper.BOOK_ID + " = ?";
            String[] selectionArgs = {username, bookId};
            cursor = db.query(DBHelper.TABLE_BOOKSHELF, new String[]{DBHelper.BOOK_ID},
                    selection, selectionArgs, null, null, null);
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // 新增用户管理方法
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USER, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_NAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_PWD));
            users.add(new User(username, password));
        }
        cursor.close();
        return users;
    }

    public List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USER, new String[]{DBHelper.USER_NAME},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            usernames.add(cursor.getString(0));
        }
        cursor.close();
        return usernames;
    }

    public boolean deleteUser(String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DBHelper.TABLE_USER, DBHelper.USER_NAME + "=?", new String[]{username});
        return rows > 0;
    }

    public boolean updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.USER_PWD, newPassword);
        int rows = db.update(DBHelper.TABLE_USER, values, DBHelper.USER_NAME + "=?", new String[]{username});
        return rows > 0;
    }

    public boolean addBookReview(BookReview review) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.REVIEW_ID, review.getId());
        values.put(DBHelper.REVIEW_BOOK_ID, review.getBookId());
        values.put(DBHelper.REVIEW_BOOK_TITLE, review.getBookTitle());
        values.put(DBHelper.REVIEW_USERNAME, review.getUsername());
        values.put(DBHelper.REVIEW_CONTENT, review.getContent());
        values.put(DBHelper.REVIEW_PUBLISH_TIME, review.getPublishTime());

        long result = db.insert(DBHelper.TABLE_BOOK_REVIEW, null, values);
        return result != -1;
    }

    // 新增：查询书籍的所有书评（按发布时间倒序）
    public List<BookReview> getBookReviews(String bookId) {
        List<BookReview> reviewList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {bookId};
        String orderBy = DBHelper.REVIEW_PUBLISH_TIME + " DESC"; // 倒序排列

        Cursor cursor = db.query(DBHelper.TABLE_BOOK_REVIEW, null,
                DBHelper.REVIEW_BOOK_ID + "=?", selectionArgs, null, null, orderBy);

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_ID));
            String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_BOOK_TITLE));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_USERNAME));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_CONTENT));
            long publishTime = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_PUBLISH_TIME));

            BookReview review = new BookReview(id, bookId, bookTitle, username, content, publishTime);
            reviewList.add(review);
        }
        cursor.close();
        return reviewList;
    }

    /**
     * 修改用户名
     * @param oldUsername 旧用户名
     * @param newUsername 新用户名
     * @return 是否修改成功
     */
    public boolean updateUsername(String oldUsername, String newUsername) {
        // 先检查新用户名是否已存在
        if (isUsernameExists(newUsername)) {
            Log.d(TAG, "Username " + newUsername + " already exists");
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. 更新用户表中的用户名
            ContentValues userValues = new ContentValues();
            userValues.put(DBHelper.USER_NAME, newUsername);
            int userRows = db.update(DBHelper.TABLE_USER, userValues,
                    DBHelper.USER_NAME + "=?", new String[]{oldUsername});

            // 2. 更新书架表中的外键用户名
            ContentValues shelfValues = new ContentValues();
            shelfValues.put(DBHelper.USERNAME_FK, newUsername);
            int shelfRows = db.update(DBHelper.TABLE_BOOKSHELF, shelfValues,
                    DBHelper.USERNAME_FK + "=?", new String[]{oldUsername});

            // 3. 更新书评表中的用户名
            ContentValues reviewValues = new ContentValues();
            reviewValues.put(DBHelper.REVIEW_USERNAME, newUsername);
            int reviewRows = db.update(DBHelper.TABLE_BOOK_REVIEW, reviewValues,
                    DBHelper.REVIEW_USERNAME + "=?", new String[]{oldUsername});

            db.setTransactionSuccessful();
            return userRows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating username: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DBHelper.TABLE_USER, new String[]{DBHelper.USER_NAME},
                    DBHelper.USER_NAME + "=?", new String[]{username},
                    null, null, null);
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // 获取所有书评（供管理员查看）
    public List<BookReview> getAllBookReviews() {
        List<BookReview> reviewList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String orderBy = DBHelper.REVIEW_PUBLISH_TIME + " DESC"; // 按时间倒序

        Cursor cursor = db.query(DBHelper.TABLE_BOOK_REVIEW, null,
                null, null, null, null, orderBy);

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_ID));
            String bookId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_BOOK_ID));
            String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_BOOK_TITLE));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_USERNAME));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_CONTENT));
            long publishTime = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_PUBLISH_TIME));

            BookReview review = new BookReview(id, bookId, bookTitle, username, content, publishTime);
            reviewList.add(review);
        }
        cursor.close();
        return reviewList;
    }

    // 删除书评
    public boolean deleteBookReview(String reviewId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DBHelper.TABLE_BOOK_REVIEW,
                DBHelper.REVIEW_ID + "=?", new String[]{reviewId});
        return rowsDeleted > 0;
    }

    public List<BookReview> getReviewsByUser(String username) {
        List<BookReview> reviews = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询条件：用户名匹配
        String[] selectionArgs = {username};
        Cursor cursor = db.query(DBHelper.TABLE_BOOK_REVIEW, null,
                DBHelper.REVIEW_USERNAME + "=?", selectionArgs,
                null, null, DBHelper.REVIEW_PUBLISH_TIME + " DESC");

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_ID));
            String bookId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_BOOK_ID));
            String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_BOOK_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_CONTENT));
            long publishTime = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_PUBLISH_TIME));

            reviews.add(new BookReview(id, bookId, bookTitle, username, content, publishTime));
        }
        cursor.close();
        return reviews;
    }
}