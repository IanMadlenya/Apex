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

package de.jackwhite20.apex.http.pipeline.handler;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.util.BackendInfo;
import de.jackwhite20.apex.util.ChannelUtil;
import de.jackwhite20.apex.util.PipelineUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.timeout.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by JackWhite20 on 08.01.2017.
 */
public class HttpProxyUpstreamHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpProxyUpstreamHandler.class);

    private Channel downstreamChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        ctx.channel().read();

        // Add the channel to the channel group
        Apex.getChannelGroup().add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            //System.out.println("Host: " + req.headers().get("host") + " Path: " + req.uri());

            if (downstreamChannel == null || !downstreamChannel.isActive()) {
                final Channel inboundChannel = ctx.channel();

                Bootstrap b = new Bootstrap()
                        .group(inboundChannel.eventLoop())
                        .channel(PipelineUtils.getChannel())
                        .handler(new ChannelInitializer<SocketChannel>() {

                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {

                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpClientCodec());
                                pipeline.addLast(new HttpProxyDownstreamHandler(inboundChannel));
                            }
                        })
                        .option(ChannelOption.TCP_NODELAY, true)
                        // No initial connection should take longer than 4 seconds
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, BackendInfo.DEFAULT_TCP_TIMEOUT)
                        .option(ChannelOption.AUTO_READ, false);

                String http = req.headers().get("host");
                ChannelFuture f = b.connect((!http.contains(":")) ? http : http.split(":")[0], (!http.contains(":")) ? 80 : 443);
                downstreamChannel = f.channel();
                f.addListener((ChannelFutureListener) future -> {

                    if (future.isSuccess()) {
                        downstreamChannel.writeAndFlush(req).addListener(future1 -> {

                            if (future1.isSuccess()) {
                                inboundChannel.read();
                            } else {
                                ChannelUtil.closeOnFlush(inboundChannel);
                            }
                        });
                    } else {
                        ChannelUtil.close(inboundChannel);
                    }
                });
            } else {
                downstreamChannel.writeAndFlush(req).addListener(future -> {

                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        ChannelUtil.close(downstreamChannel);
                    }
                });
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        ChannelUtil.closeOnFlush(downstreamChannel);
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
