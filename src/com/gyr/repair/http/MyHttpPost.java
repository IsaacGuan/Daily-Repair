package com.gyr.repair.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

@SuppressWarnings("deprecation")
public class MyHttpPost {
    // 服务器地址
    private static String SERVER = "http://120.24.82.119:8080";     //120.24.82.119 192.168.137.1 192.168.1.8 192.168.1.108
    // 项目地址
    private static String PROJECT = "/repairWeb/";
    // 请求超时
    private static final int REQUEST_TIMEOUT = 0;
    // 读取超时
    private static final int SO_TIMEOUT = 0;

    // 通过 POST 方式获取HTTP服务器数据
    public static String executeHttpPost(String servlet, List<NameValuePair> params) {
        String baseURL = SERVER + PROJECT + servlet;
        String responseMsg = "FAILED";
        try {
            //连接到服务器端相应的Servlet
            HttpPost request = new HttpPost(baseURL);
            request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode()==200) { //是否成功收取信息
            	responseMsg = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseMsg;
    }
}
