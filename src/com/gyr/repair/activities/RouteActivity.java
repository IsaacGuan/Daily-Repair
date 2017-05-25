package com.gyr.repair.activities;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.gyr.repair.R;
import com.gyr.repair.R.layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RouteActivity extends Activity implements BaiduMap.OnMapClickListener, OnGetRoutePlanResultListener, OnClickListener {
	
	private Button walkbt, drivebt, transitbt, routeclearbt;
	
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	
	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;
	private RoutePlanSearch mSearch = null;
	
	private WalkingRouteResult nowResultwalk = null;
	private TransitRouteResult nowResultransit = null;
	private DrivingRouteResult nowResultdrive  = null;
	
	private Button mBtnPre = null; // 上一个节点
	private Button mBtnNext = null; // 下一个节点
	private int nodeIndex = -1; // 节点索引,供浏览节点时使用
    private RouteLine route = null;
    private OverlayManager routeOverlay = null;
    private TextView popupText = null;
    
    private static double fromlatitude, fromlongitude, tolatitude, tolongitude;
    private static String city, address;
    
    private PlanNode stNode, enNode;
	
    //private boolean isFirstLoc = true;
    private boolean isFirstRoute = true;
    
    private boolean useAddress = true;
    
    private Handler routeHandler;
	private Timer timer;
	private ProgressDialog progressDialog;

	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		
		//isFirstLoc = true;
		isFirstRoute = true;
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		fromlatitude = bundle.getDouble("fromlatitude");
		fromlongitude = bundle.getDouble("fromlongitude");
		tolatitude = bundle.getDouble("tolatitude");
		tolongitude = bundle.getDouble("tolongitude");
		city = bundle.getString("city");
		address = bundle.getString("address");
		
		walkbt = (Button)findViewById(R.id.bt_walk);
		walkbt.setOnClickListener(this);
		drivebt = (Button)findViewById(R.id.bt_drive);
		drivebt.setOnClickListener(this);
		transitbt = (Button)findViewById(R.id.bt_transit);
		transitbt.setOnClickListener(this);
		routeclearbt = (Button)findViewById(R.id.bt_routeclear);
		routeclearbt.setOnClickListener(this);
		
		mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
		
		mMapView = (MapView) findViewById(R.id.bmapView_route);
		mBaiduMap = mMapView.getMap();
		
		mBaiduMap.setMyLocationEnabled(true);

		mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        
        routeHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
					timer.cancel();
					progressDialog.dismiss();
					break;
				case FAILED:
					Thread.interrupted();
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "超时",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
        	
        };
        
        progressDialog = ProgressDialog.show(this, "请稍候", "正在规划路线...", true);
        useAddress = true;
        stNode = PlanNode.withLocation(new LatLng(fromlatitude, fromlongitude));
        enNode = PlanNode.withCityNameAndPlaceName(city, address);
        mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
        timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendMsg(FAILED, routeHandler);
			}

		}, 10000);
        
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_walk:
			clickWalk();
			break;
		case R.id.bt_drive:
			clickDrive();
			break;
		case R.id.bt_transit:
			clickTransit();
			break;
		case R.id.bt_routeclear:
			clickRouteClear();
			break;
		}
		
	}
	
	private void clickWalk() {
		mBaiduMap.clear();
		progressDialog = ProgressDialog.show(this, "请稍候", "正在规划路线...", true);
		useAddress = true;
		stNode = PlanNode.withLocation(new LatLng(fromlatitude, fromlongitude));
		enNode = PlanNode.withCityNameAndPlaceName(city, address);
		mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendMsg(FAILED, routeHandler);
			}

		}, 10000);
		
	}

	private void clickDrive() {
		mBaiduMap.clear();
		progressDialog = ProgressDialog.show(this, "请稍候", "正在规划路线...", true);
		useAddress = true;
		stNode = PlanNode.withLocation(new LatLng(fromlatitude, fromlongitude));
		enNode = PlanNode.withCityNameAndPlaceName(city, address);
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendMsg(FAILED, routeHandler);
			}

		}, 10000);
		
	}

	private void clickTransit() {
		mBaiduMap.clear();
		progressDialog = ProgressDialog.show(this, "请稍候", "正在规划路线...", true);
		useAddress = true;
		stNode = PlanNode.withLocation(new LatLng(fromlatitude, fromlongitude));
		enNode = PlanNode.withCityNameAndPlaceName(city, address);
		mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode).city(city).to(enNode));
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendMsg(FAILED, routeHandler);
			}

		}, 10000);
		
	}

	private void clickRouteClear() {
		mBaiduMap.clear();
		mBtnPre.setVisibility(View.GONE);
        mBtnNext.setVisibility(View.GONE);
		
	}

	public void nodeClick(View v) {
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = null;
        
        if (route == null || route.getAllStep() == null) {
            return;
        }
        if (nodeIndex == -1 && v.getId() == R.id.pre) {
            return;
        }
        if (v.getId() == R.id.next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
                nodeIndex++;
            } else {
                return;
            }
        } else if (v.getId() == R.id.pre) {
            if (nodeIndex > 0) {
                nodeIndex--;
            } else {
                return;
            }
        }
        step = route.getAllStep().get(nodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }
        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
        
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        
        popupText = new TextView(RouteActivity.this);
        popupText.setText(nodeTitle);
        popupText.setBackgroundResource(R.drawable.popup);
        mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
	}
	
	private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            fromlatitude = location.getLatitude();
			fromlongitude = location.getLongitude();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            /*
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(17.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            */
        }

        public void onReceivePoi(BDLocation poiLocation) {
        	
        }
        
    }

	@Override
	public void onGetBikingRouteResult(BikingRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		sendMsg(SUCCEEDED, routeHandler);
		
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			if (useAddress) {
				useAddress = false;
				stNode = PlanNode.withLocation(new LatLng(fromlatitude, fromlongitude));
		        enNode = PlanNode.withLocation(new LatLng(tolatitude, tolongitude));
		        mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
			} else {
				Toast.makeText(RouteActivity.this, "抱歉，未找到驾车结果，请选择其他交通方式",
						Toast.LENGTH_SHORT).show();
				mBtnPre.setVisibility(View.GONE);
				mBtnNext.setVisibility(View.GONE);
			}
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) { 
        	nodeIndex = -1;
        	mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            
            if (result.getRouteLines().size() > 1 ) {
            	nowResultdrive = result;
            	MyTransitDlg myTransitDlg = new MyTransitDlg(RouteActivity.this,
            			result.getRouteLines(), RouteLineAdapter.Type.DRIVING_ROUTE);
            	myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {

					@Override
					public void onItemClick(int position) {
						route = nowResultdrive.getRouteLines().get(position);
						DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
						mBaiduMap.setOnMarkerClickListener(overlay);
						routeOverlay = overlay;
                        overlay.setData(nowResultdrive.getRouteLines().get(position));
                        overlay.addToMap();
                        overlay.zoomToSpan();
						
					}
            		
            	});
            	myTransitDlg.show();
            	
            } else if ( result.getRouteLines().size() == 1 ) {
				route = result.getRouteLines().get(0);
				DrivingRouteOverlay overlay = new DrivingRouteOverlay(
						mBaiduMap);
				routeOverlay = overlay;
				mBaiduMap.setOnMarkerClickListener(overlay);
				overlay.setData(result.getRouteLines().get(0));
				overlay.addToMap();
				overlay.zoomToSpan();
            } else {
                Log.d("guan", "结果数<0" );
                return;
            }
            
        }
		
	}

	@Override
	public void onGetIndoorRouteResult(IndoorRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetMassTransitRouteResult(MassTransitRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {
		sendMsg(SUCCEEDED, routeHandler);
		
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			if (useAddress) {
				useAddress = false;
				stNode = PlanNode.withLocation(new LatLng(fromlatitude, fromlongitude));
		        enNode = PlanNode.withLocation(new LatLng(tolatitude, tolongitude));
		        mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode).city(city).to(enNode));
			} else {
				Toast.makeText(RouteActivity.this, "抱歉，未找到公交结果，请选择其他交通方式",
						Toast.LENGTH_SHORT).show();
				mBtnPre.setVisibility(View.GONE);
				mBtnNext.setVisibility(View.GONE);
			}
        }
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            
            if (result.getRouteLines().size() > 1 ) {
            	nowResultransit = result;
            	MyTransitDlg myTransitDlg = new MyTransitDlg(RouteActivity.this, 
            			result.getRouteLines(), RouteLineAdapter.Type.TRANSIT_ROUTE);
            	myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {

					@Override
					public void onItemClick(int position) {
						route = nowResultransit.getRouteLines().get(position);
						TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
						mBaiduMap.setOnMarkerClickListener(overlay);
						routeOverlay = overlay;
                        overlay.setData(nowResultransit.getRouteLines().get(position));
                        overlay.addToMap();
                        overlay.zoomToSpan();
						
					}
            		
            	});
            	myTransitDlg.show();
            	
            } else if ( result.getRouteLines().size() == 1 ) {
            	route = result.getRouteLines().get(0);
            	TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
            	mBaiduMap.setOnMarkerClickListener(overlay);
            	routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            } else {
                Log.i("guan", "结果数<0" );
                return;
            }
            
		}
		
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		sendMsg(SUCCEEDED, routeHandler);
		
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			if (useAddress) {
				useAddress = false;
				stNode = PlanNode.withLocation(new LatLng(fromlatitude, fromlongitude));
		        enNode = PlanNode.withLocation(new LatLng(tolatitude, tolongitude));
		        mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
			} else {
				Toast.makeText(RouteActivity.this, "抱歉，未找到步行结果，请选择其他交通方式",
						Toast.LENGTH_SHORT).show();
				mBtnPre.setVisibility(View.GONE);
				mBtnNext.setVisibility(View.GONE);
			}
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
        	nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            
			if (isFirstRoute) {
				isFirstRoute = false;
				route = result.getRouteLines().get(0);
				WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
				mBaiduMap.setOnMarkerClickListener(overlay);
				routeOverlay = overlay;
				overlay.setData(result.getRouteLines().get(0));
				overlay.addToMap();
				overlay.zoomToSpan();
				
			} else {
				if (result.getRouteLines().size() > 1 ) {
					nowResultwalk = result;
					MyTransitDlg myTransitDlg = new MyTransitDlg(RouteActivity.this,
							result.getRouteLines(),RouteLineAdapter.Type.WALKING_ROUTE);
					myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {

						@Override
						public void onItemClick(int position) {
							route = nowResultwalk.getRouteLines().get(position);
							WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
	                        mBaiduMap.setOnMarkerClickListener(overlay);
	                        routeOverlay = overlay;
	                        overlay.setData(nowResultwalk.getRouteLines().get(position));
	                        overlay.addToMap();
	                        overlay.zoomToSpan();
							
						}
						
					});
					myTransitDlg.show();
					
				} else if (result.getRouteLines().size() == 1) {
					route = result.getRouteLines().get(0);
	                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
	                mBaiduMap.setOnMarkerClickListener(overlay);
	                routeOverlay = overlay;
	                overlay.setData(result.getRouteLines().get(0));
	                overlay.addToMap();
	                overlay.zoomToSpan();
	                
				} else {
	                Log.d("guan", "结果数<0" );
	                return;
	            }
				
			}
			
        }
		
	}
	
	@Override
    public void onMapClick(LatLng point) {
        mBaiduMap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mSearch != null) {
            mSearch.destroy();
        }
        mMapView.onDestroy();
        super.onDestroy();
    }

	private void sendMsg(int i, Handler handler) {
		Message msg = new Message();
		msg.what = i;
		handler.sendMessage(msg);
	}
    
    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List< ? extends RouteLine> transitRouteLines,  RouteLineAdapter.Type
                type) {
            this( context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new  RouteLineAdapter( context, mtransitRouteLines , type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener( new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemInDlgClickListener.onItemClick( position);
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);
                    dismiss();

                }
            });
        }

        public void setOnItemInDlgClickLinster( OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }

    }
    
    static class RouteLineAdapter extends BaseAdapter {

        private List<? extends  RouteLine> routeLines;
        private LayoutInflater layoutInflater;
        private Type mtype;

        public RouteLineAdapter(Context context, List<?extends RouteLine> routeLines, Type type) {
            this.routeLines = routeLines;
            layoutInflater = LayoutInflater.from( context);
            mtype = type;
        }

        @Override
        public int getCount() {
            return routeLines.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NodeViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.activity_transit_item, null);
                holder = new NodeViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.transitName);
                holder.lightNum = (TextView) convertView.findViewById(R.id.lightNum);
                holder.dis = (TextView) convertView.findViewById(R.id.dis);
                convertView.setTag(holder);
            } else {
                holder = (NodeViewHolder) convertView.getTag();
            }

            switch (mtype) {
                case TRANSIT_ROUTE:
                case WALKING_ROUTE:
                case BIKING_ROUTE:
                    holder.name.setText("路线" + (position + 1));
                    int time = routeLines.get(position).getDuration();
                    if ( time / 3600 == 0 ) {
                        holder.lightNum.setText( "大约需要：" + time / 60 + "分钟" );
                    } else {
                        holder.lightNum.setText( "大约需要：" + time / 3600 + "小时" + (time % 3600) / 60 + "分钟" );
                    }
                    holder.dis.setText("距离大约是：" + routeLines.get(position).getDistance() + "米");
                    break;

                case DRIVING_ROUTE:
                    DrivingRouteLine drivingRouteLine = (DrivingRouteLine) routeLines.get(position);
                    holder.name.setText( "线路" + (position + 1));
                    holder.lightNum.setText( "红绿灯数：" + drivingRouteLine.getLightNum());
                    holder.dis.setText("拥堵距离为：" + drivingRouteLine.getCongestionDistance() + "米");
                    break;
                case MASS_TRANSIT_ROUTE:
                    MassTransitRouteLine massTransitRouteLine = (MassTransitRouteLine) routeLines.get(position);
                    holder.name.setText("线路" + (position + 1));
                    holder.lightNum.setText( "预计达到时间：" + massTransitRouteLine.getArriveTime() );
                    holder.dis.setText("总票价：" + massTransitRouteLine.getPrice() + "元");
                    break;

                default:
                    break;

            }

            return convertView;
        }

        private class NodeViewHolder {

            private TextView name;
            private TextView lightNum;
            private TextView dis;
        }

        public enum Type {
            MASS_TRANSIT_ROUTE, // 综合交通
            TRANSIT_ROUTE, // 公交
            DRIVING_ROUTE, // 驾车
            WALKING_ROUTE, // 步行
            BIKING_ROUTE // 骑行

        }
    }
	
}
