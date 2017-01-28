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

package de.jackwhite20.apex.task;

import com.google.common.collect.Lists;
import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.strategy.BalancingStrategy;
import de.jackwhite20.apex.util.BackendInfo;

import java.util.List;

/**
 * Created by JackWhite20 on 06.11.2016.
 */
public abstract class CheckBackendTask implements Runnable {

    protected final List<BackendInfo> backendInfo;

    protected BalancingStrategy balancingStrategy;

    public CheckBackendTask(BalancingStrategy balancingStrategy) {

        this.balancingStrategy = balancingStrategy;
        this.backendInfo = Lists.newArrayList(balancingStrategy.getBackend());
    }

    public abstract void check();

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

        check();
    }
}
