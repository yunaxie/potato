package org.mengdadou.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.mengdadou.net.NettyManager;
import org.mengdadou.net.client.handler.ClientInHandler;
import org.mengdadou.net.codec.MsgDecoder;
import org.mengdadou.net.codec.MsgEncoder;
import org.mengdadou.net.hb.ClientHbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by mengdadou on 16-11-3.
 */
public class NettyClient {
    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private volatile Channel channel;

    public NettyClient(String host) {
        connect(host, NettyManager.me().getDEFAULT_PORT());
    }

    private EventLoopGroup workGroup;

    private void connect(String host, int port) {
        try {
            workGroup = new NioEventLoopGroup();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)// 保活
                    .option(ChannelOption.TCP_NODELAY, false)// 有数据就发
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, NettyManager.me().getCONNECT_TIME_OUT()) // 连接超时时间
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MsgDecoder(NettyManager.me().getMSG_MAX_LEN()
                                    , NettyManager.me().getMSG_LEN_OFFSET(), NettyManager.me().getMSG_LEN_FIELD()
                                    , NettyManager.me().getMSG_LEN_ADJUSTMENT()));
                            ch.pipeline().addLast(new MsgEncoder());
                            ch.pipeline().addLast(new IdleStateHandler(0, NettyManager.me().getIDLE_TIME_OUT(), 0, TimeUnit.MILLISECONDS));
                            ch.pipeline().addLast(new ClientHbHandler());
                            ch.pipeline().addLast(new ClientInHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.channel = future.channel();

            logger.info("client connH ok {} - {}", host, port);
            this.channel.closeFuture().addListener(a -> {
                logger.info("client connH close {} - {}", host, port);
                // 关闭资源
                this.close();
            });
        } catch (Exception e) {
            logger.error("client connH exception : {} - {} / {}", host, port, e.getMessage());
            // 关闭资源
            this.close();
            e.printStackTrace();
        }
    }

    public Channel channel() {
        return this.channel;
    }

    private void close() {
        workGroup.shutdownGracefully();
        logger.info("client close over");
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.channel.id().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NettyClient)) {
            return false;
        }
        NettyClient p = (NettyClient) obj;
        return p.channel.equals(this.channel);
    }

}
