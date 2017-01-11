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

package de.jackwhite20.apex.http.pipeline.initialize;

import com.google.common.base.Preconditions;
import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.http.pipeline.handler.HttpUpstreamHandler;
import de.jackwhite20.apex.task.ConnectionsPerSecondTask;
import de.jackwhite20.apex.util.BackendInfo;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 08.01.2017.
 */
public class ApexHttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Logger logger = LoggerFactory.getLogger(ApexHttpChannelInitializer.class);

    private int readTimeout;

    private int writeTimeout;

    private ConnectionsPerSecondTask connectionsPerSecondTask;

    public ApexHttpChannelInitializer(int readTimeout, int writeTimeout) {

        Preconditions.checkState(readTimeout > 0, "readTimeout cannot be negative");
        Preconditions.checkState(writeTimeout > 0, "writeTimeout cannot be negative");

        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.connectionsPerSecondTask = Apex.getInstance().getConnectionsPerSecondTask();

        logger.debug("Read timeout: {}", readTimeout);
        logger.debug("Write timeout: {}", writeTimeout);
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        BackendInfo backendInfo = Apex.getBalancingStrategy()
                .selectBackend(channel.remoteAddress().getHostName(), channel.remoteAddress().getPort());

        if (backendInfo == null) {
            // Gracefully close the channel
            channel.close();

            logger.error("Unable to select a backend server. All down?");
            return;
        }

        logger.info("New connection");

        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(new ReadTimeoutHandler(readTimeout));
        pipeline.addLast(new WriteTimeoutHandler(writeTimeout));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpUpstreamHandler(backendInfo));

        connectionsPerSecondTask.inc();
    }
}
