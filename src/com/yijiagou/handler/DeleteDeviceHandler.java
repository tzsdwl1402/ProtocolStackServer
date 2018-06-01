package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class DeleteDeviceHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger = Logger.getLogger(DeleteDeviceHandler.class.getName());
    private static final String KEY="DEVICE";
    public DeleteDeviceHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if(actiontype.equals(JsonKeyword.DELETE_DEVICE)){
            String userName=jsonObject.getString(JsonKeyword.USERNAME);
            String deviceId=jsonObject.getString(JsonKeyword.DEVICEID);
            logger.info("[deleteDevice,"+userName+",["+deviceId+"],解邦家电,"+System.currentTimeMillis()+"]");
            int ret= deleteDevicebyUserName(userName,deviceId);
            if(ret>=0){
                ctx.writeAndFlush("1");
            }else {
                ctx.writeAndFlush("2");
            }
        }else {
            ctx.fireChannelRead(msg);
        }

    }
    public int deleteDevicebyUserName(String userName,String deviceId){
        Jedis jedis = null;
        jedis=sJedisPool.getConnection();

        if(jedis.exists(userName+KEY)){
            try{
                jedis.hdel(userName+KEY,deviceId);
            }catch (Exception e){
                return -1;
            }
        }else {
            return -1;
        }
        sJedisPool.putbackConnection(jedis);
        int a=deleteFromDB(userName,deviceId);
        return a;

    }
    public int deleteFromDB(String userName,String deviceId){
        String sql = "delete from userAndDevice where userName=? and deviceId=?";
        int a=0;
        try {
            a= ConnPoolUtil.updata(sql,userName,deviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }
}
