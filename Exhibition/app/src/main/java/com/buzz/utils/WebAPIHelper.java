package com.buzz.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by NickChung on 7/22/15.
 */
public class WebAPIHelper {

    final static String API_PORT = "81";
    final static String API_HOST = "http://arts.things.buzz:" + API_PORT;
    final static String API_GET_APP_USER_URL = "http://arts.things.buzz:" + API_PORT + "/api/appuser/Getappuser/%s";
    final static String API_POST_APP_USER_URL = "http://arts.things.buzz:" + API_PORT + "/api/appuser/Postappuser";
    final static String API_PUT_APP_USER_URL = "http://arts.things.buzz:" + API_PORT + "/api/appuser/Putappuser/%s";
    final static String API_GET_APP_VERSION_URL = "http://arts.things.buzz:" + API_PORT + "/api/repo/appversion";
    final static String API_GET_DATA_VERSION_URL = "http://arts.things.buzz:" + API_PORT + "/api/repo/dataversion";
    final static String API_GET_BEACON_UUID_URL = "http://arts.things.buzz:" + API_PORT + "/api/repo/beaconuuid";
    final static String API_GET_CATALOG_URL = "http://arts.things.buzz:" + API_PORT + "/api/repo/catalog";
    final static String API_GET_EX_CONTENT_URL = "http://arts.things.buzz:" + API_PORT + "/api/repo/excontent/%s";
    final static String API_POST_SYS_LOG_URL = "http://arts.things.buzz:" + API_PORT + "/api/syslog/Postsyslog";


    public static class Factory {
        public static boolean getPingServer() {
            boolean netState = false;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet(API_HOST);
                HttpResponse response = httpclient.execute(request);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    netState = true;
                }
                httpclient.getConnectionManager().shutdown();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return netState;
        }

        public static String getAppUser(String userId) {
            return getAPI(String.format(API_GET_APP_USER_URL, userId));
        }

        public static String getAppVersion() {
            return getAPI(API_GET_APP_VERSION_URL);
        }

        public static String getDataVersion() {
            return getAPI(API_GET_DATA_VERSION_URL);
        }

        public static String getBeaconUUID() {
            return getAPI(API_GET_BEACON_UUID_URL);
        }

        public static String getCatalog() {
            return getAPI(API_GET_CATALOG_URL);
        }

        public static String getExContent(String extag) {
            return getAPI(String.format(API_GET_EX_CONTENT_URL, extag));
        }

        public static boolean postAppUser(JSONObject jsonObject) {
            return postAPI(API_POST_APP_USER_URL, jsonObject);
        }

        public static boolean putAppUser(String userId, JSONObject jsonObject) {
            return putAPI(String.format(API_PUT_APP_USER_URL, userId), jsonObject);
        }

        public static boolean postSyslog(JSONObject jsonObject) {
            return postAPI(API_POST_SYS_LOG_URL, jsonObject);
        }
    }

    public static String getAPI(String getURL) {
        String result = "";
        try {
            //创建一个HttpClient对象
            HttpClient httpclient = new DefaultHttpClient();
            //创建HttpGet对象
            HttpGet request = new HttpGet(getURL);
            //请求信息类型MIME每种响应类型的输出（普通文本、html 和 XML）。允许的响应类型应当匹配资源类中生成的 MIME 类型
            //资源类生成的 MIME 类型应当匹配一种可接受的 MIME 类型。如果生成的 MIME 类型和可接受的 MIME 类型不 匹配，那么将
            //生成 com.sun.jersey.api.client.UniformInterfaceException。例如，将可接受的 MIME 类型设置为 text/xml，而将
            //生成的 MIME 类型设置为 application/xml。将生成 UniformInterfaceException。
            request.addHeader("Accept", "application/json");
            //获取响应的结果
            HttpResponse response = httpclient.execute(request);
            //获取HttpEntity
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //获取响应的结果信息
                result = EntityUtils.toString(entity);
            }
            httpclient.getConnectionManager().shutdown();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static boolean putAPI(String putURL, JSONObject jsonObject) {
        boolean httpStatus = false;
        try {
            //Create the POST object and add the parameters
            HttpPut httpPut = new HttpPut(putURL);
            StringEntity entity = new StringEntity(jsonObject.toString(), HTTP.UTF_8);
            entity.setContentType("application/json");
            httpPut.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(httpPut);
            httpStatus = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
            client.getConnectionManager().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return httpStatus;
    }

    public static boolean postAPI(String postURL, JSONObject jsonObject) {
        boolean httpStatus = false;
        try {
            // Create the POST object and add the parameters
            HttpPost httpPost = new HttpPost(postURL);
            StringEntity entity = new StringEntity(jsonObject.toString(), HTTP.UTF_8);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(httpPost);
            httpStatus = response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED;
            client.getConnectionManager().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return httpStatus;
    }
}

