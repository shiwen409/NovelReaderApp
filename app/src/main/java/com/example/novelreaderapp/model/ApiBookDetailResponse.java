package com.example.novelreaderapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// 书籍详情响应模型
public class ApiBookDetailResponse {
    private int code;
    private String message;
    @SerializedName("data") // 对应API的data字段
    private BookDetailResult result;

    // 补充 setter 方法
    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResult(BookDetailResult result) {
        this.result = result;
    }

    // getter和setter
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public BookDetailResult getResult() { return result; }

    // 书籍详情数据模型
    public static class BookDetailResult {
        @SerializedName("id") // 对应API的data.*.id（bookId）
        private String bookId;
        @SerializedName("title") // 对应API的data.*.title
        private String title;
        @SerializedName("author") // 对应API的data.*.author
        private String author;
        @SerializedName("thumb") // 对应API的data.*.thumb（封面）
        private String cover;
        @SerializedName("docs") // 对应API的data.*.docs（描述）
        private String brief;
        @SerializedName("serial") // 对应API的data.*.serial（更新章节数）
        private int serial;
        @SerializedName("word_number") // 对应API的data.*.word_number
        private String wordNumber;
        @SerializedName("read_count") // 对应API的data.*.read_count
        private String readCount;

        private String chapter_title; // 章节名
        private int chapter; // 章节号
        private String chapter_id; // 章节id

        // 章节列表（与API响应的"data"字段匹配）
        private List<Chapter> data;

        // 补充 getter方法，用于获取章节列表
        public List<Chapter> getChapters() {
            return data;
        }

        // 章节信息内部类
        public static class Chapter {
            @SerializedName("chapter_id")
            private String chapterId;
            @SerializedName("chapter_title")
            private String chapterTitle;

            // 补充 getter 方法
            public String getChapterId() {
                return chapterId;
            }

            public String getChapterTitle() {
                return chapterTitle;
            }
        }

        // getter和setter
        public String getBookId() { return bookId; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCover() { return cover; }
        public String getBrief() { return brief; }
        public int getSerial() { return serial; }// 总章节数
        public String getWordNumber() { return wordNumber; }
        public String getReadCount() { return readCount; }
        public String getChapterTitle() { return chapter_title; }
        public int getChapter() { return chapter; }
        public String getChapterId() { return chapter_id; }
    }
}