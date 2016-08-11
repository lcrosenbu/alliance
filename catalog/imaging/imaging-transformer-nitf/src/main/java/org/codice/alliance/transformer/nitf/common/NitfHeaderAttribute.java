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

import java.io.Serializable;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.imaging.nitf.core.header.NitfHeader;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;

/**
 * NitfAttributes to represent the properties of a NitfFileHeader.
 */
public enum NitfHeaderAttribute implements NitfAttribute<NitfHeader> {
    FILE_PROFILE_NAME(Media.FORMAT,
            "FHDR",
            header -> header.getFileType()
                    .name()),
    FILE_VERSION(Media.FORMAT_VERSION,
            "FVER",
            header -> header.getFileType()
                    .name()),
    ORIGINATING_STATION_ID(Isr.ORGANIZATIONAL_UNIT,
            "OSTAID",
            NitfHeader::getOriginatingStationId),
    FILE_TITLE(Core.TITLE,
            "FTITLE",
            NitfHeader::getFileTitle),
    FILE_SECURITY_CLASSIFICATION(Security.CLASSIFICATION,
            "FSCLAS",
            header -> header.getFileSecurityMetadata()
                    .getSecurityClassification()
                    .name()),
    FILE_CLASSIFICATION_SECURITY_SYSTEM(Security.CLASSIFICATION_SYSTEM,
            "FSCLSY",
            header -> header.getFileSecurityMetadata()
                    .getSecurityClassificationSystem()),
    FILE_CODE_WORDS(Security.CODEWORDS,
            "FSCODE",
            header -> header.getFileSecurityMetadata()
                    .getCodewords()),
    FILE_CONTROL_AND_HANDLING(Security.DISSEMINATION_CONTROLS,
            "FSCTLH",
            header -> header.getFileSecurityMetadata()
                    .getControlAndHandling()),
    FILE_RELEASING_INSTRUCTIONS(Security.RELEASABILITY,
            "FSREL",
            header -> header.getFileSecurityMetadata()
                    .getReleaseInstructions()),
    ORIGINATORS_NAME(Contact.CREATOR_NAME,
            "ONAME",
            NitfHeader::getOriginatorsName),
    ORIGINATORS_PHONE_NUMBER(Contact.CREATOR_PHONE,
            "OPHONE",
            NitfHeader::getOriginatorsPhoneNumber);

    private String shortName;

    private String longName;

    private Function<NitfHeader, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    private NitfHeaderAttribute(final String lName, final String sName,
            final Function<NitfHeader, Serializable> function) {
        this(lName, sName, function, BasicTypes.STRING_TYPE);
    }

    private NitfHeaderAttribute(final String lName, final String sName,
            final Function<NitfHeader, Serializable> function, AttributeType attributeType) {
        this.shortName = sName;
        this.longName = lName;
        this.accessorFunction = function;
        this.attributeDescriptor = new AttributeDescriptorImpl(longName,
                true,
                true,
                false,
                false,
                attributeType);
    }

    /**
     * {@inheritDoc}
     */
    public final String getShortName() {
        return this.shortName;
    }

    /**
     * {@inheritDoc}
     */
    public final String getLongName() {
        return this.longName;
    }

    /**
     * {@inheritDoc}
     */
    public Function<NitfHeader, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    /**
     * Equivalent to getLongName().
     *
     * @return the attribute's long name.
     */
    public String toString() {
        return getLongName();
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }
}
