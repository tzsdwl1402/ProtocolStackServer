package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.pojo.UserAndDevice;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;


/**
 * Created by zgl on 17-8-15.
 */
public class AddDeviceHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger = Logger.getLogger(AddDeviceHandler.class.getName());

    public AddDeviceHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.ADDDEVICE)) {
            String username = (String) jsonObject.get(JsonKeyword.USERNAME);
            String deviceid = (String) jsonObject.get(JsonKeyword.DEVICEID);
            String devicetype = (String) jsonObject.get(JsonKeyword.DEVICETYPE);
            UserAndDevice userAndDevice = new UserAndDevice(username, deviceid, devicetype);
            String a = this.insertUsersDevice(userAndDevice);
            //1:该设备已经绑定到用户
            //2:绑定成功
            //3:绑定失败
            logger.info(username + "后端处理完得到的值===》addDeviceHandler:channelRead" + a);
            ctx.writeAndFlush(a);
            if (a.equals("1") == false && a.equals("3") == false) {
                String sql = "insert into devicebind values(?,?,?)";
                this.insertMysql(sql, userAndDevice);
            }
        } else {
            ctx.fireChannelRead(msg);
        }

    }

    private String insertUsersDevice(UserAndDevice userAndDevice) {
        Jedis jedis = null;
        int count = 0;
        String username = userAndDevice.getUname();
        String deviceid = userAndDevice.getDeviceid();
        String devicetype = userAndDevice.getDevicetype();

        jedis = sJedisPool.getConnection();
        while (true) {
            try {
                System.out.println(username);
                System.out.println(deviceid);
                System.out.println(jedis.exists(username));
                if(jedis.exists(username)) {
                    if (jedis.hexists(username, deviceid)) {
                        sJedisPool.putbackConnection(jedis);
                        logger.info(username + "业务处理完返回值===>insertUsersDevice 1");
                        return "1";
                    }
                }
                jedis.hset(username, deviceid, devicetype);
                sJedisPool.putbackConnection(jedis);
                logger.info(username + "业务处理完返回值===>insertUsersDevice 2");
                return "2";
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e +username+ "===>insertUserDevice");
                if (count++ >= 2) {
                    logger.error(username+"暂时无法访问redis===>insertUsersDevice 3");
                    return "3";
                } else {
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        logger.error(e1 + "===>insertUserDevice:jedis is error");
                    }
                    continue;
                }
            }
        }

    }

    private int insertMysql(String sql, UserAndDevice userAndDevice) {
        int count = 0;
        int a = 0;
        while (true) {
            try {
                a = ConnPoolUtil.updata(sql, userAndDevice.getUname(), userAndDevice.getDeviceid(), userAndDevice.getDevicetype());
                logger.info(userAndDevice.getUname() + "数据库更新完返回值===>insertMysql" + a);
                return a;
            } catch (Exception e) {
                logger.warn(e + "insertMysql");
                if (count++ >= 2) {
                    logger.error(userAndDevice.getUname() + "访问数据库时无法提供服务===>insertMysql"+a);
                    return a;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    logger.error("insertMysql" + e1);
                }
                continue;
            }
        }

    }
}
