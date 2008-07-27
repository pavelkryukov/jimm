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
public class ContactItem implements ContactListItem, JimmScreen
{
	/* String */
	public static final int CONTACTITEM_UIN           = 0; 
	public static final int CONTACTITEM_NAME          = 1;
	public static final int CONTACTITEM_CLIVERSION    = 2;	
	public static final int CONTACTITEM_XSTATUSMSG    = 3;	
	
	/* Integer */
	public static final int CONTACTITEM_ID            = 64; 
	public static final int CONTACTITEM_GROUP         = 65;
	public static final int CONTACTITEM_IDLE          = 71;
	public static final int CONTACTITEM_DC_TYPE       = 72;
	public static final int CONTACTITEM_ICQ_PROT      = 73;
	public static final int CONTACTITEM_DC_PORT       = 74;
	public static final int CONTACTITEM_CAPABILITIES  = 75;
	public static final int CONTACTITEM_CLIENT        = 76;
	public static final int CONTACTITEM_XSTATUS       = 78;
	public static final int CONTACTITEM_STATUS        = 79;
	public static final int CONTACTITEM_AUTH_COOKIE   = 80;
	public static final int CONTACTITEM_SIGNON        = 81;
	public static final int CONTACTITEM_ONLINE        = 82;
	public static final int CONTACTITEM_INV_ID        = 83;
	public static final int CONTACTITEM_VIS_ID        = 84;
	public static final int CONTACTITEM_IGN_ID        = 85;

	/* Boolean */
	public static final int CONTACTITEM_ADDED         = 1 << 0; 
	public static final int CONTACTITEM_NO_AUTH       = 1 << 1;
	public static final int CONTACTITEM_CHAT_SHOWN    = 1 << 2;
	public static final int CONTACTITEM_IS_TEMP       = 1 << 3;
	public static final int CONTACTITEM_HAS_CHAT      = 1 << 4;
	public static final int CONTACTITEM_IS_PHANTOM    = 1 << 5;
	public static final int CONTACTITEM_B_PLMESSAGES  = 1 << 6;
	public static final int CONTACTITEM_B_URLMESSAGES = 1 << 7;
	public static final int CONTACTITEM_B_SYSNOTICES  = 1 << 8;
	public static final int CONTACTITEM_B_AUTREQUESTS = 1 << 9;
	
	/* bytes[] */
	public static final int CONTACTITEM_INTERNAL_IP			 = 225;
	public static final int CONTACTITEM_EXTERNAL_IP     	 = 226;
	public static final int CONTACTITEM_SS_DATA         	 = 227;

	//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
	public static final int CONTACTITEM_BUDDYICON_HASH       = 228;
	public static final int CONTACTITEM_BUDDYICON_HASH_READY = 229;
	public static final int CONTACTITEM_BUDDYICON       	 = 230;
	//#sijapp cond.end #

	/* No capability */
	public static final int CAP_NO_INTERNAL = 0x00000000;

	private int idAndGropup;
	private int caps;
	private int idle;
	private int booleanValues;

	//#sijapp cond.if modules_FILES is "true"#
	private int typeAndClientId;
	private int portAndProt;
	private int intIP;
	private int extIP;
	private int authCookie;
	//#sijapp cond.end #

	private long privacyData;
	private int uinLong;
	private int online;
	private int signOn;
	private int status;
	private byte xStatusId;

	private String name;
	private String clientVersion;
	private String lowerText;
	private String xStatusMessage;
	private byte[] ssData; // server-size raw data
	//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
	private byte[] biHash; // buddy-icon hash
	private byte[] biHashDone; // buddy-icon hash of downloaded image
	private byte[] buddyIcon; // buddy-icon raw data
	//#sijapp cond.end #

	private int COLOR_NORMAL;
	private int COLOR_HASCHAT;
	private int COLOR_FANTOM;

