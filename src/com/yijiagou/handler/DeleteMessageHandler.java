package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class DeleteMessageHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger = Logger.getLogger(DeleteMessageHandler.class.getName());
    private static String KEY="MESS";

    public DeleteMessageHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if(actiontype.equals(JsonKeyword.DELETEMESSAGE)){
            String userName=jsonObject.getString(JsonKeyword.USERNAME);
            String sender=jsonObject.getString(JsonKeyword.SENDER);
            String addTime=jsonObject.getString(JsonKeyword.ADDTIME);
            logger.info("[deleteMessage,"+userName+",["+sender+","+addTime+"],"+"删除留言,"+System.currentTimeMillis()+"]");
            Jedis jedis=null;
            jedis=sJedisPool.getConnection();
            int ret=deleteFromCache(jedis,userName,sender,addTime);
            sJedisPool.putbackConnection(jedis);
            if(ret==1){
                ctx.writeAndFlush("1");
            }else {
                ctx.writeAndFlush("2");
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    public int deleteFromCache(Jedis jedis,String userName,String sender,String addTime){
        try {
            System.out.println(userName+KEY+"   "+addTime);
            jedis.hdel(userName+KEY,sender+"|"+addTime);
            return 1;
        }catch (Exception e) {
            System.out.println(e);
            return 2;
        }
    }
}
