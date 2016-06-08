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
package alliance.test.itests;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.cm.Configuration;

import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.response.ValidatableResponse;

import ddf.common.test.AfterExam;
import ddf.common.test.BeforeExam;
import ddf.test.itests.AbstractIntegrationTest;

/**
 * Tests the Alliance additions to DDF framework components.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestFederation extends AbstractIntegrationTest {

    private Thread mockServerThread;

    private static final String[] REQUIRED_APPS = {"catalog-app", "solr-app", "spatial-app",
        "nsili-app"};

    private static final String NSILI_SOURCE_ID = "nsiliSource";

    @Override
    protected Option[] configureDistribution() {
        return options(
                karafDistributionConfiguration(
                        maven().groupId("org.codice.alliance.distribution").artifactId("alliance")
                                .type("zip").versionAsInProject().getURL(),
                        "alliance", KARAF_VERSION).unpackDirectory(new File("target/exam"))
                                .useDeployFolder(false));
    }

    @Override
    protected Option[] configureCustom() {
        return options(
                wrappedBundle(mavenBundle("ddf.test.itests", "test-itests-ddf").classifier("tests")
                        .versionAsInProject()).bundleSymbolicName("test-itests-ddf")
                                .exports("ddf.test.itests.*"),
                wrappedBundle(mavenBundle().groupId("org.codice.alliance.distribution")
                        .artifactId("sample-nsili-server").versionAsInProject()),
                wrappedBundle(mavenBundle().groupId("ddf.catalog.core")
                        .artifactId("catalog-core-api-impl").versionAsInProject()),
                wrappedBundle(mavenBundle().groupId("org.codice.alliance.nsili")
                        .artifactId("catalog-nsili-common").versionAsInProject()),
                wrappedBundle(mavenBundle().groupId("org.codice.alliance.nsili")
                        .artifactId("catalog-nsili-transformer").versionAsInProject()),
                wrappedBundle(mavenBundle().groupId("org.jgrapht").artifactId("jgrapht-core")
                        .versionAsInProject()),
                keepRuntimeFolder());
    }

    @BeforeExam
    public void beforeAllianceTest() throws Exception {
        try {
            basePort = getBasePort();
            setLogLevels();

            getServiceManager().waitForRequiredApps(REQUIRED_APPS);
            getServiceManager().waitForAllBundles();
            getCatalogBundle().waitForCatalogProvider();

            configureSecurityStsClient();

            startMockResources();
            configureNsiliSource();
        } catch (Exception e) {
            LOGGER.error("Failed in @BeforeExam: ", e);
            fail("Failed in @BeforeExam: " + e.getMessage());
        }
    }

    @Test
    public void testNsiliSourceAvailable() throws Exception {
        // Determine if the Nsili Source has been configured in Alliance and available
        // @formatter:off
        given().auth().basic("admin", "admin").when().get(ADMIN_ALL_SOURCES_PATH.getUrl()).then()
                .log().all().assertThat().body(containsString("\"fpid\":\"NSILI_Federated_Source\""));
        // @formatter:on
    }

    @Test
    public void testNsiliSourceOpenSearchGetAll() throws Exception {
        // Simple search query to assert # records returned from mock source
        ValidatableResponse response = executeOpenSearch("xml", "q=*", "src=" + NSILI_SOURCE_ID);
        response.body("metacards.metacard.size()", equalTo(10));
    }

    @Test
    public void testNsiliSourceOpenSearchLocation() throws Exception {
        // Perform query with location filtering
        ValidatableResponse response = executeOpenSearch("xml", "q=*", "lat=-53.0", "lon=-111.0",
                "radius=50", "src=" + NSILI_SOURCE_ID);
        response.log().all().body("metacards.metacard.size()", equalTo(10));
    }

    @AfterExam
    public void afterAllianceTest() throws Exception {
        if (mockServerThread != null) {
            mockServerThread.interrupt();
        }
        mockServerThread = null;
    }

    private void startMockResources() throws Exception {
        DynamicPort webPort = new DynamicPort("org.codice.alliance.corba_web_port", 6);
        DynamicPort corbaPort = new DynamicPort("org.codice.alliance.corba_port", 7);

        MockNsiliRunnable mockServer = new MockNsiliRunnable(Integer.parseInt(webPort.getPort()),
                Integer.parseInt(corbaPort.getPort()));
        mockServerThread = new Thread(mockServer, "mockServer");
        mockServerThread.start();
    }

    private void configureNsiliSource() throws IOException {
        NsiliSourceProperties sourceProperties = new NsiliSourceProperties(NSILI_SOURCE_ID,
                "file://" + System.getProperty("user.dir") + "/../../ior.txt");

        sourceProperties.put("maxHitCount", 250);
        sourceProperties.put("numberWorkerThreads", 4);
        sourceProperties.put("excludeSortOrder", false);

        getServiceManager().createManagedService(NsiliSourceProperties.FACTORY_PID,
                sourceProperties);
    }

    private void configureSecurityStsClient() throws IOException, InterruptedException {
        Configuration stsClientConfig = configAdmin
                .getConfiguration("ddf.security.sts.client.configuration.cfg", null);
        Dictionary<String, Object> properties = new Hashtable<>();

        properties.put("address", DynamicUrl.SECURE_ROOT + HTTPS_PORT.getPort()
                + "/services/SecurityTokenService?wsdl");
        stsClientConfig.update(properties);
    }

    private ValidatableResponse executeOpenSearch(String format, String... query) {
        StringBuilder buffer = new StringBuilder(OPENSEARCH_PATH.getUrl()).append("?")
                .append("format=").append(format);

        for (String term : query) {
            buffer.append("&").append(term);
        }

        String url = buffer.toString();
        LOGGER.info("Getting response to {}", url);

        return when().get(url).then();
    }

    private void assertMetacards(String responseXml, int expectedNumMetacards,
            String expectedSource, String expectedType) {
        XmlPath xmlPath = new XmlPath(responseXml);
        int numMetacards = (Integer) xmlPath.get("metacards.metacard.size()");
        assertThat(numMetacards, equalTo(expectedNumMetacards));

        for (int i = 0; i < numMetacards; i++) {
            assertThat((String) xmlPath.get("metacards.metacard[" + i + "].type"),
                    equalTo(expectedType));
            assertThat((String) xmlPath.get("metacards.metacard[" + i + "].source"),
                    equalTo(expectedSource));
        }
    }

    public class NsiliSourceProperties extends HashMap<String, Object> {
        public static final String SYMBOLIC_NAME = "catalog-nsili-source";

        public static final String FACTORY_PID = "NSILI_Federated_Source";

        public NsiliSourceProperties(String sourceId, String iorUrl) {
            this.putAll(getMetatypeDefaults(SYMBOLIC_NAME, FACTORY_PID));
            this.put("id", sourceId);
            this.put("iorUrl", iorUrl);
            this.put("pollInterval", 1);
        }
    }
}
