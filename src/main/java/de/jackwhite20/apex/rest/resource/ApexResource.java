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

package de.jackwhite20.apex.rest.resource;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.util.BackendInfo;
import de.jackwhite20.cobra.server.http.Request;
import de.jackwhite20.cobra.server.http.annotation.Path;
import de.jackwhite20.cobra.server.http.annotation.PathParam;
import de.jackwhite20.cobra.server.http.annotation.method.GET;
import de.jackwhite20.cobra.shared.Status;
import de.jackwhite20.cobra.shared.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 27.06.2016.
 */
@Path("/apex")
public class ApexResource {

    private static Logger logger = LoggerFactory.getLogger(ApexResource.class);

    @GET
    @Path("/add/{name}/{ip}/{port}")
    public Response add(Request httpRequest, @PathParam String name, @PathParam String ip, @PathParam String port) {

        BackendInfo found = null;
        synchronized (Apex.getBalancingStrategy().getBackend()) {
            for (BackendInfo info : Apex.getBalancingStrategy().getBackend()) {
                if (info.getName().equalsIgnoreCase(name)) {
                    found = info;
                    break;
                }
            }
        }

        if (found == null) {
            BackendInfo backend = new BackendInfo(name, ip, Integer.valueOf(port));
            Apex.getBalancingStrategy().addBackend(backend);
            Apex.getBackendTask().addBackend(backend);

            logger.info("Added backend {}:{} to the load balancer", ip, port);

            // TODO: 30.06.2016 Nicer JSON response
            return Response.ok().content("success").build();
        } else {
            return Response.ok().content("already exists").build();
        }
    }

    @GET
    @Path("/remove/{name}")
    public Response remove(Request httpRequest, @PathParam String name) {

        BackendInfo found = null;
        synchronized (Apex.getBalancingStrategy().getBackend()) {
            for (BackendInfo info : Apex.getBalancingStrategy().getBackend()) {
                if (info.getName().equalsIgnoreCase(name)) {
                    found = info;
                    break;
                }
            }
        }

        if (found != null) {
            Apex.getBalancingStrategy().removeBackend(found);
            Apex.getBackendTask().removeBackend(found);

            logger.info("Removed backend {} from the load balancer", name);

            // TODO: 30.06.2016 Nicer JSON response
            return Response.ok().content("success").build();
        } else {
            return Response.status(Status.NOT_FOUND).content("not found").build();
        }
    }
}
