package com.yijiagou.server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.yijiagou.config.Configurator;
import com.yijiagou.handler.DownLoadHandler;
import com.yijiagou.pojo.MyLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by wangwei on 17-7-28.
 */
public class PSServer {
    private int port;
    private String host;
    private SJedisPool sJedisPool;
    private ConnectionFactory connecFac;

    private PSServer(){

    }

    private void init() throws Exception {
        Configurator.init();
        new MyLogger();
        this.sJedisPool = new SJedisPool(Configurator.getJPWorkMaxNum(),Configurator.getJPWorkMinNum(),
                Configurator.getJPHost(),Configurator.getJPPort());
        this.connecFac= new ConnectionFactory();
        connecFac.setHost("127.0.0.1");
        Thread thread = new Thread(new Listener(connecFac));
        thread.start();
    }


    public static PSServer newInstance() throws Exception {
        PSServer psServer = new PSServer();
        psServer.init();
        return psServer;
    }

    public PSServer bind(String host,int port){
        this.host = host;
        this.port = port;
        return this;
    }

    public void run(){
        EventLoopGroup bossgroup = new NioEventLoopGroup();
        EventLoopGroup workgroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();

            server.group(bossgroup, workgroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 102480)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializerImp(sJedisPool,connecFac));

            ChannelFuture future = server.bind(host,port).sync();

            future.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            bossgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }
    }

    public void close(){

    }
}
class Listener extends Thread{
    private ConnectionFactory connecFac;

    public Listener(ConnectionFactory connecFac){
        this.connecFac=connecFac;
    }
    @Override
    public void run() {
        consumer();
    }

    public void consumer(){
        QueueingConsumer consumer=null;
        try {
            consumer = getConsumer();
            while (true){
                QueueingConsumer.Delivery delivery = null;
                delivery = consumer.nextDelivery();
                String message= new String(delivery.getBody());
//                String messagewithoutHead[]=message.split("\\|");
                String retsessionId=message.substring(message.indexOf("\\|"),message.indexOf("*"));
                System.out.println("result sessionId:"+retsessionId);
                ChannelHandlerContext ctx=DownLoadHandler.getCtxs().get(retsessionId);
                if(ctx!=null){
                    ctx.writeAndFlush(message);

                }else {
                    ctx.writeAndFlush("2");
                }
                DownLoadHandler.getCtxs().remove(retsessionId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public QueueingConsumer getConsumer() throws IOException, InterruptedException {

        Connection con=connecFac.newConnection();
        Channel channel=con.createChannel();
        String exchangeName="exchange02";
        channel.exchangeDeclare(exchangeName,"direct");
        String queueName=channel.queueDeclare().getQueue();
        channel.queueBind(queueName,exchangeName,"type01");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName,true,consumer);

        return consumer;
    }
}


