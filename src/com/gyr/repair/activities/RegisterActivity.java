package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.SharedPreferencesUtils;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.RegisterPostService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {
	
	private static final String tag="guan";

	private TextView tologintv;
	private EditText registermobileet, registerpasswordet, registerpassword2et, registernameet;
	private CheckBox agreecb;
	private Button registerbt;
	
	private String registermobile, registerpassword, registerpassword2, registername;
	
	private Handler registerHandler;
	private Timer timer;
	private ProgressDialog dialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		tologintv = (TextView) findViewById(R.id.tv_tologin);
		tologintv.setOnClickListener(this);
		registerbt = (Button) findViewById(R.id.bt_register);
		registerbt.setOnClickListener(this);
		
		registermobileet = (EditText) findViewById(R.id.et_registermobile);
		registerpasswordet = (EditText) findViewById(R.id.et_registerpassword);
		registerpassword2et = (EditText) findViewById(R.id.et_registerpassword2);
		registernameet = (EditText) findViewById(R.id.et_registername);
		agreecb = (CheckBox) findViewById(R.id.cb_agree);
		
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
        
        registermobileet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(11)});
        registerpasswordet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(15)});
        registerpassword2et.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(15)});
        registernameet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
		
        registerHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				dialog.dismiss();
    				getUserInfom();
    				Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
    				ResponseCache.cacheFlag = true;
            		startActivity(intent);
            		RegisterActivity.this.finish();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	dialog.dismiss();
                	Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
    				break;
                }
                /*
                dialog.dismiss();
                if (msg.what == 1) {  // 处理发送线程传回的消息
                    if(msg.obj.toString().equals("SUCCEEDED")){
                        Log.i("tag", "注册模拟跳转");
                        //跳转
                        UserCache.setUser();
                        UserCache.setMobile(registermobile);
                    	Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    	startActivity(intent);
                    	RegisterActivity.this.finish();
                        //Toast.makeText(RegisterActivity.this, "模拟跳转", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    }
                }
                */
            }
        };
		
	}
	
	private void getUserInfom() {
		UserCache.setUser();
		SharedPreferencesUtils.setParam(this, "type", 1);
		UserCache.setId("u"+registermobile);
		SharedPreferencesUtils.setParam(this, "id", "u"+registermobile);
		UserCache.setMobile(registermobile);
		SharedPreferencesUtils.setParam(this, "mobile", registermobile);
		UserCache.setPassword(registerpassword);
		SharedPreferencesUtils.setParam(this, "password", registerpassword);
		UserCache.setName(registername);
		SharedPreferencesUtils.setParam(this, "name", registername);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_tologin:
			clickToLogin();
			break;
		case R.id.bt_register:
			attemptRegister();
			break;
		}
	}

	private void clickToLogin() {
		Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
		startActivity(intent);
		RegisterActivity.this.finish();
	}
	
	private void attemptRegister() {
		registermobileet.setError(null);
		registerpasswordet.setError(null);
		registerpassword2et.setError(null);
		registernameet.setError(null);
		
		registermobile = registermobileet.getText().toString().trim();
		registerpassword = registerpasswordet.getText().toString().trim();
		registerpassword2 = registerpassword2et.getText().toString().trim();
		registername = registernameet.getText().toString().trim();
		
		boolean cancel = false;
		View focusView = null;
		
		if (TextUtils.isEmpty(registermobile)) {
			registermobileet.setError("必须填");
			focusView = registermobileet;
			cancel = true;
		} else if (!isMobileValid(registermobile)) {
			registermobileet.setError("手机号错误");
			focusView = registermobileet;
			cancel = true;
		}
		
		if (TextUtils.isEmpty(registerpassword) || !isPasswordValid(registerpassword)) {
			registerpasswordet.setError("密码过短");
			focusView = registerpasswordet;
            cancel = true;
		}
		
		if (!registerpassword2.equals(registerpassword)) {
			registerpassword2et.setError("两次输入密码不一致");
			focusView = registerpassword2et;
            cancel = true;
		}
		
		if (TextUtils.isEmpty(registername)) {
			registernameet.setError("必须填");
			focusView = registernameet;
			cancel = true;
		}
		
		if (!agreecb.isChecked()) {
			Toast.makeText(getApplicationContext(), "请阅读并同意《修修生活协议》", Toast.LENGTH_SHORT).show();
			cancel = true;
		}
		
		if (cancel) {
			focusView.requestFocus();
		} else {
        	Log.i(tag, "success");
        	register();
        }
		
	}
	
    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
    
    private boolean isMobileValid(String mobile) {
    	Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
    	Matcher m = p.matcher(mobile);
    	return m.matches();
    }
    
    private void register() {
    	if (isConnectingToInternet()) {
    		dialog = ProgressDialog.show(this, "请稍候", "正在注册...", true);
    		RegisterPostThread registerThread = new RegisterPostThread(registermobile, registerpassword, registername);
    		registerThread.start();
    		timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, registerHandler);
				}
				
			}, 10000);
    	} else {
    		Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public class RegisterPostThread extends Thread {
    	public String mobile, password, name;
    	
        public RegisterPostThread(String mobile, String password, String name) {
            this.mobile = mobile;
            this.password = password;
            this.name = name;
        }

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
            String responseMsg;
            if(!mobile.equals("")) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("mobile", mobile));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("name", name));
                responseMsg = RegisterPostService.send(params);
                Log.i("tag", "RegisterActivity: responseInt = " + responseMsg);
                if (responseMsg.equals("FAILED")) {
                	sendMsg(FAILED, registerHandler);
                } else {
                	sendMsg(SUCCEEDED, registerHandler);
                }
            }
		}
        
    }
    
    public boolean isConnectingToInternet() {
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
