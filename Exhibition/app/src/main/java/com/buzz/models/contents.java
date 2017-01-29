package com.buzz.models;

/**
 * Created by NickChung on 2/14/15.
 * @deprecated
 */
public class contents {
    private String id;

    private String contenttype;

    private String serverpath;

    private String clientpath;

    private String filename;

    public void setId(String id) {
        this.id = id;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }

    public void setServerpath(String serverpath) {
        this.serverpath = serverpath;
    }

    public void setClientpath(String clientpath) {
        this.clientpath = clientpath;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getId() {
        return this.id;
    }

    public String getContenttype() {
        return this.contenttype;
    }

    public String getServerpath() {
        return this.serverpath;
    }

    public String getClientpath() {
        return this.clientpath;
    }

    public String getFilename() {
        return this.filename;
    }

    @Override
    public String toString() {
        return "contents{" +
                "id='" + id + '\'' +
                ", contenttype='" + contenttype + '\'' +
                ", serverpath='" + serverpath + '\'' +
                ", clientpath='" + clientpath + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
