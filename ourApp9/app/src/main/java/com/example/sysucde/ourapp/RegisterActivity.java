package com.example.sysucde.ourapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    EditText name;
    EditText password1, password2;
    EditText email;
    Spinner schools;
    ImageView header;
    CheckBox checkBox;
    Toolbar mToolbar;
    Uri imgUri=null;
    EditText myschool;
    String res="no";
    String schoolstr=null;
    public static final int REQUEST_CODE=100101;

    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_register);

        name=(EditText)findViewById(R.id.reg_user);
        password1=(EditText)findViewById(R.id.reg_pwd1);
        password2=(EditText)findViewById(R.id.reg_pwd2);
        email=(EditText)findViewById(R.id.reg_email);
        header=(ImageView) findViewById(R.id.imageView1);
        schools=(Spinner) findViewById(R.id.schools);
        checkBox = (CheckBox)findViewById(R.id.otherSchool);
        myschool = (EditText) findViewById(R.id.myschool);

        Button button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRegister();
            }
        });
        Button button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPerm()) toSetHeader();
            }
        });
        Button button3 = (Button)findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPerm()) toTakePhoto();
            }
        });
        mToolbar = (Toolbar) findViewById(R.id.toolbar_register);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        ExitApplication.getInstance().addActivity(this);

        checkBox.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View v){
                if(checkBox.isChecked()){
                    myschool.setEnabled(true);
                }else{
                    myschool.setEnabled(false);
                }
            }
        });
    }

    private void toRegister(){
        /*check empty*/
        if(name.getText().toString().equals("")){
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password1.getText().toString().equals("")){
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password2.getText().toString().equals("")){
            Toast.makeText(this, "请再次输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if(email.getText().toString().equals("")){
            Toast.makeText(this, "请输入邮箱", Toast.LENGTH_SHORT).show();
            return;
        }
        /*check forma*/
        if(!password1.getText().toString().equals(password2.getText().toString())){
            Toast.makeText(this, "两次输入的密码不同", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!testEmail(email.getText().toString())){
            Toast.makeText(this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        /*check school and register*/
        if(!checkBox.isChecked()){
            String[] schoolsList = getResources().getStringArray(R.array.schools);
            int schoolIndex = schools.getSelectedItemPosition();
            schoolstr = schoolsList[schoolIndex];
        }else{
            schoolstr = myschool.getText().toString();
        }
        if(schoolstr == null) {
            Toast.makeText(this, "请填写学校", Toast.LENGTH_SHORT).show();
            return;
        }

        connectServer();
        return;
    }

    private boolean connectServer(){
        new Thread(regis2server).start();

        return true;
    }

    private boolean testEmail(String email){
        String format = "\\w{2,15}[@][a-z0-9]{2,}[.]\\p{Lower}{2,}";
        if (email.matches(format))return true;
        else return false;
    }

    private void toSetHeader(){
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);
        it.setType("images/*");
        startActivityForResult(it, 101);
    }

    private void toTakePhoto(){
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String imgName = "p" + System.currentTimeMillis() + ".jsp";
        imgUri = Uri.parse("file://" + dir + "/" + imgName);
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(it, 100);
    }

    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            switch(requestCode){
                case 100:
                    break;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(RegisterActivity.this, "请求权限通过", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(RegisterActivity.this, "请求权限失败", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private String edit_headPicture(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//将Bitmap转成Byte[]
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);//压缩
        String headPicture = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);//加密转换成String

        return headPicture;
    }

    private Thread regis2server = new Thread() {
        @Override
        public void run() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("un", name.getText().toString());
                obj.put("pw", password1.getText().toString());
                obj.put("email",email.getText().toString());
                obj.put("school",schoolstr);
                String img = edit_headPicture(bmp);
                obj.put("img", img);
                obj.put("method", "2");
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
                connection.setReadTimeout(10_1000);
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

    Handler postHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 && res != null) {
                Toast.makeText(RegisterActivity.this, res, Toast.LENGTH_SHORT).show();

                final Data app = (Data)getApplication();
                app.setIsLogin(true);
                app.setUsername(name.getText().toString());
                app.setNickname(name.getText().toString());
                app.setBmp(bmp);

                Intent it = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(it);
            }
        }
    };
}
