package com.example.sysucde.ourapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationService extends Service {
    private NotifyLocationListener listener;
    private NotifyLister mNotifyLister;
    private LocationClient mLocationClient;

    private double now_longitude, now_latitude;
    private double longitude, latitude;
    private String uuid;
    private String description;

    private int id;
    private boolean isNotify = false;
    private boolean isSend = false;

    private Vibrator mVibrator;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        listener = new NotifyLocationListener();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(listener);

        LocationClientOption option = new LocationClientOption();

        // 采用低功耗模式
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        // 定位结果坐标系
        option.setCoorType("bd09ll");
        // 每隔十秒重新获取位置
        option.setScanSpan(10000);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(false);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);

        mLocationClient.setLocOption(option);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_STICKY;
        longitude = intent.getDoubleExtra("longitude", 0);
        latitude = intent.getDoubleExtra("latitude", 0);
        id = intent.getIntExtra("id", 0);
        uuid = intent.getStringExtra("uuid");
        description = intent.getStringExtra("description");

        isSend = false;
        isNotify = false;
        // 地图提醒监听器
        mNotifyLister = new NotifyLister();
        // 开始定位
        mLocationClient.start();

        return START_STICKY;
    }

    private Handler notifyHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (!isNotify) {
                // 设置需要提醒的位置
                mNotifyLister.SetNotifyLocation(latitude, longitude, 100, "bd09ll");
                mLocationClient.registerNotify(mNotifyLister);
                isNotify = true;
            }
        }
    };

    public class NotifyLister extends BDNotifyListener {
        public void onNotify(BDLocation mLocation, float distance) {
            mVibrator.vibrate(600);
            if (!isSend) {
                Intent notificationIntent = new Intent("com.example.sysucde.ourapp.baidumap.NOTIFICATION");
                notificationIntent.putExtra("address", description);
                notificationIntent.putExtra("id", id);
                notificationIntent.putExtra("uuid", uuid);
                // 发送广播
                sendBroadcast(notificationIntent);
                isSend = true;
            }
        }
    }

    public class NotifyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null) {
                now_longitude = location.getLongitude();
                now_latitude = location.getLatitude();
                notifyHandler.sendEmptyMessage(0);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationClient.stop();
        mLocationClient.removeNotifyEvent(mNotifyLister);
        mLocationClient.unRegisterLocationListener(listener);
        notifyHandler = null;
        mLocationClient = null;
        mNotifyLister = null;
        listener = null;
    }
}
