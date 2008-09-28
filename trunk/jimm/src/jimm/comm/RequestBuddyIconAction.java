/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-08  Jimm Project

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
 File: src/jimm/comm/RequestBuddyIconAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Ivan Mikitevich
 *******************************************************************************/
//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
package jimm.comm;

import java.util.*;
import javax.microedition.lcdui.Image;

import jimm.JimmException;
import jimm.DebugLog;
import jimm.JimmUI;
import jimm.RunnableImpl;
import jimm.util.ResourceBundle;
import jimm.comm.connections.SOCKETConnection;

public class RequestBuddyIconAction extends Action
{
    // Action states
    public static final int STATE_ERROR = -1;
    public static final int STATE_INIT_DONE = 0;
    public static final int STATE_CONNECTION_ESTB = 1;
    public static final int STATE_CLI_COOKIE_SENT = 2;
    public static final int STATE_CLI_READY_SENT = 3;
    public static final int STATE_CLI_REQBUDDYICON_SENT = 4;
    public static final int STATE_ACTION_DONE = 5;

    public static final int STATE_MAX = 5;

    // Timeout
    public int TIMEOUT = 30 * 1000; // milliseconds

    /** *********************************************************************** */

    // Server host
    private String srvHost;
    // Server port
    private String srvPort;

    private String uin;

    private byte[] biHash;

    // Client cookie
    private byte[] clicookie;

    // Action state
    private int state;

    // Last activity
    private Date lastActivity = new Date();
    private boolean active;

    // Constructor
    public RequestBuddyIconAction (String uin, byte[] biHash)
    {
    	super(false, true);
		this.uin = uin;
		this.biHash = biHash;
    }

    // Init action
    protected void init() throws JimmException
    {

		if (Icq.bartC != null)
		{
			if (Icq.bartC.getState())
			{
				this.sendBuddyIconRequest();
		        // Set STATE_CLI_REQBUDDYICON_SENT
		    	this.state = RequestBuddyIconAction.STATE_CLI_REQBUDDYICON_SENT;
		        // Update activity timestamp
		        this.lastActivity = new Date();
				return;
			}
		}

		try
		{
			// Request BART server addr
			byte[] rbuf = new byte[2];
			Util.putWord(rbuf, 0, 0x0010);
			Packet reqp = new SnacPacket(SnacPacket.CLI_SERVICEREQUEST_FAMILY, SnacPacket.CLI_SERVICEREQUEST_COMMAND, 0x00010004, new byte[0], rbuf);
			Icq.c.sendPacket(reqp);
			reqp = null;
			this.state = RequestBuddyIconAction.STATE_INIT_DONE;
		}
		catch (JimmException je)
		{
			this.state = RequestBuddyIconAction.STATE_ERROR;
			throw (new JimmException (100, 50, true));
		}

        // Update activity timestamp
        this.lastActivity = new Date();

    }

