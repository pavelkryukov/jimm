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
 File: src/jimm/comm/SetStatusAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;

import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.ContactList;


public class SetOnlineStatusAction extends Action
{


	// CLI_SETSTATUS packet data
	public static final byte[] CLI_SETSTATUS_DATA = ConnectAction.CLI_SETSTATUS_DATA;


	/****************************************************************************/


	// Requested online status
	private int onlineStatus;


	// Constructor
	public SetOnlineStatusAction(int onlineStatus)
	{
		super(false, true);
		this.onlineStatus = onlineStatus;
	}

	// Init action
	protected void init() throws JimmException
	{

		// Convert online status
		int onlineStatus = Util.translateStatusSend(this.onlineStatus);

		int visibilityItemId = Options.getInt(Options.OPTION_VISIBILITY_ID);
		byte[] buf = new byte[15];
		byte bCode = 0;
		if(visibilityItemId != 0)
		{
			// Build packet for privacy setting changing
			int marker = 0;

			if(onlineStatus == Util.SET_STATUS_INVISIBLE)
				bCode = (this.onlineStatus == ContactList.STATUS_INVIS_ALL)?(byte)2:(byte)3;
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
		Util.putDWord(SetOnlineStatusAction.CLI_SETSTATUS_DATA, 4, 0x10000000+onlineStatus);
		SnacPacket packet = new SnacPacket(SnacPacket.CLI_SETSTATUS_FAMILY,
										   SnacPacket.CLI_SETSTATUS_COMMAND,
										   0x00000000,
										   new byte[0],
										   SetOnlineStatusAction.CLI_SETSTATUS_DATA);
		Jimm.jimm.getIcqRef().c.sendPacket(packet);

		// Change privacy setting according to new status
		if(visibilityItemId != 0 && onlineStatus != Util.SET_STATUS_INVISIBLE)
		{
			SnacPacket reply2post = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
										SnacPacket.CLI_ROSTERUPDATE_COMMAND,
										SnacPacket.CLI_ROSTERUPDATE_COMMAND,
										new byte[0],
										buf);
			this.icq.c.sendPacket(reply2post);
		}
		
		// Save new online status
		Options.setLong(Options.OPTION_ONLINE_STATUS, this.onlineStatus);
		try
		{
			Options.save();
		}
		catch (Exception e)
		{
			JimmException.handleException(new JimmException(172,0,true));
		}

	}


	// Forwards received packet, returns true if packet was consumed
	protected boolean forward(Packet packet) throws JimmException
	{
		return (false);
	}


	// Returns true if the action is completed
	public boolean isCompleted()
	{
		return (true);
	}


	// Returns true if an error has occured
	public boolean isError()
	{
		return (false);
	}


}
