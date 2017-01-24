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

import de.jackwhite20.apex.util.ChannelUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by JackWhite20 on 24.01.2017.
 */
public class SocksRelayHandler extends ChannelInboundHandlerAdapter {

    private final Channel relayChannel;

    public SocksRelayHandler(Channel relayChannel) {

        this.relayChannel = relayChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (relayChannel.isActive()) {
            relayChannel.writeAndFlush(msg);
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        ChannelUtil.closeOnFlush(relayChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        ChannelUtil.close(ctx.channel());
    }
}
