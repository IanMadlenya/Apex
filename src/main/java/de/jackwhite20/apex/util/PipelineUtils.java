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

package de.jackwhite20.apex.util;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by JackWhite20 on 23.09.2016.
 */
public final class PipelineUtils {

    public static final int DEFAULT_THREADS_THRESHOLD = 1;

    public static final int DEFAULT_BOSS_THREADS = 1;

    public static final int DEFAULT_WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private static boolean epoll;

    static {
        epoll = Epoll.isAvailable();
    }

    private PipelineUtils() {
        // No instance
    }

    public static EventLoopGroup newEventLoopGroup(int threads) {

        return epoll ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
    }

    public static Class<? extends ServerChannel> getServerChannel() {

        return epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> getChannel() {

        return epoll ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    public static Class<? extends Channel> getDatagramChannel() {

        return epoll ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }

    public static boolean isEpoll() {

        return epoll;
    }
}
