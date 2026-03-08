package com.example.novelreaderapp.model;


public class ApiChapterContentResponse {
    private int code;
    private String message;
    private ChapterContentResult result;

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ChapterContentResult getResult() { return result; }
    public void setResult(ChapterContentResult result) { this.result = result; }

    public static class ChapterContentResult {
        private String chapterId;
        private String chapterName;
        private String content; // 章节内容

        public String getChapterId() { return chapterId; }
        public void setChapterId(String chapterId) { this.chapterId = chapterId; }

        public String getChapterName() { return chapterName; }
        public void setChapterName(String chapterName) { this.chapterName = chapterName; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}