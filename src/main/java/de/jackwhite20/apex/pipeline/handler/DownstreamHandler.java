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

package de.jackwhite20.apex.pipeline.handler;

import de.jackwhite20.apex.util.ChannelUtil;
import io.netty.channel.*;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class DownstreamHandler extends ChannelHandlerAdapter {

    private Channel inboundChannel;

    public DownstreamHandler(Channel inboundChannel) {

        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        if (inboundChannel.isActive()) {
            inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {

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

        ChannelUtil.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        ChannelUtil.closeOnFlush(ctx.channel());
        cause.printStackTrace();
    }
}
