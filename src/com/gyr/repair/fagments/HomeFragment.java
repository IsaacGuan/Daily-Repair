package com.gyr.repair.fagments;

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
import com.gyr.repair.activities.VerifyIdentityActivity;
import com.gyr.repair.activities.EngineerlistActivity;
import com.gyr.repair.activities.OrderActivity;
import com.gyr.repair.activities.OrderlistActivity;
import com.gyr.repair.activities.SendOrderActivity;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.GetEngineerlistPostService;
import com.gyr.repair.http.GetOrderlistPostService;
import com.gyr.repair.view.Kanner;
import com.gyr.repair.view.MarqueeView;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("deprecation")
public class HomeFragment extends Fragment implements OnClickListener{
	
	private RelativeLayout sendorderlayout, applyengineerlayout, receiveorderlayout, engineerlistlayout, showlayout;
	private TextView newstv;
	private MarqueeView newsmv;
	
	private static ArrayList<Integer> cursor;
	
	private ListView showlv;
	private SimpleAdapter mAdapter;
	private List<Map<String, Object>> data;
	
	private String[] from;
	private int[] to;
	
	/*
	private Handler orderlistHandler;
	private Handler engineerlistHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;
	*/
	
	private Kanner kanner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container,
				false);
		
		sendorderlayout = (RelativeLayout) view.findViewById(R.id.rl_sendorder);
		sendorderlayout.setOnClickListener(this);
		applyengineerlayout = (RelativeLayout) view.findViewById(R.id.rl_applyengineer);
		applyengineerlayout.setOnClickListener(this);
		receiveorderlayout = (RelativeLayout) view.findViewById(R.id.rl_receiveorder);
		receiveorderlayout.setOnClickListener(this);
		engineerlistlayout = (RelativeLayout) view.findViewById(R.id.rl_engineerlist);
		engineerlistlayout.setOnClickListener(this);
		showlayout = (RelativeLayout) view.findViewById(R.id.rl_show1);
		showlayout.setOnClickListener(this);
		
		newstv = (TextView) view.findViewById(R.id.tv_news);
		String news = "<font color='#1087eb'>修修</font>新闻 ";
		newstv.setText(Html.fromHtml(news));
		
		newsmv = (MarqueeView) view.findViewById(R.id.mv_news);
		newsmv.startWithList(setItems(newsmv));
		
		showlv = (ListView) view.findViewById(R.id.lv_show);
		showlvget();
		/*
		data = getData();
		from = new String[]{"showcontent"};
		to = new int[]{R.id.showcontent};
		
		mAdapter = new SimpleAdapter(getActivity(), data, R.layout.item_show, from, to);
		showlv.setAdapter(mAdapter);
		showlv.setOnItemClickListener(new ShowlistOnItemClickListener());
		*/

		kanner = (Kanner) view.findViewById(R.id.kanner_ad);
		int[] imagesRes = { R.drawable.ad1, R.drawable.ad2, R.drawable.ad3,
				R.drawable.ad4, R.drawable.ad5 };
		kanner.setImagesRes(imagesRes);
		
		/*
		orderlistHandler = new Handler() {
			
			@Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				Intent intent = new Intent(getActivity(), OrderlistActivity.class);
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
        
        engineerlistHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				Intent intent = new Intent(getActivity(), EngineerlistActivity.class);
					startActivity(intent);
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(getActivity(), "维修工列表载入失败，请检查网络连接", Toast.LENGTH_SHORT).show();
    				break;
                }
				
			}
        	
        };
        */
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		showlvget();
	}

	private void showlvget() {
		data = getData();
		from = new String[]{"showcontent"};
		to = new int[]{R.id.showcontent};
		
		mAdapter = new SimpleAdapter(getActivity(), data, R.layout.item_show, from, to);
		showlv.setAdapter(mAdapter);
		showlv.setOnItemClickListener(new ShowlistOnItemClickListener());
		
	}

	private List<String> setItems (MarqueeView mv) {
		List<String> items = new ArrayList();
		items.add("顺势而为——汇川技术机器人的开春新篇章");
		items.add("OEM行业最新资讯");
		items.add("VR、AR、MR，虚拟 + 工业，看谁玩得转！");
		return items;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.rl_sendorder:
			clickSendOrder();
			break;
		case R.id.rl_applyengineer:
			if (!UserCache.isEngineer()) {
				clickApplyEngineer();
			} else {
				Toast.makeText(getActivity(), "您已是维修工", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.rl_receiveorder:
			clickReceiveOrders();
			break;
		case R.id.rl_engineerlist:
			clickEngineerList();
			break;
		case R.id.rl_show1:
			clickReceiveOrders();
			break;
		}		
	}

	private void clickSendOrder() {
		Intent intent = new Intent(getActivity().getApplicationContext(), SendOrderActivity.class);
		intent.putExtra("flag", 1);
		startActivity(intent);
	}
	
	private void clickApplyEngineer() {
		Intent intent = new Intent(getActivity().getApplicationContext(), VerifyIdentityActivity.class);
		startActivity(intent);
	}
	
	private void clickReceiveOrders() {
		Intent intent = new Intent(getActivity(), OrderlistActivity.class);
		startActivity(intent);
		/*
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(getActivity(), null, null, true);
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
    		Toast.makeText(getActivity(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
    	*/
	}
	
	private void clickEngineerList() {
		Intent intent = new Intent(getActivity(), EngineerlistActivity.class);
		startActivity(intent);
		/*
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(getActivity(), null, null, true);
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
			Toast.makeText(getActivity(), "网络未连接", Toast.LENGTH_SHORT).show();
		}
		*/
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		cursor = new ArrayList<Integer>();
		
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseOrderlist);
			int length = 0;
			if (jsonArray.length() >= 10) {
				length = 10;
			} else {
				length = jsonArray.length();
			}
			for(int i=jsonArray.length()-1; i>jsonArray.length()-1-length; i--){
				cursor.add(i);
				map = new HashMap<String, Object>();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				map.put("showcontent", "["+jsonObject.getString("city") + " " + jsonObject.getString("district") + "]" + "  " + jsonObject.getString("title"));
				list.add(map);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	class ShowlistOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) mAdapter.getItem(position);
			Intent intent = new Intent (getActivity(), OrderActivity.class);
			intent.putExtra("flag", 1);
			intent.putExtra("cursor", cursor.get(position));
			startActivity(intent);
			
		}
		
	}
	
	/*
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
