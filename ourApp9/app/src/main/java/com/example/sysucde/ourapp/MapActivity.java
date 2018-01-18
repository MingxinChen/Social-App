package com.example.sysucde.ourapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class MapActivity extends AppCompatActivity implements OnGetPoiSearchResultListener {

    private MapView mMapView;
    private BaiduMap mMap;
    private BDLocation currentLocation;
    private Button btn_comfirm;
    private ImageButton btn_center;

    public LocationClient mLocationClient;
    public BDLocationListener mLocationListener;

    private Marker marker;
    private boolean isFirst = true;

    private PoiSearch mPoiSearch;
    private PoiDetailResult mPoiDetailResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        initial();
        initialMap();
        btn_comfirm = (Button)findViewById(R.id.position_confirm);
        btn_comfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        ExitApplication.getInstance().addActivity(this);
    }

    private void initial() {
     //   initialActionBar();

        btn_center = (ImageButton) findViewById(R.id.btn_center);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void save() {
        // 假如用户没有选择地点那么提醒用户选择地点，否则返回数据
        if (mPoiDetailResult == null) {
            Toast.makeText(this, "请选择一个地点", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Intent intent = new Intent();

            intent.putExtra("placeDescription",
                    mPoiDetailResult.getAddress() + mPoiDetailResult.getName());
            intent.putExtra("placeUid", mPoiDetailResult.getUid());
            intent.putExtra("longitude", mPoiDetailResult.getLocation().longitude);
            intent.putExtra("latitude", mPoiDetailResult.getLocation().latitude);

            setResult(RESULT_OK, intent);
            finish();
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
                Toast.makeText(MapActivity.this, "请选择地图上带有文字的地点或建筑",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                if (mPoiSearch == null) {
                    mPoiSearch = PoiSearch.newInstance();
                }

                mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(mapPoi.getUid()));
                mPoiSearch.setOnGetPoiSearchResultListener(MapActivity.this);
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

            if (marker != null) {
                marker.remove();
            }

            marker = (Marker) mMap.addOverlay(option);

            mPoiDetailResult = poiDetailResult;
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
    }
}