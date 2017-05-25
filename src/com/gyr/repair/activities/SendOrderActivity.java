package com.gyr.repair.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bigkoo.pickerview.OptionsPickerView;
import com.gyr.repair.R;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.cache.UserCache;
import com.gyr.repair.http.RegisterPostService;
import com.gyr.repair.http.SendOrderPostService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SendOrderActivity extends Activity implements OnClickListener, OnGetGeoCoderResultListener {
	
	private static final String tag="guan";
	
	private TextView senddatetv, sendcitytv;
	private EditText sendtitleet, sendbudgetet, senddetailet, sendaddresset;
	//private Spinner sendcitysp, senddistrictsp;
	private Button sendorderbt;
	
	private String sendtitle, sendbudget, senddate, senddetail;
	private String sendprovince, sendcity, senddistrict, sendaddress;
	private String sendlatitude = "0";
	private String sendlongitude = "0";
	
	private GeoCoder mSearch = null;
	
	private int flag;
	private String nameengineer;
	private String mobileengineer;
	private String status;
	
	// 省数据集合
    private ArrayList<String> mListProvince = new ArrayList<String>();
    // 市数据集合
    private ArrayList<ArrayList<String>> mListCiry = new ArrayList<ArrayList<String>>();
    // 区数据集合
    private ArrayList<ArrayList<ArrayList<String>>> mListArea = new ArrayList<ArrayList<ArrayList<String>>>();
    
    private OptionsPickerView<String> mOpv;
    private JSONObject mJsonObj;
	
	private Handler sendorderHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sendorder);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		senddatetv = (TextView) findViewById(R.id.tv_senddateconfigue);
		senddatetv.setOnClickListener(this);
		sendorderbt = (Button) findViewById(R.id.bt_sendorder);
		sendorderbt.setOnClickListener(this);
		
		sendtitleet = (EditText) findViewById(R.id.et_sendtitle);
		sendbudgetet = (EditText) findViewById(R.id.et_sendbudget);
		senddetailet = (EditText) findViewById(R.id.et_senddetail);
		sendaddresset = (EditText) findViewById(R.id.et_sendaddress);
		
		//sendcitysp = (Spinner) findViewById(R.id.sp_sendcity);
		//senddistrictsp = (Spinner) findViewById(R.id.sp_senddistrict);
		
		sendcitytv = (TextView) findViewById(R.id.tv_sendcity);
		
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
        
        sendtitleet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(10)});
        sendbudgetet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(10)});
        sendaddresset.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(50)});
        
        Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		flag = bundle.getInt("flag");
		nameengineer = "";
		mobileengineer = "";
		status = "unreceived";
		if (flag == 2) {
			nameengineer = bundle.getString("engineername");
			mobileengineer = bundle.getString("engineermobile");
			status = "received";
		} else if (flag == 3) {
			sendtitle = bundle.getString("sendtitle");
			sendbudget = Pattern.compile("[^0-9]").matcher(bundle.getString("sendbudget")).replaceAll("");
			senddetail = bundle.getString("senddetail");
			sendcity = bundle.getString("sendcity");
			senddistrict = bundle.getString("senddistrict");
			sendaddress = bundle.getString("sendaddress");
			sendtitleet.setText(sendtitle);
			sendbudgetet.setText(sendbudget);
			senddetailet.setText(senddetail);
			sendcitytv.setText(sendcity + "  " + senddistrict);
			sendaddresset.setText(sendaddress);
		}
		
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
            	sendprovince = mListProvince.get(options1);
            	sendcity = mListCiry.get(options1).get(option2) + "市";
                senddistrict = mListArea.get(options1).get(option2).get(options3);
                //String tx = mListProvince.get(options1) + " " + mListCiry.get(options1).get(option2) + " " + mListArea.get(options1).get(option2).get(options3);
                sendcitytv.setText(sendcity + "  " + senddistrict);
            }
        });
        
        sendcitytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpv.show();
            }
        });
		
		mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
		
		sendorderHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
                case SUCCEEDED:
    				timer.cancel();
    				progressDialog.dismiss();
    				Toast.makeText(SendOrderActivity.this, "发活儿成功！", Toast.LENGTH_SHORT).show();
    				Intent intent = new Intent(SendOrderActivity.this, MainActivity.class);
    				ResponseCache.cacheFlag = true;
            		startActivity(intent);
    				break;
                case FAILED:
                	Thread.interrupted();
                	timer.cancel();
                	progressDialog.dismiss();
                	Toast.makeText(SendOrderActivity.this, "发活儿失败", Toast.LENGTH_SHORT).show();
    				break;
                }
			}
			
		};
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_senddateconfigue:
			dateConfigue();
			break;
		case R.id.bt_sendorder:
			if(!UserCache.isGuest()){
				attemptSendOrder();
			} else {
				Toast.makeText(getApplicationContext(), "游客请登录", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
	
	private void dateConfigue() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, 7);
		DatePickerDialog mDatePickerDialog = new DatePickerDialog (SendOrderActivity.this,
				new DatePickerDialog.OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						senddatetv.setText(year + "/" + (monthOfYear+1) + "/" + dayOfMonth);
						
					}
				}
		, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		long minDate = c.getTimeInMillis();
		mDatePickerDialog.getDatePicker().setMinDate(minDate);
		mDatePickerDialog.show();
		
	}
	
	private void attemptSendOrder() {
		sendtitleet.setError(null);
		sendbudgetet.setError(null);
		senddetailet.setError(null);
		sendaddresset.setError(null);
		
		sendtitle = sendtitleet.getText().toString();
		sendbudget = sendbudgetet.getText().toString();
		senddetail = senddetailet.getText().toString();
		sendaddress = sendaddresset.getText().toString();
		senddate = senddatetv.getText().toString();
		
		boolean cancel = false;
		View focusView = null;
		
		if (TextUtils.isEmpty(sendtitle)) {
			sendtitleet.setError("必须填");
			focusView = sendtitleet;
			cancel = true;
		}
		
		if (TextUtils.isEmpty(sendbudget)) {
			sendbudgetet.setError("必须填");
			focusView = sendbudgetet;
			cancel = true;
		} else if (!isNumeric(sendbudget)) {
			sendbudgetet.setError("必须填数字");
			focusView = sendbudgetet;
			cancel = true;
		}
		
		if (TextUtils.isEmpty(senddetail)) {
			senddetailet.setError("必须填");
			focusView = senddetailet;
			cancel = true;
		}
		
		if (TextUtils.isEmpty(sendaddress)) {
			sendaddresset.setError("必须填");
			focusView = sendaddresset;
			cancel = true;
		}
		
		if (senddate.equals("点击设置日期")) {
			Toast.makeText(getApplicationContext(), "请设置截止日期", Toast.LENGTH_SHORT).show();
			focusView = senddatetv;
			cancel = true;
		}
		
		if (sendcitytv.getText().equals("点击选择城市区县")) {
			Toast.makeText(getApplicationContext(), "请选择城市区县", Toast.LENGTH_SHORT).show();
			focusView = sendcitytv;
			cancel = true;
		}
		
		if (cancel) {
			focusView.requestFocus();
		} else {
			Log.i(tag, "success");
			sendOrderConfirm();//sendOrder();
		}
		
	}
	
	public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
        
	}
	
	private void sendOrderConfirm() {
		new AlertDialog.Builder(this).setTitle("确认发活儿？")
		.setMessage("请确认填写信息正确")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendOrder();
				
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		}).show();

	}
	
	private void sendOrder() {
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, "请稍候", "正在发活儿...", true);
			mSearch.geocode(new GeoCodeOption().city(sendcity).address(senddistrict + sendaddress));
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, sendorderHandler);
				}
				
			}, 10000);
    	} else {
    		Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
	}
	
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			sendaddresset.setError("地址信息无效，请正确填写！");
			//Toast.makeText(ApplyEngineerActivity.this, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Log.i("guan", "SendOrderActivity: " + result);
		sendlatitude = String.valueOf(result.getLocation().latitude);
		sendlongitude = String.valueOf(result.getLocation().longitude);
		
		SendOrderPostThread sendorderPostThread = new SendOrderPostThread(
				sendtitle, UserCache.getName(), UserCache.getMobile(),
				nameengineer, mobileengineer, sendbudget, senddate,
				sendcity, senddistrict, sendaddress, sendlatitude,
				sendlongitude, senddetail, status);
		sendorderPostThread.start();
		
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		
	}
	
	public class SendOrderPostThread extends Thread {
		public String title, nameuser, mobileuser, nameengineer,
				mobileengineer, budget, date, city, district, address,
				latitude, longitude, detail, status;
		
		public SendOrderPostThread(String title, String nameuser,
				String mobileuser, String nameengineer, String mobileengineer,
				String budget, String date, String city, String district,
				String address, String latitude, String longitude,
				String detail, String status) {
			super();
			this.title = title;
			this.nameuser = nameuser;
			this.mobileuser = mobileuser;
			this.nameengineer = nameengineer;
			this.mobileengineer = mobileengineer;
			this.budget = budget;
			this.date = date;
			this.city = city;
			this.district = district;
			this.address = address;
			this.latitude = latitude;
			this.longitude = longitude;
			this.detail = detail;
			this.status = status;
		}
		
		@Override
		public void run() {
			String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("title", title));
			params.add(new BasicNameValuePair("nameuser", nameuser));
            params.add(new BasicNameValuePair("mobileuser", mobileuser));
            params.add(new BasicNameValuePair("nameengineer", nameengineer));
            params.add(new BasicNameValuePair("mobileengineer", mobileengineer));
            params.add(new BasicNameValuePair("budget", budget));
            params.add(new BasicNameValuePair("date", date));
            params.add(new BasicNameValuePair("city", city));
            params.add(new BasicNameValuePair("district", district));
            params.add(new BasicNameValuePair("address", address));
            params.add(new BasicNameValuePair("latitude", latitude));
            params.add(new BasicNameValuePair("longitude", longitude));
            params.add(new BasicNameValuePair("detail", detail));
            params.add(new BasicNameValuePair("status", status));
            responseMsg = SendOrderPostService.send(params);
            Log.i("tag", "SendOrderActivity: responseMsg = " + responseMsg);
            if (responseMsg.equals("FAILED")) {
            	sendMsg(FAILED, sendorderHandler);
            } else {
            	sendMsg(SUCCEEDED, sendorderHandler);
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
