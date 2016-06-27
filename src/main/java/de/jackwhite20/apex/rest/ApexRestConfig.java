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

package de.jackwhite20.apex.rest;

import de.jackwhite20.apex.rest.resource.ApexResource;
import de.jackwhite20.cobra.server.impl.CobraConfig;

/**
 * Created by JackWhite20 on 27.06.2016.
 */
public class ApexRestConfig extends CobraConfig {

    public ApexRestConfig(String host, int port) {

        host(host);
        port(port);
        corePoolSize(2);
        maxPoolSize(4);
        backLog(50);
        register(ApexResource.class);
    }
}
