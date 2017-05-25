package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class ModifyPasswordService {
	public static String send(List<NameValuePair> params) {
		String servlet = "ModifyPasswordServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "ModifyPasswordService: responseMsg = " + responseMsg);
        return responseMsg;
	}

}
