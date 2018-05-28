package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.Vo.VoAppDetailInfo;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

public class ShowDetailAppInfo  extends ChannelHandlerAdapter {

    private static Logger logger =Logger.getLogger(ShowDetailAppInfo.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        if (jsonObject.get(JsonKeyword.TYPE).equals(JsonKeyword.APP_DETAIL_INFO)) {
//            String userName=jsonObject.getString(JsonKeyword.USERNAME);
            String appId=jsonObject.getString(JsonKeyword.APPID);
            VoAppDetailInfo voAppDetailInfo = getDetailInfoFromDB(appId);
            JSONArray jsonArray = new JSONArray();
            String json= JSON.toJSONString(voAppDetailInfo);
            jsonArray.add(json);
            ctx.writeAndFlush(jsonArray.toString());
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    public VoAppDetailInfo getDetailInfoFromDB(String appId){
        String sql = "select * from appInfo where appId="+appId;
        VoAppDetailInfo voAppDetailInfo = new VoAppDetailInfo();
        try {
            ResultSet rs = ConnPoolUtil.select(sql);
            while(rs.next()){
                String userName=rs.getString("username");
                String nickName=getNickNameFromDB(userName);
                voAppDetailInfo.setUploadTime(rs.getString("uploadTime"));
                voAppDetailInfo.setDetailInfo(rs.getString("appInfo"));
                voAppDetailInfo.setNickName(nickName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return voAppDetailInfo;
    }

    public String getNickNameFromDB(String username){
        String sql="select * from user where userName = "+username;
        String nickName="";
        try {
            ResultSet rs = ConnPoolUtil.select(sql);
            while (rs.next()){
                nickName=rs.getString("nickName");
            }
        }catch (Exception e){
            return null;
        }
        return nickName;
    }
}
