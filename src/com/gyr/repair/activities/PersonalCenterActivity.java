package com.gyr.repair.activities;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.SharedPreferencesUtils;
import com.gyr.repair.cache.UserCache;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PersonalCenterActivity extends Activity implements OnClickListener {
	private LinearLayout personalinfoLayout, engineerinfoLayout;
	private TextView personalheadtv;
	private ImageView personalheadiv;
	private Button exitbt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personalcenter);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		personalinfoLayout = (LinearLayout) findViewById(R.id.ll_personalinfo);
		personalinfoLayout.setOnClickListener(this);
		engineerinfoLayout = (LinearLayout) findViewById(R.id.ll_engineerinfo);
		engineerinfoLayout.setOnClickListener(this);
		
		personalheadtv = (TextView) findViewById(R.id.tv_personalhead);
		personalheadtv.setText(UserCache.getName());
		
		personalheadiv = (ImageView) findViewById(R.id.iv_personalhead);
		if (UserCache.isUser()) {
			personalheadiv.setImageResource(R.drawable.user);
		}
		if (UserCache.isEngineer()) {
			personalheadiv.setImageResource(R.drawable.engineer);
		}
		
		exitbt = (Button) findViewById(R.id.bt_exit);
		exitbt.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_personalinfo:
			clickPersonalInfo();
			break;
		case R.id.ll_engineerinfo:
			clickEngineerInfo();
			break;
		case R.id.bt_exit:
			new AlertDialog.Builder(this).setTitle("确认退出？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					clickExit();
					
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).show();
			//clickExit();
			break;
		}
		
	}
	
	private void clickPersonalInfo() {
		Intent intent = new Intent(PersonalCenterActivity.this, PersonalInfomationActivity.class);
		startActivity(intent);
	}
	
	private void clickEngineerInfo() {
		if (UserCache.isEngineer()) {
			Intent intent = new Intent(PersonalCenterActivity.this, EngineerInfomationActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(PersonalCenterActivity.this, VerifyIdentityActivity.class);
			startActivity(intent);
		}
	}
	
	private void clickExit() {
		UserCache.setGuest();
		SharedPreferencesUtils.setParam(this, "type", 0);
		Intent intent = new Intent(PersonalCenterActivity.this, MainActivity.class);
		startActivity(intent);
		PersonalCenterActivity.this.finish();
	}
	
}
