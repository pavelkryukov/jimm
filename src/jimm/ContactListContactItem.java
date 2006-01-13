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
 File: src/jimm/ContactListContactItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis
 *******************************************************************************/

package jimm;

import java.util.TimerTask;
import javax.microedition.lcdui.*;

import jimm.JimmUI;
import jimm.comm.*;
import jimm.util.ResourceBundle;
import jimm.SplashCanvas;
import DrawControls.*;


public class ContactListContactItem implements CommandListener, ContactListItem
{
	private static final int SENDING_MESSAGE = 1;
	private static final int SENDING_URL     = 2;
	private static int sendingType; 
	
	
	// No capability
	public static final int CAP_NO_INTERNAL = 0x00000000;

	// Message types
	public static final int MESSAGE_PLAIN		 = 1;
	public static final int MESSAGE_URL		     = 2;
	public static final int MESSAGE_SYS_NOTICE   = 3;
	public static final int MESSAGE_AUTH_REQUEST = 4;
	
	/*******************************************************************************
	Persistant variables accessible with keys
	Variable key          Value
	  0 -  63 (00XXXXXX)  String
	 64 - 127 (01XXXXXX)  INTEGER
	192 - 224 (110XXXXX)  LONG
	225 - 255 (111XXXXX)  byte array
	******************************************************************************/
	
	private int    id,
	               caps,
	               group,
	               idle,
	               booleanValues,
	               plainAndUrlMessages, 
	               sysNoticesAndAuthRequests;
	//#sijapp cond.if modules_FILES is "true"#
	private int    dcType,
	               dcPort, 
	               icqProt,
	               clientId,
	               intIP,
	               extIP;
	
	private long   authCookie;
	
	// #sijapp cond.end #
	
	private long   uinLong,
	               online,
	               signOn,
	               status;
	               
	private String name,
	               clientVersion,
	               lowerText;
	
///////////////////////////////////////////////////////////////////////////
	
	synchronized public void setStringValue(int key, String value)
	{
		switch(key)
		{
		case CONTACTITEM_UIN:        uinLong = Long.parseLong(value); return;
		case CONTACTITEM_NAME:       name = value; lowerText = null; return;
		case CONTACTITEM_CLIVERSION: clientVersion = value; return;
		}
	}
	
	synchronized public String getStringValue(int key)
	{
		switch(key)
		{
		case CONTACTITEM_UIN:        return Long.toString(uinLong);
		case CONTACTITEM_NAME:       return name;
		case CONTACTITEM_CLIVERSION: return clientVersion;
		}
		return null;
	}
	
///////////////////////////////////////////////////////////////////////////
	
	synchronized public void setIntValue(int key, int value)
	{
		switch (key)
		{
		case CONTACTITEM_ID:            id = value;            return;
		case CONTACTITEM_GROUP:         group = value;         return;
		case CONTACTITEM_PLAINMESSAGES:
			plainAndUrlMessages = (plainAndUrlMessages & 0x0000FFFF) | (value << 16);
			return;
			
		case CONTACTITEM_URLMESSAGES:
			plainAndUrlMessages = (plainAndUrlMessages & 0xFFFF0000) | value;
			return;
			
		case CONTACTITEM_SYSNOTICES:
			sysNoticesAndAuthRequests = (sysNoticesAndAuthRequests & 0x0000FFFF) | (value << 16);
			return;
			
		case CONTACTITEM_AUTREQUESTS:
			sysNoticesAndAuthRequests = (sysNoticesAndAuthRequests & 0xFFFF0000) | value;
			return;
		
		case CONTACTITEM_IDLE:          idle = value;          return;
		case CONTACTITEM_CAPABILITIES:  caps = value;          return;
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		//#sijapp cond.if modules_FILES is "true"#
		case CONTACTITEM_DC_TYPE:       dcType = value;        return;
		case CONTACTITEM_ICQ_PROT:      icqProt = value;       return;
		case CONTACTITEM_DC_PORT:       dcPort = value;        return;
		case CONTACTITEM_CLIENT:     	clientId = value;      return;
		// #sijapp cond.end #
		// #sijapp cond.end #
		}
	}
	
