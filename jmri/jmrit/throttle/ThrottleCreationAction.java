package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.ThrottleManager;
import jmri.InstanceManager;

/**
 * Create a new throttle.
 *
 * @author			Glen Oberhauser
 * @version
 */
public class ThrottleCreationAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public ThrottleCreationAction(String s) {
        super(s);
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
		jmri.InstanceManager.throttleFrameManagerInstance().createThrottleFrame();		
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ThrottleCreationAction.class.getName());

}