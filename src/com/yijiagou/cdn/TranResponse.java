package com.yijiagou.cdn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangwei on 17-7-3.
 */
public class TranResponse {
    //    private InputStream in;
    private String resline;
    private Map<String, String> heads;
    private String body;
    private byte[] orgMessage;

    public TranResponse(byte[] orgmessage) throws IOException {
        heads = new HashMap<>();
        this.orgMessage = orgmessage;

        String message = new String(orgmessage, "UTF-8");
        if (message != null) {
            int line = 0;

            String[] info = message.split("\n");
            for (int i = 0; i < info.length; i++) {
                if (0 == i && !info[i].equalsIgnoreCase("")) {
                    resline = info[0];
                    System.out.print(resline);
                    continue;
                }
                if (!(info[i].equals("\r") || info[i].equals(""))) {
                    String[] strs = info[i].split(": ");
                    heads.put(strs[0], strs[1].trim());
                } else {
                    line = i;
                    break;
                }
            }

            String[] strs = resline.split(" ");


            StringBuffer body0 = new StringBuffer();
            for (int i = line; i < info.length; i++) {
                body0.append(info[i]);
                body0.append("\n");
            }
            body = body0.toString();


        }
    }

    public String getHead(String name) {
        return heads.get(name);
    }

    public void setHeads(String name, String value) {
        heads.put(name, value);
    }

    public String getBody() {
        return this.body;
    }

    public String getResline() {
        System.out.println(resline);
        return resline;
    }

    public String getMessage() throws UnsupportedEncodingException {
        return new String(orgMessage, "UTF-8");
    }

}
