package com.yijiagou.message;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by wangwei on 17-9-9.
 */
public class PSRequest extends Message {
    private String sessionId;
    private String[] deviceIds;
    private String deviceType;
    private String appId;
    private StringBuilder sb1;
    private StringBuilder sb2;

    //0000|session*urlsize*deviceid1#deviceid2#deviceid3..*deviceType*appid
    public PSRequest(String sessionId, String[] deviceIds, String deviceType, String appId) {
        this.sb1 = new StringBuilder();
        this.sb2 = new StringBuilder();
        this.head = MessageKeyword.PSCOMMAND;
        int urlsize = 0;
        for (int i = 0; i < deviceIds.length; i++) {
            sb1.append(deviceIds[i]).append("#");
        }
        String deviceids = sb1.toString().substring(0, sb1.length() - 1);
        urlsize = (deviceids + "*" + deviceType + "*" + appId).getBytes().length;
        sb2.append(sessionId).append("*").append(urlsize).append("*").append(deviceids).
                append("*").append(deviceType).append("*").append(appId);
        this.body = sb2.toString();
    }
}
