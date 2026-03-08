package com.example.novelreaderapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiChapterListResponse {
    private int code;
    private String message;
    @SerializedName("data")
    private ChapterListResult result;

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public ChapterListResult getResult() { return result; }

    public static class ChapterListResult {
        @SerializedName("total") // 总章节数
        private int totalChapters;
        @SerializedName("list") // 当前页章节列表
        private List<ChapterItem> chapters;

        public int getTotalChapters() { return totalChapters; }
        public List<ChapterItem> getChapters() { return chapters; }
    }

    public static class ChapterItem {
        @SerializedName("chapter_id")
        private String chapterId;
        @SerializedName("chapter_title")
        private String chapterTitle;
        @SerializedName("volume")
        private String volume; // 卷名
        @SerializedName("chapter")
        private int chapterNumber; // 章节号

        public String getChapterId() { return chapterId; }
        public String getChapterTitle() { return chapterTitle; }
        public String getVolume() { return volume; }
        public int getChapterNumber() { return chapterNumber; }
    }
}