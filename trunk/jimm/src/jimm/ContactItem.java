/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/jimm/ContactItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Denis Artyomov
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
public class ContactItem implements ContactListItem
{
	/* Variable keys */
	public static final int CONTACTITEM_UIN           = 0; /* String */
	public static final int CONTACTITEM_NAME          = 1; /* String */
	public static final int CONTACTITEM_CLIVERSION    = 2; /* String */	
	
	public static final int CONTACTITEM_ID            = 64; /* Integer */
	public static final int CONTACTITEM_GROUP         = 65; /* Integer */
	public static final int CONTACTITEM_PLAINMESSAGES = 67; /* Integer */
	public static final int CONTACTITEM_URLMESSAGES   = 68; /* Integer */
	public static final int CONTACTITEM_SYSNOTICES    = 69; /* Integer */
	public static final int CONTACTITEM_AUTREQUESTS   = 70; /* Integer */
	public static final int CONTACTITEM_IDLE          = 71; /* Integer */
	public static final int CONTACTITEM_DC_TYPE       = 72; /* Integer */
	public static final int CONTACTITEM_ICQ_PROT      = 73; /* Integer */
	public static final int CONTACTITEM_DC_PORT       = 74; /* Integer */
	public static final int CONTACTITEM_CAPABILITIES  = 75; /* Integer */
	public static final int CONTACTITEM_CLIENT        = 76; /* Integer */
	public static final int CONTACTITEM_XSTATUS       = 78; /* Integer */
	public static final int CONTACTITEM_STATUS        = 79; /* Integer */
	public static final int CONTACTITEM_AUTH_COOKIE   = 80; /* Integer */
	public static final int CONTACTITEM_SIGNON        = 81; /* Integer */
	public static final int CONTACTITEM_ONLINE        = 82; /* Integer */
	public static final int CONTACTITEM_INV_ID        = 83; /* Integer */
	public static final int CONTACTITEM_VIS_ID        = 84; /* Integer */
	public static final int CONTACTITEM_IGN_ID        = 85; /* Integer */

	public static final int CONTACTITEM_ADDED         = 1 << 0; /* Boolean */
	public static final int CONTACTITEM_NO_AUTH       = 1 << 1; /* Boolean */
	public static final int CONTACTITEM_CHAT_SHOWN    = 1 << 2; /* Boolean */
	public static final int CONTACTITEM_IS_TEMP       = 1 << 3; /* Boolean */
	public static final int CONTACTITEM_HAS_CHAT      = 1 << 4; /* Boolean */
	
	public static final int CONTACTITEM_SS_DATA       = 227; /* bytes[] */
	public static final int CONTACTITEM_INTERNAL_IP   = 225; /* bytes[] */
	public static final int CONTACTITEM_EXTERNAL_IP   = 226; /* bytes[] */
	
	
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

	private long privacyData;
	private int uinLong, online, signOn, status;
	private byte xStatusId;

	private String name, clientVersion, lowerText;
	private byte[] ssData; // server-size raw data

	///////////////////////////////////////////////////////////////////////////

	synchronized public void setStringValue(int key, String value)
	{
		setStringValue_(key, value);
	}
	
	private void setStringValue_(int key, String value)
	{
		switch (key)
		{
		case CONTACTITEM_UIN:
			try
			{
				uinLong = Integer.parseInt(value);
			} catch (NumberFormatException e)
			{
				System.out.println ("NFE: setStringValue(UIN,"+value+")");
			}
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
		int status = getIntValue(ContactItem.CONTACTITEM_STATUS); 

		switch (status)
		{
			case ContactList.STATUS_ONLINE:     return 0;
			case ContactList.STATUS_CHAT:       return 1;
			case ContactList.STATUS_EVIL:       return 2;
			case ContactList.STATUS_DEPRESSION: return 3;
			case ContactList.STATUS_HOME:       return 4;
			case ContactList.STATUS_WORK:       return 5;
			case ContactList.STATUS_LUNCH:      return 6;
			case ContactList.STATUS_AWAY:       return 7;
			case ContactList.STATUS_NA:         return 8;
			case ContactList.STATUS_OCCUPIED:   return 9;
			case ContactList.STATUS_DND:        return 10;
			case ContactList.STATUS_INVISIBLE:  return 11;

			case ContactList.STATUS_OFFLINE:
				if (getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP))
					return 19;
				return 20;
		}

		return 15;
	}

	///////////////////////////////////////////////////////////////////////////

