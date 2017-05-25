package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.SharedPreferencesUtils;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.GetMyReceivedOrdersService;
import com.gyr.repair.http.GetMySendedOrdersService;
import com.gyr.repair.http.LoginPostService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{
	
	private static final String tag="guan";
	
	private TextView toregistertv;
	private AutoCompleteTextView loginmobileet;
	private EditText loginpasswordet;
	private Button loginbt;
	
	private String loginmobile, loginpassword;
	
	private Handler loginHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;
	
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		toregistertv = (TextView) findViewById(R.id.tv_toregister);
		toregistertv.setOnClickListener(this);
		loginbt = (Button) findViewById(R.id.bt_login);
		loginbt.setOnClickListener(this);
		
		loginmobileet = (AutoCompleteTextView) findViewById(R.id.et_loginmobile);
		loginpasswordet = (EditText) findViewById(R.id.et_loginpassword);
		
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
        
        loginmobileet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(11)});
        loginpasswordet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(15)});

        loginHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				getUserInfom();
    				Log.i("guan", UserCache.getMobile());
    				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    				ResponseCache.cacheFlag = true;
            		startActivity(intent);
            		LoginActivity.this.finish();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(LoginActivity.this, "登录失败，请检查账号和密码", Toast.LENGTH_SHORT).show();
    				break;
                }
                /*
                progressDialog.dismiss();
                if (msg.what == 1) {  // 处理发送线程传回的消息
                    if(msg.obj.toString().equals("SUCCEEDED")){
                        //跳转
                    	u.setUser();
                    	u.setMobile(loginmobile);
                    	//Log.i(tag, u.mobile);
                    	Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    	startActivity(intent);
                		LoginActivity.this.finish();
                        //Toast.makeText(LoginActivity.this, "模拟跳转", Toast.LENGTH_SHORT).show();
                    	
                    }else{
                        Toast.makeText(LoginActivity.this, "登录失败，请检查账号和密码", Toast.LENGTH_SHORT).show();
                    }
                }
                */
            }
        };
	}
	
	private void getUserInfom() {
		try {
			JSONArray jsonArray = new JSONArray(ResponseCache.responseUser);
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			UserCache.setMobile(jsonObject.getString("mobile"));
			SharedPreferencesUtils.setParam(this, "mobile", jsonObject.getString("mobile"));
			UserCache.setPassword(jsonObject.getString("password"));
			SharedPreferencesUtils.setParam(this, "password", jsonObject.getString("password"));
			UserCache.setName(jsonObject.getString("name"));
			SharedPreferencesUtils.setParam(this, "name", jsonObject.getString("name"));
			if (jsonObject.has("expert")) {
				UserCache.setEngineer();
				SharedPreferencesUtils.setParam(this, "type", 2);
				UserCache.setId(jsonObject.getString("idengineer"));
				SharedPreferencesUtils.setParam(this, "id", jsonObject.getString("idengineer"));
				UserCache.setExpert(jsonObject.getString("expert"));
				SharedPreferencesUtils.setParam(this, "expert", jsonObject.getString("expert"));
				UserCache.setCity(jsonObject.getString("city"));
				SharedPreferencesUtils.setParam(this, "city", jsonObject.getString("city"));
				UserCache.setDistrict(jsonObject.getString("district"));
				SharedPreferencesUtils.setParam(this, "district", jsonObject.getString("district"));
				UserCache.setAddress(jsonObject.getString("address"));
				SharedPreferencesUtils.setParam(this, "address", jsonObject.getString("address"));
				UserCache.setScore(jsonObject.getString("score"));
				SharedPreferencesUtils.setParam(this, "score", jsonObject.getString("score"));
				UserCache.setOrdernumber(jsonObject.getString("ordernumber"));
				SharedPreferencesUtils.setParam(this, "ordernumber", jsonObject.getString("ordernumber"));
			} else {
				UserCache.setUser();
				SharedPreferencesUtils.setParam(this, "type", 1);
				UserCache.setId(jsonObject.getString("iduser"));
				SharedPreferencesUtils.setParam(this, "id", jsonObject.getString("iduser"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_toregister:
			clickToRegister();
			break;
		case R.id.bt_login:
			attemptLogin();
			break;
		}
	}

	private void clickToRegister() {
		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(intent);
		LoginActivity.this.finish();
	}
	
    private void attemptLogin() {
    	loginmobileet.setError(null);
    	loginpasswordet.setError(null);
    	
        loginmobile = loginmobileet.getText().toString().trim();
        loginpassword = loginpasswordet.getText().toString().trim();
        
        boolean cancel = false;
        View focusView = null;
        
        if (TextUtils.isEmpty(loginpassword) || !isPasswordValid(loginpassword)) {
        	loginpasswordet.setError("密码过短");
            focusView = loginpasswordet;
            cancel = true;
        }
        
        if (TextUtils.isEmpty(loginmobile)) {
            loginmobileet.setError("必须填");
            focusView = loginmobileet;
            cancel = true;
        } else if (!isMobileValid(loginmobile)) {
        	loginmobileet.setError("手机号错误");
            focusView = loginmobileet;
            cancel = true;
        }
        
        if (cancel) {
            focusView.requestFocus();
        } else {
        	//Log.i(tag, "success");
        	login();
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
    
    private void login(){
    	if (isConnectingToInternet()) {
    		progressDialog = ProgressDialog.show(this, "请稍候", "正在登陆...", true);
    		LoginPostThread loginThread = new LoginPostThread(loginmobile, loginpassword);
    		loginThread.start();
    		timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, loginHandler);
				}
				
			}, 10000);
    	} else {
    		Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public class LoginPostThread extends Thread {
    	public String mobile, password;
    	
    	public LoginPostThread(String mobile, String password) {
    		this.mobile = mobile;
    		this.password = password;
    	}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
            if(!mobile.equals("")) {
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("mobile", mobile));
                params1.add(new BasicNameValuePair("password", password));
                ResponseCache.responseUser = LoginPostService.send(params1);
                Log.i(tag, "LoginActivity: responseUser = " + ResponseCache.responseUser);
                if (ResponseCache.responseUser.equals("FAILED")) {
                	sendMsg(FAILED, loginHandler);
                } else {
                	sendMsg(SUCCEEDED, loginHandler);
                }
            }
		}
		
    }
    
    public boolean isConnectingToInternet() {
    	ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            @SuppressWarnings("deprecation")
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

