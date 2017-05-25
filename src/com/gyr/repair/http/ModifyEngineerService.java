package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class ModifyEngineerService {
	public static String send(List<NameValuePair> params) {
		String servlet = "ModifyEngineerServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "ModifyEngineerService: responseMsg = " + responseMsg);
        return responseMsg;
	}

}
