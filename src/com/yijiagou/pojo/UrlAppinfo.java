package com.yijiagou.pojo;

/**
 * Created by zgl on 17-8-11.
 */
public class UrlAppinfo {
    private String appid;
    private String info;

    public UrlAppinfo() {
    }

    public UrlAppinfo(String appid){
        this.appid = appid;
    }

    public UrlAppinfo(String appid, String info) {
        this.appid = appid;
        this.info = info;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
