package jmri;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Locate a Turnout object representing some specific turnout on the layout.
 * <P>
 * Turnout objects are obtained from a TurnoutManager, which in turn is
 * generally located from the InstanceManager. A typical call sequence might be:
 * <PRE>
 * Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout("23");
 * </PRE>
 * <P>
 * Each turnout has a two names. The "user" name is entirely free form, and can
 * be used for any purpose. The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
 * Much of the book-keeping is implemented in the AbstractTurnoutManager class,
 * which can form the basis for a system-specific implementation.
 * <P>
 * A sample use of the TurnoutManager interface can be seen in the
 * jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame class, which provides a
 * simple GUI for controlling a single turnout.
 *
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001
 * @see jmri.Turnout
 * @see jmri.InstanceManager
 * @see jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 */
public interface TurnoutManager extends Manager {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new turnout. If the name is a valid system name, it will be used for the
     * new turnout. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if Turnout doesn't already exist and the
     *                                  manager cannot create the Turnout due to
     *                                  e.g. an illegal name or name that can't
     *                                  be parsed.
     */
    public @Nonnull Turnout provideTurnout(@Nonnull String name) throws IllegalArgumentException;

    /**
     * Locate via user name, then system name if needed. If that fails, return
     * null
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public @Nullable Turnout getTurnout(@Nonnull String name);

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @return requested Turnout object or null if none exists
     */
    public @Nullable Turnout getBySystemName(@Nonnull String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @return requested Turnout object or null if none exists
     */
    public @Nullable Turnout getByUserName(@Nonnull String userName);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Turnout object representing a given physical turnout and
     * therefore only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the Turnout object created; a valid system name must be
     * provided
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired turnout, and the user address is associated with
     * it. The system name must be valid.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Turnouts when you should be looking them up.
     *
     * @return requested Turnout object (never null)
     * @throws IllegalArgumentException if cannot create the Turnout due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    public @Nonnull Turnout newTurnout(@Nonnull String systemName, @Nullable String userName)  throws IllegalArgumentException;

    /**
     * Get a list of all Turnouts' system names.
     */
    public @Nonnull List<String> getSystemNameList();

    /**
     * Get text to be used for the Turnout.CLOSED state in user communication.
     * Allows text other than "CLOSED" to be use with certain hardware system to
     * represent the Turnout.CLOSED state.
     */
    public @Nonnull String getClosedText();

    /**
     * Get text to be used for the Turnout.THROWN state in user communication.
     * Allows text other than "THROWN" to be use with certain hardware system to
     * represent the Turnout.THROWN state.
     */
    public @Nonnull String getThrownText();

    /**
     * Get a list of the valid TurnoutOPeration subtypes for use with turnouts
     * of this system
     */
    public @Nonnull String[] getValidOperationTypes();

    /**
     * Get from the user, the number of addressed bits used to control a
     * turnout. Normally this is 1, and the default routine returns one
     * automatically. Turnout Managers for systems that can handle multiple
     * control bits should override this method with one which asks the user to
     * specify the number of control bits. If the user specifies more than one
     * control bit, this method should check if the additional bits are
     * available (not assigned to another object). If the bits are not
     * available, this method should return 0 for number of control bits, after
     * informing the user of the problem.
     */
    public int askNumControlBits(@Nonnull String systemName);

    /**
     * Determines if the manager supports multiple control bits, as the
     * askNumControlBits will always return a value even if it is not supported
     */
    public boolean isNumControlBitsSupported(@Nonnull String systemName);

    /**
     * Get from the user, the type of output to be used bits to control a
     * turnout. Normally this is 0 for 'steady state' control, and the default
     * routine returns 0 automatically. Turnout Managers for systems that can
     * handle pulsed control as well as steady state control should override
     * this method with one which asks the user to specify the type of control
     * to be used. The routine should return 0 for 'steady state' control, or n
     * for 'pulsed' control, where n specifies the duration of the pulse
     * (normally in seconds).
     */
    public int askControlType(@Nonnull String systemName);

    /**
     * Determines if the manager supports the handling of pulsed and steady
     * state control as the askControlType will always return a value even if it
     * is not supported
     */
    public boolean isControlTypeSupported(@Nonnull String systemName);

    /**
     * A method that determines if it is possible to add a range of turnouts in
     * numerical order eg 10 to 30 will return true. where as if the address
     * format is 1b23 this will return false.
     *
     */
    public boolean allowMultipleAdditions(@Nonnull String systemName);

    /**
     * Determine if the address supplied is valid and free, if not then it shall
     * return the next free valid address up to a maximum of 10 address away
     * from the initial address.
     *
     * @param prefix     - The System Prefix used to make up the systemName
     * @param curAddress - The hardware address of the turnout we which to
     *                   check.
     */
    public @Nullable String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;

    /**
     * Returns a system name for a given hardware address and system prefix.
     */
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;

    public void setDefaultClosedSpeed(String speed) throws JmriException;

    public void setDefaultThrownSpeed(String speed) throws JmriException;

    public String getDefaultThrownSpeed();

    public String getDefaultClosedSpeed();
}

