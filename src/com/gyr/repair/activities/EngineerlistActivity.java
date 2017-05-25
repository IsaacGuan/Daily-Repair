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
import org.json.JSONObject;

import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.http.GetEngineerlistPostService;
import com.gyr.repair.view.XListView;
import com.gyr.repair.view.XListView.IXListViewListener;

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
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class EngineerlistActivity extends Activity implements IXListViewListener, OnQueryTextListener{
	
	private SearchView engineerssv;
	
	private Handler mHandler;
	private XListView engineerslv;
	private SimpleAdapter mAdapter;
	
	private List<Map<String, Object>> data;
	
	private ArrayList<Integer> cursor;
	
	private String[] from;
	private int[] to;
	
	private Handler engineerlistHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;
	
	private static boolean refreshflag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_engineerlist);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		/*
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		responseEngineerlist = bundle.getString("responseEngineerlist");
		*/
		
		engineerslv = (XListView)findViewById(R.id.lv_engineers);
		engineerslv.setPullLoadEnable(true);
		
		engineerssv = (SearchView) findViewById(R.id.sv_engineers);
		engineerssv.setOnQueryTextListener(this);
		
		engineerlistHandler = new Handler() {
			
			@Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
                	if (refreshflag) {
                		timer.cancel();
        				progressDialog.dismiss();
                	}
    				loadEngineers();
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
		
		getEngineers();
		
	}
	
	private void loadEngineers() {
		data = getData();
		from = new String[]{"photo", "name", "expert", "location"};
		to = new int[]{R.id.engineerphoto, R.id.engineername, R.id.engineerexpert, R.id.engineerlocation};
		
		mAdapter = new SimpleAdapter(this, data, R.layout.item_engineer, from, to);
		engineerslv.setAdapter(mAdapter);
		engineerslv.setOnItemClickListener(new EngineerlistOnItemClickListener());
		engineerslv.setXListViewListener(this);
		
		mHandler = new Handler();
		
	}
	
	private void getEngineers() {
		if (isConnectingToInternet()) {
			refreshflag = true;
			progressDialog = ProgressDialog.show(this, null, null, true);
			GetEngineerlistPostThread getEngineerlistPostThread = new GetEngineerlistPostThread();
			getEngineerlistPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, engineerlistHandler);
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
		mAdapter = new SimpleAdapter(this, obj, R.layout.item_engineer, from, to);
		engineerslv.setAdapter(mAdapter);
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
		engineerslv.stopRefresh();
		engineerslv.stopLoadMore();
		engineerslv.setRefreshTime("刚刚");
	}
	
	@Override
	public void onRefresh() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				refreshflag = false;
				GetEngineerlistPostThread getEngineerlistPostThread = new GetEngineerlistPostThread();
				getEngineerlistPostThread.start();
				//getData();
				//engineerslv.setAdapter(mAdapter);
				onLoad();
				
			}
			
		}, 2000);
		
	}

	@Override
	public void onLoadMore() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				refreshflag = false;
				GetEngineerlistPostThread getEngineerlistPostThread = new GetEngineerlistPostThread();
				getEngineerlistPostThread.start();
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
		
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseEngineerlist);
			for(int i = 0; i < jsonArray.length(); i++){
				cursor.add(i);
				map = new HashMap<String, Object>();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				map.put("photo", R.drawable.engineer);
				map.put("name", jsonObject.getString("name"));
				map.put("expert", jsonObject.getString("expert"));
				map.put("location", jsonObject.getString("city") + " " + jsonObject.getString("district"));
				list.add(map);
			}
		} catch (Exception e) {
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
	
	private class GetEngineerlistPostThread extends Thread {

		public GetEngineerlistPostThread() {
			super();
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("default", ""));
            ResponseCache.responseEngineerlist = GetEngineerlistPostService.send(params);
			Log.i("guan", "MainActivity: responseMsg = " + ResponseCache.responseEngineerlist);
			if (ResponseCache.responseEngineerlist.equals("FAILED")) {
				sendMsg(FAILED, engineerlistHandler);
			} else {
				sendMsg(SUCCEEDED, engineerlistHandler);
			}
		}
		
	}
	
	class EngineerlistOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			/*
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) EngineerlistActivity.this.mAdapter.getItem(position-1);
			int cursor = position - 1;
			*/
			Intent intent = new Intent (EngineerlistActivity.this, EngineerActivity.class);
			intent.putExtra("cursor", cursor.get(position-1));
			startActivity(intent);
			
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
