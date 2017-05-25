package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class GetMyReceivedOrdersService {

	public static String send(List<NameValuePair> params) {
		String servlet = "GetMyReceivedOrdersServlet";
		String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "GetMyReceivedOrdersService: responseMsg = " + responseMsg);
        return responseMsg;
	}
	
}
