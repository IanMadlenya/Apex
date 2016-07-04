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

package de.jackwhite20.apex.task;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.strategy.BalancingStrategy;
import de.jackwhite20.apex.util.BackendInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class CheckBackendTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CheckBackendTask.class);

    private final List<BackendInfo> backendInfo;

    private BalancingStrategy balancingStrategy;

    public CheckBackendTask(BalancingStrategy balancingStrategy) {

        this.balancingStrategy = balancingStrategy;
        this.backendInfo = Collections.synchronizedList(new ArrayList<>(balancingStrategy.getBackend()));
    }

    public synchronized void addBackend(BackendInfo backendInfo) {

        this.backendInfo.add(backendInfo);
    }

    public synchronized void removeBackend(BackendInfo backendInfo) {

        this.backendInfo.remove(backendInfo);
    }

    @Override
    public void run() {

        if (!Apex.getServerChannel().isActive()) {
            return;
        }

        synchronized (balancingStrategy.getBackend()) {
            synchronized (backendInfo) {
                for (BackendInfo info : backendInfo) {
                    if (info.check()) {
                        if (!balancingStrategy.hasBackend(info)) {
                            balancingStrategy.addBackend(info);
                            logger.info("{} is up again and was added back to the load balancer", info.getName());
                        }
                    } else {
                        if (balancingStrategy.hasBackend(info)) {
                            logger.warn("{} went down and was removed from the load balancer", info.getName());
                            balancingStrategy.removeBackend(info);

                            if (balancingStrategy.getBackend().size() == 0) {
                                logger.error("No more backend servers online");
                            } else {
                                logger.debug("{} backend servers left", balancingStrategy.getBackend().size());
                            }
                        }
                    }
                }
            }
        }
    }
}
