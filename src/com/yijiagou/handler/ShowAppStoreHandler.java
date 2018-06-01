package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.pojo.UrlAppinfo;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.*;

/**
 * Created by zgl on 17-8-11.
 */
public class ShowAppStoreHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger =Logger.getLogger(ShowAppStoreHandler.class.getName());
    public ShowAppStoreHandler(SJedisPool sJedisPool){
        this.sJedisPool=sJedisPool;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        if (jsonObject.get(JsonKeyword.TYPE).equals(JsonKeyword.APPSTORE)) {
            String devicetype = (String) jsonObject.get(JsonKeyword.DEVICETYPE);
            String userName=(String) jsonObject.get(JsonKeyword.USERNAME);
            String page = (String) jsonObject.get(JsonKeyword.PAGE);
            logger.info("[appStore,"+userName+",["+devicetype+","+page+"],"+"获取应用商店,"+System.currentTimeMillis()+"]");
            JSONArray jsonArray = jedisGeturlinfo(devicetype, Integer.parseInt(page));
            logger.info("返回前端的数据===>ShowAppStoreHandler:channelRead "+jsonArray.toString());
            ctx.writeAndFlush(jsonArray.toString());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private JSONArray jedisGeturlinfo(String devicetype, int a) {
        Set set1 = null;
        Jedis jedis=null;
        String deviceinfo=devicetype+"info";
        int count=0;
        jedis = sJedisPool.getConnection();
        while (true) {
            try {
                int start = (a - 1) * 10;
                int end = (a - 1) * 10 + 9;
                set1 = jedis.zrevrange(devicetype, start, end);
                sJedisPool.putbackConnection(jedis);
                break ;
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e+"===>jedisGeturlinfo");
                if(count++>=2){
                    logger.error("暂时无法链接数据库===>jedisGeturlinfo");
                    return null;
                }else {
                    sJedisPool.repairConnection(jedis);
                    continue ;
                }
            }
        }
        Iterator iterator = set1.iterator();
        String json = "";
        JSONArray jsonArray = new JSONArray();
        Jedis jedis1=sJedisPool.getConnection();
        try {
            while (iterator.hasNext()) {
                String appid=(String) iterator.next();
                String info="";
                while (true) {
                    try {
                        info = jedis1.hget(deviceinfo, appid);
                        sJedisPool.putbackConnection(jedis1);
                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                        logger.warn(e+"===>jedisGeturlinfo");
                        if(count++>2){
                            logger.error("暂时无法链接数据库===>jedisGeturlinfo");
                        }else {
                            sJedisPool.repairConnection(jedis);
                            continue;
                        }
                    }

                }
                UrlAppinfo urlAppinfo = new UrlAppinfo(appid,info);
                json = JSON.toJSONString(urlAppinfo);
                System.out.println(json);
                jsonArray.add(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e+"===>jedisGeturlinfo");
        }
        logger.info("得到家电类型对应的应用集合===>jedisGeturlinfo "+jsonArray);
        return jsonArray;
    }
}
