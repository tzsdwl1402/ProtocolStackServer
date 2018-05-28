package com.yijiagou.mqServer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

public class Test {

    private static ConnectionFactory connecFac=new ConnectionFactory();
    public Test(){

    }

    public static ConnectionFactory getInstance(){
        return connecFac;
    }

    public static void main(String[] args) throws IOException,InterruptedException{
        Test test = new Test();
        try {
            test.consumer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public QueueingConsumer getConsumer() throws IOException,InterruptedException{
        ConnectionFactory connecFac=Test.getInstance();
        connecFac.setHost("127.0.0.1");
        Connection conn = connecFac.newConnection();
        Channel channel = conn.createChannel();
        String exchangeName="exchange03";
        channel.exchangeDeclare(exchangeName,"topic");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, "#.type01");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
        return consumer;
    }

    public String consumer() throws Exception{
        while (true) {
            QueueingConsumer consumer = null;
            consumer = getConsumer();
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String msg = new String(delivery.getBody());
            System.out.println(msg);
            producer(msg);//msg is handler result
        }


    }
    public void producer(String msg) throws Exception{
        ConnectionFactory connFac = Test.getInstance();
        Connection con = connFac.newConnection();
        Channel channel = con.createChannel();
        String exchangeName2 = "exchange02";
        String messageType = "type01";
        channel.exchangeDeclare(exchangeName2, "direct");
        channel.basicPublish(exchangeName2, messageType, null, msg.getBytes());
    }
}
