/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-06  Jimm Project

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
 File: src/jimm/ContactListContactItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis
 *******************************************************************************/

package jimm;

import java.io.IOException;
import javax.microedition.lcdui.*;

import java.io.DataOutputStream;
import java.io.DataInputStream;

import jimm.JimmUI;
import jimm.comm.*;
import jimm.SplashCanvas;


/* TODO: remove UI code to ChatHistory */
public class ContactListContactItem implements ContactListItem
{
	/* No capability */
	public static final int CAP_NO_INTERNAL = 0x00000000;

	/* Message types */
	public static final int MESSAGE_PLAIN = 1;

	public static final int MESSAGE_URL = 2;

	public static final int MESSAGE_SYS_NOTICE = 3;

	public static final int MESSAGE_AUTH_REQUEST = 4;

	private int idAndGropup, caps, idle, booleanValues, messCounters;

	//#sijapp cond.if modules_FILES is "true"#
	private int typeAndClientId, portAndProt, intIP, extIP, authCookie;
	//#sijapp cond.end #

	private int uinLong, online, signOn, status;
	private byte xStatusId;

	private String name, clientVersion, lowerText;

	///////////////////////////////////////////////////////////////////////////

	synchronized public void setStringValue(int key, String value)
	{
		switch (key)
		{
		case CONTACTITEM_UIN:
			uinLong = Integer.parseInt(value);
			return;
		case CONTACTITEM_NAME:
			name = value;
			lowerText = null;
			return;
		case CONTACTITEM_CLIVERSION:
			clientVersion = value;
			return;
		}
	}

	synchronized public String getStringValue(int key)
	{
		switch (key)
		{
		case CONTACTITEM_UIN:
			return Integer.toString(uinLong);
		case CONTACTITEM_NAME:
			return name;
		case CONTACTITEM_CLIVERSION:
			return clientVersion;
		}
		return null;
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	public String getSortText()
	{
		return getLowerText(); 
	}
	
	public int getSortWeight()
	{
		int status = getIntValue(ContactListContactItem.CONTACTITEM_STATUS); 
		if (status != ContactList.STATUS_OFFLINE) return 0;
		if (getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP) 
				&& status == ContactList.STATUS_OFFLINE) return 20;

		return 10;
	}

	///////////////////////////////////////////////////////////////////////////

	synchronized public void setIntValue(int key, int value)
	{
		switch (key)
		{
		case CONTACTITEM_ID:
			idAndGropup = (idAndGropup & 0x0000FFFF) | (value << 16);
			return;

		case CONTACTITEM_GROUP:
			idAndGropup = (idAndGropup & 0xFFFF0000) | value;
			return;

		case CONTACTITEM_PLAINMESSAGES:
			messCounters = (messCounters & 0x00FFFFFF) | (value << 24);
			return;

		case CONTACTITEM_URLMESSAGES:
			messCounters = (messCounters & 0xFF00FFFF) | (value << 16);
			return;

		case CONTACTITEM_SYSNOTICES:
			messCounters = (messCounters & 0xFFFF00FF) | (value << 8);
			return;

		case CONTACTITEM_AUTREQUESTS:
			messCounters = (messCounters & 0xFFFFFF00) | value;
			return;

		case CONTACTITEM_IDLE:
			idle = value;
			return;
		case CONTACTITEM_STATUS:
			status = value;
			return;
			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			//#sijapp cond.if modules_FILES is "true"#
		case CONTACTITEM_DC_TYPE:
			typeAndClientId = (typeAndClientId & 0xff) | ((value & 0xff) << 8);
			return;
		case CONTACTITEM_ICQ_PROT:
			portAndProt = (portAndProt & 0xffff0000) | (value & 0xffff);
			return;
		case CONTACTITEM_DC_PORT:
			portAndProt = (portAndProt & 0xffff) | ((value & 0xffff) << 16);
			return;
		case CONTACTITEM_CLIENT:
			typeAndClientId = (typeAndClientId & 0xff00) | (value & 0xff);
			return;
		case CONTACTITEM_AUTH_COOKIE:
			authCookie = value;
			return;
			//#sijapp cond.end #
			//#sijapp cond.end #

		case CONTACTITEM_ONLINE:
			online = value;
			return;
		case CONTACTITEM_SIGNON:
			signOn = value;
			return;
			
		case CONTACTITEM_XSTATUS:
			xStatusId = (byte)value;
			return;
		}
	}

