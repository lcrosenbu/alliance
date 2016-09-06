/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.transformer.nitf.image;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestPreStoragePluginParameterized {

    private static final String FILESYSTEM_INVALID_CHARS = "\\/:*?<>|&;";

    private static final String QUALIFIER = "testqualifier";

    private static final String DEFAULT_DERIVED_FILENAME = QUALIFIER + ".jpg";

    private NitfPreStoragePlugin nitfPreStoragePlugin = null;

    private String fInput;

    private String fExpected;

    public TestPreStoragePluginParameterized(String input, String expectedOutput) {
        fInput= input;
        fExpected= expectedOutput;
    }

    @Parameterized.Parameters(name = "Test #{index}: Create derived image filename from \"{0}\"")
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] {
                {null, DEFAULT_DERIVED_FILENAME},
                {"", DEFAULT_DERIVED_FILENAME},
                {"@#$%^&*()+-={}|[]<>?:", DEFAULT_DERIVED_FILENAME},
                {"_", DEFAULT_DERIVED_FILENAME},
                {"Too Legit To Quit", QUALIFIER + "-toolegittoquit.jpg"},
                {"A bunch of _invalid_ characters! @#$%^&*()+-={}|[]<>?:",
                        QUALIFIER + "-abunchof_invalid_characters.jpg"}});
    }

    @Before
    public void setUp() {
        nitfPreStoragePlugin = new NitfPreStoragePlugin();
    }

    @Test
    public void testBuildDerivedImageTitle() {
        String derivedFilename = nitfPreStoragePlugin.buildDerivedImageTitle(fInput, QUALIFIER);
        assertThat(StringUtils.containsNone(derivedFilename, FILESYSTEM_INVALID_CHARS), is(true));
        assertThat(derivedFilename, is(fExpected));
    }
}
