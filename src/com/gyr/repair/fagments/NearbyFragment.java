package com.gyr.repair.fagments;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gyr.repair.R;
import com.gyr.repair.activities.EngineerActivity;
import com.gyr.repair.activities.OrderActivity;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.http.GetEngineerlistPostService;
import com.gyr.repair.http.GetOrderlistPostService;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.platform.comapi.map.y;

public class NearbyFragment extends Fragment implements OnClickListener, OnMarkerClickListener{
	
	private Button showengineersbt, showordersbt, showallbt, refreshbt, nearbyclearbt;
	private TextView engineersnumbertv, ordersnumbertv;
	
	LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();
    
    private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;
	
	boolean isFirstLoc = true;
	
	private BitmapDescriptor bde = BitmapDescriptorFactory.fromResource(R.drawable.icon_marke);
	private BitmapDescriptor bdo = BitmapDescriptorFactory.fromResource(R.drawable.icon_marko);
	
	private int engineersnum, ordersnum;
	private int countengineer, countorder;
	
	private static ArrayList<Integer> cursorengineer, cursororder;
	
	private ArrayList<LatLng> latlngEngineer;
	private ArrayList<LatLng> latlngOrder;
	private ArrayList<Marker> markerEngineer;
	private ArrayList<Marker> markerOrder;
	
	private static LatLng llleft, llright;
	
	private static boolean flagEngineer = true;
	private static boolean flagOrder = true;
	
	private Handler refreshmapHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;
	
	private boolean isfirst = true;
	
