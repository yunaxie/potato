package org.mengdadou.net.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mengdadou.net.event.active.ActiveWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mengdadou on 16-11-4.
 */
public class ServerActiveHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(ServerActiveHandler.class);
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("{} exception {}", ctx, cause.getMessage());
        cause.printStackTrace();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("active {}", ctx);
        if (!ActiveWrapper.active(ctx)) ctx.close();
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("inactive {}", ctx);
        ActiveWrapper.inactive(ctx);
    }
}
