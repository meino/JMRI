package jmri.jmrix.rfid.merg.concentrator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * ConcentratorTrafficControllerTest.java
 *
 * Description:	tests for the ConcentratorTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorTrafficControllerTest {

    ConcentratorTrafficController tc = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(tc);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new ConcentratorTrafficController(new ConcentratorSystemConnectionMemo(),"A-H"){
           @Override
           public void sendInitString(){
           }
        };
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
