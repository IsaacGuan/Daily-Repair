package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.ModifyPasswordService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyPasswordActivity extends Activity implements OnClickListener {
	private EditText formerpasswordet, newpassword1et, newpassword2et;
	private Button modifypasswordbt;
	
	private String formerpassword, newpassword1, newpassword2;
	
	private Handler passwordHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifypassword);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		formerpasswordet = (EditText) findViewById(R.id.et_formerpassword);
		newpassword1et = (EditText) findViewById(R.id.et_newpassword1);
		newpassword2et = (EditText) findViewById(R.id.et_newpassword2);
		
		modifypasswordbt = (Button) findViewById(R.id.bt_modifypassword);
		modifypasswordbt.setOnClickListener(this);
		
		InputFilter filter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if(source.equals(" "))
                	return "";
                else
                	return null;
			}
			
        };
        
        formerpasswordet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(15)});
        newpassword1et.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(15)});
        newpassword2et.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(15)});
		
		passwordHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				changeUserPasswod();
    				Toast.makeText(getApplicationContext(), "修改成功！", Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent(ModifyPasswordActivity.this, MainActivity.class);
            		startActivity(intent);
            		ModifyPasswordActivity.this.finish();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(getApplicationContext(), "修改失败", Toast.LENGTH_SHORT).show();
    				break;
				}
			}
        	
        };
	}
	
	private void changeUserPasswod() {
		UserCache.setPassword(newpassword1);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_modifypassword:
			attemptModifyPassword();
			break;
		}
		
	}

	private void attemptModifyPassword() {
		formerpasswordet.setError(null);
		newpassword1et.setError(null);
		newpassword2et.setError(null);
		
		formerpassword = formerpasswordet.getText().toString().trim();
		newpassword1 = newpassword1et.getText().toString().trim();
		newpassword2 = newpassword2et.getText().toString().trim();
		
		boolean cancel = false;
		View focusView = null;
		
		if (!formerpassword.equals(UserCache.getPassword())) {
			formerpasswordet.setError("原密码错误");
			focusView = formerpasswordet;
			cancel = true;
		}
		
		if (TextUtils.isEmpty(newpassword1) || !isPasswordValid(newpassword1)) {
			newpassword1et.setError("密码过短");
			focusView = newpassword1et;
            cancel = true;
		}
		
		if (!newpassword2.equals(newpassword1)) {
			newpassword2et.setError("两次输入密码不一致");
			focusView = newpassword2et;
            cancel = true;
		}
		
		if (cancel) {
			focusView.requestFocus();
		} else {
			Log.i("guan", "success");
			modifyPasswordConfirm();
		}
		
	}
	
	private void modifyPasswordConfirm() {
		new AlertDialog.Builder(this).setTitle("确认修改？")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				modifyPassword();
				
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		}).show();
		
	}
	
	private void modifyPassword() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, "请稍候", "正在提交...", true);
			ModifyPasswordPostThread modifyPasswordPostThread = new ModifyPasswordPostThread(UserCache.getMobile(), newpassword1);
			modifyPasswordPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMsg(FAILED, passwordHandler);
				}

			}, 10000);			
		} else {
			Toast.makeText(getApplicationContext(), "网络未连接",
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
	
	private class ModifyPasswordPostThread extends Thread {
		private String mobile, password;

		public ModifyPasswordPostThread(String mobile, String password) {
			super();
			this.mobile = mobile;
			this.password = password;
		}

		@Override
		public void run() {
			String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mobile", mobile));
            params.add(new BasicNameValuePair("password", password));
            responseMsg = ModifyPasswordService.send(params);
            Log.i("tag", "ModifyPasswordActivity: responseMsg = " + responseMsg);
            if (responseMsg.equals("FAILED")) {
            	sendMsg(FAILED, passwordHandler);
            } else {
            	sendMsg(SUCCEEDED, passwordHandler);
            }
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
