package com.gyr.repair.activities;

import java.util.ArrayList;
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
import com.gyr.repair.fagments.MyFragment;
import com.gyr.repair.fagments.MyReceivedOrdersAllFragment;
import com.gyr.repair.fagments.MyReceivedOrdersCompletedFragment;
import com.gyr.repair.fagments.MyReceivedOrdersUndergoningFragment;
import com.gyr.repair.http.GetMyReceivedOrdersService;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MyReceivedOrdersActivity extends FragmentActivity {
	
	private TabFragmentPagerAdapter mAdapter;
	private ViewPager myreceivedcontentvp;
	private RadioGroup myreceivedordersrg;
	
	private Handler myreceivedordersHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myreceivedorders);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		myreceivedordersrg = (RadioGroup) findViewById(R.id.rg_myreceivedorders);
		myreceivedcontentvp = (ViewPager) findViewById(R.id.vp_myreceivedcontent);
		
		MainActivity.badge.setBadgeCount(0);
		MyFragment.badgemy.setBadgeCount(0);
		
		myreceivedordersHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
    				myreceivedcontentvp.setAdapter(mAdapter);
    				setListener();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(MyReceivedOrdersActivity.this, "维修单列表载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
				}
			}
			
		};
		
		getMyReceivedOrders();
		
		/*
		mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
		myreceivedcontentvp.setAdapter(mAdapter);
		
		setListener();
		*/
		
	}
	
	public void setListener() {
		
		myreceivedcontentvp.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageSelected(int position) {
				
				if (myreceivedordersrg != null && myreceivedordersrg.getChildCount()>position) {
					((RadioButton)myreceivedordersrg.getChildAt(position)).performClick();
				}
				
			}
			
		});
		
		myreceivedordersrg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				//Log.i(tag, String.valueOf(R.id.rb_all));
				int mark = R.id.rb_myreceivedall;
				
				if (myreceivedordersrg.getChildAt(group.getCheckedRadioButtonId() - mark) != null) {
					myreceivedcontentvp.setCurrentItem(group.getCheckedRadioButtonId() - mark);
					
				}
				
			}
			
		});
	}

	private static class TabFragmentPagerAdapter extends FragmentPagerAdapter {

		public TabFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment ft = null;
			switch (arg0) {
			case 0:
				ft = new MyReceivedOrdersAllFragment();
				break;
			case 1:
				ft = new MyReceivedOrdersUndergoningFragment();
				break;
			case 2:
				ft = new MyReceivedOrdersCompletedFragment();
				break;
			}
			return ft;
		}

		@Override
		public int getCount() {
			return 3;
		}
		
	}
	
	private void getMyReceivedOrders() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(MyReceivedOrdersActivity.this, null, null, true);
			GetMyReceivedOrdersPostThread getMyReceivedOrdersPostThread = new GetMyReceivedOrdersPostThread(UserCache.getMobile());
			getMyReceivedOrdersPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, myreceivedordersHandler);
				}
				
			}, 10000);
		} else {
			Toast.makeText(MyReceivedOrdersActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
		}
	}
	
	private class GetMyReceivedOrdersPostThread extends Thread {
		private String mobileengineer;

		public GetMyReceivedOrdersPostThread(String mobileengineer) {
			super();
			this.mobileengineer = mobileengineer;
		}

		@Override
		public void run() {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mobileengineer", mobileengineer));
            ResponseCache.responseMyReceivedOrders = GetMyReceivedOrdersService.send(params);
            SharedPreferencesUtils.setParam(getApplicationContext(), "myreceivedorders", ResponseCache.responseMyReceivedOrders);
            Log.i("guan", "MainActivity: responseMsg = " + ResponseCache.responseMyReceivedOrders);
            if (ResponseCache.responseMyReceivedOrders.equals("FAILED")) {
            	sendMsg(FAILED, myreceivedordersHandler);
            } else {
            	sendMsg(SUCCEEDED, myreceivedordersHandler);
            }
		}
		
	}
	
    private boolean isConnectingToInternet() {
    	ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
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
