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
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.util.Map;

public class GetFriendsHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static final String KEY = "FRIENDS";
    public GetFriendsHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.GET_FRIENDS)) {
            String status = jsonObject.getString(JsonKeyword.FRIEND);
            String userName = jsonObject.getString(JsonKeyword.USERNAME);
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
                        if (friends.get(username).equals("0")) {
                            VoFriend voFriend = getUsersFromDB(userName);
                            json = JSON.toJSONString(voFriend);
                            jsonArray.add(json);
                        }
                    }
                }
            } else {
                if (friends != null) {
                    for (String username : friends.keySet()) {
                        if (friends.get(username).equals("1")) {
                            VoFriend voFriend = getUsersFromDB(userName);
                            json = JSON.toJSONString(voFriend);
                            jsonArray.add(json);
                        }
                    }
                }
            }
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
