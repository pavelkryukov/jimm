/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ********************************************************************************
 File: src/jimm/comm/ConnectAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.TimeZone;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactListItem;
import jimm.ContactList;
import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.util.ResourceBundle;

public class ConnectAction extends Action
{
    // Action states
    public static final int STATE_ERROR = -1;
    public static final int STATE_INIT_DONE = 1;
    public static final int STATE_CLI_IDENT_SENT = 2;
    public static final int STATE_CLI_DISCONNECT_SENT = 3;
    public static final int STATE_CLI_COOKIE_SENT = 4;
    public static final int STATE_CLI_FAMILIES_SENT = 5;
    public static final int STATE_CLI_RATESREQUEST_SENT = 6;
    public static final int STATE_CLI_REQLISTS_SENT = 7;
    public static final int STATE_CLI_CHECKROSTER_SENT = 8;
    public static final int STATE_CLI_REQOFFLINEMSGS_SENT = 9;
    public static final int STATE_CLI_ACKOFFLINEMSGS_SENT = 10;

    // CLI_FAMILIES packet data
    public static final byte[] CLI_FAMILIES_DATA =
    { (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x04, 
      (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x04, 
      (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01, 
      (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x01, 
      (byte) 0x00, (byte) 0x15, (byte) 0x00, (byte) 0x01, 
      (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x01, 
      (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01, 
      (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x01, 
      (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x01, 
      (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x01};

    // CLI_ACKRATES packet data
    public static final byte[] CLI_ACKRATES_DATA =
    { (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x04, 
      (byte) 0x00, (byte) 0x05};

    // CLI_SETUSERINFO packet data
    public static final byte[] CLI_SETUSERINFO_DATA =
    { (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x30, // 3 capabilities
                                                          // (48 bytes) follow
      (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x49, // CAP_AIM_SERVERRELAY
      (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, 
      (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, 
      (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00, 
      (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x44, // CAP_AIM_ISICQ
      (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, 
      (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, 
      (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00, 
      (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x4E, // CAP_UTF8
      (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, 
      (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, 
      (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};

    // CLI_SETICBM packet data
    public static final byte[] CLI_SETICBM_DATA =
    { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
      (byte) 0x00, (byte) 0x03, (byte) 0x1F, (byte) 0x40, 
      (byte) 0x03, (byte) 0xE7, (byte) 0x03, (byte) 0xE7, 
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    // CLI_SETSTATUS packet data
    public static final byte[] CLI_SETSTATUS_DATA =
    { (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x04, 
	  (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Online status
      (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x02, 
      (byte) 0x00, (byte) 0x00, // No error
      (byte) 0x00, (byte) 0x0C, (byte) 0x00, (byte) 0x25, // TLV(C)
      (byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1, cannot get own IP address
      (byte) 0x00, (byte) 0x00, (byte) 0xAB, (byte) 0xCD, // Port 43981
      (byte) 0x01, // Firewall
      (byte) 0x00, (byte) 0x08, // Support protocol version 8
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x50, 
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // 3                                                                                                                                                                 // follow
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, // Timestamp 1
      (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, // Timestamp 2
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, // Timestamp 3
      (byte) 0x00, (byte) 0x00};

    // CLI_READY packet data
    public static final byte[] CLI_READY_DATA =
    { (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x03, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x02, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01, 
      (byte) 0x01, (byte) 0x01, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x01, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x15, (byte) 0x00, (byte) 0x01,
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x01, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x01, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B, 
      (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x01, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B,
      (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x01, 
      (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7B};

    // Timeout
    // #sijapp cond.if modules_PROXY is "true" #
    public static final int TIME_OUT = 20 * 1000;
    // #sijapp cond.end #
    public int TIMEOUT = 20 * 1000; // milliseconds

    /** *********************************************************************** */

    // UIN
    private String uin;

    // Password
    private String password;

    // Server host
    private String srvHost;

    // Server port
    private String srvPort;

    // Action state
    private int state;

    // Last activity
    private Date lastActivity = new Date();
    private boolean active;

    // Temporary variables
    private byte[] cookie;
    private boolean srvFamilies2Rcvd;
    private boolean srvMotdRcvd;
    private boolean srvReplyLocationRcvd;
    private boolean srvReplyBuddyRcvd;
    private boolean srvReplyIcbmRcvd;
    private boolean srvReplyBosRcvd;
    private boolean srvReplyListsRcvd;
    private boolean srvReplyRosterRcvd;

    // Constructor
    public ConnectAction(String uin, String password, String srvHost, String srvPort)
    {
        this.uin = new String(uin);
        this.password = new String(password);
        this.srvHost = new String(srvHost);
        this.srvPort = new String(srvPort);
    }

    // Returns the UID
    public String getUin()
    {
        return (new String(this.uin));
    }

    // Returns the password
    public String getPassword()
    {
        return (new String(this.password));
    }

    // Returns the server host
    public String getSrvHost()
    {
        return (new String(this.srvHost));
    }

    // Returns the server port
    public String getSrvPort()
    {
        return (new String(this.srvPort));
    }

    // Returns true if the action can be performed
    public boolean isExecutable()
    {
        return (Icq.isNotConnected());
    }

    // Returns true if this is an exclusive command
    public boolean isExclusive()
    {
        return (true);
    }

    // Init action
    protected void init() throws JimmException
    {
        // #sijapp cond.if modules_PROXY is "true" #
        int retry = 1;
        try
        {
            retry = Integer.parseInt(Options.getStringOption(Options.OPTION_AUTORETRY_COUNT));
            retry = (retry > 0) ? retry : 1;
        } catch (NumberFormatException e)
        {
            retry = 1;
        }

        this.TIMEOUT = ConnectAction.TIME_OUT * retry;
        // #sijapp cond.end#
 
        // Check parameters
        if ((this.uin.length() == 0) || (this.password.length() == 0))
        {
            this.state = ConnectAction.STATE_ERROR;
            throw (new JimmException(117, 0));
        }

        // Open connection
        // #sijapp cond.if modules_PROXY is "true" #

        for (int i = 0; i < retry; i++)
        {
            // #sijapp cond.end#

            try
            {
            	Jimm.jimm.getIcqRef().c.connect(this.srvHost + ":" + this.srvPort);
                // #sijapp cond.if modules_PROXY is "true" #
                break;
                // #sijapp cond.end #
            } catch (JimmException e)
            {
                // #sijapp cond.if modules_PROXY is "true" #
                if (i >= (retry - 1) || ((this.lastActivity.getTime() + this.TIMEOUT) < System.currentTimeMillis()))
                {
                    // #sijapp cond.end #

                    this.state = ConnectAction.STATE_ERROR;
                    throw (e);
                }
                // #sijapp cond.if modules_PROXY is "true" #
                else
                    if ((this.lastActivity.getTime() + this.TIMEOUT) > System.currentTimeMillis())
                    {
                    	Jimm.jimm.getIcqRef().c.close();
                        try
                        {
                            // Wait the given time
                            Thread.sleep(2000);
                        } catch (InterruptedException er)
                        {
                            // Do nothing
                        }
                    }
            }
        }
        // #sijapp cond.end #

        // Set STATE_INIT
        this.state = ConnectAction.STATE_INIT_DONE;

        // Update activity timestamp
        this.lastActivity = new Date();

    }

    // Forwards received packet, returns true if packet has been consumed
    protected boolean forward(Packet packet) throws JimmException
    {
        // #sijapp cond.if modules_PROXY is "true" #

        int retry = 1;
        try
        {
            retry = Integer.parseInt(Options.getStringOption(Options.OPTION_AUTORETRY_COUNT));
            retry = (retry > 0) ? retry : 1;
        } catch (NumberFormatException e)
        {
            retry = 1;
        }
        // #sijapp cond.end #

        // Set activity flag
        this.active = true;

        // Catch JimmExceptions
        try
        {

            // Flag indicates whether packet has been consumed or not
            boolean consumed = false;

            // Watch out for STATE_INIT_DONE
            if (this.state == ConnectAction.STATE_INIT_DONE)
            {
                // Watch out for SRV_CLI_HELLO packet
                if (packet instanceof ConnectPacket)
                {
                    ConnectPacket connectPacket = (ConnectPacket) packet;
                    if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
                    {
                        // Send a CLI_IDENT packet as reply
                        ConnectPacket reply = new ConnectPacket(this.uin, this.password, ResourceBundle.getCurrUiLanguage().toLowerCase(), ResourceBundle.getCurrUiLanguage().toLowerCase());
                        Jimm.jimm.getIcqRef().c.sendPacket(reply);

                        // Move to next state
                        this.state = ConnectAction.STATE_CLI_IDENT_SENT;

                        // Packet has been consumed
                        consumed = true;

                    }
                }

            }
            // Watch out for STATE_CLI_IDENT_SENT
            else
                if (this.state == ConnectAction.STATE_CLI_IDENT_SENT)
                {

                    // watch out for channel 4 packet
                    if (packet instanceof DisconnectPacket)
                    {
                        DisconnectPacket disconnectPacket = (DisconnectPacket) packet;

                        // Watch out for SRV_COOKIE packet
                        if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_COOKIE)
                        {

                            // Save cookie
                            this.cookie = disconnectPacket.getCookie();

                            // Send a CLI_GOODBYE packet as reply
                            // DisconnectPacket reply = new DisconnectPacket();
                            // Jimm.jimm.getIcqRef().c.sendPacket(reply);

                            // Close connection
                            Jimm.jimm.getIcqRef().c.close();
                            // #sijapp cond.if target is "DEFAULT" | target is "MIDP2"#
                            if (Options.getBooleanOption(Options.OPTION_SHADOW_CON))
                            {
                                try
                                {
                                    // Wait the given time before starting the
                                    // new connection
                                    Thread.sleep(2000);
                                } catch (InterruptedException e)
                                {
                                    // Do nothing
                                }
                            }
                            // #sijapp cond.end#
                            // Open connection
                            // #sijapp cond.if modules_PROXY is "true" #
                            for (int i = 0; i < retry; i++)
                            {
                                try
                                {
                                    // #sijapp cond.end #
                                	Jimm.jimm.getIcqRef().c.connect(disconnectPacket.getServer());
                                    // #sijapp cond.if modules_PROXY is "true" #
                                    break;

                                } catch (JimmException e)
                                {
                                    if (i >= (retry - 1) || ((this.lastActivity.getTime() + this.TIMEOUT) < System.currentTimeMillis()))
                                    {
                                        this.active = false;
                                        this.state = ConnectAction.STATE_ERROR;
                                        throw (e);
                                    }
                                    else
                                        if ((this.lastActivity.getTime() + this.TIMEOUT) > System.currentTimeMillis())
                                        {
                                        	Jimm.jimm.getIcqRef().c.close();
                                            try
                                            {
                                                // Wait the given time
                                                Thread.sleep(2000);
                                            } catch (InterruptedException er)
                                            {
                                                // Do nothing
                                            }
                                        }
                                }
                            }
                            // #sijapp cond.end #

                            // Move to next state
                            this.state = ConnectAction.STATE_CLI_DISCONNECT_SENT;

                            // Packet has been consumed
                            consumed = true;

                        }
                        // Watch out for SRV_GOODBYE packet
                        else
                            if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_GOODBYE)
                            {

                                // Multiple logins
                                if (disconnectPacket.getError() == 0x0001)
                                {
                                    throw (new JimmException(110, 1));
                                }
                                // Bad password
                                else
                                    if ((disconnectPacket.getError() == 0x0004) || (disconnectPacket.getError() == 0x0005))
                                    {
                                        throw (new JimmException(111, 0));
                                    }
                                    // Non-existant UIN
                                    else
                                        if ((disconnectPacket.getError() == 0x0007) || (disconnectPacket.getError() == 0x0008))
                                        {
                                            throw (new JimmException(112, 0));
                                        }
                                        // Too many clients from same IP
                                        else
                                            if ((disconnectPacket.getError() == 0x0015) || (disconnectPacket.getError() == 0x0016))
                                            {
                                                throw (new JimmException(113, 0));
                                            }
                                            // Rate exceeded
                                            else
                                                if (disconnectPacket.getError() == 0x0018)
                                                {
                                                    throw (new JimmException(114, 0));
                                                }
                                                // Uknown error
                                                else
                                                {
                                                    throw (new JimmException(100, 1));
                                                }

                            }

                    }

                }
                // Watch out for STATE_CLI_DISCONNECT_SENT
                else
                    if (this.state == ConnectAction.STATE_CLI_DISCONNECT_SENT)
                    {

                        // Watch out for SRV_HELLO packet
                        if (packet instanceof ConnectPacket)
                        {
                            ConnectPacket connectPacket = (ConnectPacket) packet;
                            if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
                            {

                                // Send a CLI_COOKIE packet as reply
                                ConnectPacket reply = new ConnectPacket(this.cookie);
                                Jimm.jimm.getIcqRef().c.sendPacket(reply);

                                // Move to next state
                                this.state = ConnectAction.STATE_CLI_COOKIE_SENT;

                                // Packet has been consumed
                                consumed = true;

                            }
                        }

                    }
                    // Watch out for STATE_CLI_COOKIE_SENT
                    else
                        if (this.state == ConnectAction.STATE_CLI_COOKIE_SENT)
                        {

                            // Watch out for SRV_FAMILIES packet type
                            if (packet instanceof SnacPacket)
                            {
                                SnacPacket snacPacket = (SnacPacket) packet;
                                if ((snacPacket.getFamily() == SnacPacket.SRV_FAMILIES_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_FAMILIES_COMMAND))
                                {

                                    // Send a CLI_FAMILIES packet as reply
                                    SnacPacket reply = new SnacPacket(SnacPacket.CLI_FAMILIES_FAMILY, SnacPacket.CLI_FAMILIES_COMMAND, SnacPacket.CLI_FAMILIES_COMMAND, new byte[0], ConnectAction.CLI_FAMILIES_DATA);
                                    Jimm.jimm.getIcqRef().c.sendPacket(reply);

                                    // Move to next state
                                    this.state = ConnectAction.STATE_CLI_FAMILIES_SENT;

                                    // Packet has been consumed
                                    consumed = true;

                                }
                            }

                        }
                        // Watch out for STATE_CLI_FAMILIES_SENT
                        else
                            if (this.state == ConnectAction.STATE_CLI_FAMILIES_SENT)
                            {

                                // Watch out for SNAC packet
                                if (packet instanceof SnacPacket)
                                {
                                    SnacPacket snacPacket = (SnacPacket) packet;

                                    // Watch out for SRV_FAMILIES2 packet
                                    if ((snacPacket.getFamily() == SnacPacket.SRV_FAMILIES2_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_FAMILIES2_COMMAND))
                                    {
                                        this.srvFamilies2Rcvd = true;

                                        // Packet has been consumed
                                        consumed = true;

                                    }

                                    // Watch out for SRV_MOTD packet
                                    if ((snacPacket.getFamily() == SnacPacket.SRV_MOTD_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_MOTD_COMMAND))
                                    {
                                        this.srvMotdRcvd = true;

                                        // Packet has been consumed
                                        consumed = true;

                                    }

                                    // Check if we received both SRV_FAMILIES2
                                    // and SRV_MOTD packet
                                    if (this.srvFamilies2Rcvd && this.srvMotdRcvd)
                                    {

                                        // Send a CLI_RATESREQUEST packet as
                                        // reply
                                        SnacPacket reply = new SnacPacket(SnacPacket.CLI_RATESREQUEST_FAMILY, SnacPacket.CLI_RATESREQUEST_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                        Jimm.jimm.getIcqRef().c.sendPacket(reply);

                                        // Move to next state
                                        this.state = ConnectAction.STATE_CLI_RATESREQUEST_SENT;

                                    }

                                }

                            }
                            // Watch out for STATE_CLI_RATESREQUEST_SENT
                            else
                                if (this.state == ConnectAction.STATE_CLI_RATESREQUEST_SENT)
                                {

                                    // Watch out for SRV_RATES packet
                                    if (packet instanceof SnacPacket)
                                    {
                                        SnacPacket snacPacket = (SnacPacket) packet;
                                        if ((snacPacket.getFamily() == SnacPacket.SRV_RATES_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_RATES_COMMAND))
                                        {

                                            // Send a CLI_ACKRATES packet
                                            SnacPacket reply1 = new SnacPacket(SnacPacket.CLI_ACKRATES_FAMILY, SnacPacket.CLI_ACKRATES_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_ACKRATES_DATA);
                                            Jimm.jimm.getIcqRef().c.sendPacket(reply1);

                                            // Send a CLI_REQLOCATION packet
                                            SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_REQLOCATION_FAMILY, SnacPacket.CLI_REQLOCATION_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                            Jimm.jimm.getIcqRef().c.sendPacket(reply2);

                                            // Send a CLI_SETUSERINFO packet
                                            SnacPacket reply3 = new SnacPacket(SnacPacket.CLI_SETUSERINFO_FAMILY, SnacPacket.CLI_SETUSERINFO_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_SETUSERINFO_DATA);
                                            Jimm.jimm.getIcqRef().c.sendPacket(reply3);

                                            // Send a CLI_REQBUDDY packet
                                            SnacPacket reply4 = new SnacPacket(SnacPacket.CLI_REQBUDDY_FAMILY, SnacPacket.CLI_REQBUDDY_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                            Jimm.jimm.getIcqRef().c.sendPacket(reply4);

                                            // Send a CLI_REQICBM packet
                                            SnacPacket reply5 = new SnacPacket(SnacPacket.CLI_REQICBM_FAMILY, SnacPacket.CLI_REQICBM_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                            Jimm.jimm.getIcqRef().c.sendPacket(reply5);

                                            // Send a CLI_REQBOS packet
                                            SnacPacket reply6 = new SnacPacket(SnacPacket.CLI_REQBOS_FAMILY, SnacPacket.CLI_REQBOS_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                            Jimm.jimm.getIcqRef().c.sendPacket(reply6);

                                            // Send a CLI_REQLISTS packet
                                            SnacPacket reply7 = new SnacPacket(SnacPacket.CLI_REQLISTS_FAMILY, SnacPacket.CLI_REQLISTS_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                            Jimm.jimm.getIcqRef().c.sendPacket(reply7);

                                            // Move to next state
                                            this.state = ConnectAction.STATE_CLI_REQLISTS_SENT;

                                            // Packet has been consumed
                                            consumed = true;

                                        }
                                    }

                                }
                                // Watch out for STATE_CLI_REQLISTS_SENT
                                else
                                    if (this.state == ConnectAction.STATE_CLI_REQLISTS_SENT)
                                    {

                                        // Watch out for SNAC packet
                                        if (packet instanceof SnacPacket)
                                        {
                                            SnacPacket snacPacket = (SnacPacket) packet;

                                            // Watch out for SRV_REPLYLOCATION
                                            // packet
                                            if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYLOCATION_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYLOCATION_COMMAND))
                                            {
                                                this.srvReplyLocationRcvd = true;

                                                // Packet has been consumed
                                                consumed = true;

                                            }

                                            // Watch out for SRV_REPLYBUDDY
                                            // packet
                                            if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYBUDDY_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYBUDDY_COMMAND))
                                            {
                                                this.srvReplyBuddyRcvd = true;

                                                // Packet has been consumed
                                                consumed = true;

                                            }

                                            // Watch out for SRV_REPLYICBM
                                            // packet
                                            if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYICBM_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYICBM_COMMAND))
                                            {
                                                this.srvReplyIcbmRcvd = true;

                                                // Packet has been consumed
                                                consumed = true;

                                            }

                                            // Watch out for SRV_REPLYBOS packet
                                            if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYBOS_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYBOS_COMMAND))
                                            {
                                                this.srvReplyBosRcvd = true;

                                                // Packet has been consumed
                                                consumed = true;

                                            }

                                            // Watch out for SRV_REPLYLISTS
                                            // packet
                                            if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYLISTS_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYLISTS_COMMAND))
                                            {
                                                this.srvReplyListsRcvd = true;

                                                // Packet has been consumed
                                                consumed = true;

                                            }

                                            // Check if all packets have been
                                            // received
                                            if (this.srvReplyLocationRcvd && this.srvReplyBuddyRcvd && this.srvReplyIcbmRcvd && this.srvReplyBosRcvd && this.srvReplyListsRcvd)
                                            {

                                                // Send a CLI_SETICBM packet
                                                SnacPacket reply1 = new SnacPacket(SnacPacket.CLI_SETICBM_FAMILY, SnacPacket.CLI_SETICBM_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_SETICBM_DATA);
                                                Jimm.jimm.getIcqRef().c.sendPacket(reply1);

                                                // Send a CLI_REQROSTER or
                                                // CLI_CHECKROSTER packet
                                                long versionId1 = ContactList.getVersionId1();
                                                int versionId2 = ContactList.getVersionId2();
                                                if (((versionId1 == -1) && (versionId2 == -1)) || (ContactList.getSize() == 0))
                                                {
                                                    SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_REQROSTER_FAMILY, SnacPacket.CLI_REQROSTER_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                                    Jimm.jimm.getIcqRef().c.sendPacket(reply2);
                                                }
                                                else
                                                {
                                                    byte[] data = new byte[6];
                                                    Util.putDWord(data, 0, versionId1);
                                                    Util.putWord(data, 4, versionId2);
                                                    SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_CHECKROSTER_FAMILY, SnacPacket.CLI_CHECKROSTER_COMMAND, 0x00000000, new byte[0], data);
                                                    Jimm.jimm.getIcqRef().c.sendPacket(reply2);
                                                }

                                                // Move to next state
                                                this.state = ConnectAction.STATE_CLI_CHECKROSTER_SENT;

                                            }

                                        }

                                    }
                                    // Watch out for STATE_CLI_CHECKROSTER_SENT
                                    else
                                        if (this.state == ConnectAction.STATE_CLI_CHECKROSTER_SENT)
                                        {

                                            // Watch out for SNAC packet
                                            if (packet instanceof SnacPacket)
                                            {
                                                SnacPacket snacPacket = (SnacPacket) packet;

                                                // Watch out for
                                                // SRV_REPLYROSTEROK
                                                if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTEROK_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTEROK_COMMAND))
                                                {
                                                    this.srvReplyRosterRcvd = true;

                                                    // Packet has been consumed
                                                    consumed = true;

                                                }
                                                // watch out for SRV_REPLYROSTER
                                                // packet
                                                else
                                                    if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTER_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTER_COMMAND))
                                                    {

                                                        if (snacPacket.getFlags() != 1) this.srvReplyRosterRcvd = true;

                                                        // System.out.println("Flag:
                                                        // "+snacPacket.getFlags());

                                                        // Initialize vector for
                                                        // items
                                                        Vector items = new Vector();

                                                        // Get data
                                                        byte[] buf = snacPacket.getData();
                                                        int marker = 0;

                                                        // Check length
                                                        if (buf.length < 3) { throw (new JimmException(115, 0)); }

                                                        // Skip
                                                        // SRV_REPLYROSTER.UNKNOWN
                                                        marker += 1;

                                                        // Iterate through all
                                                        // items
                                                        int count = Util.getWord(buf, marker);
                                                        marker += 2;
                                                        // System.out.println("elemente in serverlist: "+count);
                                                        for (int i = 0; i < count; i++)
                                                        {

                                                            // Check length
                                                            if (buf.length < marker + 2) { throw (new JimmException(115, 1)); }

                                                            // Get name length
                                                            int nameLen = Util.getWord(buf, marker);
                                                            marker += 2;

                                                            // Check length
                                                            if (buf.length < marker + nameLen + 2 + 2 + 2 + 2) { throw (new JimmException(115, 2)); }

                                                            // Get name
                                                            String name = Util.byteArrayToString(buf, marker, nameLen, true);
                                                            marker += nameLen;

                                                            // Get group, id and type
                                                            int group = Util.getWord(buf, marker);
                                                            int id = Util.getWord(buf, marker + 2);
                                                            int type = Util.getWord(buf, marker + 4);
                                                            marker += 6;

                                                            // Get length of the following TLVs
                                                            int len = Util.getWord(buf, marker);
                                                            marker += 2;

                                                            // Check length
                                                            if (buf.length < marker + len) { throw (new JimmException(115, 3)); }

                                                            // Normal contact
                                                            if (type == 0x0000)
                                                            {

                                                                // Get nick
                                                                String nick = new String(name);
                                                                //System.out.println("c "+i+": "+name);
                                                                boolean noAuth = false;
                                                                while (len > 0)
                                                                {
                                                                    byte[] tlvData = Util.getTlv(buf, marker);
                                                                    if (tlvData == null) { throw (new JimmException(115, 4)); }
                                                                    int tlvType = Util.getWord(buf, marker);
                                                                    if (tlvType == 0x0131)
                                                                    {
                                                                        nick = Util.byteArrayToString(tlvData, true);
                                                                    }
                                                                    else
                                                                        if (tlvType == 0x0066)
                                                                        {
                                                                            noAuth = true;
                                                                        }
                                                                    len -= 4;
                                                                    len -= tlvData.length;
                                                                    marker += 4 + tlvData.length;
                                                                }
                                                                if (len != 0) { throw (new JimmException(115, 5)); }

                                                                // Add this contact item to the vector
                                                                items.addElement(new ContactListContactItem(id, group, name, nick, noAuth, true));

                                                            }
                                                            // Group of contacts
                                                            else
                                                                if (type == 0x0001)
                                                                {
                                                                    //System.out.println("g "+i+": "+name);
                                                                    // Skip TLVs
                                                                    marker += len;

                                                                    // Add this group item to the vector
                                                                    if (group != 0x0000)
                                                                    {
                                                                        items.addElement(new ContactListGroupItem(group, name));
                                                                    }

                                                                }
															// My visibility settings
															else
															if (type == 0x0004)
															{
																while (len > 0)
																{
																	byte[] tlvData = Util.getTlv(buf, marker);
																	if (tlvData == null) { throw (new JimmException(115, 110)); }
																	int tlvType = Util.getWord(buf, marker);

																	if (tlvType == 0x00CA)
																	{
																		Options.setIntOption(Options.OPTION_VISIBILITY_ID, (int)id);
																	}

																	len -= 4;
																	len -= tlvData.length;
																	marker += 4 + tlvData.length;
																}
																if (len != 0) { throw (new JimmException(115, 111)); }
															}
                                                                // All other item types
                                                                else
                                                                {
                                                                    //System.out.println("x "+i+": ");
                                                                    // Skip TLVs
                                                                    marker += len;

                                                                }

                                                        }

                                                        // Check length
                                                        if (buf.length != marker + 4) { throw (new JimmException(115, 6)); }

                                                        // Get timestamp
                                                        long timestamp = Util.getDWord(buf, marker);

                                                        // Update contact list
                                                        ContactListItem[] itemsAsArray = new ContactListItem[items.size()];
                                                        items.copyInto(itemsAsArray);
                                                        ContactList.update(snacPacket.getFlags(), timestamp, count, itemsAsArray);

                                                        // Packet has been consumed
                                                        consumed = true;

                                                    }

                                                // Check if all required packets have been received
                                                if (this.srvReplyRosterRcvd)
                                                {

                                                    // Send a CLI_ROSTERACK packet
                                                    SnacPacket reply1 = new SnacPacket(SnacPacket.CLI_ROSTERACK_FAMILY, SnacPacket.CLI_ROSTERACK_COMMAND, 0x00000000, new byte[0], new byte[0]);
                                                    Jimm.jimm.getIcqRef().c.sendPacket(reply1);

													long onlineStatusOpt = Options.getLongOption(Options.OPTION_ONLINE_STATUS);
													long onlineStatus = Util.translateStatusSend(onlineStatusOpt);
													int visibilityItemId = Options.getIntOption(Options.OPTION_VISIBILITY_ID);
													byte[] buf = new byte[15];
													byte bCode = 0;
													if(visibilityItemId != 0)
													{
														// Build packet for privacy setting changing
														int marker = 0;

														if(onlineStatus == Util.SET_STATUS_INVISIBLE)
															bCode = (onlineStatusOpt == ContactList.STATUS_INVIS_ALL)?(byte)2:(byte)3;
														else
															bCode = (byte)4;

														Util.putWord(buf, marker,    0); marker += 2; // name (null)
														Util.putWord(buf, marker,    0); marker += 2; // GroupID
														Util.putWord(buf, marker,  visibilityItemId); marker += 2; // EntryID
														Util.putWord(buf, marker,    4); marker += 2; // EntryType
														Util.putWord(buf, marker,    5); marker += 2; // Length in bytes of following TLV
														Util.putWord(buf, marker, 0xCA); marker += 2; // TLV Type
														Util.putWord(buf, marker,    1); marker += 2; // TLV Length
														Util.putByte(buf, marker,bCode);              // TLV Value

														// Change privacy setting according to new status
														if(onlineStatus == Util.SET_STATUS_INVISIBLE)
														{
															SnacPacket reply2pre = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
																					   SnacPacket.CLI_ROSTERUPDATE_COMMAND,
																					   SnacPacket.CLI_ROSTERUPDATE_COMMAND,
																					   new byte[0],
																					   buf);
															Jimm.jimm.getIcqRef().c.sendPacket(reply2pre);
														}
													}

													// Send a CLI_SETSTATUS packet
													Util.putDWord(ConnectAction.CLI_SETSTATUS_DATA, 4, 0x10000000+onlineStatus);
													SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_SETSTATUS_FAMILY, SnacPacket.CLI_SETSTATUS_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_SETSTATUS_DATA);
													Jimm.jimm.getIcqRef().c.sendPacket(reply2);

													// Change privacy setting according to new status
													if(visibilityItemId != 0 && onlineStatus != Util.SET_STATUS_INVISIBLE)
													{
														SnacPacket reply2post = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
																					SnacPacket.CLI_ROSTERUPDATE_COMMAND,
																					SnacPacket.CLI_ROSTERUPDATE_COMMAND,
																					new byte[0],
																					buf);
														Jimm.jimm.getIcqRef().c.sendPacket(reply2post);
													}

                                                    // Send a CLI_READY packet
                                                    SnacPacket reply3 = new SnacPacket(SnacPacket.CLI_READY_FAMILY, SnacPacket.CLI_READY_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_READY_DATA);
                                                    Jimm.jimm.getIcqRef().c.sendPacket(reply3);

                                                    // Send a CLI_TOICQSRV/CLI_REQOFFLINEMSGS packet
                                                    ToIcqSrvPacket reply4 = new ToIcqSrvPacket(0x00000000, this.uin, ToIcqSrvPacket.CLI_REQOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
                                                    Jimm.jimm.getIcqRef().c.sendPacket(reply4);

                                                    // Move to next state
                                                    this.state = ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT;

                                                }

                                            }

                                        }
                                        // Watch out for STATE_CLI_REQOFFLINEMSGS_SENT
                                        else
                                            if (this.state == ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT)
                                            {

                                                if (packet instanceof FromIcqSrvPacket)
                                                {
                                                    FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

                                                    // Watch out for SRV_OFFLINEMSG
                                                    if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_OFFLINEMSG_SUBCMD)
                                                    {

                                                        // Get raw data
                                                        byte[] buf = fromIcqSrvPacket.getData();

                                                        // Check length
                                                        if (buf.length < 14) { throw (new JimmException(116, 0)); }

                                                        // Extract UIN
                                                        long uinRaw = Util.getDWord(buf, 0, false);
                                                        // If message is from uin 1003 it's from ICQ webmessage and we ignore it
                                                        if (uinRaw != 1003)
                                                        {
                                                            String uin = String.valueOf(uinRaw);

                                                            // Extract date of dispatch
                                                            int dateYear = Util.getWord(buf, 4, false);
                                                            int dateMonth = Util.getByte(buf, 6);
                                                            int dateDay = Util.getByte(buf, 7);
                                                            int dateHour = Util.getByte(buf, 8);
                                                            int dateMinute = Util.getByte(buf, 9);
                                                            Calendar c = Calendar.getInstance();
                                                            c.set(Calendar.YEAR, dateYear);
                                                            switch (dateMonth)
                                                            {
                                                            case 1:
                                                                c.set(Calendar.MONTH, Calendar.JANUARY);
                                                                break;
                                                            case 2:
                                                                c.set(Calendar.MONTH, Calendar.FEBRUARY);
                                                                break;
                                                            case 3:
                                                                c.set(Calendar.MONTH, Calendar.MARCH);
                                                                break;
                                                            case 4:
                                                                c.set(Calendar.MONTH, Calendar.APRIL);
                                                                break;
                                                            case 5:
                                                                c.set(Calendar.MONTH, Calendar.MAY);
                                                                break;
                                                            case 6:
                                                                c.set(Calendar.MONTH, Calendar.JUNE);
                                                                break;
                                                            case 7:
                                                                c.set(Calendar.MONTH, Calendar.JULY);
                                                                break;
                                                            case 8:
                                                                c.set(Calendar.MONTH, Calendar.AUGUST);
                                                                break;
                                                            case 9:
                                                                c.set(Calendar.MONTH, Calendar.SEPTEMBER);
                                                                break;
                                                            case 10:
                                                                c.set(Calendar.MONTH, Calendar.OCTOBER);
                                                                break;
                                                            case 11:
                                                                c.set(Calendar.MONTH, Calendar.NOVEMBER);
                                                                break;
                                                            case 12:
                                                                c.set(Calendar.MONTH, Calendar.DECEMBER);
                                                            }
                                                            c.set(Calendar.DAY_OF_MONTH, dateDay);
                                                            c.set(Calendar.HOUR_OF_DAY, dateHour);
                                                            c.set(Calendar.MINUTE, dateMinute);
                                                            c.set(Calendar.SECOND, 0);
                                                            Date date = c.getTime();
                                                            date.setTime(date.getTime() - (TimeZone.getDefault().useDaylightTime() ? (60 * 60 * 1000) : 0));

                                                            // Get type
                                                            int type = Util.getWord(buf, 10, false);

                                                            // Get text length
                                                            int textLen = Util.getWord(buf, 12, false);

                                                            // Check length
                                                            if (buf.length != 14 + textLen) { throw (new JimmException(116, 1)); }

                                                            // Get text
                                                            String text = Util.crlfToCr(Util.byteArrayToString(buf, 14, textLen, Util.isDataUTF8(buf, 14, textLen)));

                                                            // Normal message
                                                            if (type == 0x0001)
                                                            {
                                                                 // Forward message to contact list
                                                                PlainMessage message = new PlainMessage(uin, this.uin, date, text, true);
                                                                ContactList.addMessage(message);

                                                            }
                                                            // URL message
                                                            else
                                                                if (type == 0x0004)
                                                                {

                                                                    // Search for delimiter
                                                                    int delim = text.indexOf(0xFE);

                                                                    // Split message, if delimiter could be found
                                                                    String urlText;
                                                                    String url;
                                                                    if (delim != -1)
                                                                    {
                                                                        urlText = text.substring(0, delim);
                                                                        url = text.substring(delim + 1);
                                                                    }
                                                                    else
                                                                    {
                                                                        urlText = text;
                                                                        url = "";
                                                                    }

                                                                    // Forward message message to contact list
                                                                    UrlMessage message = new UrlMessage(uin, this.uin, date, url, urlText);
                                                                    ContactList.addMessage(message);

                                                                }
                                                        }

                                                        // Packet has been consumed
                                                        consumed = true;

                                                    }
                                                    // Watch out for SRV_DONEOFFLINEMSGS
                                                    else
                                                        if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_DONEOFFLINEMSGS_SUBCMD)
                                                        {

                                                            // Send a CLI_TOICQSRV/CLI_ACKOFFLINEMSGS packet
                                                            ToIcqSrvPacket reply = new ToIcqSrvPacket(0x00000000, this.uin, ToIcqSrvPacket.CLI_ACKOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
                                                            Jimm.jimm.getIcqRef().c.sendPacket(reply);

                                                            // Move to next state
                                                            this.state = ConnectAction.STATE_CLI_ACKOFFLINEMSGS_SENT;

                                                            // Move to STATE_CONNECTED
                                                            Icq.setConnected();

                                                            // Packet has been consumed
                                                            consumed = true;

                                                        }

                                                }

                                            }

            // Update activity timestamp and reset activity flag
            this.lastActivity = new Date();
            this.active = false;

            // Return consumption flag
            return (consumed);

        }
        // Catch JimmExceptions
        catch (JimmException e)
        {

            // Update activity timestamp and reset activity flag
            this.lastActivity = new Date();
            this.active = false;

            // Set error state if exception is critical
            if (e.isCritical()) this.state = ConnectAction.STATE_ERROR;

            // Forward exception
            throw (e);

        }

    }

    // Returns true if the action is completed
    public boolean isCompleted()
    {
        return (this.state == ConnectAction.STATE_CLI_ACKOFFLINEMSGS_SENT);
    }

    // Returns true if an error has occured
    public boolean isError()
    {
    	if ((this.state != ConnectAction.STATE_ERROR) && !this.active && (this.lastActivity.getTime() + this.TIMEOUT < System.currentTimeMillis()))
        {
            JimmException.handleException(new JimmException(118, 0));
            this.state = ConnectAction.STATE_ERROR;
        }
        return (this.state == ConnectAction.STATE_ERROR);
    }

    // Returns a number between 0 and 100 (inclusive) which indicates the current progress
    public int getProgress()
    {
        switch (this.state)
        {
        case ConnectAction.STATE_INIT_DONE:
            return (10);
        case ConnectAction.STATE_CLI_IDENT_SENT:
            return (20);
        case ConnectAction.STATE_CLI_DISCONNECT_SENT:
            return (30);
        case ConnectAction.STATE_CLI_COOKIE_SENT:
            return (40);
        case ConnectAction.STATE_CLI_FAMILIES_SENT:
            return (50);
        case ConnectAction.STATE_CLI_RATESREQUEST_SENT:
            return (60);
        case ConnectAction.STATE_CLI_REQLISTS_SENT:
            return (70);
        case ConnectAction.STATE_CLI_CHECKROSTER_SENT:
            return (80);
        case ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT:
            return (90);
        case ConnectAction.STATE_CLI_ACKOFFLINEMSGS_SENT:
            return (100);
        default:
            return (0);
        }
    }

}