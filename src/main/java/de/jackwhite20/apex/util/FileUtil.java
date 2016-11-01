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

package de.jackwhite20.apex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by JackWhite20 on 01.11.2016.
 */
public final class FileUtil {

    private static final String STATS_FILE = ".stats";

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {
        // No instance
    }

    public static long[] loadStats() {

        long[] result = new long[2];

        // Don't proceed if the file doesn't exists yet
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return result;
        }

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            // Total read bytes
            result[0] = Long.valueOf(in.readLine());
            // Total written bytes
            result[1] = Long.valueOf(in.readLine());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    public static void saveStats(long totalReadBytes, long totalWrittenBytes) {

        try (BufferedWriter out = new BufferedWriter(new FileWriter(STATS_FILE))) {
            out.write(Long.toString(totalReadBytes));
            out.write(System.lineSeparator());
            out.write(Long.toString(totalWrittenBytes));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
