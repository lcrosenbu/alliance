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
package org.codice.alliance.transformer.nitf.image;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Function;

import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.text.TextSegment;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

/**
 * NitfAttributes to represent the properties of a TextSegment.
 */
enum TextAttribute implements NitfAttribute<TextSegment> {
    FILE_PART_TYPE("nitf.text.filePartType",
            "TE",
            segment -> "TE"),
    TEXT_IDENTIFIER("nitf.text.textIdentifier",
            "TEXTID",
            TextSegment::getIdentifier),
    TEXT_ATTACHMENT_LEVEL("nitf.text.textAttachmentLevel",
            "TXTALVL",
            TextSegment::getAttachmentLevel,
            BasicTypes.INTEGER_TYPE),
    TEXT_DATE_AND_TIME("nitf.text.textDateAndTime",
            "TXTDT",
            segment -> convertNitfDate(segment.getTextDateTime()),
            BasicTypes.DATE_TYPE),
    TEXT_TITLE("nitf.text.textTitle",
            "TXTITL",
            TextSegment::getTextTitle),
    TEXT_SECURITY_CLASSIFICATION("nitf.text.textSecurityClassification",
            "TSCLAS",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassificationSystem()),
    TEXT_CLASSIFICATION_SECURITY_SYSTEM("nitf.text.textClassificationSecuritySystem",
            "TSCLSY",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassificationSystem()),
    TEXT_CODEWORDS("nitf.text.textCodewords",
            "TSCODE",
            segment -> segment.getSecurityMetadata()
                    .getCodewords()),
    TEXT_CONTROL_AND_HANDLING("nitf.text.textControlandHandling",
            "TSCTLH",
            segment -> segment.getSecurityMetadata()
                    .getControlAndHandling()),
    TEXT_RELEASING_INSTRUCTIONS("nitf.text.textReleasingInstructions",
            "TSREL",
            segment -> segment.getSecurityMetadata()
                    .getReleaseInstructions()),
    TEXT_DECLASSIFICATION_TYPE("nitf.text.textDeclassificationType",
            "TSDCTP",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationType()),
    TEXT_DECLASSIFICATION_DATE("nitf.text.textDeclassificationDate",
            "TSDCDT",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationDate()),
    TEXT_DECLASSIFICATION_EXEMPTION("nitf.text.textDeclassificationExemption",
            "TSDCXM",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationExemption()),
    TEXT_DOWNGRADE("nitf.text.textDowngrade",
            "TSDG",
            segment -> segment.getSecurityMetadata()
                    .getDowngrade()),
    TEXT_DOWNGRADE_DATE("nitf.text.textDowngradeDate",
            "TSDGDT",
            segment -> segment.getSecurityMetadata()
                    .getDowngradeDate()),
    TEXT_CLASSIFICATION_TEXT("nitf.text.textClassificationText",
            "TSCLTX",
            segment -> segment.getSecurityMetadata()
                    .getClassificationText()),
    TEXT_CLASSIFICATION_AUTHORITY_TYPE("nitf.text.textClassificationAuthorityType",
            "TSCA TP",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthorityType()),
    TEXT_CLASSIFICATION_AUTHORITY("nitf.text.textClassificationAuthority",
            "TSCAUT",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthority()),
    TEXT_CLASSIFICATION_REASON("nitf.text.textClassificationReason",
            "TSCRSN",
            segment -> segment.getSecurityMetadata()
                    .getClassificationReason()),
    TEXT_SECURITY_SOURCE_DATE("nitf.text.textSecuritySourceDate",
            "TSSRDT",
            segment -> segment.getSecurityMetadata()
                    .getSecuritySourceDate()),
    TEXT_SECURITY_CONTROL_NUMBER("nitf.text.textSecurityControlNumber",
            "TSCTLN",
            segment -> segment.getSecurityMetadata()
                    .getSecurityControlNumber()),
    TEXT_FORMAT("nitf.text.textFormat",
            "TXTFMT",
            segment -> segment.getTextFormat()
                    .name()),
    TEXT_EXTENDED_SUBHEADER_DATA_LENGTH("nitf.text.textExtendedSubheaderDataLength",
            "TXSHDL",
            TextSegment::getExtendedHeaderDataOverflow,
            BasicTypes.INTEGER_TYPE);

    private String shortName;

    private String longName;

    private Function<TextSegment, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    TextAttribute(final String lName, final String sName,
            final Function<TextSegment, Serializable> accessor) {
        this(lName, sName, accessor, BasicTypes.STRING_TYPE);
    }

    TextAttribute(final String lName, final String sName,
            final Function<TextSegment, Serializable> accessor, AttributeType attributeType) {
        this.longName = lName;
        this.shortName = sName;
        this.accessorFunction = accessor;
        this.attributeDescriptor = new AttributeDescriptorImpl(
                longName,
                true,
                true,
                false,
                true,
                attributeType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getShortName() {
        return this.shortName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLongName() {
        return this.longName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Function<TextSegment, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    /**
     * Equivalent to getLongName()
     *
     * @return the attribute's long name.
     */
    @Override
    public String toString() {
        return getLongName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
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
