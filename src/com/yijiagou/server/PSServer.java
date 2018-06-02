package com.yijiagou.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.yijiagou.Vo.VoDownLoadResult;
import com.yijiagou.config.Configurator;
import com.yijiagou.config.EnvironmentUtil;
import com.yijiagou.handler.DownLoadHandler;
import com.yijiagou.pojo.MyLogger;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangwei on 17-7-28.
 */
public class PSServer {
    private int port;
    private String host;
    private SJedisPool sJedisPool;
    private ConnectionFactory connecFac;
    private static EnvironmentUtil sysenv= new EnvironmentUtil("config/mq.properties");

    private PSServer() {

    }

    private void init() throws Exception {
        Configurator.init();
        new MyLogger();
        this.sJedisPool = new SJedisPool(Configurator.getJPWorkMaxNum(), Configurator.getJPWorkMinNum(),
                Configurator.getJPHost(), Configurator.getJPPort());
        this.connecFac = new ConnectionFactory();
        String host=sysenv.getPropertyValue("mq.host");
        Integer port=Integer.parseInt(sysenv.getPropertyValue("mq.port"));
        String userName=sysenv.getPropertyValue("mq.user");
        String passWord=sysenv.getPropertyValue("mq.password");
        connecFac.setHost(host);
        connecFac.setPort(port);
        connecFac.setUsername(userName);
        connecFac.setPassword(passWord);
        Thread thread = new Thread(new Listener(connecFac));
        thread.start();
    }


    public static PSServer newInstance() throws Exception {
        PSServer psServer = new PSServer();
        psServer.init();
        return psServer;
    }

    public PSServer bind(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public void run() {
        EventLoopGroup bossgroup = new NioEventLoopGroup();
        EventLoopGroup workgroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();

            server.group(bossgroup, workgroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 102480)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializerImp(sJedisPool, connecFac));

            ChannelFuture future = server.bind(host, port).sync();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            bossgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }
    }

    public void close() {

    }
}

class Listener extends Thread {
    private ConnectionFactory connecFac;

    public Listener(ConnectionFactory connecFac) {
        this.connecFac = connecFac;
    }

    @Override
    public void run() {
        consumer();
    }

    public void consumer() {
        QueueingConsumer consumer = null;
        Map<String, Map<String, String>> statusMap = new HashMap<>();
        try {
            consumer = getConsumer();
            while (true) {
                QueueingConsumer.Delivery delivery = null;
                delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                System.out.println("result Message:"+message);
                String messages[] = message.split("\\*");
                String deviceid = messages[2];
                String sessionid = messages[1];
                String status = messages[3];
                //result*sessionid*deviceid*status
                if (messages[0].equals("result")) {
                    Map<String, String> deviceIdstatus = new HashMap<>();
                    if (!deviceIdstatus.containsKey(deviceid)) {
                        deviceIdstatus.put(deviceid, status);
                    }
                    if(!statusMap.containsKey(sessionid)){
                        statusMap.put(sessionid, deviceIdstatus);
                    }else {
                        statusMap.get(sessionid).put(deviceid,status);
                    }

                    Map<String, String> map = DownLoadHandler.getDeviceIdAndSessionMap();
                    if (map.containsKey(deviceid) && map.get(deviceid).equals(sessionid)) {
                        map.remove(deviceid);
                        if (map.size()==0) {
//                            statusMap.put()
                            Map<String, String> maps = statusMap.get(sessionid);
                            System.out.println("map size:"+maps.size());
                            JSONArray jsonArray = new JSONArray();
                            String json = null;
                            for (String deviceId : maps.keySet()) {
                                VoDownLoadResult voDownLoadResult = new VoDownLoadResult(deviceId, maps.get(deviceId));
                                json = JSON.toJSONString(voDownLoadResult);
                                jsonArray.add(json);
                            }
                            ChannelHandlerContext ctx = DownLoadHandler.getCtxs().get(sessionid);
                            if (ctx != null) {
                                System.out.println(jsonArray.toString());
                                ctx.writeAndFlush(jsonArray.toString());
                            } else {
                                ctx.writeAndFlush("2");//error result;
                            }
                            statusMap.remove(sessionid);
                            DownLoadHandler.getCtxs().remove(sessionid);
                        }
                    }
//                    else {
//                        Map<String, String> maps = statusMap.get(sessionid);
////                        String statuss = "{\"status\":\"";
//                        JSONArray jsonArray = new JSONArray();
//                        String json = null;
//                        for (String deviceId : maps.keySet()) {
////                            statuss += maps.get(devid) + "#";
////                            statuss+="{\"deviceId\":\""+devid+"\""+"\"status\":"+
//                            VoDownLoadResult voDownLoadResult = new VoDownLoadResult(deviceId,maps.get(deviceId));
//                            json = JSON.toJSONString(voDownLoadResult);
//                            jsonArray.add(json);
//                        }
////                        String result = statuss.substring(0, statuss.length() - 1) + "\"}";
//                        ChannelHandlerContext ctx = DownLoadHandler.getCtxs().get(sessionid);
//                        if (ctx != null) {
//                            ctx.writeAndFlush(jsonArray.toString());
//                        } else {
//                            ctx.writeAndFlush("2");//error result;
//                        }
//                        DownLoadHandler.getCtxs().remove(sessionid);
//                    }
                }
//                String retsessionId=message.substring(message.indexOf("\\|"),message.indexOf("*"));
//                System.out.println("result sessionId:"+retsessionId);
//                ChannelHandlerContext ctx=DownLoadHandler.getCtxs().get(retsessionId);
//                if(ctx!=null){
//                    ctx.writeAndFlush(message);
//
//                }else {
//                    ctx.writeAndFlush("2");
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public QueueingConsumer getConsumer() throws IOException, InterruptedException {

        Connection con = connecFac.newConnection();
        Channel channel = con.createChannel();
        String exchangeName = "exchange02";
        channel.exchangeDeclare(exchangeName, "direct");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, "type01");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        return consumer;
    }
}


