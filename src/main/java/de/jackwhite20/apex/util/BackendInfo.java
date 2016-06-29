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

    private double ping;

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

    public double getPing() {

        return ping;
    }

    public boolean check() {

        Socket socket = new Socket();
        try {
            long now = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(host, port), 4000);
            ping = System.currentTimeMillis() - now;

            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BackendInfo that = (BackendInfo) o;

        return port == that.port &&
                (name != null ? name.equals(that.name) : that.name == null &&
                        (host != null ? host.equals(that.host) : that.host == null));
    }

    @Override
    public int hashCode() {

        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;

        return result;
    }

    @Override
    public String toString() {

        return "BackendInfo{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", ping=" + ping +
                '}';
    }
}
