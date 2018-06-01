package com.yijiagou.handler;

import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zgl on 17-7-28.
 */
//  String string ="{\"type\":\"register\",\"username\":\"xxxxx\",\"passwd\":\"xxxxxx\",\"phone\":\"21865165\"}";
public class RegisterHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private ReentrantLock lock = new ReentrantLock();

    public RegisterHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    private static Logger logger = Logger.getLogger(RegisterHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        if (jsonObject.get(JsonKeyword.TYPE).equals(JsonKeyword.REGIST)) {
            String username = (String) jsonObject.get(JsonKeyword.USERNAME);
            String passwd = (String) jsonObject.get(JsonKeyword.PASSWORD);
            String checkcode = (String) jsonObject.get(JsonKeyword.CHECKCODE);
            String state = jedisCheckUser(username, passwd, checkcode);
            logger.info("[register,"+username+",["+username+","+passwd+","+checkcode+"],"+"用户注册,"+System.currentTimeMillis()+"]");
            logger.info(username+"后端处理完得到的值===>RegisterHandler:channelRead"+state);
            ctx.writeAndFlush(state);
            if (state.equals("1")) {

                String sql ="insert into user(userName,passWord) values(?,?)";
//                String sql = "update user set userName=?,passWord=?";
                mysqlAdd(sql, username, passwd);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private String jedisCheckUser(String username, String passwd, String checkcode) {

        Jedis jedis = null;
        int count = 0;
        jedis = sJedisPool.getConnection();
        lable:
        while (true) {
            try {
                lock.lock();
                if (jedis.hexists(JsonKeyword.USERS, username)) {
                    if(jedis.exists(username)){
                        jedis.del(username);
                    }
                    sJedisPool.putbackConnection(jedis);
                    logger.info(username + "业务处理完返回值===>jedisCheckUser 2");
                    return "2";
                } else {
                    String code = jedis.get(username);
                    System.out.println("code:"+code);
                    if (code == null) {
                        if(jedis.exists(username)){
                            jedis.del(username);
                        }
                        sJedisPool.putbackConnection(jedis);
                        logger.info(username+"业务处理完返回值===>jedisCheckUser 0");
                        return "0";
                    } else if (code.equals(checkcode)) {
                        System.out.println("hello");
                        jedis.hset(JsonKeyword.USERS, username, passwd);
                        if(jedis.exists(username)){
                            jedis.del(username);
                        }
                        sJedisPool.putbackConnection(jedis);
                        logger.info(username+"业务处理完返回值===>jedisCheckUser 1");
                        return "1";
                    } else {
                        logger.info(username+"业务处理完返回值===>jedisCheckUser 3");
                        return "3";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e +username+ "===>jedisLpush");
                if (count++ >= 2) {
                    if(jedis.exists(username)){
                        jedis.del(username);
                    }
                    logger.error(username+"暂时不能访问redis===>jedisLpush 4");
                    return "4";
                } else {
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        logger.error(e1 + "thread is error in jedisLpush");
                    }
                    continue;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private int mysqlAdd(String sql, String username, String passwd) {
        int a = 0;
        int count = 0;
        while (true) {
            try {
                a = ConnPoolUtil.updata(sql, username, passwd);
                logger.info(username+"通过mysqlAdd访问数据库返回结果"+a);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e +username+ "===>msqlAdd");
                if (count++ >= 2) {
                    logger.error(username+"暂时不能访问数据库===>mysqlAdd "+a);
                    return a;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    logger.error(e1 + "thread sleep is error in mysqlAdd");
                }
                continue;
            }
        }
        return a;
    }
}
