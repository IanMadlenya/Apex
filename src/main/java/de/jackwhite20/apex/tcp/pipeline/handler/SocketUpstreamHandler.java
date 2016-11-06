/*
 * Copyright (c) 2016 "JackWhite20"
 *
 * This file is part of Apex.
 *
 * Apex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jackwhite20.apex.tcp.pipeline.handler;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.tcp.ApexSocket;
import de.jackwhite20.apex.util.BackendInfo;
import de.jackwhite20.apex.util.ChannelUtil;
import de.jackwhite20.apex.util.PipelineUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class SocketUpstreamHandler extends ChannelHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SocketUpstreamHandler.class);

    private BackendInfo backendInfo;

    private Channel downstreamChannel;

    public SocketUpstreamHandler(BackendInfo backendInfo) {

        this.backendInfo = backendInfo;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        final Channel inboundChannel = ctx.channel();

        Bootstrap b = new Bootstrap()
                .group(inboundChannel.eventLoop())
                .channel(PipelineUtils.getChannel())
                .handler(new SocketDownstreamHandler(inboundChannel))
                .option(ChannelOption.TCP_NODELAY, true)
                // No initial connection should take longer than 4 seconds
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, BackendInfo.DEFAULT_TCP_TIMEOUT)
                .option(ChannelOption.AUTO_READ, false);

        ChannelFuture f = b.connect(backendInfo.getHost(), backendInfo.getPort());
        downstreamChannel = f.channel();
        f.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) {

                if (future.isSuccess()) {
                    inboundChannel.read();
                } else {
                    inboundChannel.close();
                }
            }
        });

        // Add the channel to the channel group
        Apex.getChannelGroup().add(inboundChannel);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        if (downstreamChannel.isActive()) {
            downstreamChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) {

                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        ChannelUtil.closeOnFlush(downstreamChannel);

        ApexSocket.getBalancingStrategy().disconnectedFrom(backendInfo);

        logger.debug("Disconnected [{}] <-> [{}:{} ({})]", ctx.channel().remoteAddress(), backendInfo.getHost(), backendInfo.getPort(), backendInfo.getName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        ChannelUtil.closeOnFlush(ctx.channel());

        // Ignore IO and timeout related exceptions
        if (!(cause instanceof IOException) && !(cause instanceof TimeoutException)) {
            logger.error(cause.getMessage(), cause);
        }
    }
}
