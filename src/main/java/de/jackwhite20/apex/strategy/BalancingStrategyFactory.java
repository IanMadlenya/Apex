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

package de.jackwhite20.apex.strategy;

import de.jackwhite20.apex.strategy.impl.FastestBalancingStrategy;
import de.jackwhite20.apex.strategy.impl.LeastConnectionBalancingStrategy;
import de.jackwhite20.apex.strategy.impl.RandomBalancingStrategy;
import de.jackwhite20.apex.strategy.impl.RoundRobinBalancingStrategy;
import de.jackwhite20.apex.util.BackendInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by JackWhite20 on 04.07.2016.
 */
public final class BalancingStrategyFactory {

    private static Logger logger = LoggerFactory.getLogger(BalancingStrategyFactory.class);

    private BalancingStrategyFactory() {
        // no instance
    }

    public static BalancingStrategy create(StrategyType type, List<BackendInfo> backendInfo) {

        if (type == null) {
            type = StrategyType.RANDOM;

            logger.info("Using default strategy: {}", type);
        } else {
            logger.info("Using strategy: {}", type);
        }

        switch (type) {
            case RANDOM:
                return new RandomBalancingStrategy(backendInfo);
            case ROUND_ROBIN:
                return new RoundRobinBalancingStrategy(backendInfo);
            case LEAST_CON:
                return new LeastConnectionBalancingStrategy(backendInfo);
            case FASTEST:
                return new FastestBalancingStrategy(backendInfo);
            default:
                throw new IllegalStateException("unknown strategy type '" + type + "'");
        }
    }
}
