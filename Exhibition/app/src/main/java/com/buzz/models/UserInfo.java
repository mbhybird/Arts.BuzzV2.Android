package com.buzz.models;

import com.buzz.utils.GlobalConst;

/**
 * Created by NickChung on 2/14/15.
 */
public class UserInfo {
    private String id;

    private String userid;

    private String username_cn;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;


    public String getUsername_cn() {
        return username_cn;
    }

    public void setUsername_cn(String username_cn) {
        this.username_cn = username_cn;
    }

    public String getUsername_en() {
        return username_en;
    }

    public void setUsername_en(String username_en) {
        this.username_en = username_en;
    }

    private String username_en;
    private String username_tw;

    public String getUsername_pt() {
        return username_pt;
    }

    public void setUsername_pt(String username_pt) {
        this.username_pt = username_pt;
    }

    public String getUsername_tw() {
        return username_tw;
    }

    public void setUsername_tw(String username_tw) {
        this.username_tw = username_tw;
    }

    private String username_pt;

    private String nickname;

    private String isauthorized;

    private String password;

    private String defaultlang;

    public String getVoicelang() {
        return voicelang;
    }

    public void setVoicelang(String voicelang) {
        this.voicelang = voicelang;
    }

    private String voicelang;

    public void setDefaultlang(String defaultlang) {
        this.defaultlang = defaultlang;
    }

    public String getDefaultlang() {
        return this.defaultlang;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setIsauthorized(String isauthorized) {
        this.isauthorized = isauthorized;
    }

    public String getIsauthorized() {
        return this.isauthorized;
    }

    private String autoplay = GlobalConst.AUTO_PLAY_ON;

    public String getAutoplay() {
        return autoplay;
    }

    public void setAutoplay(String autoplay) {
        this.autoplay = autoplay;
    }

    public String getEarphoneplay() {
        return earphoneplay;
    }

    public void setEarphoneplay(String earphoneplay) {
        this.earphoneplay = earphoneplay;
    }

    private String earphoneplay = GlobalConst.EARPHONE_PLAY_OFF;

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", userid='" + userid + '\'' +
                ", username_cn='" + username_cn + '\'' +
                ", email='" + email + '\'' +
                ", username_en='" + username_en + '\'' +
                ", username_tw='" + username_tw + '\'' +
                ", username_pt='" + username_pt + '\'' +
                ", nickname='" + nickname + '\'' +
                ", isauthorized='" + isauthorized + '\'' +
                ", password='" + password + '\'' +
                ", defaultlang='" + defaultlang + '\'' +
                ", voicelang='" + voicelang + '\'' +
                ", autoplay='" + autoplay + '\'' +
                ", earphoneplay='" + earphoneplay + '\'' +
                '}';
    }
}
