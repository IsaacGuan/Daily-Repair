package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class GetMySendedOrdersService {

	public static String send(List<NameValuePair> params) {
		String servlet = "GetMySendedOrdersServlet";
		String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "GetMySendedOrdersService: responseMsg = " + responseMsg);
        return responseMsg;
	}
	
}
