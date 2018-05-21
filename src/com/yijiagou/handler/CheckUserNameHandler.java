package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zgl on 17-9-6.
 */
public class CheckUserNameHandler extends ChannelHandlerAdapter {

    private SJedisPool sJedisPool;
    private ReentrantLock lock = new ReentrantLock();
    private static Logger logger = Logger.getLogger(CheckUserNameHandler.class.getName());

    public CheckUserNameHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        if (jsonObject.get(JsonKeyword.TYPE).equals(JsonKeyword.CHECKUSERNAME)) {
            String username = (String) jsonObject.get(JsonKeyword.USERNAME);
            String status = jedisGetUsernaem(username);
            logger.info(username+"后台检查是否用户存在返回值===>"+status);
            ctx.writeAndFlush(status);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private String jedisGetUsernaem(String username) {

        Jedis jedis = null;
        int count = 0;
        jedis = sJedisPool.getConnection();
        lable:
        while (true) {
            try {
                lock.lock();
                if (jedis.hexists(JsonKeyword.USERS, username)) {
                    sJedisPool.putbackConnection(jedis);
                    lock.unlock();
                    logger.info(username+"访问redis完返回值===>jedisGetUsernaem 0");
                    return "0";
                } else {
                    /* 获取验证码*/
                    String checkcode = "aaaaaa";
                    jedis.set(username, checkcode);
                    jedis.expire(username, 60);
                    sJedisPool.putbackConnection(jedis);
                    lock.unlock();
                    logger.info(username+"访问redis完返回值===jedisGetUsernaem 1");
                    return "1";
                }
            } catch (Exception e) {
                logger.warn(e+username+"访问redis===>jedisLpush");
                if (count++ >= 2) {
                    logger.error(username+"访问redis时，redis不能提供服务===》2");
                    return "2";
                } else {
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        logger.error(e1 + "thread is error in jedisLpush");
                    }
                    continue;
                }
            }
        }
    }
}
