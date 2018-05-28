package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.pojo.LeaveMessage;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.security.Key;

public class LeaveMessageHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(LeaveMessageHandler.class.getName());
    private SJedisPool sJedisPool;
    private static String KEY="MESS";

    public LeaveMessageHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.LEAVE_MESSAGE)) {
            String userName=jsonObject.getString(JsonKeyword.USERNAME);
            String receiver=jsonObject.getString(JsonKeyword.RECEIVER);
            String message=jsonObject.getString(JsonKeyword.MESSAGE);
            LeaveMessage lm=new LeaveMessage();
            lm.setSender(userName);
            lm.setReceiver(receiver);
            lm.setMessage(message);
            Jedis jedis = null;
            jedis = sJedisPool.getConnection();
            int ret = 0;
            ret = saveToCache(jedis,lm);
            if(ret==1){
                long addTime=System.currentTimeMillis();
                lm.setAddTime(addTime);
                saveToDB(lm);
            }

        }else {
            ctx.fireChannelRead(msg);
        }
    }

    public int saveToCache(Jedis jedis,LeaveMessage lm){
        try{
            long time=System.currentTimeMillis();
            System.out.println("time:"+time);
            jedis.hset(lm.getReceiver()+KEY,lm.getSender()+"|"+time,lm.getMessage());
            sJedisPool.repairConnection(jedis);
            return 1;//success
        }catch (Exception e){
            System.out.println(e);
            sJedisPool.repairConnection(jedis);
            return 2;//failed
        }
    }

    public int saveToDB(LeaveMessage lm){
        String sql="insert into message(sender,receiver,message,addTime) values(?,?,?,?)";
        int a = 0;
        try {
            a = ConnPoolUtil.updata(sql, lm.getSender(), lm.getReceiver(), lm.getMessage(),lm.getAddTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }
}
