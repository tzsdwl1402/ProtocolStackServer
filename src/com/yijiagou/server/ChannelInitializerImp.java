package com.yijiagou.server;

import com.rabbitmq.client.ConnectionFactory;
import com.yijiagou.handler.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import com.yijiagou.tools.JedisUtils.SJedisPool;

/**
 * Created by wangwei on 17-8-19.
 */
public class ChannelInitializerImp extends ChannelInitializer<NioSocketChannel> {
    private SJedisPool sJedisPool;
    private ConnectionFactory connecFac;

    public ChannelInitializerImp(SJedisPool sJedisPool,ConnectionFactory connecFac){
        this.sJedisPool = sJedisPool;
        this.connecFac = connecFac;
    }
    @Override
    protected void initChannel(NioSocketChannel channel) throws Exception {
        channel.pipeline().addLast(new HttpRequestDecoder());//inbound
        channel.pipeline().addLast(new HttpObjectAggregator(65536));//inbound
        channel.pipeline().addLast(new HttpResponseEncoder());//outbound
        channel.pipeline().addLast(new HttpHeadHandler());//outbound
        channel.pipeline().addLast(new HttpContentHandler());//inbound
        channel.pipeline().addLast(new RegisterHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new LoginHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new UploadHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new DownLoadHandler(sJedisPool,connecFac));//inbound outbound
        channel.pipeline().addLast(new ShowAppStoreHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new BindDeviceHandler(sJedisPool));
        channel.pipeline().addLast(new CheckUserNameHandler(sJedisPool));
        channel.pipeline().addLast(new MobileCheckUserNameHandler(sJedisPool));
        channel.pipeline().addLast(new GetUserDeviceHandler(sJedisPool));
        channel.pipeline().addLast(new ImprovePersonalInfoHandler());
        channel.pipeline().addLast(new GetPersonalInfoHandler(sJedisPool));
        channel.pipeline().addLast(new DeleteDeviceHandler(sJedisPool));
        channel.pipeline().addLast(new AddDeviceHandler());
        channel.pipeline().addLast(new AddFriendsHandler(sJedisPool));
        channel.pipeline().addLast(new GetFriendsHandler(sJedisPool));
        channel.pipeline().addLast(new DeleteFriendHandler(sJedisPool));
        channel.pipeline().addLast(new LeaveMessageHandler(sJedisPool));
        channel.pipeline().addLast(new GetMessageHandler(sJedisPool));
        channel.pipeline().addLast(new DeleteMessageHandler(sJedisPool));
        channel.pipeline().addLast(new ShowDetailAppInfo());
    }
}
