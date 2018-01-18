package com.example.sysucde.ourapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ScrollingTabContainerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ReplyActivity extends AppCompatActivity {
    Toolbar mToolbar;
    int index;
    int message_id;
    String username;
    String content;
    String res;
    String motto;
    String nickname;
    Bitmap header;
    Bitmap picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        ExitApplication.getInstance().addActivity(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_publish);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent it = getIntent();
        index = Integer.parseInt(it.getStringExtra("message_id"));
        message_id = Integer.parseInt(it.getStringExtra("mes_id"));
        //username = it.getStringExtra("writer");
        motto = it.getStringExtra("content");
        nickname = it.getStringExtra("nickname");
        //header = decode_headPicture(it.getStringExtra("header"));
        //picture = decode_headPicture(it.getStringExtra("picture"));

        final Data app = (Data)getApplication();
        header = app.getTemp_header();
        picture = app.getTemp_pic();
        username = app.getUsername();

        /*访问服务器，取得该message_id下的动态详情*/
        AuthorInformation card = getMessage();

        ImageView header1 = (ImageView)findViewById(R.id.header_reply);
        header1.setImageBitmap(header); //card.
        TextView nickname1 = (TextView)findViewById(R.id.nickname_reply);
        nickname1.setText(nickname);
        TextView motto1 = (TextView)findViewById(R.id.motto_reply);
        motto1.setText(motto);

        Button button = (Button)findViewById(R.id.replyConfirm_reply);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                toConfirmReply();
            }
        });
    }

    private Bitmap decode_headPicture(String headPicture) {
        if(headPicture.equals("")) {
            return null;
        }
        byte[] bytes = Base64.decode(headPicture, Base64.DEFAULT);
        Bitmap bp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return bp;
    }

    public void toConfirmReply() {
        EditText reply = (EditText)findViewById(R.id.replyContent_reply);
        content = reply.getText().toString();
        if(content.equals("")){
            Toast.makeText(this, "请输入回复内容", Toast.LENGTH_LONG).show();
            return;
        }
        /*将回复的内容和回复的动态的id传回服务器*/
        boolean mark = send(username, content, message_id);
        if(mark){
            Intent intent = new Intent();
            intent.putExtra("result", content);
            intent.putExtra("nickname", nickname);
            intent.putExtra("index", String.valueOf(index));
            setResult(RESULT_OK, intent);// 设置resultCode，onActivityResult()中能获取到
            finish();
        }
        else{
            Toast.makeText(this, "未知错误", Toast.LENGTH_LONG).show();
        }
    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public String connectServer(JSONObject object) {
        String result = "";
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://139.199.6.110:8080/Login/comment");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Charset","UTF-8");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setReadTimeout(10_1000);
            connection.setConnectTimeout(5 * 1000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            String content = String.valueOf(object);
            OutputStream os = connection.getOutputStream();
            os.write(content.getBytes("utf-8"));
            os.flush();
            os.close();

            if(connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                result = convertStreamToString(is);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    /*将回复的内容和回复的动态的id传回服务器*/
    public boolean send(String username, String content, int message_id){
        new Thread(replyTh).start();

        return true;
    }

    Thread replyTh = new Thread() {
        @Override
        public void run() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("un", username);
                obj.put("mes_id", message_id);
                obj.put("text", content);
                res = connectServer(obj);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    };

    public AuthorInformation getMessage(){
        String nickname, motto, location;
        int message_id;
        Bitmap header, picture;
        header = BitmapFactory.decodeResource(getResources(), R.mipmap.head1);
        picture = BitmapFactory.decodeResource(getResources(), R.mipmap.bg2);
        nickname = "MCC_小叉";
        motto = "今天哼哼也没有发新照片！";
        message_id = 1;
        location = "中山大学";
        AuthorInformation card = new AuthorInformation(header, nickname, picture, motto, message_id, location);
        return card;
    }
}
