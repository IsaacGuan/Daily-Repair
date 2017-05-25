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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MySendedOrdersAllFragment extends Fragment {
	
	private ListView mysendedordersalllv;
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
		View view = inflater.inflate(R.layout.fragment_mysendedordersall, container,
				false);
		
		mysendedordersalllv = (ListView) view.findViewById(R.id.lv_mysendedordersall);
		data = getData();
		from = new String[]{"title", "location", "budget", "date"};
		to = new int[]{R.id.ordertitle, R.id.orderlocation, R.id.orderbudget, R.id.orderdate};
		
		MyAdapter myAdapter = new MyAdapter(getActivity(), data);
		
		//mAdapter = new SimpleAdapter(getActivity(), data, R.layout.item_order, from, to);
		mysendedordersalllv.setAdapter(myAdapter);
		mysendedordersalllv.setOnItemClickListener(new MySendedOrdersAllOnItemClickListener());
		
		return view;
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseMySendedOrders);
			for(int i = 0; i < jsonArray.length(); i++){
				map = new HashMap<String, Object>();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				map.put("title", jsonObject.getString("title"));
				map.put("location", jsonObject.getString("city") + " " + jsonObject.getString("district"));
				map.put("budget", jsonObject.getString("budget") + "元");
				map.put("date", jsonObject.getString("date"));
				map.put("status", jsonObject.getString("status"));
				list.add(map);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	class MySendedOrdersAllOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			int cursor = position;
			Intent intent = new Intent(getActivity().getApplicationContext(), OrderActivity.class);
			intent.putExtra("flag", 2);
			intent.putExtra("cursor", cursor);
			startActivity(intent);
			
		}
		
	}
	
	class MyAdapter extends BaseAdapter {
		
		private List<Map<String, Object>> data;
	    private LayoutInflater layoutInflater;
	    private Context context;
		
		public MyAdapter(Context context, List<Map<String, Object>> data ) {
			this.data = data;
			this.context = context;
			this.layoutInflater = layoutInflater.from(context);
			
		}
		
		public final class ViewHolder {
			public TextView title;
			public TextView location;
			public TextView budget;
			public TextView date;
			
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = layoutInflater.inflate(R.layout.item_order, null);
				holder.title = (TextView)convertView.findViewById(R.id.ordertitle);
				holder.location = (TextView)convertView.findViewById(R.id.orderlocation);
				holder.budget = (TextView)convertView.findViewById(R.id.orderbudget);
				holder.date = (TextView)convertView.findViewById(R.id.orderdate);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.title.setText((String)data.get(position).get("title"));
			holder.location.setText((String)data.get(position).get("location"));
			holder.budget.setText((String)data.get(position).get("budget"));
			holder.date.setText((String)data.get(position).get("date"));
			
			if (data.get(position).get("status").equals("expired")) {
				holder.date.setTextColor(getResources().getColor(R.color.red));
				holder.date.setText("[已过期] " + (String)data.get(position).get("date"));
			}
			
			return convertView;
		}
		
	}

}
