package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.baidu.mapapi.SDKInitializer;
import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.SharedPreferencesUtils;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.fagments.HomeFragment;
import com.gyr.repair.fagments.MyFragment;
import com.gyr.repair.fagments.NearbyFragment;
import com.gyr.repair.http.GetEngineerlistPostService;
import com.gyr.repair.http.GetMyReceivedOrdersService;
import com.gyr.repair.http.GetMySendedOrdersService;
import com.gyr.repair.http.GetOrderlistPostService;
import com.gyr.repair.polling.PollingService;
import com.gyr.repair.polling.PollingUtils;
import com.jauker.widget.BadgeView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener{
	
	private Fragment homefragment, nearbyfragment, myfragment, currentfragment;
	private RelativeLayout homelayout, nearbylayout, mylayout;
	private TextView hometv, nearbytv, mytv;
	private ImageView homeiv, nearbyiv, myiv;
	
	private long exitTime = 0;

	private Handler refreshHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;
	
	public static BadgeView badge;
	
	private mainactivityBroadcastReceiver receiver;
	
	private static boolean serviceflag = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getActionBar().setDisplayHomeAsUpEnabled(false);
        
        SDKInitializer.initialize(getApplicationContext());

        receiver = new mainactivityBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.gyr.broadcast.badge");
		this.registerReceiver(receiver, filter);
        
        refreshHandler = new Handler() {
			
			@Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(MainActivity.this, "载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
                }
			}
			
		};
		refreshResponseCache();
        
        homelayout = (RelativeLayout) findViewById(R.id.rl_tab_home);
		nearbylayout = (RelativeLayout) findViewById(R.id.rl_tab_nearby);
		mylayout = (RelativeLayout) findViewById(R.id.rl_tab_my);
		
		homelayout.setOnClickListener(this);
		nearbylayout.setOnClickListener(this);
		mylayout.setOnClickListener(this);

		hometv = (TextView) findViewById(R.id.tv_home);
		TextPaint hometp = hometv.getPaint();
		hometp.setFakeBoldText(true);
		nearbytv = (TextView) findViewById(R.id.tv_nearby);
		TextPaint nearbytp = nearbytv.getPaint();
		nearbytp.setFakeBoldText(true);
		mytv = (TextView) findViewById(R.id.tv_my);
		TextPaint mytp = mytv.getPaint();
		mytp.setFakeBoldText(true);
		
		homeiv = (ImageView) findViewById(R.id.iv_home);
		nearbyiv = (ImageView) findViewById(R.id.iv_nearby);
		myiv = (ImageView) findViewById(R.id.iv_my);
		
		badge = new BadgeView(this);
		
		initTab();

        if (!UserCache.isGuest() && serviceflag) {
        	PollingUtils.startPollingService(this, 5, PollingService.class, PollingService.ACTION);
        	serviceflag = false;
        }
		
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PollingUtils.stopPollingService(this, PollingService.class, PollingService.ACTION);
	}

	private void refreshResponseCache() {		
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, null, null, true);
			RefreshPostThread refreshPostThread = new RefreshPostThread("unreceived");
			refreshPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, refreshHandler);
				}
				
			}, 10000);
		} else {
    		Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
		
	}
	
	private class RefreshPostThread extends Thread {
		public String status;

		public RefreshPostThread(String status) {
			super();
			this.status = status;
		}

		@Override
		public void run() {
			if (!status.equals("")) {
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				List<NameValuePair> params2 = new ArrayList<NameValuePair>();
				List<NameValuePair> params3 = new ArrayList<NameValuePair>();
				List<NameValuePair> params4 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("status", status));
                params2.add(new BasicNameValuePair("default", ""));
                params3.add(new BasicNameValuePair("mobileuser", UserCache.getMobile()));
                params4.add(new BasicNameValuePair("mobileengineer", UserCache.getMobile()));
                ResponseCache.responseOrderlist = GetOrderlistPostService.send(params1);
                Log.i("guan", "MainActivity: responseOrderlist = " + ResponseCache.responseOrderlist);
                ResponseCache.responseEngineerlist = GetEngineerlistPostService.send(params2);
                Log.i("guan", "MainActivity: responseEngineerlist = " + ResponseCache.responseEngineerlist);
                if (ResponseCache.cacheFlag == true) {
                	ResponseCache.cacheFlag = false;
                	if (!UserCache.isGuest()) {
                    	ResponseCache.responseMySendedOrders = GetMySendedOrdersService.send(params3);
                    	SharedPreferencesUtils.setParam(getApplicationContext(), "mysendedorders", ResponseCache.responseMySendedOrders);
                    	Log.i("guan", "MainActivity: responseMySendedOrders = " + ResponseCache.responseMySendedOrders);
                    	if (UserCache.isEngineer()) {
                    		ResponseCache.responseMyReceivedOrders = GetMyReceivedOrdersService.send(params4);
                    		SharedPreferencesUtils.setParam(getApplicationContext(), "myreceivedorders", ResponseCache.responseMyReceivedOrders);
                    		Log.i("guan", "MainActivity: responseMyReceivedOrders = " + ResponseCache.responseMyReceivedOrders);
                    	}
                    }
                }
				if (ResponseCache.responseOrderlist.equals("FAILED")
						|| ResponseCache.responseEngineerlist.equals("FAILED")
						|| ResponseCache.responseMySendedOrders
								.equals("FAILED")
						|| ResponseCache.responseMyReceivedOrders
								.equals("FAILED")) {
                	sendMsg(FAILED, refreshHandler);
                } else {
                	sendMsg(SUCCEEDED, refreshHandler);
                }
			}
		}
		
	}
	
    private boolean isConnectingToInternet() {
    	ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
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

	private class mainactivityBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.i("broadcastguan", "receive");
			Bundle bundle=intent.getExtras();
			int badgeflag = bundle.getInt("badgeflag");
			if (badgeflag == 1 || badgeflag == 2) {
				//Log.i("broadcastguan", String.valueOf(badgeflag));
				badge.setBadgeCount(1);
				badge.setTargetView(mylayout);
				
			}
			
		}
	}
    
	@SuppressWarnings("deprecation")
	private void initTab() {
		if (homefragment == null) {
			homefragment = new HomeFragment();
		}
		if (myfragment == null) {
			myfragment = new MyFragment();
		}

		if (!homefragment.isAdded() && !myfragment.isAdded()) {
			// 提交事务
			getFragmentManager().beginTransaction().add(R.id.content_layout, myfragment).commit();
			getFragmentManager().beginTransaction().hide(myfragment).commit();
			getFragmentManager().beginTransaction().add(R.id.content_layout, homefragment).commit();

			// 记录当前Fragment
			currentfragment = homefragment;
			// 设置文本的变化
			hometv.setTextColor(getResources().getColor(R.color.blue_light));
			homeiv.setColorFilter(getResources().getColor(R.color.blue_light));
			nearbytv.setTextColor(getResources().getColor(R.color.grey));
			nearbyiv.setColorFilter(getResources().getColor(R.color.grey));
			mytv.setTextColor(getResources().getColor(R.color.grey));
			myiv.setColorFilter(getResources().getColor(R.color.grey));

		}
		
		if (!myfragment.isAdded()) {
			
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_tab_home:
			clickTab1Layout();
			break;
		case R.id.rl_tab_nearby:
			clickTab2Layout();
			break;
		case R.id.rl_tab_my:
			badge.setBadgeCount(0);
			clickTab3Layout();
			break;
		default:
			break;
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private void clickTab1Layout() {
		if (homefragment == null) {
			homefragment = new HomeFragment();
		}
		addOrShowFragment(getFragmentManager().beginTransaction(), homefragment);
		
		hometv.setTextColor(getResources().getColor(R.color.blue_light));
		homeiv.setColorFilter(getResources().getColor(R.color.blue_light));
		nearbytv.setTextColor(getResources().getColor(R.color.grey));
		nearbyiv.setColorFilter(getResources().getColor(R.color.grey));
		mytv.setTextColor(getResources().getColor(R.color.grey));
		myiv.setColorFilter(getResources().getColor(R.color.grey));
		
	}
	
	@SuppressWarnings("deprecation")
	private void clickTab2Layout() {
		if (nearbyfragment == null) {
			nearbyfragment = new NearbyFragment();
		}
		addOrShowFragment(getFragmentManager().beginTransaction(), nearbyfragment);
		
		hometv.setTextColor(getResources().getColor(R.color.grey));
		homeiv.setColorFilter(getResources().getColor(R.color.grey));
		nearbytv.setTextColor(getResources().getColor(R.color.blue_light));
		nearbyiv.setColorFilter(getResources().getColor(R.color.blue_light));
		mytv.setTextColor(getResources().getColor(R.color.grey));
		myiv.setColorFilter(getResources().getColor(R.color.grey));
		
	}
	
	@SuppressWarnings("deprecation")
	private void clickTab3Layout() {
		if (myfragment == null) {
			myfragment = new MyFragment();
		}
		addOrShowFragment(getFragmentManager().beginTransaction(), myfragment);
		
		hometv.setTextColor(getResources().getColor(R.color.grey));
		homeiv.setColorFilter(getResources().getColor(R.color.grey));
		nearbytv.setTextColor(getResources().getColor(R.color.grey));
		nearbyiv.setColorFilter(getResources().getColor(R.color.grey));
		mytv.setTextColor(getResources().getColor(R.color.blue_light));
		myiv.setColorFilter(getResources().getColor(R.color.blue_light));
		
	}
	
	private void addOrShowFragment(FragmentTransaction transaction,
			Fragment fragment) {
		if (currentfragment == fragment)
			return;

		if (!fragment.isAdded()) { // 如果当前fragment未被添加，则添加到Fragment管理器中
			transaction.hide(currentfragment)
					.add(R.id.content_layout, fragment).commit();
		} else {
			transaction.hide(currentfragment).show(fragment).commit();
		}

		currentfragment = fragment;
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   
	        } else {
	        	
	        	Intent intent = new Intent(this, WelcomeActivity.class);
	        	intent.putExtra(WelcomeActivity.EXIST, true);
	        	startActivity(intent);
	        	/*
	            finish();
	            System.exit(0);
	            */
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public Context getContext() {
		return this;
	}
	
}
