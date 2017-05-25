package com.gyr.repair.activities;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.UserCache;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EngineerActivity extends Activity implements OnClickListener {
	
	private TextView engineernametv, engineerorderstv, engineerscoretv, engineerexperttv, engineermobiletv, engineerdistricttv, engineeraddresstv;
	private Button engineersendorderbt;
	
	private String engineername, engineermobile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_engineer);

		engineernametv = (TextView)findViewById(R.id.tv_engineername);
		engineerorderstv = (TextView)findViewById(R.id.tv_engineerorders2);
		engineerscoretv = (TextView)findViewById(R.id.tv_engineerscore2);
		engineerexperttv = (TextView)findViewById(R.id.tv_engineerexpert2);
		engineermobiletv = (TextView)findViewById(R.id.tv_engineermobile2);
		engineerdistricttv = (TextView)findViewById(R.id.tv_engineerdistrict2);
		engineeraddresstv = (TextView)findViewById(R.id.tv_engineeraddress2);
		
		engineersendorderbt = (Button)findViewById(R.id.bt_engineersendorder);
		engineersendorderbt.setOnClickListener(this);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		int cursor = bundle.getInt("cursor");
		
		DecimalFormat format = new DecimalFormat("0.00");
		
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseEngineerlist);
			JSONObject jsonObject = jsonArray.getJSONObject(cursor);
			engineername = jsonObject.getString("name");
			engineernametv.setText(engineername);
			engineerorderstv.setText(jsonObject.getString("ordernumber"));
			engineerscoretv.setText(String.valueOf(format.format(Float.parseFloat(jsonObject.getString("score")))));
			engineerexperttv.setText(jsonObject.getString("expert"));
			engineermobile = jsonObject.getString("mobile");
			engineermobiletv.setText(engineermobile);
			engineerdistricttv.setText(jsonObject.getString("city")+" "+jsonObject.getString("district"));
			engineeraddresstv.setText(jsonObject.getString("address"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_engineersendorder:
			if (UserCache.isEngineer() && engineermobile.equals(UserCache.getMobile())) {
				Toast.makeText(getApplicationContext(), "不可自己给自己发维修单",
						Toast.LENGTH_SHORT).show();
			} else {
				clickEngineerSendOrder();
			}
			break;
		}
		
	}

	private void clickEngineerSendOrder() {
		Log.i("guan", engineername + " " + engineermobile);
		Intent intent = new Intent(EngineerActivity.this, SendOrderActivity.class);
		intent.putExtra("flag", 2);
		intent.putExtra("engineername", engineername);
		intent.putExtra("engineermobile", engineermobile);
		startActivity(intent);
		
	}
	
}
