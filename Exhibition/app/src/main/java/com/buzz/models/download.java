package com.buzz.models;

import android.content.Context;

import com.buzz.activity.MyApplication;
import com.buzz.utils.ConfigHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NickChung on 6/11/15.
 */
public class download {
    private extag dextag;
    private List<content> contentList;
    private String updateSchema;
    private String localSchema;
    private boolean finished;
    private Context mContext;
    private boolean needVersionUpdate;
    private String extag;

    public extag getDextag() {
        return dextag;
    }

    public void setDextag(extag dextag) {
        this.dextag = dextag;
    }

    public download(String extag, Context context) {
        this.dextag = new extag();
        this.contentList = new ArrayList<content>();
        this.finished = true;
        this.needVersionUpdate = false;
        this.mContext = context;
        this.extag = extag;

        this.localSchema = ConfigHelper.getInstance(context).getExContent(extag);
        this.updateSchema = ConfigHelper.getInstance(context).getLatestExContent(extag);

        MyApplication myApp = (MyApplication) mContext;
        String oldVersion = myApp.oldCatalogVersionList.get(extag);
        String newVersion = myApp.newCatalogVersionList.get(extag);
        if (this.localSchema == "") {
            this.needVersionUpdate = true;
        } else if (oldVersion != null && newVersion != null && !oldVersion.equals(newVersion)) {
            this.needVersionUpdate = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public List<content> getContentList() {
        return contentList;
    }

    public void setContentList(List<content> contentList) {
        this.contentList = contentList;
    }

    public String getUpdateSchema() {
        return updateSchema;
    }

    public String getLocalSchema() {
        return localSchema;
    }

    public boolean isNeedVersionUpdate() {
        return needVersionUpdate;
    }

    public void saveToConfigThenReInit() {
        if (ConfigHelper.getInstance(mContext).updateExContentToConfig(this.extag, this.updateSchema)) {
            MyApplication myApp = (MyApplication) mContext;
            myApp.initSysParams();
            String newVersion = myApp.newCatalogVersionList.get(this.extag);
            if (newVersion != null) {
                if (myApp.oldCatalogVersionList.containsKey(this.extag)) {
                    myApp.oldCatalogVersionList.put(this.extag, newVersion);
                }
            }

        }
    }

    public void deleteConfigThenReInit() {
        if (ConfigHelper.getInstance(mContext).deleteExContentFromConfig(this.extag)) {
            MyApplication myApp = (MyApplication) mContext;
            myApp.initSysParams();
        }
    }
}
