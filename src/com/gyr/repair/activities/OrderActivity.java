package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.CancelOrderService;
import com.gyr.repair.http.DeleteOrderService;
import com.gyr.repair.http.TakeOrderService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OrderActivity extends Activity implements OnClickListener {

	private TextView ordernametv, ordercitytv, orderdistricttv, ordercontacttv,
			ordermobiletv, orderbudgettv, orderaddresstv, orderdetailtv,
			orderstatustv, orderengineernametv, orderengineermobiletv;
	private Button takeorderbt, completeorderbt, cancelorderbt, deleteorderbt, orderroutebt, sendagainorderbt;
	
	private ImageView ordermobileiv, orderengineermobileiv;

	private String idorder;
	private String nameuser, mobileuser;
	private String nameengineer, mobileengineer;
	
	private String phone;
	
	private LocationClient mLocationClient;
	private BDLocationListener mBDLocationListener;
	
	private static String city, address;
	private static double fromlatitude, fromlongitude, tolatitude, tolongitude;

	private Handler takeorderHandler, cancelorderHandler, deleteorderHandler, routeorderHandler;
	private Timer timer;
	private ProgressDialog progressDialog;

	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		int cursor = bundle.getInt("cursor");
		int flag = bundle.getInt("flag");     //flag=1 OrderList; flag=2 SendedOrders; flag=3 receivedorders

		ordernametv = (TextView) findViewById(R.id.tv_ordername);
		ordercitytv = (TextView) findViewById(R.id.tv_ordercity);
		orderdistricttv = (TextView) findViewById(R.id.tv_orderdistrict);
		ordercontacttv = (TextView) findViewById(R.id.tv_ordercontact2);
		ordermobiletv = (TextView) findViewById(R.id.tv_ordermobile2);
		orderbudgettv = (TextView) findViewById(R.id.tv_orderbudget2);
		orderaddresstv = (TextView) findViewById(R.id.tv_orderaddress2);
		orderdetailtv = (TextView) findViewById(R.id.tv_orderdetail2);
		orderstatustv = (TextView) findViewById(R.id.tv_orderstatus2);
		orderengineernametv = (TextView) findViewById(R.id.tv_orderengineername2);
		orderengineermobiletv = (TextView) findViewById(R.id.tv_orderengineermobile2);
		
		ordermobileiv = (ImageView) findViewById(R.id.iv_ordermobile2);
		orderengineermobileiv = (ImageView) findViewById(R.id.iv_orderengineermobile2);

		takeorderbt = (Button) findViewById(R.id.bt_takeorder);
		takeorderbt.setOnClickListener(this);
		completeorderbt = (Button) findViewById(R.id.bt_ordercomplete);
		completeorderbt.setOnClickListener(this);
		cancelorderbt = (Button) findViewById(R.id.bt_ordercancel);
		cancelorderbt.setOnClickListener(this);
		deleteorderbt = (Button) findViewById(R.id.bt_deleteorder);
		deleteorderbt.setOnClickListener(this);
		orderroutebt = (Button) findViewById(R.id.bt_orderroute);
		orderroutebt.setOnClickListener(this);
		sendagainorderbt = (Button) findViewById(R.id.bt_sendagainorder);
		sendagainorderbt.setOnClickListener(this);
		
		takeorderbt.setVisibility(View.GONE);
		completeorderbt.setVisibility(View.GONE);
		cancelorderbt.setVisibility(View.GONE);
		deleteorderbt.setVisibility(View.GONE);
		orderroutebt.setVisibility(View.GONE);
		sendagainorderbt.setVisibility(View.GONE);
		ordermobileiv.setVisibility(View.GONE);
		orderengineermobileiv.setVisibility(View.GONE);
		
		if (flag == 1) {
			takeorderbt.setVisibility(View.VISIBLE);
			//completeorderbt.setVisibility(View.GONE);
			//cancelorderbt.setVisibility(View.GONE);
			//deleteorderbt.setVisibility(View.GONE);
			//orderroutebt.setVisibility(View.GONE);
			try {
				JSONArray jsonArray = new JSONArray(
						ResponseCache.responseOrderlist);
				JSONObject jsonObject = jsonArray.getJSONObject(cursor);
				idorder = jsonObject.getString("idorder");
				ordernametv.setText(jsonObject.getString("title"));
				ordercitytv.setText(jsonObject.getString("city"));
				orderdistricttv.setText(jsonObject.getString("district"));
				nameuser = jsonObject.getString("nameuser");
				ordercontacttv.setText(nameuser);
				mobileuser = jsonObject.getString("mobileuser");
				ordermobiletv.setText(mobileuser);
				orderbudgettv.setText(jsonObject.getString("budget") + "元");
				orderaddresstv.setText(jsonObject.getString("address"));
				orderdetailtv.setText(jsonObject.getString("detail"));
				city = jsonObject.getString("city");
				address = jsonObject.getString("district") + jsonObject.getString("address");
				tolatitude = jsonObject.getDouble("latitude");
				tolongitude = jsonObject.getDouble("longitude");
				if (jsonObject.getString("status").equals("unreceived")) {
					orderstatustv.setText("无人接单");
					orderengineernametv.setText("无");
					orderengineermobiletv.setText("无");
				} else if (jsonObject.getString("status").equals("received")) {
					orderstatustv.setText("已接单");
					nameengineer = jsonObject.getString("nameengineer");
					orderengineernametv.setText(nameengineer);
					mobileengineer = jsonObject.getString("mobileengineer");
					orderengineermobiletv.setText(mobileengineer);
				} else if (jsonObject.getString("status").equals("completed")) {
					orderstatustv.setText("已完成");
					nameengineer = jsonObject.getString("nameengineer");
					orderengineernametv.setText(nameengineer);
					mobileengineer = jsonObject.getString("mobileengineer");
					orderengineermobiletv.setText(mobileengineer);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (flag == 2) {     //发过的活儿
			//takeorderbt.setVisibility(View.GONE);
			//orderroutebt.setVisibility(View.GONE);
			try {
				JSONArray jsonArray = new JSONArray(
						ResponseCache.responseMySendedOrders);
				JSONObject jsonObject = jsonArray.getJSONObject(cursor);
				idorder = jsonObject.getString("idorder");
				ordernametv.setText(jsonObject.getString("title"));
				ordercitytv.setText(jsonObject.getString("city"));
				orderdistricttv.setText(jsonObject.getString("district"));
				nameuser = jsonObject.getString("nameuser");
				ordercontacttv.setText(nameuser);
				mobileuser = jsonObject.getString("mobileuser");
				ordermobiletv.setText(mobileuser);
				orderbudgettv.setText(jsonObject.getString("budget") + "元");
				orderaddresstv.setText(jsonObject.getString("address"));
				orderdetailtv.setText(jsonObject.getString("detail"));
				city = jsonObject.getString("city");
				address = jsonObject.getString("district") + jsonObject.getString("address");
				tolatitude = jsonObject.getDouble("latitude");
				tolongitude = jsonObject.getDouble("longitude");
				if (jsonObject.getString("status").equals("unreceived")) {
					//cancelorderbt.setVisibility(View.GONE);
					//completeorderbt.setVisibility(View.GONE);
					deleteorderbt.setVisibility(View.VISIBLE);
					orderstatustv.setText("无人接单");
					orderengineernametv.setText("无");
					orderengineermobiletv.setText("无");
				} else if (jsonObject.getString("status").equals("received")) {
					cancelorderbt.setVisibility(View.VISIBLE);
					completeorderbt.setVisibility(View.VISIBLE);
					orderengineermobileiv.setVisibility(View.VISIBLE);
					//deleteorderbt.setVisibility(View.GONE);
					orderstatustv.setText("已接单");
					nameengineer = jsonObject.getString("nameengineer");
					orderengineernametv.setText(nameengineer);
					mobileengineer = jsonObject.getString("mobileengineer");
					orderengineermobiletv.setText(mobileengineer);
					phone = orderengineermobiletv.getText().toString().trim();
					orderengineermobiletv.setTextColor(getResources().getColor(R.color.blue_light));
					orderengineermobiletv.setOnClickListener(this);
					orderengineermobileiv.setOnClickListener(this);
					/*
					orderengineermobiletv.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String phone = orderengineermobiletv.getText().toString().trim();
							if (phone != null && !phone.equals("")) {
								Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+phone));
							    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							    startActivity(intent);
							}
							
						}
					});
					*/
				} else if (jsonObject.getString("status").equals("completed")) {
					//cancelorderbt.setVisibility(View.GONE);
					//completeorderbt.setVisibility(View.GONE);
					//deleteorderbt.setVisibility(View.GONE);
					orderstatustv.setText("已完成");
					nameengineer = jsonObject.getString("nameengineer");
					orderengineernametv.setText(nameengineer);
					mobileengineer = jsonObject.getString("mobileengineer");
					orderengineermobiletv.setText(mobileengineer);
				} else if (jsonObject.getString("status").equals("expired")) {
					sendagainorderbt.setVisibility(View.VISIBLE);
					orderstatustv.setText("已过期");
					orderstatustv.setTextColor(getResources().getColor(R.color.red));
					orderengineernametv.setText("无");
					orderengineermobiletv.setText("无");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (flag == 3) {     //接到的活儿
			//takeorderbt.setVisibility(View.GONE);
			//completeorderbt.setVisibility(View.GONE);
			//deleteorderbt.setVisibility(View.GONE);
			try {
				JSONArray jsonArray = new JSONArray(
						ResponseCache.responseMyReceivedOrders);
				JSONObject jsonObject = jsonArray.getJSONObject(cursor);
				idorder = jsonObject.getString("idorder");
				ordernametv.setText(jsonObject.getString("title"));
				ordercitytv.setText(jsonObject.getString("city"));
				orderdistricttv.setText(jsonObject.getString("district"));
				nameuser = jsonObject.getString("nameuser");
				ordercontacttv.setText(nameuser);
				mobileuser = jsonObject.getString("mobileuser");
				ordermobiletv.setText(mobileuser);
				orderbudgettv.setText(jsonObject.getString("budget") + "元");
				orderaddresstv.setText(jsonObject.getString("address"));
				orderdetailtv.setText(jsonObject.getString("detail"));
				nameengineer = jsonObject.getString("nameengineer");
				orderengineernametv.setText(nameengineer);
				mobileengineer = jsonObject.getString("mobileengineer");
				orderengineermobiletv.setText(mobileengineer);
				city = jsonObject.getString("city");
				address = jsonObject.getString("district") + jsonObject.getString("address");
				tolatitude = jsonObject.getDouble("latitude");
				tolongitude = jsonObject.getDouble("longitude");
				if (jsonObject.getString("status").equals("received")) {
					orderroutebt.setVisibility(View.VISIBLE);
					cancelorderbt.setVisibility(View.VISIBLE);
					ordermobileiv.setVisibility(View.VISIBLE);
					orderstatustv.setText("已接单");
					phone = ordermobiletv.getText().toString().trim();
					ordermobiletv.setTextColor(getResources().getColor(R.color.blue_light));
					ordermobiletv.setOnClickListener(this);
					ordermobileiv.setOnClickListener(this);
					/*
					ordermobiletv.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String phone = ordermobiletv.getText().toString().trim();
							if (phone != null && !phone.equals("")) {
								Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+phone));
							    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							    startActivity(intent);
							}
							
						}
					});
					*/
				} else if (jsonObject.getString("status").equals("completed")) {
					//orderroutebt.setVisibility(View.GONE);
					//cancelorderbt.setVisibility(View.GONE);
					orderstatustv.setText("已完成");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		takeorderHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "接活儿成功！",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(OrderActivity.this,
							MainActivity.class);
					ResponseCache.cacheFlag = true;
					startActivity(intent);
					break;
				case FAILED:
					Thread.interrupted();
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "接活儿失败",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}

		};
		
		cancelorderHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "取消成功！",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(OrderActivity.this,
							MainActivity.class);
					ResponseCache.cacheFlag = true;
					startActivity(intent);
					break;
				case FAILED:
					Thread.interrupted();
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "取消失败",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
		};
		
		deleteorderHandler = new Handler () {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "删除成功！",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(OrderActivity.this,
							MainActivity.class);
					ResponseCache.cacheFlag = true;
					startActivity(intent);
					break;
				case FAILED:
					Thread.interrupted();
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "删除失败",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
		};
		
		routeorderHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
					timer.cancel();
					progressDialog.dismiss();
					Intent intent = new Intent(OrderActivity.this, RouteActivity.class);
					intent.putExtra("fromlatitude", fromlatitude);
					intent.putExtra("fromlongitude", fromlongitude);
					intent.putExtra("tolatitude", tolatitude);
					intent.putExtra("tolongitude", tolongitude);
					intent.putExtra("city", city);
					intent.putExtra("address", address);
					startActivity(intent);
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

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_takeorder:
			if (UserCache.isEngineer()
					&& !mobileuser.equals(UserCache.getMobile())) {
				clickTakeOrder();
			} else if (UserCache.isEngineer()
					&& mobileuser.equals(UserCache.getMobile())) {
				Toast.makeText(getApplicationContext(), "不可接自己发出的维修单",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "请申请成为维修工",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.bt_ordercomplete:
			clickOrderComplete();
			break;
		case R.id.bt_ordercancel:
			new AlertDialog.Builder(this).setTitle("确认取消？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					clickOrderCancel();
					
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).show();
			//clickOrderCancel();
			break;
		case R.id.bt_deleteorder:
			new AlertDialog.Builder(this).setTitle("确认删除？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					clickOrderDelete();
					
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).show();
			break;
		case R.id.bt_orderroute:
			clickOrderRoute();
			break;
		case R.id.bt_sendagainorder:
			clickSendAgainOrder();
			break;
		case R.id.tv_ordermobile2:
		case R.id.tv_orderengineermobile2:
		case R.id.iv_ordermobile2:
		case R.id.iv_orderengineermobile2:
			clickCall();
			break;
		}
		
	}

	private void clickSendAgainOrder() {
		Intent intent = new Intent(OrderActivity.this, SendOrderActivity.class);
		intent.putExtra("flag", 3);
		intent.putExtra("sendtitle", ordernametv.getText().toString().trim());
		intent.putExtra("sendbudget", orderbudgettv.getText().toString().trim());
		intent.putExtra("senddetail", orderdetailtv.getText().toString().trim());
		intent.putExtra("sendcity", ordercitytv.getText().toString().trim());
		intent.putExtra("senddistrict", orderdistricttv.getText().toString().trim());
		intent.putExtra("sendaddress", orderaddresstv.getText().toString().trim());
		startActivity(intent);
		
	}

	private void clickCall() {
		if (phone != null && !phone.equals("")) {
			Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+phone));
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    startActivity(intent);
		}
		
	}

	private void clickOrderDelete() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, "请稍候", "正在删除...", true);
			DeleteOrderPostThread deleteOrderPostThread = new DeleteOrderPostThread(idorder);
			deleteOrderPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMsg(FAILED, deleteorderHandler);
				}

			}, 10000);
		} else {
			Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT)
			.show();
		}
		
	}

	private void clickOrderRoute() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, null, null, true);

			mLocationClient = new LocationClient(getApplicationContext());
			mBDLocationListener = new MyBDLocationListener();
			mLocationClient.registerLocationListener(mBDLocationListener);

			LocationClientOption option = new LocationClientOption();
			option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式 高精度
			option.setCoorType("bd09ll");// 设置返回定位结果是百度经纬度 默认gcj02
			option.setIsNeedAddress(true);// 设置定位结果包含地址信息
			option.setNeedDeviceDirect(true);// 设置定位结果包含手机机头 的方向
			// 设置定位参数
			mLocationClient.setLocOption(option);
			// 启动定位
			mLocationClient.start();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMsg(FAILED, routeorderHandler);
				}

			}, 10000);
			// Intent intent = new Intent(OrderActivity.this,
			// RouteActivity.class);
			// startActivity(intent);
		} else {
			Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT)
			.show();
		}
		
	}
	
	private class MyBDLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 非空判断
			if (location != null) {
				// 根据BDLocation 对象获得经纬度以及详细地址信息
				fromlatitude = location.getLatitude();
				fromlongitude = location.getLongitude();
				//String address = location.getAddrStr();
				Log.i("guan", /*"address:" + address +*/ " fromlatitude:" + fromlatitude
						+ " fromlongitude:" + fromlongitude);
				if (mLocationClient.isStarted()) {
					// 获得位置之后停止定位
					mLocationClient.stop();
					sendMsg(SUCCEEDED, routeorderHandler);
					/*
					Intent intent = new Intent(OrderActivity.this, RouteActivity.class);
					intent.putExtra("fromlatitude", fromlatitude);
					intent.putExtra("fromlongitude", fromlongitude);
					intent.putExtra("tolatitude", tolatitude);
					intent.putExtra("tolongitude", tolongitude);
					intent.putExtra("city", city);
					intent.putExtra("address", address);
					startActivity(intent);
					*/
				}
			}
		}
	}

	private void clickTakeOrder() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, "请稍候", "正在接活儿...", true);
			TakeOrderPostThread takeOrderPostThread = new TakeOrderPostThread(
					idorder, UserCache.getName(), UserCache.getMobile());
			takeOrderPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMsg(FAILED, takeorderHandler);
				}

			}, 10000);
		} else {
			Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void clickOrderComplete() {
		Intent intent = new Intent(OrderActivity.this, RateEngineerActivity.class);
		intent.putExtra("idorder", idorder);
		intent.putExtra("nameengineer", nameengineer);
		intent.putExtra("mobileengineer", mobileengineer);
		startActivity(intent);
		
	}

	private void clickOrderCancel() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, "请稍候", "正在取消...", true);
			CancelOrderPostThread cancelOrderPostThread = new CancelOrderPostThread(idorder);
			cancelOrderPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMsg(FAILED, cancelorderHandler);
				}

			}, 10000);
		} else {
			Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT)
			.show();
		}
		
	}

	private class TakeOrderPostThread extends Thread {
		private String idorder, nameengineer, mobileengineer;

		public TakeOrderPostThread(String idorder, String nameengineer,
				String mobileengineer) {
			super();
			this.idorder = idorder;
			this.nameengineer = nameengineer;
			this.mobileengineer = mobileengineer;
		}

		@Override
		public void run() {
			String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("idorder", idorder));
			params.add(new BasicNameValuePair("nameengineer", nameengineer));
			params.add(new BasicNameValuePair("mobileengineer", mobileengineer));
			responseMsg = TakeOrderService.send(params);
			Log.i("tag", "TakeOrderActivity: responseMsg = " + responseMsg);
			if (responseMsg.equals("FAILED")) {
				sendMsg(FAILED, takeorderHandler);
			} else {
				sendMsg(SUCCEEDED, takeorderHandler);
			}
		}

	}
	
	private class CancelOrderPostThread extends Thread {
		private String idorder;

		public CancelOrderPostThread(String idorder) {
			super();
			this.idorder = idorder;
		}

		@Override
		public void run() {
			String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("idorder", idorder));
			responseMsg = CancelOrderService.send(params);
			Log.i("tag", "CancelOrderActivity: responseMsg = " + responseMsg);
			if (responseMsg.equals("FAILED")) {
				sendMsg(FAILED, cancelorderHandler);
			} else {
				sendMsg(SUCCEEDED, cancelorderHandler);
			}
		}
		
	}
	
	private class DeleteOrderPostThread extends Thread {
		private String idorder;

		public DeleteOrderPostThread(String idorder) {
			super();
			this.idorder = idorder;
		}

		@Override
		public void run() {
			String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("idorder", idorder));
			responseMsg = DeleteOrderService.send(params);
			Log.i("tag", "DeleteOrderActivity: responseMsg = " + responseMsg);
			if (responseMsg.equals("FAILED")) {
				sendMsg(FAILED, deleteorderHandler);
			} else {
				sendMsg(SUCCEEDED, deleteorderHandler);
			}
		}
		
	}

	private boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
		}
		return false;
	}

	private void sendMsg(int i, Handler handler) {
		Message msg = new Message();
		msg.what = i;
		handler.sendMessage(msg);
	}

}
