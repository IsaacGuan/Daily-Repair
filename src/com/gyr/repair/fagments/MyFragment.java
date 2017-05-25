package com.gyr.repair.fagments;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.baidu.mapapi.SDKInitializer;
import com.gyr.repair.R;
import com.gyr.repair.activities.MainActivity;
import com.gyr.repair.activities.MyReceivedOrdersActivity;
import com.gyr.repair.activities.OrderlistActivity;
import com.gyr.repair.activities.VerifyIdentityActivity;
import com.gyr.repair.activities.LoginActivity;
import com.gyr.repair.activities.MySendedOrdersActivity;
import com.gyr.repair.activities.PersonalCenterActivity;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.GetMyReceivedOrdersService;
import com.gyr.repair.http.GetMySendedOrdersService;
import com.jauker.widget.BadgeView;

import android.app.Fragment;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyFragment extends Fragment implements OnClickListener{
	
	private LinearLayout headlayout;
	private RelativeLayout personalcenterlayout, mysendedorderslayout, engineerlayout, sendedbadgelayout, receivedbadgelayout;
	private TextView headtv, engineertv;
	private ImageView headiv;
	
	/*
	private Handler mysendedordersHandler;
	private Handler myreceivedordersHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;
	*/
	
	public static BadgeView badgemy;
	
	private static boolean issendedbadge = false;
	private static boolean isreceivedbadge = false;
	
	private myfragmentBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_my, container,
				false);
		//SDKInitializer.initialize(getActivity().getApplicationContext());
		
		receiver = new myfragmentBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.gyr.broadcast.badge");
		getActivity().getApplicationContext().registerReceiver(receiver, filter);
		
		headlayout = (LinearLayout) view.findViewById(R.id.ll_head);
		headlayout.setOnClickListener(this);
		personalcenterlayout = (RelativeLayout) view.findViewById(R.id.rl_personalcenter);
		personalcenterlayout.setOnClickListener(this);
		mysendedorderslayout = (RelativeLayout) view.findViewById(R.id.rl_mysendedorders);
		mysendedorderslayout.setOnClickListener(this);
		engineerlayout = (RelativeLayout) view.findViewById(R.id.rl_engineer);
		engineerlayout.setOnClickListener(this);
		
		sendedbadgelayout = (RelativeLayout) view.findViewById(R.id.rl_sendedbadge);
		receivedbadgelayout = (RelativeLayout) view.findViewById(R.id.rl_receivedbadge);
		
		headtv = (TextView) view.findViewById(R.id.tv_head);
		if (!UserCache.isGuest()) {
			headtv.setText(UserCache.getName());
		}
		headiv = (ImageView) view.findViewById(R.id.iv_head);
		engineertv = (TextView) view.findViewById(R.id.tv_engineer);
		if (UserCache.isUser()) {
			headiv.setImageResource(R.drawable.user);
		}
		if (UserCache.isEngineer()) {
			headiv.setImageResource(R.drawable.engineer);
			engineertv.setText("接到的活儿");
		}
		
		issendedbadge = false;
		isreceivedbadge = false;
		badgemy = new BadgeView(getActivity());
		
		/*
		mysendedordersHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				Intent intent = new Intent(getActivity(), MySendedOrdersActivity.class);
            		startActivity(intent);
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(getActivity(), "维修单列表载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
				}
			}
			
		};
		
		myreceivedordersHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				Intent intent = new Intent(getActivity(), MyReceivedOrdersActivity.class);
            		startActivity(intent);
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(getActivity(), "维修单列表载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
				}
			}
			
		};
		*/
		
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_head:
			clickHead();
			break;
		case R.id.rl_personalcenter:
			clickPersonalCenter();
			break;
		case R.id.rl_mysendedorders:
			if (issendedbadge) {
				badgemy.setBadgeCount(0);
				MainActivity.badge.setBadgeCount(0);
				issendedbadge = false;
			}
			clickMySendedOrders();
			break;
		case R.id.rl_engineer:
			if (isreceivedbadge) {
				MainActivity.badge.setBadgeCount(0);
				badgemy.setBadgeCount(0);
				isreceivedbadge = false;
			}
			clickEngineer();
			break;
		}
	}
	
	private void clickHead() {
		if (UserCache.isGuest()) {
			Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(getActivity().getApplicationContext(), PersonalCenterActivity.class);
			startActivity(intent);
		}
	}
	
	private void clickPersonalCenter() {
		if (UserCache.isGuest()) {
			Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(getActivity().getApplicationContext(), PersonalCenterActivity.class);
			startActivity(intent);
		}
	}
	
	private void clickMySendedOrders() {
		if (UserCache.isGuest()) {
			Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(getActivity(), MySendedOrdersActivity.class);
    		startActivity(intent);
			/*
			if (isConnectingToInternet()) {
				progressDialog = ProgressDialog.show(getActivity(), null, null, true);
				GetMySendedOrdersPostThread getMySendedOrdersPostThread = new GetMySendedOrdersPostThread(UserCache.getMobile());
				getMySendedOrdersPostThread.start();
				timer = new Timer();
				timer.schedule(new TimerTask(){

					@Override
					public void run() {
						sendMsg(FAILED, mysendedordersHandler);
					}
					
				}, 10000);
				//Intent intent = new Intent(getActivity(), MySendedOrdersActivity.class);
        		//startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "网络未连接", Toast.LENGTH_SHORT).show();
			}
			*/
		}
	}
	
	private void clickEngineer() {
		if (UserCache.isEngineer()) {
			Intent intent = new Intent(getActivity(), MyReceivedOrdersActivity.class);
    		startActivity(intent);
			/*
			if (isConnectingToInternet()) {
				progressDialog = ProgressDialog.show(getActivity(), null, null, true);
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
				Toast.makeText(getActivity(), "网络未连接", Toast.LENGTH_SHORT).show();
			}
			//Intent intent = new Intent(getActivity(), MyReceivedOrdersActivity.class);
    		//startActivity(intent);
    		*/
		} else {
			Intent intent = new Intent(getActivity().getApplicationContext(), VerifyIdentityActivity.class);
			startActivity(intent);
		}
	}
	
	private class myfragmentBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle=intent.getExtras();
			int badgeflag = bundle.getInt("badgeflag");
			if (badgeflag == 1) {
				badgemy.setBadgeCount(1);
				badgemy.setTargetView(sendedbadgelayout);
				badgemy.setBadgeGravity(Gravity.CENTER);
				issendedbadge = true;
			}
			if (badgeflag == 2) {
				badgemy.setBadgeCount(1);
				badgemy.setTargetView(receivedbadgelayout);
				badgemy.setBadgeGravity(Gravity.CENTER);
				isreceivedbadge = true;
			}
			
		}
		
	}
	
	/*
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
            Log.i("guan", "MainActivity: responseMsg = " + ResponseCache.responseMySendedOrders);
            if (ResponseCache.responseMySendedOrders.equals("FAILED")) {
            	sendMsg(FAILED, mysendedordersHandler);
            } else {
            	sendMsg(SUCCEEDED, mysendedordersHandler);
            }
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
            Log.i("guan", "MainActivity: responseMsg = " + ResponseCache.responseMyReceivedOrders);
            if (ResponseCache.responseMyReceivedOrders.equals("FAILED")) {
            	sendMsg(FAILED, myreceivedordersHandler);
            } else {
            	sendMsg(SUCCEEDED, myreceivedordersHandler);
            }
		}
		
	}
	
    private boolean isConnectingToInternet() {
    	ConnectivityManager connectivity = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
	*/

}
