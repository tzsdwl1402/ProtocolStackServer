package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.Vo.VoDevice;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.pojo.UserAndDevice;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.sql.ResultSet;
import java.util.Map;

/**
 * Created by zgl on 17-8-15.
 */

public class GetUserDeviceHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(GetUserDeviceHandler.class.getName());
    private SJedisPool sJedisPool;

    public GetUserDeviceHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.GETDEVICE)) {
            String uname = (String) jsonObject.get(JsonKeyword.USERNAME);
            String devicetype = (String) jsonObject.get(JsonKeyword.DEVICETYPE);
            JSONArray jsonArray = this.getUserdevices(uname, devicetype);
            logger.info(uname+"GetUserDevice:channelRead===>"+jsonArray.toString());
            ctx.writeAndFlush(jsonArray.toString());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private JSONArray getUserdevices(String uname, String devicetype) {
        Jedis jedis = null;
        JSONArray jsonArray = new JSONArray();
        String json = null;
        Map<String, String> device = null;
        int count = 0;
        jedis = sJedisPool.getConnection();
        while (true) {
            try {
                device = jedis.hgetAll(uname);
                sJedisPool.putbackConnection(jedis);
                break;
            } catch (JedisConnectionException e) {
                logger.warn(e+uname+ "===>getUserdevices");
                if (count++ >= 2) {
                    logger.error(uname+"===>redis暂时无法提供服务");
                    sJedisPool.putbackConnection(jedis);
                    return null;
                } else {
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        logger.error(e1+"===>getUserdevices");
                    }
                    continue;
                }
            }
        }
        if (device != null) {
            for (String deviceId : device.keySet()) {
                if (device.get(deviceId).equals(devicetype)) {
                    Long addTime=getAddTime(deviceId);
                    UserAndDevice userAndDevice = new UserAndDevice(deviceId,addTime);
                    json = JSON.toJSONString(userAndDevice);
                    jsonArray.add(json);
                }
            }
        }
        logger.info(uname+"===>返回家电集合："+jsonArray);
        return jsonArray;
    }

    public Long getAddTime(String deviceId){
        String sql ="select addTime from device where deviceId=?";
        VoDevice voDevice=new VoDevice();
        ResultSet rs = null;
        try {
            rs = ConnPoolUtil.select(sql,deviceId);
            while(rs.next()){
                voDevice.setAddTime(rs.getLong("addTime"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return voDevice.getAddTime();
    }

}
