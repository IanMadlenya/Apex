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

package de.jackwhite20.apex.task.impl;

import de.jackwhite20.apex.strategy.BalancingStrategy;
import de.jackwhite20.apex.task.CheckBackendTask;
import de.jackwhite20.apex.util.BackendInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class CheckSocketBackendTask extends CheckBackendTask {

    private static Logger logger = LoggerFactory.getLogger(CheckSocketBackendTask.class);

    public CheckSocketBackendTask(BalancingStrategy balancingStrategy) {

        super(balancingStrategy);
    }

    @Override
    public void check() {

        synchronized (balancingStrategy.getBackend()) {
            synchronized (backendInfo) {
                for (BackendInfo info : backendInfo) {
                    if (info.checkSocket()) {
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
                                logger.info("{} backend servers left", balancingStrategy.getBackend().size());
                            }
                        }
                    }
                }
            }
        }
    }
}
