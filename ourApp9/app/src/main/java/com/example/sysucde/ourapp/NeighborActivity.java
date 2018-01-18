package com.example.sysucde.ourapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NeighborActivity extends AppCompatActivity implements OnGetPoiSearchResultListener {


    private MapView mMapView;
    private BaiduMap mMap;
    private BDLocation currentLocation;
    private ImageButton btn_center;

    public LocationClient mLocationClient;
    public BDLocationListener mLocationListener;

    private Marker marker;
    private boolean isFirst = true;

    private PoiSearch mPoiSearch;
    private PoiDetailResult mPoiDetailResult = null;

  //  private Location loc;
    private LatLng point1 = new LatLng(23.064668,113.3988);
    private LatLng point2 = new LatLng(23.063668,113.3948);
    private LatLng point3 = new LatLng(23.059668,113.4188);


    String res = "";

    private List<LatLng> neigh_list = new ArrayList<LatLng>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_neighbor);
        initial();
        initialMap();
        //从服务器端获取附近校友的位置
        getNeighbor();
        showPos();

        ExitApplication.getInstance().addActivity(this);
    }


    private void initial() {
        //   initialActionBar();

        btn_center = (ImageButton) findViewById(R.id.btn_center2);
        // 将用户的位置居中
        btn_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null) {
                    float zoom = mMapView.getMap().getMapStatus().zoom;
                    if (zoom < 19) {
                        zoom = 19;
                    }

                    MapStatus mMapStatus = new MapStatus.Builder()
                            .target(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()))
                            .zoom(zoom).build();
                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                    mMapView.getMap().setMapStatus(mMapStatusUpdate);
                }
            }
        });
    }

    private void initialActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //  actionBar.setTitle("选择地点");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }


    public void getNeighbor() {
       // 23.064668,113.3988
        LatLng l1 = new LatLng(23.064511,113.399224);
        LatLng l2 = new LatLng(23.065039, 113.39736);
        LatLng l3 = new LatLng(23.063601,113.398218);
        neigh_list.add(l1);
        neigh_list.add(l2);
        neigh_list.add(l3);
       // new Thread(getNeighborTh).start();

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

    public static List<LatLng> getLatLng(String key, String jsonString) {
        List<LatLng> list = new ArrayList<LatLng>();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            JSONArray LatLngs = jsonObject.getJSONArray(key);
            for (int i = 0; i < LatLngs.length(); i++) {
                JSONObject jsonObject2 = LatLngs.getJSONObject(i);
                double lo = jsonObject2.getDouble("longitude");
                double la = jsonObject2.getDouble("latitude");

                LatLng latlng = new LatLng(la, lo);

                list.add(latlng);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return list;
    }
/*
    private Thread getNeighborTh = new Thread() {
        @Override
        public void run() {
            //传json格式文件给服务器
            JSONObject obj = new JSONObject();
            try {
                obj.put("longitude", currentLocation.getLatitude());
                obj.put("latitude", currentLocation.getLatitude());
                res = Server(obj);

                try{
                    JSONTokener jsonParser = new JSONTokener(res);
                    JSONObject userinfo = (JSONObject)jsonParser.nextValue();
                    res = userinfo.getString("mes");
                    if(res.equals("yes")) {
                        String key = "neighbors";
                        neigh_list = getLatLng(key, userinfo.getString("neighbors"));
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
    */
/*
    Handler getHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 && res.equals("yes")) {
                for(int i = 0; i < neigh_list.size(); i++) {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.marker);
                    OverlayOptions option = new MarkerOptions().icon(icon).position(neigh_list.get(i));
                    marker = (Marker) mMap.addOverlay(option);
                }
                showPos();
            }
        }
    };
*/
    public void showPos() {
    /*   BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.marker);
        OverlayOptions option1 = new MarkerOptions().icon(icon).position(point1);
        marker = (Marker) mMap.addOverlay(option1);
        OverlayOptions option2 = new MarkerOptions().icon(icon).position(point2);
        marker = (Marker) mMap.addOverlay(option2);
        OverlayOptions option3 = new MarkerOptions().icon(icon).position(point3);
        marker = (Marker) mMap.addOverlay(option3);*/
        for(int i = 0; i < neigh_list.size(); i++) {
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.marker);
            OverlayOptions option = new MarkerOptions().position(neigh_list.get(i)).icon(icon);
            marker = (Marker) mMap.addOverlay(option);
        }
    }
    private void initialMap() {
        mMapView = (MapView) findViewById(R.id.BMapView);

        mMap = mMapView.getMap();

        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.mipmap.map_marker), 100, 100, true);
        BitmapDescriptor bitmapD = BitmapDescriptorFactory
                .fromBitmap(bitmap);

        mMapView.getMap().setMyLocationEnabled(true);
        mMapView.getMap().setMapType(BaiduMap.MAP_TYPE_NORMAL);
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, bitmapD);
        mMapView.getMap().setMyLocationConfigeration(config);

        // 用户选择地点将查询POI的详情
        mMapView.getMap().setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(NeighborActivity.this, "latitude="+ latLng.latitude +",longitude="+ latLng.longitude,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                if (mPoiSearch == null) {
                    mPoiSearch = PoiSearch.newInstance();
                }

                mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(mapPoi.getUid()));
                mPoiSearch.setOnGetPoiSearchResultListener(NeighborActivity.this);
                return false;
            }
        });

        // 初始化位置监听器，在地图上显示用户的位置
        mLocationListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                MyLocationData myLocationData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();

                mMap.setMyLocationData(myLocationData);

                currentLocation = location;

                if (isFirst) {
                    btn_center.callOnClick();
                    isFirst = false;
                }
            }
        };

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(mLocationListener);


        initialLocation();
        mLocationClient.start();
    }

    private void initialLocation() {
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd09ll");
        option.setScanSpan(2000);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);

        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        if (mPoiSearch != null) {
            mPoiSearch.destroy();
        }

        mPoiSearch = null;

        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        if (mPoiSearch != null) {
            mPoiSearch.destroy();
        }
        mPoiSearch = null;

        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {

    }

    // 获取到POI的详情那么在对应的点上添加marker
    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        String address = poiDetailResult.getAddress();
        String name = poiDetailResult.getName();

        if (address != null && !address.isEmpty() &&
                name != null && !name.isEmpty()) {
            Toast.makeText(this,
                    address + name,
                    Toast.LENGTH_SHORT).show();

            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.mipmap.marker);

            OverlayOptions option = new MarkerOptions()
                    .position(poiDetailResult.getLocation())
                    .icon(bitmap);
/*
            if (marker != null) {
                marker.remove();
            }
*/
            marker = (Marker) mMap.addOverlay(option);

            mPoiDetailResult = poiDetailResult;
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
    }
}