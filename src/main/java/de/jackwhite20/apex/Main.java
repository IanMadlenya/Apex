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

package de.jackwhite20.apex;

import de.jackwhite20.cope.Cope;
import de.jackwhite20.cope.CopeConfig;
import de.jackwhite20.cope.config.Header;
import de.jackwhite20.cope.config.Key;
import de.jackwhite20.cope.config.Value;
import de.jackwhite20.cope.exception.CopeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by JackWhite20 on 26.06.2016.
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Starting Apex");

        File config = new File("config.cope");
        if (!config.exists()) {
            try {
                Files.copy(Main.class.getClassLoader().getResourceAsStream("config.cope"), config.toPath());
            } catch (IOException e) {
                logger.error("Unable to copy default config! No write permissions?", e);
                return;
            }
        }

        try {
            CopeConfig copeConfig = Cope.from(config)
                    .def(new Header("general"), new Key("server"), new Value("0.0.0.0"), new Value("80"))
                    .def(new Header("general"), new Key("backlog"), new Value("100"))
                    .def(new Header("general"), new Key("threads"), new Value(String.valueOf(Runtime.getRuntime().availableProcessors() * 2)))
                    .def(new Header("general"), new Key("balance"), new Value("RANDOM"))
                    .def(new Header("general"), new Key("timeout"), new Value("60"), new Value("60"))
                    .def(new Header("general"), new Key("probe"), new Value("10000"))
                    .def(new Header("general"), new Key("debug"), new Value("true"))
                    .build();

            logger.info("Config loaded");

            Apex apex = new Apex(copeConfig);
            apex.start();
            apex.console();
        } catch (CopeException e) {
            logger.error("Unable to load config", e);
        }
    }
}
