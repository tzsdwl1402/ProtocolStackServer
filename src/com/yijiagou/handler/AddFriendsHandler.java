package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.code.AddFriendStatus;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class AddFriendsHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(AddDeviceHandler.class.getName());
    private static final String KEY = "FRIENDS";
    private static final String STATUS1 = "1";
    private static final String STATUS0 = "0";

    private SJedisPool sJedisPool;

    public AddFriendsHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.ADD_FRIENDS)) {
            String userName = jsonObject.getString(JsonKeyword.USERNAME);
            String friend = jsonObject.getString(JsonKeyword.FRIEND);
            String status = jsonObject.getString(JsonKeyword.STATUS);
            Jedis jedis = null;
            jedis = sJedisPool.getConnection();
            int ret = 0;
//            if (jedis.hexists(JsonKeyword.USERS, friend) == true) {
//                ctx.writeAndFlush("3");// already invite;
//            } else {
                ret = saveToCache(jedis, userName, friend, status);
//            }
            sJedisPool.putbackConnection(jedis);
            if(ret==3){
                ctx.writeAndFlush("3");//already invite;
            }else if(ret==1){
                ctx.writeAndFlush("1");
                saveRelationToDB(userName,friend,STATUS1);
            }else if(ret==2){
                ctx.writeAndFlush("2");//invite failed
            }

            ctx.writeAndFlush("1");
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    public int saveToCache(Jedis jedis, String user1, String user2, String status) {

        try {
            if (status.equals(AddFriendStatus.INVITE.getValue())) {
                if(jedis.hexists(user1+KEY,user2)){
                    return 3;//already invite
                }
                try{
                    jedis.hset(user1 + KEY, user2, STATUS0);
                    jedis.hset(user2 + KEY, user1, STATUS0);
                }catch (Exception e){
                    System.out.println(e);
                }

            }
            if (status.equals(AddFriendStatus.AGREE.getValue())) {
                jedis.hset(user1 + KEY, user2, STATUS1);
                jedis.hset(user2 + KEY, user1, STATUS1);
            }

            return 1;//success;
        } catch (Exception e) {
            return 2; //failed
        }

    }

    public int saveRelationToDB(String user1, String user2, String status) {
        String sql = "insert into friends(user1,user2,status) values(?,?,?)";
        int a = 0;
        try {
            a = ConnPoolUtil.updata(sql, user1, user2, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;

    }
}
