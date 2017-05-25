package com.gyr.repair.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.SharedPreferencesUtils;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.ExpireOrderService;
import com.gyr.repair.http.GetEngineerlistPostService;
import com.gyr.repair.http.GetOrderlistPostService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class WelcomeActivity extends Activity /*implements OnClickListener*/ {
	//private Button welcomebt;
	
	private Handler welcomeHandler;
	//private Timer timer;
	//private ProgressDialog progressDialog;
	
	public static final String EXIST = "exist";
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		ResponseCache.cacheFlag = false;
		
		if (Integer.parseInt(SharedPreferencesUtils.getParam(this, "type", 0).toString()) == 1) {
			UserCache.setUser();
			UserCache.setId(SharedPreferencesUtils.getParam(this, "id", "").toString());
			UserCache.setMobile(SharedPreferencesUtils.getParam(this, "mobile", "").toString());
			UserCache.setPassword(SharedPreferencesUtils.getParam(this, "password", "").toString());
			UserCache.setName(SharedPreferencesUtils.getParam(this, "name", "").toString());
			ResponseCache.responseMySendedOrders = SharedPreferencesUtils.getParam(this, "mysendedorders", "").toString();
		} else if (Integer.parseInt(SharedPreferencesUtils.getParam(this, "type", 0).toString()) == 2) {
			UserCache.setEngineer();
			UserCache.setId(SharedPreferencesUtils.getParam(this, "id", "").toString());
			UserCache.setMobile(SharedPreferencesUtils.getParam(this, "mobile", "").toString());
			UserCache.setPassword(SharedPreferencesUtils.getParam(this, "password", "").toString());
			UserCache.setName(SharedPreferencesUtils.getParam(this, "name", "").toString());
			UserCache.setExpert(SharedPreferencesUtils.getParam(this, "expert", "").toString());
			UserCache.setCity(SharedPreferencesUtils.getParam(this, "city", "").toString());
			UserCache.setDistrict(SharedPreferencesUtils.getParam(this, "district", "").toString());
			UserCache.setAddress(SharedPreferencesUtils.getParam(this, "address", "").toString());
			UserCache.setScore(SharedPreferencesUtils.getParam(this, "score", "").toString());
			UserCache.setOrdernumber(SharedPreferencesUtils.getParam(this, "ordernumber", "").toString());
			ResponseCache.responseMySendedOrders = SharedPreferencesUtils.getParam(this, "mysendedorders", "").toString();
			ResponseCache.responseMyReceivedOrders = SharedPreferencesUtils.getParam(this, "myreceivedorders", "").toString();
		} else {
			UserCache.setGuest();
		}
		
		/*
		welcomebt = (Button)findViewById(R.id.bt_welcome);
		welcomebt.setOnClickListener(this);
		*/
		
		welcomeHandler = new Handler() {
			
			@Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
    				//timer.cancel();
    				//progressDialog.dismiss();
    				Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            		startActivity(intent);
            		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            		//WelcomeActivity.this.finish();
    				break;
                case FAILED:
                	Thread.interrupted();
                	//timer.cancel();
                	//progressDialog.dismiss();
                	Toast.makeText(WelcomeActivity.this, "载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                	WelcomeActivity.this.finish();
    				break;
                }
			}
			
		};
		welcome();
		
		
		/*
		welcomeHandler = new Handler();
		welcomeHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
				startActivity(intent);
				WelcomeActivity.this.finish();
				}
			}, 1000);
		*/
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		welcome();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent != null) {//判断其他Activity启动本Activity时传递来的intent是否为空
	        //获取intent中对应Tag的布尔值
	        boolean isExist = intent.getBooleanExtra(EXIST, false);
	        //如果为真则退出本Activity
	        if (isExist) {
	            this.finish();
	        }
	    }

	}
	
	/*
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_welcome:
			clickWelcome();
			break;
		}
		
	}
	*/	
	
	private void welcome() {
		//progressDialog = ProgressDialog.show(this, null, null, true);
		WelcomePostThread welcomePostThread = new WelcomePostThread("unreceived");
		welcomePostThread.start();
		/*	
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendMsg(FAILED, welcomeHandler);
			}

		}, 10000);
		*/
		
	}
	
	private class WelcomePostThread extends Thread {
		public String status;

		public WelcomePostThread(String status) {
			super();
			this.status = status;
		}
		
		@Override
		public void run() {
			Date now = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String date = df.format(now);
			Log.i("guan", "WelcomeActivity: " + date);
			if (!status.equals("")) {
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				List<NameValuePair> params2 = new ArrayList<NameValuePair>();
				List<NameValuePair> params3 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("status", status));
                params2.add(new BasicNameValuePair("default", ""));
                params3.add(new BasicNameValuePair("date", date));
                String expireresult = ExpireOrderService.send(params3);
                ResponseCache.responseOrderlist = GetOrderlistPostService.send(params1);
                ResponseCache.responseEngineerlist = GetEngineerlistPostService.send(params2);
                Log.i("guan", "WelcomeActivity: responseMsg = " + ResponseCache.responseOrderlist);
                Log.i("guan", "WelcomeActivity: responseMsg = " + ResponseCache.responseEngineerlist);
                if (expireresult.equals("FAILED") || ResponseCache.responseOrderlist.equals("FAILED") || ResponseCache.responseEngineerlist.equals("FAILED")) {
                	sendMsg(FAILED, welcomeHandler);
                } else {
                	sendMsg(SUCCEEDED, welcomeHandler);
                }
			}
		}
	}
    
	private void sendMsg(int i, Handler handler){
		Message msg = new Message();
		msg.what = i;
		handler.sendMessage(msg);
	}
	
}
