package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class LoginPostService {
    
	public static String send(List<NameValuePair> params) {
		String servlet = "LoginServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "LoginService: responseMsg = " + responseMsg);
        return responseMsg;
    }
	
}
