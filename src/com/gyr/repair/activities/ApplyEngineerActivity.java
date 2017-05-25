package com.gyr.repair.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bigkoo.pickerview.OptionsPickerView;
import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.ApplyEngineerService;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ApplyEngineerActivity extends Activity implements OnClickListener, OnGetGeoCoderResultListener{
	
	//private Spinner applycitysp, applydistrictsp, applyexpertsp;
	private Spinner applyexpertsp;
	private TextView applycitytv;
	private EditText applyaddresset;
	private Button applyengineerbt;
	
	private String applyexpert, applyaddress;
	private String applyprovince, applycity, applydistrict;
	private String applylatitude = "0";
	private String applylongitude = "0";
	
	private GeoCoder mSearch = null;
	
	// 省数据集合
    private ArrayList<String> mListProvince = new ArrayList<String>();
    // 市数据集合
    private ArrayList<ArrayList<String>> mListCiry = new ArrayList<ArrayList<String>>();
    // 区数据集合
    private ArrayList<ArrayList<ArrayList<String>>> mListArea = new ArrayList<ArrayList<ArrayList<String>>>();
    
    private OptionsPickerView<String> mOpv;
    private JSONObject mJsonObj;
	
	private Handler applyHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_applyengineer);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//applycitysp = (Spinner) findViewById(R.id.sp_applycity);
		//applydistrictsp = (Spinner) findViewById(R.id.sp_applydistrict);
		applyexpertsp = (Spinner) findViewById(R.id.sp_applyexpert);
		applyaddresset = (EditText) findViewById(R.id.et_applyaddress);
		
		applyengineerbt = (Button) findViewById(R.id.bt_applyengineer);
		applyengineerbt.setOnClickListener(this);
		
		applycitytv = (TextView) findViewById(R.id.tv_applycity);
		
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
        
        applyaddresset.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(50)});
		
		initJsonData();
        initJsonDatas();
        
        mOpv = new OptionsPickerView<String>(this);
        mOpv.setTitle("选择城市");
        mOpv.setPicker(mListProvince, mListCiry, mListArea, true);
        mOpv.setCyclic(false, false, false);
        mOpv.setSelectOptions(0, 0, 0);
        mOpv.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
            	applyprovince = mListProvince.get(options1);
            	applycity = mListCiry.get(options1).get(option2) + "市";
                applydistrict = mListArea.get(options1).get(option2).get(options3);
                //String tx = mListProvince.get(options1) + " " + mListCiry.get(options1).get(option2) + " " + mListArea.get(options1).get(option2).get(options3);
                applycitytv.setText(applycity + "  " + applydistrict);
            }
        });
        
        applycitytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpv.show();
            }
        });
		
		mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
		
		applyHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				changeUserInfrom();
    				Toast.makeText(getApplicationContext(), "提交成功！", Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent(ApplyEngineerActivity.this, MainActivity.class);
            		startActivity(intent);
            		ApplyEngineerActivity.this.finish();
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(getApplicationContext(), "提交失败", Toast.LENGTH_SHORT).show();
    				break;
				}
			}
			
		};
		
	}
	
	private void changeUserInfrom() {
		UserCache.setEngineer();
		UserCache.setId("e"+UserCache.getMobile());
		UserCache.setExpert(applyexpert);
		UserCache.setCity(applycity);
		UserCache.setDistrict(applydistrict);
		UserCache.setAddress(applyaddress);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_applyengineer:
			attemptApplyEngineer();
			break;
		}
		
	}

	private void attemptApplyEngineer() {
		applyaddresset.setError(null);
		
		applyaddress = applyaddresset.getText().toString().trim();
		//applycity = applycitysp.getSelectedItem().toString();
		//applydistrict = applydistrictsp.getSelectedItem().toString();
		applyexpert = applyexpertsp.getSelectedItem().toString().trim();
		
		boolean cancel = false;
		View focusView = null;
		
		if (TextUtils.isEmpty(applyaddress)) {
			applyaddresset.setError("必须填");
			focusView = applyaddresset;
			cancel = true;
		}
		
		if (applycitytv.getText().equals("点击选择城市区县")) {
			Toast.makeText(getApplicationContext(), "请选择城市区县", Toast.LENGTH_SHORT).show();
			focusView = applycitytv;
			cancel = true;
		}
		
		if (cancel) {
			focusView.requestFocus();
		} else {
			Log.i("guan", "success");
			applyEngineerConfirm();
		}
		
	}
	
	private void applyEngineerConfirm() {
		new AlertDialog.Builder(this).setTitle("确认申请？")
		.setMessage("请确认填写信息正确")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				applyEngineer();
				
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		}).show();
	}
	
	private void applyEngineer() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, "请稍候", "正在提交...",
					true);
			mSearch.geocode(new GeoCodeOption().city(applycity).address(
					applydistrict + applyaddress));
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMsg(FAILED, applyHandler);
				}

			}, 10000);
		} else {
			Toast.makeText(getApplicationContext(), "网络未连接",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			applyaddresset.setError("地址信息无效，请正确填写！");
			//Toast.makeText(ApplyEngineerActivity.this, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.i("guan", "ApplyEngineerActivity: " + result);
		applylatitude = String.valueOf(result.getLocation().latitude);
		applylongitude = String.valueOf(result.getLocation().longitude);
		
		ApplyEngineerPostThread applyEngineerPostThread = new ApplyEngineerPostThread(
				UserCache.getMobile(), UserCache.getPassword(),
				UserCache.getName(), applyexpert, applycity, applydistrict,
				applyaddress, applylatitude, applylongitude);
		applyEngineerPostThread.start();
		
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public class ApplyEngineerPostThread extends Thread {
		public String mobile, password, name, expert, city, district, address, latitude, longitude;
		
		public ApplyEngineerPostThread(String mobile, String password,
				String name, String expert, String city, String district,
				String address, String latitude, String longitude) {
			super();
			this.mobile = mobile;
			this.password = password;
			this.name = name;
			this.expert = expert;
			this.city = city;
			this.district = district;
			this.address = address;
			this.latitude = latitude;
			this.longitude = longitude;
		}

		@Override
		public void run() {
			String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("mobile", mobile));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("expert", expert));
            params.add(new BasicNameValuePair("city", city));
            params.add(new BasicNameValuePair("district", district));
            params.add(new BasicNameValuePair("address", address));
            params.add(new BasicNameValuePair("latitude", latitude));
            params.add(new BasicNameValuePair("longitude", longitude));
            responseMsg = ApplyEngineerService.send(params);
            Log.i("tag", "RegisterActivity: responseMsg = " + responseMsg);
            if (responseMsg.equals("FAILED")) {
            	sendMsg(FAILED, applyHandler);
            } else {
            	sendMsg(SUCCEEDED, applyHandler);
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
	
	private void initJsonData() {
		try {
			StringBuffer sb = new StringBuffer();
			InputStream is = getAssets().open("city.json");
			int len = -1;
			byte[] buf = new byte[1024];
			while ((len = is.read(buf)) != -1) {
				sb.append(new String(buf, 0, len, "GBK"));
			}
			is.close();
			mJsonObj = new JSONObject(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void initJsonDatas(){
		try {
			JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonP = jsonArray.getJSONObject(i);// 获取每个省的Json对象
				String province = jsonP.getString("name");
				
				ArrayList<String> options2Items_01 = new ArrayList<String>();
				ArrayList<ArrayList<String>> options3Items_01 = new ArrayList<ArrayList<String>>();
				JSONArray jsonCs = jsonP.getJSONArray("city");
				for (int j = 0; j < jsonCs.length(); j++) {
					JSONObject jsonC = jsonCs.getJSONObject(j);// 获取每个市的Json对象
					String city = jsonC.getString("name");
					options2Items_01.add(city);// 添加市数据
					
					ArrayList<String> options3Items_01_01 = new ArrayList<String>();
					JSONArray jsonAs = jsonC.getJSONArray("area");
					for (int k = 0; k < jsonAs.length(); k++) {
						options3Items_01_01.add(jsonAs.getString(k));// 添加区数据
					}
					options3Items_01.add(options3Items_01_01);
				}
				mListProvince.add(province);// 添加省数据
				mListCiry.add(options2Items_01);
				mListArea.add(options3Items_01);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mJsonObj = null;
	}
	
}
