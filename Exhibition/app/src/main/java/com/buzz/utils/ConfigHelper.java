package com.buzz.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.buzz.activity.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by NickChung on 7/26/15.
 */
public class ConfigHelper {

    Context ctx;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    final String SETTING_KEY_APP_VERSION = "app_version";
    final String SETTING_KEY_DATA_VERSION = "data_version";
    final String SETTING_KEY_CATALOG = "catalog";
    final String SETTING_KEY_APP_USER = "app_user";
    final String SETTING_KEY_BEACON_UUID = "beacon_uuid";
    final String SETTING_FILE_NAME = "arts.buzz.config";
    final String SETTING_KEY_FAVORITE = "favorite";
    final String SETTING_KEY_FIRST_TIME_IN = "first";
    final String SETTING_KEY_PROFILE = "profile";

    private static ConfigHelper configHelper;

    public static ConfigHelper getInstance(Context context) {
        if (configHelper == null) {
            configHelper = new ConfigHelper(context);
        }
        return configHelper;
    }

    public ConfigHelper(Context context) {
        this.ctx = context;
        this.settings = this.ctx.getSharedPreferences(SETTING_FILE_NAME, Context.MODE_PRIVATE);
        this.editor = settings.edit();
        boolean needCommit = false;

        //添加固定设置
        if (!this.settings.contains(SETTING_KEY_CATALOG)) {
            this.editor.putString(SETTING_KEY_CATALOG, "");
            needCommit = true;
        }

        if (!this.settings.contains(SETTING_KEY_BEACON_UUID)) {
            this.editor.putString(SETTING_KEY_BEACON_UUID, "");
            needCommit = true;
        }

        if (!this.settings.contains(SETTING_KEY_DATA_VERSION)) {
            this.editor.putString(SETTING_KEY_DATA_VERSION, "");
            needCommit = true;
        }

        if (!this.settings.contains(SETTING_KEY_APP_VERSION)) {
            this.editor.putString(SETTING_KEY_APP_VERSION, "");
            needCommit = true;
        }

        if (!this.settings.contains(SETTING_KEY_APP_USER)) {
            this.editor.putString(SETTING_KEY_APP_USER, "");
            needCommit = true;
        }

        if (!this.settings.contains(SETTING_KEY_FAVORITE)) {
            this.editor.putString(SETTING_KEY_FAVORITE, "");
            needCommit = true;
        }

        if (!this.settings.contains(SETTING_KEY_FIRST_TIME_IN)) {
            this.editor.putBoolean(SETTING_KEY_FIRST_TIME_IN, true);
            needCommit = true;
        }

        if (!this.settings.contains(SETTING_KEY_PROFILE)) {
            this.editor.putString(SETTING_KEY_PROFILE, "");
            needCommit = true;
        }

        if (needCommit) {
            this.editor.commit();
        }
    }

    public boolean updateFirstTimeIn() {
        return this.editor.putBoolean(SETTING_KEY_FIRST_TIME_IN, false).commit();
    }

    public boolean updateProfile(String info) {
        return this.editor.putString(SETTING_KEY_PROFILE, info).commit();
    }

    public String getProfile() {
        return this.settings.getString(SETTING_KEY_PROFILE, "");
    }

    public boolean getFirstTimeIn() {
        return this.settings.getBoolean(SETTING_KEY_FIRST_TIME_IN, true);
    }

    public boolean findAppUser(String userId) {
        String jsonUser = WebAPIHelper.Factory.getAppUser(userId);
        return (jsonUser != "");
    }

