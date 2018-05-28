package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.code.Find;
import com.yijiagou.pojo.Device;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

public class AddDeviceHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(AddDeviceHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        Device device = new Device();
        if(actiontype.equals(JsonKeyword.ADDDEVICE)){
              String deviceId=(String) jsonObject.get(JsonKeyword.DEVICEID);
              int ret=checkDeviceId(deviceId);
              if (ret== Find.EXIST.getValue()){
                  ctx.writeAndFlush("3"); //exist
              }else {
                  String deviceType = (String) jsonObject.get(JsonKeyword.DEVICETYPE);
                  String brand = (String) jsonObject.get(JsonKeyword.BRAND);
                  String modelNumber = (String) jsonObject.get(JsonKeyword.MODEL_NUMBER);
                  String mAddress = (String) jsonObject.get(JsonKeyword.MADDRESS);
                  device.setDeviceId(deviceId);
                  device.setDeviceType(deviceType);
                  device.setBrand(brand);
                  device.setModelNumber(modelNumber);
                  device.setmAddress(mAddress);
                  String sql = "insert into device(deviceId,deviceType,brand,modelNumber,mAddress) values(?,?,?,?,?)";
                  int a = saveDeviceToDB(sql, device);
                  if (a >= 0) {
                      ctx.writeAndFlush("1");//success
                  } else {
                      ctx.writeAndFlush("2");//failed
                  }
              }
        }else {
            ctx.fireChannelRead(msg);
        }

    }

    public int checkDeviceId(String deviceId){
        String sql="select count(*) from device where deviceId="+deviceId;

        try {
            int count=ConnPoolUtil.selectCount(sql);
            if(count==0){
                return Find.NOT_EXIST.getValue();//not exist
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Find.EXIST.getValue();//exist
    }

    public int saveDeviceToDB(String sql,Device device){
         int a=0;
        try {
            a = ConnPoolUtil.updata(sql, device.getDeviceId(),device.getDeviceType(),device.getBrand(),device.getModelNumber(),device.getmAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }
}
