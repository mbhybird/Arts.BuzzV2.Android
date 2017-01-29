package com.buzz.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.buzz.activity.NavActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Buzz on 2015/8/7.
 */
public class VersionUpdateHelper {
    private Context mContext;
    private String mLatestVersion;
    private String mCurrentVersion;

    public String getmPackageURL() {
        return mPackageURL;
    }

    private String mPackageURL;

    public VersionUpdateHelper(Context context) {
        this.mContext = context;
        //this.setVersionAndURL();
    }

    public int getVersionCode() {
        return getPackageInfo().versionCode;
    }

    public String getVersionName() {
        String result = getPackageInfo().versionName;
        return result;
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            PackageManager pm = this.mContext.getPackageManager();
            pi = pm.getPackageInfo(this.mContext.getPackageName(), PackageManager.GET_CONFIGURATIONS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    public boolean isNeedUpdate() {
        return false;//!this.mCurrentVersion.equals(this.mLatestVersion);
    }

    public void setVersionAndURL() {
        String result = "";
        HttpGet httpGet = new HttpGet("http://macauarts.buzz:81/api/repo/appversion");
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            httpGet.addHeader("Accept", "application/json");
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != "") {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("version")) {
                    this.mLatestVersion = jsonObject.getString("version");
                }
                if (jsonObject.has("url")) {
                    this.mPackageURL = jsonObject.getString("url");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            this.mLatestVersion = this.getVersionName();
        }

        this.mCurrentVersion = this.getVersionName();
    }

    public String getFileName() {
        String path = this.mPackageURL;
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? "" : path.substring(separatorIndex + 1, path.length());
    }

    public void install() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String fileFullPath = GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_APK).concat(this.getFileName());
        intent.setDataAndType(Uri.fromFile(new File(fileFullPath)), "application/vnd.android.package-archive");
        this.mContext.startActivity(intent);
        ((NavActivity)this.mContext).finish();
    }
}
