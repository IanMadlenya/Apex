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

package de.jackwhite20.apex.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JackWhite20 on 29.10.2016.
 */
public class CommandManager {

    private Map<String, Command> commands = new HashMap<>();

    public Command findCommand(String name) {

        return (commands.containsKey(name)) ?
                commands.get(name) :
                commands.values().stream().filter((Command c) -> c.isValidAlias(name)).findFirst().orElse(null);
    }

    public void addCommand(Command command) {

        commands.put(command.getName(), command);
    }

    public void removeCommand(String command) {

        commands.remove(command);
    }

    public List<Command> getCommands() {

        return new ArrayList<>(commands.values());
    }
}
