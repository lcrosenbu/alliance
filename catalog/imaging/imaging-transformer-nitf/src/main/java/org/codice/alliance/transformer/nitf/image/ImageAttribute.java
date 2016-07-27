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

import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Media;

/**
 * NitfAttributes to represent the properties of a ImageSegment.
 */
public enum ImageAttribute implements NitfAttribute<ImageSegment> {
    FILE_PART_TYPE("nitf.image.filePartType",
            "IM",
            segment -> "IM"),
    IMAGE_IDENTIFIER_1("nitf.image.imageIdentifier1",
            "IID1",
            ImageSegment::getIdentifier),
    IMAGE_DATE_AND_TIME(DateTime.START,
            "IDATIM",
            segment -> convertNitfDate(segment.getImageDateTime()), BasicTypes.DATE_TYPE),
    TARGET_IDENTIFIER(Isr.TARGET_ID,
            "TGTID",
            segment -> getTargetId(segment)),
    IMAGE_IDENTIFIER_2(Isr.IMAGE_ID,
            "IID2",
            ImageSegment::getImageIdentifier2),
    IMAGE_SECURITY_CLASSIFICATION("nitf.image.imageSecurityClassification",
            "ISCLAS",
            segment -> segment.getSecurityMetadata().getSecurityClassification()
                    .name()),
    IMAGE_CLASSIFICATION_SECURITY_SYSTEM("nitf.image.imageClassificationSecuritySystem",
            "ISCLSY",
            segment -> segment.getSecurityMetadata()
                    .getSecurityClassificationSystem()),
    IMAGE_CODEWORDS("nitf.image.imageCodewords",
            "ISCODE",
            segment -> segment.getSecurityMetadata()
                    .getCodewords()),
    IMAGE_CONTROL_AND_HANDLING("nitf.image.imageControlandHandling",
            "ISCTLH",
            segment -> segment.getSecurityMetadata()
                    .getControlAndHandling()),
    IMAGE_RELEASING_INSTRUCTIONS("nitf.image.imageReleasingInstructions",
            "ISREL",
            segment -> segment.getSecurityMetadata()
                    .getReleaseInstructions()),
    IMAGE_DECLASSIFICATION_TYPE("nitf.image.imageDeclassificationType",
            "ISDCTP",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationType()),
    IMAGE_DECLASSIFICATION_DATE("nitf.image.imageDeclassificationDate",
            "ISDCDT",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationDate()),
    IMAGE_DECLASSIFICATION_EXEMPTION("nitf.image.imageDeclassificationExemption",
            "ISDCXM",
            segment -> segment.getSecurityMetadata()
                    .getDeclassificationExemption()),
    IMAGE_DOWNGRADE("nitf.image.imageDowngrade",
            "ISDG",
            segment -> segment.getSecurityMetadata()
                    .getDowngrade()),
    IMAGE_DOWNGRADE_DATE("nitf.image.imageDowngradeDate",
            "ISDGDT",
            segment -> segment.getSecurityMetadata()
                    .getDowngradeDate()),
    IMAGE_CLASSIFICATION_TEXT("nitf.image.imageClassificationText",
            "ISCLTX",
            segment -> segment.getSecurityMetadata()
                    .getClassificationText()),
    IMAGE_CLASSIFICATION_AUTHORITY_TYPE("nitf.image.imageClassificationAuthorityType",
            "ISCATP",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthorityType()),
    IMAGE_CLASSIFICATION_AUTHORITY("nitf.image.imageClassificationAuthority",
            "ISCAUT",
            segment -> segment.getSecurityMetadata()
                    .getClassificationAuthority()),
    IMAGE_CLASSIFICATION_REASON("nitf.image.imageClassificationReason",
            "ISCRSN",
            segment -> segment.getSecurityMetadata()
                    .getClassificationReason()),
    IMAGE_SECURITY_SOURCE_DATE("nitf.image.imageSecuritySourceDate",
            "ISSRDT",
            segment -> segment.getSecurityMetadata()
                    .getSecuritySourceDate()),
    IMAGE_SECURITY_CONTROL_NUMBER("nitf.image.imageSecurityControlNumber",
            "ISCTLN",
            segment -> segment.getSecurityMetadata()
                    .getSecurityControlNumber()),
    IMAGE_SOURCE(Isr.ORIGINAL_SOURCE,
            "ISORCE",
            ImageSegment::getImageSource),
    NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE(Media.HEIGHT,
            "NROWS",
            ImageSegment::getNumberOfRows,
            BasicTypes.LONG_TYPE),
    NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE(Media.WIDTH,
            "NCOLS",
            ImageSegment::getNumberOfColumns,
            BasicTypes.LONG_TYPE),
    PIXEL_VALUE_TYPE("nitf.image.pixelValueType",
            "PVTYPE",
            segment -> segment.getPixelValueType()
                    .name()),
    IMAGE_REPRESENTATION(Media.ENCODING,
            "IREP",
            segment -> segment.getImageRepresentation()
                    .name()),
    IMAGE_CATEGORY(Isr.CATEGORY,
            "ICAT",
            segment -> segment.getImageCategory()
                    .name()),
    ACTUAL_BITS_PER_PIXEL_PER_BAND("nitf.image.actualBitsPerPixelPerBand",
            "ABPP",
            ImageSegment::getActualBitsPerPixelPerBand,
            BasicTypes.INTEGER_TYPE),
    PIXEL_JUSTIFICATION("nitf.image.pixelJustification",
            "PJUST",
            segment -> segment.getPixelJustification()
                    .name()),
    IMAGE_COORDINATE_REPRESENTATION("nitf.image.imageCoordinateRepresentation",
            "ICORDS",
            segment -> segment.getImageCoordinatesRepresentation()
                    .name()),
    NUMBER_OF_IMAGE_COMMENTS("nitf.image.numberOfImageComments",
            "NICOM",
            segment -> segment.getImageComments()
                    .size(),
            BasicTypes.INTEGER_TYPE),
    IMAGE_COMMENT_1("nitf.image.imageComment1",
            "ICOM1",
            segment -> segment.getImageComments()
                    .size() > 0 ?
                    segment.getImageComments()
                            .get(0) :
                    ""),
    IMAGE_COMMENT_2("nitf.image.imageComment2",
            "ICOM2",
            segment -> segment.getImageComments()
                    .size() > 1 ?
                    segment.getImageComments()
                            .get(1) :
                    ""),
    IMAGE_COMMENT_3("nitf.image.imageComment3",
            "ICOM3",
            segment -> segment.getImageComments()
                    .size() > 2 ?
                    segment.getImageComments()
                            .get(2) :
                    ""),
    IMAGE_COMPRESSION(Media.COMPRESSION,
            "IC",
            segment -> segment.getImageCompression()
                    .name()),
    NUMBER_OF_BANDS(Media.NUMBER_OF_BANDS,
            "NBANDS",
            ImageSegment::getNumBands, BasicTypes.INTEGER_TYPE),
    IMAGE_MODE("nitf.image.imageMode",
            "IMODE",
            segment -> segment.getImageMode()
                    .name()),
    NUMBER_OF_BLOCKS_PER_ROW("nitf.image.numberOfBlocksperRow",
            "NBPR",
            ImageSegment::getNumberOfBlocksPerRow,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_BLOCKS_PER_COLUMN("nitf.image.numberOfBlocksperColumn",
            "NBPC",
            ImageSegment::getNumberOfBlocksPerColumn,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_PIXELS_PER_BLOCK_HORIZONTAL("nitf.image.numberOfPixelsPerBlockHorizontal",
            "NPPBH",
            ImageSegment::getNumberOfPixelsPerBlockHorizontal,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_PIXELS_PER_BLOCK_VERTICAL("nitf.image.numberOfPixelsPerBlockVertical",
            "NPPBV",
            ImageSegment::getNumberOfPixelsPerBlockVertical,
            BasicTypes.INTEGER_TYPE),
    NUMBER_OF_BITS_PER_PIXEL(Media.BITS_PER_SAMPLE,
            "NBPP",
            ImageSegment::getNumberOfBitsPerPixelPerBand,
            BasicTypes.INTEGER_TYPE),
    IMAGE_DISPLAY_LEVEL("nitf.image.imageDisplayLevel",
            "IDLVL",
            ImageSegment::getImageDisplayLevel,
            BasicTypes.INTEGER_TYPE),
    IMAGE_ATTACHMENT_LEVEL("nitf.image.imageAttachmentLevel",
            "IALVL",
            ImageSegment::getAttachmentLevel,
            BasicTypes.INTEGER_TYPE),
    IMAGE_LOCATION(Core.LOCATION,
            "ILOC",
            segment -> segment.getImageLocationRow() + "," + segment.getImageLocationColumn()),
    IMAGE_MAGNIFICATION("nitf.image.imageMagnification",
            "IMAG",
            segment -> segment.getImageMagnification()
                    .trim());

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageAttribute.class);

    private String shortName;

    private String longName;

    private Function<ImageSegment, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    ImageAttribute(final String lName, final String sName,
            final Function<ImageSegment, Serializable> accessor) {
        this(lName, sName, accessor, BasicTypes.STRING_TYPE);
    }

    ImageAttribute(final String lName, final String sName,
            final Function<ImageSegment, Serializable> accessor, AttributeType attributeType) {
        this.accessorFunction = accessor;
        this.shortName = sName;
        this.longName = lName;
        this.attributeDescriptor = new AttributeDescriptorImpl(
                longName,
                true,
                true,
                false,
                true,
                attributeType);
    }

    private static String getTargetId(ImageSegment imageSegment) {
        if (imageSegment == null || imageSegment.getImageTargetId() == null) {
            return null;
        }

        try {
            return imageSegment.getImageTargetId()
                    .textValue()
                    .trim();
        } catch (NitfFormatException nfe) {
            LOGGER.warn(nfe.getMessage(), nfe);
        }

        return null;
    }

    private static Date convertNitfDate(org.codice.imaging.nitf.core.common.DateTime nitfDateTime) {
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
    public Function<ImageSegment, Serializable> getAccessorFunction() {
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
}
