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
 File: src/jimm/comm/UpdateContactListAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import java.util.Date;
import java.util.Vector;

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.Jimm;
import jimm.JimmException;


public class UpdateContactListAction extends Action
{


	// CURRENTLY, ONLY CONTACT ITEM REMOVAL IS SUPPORTED!


	// Action states
	public static final int STATE_ERROR = -1;
	public static final int STATE_CLI_ROSTERDELETE_SENT = 1;
	public static final int STATE_CLI_ADDEND_SENT = 2;
	public static final int STATE_SRV_UPDATEACK_RCVD = 3;


	// Timeout
	public static final int TIMEOUT = 10 * 1000; // milliseconds


	/****************************************************************************/


	// Contact item
	private ContactListContactItem cItem;


	// Action state
	private int state;


	// Last activity
	private Date lastActivity = new Date();


	// Constructor (removes given contact item)
	public UpdateContactListAction(ContactListContactItem cItem)
	{
		this.cItem = cItem;
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

		// Send a CLI_ADDSTART packet
		SnacPacket packet1 = new SnacPacket(SnacPacket.CLI_ADDSTART_FAMILY,
											SnacPacket.CLI_ADDSTART_COMMAND,
											0x00000000,
											new byte[0],
											new byte[0]);
		this.icq.c.sendPacket(packet1);

		// Pack CLI_ROSTERDELETE packet
		byte[] uinRaw = Util.stringToByteArray(this.cItem.getUin());
		byte[] nameRaw = Util.stringToByteArray(this.cItem.getName());
		byte[] buf = new byte[2 + uinRaw.length + 8 + 4 + nameRaw.length];
		int marker = 0;
		Util.putWord(buf, marker, uinRaw.length);
		System.arraycopy(uinRaw, 0, buf, marker + 2, uinRaw.length);
		marker += 2 + uinRaw.length;
		Util.putWord(buf, marker, this.cItem.getGroup());
		marker += 2;
		Util.putWord(buf, marker, this.cItem.getId());
		marker += 2;
		Util.putWord(buf, marker, 0x0000);
		marker += 2;
		Util.putWord(buf, marker, 4 + nameRaw.length);
		marker += 2;
		Util.putWord(buf, marker, 0x0131);
		Util.putWord(buf, marker + 2, nameRaw.length);
		System.arraycopy(nameRaw, 0, buf, marker + 4, nameRaw.length);
		marker += 4 + nameRaw.length;

		// Send a CLI_ROSTERDELETE packet
		SnacPacket packet2 = new SnacPacket(SnacPacket.CLI_ROSTERDELETE_FAMILY,
											SnacPacket.CLI_ROSTERDELETE_COMMAND,
											0x00000000,
											new byte[0],
											buf);
		this.icq.c.sendPacket(packet2);

		// Set state
		this.state = UpdateContactListAction.STATE_CLI_ROSTERDELETE_SENT;

	}


