package com.example.novelreaderapp.model;

import com.example.novelreaderapp.entity.Book;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * API搜索接口的响应模型类
 * 用于封装搜索书籍的接口返回数据
 */
public class ApiSearchResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data") // 修改为与JSON中的"data"字段绑定
    private List<Book> result;

    // 空参构造方法
    public ApiSearchResponse() {
    }

    // 全参构造方法
    public ApiSearchResponse(int code, String message, List<Book> result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    // Getter和Setter方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Book> getResult() {
        return result;
    }

    public void setResult(List<Book> result) {
        this.result = result;
    }
}