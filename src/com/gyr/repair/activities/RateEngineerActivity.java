package com.gyr.repair.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.gyr.repair.R;
import com.gyr.repair.R.layout;
import com.gyr.repair.cache.ResponseCache;
import com.gyr.repair.http.CompleteOrderService;
import com.gyr.repair.http.RateEngineerService;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class RateEngineerActivity extends Activity implements OnClickListener {
	private TextView rateengineernametv, rateengineermobiletv;
	private RatingBar rateengineerrtb;
	private Button confirmcompletebt;
	
	private String idorder, nameengineer, mobileengineer, idengineer, score;
	
	private Handler rateengineerHandler;
	private Timer timer;
	private ProgressDialog progressDialog;

	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rateengineer);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		idorder = bundle.getString("idorder");
		nameengineer = bundle.getString("nameengineer");
		mobileengineer = bundle.getString("mobileengineer");
		
		idengineer = "e" + mobileengineer;
		
		rateengineernametv = (TextView)findViewById(R.id.tv_rateengineername2);
		rateengineernametv.setText(nameengineer);
		rateengineermobiletv = (TextView)findViewById(R.id.tv_rateengineermobile2);
		rateengineermobiletv.setText(mobileengineer);
		
		rateengineerrtb = (RatingBar)findViewById(R.id.rtb_rateengineer);
		
		confirmcompletebt = (Button)findViewById(R.id.bt_confirmcomplete);
		confirmcompletebt.setOnClickListener(this);
		
		rateengineerHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCEEDED:
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "完成！",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(RateEngineerActivity.this,
							MainActivity.class);
					ResponseCache.cacheFlag = true;
					startActivity(intent);
					break;
				case FAILED:
					Thread.interrupted();
					timer.cancel();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "失败",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
		};
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_confirmcomplete:
			new AlertDialog.Builder(this).setTitle("确认完成？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					clickConfirmComplete();
					
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).show();
			//clickConfirmComplete();
			break;
		}
		
	}

	private void clickConfirmComplete() {
		score = String.valueOf(rateengineerrtb.getRating());
		if (isConnectingToInternet()) {
			progressDialog = ProgressDialog.show(this, null, null, true);
			RateEngineerPostThread rateEngineerPostThread = new RateEngineerPostThread(idorder, idengineer, score);
			rateEngineerPostThread.start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendMsg(FAILED, rateengineerHandler);
				}
				
			}, 10000);
		} else {
    		Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT).show();
    	}
		
	}
	
	private class RateEngineerPostThread extends Thread {
		private String idorder, idengineer, score;
		
		public RateEngineerPostThread(String idorder, String idengineer,
				String score) {
			super();
			this.idorder = idorder;
			this.idengineer = idengineer;
			this.score = score;
		}

		@Override
		public void run() {
			String responseMsg;
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			List<NameValuePair> params2 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("idorder", idorder));
			params2.add(new BasicNameValuePair("idengineer", idengineer));
			params2.add(new BasicNameValuePair("score", score));
			responseMsg = CompleteOrderService.send(params1);
			if (!responseMsg.equals("FAILED")) {
				responseMsg = RateEngineerService.send(params2);
			}
			if (responseMsg.equals("FAILED")) {
				sendMsg(FAILED, rateengineerHandler);
			} else {
				sendMsg(SUCCEEDED, rateengineerHandler);
			}
		}
		
	}
	
    private boolean isConnectingToInternet() {
    	ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
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
