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

import java.util.Date;
import java.util.Vector;

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactListItem;
import jimm.ContactList;
import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.comm.Icq.HTTPConnection;
import jimm.util.ResourceBundle;

public class ConnectAction extends Action
{
    // Action states
    public static final int STATE_ERROR = -1;
    public static final int STATE_INIT_DONE = 0;
	public static final int STATE_AUTHKEY_REQUESTED = 1;
    public static final int STATE_CLI_IDENT_SENT = 2;
    public static final int STATE_CLI_DISCONNECT_SENT = 3;
    public static final int STATE_CLI_COOKIE_SENT = 4;
    public static final int STATE_CLI_CHECKROSTER_SENT = 5;
    public static final int STATE_CLI_REQOFFLINEMSGS_SENT = 6;
    public static final int STATE_CLI_ACKOFFLINEMSGS_SENT = 7;

    // CLI_SETUSERINFO packet data
    public static final byte[] CLI_SETUSERINFO_DATA =
	{
		(byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x50,   // 4 capabilities follow
		(byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x49, // CAP_AIM_SERVERRELAY
		(byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, 
		(byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, 
		(byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00,
		(byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x44,   // CAP_AIM_ISICQ
		(byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, 
		(byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, 
		(byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00,		
		(byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x4E,   //CAP_UTF8
		(byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, 
		(byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, 
		(byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00,
		(byte) 0x56, (byte) 0x3f, (byte) 0xc8, (byte) 0x09,      // CAP_MTN
		(byte) 0x0b, (byte) 0x6f, (byte) 0x41, (byte) 0xbd,
		(byte) 0x9f, (byte) 0x79, (byte) 0x42, (byte) 0x26,
		(byte) 0x09, (byte) 0xdf, (byte) 0xa2, (byte) 0xf3,
		/* Jimm */ 'J', 'i', 'm', 'm', //Jimm version
		(byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//Place for string & raw version
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
	};

    // CLI_SETICBM packet data
    public static final byte[] CLI_SETICBM_DATA =
    { 0, 0, 0, 0, 0, 3, 0x1F,  0x40, 3, (byte)0xE7, 3, (byte)0xE7, 0, 0, 0, 0};

    // CLI_SETSTATUS packet data
    public static final byte[] CLI_SETSTATUS_DATA =
    { (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x04, 
	  (byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Online status
      (byte) 0x00, (byte) 0x0C, (byte) 0x00, (byte) 0x25, // TLV(C)
      (byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1, cannot get own IP address
      (byte) 0x00, (byte) 0x00, (byte) 0xAB, (byte) 0xCD, // Port 43981
      (byte) 0x00, // Firewall
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
    {(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x03,
	 (byte) 0x01, (byte) 0x10, (byte) 0x04,	(byte) 0x7B,
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
	private String server;
    private byte[] cookie;
    private boolean srvReplyRosterRcvd;

    // Constructor
    public ConnectAction(String uin, String password, String srvHost, String srvPort)
    {
    	super(true, false);
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

    // Init action
    protected void init() throws JimmException
    {
        // #sijapp cond.if modules_PROXY is "true" #
        int retry = 1;
        try
        {
            retry = Integer.parseInt(Options.getString(Options.OPTION_AUTORETRY_COUNT));
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
            	Icq.c.connect(this.srvHost + ":" + this.srvPort);
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
                    	Icq.c.close();
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
            retry = Integer.parseInt(Options.getString(Options.OPTION_AUTORETRY_COUNT));
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
						if (Options.getBoolean(Options.OPTION_MD5_LOGIN)) {
							Icq.c.sendPacket(new ConnectPacket());
							byte[] buf = new byte[4 + this.uin.length()];
							Util.putWord(buf, 0, 0x0001);
							Util.putWord(buf, 2, this.uin.length());
							byte[] uinRaw = Util.stringToByteArray(this.uin);
							System.arraycopy(uinRaw, 0, buf, 4, uinRaw.length);
							Icq.c.sendPacket(new SnacPacket(0x0017, 0x0006, 0, new byte[0], buf));
						} else {
							// Send a CLI_IDENT packet as reply
							ConnectPacket reply = new ConnectPacket(this.uin, this.password);
							Icq.c.sendPacket(reply);
						}

                        // Move to next state
                        this.state = !Options.getBoolean(Options.OPTION_MD5_LOGIN) ? ConnectAction.STATE_CLI_IDENT_SENT :
							STATE_AUTHKEY_REQUESTED;

                        // Packet has been consumed
                        consumed = true;

                    }
                }

            }
			else if (state == STATE_AUTHKEY_REQUESTED) {
				if (packet instanceof SnacPacket) {
					SnacPacket snacPacket = (SnacPacket)packet;
					if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x0007)) {
						byte[] rbuf = snacPacket.getData();
						int len = Util.getWord(rbuf, 0);
						byte[] authkey = new byte[len];
						System.arraycopy(rbuf, 2, authkey, 0, len);
						rbuf = null;
						byte[] buf = new byte[2 + 2 + this.uin.length() + 2 + 2 + 16];
						int marker = 0;
						Util.putWord(buf, marker, 0x0001);
						marker += 2;
						Util.putWord(buf, marker, this.uin.length());
						marker += 2;
						byte[] uinRaw = Util.stringToByteArray(this.uin);
						System.arraycopy(uinRaw, 0, buf, marker, uinRaw.length);
						marker += uinRaw.length;
						Util.putWord(buf, marker, 0x0025);
						marker += 2;
						Util.putWord(buf, marker, 0x0010);
						marker += 2;
						byte[] md5buf = new byte[authkey.length + this.password.length() + Util.AIM_MD5_STRING.length];
						int md5marker = 0;
						System.arraycopy(authkey, 0, md5buf, md5marker, authkey.length);
						md5marker += authkey.length;
						byte[] passwordRaw = Util.stringToByteArray(this.password);
						System.arraycopy(passwordRaw, 0, md5buf, md5marker, passwordRaw.length);
						md5marker += passwordRaw.length;
						System.arraycopy(Util.AIM_MD5_STRING, 0, md5buf, md5marker, Util.AIM_MD5_STRING.length);
						byte[] hash = Util.calculateMD5(md5buf);
						System.arraycopy(hash, 0, buf, marker, 16);
						Icq.c.sendPacket(new SnacPacket(0x0017, 0x0002, 0, new byte[0], buf));
						state = STATE_CLI_IDENT_SENT;
					} else {
						throw new JimmException(100,0);
					}
				}
				consumed = true;
			}
            // Watch out for STATE_CLI_IDENT_SENT
            else if (this.state == ConnectAction.STATE_CLI_IDENT_SENT)
			{
				int errcode = -1;
				if (Options.getBoolean(Options.OPTION_MD5_LOGIN)) {
					if (packet instanceof SnacPacket) {
						SnacPacket snacPacket = (SnacPacket)packet;
						if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x0003)) {
							byte[] buf = snacPacket.getData();
							int marker = 0;
							while (marker < buf.length) {
								byte[] tlvData = Util.getTlv(buf, marker);
								int tlvType = Util.getWord(buf, marker);
								marker += 4 + tlvData.length;
								switch (tlvType) {
									case 0x0008:
										errcode = Util.getWord(tlvData, 0);
										break;
									case 0x0005:
										this.server = Util.byteArrayToString(tlvData);
										break;
									case 0x0006:
										this.cookie = tlvData;
										break;
								}
							}
						}
					} else if (packet instanceof DisconnectPacket) {
						consumed = true;
					}
				} else {
					// watch out for channel 4 packet
					if (packet instanceof DisconnectPacket) {
						DisconnectPacket disconnectPacket = (DisconnectPacket) packet;
						// Watch out for SRV_COOKIE packet
						if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_COOKIE) {
							// Save cookie
							this.cookie = disconnectPacket.getCookie();
							this.server = disconnectPacket.getServer();
						}
						// Watch out for SRV_GOODBYE packet
						else if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_GOODBYE)
							errcode = disconnectPacket.getError();
						consumed = true;
					}
				}

				if (errcode != -1) {
					int toThrow = 100;
					switch (errcode) {
						// Multiple logins
						case 0x0001:
							toThrow = 110;
							break;
						// Bad password
						case 0x0004: case 0x0005:
							toThrow = 111;
							break;
						// Non-existant UIN
						case 0x0007: case 0x0008:
							toThrow = 112;
							break;
						// Too many clients from same IP
						case 0x0015: case 0x0016:
							toThrow = 113;
							break;
						// Rate exceeded
						case 0x0018: case 0x001d:
							toThrow = 114;
							break;
					}
					throw new JimmException(toThrow, errcode);
				}

				if (consumed & (this.server != null) & (this.cookie != null)) {
					// Close connection (only if not HTTP Connection)
					if (!(Icq.c instanceof HTTPConnection))
						Icq.c.close();
					// #sijapp cond.if target is "DEFAULT" | target is "MIDP2"#
					if (Options.getBoolean(Options.OPTION_SHADOW_CON)) try
					{
						// Wait the given time before starting the
						// new connection
						Thread.sleep(2000);
					} catch (InterruptedException e) {}
					// #sijapp cond.end#
					// Open connection
					// #sijapp cond.if modules_PROXY is "true" #
					for (int i = 0; i < retry; i++)
					{
						try
						{
							// #sijapp cond.end #
							Icq.c.connect(server);
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
							else if ((this.lastActivity.getTime() + this.TIMEOUT) > System.currentTimeMillis())
							{
								Icq.c.close();
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
				}

			}
			// Watch out for STATE_CLI_DISCONNECT_SENT
			else if (this.state == ConnectAction.STATE_CLI_DISCONNECT_SENT)
			{

				// Watch out for SRV_HELLO packet
				if (packet instanceof ConnectPacket)
				{
					ConnectPacket connectPacket = (ConnectPacket) packet;
					if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
					{

						// Send a CLI_COOKIE packet as reply
						ConnectPacket reply = new ConnectPacket(this.cookie);
						Icq.c.sendPacket(reply);

						// Send a CLI_SETUSERINFO packet
						// Set version information to this packet in our capability
						byte[] tmp = ConnectAction.CLI_SETUSERINFO_DATA;
						byte[] ver = Util.stringToByteArray(Jimm.VERSION);
						if ( ver.length<12 )
							System.arraycopy(ver,0,tmp,tmp.length-11,ver.length);
						Icq.c.sendPacket(new SnacPacket(0x0002, 0x0004, 0, new byte[0], tmp));

						// Send a CLI_SETICBM packet
						SnacPacket reply1 = new SnacPacket(SnacPacket.CLI_SETICBM_FAMILY, SnacPacket.CLI_SETICBM_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_SETICBM_DATA);
						Icq.c.sendPacket(reply1);

						// Send a CLI_SETSTATUS packet
						Util.putDWord(ConnectAction.CLI_SETSTATUS_DATA, 4, (0x10<<24)|Util.translateStatusSend((int)Options.getLong(Options.OPTION_ONLINE_STATUS)));
						SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_SETSTATUS_FAMILY, SnacPacket.CLI_SETSTATUS_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_SETSTATUS_DATA);
						Icq.c.sendPacket(reply2);

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

				// Send a CLI_REQROSTER or
				// CLI_CHECKROSTER packet
				long versionId1 = ContactList.getSsiListLastChangeTime();
				int versionId2 = ContactList.getSsiNumberOfItems();
				if (((versionId1 == -1) && (versionId2 == -1)) || (ContactList.getSize() == 0))
				{
					SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_REQROSTER_FAMILY, SnacPacket.CLI_REQROSTER_COMMAND, 0x00000000, new byte[0], new byte[0]);
					Icq.c.sendPacket(reply2);
				}
				else
				{
					byte[] data = new byte[6];
					Util.putDWord(data, 0, versionId1);
					Util.putWord(data, 4, versionId2);
					SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_CHECKROSTER_FAMILY, SnacPacket.CLI_CHECKROSTER_COMMAND, 0x00000000, new byte[0], data);
					Icq.c.sendPacket(reply2);
				}

				// Move to next state
				this.state = ConnectAction.STATE_CLI_CHECKROSTER_SENT;

			}
			// Watch out for STATE_CLI_CHECKROSTER_SENT
			else if (this.state == ConnectAction.STATE_CLI_CHECKROSTER_SENT)
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
					else if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYROSTER_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_REPLYROSTER_COMMAND))
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
							String name = Util.byteArrayToString(buf, marker, nameLen, Util.isDataUTF8(buf, marker, nameLen));
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
									else if (tlvType == 0x0066)
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
							else if (type == 0x0001)
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
							else if (type == 0x0004)
							{
								while (len > 0)
								{
									byte[] tlvData = Util.getTlv(buf, marker);
									if (tlvData == null) { throw (new JimmException(115, 110)); }
									int tlvType = Util.getWord(buf, marker);

									if (tlvType == 0x00CA)
									{
										Options.setInt(Options.OPTION_VISIBILITY_ID, (int)id);
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
						int timestamp = (int)Util.getDWord(buf, marker);

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
						Icq.c.sendPacket(reply1);

						int onlineStatusOpt = (int)Options.getLong(Options.OPTION_ONLINE_STATUS);
						int onlineStatus = Util.translateStatusSend(onlineStatusOpt);
						int visibilityItemId = Options.getInt(Options.OPTION_VISIBILITY_ID);
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
								Icq.c.sendPacket(reply2pre);
							}
						}

						// Send to server sequence of unuthoruzed contacts to see their statuses 
						String[] noauth = ContactList.getUnauthAndTempContacts();
						if (noauth.length > 0)
							Icq.addLocalContacts(noauth);

						// Change privacy setting according to new status
						if(visibilityItemId != 0 && onlineStatus != Util.SET_STATUS_INVISIBLE)
						{
							SnacPacket reply2post = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
														SnacPacket.CLI_ROSTERUPDATE_COMMAND,
														SnacPacket.CLI_ROSTERUPDATE_COMMAND,
														new byte[0],
														buf);
							Icq.c.sendPacket(reply2post);
						}

						// Send a CLI_READY packet
						SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_READY_FAMILY, SnacPacket.CLI_READY_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_READY_DATA);
						Icq.c.sendPacket(reply2);

						// Send a CLI_TOICQSRV/CLI_REQOFFLINEMSGS packet
						ToIcqSrvPacket reply3 = new ToIcqSrvPacket(0x00000000, this.uin, ToIcqSrvPacket.CLI_REQOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
						Icq.c.sendPacket(reply3);

						// Move to next state
						this.state = ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT;

					}

				}

			}
			// Watch out for STATE_CLI_REQOFFLINEMSGS_SENT
			else if (this.state == ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT)
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

						String uin = String.valueOf(uinRaw);
						
						// Extract date of dispatch
						long date = Util.createLongTime
									  (
											Util.getWord(buf, 4, false),
											Util.getByte(buf, 6),
											Util.getByte(buf, 7),
											Util.getByte(buf, 8),
											Util.getByte(buf, 9),
											0
									   );

						// Get type
						int type = Util.getWord(buf, 10, false);

						// Get text length
						int textLen = Util.getWord(buf, 12, false);

						// Check length
						if (buf.length != 14 + textLen) { throw (new JimmException(116, 1)); }

						// Get text
						String text = Util.removeCr(Util.byteArrayToString(buf, 14, textLen, Util.isDataUTF8(buf, 14, textLen)));

						// Normal message
						if (type == 0x0001)
						{
							 // Forward message to contact list
							
							PlainMessage message = new PlainMessage(uin, this.uin, date, text, true);
							ContactList.addMessage(message);

						}
						// URL message
						else if (type == 0x0004)
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

						// Packet has been consumed
						consumed = true;

					}
					// Watch out for SRV_DONEOFFLINEMSGS
					else if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_DONEOFFLINEMSGS_SUBCMD)
					{

						// Send a CLI_TOICQSRV/CLI_ACKOFFLINEMSGS packet
						ToIcqSrvPacket reply = new ToIcqSrvPacket(0x00000000, this.uin, ToIcqSrvPacket.CLI_ACKOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
						Icq.c.sendPacket(reply);

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
            return 12;
		case STATE_AUTHKEY_REQUESTED:
			return 25;
        case ConnectAction.STATE_CLI_IDENT_SENT:
            return 37;
        case ConnectAction.STATE_CLI_DISCONNECT_SENT:
            return 50;
        case ConnectAction.STATE_CLI_COOKIE_SENT:
            return 62;
        case ConnectAction.STATE_CLI_CHECKROSTER_SENT:
            return 75;
        case ConnectAction.STATE_CLI_REQOFFLINEMSGS_SENT:
            return 87;
        case ConnectAction.STATE_CLI_ACKOFFLINEMSGS_SENT:
            return 100;
        default:
            return (0);
        }
    }
    
    public void onEvent(int eventType)
    {
    	switch (eventType)
    	{
    	case ON_COMPLETE:
    		ContactList.activate();
    		break;
    	}
    }

}
