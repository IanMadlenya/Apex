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

package de.jackwhite20.apex.udp;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.udp.pipeline.DatagramUpstreamHandler;
import de.jackwhite20.apex.util.PipelineUtils;
import de.jackwhite20.cope.CopeConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 04.11.2016.
 */
public class ApexDatagram extends Apex {

    private static Logger logger = LoggerFactory.getLogger(ApexDatagram.class);

    public ApexDatagram(CopeConfig copeConfig) {

        super(copeConfig);
    }

    @Override
    public Channel bootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String ip, int port, int backlog, int readTimeout, int writeTimeout) throws Exception {

        logger.info("Bootstrapping datagram server");

        return new Bootstrap()
                .group(workerGroup)
                .channel(PipelineUtils.getDatagramChannel())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new DatagramUpstreamHandler())
                .bind(ip, port)
                .sync()
                .channel();
    }
}