	// Forwards received packet, returns true if packet was consumed
	protected boolean forward(Packet packet) throws JimmException
	{

		// Catch JimmExceptions
		try
		{

			// Flag indicates whether packet has been consumed or not
			boolean consumed = false;

			// Watch out for STATE_CLI_ROSTERDELETE_SENT
			if (this.state == UpdateContactListAction.STATE_CLI_ROSTERDELETE_SENT)
			{

				// Watch out for SRV_UPDATEACK packet type
				if (packet instanceof SnacPacket)
				{
					SnacPacket snacPacket = (SnacPacket) packet;
					if ((snacPacket.getFamily() == SnacPacket.SRV_UPDATEACK_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_UPDATEACK_COMMAND))
					{

						// Check error code, see ICQv8 specification
						if (Util.getWord(snacPacket.getData(), 0) != 0x0000)
						{
							throw (new JimmException(154, 0, true));
						}

						// Get all contact items and group items as aray
						ContactListContactItem[] cItems = Jimm.jimm.getContactListRef().getContactItems();
						ContactListGroupItem[] gItems = Jimm.jimm.getContactListRef().getGroupItems();

						// Get group of contact item to be removed
						ContactListGroupItem gItem = null;
						for (int i = 0; i < gItems.length; i++)
						{
							if (gItems[i].getId() == this.cItem.getGroup())
							{
								gItem = gItems[i];
								break;
							}
						}
						if (gItem == null)
						{
							throw (new JimmException(154, 1, true));
						}

						// Get all contact items in this group
						Vector cItemsRemaining = new Vector();
						for (int i = 0; i < cItems.length; i++)
						{
							if ((gItem.getId() == cItems[i].getGroup()) && (this.cItem != cItems[i]))
							{
								cItemsRemaining.addElement(cItems[i]);
							}
						}

						// Pack CLI_ROSTERUPDATE packet
						byte[] nameRaw = Util.stringToByteArray(gItem.getName());
						byte[] buf = new byte[2 + nameRaw.length + 8 + 4 + cItemsRemaining.size() * 2];
						int marker = 0;
						Util.putWord(buf, marker, nameRaw.length);
						System.arraycopy(nameRaw, 0, buf, marker + 2, nameRaw.length);
						marker += 2 + nameRaw.length;
						Util.putWord(buf, marker, gItem.getId());
						marker += 2;
						Util.putWord(buf, marker, 0x0000);
						marker += 2;
						Util.putWord(buf, marker, 0x0001);
						marker += 2;
						Util.putWord(buf, marker, 4 + cItemsRemaining.size() * 2);
						marker += 2;
						Util.putWord(buf, marker, 0x00C8);
						Util.putWord(buf, marker + 2, cItemsRemaining.size() * 2);
						marker += 4;
						for (int i = 0; i < cItemsRemaining.size(); i++)
						{
							Util.putWord(buf, marker, ((ContactListContactItem) cItemsRemaining.elementAt(i)).getId());
							marker += 2;
						}

						// Send CLI_ROSTERUPDATE packet
						SnacPacket packet1 = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
															SnacPacket.CLI_ROSTERUPDATE_COMMAND,
															0x00000000,
															new byte[0],
															buf);
						this.icq.c.sendPacket(packet1);

						// Send a CLI_ADDEND packet
						SnacPacket packet2 = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
															SnacPacket.CLI_ADDEND_COMMAND,
															0x00000000,
															new byte[0],
															new byte[0]);
						this.icq.c.sendPacket(packet2);

						// Move to next state
						this.state = UpdateContactListAction.STATE_CLI_ADDEND_SENT;

						// Packet has been consumed
						consumed = true;

					}
				}

			}
			// Watch out for STATE_CLI_ADDEND_SENT
			else if (this.state == UpdateContactListAction.STATE_CLI_ADDEND_SENT)
			{

				// Watch out for SRV_UPDATEACK packet type
				if (packet instanceof SnacPacket)
				{
					SnacPacket snacPacket = (SnacPacket) packet;
					if ((snacPacket.getFamily() == SnacPacket.SRV_UPDATEACK_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_UPDATEACK_COMMAND))
					{

						// Check error code, see ICQv8 specification
						if (Util.getWord(snacPacket.getData(), 0) != 0x0000)
						{
							throw (new JimmException(154, 0, true));
						}

						// Delete contact item from internal list
						Jimm.jimm.getContactListRef().removeContactItem(this.cItem);

						// Move to next state
						this.state = UpdateContactListAction.STATE_SRV_UPDATEACK_RCVD;

						// Packet has been consumed
						consumed = true;

					}
				}

			}

			// Update activity timestamp
			this.lastActivity = new Date();

			// Return consumption flag
			return (consumed);

		}
				// Catch JimmExceptions
		catch (JimmException e)
		{

			// Update activity timestamp
			this.lastActivity = new Date();

			// Set error state if exception is critical
			if (e.isCritical()) this.state = ConnectAction.STATE_ERROR;

			// Forward exception
			throw (e);

		}

	}


	// Returns true if the action is completed
	public boolean isCompleted()
	{
		return (this.state == UpdateContactListAction.STATE_SRV_UPDATEACK_RCVD);
	}


	// Returns true if an error has occured
	public boolean isError()
	{
		if ((this.state != ConnectAction.STATE_ERROR) && (this.lastActivity.getTime() + UpdateContactListAction.TIMEOUT < System.currentTimeMillis()))
		{
			JimmException.handleException(new JimmException(154, 3));
			this.state = ConnectAction.STATE_ERROR;
		}
		return (this.state == ConnectAction.STATE_ERROR);
	}


}