	private String formattedName;
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
			} catch (NumberFormatException e) {}
			return;
		case CONTACTITEM_NAME:
			name = value;
			lowerText = null;
			return;
		case CONTACTITEM_CLIVERSION:
			clientVersion = value;
			return;
		case CONTACTITEM_XSTATUSMSG:
			xStatusMessage = (value == null) ? xStatusMessage : value;
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
		case CONTACTITEM_XSTATUSMSG:
			return xStatusMessage;
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
			int value = (idAndGropup & 0x0000FFFF);
			if (value == 0 && !getBooleanValue_(CONTACTITEM_IS_PHANTOM)) value = -1; // Group is -1 for temporary contacts
			return value; 
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
		//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
		case CONTACTITEM_BUDDYICON_HASH:
			biHash = value;
			break;
		case CONTACTITEM_BUDDYICON_HASH_READY:
			biHashDone = value;
			break;
		case CONTACTITEM_BUDDYICON:
			buddyIcon = value;
			break;
		//#sijapp cond.end #
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
		//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
		case CONTACTITEM_BUDDYICON_HASH:
			return biHash;
		case CONTACTITEM_BUDDYICON_HASH_READY:
			return biHashDone;
		case CONTACTITEM_BUDDYICON:
			return buddyIcon;
		//#sijapp cond.end #
		}
		return null;
	}

	public void saveToStream(DataOutputStream stream) throws IOException
	{
		stream.writeByte(0);
		stream.writeInt(idAndGropup);
		stream.writeInt(booleanValues & (CONTACTITEM_IS_TEMP | CONTACTITEM_NO_AUTH | CONTACTITEM_IS_PHANTOM));
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
			setStringValue_(ContactItem.CONTACTITEM_XSTATUSMSG, new String(""));
			setBooleanValue_(ContactItem.CONTACTITEM_NO_AUTH, noAuth);
			setBooleanValue_(ContactItem.CONTACTITEM_IS_TEMP, false);
			setBooleanValue_(ContactItem.CONTACTITEM_HAS_CHAT, false);
			setBooleanValue_(ContactItem.CONTACTITEM_ADDED, added);
			setIntValue_(ContactItem.CONTACTITEM_STATUS,
					ContactList.STATUS_OFFLINE);
			setIntValue_(ContactItem.CONTACTITEM_CAPABILITIES,
					Icq.CAPF_NO_INTERNAL);
			//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
			setBytesArray(ContactItem.CONTACTITEM_BUDDYICON_HASH, new byte[16]);
			setBytesArray(ContactItem.CONTACTITEM_BUDDYICON_HASH_READY, new byte[16]);
			//#sijapp cond.end#
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

			COLOR_NORMAL = Options.getSchemeColor(Options.CLRSCHHEME_TEXT, -1);
			COLOR_HASCHAT = Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING, -1);
			COLOR_FANTOM = 0x808080;

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

	//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
	/* Returns true if buddy icon can to be downloaded */
	synchronized public boolean iconReady()
	{
		if (Util.byteArrayIsEmpty(biHash, biHash.length))
			return false;
		
		if (Util.byteArrayEquals(biHash, 0, biHashDone, 0, biHash.length))
			return false;

		if ((buddyIcon != null) && (buddyIcon.length > 0))
			return false;

		return true;
	}
	//#sijapp cond.end#

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
				return COLOR_FANTOM;
			return getBooleanValue_(ContactItem.CONTACTITEM_HAS_CHAT) ? COLOR_HASCHAT : COLOR_NORMAL;
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
		if (getBooleanValue(CONTACTITEM_B_PLMESSAGES)) tempIndex = 13;
		else if (getBooleanValue(CONTACTITEM_B_URLMESSAGES)) tempIndex = 14;
		else if (getBooleanValue(CONTACTITEM_B_AUTREQUESTS)) tempIndex = 15;
		else if (getBooleanValue(CONTACTITEM_B_SYSNOTICES)) tempIndex = 16;
		else tempIndex = JimmUI.getStatusImageIndex(getIntValue(ContactItem.CONTACTITEM_STATUS));
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
	
	public void generateFormattedName ()
	{
		formattedName = getText(-1);
	}

	public synchronized String getText()
	{
		return formattedName;
	}

	private String getText(int type)
	{
		if (tmpStringBuffer.length() != 0)
			tmpStringBuffer.delete(0, tmpStringBuffer.length());
		
		synchronized (this)
		{
			if (getBooleanValue_(CONTACTITEM_NO_AUTH)) tmpStringBuffer.append("!");
			
			if (getBooleanValue_(CONTACTITEM_IS_PHANTOM))
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
	synchronized protected boolean isContainingUnreadMessages()
	{
		return getBooleanValue(
			CONTACTITEM_B_PLMESSAGES|
			CONTACTITEM_B_URLMESSAGES|
			CONTACTITEM_B_SYSNOTICES|
			CONTACTITEM_B_AUTREQUESTS
		);
	}

	public synchronized void resetUnreadMessages()
	{
		setBooleanValue(
				CONTACTITEM_B_PLMESSAGES|
				CONTACTITEM_B_URLMESSAGES|
				CONTACTITEM_B_SYSNOTICES|
				CONTACTITEM_B_AUTREQUESTS,
				false
			);		
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
			chat.activate();
		} 
		else
		/* Display menu */
		{
			JimmUI.showContactMenu(this);
		}
	}
	
	public boolean isScreenActive()
	{
		return JimmUI.isContactMenuActive(this);
	}
	

	/****************************************************************************/
	/****************************************************************************/
	/***/

	public void setStatusImage()
	{
		int imgIndex;

//#sijapp cond.if target isnot "DEFAULT"#		
		imgIndex = typing ? 13 : JimmUI.getStatusImageIndex(getIntValue(CONTACTITEM_STATUS));
//#sijapp cond.else#
		imgIndex = JimmUI.getStatusImageIndex(getIntValue(CONTACTITEM_STATUS));
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

