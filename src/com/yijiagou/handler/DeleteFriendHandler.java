package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DeleteFriendHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(DeleteFriendHandler.class.getName());
    private SJedisPool sJedisPool;
    private static final String KEY = "FRIENDS";

    public DeleteFriendHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.DELETE_FRIENDS)) {
            String userName = jsonObject.getString("userName");
            String friend = jsonObject.getString("friend");
            logger.info("[deleteFriends,"+userName+",["+friend+"],"+"删除朋友,"+System.currentTimeMillis()+"]");
            Jedis jedis = null;
            jedis = sJedisPool.getConnection();
            if (jedis.hexists(userName + KEY, friend)) {
                try {
                    jedis.hdel(userName + KEY, friend);
                    jedis.hdel(friend + KEY, userName);
                    int a=deleteFromDB(userName, friend);
                    int b=deleteFromDB(friend, userName);
                    sJedisPool.putbackConnection(jedis);
                    if(a>=0&&b>=0){
                        ctx.writeAndFlush("1");//success
                    }
                } catch (Exception e) {
                    ctx.writeAndFlush("2");//failed
                    sJedisPool.repairConnection(jedis);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    public int deleteFromDB(String user1, String user2) {
        String sql = "delete from friends where user1=? and user2=?";
        int a = 0;
        try {
            a = ConnPoolUtil.updata(sql, user1, user2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }


}
