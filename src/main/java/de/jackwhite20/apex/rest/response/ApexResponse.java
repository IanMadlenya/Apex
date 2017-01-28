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

package de.jackwhite20.apex.rest.response;

/**
 * Created by JackWhite20 on 02.07.2016.
 */
public class ApexResponse {

    private Status status;

    private String message;

    public ApexResponse(Status status, String message) {

        this.status = status;
        this.message = message;
    }

    public Status getStatus() {

        return status;
    }

    public String getMessage() {

        return message;
    }

    public enum Status {

        OK,
        SERVER_NOT_FOUND,
        SERVER_ALREADY_ADDED,
        ERROR
    }
}
