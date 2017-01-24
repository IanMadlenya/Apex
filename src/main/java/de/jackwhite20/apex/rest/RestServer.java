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

package de.jackwhite20.apex.rest;

import de.jackwhite20.cobra.server.CobraServer;
import de.jackwhite20.cobra.server.CobraServerFactory;
import de.jackwhite20.cope.CopeConfig;
import de.jackwhite20.cope.config.Header;
import de.jackwhite20.cope.config.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 27.06.2016.
 */
public class RestServer {

    private static Logger logger = LoggerFactory.getLogger(RestServer.class);

    private CopeConfig copeConfig;

    private CobraServer cobraServer;

    public RestServer(CopeConfig copeConfig) {

        this.copeConfig = copeConfig;
    }

    public void start() {

        Header restHeader = copeConfig.getHeader("rest");
        Key serverKey = restHeader.getKey("server");

        String ip = serverKey.getValue(0).asString();
        int port = serverKey.getValue(1).asInt();

        cobraServer = CobraServerFactory.create(new ApexRestConfig(ip, port));
        cobraServer.start();

        logger.info("RESTful API listening on {}:{}", ip, port);
    }

    public void stop() {

        cobraServer.stop();
    }
}
