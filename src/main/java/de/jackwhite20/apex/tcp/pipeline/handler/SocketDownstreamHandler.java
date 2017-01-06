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

import de.jackwhite20.apex.util.ChannelUtil;
import io.netty.channel.*;
import io.netty.handler.timeout.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class SocketDownstreamHandler extends ChannelHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SocketDownstreamHandler.class);

    private Channel inboundChannel;

    public SocketDownstreamHandler(Channel inboundChannel) {

        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        if (inboundChannel.isActive()) {
            inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {

                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        ChannelUtil.closeOnFlush(inboundChannel);
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
