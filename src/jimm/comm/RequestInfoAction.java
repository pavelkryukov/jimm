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
 File: src/jimm/comm/RequestInfoAction.java
 Version: 0.3.1  Date: 2004/12/25
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import java.util.Date;

import jimm.JimmException;


public class RequestInfoAction extends Action
{


	// Receive timeout
	private static final int TIMEOUT = 7 * 1000; // milliseconds


	/****************************************************************************/


	// UIN of the user to request information about
	private String uin;


	// Information about the user
	private String nick;      // = null;
	private String firstName; // = null;
	private String lastName;  // = null;
	private String email;     // = null;


	// Date of init
	private Date init;


	// Constructor
	public RequestInfoAction(String uin)
	{
		this.uin = new String(uin);
	}


	// Returns the UIN of the user
	public String getUin()
	{
		return (new String(this.uin));
	}


	// Returns the nick of the user
	public String getNick()
	{
		return (new String(this.nick));
	}


	// Returns the first name of the user
	public String getFirstName()
	{
		return (new String(this.firstName));
	}


	// Returns the last name of the user
	public String getLastName()
	{
		return (new String(this.lastName));
	}


	// Returns the email address of the user
	public String getEmail()
	{
		return (new String(this.email));
	}


	// Returns true if the action can be performed
	public boolean isExecutable()
	{
		return (this.icq.isConnected());
	}


	// Returns true if this is an exclusive command
	public boolean isExclusive()
	{
		return (false);
	}


	// Init action
	protected void init() throws JimmException
	{

		// Send a CLI_METAREQINFO packet
		byte[] buf = new byte[6];
		Util.putWord(buf, 0, ToIcqSrvPacket.CLI_META_REQMOREINFO_TYPE, false);
		Util.putDWord(buf, 2, Long.parseLong(this.uin), false);
		ToIcqSrvPacket packet = new ToIcqSrvPacket(0, this.icq.getUin(), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], buf);
		this.icq.c.sendPacket(packet);

		// Save date
		this.init = new Date();

	}


	// Forwards received packet, returns true if packet was consumed
	protected synchronized boolean forward(Packet packet) throws JimmException
	{

		// Watch out for SRV_FROMICQSRV packet
		if (packet instanceof FromIcqSrvPacket)
		{
			FromIcqSrvPacket fromIcqSrvPacket = (FromIcqSrvPacket) packet;

			// Watch out for SRV_META packet
			if (fromIcqSrvPacket.getSubcommand() == FromIcqSrvPacket.SRV_META_SUBCMD)
			{

				// Get packet data
				byte[] data = fromIcqSrvPacket.getData();

				// Watch out for SRV_METAGENERAL packet
				if (Util.getWord(data, 0, false) == FromIcqSrvPacket.SRV_META_GENERAL_TYPE)
				{

					// Get meta information
					int marker = 3;
					int len = Util.getWord(data, marker, false);
					this.nick = Util.byteArrayToString(data, marker + 2, len);
					marker += 2 + len;
					len = Util.getWord(data, marker, false);
					this.firstName = Util.byteArrayToString(data, marker + 2, len);
					marker += 2 + len;
					len = Util.getWord(data, marker, false);
					this.lastName = Util.byteArrayToString(data, marker + 2, len);
					marker += 2 + len;
					len = Util.getWord(data, marker, false);
					this.email = Util.byteArrayToString(data, marker + 2, len);
					marker += 2 + len;

					// Packet has been consumed
					return (true);

				}

			}

		}

		// Packet has not been consumed
		return (false);

	}


	// Returns true if the action is completed
	public synchronized boolean isCompleted()
	{
		return ((this.nick != null) && (this.firstName != null) && (this.lastName != null) && (this.email != null));
	}


	// Returns true if an error has occured
	public synchronized boolean isError()
	{
		return (this.init.getTime() + RequestInfoAction.TIMEOUT < System.currentTimeMillis());
	}


}
