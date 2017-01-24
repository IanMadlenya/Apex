/*
 * Copyright (c) 2017 "JackWhite20"
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

package de.jackwhite20.apex.socks.handler;

import de.jackwhite20.apex.util.BackendInfo;
import de.jackwhite20.apex.util.ChannelUtil;
import de.jackwhite20.apex.util.PipelineUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;

/**
 * Created by JackWhite20 on 24.01.2017.
 */
public class SocksUpstreamHandler extends SimpleChannelInboundHandler<SocksMessage> {

    private final Bootstrap b = new Bootstrap();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksMessage message) throws Exception {

        if (message instanceof Socks4CommandRequest) {
            final Socks4CommandRequest request = (Socks4CommandRequest) message;
            Promise<Channel> promise = ctx.executor().newPromise();
            promise.addListener(
                    (FutureListener<Channel>) future -> {

                        final Channel outboundChannel = future.getNow();
                        if (future.isSuccess()) {
                            ChannelFuture responseFuture = ctx.channel().writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS));

                            responseFuture.addListener((ChannelFutureListener) channelFuture -> {

                                ctx.pipeline().remove(SocksUpstreamHandler.this);
                                outboundChannel.pipeline().addLast(new SocksRelayHandler(ctx.channel()));
                                ctx.pipeline().addLast(new SocksRelayHandler(outboundChannel));
                            });
                        } else {
                            ctx.channel().writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED));
                            ChannelUtil.closeOnFlush(ctx.channel());
                        }
                    });

            final Channel inboundChannel = ctx.channel();
            b.group(inboundChannel.eventLoop())
                    .channel(PipelineUtils.getChannel())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, BackendInfo.DEFAULT_TCP_TIMEOUT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new SocksConnectPromise(promise));

            b.connect(request.dstAddr(), request.dstPort()).addListener((ChannelFutureListener) future -> {

                if (!future.isSuccess()) {
                    ctx.channel().writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED));
                    ChannelUtil.closeOnFlush(ctx.channel());
                }
            });
        } else if (message instanceof Socks5CommandRequest) {
            final Socks5CommandRequest request = (Socks5CommandRequest) message;
            Promise<Channel> promise = ctx.executor().newPromise();
            promise.addListener(
                    (FutureListener<Channel>) future -> {

                        final Channel outboundChannel = future.getNow();
                        if (future.isSuccess()) {
                            ChannelFuture responseFuture =
                                    ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                            Socks5CommandStatus.SUCCESS,
                                            request.dstAddrType(),
                                            request.dstAddr(),
                                            request.dstPort()));

                            responseFuture.addListener((ChannelFutureListener) channelFuture -> {

                                ctx.pipeline().remove(SocksUpstreamHandler.this);
                                outboundChannel.pipeline().addLast(new SocksRelayHandler(ctx.channel()));
                                ctx.pipeline().addLast(new SocksRelayHandler(outboundChannel));
                            });
                        } else {
                            ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                    Socks5CommandStatus.FAILURE, request.dstAddrType()));
                            ChannelUtil.closeOnFlush(ctx.channel());
                        }
                    });

            final Channel inboundChannel = ctx.channel();
            b.group(inboundChannel.eventLoop())
                    .channel(PipelineUtils.getChannel())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, BackendInfo.DEFAULT_TCP_TIMEOUT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new SocksConnectPromise(promise));

            b.connect(request.dstAddr(), request.dstPort()).addListener((ChannelFutureListener) future -> {

                if (!future.isSuccess()) {
                    ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()));
                    ChannelUtil.closeOnFlush(ctx.channel());
                }
            });
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        ChannelUtil.closeOnFlush(ctx.channel());
    }
}
