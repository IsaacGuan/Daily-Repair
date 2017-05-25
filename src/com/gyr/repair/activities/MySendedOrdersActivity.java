package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.SharedPreferencesUtils;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.fagments.HomeFragment;
import com.gyr.repair.fagments.MyFragment;
import com.gyr.repair.fagments.MySendedOrdersAllFragment;
import com.gyr.repair.fagments.MySendedOrdersCompletedFragment;
import com.gyr.repair.fagments.MySendedOrdersUndergoningFragment;
import com.gyr.repair.http.GetMySendedOrdersService;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MySendedOrdersActivity extends FragmentActivity {
	
	private static final String tag="guan";

	private TabFragmentPagerAdapter mAdapter;
	private ViewPager mysendedcontentvp;
	private RadioGroup mysendedordersrg;
	
	private Handler mysendedordersHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mysendedorders);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mysendedordersrg = (RadioGroup) findViewById(R.id.rg_mysendedorders);
		mysendedcontentvp = (ViewPager) findViewById(R.id.vp_mysendedcontent);
		
		MainActivity.badge.setBadgeCount(0);
		MyFragment.badgemy.setBadgeCount(0);
		
		mysendedordersHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
    				mysendedcontentvp.setAdapter(mAdapter);
    				setListener();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(MySendedOrdersActivity.this, "维修单列表载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
				}
			}
			
		};
		
		getMySendedOrders();
		
		/*
		mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
		mysendedcontentvp.setAdapter(mAdapter);
		
		setListener();
		*/
		
	}

	public void setListener() {
		
		mysendedcontentvp.setOnPageChangeListener(new OnPageChangeListener() {

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
				
				if (mysendedordersrg != null && mysendedordersrg.getChildCount()>position) {
					((RadioButton)mysendedordersrg.getChildAt(position)).performClick();
				}
				
			}
			
		});
		
		mysendedordersrg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				//Log.i(tag, String.valueOf(R.id.rb_all));
				int mark = R.id.rb_mysendedall;
				
				if (mysendedordersrg.getChildAt(group.getCheckedRadioButtonId() - mark) != null) {
					mysendedcontentvp.setCurrentItem(group.getCheckedRadioButtonId() - mark);
					
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
				ft = new MySendedOrdersAllFragment();
				break;
			case 1:
				ft = new MySendedOrdersUndergoningFragment();
				break;
			case 2:
				ft = new MySendedOrdersCompletedFragment();
				break;
			}
			return ft;
		}

		@Override
		public int getCount() {
			return 3;
		}
		
	}
	
	private void getMySendedOrders() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(MySendedOrdersActivity.this, null, null, true);
			GetMySendedOrdersPostThread getMySendedOrdersPostThread = new GetMySendedOrdersPostThread(UserCache.getMobile());
			getMySendedOrdersPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, mysendedordersHandler);
				}
				
			}, 10000);
		} else {
			Toast.makeText(MySendedOrdersActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
		}
	}
	
	private class GetMySendedOrdersPostThread extends Thread {
		private String mobileuser;

		public GetMySendedOrdersPostThread(String mobileuser) {
			super();
			this.mobileuser = mobileuser;
		}

		@Override
		public void run() {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mobileuser", mobileuser));
            ResponseCache.responseMySendedOrders = GetMySendedOrdersService.send(params);
            SharedPreferencesUtils.setParam(getApplicationContext(), "mysendedorders", ResponseCache.responseMySendedOrders);
            Log.i("guan", "MainActivity: responseMsg = " + ResponseCache.responseMySendedOrders);
            if (ResponseCache.responseMySendedOrders.equals("FAILED")) {
            	sendMsg(FAILED, mysendedordersHandler);
            } else {
            	sendMsg(SUCCEEDED, mysendedordersHandler);
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
