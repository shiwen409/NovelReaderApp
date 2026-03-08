package com.example.novelreaderapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库帮助类，负责数据库的创建、升级和管理
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    // 数据库名称和版本
    private static final String DB_NAME = "NovelReaderDB.db";
    private static final int DB_VERSION = 4;

    // 用户表字段
    public static final String TABLE_USER = "user";
    public static final String USER_ID = "_id";
    public static final String USER_NAME = "username";
    public static final String USER_PWD = "password";
    public static final String USER_CREATE_TIME = "create_time";

    // 书架表字段
    public static final String TABLE_BOOKSHELF = "bookshelf";
    public static final String BOOK_ID = "_id";
    public static final String BOOK_TITLE = "title";
    public static final String BOOK_AUTHOR = "author";
    public static final String BOOK_COVER = "cover";
    public static final String BOOK_DESC = "description";
    public static final String USERNAME_FK = "username";
    public static final String BOOK_ADD_TIME = "add_time";
    public static final String BOOK_LAST_READ_POSITION = "last_read_position";
    public static final String BOOK_SERIAL = "serial";
    public static final String BOOK_WORD_NUMBER = "word_number";
    public static final String BOOK_READ_COUNT = "read_count";

    // 新增书评表字段
    public static final String TABLE_BOOK_REVIEW = "book_review";
    public static final String REVIEW_ID = "_id";
    public static final String REVIEW_BOOK_ID = "book_id";
    public static final String REVIEW_BOOK_TITLE = "book_title";
    public static final String REVIEW_USERNAME = "username";
    public static final String REVIEW_CONTENT = "content";
    public static final String REVIEW_PUBLISH_TIME = "publish_time";

    // 创建用户表SQL
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " (" +
            USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            USER_NAME + " TEXT UNIQUE NOT NULL, " +
            USER_PWD + " TEXT NOT NULL, " +
            USER_CREATE_TIME + " INTEGER DEFAULT (strftime('%s', 'now')))";

    // 创建书架表SQL（含外键约束）
    private static final String CREATE_TABLE_BOOKSHELF = "CREATE TABLE " + TABLE_BOOKSHELF + " (" +
            BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BOOK_TITLE + " TEXT NOT NULL, " +
            BOOK_AUTHOR + " TEXT NOT NULL, " +
            BOOK_COVER + " TEXT, " +
            BOOK_DESC + " TEXT, " +
            USERNAME_FK + " TEXT NOT NULL, " +
            BOOK_ADD_TIME + " INTEGER DEFAULT (strftime('%s', 'now')), " +
            BOOK_LAST_READ_POSITION + " INTEGER DEFAULT 0, " +
            BOOK_SERIAL + " INTEGER, " +
            BOOK_WORD_NUMBER + " TEXT, " +
            BOOK_READ_COUNT + " TEXT, " +
            "FOREIGN KEY (" + USERNAME_FK + ") REFERENCES " + TABLE_USER + "(" + USER_NAME + "), " +
            "UNIQUE (" + BOOK_TITLE + ", " + USERNAME_FK + "))";

    private static DBHelper instance;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 新增创建书评表SQL
    private static final String CREATE_TABLE_REVIEW = "CREATE TABLE " + TABLE_BOOK_REVIEW + " (" +
            REVIEW_ID + " TEXT PRIMARY KEY, " +
            REVIEW_BOOK_ID + " TEXT NOT NULL, " +
            REVIEW_BOOK_TITLE + " TEXT NOT NULL, " +
            REVIEW_USERNAME + " TEXT NOT NULL, " +
            REVIEW_CONTENT + " TEXT NOT NULL, " +
            REVIEW_PUBLISH_TIME + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + REVIEW_USERNAME + ") REFERENCES " + TABLE_USER + "(" + USER_NAME + "))";

    /**
     * 获取单例实例，确保全局只有一个DBHelper实例
     * @param context 上下文
     * @return DBHelper实例
     */
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            // 使用应用上下文，避免内存泄漏
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "开始创建数据库表");
        try {
            // 启用外键约束
            db.execSQL("PRAGMA foreign_keys = ON");
            // 先创建用户表（被引用的主表）
            db.execSQL(CREATE_TABLE_USER);
            Log.d(TAG, "用户表创建成功");
            // 再创建书架表（依赖用户表的外键）
            db.execSQL(CREATE_TABLE_BOOKSHELF);
            Log.d(TAG, "书架表创建成功");
            db.execSQL(CREATE_TABLE_REVIEW);
            Log.d(TAG, "书评表创建成功");
            Log.d(TAG, "所有数据库表创建完成");
        } catch (Exception e) {
            Log.e(TAG, "数据库表创建失败: " + e.getMessage(), e);
            // 抛出异常，让调用者知道创建失败
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "开始数据库升级，旧版本: " + oldVersion + ", 新版本: " + newVersion);
        // 启用外键约束
        db.execSQL("PRAGMA foreign_keys = ON");

        // 采用渐进式升级，确保每个版本都能正确升级到最新版
        if (oldVersion < 2) {
            upgradeToVersion2(db);
            oldVersion = 2; // 升级后将版本号更新，避免重复执行
        }

        if (oldVersion < 3) {
            upgradeToVersion3(db);
            oldVersion = 3;
        }

        if (oldVersion < 4) {
            try {
                db.execSQL(CREATE_TABLE_REVIEW);
                Log.d(TAG, "书评表创建成功");
            } catch (Exception e) {
                Log.e(TAG, "创建书评表失败", e);
            }
            oldVersion = 4;
        }

        Log.d(TAG, "数据库升级完成");
    }

    /**
     * 升级到版本2
     */
    private void upgradeToVersion2(SQLiteDatabase db) {
        Log.d(TAG, "升级数据库到版本2");
        try {
            // 1. 为用户表添加创建时间字段
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + USER_CREATE_TIME + " INTEGER");
            // 为现有记录设置默认时间
            db.execSQL("UPDATE " + TABLE_USER + " SET " + USER_CREATE_TIME + " = strftime('%s', 'now') WHERE " + USER_CREATE_TIME + " IS NULL");
            Log.d(TAG, "用户表升级完成");

            // 2. 升级书架表（添加时间字段和阅读位置字段）
            // 先备份数据
            db.execSQL("CREATE TABLE " + TABLE_BOOKSHELF + "_temp AS SELECT * FROM " + TABLE_BOOKSHELF);
            // 删除旧表
            db.execSQL("DROP TABLE " + TABLE_BOOKSHELF);
            // 创建新版本表（版本2）
            String oldBookshelfSQL = "CREATE TABLE " + TABLE_BOOKSHELF + " (" +
                    BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BOOK_TITLE + " TEXT NOT NULL, " +
                    BOOK_AUTHOR + " TEXT NOT NULL, " +
                    BOOK_COVER + " TEXT, " +
                    BOOK_DESC + " TEXT, " +
                    USERNAME_FK + " TEXT NOT NULL, " +
                    BOOK_ADD_TIME + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                    BOOK_LAST_READ_POSITION + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (" + USERNAME_FK + ") REFERENCES " + TABLE_USER + "(" + USER_NAME + "), " +
                    "UNIQUE (" + BOOK_TITLE + ", " + USERNAME_FK + "))";
            db.execSQL(oldBookshelfSQL);
            // 恢复数据（明确字段映射，避免列不匹配）
            db.execSQL("INSERT INTO " + TABLE_BOOKSHELF + " (" +
                    BOOK_ID + ", " + BOOK_TITLE + ", " + BOOK_AUTHOR + ", " +
                    BOOK_COVER + ", " + BOOK_DESC + ", " + USERNAME_FK + ", " +
                    BOOK_ADD_TIME + ", " + BOOK_LAST_READ_POSITION + ") " +
                    "SELECT " +
                    "_id, title, author, cover, description, username, " +
                    "strftime('%s', 'now'), 0 " +
                    "FROM " + TABLE_BOOKSHELF + "_temp");
            // 删除临时表
            db.execSQL("DROP TABLE " + TABLE_BOOKSHELF + "_temp");
            Log.d(TAG, "书架表升级到版本2完成");
        } catch (Exception e) {
            Log.e(TAG, "升级到版本2失败", e);
            throw new RuntimeException("数据库升级到版本2失败", e);
        }
    }

    /**
     * 升级到版本3
     */
    private void upgradeToVersion3(SQLiteDatabase db) {
        Log.d(TAG, "升级数据库到版本3");
        try {
            // 为书架表添加新字段
            db.execSQL("ALTER TABLE " + TABLE_BOOKSHELF + " ADD COLUMN " + BOOK_SERIAL + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_BOOKSHELF + " ADD COLUMN " + BOOK_WORD_NUMBER + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_BOOKSHELF + " ADD COLUMN " + BOOK_READ_COUNT + " TEXT");
            Log.d(TAG, "书架表升级到版本3完成");
        } catch (Exception e) {
            Log.e(TAG, "升级到版本3失败", e);
            throw new RuntimeException("数据库升级到版本3失败", e);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "数据库降级，旧版本: " + oldVersion + ", 新版本: " + newVersion);
        // 降级时删除所有表并重新创建（仅用于开发环境，生产环境需谨慎）
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKSHELF);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    /**
     * 关闭数据库连接（在应用退出时调用）
     */
    public void closeDatabase() {
        if (instance != null) {
            instance.close();
            instance = null;
            Log.d(TAG, "数据库连接已关闭");
        }
    }
}