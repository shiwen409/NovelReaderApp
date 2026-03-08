package com.example.novelreaderapp.utils;

import android.content.Context;
import android.util.Log;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class BDLocationUtils {
    private static final String TAG = "BDLocationUtils"; // 增加日志标签，便于调试
    private LocationClient mLocationClient;
    private OnLocationListener mListener;
    // 增加定位监听器实例引用，避免注销时传null（部分SDK版本可能有问题）
    private BDAbstractLocationListener locationListener;

    // 定位结果回调接口
    public interface OnLocationListener {
        void onSuccess(String cityName); // 成功返回城市名
        void onFailure(String errorMsg); // 失败返回错误信息
    }

    public void setOnLocationListener(OnLocationListener listener) {
        this.mListener = listener;
    }

    public BDLocationUtils(Context context) {
        try {

            LocationClient.setAgreePrivacy(true);

            // 初始化定位客户端，使用应用上下文避免内存泄漏
            mLocationClient = new LocationClient(context.getApplicationContext());
            // 初始化定位监听器并保存引用
            locationListener = new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation location) {
                    if (location == null) {
                        if (mListener != null) {
                            mListener.onFailure("定位失败，位置信息为空");
                        }
                        return;
                    }

                    // 增加错误码判断（百度SDK推荐）
                    int errorCode = location.getLocType();
                    if (errorCode == BDLocation.TypeGpsLocation
                            || errorCode == BDLocation.TypeNetWorkLocation
                            || errorCode == BDLocation.TypeOffLineLocation) {
                        String city = location.getCity();
                        if (city != null && !city.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSuccess(city);
                            }
                        } else {
                            if (mListener != null) {
                                mListener.onFailure("未获取到城市信息");
                            }
                        }
                    } else {
                        // 输出具体错误信息，便于调试
                        String errorMsg = "定位失败，错误码：" + errorCode
                                + "，描述：" + location.getLocTypeDescription();
                        Log.e(TAG, errorMsg);
                        if (mListener != null) {
                            mListener.onFailure(errorMsg);
                        }
                    }
                    // 单次定位完成后自动停止
                    stopLocation();
                }
            };
            mLocationClient.registerLocationListener(locationListener);
            initLocationOptions();
        } catch (Exception e) {
            // 捕获初始化异常（如SDK未正确集成）
            Log.e(TAG, "定位客户端初始化失败", e);
            if (mListener != null) {
                mListener.onFailure("定位服务初始化失败，请检查配置");
            }
        }
    }

    // 配置定位参数
    private void initLocationOptions() {
        if (mLocationClient == null) return;

        LocationClientOption option = new LocationClientOption();
        // 定位模式：高精度（同时使用GPS、网络和基站）
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 坐标类型：百度bd09ll坐标
        option.setCoorType("bd09ll");
        // 开启GPS（若SDK版本支持，此方法有效）
        option.setOpenGps(true);
        // 是否需要地址信息（如城市、街道等）
        option.setIsNeedAddress(true);
        // 不需要设备方向
        option.setNeedDeviceDirect(false);
        // 单次定位（scanSpan=0表示只定位一次）
        option.setScanSpan(0);

        mLocationClient.setLocOption(option);
    }

    // 开始定位
    public void startLocation() {
        if (mLocationClient != null && !mLocationClient.isStarted()) {
            mLocationClient.start();
        } else if (mListener != null) {
            mListener.onFailure("定位客户端未初始化或已启动");
        }
    }

    // 停止定位
    public void stopLocation() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    // 销毁定位客户端
    public void destroyLocation() {
        stopLocation();
        if (mLocationClient != null) {
            // 使用保存的监听器实例注销，避免空指针
            mLocationClient.unRegisterLocationListener(locationListener);
            mLocationClient = null;
        }
        // 清空回调引用，避免内存泄漏
        mListener = null;
    }
}