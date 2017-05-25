package com.gyr.repair.activities;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.UserCache;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EngineerInfomationActivity extends Activity implements OnClickListener {
	private TextView engineerinfoidtv, engineerinfoexperttv, engineerinfodistricttv, engineerinfoscoretv, engineerinfoordernumbertv;
	private RelativeLayout modifyengineerinfolayout;
	
	private String engineerinfoexpert, engineerinfocity, engineerinfodistrict, engineerinfoaddress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_engineerinfomation);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		engineerinfoidtv = (TextView) findViewById(R.id.tv_engineerinfoid2);
		engineerinfoidtv.setText(UserCache.getId());
		engineerinfoexperttv = (TextView) findViewById(R.id.tv_engineerinfoexpert2);
		engineerinfoexpert = UserCache.getExpert();
		engineerinfoexperttv.setText(engineerinfoexpert);
		engineerinfodistricttv = (TextView) findViewById(R.id.tv_engineerinfodistrict2);
		engineerinfocity = UserCache.getCity();
		engineerinfodistrict = UserCache.getDistrict();
		engineerinfodistricttv.setText(engineerinfocity + " " + engineerinfodistrict);
		engineerinfoaddress = UserCache.getAddress();
		
		engineerinfoscoretv = (TextView) findViewById(R.id.tv_engineerinfoscore2);
		engineerinfoscoretv.setText(UserCache.getScore());
		engineerinfoordernumbertv = (TextView) findViewById(R.id.tv_engineerinfoordernumber2);
		engineerinfoordernumbertv.setText(UserCache.getOrdernumber());
		
		modifyengineerinfolayout = (RelativeLayout) findViewById(R.id.rl_modifyengineerinfo);
		modifyengineerinfolayout.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_modifyengineerinfo:
			clickModifyEngineerInfo();
			break;
		}
		
	}

	private void clickModifyEngineerInfo() {
		Intent intent = new Intent(EngineerInfomationActivity.this, ModifyEngineerActivity.class);
		intent.putExtra("modifycity", engineerinfocity);
		intent.putExtra("modifydistrict", engineerinfodistrict);
		intent.putExtra("modifyaddress", engineerinfoaddress);
		intent.putExtra("modifyexpert", engineerinfoexpert);
		startActivity(intent);
		
	}
}