	private void setIntValue_(int key, int value)
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
		case CONTACTITEM_CAPABILITIES:
			caps = value;
			return;
		case CONTACTITEM_STATUS:
			status = value;
			if (status != ContactList.STATUS_OFFLINE && getBooleanValue_(CONTACTITEM_NO_AUTH)) 
			{
				setBooleanValue_(CONTACTITEM_NO_AUTH, false);
				ChatHistory.rebuildMenu(this);
			}
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
			portAndProt = (portAndProt & 0x0000ffff) | ((value & 0xffff) << 16);
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
			
		case CONTACTITEM_INV_ID:
			privacyData = (privacyData & 0xFFFFFFFFFFFF0000l) | (long)(value);
			return;
			
		case CONTACTITEM_VIS_ID:
			privacyData = (privacyData & 0xFFFFFFFF0000FFFFl) | ((long)(value) << 16);
			return;
			
		case CONTACTITEM_IGN_ID:
			privacyData = (privacyData & 0xFFFF0000FFFFFFFFl) | ((long)value << 32);
			return;
		}
		
		//throw new Exception("setIntValue");
	}
	
	synchronized public void setIntValue(int key, int value)
	{
		setIntValue_(key, value);
	}

	private int getIntValue_(int key)
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
		case CONTACTITEM_CAPABILITIES:
			return caps;
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
			
		case CONTACTITEM_INV_ID: return (int)(privacyData&0xFFFF);
		case CONTACTITEM_VIS_ID: return (int)((privacyData >> 16)&0xFFFF);
		case CONTACTITEM_IGN_ID: return (int)((privacyData >> 32)&0xFFFF);
		}
		return 0;
		
	}
	
	synchronized public int getIntValue(int key)
	{
		return getIntValue_(key);
	}

	///////////////////////////////////////////////////////////////////////////

	synchronized public void setBooleanValue(int key, boolean value)
	{
		setBooleanValue_(key, value);
	}
	
	private void setBooleanValue_(int key, boolean value)
	{
		booleanValues = (booleanValues & (~key)) | (value ? key : 0x00000000);
	}

	synchronized public boolean getBooleanValue(int key)
	{
		return getBooleanValue_(key);
	}
	
	private boolean getBooleanValue_(int key)
	{
		return (booleanValues & key) != 0;
	}
	

	///////////////////////////////////////////////////////////////////////////

	//#sijapp cond.if (target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2") & modules_FILES is "true"#
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

	//#sijapp cond.end #
	
	synchronized public void setBytesArray(int key, byte[] value)
	{
		switch (key)
		{
		//#sijapp cond.if (target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2") & modules_FILES is "true"#
		case CONTACTITEM_INTERNAL_IP:
			intIP = arrayToLongIP(value);
			break;
		case CONTACTITEM_EXTERNAL_IP:
			extIP = arrayToLongIP(value);
			break;
		//#sijapp cond.end #
			
		case CONTACTITEM_SS_DATA:
			ssData = value;
			break;
		}
	}

	synchronized public byte[] getBytesArray(int key)
	{
		switch (key)
		{
		//#sijapp cond.if (target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2") & modules_FILES is "true"#
		case CONTACTITEM_INTERNAL_IP:
			return longIPToByteAray(intIP);
		case CONTACTITEM_EXTERNAL_IP:
			return longIPToByteAray(extIP);
		//#sijapp cond.end #
			
		case CONTACTITEM_SS_DATA:
			return ssData;
		}
		return null;
	}

	public void saveToStream(DataOutputStream stream) throws IOException
	{
		stream.writeByte(0);
		stream.writeInt(idAndGropup);
		stream.writeInt(booleanValues & (CONTACTITEM_IS_TEMP | CONTACTITEM_NO_AUTH));
		stream.writeInt(uinLong);
		stream.writeUTF(name);
		if (ssData != null)
		{
			stream.writeShort(ssData.length);
			stream.write(ssData);
		}
		else stream.writeShort(0);
		stream.writeLong(privacyData);
	}

	public void loadFromStream(DataInputStream stream) throws IOException
	{
		idAndGropup = stream.readInt();
		booleanValues = stream.readInt();
		uinLong = stream.readInt();
		name = stream.readUTF();
		int ssInfoLen = stream.readShort();
		if (ssInfoLen != 0)
		{
			ssData = new byte[ssInfoLen];
			stream.read(ssData);
		}
		else ssData = null;
		privacyData = stream.readLong();
	}

	public void init(int id, int group, String uin, String name,
			boolean noAuth, boolean added)
	{
		synchronized (this)
		{
			if (id == -1)
				setIntValue_(ContactItem.CONTACTITEM_ID, ContactList.generateNewIdForBuddy());
			else
				setIntValue_(ContactItem.CONTACTITEM_ID, id);
			setIntValue_(ContactItem.CONTACTITEM_GROUP, group);
			setStringValue_(ContactItem.CONTACTITEM_UIN, uin);
			setStringValue_(ContactItem.CONTACTITEM_NAME, name);
			setBooleanValue_(ContactItem.CONTACTITEM_NO_AUTH, noAuth);
			setBooleanValue_(ContactItem.CONTACTITEM_IS_TEMP, false);
			setBooleanValue_(ContactItem.CONTACTITEM_HAS_CHAT, false);
			setBooleanValue_(ContactItem.CONTACTITEM_ADDED, added);
			setIntValue_(ContactItem.CONTACTITEM_STATUS,
					ContactList.STATUS_OFFLINE);
			setIntValue_(ContactItem.CONTACTITEM_CAPABILITIES,
					Icq.CAPF_NO_INTERNAL);
			setIntValue_(ContactItem.CONTACTITEM_PLAINMESSAGES, 0);
			setIntValue_(ContactItem.CONTACTITEM_URLMESSAGES, 0);
			setIntValue_(ContactItem.CONTACTITEM_SYSNOTICES, 0);
			setIntValue_(ContactItem.CONTACTITEM_AUTREQUESTS, 0);
			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			//#sijapp cond.if modules_FILES is "true"#
			setBytesArray(ContactItem.CONTACTITEM_INTERNAL_IP, new byte[4]);
			setBytesArray(ContactItem.CONTACTITEM_EXTERNAL_IP, new byte[4]);
			setIntValue_(ContactItem.CONTACTITEM_DC_PORT, 0);
			setIntValue_(ContactItem.CONTACTITEM_DC_TYPE, 0);
			setIntValue_(ContactItem.CONTACTITEM_ICQ_PROT, 0);
			setIntValue_(ContactItem.CONTACTITEM_AUTH_COOKIE, 0);
			//#sijapp cond.end#
			//#sijapp cond.end#
			setIntValue_(ContactItem.CONTACTITEM_SIGNON, -1);
			online = -1;
			setIntValue_(ContactItem.CONTACTITEM_IDLE, -1);
			setIntValue_(ContactItem.CONTACTITEM_CLIENT, Icq.CLI_NONE);
			setStringValue_(ContactItem.CONTACTITEM_CLIVERSION, "");
			xStatusId = -1;
		}
	}

	/* Constructor for an existing contact item */
	public ContactItem(int id, int group, String uin, String name,
			boolean noAuth, boolean added)
	{
		this.init(id, group, uin, name, noAuth, added);
	}

	public ContactItem()
	{
		xStatusId = -1;
	}

	/* Returns true if client supports given capability */
	synchronized public boolean hasCapability(int capability)
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
		synchronized (this)
		{
			if (getBooleanValue_(CONTACTITEM_IS_TEMP))
				return 0x808080;
			int color = getBooleanValue_(ContactItem.CONTACTITEM_HAS_CHAT) ? Options
					.getSchemeColor(Options.CLRSCHHEME_OUTGOING, -1)
					: Options.getSchemeColor(Options.CLRSCHHEME_TEXT, -1);
			return color;
		}
	}

	/* Returns font style for contact name */
	public int getFontStyle()
	{
		return getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT) ? Font.STYLE_BOLD
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
			return 17;
		//#sijapp cond.end#			
		if (isMessageAvailable(MESSAGE_PLAIN))
			tempIndex = 13;
		else if (isMessageAvailable(MESSAGE_URL))
			tempIndex = 14;
		else if (isMessageAvailable(MESSAGE_AUTH_REQUEST))
			tempIndex = 15;
		else if (isMessageAvailable(MESSAGE_SYS_NOTICE))
			tempIndex = 16;
		else
			tempIndex = JimmUI
					.getStatusImageIndex(getIntValue(ContactItem.CONTACTITEM_STATUS));
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

	static private StringBuffer tmpStringBuffer = new StringBuffer();	
	
	public String getText()
	{
		tmpStringBuffer.setLength(0);
		
		synchronized (this)
		{
			if (getBooleanValue(CONTACTITEM_NO_AUTH)) tmpStringBuffer.append("!");
			
			if (getIntValue_(CONTACTITEM_GROUP) == 0)
			{
				if (tmpStringBuffer.length() != 0) tmpStringBuffer.append(',');
				tmpStringBuffer.append("f");
			}
			
			if (getIntValue_(CONTACTITEM_IGN_ID) != 0)
			{
				if (tmpStringBuffer.length() != 0) tmpStringBuffer.append(',');
				tmpStringBuffer.append('d');
			}
			
			if (getIntValue_(CONTACTITEM_INV_ID) != 0)
			{
				if (tmpStringBuffer.length() != 0) tmpStringBuffer.append(',');
				tmpStringBuffer.append('i');
			}
			
			if (getIntValue_(CONTACTITEM_VIS_ID) != 0)
			{
				if (tmpStringBuffer.length() != 0) tmpStringBuffer.append(',');
				tmpStringBuffer.append('v');
			}
			
			if (tmpStringBuffer.length() != 0)
			{
				return "["+tmpStringBuffer.toString()+"] "+name;
			}
			
			return name;
		}
	}

	/* Returns true if contact must be shown even user offline
	 and "hide offline" is on */
	protected boolean mustBeShownAnyWay()
	{
		return getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT|ContactItem.CONTACTITEM_IS_TEMP);
	}

	/* Returns total count of all unread messages (messages, sys notices, urls, auths) */
	protected int getUnreadMessCount()
	{
		synchronized (this)
		{
			return getIntValue_(ContactItem.CONTACTITEM_PLAINMESSAGES)
				+ getIntValue_(ContactItem.CONTACTITEM_URLMESSAGES)
				+ getIntValue_(ContactItem.CONTACTITEM_SYSNOTICES)
				+ getIntValue_(ContactItem.CONTACTITEM_AUTREQUESTS);
		}
	}

	/* Returns true if the next available message is a message of given type
	 Returns false if no message at all is available, or if the next available
	 message is of another type */
	public boolean isMessageAvailable(int type)
	{
		switch (type)
		{
		case MESSAGE_PLAIN:
			return (getIntValue(ContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
		case MESSAGE_URL:
			return (getIntValue(ContactItem.CONTACTITEM_URLMESSAGES) > 0);
		case MESSAGE_SYS_NOTICE:
			return (getIntValue(ContactItem.CONTACTITEM_SYSNOTICES) > 0);
		case MESSAGE_AUTH_REQUEST:
			return (getIntValue(ContactItem.CONTACTITEM_AUTREQUESTS) > 0);
		}
		return (getIntValue(ContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
	}

	/* Increases the message count */
	protected synchronized void increaseMessageCount(int type)
	{
		switch (type)
		{
		case MESSAGE_PLAIN:
			setIntValue_(
				ContactItem.CONTACTITEM_PLAINMESSAGES,
				getIntValue_(ContactItem.CONTACTITEM_PLAINMESSAGES) + 1
			);
			break;
			
		case MESSAGE_URL:
			setIntValue_(
				ContactItem.CONTACTITEM_URLMESSAGES,
				getIntValue_(ContactItem.CONTACTITEM_URLMESSAGES) + 1
			);
			break;
			
		case MESSAGE_SYS_NOTICE:
			setIntValue_(
				ContactItem.CONTACTITEM_SYSNOTICES,
				getIntValue_(ContactItem.CONTACTITEM_SYSNOTICES) + 1
			);
			break;
			
		case MESSAGE_AUTH_REQUEST:
			setIntValue_(
				ContactItem.CONTACTITEM_AUTREQUESTS,
				getIntValue_(ContactItem.CONTACTITEM_AUTREQUESTS) + 1
			);
			break;
		}
	}

	public synchronized void resetUnreadMessages()
	{
		synchronized (this)
		{
			setIntValue_(ContactItem.CONTACTITEM_PLAINMESSAGES, 0);
			setIntValue_(ContactItem.CONTACTITEM_URLMESSAGES, 0);
			setIntValue_(ContactItem.CONTACTITEM_SYSNOTICES, 0);
		}
	}

	//#sijapp cond.if modules_HISTORY is "true" #
	public void showHistory()
	{
		HistoryStorage.showHistoryList(
				getStringValue(ContactItem.CONTACTITEM_UIN), name);
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
		ChatHistory.contactRenamed(getStringValue(ContactItem.CONTACTITEM_UIN), name);
	}

	/* Activates the contact item menu */
	public void activate()
	{
		Jimm.aaUserActivity();
		
		String currentUin = getStringValue(ContactItem.CONTACTITEM_UIN);

		/* Display chat history */
		if (getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT))
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

	public void setStatusImage()
	{
		int imgIndex;

		//#sijapp cond.if target isnot "DEFAULT"#		
		imgIndex = typing ? 13 : JimmUI
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