	synchronized public int getIntValue(int key)
	{
		switch (key)
		{
		case CONTACTITEM_ID:            return id;
		case CONTACTITEM_GROUP:         return group;
		case CONTACTITEM_PLAINMESSAGES: return (plainAndUrlMessages & 0xFFFF0000) >> 16;
		case CONTACTITEM_URLMESSAGES:   return (plainAndUrlMessages & 0x0000FFFF);
		case CONTACTITEM_SYSNOTICES:    return (sysNoticesAndAuthRequests & 0xFFFF0000) >> 16;
		case CONTACTITEM_AUTREQUESTS:   return (sysNoticesAndAuthRequests & 0x0000FFFF);
		case CONTACTITEM_IDLE:          return idle;
		case CONTACTITEM_CAPABILITIES:  return caps;
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		//#sijapp cond.if modules_FILES is "true"#
		case CONTACTITEM_DC_TYPE:       return dcType;
		case CONTACTITEM_ICQ_PROT:      return icqProt;
		case CONTACTITEM_DC_PORT:       return dcPort;
		case CONTACTITEM_CLIENT:        return clientId;
		// #sijapp cond.end #
		// #sijapp cond.end #
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
		return (booleanValues&key) != 0;
	}
	
///////////////////////////////////////////////////////////////////////////
	
	synchronized public void setLongValue(int key, long value)
	{
		switch (key)
		{
		case CONTACTITEM_ONLINE:      online = value;     return;
		case CONTACTITEM_SIGNON:      signOn = value;     return;
		case CONTACTITEM_STATUS:      status = value;     return;
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		//#sijapp cond.if modules_FILES is "true"#
		case CONTACTITEM_AUTH_COOKIE: authCookie = value; return;
		//#sijapp cond.end #
		//#sijapp cond.end #
		}
	}
	
	synchronized public long getLongValue(int key)
	{
		switch (key)
		{
		case CONTACTITEM_ONLINE:      return online;
		case CONTACTITEM_SIGNON:      return signOn;
		case CONTACTITEM_STATUS:      return status;
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		//#sijapp cond.if modules_FILES is "true"#
		case CONTACTITEM_AUTH_COOKIE: return authCookie;
		//#sijapp cond.end #
		//#sijapp cond.end #
		}
		return 0;
	}
	
///////////////////////////////////////////////////////////////////////////
	
	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	//#sijapp cond.if modules_FILES is "true"#
	public static byte[] longIPToByteAray(int value)
	{
		if (value == 0) return null;
		return new byte[] 
		                { 
							(byte)( value&0x000000FF),
							(byte)((value&0x0000FF00) >> 8),
							(byte)((value&0x00FF0000) >> 16),
							(byte)((value&0xFF000000) >> 24)
						}; 
	}
	
	public static int arrayToLongIP(byte[] array)
	{
		if ((array == null) || (array.length < 4)) return 0;
		return   (int)array[0] & 0xFF       | 
		       (((int)array[1] & 0xFF)<< 8) | 
		       (((int)array[2] & 0xFF)<<16) |
		       (((int)array[3] & 0xFF)<<24);
	}

	synchronized public void setIPValue(int key, byte[] value)
	{
		switch (key)
		{
		case CONTACTITEM_INTERNAL_IP: intIP = arrayToLongIP(value); break;
		case CONTACTITEM_EXTERNAL_IP: extIP = arrayToLongIP(value); break;
		}
	}
	
	synchronized public byte[] getIPValue(int key)
	{
		switch (key)
		{
		case CONTACTITEM_INTERNAL_IP: return longIPToByteAray(intIP);
		case CONTACTITEM_EXTERNAL_IP: return longIPToByteAray(extIP);
		}
		return null;
	}
	
	//#sijapp cond.end #
	//#sijapp cond.end #
	
	// Variable keys
	public static final int CONTACTITEM_UIN           = 0;      /* String */
	public static final int CONTACTITEM_NAME          = 1;      /* String */
	public static final int CONTACTITEM_ID            = 64;     /* Integer */
	public static final int CONTACTITEM_GROUP         = 65;     /* Integer */
	public static final int CONTACTITEM_PLAINMESSAGES = 67;     /* Integer */
	public static final int CONTACTITEM_URLMESSAGES   = 68;     /* Integer */
	public static final int CONTACTITEM_SYSNOTICES    = 69;     /* Integer */
	public static final int CONTACTITEM_AUTREQUESTS   = 70;     /* Integer */
	public static final int CONTACTITEM_IDLE          = 71;     /* Integer */
	public static final int CONTACTITEM_ADDED         = 1 << 0; /* Boolean */
	public static final int CONTACTITEM_NO_AUTH       = 1 << 1; /* Boolean */
	public static final int CONTACTITEM_CHAT_SHOWN    = 1 << 2; /* Boolean */
	public static final int CONTACTITEM_IS_TEMP       = 1 << 3; /* Boolean */
	public static final int CONTACTITEM_HAS_CHAT      = 1 << 4; /* Boolean */
	public static final int CONTACTITEM_REQU_REASON   = 1 << 5; /* Boolean */
	public static final int CONTACTITEM_STATUS        = 192;    /* Long */
	public static final int CONTACTITEM_SIGNON        = 194;    /* Long */
	public static final int CONTACTITEM_ONLINE        = 195;    /* Long */
	
	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// #sijapp cond.if modules_FILES is "true"#
	public static final int CONTACTITEM_INTERNAL_IP   = 225;    /* IP address */
	public static final int CONTACTITEM_EXTERNAL_IP   = 226;    /* IP address */
	public static final int CONTACTITEM_AUTH_COOKIE   = 193;    /* Long */
	public static final int CONTACTITEM_DC_TYPE       = 72;     /* Integer */
	public static final int CONTACTITEM_ICQ_PROT      = 73;     /* Integer */
	public static final int CONTACTITEM_DC_PORT       = 74;     /* Integer */
	// #sijapp cond.end#
	// #sijapp cond.end#
	public static final int CONTACTITEM_CAPABILITIES  = 75;     /* Integer */
	public static final int CONTACTITEM_CLIENT        = 76;     /* Integer */
	public static final int CONTACTITEM_CLIVERSION    = 2;      /* String */	


	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// #sijapp cond.if modules_FILES is "true"#
	// DC values
	private FileTransferMessage ftm;
	private FileTransfer ft;
	//  #sijapp cond.end#
	//  #sijapp cond.end#
	
	public static String currentUin = new String();
	
	public void init(int id, int group, String uin, String name, boolean noAuth, boolean added)
	{
		if (id == -1)
			setIntValue(ContactListContactItem.CONTACTITEM_ID, Util.createRandomId());
		else
			setIntValue(ContactListContactItem.CONTACTITEM_ID, id);
		setIntValue(ContactListContactItem.CONTACTITEM_GROUP, group);
		setStringValue(ContactListContactItem.CONTACTITEM_UIN, uin);
		setStringValue(ContactListContactItem.CONTACTITEM_NAME, name);
		setBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH, noAuth);
		setBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP, false);
		setBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT, false);
		setBooleanValue(ContactListContactItem.CONTACTITEM_ADDED, added);
		setLongValue(ContactListContactItem.CONTACTITEM_STATUS, ContactList.STATUS_OFFLINE);
		setIntValue(ContactListContactItem.CONTACTITEM_CAPABILITIES, Util.CAPF_NO_INTERNAL);
		setIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS, 0);
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#
		setIPValue(ContactListContactItem.CONTACTITEM_INTERNAL_IP, new byte[4]);
		setIPValue(ContactListContactItem.CONTACTITEM_EXTERNAL_IP, new byte[4]);
		setIntValue(ContactListContactItem.CONTACTITEM_DC_PORT, 0);
		setIntValue(ContactListContactItem.CONTACTITEM_DC_TYPE, -1);
		setIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT, 0);
		setLongValue(ContactListContactItem.CONTACTITEM_AUTH_COOKIE, 0);
		this.ft = null;
		// #sijapp cond.end#
		// #sijapp cond.end#
		setLongValue(ContactListContactItem.CONTACTITEM_SIGNON, -1);
		online = -1;
		setIntValue(ContactListContactItem.CONTACTITEM_IDLE, -1);
		setBooleanValue(ContactListContactItem.CONTACTITEM_REQU_REASON, false);
		setIntValue(ContactListContactItem.CONTACTITEM_CLIENT, Util.CLI_NONE);
		setStringValue(ContactListContactItem.CONTACTITEM_CLIVERSION, "");
	}
	
	// Constructor for an existing contact item
	public ContactListContactItem(int id, int group, String uin, String name, boolean noAuth, boolean added)
	{
	    this.init(id,group,uin,name,noAuth,added);
	}
	
	public ContactListContactItem()
	{
	    this.init(-1, -1, null, null, false, false);
	}
	
	// Returns true if client supports given capability
	public boolean hasCapability(int capability)
	{
		return ((capability & this.caps) != 0x00000000);
	}
	
	public String getLowerText()
	{
		if (lowerText == null)
		{
			lowerText = name.toLowerCase();
			if (lowerText.equals(name)) lowerText = name; // to decrease memory usage 
		}
		return lowerText;
	}
	
	// Returns color for contact name
	public int getTextColor()
	{
		if (getBooleanValue(CONTACTITEM_IS_TEMP)) return 0x808080;
		int color = getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT) 
				? Options.getSchemeColor(Options.CLRSCHHEME_BLUE)
				: Options.getSchemeColor(Options.CLRSCHHEME_TEXT); 
		return color;
	}
	
	// Returns font style for contact name 
	public int getFontStyle()
	{
		return getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT) ? Font.STYLE_BOLD : Font.STYLE_PLAIN;
	}
	
	public long getUIN()
	{
		return uinLong;
	}

	// Returns imaghe index for contact
	public int getImageIndex()
	{
		int tempIndex = -1;
		
		if (isMessageAvailable(MESSAGE_PLAIN)) tempIndex = 8;
		else if (isMessageAvailable(MESSAGE_URL)) tempIndex = 9;
		else if (isMessageAvailable(MESSAGE_AUTH_REQUEST)) tempIndex = 11;
		else if (isMessageAvailable(MESSAGE_SYS_NOTICE)) tempIndex = 10;
		else tempIndex = getStatusImageIndex(getLongValue(ContactListContactItem.CONTACTITEM_STATUS));
		return tempIndex;
	}
	
	public String getText()
	{
		if (getBooleanValue(CONTACTITEM_NO_AUTH)) return "[!] "+name; 
		return name;
	}

	private final static long[] statuses = 
	{
		ContactList.STATUS_AWAY,
		ContactList.STATUS_CHAT,
		ContactList.STATUS_DND,
		ContactList.STATUS_INVISIBLE,
		ContactList.STATUS_NA,
		ContactList.STATUS_OCCUPIED,
		ContactList.STATUS_OFFLINE,
		ContactList.STATUS_ONLINE,
		ContactList.STATUS_INVIS_ALL,
	};
	
	private final static int[] imageIndexes = { 0, 1, 2, 3, 4, 5, 6, 7, 3 };
	
	private final static String[] statusStrings = 
	{
		"status_away",
		"status_chat",
		"status_dnd",
		"status_invisible",
		"status_na",
		"status_occupied",
		"status_offline",
		"status_online",
		"status_invis_all"
	};
	
	private static int getStatusIndex(long status)
	{
		for (int i = 0; i < statuses.length; i++) if (statuses[i] == status) return i;
		return -1;
	}
	
	public static int getStatusImageIndex(long status)
	{
		int index = getStatusIndex(status);
		return (index == -1) ? -1 : imageIndexes[index];
	}

	public static String getStatusString(long status)
	{
		int index = getStatusIndex(status);
		return (index == -1) ? null : ResourceBundle.getString(statusStrings[index]);
	}
	
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#  
	
	// Returns the fileTransfer Object of this contact
	public FileTransfer getFT()
	{
		return this.ft;
	}
	
	// Set the FileTransferMessage of this contact
	public void setFTM(FileTransferMessage _ftm)
	{
		this.ftm = _ftm;
	}

	// Returns the FileTransferMessage of this contact
	public FileTransferMessage getFTM()
	{
		return this.ftm;
	}
	//  #sijapp cond.end# 
	//  #sijapp cond.end# 
	
	// Returns true if contact must be shown even user offline
	// and "hide offline" is on
	protected boolean mustBeShownAnyWay()
	{
		return (getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES) > 0) ||
			   (getIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES) > 0)   || 
			   (getIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES) > 0)	||
			   (getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS) > 0)   || 
			   getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP); 
	}

	// Returns total count of all unread messages (messages, sys notices, urls, auths)
	protected int getUnreadMessCount()
	{
		return getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES)+
			getIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES)+
			getIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES)+
			getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS);
	}

	// Returns true if the next available message is a message of given type
	// Returns false if no message at all is available, or if the next available
	// message is of another type
	protected synchronized boolean isMessageAvailable(int type)
	{
		switch (type)
		{
			case MESSAGE_PLAIN:		return (this.getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
			case MESSAGE_URL:		  return (this.getIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES) > 0); 
			case MESSAGE_SYS_NOTICE:   return (this.getIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES) > 0);
			case MESSAGE_AUTH_REQUEST: return (this.getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS) > 0); 
		}
		return (this.getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES) > 0);
	}
	
	// Increases the mesage count
	protected synchronized void increaseMessageCount(int type)
	{ 
		System.out.println("msg++");
		switch (type)
		{
			case MESSAGE_PLAIN:		this.setIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES,this.getIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES)+1); break;
			case MESSAGE_URL:		  this.setIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES,this.getIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES)+1); break;
			case MESSAGE_SYS_NOTICE:   this.setIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES,this.getIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES)+1); break;
			case MESSAGE_AUTH_REQUEST: this.setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS,this.getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS)+1);
		}
	}

	// Adds a message to the message display
	protected synchronized void addMessage(Message message)
	{
		ChatHistory.addMessage(getStringValue(ContactListContactItem.CONTACTITEM_UIN), message,this);
	}

	public synchronized void resetUnreadMessages()
	{
		setIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES,0);
		setIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES,0);
		setIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES,0);
	}

	// Activates the contact item menu
	public void activateMenu()
	{
		this.activate(true);
	}

	// Checks whether some other object is equal to this one
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ContactListContactItem)) return (false);
		ContactListContactItem ci = (ContactListContactItem) obj;
		return (this.getStringValue(ContactListContactItem.CONTACTITEM_UIN).equals(ci.getStringValue(ContactListContactItem.CONTACTITEM_UIN)) && (this.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP) == ci.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP)));
	}
	
	private void clearMessBoxCommands()
	{
		messageTextbox.removeCommand(renameOkCommand);
		messageTextbox.removeCommand(textboxOkCommand);
		messageTextbox.removeCommand(textboxSendCommand);
		//#sijapp cond.if modules_SMILES is "true" #
		messageTextbox.removeCommand(insertEmotionCommand);
		// #sijapp cond.end#
	}


	public void checkForInvis()
	{
		if ( getLongValue(CONTACTITEM_STATUS) == ContactList.STATUS_OFFLINE )
		{
			VisibilityCheckerAction act = new VisibilityCheckerAction(this);
			try
			{
				Icq.requestAction(act);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}
		}
	}
	
	//#sijapp cond.if modules_HISTORY is "true" #
	public void showHistory()
	{
		HistoryStorage.showHistoryList(getStringValue(ContactListContactItem.CONTACTITEM_UIN), name);
	}
	//#sijapp cond.end#
	
	public void newMessage()
	{
		if (menuList == null) this.activate(true);
		menuList.setSelectedIndex(0, true);
		writeMessage(null);
	} 

	/** ************************************************************************* */
	/** ************************************************************************* */
	/** ************************************************************************* */

	private static String lastAnsUIN = new String();
	private static boolean repliedWithQuota = false;
	
	final public static int MSGBS_DELETECONTACT = 1;
	final public static int MSGBS_REMOVEME      = 2;
	final public static int SELECTOR_DEL_CHAT   = 3;

	// Shows new message form 
	private void writeMessage(String initText)
	{
		sendingType = SENDING_MESSAGE;
		
		// If user want reply with quotation 
		if (initText != null) messageTextbox.setString(initText);

		// Keep old text if press "cancel" while last edit 
		else if (!lastAnsUIN
				.equals(getStringValue(ContactListContactItem.CONTACTITEM_UIN))) messageTextbox
				.setString(null);

		// Display textbox for entering messages
		messageTextbox.setTitle(ResourceBundle.getString("message") + " "
				+ name);
		clearMessBoxCommands();
		//#sijapp cond.if modules_SMILES is "true" #
		messageTextbox.addCommand(insertEmotionCommand);
		// #sijapp cond.end#
		messageTextbox.addCommand(textboxSendCommand);
		messageTextbox.setCommandListener(this);
		Jimm.display.setCurrent(messageTextbox);
		lastAnsUIN = getStringValue(ContactListContactItem.CONTACTITEM_UIN);
	}

	// Command listener
	public void commandAction(Command c, Displayable d)
	{

		// #sijapp cond.if target is "MOTOROLA"#
		//Constantly turn on backlight
		LightControl.flash(true);
		// #sijapp cond.end#

		// Return to contact list
		if (c == backCommand)
		{
			ContactListContactItem.this.resetUnreadMessages();
			ContactList.activate();
		}
		// Message has been closed
		else if (c == msgCloseCommand)
		{
			ContactList.activate();
		}
		// User wants to send a reply
		else if ((c == msgReplyCommand) || (c == replWithQuotaCommand))
		{
			repliedWithQuota = (c == replWithQuotaCommand);

			// Select first list element (new message)
			menuList.setSelectedIndex(0, true);

			// Show message form
			try
			{
				writeMessage(repliedWithQuota ? JimmUI.getClipBoardText() : null);
			}
			catch (Exception e)
			{
				Alert alert = new Alert(ResourceBundle
						.getString("text_too_long"));
				Jimm.display.setCurrent(alert, Jimm.display.getCurrent());
			}
		}
		
		// Menu item has been selected
		else if (((c == List.SELECT_COMMAND) || (c == selectCommand)) && (d == menuList))
		{
			String uin = getStringValue(ContactListContactItem.CONTACTITEM_UIN);
			long status = getLongValue(ContactListContactItem.CONTACTITEM_STATUS);
			
			switch (eventList[menuList.getSelectedIndex()])
			{
			case USER_MENU_MESSAGE:
				// Send plain message
				writeMessage(null);
				break;

			case USER_MENU_QUOTA:
				// Send plain message with quotation
				repliedWithQuota = true;
				writeMessage(JimmUI.getClipBoardText());
				break;

			case USER_MENU_URL:
				// Send URL message
				// Reset and display textbox for entering messages
				sendingType = SENDING_URL;
				messageTextbox.setString(null);
				messageTextbox.setTitle(ResourceBundle.getString("send_url"));
				clearMessBoxCommands();
				messageTextbox.addCommand(textboxOkCommand);
				messageTextbox.setCommandListener(this);
				Jimm.display.setCurrent(messageTextbox);
				break;

			case USER_MENU_STATUS_MESSAGE:
				// Send a status message request message
				if (!((status == ContactList.STATUS_ONLINE)
						|| (status== ContactList.STATUS_OFFLINE) 
						|| (status == ContactList.STATUS_INVISIBLE)))
				{
					int msgType;
					// Send a status message request message
					if (status == ContactList.STATUS_AWAY) msgType = Message.MESSAGE_TYPE_AWAY;
					else if (status == ContactList.STATUS_OCCUPIED) msgType = Message.MESSAGE_TYPE_OCC;
					else if (status == ContactList.STATUS_DND) msgType = Message.MESSAGE_TYPE_DND;
					else if (status == ContactList.STATUS_CHAT) msgType = Message.MESSAGE_TYPE_FFC;
					else msgType = Message.MESSAGE_TYPE_AWAY;

					PlainMessage awayReq = new PlainMessage(Options.getStringOption(Options.OPTION_UIN), ContactListContactItem.this, msgType, Util.createCurrentDate(), "");

					SendMessageAction act = new SendMessageAction(awayReq);
					try
					{
						Icq.requestAction(act);

					}
					catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) return;
					}
				}
				break;

			// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			// #sijapp cond.if modules_FILES is "true"#                    
			case USER_MENU_FILE_TRANS:
				// Send a filetransfer with a file given by path
				// We can only make file transfers with ICQ clients prot V8 and up
				if (getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT) < 8)
				{
					JimmException.handleException(new JimmException(190, 0, true));
				}
				else
				{
					ft = new FileTransfer(FileTransfer.FT_TYPE_FILE_BY_NAME, this);
					ft.startFT();
				}
				break;

			// #sijapp cond.if target isnot "MOTOROLA" #
			case USER_MENU_CAM_TRANS:
				// Send a filetransfer with a camera image
				// We can only make file transfers with ICQ clients prot V8 and up
				if (getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT) < 8)
				{
					JimmException.handleException(new JimmException(190, 0, true));
				}
				else
				{
					ft = new FileTransfer(FileTransfer.FT_TYPE_CAMERA_SNAPSHOT, this);
					ft.startFT();
				}
				break;
			// #sijapp cond.end#
			// #sijapp cond.end#
			// #sijapp cond.end#

			case USER_MENU_USER_REMOVE:
				JimmUI.messageBox
				(
					ResourceBundle.getString("remove") + "?",
					ResourceBundle.getString("remove") + " " + name + "?",
					JimmUI.MESBOX_OKCANCEL, 
					this,
					MSGBS_DELETECONTACT
				);
				break;

			case USER_MENU_REMOVE_ME:
				// Remove me from other users contact list
				JimmUI.messageBox
				(
					ResourceBundle.getString("remove_me") + "?",
					ResourceBundle.getString("remove_me_from") + name + "?",
					JimmUI.MESBOX_OKCANCEL,
					this,
					MSGBS_REMOVEME
				);
				break;

			case USER_MENU_RENAME:
				// Rename the contact local and on the server
				// Reset and display textbox for entering name
				messageTextbox.setTitle(ResourceBundle.getString("rename"));
				messageTextbox.setString(name);
				clearMessBoxCommands();
				messageTextbox.addCommand(renameOkCommand);
				messageTextbox.setCommandListener(this);
				Jimm.display.setCurrent(messageTextbox);
				break;

			// #sijapp cond.if modules_HISTORY is "true" #                    
			case USER_MENU_HISTORY:
				// Stored history
				HistoryStorage.showHistoryList(uin, name);
				break;
			// #sijapp cond.end#

			case USER_MENU_USER_INFO:
				JimmUI.requiestUserInfo(uin);
				break;

			// Show Timeing info and DC info 
			case USER_MENU_LOCAL_INFO:
				TextList tlist = JimmUI.getInfoTextList(uin, true);
				String[] clInfoData = new String[JimmUI.UI_LAST_ID];
				
				// sign on time
				long signonTime = getLongValue(ContactListContactItem.CONTACTITEM_SIGNON); 
				if (signonTime > 0) clInfoData[JimmUI.UI_SIGNON] = Util.getDateString(false, Util.createDate(signonTime));
				
				// online time
				long onlineTime = getLongValue(ContactListContactItem.CONTACTITEM_ONLINE);
				if (onlineTime > 0) clInfoData[JimmUI.UI_ONLINETIME] = Util.longitudeToString(onlineTime);
				
				// idle time
				int idleTime = getIntValue(ContactListContactItem.CONTACTITEM_IDLE);
				if (idleTime > 0) clInfoData[JimmUI.UI_IDLE_TIME] = Util.longitudeToString(idleTime);
				
				//#sijapp cond.if (target="MIDP2" | target="MOTOROLA" | target="SIEMENS2") & modules_FILES="true"#
				
				// Client version
				int clientVers = getIntValue(CONTACTITEM_CLIENT);
				if (clientVers != Util.CLI_NONE) clInfoData[JimmUI.UI_ICQ_CLIENT] = Util.getClientString((byte)clientVers)+ " " + getStringValue(CONTACTITEM_CLIVERSION);
				
				// ICQ protocol version
				clInfoData[JimmUI.UI_ICQ_VERS] = Integer.toString(getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT));
				
				// Internal IP
				clInfoData[JimmUI.UI_INT_IP] = Util.ipToString(getIPValue(ContactListContactItem.CONTACTITEM_INTERNAL_IP));
				
				// External IP
				clInfoData[JimmUI.UI_EXT_IP] = Util.ipToString(getIPValue(ContactListContactItem.CONTACTITEM_EXTERNAL_IP));
				
				// Port
				int port = getIntValue(ContactListContactItem.CONTACTITEM_DC_PORT);
				if (port != 0) clInfoData[JimmUI.UI_PORT] = Integer.toString(port);
				
				//#sijapp cond.end#
				
				JimmUI.fillUserInfo(clInfoData, tlist);
				JimmUI.showInfoTextList(tlist);
				break;

			case USER_MENU_REQU_AUTH:
				// Request auth
				setBooleanValue(ContactListContactItem.CONTACTITEM_REQU_REASON, true);
				reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
				reasonTextbox.removeCommand(textboxOkCommand);
				reasonTextbox.addCommand(textboxSendCommand);
				reasonTextbox.setCommandListener(this);
				Jimm.display.setCurrent(reasonTextbox);
				break;

			case USER_MENU_INVIS_CHECK:
				checkForInvis();
				break;
			}
		}

		// User wants to add temporary contact
		else if (c == addUrsCommand)
		{
			MainMenu.addUserOrGroupCmd(getStringValue(ContactListContactItem.CONTACTITEM_UIN), true);
		}

		// User adds selected message to history
		//#sijapp cond.if modules_HISTORY is "true" #
		else if (c == addToHistoryCommand)
		{
			ChatHistory.addTextToHistory(getStringValue(ContactListContactItem.CONTACTITEM_UIN));
		}
		//#sijapp cond.end#

		// "Copy text" command selected
		else if (c == copyTextCommand)
		{
			ChatHistory.copyText(getStringValue(ContactListContactItem.CONTACTITEM_UIN));
			getCurrDisplay().addCommand(replWithQuotaCommand);
		}
		
		// Delete chat history
		else if (c == deleteChatCommand)
		{
			JimmUI.showSelector
			(
				"delete_chat",
				JimmUI.stdSelector,
				this,
				SELECTOR_DEL_CHAT
			);
		}
		
		else if (JimmUI.getCommandType(c, SELECTOR_DEL_CHAT) == JimmUI.CMD_OK)
		{
			String uin = getStringValue(ContactListContactItem.CONTACTITEM_UIN);
			int delType = -1;
			
			switch (JimmUI.getLastSelIndex())
			{
			case 0: delType = ChatHistory.DEL_TYPE_CURRENT;        break;
			case 1: delType = ChatHistory.DEL_TYPE_ALL_EXCEPT_CUR; break;
			case 2: delType = ChatHistory.DEL_TYPE_ALL;            break;
			}
			
			ChatHistory.chatHistoryDelete(uin, delType);
			ContactList.activate();
			return;
		}

		// User wants to rename Contact
		else if (c == renameOkCommand)
		{
			name = messageTextbox.getString();
			try
			{
				// Save ContactList
				ContactList.save();

				// Try to save ContactList to server
				UpdateContactListAction action = new UpdateContactListAction(this, UpdateContactListAction.ACTION_RENAME);
				Icq.requestAction(action);
			}
			catch (JimmException je)
			{
				messageTextbox.setString(null);
				if (je.isCritical()) return;
			}
			catch (Exception e)
			{
				// Do nothing
			}

			ChatHistory.contactRenamed(getStringValue(ContactListContactItem.CONTACTITEM_UIN), name);
			ContactList.activate();
			messageTextbox.setString(null);
		}
		// Textbox has been closed
		else if ((c == textboxOkCommand) || (c == textboxSendCommand))
		{
			// Message has been entered
			if (d == messageTextbox)
			{
				// Abort if nothing has been entered
				if (messageTextbox.getString().length() < 1)
				{
					this.activate(true);
				}

				// Send plain message
				if ((sendingType == SENDING_MESSAGE) && !messageTextbox.getString().equals(""))
				{
					// Send message via icq
					sendMessage(messageTextbox.getString());

					// Return to chat or menu
					this.activate(true);

					// Clear text in messageTextbox
					messageTextbox.setString(null);

					// Clear clipboard
					if (repliedWithQuota) JimmUI.clearClipBoardText();
					getCurrDisplay().removeCommand(replWithQuotaCommand);
					repliedWithQuota = false;
				}

				// Send URL message (continue creation)
				else if (sendingType == SENDING_URL)
				{
					// Reset and display textbox for entering URLs
					urlTextbox.setString(null);
					urlTextbox.setCommandListener(this);
					Jimm.display.setCurrent(urlTextbox);
				}

			}
			// URL has been entered
			else if (d == urlTextbox)
			{

				// Abort if nothing has been entered
				if (urlTextbox.getString().length() < 1)
				{
					this.activate(true);
				}

				// Construct URL message object and request new
				// SendMessageAction
				UrlMessage urlMsg = new UrlMessage(Options.getStringOption(Options.OPTION_UIN), this, Message.MESSAGE_TYPE_NORM, Util.createCurrentDate(), urlTextbox.getString(), messageTextbox.getString());
				SendMessageAction sendMsgAct = new SendMessageAction(urlMsg);
				try
				{
					Icq.requestAction(sendMsgAct);
				}
				catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}

				// Return to contact list
				this.activate(true);

			}
			// Reason has been entered
			else if (d == reasonTextbox)
			{

				SystemNotice notice;
				boolean reqReason = getBooleanValue(ContactListContactItem.CONTACTITEM_REQU_REASON); 

				// Decrease the number of handled auth requests by one
				if (!reqReason) setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS, getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS) - 1);

				// If or if not a reason was entered
				// Though this box is used twice (reason for auth request and auth repley)
				// we have to distinguish what we wanna do requReason is used for that
				if (reasonTextbox.getString().length() < 1)
				{
					if (reqReason) 
						notice = new SystemNotice(SystemNotice.SYS_NOTICE_REQUAUTH, getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, "");
					else
						notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE,getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, "");
				}
				else
				{
					if (reqReason)
						notice = new SystemNotice(SystemNotice.SYS_NOTICE_REQUAUTH, getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, reasonTextbox.getString());
					else
						notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, reasonTextbox.getString());
				}

				// Assemble the sysNotAction and request it
				SysNoticeAction sysNotAct = new SysNoticeAction(notice);
				UpdateContactListAction updateAct = new UpdateContactListAction(this, UpdateContactListAction.ACTION_ADD);

				try
				{
					Icq.requestAction(sysNotAct);
					if (getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP)) Icq.requestAction(updateAct);
				}
				catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
				setBooleanValue(ContactListContactItem.CONTACTITEM_REQU_REASON, false);
				ContactList.activate();
			}
		}

		// user select Ok in delete contact message box
		else if (JimmUI.getCommandType(c, MSGBS_DELETECONTACT) == JimmUI.CMD_OK)
		{
			Icq.delFromContactList(ContactListContactItem.this);
			//#sijapp cond.if modules_HISTORY is "true" #
			HistoryStorage.clearHistory(getStringValue(ContactListContactItem.CONTACTITEM_UIN));
			//#sijapp cond.end# 
		}

		// user select CANCEL in delete contact message box
		else if (JimmUI.getCommandType(c, MSGBS_DELETECONTACT) == JimmUI.CMD_CANCEL)
		{
			this.activate(true);
		}

		// user select Ok in delete me message box
		else if (JimmUI.getCommandType(c, MSGBS_REMOVEME) == JimmUI.CMD_OK)
		{
			RemoveMeAction remAct = new RemoveMeAction(getStringValue(ContactListContactItem.CONTACTITEM_UIN));

			try
			{
				Icq.requestAction(remAct);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}
			ContactList.activate();
		}

		// user select CANCEL in delete contact message box
		else if (JimmUI.getCommandType(c, MSGBS_REMOVEME) == JimmUI.CMD_CANCEL)
		{
			this.activate(true);
		}

		// Textbox has been canceled
		else if (c == textboxCancelCommand)
		{
			this.activate(true);
		}
		// Menu should be activated
		else if (c == addMenuCommand)
		{
			menuList.setTitle(name);
			menuList.setSelectedIndex(0, true);
			menuList.setCommandListener(this);
			Jimm.display.setCurrent(menuList);
		}

		//Grant authorisation
		else if (c == grantAuthCommand)
		{
			setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS, getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS) - 1);
			SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, getStringValue(ContactListContactItem.CONTACTITEM_UIN), true, "");
			SysNoticeAction sysNotAct = new SysNoticeAction(notice);
			try
			{
				Icq.requestAction(sysNotAct);
			}
			catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}
			this.activate(true);
		}
		//Deny authorisation OR request authorisation
		else if (c == denyAuthCommand || c == reqAuthCommand)
		{
			// Reset and display textbox for entering deney reason
			if (c == reqAuthCommand) setBooleanValue(ContactListContactItem.CONTACTITEM_REQU_REASON, true);
			if (c == reqAuthCommand) reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
			else reasonTextbox.setString(null);
			reasonTextbox.removeCommand(textboxOkCommand);
			reasonTextbox.addCommand(textboxSendCommand);
			reasonTextbox.setCommandListener(this);
			Jimm.display.setCurrent(reasonTextbox);
		}

		// User wants to insert emotion in text
		//#sijapp cond.if modules_SMILES is "true" # 
		else if (c == insertEmotionCommand)
		{
			caretPos = messageTextbox.getCaretPosition();
			Emotions.selectEmotion(this, messageTextbox);
		}

		// User select a emotion
		else if (Emotions.isMyOkCommand(c))
		{
			// #sijapp cond.if target is "MOTOROLA"#
			caretPos = messageTextbox.getString().length();
			// #sijapp cond.end#

			messageTextbox.insert(" " + Emotions.getSelectedEmotion() + " ", caretPos);
		}
		// #sijapp cond.end#
	}
		
	public void sendMessage(String text)
	{
		// Construct plain message object and request new SendMessageAction
		// Add the new message to the chat history

		if (text == null) return;
		if (text.length() == 0) return;
		
		PlainMessage plainMsg = 
			new PlainMessage
			    (
			    	Options.getStringOption(Options.OPTION_UIN),
			    	this,
			    	Message.MESSAGE_TYPE_NORM,
			    	Util.createCurrentDate(),
			    	text
			    );
			
		SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
		try
		{
			Icq.requestAction(sendMsgAct);
		} catch (JimmException e)
		{
			ContactList.activate(JimmException.handleException(e));
			if (e.isCritical()) return;
		}
		ChatHistory.addMyMessage(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN), text, plainMsg.getNewDate(), name);

		// #sijapp cond.if modules_HISTORY is "true" #
		if ( Options.getBooleanOption(Options.OPTION_HISTORY) )
			HistoryStorage.addText(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN), text, (byte)1, ResourceBundle.getString("me"), plainMsg.getNewDate());
		// #sijapp cond.end#
	}
		
	static private int caretPos;
		
	private Displayable getCurrDisplay()
	{
		return ChatHistory.getChatHistoryAt(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN)).getDisplayable();
	}
		
	// Activates the contact item menu
	public void activate(boolean initChat)
	{
		currentUin = getStringValue(ContactListContactItem.CONTACTITEM_UIN);
		
		//#sijapp cond.if modules_HISTORY is "true" #
		ChatHistory.fillFormHistory(getStringValue(ContactListContactItem.CONTACTITEM_UIN), name);
		//#sijapp cond.end#
			
		// Display chat history
		if (getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT))
		{
			initList(getBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH), this);
			Displayable msgDisplay = getCurrDisplay();

			msgDisplay.removeCommand(addUrsCommand);
			msgDisplay.removeCommand(grantAuthCommand);
			msgDisplay.removeCommand(denyAuthCommand);
			msgDisplay.removeCommand(reqAuthCommand);
			msgDisplay.removeCommand(replWithQuotaCommand);
			msgDisplay.removeCommand(msgReplyCommand);
			msgDisplay.addCommand(copyTextCommand);
			msgDisplay.addCommand(msgCloseCommand);
			
			// #sijapp cond.if target is "SIEMENS2"#
			if (!Options.getBooleanOption(Options.OPTION_CLASSIC_CHAT)) msgDisplay.addCommand(msgReplyCommand);
			//#sijapp cond.else#
			msgDisplay.addCommand(msgReplyCommand);
			//#sijapp cond.end#
				
			msgDisplay.addCommand(deleteChatCommand);
			msgDisplay.addCommand(addMenuCommand);
			//#sijapp cond.if modules_HISTORY is "true" #
			msgDisplay.removeCommand(addToHistoryCommand);
			if ( !Options.getBooleanOption(Options.OPTION_HISTORY) )
				msgDisplay.addCommand(addToHistoryCommand);
			//#sijapp cond.end#
			if (isMessageAvailable(ContactListContactItem.MESSAGE_AUTH_REQUEST))
			{
				msgDisplay.addCommand(grantAuthCommand);
				msgDisplay.addCommand(denyAuthCommand);
			}
			
			if (JimmUI.getClipBoardText() != null) msgDisplay.addCommand(replWithQuotaCommand);
			
			if (getBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH)) msgDisplay.addCommand(reqAuthCommand);
				msgDisplay.setCommandListener(this);
				
			if (getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP) && !getBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH)) 
                   msgDisplay.addCommand(addUrsCommand);
			
			ChatHistory.UpdateCaption(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
			
			// Display history
			resetUnreadMessages();
			ChatHistory.getChatHistoryAt( getStringValue(ContactListContactItem.CONTACTITEM_UIN) ).activate(initChat, !currentUin.equals(lastAnsUIN));
			lastAnsUIN = currentUin;
				
			// #sijapp cond.if target is "MOTOROLA"#
			LightControl.flash(false);
			// #sijapp cond.end#
		}
		// Display menu
		else
		{
			initList(getBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH), this);
			menuList.setTitle(name);
			menuList.setSelectedIndex(0, true);
			menuList.setCommandListener(this);
			Jimm.display.setCurrent(menuList);
			// #sijapp cond.if target is "MOTOROLA"#
			LightControl.flash(true);
			// #sijapp cond.end#
		}
	}

	/****************************************************************************/
	/****************************************************************************/
	/****************************************************************************/
    
    // Static constants for menu actios
    private static final int USER_MENU_MESSAGE          = 1;
    private static final int USER_MENU_URL              = 2;
    private static final int USER_MENU_STATUS_MESSAGE   = 3;
    private static final int USER_MENU_REQU_AUTH        = 4;
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
    private static final int USER_MENU_FILE_TRANS       = 5;
    // #sijapp cond.if target isnot "MOTOROLA" #
    private static final int USER_MENU_CAM_TRANS        = 6;
    // #sijapp cond.end#
    // #sijapp cond.end#
    // #sijapp cond.end#        
    private static final int USER_MENU_USER_REMOVE      = 7;
    private static final int USER_MENU_REMOVE_ME        = 8;
    private static final int USER_MENU_RENAME           = 9;
    // #sijapp cond.if modules_HISTORY is "true"#         
    private static final int USER_MENU_HISTORY          = 10;
    // #sijapp cond.end#
    private static final int USER_MENU_LOCAL_INFO       = 11;
    private static final int USER_MENU_USER_INFO        = 12;
    private static final int USER_MENU_QUOTA            = 14;
    private static final int USER_MENU_INVIS_CHECK      = 15;
    
    private static final int USER_MENU_LAST_ITEM        = 16; // YOU NEED TO CHANGE IT!

    
	// Menu list
	private static List menuList;
    
    // Event list for menu event handler
    private static int[] eventList;

	// Textbox for entering messages
	private static TextBox messageTextbox;

	// Textbox for entering URLs
	private static TextBox urlTextbox;

	// Textbox for entering a reason
	private static TextBox reasonTextbox;
	
	// Abort command
	private static Command backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);

	private static Command selectCommand = new Command(ResourceBundle.getString("select"), Command.OK, 2);
	
	// Message close command
    private static Command msgCloseCommand;
    
    // Message reply command
    private static Command msgReplyCommand;
    
    private static Command replWithQuotaCommand = new Command(ResourceBundle.getString("quote"), Command.ITEM, 3);
    
    private static Command copyTextCommand = new Command(ResourceBundle.getString("copy_text"), Command.ITEM, 4);

	// Add temporary user to contact list
	private static Command addUrsCommand = new Command(ResourceBundle.getString("add_user", ResourceBundle.FLAG_ELLIPSIS), Command.ITEM, 5);
	
	// Add selected message to history 
	//#sijapp cond.if modules_HISTORY is "true" #
	private static Command addToHistoryCommand  = new Command(ResourceBundle.getString("add_to_history"), Command.ITEM, 6);
	//#sijapp cond.end#

	//Show the message menu
	private static Command addMenuCommand = new Command(ResourceBundle.getString("user_menu"), Command.ITEM, 7);

	//Delete Chat History
	private static Command deleteChatCommand = new Command(ResourceBundle.getString("delete_chat", ResourceBundle.FLAG_ELLIPSIS), Command.ITEM, 8);

	// Textbox OK command
	private static Command textboxOkCommand = new Command(ResourceBundle.getString("ok"), Command.OK, 2);

	// Textbox Send command
	private static Command textboxSendCommand = new Command(ResourceBundle.getString("send"), Command.OK, 1);

	// Textbox cancel command
	private static Command textboxCancelCommand = new Command(ResourceBundle.getString("cancel"), Command.BACK, 3);

	// Grand authorisation a for authorisation asking contact
	private static Command grantAuthCommand = new Command(ResourceBundle.getString("grant"), Command.OK, 1);

	// Deny authorisation a for authorisation asking contact
	private static Command denyAuthCommand = new Command(ResourceBundle.getString("deny"), Command.CANCEL, 1);

	// Request authorisation from a contact
	private static Command reqAuthCommand = new Command(ResourceBundle.getString("requauth"), Command.ITEM, 1);

	// Insert imotion (smile) in text
	
	//#sijapp cond.if modules_SMILES is "true" #
	private static Command insertEmotionCommand = new Command(ResourceBundle.getString("insert_emotion"), Command.ITEM, 3);
	// #sijapp cond.end#
    
    // Rename a contact
    private static Command renameOkCommand;
    
	static void initList(boolean showAuthItem, ContactListContactItem item)
	{
		long status = item.getLongValue(ContactListContactItem.CONTACTITEM_STATUS);
		
        // Size of the event list equals last entry number
        eventList = new int[USER_MENU_LAST_ITEM];
        menuList = new List("", List.IMPLICIT);
        
        // #sijapp cond.if target is "MOTOROLA"#
        menuList.addCommand(selectCommand);
        // #sijapp cond.end#
        menuList.addCommand(backCommand);
        
        // Add the needed elements to the event list
        if (showAuthItem)
            eventList[menuList.append(ResourceBundle.getString("requauth", ResourceBundle.FLAG_ELLIPSIS), null)] = USER_MENU_REQU_AUTH;
        
        eventList[menuList.append(ResourceBundle.getString("send_message", ResourceBundle.FLAG_ELLIPSIS), null)] = USER_MENU_MESSAGE;
        
        if (JimmUI.getClipBoardText() != null)
        	eventList[menuList.append(ResourceBundle.getString("quote"), null)] = USER_MENU_QUOTA;
        
        eventList[menuList.append(ResourceBundle.getString("send_url", ResourceBundle.FLAG_ELLIPSIS), null)]     = USER_MENU_URL;
        
        eventList[menuList.append(ResourceBundle.getString("info"), null)]      = USER_MENU_USER_INFO;
        
        if (status == ContactList.STATUS_OFFLINE)
        {
        	eventList[menuList.append(ResourceBundle.getString("invisible_check"), null)] = USER_MENU_INVIS_CHECK;
        }
        else
        {
        	if (status != ContactList.STATUS_ONLINE)
        		eventList[menuList.append(ResourceBundle.getString("reqstatmsg"), null)] = USER_MENU_STATUS_MESSAGE;
        }
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_FILES is "true"#
		if (item.getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT) >= 8)
		{
			eventList[menuList.append(ResourceBundle.getString("ft_name"), null)] = USER_MENU_FILE_TRANS;
			// #sijapp cond.if target isnot "MOTOROLA"#
			eventList[menuList.append(ResourceBundle.getString("ft_cam"), null)] = USER_MENU_CAM_TRANS;
			// #sijapp cond.end#
		}
		// #sijapp cond.end#
		// #sijapp cond.end#
        
        eventList[menuList.append(ResourceBundle.getString("remove", ResourceBundle.FLAG_ELLIPSIS), null)]    = USER_MENU_USER_REMOVE;
        eventList[menuList.append(ResourceBundle.getString("remove_me", ResourceBundle.FLAG_ELLIPSIS), null)] = USER_MENU_REMOVE_ME;
        eventList[menuList.append(ResourceBundle.getString("rename", ResourceBundle.FLAG_ELLIPSIS), null)]    = USER_MENU_RENAME;
        // #sijapp cond.if modules_HISTORY is "true" #
        eventList[menuList.append(ResourceBundle.getString("history"), null)]   = USER_MENU_HISTORY;
        // #sijapp cond.end#
        
        if (status != ContactList.STATUS_OFFLINE)
        	eventList[menuList.append(ResourceBundle.getString("dc_info"), null)]   = USER_MENU_LOCAL_INFO;
	}
	 
	static private Displayable getCurrDisplayable(String uin)
	{
		Displayable vis = null;
		if (messageTextbox.isShown()) vis = messageTextbox;
		else if (ChatHistory.chatHistoryShown(uin)) vis = ChatHistory.getChatHistoryAt(uin).getDisplayable();
		else if (menuList != null) if (menuList.isShown()) vis = menuList;
		return vis;
	}
	
	// Shows popup window with text of received message
	static public void showPopupWindow(String uin, String name, String text)
	{
		if (SplashCanvas.locked()) return;
		
		boolean haveToShow = false;
		boolean chatVisible = ChatHistory.chatHistoryShown(uin);
		boolean uinEquals = uin.equals(currentUin); 
		
		switch (Options.getIntOption(Options.OPTION_POPUP_WIN2) )
		{
		case 0: return;
		case 1:
			haveToShow = !chatVisible & uinEquals;
			break;
		case 2:
			haveToShow = !chatVisible || (chatVisible && !uinEquals);
			break;
		}
		
		if (!haveToShow) return;
		
		String textToAdd = "["+name+"]\n"+text;
		
		if (Jimm.display.getCurrent() instanceof Alert)
		{
			Alert currAlert = (Alert)Jimm.display.getCurrent();
			if (currAlert.getImage() != null) currAlert.setImage(null);
			currAlert.setString(currAlert.getString()+"\n\n"+textToAdd);
			return;
		}
		
		// #sijapp cond.if target is "MIDP2"#
		String oldText = messageTextbox.isShown() ? messageTextbox.getString() : null;
		// #sijapp cond.end#
	
		Alert alert = new Alert(ResourceBundle.getString("message"), textToAdd, null, null);
		alert.setTimeout(Alert.FOREVER);
		
		Jimm.display.setCurrent(alert);
		
		// #sijapp cond.if target is "MIDP2"#
		if (Jimm.is_phone_SE() && (oldText != null)) messageTextbox.setString(oldText); 
		// #sijapp cond.end#
	}
	
	// Timer task for flashing form caption
	private static FlashCapClass lastFlashTask = null;

	// flashs form caption when current contact have changed status
	static synchronized public void statusChanged(String uin, long status)
	{
		if (currentUin.equals(uin))
		{
			Displayable disp = getCurrDisplayable(uin);
			if (disp != null)
			{
				if (lastFlashTask != null)
				{
					lastFlashTask.restoreCaption();
					lastFlashTask.cancel();
				}
				lastFlashTask = new FlashCapClass(disp, getStatusString(status));;
				Jimm.jimm.getTimerRef().scheduleAtFixedRate(lastFlashTask, 0, 500);
			}
		}	 
	}

	// Initializer
	static
	{
		// Initialize the textbox for entering messages
		
		
	    //#sijapp cond.if target is "MOTOROLA"#
		msgCloseCommand = new Command(ResourceBundle.getString("close"),Command.BACK, 2);
		msgReplyCommand = new Command(ResourceBundle.getString("reply", ResourceBundle.FLAG_ELLIPSIS),Command.OK, 2);
		renameOkCommand = new Command(ResourceBundle.getString("ok"),Command.OK, 2);
	    //#sijapp cond.else#
		msgCloseCommand = new Command(ResourceBundle.getString("close"),Command.BACK, 2);
		msgReplyCommand = new Command(ResourceBundle.getString("reply", ResourceBundle.FLAG_ELLIPSIS),Command.OK, 1);
		renameOkCommand = new Command(ResourceBundle.getString("rename"),Command.OK, 2);
	    //#sijapp cond.end#
	
		
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#		
		messageTextbox = new TextBox(ResourceBundle.getString("message"), null, 1000, TextField.ANY|TextField.INITIAL_CAPS_SENTENCE);
		//#sijapp cond.else#
		messageTextbox = new TextBox(ResourceBundle.getString("message"), null, 1000, TextField.ANY);
		//#sijapp cond.end#
		
		messageTextbox.addCommand(textboxCancelCommand);

		// Initialize the textbox for entering URLs
		urlTextbox = new TextBox(ResourceBundle.getString("url"), null, 1000, TextField.URL);
		urlTextbox.addCommand(textboxCancelCommand);
		urlTextbox.addCommand(textboxSendCommand);

		// Initialize the textfor for entering reasons
		reasonTextbox = new TextBox(ResourceBundle.getString("reason"), null, 1000, TextField.ANY);
		reasonTextbox.addCommand(textboxCancelCommand);
		reasonTextbox.addCommand(textboxSendCommand);
	}
}

class FlashCapClass extends TimerTask
{
	private Displayable displ;
	private String text, oldText;
	private int counter;
	
	public FlashCapClass(Displayable displ, String text)
	{
		this.displ   = displ;
		this.text    = text;
		this.oldText = JimmUI.getCaption(displ);
		this.counter = 8;
	}
	
	public void run()
	{
		if ((counter != 0) && displ.isShown())
		{
			JimmUI.setCaption(displ, ((counter&1) == 0) ? text : " ");
			counter--;
		}
		else
		{
			JimmUI.setCaption(displ, oldText);
			cancel();
		}
	}
	
	public void restoreCaption()
	{
		JimmUI.setCaption(displ, oldText);
	}

}