	synchronized public int getIntValue(int key)
	{
		switch (key)
		{
		case CONTACTITEM_ID:
			return ((idAndGropup & 0xFFFF0000) >> 16) & 0xFFFF;
		case CONTACTITEM_GROUP:
			return (idAndGropup & 0x0000FFFF);
		case CONTACTITEM_PLAINMESSAGES:
			return ((messCounters & 0xFF000000) >> 24) & 0xFF;
		case CONTACTITEM_URLMESSAGES:
			return ((messCounters & 0x00FF0000) >> 16) & 0xFF;
		case CONTACTITEM_SYSNOTICES:
			return ((messCounters & 0x0000FF00) >> 8) & 0xFF;
		case CONTACTITEM_AUTREQUESTS:
			return (messCounters & 0x000000FF);
		case CONTACTITEM_IDLE:
			return idle;
		case CONTACTITEM_STATUS:
			return status;
			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			//#sijapp cond.if modules_FILES is "true"#
		case CONTACTITEM_DC_TYPE:
			return ((typeAndClientId & 0xff00) >> 8) & 0xFF;
		case CONTACTITEM_ICQ_PROT:
			return portAndProt & 0xffff;
		case CONTACTITEM_DC_PORT:
			return ((portAndProt & 0xffff0000) >> 16) & 0xFFFF;
		case CONTACTITEM_CLIENT:
			return typeAndClientId & 0xff;
		case CONTACTITEM_AUTH_COOKIE:
			return authCookie;
			//#sijapp cond.end #
			//#sijapp cond.end #
		case CONTACTITEM_ONLINE:
			return online;
		case CONTACTITEM_SIGNON:
			return signOn;
		case CONTACTITEM_XSTATUS:
			return xStatusId; 
		}
		return 0;
	}

	///////////////////////////////////////////////////////////////////////////

	synchronized public void setBooleanValue(int key, boolean value)
	{
		booleanValues = (booleanValues & (~key)) | (value ? key : 0x00000000);
	}

	synchronized public boolean getBooleanValue(int key)
	{
		return (booleanValues & key) != 0;
	}

	///////////////////////////////////////////////////////////////////////////

	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	//#sijapp cond.if modules_FILES is "true"#
	public static byte[] longIPToByteAray(int value)
	{
		if (value == 0)
			return null;
		return new byte[]
		{ (byte) (value & 0x000000FF), (byte) ((value & 0x0000FF00) >> 8),
				(byte) ((value & 0x00FF0000) >> 16),
				(byte) ((value & 0xFF000000) >> 24) };
	}

	public static int arrayToLongIP(byte[] array)
	{
		if ((array == null) || (array.length < 4))
			return 0;
		return (int) array[0] & 0xFF | (((int) array[1] & 0xFF) << 8)
				| (((int) array[2] & 0xFF) << 16)
				| (((int) array[3] & 0xFF) << 24);
	}

	synchronized public void setIPValue(int key, byte[] value)
	{
		switch (key)
		{
		case CONTACTITEM_INTERNAL_IP:
			intIP = arrayToLongIP(value);
			break;
		case CONTACTITEM_EXTERNAL_IP:
			extIP = arrayToLongIP(value);
			break;
		}
	}

	synchronized public byte[] getIPValue(int key)
	{
		switch (key)
		{
		case CONTACTITEM_INTERNAL_IP:
			return longIPToByteAray(intIP);
		case CONTACTITEM_EXTERNAL_IP:
			return longIPToByteAray(extIP);
		}
		return null;
	}

	//#sijapp cond.end #
	//#sijapp cond.end #

