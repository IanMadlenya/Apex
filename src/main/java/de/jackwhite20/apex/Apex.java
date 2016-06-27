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

package de.jackwhite20.apex;

import de.jackwhite20.apex.pipeline.initialize.ApexChannelInitializer;
import de.jackwhite20.apex.rest.RestServer;
import de.jackwhite20.apex.strategy.AbstractBalancingStrategy;
import de.jackwhite20.apex.strategy.impl.LeastConnectionStrategy;
import de.jackwhite20.apex.strategy.impl.RandomBalancingStrategy;
import de.jackwhite20.apex.strategy.impl.RoundRobinBalancingStrategy;
import de.jackwhite20.apex.task.CheckBackendTask;
import de.jackwhite20.apex.util.BackendInfo;
import de.jackwhite20.cope.CopeConfig;
import de.jackwhite20.cope.config.Header;
import de.jackwhite20.cope.config.Key;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class Apex {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static Apex instance;

    private CopeConfig copeConfig;

    private AbstractBalancingStrategy balancingStrategy;

    private CheckBackendTask backendTask;

    private ScheduledExecutorService scheduledExecutorService;

    private Channel serverChannel;

    private EventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private RestServer restServer;

    public Apex(CopeConfig copeConfig) {

        instance = this;

        this.copeConfig = copeConfig;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {

        Header generalHeader = copeConfig.getHeader("general");
        Key serverKey = generalHeader.getKey("server");
        Key balanceKey = generalHeader.getKey("balance");
        Key threadsKey = generalHeader.getKey("threads");
        Key timeoutKey = generalHeader.getKey("timeout");
        Key backlogKey = generalHeader.getKey("backlog");
        Key probeKey = generalHeader.getKey("probe");

        logger.debug("Server: {}", serverKey.getValue(0).asString() + ":" + serverKey.getValue(1).asInt());
        logger.debug("Balance: {}", balanceKey.getValue(0).asString());
        logger.debug("Backlog: {}", backlogKey.getValue(0).asInt());
        logger.debug("Threads: {}", threadsKey.getValue(0).asInt());
        logger.debug("Backend: {}", String.join(", ", copeConfig.getHeader("backend").getKeys().stream().map(key -> key.getValue(0).asString() + ":" + key.getValue(1).asInt()).collect(Collectors.toList())));
        logger.debug("Probe: {}", probeKey.getValue(0).asInt());

        List<BackendInfo> backendInfo = copeConfig.getHeader("backend").getKeys()
                .stream()
                .map(backend -> new BackendInfo(backend.getName(), backend.getValue(0).asString(), backend.getValue(1).asInt()))
                .collect(Collectors.toList());

        AbstractBalancingStrategy.Type type = AbstractBalancingStrategy.Type.valueOf(balanceKey.getValue(0).asString());

        if (type == null) {
            type = AbstractBalancingStrategy.Type.RANDOM;

            logger.info("Using default strategy: {}", type);
        } else {
            logger.info("Using strategy: {}", type);
        }

        switch (type) {
            case RANDOM:
                balancingStrategy = new RandomBalancingStrategy(backendInfo);
                break;
            case ROUND_ROBIN:
                balancingStrategy = new RoundRobinBalancingStrategy(backendInfo);
                break;
            case LEAST_CON:
                balancingStrategy = new LeastConnectionStrategy(backendInfo);
                break;
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(threadsKey.getValue(0).asInt());

        try {
            ServerBootstrap b = new ServerBootstrap();
            serverChannel = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ApexChannelInitializer(balancingStrategy, timeoutKey.getValue(0).asInt(), timeoutKey.getValue(1).asInt()))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, backlogKey.getValue(0).asInt())
                    .bind(serverKey.getValue(0).asString(), serverKey.getValue(1).asInt())
                    .sync()
                    .channel();

            scheduledExecutorService.scheduleAtFixedRate(backendTask = new CheckBackendTask(balancingStrategy), 0, probeKey.getValue(0).asInt(), TimeUnit.MILLISECONDS);

            restServer = new RestServer(copeConfig);
            restServer.start();

            logger.info("Apex listening on {}:{}", serverKey.getValue(0).asString(), serverKey.getValue(1).asInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

        serverChannel.close();

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        restServer.stop();

        scheduledExecutorService.shutdown();
    }

    public static AbstractBalancingStrategy getBalancingStrategy() {

        return instance.balancingStrategy;
    }

    public static CheckBackendTask getBackendTask() {

        return instance.backendTask;
    }

    public static Channel getServerChannel() {

        return instance.serverChannel;
    }
}
