package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;

/**
 * Created by wangwei on 17-9-19.
 */
public class MobileCheckUserNameHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    public MobileCheckUserNameHandler(SJedisPool sJedisPool){
        this.sJedisPool = sJedisPool;
    }
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        JSONObject body = (JSONObject)msg;
        if(JsonKeyword.MOBILECHECKUSERNAME.equals(body.getString("type"))){
            Jedis jedis = sJedisPool.getConnection();
            String username = body.getString("username");
            if(jedis.exists(username)){
                ctx.writeAndFlush("0");
            }else {
                ctx.writeAndFlush("1");
            }
        }else {
            ctx.fireChannelRead(body);
        }
    }
}
