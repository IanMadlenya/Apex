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

package de.jackwhite20.apex.command.impl;

import de.jackwhite20.apex.Apex;
import de.jackwhite20.apex.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by JackWhite20 on 29.10.2016.
 */
public class HelpCommand extends Command {

    private static Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    public HelpCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        logger.info("Available Commands:");
        for (Command command : Apex.getCommandManager().getCommands()) {
            logger.info("{} [{}] - {}", command.getName(), String.join(", ", command.getAliases()), command.getDescription());
        }

        return true;
    }
}
