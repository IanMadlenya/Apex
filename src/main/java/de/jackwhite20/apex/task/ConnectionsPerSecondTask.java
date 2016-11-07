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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by JackWhite20 on 06.11.2016.
 */
public class ConnectionsPerSecondTask {

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private AtomicInteger value = new AtomicInteger();

    private int lastValue = 0;

    private int perSecond = 0;

    public ConnectionsPerSecondTask() {

        // TODO: 07.11.2016 Use shared executor service
        scheduledExecutorService.scheduleAtFixedRate(this::check, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void stop() {

        scheduledExecutorService.shutdown();
    }

    public void inc() {

        value.incrementAndGet();
    }

    private void check() {

        int now = value.get();

        perSecond = Math.max(0, now - lastValue);

        lastValue = now;
    }

    public int getPerSecond() {

        return perSecond;
    }
}
