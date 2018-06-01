package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.Vo.VoFriend;
import com.yijiagou.code.Friend;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.util.Map;

public class GetFriendsHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static final String KEY = "FRIENDS";
    public GetFriendsHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }
    private static Logger logger = Logger.getLogger(GetFriendsHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        String userName="";
        if (actiontype.equals(JsonKeyword.GET_FRIENDS)) {
            String status = jsonObject.getString(JsonKeyword.FRIEND);
            userName = jsonObject.getString(JsonKeyword.USERNAME);
            logger.info("[getFriends,"+userName+",["+status+"],"+"获取所有好友,"+System.currentTimeMillis()+"]");
            Jedis jedis = null;
            jedis = sJedisPool.getConnection();
            Map<String, String> friends = jedis.hgetAll(userName+KEY);
            System.out.println(friends.size());
            sJedisPool.putbackConnection(jedis);
            JSONArray jsonArray = new JSONArray();
            String json = null;
            if (status.equals(Friend.NEWFRIEND.getValue())) {
                if (friends != null) {
                    for (String username : friends.keySet()) {
                        String valMessage="";
                        if(jedis.exists(username+userName)) {
                            valMessage = jedis.get(username + userName);
                        }
                        if (friends.get(username).equals("0")) {
                            VoFriend voFriend = getUsersFromDB(username);
                            voFriend.setValMessage(valMessage);
                            json = JSON.toJSONString(voFriend);
                            if(voFriend.getUserName().equals(userName)==false) {
                                jsonArray.add(json);
                            }
                        }
                    }
                }
            } else {
                if (friends != null) {
                    for (String username : friends.keySet()) {
                        if (friends.get(username).equals("1")) {
                            VoFriend voFriend = getUsersFromDB(username);
                            json = JSON.toJSONString(voFriend);
//                            System.out.println(json);
                            if(voFriend.getUserName().equals(userName)==false) {
                                jsonArray.add(json);
                            }
                        }
                    }
                }
            }
            System.out.println(jsonArray.toString());
            ctx.writeAndFlush(jsonArray.toString());
        }else {
            ctx.fireChannelRead(msg);
        }

    }


    public VoFriend getUsersFromDB(String userName) {
        String sql = "select * from user where userName=?";
        VoFriend voFriend = new VoFriend();
        try {
            ResultSet rs = ConnPoolUtil.select(sql,userName);
            while (rs.next()) {
                    voFriend.setNickName(rs.getString("nickName"));
                    voFriend.setUserName(rs.getString("userName"));
                    voFriend.setAddress(rs.getString("address"));
                    voFriend.setAge(rs.getInt("age"));
                    voFriend.setGender(rs.getString("gender"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return voFriend;
    }
}
