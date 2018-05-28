package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.Vo.VoUser;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

public class GetPersonalInfoHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger = Logger.getLogger(GetPersonalInfoHandler.class.getName());

    public GetPersonalInfoHandler(SJedisPool sJedisPool){
        this.sJedisPool=sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject body = (JSONObject)msg;
        String type = body.getString(JsonKeyword.TYPE);
        if(type.equalsIgnoreCase(JsonKeyword.GET_PERSONAL_INFO)){
            String userName=body.getString(JsonKeyword.USERNAME);
            String sql="select * from user where username="+userName;
            VoUser voUser=GetPersonalInfoFromDB(sql);
            String json= JSON.toJSONString(voUser);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(json);
            ctx.writeAndFlush(jsonArray.toString());
        }else {
            ctx.fireChannelRead(msg);
        }

    }

    public VoUser GetPersonalInfoFromDB(String sql){
        VoUser voUser = new VoUser();
        try {
            ResultSet rs = ConnPoolUtil.select(sql);
            while(rs.next()){
                voUser.setUserName(rs.getString("userName"));
                voUser.setGender(rs.getInt("gender"));
                voUser.setAge(rs.getInt("age"));
                voUser.setNickName(rs.getString("nickName"));
                voUser.setAddress(rs.getString("address"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return voUser;
    }
}
