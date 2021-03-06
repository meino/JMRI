package jmri.jmrix.openlcb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CanConverterTest.class,
        OlcbAddressTest.class,
        OpenLcbLocoAddressTest.class,
        OlcbSensorManagerTest.class,
        OlcbProgrammerTest.class,
        OlcbProgrammerManagerTest.class,
        OlcbSensorTest.class,
        OlcbSystemConnectionMemoTest.class,
        OlcbTurnoutManagerTest.class,
        OlcbTurnoutTest.class,
        OlcbThrottleTest.class,
        OlcbThrottleManagerTest.class,
        BundleTest.class,
        jmri.jmrix.openlcb.swing.PackageTest.class,
        jmri.jmrix.openlcb.configurexml.PackageTest.class
})


/**
 * Tests for the jmri.jmrix.openlcb package.
 *
 * @author Bob Jacobsen Copyright 2009, 2012, 2015
 */
public class PackageTest {
}
