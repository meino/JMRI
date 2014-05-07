package jmri.jmrix.ieee802154.serialdriver;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.ieee802154.IEEE802154Node;

/**
 * SerialTrafficControllerTest.java
 *
 * Description:	    tests for the jmri.jmrix.ieee802154.serialdriver.SerialTrafficController class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class SerialTrafficControllerTest extends TestCase {

    public void testCtor() {
        SerialTrafficController m = new SerialTrafficController();
        Assert.assertNotNull("exists",m);
    }
        
    public void testCreateNode() {
           // test the code to get a new IEEE802154 node
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
	   Assert.assertNotNull("node create failed",m);
        }

        public void testGetNodeFromAddressTest() {
           // test the code to get an IEEE802154 node from its address
           // specified as a string to make sure it returns null on failure.
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
           node.setNodeAddress(28055);
           byte uad[]={(byte)0x6D,(byte)0x97};
           node.setUserAddress(uad);
           node.setPANAddress(uad);
           byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
           node.setGlobalAddress(gad);
           m.registerNode(node);
           IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress("00 01");
	   Assert.assertNull("node found",n);
        }

        public void testGetNodeFromUserAddressIntTest() {
           // test the code to get an IEEE802154 node from its User address
           // specified as an integer array.
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
           m.registerNode(node);
           node.setNodeAddress(28055);
           byte uad[]={(byte)0x6D,(byte)0x97};
           int iad[]={0x6D,0x97};
           node.setUserAddress(uad);
           byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
           node.setGlobalAddress(gad);
           IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(iad);
           Assert.assertNotNull("node not found",n); 
        }

        public void testGetNodeFromUserAddressByteTest() {
           // test the code to get an IEEE802154 node from its User address
           // specified as a byte array.
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
           m.registerNode(node);
           node.setNodeAddress(28055);
           byte uad[]={(byte)0x6D,(byte)0x97};
           node.setUserAddress(uad);
           byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
           node.setGlobalAddress(gad);
           IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(uad);
           Assert.assertNotNull("node not found",n); 
        }

        public void testGetNodeFromUserAddressTest() {
           // test the code to get an IEEE802154 node from its User address
           // specified as a string.
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
           m.registerNode(node);
           node.setNodeAddress(28055);
           byte uad[]={(byte)0x6D,(byte)0x97};
           node.setUserAddress(uad);
           node.setPANAddress(uad);
           byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
           node.setGlobalAddress(gad);
           m.registerNode(node);
           IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress("6D 97");
           Assert.assertNotNull("node not found",n); 
        }

        public void testGetNodeFromAddressGlobalByteTest() {
           // test the code to get an IEEE802154 node from its Global address
           // specified as a byte array.
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
           m.registerNode(node);
           node.setNodeAddress(28055);
           byte uad[]={(byte)0x6D,(byte)0x97};
           node.setUserAddress(uad);
           node.setPANAddress(uad);
           byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
           node.setGlobalAddress(gad);
           m.registerNode(node);
	   IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(gad);
	   Assert.assertNotNull("node not found",n); 
        }

        public void testGetNodeFromAddressGlobalIntTest() {
           // test the code to get an IEEE802154 node from its Global address
           // specified as an integer array.
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
           node.setNodeAddress(28055);
           byte uad[]={(byte)0x6D,(byte)0x97};
           node.setUserAddress(uad);
           node.setPANAddress(uad);
           byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
           int iad[]={0x00,0x13,0xA2,0x00,0x40,0xA0,0x4D,0x2D};
           node.setGlobalAddress(gad);
           m.registerNode(node);
	   IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(iad);
	   Assert.assertNotNull("node not found",n); 
        }

        public void testGetNodeFromAddressGlobalTest() {
           // test the code to get an IEEE802154 node from its Global address
           // specified as a string.
           SerialTrafficController m = new SerialTrafficController();
           IEEE802154Node node = m.newNode();
           node.setNodeAddress(28055);
           byte uad[]={(byte)0x6D,(byte)0x97};
           node.setUserAddress(uad);
           node.setPANAddress(uad);
           byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
           node.setGlobalAddress(gad);
           m.registerNode(node);
	   IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress("00 13 A2 00 40 A0 4D 2D");
	   Assert.assertNotNull("node not found",n); 
        }


	// from here down is testing infrastructure

	public SerialTrafficControllerTest(String s) {
		super(s);
	}


	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SerialTrafficControllerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialTrafficControllerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(SerialTrafficControllerTest.class.getName());

}