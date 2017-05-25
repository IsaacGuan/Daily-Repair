package com.gyr.repair.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.ParseException;

import com.gyr.repair.R;
import com.gyr.repair.cache.UserCache;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class VerifyIdentityActivity extends Activity implements OnClickListener{
	
	private ImageView idfrontiv, idbackiv;
	private Bitmap idfrontbt, idbackbt;
	private EditText verifynameet, verifyidet;
	private Button verifybt;
	
	private String verifyname, verifyid;
	
	private Handler verifyHandler;
	private Timer timer;
	private ProgressDialog progressDialog;
	
	private static final int REQUEST_PICK_IDFRONT = 1;
	private static final int REQUEST_PICK_IDBACK = 2;
	
	private int flag_idfront = 0;
	private int flag_idback = 0;
	
	private static final int SUCCEEDED = 1;
	private static final int FAILED = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verifyidentity);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		verifynameet = (EditText) findViewById(R.id.et_verifyname);
		verifyidet = (EditText) findViewById(R.id.et_verifyid);
		idfrontiv = (ImageView) findViewById(R.id.iv_idfront);
		idfrontiv.setOnClickListener(this);
		idbackiv = (ImageView) findViewById(R.id.iv_idback);
		idbackiv.setOnClickListener(this);
		verifybt = (Button) findViewById(R.id.bt_verify);
		verifybt.setOnClickListener(this);
		
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
        
        verifynameet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
        verifyidet.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(18)});
		
		verifyHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
				case SUCCEEDED:
					timer.cancel();
    				progressDialog.dismiss();
    				Intent intent = new Intent(VerifyIdentityActivity.this, ApplyEngineerActivity.class);
    				startActivity(intent);
    				break;
				case FAILED:
					timer.cancel();
    				progressDialog.dismiss();
					break;
				}
			}
			
		};
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (data == null) {
			return;
		}
		
		Uri uri = data.getData();
		InputStream is = null;
		
		if (requestCode == REQUEST_PICK_IDFRONT
				&& resultCode == Activity.RESULT_OK) {			
			if (uri != null) {				
				try {
					is = getContentResolver().openInputStream(uri);
					idfrontbt = BitmapFactory.decodeStream(is);
					idfrontiv.setImageBitmap(idfrontbt);
					flag_idfront = 1;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		if (requestCode == REQUEST_PICK_IDBACK
				&& resultCode == Activity.RESULT_OK){
			if (uri != null) {
				try {
					is = getContentResolver().openInputStream(uri);
					idbackbt = BitmapFactory.decodeStream(is);
					idbackiv.setImageBitmap(idbackbt);
					flag_idback = 1;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.iv_idfront:
			clickIdFront();
			break;
		case R.id.iv_idback:
			clickIdBack();
			break;
		case R.id.bt_verify:
			if (UserCache.isUser()) {
				attemptVerify();
			} else if (UserCache.isGuest()) {
				Toast.makeText(getApplicationContext(), "游客请登录", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private void clickIdFront() {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_PICK_IDFRONT);
		
	}

	private void clickIdBack() {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_PICK_IDBACK);
		
	}
	
	private void attemptVerify() {
		verifynameet.setError(null);
		verifyidet.setError(null);
		
		verifyname = verifynameet.getText().toString().trim();
		verifyid = verifyidet.getText().toString().trim();
		
		boolean cancel = false;
        View focusView = null;
        
        String errorid = idValidate(verifyid);
        
        if (TextUtils.isEmpty(verifyname)) {
        	verifynameet.setError("必须填");
        	focusView = verifynameet;
        	cancel = true;
        }
        if (!errorid.equals("")) {
        	verifyidet.setError(errorid);
        	focusView = verifyidet;
        	cancel = true;
        }
        if (flag_idback == 0 || flag_idfront == 0) {
        	Toast.makeText(getApplicationContext(), "请上传身份证照片", Toast.LENGTH_SHORT).show();
        	focusView = verifynameet;
        	cancel = true;
        }
        
        if (cancel) {
            focusView.requestFocus();
        } else {
        	verify();
        }
		
	}
	
	private void verify() {
		progressDialog = ProgressDialog.show(this, "请稍候", "正在提交...", true);
		timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				sendMsg(SUCCEEDED, verifyHandler);
				
			}
			
		}, 1000);
	}
	
	private void sendMsg(int i, Handler handler){
		Message msg = new Message();
		msg.what = i;
		handler.sendMessage(msg);
	}
	
    private static String idValidate(String IDStr) throws ParseException {
        String errorInfo = "";// 记录错误信息
        String[] ValCodeArr = { "1", "0", "x", "9", "8", "7", "6", "5", "4",
                "3", "2" };
        String[] Wi = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7",
                "9", "10", "5", "8", "4", "2" };
        String Ai = "";
        
        if (IDStr.length() != 15 && IDStr.length() != 18) {
            errorInfo = "身份证号码长度应该为15位或18位";
            return errorInfo;
        }
        
        if (IDStr.length() == 18) {
            Ai = IDStr.substring(0, 17);
        } else if (IDStr.length() == 15) {
            Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
        }
        if (isNumeric(Ai) == false) {
            errorInfo = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字";
            return errorInfo;
        }
        
        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 月份
        if (isDate(strYear + "-" + strMonth + "-" + strDay) == false) {
            errorInfo = "身份证生日无效";
            return errorInfo;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                    || (gc.getTime().getTime() - s.parse(
                            strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                errorInfo = "身份证生日不在有效范围";
                return errorInfo;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            errorInfo = "身份证月份无效";
            return errorInfo;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            errorInfo = "身份证日期无效";
            return errorInfo;
        }
        
        Hashtable h = GetAreaCode();
        if (h.get(Ai.substring(0, 2)) == null) {
            errorInfo = "身份证地区编码错误";
            return errorInfo;
        }
        
        int TotalmulAiWi = 0;
        for (int i = 0; i < 17; i++) {
            TotalmulAiWi = TotalmulAiWi
                    + Integer.parseInt(String.valueOf(Ai.charAt(i)))
                    * Integer.parseInt(Wi[i]);
        }
        int modValue = TotalmulAiWi % 11;
        String strVerifyCode = ValCodeArr[modValue];
        Ai = Ai + strVerifyCode;

        if (IDStr.length() == 18) {
            if (Ai.equals(IDStr) == false) {
                errorInfo = "身份证无效，不是合法的身份证号码";
                return errorInfo;
            }
        } else {
            return "";
        }
        return "";
    }
    
    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }
    
    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDate(String strDate) {
        Pattern pattern = Pattern
                .compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
        Matcher m = pattern.matcher(strDate);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }
    
}
