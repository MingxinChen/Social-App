package com.example.sysucde.ourapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.sysucde.ourapp.RegisterActivity.REQUEST_CODE;


public class Modify_info extends AppCompatActivity {
    Switch modify_password;
    LinearLayout change_password;
    ImageView header;
    Uri imgUri = null;

    Bitmap bmp = null;
    String username = "MCC";
    String nickname = "小叉";
    String school = "山中大学";
    String gender = "保密";
    String email = "mcc@163.com";
    String ori_pwd = null;
    String new_pwd = null;

    String res = "";

    //bitmap2string
    private String edit_headPicture(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//将Bitmap转成Byte[]
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);//压缩
        String headPicture = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);//加密转换成String
        //int n = headPicture.length();

        return headPicture;
    }

    public String Server(JSONObject object) {
        HttpURLConnection connection = null;
        String result = "";
        try {
            URL url = new URL("http://139.199.6.110:8080/Login/change");

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

    /*与服务器连接  取得原始信息*/
    private void getInfomation() {
        final Data app = (Data)getApplication();
        username = app.getUsername();
        bmp = app.getBmp();
        school = app.getSchool();

        TextView tv_username = (TextView)findViewById(R.id.username_modify);
        tv_username.setText(username + "(" + school + ")");
        header.setImageBitmap(bmp);

        new Thread(getInfoTh).start();
    }

    private Thread getInfoTh = new Thread() {
        @Override
        public void run() {
            //传json格式文件给服务器
            JSONObject obj = new JSONObject();
            try {
                obj.put("un", username);
                obj.put("method", "1");
                res = Server(obj);

                try{
                    JSONTokener jsonParser = new JSONTokener(res);
                    JSONObject userinfo = (JSONObject)jsonParser.nextValue();
                    res = userinfo.getString("mes");
                    email = userinfo.getString("email");
                    nickname = userinfo.getString("nickname");
                    school = userinfo.getString("school");
                    String sid = userinfo.getString("sex");
                    if(sid.equals("0")) {
                        gender = "保密";
                    }
                    else if(sid.equals("1")) {
                        gender = "男";
                    }
                    else {
                        gender = "女";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return ;
                }

                Message msg = Message.obtain();
                msg.what = 0;
                getHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /*与服务器连接  修改信息*/
    private String change(String nickname, String school, String email, String gender, String ori_pwd, String new_pwd) {
        new Thread(changeInfoTh).start();

        return "修改成功";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_info);

        modify_password=(Switch)findViewById(R.id.modify_pwd);
        change_password=(LinearLayout)findViewById(R.id.change_pwd);
        modify_password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    change_password.setVisibility(View.VISIBLE);
                } else {
                    change_password.setVisibility(View.GONE);
                }
            }
        });

        ExitApplication.getInstance().addActivity(this);
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar_modify);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        header = (ImageView)findViewById(R.id.header_modify);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPerm()) toSetHeader();
            }
        });

        /* ger information from server*/
        getInfomation();

        Button modify_confirm = (Button)findViewById(R.id.button_modify);
        modify_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toModify();
            }
        });
    }

    private void toModify(){
        EditText et_nickname = (EditText)findViewById(R.id.nickname_modify);
        nickname = et_nickname.getText().toString();
        if(nickname.equals("")) {
            Toast.makeText(this, "请输入昵称", Toast.LENGTH_LONG).show();
            return;
        }
/*
        EditText et_school = (EditText)findViewById(R.id.school_modify);
        school = et_school.getText().toString();
        if(school.equals("")) {
            Toast.makeText(this, "请输入学校", Toast.LENGTH_LONG).show();
            return;

        }*/

        EditText et_email = (EditText)findViewById(R.id.email_modify);
        email = et_email.getText().toString();
        if(email.equals("")) {
            Toast.makeText(this, "请输入邮箱", Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton man = (RadioButton) findViewById(R.id.man_gender_modify);
        RadioButton woman = (RadioButton) findViewById(R.id.woman_gender_modify);
        RadioButton none = (RadioButton)findViewById(R.id.none_gender_modify);
        if(man.isChecked()) {
            gender="男";
        }else if(woman.isChecked()) {
            gender="女";
        }else if(none.isChecked()) {
            gender="保密";
        }else{
            Toast.makeText(this, "请选择性别", Toast.LENGTH_LONG).show();
            return;
        }

        EditText et_ori_pwd = (EditText)findViewById(R.id.original_pwd_modify);
        ori_pwd = et_ori_pwd.getText().toString();
        EditText et_new_pwd = (EditText)findViewById(R.id.new_pwd_modify);
        new_pwd = et_new_pwd.getText().toString();

        /**
         * 与服务器连接
         */
        String msg = change(nickname, school, email, gender, ori_pwd, new_pwd);
        return;
    }

    private void toSetHeader(){
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);
        it.setType("images/*");
        startActivityForResult(it, 101);
    }

    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            switch(requestCode){
                case 101:
                    imgUri = converUri(data.getData());
                    break;
            }
            showImg();
        }
        else{
            Toast.makeText(this, "请求失败：未知错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImg() {
        int iw, ih, vw, vh;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgUri.getPath(), option);
        iw = option.outWidth;
        ih = option.outHeight;
        vw = header.getWidth();
        vh = header.getHeight();
        int scaleFactor= Math.min(iw/vw, ih/vh);
        option.inJustDecodeBounds = false;
        option.inSampleSize = scaleFactor;
        bmp= BitmapFactory.decodeFile(imgUri.getPath(), option);
        Toast.makeText(this, imgUri.getPath()+"\n"+iw+" "+ih+"\n"+vw+" "+vh, Toast.LENGTH_LONG).show();
        header.setImageBitmap(bmp);
    }

    private Uri converUri(Uri uri){
        if(uri.toString().substring(0,7).equals("content")){
            String[] colName = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, colName, null, null, null);
            cursor.moveToFirst();
            uri = Uri.parse("file://" + cursor.getString(0));
            cursor.close();
        }
        return uri;
    }

    private boolean checkPerm() {
        /*apply for permission*/
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        /*check permission*/
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "权限请求通过", Toast.LENGTH_LONG).show();
            return true;
        }
        else{
            Toast.makeText(this, "权限请求被拒绝", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    Handler getHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 && res.equals("yes")) {
                EditText ev_nickname = (EditText)findViewById(R.id.nickname_modify);
                ev_nickname.setText(nickname);
                EditText ev_email = (EditText)findViewById(R.id.email_modify);
                ev_email.setText(email);
                RadioButton man = (RadioButton) findViewById(R.id.man_gender_modify);
                RadioButton woman = (RadioButton) findViewById(R.id.woman_gender_modify);
                RadioButton none = (RadioButton)findViewById(R.id.none_gender_modify);
                woman.setChecked(false);
                man.setChecked(false);
                none.setChecked(false);
                if(gender.equals("男")){
                    man.setChecked(true);
                }else if(gender.equals("女")){
                    woman.setChecked(true);
                }
                else if(gender.equals("保密")){
                    none.setChecked(true);
                }
            }
        }
    };

    private Thread changeInfoTh = new Thread() {
        @Override
        public void run() {
            //传json格式文件给服务器
            JSONObject obj = new JSONObject();
            try {
                obj.put("un", username);
                obj.put("nickname", nickname);
                obj.put("img", edit_headPicture(bmp));
                obj.put("email", email);
                obj.put("method", "2");
                if(gender.equals("男")) {
                    obj.put("sex","1");
                }
                else if(gender.equals("女")) {
                    obj.put("sex","2");
                }
                else if(gender.equals("保密")) {
                    obj.put("sex","0");
                }
                obj.put("pwd", ori_pwd);
                obj.put("new_pwd", new_pwd);
                //if(!ori_pwd.isEmpty() && !new_pwd.isEmpty())
                res = Server(obj);

                try{
                    JSONTokener jsonParser = new JSONTokener(res);
                    JSONObject userinfo = (JSONObject)jsonParser.nextValue();
                    res = userinfo.getString("mes");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return ;
                }

                Message msg = Message.obtain();
                msg.what = 0;
                changeInfoHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Handler changeInfoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 && res.equals("yes")) {
                Toast.makeText(Modify_info.this, "修改成功", Toast.LENGTH_LONG).show();
                final Data app = (Data)getApplication();
                app.setNickname(nickname);
                app.setBmp(bmp);

                Intent it = new Intent(Modify_info.this, HomeActivity.class);
                startActivity(it);
            }
            else {
                Toast.makeText(Modify_info.this, "密码修改失败", Toast.LENGTH_LONG).show();

                Intent it = new Intent(Modify_info.this, HomeActivity.class);
                startActivity(it);
            }
            super.handleMessage(msg);
        }
    };
}
