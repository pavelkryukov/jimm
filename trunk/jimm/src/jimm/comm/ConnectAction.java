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

import java.util.*;
import java.io.*;

import jimm.ContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactListItem;
import jimm.ContactList;
import jimm.JimmUI;
import jimm.JimmException;
import jimm.Options;
import jimm.MainThread;
import jimm.util.ResourceBundle;
import jimm.comm.connections.HTTPConnection;

public class ConnectAction extends Action
{
    // Action states
    public static final int STATE_ERROR = -1;
    public static final int STATE_INIT_DONE = 0;
	public static final int STATE_AUTHKEY_REQUESTED = 1;
    public static final int STATE_CLI_IDENT_SENT = 2;
    public static final int STATE_CLI_DISCONNECT_SENT = 3;
    public static final int STATE_CLI_COOKIE_SENT = 4;
    public static final int STATE_CLI_WANT_CAPS_SENT = 5;
    public static final int STATE_CLI_CHECKROSTER_SENT = 6;
    public static final int STATE_CLI_STATUS_INFO_SENT = 7;
    public static final int STATE_CONNECTED = 8;
    
    public static final int STATE_MAX = 8;

    // CLI_SETICBM packet data
    public static final byte[] CLI_SETICBM_DATA = Util.explodeToBytes("0,0,0,0,0,0B,1F,40,3,E7,3,E7,0,0,0,0", ',', 16);

    // CLI_READY packet data
    public static final byte[] CLI_READY_DATA =
    	Util.explodeToBytes
    	(
    		"00,22,00,01,01,10,16,4f,"+
    		"00,01,00,04,01,10,16,4f,"+ 
		"00,13,00,04,01,10,16,4f,"+
		"00,02,00,01,01,10,16,4f,"+
		"00,03,00,01,01,10,16,4f,"+
		"00,15,00,01,01,10,16,4f,"+
		"00,04,00,01,01,10,16,4f,"+
		"00,06,00,01,01,10,16,4f,"+
		"00,09,00,01,01,10,16,4f,"+
		"00,0a,00,01,01,10,16,4f,"+
		"00,0b,00,01,01,10,16,4f",
		',', 16
    	);
    
    public static final short[] FAMILIES_AND_VER_LIST =
    {
    	0x0022, 0x0001,
    	0x0001, 0x0004,
    	0x0013, 0x0004,
    	0x0002, 0x0001,
    	0x0003, 0x0001,
    	0x0015, 0x0001,
    	0x0004, 0x0001,
    	0x0006, 0x0001,
    	0x0009, 0x0001,
    	0x000a, 0x0001,
    	0x000b, 0x0001,
    };
    
    // Timeout
    // #sijapp cond.if modules_PROXY is "true" #
    public static final int TIME_OUT = 30 * 1000;
    // #sijapp cond.end #
    public int TIMEOUT = 30 * 1000; // milliseconds

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
    private long lastActivity;
    private boolean active;
    private boolean canceled;
    private boolean canceled_by_timeout;

    // Temporary variables
	private String server;
    private byte[] cookie;
    private boolean srvReplyRosterRcvd;

    // Constructor
    public ConnectAction(String uin, String password, String srvHost, String srvPort)
    {
    	super(true, false);
    	System.out.println("conn. act. srvHost="+srvHost);
    	lastActivity = System.currentTimeMillis();
        this.uin = uin;
        this.password = password;
        this.srvHost = srvHost;
        this.srvPort = srvPort;
        
        System.out.println("ConnectAction, this="+this);
    }

    // Returns the UID
    public String getUin()
    {
        return this.uin;
    }

    // Returns the password
    public String getPassword()
    {
        return this.password;
    }

    // Returns the server host
    public String getSrvHost()
    {
        return this.srvHost;
    }

    // Returns the server port
    public String getSrvPort()
    {
        return this.srvPort;
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
            	Icq.connect(this.srvHost + ":" + this.srvPort);
                // #sijapp cond.if modules_PROXY is "true" #
                break;
                // #sijapp cond.end #
            } catch (JimmException e)
            {
                // #sijapp cond.if modules_PROXY is "true" #
                if (i >= (retry - 1) || ((this.lastActivity + this.TIMEOUT) < System.currentTimeMillis()))
                {
                    // #sijapp cond.end #

                    this.state = ConnectAction.STATE_ERROR;
                    throw (e);
                }
                // #sijapp cond.if modules_PROXY is "true" #
                else
                    if ((this.lastActivity + this.TIMEOUT) > System.currentTimeMillis())
                    {
                    	Icq.c.notifyToDisconnect();
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
        this.lastActivity = System.currentTimeMillis();

    }

    private int getConnectionErrorCode (int errCode) {
	switch (errCode) {
		// Multiple logins
		case 0x0001:
			return 110;
		// Bad password
		case 0x0004: case 0x0005:
			return 111;
		// Non-existant UIN
		case 0x0007: case 0x0008:
			return 112;
		// Too many clients from same IP
		case 0x0015: case 0x0016:
			return 113;
		// Rate exceeded
		case 0x0018: case 0x001d:
			return 114;
	}
	return 100;
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
				    int errcode = -1;
				    if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x0003)) {
					    byte[] buf = snacPacket.getData();
					    int marker = 0;
					    while (marker < buf.length) {
						    byte[] tlvData = Util.getTlv(buf, marker);
						    int tlvType = Util.getWord(buf, marker);
						    marker += 4 + tlvData.length;
						    if (tlvType == 0x0008) {
							    errcode = Util.getWord(tlvData, 0);
						    }
					    }
				    }