	private static boolean initflag = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_nearby, container,
				false);
		
		showengineersbt = (Button) view.findViewById(R.id.bt_showengineers);
		showengineersbt.setOnClickListener(this);
		showordersbt = (Button) view.findViewById(R.id.bt_showorders);
		showordersbt.setOnClickListener(this);
		showallbt = (Button) view.findViewById(R.id.bt_showall);
		showallbt.setOnClickListener(this);
		refreshbt = (Button) view.findViewById(R.id.bt_refresh);
		refreshbt.setOnClickListener(this);
		nearbyclearbt = (Button) view.findViewById(R.id.bt_nearbyclear);
		nearbyclearbt.setOnClickListener(this);
		
		engineersnumbertv = (TextView) view.findViewById(R.id.tv_engineernum2);
		ordersnumbertv = (TextView) view.findViewById(R.id.tv_ordernum2);
		
		isFirstLoc = true;
		
		isfirst = true;
		
		mMapView = (MapView) view.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		
		mBaiduMap.setMyLocationEnabled(true);
		
		mLocClient = new LocationClient(getActivity().getApplicationContext());
        mLocClient.registerLocationListener(myListener);
		
		LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        
        mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChange(MapStatus arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus status) {
				updateMapState();
				
			}

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
		
		refreshmapHandler = new Handler() {
			
			@Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				initMap();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(getActivity(), "载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
                }
			}
			
		};
		
		cursorengineer = new ArrayList<Integer>();
		cursororder = new ArrayList<Integer>();
		
		if (initflag) {
			refreshMap();
		}
        
		return view;
	}
	
	private void updateMapState() {
		//LatLng mCenterLatLng = status.target;
		
		//double centerlat = mCenterLatLng.latitude;
		//double centerlng = mCenterLatLng.longitude;
		
		WindowManager wm = getActivity().getWindowManager();
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		int width = outMetrics.widthPixels;
		int height = outMetrics.heightPixels;
		
		Point ptleft = new Point();
		ptleft.x = 0;
		ptleft.y = 0;
		llleft = mBaiduMap.getProjection().fromScreenLocation(ptleft);
		
		Point ptRight = new Point();
		ptRight.x = width;
		ptRight.y = height;
		llright = mBaiduMap.getProjection().fromScreenLocation(ptRight);
		
		initflag = true;
		
		if (isfirst) {
			refreshMap();
		} else {
			initMap();
		}
		
	}
	
	private void initMap() {
        
        latlngEngineer = new ArrayList<LatLng>();
        latlngOrder = new ArrayList<LatLng>();
        markerEngineer = new ArrayList<Marker>();
        markerOrder = new ArrayList<Marker>();
        
        double mlatitude, mlongitude;
        
        countengineer = 0;
        countorder = 0;
        
        cursorengineer.clear();
        cursororder.clear();
        
        try {
			JSONArray jsonArray1 = new JSONArray(ResponseCache.responseEngineerlist);
			engineersnum = jsonArray1.length();
			for (int i = 0; i < engineersnum; i++) {
				JSONObject jsonObject = jsonArray1.getJSONObject(i);
				mlatitude = jsonObject.getDouble("latitude");
				mlongitude = jsonObject.getDouble("longitude");
				if (llright.latitude < mlatitude && mlatitude < llleft.latitude && llleft.longitude < mlongitude && mlongitude < llright.longitude) {
					latlngEngineer.add(new LatLng (mlatitude, mlongitude));
					countengineer ++;
					cursorengineer.add(i);
				}
			}
				
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
			JSONArray jsonArray2 = new JSONArray(ResponseCache.responseOrderlist);
			ordersnum = jsonArray2.length();
			for (int i = 0; i < ordersnum; i++) {
				JSONObject jsonObject = jsonArray2.getJSONObject(i);
				mlatitude = jsonObject.getDouble("latitude");
				mlongitude = jsonObject.getDouble("longitude");
				if (llright.latitude < mlatitude && mlatitude < llleft.latitude && llleft.longitude < mlongitude && mlongitude < llright.longitude) {
					latlngOrder.add(new LatLng (mlatitude, mlongitude));
					countorder ++;
					cursororder.add(i);
				}
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        engineersnumbertv.setText(String.valueOf(countengineer));
        ordersnumbertv.setText(String.valueOf(countorder));
        
        flagEngineer = true;
        flagOrder = true;
        showAll();
        
        mBaiduMap.setOnMarkerClickListener(this);
        
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (flagEngineer) {
			for (int i = 0; i < countengineer; i++) {
				if (marker == markerEngineer.get(i)) {
					Intent intent = new Intent(getActivity().getApplicationContext(), EngineerActivity.class);
					intent.putExtra("cursor", cursorengineer.get(i));
					startActivity(intent);
				}
			}
		}
		if (flagOrder) {
			for (int i = 0; i < countorder; i++) {
				if (marker == markerOrder.get(i)) {
					Intent intent = new Intent(getActivity().getApplicationContext(), OrderActivity.class);
					intent.putExtra("flag", 1);
					intent.putExtra("cursor", cursororder.get(i));
					startActivity(intent);
				}
			}
		}
		return true;
	}
	
	private void refreshMap() {
		isfirst = false;
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(getActivity(), null, null, true);
			RefreshMapPostThread refreshPostThread = new RefreshMapPostThread("unreceived");
			refreshPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, refreshmapHandler);
				}
				
			}, 10000);
		} else {
    		Toast.makeText(getActivity(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_showengineers:
			flagEngineer = true;
			flagOrder = false;
			markerEngineer.clear();
			mBaiduMap.clear();
			showEngineers();
			break;
		case R.id.bt_showorders:
			flagEngineer = false;
			flagOrder = true;
			markerOrder.clear();
			mBaiduMap.clear();
			showOrders();
			break;
		case R.id.bt_showall:
			flagEngineer = true;
	        flagOrder = true;
			markerEngineer.clear();
			markerOrder.clear();
			mBaiduMap.clear();
			showAll();
			break;
		case R.id.bt_refresh:
			flagEngineer = true;
	        flagOrder = true;
			markerEngineer.clear();
			markerOrder.clear();
			mBaiduMap.removeMarkerClickListener(this);
			mBaiduMap.clear();
			refreshMap();
			break;
		case R.id.bt_nearbyclear:
			markerEngineer.clear();
			markerOrder.clear();
			mBaiduMap.clear();
			break;
		}
		
	}
	
	private void showAll() {
		showOrders();
		showEngineers();
		
	}

	private void showEngineers() {
		for (int i = 0; i < countengineer; i++) {
			MarkerOptions ooe = new MarkerOptions().position(latlngEngineer.get(i)).icon(bde);
			markerEngineer.add((Marker)(mBaiduMap.addOverlay(ooe)));
		}
		
	}

	private void showOrders() {
		for (int i = 0; i < countorder; i++) {
			MarkerOptions ooo = new MarkerOptions().position(latlngOrder.get(i)).icon(bdo);
			markerOrder.add((Marker)(mBaiduMap.addOverlay(ooo)));
		}
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
		super.onDestroy();
	}
	
	private class RefreshMapPostThread extends Thread {
		public String status;

		public RefreshMapPostThread(String status) {
			super();
			this.status = status;
		}

		@Override
		public void run() {
			if (!status.equals("")) {
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				List<NameValuePair> params2 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("status", status));
                params2.add(new BasicNameValuePair("default", ""));
                ResponseCache.responseOrderlist = GetOrderlistPostService.send(params1);
                ResponseCache.responseEngineerlist = GetEngineerlistPostService.send(params2);
                Log.i("guan", "Nearby: responseMsg = " + ResponseCache.responseOrderlist);
                Log.i("guan", "Nearby: responseMsg = " + ResponseCache.responseEngineerlist);
				if (ResponseCache.responseOrderlist.equals("FAILED") || ResponseCache.responseEngineerlist.equals("FAILED")) {
                	sendMsg(FAILED, refreshmapHandler);
                } else {
                	sendMsg(SUCCEEDED, refreshmapHandler);
                }
			}
		}
		
	}
	
    private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                //MapStatus mapStatus = new MapStatus.Builder().target(ll).zoom(16.0f).build();
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(16.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        	
        }
        
    }
    
    private boolean isConnectingToInternet() {
    	ConnectivityManager connectivity = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
    	return false;
    }
    
	private void sendMsg(int i, Handler handler){
		Message msg = new Message();
		msg.what = i;
		handler.sendMessage(msg);
	}
    
}
