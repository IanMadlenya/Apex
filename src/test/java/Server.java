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

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by JackWhite20 on 05.11.2016.
 *
 * Just a simple echo UDP Server for testing purposes.
 */
public class Server {

    public static void main(String[] args) throws Exception {

        DatagramPacket pack = new DatagramPacket(new byte[100], 100);
        DatagramSocket datagramSocket = new DatagramSocket(6000);

        while (true) {
            datagramSocket.receive(pack);
            System.out.println("Received");
            datagramSocket.send(pack);
        }
    }
}
