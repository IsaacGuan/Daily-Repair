package com.gyr.repair.polling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.activities.MainActivity;
import com.gyr.repair.activities.MyReceivedOrdersActivity;
import com.gyr.repair.activities.MySendedOrdersActivity;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.GetMyReceivedOrdersService;
import com.gyr.repair.http.GetMySendedOrdersService;
import com.jauker.widget.BadgeView;

import android.R.integer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class PollingService extends Service {
	
	public static final String ACTION = "com.gyr.service.PollingService";
	
	private Notification mNotification;
	private NotificationManager mManager;
	
	private int flag = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		initNotifiManager();
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Bundle bundle = intent.getExtras();
		flag = bundle.getInt("flag");
		new PollingThread().start();
	}

	private void initNotifiManager() {
		mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		int icon = R.drawable.ic_launcher_repair;
		mNotification = new Notification();
		mNotification.icon = icon;
		mNotification.tickerText = "New Message";
		mNotification.defaults = Notification.DEFAULT_ALL;
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
		
	}

	private void showNotification() {
		mNotification.when = System.currentTimeMillis();
		//Navigator to the new activity when click the notification title
		
		if (flag == 1) {
			Intent intent = new Intent(this, MySendedOrdersActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			mNotification.setLatestEventInfo(this,
					getResources().getString(R.string.app_name), "您有维修单被接收了！",
					pendingIntent);
			// mNotification = new
			// Notification.Builder(this).setContentTitle("新消息").setContentText("您有的维修单被接收了！").setSmallIcon(R.drawable.ic_launcher).build();
			mManager.notify(0, mNotification);
			
			Intent intentbadge = new Intent("com.gyr.broadcast.badge");
			intentbadge.putExtra("badgeflag", 1);
			sendBroadcast(intentbadge);
			
		}

		
		if (flag == 2) {
			Intent intent = new Intent(this, MyReceivedOrdersActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			mNotification.setLatestEventInfo(this,
					getResources().getString(R.string.app_name), "您有新的维修单了！",
					pendingIntent);
			// mNotification = new
			// Notification.Builder(this).setContentTitle("新消息").setContentText("您有新的维修单了！").setSmallIcon(R.drawable.ic_launcher).build();
			mManager.notify(0, mNotification);
			
			Intent intentbadge = new Intent("com.gyr.broadcast.badge");
			intentbadge.putExtra("badgeflag", 2);
			sendBroadcast(intentbadge);
		}
		
		if (flag == 3) {
			Intent intent = new Intent(this, MyReceivedOrdersActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			mNotification.setLatestEventInfo(this,
					getResources().getString(R.string.app_name), "您有维修单完成了！",
					pendingIntent);
			mManager.notify(0, mNotification);
			
			Intent intentbadge = new Intent("com.gyr.broadcast.badge");
			intentbadge.putExtra("badgeflag", 2);
			sendBroadcast(intentbadge);
		}
		
		
	}

	/**
	 * Polling thread
	 * @Author Ryan
	 * @Create 2013-7-13 上午10:18:34
	 */
	class PollingThread extends Thread {
		@Override
		public void run() {
			showNotification();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("Service:onDestroy");
	}

}
