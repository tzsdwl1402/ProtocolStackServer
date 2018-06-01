package com.yijiagou.handler;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import com.yijiagou.tools.JedisUtils.SJedisPool;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangwei on 17-7-28.
 */
public class LoginHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(LoginHandler.class.getName());
    private SJedisPool sJedisPool;
    private ReentrantLock lock = new ReentrantLock();
    private static int i = 0;

    public LoginHandler(SJedisPool sJedisPool){
        this.sJedisPool = sJedisPool;
    }

    public void channelRead(ChannelHandlerContext ctx,Object msg) throws JSONException, UnsupportedEncodingException {
        JSONObject body = (JSONObject)msg;
        String type = body.getString(JsonKeyword.TYPE);

        if(type.equalsIgnoreCase(JsonKeyword.LOGIN)){
            String username = body.getString(JsonKeyword.USERNAME);
            String passwd = body.getString(JsonKeyword.PASSWORD);
            System.out.println(passwd);
            logger.info("[login,"+username+",["+username+","+passwd+"],"+"用户登录,"+System.currentTimeMillis()+"]");
            String can = "0";
            if(canLogin(username,passwd)){
                can = "1";
            }
            logger.info(username+"访问后台返回值===>Loginhandler:channelRead"+can);
            System.out.println(username+"访问后台返回值===>Loginhandler:channelRead"+can);
            ctx.writeAndFlush(can).addListener(ChannelFutureListener.CLOSE);

            lock.lock();
            System.out.println(body+" :: "+(i++));
            lock.unlock();

        }else {
            ctx.fireChannelRead(msg);
        }

    }

    private boolean canLogin(String username,String passwd){
        Jedis jedis = null;
        int count = 0;
        jedis = sJedisPool.getConnection();
        System.out.println(username);
        while (count < 3) {
            try{
                String passwd0 = jedis.hget(JsonKeyword.USERS,username);
                System.out.println("passwd0"+passwd0);
                if(passwd0 != null && passwd0.equals(passwd)){
                    sJedisPool.putbackConnection(jedis);
                    logger.info(username+"===>canLogin true");
                    return true;
                }
                sJedisPool.putbackConnection(jedis);
                logger.info(username+"===>canLogin false");
                return false;
            }catch(JedisConnectionException e){
                sJedisPool.repairConnection(jedis);
                logger.warn(e+"===>login : "+"redis connection down!");
                count ++;
                if(count >= 3){
                    sJedisPool.putbackConnection(jedis);
                    logger.info(username+"暂时无法访问redis===>canLogin false");
                    return false;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    logger.error(e1+"thread sleep is error in canLogin");
                }
            }
        }
        return false;
    }

    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

}
