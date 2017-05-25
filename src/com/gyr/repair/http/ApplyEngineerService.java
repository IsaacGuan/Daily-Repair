package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class ApplyEngineerService {
	
	public static String send(List<NameValuePair> params) {
		String servlet = "ApplyEngineerServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "ApplyEngineerService: responseMsg = " + responseMsg);
        return responseMsg;
	}

}
