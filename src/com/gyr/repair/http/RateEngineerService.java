package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class RateEngineerService {
	public static String send(List<NameValuePair> params) {
		String servlet = "RateEngineerServlet";
		String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "RateEngineerService: responseMsg = " + responseMsg);
        return responseMsg;
	}
}
