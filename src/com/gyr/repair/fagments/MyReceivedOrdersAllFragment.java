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

public class MyReceivedOrdersAllFragment extends Fragment {
	
	private ListView myreceivedordersalllv;
	private SimpleAdapter mAdapter;
	private List<Map<String, Object>> data;
	
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
		View view = inflater.inflate(R.layout.fragment_myreceivedordersall, container,
				false);
		
		myreceivedordersalllv = (ListView) view.findViewById(R.id.lv_myreceivedordersall);
		data = getData();
		from = new String[]{"title", "location", "budget", "date"};
		to = new int[]{R.id.ordertitle, R.id.orderlocation, R.id.orderbudget, R.id.orderdate};
		
		mAdapter = new SimpleAdapter(getActivity(), data, R.layout.item_order, from, to);
		myreceivedordersalllv.setAdapter(mAdapter);
		myreceivedordersalllv.setOnItemClickListener(new MyReceivedOrdersAllOnItemClickListener());
		
		return view;
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseMyReceivedOrders);
			for(int i = 0; i < jsonArray.length(); i++){
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
	
	class MyReceivedOrdersAllOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			int cursor = position;
			Intent intent = new Intent(getActivity().getApplicationContext(), OrderActivity.class);
			intent.putExtra("flag", 3);
			intent.putExtra("cursor", cursor);
			startActivity(intent);
			
		}
		
	}
	
}
