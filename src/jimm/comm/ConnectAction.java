/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

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

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactListItem;
import jimm.Jimm;
import jimm.JimmException;
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
  public static final int STATE_CLI_REQBOS_SENT = 7;
  public static final int STATE_CLI_REQOFFLINEMSGS_SENT = 8;
  public static final int STATE_CLI_ACKOFFLINEMSGS_SENT = 9;


  // CLI_FAMILIES packet data
  public static final byte[] CLI_FAMILIES_DATA = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x03,
                                                  (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x02,
                                                  (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01,
                                                  (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x01,
                                                  (byte) 0x00, (byte) 0x15, (byte) 0x00, (byte) 0x01,
                                                  (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x01,
                                                  (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01,
                                                  (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x01,
                                                  (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x01,
                                                  (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x01};


  // CLI_ACKRATES packet data
  public static final byte[] CLI_ACKRATES_DATA = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x02,
                                                  (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x04,
                                                  (byte) 0x00, (byte) 0x05};


  // CLI_SETUSERINFO packet data
  public static final byte[] CLI_SETUSERINFO_DATA = {(byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x30, // 3 capabilities (48 bytes) follow
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
  public static final byte[] CLI_SETICBM_DATA = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                 (byte) 0x00, (byte) 0x03, (byte) 0x1F, (byte) 0x40,
                                                 (byte) 0x03, (byte) 0xE7, (byte) 0x03, (byte) 0xE7,
                                                 (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};


  // CLI_SETSTATUS packet data
  public static final byte[] CLI_SETSTATUS_DATA = {(byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Online status
                                                   (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, // No error
                                                   (byte) 0x00, (byte) 0x0C, (byte) 0x00, (byte) 0x25, // TLV(C)
                                                   (byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1, cannot get own IP address
                                                   (byte) 0x00, (byte) 0x00, (byte) 0xAB, (byte) 0xCD, // Port 43981
                                                   (byte) 0x01, // Firewall
                                                   (byte) 0x00, (byte) 0x08, // Support protocol version 8
                                                   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                   (byte) 0x00, (byte) 0x00,
                                                   (byte) 0x00, (byte) 0x50,
                                                   (byte) 0x00, (byte) 0x00,
                                                   (byte) 0x00, (byte) 0x03, // 3 timestamps follow
                                                   (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, // Timestamp 1
                                                   (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, // Timestamp 2
                                                   (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, // Timestamp 3
                                                   (byte) 0x00, (byte) 0x00};


  // CLI_READY packet data
  public static final byte[] CLI_READY_DATA = {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x03,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x02,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x01, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x15, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C,
                                               (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x01,
                                               (byte) 0x01, (byte) 0x10, (byte) 0x04, (byte) 0x7C};


  // Timeout
  public static final int TIMEOUT = 20 * 1000; // milliseconds


  /****************************************************************************/


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
  private boolean srvReplyInfoRcvd;
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
    return (this.icq.isNotConnected());
  }


  // Returns true if this is an exclusive command
  public boolean isExclusive()
  {
    return (true);
  }


  // Init action
  protected void init() throws JimmException
  {

    // Check parameters
    if ((this.uin.length() == 0) || (this.password.length() == 0))
    {
      this.state = ConnectAction.STATE_ERROR;
      throw (new JimmException(117, 0));
    }

    // Open connection
    try
    {
      this.icq.c.connect(this.srvHost, this.srvPort);
    }
    catch (JimmException e)
    {
      this.state = ConnectAction.STATE_ERROR;
      throw (e);
    }

    // Set STATE_INIT
    this.state = ConnectAction.STATE_INIT_DONE;

    // Update activity timestamp
    this.lastActivity = new Date();
  }


  // Forwards received packet, returns true if packet has been consumed
  protected boolean forward(Packet packet) throws JimmException
  {

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
      	// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:01 ");
        // Watch out for SRV_CLI_HELLO packet
        if (packet instanceof ConnectPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:02 ");
          ConnectPacket connectPacket = (ConnectPacket) packet;
          if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:03 ");
            // Send a CLI_IDENT packet as reply
            ConnectPacket reply = new ConnectPacket(this.uin,
                                this.password,
                                ResourceBundle.getCurrUiLanguage().toLowerCase(),
                                ResourceBundle.getCurrUiLanguage().toLowerCase());
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:03.1 ");
            this.icq.c.sendPacket(reply);
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:03.2 ");

            // Move to next state
            this.state = ConnectAction.STATE_CLI_IDENT_SENT;
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:03.3 ");

            // Packet has been consumed
            consumed = true;

          }
        }

      }
      // Watch out for STATE_CLI_IDENT_SENT
      else if (this.state == ConnectAction.STATE_CLI_IDENT_SENT)
      {
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:04 ");
        // watch out for channel 4 packet
        if (packet instanceof DisconnectPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:05 ");
          DisconnectPacket disconnectPacket = (DisconnectPacket) packet;

          // Watch out for SRV_COOKIE packet
          if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_COOKIE)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:06 ");
            // Save cookie
            this.cookie = disconnectPacket.getCookie();

            // Send a CLI_GOODBYE packet as reply
            DisconnectPacket reply = new DisconnectPacket();
            this.icq.c.sendPacket(reply);

            // Close connection
            //// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:forward");
            this.icq.c.close();

            // Open connection
            this.icq.c.connect(disconnectPacket.getServer());

            // Move to next state
            this.state = ConnectAction.STATE_CLI_DISCONNECT_SENT;

            // Packet has been consumed
            consumed = true;

          }
          // Watch out for SRV_GOODBYE packet
          if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_GOODBYE)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:07 ");;
            // Throw exception
            if (disconnectPacket.getError() == 0x0001)
            {   // Multiple logins
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:08 ");
              throw (new JimmException(110, 1));
            }
            else if ((disconnectPacket.getError() == 0x0004) || (disconnectPacket.getError() == 0x0005))
            {   // Bad password
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:09 ");
              throw (new JimmException(111, 0));
            }
            else if ((disconnectPacket.getError() == 0x0007) || (disconnectPacket.getError() == 0x0008))
            {   // Non-existant UIN
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:10 ");
              throw (new JimmException(112, 0));
            }
            else if ((disconnectPacket.getError() == 0x0015) || (disconnectPacket.getError() == 0x0016))
            {   // Too many clients from same IP
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:11 ");
              throw (new JimmException(113, 0));
            }
            else if (disconnectPacket.getError() == 0x0018)
            {   // Rate exceeded
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:12 ");
              throw (new JimmException(114, 0));
            }
            else
            {   // Unknown error
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:13 ");
              throw (new JimmException(100, 1));
            }

          }

        }

      }
      // Watch out for STATE_CLI_DISCONNECT_SENT
      else if (this.state == ConnectAction.STATE_CLI_DISCONNECT_SENT)
      {
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:14 ");
        // Watch out for SRV_HELLO packet
        if (packet instanceof ConnectPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:15 ");
          ConnectPacket connectPacket = (ConnectPacket) packet;
          if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:16 ");
            // Send a CLI_COOKIE packet as reply
            ConnectPacket reply = new ConnectPacket(this.cookie);
            this.icq.c.sendPacket(reply);

            // Move to next state
            this.state = ConnectAction.STATE_CLI_COOKIE_SENT;

            // Packet has been consumed
            consumed = true;

          }
        }

      }
      // Watch out for STATE_CLI_COOKIE_SENT
      else if (this.state == ConnectAction.STATE_CLI_COOKIE_SENT)
      {
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:17 ");
        // Watch out for SRV_FAMILIES packet type
        if (packet instanceof SnacPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:18 ");
          SnacPacket snacPacket = (SnacPacket) packet;
          if ((snacPacket.getFamily() == SnacPacket.SRV_FAMILIES_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_FAMILIES_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:19 ");
            // Send a CLI_FAMILIES packet as reply
            SnacPacket reply = new SnacPacket(SnacPacket.CLI_FAMILIES_FAMILY,
                              SnacPacket.CLI_FAMILIES_COMMAND,
                              0x00000000,
                              new byte[0],
                              ConnectAction.CLI_FAMILIES_DATA);
            this.icq.c.sendPacket(reply);

            // Move to next state
            this.state = ConnectAction.STATE_CLI_FAMILIES_SENT;

            // Packet has been consumed
            consumed = true;

          }
        }

      }
      // Watch out for STATE_CLI_FAMILIES_SENT
      else if (this.state == ConnectAction.STATE_CLI_FAMILIES_SENT)
      {
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:20 ");
        // Watch out for SNAC packet
        if (packet instanceof SnacPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:21 ");
          SnacPacket snacPacket = (SnacPacket) packet;

          // Watch out for SRV_FAMILIES2 packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_FAMILIES2_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_FAMILIES2_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:22 ");
            this.srvFamilies2Rcvd = true;
          }

          // Watch out for SRV_MOTD packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_MOTD_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_MOTD_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:23 ");
            this.srvMotdRcvd = true;
          }

          // Check if we received both SRV_FAMILIES2 and SRV_MOTD packet
          if (this.srvFamilies2Rcvd && this.srvMotdRcvd)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:24 ");
            // Send a CLI_RATESREQUEST packet as reply
            SnacPacket reply = new SnacPacket(SnacPacket.CLI_RATESREQUEST_FAMILY,
                              SnacPacket.CLI_RATESREQUEST_COMMAND,
                              0x00000000,
                              new byte[0],
                              new byte[0]);
            this.icq.c.sendPacket(reply);

            // Move to next state
            this.state = ConnectAction.STATE_CLI_RATESREQUEST_SENT;

            // Packet has been consumed
            consumed = true;

          }

        }

      }
      // Watch out for STATE_CLI_RATESREQUEST_SENT
      else if (this.state == ConnectAction.STATE_CLI_RATESREQUEST_SENT)
      {
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:25 ");
        // Watch out for SRV_RATES packet
        if (packet instanceof SnacPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:26 ");
          SnacPacket snacPacket = (SnacPacket) packet;
          if ((snacPacket.getFamily() == SnacPacket.SRV_RATES_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_RATES_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:27 ");
            // Send a CLI_ACKRATES packet
            SnacPacket reply1 = new SnacPacket(SnacPacket.CLI_ACKRATES_FAMILY,
                               SnacPacket.CLI_ACKRATES_COMMAND,
                               0x00000000,
                               new byte[0],
                               ConnectAction.CLI_ACKRATES_DATA);
            this.icq.c.sendPacket(reply1);

            // Send a CLI_REQINFO packet
            SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_REQINFO_FAMILY,
                               SnacPacket.CLI_REQINFO_COMMAND,
                               0x00000000,
                               new byte[0],
                               new byte[0]);
            this.icq.c.sendPacket(reply2);

            // Send a CLI_REQLISTS packet
            SnacPacket reply3 = new SnacPacket(SnacPacket.CLI_REQLISTS_FAMILY,
                               SnacPacket.CLI_REQLISTS_COMMAND,
                               0x00000000,
                               new byte[0],
                               new byte[0]);
            this.icq.c.sendPacket(reply3);

            // Send a CLI_REQROSTER or CLI_CHECKROSTER packet
            long versionId1 = Jimm.jimm.getContactListRef().getVersionId1();
            int versionId2 = Jimm.jimm.getContactListRef().getVersionId2();
            if ((versionId1 == -1) && (versionId2 == -1))
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:28 ");
              SnacPacket reply4 = new SnacPacket(SnacPacket.CLI_REQROSTER_FAMILY,
                                 SnacPacket.CLI_REQROSTER_COMMAND,
                                 0x00000000,
                                 new byte[0],
                                 new byte[0]);
              this.icq.c.sendPacket(reply4);
            }
            else
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:29 ");
              byte[] data = new byte[6];
              Util.putDWord(data, 0, versionId1);
              Util.putWord(data, 4, versionId2);
              SnacPacket reply4 = new SnacPacket(SnacPacket.CLI_CHECKROSTER_FAMILY,
                                 SnacPacket.CLI_CHECKROSTER_COMMAND,
                                 0x00000000,
                                 new byte[0],
                                 data);
              this.icq.c.sendPacket(reply4);
            }

            // Send a CLI_REQLOCATION packet
            SnacPacket reply5 = new SnacPacket(SnacPacket.CLI_REQLOCATION_FAMILY,
                               SnacPacket.CLI_REQLOCATION_COMMAND,
                               0x00000000,
                               new byte[0],
                               new byte[0]);
            this.icq.c.sendPacket(reply5);

            // Send a CLI_REQBUDDY packet
            SnacPacket reply6 = new SnacPacket(SnacPacket.CLI_REQBUDDY_FAMILY,
                               SnacPacket.CLI_REQBUDDY_COMMAND,
                               0x00000000,
                               new byte[0],
                               new byte[0]);
            this.icq.c.sendPacket(reply6);

            // Send a CLI_REQICBM packet
            SnacPacket reply7 = new SnacPacket(SnacPacket.CLI_REQICBM_FAMILY,
                               SnacPacket.CLI_REQICBM_COMMAND,
                               0x00000000,
                               new byte[0],
                               new byte[0]);
            this.icq.c.sendPacket(reply7);

            // Send a CLI_REQBOS packet
            SnacPacket reply8 = new SnacPacket(SnacPacket.CLI_REQBOS_FAMILY,
                               SnacPacket.CLI_REQBOS_COMMAND,
                               0x00000000,
                               new byte[0],
                               new byte[0]);
            this.icq.c.sendPacket(reply8);

            // Move to next state
            this.state = ConnectAction.STATE_CLI_REQBOS_SENT;

            // Packet has been consumed
            consumed = true;

          }
        }

      }
      // Watch out for STATE_CLI_REQBOS_SENT
      else if (this.state == ConnectAction.STATE_CLI_REQBOS_SENT)
      {
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:30 ");
        // Watch out for SNAC packet
        if (packet instanceof SnacPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:31 ");
          SnacPacket snacPacket = (SnacPacket) packet;

          // Watch out for SRV_REPLYINFO packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYINFO_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYINFO_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:32 ");
            this.srvReplyInfoRcvd = true;

            // Packet has been consumed
            consumed = true;

          }

          // Watch out for SRV_REPLYLOCATION packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYLOCATION_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYLOCATION_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:33 ");
            this.srvReplyLocationRcvd = true;

            // Packet has been consumed
            consumed = true;

          }

          // Watch out for SRV_REPLYBUDDY packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYBUDDY_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYBUDDY_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:34 ");
            this.srvReplyBuddyRcvd = true;

            // Packet has been consumed
            consumed = true;

          }

          // Watch out for SRV_REPLYICBM packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYICBM_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYICBM_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:35 ");
            this.srvReplyIcbmRcvd = true;

            // Packet has been consumed
            consumed = true;

          }

          // Watch out for SRV_REPLYBOS packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYBOS_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYBOS_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:36 ");
            this.srvReplyBosRcvd = true;

            // Packet has been consumed
            consumed = true;

          }

          // Watch out for SRV_REPLYLISTS packet
          if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYLISTS_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYLISTS_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:37 ");
            this.srvReplyListsRcvd = true;

            // Packet has been consumed
            consumed = true;

          }

          // Watch out for SRV_REPLYROSTEROK
          if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTEROK_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTEROK_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:38 ");
            this.srvReplyRosterRcvd = true;

            // Update contact list
            Jimm.jimm.getContactListRef().update();

            // Packet has been consumed
            consumed = true;

          }
          // watch out for SRV_REPLYROSTER packet
          else if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTER_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTER_COMMAND))
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:39 ");
            this.srvReplyRosterRcvd = true;

            // Initialize vector for items
            Vector items = new Vector();

            // Get data
            byte[] buf = snacPacket.getData();
            int marker = 0;

            // Check length
            if (buf.length < 3)
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:40 ");
              throw (new JimmException(115, 0));
            }

            // Skip SRV_REPLYROSTER.UNKNOWN
            marker += 1;

            // Iterate through all items
            int count = Util.getWord(buf, marker);
            marker += 2;
            for (int i = 0; i < count; i++)
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:41 ");
              // Check length
              if (buf.length < marker + 2)
              {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:42 ");
                throw (new JimmException(115, 1));
              }

              // Get name length
              int nameLen = Util.getWord(buf, marker);
              marker += 2;

              // Check length
              if (buf.length < marker + nameLen + 2 + 2 + 2 + 2)
              {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:43 ");
                throw (new JimmException(115, 2));
              }

              // Get name
              String name = Util.byteArrayToString(buf, marker, nameLen);
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
              if (buf.length < marker + len)
              {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:44 ");
                throw (new JimmException(115, 3));
              }
              // Normal contact
              if (type == 0x0000)
              {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:45 ");
                // Get nick
                String nick = new String(name);
                boolean noAuth = false;
                
                while (len > 0)
                {
			 	  // Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:46 ");
                  byte[] tlvData = Util.getTlv(buf, marker);
                  if (tlvData == null)
                  {
					// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:47 ");
                    throw (new JimmException(115, 4));
                  }
                  int tlvType = Util.getWord(buf, marker);
                  if (tlvType == 0x0131)
                  {
					// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:48 ");
                    nick = Util.byteArrayToString(tlvData, true);
                  }
                  if (tlvType == 0x0066)
                  {
                  	noAuth = true;
                  }
                  len -= 4;
                  len -= tlvData.length;
                  marker += 4 + tlvData.length;
                }
                if (len != 0)
                {
					// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:49 ");
                  throw (new JimmException(115, 5));
                }

                // Add this contact item to the vector
                items.addElement(new ContactListContactItem(id, group, name, nick, noAuth));

              }
              // Group of contacts
              else if (type == 0x0001)
              {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:50 ");
                // Skip TLVs
                marker += len;

                // Add this group item to the vector
                if (group != 0x0000)
                {
					// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:51 ");
                  items.addElement(new ContactListGroupItem(group, name));
                }

              }
              // All other item types
              else
              {

				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:52 ");
                // Skip TLVs
                marker += len;

              }

            }

            // Check length
            if (buf.length != marker + 4)
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:53 ");
              throw (new JimmException(115, 6));
            }

            // Get timestamp
            long timestamp = Util.getDWord(buf, marker);

            // Update contact list
            ContactListItem[] itemsAsArray = new ContactListItem[items.size()];
            items.copyInto(itemsAsArray);
				    Jimm.jimm.getContactListRef().update(timestamp, count, itemsAsArray);
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:1");
            // Packet has been consumed
            consumed = true;

          }

          // Check if all packets have been received
          if (this.srvReplyInfoRcvd && this.srvReplyLocationRcvd && this.srvReplyBuddyRcvd && this.srvReplyIcbmRcvd && this.srvReplyBosRcvd && this.srvReplyListsRcvd && this.srvReplyRosterRcvd)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:54 ");
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:2");
            // Send a CLI_ROSTERACK packet
            SnacPacket reply1 = new SnacPacket(SnacPacket.CLI_ROSTERACK_FAMILY,
                               SnacPacket.CLI_ROSTERACK_COMMAND,
                               0x00000000,
                               new byte[0],
                               new byte[0]);
            this.icq.c.sendPacket(reply1);

            // Send a CLI_SETUSERINFO packet
            SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_SETUSERINFO_FAMILY,
                               SnacPacket.CLI_SETUSERINFO_COMMAND,
                               0x00000000,
                               new byte[0],
                               ConnectAction.CLI_SETUSERINFO_DATA);
            this.icq.c.sendPacket(reply2);

            // Send a CLI_SETICBM packet
            SnacPacket reply3 = new SnacPacket(SnacPacket.CLI_SETICBM_FAMILY,
                               SnacPacket.CLI_SETICBM_COMMAND,
                               0x00000000,
                               new byte[0],
                               ConnectAction.CLI_SETICBM_DATA);
            this.icq.c.sendPacket(reply3);

            // Send a CLI_SETSTATUS packet
            long onlineStatus = Util.translateStatusSend(Jimm.jimm.getOptionsRef().getOnlineStatus());
            Util.putDWord(ConnectAction.CLI_SETSTATUS_DATA, 4, onlineStatus);
            SnacPacket reply4 = new SnacPacket(SnacPacket.CLI_SETSTATUS_FAMILY,
                               SnacPacket.CLI_SETSTATUS_COMMAND,
                               0x00000000,
                               new byte[0],
                               ConnectAction.CLI_SETSTATUS_DATA);
            this.icq.c.sendPacket(reply4);

            // Send a CLI_READY packet
            SnacPacket reply5 = new SnacPacket(SnacPacket.CLI_READY_FAMILY,
                               SnacPacket.CLI_READY_COMMAND,
                               0x00000000,
                               new byte[0],
                               ConnectAction.CLI_READY_DATA);
            this.icq.c.sendPacket(reply5);

            // Send a CLI_TOICQSRV/CLI_REQOFFLINEMSGS packet
            ToIcqSrvPacket reply6 = new ToIcqSrvPacket(0x00000000,
                                   this.uin,
                                   ToIcqSrvPacket.CLI_REQOFFLINEMSGS_SUBCMD,
                                   new byte[0],
                                   new byte[0]);
            this.icq.c.sendPacket(reply6);

            // Move to next state
            this.state = ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT;

          }

        }

      }
      // Watch out for STATE_CLI_REQOFFLINEMSGS_SENT
      else if (this.state == ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT)
      {
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:55 ");
		// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:3");
        // Watch out for SRV_FROMICQSRV packet
//		if (packet instanceof Packet)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:Packet");
//		if (packet instanceof ConnectPacket)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:ConnectPacket");
//		if (packet instanceof DisconnectPacket)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:DisconnectPacket");
//		if (packet instanceof ErrorPacket)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:ErrorPacket");
//        if (packet instanceof FromIcqSrvPacket)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:FromIcqSrvPacket");
//		if (packet instanceof PingPacket)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:PingPacket");
//		if (packet instanceof SnacPacket)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:SnacPacket");
//		if (packet instanceof ToIcqSrvPacket)
//			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:ToIcqSrvPacket");
        
        if (packet instanceof FromIcqSrvPacket)
        {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:56 ");
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:3.5");
          FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

          // Watch out for SRV_OFFLINEMSG
          if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_OFFLINEMSG_SUBCMD)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:57 ");
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:4");
            // Get raw data
            byte[] buf = fromIcqSrvPacket.getData();

            // Check length
            if (buf.length < 14)
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:58 ");
              throw (new JimmException(116, 0));
            }

            // Extract UIN
            long uinRaw = Util.getDWord(buf, 0, false);
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

            // Get type
            int type = Util.getWord(buf, 10, false);

            // Get text length
            int textLen = Util.getWord(buf, 12, false);

            // Check length
            if (buf.length != 14 + textLen)
            {
              throw (new JimmException(116, 1));
            }

            // Get text
            String text = Util.crlfToCr(Util.byteArrayToString(buf, 14, textLen));

            // Normal message
            if (type == 0x0001)
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:59 ");
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:5");
              // Forward message to contact list
              PlainMessage message = new PlainMessage(uin, this.uin, date, text);
              Jimm.jimm.getContactListRef().addMessage(message);

            }
            // URL message
            else if (type == 0x0004)
            {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:60 ");
              // Search for delimiter
              int delim = text.indexOf(0xFE);

              // Split message, if delimiter could be found
              String urlText;
              String url;
              if (delim != -1)
              {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:61 ");
                urlText = text.substring(0, delim);
                url = text.substring(delim + 1);
              }
              else
              {
				// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:62 ");
                urlText = text;
                url = "";
              }

              // Forward message message to contact list
              UrlMessage message = new UrlMessage(uin, this.uin, date, url, urlText);
              Jimm.jimm.getContactListRef().addMessage(message);

            }

            // Packet has been consumed
            consumed = true;
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:5.1");
          }
		 // Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:5.2");
          // Watch out for SRV_DONEOFFLINEMSGS
          if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_DONEOFFLINEMSGS_SUBCMD)
          {
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:63 ");
			// Jimm.jimm.getSplashCanvasRef().setMessage("ConnectAction:forward:6");
            // Send a CLI_TOICQSRV/CLI_ACKOFFLINEMSGS packet
            ToIcqSrvPacket reply = new ToIcqSrvPacket(0x00000000,
                                  this.uin,
                                  ToIcqSrvPacket.CLI_ACKOFFLINEMSGS_SUBCMD,
                                  new byte[0],
                                  new byte[0]);
            this.icq.c.sendPacket(reply);

            // Move to next state
            this.state = ConnectAction.STATE_CLI_ACKOFFLINEMSGS_SENT;

            // Move to STATE_CONNECTED
            this.icq.setConnected();

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
    if ((this.state != ConnectAction.STATE_ERROR) && !this.active && (this.lastActivity.getTime() + ConnectAction.TIMEOUT < System.currentTimeMillis()))
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
        return (11);
      case ConnectAction.STATE_CLI_IDENT_SENT:
        return (22);
      case ConnectAction.STATE_CLI_DISCONNECT_SENT:
        return (33);
      case ConnectAction.STATE_CLI_COOKIE_SENT:
        return (44);
      case ConnectAction.STATE_CLI_FAMILIES_SENT:
        return (55);
      case ConnectAction.STATE_CLI_RATESREQUEST_SENT:
        return (66);
      case ConnectAction.STATE_CLI_REQBOS_SENT:
        return (77);
      case ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT:
        return (88);
      case ConnectAction.STATE_CLI_ACKOFFLINEMSGS_SENT:
        return (100);
      default:
        return (0);
    }
  }


}