				    if (errcode != -1) {
					    consumed = true;
					    Icq.c.notifyToDisconnect();
					    this.state = ConnectAction.STATE_ERROR;
					    int toThrow = getConnectionErrorCode (errcode);
					    throw new JimmException(toThrow, errcode);
				    }

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
				    consumed = true;
				    Icq.c.notifyToDisconnect();
				    this.state = ConnectAction.STATE_ERROR;
				    int toThrow = getConnectionErrorCode (errcode);
				    throw new JimmException(toThrow, errcode);
				}

				if (consumed & (this.server != null) & (this.cookie != null)) {
					// Close connection (only if not HTTP Connection)
					if (!(Icq.c instanceof HTTPConnection))
					{
						Icq.c.forceDisconnect();
						Thread.yield();
						try { Thread.sleep(1000); } catch (Exception e ) {}
					}
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
							if (i >= (retry - 1) || ((this.lastActivity + this.TIMEOUT) < System.currentTimeMillis()))
							{
								this.active = false;
								this.state = ConnectAction.STATE_ERROR;
								throw (e);
							}
							else if ((this.lastActivity + this.TIMEOUT) > System.currentTimeMillis())
							{
								Icq.c.notifyToDisconnect();
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
						
						// Move to next state
						this.state = ConnectAction.STATE_CLI_COOKIE_SENT;

						// Packet has been consumed
						consumed = true;
					}
				}
			}
            
            // STATE_CLI_COOKIE_SENT
			else if (this.state == ConnectAction.STATE_CLI_COOKIE_SENT)
			{
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				
				for (int i = 0; i < FAMILIES_AND_VER_LIST.length; i++) 
					Util.writeWord(stream, FAMILIES_AND_VER_LIST[i], true);
				
				Icq.c.sendPacket
				(
					new SnacPacket
					(
						SnacPacket.CLI_FAMILIES_FAMILY, 
						SnacPacket.CLI_FAMILIES_COMMAND, 
						SnacPacket.CLI_FAMILIES_COMMAND, 
						new byte[0], 
						stream.toByteArray()
					)
				);
				
				this.state = ConnectAction.STATE_CLI_WANT_CAPS_SENT;
			}
       
			// Watch out for STATE_CLI_COOKIE_SENT
			else if (this.state == ConnectAction.STATE_CLI_WANT_CAPS_SENT)
			{

				SnacPacket reqp = new SnacPacket(SnacPacket.CLI_REQINFO_FAMILY, SnacPacket.CLI_REQINFO_COMMAND,
									SnacPacket.CLI_REQINFO_COMMAND, new byte[0], new byte[0]);
				Icq.c.sendPacket(reqp);

				byte[] rdata = new byte[6];
				Util.putDWord(rdata, 0, 0x000B0002);
				Util.putWord(rdata, 4, 0x000F);
				reqp = new SnacPacket(SnacPacket.CLI_REQLISTS_FAMILY, SnacPacket.CLI_REQLISTS_COMMAND,
							SnacPacket.CLI_REQLISTS_COMMAND, new byte[0], rdata);
				Icq.c.sendPacket(reqp);
				reqp = null;


				// Send a CLI_REQROSTER or
				// CLI_CHECKROSTER packet
				long versionId1 = ContactList.getSsiListLastChangeTime();
				int versionId2 = ContactList.getSsiNumberOfItems();
				if (((versionId1 == -1) && (versionId2 == -1)) || (ContactList.getSize() == 0))
				{
					reqp = new SnacPacket(SnacPacket.CLI_REQROSTER_FAMILY, SnacPacket.CLI_REQROSTER_COMMAND, SnacPacket.CLI_REQROSTER_COMMAND, new byte[0], new byte[0]);
					Icq.c.sendPacket(reqp);
				}
				else
				{
					byte[] data = new byte[6];
					Util.putDWord(data, 0, versionId1);
					Util.putWord(data, 4, versionId2);
					reqp = new SnacPacket(SnacPacket.CLI_CHECKROSTER_FAMILY, SnacPacket.CLI_CHECKROSTER_COMMAND, SnacPacket.CLI_CHECKROSTER_COMMAND, new byte[0], data);
					Icq.c.sendPacket(reqp);
				}
				reqp = null;

				reqp = new SnacPacket(SnacPacket.CLI_REQLOCATION_FAMILY, SnacPacket.CLI_REQLOCATION_COMMAND,
							SnacPacket.CLI_REQLOCATION_COMMAND, new byte[0], new byte[0]);
				Icq.c.sendPacket(reqp);
				reqp = null;

				rdata = new byte[6];
				Util.putDWord(rdata, 0, 0x00050002);
				Util.putWord(rdata, 4, 0x0003);
				reqp = new SnacPacket(SnacPacket.CLI_REQBUDDY_FAMILY, SnacPacket.CLI_REQBUDDY_COMMAND,
							SnacPacket.CLI_REQBUDDY_COMMAND, new byte[0], rdata);
				Icq.c.sendPacket(reqp);
				reqp = null;

				reqp = new SnacPacket(SnacPacket.CLI_REQICBM_FAMILY, SnacPacket.CLI_REQICBM_COMMAND,
							SnacPacket.CLI_REQICBM_COMMAND, new byte[0], new byte[0]);
				Icq.c.sendPacket(reqp);
				reqp = null;

				reqp = new SnacPacket(SnacPacket.CLI_REQBOS_FAMILY, SnacPacket.CLI_REQBOS_COMMAND,
							SnacPacket.CLI_REQBOS_COMMAND, new byte[0], new byte[0]);
				Icq.c.sendPacket(reqp);
				reqp = null;

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
						Vector privacyLists = new Vector(); 

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
							
							int privacyMode = -1;

							// Normal contact 	// Deleted contact
							if (
								(type == 0x0000)
								 || (Options.getBoolean(Options.OPTION_SHOW_DELETED_CONT) &&
								((type == 0x0019) || (type == 0x001B)))
							)
							{
								ByteArrayOutputStream serverData = new ByteArrayOutputStream();
								
								// Get nick
								String nick = new String(name);
								//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
								byte[] biHash = new byte[16];
								//#sijapp cond.end#
								
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
									
									/* Server-side additional data */
									else if ((tlvType == 0x006D) || (tlvType == 0x015c) || (tlvType == 0x015d))
									{
										Util.writeWord(serverData, tlvType, true);
										Util.writeWord(serverData, tlvData.length, true);
										Util.writeByteArray(serverData, tlvData);
									}
									
									len -= 4;
									len -= tlvData.length;
									marker += 4 + tlvData.length;
								}
								if (len != 0) { throw (new JimmException(115, 5)); }

								// Add this contact item to the vector
								try
								{
									ContactItem item = new ContactItem(id, group, name, nick, noAuth, true);
									if (group == 0) item.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP|ContactItem.CONTACTITEM_IS_PHANTOM, true); 
									item.setBytesArray(ContactItem.CONTACTITEM_SS_DATA, (serverData.size() != 0) ? serverData.toByteArray() : null);
									//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
									item.setBytesArray(ContactItem.CONTACTITEM_BUDDYICON_HASH, (biHash.length != 0) ? biHash : null);
									//#sijapp cond.end#
									items.addElement(item);
								}
								catch (NumberFormatException ne)
								{
									// Contact with wrong uin was received  
								}
								catch (Exception e)
								{
									// Contact with wrong uin was received  
								}

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
								} else if (Options.getBoolean(Options.OPTION_SHOW_DELETED_CONT)) {
									items.addElement(new ContactListGroupItem(group,
											ResourceBundle.getString("phantoms")));
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
										Icq.setVisibility (Util.getWord(buf, marker + 4));
									}

									len -= 4;
									len -= tlvData.length;
									marker += 4 + tlvData.length;
								}
								if (len != 0) { throw (new JimmException(115, 111)); }
							}
							
							// visible. item
							else if (type == 0x0002) privacyMode = ContactItem.CONTACTITEM_VIS_ID;
							
							// invis.
							else if (type == 0x0003) privacyMode = ContactItem.CONTACTITEM_INV_ID;
							
							// ignore
							else if (type == 0x000E) privacyMode = ContactItem.CONTACTITEM_IGN_ID;
							
							// All other item types
							else
							{
								// Skip TLVs
								marker += len;
							}
							
							if (privacyMode != -1)
							{
								privacyLists.addElement(name);
								privacyLists.addElement(new int[] { privacyMode, id });
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
						
						// Save values into contact list
						ContactList.update(timestamp, count, itemsAsArray, privacyLists);
						
						if (timestamp != 0) this.srvReplyRosterRcvd = true;

						// Packet has been consumed
						consumed = true;
					}

					// Check if all required packets have been received
					if (this.srvReplyRosterRcvd)
					{
						// Send a CLI_ROSTERACK packet
						Icq.c.sendPacket(new SnacPacket(SnacPacket.CLI_ROSTERACK_FAMILY, SnacPacket.CLI_ROSTERACK_COMMAND, 0x00000007, new byte[0], new byte[0]));
			
						// Send CLI_SETUSERINFO packet
						Icq.sendUserUnfoPacket();
					    
						byte[] tmp_packet;

						/* TODO: make better :) */
						// Send a CLI_SETICBM packet
						SnacPacket reply;
						
						//#sijapp cond.if target isnot "DEFAULT"#
						if (Options.getInt(Options.OPTION_TYPING_MODE) > 0)
						{
							reply = new SnacPacket(SnacPacket.CLI_SETICBM_FAMILY, SnacPacket.CLI_SETICBM_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_SETICBM_DATA);
						}
						else
						{
							//#sijapp cond.end#
							tmp_packet = ConnectAction.CLI_SETICBM_DATA;
							tmp_packet[5] = 0x03;
							reply = new SnacPacket(SnacPacket.CLI_SETICBM_FAMILY, SnacPacket.CLI_SETICBM_COMMAND, 0x00000000, new byte[0], tmp_packet);
							//#sijapp cond.if target isnot "DEFAULT"#
						}
						//#sijapp cond.end#
						Icq.c.sendPacket(reply);

						// Send a client status packet
						Icq.setOnlineStatus(
							(int)Options.getLong(Options.OPTION_ONLINE_STATUS), 
							Options.getInt(Options.OPTION_XSTATUS), 
							true
						);

						// Move to next state
						this.state = ConnectAction.STATE_CLI_STATUS_INFO_SENT;
						
						MainThread.resetLoginTimer();
					}
				}
			}
            
            // STATE_CLI_STATUS_INFO_SENT
			else if (this.state == ConnectAction.STATE_CLI_STATUS_INFO_SENT)
			{
				// Send a CLI_READY packet
				SnacPacket reply2 = new SnacPacket(SnacPacket.CLI_READY_FAMILY, SnacPacket.CLI_READY_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_READY_DATA);
				Icq.c.sendPacket(reply2);

				// Send a CLI_TOICQSRV/CLI_REQOFFLINEMSGS packet
				ToIcqSrvPacket reply3 = new ToIcqSrvPacket(0x00000000, this.uin, ToIcqSrvPacket.CLI_REQOFFLINEMSGS_SUBCMD, new byte[0], new byte[0]);
				Icq.c.sendPacket(reply3);
				
				// Move to STATE_CONNECTED
				Icq.setConnected();
				this.state = ConnectAction.STATE_CONNECTED;
			}
            
            // Update activity timestamp and reset activity flag
            this.lastActivity = System.currentTimeMillis();
            this.active = false;

            // Return consumption flag
            return (consumed);

        }
        // Catch JimmExceptions
        catch (JimmException e)
        {

            // Update activity timestamp and reset activity flag
            this.lastActivity = System.currentTimeMillis();
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
        return (this.state == ConnectAction.STATE_CONNECTED);
    }

    // Returns true if an error has occured
    public boolean isError()
    {
    	if (
    		(this.state != ConnectAction.STATE_ERROR) && 
    		!this.active && 
    		(this.lastActivity + this.TIMEOUT < System.currentTimeMillis())
    	)
		{
			this.canceled_by_timeout = true;
			this.state = ConnectAction.STATE_ERROR;
		}
        return (this.state == ConnectAction.STATE_ERROR);
    }

    // Returns a number between 0 and 100 (inclusive) which indicates the current progress
    public int getProgress()
    {
    	return (state > 0) ? 100*state/STATE_MAX : 0;
    }
    
    public void onEvent(int eventType)
	{
		switch (eventType)
		{
		case ON_COMPLETE:
			MainThread.resetLoginTimer();
			MainThread.activateContactListMT(null);
			Options.safeSave(); // Save last server
			break;
		
		case ON_CANCEL:
			Icq.disconnect(false);
			canceled = true;
			Icq.reconnect_attempts = 0;
			MainThread.backToLastScreenMT();
			break;
			
		case ON_ERROR:
    		if (canceled_by_timeout)
    		{
    			JimmException e = new JimmException(118, 0);
    			JimmException.handleException(e);
    		}
			break;
		}
	}

}