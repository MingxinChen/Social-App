package com.example.sysucde.ourapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.Text;


public class HomeActivity extends AppCompatActivity  {
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    String nickname = null;
    Bitmap headerbmp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplication());
        setContentView(R.layout.home);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        //设置username和headPicture
        final Data app = (Data)getApplication();
        nickname = app.getNickname();
        headerbmp = app.getBmp();

        ImageView imgview = (ImageView) findViewById(R.id.header_img);
        imgview.setImageBitmap(headerbmp);
        TextView textView = (TextView) findViewById(R.id.header_nickname);
        textView.setText(nickname);

       //TextView hometext = (TextView) findViewById(R.id.home_text);
        //hometext.setText("小组成员：\n邓雅卿，徐梓轩，邢灿盛，陈东儿，陈铭昕");

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        Button btn_dis = (Button) findViewById(R.id.discovery);
        btn_dis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDiscovery();
            }
        });

        Button btn_change = (Button) findViewById(R.id.change_user);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toChangeUser();
            }
        });
        Button btn_neighbor = (Button) findViewById(R.id.neighbor);
        btn_neighbor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNeighbor();
            }
        });
         Button modify_button = (Button) findViewById(R.id.tomodify_button);
        modify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toModify();
            }
        });


       Button btn_exit = (Button) findViewById(R.id.exit_button);
        btn_exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ExitApplication.getInstance().exit();
            }
        });
        ExitApplication.getInstance().addActivity(this);
    }


    public void toDiscovery(){
        Intent it=new Intent(this, DiscoveryActivity.class);
        startActivity(it);
    }

    public void toChangeUser(){
        Intent it=new Intent(this, MainActivity.class);
        startActivity(it);
    }
    public void toModify() {
        Intent it = new Intent(this, Modify_info.class);
        startActivity(it);
    }
    public void toNeighbor() {
        Intent it = new Intent(this, NeighborActivity.class);
        startActivity(it);
        int  a = 2;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
}
