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

import ch.qos.logback.classic.Level;
import de.jackwhite20.apex.command.Command;
import de.jackwhite20.apex.command.CommandManager;
import de.jackwhite20.apex.command.impl.DebugCommand;
import de.jackwhite20.apex.command.impl.EndCommand;
import de.jackwhite20.apex.command.impl.HelpCommand;
import de.jackwhite20.apex.command.impl.StatsCommand;
import de.jackwhite20.apex.pipeline.initialize.ApexChannelInitializer;
import de.jackwhite20.apex.rest.RestServer;
import de.jackwhite20.apex.strategy.BalancingStrategy;
import de.jackwhite20.apex.strategy.BalancingStrategyFactory;
import de.jackwhite20.apex.strategy.StrategyType;
import de.jackwhite20.apex.task.CheckBackendTask;
import de.jackwhite20.apex.util.BackendInfo;
import de.jackwhite20.apex.util.PipelineUtils;
import de.jackwhite20.cope.CopeConfig;
import de.jackwhite20.cope.config.Header;
import de.jackwhite20.cope.config.Key;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.ResourceLeakDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class Apex {

    private static final String APEX_PACKAGE_NAME = "de.jackwhite20.apex";

    private static final Pattern ARGS_PATTERN = Pattern.compile(" ");

    private static Logger logger = LoggerFactory.getLogger(Apex.class);

    private static Apex instance;

    private static ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(APEX_PACKAGE_NAME);

    private CopeConfig copeConfig;

    private BalancingStrategy balancingStrategy;

    private CheckBackendTask backendTask;

    private ScheduledExecutorService scheduledExecutorService;

    private Channel serverChannel;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private GlobalTrafficShapingHandler trafficShapingHandler;

    private RestServer restServer;

    private CommandManager commandManager;

    private Scanner scanner;

    public Apex(CopeConfig copeConfig) {

        Apex.instance = this;

        this.copeConfig = copeConfig;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.commandManager = new CommandManager();
    }

    public void start() {

        commandManager.addCommand(new HelpCommand("help", "List of available commands", "h"));
        commandManager.addCommand(new EndCommand("end", "Stops Apex", "stop", "exit"));
        commandManager.addCommand(new DebugCommand("debug", "Turns the debug mode on/off", "d"));
        commandManager.addCommand(new StatsCommand("stats", "Shows live stats", "s", "info"));

        Header generalHeader = copeConfig.getHeader("general");
        Key serverKey = generalHeader.getKey("server");
        Key balanceKey = generalHeader.getKey("balance");
        Key bossKey = generalHeader.getKey("boss");
        Key workerKey = generalHeader.getKey("worker");
        Key timeoutKey = generalHeader.getKey("timeout");
        Key backlogKey = generalHeader.getKey("backlog");
        Key probeKey = generalHeader.getKey("probe");
        Key debugKey = generalHeader.getKey("debug");
        Key statsKey = generalHeader.getKey("stats");

        // Set the log level to debug or info based on the config value
        changeDebug((Boolean.valueOf(debugKey.getValue(0).asString())) ? Level.DEBUG : Level.INFO);

        List<BackendInfo> backendInfo = copeConfig.getHeader("backend").getKeys()
                .stream()
                .map(backend -> new BackendInfo(backend.getName(),
                        backend.getValue(0).asString(),
                        backend.getValue(1).asInt()))
                .collect(Collectors.toList());

        logger.debug("Host: {}", serverKey.getValue(0).asString());
        logger.debug("Port: {}", serverKey.getValue(1).asString());
        logger.debug("Balance: {}", balanceKey.getValue(0).asString());
        logger.debug("Backlog: {}", backlogKey.getValue(0).asInt());
        logger.debug("Boss: {}", bossKey.getValue(0).asInt());
        logger.debug("Worker: {}", workerKey.getValue(0).asInt());
        logger.debug("Stats: {}", statsKey.getValue(0).asString());
        logger.debug("Probe: {}", probeKey.getValue(0).asInt());
        logger.debug("Backend: {}", backendInfo.stream().map(BackendInfo::getHost).collect(Collectors.joining(", ")));

        StrategyType type = StrategyType.valueOf(balanceKey.getValue(0).asString());

        balancingStrategy = BalancingStrategyFactory.create(type, backendInfo);

        // Disable the resource leak detector
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);

        if (PipelineUtils.isEpoll()) {
            logger.info("Using high performance epoll event notification mechanism");
        } else {
            logger.info("Using normal select/poll event notification mechanism");
        }

        // Choose the type of the event loop group
        int bossThreads = bossKey.getValue(0).asInt();
        if (bossThreads < PipelineUtils.DEFAULT_THREADS_THRESHOLD) {
            bossThreads = PipelineUtils.DEFAULT_BOSS_THREADS;

            logger.warn("Boss threads needs to be greater or equal than {}. Using default value of {}",
                    PipelineUtils.DEFAULT_THREADS_THRESHOLD,
                    PipelineUtils.DEFAULT_BOSS_THREADS);
        }

        int workerThreads = workerKey.getValue(0).asInt();
        if (workerThreads < PipelineUtils.DEFAULT_THREADS_THRESHOLD) {
            workerThreads = PipelineUtils.DEFAULT_WORKER_THREADS;

            logger.warn("Worker threads needs to be greater or equal than {}. Using default value of {}",
                    PipelineUtils.DEFAULT_THREADS_THRESHOLD,
                    PipelineUtils.DEFAULT_WORKER_THREADS);
        }

        bossGroup = PipelineUtils.newEventLoopGroup(bossThreads);
        workerGroup = PipelineUtils.newEventLoopGroup(workerThreads);

        try {
            ServerBootstrap b = new ServerBootstrap();
            serverChannel = b.group(bossGroup, workerGroup)
                    .channel(PipelineUtils.getServerChannel())
                    .childHandler(new ApexChannelInitializer(timeoutKey.getValue(0).asInt(),
                            timeoutKey.getValue(1).asInt()))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, backlogKey.getValue(0).asInt())
                    .bind(serverKey.getValue(0).asString(), serverKey.getValue(1).asInt())
                    .sync()
                    .channel();

            int probe = probeKey.getValue(0).asInt();
            if (probe < -1 || probe == 0) {
                probe = 10000;

                logger.warn("Probe time value must be -1 to turn it off or greater than 0");
                logger.warn("Using default probe time of 10000 milliseconds (10 seconds)");
            }

            if (probe != -1) {
                backendTask = new CheckBackendTask(balancingStrategy);
                scheduledExecutorService.scheduleAtFixedRate(backendTask, 0, probe, TimeUnit.MILLISECONDS);
            } else {
                // Shutdown unnecessary scheduler
                scheduledExecutorService.shutdown();
            }

            if (Boolean.parseBoolean(statsKey.getValue(0).asString())) {
                // Traffic shaping handler with default check interval of 1000
                trafficShapingHandler = new GlobalTrafficShapingHandler(workerGroup, 0, 0);

                logger.debug("Traffic stats collect handler initialized");
            }

            restServer = new RestServer(copeConfig);
            restServer.start();

            logger.info("Apex listening on {}:{}", serverKey.getValue(0).asString(), serverKey.getValue(1).asInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void console() {

        scanner = new Scanner(System.in);

        try {
            String line;
            while ((line = scanner.nextLine()) != null) {
                if (!line.isEmpty()) {
                    String[] split = ARGS_PATTERN.split(line);

                    if (split.length == 0) {
                        continue;
                    }

                    // Get the command name
                    String commandName = split[0].toLowerCase();

                    // Try to get the command with the name
                    Command command = commandManager.findCommand(commandName);

                    if (command != null) {
                        logger.info("Executing command: {}", line);

                        String[] cmdArgs = Arrays.copyOfRange(split, 1, split.length);
                        command.execute(cmdArgs);
                    } else {
                        logger.info("Command not found!");
                    }
                }
            }
        } catch (IllegalStateException ignore) {}
    }

    public void changeDebug(Level level) {

        // Set the log level to debug or info based on the config value
        rootLogger.setLevel(level);

        logger.info("Logger level is now {}", rootLogger.getLevel());
    }

    public void changeDebug() {

        changeDebug((rootLogger.getLevel() == Level.INFO) ? Level.DEBUG : Level.INFO);
    }

    public void stop() {

        logger.info("Apex is going to be stopped");

        // Close the scanner
        scanner.close();

        // Close the server channel
        if (serverChannel != null) {
            serverChannel.close();
        }

        // Release the traffic shaping handler
        if (trafficShapingHandler != null) {
            trafficShapingHandler.release();
        }

        // Shutdown the event loop groups
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        try {
            restServer.stop();
        } catch (Exception e) {
            logger.warn("RESTful API server already stopped");
        }

        scheduledExecutorService.shutdown();

        logger.info("Apex has been stopped");
    }

    public static CommandManager getCommandManager() {

        return instance.commandManager;
    }

    public static BalancingStrategy getBalancingStrategy() {

        return instance.balancingStrategy;
    }

    public static CheckBackendTask getBackendTask() {

        return instance.backendTask;
    }

    public static Channel getServerChannel() {

        return instance.serverChannel;
    }

    public GlobalTrafficShapingHandler getTrafficShapingHandler() {

        return trafficShapingHandler;
    }

    public static Apex getInstance() {

        return instance;
    }
}