    private void sendBuddyIconRequest() throws JimmException
    {
		byte[] uinRaw = Util.stringToByteArray(this.uin);
		int uinLength = uinRaw.length;
		byte[] buf = new byte[1 + uinLength +1+2+1+1+16];
	
		Util.putByte(buf, 0, uinLength);
	
		System.arraycopy(uinRaw, 0, buf, 1, uinLength);
	
		Util.putByte(buf, 1+uinLength, 0x01);
		Util.putWord(buf, 2+uinLength, 0x0001);
		Util.putByte(buf, 4+uinLength, 0x01);
		Util.putByte(buf, 5+uinLength, 0x10);
	
		System.arraycopy(biHash, 0, buf, 6+uinLength, 16);
		SnacPacket request = new SnacPacket(0x0010, 0x0006, 0x0006, new byte[0], buf);
		try
		{
			Icq.bartC.sendPacket(request);
		}
		catch (JimmException je)
		{
			this.state = RequestBuddyIconAction.STATE_ERROR;
			throw (new JimmException (100, 53, true));
		}
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

		    switch (this.state)
		    {
			// Watch out for STATE_INIT_DONE
			case RequestBuddyIconAction.STATE_INIT_DONE:
			    // Watch out for SNAC packet
			    if (packet instanceof SnacPacket)
			    {
					SnacPacket snacPacket = (SnacPacket) packet;
					if ((snacPacket.getFamily() == SnacPacket.SRV_REDIRECT_FAMILY)
						&& (snacPacket.getCommand() == SnacPacket.SRV_REDIRECT_COMMAND))
					{
						// Get data
						byte[] buf = snacPacket.getData();
						int marker = 0;
						for (int i = 0; i < 3; i++)
						{
							int tlvType = Util.getWord(buf, marker);
							byte[] tlvData = Util.getTlv(buf, marker);
							switch (tlvType)
							{
								case 0x0005 : // BART server
									this.srvHost = Util.byteArrayToString(tlvData);
									this.srvPort = "5190";
									break;
								case 0x0006 : // client cookie
									this.clicookie = tlvData;
									break;
							}
							marker += 2 + 2 + tlvData.length;
						}
					}
					// Check parameters
					if (this.clicookie.length == 0)
					{
						this.state = RequestBuddyIconAction.STATE_ERROR;
						throw (new JimmException(117, 0, false));
					}
	
					// Open connection
					try
					{
						Icq.bartC = new SOCKETConnection(JimmException.ICQ_BART);
						Icq.bartC.connect(this.srvHost + ":" + this.srvPort);
		            }
					catch (JimmException e)
					{
						DebugLog.addText (e.getMessage());
						this.state = RequestBuddyIconAction.STATE_ERROR;
						throw (new JimmException (100, 51, true));
					}
					catch (Exception e)
					{
						DebugLog.addText (e.toString());
						this.state = RequestBuddyIconAction.STATE_ERROR;
						throw (new JimmException (100, 52, true));
					}
					// Set STATE_CONNECTION_ESTB
					this.state = RequestBuddyIconAction.STATE_CONNECTION_ESTB;
					// Packet has been consumed
					consumed = true;
			    }	
				break;
	
			// Watch out for STATE_CONNECTION_ESTB
			case RequestBuddyIconAction.STATE_CONNECTION_ESTB:
			    // Watch out for SRV_CLI_HELLO packet
			    if (packet instanceof ConnectPacket)
			    {
					ConnectPacket connectPacket = (ConnectPacket) packet;
					if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
					{
					    // Send a CLI_IDENT packet as reply
				    	ConnectPacket reply = new ConnectPacket(this.clicookie);
				    	Icq.bartC.sendPacket(reply);
					}
					// Move to next state
					this.state = RequestBuddyIconAction.STATE_CLI_COOKIE_SENT;
					// Packet has been consumed
					consumed = true;
			    }
			    break;
	
			// Watch out for STATE_CLI_COOKIE_SENT
			case RequestBuddyIconAction.STATE_CLI_COOKIE_SENT:
			    // Watch out for SNAC packet
			    if (packet instanceof SnacPacket)
			    {
					SnacPacket snacPacket = (SnacPacket) packet;
	
					// Send a CLI_READY packet
					SnacPacket reply = new SnacPacket(SnacPacket.CLI_READY_FAMILY, SnacPacket.CLI_READY_COMMAND, 0x00000000, new byte[0], ConnectAction.CLI_READY_DATA);
					Icq.bartC.sendPacket(reply);
	
					this.sendBuddyIconRequest();
					// Move to next state
					this.state = RequestBuddyIconAction.STATE_CLI_REQBUDDYICON_SENT;
					// Packet has been consumed
					consumed = true;
			    }
			    break;
	
			// Watch out for STATE_CLI_REQBUDDYICON_SENT
			case RequestBuddyIconAction.STATE_CLI_REQBUDDYICON_SENT:
			    // Watch out for SNAC packet
			    if (packet instanceof SnacPacket)
			    {
				    SnacPacket snacPacket = (SnacPacket) packet;
				    if ((snacPacket.getFamily() == SnacPacket.SRV_REPLYAVATAR_FAMILY)
						    && (snacPacket.getCommand() == SnacPacket.SRV_REPLYAVATAR_COMMAND))
				    {
					    // Get data
					    byte[] buf = snacPacket.getData();
	
					    int marker = 0;
					    int uinLength = Util.getByte(buf, marker);
					    marker += 1;
	
					    String uin = Util.byteArrayToString(buf, marker, uinLength);
	
					    marker += uinLength;
					    marker += 2 + 1 + 1 + 16 + 1 + 2 + 1 + 1 + 16;
	
					    int imgLength = Util.getWord(buf, marker);
					    marker += 2;
					    byte[] iconRaw = new byte[imgLength];
					    System.arraycopy(buf, marker, iconRaw, 0, imgLength);
					    Image av = null;
					    try {
							av = Image.createImage (iconRaw, 0, iconRaw.length);
					    } catch (Exception ignore) {/* Do nothing */}
					    if (av != null) {
							RunnableImpl.updateBuddyIcon (uin, av, biHash);
							av = null;
					    }
					    iconRaw = null;
					    buf = null;
	
					    // Move to next state
					    this.state = RequestBuddyIconAction.STATE_ACTION_DONE;
					    // Packet has been consumed
					    consumed = true;
				    }
			    }
			    break;
		    }
	
			// Update activity timestamp and reset activity flag
			if (consumed) this.lastActivity = new Date();
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

            this.state = RequestBuddyIconAction.STATE_ERROR;

            // Forward exception
            throw (e);
        }

    }

    // Returns true if the action is completed
    public boolean isCompleted()
    {
        return (this.state == RequestBuddyIconAction.STATE_ACTION_DONE);
    }

    // Returns true if an error has occured
    public boolean isError()
    {
    	if ((this.state != RequestBuddyIconAction.STATE_ERROR) && !this.active && (this.lastActivity.getTime() + this.TIMEOUT < System.currentTimeMillis()))
        {
            this.state = RequestBuddyIconAction.STATE_ERROR;
        }
        return (this.state == RequestBuddyIconAction.STATE_ERROR);
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
				DebugLog.addText ("RequestBuddyIconAction ON_COMPLETE");
				JimmUI.updateActiveUserInfo(this.uin);
	    		break;
	    	case ON_CANCEL:
	    	case ON_ERROR:
				DebugLog.addText ("RequestBuddyIconAction ON_ERROR");
				Icq.disconnectBart(true);
	    		break;
		}
    }
}
//#sijapp cond.end#