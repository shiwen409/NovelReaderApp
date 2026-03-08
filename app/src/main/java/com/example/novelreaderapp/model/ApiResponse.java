package com.example.novelreaderapp.model;


import java.util.List;

// API根响应模型
public class ApiResponse {
    private int code;
    private String message;
    private List<ApiDataItem> data;  // 对应API返回的data数组

    // Getter方法
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public List<ApiDataItem> getData() { return data; }
}

// API data数组中的单个书籍项模型
class ApiDataItem {
    private String id;         // 书籍ID
    private String title;      // 书名
    private String author;     // 作者
    private String thumb;      // 封面URL
    private String docs;       // 简介
    private int serial;        // 更新的章节数量
    private String word_number;// 字数
    private String read_count; // 在读人数

    // Getter方法（仅暴露需要的字段）
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getThumb() { return thumb; }
    public String getDocs() { return docs; }
    public int getSerial() { return serial; }
    public String getWord_number() { return word_number; }
    public String getRead_count() { return read_count; }
}