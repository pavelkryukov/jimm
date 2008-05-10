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
 File: src/jimm/comm/RegisterNewUinAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Ivan Mikitevich
 *******************************************************************************/

package jimm.comm;

import java.util.*;
import java.io.*;
import javax.microedition.lcdui.Image;

import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.TimerTasks;

public class RegisterNewUinAction extends Action
{
    // Action states
    public static final int STATE_ERROR = -1;
    public static final int STATE_INIT_DONE = 0;
    public static final int STATE_CAPTCHA_REQUESTED = 1;
    public static final int STATE_WAITING_FOR_CONFIRM = 2;
    public static final int STATE_WELL_DONE = 3;
    
    public static final int STATE_MAX = 3;

    // Timeout
    public int TIMEOUT = 60 * 1000; // milliseconds

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

    // Constructor
    public RegisterNewUinAction(String password, String srvHost, String srvPort)
    {
    	super(true, false);
        this.password = password;
        this.srvHost = srvHost;
        this.srvPort = srvPort;
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

    public static void addTimerTask(Action action)
    {
	TimerTasks timerTask = new TimerTasks(action);
	Jimm.getTimerRef().schedule(timerTask, 1000, 1000);
    }

    // Send new uin registration request
    public static void requestRegistration (String password, String captcha) throws JimmException
    {
	if (captcha.length() == 0)
		return;

	byte[] paswdRaw = Util.stringToByteArray(password);
	byte[] codeRaw = Util.stringToByteArray(captcha);

	int regCookie = 0;

	// Byte array for the packet
	int wTlvLen1 = 4 + 51 + paswdRaw.length;
	int wTlvLen2 = 4 + codeRaw.length;
	byte[] rbuf = new byte[wTlvLen1 + wTlvLen2];

	int marker = 0;

	//TLV Type and length
	Util.putWord(rbuf, marker, 0x0001);
	Util.putWord(rbuf, marker + 2, wTlvLen1 - 4);
	marker += 4;

	Util.putDWord(rbuf, marker, 0x00000000);
	marker += 4;
        
	Util.putWord(rbuf, marker, 0x2800);
	Util.putWord(rbuf, marker + 2, 0x0000);
	marker += 4;

	Util.putDWord(rbuf, marker, 0x00000000);
	Util.putDWord(rbuf, marker + 4, 0x00000000);
	marker += 8;

	Util.putDWord(rbuf, marker, regCookie);
	Util.putDWord(rbuf, marker + 4, regCookie);
	marker += 8;

	Util.putDWord(rbuf, marker, 0x00000000);
	Util.putDWord(rbuf, marker + 4, 0x00000000);
	Util.putDWord(rbuf, marker + 8, 0x00000000);
	Util.putDWord(rbuf, marker + 12, 0x00000000);
	marker += 16;

	Util.putWord(rbuf, marker, paswdRaw.length+1, false);
	System.arraycopy(paswdRaw, 0, rbuf, marker + 2, paswdRaw.length);
	marker += 2 + paswdRaw.length;
	Util.putByte(rbuf, marker, 0);
	marker += 1;

	Util.putDWord(rbuf, marker, regCookie);
	marker += 4;

	Util.putDWord(rbuf, marker, 0xE3070000);
	marker += 4;
		
	Util.putWord(rbuf, marker, 0x0009);
	Util.putWord(rbuf, marker+2, wTlvLen2 - 4, true);
	marker += 4;
	System.arraycopy(codeRaw, 0, rbuf, marker, codeRaw.length);
	try {
		Icq.c.sendPacket(new SnacPacket(0x0017, 0x0004, 0x0004, new byte[0], rbuf));
	} catch (Exception e) {
		throw (new JimmException(231, 0));
	}
    }


    // Init action
    protected void init() throws JimmException
    {
 
        // Check parameters
        if (this.password.length() == 0)
        {
            this.state = RegisterNewUinAction.STATE_ERROR;
            return;
        }

        // Open connection
	try
            {
            	Icq.c.connect(this.srvHost + ":" + this.srvPort);
            } catch (JimmException e)
            {
		this.state = RegisterNewUinAction.STATE_ERROR;
		throw (e);
            }

        // Set STATE_INIT
        this.state = RegisterNewUinAction.STATE_INIT_DONE;

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

		switch (this.state) {
			case RegisterNewUinAction.STATE_INIT_DONE :
        		        // Watch out for SRV_CLI_HELLO packet
		                if (packet instanceof ConnectPacket)
		                {
		                    ConnectPacket connectPacket = (ConnectPacket) packet;
		                    if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
		                    {
					// Send CLI_HELLO packet
					Icq.c.sendPacket(new ConnectPacket());

					// Request CAPTCHA image
					byte[] buf = new byte[8];
					Util.putDWord(buf, 0, 0x00010000);
					Util.putDWord(buf, 4, 0x00000000);
					Icq.c.sendPacket(new SnacPacket(0x0017, 0x000C, 0x000C, new byte[0], buf));

		                        // Move to next state
		                        this.state = RegisterNewUinAction.STATE_CAPTCHA_REQUESTED;
		                        // Packet has been consumed
		                        consumed = true;
		                    }
		                }
				break;
			case RegisterNewUinAction.STATE_CAPTCHA_REQUESTED :
				if (packet instanceof SnacPacket) {
					SnacPacket snacPacket = (SnacPacket)packet;
					if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x000d)) {
						byte[] rbuf = snacPacket.getData();
						
						int marker = 0;
						while (marker < rbuf.length) {
							byte[] tlvData = Util.getTlv(rbuf, marker);
							int tlvType = Util.getWord(rbuf, marker);
							marker += 4 + tlvData.length;
							switch (tlvType) {
								case 0x0001:
									String imgtype = Util.byteArrayToString(tlvData);
									System.out.println ("captcha image type: " + imgtype);
									break;
								case 0x0002:
									Options.setCaptchaImage(Image.createImage(tlvData, 0, tlvData.length));
									break;
							}
						}

						Icq.setConnected();
			                        // Move to next state
			                        this.state = RegisterNewUinAction.STATE_WAITING_FOR_CONFIRM;
			                        // Packet has been consumed
		        	                consumed = true;
					}
				}
				break;
			case RegisterNewUinAction.STATE_WAITING_FOR_CONFIRM :
				if (packet instanceof SnacPacket) {
					SnacPacket snacPacket = (SnacPacket)packet;
					if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x0005)) {
						// Get data
						byte[] buf = snacPacket.getData();

						// Read the important data from the packet
						int marker = 0;
						if (Util.getWord(buf, marker) != 0x0001) {
							this.state = RegisterNewUinAction.STATE_ERROR;
							throw (new JimmException(232, 0, true));
						}
						marker += 10;
						if (Util.getWord(buf, marker) != 0x2d00) {
							this.state = RegisterNewUinAction.STATE_ERROR;
							throw (new JimmException(232, 1, true));
						}
						marker += 36;
                        
						// Get the UIN as a long in Little Endian and save it in a string
						long uinL = Util.getDWord(buf, marker, false);

						this.uin = String.valueOf(uinL);
                        
			                        // Move to next state
			                        this.state = RegisterNewUinAction.STATE_WELL_DONE;
			                        // Packet has been consumed
		        	                consumed = true;
					} else
					if ((snacPacket.getFamily() == 0x0017) && (snacPacket.getCommand() == 0x0001)) {
                        
			                        // Move to next state
			                        this.state = RegisterNewUinAction.STATE_ERROR;
			                        // Packet has been consumed
		        	                consumed = true;
						throw (new JimmException(230, 0));
					}
				}
				break;
			default :
				// watch out for channel 4 packet
				if (packet instanceof DisconnectPacket) {
					DisconnectPacket disconnectPacket = (DisconnectPacket) packet;
					// Watch out for SRV_GOODBYE packet
					if (disconnectPacket.getType() == DisconnectPacket.TYPE_SRV_GOODBYE) {
						// Send CLI_GOODBYE packet
//						Icq.c.sendPacket(new DisconnectPacket());
					}
					consumed = true;
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
		if (e.isCritical()) this.state = RegisterNewUinAction.STATE_ERROR;

		// Forward exception
		throw (e);
        }

    }

    // Returns true if the action is completed
    public boolean isCompleted()
    {
        return (this.state == RegisterNewUinAction.STATE_WELL_DONE);
    }

    // Returns true if an error has occured
    public boolean isError()
    {
//    	if ((this.state != RegisterNewUinAction.STATE_ERROR) && !this.active && (this.lastActivity.getTime() + this.TIMEOUT < System.currentTimeMillis()))
//        {
//    		JimmException e = new JimmException(119, 0);
//		JimmException.handleException(e);
//		this.state = RegisterNewUinAction.STATE_ERROR;
//        }
        return (this.state == RegisterNewUinAction.STATE_ERROR);
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
		System.out.println ("new uin: " + this.getUin());
		System.out.println ("password: " + this.getPassword());
		Options.submitNewUinPassword (this.getUin(), this.getPassword());
    		Icq.disconnect();
    		break;
    		
    	case ON_CANCEL:
    		Icq.disconnect();
    		break;
    	}
    }
}
