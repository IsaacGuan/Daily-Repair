package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class ExpireOrderService {
	
	public static String send(List<NameValuePair> params) {
		String servlet = "ExpireOrderServlet";
		String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "ExpireOrderService: responseMsg = " + responseMsg);
        return responseMsg;
	}
	
}
