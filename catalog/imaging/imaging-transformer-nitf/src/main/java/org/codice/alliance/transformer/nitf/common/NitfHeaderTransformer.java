/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.transformer.nitf.common;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;

public class NitfHeaderTransformer extends SegmentHandler {

    private static final String NULL_ARGUMENT_MESSAGE = "Cannot transform null input.";

    public NitfSegmentsFlow transform(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard)
            throws IOException {
        if (nitfSegmentsFlow == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_MESSAGE);
        }

        nitfSegmentsFlow.fileHeader(header -> handleNitfHeader(metacard, header));
        return nitfSegmentsFlow;
    }

    private void handleNitfHeader(Metacard metacard, NitfHeader header) {
        Date date = convertNitfDate(header.getFileDateTime());
        metacard.setAttribute(new AttributeImpl(Core.MODIFIED, date));
        metacard.setAttribute(new AttributeImpl(Core.CREATED, date));
        metacard.setAttribute(new AttributeImpl(Metacard.EFFECTIVE, date));

        handleSegmentHeader(metacard, header, NitfHeaderAttribute.values());
    }

    private static Date convertNitfDate(DateTime nitfDateTime) {
        if (nitfDateTime == null || nitfDateTime.getZonedDateTime() == null) {
            return null;
        }

        ZonedDateTime zonedDateTime = nitfDateTime.getZonedDateTime();
        Instant instant = zonedDateTime.toInstant();

        if (instant != null) {
            return Date.from(instant);
        }

        return null;
    }
}
