package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.http.GetOrderlistPostService;
import com.gyr.repair.view.XListView;
import com.gyr.repair.view.XListView.IXListViewListener;

import android.R.integer;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleAdapter;

public class OrderlistActivity extends Activity implements IXListViewListener, OnQueryTextListener{
	
	private SearchView orderssv;
	
	private Handler mHandler;
	private XListView orderslv;
	private SimpleAdapter mAdapter;
	
	private List<Map<String, Object>> data;
	
	private ArrayList<Integer> cursor;
	
	private String[] from;
	private int[] to;
	
	private Handler orderlistHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;
	
	private static boolean refreshflag = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_orderlist);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		/*
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		responseOrderlist = bundle.getString("responseOrderlist");
		*/
				
		orderslv = (XListView)findViewById(R.id.lv_orders);
		orderslv.setPullLoadEnable(true);
		
		orderssv = (SearchView) findViewById(R.id.sv_orders);
		orderssv.setOnQueryTextListener(this);
		
		orderlistHandler = new Handler() {
			
			@Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
                	if (refreshflag) {
                		timer.cancel();
        				progressDialog.dismiss();
                	}
    				loadOrders();
    				break;
                case FAILED:
                	Thread.interrupted();
                	if (refreshflag) {
                		timer.cancel();
                    	progressDialog.dismiss();
                	}
                	Toast.makeText(getApplicationContext(), "载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
                }
			}
			
		};
		getOrders();
		
	}
	
	private void loadOrders() {
		from = new String[]{"title", "location", "budget", "date"};
		to = new int[]{R.id.ordertitle, R.id.orderlocation, R.id.orderbudget, R.id.orderdate};
		data = getData();
		
		mAdapter = new SimpleAdapter(this, data, R.layout.item_order, from, to);
		orderslv.setAdapter(mAdapter);
		orderslv.setOnItemClickListener(new OrderlistOnItemClickListener());
		orderslv.setXListViewListener(this);
		
		mHandler = new Handler();
	}
	
	private void getOrders() {
		if (isConnectingToInternet()) {
			refreshflag = true;
			progressDialog = ProgressDialog.show(this, null, null, true);
			GetOrderlistPostThread getOrderlistPostThread = new GetOrderlistPostThread("unreceived");
			getOrderlistPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, orderlistHandler);
				}
				
			}, 10000);
		} else {
    		Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		List<Map<String, Object>> obj = searchItem(newText);
		mAdapter = new SimpleAdapter(this, obj, R.layout.item_order, from, to);
		orderslv.setAdapter(mAdapter);
		return false;
	}
	
	public List<Map<String, Object>> searchItem(String name) {
		List<Map<String, Object>> mSearchList = new ArrayList<Map<String, Object>>();
		cursor = new ArrayList<Integer>();
		int i = 0;
		for (Map<String, Object> m: data) {
			for (String k: m.keySet()) {
				int index = String.valueOf(m.get(k)).indexOf(name);
				if (index != -1) {
					mSearchList.add(m);
					cursor.add(i);
					break;
				}
			}
			i++;
		}
		return mSearchList;
	}
	
	private void onLoad() {
		orderslv.stopRefresh();
		orderslv.stopLoadMore();
		orderslv.setRefreshTime("刚刚");
	}
	
	
	@Override
	public void onRefresh() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				refreshflag = false;
				GetOrderlistPostThread getOrderlistPostThread = new GetOrderlistPostThread("unreceived");
				getOrderlistPostThread.start();
				//getData();
				//orderslv.setAdapter(mAdapter);
				onLoad();
				
			}
			
		}, 2000);
		
	}

	@Override
	public void onLoadMore() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				//refreshflag = false;
				//GetOrderlistPostThread getOrderlistPostThread = new GetOrderlistPostThread("unreceived");
				//getOrderlistPostThread.start();
				//getData();
				//mAdapter.notifyDataSetChanged();
				onLoad();
				
			}
			
		}, 2000);
		
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map;
		cursor = new ArrayList<Integer>();
		/*
		for(int i=1; i<=10; i++){
			map = new HashMap<String, Object>();
			map.put("title", "标题"+String.valueOf(i));
			map.put("location", "地址"+String.valueOf(i));
			map.put("budget", "预算"+String.valueOf(i));
			map.put("date", "时间"+String.valueOf(i));
			list.add(map);
		}
		*/
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseOrderlist);
			for (int i = 0; i < jsonArray.length(); i++) {
				cursor.add(i);
				map = new HashMap<String, Object>();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				map.put("title", jsonObject.getString("title"));
				map.put("location", jsonObject.getString("city") + " " + jsonObject.getString("district"));
				map.put("budget", jsonObject.getString("budget") + "元");
				map.put("date", jsonObject.getString("date"));
				list.add(map);
					
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.finish();
		}
		return false;
	}
	
	private class GetOrderlistPostThread extends Thread {
		public String status;

		public GetOrderlistPostThread(String status) {
			super();
			this.status = status;
		}

		@Override
		public void run() {
			if (!status.equals("")) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("status", status));
                ResponseCache.responseOrderlist = GetOrderlistPostService.send(params);
                Log.i("guan", "MainActivity: responseMsg = " + ResponseCache.responseOrderlist);
                if (ResponseCache.responseOrderlist.equals("FAILED")) {
                	sendMsg(FAILED, orderlistHandler);
                } else {
                	sendMsg(SUCCEEDED, orderlistHandler);
                }
			}
		}
	}
	
	class OrderlistOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			//Log.i(tag, String.valueOf(position));
			
			/*
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) OrderlistActivity.this.mAdapter.getItem(position-1);
			int cursor = position-1;
			*/
			Intent intent = new Intent (OrderlistActivity.this, OrderActivity.class);
			intent.putExtra("flag", 1);
			intent.putExtra("cursor", cursor.get(position-1));
			startActivity(intent);
			
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
	
}
