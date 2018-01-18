package com.example.sysucde.ourapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.design.widget.CollapsingToolbarLayout;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import static java.lang.Thread.sleep;

public class DiscoveryActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private RecyclerView mRecyclerView;
    FloatingActionButton btn_publish;

    int count = 0;
    int comment_id = -1;

    private LocationManager locationManager;//位置服务
    private Location location;
    double longtitude, latitude;
    String school;
    //private List<AuthorInformation> datas=new ArrayList<AuthorInformation>();
    String res = "";
    myAdapter adapter;

    private LocationClient mLocationClient;
    private BDLocationListener mBDLocationListener;
    private BDLocation currentlocation;
    GeoCoder geoCoder;
    String address;

    List<AuthorInformation> cards = new ArrayList<AuthorInformation>();
    AuthorInformation card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        ExitApplication.getInstance().addActivity(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_discovery);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_publish = (FloatingActionButton)findViewById(R.id.publish);
        btn_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPublish();
            }
        });

        final Data app = (Data)getApplication();
        String nickname = app.getNickname();
        school = app.getSchool();

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mCollapsingToolbarLayout.setTitle(nickname);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        List<AuthorInformation> datas=new ArrayList<AuthorInformation>();
        getData(datas);
        //initialAdapter();
        Log.i("初始化：","adapter");
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new myAdapter(datas);
        mRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new onRecyclerViewItemClickListener(){
            @Override
            public void onItemClick(View view,String data){
                //Toast.makeText(DiscoveryActivity.this,"你好,我是" + data, Toast.LENGTH_SHORT).show();
                Log.i("回复的动态id：", data);
                toReply(data);
            }
        });

        // 声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        mBDLocationListener = new MyBDLocationListener();
        // 注册监听
        mLocationClient.registerLocationListener(mBDLocationListener);

       // Button btn_location = (Button) findViewById(R.id.btn_location);
      //  btn_location.performClick();
        // 声明定位参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式 高精度
        option.setCoorType("bd09ll");// 设置返回定位结果是百度经纬度 默认gcj02
        option.setScanSpan(5000);// 设置发起定位请求的时间间隔 单位ms
        option.setIsNeedAddress(true);// 设置定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 设置定位结果包含手机机头 的方向
        // 设置定位参数
        mLocationClient.setLocOption(option);
        // 启动定位
        mLocationClient.start();

        Log.i("纬度", latitude+"");
        Log.i("经度", longtitude+"");
        geoCoder = GeoCoder.newInstance();
    }

    /** 获得所在位置经纬度及详细地址 */
    public void getLocation(View view) {
        // 声明定位参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式 高精度
        option.setCoorType("bd09ll");// 设置返回定位结果是百度经纬度 默认gcj02
        option.setScanSpan(5000);// 设置发起定位请求的时间间隔 单位ms
        option.setIsNeedAddress(true);// 设置定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 设置定位结果包含手机机头 的方向
        // 设置定位参数
        mLocationClient.setLocOption(option);
        // 启动定位
        mLocationClient.start();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // 取消监听函数
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(mBDLocationListener);
        }
    }

    private class MyBDLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // 非空判断
            if (location != null) {
                // 根据BDLocation 对象获得经纬度以及详细地址信息
                double latitu = location.getLatitude();
                double longitu = location.getLongitude();
                String address = location.getAddrStr();
                Log.i("位置", "address:" + address + " latitude:" + latitu
                        + " longitude:" + longitu + "---");
                currentlocation = location;
                latitude = currentlocation.getLatitude();
                longtitude = currentlocation.getLongitude();
                Log.d("longtitude: ", longtitude+"");
                Log.d("latitude: ", latitude+"");
                if (mLocationClient.isStarted()) {
                    // 获得位置之后停止定位
                    mLocationClient.stop();
                    /*从服务器取回附近的动态的数据*/
                    getDataFromServer();
                }
            }
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
            URL url = new URL("http://139.199.6.110:8080/Login/getschool");

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

    public void toPublish() {
        Intent it=new Intent(this, PublishActivity.class);
        startActivity(it);
    }

    /**
     * 反地理编码得到地址信息
     */
    private String reverseGeoCode(LatLng latLng) {
        // 创建地理编码检索实例
      /*  GeoCoder geoCoder = GeoCoder.newInstance();
        //
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            // 反地理编码查询结果回调函数
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                    Toast.makeText(DiscoveryActivity.this, "抱歉，未能找到结果",
                            Toast.LENGTH_LONG).show();
                }
                Toast.makeText(DiscoveryActivity.this,
                        "位置：" + result.getAddress(), Toast.LENGTH_LONG)
                        .show();
            }

            // 地理编码查询结果回调函数
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检测到结果
                }
            }
        };
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);
        //
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        // 释放地理编码检索实例
        // geoCoder.destroy();
        */
        return "纬度：" + latLng.latitude + ";经度：" + latLng.longitude;
    }


    public void toReply(String data) {
        Intent it = new Intent(this, ReplyActivity.class);
        int index = Integer.parseInt(data);
        AuthorInformation temp_card = cards.get(0);
        for(int i = 0; i < cards.size(); i++) {
            if(index == cards.get(i).message_id) {
                temp_card = cards.get(i);
                comment_id = i;
                break;
            }
        }
        it.putExtra("message_id", String.valueOf(comment_id));
        it.putExtra("mes_id", String.valueOf(temp_card.message_id));
        it.putExtra("content", temp_card.motto);
        /*it.putExtra("bitmap", edit_headPicture(temp_card.picture));
        it.putExtra("header", edit_headPicture(temp_card.header));*/
        it.putExtra("nickname",temp_card.nickname);

        final Data app = (Data)getApplication();
        app.setTemp_header(temp_card.header);
        app.setTemp_header(temp_card.picture);
        //it.putExtra("writer", app.getUsername());

        startActivityForResult(it, 0);
    }

    public void getData(List<AuthorInformation> datas) {
        Log.i("hahah","getData");
        String nickname, motto ,location;
        int message_id;
        Bitmap header, picture;

        header = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        picture = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        nickname = "";
        motto = "";
        message_id = 0;
        location = "";
        datas.add(new AuthorInformation(header, nickname, picture, motto, message_id, location));
    }

    public void getDataFromServer(){
        Log.i("hahah","hhh");
        adapter.removeItem(0);

        /*String nickname, motto ,location;
        int message_id, index;
        Bitmap header, picture;*/

        if(count < 3) new Thread(discoveryTh).start();

        /*header = BitmapFactory.decodeResource(getResources(), R.mipmap.head1);
        picture = BitmapFactory.decodeResource(getResources(), R.mipmap.bg2);
        nickname = "MCC_小叉";
        motto = "今天哼哼也没有发新照片！";
        message_id = 1;
        location = "中山大学";
        AuthorInformation card = new AuthorInformation(header, nickname, picture, motto, message_id, location);
        card.setReply("压禽", "好的！");
        index = adapter.adatas.size();
        adapter.addItem(index, card);*/
    }

    private String edit_headPicture(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//将Bitmap转成Byte[]
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);//压缩
        String headPicture = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);//加密转换成String

        return headPicture;
    }

    private Bitmap decode_headPicture(String headPicture) {
        if(headPicture.equals("")) {
            return null;
        }
        byte[] bytes = Base64.decode(headPicture, Base64.DEFAULT);
        Bitmap bp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return bp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                Bundle b = data.getExtras(); //data为B中回传的Intent
                String ni = b.getString("nickname");//str即为回传的值
                String co = b.getString("result");
                int ind = Integer.parseInt(b.getString("index"));
                cards.get(ind).setReply(ni, co);
                adapter.modifyItem(ind,cards.get(ind));

                break;
            default:
                break;
        }
    }

    Thread discoveryTh = new Thread() {
        @Override
        public void run() {
            super.run();
            //获取地理位置传给服务器并接受信息 接受的信息为3个AuthorInformation
            JSONObject obj = new JSONObject();
            try {
                obj.put("longitude", longtitude);
                obj.put("latitude", latitude);
                obj.put("school", school);
                res = connectServer(obj);

                //将res转成JSON
                try{
                    /*JSONTokener jsonParser = new JSONTokener(res);
                    JSONObject userinfo = (JSONObject)jsonParser.nextValue();*/
                    JSONObject userinfo = new JSONObject(res);
                    res = userinfo.getString("mes");
                    JSONArray jsa = userinfo.getJSONArray("moments");
                    for(int i = 0; i < jsa.length(); i++) {
                        JSONObject oj = jsa.getJSONObject(i);
                        int mes_id = oj.getInt("mes_id");
                        String text = oj.getString("text");
                        String img = oj.getString("img");
                        String header = oj.getString("header");
                        String nickname = oj.getString("nickname");
                        /*double lo = oj.getDouble("longitude");
                        double la = oj.getDouble("latitude");
                        LatLng ll = new LatLng(la,lo);*/

                        /*Bundle bu = new Bundle();
                        bu.putInt("mes_id", mes_id);*/

                        //String temp_addr = reverseGeoCode(ll);
                        //Log.i("地址", address);
                        String temp_addr = oj.getString("description");
                        Bitmap bp1 = decode_headPicture(header);
                        Bitmap bp2 = decode_headPicture(img);
                        card = new AuthorInformation(bp1, nickname, bp2, text, mes_id, temp_addr);

                        JSONArray jsa2 = oj.getJSONArray("comment");
                        for(int j = 0; j < jsa2.length(); j++) {
                            JSONObject oj2 = jsa2.getJSONObject(i);
                            String ni = oj2.getString("nickname");
                            String tx = oj2.getString("text");
                            card.setReply(ni, tx);
                        }
                        Message msg = Message.obtain();
                        msg.what = 0;
                        postHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return ;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return ;
            }
        }
    };

    Handler postHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 && res != null) {
                if(res.compareTo("yes") == 0) {
                    res = "original";
                    int index = adapter.adatas.size();
                    cards.add(card);
                    adapter.addItem(index, card);
                    count++;
                    if(count < 3) new Thread(discoveryTh).start();
                }
            }
        }
    };

    public  interface onRecyclerViewItemClickListener {

        void onItemClick(View v, String tag);
    }

    public class myAdapter extends RecyclerView.Adapter<myAdapter.myViewHolder>{
        private List<AuthorInformation> adatas;
        private onRecyclerViewItemClickListener itemClickListener = null;

        public myAdapter(List<AuthorInformation> authorInfoList) {
            adatas = new ArrayList<AuthorInformation>();
            for(int i=0;i<authorInfoList.size();i++) {
                AuthorInformation a  = authorInfoList.get(i);
                adatas.add(a);
            }
        }

        public void removeItem(int position){
            adatas.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,adatas.size() - position);//通知数据与界面重新绑定
            notifyDataSetChanged();//通知重新绑定所有数据与界面
        }

        public void addItem(int position, AuthorInformation card){
            adatas.add(position,card);
            notifyItemInserted(position);
            notifyItemRangeChanged(position,adatas.size() - position);//通知数据与界面重新绑定
            notifyDataSetChanged();//通知重新绑定所有数据与界面
        }
        public void modifyItem(int position, AuthorInformation card) {
            adatas.remove(position);
            adatas.add(position, card);
            notifyItemRemoved(position);
            notifyItemInserted(position);
            notifyItemRangeChanged(position, adatas.size()-position);
            notifyDataSetChanged();
        }

        @Override
        public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_card_layout,parent, false);
            myViewHolder holder = new myViewHolder(view);

            holder.replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(DiscoveryActivity.this, this.toString(), Toast.LENGTH_LONG).show();
                    //TextView idView = (TextView) v.findViewById(R.id.message_id_block);
                    //int id = Integer.parseInt(idView.getText().toString());
                    //Toast.makeText(DiscoveryActivity.this, id+"", Toast.LENGTH_LONG).show();
                    Log.i("view v:", v.getId()+"");
                    Log.i("view v:", v.toString()+"");
                    if (itemClickListener != null){
                        itemClickListener.onItemClick(v,(v.getTag()+""));
                        Log.i("onClick", v.getTag()+"");
                    }
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(myViewHolder holder, int position) {
            AuthorInformation card = adatas.get(position);
            holder.header.setImageBitmap(card.header);
            holder.picture.setImageBitmap(card.picture);
            holder.nickname.setText(card.nickname);
            holder.motto.setText(card.motto);
            holder.messageId.setText(card.message_id+"");
            holder.location.setText(card.location);
            Log.i("messageId",  holder.messageId.getText()+"");
            holder.replyButton.setTag(holder.messageId.getText()+"");
            Log.i("Tag",  holder.replyButton.getTag()+"");

            int replyNum = card.replyList.size();
            for(int i=0;i<replyNum;i++) {
                TextView tv = new TextView(DiscoveryActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(lp);
                tv.setText(card.replyList.get(i).writer + "："+card.replyList.get(i).words);
                tv.setTextSize(15);
                holder.replyWords.addView(tv);
            }
        }

        @Override
        public int getItemCount() {
            return adatas.size();
        }
        public void setOnItemClickListener(onRecyclerViewItemClickListener listener) {
            this.itemClickListener = listener;
            //  Log.d("ddd", itemClickListener.toString());
        }

        public class myViewHolder extends RecyclerView.ViewHolder {
            ImageView header;
            ImageView picture;
            TextView nickname;
            TextView motto;
            TextView messageId;
            ImageButton replyButton;
            LinearLayout replyWords;
            TextView location;
            public myViewHolder(View itemView) {
                super(itemView);
                header = (ImageView)itemView.findViewById(R.id.iv_portrait);
                picture = (ImageView)itemView.findViewById(R.id.discovery_photo);
                nickname = (TextView)itemView.findViewById(R.id.tv_nickname);
                messageId = (TextView) itemView.findViewById(R.id.message_id);
                motto = (TextView)itemView.findViewById(R.id.tv_motto);
                replyButton = (ImageButton)itemView.findViewById(R.id.reply_button);
                replyWords = (LinearLayout)itemView.findViewById(R.id.reply_area);
                location = (TextView)itemView.findViewById(R.id.location);
            }
        }
    }
}
