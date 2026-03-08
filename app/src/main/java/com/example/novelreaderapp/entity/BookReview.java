package com.example.novelreaderapp.entity;

public class BookReview {
    private String id;
    private String bookId;
    private String bookTitle;
    private String username;
    private String content;
    private long publishTime;

    public BookReview(String id, String bookId, String bookTitle, String username, String content, long publishTime) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.username = username;
        this.content = content;
        this.publishTime = publishTime;
    }

    // getter和setter
    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public long getPublishTime() { return publishTime; }
}