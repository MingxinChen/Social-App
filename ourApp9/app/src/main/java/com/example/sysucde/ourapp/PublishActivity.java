package com.example.sysucde.ourapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import org.json.JSONTokener;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class PublishActivity extends AppCompatActivity {
    Toolbar mToolbar;
    TextView mChooseLocation;
    EditText mText;
    ImageView mPostPhoto;
    Uri imgUri=null;
    Bitmap bmp = null;
    private String placeDescription, placeUid;
    private double longitude, latitude;

    String username = "";
    String res = "no";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        mChooseLocation = (TextView)findViewById(R.id.choose_location);
        mPostPhoto = (ImageView)findViewById(R.id.post_photo);
        mText = (EditText)findViewById(R.id.post_text);

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
        mChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toChooseLocation();
            }
        });
        mPostPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSetHeader();
            }
        });

        final Data app = (Data)getApplication();
        username = app.getUsername();

        Button sendButton = (Button)findViewById(R.id.post);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                /*发送要发布的动态到服务器*/
                send();
            }
        });

    }

    /*发送要发布的动态到服务器*/
    public void send(){
        if(mChooseLocation.getText().toString().equals("轻触以选择地点")){
            Toast.makeText(this, "请选择地理位置", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(sendTh).start();
        //等待UI
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

    public String connectServer(JSONObject object) {
        String result = "";
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://139.199.6.110:8080/Login/publish");

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

    private String edit_headPicture(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//将Bitmap转成Byte[]
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);//压缩
        String headPicture = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);//加密转换成String
        //int n = headPicture.length();

        return headPicture;
    }

    Thread sendTh = new Thread() {
        @Override
        public void run() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("un", username);
                obj.put("text", mText.getText().toString());
                obj.put("img", edit_headPicture(bmp));
                obj.put("description", placeDescription);
                obj.put("uid", placeUid);
                obj.put("longitude", longitude);
                obj.put("latitude", latitude);

                res = connectServer(obj);

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
                waitHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    };

    Handler waitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 && res.equals("yes")) {
                //Toast.makeText(PublishActivity.this, "发布动态成功", Toast.LENGTH_LONG).show();

                Intent it = new Intent(PublishActivity.this, HomeActivity.class);
                startActivity(it);
            }
            else {
                Toast.makeText(PublishActivity.this, "发布失败", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void toChooseLocation(){
        Intent intent = new Intent();
        intent.setClass(PublishActivity.this, MapActivity.class);
        startActivityForResult(intent, 0);
    }

    private void toSetHeader(){
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);
        it.setType("images/*");
        startActivityForResult(it, 101);
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

    private void showImg() {
        int iw, ih, vw, vh;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgUri.getPath(), option);
        iw = option.outWidth;
        ih = option.outHeight;
        vw = mPostPhoto.getWidth();
        vh = mPostPhoto.getHeight();
        int scaleFactor= Math.min(iw/vw, ih/vh);
        option.inJustDecodeBounds = false;
        option.inSampleSize = scaleFactor;
        bmp= BitmapFactory.decodeFile(imgUri.getPath(), option);
        Toast.makeText(this, imgUri.getPath()+"\n"+iw+" "+ih+"\n"+vw+" "+vh, Toast.LENGTH_LONG).show();
        mPostPhoto.setImageBitmap(bmp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            placeDescription = data.getExtras().getString("placeDescription");
            placeUid = data.getExtras().getString("placeUid");
            longitude = data.getExtras().getDouble("longitude");
            latitude = data.getExtras().getDouble("latitude");

            mChooseLocation.setText(placeDescription);
            return;
        }
        else{
            if(resultCode== Activity.RESULT_OK){
                switch(requestCode){
                    case 101:
                        imgUri = converUri(data.getData());
                        break;
                }
                showImg();
                return;
            }
            Toast.makeText(this, "请求失败：未知错误", Toast.LENGTH_SHORT).show();
        }
    }
}
