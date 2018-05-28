package com.yijiagou.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.cdn.Upload;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangwei on 17-7-29.
 */
public class UploadHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(UploadHandler.class.getName());
    private SJedisPool sJedisPool;
    private static final String IF = "if";
    private static final String ELIF = "elif";
    private static final String ELSE = "else";
    private static final String WHILE = "while";
    private static final String END = "end";
    private static final String TRUE = "True";
    private static ReentrantLock lock;

    public UploadHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
        lock = new ReentrantLock();
    }

    private static final String retract = "    ";

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        JSONObject jsonObject = (JSONObject) msg;
        try {
            String type = jsonObject.getString(JsonKeyword.TYPE);
            if (type.equals(JsonKeyword.CODE)) {
                String result=jsonToCode(jsonObject);
                System.out.println("result====>"+result);
                ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
                logger.info("Upload ok");
            } else {
                ctx.fireChannelRead(msg);
            }
        } catch (JSONException e) {
            logger.error(e + "===>Upload error");
            ctx.writeAndFlush("error");
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Upload error");
            ctx.writeAndFlush("error");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public String jsonToCode(JSONObject jsonObject) throws Exception {
        try {
            JSONArray code = jsonObject.getJSONArray(JsonKeyword.CODE);
            String info = jsonObject.getString(JsonKeyword.INFO);
            String type = jsonObject.getString(JsonKeyword.DEVICETYPE);
            String userName= jsonObject.getString(JsonKeyword.USERNAME);
            String typeinfo=type+"info";
            Step step = new Step(0);
            StringBuffer codes = new StringBuffer();
            codes.append("from command.power import *\n");
            codes.append(getBlock(step, code, ""));
            int count = 0;
            Jedis jedis = sJedisPool.getConnection();

            String info1 = "";

            if(info.length() > 10)
                info1 = info.substring(0, 10);
            else
                info1 = info;

            Transaction t=null;
            while (count < 3) {//重连３次
                try {
                    long number = 0;
                    //保证编号唯一

                    lock.lock();
                    if (jedis.exists(type)) {
                        number = jedis.zcard(type);
                        System.out.println(number);
                    }
                    String appid=number+"";
                    t=jedis.multi();
                    t.zadd(type, 1,appid);
                    lock.unlock();
                    t.hset(typeinfo,number + "",info1+"...");
                    t.exec();

                    long uploadTime=System.currentTimeMillis();
                    String sql = "insert into appInfo(appid,deviceType,appInfo,uploadTime,username) values(?,?,?,?,?)";
                    shortInfo(sql,number+"",type,info,uploadTime,userName);

                    //----------------------ftp server请求 回复-----------------------------
                    StringBuffer sb = new StringBuffer();
                    sb.append(info);
//                    String filepath0="/code/"+type+"/"+number+".py";
//                    String infopath = "/info/"+type+"/"+number+".info";
//                    System.out.println(codes);
//                    System.out.println("xxxxxxxxxxxxxxxxxxtype:"+type);
//                    System.out.println(filepath0);
                    String filenamePy=number+".py";
                    String filenameInfo=number+".info";
//                    System.out.println("codes:"+codes);
//                    System.out.println("info:"+info);
//                    String result = Upload.uploadFile(filepath0,codes,infopath,info);

                    saveStringToFile(codes.toString(),"/opt/ftp/code/"+filenamePy);
//                    saveStringToFile(info,"/opt/ftp/info/"+filenameInfo);
                    FileInputStream input = new FileInputStream("/opt/ftp/code/"+filenamePy);
                    boolean result1 = Upload.uploadFile("127.0.0.1",2121,"test","123456","usr/share/ftp/",filenamePy,input);
//                    input=new FileInputStream("/opt/ftp/info/"+filenameInfo);
//                    boolean result2 =Upload.uploadFile("127.0.0.1",2121,"test","123456","usr/share/ftp/",filenameInfo,input);
//                    System.out.println("result1:"+result1);
//                    System.out.println("result2:"+result2);
                    if(result1==true){
                        return "1";
                    }else {
                        return "0";
                    }

                    //----------------------ftp server 请求 回复-----------------------------

                } catch (JedisConnectionException e) {
                    e.printStackTrace();
                    logger.error(e + "===>upload");
                    count++;
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        count++;
                    }
                }
            }

        } catch (JSONException e) {
            //json解析出错
            logger.error(e+"===>jsonToCode");
//            e.printStackTrace();
            return "error";

        }
        return "error";

    }

    public StringBuffer getBlock(Step step, JSONArray code, String retract) {
        StringBuffer block = new StringBuffer();
        try {
            String retract0 = this.retract;
            retract0 += retract;

            for (int i = step.getStep(); i < code.size(); i = step.getStep()) {
                JSONObject stateBlock = code.getJSONObject(i);
                String statement = stateBlock.getString(JsonKeyword.COUNT);//可能会改
                if (statement.equals(IF) || statement.equals(ELIF)) {
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" ");
                    step.addStep();

                    stateBlock = code.getJSONObject(step.getStep());
                    String methodCount = stateBlock.getString(JsonKeyword.COUNT);
                    String[] methodCounts = methodCount.split("\\|");
                    block.append("get");
                    block.append(methodCounts[1]);//可能会改
                    block.append("()");
                    if (methodCounts[1].equals("Time")) {
                        block.append(" == ");
                    }

                    if (methodCounts[2].equals("String"))
                        block.append("\"" + methodCounts[0] + "\"");
                    else
                        block.append(methodCounts[0]);
                    block.append(" :");
                    step.addStep();

                    block.append(this.getBlock(step, code, retract0));
                } else if (statement.equals(WHILE)) {
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" " + TRUE + " :");
                    step.addStep();
                    block.append(this.getBlock(step, code, retract0));
                } else if (statement.equals(ELSE)) {
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" :");
                    step.addStep();
                    block.append(this.getBlock(step, code, retract0));
                } else if (statement.equals(END)) {
                    step.addStep();
                    return block;
                } else {
                    block.append("\n");
                    block.append(retract);

                    String[] counts = statement.split("\\|");
                    block.append(counts[0]);
//                    System.out.println(step.getStep());
                    int methodNum = Integer.parseInt(counts[1]);
                    block.append("(");
                    for (int j = 0; j < methodNum; j++) {
                        step.addStep();
                        JSONObject argBlock = code.getJSONObject(step.getStep());
                        String arg = argBlock.getString(JsonKeyword.COUNT);
                        String[] args = arg.split("\\|");
                        if (args[2].equals("String"))
                            block.append("\"" + args[0] + "\"");
                        else
                            block.append(args[0]);

                        if (j != methodNum - 1) {
                            block.append(",");
                        }
                    }
                    block.append(")");
                    step.addStep();
                }
            }
        } catch (JSONException e) {
            logger.error(e + "===>upload");
            e.printStackTrace();
        }
        logger.info(code.toArray().toString() + "翻译成功===>" + block);
        return block;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    public int shortInfo(String sql,String appid,String deviceType,String info,long uploadTime,String userName){
        int count = 0;
        int a = 0;
        while (true) {
            try {
                a = ConnPoolUtil.updata(sql,appid, deviceType,info,uploadTime,userName);
                return a;
            } catch (Exception e) {
                logger.warn(e + "insertMysql");
                if (count++ >= 2) {
                    logger.error("访问数据库时无法提供服务===>insertMysql:"+a);
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

    public void saveStringToFile(String str,String fileName) throws IOException {
        File file = new File(fileName);
        FileWriter fw = new FileWriter(file);
        if(!file.exists()){
            file.createNewFile();
        }
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write(str, 0, str.length()-1);
        bw.flush();
        bw.close();
    }

    class Step {
        private int step;

        public Step(int step) {
            this.step = step;
        }

        public void addStep() {
            this.step += 1;
        }

        public int getStep() {
            return step;
        }
    }
}
