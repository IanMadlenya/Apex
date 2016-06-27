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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class BackendInfo {

    private String name;

    private String host;

    private int port;

    public BackendInfo(String name, String host, int port) {

        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getName() {

        return name;
    }

    public String getHost() {

        return host;
    }

    public int getPort() {

        return port;
    }

    public boolean check() {

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String toString() {

        return "BackendInfo{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
