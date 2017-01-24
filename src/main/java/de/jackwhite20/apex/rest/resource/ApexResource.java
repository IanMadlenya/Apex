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

package de.jackwhite20.apex.rest.resource;

import com.google.gson.Gson;
import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.rest.response.ApexListResponse;
import de.jackwhite20.apex.rest.response.ApexResponse;
import de.jackwhite20.apex.rest.response.ApexStatsResponse;
import de.jackwhite20.apex.strategy.BalancingStrategy;
import de.jackwhite20.apex.task.ConnectionsPerSecondTask;
import de.jackwhite20.apex.util.BackendInfo;
import de.jackwhite20.cobra.server.http.Request;
import de.jackwhite20.cobra.server.http.annotation.Path;
import de.jackwhite20.cobra.server.http.annotation.PathParam;
import de.jackwhite20.cobra.server.http.annotation.Produces;
import de.jackwhite20.cobra.server.http.annotation.method.GET;
import de.jackwhite20.cobra.shared.ContentType;
import de.jackwhite20.cobra.shared.http.Response;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 27.06.2016.
 */
@Path("/apex")
public class ApexResource {

    private static final Response STATS_DISABLED;

    private static Logger logger = LoggerFactory.getLogger(ApexResource.class);

    private static Gson gson = new Gson();

    private static GlobalTrafficShapingHandler trafficShapingHandler = Apex.getInstance().getTrafficShapingHandler();

    private static ConnectionsPerSecondTask connectionsPerSecondTask = Apex.getInstance().getConnectionsPerSecondTask();

    static {
        STATS_DISABLED = Response.ok().content(gson.toJson(new ApexStatsResponse(ApexResponse.Status.ERROR,
                "Stats are disabled",
                -1,
                -1,
                -1,
                -1,
                -1,
                -1,
                -1,
                -1,
                -1))).build();
    }

    @GET
    @Path("/add/{name}/{ip}/{port}")
    @Produces(ContentType.APPLICATION_JSON)
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

            logger.info("Added backend server {}:{} to the load balancer", ip, port);

            return Response.ok().content(gson.toJson(new ApexResponse(ApexResponse.Status.OK,
                    "Successfully added server"))).build();
        } else {
            return Response.ok().content(gson.toJson(new ApexResponse(ApexResponse.Status.SERVER_ALREADY_ADDED,
                    "Server was already added"))).build();
        }
    }

    @GET
    @Path("/remove/{name}")
    @Produces(ContentType.APPLICATION_JSON)
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

            logger.info("Removed backend server {} from the load balancer", name);

            return Response.ok().content(gson.toJson(new ApexResponse(ApexResponse.Status.OK,
                    "Successfully removed server"))).build();
        } else {
            return Response.ok().content(gson.toJson(new ApexResponse(ApexResponse.Status.SERVER_NOT_FOUND,
                    "Server not found"))).build();
        }
    }

    @GET
    @Path("/list")
    @Produces(ContentType.APPLICATION_JSON)
    public Response list(Request httpRequest) {

        BalancingStrategy balancingStrategy = Apex.getBalancingStrategy();
        if (balancingStrategy != null) {
            return Response.ok().content(gson.toJson(new ApexListResponse(ApexResponse.Status.OK, "List received",
                    balancingStrategy.getBackend()))).build();
        } else {
            return Response.ok().content(gson.toJson(new ApexListResponse(ApexResponse.Status.ERROR,
                    "Unable to get the balancing strategy",
                    null))).build();
        }
    }

    @GET
    @Path("/stats")
    @Produces(ContentType.APPLICATION_JSON)
    public Response stats(Request httpRequest) {

        if (trafficShapingHandler != null) {
            TrafficCounter trafficCounter = trafficShapingHandler.trafficCounter();

            return Response.ok().content(gson.toJson(new ApexStatsResponse(ApexResponse.Status.OK,
                    "OK",
                    Apex.getChannelGroup().size(),
                    connectionsPerSecondTask.getPerSecond(),
                    Apex.getBalancingStrategy().getBackend().size(),
                    trafficCounter.currentReadBytes(),
                    trafficCounter.currentWrittenBytes(),
                    trafficCounter.lastReadThroughput(),
                    trafficCounter.lastWriteThroughput(),
                    trafficCounter.cumulativeReadBytes(),
                    trafficCounter.cumulativeWrittenBytes()))).build();
        } else {
            return STATS_DISABLED;
        }
    }
}
