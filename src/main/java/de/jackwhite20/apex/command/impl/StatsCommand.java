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

package de.jackwhite20.apex.command.impl;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.command.Command;
import de.jackwhite20.apex.util.ConnectionManager;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 31.10.2016.
 */
public class StatsCommand extends Command {

    private static Logger logger = LoggerFactory.getLogger(StatsCommand.class);

    public StatsCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        logger.info("Connections: {}", ConnectionManager.getConnections());
        logger.info("Online backend servers: {}", Apex.getBalancingStrategy().getBackend().size());

        GlobalTrafficShapingHandler trafficShapingHandler = Apex.getInstance().getTrafficShapingHandler();
        if (trafficShapingHandler != null) {
            TrafficCounter trafficCounter = trafficShapingHandler.trafficCounter();

            logger.info("Current bytes read: {}", trafficCounter.currentReadBytes());
            logger.info("Current bytes written: {}", trafficCounter.currentWrittenBytes());
            logger.info("Last read throughput: {}", trafficCounter.lastReadThroughput());
            logger.info("Last write throughput: {}", trafficCounter.lastWrittenBytes());
            logger.info("Total bytes read: {}", trafficCounter.cumulativeReadBytes());
            logger.info("Total bytes written: {}", trafficCounter.cumulativeWrittenBytes());
        }

        return true;
    }
}
