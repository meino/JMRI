package jmri.jmrix.srcp.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.srcp.swing.srcpmon.PackageTest.class,
    jmri.jmrix.srcp.swing.packetgen.PackageTest.class
})

/**
 * Tests for the jmri.jmrix.srcp.swing package.
 *
 * @author Paul Bender Copyright 2016
 */
public class PackageTest {

}