	public void saveToStream(DataOutputStream stream) throws IOException
	{
		stream.writeByte(0);
		stream.writeInt(idAndGropup);
		stream.writeByte(booleanValues
				& (CONTACTITEM_IS_TEMP | CONTACTITEM_NO_AUTH));
		stream.writeInt(uinLong);
		stream.writeUTF(name);
	}

	public void loadFromStream(DataInputStream stream) throws IOException
	{
		idAndGropup = stream.readInt();
		booleanValues = stream.readByte();
		uinLong = stream.readInt();
		name = stream.readUTF();
	}

	/* Variable keys */
	public static final int CONTACTITEM_UIN = 0; /* String */

	public static final int CONTACTITEM_NAME = 1; /* String */

	public static final int CONTACTITEM_ID = 64; /* Integer */

	public static final int CONTACTITEM_GROUP = 65; /* Integer */

	public static final int CONTACTITEM_PLAINMESSAGES = 67; /* Integer */

	public static final int CONTACTITEM_URLMESSAGES = 68; /* Integer */

	public static final int CONTACTITEM_SYSNOTICES = 69; /* Integer */

	public static final int CONTACTITEM_AUTREQUESTS = 70; /* Integer */

	public static final int CONTACTITEM_IDLE = 71; /* Integer */

	public static final int CONTACTITEM_ADDED = 1 << 0; /* Boolean */

	public static final int CONTACTITEM_NO_AUTH = 1 << 1; /* Boolean */

	public static final int CONTACTITEM_CHAT_SHOWN = 1 << 2; /* Boolean */

	public static final int CONTACTITEM_IS_TEMP = 1 << 3; /* Boolean */

	public static final int CONTACTITEM_HAS_CHAT = 1 << 4; /* Boolean */


	public static final int CONTACTITEM_STATUS = 192; /* Integer */

	public static final int CONTACTITEM_SIGNON = 194; /* Integer */

	public static final int CONTACTITEM_ONLINE = 195; /* Integer */

	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	//#sijapp cond.if modules_FILES is "true"#
	public static final int CONTACTITEM_INTERNAL_IP = 225; /* IP address */

	public static final int CONTACTITEM_EXTERNAL_IP = 226; /* IP address */

	public static final int CONTACTITEM_AUTH_COOKIE = 193; /* Integer */

	public static final int CONTACTITEM_DC_TYPE = 72; /* Integer */

	public static final int CONTACTITEM_ICQ_PROT = 73; /* Integer */

	public static final int CONTACTITEM_DC_PORT = 74; /* Integer */

	//#sijapp cond.end#
	//#sijapp cond.end#
	public static final int CONTACTITEM_CLIENT = 76; /* Integer */

	public static final int CONTACTITEM_CLIVERSION = 2; /* String */
	
	public static final int CONTACTITEM_XSTATUS = 78;

