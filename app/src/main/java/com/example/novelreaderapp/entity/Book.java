package com.example.novelreaderapp.entity;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Book implements Serializable {
//    private String bookId;       // 书籍ID (新增)
//    private String name;         // 书名 (原 title)
//    private String author;       // 作者
//    private String cover;        // 封面URL
//    private String brief;        // 简介 (原 description)
    @SerializedName("id")  // 对应JSON的id
    private String bookId;

    @SerializedName("title")  // 对应JSON的title
    private String name;
    @SerializedName("author")
    private String author;       // 作者

    @SerializedName("thumb")  // 对应JSON的thumb
    private String cover;

    @SerializedName("docs")  // 对应JSON的docs
    private String brief;

    @SerializedName("word_number")  // 对应JSON的word_number
    private String wordNumber;

    @SerializedName("read_count")  // 对应JSON的read_count
    private String readCount;

    @SerializedName("serial")
    private int serial;


    // 无参构造（Gson解析和序列化必需）
    public Book() {}

//    // 有参构造 (用于数据库和UI)
//    public Book(String name, String author, String cover, String brief) {
//        this.name = name;
//        this.author = author;
//        this.cover = cover;
//        this.brief = brief;
//    }
// 全参构造
    public Book(String bookId, String name, String author, String cover,
            String brief, int serial, String wordNumber, String readCount) {
    this.bookId = bookId;
    this.name = name;
    this.author = author;
    this.cover = cover;
    this.brief = brief;
    this.serial = serial;
    this.wordNumber = wordNumber;
    this.readCount = readCount;
    }

    // 包含bookId的有参构造（可选，用于需要ID的场景）
    public Book(String bookId, String name, String author, String cover, String brief) {
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.cover = cover;
        this.brief = brief;
    }

    // Getter 和 Setter
    public String getBookId() {
        Log.d("Book", "当前bookId: " + (bookId != null ? bookId : "null"));
        //return bookId;
        return bookId != null ? bookId : "";
    }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public String getBrief() { return brief; }
    public void setBrief(String brief) { this.brief = brief; }

    // 补充getter方法
    public int getSerial() {
        return serial;
    }

    public String getWordNumber() {
        return wordNumber;
    }

    // 补充构造器参数（如果需要）
    public Book(/* 其他参数, */ String readCount) {
        // 其他参数赋值...
        this.readCount = readCount;
    }

    // 补充getter方法
    public String getReadCount() {
        return readCount;
    }


    // 1. 设置连载状态
    public void setSerial(int serial) {
        this.serial = serial;  // 将参数值赋值给成员变量
    }

    // 2. 设置字数
    public void setWordNumber(String wordNumber) {
        this.wordNumber = wordNumber;  // 将参数值赋值给成员变量
    }

    // 3. 设置阅读量
    public void setReadCount(String readCount) {
        this.readCount = readCount;  // 将参数值赋值给成员变量
    }


}
