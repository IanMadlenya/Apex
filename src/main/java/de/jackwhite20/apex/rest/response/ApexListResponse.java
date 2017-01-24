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

import de.jackwhite20.apex.util.BackendInfo;

import java.util.List;

/**
 * Created by JackWhite20 on 28.10.2016.
 */
public class ApexListResponse extends ApexResponse {

    private List<BackendInfo> backendInfo;

    public ApexListResponse(Status status, String message, List<BackendInfo> backendInfo) {

        super(status, message);

        this.backendInfo = backendInfo;
    }
}
