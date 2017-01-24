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

package de.jackwhite20.apex.socks;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.socks.initialize.ApexSocksChannelInitializer;
import de.jackwhite20.apex.util.PipelineUtils;
import de.jackwhite20.cope.CopeConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 24.01.2017.
 */
public class ApexSocks extends Apex {

    private static Logger logger = LoggerFactory.getLogger(ApexSocks.class);

    public ApexSocks(CopeConfig copeConfig) {

        super(copeConfig);
    }

    @Override
    public Channel bootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String ip, int port, int backlog, int readTimeout, int writeTimeout) throws Exception {

        logger.info("Bootstrapping socks server");

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(PipelineUtils.getServerChannel())
                .childHandler(new ApexSocksChannelInitializer(readTimeout, writeTimeout));

        if (PipelineUtils.isEpoll()) {
            bootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);

            logger.debug("Epoll mode is now level triggered");
        }

        return bootstrap
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, backlog)
                .bind(ip, port)
                .sync()
                .channel();
    }
}
