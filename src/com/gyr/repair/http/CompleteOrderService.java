package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class CompleteOrderService {
	public static String send(List<NameValuePair> params) {
		String servlet = "CompleteOrderServlet";
		String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "CompleteOrderService: responseMsg = " + responseMsg);
        return responseMsg;
	}
}
