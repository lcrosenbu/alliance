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
package org.codice.alliance.nsili.mockserver.server;

import java.io.IOException;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codice.alliance.nsili.mockserver.impl.LibraryImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class MockNsili {

    private String iorString;

    // Singleton providing access to IOR string from reflection instantiated web service
    private static final MockNsili mockNsili = new MockNsili();

    private MockNsili() {
    }

    public static MockNsili getInstance() {
        return mockNsili;
    }

    public String getIorString() {
        return iorString;
    }

    public void startMockServer(int corbaPort) {
        ORB orb = null;

        try {
            orb = getOrbForServer(corbaPort);
            System.out.println("Server Started...");
            orb.run(); // blocks the current thread until the ORB is shutdown
        } catch (InvalidName | AdapterInactive | WrongPolicy | ServantNotActive e) {
            System.out.println("Unable to start the CORBA server.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Unable to generate the IOR file.");
            e.printStackTrace();
        }

        if (orb != null) {
            orb.destroy();
        }
    }

    public void startWebServer(int port) {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(MockWebService.class);
        sf.setAddress("http://localhost:" + port + "/");
        sf.create();
    }

    private ORB getOrbForServer(int port)
        throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, IOException {

        System.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(port));

        final ORB orb = ORB.init(new String[0], null);

        System.clearProperty("org.omg.CORBA.ORBInitialPort");

        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager().activate();

        org.omg.CORBA.Object objref = rootPOA.servant_to_reference(new LibraryImpl(rootPOA));
        iorString = orb.object_to_string(objref);

        return orb;
    }

    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("ERROR: Cannot start the mock NSILI server; No ports specified." +
                    "\nProvide arguments in format: [WEB_PORT], [CORBA_PORT]");
            return;
        }

        String[] ports = args[0].split(",");

        int webPort = Integer.parseInt(ports[0]);
        int corbaPort = Integer.parseInt(ports[1]);

        mockNsili.startWebServer(webPort);
        mockNsili.startMockServer(corbaPort);

        System.exit(0);
    }
}