	public void init(int id, int group, String uin, String name,
			boolean noAuth, boolean added)
	{
		if (id == -1)
			setIntValue(ContactListContactItem.CONTACTITEM_ID, Util
					.createRandomId());
		else
			setIntValue(ContactListContactItem.CONTACTITEM_ID, id);
		setIntValue(ContactListContactItem.CONTACTITEM_GROUP, group);
		setStringValue(ContactListContactItem.CONTACTITEM_UIN, uin);
		setStringValue(ContactListContactItem.CONTACTITEM_NAME, name);
		setBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH, noAuth);
		setBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP, false);
		setBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT, false);
		setBooleanValue(ContactListContactItem.CONTACTITEM_ADDED, added);
		setIntValue(ContactListContactItem.CONTACTITEM_STATUS,
				ContactList.STATUS_OFFLINE);
		setIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS, 0);
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		//#sijapp cond.if modules_FILES is "true"#
		setIPValue(ContactListContactItem.CONTACTITEM_INTERNAL_IP, new byte[4]);
		setIPValue(ContactListContactItem.CONTACTITEM_EXTERNAL_IP, new byte[4]);
		setIntValue(ContactListContactItem.CONTACTITEM_DC_PORT, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_DC_TYPE, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_AUTH_COOKIE, 0);
		//#sijapp cond.end#
		//#sijapp cond.end#
		setIntValue(ContactListContactItem.CONTACTITEM_SIGNON, -1);
		online = -1;
		setIntValue(ContactListContactItem.CONTACTITEM_IDLE, -1);
		setIntValue(ContactListContactItem.CONTACTITEM_CLIENT, Icq.CLI_NONE);
		setStringValue(ContactListContactItem.CONTACTITEM_CLIVERSION, "");
		xStatusId = -1;
	}

	/* Constructor for an existing contact item */
	public ContactListContactItem(int id, int group, String uin, String name,
			boolean noAuth, boolean added)
	{
		this.init(id, group, uin, name, noAuth, added);
	}

	public ContactListContactItem()
	{
		xStatusId = -1;
	}

	/* Returns true if client supports given capability */
	public boolean hasCapability(int capability)
	{
		return ((capability & this.caps) != 0x00000000);
	}

	/* Adds a capability by its CAPF value */
	public void addCapability(int capability)
	{
		this.caps |= capability;
	}

	public String getLowerText()
	{
		if (lowerText == null)
		{
			lowerText = name.toLowerCase();
			if (lowerText.equals(name))
				lowerText = name; // to decrease memory usage 
		}
		return lowerText;
	}

	/* Returns color for contact name */
	public int getTextColor()
	{
		if (getBooleanValue(CONTACTITEM_IS_TEMP))
			return 0x808080;
		int color = getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT) ? Options
				.getSchemeColor(Options.CLRSCHHEME_OUTGOING)
				: Options.getSchemeColor(Options.CLRSCHHEME_TEXT);
		return color;
	}

	/* Returns font style for contact name */
	public int getFontStyle()
	{
		return getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT) ? Font.STYLE_BOLD
				: Font.STYLE_PLAIN;
	}

	public int getUIN()
	{
		return uinLong;
	}

	/* Returns image index for contact */
	public int getLeftImageIndex(boolean expanded)
	{
		int tempIndex = -1;
		//#sijapp cond.if target isnot "DEFAULT"#		
		if (typing)
			return 12;
		//#sijapp cond.end#			
		if (isMessageAvailable(MESSAGE_PLAIN))
			tempIndex = 8;
		else if (isMessageAvailable(MESSAGE_URL))
			tempIndex = 9;
		else if (isMessageAvailable(MESSAGE_AUTH_REQUEST))
			tempIndex = 11;
		else if (isMessageAvailable(MESSAGE_SYS_NOTICE))
			tempIndex = 10;
		else
			tempIndex = JimmUI
					.getStatusImageIndex(getIntValue(ContactListContactItem.CONTACTITEM_STATUS));
		return tempIndex;
	}
	
	public int getSecondLeftImageIndex()
	{
		return getIntValue(CONTACTITEM_XSTATUS); 
	}
	
	/* Returns image index client */
	public int getRightImageIndex()
	{
		return Icq.getClientImageID(getIntValue(CONTACTITEM_CLIENT)); 
	}

	public String getText()
	{
		if (getBooleanValue(CONTACTITEM_NO_AUTH))
			return "[!] " + name;
		return name;
	}

	/* Returns true if contact must be shown even user offline
	 and "hide offline" is on */
	protected boolean mustBeShownAnyWay()
	{
		return getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT)
				|| getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP);
	}

	/* Returns total count of all unread messages (messages, sys notices, urls, auths) */
	protected int getUnreadMessCount()
	{
		return getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES)
				+ getIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES)
				+ getIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES)
				+ getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS);
	}

	/* Returns true if the next available message is a message of given type
	 Returns false if no message at all is available, or if the next available
	 message is of another type */
	protected synchronized boolean isMessageAvailable(int type)
	{
		switch (type)
		{
		case MESSAGE_PLAIN:
			return (this
					.getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
		case MESSAGE_URL:
			return (this
					.getIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES) > 0);
		case MESSAGE_SYS_NOTICE:
			return (this
					.getIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES) > 0);
		case MESSAGE_AUTH_REQUEST:
			return (this
					.getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS) > 0);
		}
		return (this
				.getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
	}

	/* Increases the mesage count */
	protected synchronized void increaseMessageCount(int type)
	{
		switch (type)
		{
		case MESSAGE_PLAIN:
			this
					.setIntValue(
							ContactListContactItem.CONTACTITEM_PLAINMESSAGES,
							this
									.getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES) + 1);
			break;
		case MESSAGE_URL:
			this
					.setIntValue(
							ContactListContactItem.CONTACTITEM_URLMESSAGES,
							this
									.getIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES) + 1);
			break;
		case MESSAGE_SYS_NOTICE:
			this
					.setIntValue(
							ContactListContactItem.CONTACTITEM_SYSNOTICES,
							this
									.getIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES) + 1);
			break;
		case MESSAGE_AUTH_REQUEST:
			this
					.setIntValue(
							ContactListContactItem.CONTACTITEM_AUTREQUESTS,
							this
									.getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS) + 1);
		}
	}

	public synchronized void resetUnreadMessages()
	{
		setIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES, 0);
	}

	/* Checks whether some other object is equal to this one */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ContactListContactItem))
			return (false);
		ContactListContactItem ci = (ContactListContactItem) obj;
		return (this
				.getStringValue(ContactListContactItem.CONTACTITEM_UIN)
				.equals(
						ci
								.getStringValue(ContactListContactItem.CONTACTITEM_UIN)) && (this
				.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP) == ci
				.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP)));
	}


	//#sijapp cond.if modules_HISTORY is "true" #
	public void showHistory()
	{
		HistoryStorage.showHistoryList(
				getStringValue(ContactListContactItem.CONTACTITEM_UIN), name);
	}

	//#sijapp cond.end#


	//#sijapp cond.if target isnot "DEFAULT"#
	private boolean typing = false;

	public void BeginTyping(boolean type)
	{
		typing = type;
		setStatusImage();
	}

	//#sijapp cond.end#	
	/** ************************************************************************* */
	/** ************************************************************************* */
	/** ************************************************************************* */

	public void setOfflineStatus()
	{
		//#sijapp cond.if target isnot "DEFAULT"#
		typing = false;
		//#sijapp cond.end#
		setIntValue(CONTACTITEM_STATUS, ContactList.STATUS_OFFLINE);
		setIntValue(CONTACTITEM_XSTATUS, -1);
	}

	/*
	public void commandAction(Command c, Displayable d)
	{
		//#sijapp cond.if target isnot "DEFAULT"#
		if (((c == textboxCancelCommand) || (c == JimmUI.cmdOk) || (c == textboxSendCommand))
				&& (Options.getInt(Options.OPTION_TYPING_MODE) > 0)
				&& ((caps & Icq.CAPF_TYPING) != 0)
				&& ((Options.getLong(Options.OPTION_ONLINE_STATUS) != ContactList.STATUS_INVISIBLE) && (Options
						.getLong(Options.OPTION_ONLINE_STATUS) != ContactList.STATUS_INVIS_ALL)))
			try
			{
				Jimm.jimm
						.getIcqRef()
						.beginTyping(
								ContactListContactItem.this
										.getStringValue(ContactListContactItem.CONTACTITEM_UIN),
								false);
			} catch (JimmException e)
			{
			}
		//#sijapp cond.end#
	
		 */


	/* Sets new contact name */
	public void rename(String newName)
	{
		if ((newName == null) || (newName.length() == 0))
			return;
		name = newName;
		lowerText = null;
		try
		{
			/* Save ContactList */
			ContactList.save();

			/* Try to save ContactList to server */
			if (!getBooleanValue(CONTACTITEM_IS_TEMP))
			{
				UpdateContactListAction action = new UpdateContactListAction(
						this, UpdateContactListAction.ACTION_RENAME);
				Icq.requestAction(action);
			}
		} catch (JimmException je)
		{
			if (je.isCritical())
				return;
		} catch (Exception e)
		{
			/* Do nothing */
		}

		ContactList.contactChanged(this, true, true);
		ChatHistory.contactRenamed(getStringValue(ContactListContactItem.CONTACTITEM_UIN), name);
	}

	/* Activates the contact item menu */
	public void activate()
	{
		String currentUin = getStringValue(ContactListContactItem.CONTACTITEM_UIN);

		/* Display chat history */
		if (getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT))
		{
			ChatTextList chat = ChatHistory.getChatHistoryAt(currentUin);
			chat.buildMenu();
			chat.activate(true, false);
		} 
		else
		/* Display menu */
		{
			JimmUI.showContactMenu(this);
		}

		setStatusImage();
	}
	

	/****************************************************************************/
	/****************************************************************************/
	/****************************************************************************/

	/* Shows popup window with text of received message */
	static public void showPopupWindow(String uin, String name, String text)
	{
		/*
		if (SplashCanvas.locked())
			return;

		boolean haveToShow = false;
		boolean chatVisible = ChatHistory.chatHistoryShown(uin);
		boolean uinEquals = uin.equals(JimmUI.getLastUin());

		switch (Options.getInt(Options.OPTION_POPUP_WIN2))
		{
		case 0:
			return;
		case 1:
			haveToShow = !chatVisible & uinEquals;
			break;
		case 2:
			haveToShow = !chatVisible || (chatVisible && !uinEquals);
			break;
		}

		if (!haveToShow)
			return;

		String textToAdd = "[" + name + "]\n" + text;

		if (Jimm.display.getCurrent() instanceof Alert)
		{
			Alert currAlert = (Alert) Jimm.display.getCurrent();
			if (currAlert.getImage() != null)
				currAlert.setImage(null);
			currAlert.setString(currAlert.getString() + "\n\n" + textToAdd);
			return;
		}

		//#sijapp cond.if target is "MIDP2"#
		String oldText = messageTextbox.isShown() ? messageTextbox.getString()
				: null;
		//#sijapp cond.end#

		Alert alert = new Alert(ResourceBundle.getString("message"), textToAdd,
				null, null);
		alert.setTimeout(Alert.FOREVER);

		Jimm.display.setCurrent(alert);

		//#sijapp cond.if target is "MIDP2"#
		if (Jimm.is_phone_SE() && (oldText != null))
			messageTextbox.setString(oldText);
		//#sijapp cond.end#
		 */
	}

	/* flashs form caption when current contact have changed status */
	static synchronized public void statusChanged(String uin, long status)
	{
		/*
		String lastUin = JimmUI.getLastUin();
		if (lastUin == null) return;
		if (lastUin.equals(uin)) // TODO: add x-status!
			showTopLine(uin, JimmUI.getStatusString(status), 8, FlashCapClass.TYPE_FLASH);
			*/
	}

	/* Shows creeping line whan user is typing text and message has received */
	static synchronized public void showCreepingLine(String uin, String text)
	{
		/*
		String lastUin = JimmUI.getLastUin();
		if (lastUin == null) return;
		if (Options.getBoolean(Options.OPTION_CREEPING_LINE) && 
			lastUin.equals(uin) && 
			messageTextbox.isShown())
			showTopLine(uin, text, 0, FlashCapClass.TYPE_CREEPING);
			*/
	}

	public void setStatusImage()
	{
		int imgIndex;

		//#sijapp cond.if target isnot "DEFAULT"#		
		imgIndex = typing ? 8 : JimmUI
				.getStatusImageIndex(getIntValue(CONTACTITEM_STATUS));
		//#sijapp cond.else#
		//#		imgIndex = JimmUI.getStatusImageIndex(getIntValue(CONTACTITEM_STATUS));
		//#sijapp cond.end#

		if (SplashCanvas.locked())
		{
			SplashCanvas.setStatusToDraw(imgIndex);
			SplashCanvas.setMessage(getStringValue(CONTACTITEM_NAME));
			SplashCanvas.Repaint();
			SplashCanvas.startTimer();
			return;
		}

		ChatTextList chat = ChatHistory.getChatHistoryAt(getStringValue(CONTACTITEM_UIN));
		
		if (chat != null) 
			chat.setImage(ContactList.smallIcons.elementAt(imgIndex));
	}
}

