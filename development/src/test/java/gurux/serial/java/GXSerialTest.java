package gurux.serial.java;

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
    public final void testOpen() throws Exception {
        GXSerial serial = new GXSerial();
        try {
            serial.setPortName("Gurux");
            serial.open();
        } catch (Exception ex) {
            return;
        } finally {
            serial.close();
        }
        throw new Exception("Invalid serial port open test failed.");

    }
}
