package com.example.novelreaderapp.utils;

import android.util.Log;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.entity.Book;
import com.example.novelreaderapp.model.ApiBookDetailResponse;
import com.example.novelreaderapp.model.ApiChapterContentResponse;
import com.example.novelreaderapp.model.ApiChapterListResponse;
import com.example.novelreaderapp.model.ApiSearchResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiClient {
    // 配置超时时间的OkHttpClient实例
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)    // 连接超时
            .readTimeout(10, TimeUnit.SECONDS)       // 读取超时
            .writeTimeout(10, TimeUnit.SECONDS)      // 写入超时
            .build();

    private static final Gson gson = new Gson();
    private static final String BASE_URL = AppConfig.API_BASE_URL;


    // 搜索书籍（适配新接口：/search?name=xxx）
    public static List<Book> searchBooks(String query) {
        // 参数校验
        if (query == null || query.trim().isEmpty()) {
            Log.e("ApiClient", "搜索关键词不能为空");
            return Collections.emptyList();
        }

        String url = String.format("%s?keyword=%s&key=%s", BASE_URL, query, AppConfig.API_KEY);
        Log.d("ApiClient", "实际请求URL: " + url);

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = "搜索书籍请求失败，关键词=" + query + "，状态码：" + response.code()
                        + "，消息：" + response.message() + "，URL：" + url;
                Log.e("ApiClient", errorMsg);
                return Collections.emptyList();
            }

            String jsonData = response.body().string();
            if (jsonData == null || jsonData.isEmpty()) {
                Log.e("ApiClient", "响应数据为空，URL：" + url);
                return Collections.emptyList();
            }
            Log.d("ApiClient", "API返回数据：" + jsonData);

            ApiSearchResponse apiResponse;
            try {
                apiResponse = gson.fromJson(jsonData, ApiSearchResponse.class);
            } catch (JsonSyntaxException e) {
                Log.e("ApiClient", "JSON解析失败，数据：" + jsonData, e);
                return Collections.emptyList();
            }

            if (apiResponse == null) {
                Log.e("ApiClient", "解析后响应对象为null，原始数据：" + jsonData);
                return Collections.emptyList();
            }
            if (apiResponse.getCode() != 1) {
                Log.e("ApiClient", "API业务错误，错误码：" + apiResponse.getCode()
                        + "，错误信息：" + apiResponse.getMessage()
                        + "，原始数据：" + jsonData);
                return Collections.emptyList();
            }

            return apiResponse.getResult() != null ? apiResponse.getResult() : new ArrayList<>();

        } catch (IOException e) {
            Log.e("ApiClient", "网络请求异常，URL：" + url, e);
            return Collections.emptyList();
        } catch (Exception e) {
            Log.e("ApiClient", "搜索书籍发生未预期错误，URL：" + url, e);
            return Collections.emptyList();
        }
    }

    // 获取书籍详情（适配新接口：/info?bookId=xxx）
    public static ApiBookDetailResponse.BookDetailResult getBookDetail(String bookId) throws IOException {
        // 参数校验
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IOException("bookId不能为空");
        }

        Log.d("ApiClient", "请求详情的bookId: " + bookId);
        String url = BASE_URL + "/info?id=" + bookId + "&key=" + AppConfig.API_KEY;
        Log.d("ApiClient", "详情请求URL: " + url);
        //String url = BASE_URL + "/info?bookId=" + bookId + "&key=" + AppConfig.API_KEY;


        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = "获取书籍详情失败，bookId=" + bookId + ", 状态码=" + response.code() + ", URL=" + url;
                Log.e("ApiClient", errorMsg);
                throw new IOException(errorMsg);
            }

            String jsonData = response.body().string();
            Log.d("ApiClient", "详情响应数据: " + jsonData);

            ApiBookDetailResponse apiResponse;
            try {
                apiResponse = gson.fromJson(jsonData, ApiBookDetailResponse.class);
            } catch (JsonSyntaxException e) {
                Log.e("ApiClient", "Gson解析失败，尝试手动解析", e);
                // 手动解析JSON
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

                int code = jsonObject.get("code").getAsInt();
                String message = jsonObject.get("message").getAsString();

                if (code != 1) {
                    throw new IOException("API错误: " + message + "（错误码: " + code + "）");
                }

                // 手动解析结果（根据实际API响应结构调整）
                ApiBookDetailResponse manualResponse = new ApiBookDetailResponse();
                manualResponse.setCode(code);
                manualResponse.setMessage(message);

                ApiBookDetailResponse.BookDetailResult result = new ApiBookDetailResponse.BookDetailResult();
                // 这里需要根据实际JSON结构手动填充result字段
                manualResponse.setResult(result);
                apiResponse = manualResponse;
            }

            if (apiResponse == null) {
                throw new IOException("解析响应失败，返回数据为空");
            }
            if (apiResponse.getCode() != 200) {
                throw new IOException("API错误: " + apiResponse.getMessage() + "（错误码: " + apiResponse.getCode() + "）");
            }
            if (apiResponse.getResult() == null) {
                throw new IOException("API返回结果为空");
            }

            return apiResponse.getResult();
        }
    }

    // 获取章节内容（适配新接口：/chapter?bookId=xxx&chapterId=xxx）
    public static String getChapterContent(String bookId, String chapterId) throws IOException {
        // 参数校验
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IOException("bookId不能为空");
        }
        if (chapterId == null || chapterId.trim().isEmpty()) {
            throw new IOException("chapterId不能为空");
        }

        String url = String.format("%s/chapter?bookId=%s&chapterId=%s&key=%s",
                BASE_URL, bookId, chapterId, AppConfig.API_KEY);
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = "获取章节内容失败，bookId=" + bookId + ", chapterId=" + chapterId
                        + ", 状态码=" + response.code() + ", URL=" + url;
                Log.e("ApiClient", errorMsg);
                throw new IOException(errorMsg);
            }

            String jsonData = response.body().string();
            Log.d("ApiClient", "章节内容原始响应: " + jsonData);

            ApiChapterContentResponse apiResponse = gson.fromJson(jsonData, ApiChapterContentResponse.class);

            if (apiResponse != null && apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                return apiResponse.getResult().getContent().replace("\n", "<br>");
            } else {
                String errorMsg = "获取章节内容失败，bookId=" + bookId + ", chapterId=" + chapterId
                        + ", 错误信息: " + (apiResponse != null ? apiResponse.getMessage() : "Unknown error");
                Log.e("ApiClient", errorMsg);
                throw new IOException(errorMsg);
            }
        }
    }

    // 新增：获取第一章内容（增强换行符处理）
    public static String getFirstChapterContent(String url) throws IOException {
        // 参数校验
        if (url == null || url.trim().isEmpty()) {
            throw new IOException("请求URL不能为空");
        }

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = "获取第一章内容失败，状态码: " + response.code() + ", URL=" + url;
                Log.e("ApiClient", errorMsg);
                throw new IOException(errorMsg);
            }

            String jsonData = response.body().string();
            Log.d("ApiClient", "第一章响应数据: " + jsonData);

            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            if (jsonObject.has("code") && jsonObject.get("code").getAsInt() == 1) {
                String content = jsonObject.get("message").getAsString();
                return content.replace("\\r\\n", "<br>")
                        .replace("\\n", "<br>")
                        .replace("\r\n", "<br>")
                        .replace("\n", "<br>");
            } else {
                String errorMsg = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "未知错误";
                throw new IOException("API错误: " + errorMsg + ", URL=" + url);
            }
        }
    }

    // 获取指定章节号的章节ID（通过章节号遍历）
    public static String getChapterIdByNumber(String bookId, int chapterNumber) throws IOException {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IOException("bookId不能为空");
        }
        if (chapterNumber < 1) {
            throw new IOException("章节号必须大于0");
        }

        // 构建带章节号的请求URL（使用chapter参数）
        String url = String.format("%s?key=%s&id=%s&chapter=%d",
                BASE_URL, AppConfig.API_KEY, bookId, chapterNumber);
        Log.d("ApiClient", "获取章节ID请求URL: " + url);

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = "获取章节ID失败，bookId=" + bookId + ", 章节号=" + chapterNumber
                        + ", 状态码=" + response.code();
                Log.e("ApiClient", errorMsg);
                throw new IOException(errorMsg);
            }

            String jsonData = response.body().string();
            Log.d("ApiClient", "章节ID响应数据: " + jsonData);

            ApiBookDetailResponse responseObj = gson.fromJson(jsonData, ApiBookDetailResponse.class);
            if (responseObj == null || responseObj.getCode() != 1) {
                String errorMsg = "获取章节ID失败: " + (responseObj != null ? responseObj.getMessage() : "未知错误");
                throw new IOException(errorMsg);
            }

            // 返回该章节号对应的chapter_id
            return responseObj.getResult().getChapterId();
        }
    }

    // 获取章节列表（分页）
    public static ApiChapterListResponse getChapterList(String bookId, int page, int pageSize) throws IOException {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IOException("bookId不能为空");
        }

        String url = String.format("%s/chapters?bookId=%s&page=%d&pageSize=%d&key=%s",
                BASE_URL, bookId, page, pageSize, AppConfig.API_KEY);
        Log.d("ApiClient", "章节列表请求URL: " + url);

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取章节列表失败，状态码: " + response.code());
            }

            String jsonData = response.body().string();
            Log.d("ApiClient", "章节列表响应数据: " + jsonData);

            ApiChapterListResponse responseObj = gson.fromJson(jsonData, ApiChapterListResponse.class);
            if (responseObj == null || responseObj.getCode() != 1) {
                throw new IOException("章节列表获取失败: " + (responseObj != null ? responseObj.getMessage() : "未知错误"));
            }

            return responseObj;
        }
    }
}