package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.Vo.VoUser;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

public class ImprovePersonalInfoHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;
    private static Logger logger = Logger.getLogger(ImprovePersonalInfoHandler.class.getName());

//    public ImprovePersonalInfoHandler(SJedisPool sJedisPool){
//        this.sJedisPool=sJedisPool;
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject body = (JSONObject)msg;
        String type = body.getString(JsonKeyword.TYPE);
        VoUser voUser = new VoUser();
        if(type.equalsIgnoreCase(JsonKeyword.IMPROVE_PERSONAL_INFO)){
           voUser.setUserName(body.getString(JsonKeyword.USERNAME));
           voUser.setNickName(body.getString(JsonKeyword.NICKNAME));
           voUser.setAge(Integer.parseInt(body.getString(JsonKeyword.AGE)));
           voUser.setGender(Integer.parseInt(body.getString(JsonKeyword.GENDER)));
           voUser.setAddress(body.getString(JsonKeyword.ADDRESS));
//           voUser.setImageData(body.getString(JsonKeyword.IMGDATA));
            logger.info("[improvePersonalInfo,"+JsonKeyword.USERNAME+",["+JsonKeyword.USERNAME+","+JsonKeyword.NICKNAME+","+JsonKeyword.AGE+","+JsonKeyword.GENDER+"]"+",完善用户个人信息,"+System.currentTimeMillis()+"]");
            String sql="update user set nickName=?,age=?,gender=?,address=? where userName=?";
            int ret=saveInfoToDB(voUser,sql);
            if(ret>=0){
                ctx.writeAndFlush("1");//success
            }else {
                ctx.writeAndFlush("2");//failed
            }
        }else {
            ctx.fireChannelRead(msg);
        }

    }

    private int saveInfoToDB(VoUser voUser, String sql){
        int a=0;
        try {
            a= ConnPoolUtil.updata(sql, voUser.getNickName(),voUser.getAge(),voUser.getGender(),voUser.getAddress(),voUser.getUserName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }
}
