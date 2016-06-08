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

import org.codice.alliance.nsili.mockserver.server.MockNsili;

public class MockNsiliRunnable implements Runnable {

    private int webPort;

    private int corbaPort;


    public MockNsiliRunnable(int webPort, int corbaPort) {
        this.webPort = webPort;
        this.corbaPort = corbaPort;
    }

    @Override
    public void run() {
        //MockNsili.startWebServer(this.webPort);
        MockNsili.startMockServer(this.corbaPort, System.getProperty("user.dir") + "/../../ior.txt");
    }
}
