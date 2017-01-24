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

package de.jackwhite20.apex.socks.initialize;

import com.google.common.base.Preconditions;
import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.socks.handler.SocksServerHandler;
import de.jackwhite20.apex.task.ConnectionsPerSecondTask;
import de.jackwhite20.apex.tcp.pipeline.initialize.ApexSocketChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 24.01.2017.
 */
public class ApexSocksChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Logger logger = LoggerFactory.getLogger(ApexSocketChannelInitializer.class);

    private int readTimeout;

    private int writeTimeout;

    private ConnectionsPerSecondTask connectionsPerSecondTask;

    public ApexSocksChannelInitializer(int readTimeout, int writeTimeout) {

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

        channel.pipeline()
                .addLast(new ReadTimeoutHandler(readTimeout))
                .addLast(new WriteTimeoutHandler(writeTimeout))
                .addLast(new SocksPortUnificationServerHandler())
                .addLast(new SocksServerHandler());

        /*GlobalTrafficShapingHandler trafficShapingHandler = Apex.getInstance().getTrafficShapingHandler();
        if (trafficShapingHandler != null) {
            channel.pipeline().addLast(trafficShapingHandler);
        }*/

        // Keep track of connections per second
        if (connectionsPerSecondTask != null) {
            connectionsPerSecondTask.inc();
        }

        logger.debug("Connected [{}]", channel.remoteAddress());
    }
}
