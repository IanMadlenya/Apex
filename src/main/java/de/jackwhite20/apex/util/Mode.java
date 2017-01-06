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

/**
 * Created by JackWhite20 on 05.11.2016.
 */
public enum Mode {

    TCP,
    UDP;

    public static Mode of(String modeString) {

        for (Mode mode : values()) {
            if (mode.name().toLowerCase().equals(modeString.toLowerCase())) {
                return mode;
            }
        }

        return null;
    }
}