    public boolean updateAppUser(String userId) {
        String jsonUser = WebAPIHelper.Factory.getAppUser(userId);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonUser);
            jsonObject.put("autoplay", GlobalConst.AUTO_PLAY_ON);
            jsonObject.put("earphoneplay", GlobalConst.EARPHONE_PLAY_ON);
        } catch (JSONException e) {
        }
        return this.editor.putString(SETTING_KEY_APP_USER, jsonObject.toString()).commit();
    }

    public boolean updateAppUser(String userId, String defaultLang, String voiceLang, String autoPlay, String earphonePlay) {
        JSONObject jsonObject = null;
        String jsonUser = this.getAppUser();
        try {
            jsonObject = new JSONObject(jsonUser);
            jsonObject.put("defaultlang", defaultLang);
            jsonObject.put("voicelang", voiceLang);
            if (jsonObject.has("autoplay")) {
                jsonObject.remove("autoplay");
                if (((MyApplication) this.ctx.getApplicationContext()).serverReady) {
                    WebAPIHelper.Factory.putAppUser(userId, jsonObject);
                }
            }
            jsonObject.put("autoplay", autoPlay);
            jsonObject.put("earphoneplay", earphonePlay);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.editor.putString(SETTING_KEY_APP_USER, jsonObject.toString()).commit();
    }

    public boolean addAppUser(JSONObject jsonUser) {
        return WebAPIHelper.Factory.postAppUser(jsonUser);
    }

    public boolean checkAppUser(String userId, String password) {
        boolean flag = false;
        String jsonUser = WebAPIHelper.Factory.getAppUser(userId);
        JSONObject jsonObject = null;
        try {
            if (jsonUser != "") {
                jsonObject = new JSONObject(jsonUser);
                flag = jsonObject.getString("password").equals(password);
            }
        } catch (JSONException e) {

        }

        return flag;
    }

    public String getAppUser() {
        return this.settings.getString(SETTING_KEY_APP_USER, "");
    }

    public boolean addSysLog(JSONObject jsonSysLog) {
        return WebAPIHelper.Factory.postSyslog(jsonSysLog);
    }

    public boolean updateBeaconUUID() {
        String jsonBeaconUUIDs = WebAPIHelper.Factory.getBeaconUUID();
        return this.editor.putString(SETTING_KEY_BEACON_UUID, jsonBeaconUUIDs).commit();
    }

    public String getBeaconUUID() {
        return this.settings.getString(SETTING_KEY_BEACON_UUID, "");
    }

    public boolean updateCatalog() {
        String jsonCatalog = WebAPIHelper.Factory.getCatalog();
        return this.editor.putString(SETTING_KEY_CATALOG, jsonCatalog).commit();
    }

    public String getCatalog() {
        return this.settings.getString(SETTING_KEY_CATALOG, "");
    }

    public String getLatestAppVersion() {
        return WebAPIHelper.Factory.getAppVersion();
    }

    public String getCurrentAppVersion() {
        return this.settings.getString(SETTING_KEY_APP_VERSION, "");
    }

    public boolean isAppVersionUpdate() {
        return !this.getLatestAppVersion().equals(this.getCurrentAppVersion());
    }

    public String getLatestDataVersion() {
        return WebAPIHelper.Factory.getDataVersion();
    }

    public String getCurrentDataVersion() {
        return this.settings.getString(SETTING_KEY_DATA_VERSION, "");
    }

    public boolean updateDataVersion(){
        return this.editor.putString(SETTING_KEY_DATA_VERSION, this.getLatestDataVersion()).commit();
    }

    public boolean isDataVersionUpdate() {
        return !this.getLatestDataVersion().equals(this.getCurrentDataVersion());
    }

    public boolean isExTagExists(String extag) {
        return this.settings.contains(extag);
    }

    public String getExContent(String extag) {
        String jsonExContent = "";
        if (isExTagExists(extag)) {
            jsonExContent = this.settings.getString(extag, "");
        }
        return jsonExContent;
    }

    public String getLatestExContent(String extag) {
        String jsonExContent = WebAPIHelper.Factory.getExContent(extag);
        return jsonExContent;
    }

    public boolean updateExContentToConfig(String extag, String jsonExContent) {
        return this.editor.putString(extag, jsonExContent).commit();
    }

    public boolean deleteExContentFromConfig(String extag) {
        boolean delFlag = false;
        if (isExTagExists(extag)) {
            delFlag = this.editor.remove(extag).commit();
        }

        return delFlag;
    }

    public String getFavorite() {
        return this.settings.getString(SETTING_KEY_FAVORITE, "");
    }

    public boolean updateFavorite(String json) {
        return this.editor.putString(SETTING_KEY_FAVORITE, json).commit();
    }
}
