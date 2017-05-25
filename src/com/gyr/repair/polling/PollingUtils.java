package com.gyr.repair.polling;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.SharedPreferencesUtils;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.GetMyReceivedOrdersService;
import com.gyr.repair.http.GetMySendedOrdersService;

import android.R.integer;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PollingUtils {
	
	private static PendingIntent pendingIntent;
	
	private static String mysendedorders, myreceivedorders;
	
	private static int flag = 0;
	
	static Timer timer = new Timer();
	
	private static Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 1:
				try {
					pendingIntent.send();
				} catch (CanceledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	};
	
	/**
	 * @param context
	 * @param seconds
	 * @param cls
	 * @param action
	 */ 
	public static void startPollingService(final Context context, int seconds, Class<?> cls,String action) {
		/*
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
				*/
		
		final Intent intent = new Intent(context, cls);
		intent.setAction(action);
		/*
		pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
				*/
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				List<NameValuePair> params2 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("mobileuser", UserCache.getMobile()));
				params2.add(new BasicNameValuePair("mobileengineer", UserCache.getMobile()));
				mysendedorders = GetMySendedOrdersService.send(params1);
				Log.i("poll", "mysendedorders " + mysendedorders);
				if (UserCache.isEngineer()) {
					myreceivedorders = GetMyReceivedOrdersService.send(params2);
					Log.i("poll", "myreceivedorders " + myreceivedorders);
				}
				
				flag = compare();
				
				if (flag != 0) {
					Log.i("poll", "do poll 1");
					intent.putExtra("flag", flag);
					pendingIntent = PendingIntent.getService(context, 0,
							intent, PendingIntent.FLAG_UPDATE_CURRENT);
					
					Message message = new Message();
					message.what = 1;
					myHandler.sendMessage(message);
				}
				
			}
		}, 1000, seconds * 1000);
		
		
		/*
		long triggerAtTime = SystemClock.elapsedRealtime();
		manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
				seconds, pendingIntent);
				*/
		
	}

	/**
	 * 
	 * @param context
	 * @param cls
	 * @param action
	 */
	public static void stopPollingService(Context context, Class<?> cls,String action) {
		/*
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
				*/
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		//manager.cancel(pendingIntent);
		
		pendingIntent.cancel();
	}
	
	private static int compare() {
		int mysendedcount1 = 0;
		int mysendedcount2 = 0;
		int myreceivedcount1 = 0;
		int myreceivedcount2 = 0;
		int mycompletedcount1 = 0;
		int mycompletedcount2 = 0;
		
		try {
			JSONArray jsonArraySended1 = new JSONArray(ResponseCache.responseMySendedOrders);
			JSONArray jsonArraySended2 = new JSONArray(mysendedorders);
			JSONObject jsonObjectSended1 = jsonArraySended1.getJSONObject(0);
			JSONObject jsonObjectSended2 = jsonArraySended2.getJSONObject(0);
			if (!jsonObjectSended1.getString("mobileuser").equals(jsonObjectSended2.getString("mobileuser"))) {
				return 0;
			}
			for(int i = 0; i < jsonArraySended1.length(); i++){
				jsonObjectSended1 = jsonArraySended1.getJSONObject(i);
				if (jsonObjectSended1.getString("status").equals("received")) {
					mysendedcount1 ++;
				}
			}
			for(int i = 0; i < jsonArraySended2.length(); i++){
				jsonObjectSended2 = jsonArraySended2.getJSONObject(i);
				if (jsonObjectSended2.getString("status").equals("received")) {
					mysendedcount2 ++;
				}
			}
			if (UserCache.isEngineer()) {
				JSONArray jsonArrayReceived1 = new JSONArray(ResponseCache.responseMyReceivedOrders);
				JSONArray jsonArrayReceived2 = new JSONArray(myreceivedorders);
				JSONObject jsonObjectReceived1 = jsonArrayReceived1.getJSONObject(0);
				JSONObject jsonObjectReceived2 = jsonArrayReceived2.getJSONObject(0);
				if (!jsonObjectReceived1.getString("mobileuser").equals(jsonObjectReceived2.getString("mobileuser"))) {
					return 0;
				}
				for (int i = 0; i < jsonArrayReceived1.length(); i++) {
					jsonObjectReceived1 = jsonArrayReceived1.getJSONObject(i);
					if (jsonObjectReceived1.getString("status").equals("received")) {
						myreceivedcount1++;
					}
					if (jsonObjectReceived1.getString("status").equals("completed")) {
						mycompletedcount1++;
					}
				}
				for (int i = 0; i < jsonArrayReceived2.length(); i++) {
					jsonObjectReceived2 = jsonArrayReceived2.getJSONObject(i);
					if (jsonObjectReceived2.getString("status").equals("received")) {
						myreceivedcount2++;
					}
					if (jsonObjectReceived2.getString("status").equals("completed")) {
						mycompletedcount2++;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (mysendedcount1 < mysendedcount2) {
			ResponseCache.responseMySendedOrders = mysendedorders;
			Log.i("poll", "1");
			return 1;
		}
		if (myreceivedcount1 < myreceivedcount2) {
			ResponseCache.responseMyReceivedOrders = myreceivedorders;
			Log.i("poll", "2");
			return 2;
		}
		if (mycompletedcount1 < mycompletedcount2) {
			ResponseCache.responseMyReceivedOrders = myreceivedorders;
			Log.i("poll", "3");
			return 3;
		}
		
		return 0;
		
	}
	
}
