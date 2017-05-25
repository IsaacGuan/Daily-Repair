package com.gyr.repair.http;

import java.util.List;

import org.apache.http.NameValuePair;

import android.util.Log;

public class RegisterPostService {

    public static String send(List<NameValuePair> params) {
        String servlet = "RegisterServlet";
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("guan", "RegisterService: responseMsg = " + responseMsg);
        return responseMsg;
    }
}
