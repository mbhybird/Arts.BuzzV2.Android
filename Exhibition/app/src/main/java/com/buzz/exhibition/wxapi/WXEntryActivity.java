package com.buzz.exhibition.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.buzz.activity.MyApplication;
import com.buzz.utils.ConfigHelper;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.squareup.okhttp.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = WXEntryActivity.class.getSimpleName();
    MyApplication myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myApp = (MyApplication) getApplication();
        myApp.wxapi.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(final BaseResp baseResp) {
        String result;
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "success";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "cancel";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "deny";
                break;
            default:
                result = "unknown";
                break;
        }

        if (baseResp.transaction == null) {
            final OkHttpClient mOkHttpClient = new OkHttpClient();
            final Request request = new Request.Builder().url(String.format(myApp.accessTokenUrl, myApp.APP_ID, myApp.SECURE, ((SendAuth.Resp) baseResp).code)).build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    final String result = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String accessToken = jsonObject.getString("access_token");
                                String openId = jsonObject.getString("openid");
                                //Log.i(TAG, result);
                                String userInfoURI = String.format(myApp.userInfoUrl, accessToken, openId);
                                //Log.i(TAG, userInfoURI);
                                mOkHttpClient.newCall(new Request.Builder().url(userInfoURI).build()).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {

                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        final String userInfo = response.body().string();
                                        ConfigHelper.getInstance(getApplicationContext()).updateProfile(userInfo);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //Toast.makeText(getApplicationContext(), userInfo, Toast.LENGTH_LONG).show();
                                                Log.i(TAG, userInfo);
                                            }
                                        });
                                    }
                                });
                            } catch (JSONException ex) {

                            }
                        }

                    });
                }
            });

            //Toast.makeText(this, "login ".concat(result), Toast.LENGTH_LONG).show();
            myApp.wxHandler.obtainMessage(100, baseResp.errCode, baseResp.errCode, "login ".concat(result)).sendToTarget();
            this.finish();
        } else if (baseResp.transaction.startsWith("shareToTimeline")) {
            //Toast.makeText(this, "share ".concat(result), Toast.LENGTH_LONG).show();
            myApp.wxHandler.obtainMessage(200, "share ".concat(result)).sendToTarget();
            this.finish();
        }
    }

}
