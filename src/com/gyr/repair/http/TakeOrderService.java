package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class TakeOrderService {
	public static String send(List<NameValuePair> params) {
        String servlet = "TakeOrderServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "TakeOrderService: responseMsg = " + responseMsg);
        return responseMsg;
    }

}
