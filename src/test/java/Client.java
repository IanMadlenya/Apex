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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by JackWhite20 on 04.11.2016.
 *
 * Just a simple UDP Client for testing purposes.
 */
public class Client {

    public static void main(String[] args) throws Exception {

        DatagramChannel datagramChannel = DatagramChannel.open();
        // Set the Apex instance to the remote endpoint
        datagramChannel.connect(new InetSocketAddress("localhost", 8080));

        String sent = "Test";

        datagramChannel.write(ByteBuffer.wrap(sent.getBytes()));

        System.out.println("Sent: " + sent);

        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        datagramChannel.receive(byteBuffer);

        System.out.println("Received: " + new String(byteBuffer.array()));
    }
}
