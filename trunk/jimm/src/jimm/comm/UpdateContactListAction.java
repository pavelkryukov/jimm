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
 File: src/jimm/comm/UpdateContactListAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import jimm.ContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactListItem;
import jimm.ContactList;
import jimm.JimmException;
import jimm.RunnableImpl;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class UpdateContactListAction extends Action
{

	/* Action states */
	private static final int STATE_ERROR = -1;

	private static final int STATE_CLI_ROSTERMODIFY_SENT = 1;
	private static final int STATE_COMPLETED = 2;
	private static final int STATE_MOVE1 = 3;
	private static final int STATE_MOVE2 = 4;
	private static final int STATE_ADD1 = 5;
	private static final int STATE_ADD2 = 6;
	private static final int STATE_DELETE_CONTACT1 = 7;
	private static final int STATE_DELETE_CONTACT2 = 8;
	private static final int STATE_DELETE_GROUP1 = 9;
	private static final int STATE_DELETE_GROUP2 = 10;
	private static final int STATE_ADD_GROUP1 = 11;
	private static final int STATE_ADD_GROUP2 = 12;

	/* Action types */
	public static final int ACTION_ADD        = 1;
	public static final int ACTION_DEL        = 2;
	public static final int ACTION_RENAME     = 3;
	public static final int ACTION_MOVE       = 4;
	public static final int ACTION_REQ_AUTH   = 5;
	public static final int ACTION_PRIV_LISTS = 6;

	/* Timeout */
	public static final int TIMEOUT = 10 * 1000; // milliseconds

	/** ************************************************************************* */

	/* Contact item */
	private ContactItem cItem;

	/* Group item */
	private ContactListGroupItem gItem, newGItem;

	/* Action state */
	private int state;

	/* Action type */
	private int action;

	/* Type of error happend */
	private int errorCode;

	/* Last activity */
	private long lastActivity = System.currentTimeMillis();

	/* Constructor (removes or adds given contact item) */
	public UpdateContactListAction(ContactListItem cItem, int _action)
	{
		super(false, true);
		this.action = _action;
		if (cItem instanceof ContactItem)
		{
			this.cItem = (ContactItem) cItem;
			this.gItem = null;
		} else
		{
			this.cItem = null;
			this.gItem = (ContactListGroupItem) cItem;
		}
	}

	public UpdateContactListAction(ContactItem cItem, ContactListGroupItem oldGroup, ContactListGroupItem newGroup)
	{
		super(false, true);
		this.cItem = cItem;
		this.gItem = oldGroup;
		this.newGItem = newGroup;
		action = ACTION_MOVE;
	}
	
	public UpdateContactListAction(Vector privacyChanges)
	{
		super(false, true);
	}


	/* Init action */
	protected void init() throws JimmException
	{
		SnacPacket packet;
		byte[] buf = null;

		if (this.action != ACTION_RENAME)
		{
			/* Send a CLI_ADDSTART packet */
			Icq.sendCLI_ADDSTART();
		}

		switch (action)
		{
		/* Send a CLI_ROSTERUPDATE packet */
		case ACTION_RENAME:
			if (cItem != null)
				buf = packRosterItem(cItem, 0);
			else if (gItem != null)
				buf = packRosterItem(gItem);
			else
			{
			    this.state = UpdateContactListAction.STATE_ERROR;
			    break;
			}

			packet = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
					SnacPacket.CLI_ROSTERUPDATE_COMMAND, Util.getCounter(),
					new byte[0], buf);
			Icq.c.sendPacket(packet);
			this.state = UpdateContactListAction.STATE_CLI_ROSTERMODIFY_SENT;
			break;

		/* Send CLI_ROSTERADDpacket */
		case ACTION_ADD:
		case ACTION_REQ_AUTH:
			if (cItem != null)
			{
				int groupId = cItem
						.getIntValue(ContactItem.CONTACTITEM_GROUP);
				gItem = ContactList.getGroupById(groupId);
				cItem.setIntValue(ContactItem.CONTACTITEM_ID, ContactList.generateNewIdForBuddy());
				RunnableImpl.addContact(cItem);
				buf = packRosterItem(cItem, groupId);
				if (action == ACTION_REQ_AUTH)
					state = STATE_ADD1;
				else
					state = STATE_ADD2;
			} else if (gItem != null)
			{
				buf = packRosterItem(gItem);
				state = STATE_ADD_GROUP1;
			} else
			{
			    this.state = UpdateContactListAction.STATE_ERROR;
			    break;
			}
			packet = new SnacPacket(SnacPacket.CLI_ROSTERADD_FAMILY,
					SnacPacket.CLI_ROSTERADD_COMMAND, Util.getCounter(),
					new byte[0], buf);
			Icq.c.sendPacket(packet);
			break;

		/* Send a CLI_ROSTERDELETE packet */
		case ACTION_DEL:
			if (cItem != null)
			{
				buf = packRosterItem(cItem, 0);
				this.state = STATE_DELETE_CONTACT1;
			} else if (gItem != null)
			{
				buf = packRosterItem(gItem);
				this.state = STATE_DELETE_GROUP1;
			} else
			{
			    this.state = UpdateContactListAction.STATE_ERROR;
			    break;
			}

			packet = new SnacPacket(SnacPacket.CLI_ROSTERDELETE_FAMILY,
					SnacPacket.CLI_ROSTERDELETE_COMMAND, Util.getCounter(),
					new byte[0], buf);
			Icq.c.sendPacket(packet);
			break;

        case ACTION_MOVE:
            Icq.c.sendPacket
            (
                 new SnacPacket
                 (
                      SnacPacket.CLI_ROSTERUPDATE_FAMILY,
                      SnacPacket.CLI_ROSTERDELETE_COMMAND,
                      SnacPacket.CLI_ROSTERDELETE_COMMAND|((ContactList.generateNewIdForBuddy() << 16)&0x7FFF0000), 
                      new byte[0],
                      packRosterItem(cItem, gItem.getId())
                 )
            );

            cItem.setIntValue(ContactItem.CONTACTITEM_GROUP, newGItem.getId());
            cItem.setIntValue(ContactItem.CONTACTITEM_ID, ContactList.generateNewIdForBuddy());

            Icq.c.sendPacket
            (
                 new SnacPacket
                 (
                      SnacPacket.CLI_ROSTERUPDATE_FAMILY,
                      SnacPacket.CLI_ROSTERADD_COMMAND,
                      SnacPacket.CLI_ROSTERADD_COMMAND|((ContactList.generateNewIdForBuddy() << 16)&0x7FFF0000),
                      new byte[0],
                      packRosterItem(cItem, newGItem.getId())
                 )
            );
            
            state = STATE_MOVE1;
            
            break;
		}
	}

	private boolean processPaket(Packet packet) throws JimmException
	{
		/* Flag indicates whether packet has been consumed or not */
		boolean consumed = false;

		/* Watch out for SRV_UPDATEACK packet type */
		if (packet instanceof SnacPacket)
		{
			SnacPacket snacPacket = (SnacPacket) packet;

			if ((snacPacket.getFamily() == SnacPacket.SRV_UPDATEACK_FAMILY)
					&& (snacPacket.getCommand() == SnacPacket.SRV_UPDATEACK_COMMAND))
			{
				// Check error code, see ICQv8 specification
				int retCode = Util.getWord(snacPacket.getData(), 0);
				switch (retCode)
				{
				case 0x002:
					errorCode = 154;
					break;

				case 0x003:
					errorCode = 155;
					break;

				case 0x00A:
					errorCode = 156;
					break;

				case 0x00C:
					errorCode = 157;
					break;

				case 0x00D:
					errorCode = 158;
					break;
				}

				if (errorCode != 0)
				{
					state = STATE_ERROR;
					return true;
				}

				switch (state)
				{
				/* STATE_DELETE_GROUP */
				case STATE_DELETE_GROUP1:
					sendGroupsList();
					this.state = STATE_DELETE_GROUP2;
					break;

				case STATE_DELETE_GROUP2:
					ContactList.removeGroup(this.gItem);
					this.state = STATE_COMPLETED;
					Icq.sendCLI_ADDEND();
					break;

				/* STATE_ADD_GROUP */
				case STATE_ADD_GROUP1:
					sendGroupsList();
					this.state = STATE_ADD_GROUP2;
					break;

				case STATE_ADD_GROUP2:
					ContactList.addGroup(this.gItem);
					Icq.sendCLI_ADDEND();
					this.state = STATE_COMPLETED;
					break;

				/* STATE_ADD */
				case STATE_ADD1:
					sendGroup(gItem);
					Icq.sendCLI_ADDEND();
					this.state = STATE_COMPLETED;
					break;

				case STATE_ADD2:
					if (retCode == 0)
					{
						sendGroup(gItem);
						cItem.setBooleanValue(
								ContactItem.CONTACTITEM_IS_TEMP,
								false);
						cItem.setBooleanValue(
								ContactItem.CONTACTITEM_NO_AUTH,
								false);
					}

					Icq.sendCLI_ADDEND();
					this.state = STATE_COMPLETED;
					break;

				/* STATE_CLI_ROSTERMODIFY_SENT */
				case STATE_CLI_ROSTERMODIFY_SENT:
					if (this.action != ACTION_RENAME)
						Icq.sendCLI_ADDEND();

					this.state = STATE_COMPLETED;
					break;
					
                /* STATE_MOVE */
                case STATE_MOVE1:
                     sendGroup(newGItem);
                     state = STATE_MOVE2;
                     break;

                case STATE_MOVE2:
                	 Icq.sendCLI_ADDEND();
                     state = STATE_COMPLETED;
                     break;					

				/* STATE_DELETE_CONTACT */
				case STATE_DELETE_CONTACT1:
					ContactListGroupItem group = ContactList
							.getGroupById(cItem
									.getIntValue(ContactItem.CONTACTITEM_GROUP));
					ContactList.removeContactItem(this.cItem);
					if (group != null) {
					    sendGroup(group);
					    state = STATE_DELETE_CONTACT2;
					} else
					{
					    Icq.sendCLI_ADDEND();
					    this.state = STATE_COMPLETED;
					}
					break;

				case STATE_DELETE_CONTACT2:
					Icq.sendCLI_ADDEND();
					this.state = STATE_COMPLETED;
					break;
				}

				/* Packet has been consumed */
				consumed = true;

				/* Update activity timestamp */
				lastActivity = System.currentTimeMillis();
			}

		} /* end 'if (packet instanceof SnacPacket)' */

		/* Return consumption flag */
		return (consumed);
	}


	private void sendGroupsList() throws JimmException
	{
		Icq.c.sendPacket(new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
				SnacPacket.CLI_ROSTERUPDATE_COMMAND, Util.getCounter(),
				new byte[0], packGroups()));
	}

	static private void sendGroup(ContactListGroupItem group)
			throws JimmException
	{
		Icq.c.sendPacket(new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
				SnacPacket.CLI_ROSTERUPDATE_COMMAND, Util.getCounter(),
				new byte[0], packRosterItem(group)));
	}

	/* Forwards received packet, returns true if packet was consumed */
	protected synchronized boolean forward(Packet packet) throws JimmException
	{
		boolean result = processPaket(packet);

		if (result && (errorCode != 0))
		{
			/* Send a CLI_ADDEND packet */
			if (/*(action != ACTION_MOVE) && */(action != ACTION_RENAME))
				Icq.sendCLI_ADDEND();

			/* Update activity timestamp */
			lastActivity = System.currentTimeMillis();
		}

		return result;
	}

	/* Returns true if the action is completed */
	public boolean isCompleted()
	{
		return (this.state == UpdateContactListAction.STATE_COMPLETED);
	}

	public void onEvent(int eventType)
	{
		switch (eventType)
		{
		case ON_COMPLETE:
			switch (action)
			{
			case ACTION_ADD:
			case ACTION_DEL:
			case ACTION_MOVE:
				ContactList.optionsChanged(true, true);
				break;
			}
			RunnableImpl.activateListMT();
			if ((action != ACTION_DEL) && (cItem != null))
				ContactList.contactChanged(cItem, true, false);
			break;

		case ON_ERROR:
			if (errorCode != 0)
				JimmException.handleException(new JimmException(errorCode, 0,
						true));
			else
				JimmException.handleException(new JimmException(154, 3, true));

			break;
		}
	}

	/* Returns true if an error has occured */
	public boolean isError()
	{
		if (this.state == ConnectAction.STATE_ERROR)
			return true;
		if ((lastActivity + UpdateContactListAction.TIMEOUT < System
				.currentTimeMillis())
				|| (errorCode != 0))
		{
			this.state = ConnectAction.STATE_ERROR;
		}
		return (this.state == ConnectAction.STATE_ERROR);
	}

	private byte[] packRosterItem(ContactItem cItem, int groupID)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		if (groupID == 0)
			groupID = cItem
					.getIntValue(ContactItem.CONTACTITEM_GROUP);

		/* Name */
		Util.writeLenAndString(stream, cItem
				.getStringValue(ContactItem.CONTACTITEM_UIN), true);

		/* Group ID */
		Util.writeWord(stream, groupID, true);

		/* ID */
		Util.writeWord(stream, cItem
				.getIntValue(ContactItem.CONTACTITEM_ID), true);

		/* Type (Buddy record ) */
		Util.writeWord(stream, 0, true);

		/* Additional data */
		ByteArrayOutputStream addData = new ByteArrayOutputStream();

		if (groupID != 0x0000) {
			/* TLV(0x0131) - name */
			if (action != ACTION_DEL)
			{
				Util.writeWord(addData, 0x0131, true);
				Util.writeLenAndString(addData, cItem.getStringValue(ContactItem.CONTACTITEM_NAME), true);
			}

			/* Server-side additional data */
			byte[] ssData = cItem.getBytesArray(ContactItem.CONTACTITEM_SS_DATA);
			if (ssData != null)
				Util.writeByteArray(addData, ssData);

			/* TLV(0x0066) - you are awaiting authorization for this buddy */
			if (action == ACTION_REQ_AUTH)
			{
				Util.writeWord(addData, 0x0066, true);
				Util.writeWord(addData, 0x0000, true);
			}
		}
		/* Append additional data to stream */
		Util.writeWord(stream, addData.size(), true);
		stream.write(addData.toByteArray(), 0, addData.size());
		
		return stream.toByteArray();
	}

	static private byte[] packRosterItem(ContactListGroupItem gItem)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		/* Name */
		Util.writeLenAndString(stream, gItem.getName(), true);

		/* Group ID */
		Util.writeWord(stream, gItem.getId(), true);

		/* id */
		Util.writeWord(stream, 0, true);

		/* Type (Group) */
		Util.writeWord(stream, 1, true);

		/* Contact items */
		ContactItem[] items = ContactList.getItems(gItem);

		if (items.length != 0)
		{
			/* Length of the additional data */
			Util.writeWord(stream, items.length * 2 + 4, true);

			/* TLV(0x00C8) */
			Util.writeWord(stream, 0xc8, true);
			Util.writeWord(stream, items.length * 2, true);
			for (int i = 0; i < items.length; i++)
				Util.writeWord(stream, items[i]
						.getIntValue(ContactItem.CONTACTITEM_ID),
						true);
		} else
			Util.writeWord(stream, 0, true);

		return stream.toByteArray();
	}

	private byte[] packGroups()
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		ContactListGroupItem[] gItems = ContactList.getGroupItems();

		/* Name */
		Util.writeLenAndString(stream, "", true);

		/* Group ID */
		Util.writeWord(stream, 0, true);

		Util.writeWord(stream, 0, true);

		Util.writeWord(stream, 1, true);

		Util.writeWord(stream, gItems.length * 2 + 4, true);

		Util.writeWord(stream, 0xc8, true);

		Util.writeWord(stream, gItems.length * 2, true);

		for (int i = 0; i < gItems.length; i++)
			Util.writeWord(stream, gItems[i].getId(), true);

		return stream.toByteArray();
	}

}