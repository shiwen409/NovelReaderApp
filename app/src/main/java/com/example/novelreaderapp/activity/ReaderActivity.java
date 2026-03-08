package com.example.novelreaderapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.AppConfig;
import com.example.novelreaderapp.R;
import com.example.novelreaderapp.model.ApiChapterListResponse;
import com.example.novelreaderapp.utils.ApiClient;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReaderActivity extends AppCompatActivity {
    private TextView tvChapterTitle, tvContent;
    private ScrollView scrollView;
    private String bookId;
    private int currentChapterId;
    private OkHttpClient client = new OkHttpClient();
    private GestureDetector gestureDetector;
    private float currentTextSize = 16;
    private float currentLineSpacing = 15;
    private static final float TEXT_SIZE_STEP = 2;
    private static final float LINE_SPACING_STEP = 5;

    // 控件
    private LinearLayout topFunctionBar, layoutChapterList, layoutProgress, bottomMenu;
    private Button btnChapter, btnProgress, btnSetting, btnBookReview, btnPrevChapter, btnNextChapter, btnCloseChapter;
    private ListView lvChapters;
    private RadioGroup rgPageMode;
    private RadioButton rbVertical, rbHorizontal;
    private List<String> chapterList = new ArrayList<>();
    private int totalChapters = 0;
    private boolean isVerticalMode = true; // 默认上下翻页
    private List<ApiChapterListResponse.ChapterItem> loadedChapters = new ArrayList<>();

    // 分页相关变量
    private List<String> contentPages = new ArrayList<>();
    private int currentPage = 0;
    private int totalPages = 0;
    private int pageSize = 0;
    private boolean isMenuVisible = false; // 菜单显示状态标记

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        initViews();
        initGestureDetector();
        calculatePageSize();
        initData();
        initListeners();
        hideAllMenus(); // 初始隐藏所有菜单
    }

    private void initViews() {
        tvChapterTitle = findViewById(R.id.tv_chapter_title);
        tvContent = findViewById(R.id.tv_content);
        scrollView = findViewById(R.id.scroll_view);
        LinearLayout rootLayout = findViewById(R.id.root_layout);

        topFunctionBar = findViewById(R.id.top_function_bar);
        btnChapter = findViewById(R.id.btn_chapter);
        btnProgress = findViewById(R.id.btn_progress);
        btnSetting = findViewById(R.id.btn_setting);
        btnBookReview = findViewById(R.id.btn_book_review);

        layoutChapterList = findViewById(R.id.layout_chapter_list);
        lvChapters = findViewById(R.id.lv_chapters);
        btnCloseChapter = findViewById(R.id.btn_close_chapter);

        layoutProgress = findViewById(R.id.layout_progress);
        btnPrevChapter = findViewById(R.id.btn_prev_chapter);
        btnNextChapter = findViewById(R.id.btn_next_chapter);

        bottomMenu = findViewById(R.id.bottom_menu);
        rgPageMode = findViewById(R.id.rg_page_mode);
        rbVertical = findViewById(R.id.rb_vertical);
        rbHorizontal = findViewById(R.id.rb_horizontal);

        lvChapters.setAdapter(new ChapterAdapter());
    }

    // 计算每页可显示的内容量（优化版）
    private void calculatePageSize() {
        tvContent.post(() -> {
            int availableWidth = tvContent.getWidth() - tvContent.getPaddingLeft() - tvContent.getPaddingRight();
            int availableHeight = tvContent.getHeight() - tvContent.getPaddingTop() - tvContent.getPaddingBottom();

            // 使用实际文本样式计算行高
            TextPaint textPaint = tvContent.getPaint();
            textPaint.setTextSize(currentTextSize * getResources().getDisplayMetrics().scaledDensity);

            // 创建包含测试文本的布局来获取准确行高
            String testText = "测试文本用于计算行高";
            StaticLayout tempLayout = new StaticLayout(testText, textPaint, availableWidth,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, currentLineSpacing, false);

            int lineHeight = tempLayout.getLineCount() > 0 ? tempLayout.getLineBottom(0) : 60; //  fallback值
            int maxLines = availableHeight / lineHeight;

            // 根据行数和平均每行字符数计算页面容量
            pageSize = maxLines * 10; // 每行约20个字符
        });
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            private long lastClickTime = 0;
            private static final long DOUBLE_CLICK_TIME = 300;

            // 处理单击显示/隐藏菜单
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                toggleMenus();
                return true;
            }

            // 处理左右滑动翻页
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null || isMenuVisible) { // 菜单显示时不处理滑动
                    return false;
                }

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                    if (!isVerticalMode) {
                        // 右滑上一页
                        if (diffX > 0) {
                            if (currentPage > 0) {
                                currentPage--;
                                displayCurrentPage();
                            } else {
                                Toast.makeText(ReaderActivity.this, "已经是第一页", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                        // 左滑下一页
                        else {
                            if (currentPage < totalPages - 1) {
                                currentPage++;
                                displayCurrentPage();
                            } else {
                                if (currentChapterId < totalChapters || totalChapters == 0) {
                                    loadChapter(currentChapterId + 1);
                                } else {
                                    Toast.makeText(ReaderActivity.this, "已经是最后一章", Toast.LENGTH_SHORT).show();
                                }
                            }
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            // 长按不触发菜单显示
            @Override
            public void onLongPress(MotionEvent e) {
                // 空实现，避免长按显示菜单
            }
        });
    }

    private void initData() {
        bookId = getIntent().getStringExtra("bookId");
        String chapterId = getIntent().getStringExtra("chapterId");
        String chapterName = getIntent().getStringExtra("chapterName");
        String content = getIntent().getStringExtra("content");

        if (chapterId == null || chapterId.isEmpty()) {
            Toast.makeText(this, "章节信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentChapterId = Integer.parseInt(chapterId);

        tvChapterTitle.setText(chapterName);
        updateContent(content);

        SharedPreferences sp = getSharedPreferences("reader_settings", MODE_PRIVATE);
        String bg = sp.getString("background", "app_bg");
        currentTextSize = sp.getFloat("text_size", 16);
        currentLineSpacing = sp.getFloat("line_spacing", 15);
        isVerticalMode = sp.getBoolean("is_vertical_mode", true);

        tvContent.setTextSize(currentTextSize);
        tvContent.setLineSpacing(currentLineSpacing, 1.0f);
        if (isVerticalMode) {
            rbVertical.setChecked(true);
            setupVerticalMode();
        } else {
            rbHorizontal.setChecked(true);
            setupHorizontalMode();
        }

        switch (bg) {
            case "bg1":
                findViewById(R.id.root_layout).setBackgroundResource(R.drawable.bg1);
                break;
            case "bg2":
                findViewById(R.id.root_layout).setBackgroundResource(R.drawable.bg2);
                break;
            case "bg3":
                findViewById(R.id.root_layout).setBackgroundResource(R.drawable.bg3);
                break;
            default:
                findViewById(R.id.root_layout).setBackgroundResource(R.drawable.app_bg);
        }

        String currentBg = sp.getString("background", "app_bg");
        switch (currentBg) {
            case "app_bg":
                ((Spinner) findViewById(R.id.spinner_bg)).setSelection(0);
                break;
            case "bg1":
                ((Spinner) findViewById(R.id.spinner_bg)).setSelection(1);
                break;
            case "bg2":
                ((Spinner) findViewById(R.id.spinner_bg)).setSelection(2);
                break;
            case "bg3":
                ((Spinner) findViewById(R.id.spinner_bg)).setSelection(3);
                break;
        }
    }

    private void initListeners() {
        // 目录按钮
        btnChapter.setOnClickListener(v -> {
            layoutChapterList.setVisibility(View.VISIBLE);
            layoutChapterList.setLayoutParams(new FrameLayout.LayoutParams(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            if (chapterList.isEmpty()) {
                loadChapterList();
            }
        });

        // 关闭目录
        btnCloseChapter.setOnClickListener(v -> layoutChapterList.setVisibility(View.GONE));

        // 进度按钮
        btnProgress.setOnClickListener(v -> {
//            layoutProgress.setVisibility(View.VISIBLE);
//            bottomMenu.setVisibility(View.GONE);
//            layoutChapterList.setVisibility(View.GONE);
//            bottomMenu.setVisibility(View.GONE);
//            layoutProgress.setVisibility(View.VISIBLE);
//            layoutChapterList.setVisibility(View.GONE);

            layoutProgress.setVisibility(View.VISIBLE);
            bottomMenu.setVisibility(View.GONE);
            // 确保进度栏显示在顶部功能栏上方
            layoutProgress.bringToFront();
        });

        // 上一章/下一章按钮
        btnPrevChapter.setOnClickListener(v -> {
            if (currentChapterId > 1) {
                loadChapter(currentChapterId - 1);
            } else {
                Toast.makeText(this, "已经是第一章", Toast.LENGTH_SHORT).show();
            }
        });

        btnNextChapter.setOnClickListener(v -> {
            if (currentChapterId < totalChapters || totalChapters == 0) {
                loadChapter(currentChapterId + 1);
            } else {
                Toast.makeText(this, "已经是最后一章", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置按钮
        btnSetting.setOnClickListener(v -> {
//            bottomMenu.setVisibility(View.VISIBLE);
//            layoutProgress.setVisibility(View.GONE);
//            layoutChapterList.setVisibility(View.GONE);
            layoutProgress.setVisibility(View.GONE);
            bottomMenu.setVisibility(View.VISIBLE);
            layoutChapterList.setVisibility(View.GONE);
            bottomMenu.bringToFront();
        });

        // 书评按钮
        btnBookReview.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, BookReviewActivity.class);
            intent.putExtra("bookId", bookId);
            intent.putExtra("bookTitle", tvChapterTitle.getText().toString().replace("第" + currentChapterId + "章", "").trim());
            startActivity(intent);
        });

        // 翻页模式切换
        rgPageMode.setOnCheckedChangeListener((group, checkedId) -> {
            boolean newMode = (checkedId == R.id.rb_vertical);
            if (isVerticalMode != newMode) {
                isVerticalMode = newMode;
                SharedPreferences sp = getSharedPreferences("reader_settings", MODE_PRIVATE);
                sp.edit().putBoolean("is_vertical_mode", isVerticalMode).apply();

                if (isVerticalMode) {
                    setupVerticalMode();
                } else {
                    setupHorizontalMode();
                }
                scrollView.scrollTo(0, 0);
            }
        });

        // 章节列表点击
        lvChapters.setOnItemClickListener((parent, view, position, id) -> {
            int targetChapterId = position + 1;
            loadChapter(targetChapterId);
            layoutChapterList.setVisibility(View.GONE);
        });

        // 文字大小调整
        findViewById(R.id.btn_text_increase).setOnClickListener(v -> {
            currentTextSize += TEXT_SIZE_STEP;
            tvContent.setTextSize(currentTextSize);
            getSharedPreferences("reader_settings", MODE_PRIVATE)
                    .edit().putFloat("text_size", currentTextSize).apply();
            calculatePageSize();
            updateContent(tvContent.getText().toString()); // 重新分页
        });

        findViewById(R.id.btn_text_decrease).setOnClickListener(v -> {
            if (currentTextSize > TEXT_SIZE_STEP + 10) {
                currentTextSize -= TEXT_SIZE_STEP;
                tvContent.setTextSize(currentTextSize);
                getSharedPreferences("reader_settings", MODE_PRIVATE)
                        .edit().putFloat("text_size", currentTextSize).apply();
                calculatePageSize();
                updateContent(tvContent.getText().toString());
            } else {
                Toast.makeText(this, "已达最小字体", Toast.LENGTH_SHORT).show();
            }
        });

        // 行距调整
        findViewById(R.id.btn_line_increase).setOnClickListener(v -> {
            currentLineSpacing += LINE_SPACING_STEP;
            tvContent.setLineSpacing(currentLineSpacing, 1.0f);
            getSharedPreferences("reader_settings", MODE_PRIVATE)
                    .edit().putFloat("line_spacing", currentLineSpacing).apply();
            calculatePageSize();
            updateContent(tvContent.getText().toString());
        });

        findViewById(R.id.btn_line_decrease).setOnClickListener(v -> {
            if (currentLineSpacing > LINE_SPACING_STEP) {
                currentLineSpacing -= LINE_SPACING_STEP;
                tvContent.setLineSpacing(currentLineSpacing, 1.0f);
                getSharedPreferences("reader_settings", MODE_PRIVATE)
                        .edit().putFloat("line_spacing", currentLineSpacing).apply();
                calculatePageSize();
                updateContent(tvContent.getText().toString());
            } else {
                Toast.makeText(this, "已达最小行距", Toast.LENGTH_SHORT).show();
            }
        });

        // 背景切换
        ((Spinner) findViewById(R.id.spinner_bg)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = getSharedPreferences("reader_settings", MODE_PRIVATE).edit();
                LinearLayout rootLayout = findViewById(R.id.root_layout);
                switch (position) {
                    case 0:
                        rootLayout.setBackgroundResource(R.drawable.app_bg);
                        editor.putString("background", "app_bg");
                        break;
                    case 1:
                        rootLayout.setBackgroundResource(R.drawable.bg1);
                        editor.putString("background", "bg1");
                        break;
                    case 2:
                        rootLayout.setBackgroundResource(R.drawable.bg2);
                        editor.putString("background", "bg2");
                        break;
                    case 3:
                        rootLayout.setBackgroundResource(R.drawable.bg3);
                        editor.putString("background", "bg3");
                        break;
                    case 4:
                        rootLayout.setBackgroundResource(R.drawable.bg4);
                        editor.putString("background", "bg1");
                        break;
                    case 5:
                        rootLayout.setBackgroundResource(R.drawable.bg5);
                        editor.putString("background", "bg1");
                        break;
                    case 6:
                        rootLayout.setBackgroundResource(R.drawable.bg6);
                        editor.putString("background", "bg1");
                        break;
                    case 7:
                        rootLayout.setBackgroundResource(R.drawable.bg7);
                        editor.putString("background", "bg1");
                        break;
                    case 8:
                        rootLayout.setBackgroundResource(R.drawable.bg8);
                        editor.putString("background", "bg1");
                        break;
                    case 9:
                        rootLayout.setBackgroundResource(R.drawable.bg8);
                        editor.putString("background", "bg1");
                        break;
                    case 10:
                        rootLayout.setBackgroundResource(R.drawable.bg10);
                        editor.putString("background", "bg1");
                        break;
                }
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 上下模式滚动到底部加载下一章
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (!isVerticalMode) return;

            View child = scrollView.getChildAt(0);
            if (child != null) {
                int scrollBottom = scrollView.getScrollY() + scrollView.getHeight();
                int childBottom = child.getHeight();
                if (scrollBottom >= childBottom - 200) {
                    if (currentChapterId < totalChapters || totalChapters == 0) {
                        loadChapter(currentChapterId + 1);
                    }
                }
            }
        });

        // 触摸事件处理
        scrollView.setOnTouchListener((v, event) -> {
            return gestureDetector.onTouchEvent(event);
        });
    }

    // 上下翻页模式配置
    private void setupVerticalMode() {
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.setScrollContainer(true);
        // 允许垂直滚动
        scrollView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // 不消费事件，允许滚动
        });
    }

    // 左右翻页模式配置（核心修改）
    private void setupHorizontalMode() {
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setScrollContainer(false);
        // 禁用垂直滚动，只处理左右滑动
        scrollView.setOnTouchListener((v, event) -> {
            // 完全消费触摸事件，阻止垂直滚动
            return gestureDetector.onTouchEvent(event);
        });
        // 确保内容不超出屏幕
        tvContent.setMaxLines(Integer.MAX_VALUE);
        tvContent.setSingleLine(false);
    }

    // 加载章节列表
    private void loadChapterList() {
        new Thread(() -> {
            try {
                ApiChapterListResponse response = ApiClient.getChapterList(bookId, 1, 20);
                if (response.getResult() != null) {
                    totalChapters = response.getResult().getTotalChapters();
                    List<ApiChapterListResponse.ChapterItem> chapters = response.getResult().getChapters();
                    loadedChapters.addAll(chapters);

                    List<String> chapterTitles = new ArrayList<>();
                    for (ApiChapterListResponse.ChapterItem item : chapters) {
                        chapterTitles.add(item.getChapterTitle());
                    }
                    chapterList.addAll(chapterTitles);

                    runOnUiThread(() -> ((BaseAdapter) lvChapters.getAdapter()).notifyDataSetChanged());
                }
            } catch (IOException e) {
                Log.e("ReaderActivity", "加载章节列表失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "加载章节失败", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // 更新内容并分页（优化版：上下模式禁用分页）
    private void updateContent(String content) {
        contentPages.clear();
        currentPage = 0;
        totalPages = 0;

        if (isVerticalMode) {
            // 上下模式：直接显示完整内容，禁用分页
            contentPages.add(content);
            totalPages = 1;
        } else {
            // 左右模式：执行分页逻辑
            String plainText = Html.fromHtml(content).toString();
            if (pageSize > 0 && !plainText.isEmpty()) {
                int totalLength = plainText.length();
                for (int i = 0; i < totalLength; i += pageSize) {
                    int end = Math.min(i + pageSize, totalLength);
                    contentPages.add(plainText.substring(i, end));
                }
                totalPages = contentPages.size();
            } else {
                contentPages.add(content);
                totalPages = 1;
            }
        }

        displayCurrentPage();
        scrollView.scrollTo(0, 0);
    }

    // 显示当前页内容
    private void displayCurrentPage() {
        if (currentPage >= 0 && currentPage < contentPages.size()) {
            String pageContent = contentPages.get(currentPage);
            pageContent = pageContent.replace("\n", "<br>");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvContent.setText(Html.fromHtml(pageContent, Html.FROM_HTML_MODE_COMPACT));
            } else {
                tvContent.setText(Html.fromHtml(pageContent));
            }
        }
    }

    // 加载指定章节
    private void loadChapter(int chapterId) {
        new Thread(() -> {
            try {
                String url = "https://www.oiapi.net/api/FqRead?id=" + bookId
                        + "&chapter=" + chapterId
                        + "&key=" + AppConfig.API_KEY
                        + "&type=json";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("请求失败: " + response.code());
                }
                String jsonData = response.body().string();
                JSONObject jsonObject = new JSONObject(jsonData);
                if (jsonObject.getInt("code") == 1) {
                    String chapterContent = jsonObject.getString("message")
                            .replace("\\r\\n", "<br>")
                            .replace("\\n", "<br>")
                            .replace("\r\n", "<br>")
                            .replace("\n", "<br>");
                    String chapterName = "第" + chapterId + "章";
                    runOnUiThread(() -> {
                        currentChapterId = chapterId;
                        tvChapterTitle.setText(chapterName);
                        updateContent(chapterContent);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ReaderActivity.this, "没有更多章节了", Toast.LENGTH_SHORT).show();
                        totalChapters = currentChapterId;
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(ReaderActivity.this,
                        "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // 章节列表适配器
    private class ChapterAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return chapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return chapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder();
                holder.tvChapter = convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvChapter.setText("第" + (position + 1) + "章：" + chapterList.get(position));
            holder.tvChapter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            return convertView;
        }

        class ViewHolder {
            TextView tvChapter;
        }
    }

    // 切换菜单显示状态
    private void toggleMenus() {
        if (isMenuVisible) {
            hideAllMenus();
        } else {
            showTopMenu();
            // 隐藏子菜单
            layoutProgress.setVisibility(View.GONE);
            bottomMenu.setVisibility(View.GONE);
        }
    }

    // 显示顶部菜单
    private void showTopMenu() {
        topFunctionBar.setVisibility(View.VISIBLE);
        layoutProgress.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.GONE);
        layoutChapterList.setVisibility(View.GONE);
        isMenuVisible = true;
    }

    // 隐藏所有菜单
    private void hideAllMenus() {
        topFunctionBar.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.GONE);
        layoutChapterList.setVisibility(View.GONE);
        isMenuVisible = false;
    }
}