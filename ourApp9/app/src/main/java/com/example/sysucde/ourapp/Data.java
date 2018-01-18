package com.example.sysucde.ourapp;

import android.app.Application;
import android.graphics.Bitmap;

/**
 * Created by Yoki on 2017/5/28.
 */

public class Data extends Application {
    private String username = null;
    private String nickname = null;
    private boolean isLogin = false;
    private Bitmap bmp = null;
    private String school = null;

    public Bitmap temp_pic = null;
    public Bitmap temp_header = null;

    public void clearData() {
        username = "";
        nickname = "";
        school = "";
        isLogin = false;
    }

    public String getUsername() {
        return  username;
    }

    public void setUsername(String un) {
        username = un;
    }

    public boolean getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(boolean is) {
        isLogin = is;
    }

    public Bitmap getBmp() { return bmp; }

    public void setBmp(Bitmap mybmp){ bmp = mybmp; }

    public String getNickname(){ return nickname; }

    public void setNickname(String mynickname) { nickname = mynickname; }

    public String getSchool() {
        return school;
    }

    public void setSchool(String newSchool) {
        school = newSchool;
    }

    public void setTemp_pic(Bitmap temp) {
        temp_pic = temp;
    }

    public Bitmap getTemp_pic() {
        return temp_pic;
    }

    public void setTemp_header(Bitmap th) {
        temp_header = th;
    }

    public Bitmap getTemp_header() {
        return temp_header;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }
}
