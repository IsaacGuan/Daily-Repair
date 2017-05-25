package com.gyr.repair.activities;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.polling.PollingService;
import com.gyr.repair.polling.PollingUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PersonalInfomationActivity extends Activity implements OnClickListener {
	private TextView personalinfoidtv, personalinfonametv, personalinfomobiletv;
	private RelativeLayout modifypasswordlayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personalinfomation);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		personalinfoidtv = (TextView) findViewById(R.id.tv_personalinfoid2);
		personalinfoidtv.setText(UserCache.getId());
		personalinfonametv = (TextView) findViewById(R.id.tv_personalinfoname2);
		personalinfonametv.setText(UserCache.getName());
		personalinfomobiletv = (TextView) findViewById(R.id.tv_personalinfomobile2);
		personalinfomobiletv.setText(UserCache.getMobile());
		
		modifypasswordlayout = (RelativeLayout) findViewById(R.id.rl_modifypassword);
		modifypasswordlayout.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_modifypassword:
			clickModifyPassword();
			break;
		}
		
	}

	private void clickModifyPassword() {
		Intent intent = new Intent(PersonalInfomationActivity.this, ModifyPasswordActivity.class);
		startActivity(intent);
		
	}
}
