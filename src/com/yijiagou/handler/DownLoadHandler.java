package com.yijiagou.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.config.Configurator;
import com.yijiagou.exception.MessageException;
import com.yijiagou.message.MessageKeyword;
import com.yijiagou.message.PSRequest;
import com.yijiagou.message.PSResponse;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.server.PSServer;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by zgl on 17-7-29.
 */

public class DownLoadHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger = Logger.getLogger(DownLoadHandler.class.getName());
    private static final BlockingQueue<String> randomnums = new ArrayBlockingQueue<String>(100000);

    public DownLoadHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    static {
        int[] randomnum = new int[100000];
        Random random = new Random();
        for (int i = 0; i < randomnum.length; i++) {
            int number = random.nextInt(100000) + 1;
            for (int j = 0; j <= i; j++) {
                if (number != randomnum[j]) {
                    randomnum[i] = number;
                }
            }
        }
        for (int i = 0; i < randomnum.length; i++) {
            String num = "" + randomnum[i];
            int length = num.length();
            if (length <= 6) {
                num += "0" + num;
            }
            randomnums.offer(num);
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        JSONObject jsonObject = (JSONObject) msg;
        String type = (String) jsonObject.get(JsonKeyword.TYPE);
        if (type.equals(JsonKeyword.DOWNLOAD)) {
            System.out.println("dddddddddddddddddddddddddddddddddd");
            String devicetype = (String) jsonObject.get(JsonKeyword.DEVICETYPE);
            JSONArray jsonArray = jsonObject.getJSONArray(JsonKeyword.DEVICE);

            System.out.println("fffffffffffffffffffffffffffffffff");
            String appid = (String) jsonObject.get(JsonKeyword.APPID);
            JSONObject jsonObject1;
            String[] deviceids = new String[jsonArray.size()];
            System.out.println(jsonArray.size());

            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject1 = (JSONObject) jsonArray.get(i);
                deviceids[i] = (String) jsonObject1.get(JsonKeyword.DEVICEID);
                System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
            }
            BufferedWriter bw = null;
            BufferedReader br = null;
            String host = null;
            int port = 0;
            try {
                host = Configurator.getBserverHost();
                port = Configurator.getBserverPort();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e + "===>DownLoadHandler:channelRead");
            }
            int count = 0;
            String sessionid = null;
            try {
                sessionid = randomnums.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error(e + "DownLoadHandler:channelRead");
            }
            PSRequest psRequest = new PSRequest(sessionid, deviceids, devicetype, appid);
            while (true) {
                String request = psRequest.toString() + "\n";
                Socket socket = null;
                try {
                    socket = new Socket(host, port);
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    System.out.println(request);
                    logger.info(appid + "===>DownLoadHandler:channelRead" + request);
                    bw.write(request);
                    bw.flush();
                    String response = br.readLine();
                    PSResponse psResponse = null;
                    try {
                        psResponse = new PSResponse(response);
                    } catch (MessageException e) {
                        e.printStackTrace();
                    }
                    logger.info(appid + "===>DownLoadHandler:channelRead" + response);
                    if (psResponse.getBody().equals(MessageKeyword.ERROR)) {
                        continue;
                    } else {
                        String s = psResponse.getBody();
                        String[] ss = s.split("");
                        String json = "[";
                        for (int i = 1; i < ss.length; i++) {
                            System.out.println(ss[i]);
                            json += "{\"statecode\":\"" + ss[i] + "\"},";
                        }
                        String jsonArray1 = json.substring(0, json.length() - 1) + "]\n";
                        logger.info(appid + "===>DownLoadHandler:channelRead" + jsonArray1.toString());
                        ctx.writeAndFlush(jsonArray1);
                    }
                    if (response == null) {
                        continue;
                    }
                    try {
                        randomnums.put(sessionid);
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }

                    break;
                } catch (IOException e) {
                    logger.warn(e + "==>DownLoadHandler:channelRead");
                    if (count++ > 2) {
                        logger.error(appid + "暂时无法链接服务器B==>DownLoadHandler:channelRead");
                        break;
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        logger.error(e1 + "==>DownLoadHandler:channelRead");
                    }
                    continue;
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(e);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }

    }
}
