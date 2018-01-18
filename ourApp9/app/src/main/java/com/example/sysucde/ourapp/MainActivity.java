package com.example.sysucde.ourapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.LogRecord;

import com.example.sysucde.ourapp.HttpUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class MainActivity extends AppCompatActivity {
    EditText name;
    EditText password;
    String un;
    String pwd;
    String nickname = null;
    Bitmap bmp = null;
    String res = "original";
    String school = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Data app = (Data)getApplication();
        app.clearData();

        name = (EditText)findViewById(R.id.log_name);
        password = (EditText)findViewById(R.id.log_pwd);
        Button btn_login = (Button)findViewById(R.id.log_ok);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        Button btn_reg = (Button)findViewById(R.id.register);
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        ExitApplication.getInstance().addActivity(this);
    }

    Handler postHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 && res != null) {
                if(res.compareTo("yes") == 0) {
                    res = "original";

                    final Data app = (Data)getApplication();
                    app.setIsLogin(true);
                    app.setUsername(un);
                    app.setNickname(nickname);
                    app.setBmp(bmp);
                    app.setSchool(school);

                    Intent it = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(it);
                }else {
                    Toast.makeText(MainActivity.this, "输入密码错误", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    };


    public void login(){
        un = name.getText().toString();
        pwd = password.getText().toString();

        //我先注释掉方便你们调试
        if(un.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(MainActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(th).start();
    }

    public void register(){
        Intent it = new Intent(this, RegisterActivity.class);
        startActivity(it);
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

    private Bitmap decode_headPicture(String headPicture) {
        if(headPicture.equals("")) {
            return null;
        }
        byte[] bytes = Base64.decode(headPicture, Base64.DEFAULT);
        Bitmap bp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return bp;
    }

    private Thread th = new Thread() {
        @Override
        public void run() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("un", un);
                obj.put("pw", pwd);
                obj.put("method", "1");
            }catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            HttpURLConnection connection = null;

            try {
                String posturl = "http://139.199.6.110:8080/Login/message";
                URL url = new URL(posturl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Charset","UTF-8");
                connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
                connection.setReadTimeout(10 * 1000);
                connection.setConnectTimeout(5 * 1000);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                String content = String.valueOf(obj);
                OutputStream os = connection.getOutputStream();
                os.write(content.getBytes("utf-8"));
                os.flush();
                os.close();

                if(connection.getResponseCode() == 200) {
                    InputStream is = connection.getInputStream();
                    res = convertStreamToString(is);

                    try{
                        JSONTokener jsonParser = new JSONTokener(res);
                        JSONObject userinfo = (JSONObject)jsonParser.nextValue();
                        res = userinfo.getString("mes");
                        nickname = userinfo.getString("nickname");
                        String mybmp = userinfo.getString("img");
                        bmp = decode_headPicture(mybmp);
                        school = userinfo.getString("school");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return ;
                    }

                    Message msg = Message.obtain();
                    msg.what = 0;
                    postHandler.sendMessage(msg);
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
        }
    };


    /*private String toLog(String sname, String spwd) {
        String strFlag = "";
        // 使用Map封装请求参数
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("un", sname);
        map.put("pw", spwd);
        // 定义发送请求的URL
        String url = HttpUnit.BASE_URL; //+ "queryOrder?un=" + sname + "&pw=" + spwd;  //GET方式
        // String url = HttpUtil.BASE_URL + "LoginServlet"; //POST方式
        Log.d("url", url);
        Log.d("username", sname);
        Log.d("password", spwd);
        try {
            // 发送请求
            strFlag = HttpUnit.postRequest(url, map);  //POST方式
          //strFlag = HttpUtil.getRequest(url);  //GET方式
            Log.d("服务器返回值", strFlag);
            return strFlag;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Exception";
        }
    }*/
}

