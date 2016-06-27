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

package de.jackwhite20.apex.pipeline.initialize;

import de.jackwhite20.apex.pipeline.handler.UpstreamHandler;
import de.jackwhite20.apex.strategy.AbstractBalancingStrategy;
import de.jackwhite20.apex.util.BackendInfo;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class ApexChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Logger logger = LoggerFactory.getLogger(ApexChannelInitializer.class);

    private AbstractBalancingStrategy balancingStrategy;

    private int readTimeout;

    private int writeTimeout;

    public ApexChannelInitializer(AbstractBalancingStrategy balancingStrategy, int readTimeout, int writeTimeout) {

        this.balancingStrategy = balancingStrategy;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;

        logger.debug("Read timeout: {}", readTimeout);
        logger.debug("Write timeout: {}", writeTimeout);
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        BackendInfo backendInfo = balancingStrategy.selectBackend(channel.remoteAddress().getHostName(), channel.remoteAddress().getPort());

        if (backendInfo == null) {
            // Gracefully close channel
            channel.close();

            logger.error("Unable to select a backend server. All down?");
            return;
        }

        channel.pipeline()
                .addLast(new UpstreamHandler(balancingStrategy, backendInfo))
                .addLast(new ReadTimeoutHandler(readTimeout))
                .addLast(new WriteTimeoutHandler(writeTimeout));

        logger.debug("Connected [{}] <-> [{}:{} ({})]", channel.remoteAddress(), backendInfo.getHost(), backendInfo.getPort(), backendInfo.getName());
    }
}
