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
package org.codice.alliance.transformer.nitf.gmti;

import java.io.Serializable;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

enum AcftbAttribute implements NitfAttribute<Tre> {
    AIRCRAFT_MISSION_ID(Isr.MISSION_ID,
            "AC_MSN_ID",
            tre -> GmtiTreUtility.getTreValue(tre, "AC_MSN_ID")),
    AIRCRAFT_TAIL_NUMBER(Isr.PLATFORM_ID,
            "AC_TAIL_NO",
            tre -> GmtiTreUtility.getTreValue(tre, "AC_TAIL_NO")),
    SENSOR_ID_TYPE(Isr.SENSOR_TYPE,
            "SENSOR_ID_TYPE",
            tre -> GmtiTreUtility.getTreValue(tre, "SENSOR_ID_TYPE")),
    SENSOR_ID(Isr.SENSOR_ID,
            "SENSOR_ID",
            tre -> GmtiTreUtility.getTreValue(tre, "SENSOR_ID"));

    private String shortName;

    private String longName;

    private Function<Tre, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    AcftbAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction) {
        this.longName = longName;
        this.shortName = shortName;
        this.accessorFunction = accessorFunction;
        this.attributeDescriptor = new AttributeDescriptorImpl(longName,
                true,
                true,
                false,
                true,
                BasicTypes.STRING_TYPE);
    }

    @Override
    public String getLongName() {
        return this.longName;
    }

    @Override
    public String getShortName() {
        return this.shortName;
    }

    @Override
    public Function<Tre, Serializable> getAccessorFunction() {
        return this.accessorFunction;
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }
}
