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

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codice.alliance.nsili.mockserver.impl.LibraryImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MockNsili {

    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Cannot run the mock server.  No ports are specified.");
            return;
        }

        String[] ports = args[0].split(",");

        int webPort = Integer.parseInt(ports[0]);

        int corbaPort = Integer.parseInt(ports[1]);

        startWebServer(webPort);

        startMockServer(corbaPort, System.getProperty("user.dir") + "/target/ior.txt");

        System.exit(0);
    }

    public static void startMockServer(int corbaPort, String iorFilePath) {
        ORB orb = null;

        try {
            orb = getOrbForServer(corbaPort, iorFilePath);
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

    public static void startWebServer(int port) {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(MockWebService.class);
        sf.setAddress("http://localhost:" + port + "/");
        sf.create();
    }

    private static ORB getOrbForServer(int port, String iorFilePath)
        throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, IOException {

        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", port);
        final ORB orb = ORB.init(new String[0], props);

        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        rootPOA.the_POAManager().activate();

        org.omg.CORBA.Object objref = rootPOA.servant_to_reference(new LibraryImpl(rootPOA));

        Writer writer = new OutputStreamWriter(new FileOutputStream(iorFilePath), "UTF-8");

        writer.write(orb.object_to_string(objref));
        writer.close();

        rootPOA.the_POAManager().activate();

        return orb;
    }
}