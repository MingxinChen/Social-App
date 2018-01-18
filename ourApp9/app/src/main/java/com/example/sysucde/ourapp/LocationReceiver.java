package com.example.sysucde.ourapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationReceiver extends BroadcastReceiver {
  public LocationReceiver() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Double longitude, latitude;

    longitude = intent.getDoubleExtra("longitude", 0);
    latitude = intent.getDoubleExtra("latitude", 0);
    if (longitude == 0 && latitude == 0)
      return;

    Intent mIntent = new Intent();
    mIntent.setPackage("ml.huangjw.memory");

    mIntent.setAction("ml.huangjw.memory.LOCATION_SERVICE");

    mIntent.putExtra("longitude", longitude);
    mIntent.putExtra("latitude", latitude);
    mIntent.putExtra("id", intent.getIntExtra("id", 0));
    mIntent.putExtra("uuid", intent.getStringExtra("uuid"));
    mIntent.putExtra("description", intent.getStringExtra("description"));

    // 启动指定Service
    context.startService(mIntent);
  }
}
