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
import com.gyr.repair.http.ModifyEngineerService;

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

public class ModifyEngineerActivity extends Activity implements OnClickListener, OnGetGeoCoderResultListener {
	
	//private Spinner modifycitysp, modifydistrictsp, modifyexpertsp;
	private Spinner modifyexpertsp;
	private TextView modifycitytv;
	private EditText modifyaddresset;
	private Button modifyengineerbt;
	
	private String modifyexpert, modifyaddress;
	private String modifyprovince, modifycity, modifydistrict;
	private String modifylatitude = "0";
	private String modifylongitude = "0";
	
	private GeoCoder mSearch = null;
	
	// 省数据集合
    private ArrayList<String> mListProvince = new ArrayList<String>();
    // 市数据集合
    private ArrayList<ArrayList<String>> mListCiry = new ArrayList<ArrayList<String>>();
    // 区数据集合
    private ArrayList<ArrayList<ArrayList<String>>> mListArea = new ArrayList<ArrayList<ArrayList<String>>>();
    
    private OptionsPickerView<String> mOpv;
    private JSONObject mJsonObj;
	
	private Handler modifyHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifyengineer);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		modifycity = bundle.getString("modifycity");
		modifydistrict = bundle.getString("modifydistrict");
		modifyexpert = bundle.getString("modifyexpert");
		modifyaddress = bundle.getString("modifyaddress");
		
		/*
		modifycitysp = (Spinner) findViewById(R.id.sp_modifycity);
		selectValue(modifycitysp, modifycity);
		modifydistrictsp = (Spinner) findViewById(R.id.sp_modifydistrict);
		selectValue(modifydistrictsp, modifydistrict);
		*/
		modifyexpertsp = (Spinner) findViewById(R.id.sp_modifyexpert);
		selectValue(modifyexpertsp, modifyexpert);
		modifyaddresset = (EditText) findViewById(R.id.et_modifyaddress);
		modifyaddresset.setText(modifyaddress);
		
		modifyengineerbt = (Button) findViewById(R.id.bt_modifyengineer);
		modifyengineerbt.setOnClickListener(this);
		
		modifycitytv = (TextView) findViewById(R.id.tv_modifycity);
		modifycitytv.setText(modifycity + "  " + modifydistrict);
		
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
        
        modifyaddresset.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(50)});
		
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
            	modifyprovince = mListProvince.get(options1);
            	modifycity = mListCiry.get(options1).get(option2) + "市";
                modifydistrict = mListArea.get(options1).get(option2).get(options3);
                //String tx = mListProvince.get(options1) + " " + mListCiry.get(options1).get(option2) + " " + mListArea.get(options1).get(option2).get(options3);
                modifycitytv.setText(modifycity + "  " + modifydistrict);
            }
        });
        
        modifycitytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpv.show();
            }
        });
		
		mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        
        modifyHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				changeEngineerInfrom();
    				Toast.makeText(getApplicationContext(), "修改成功！", Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent(ModifyEngineerActivity.this, MainActivity.class);
            		startActivity(intent);
            		ModifyEngineerActivity.this.finish();
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
	
	private void changeEngineerInfrom() {
		UserCache.setExpert(modifyexpert);
		UserCache.setCity(modifycity);
		UserCache.setDistrict(modifydistrict);
		UserCache.setAddress(modifyaddress);
		
	}
	
	private void selectValue(Spinner spinner, Object value) {
	    for (int i = 0; i < spinner.getCount(); i++) {
	        if (spinner.getItemAtPosition(i).equals(value)) {
	            spinner.setSelection(i);
	            break;
	        }
	    }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_modifyengineer:
			attemptModifyEngineer();
			break;
		}
		
	}

	private void attemptModifyEngineer() {
		modifyaddresset.setError(null);
		
		modifyaddress = modifyaddresset.getText().toString().trim();
		//modifycity = modifycitysp.getSelectedItem().toString();
		//modifydistrict = modifydistrictsp.getSelectedItem().toString();
		modifyexpert = modifyexpertsp.getSelectedItem().toString().trim();
		
		boolean cancel = false;
		View focusView = null;
		
		if (TextUtils.isEmpty(modifyaddress)) {
			modifyaddresset.setError("必须填");
			focusView = modifyaddresset;
			cancel = true;
		}
		
		if (modifycitytv.getText().equals("点击选择城市区县")) {
			Toast.makeText(getApplicationContext(), "请选择城市区县", Toast.LENGTH_SHORT).show();
			focusView = modifycitytv;
			cancel = true;
		}
		
		if (cancel) {
			focusView.requestFocus();
		} else {
			Log.i("guan", "success");
			modifyEngineerConfirm();
		}
		
	}
	
	private void modifyEngineerConfirm() {
		new AlertDialog.Builder(this).setTitle("确认修改？")
		.setMessage("请确认填写信息正确")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				modifyEngineer();
				
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		}).show();
	}

	private void modifyEngineer() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, "请稍候", "正在提交...",
					true);
			mSearch.geocode(new GeoCodeOption().city(modifycity).address(
					modifydistrict + modifyaddress));
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					sendMsg(FAILED, modifyHandler);
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
			modifyaddresset.setError("地址信息无效，请正确填写！");
			//Toast.makeText(ApplyEngineerActivity.this, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.i("guan", "ApplyEngineerActivity: " + result);
		modifylatitude = String.valueOf(result.getLocation().latitude);
		modifylongitude = String.valueOf(result.getLocation().longitude);
		
		ModifyEngineerPostThread modifyEngineerPostThread = new ModifyEngineerPostThread(
				UserCache.getId(), modifyexpert, modifycity, modifydistrict, modifyaddress,
				modifylatitude, modifylongitude);
		modifyEngineerPostThread.start();
		
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public class ModifyEngineerPostThread extends Thread {
		public String idengineer, expert, city, district, address, latitude, longitude;

		public ModifyEngineerPostThread(String idengineer, String expert,
				String city, String district, String address, String latitude,
				String longitude) {
			super();
			this.idengineer = idengineer;
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
            params.add(new BasicNameValuePair("idengineer", idengineer));
            params.add(new BasicNameValuePair("expert", expert));
            params.add(new BasicNameValuePair("city", city));
            params.add(new BasicNameValuePair("district", district));
            params.add(new BasicNameValuePair("address", address));
            params.add(new BasicNameValuePair("latitude", latitude));
            params.add(new BasicNameValuePair("longitude", longitude));
            responseMsg = ModifyEngineerService.send(params);
            Log.i("tag", "ModifyEngineerActivity: responseMsg = " + responseMsg);
            if (responseMsg.equals("FAILED")) {
            	sendMsg(FAILED, modifyHandler);
            } else {
            	sendMsg(SUCCEEDED, modifyHandler);
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
