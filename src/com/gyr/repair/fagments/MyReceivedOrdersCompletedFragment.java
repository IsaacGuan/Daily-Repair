package com.gyr.repair.fagments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gyr.repair.R;
import com.gyr.repair.activities.OrderActivity;
import com.gyr.repair.cache.ResponseCache;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MyReceivedOrdersCompletedFragment extends Fragment {
	
	private ListView myreceivedorderscompletedlv;
	private SimpleAdapter mAdapter;
	private List<Map<String, Object>> data;
	
	private static ArrayList<Integer> cursor;
	
	private String[] from;
	private int[] to;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_myreceivedorderscompleted, container,
				false);
		
		myreceivedorderscompletedlv = (ListView) view.findViewById(R.id.lv_myreceivedorderscompleted);
		data = getData();
		from = new String[]{"title", "location", "budget", "date"};
		to = new int[]{R.id.ordertitle, R.id.orderlocation, R.id.orderbudget, R.id.orderdate};
		
		mAdapter = new SimpleAdapter(getActivity(), data, R.layout.item_order, from, to);
		myreceivedorderscompletedlv.setAdapter(mAdapter);
		myreceivedorderscompletedlv.setOnItemClickListener(new MyReceivedOrdersCompletedOnItemClickListener());
		
		return view;
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		cursor = new ArrayList<Integer>();
		
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseMyReceivedOrders);
			for(int i = 0; i < jsonArray.length(); i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.getString("status").equals("completed")) {
					cursor.add(i);
					map = new HashMap<String, Object>();
					map.put("title", jsonObject.getString("title"));
					map.put("location", jsonObject.getString("city") + " "
							+ jsonObject.getString("district"));
					map.put("budget", jsonObject.getString("budget") + "元");
					map.put("date", jsonObject.getString("date"));
					list.add(map);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	class MyReceivedOrdersCompletedOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getActivity().getApplicationContext(), OrderActivity.class);
			intent.putExtra("flag", 3);
			intent.putExtra("cursor", cursor.get(position));
			startActivity(intent);
			
		}
		
	}

}
