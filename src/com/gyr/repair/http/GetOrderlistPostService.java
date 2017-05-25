package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class GetOrderlistPostService {
	
	public static String send(List<NameValuePair> params) {
		String servlet = "GetOrderlistServlet";
		String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "GetOrderlistService: responseMsg = " + responseMsg);
        return responseMsg;
	}
	
}
