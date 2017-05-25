package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class SendOrderPostService {
    public static String send(List<NameValuePair> params) {
        String servlet = "SendOrderServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "SendOrderService: responseMsg = " + responseMsg);
        return responseMsg;
    }
}
