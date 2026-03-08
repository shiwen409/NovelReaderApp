package com.example.novelreaderapp.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.example.novelreaderapp.R;
import com.example.novelreaderapp.db.DBHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class AdminStatisticsActivity extends AppCompatActivity {
    private BarChart bookCountChart, reviewCountChart;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private List<String> userNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistics);
        initView();
        loadUserData();
        statBookCount();
        statReviewCount();
    }

    private void initView() {
        bookCountChart = findViewById(R.id.book_count_chart);
        reviewCountChart = findViewById(R.id.review_count_chart);
        dbHelper = DBHelper.getInstance(this);
        db = dbHelper.getReadableDatabase();
    }

    private void loadUserData() {
        Cursor cursor = db.query(DBHelper.TABLE_USER,
                new String[]{DBHelper.USER_NAME},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_NAME));
            userNames.add(username);
        }
        cursor.close();
    }

    private void statBookCount() {
        String query = "SELECT " + DBHelper.USERNAME_FK + ", COUNT(*) as book_count " +
                "FROM " + DBHelper.TABLE_BOOKSHELF +
                " GROUP BY " + DBHelper.USERNAME_FK;

        Cursor cursor = db.rawQuery(query, null);

        List<BarEntry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        int index = 0;
        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USERNAME_FK));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow("book_count"));

            entries.add(new BarEntry(index, count));
            xLabels.add(username);
            index++;
        }
        cursor.close();

        BarDataSet dataSet = new BarDataSet(entries, "书籍数量");
        dataSet.setValueTextColor(Color.parseColor("#333333"));
        dataSet.setColor(Color.rgb(76, 175, 80));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f); // 加宽柱子，适配每个柱子下的标签
        bookCountChart.setData(barData);

        // X轴配置：每个柱子下单独显示姓名（关闭居中对齐）
        XAxis xAxis = bookCountChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#333333"));
        xAxis.setTextSize(12f);
        xAxis.setLabelRotationAngle(0f); // 水平显示
        xAxis.setGranularity(1f); // 每个标签对应一个柱子
        xAxis.setLabelCount(xLabels.size(), true); // 显示所有标签
        xAxis.setYOffset(10f); // 标签与X轴线的距离
        xAxis.setCenterAxisLabels(false); // 关闭居中对齐（关键修改）
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int idx = (int) value;
                return idx >= 0 && idx < xLabels.size() ? xLabels.get(idx) : "";
            }
        });

        // Y轴配置（仅显示整数）
        YAxis yAxisLeft = bookCountChart.getAxisLeft();
        YAxis yAxisRight = bookCountChart.getAxisRight();
        yAxisLeft.setTextColor(Color.parseColor("#333333"));
        yAxisRight.setTextColor(Color.parseColor("#333333"));
        yAxisLeft.setAxisMinimum(0);
        yAxisRight.setAxisMinimum(0);
        yAxisLeft.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        yAxisRight.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        yAxisLeft.setGranularity(1f);
        yAxisRight.setGranularity(1f);

        bookCountChart.getLegend().setTextColor(Color.parseColor("#333333"));
        bookCountChart.getDescription().setEnabled(false);
        bookCountChart.setExtraOffsets(15f, 10f, 15f, 30f); // 底部留足标签空间

        bookCountChart.invalidate();
    }

    private void statReviewCount() {
        String query = "SELECT " + DBHelper.REVIEW_USERNAME + ", COUNT(*) as review_count " +
                "FROM " + DBHelper.TABLE_BOOK_REVIEW +
                " GROUP BY " + DBHelper.REVIEW_USERNAME;

        Cursor cursor = db.rawQuery(query, null);

        List<BarEntry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        int index = 0;
        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.REVIEW_USERNAME));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow("review_count"));

            entries.add(new BarEntry(index, count));
            xLabels.add(username);
            index++;
        }
        cursor.close();

        BarDataSet reviewDataSet = new BarDataSet(entries, "书评数量");
        reviewDataSet.setValueTextColor(Color.parseColor("#333333"));
        reviewDataSet.setColor(Color.rgb(33, 150, 243));

        BarData barData = new BarData(reviewDataSet);
        barData.setBarWidth(0.8f); // 加宽柱子
        reviewCountChart.setData(barData);

        // X轴配置：每个柱子下单独显示姓名
        XAxis xAxisReview = reviewCountChart.getXAxis();
        xAxisReview.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisReview.setTextColor(Color.parseColor("#333333"));
        xAxisReview.setTextSize(12f);
        xAxisReview.setLabelRotationAngle(0f);
        xAxisReview.setGranularity(1f);
        xAxisReview.setLabelCount(xLabels.size(), true);
        xAxisReview.setYOffset(10f);
        xAxisReview.setCenterAxisLabels(false); // 关闭居中对齐（关键修改）
        xAxisReview.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int idx = (int) value;
                return idx >= 0 && idx < xLabels.size() ? xLabels.get(idx) : "";
            }
        });

        // Y轴配置
        YAxis yAxisReviewLeft = reviewCountChart.getAxisLeft();
        YAxis yAxisReviewRight = reviewCountChart.getAxisRight();
        yAxisReviewLeft.setTextColor(Color.parseColor("#333333"));
        yAxisReviewRight.setTextColor(Color.parseColor("#333333"));
        yAxisReviewLeft.setAxisMinimum(0);
        yAxisReviewRight.setAxisMinimum(0);
        yAxisReviewLeft.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        yAxisReviewRight.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        yAxisReviewLeft.setGranularity(1f);
        yAxisReviewRight.setGranularity(1f);

        reviewCountChart.getLegend().setTextColor(Color.parseColor("#333333"));
        reviewCountChart.getDescription().setEnabled(false);
        reviewCountChart.setExtraOffsets(15f, 10f, 15f, 30f);

        reviewCountChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}