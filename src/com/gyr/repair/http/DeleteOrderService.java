package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class DeleteOrderService {
	public static String send(List<NameValuePair> params) {
		String servlet = "DeleteOrderServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "DeleteOrderService: responseMsg = " + responseMsg);
        return responseMsg;
	}

}
