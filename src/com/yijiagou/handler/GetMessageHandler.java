package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.Vo.VoMessage;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class GetMessageHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger = Logger.getLogger(GetMessageHandler.class.getName());
    private static String KEY="MESS";

    public GetMessageHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.GETMESSAGE)) {
            String userName=jsonObject.getString(JsonKeyword.USERNAME);
            Jedis jedis = null;
            jedis=sJedisPool.getConnection();
            Map<String,String> messages=getMessageFromCache(jedis,userName+KEY);
            if(messages==null){
                ctx.writeAndFlush("2");
            }else {
                String json = null;
                JSONArray jsonArray = new JSONArray();
                for (String user : messages.keySet()) {
                    VoMessage voMessage = new VoMessage();
                    String[] strs = user.split("\\|");
                    String mesg = messages.get(user);
                    voMessage.setUser(strs[0]);
                    voMessage.setTime(strs[1]);
                    voMessage.setMessage(mesg);
                    json = JSON.toJSONString(voMessage);
                    jsonArray.add(json);
                }
                ctx.writeAndFlush(jsonArray.toString());
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    public Map<String,String> getMessageFromCache(Jedis jedis,String name){
        Map<String,String> messages=null;
        try {
            messages=jedis.hgetAll(name);
            sJedisPool.repairConnection(jedis);
        }catch (Exception e){
            System.out.println(e);
            logger.error("getMessageFromCache redis connection error:"+e);
            sJedisPool.repairConnection(jedis);
            return null;
        }
        return messages;
    }
}
