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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.*;

/**
 * Created by JackWhite20 on 24.01.2017.
 */
public class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksMessage socksMessage) throws Exception {

        switch (socksMessage.version()) {
            case SOCKS4a:
                Socks4CommandRequest socksV4CmdRequest = (Socks4CommandRequest) socksMessage;
                if (socksV4CmdRequest.type() == Socks4CommandType.CONNECT) {
                    ctx.pipeline().addLast(new SocksUpstreamHandler());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksMessage);
                } else {
                    ChannelUtil.close(ctx.channel());
                }
                break;
            case SOCKS5:
                if (socksMessage instanceof Socks5InitialRequest) {
                    ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                    ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
                } else if (socksMessage instanceof Socks5PasswordAuthRequest) {
                    ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                    ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                } else if (socksMessage instanceof Socks5CommandRequest) {
                    Socks5CommandRequest socks5CmdRequest = (Socks5CommandRequest) socksMessage;
                    System.out.println(socks5CmdRequest.type());
                    if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                        ctx.pipeline().addLast(new SocksUpstreamHandler());
                        ctx.pipeline().remove(this);
                        ctx.fireChannelRead(socksMessage);
                    } else {
                        ChannelUtil.close(ctx.channel());
                    }
                } else {
                    ChannelUtil.close(ctx.channel());
                }
                break;
            case UNKNOWN:
                ctx.close();
                break;
        }
    }
}
