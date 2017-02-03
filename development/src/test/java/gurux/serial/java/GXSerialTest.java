//
// --------------------------------------------------------------------------
// Gurux Ltd
//
//
//
// Filename: $HeadURL$
//
// Version: $Revision$,
// $Date$
// $Author$
//
// Copyright (c) Gurux Ltd
//
// ---------------------------------------------------------------------------
//
// DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2.
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
// ---------------------------------------------------------------------------

package gurux.serial.java;

import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.serial.GXSerial;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for serial port media.
 */
public class GXSerialTest extends TestCase {
    /**
     * Create the test case.
     *
     * @param testName
     *            Name of the test case.
     */
    public GXSerialTest(final String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(GXSerialTest.class);
    }

    /**
     * Test native library load.
     */
    public final void testNativeLibrary() {
        GXSerial.getPortNames();
    }

    /**
     * Test serial port open.
     * 
     * @throws Exception
     */
    public final void testOpen() {
        GXSerial serial = new GXSerial();
        try {
            serial.setPortName("Gurux");
            serial.open();
        } catch (Exception ex) {
            return;
        } finally {
            serial.close();
        }
        throw new RuntimeException("Invalid serial port open test failed.");
    }

    /**
     * Settings test.
     */
    public final void testSettings() {
        String nl = System.getProperty("line.separator");
        try (GXSerial serial = new GXSerial("COM1", BaudRate.BAUD_RATE_300, 7,
                Parity.EVEN, StopBits.ONE)) {
            String expected = "<Port>COM1</Port>" + nl
                    + "<BaudRate>300</BaudRate>" + nl + "<Parity>2</Parity>"
                    + nl + "<DataBits>7</DataBits>" + nl;
            String actual = serial.getSettings();
            assertEquals(expected, actual);
            try (GXSerial serial1 = new GXSerial()) {
                serial1.setSettings(actual);
            }
        }
    }
}
