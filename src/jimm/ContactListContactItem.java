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
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis, Igor Palkin
 *******************************************************************************/

package jimm;

import java.util.Date;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Timer;
import javax.microedition.lcdui.*;

import jimm.JimmUI;
import jimm.comm.*;
import jimm.util.ResourceBundle;
import jimm.SplashCanvas;
import DrawControls.*;

public class ContactListContactItem extends ContactListItem implements CommandListener, VirtualListCommands
{
	// No capability
	public static final int CAP_NO_INTERNAL = 0x00000000;

	// Client unterstands type-2 messages
	public static final int CAP_AIM_SERVERRELAY_INTERNAL = 0x00000001;

	// Client unterstands UTF-8 messages
	public static final int CAP_UTF8_INTERNAL = 0x00000002;
	
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
	128 - 191 (10XXXXXX)  BOOLEAN
	192 - 224 (110XXXXX)  LONG
	225 - 255 (111XXXXX)  BYTE-ARRAY
	******************************************************************************/
	
	// Variable keys
	public static final int CONTACTITEM_UIN						= 0;   /* String */
	public static final int CONTACTITEM_NAME					= 1;   /* String */
	public static final int CONTACTITEM_TEXT					= 2;   /* String */
	public static final int CONTACTITEM_ID						= 64;  /* Integer */
	public static final int CONTACTITEM_GROUP					= 65;  /* Integer */
	public static final int CONTACTITEM_CAPABILITIES			= 66;  /* Integer */
	public static final int CONTACTITEM_PLAINMESSAGES			= 67;  /* Integer */
	public static final int CONTACTITEM_URLMESSAGES				= 68;  /* Integer */
	public static final int CONTACTITEM_SYSNOTICES				= 69;  /* Integer */
	public static final int CONTACTITEM_AUTREQUESTS				= 70;  /* Integer */
	public static final int CONTACTITEM_IDLE					= 71;  /* Integer */
	public static final int CONTACTITEM_VALUE_ADDED				= 128; /* Boolean */
	public static final int CONTACTITEM_VALUE_NO_AUTH			= 129; /* Boolean */
	public static final int CONTACTITEM_VALUE_CHAT_SHOWN		= 130; /* Boolean */
	public static final int CONTACTITEM_VALUE_IS_TEMP			= 131; /* Boolean */
	public static final int CONTACTITEM_VALUE_HAS_CHAT			= 132; /* Boolean */
	public static final int CONTACTITEM_VALUE_REQU_REASON		= 133; /* Boolean */
	public static final int CONTACTITEM_STATUS					= 192; /* Long */
	public static final int CONTACTITEM_SIGNON					= 194; /* Long */
	public static final int CONTACTITEM_ONLINE					= 195; /* Long */

	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// #sijapp cond.if modules_FILES is "true"#
	public static final int CONTACTITEM_INTERNAL_IP				= 225; /* Byte array */
	public static final int CONTACTITEM_EXTERNAL_IP				= 226; /* Byte array */
    public static final int CONTACTITEM_AUTH_COOKIE				= 193; /* Long */
	public static final int CONTACTITEM_DC_TYPE					= 72;  /* Integer */
	public static final int CONTACTITEM_ICQ_PROT				= 73;  /* Integer */
	public static final int CONTACTITEM_DC_PORT					= 3;   /* String */
	//  #sijapp cond.end#
	//  #sijapp cond.end#
	
	// Hashtable for the standard variables
	private Hashtable values;

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
			setIntValue (ContactListContactItem.CONTACTITEM_ID,Util.createRandomId());
		else
			setIntValue (ContactListContactItem.CONTACTITEM_ID,id);
		setIntValue (ContactListContactItem.CONTACTITEM_GROUP,group);
		setStringValue (ContactListContactItem.CONTACTITEM_UIN,uin);
		setStringValue (ContactListContactItem.CONTACTITEM_NAME,name);
		setBooleanValue (ContactListContactItem.CONTACTITEM_VALUE_NO_AUTH,noAuth);
		setBooleanValue (ContactListContactItem.CONTACTITEM_VALUE_IS_TEMP,false);
		setBooleanValue (ContactListContactItem.CONTACTITEM_VALUE_HAS_CHAT,false);
		setBooleanValue (ContactListContactItem.CONTACTITEM_VALUE_ADDED,added);
		setLongValue (ContactListContactItem.CONTACTITEM_STATUS,ContactList.STATUS_OFFLINE);
		setIntValue (ContactListContactItem.CONTACTITEM_CAPABILITIES,ContactListContactItem.CAP_NO_INTERNAL);
		setIntValue (ContactListContactItem.CONTACTITEM_PLAINMESSAGES,0);
		setIntValue (ContactListContactItem.CONTACTITEM_URLMESSAGES,0);
		setIntValue (ContactListContactItem.CONTACTITEM_SYSNOTICES,0);
		setIntValue (ContactListContactItem.CONTACTITEM_AUTREQUESTS,0);
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#
		setByteArrayValue (ContactListContactItem.CONTACTITEM_INTERNAL_IP,new byte[4]);
		setByteArrayValue (ContactListContactItem.CONTACTITEM_EXTERNAL_IP,new byte[4]);
		setStringValue (ContactListContactItem.CONTACTITEM_DC_PORT,"");
		setIntValue (ContactListContactItem.CONTACTITEM_DC_TYPE,-1);
		setIntValue (ContactListContactItem.CONTACTITEM_ICQ_PROT,0);
		setLongValue (ContactListContactItem.CONTACTITEM_AUTH_COOKIE,0);
		this.ft = null;
		//  #sijapp cond.end# 
		//  #sijapp cond.end# 
		setLongValue (ContactListContactItem.CONTACTITEM_SIGNON,-1);
		setLongValue (ContactListContactItem.CONTACTITEM_ONLINE,-1);
		setIntValue (ContactListContactItem.CONTACTITEM_IDLE,-1);
		setBooleanValue (ContactListContactItem.CONTACTITEM_VALUE_REQU_REASON,false);
	}
	
	// Constructor for an existing contact item
	public ContactListContactItem(int id, int group, String uin, String name, boolean noAuth, boolean added)
	{
	    values = new Hashtable();
	    this.init(id,group,uin,name,noAuth,added);
	}
	
	// Value retrieval methods (no type checking!)
	public synchronized String getStringValue(int key)
	{
		return ((String) values.get(new Integer(key)));
	}
	public synchronized int getIntValue(int key)
	{
	    return (((Integer) values.get(new Integer(key))).intValue());
	}
	public synchronized boolean getBooleanValue(int key)
	{
	    return (((Boolean) values.get(new Integer(key))).booleanValue());
	}
	public synchronized long getLongValue(int key)
	{
	    return (((Long) values.get(new Integer(key))).longValue());
	}
	public synchronized byte[] getByteArrayValue(int key)
	{
	    return (((byte[]) values.get(new Integer(key))));
	}

	// Value setting methods (no type checking!)
	public synchronized void setStringValue(int key, String value)
	{
		values.put(new Integer(key), value);
	}
	public synchronized void setIntValue(int key, int value)
	{
		values.put(new Integer(key), new Integer(value));
	}
	public synchronized void setBooleanValue(int key, boolean value)
	{
		values.put(new Integer(key), new Boolean(value));
	}
	public synchronized void setLongValue(int key, long value)
	{
		values.put(new Integer(key), new Long(value));
	}
	public synchronized void setByteArrayValue(int key, byte[] value)
	{
		values.put(new Integer(key), value);
	}
	
	// Returns true if client supports given capability
	public boolean hasCapability(int capability)
	{
		return ((capability & this.getIntValue(ContactListContactItem.CONTACTITEM_CAPABILITIES)) != 0x00000000);
	}
	
	private String lowerText = null;
	
	public String getLowerText()
	{
		if (lowerText == null) lowerText = getStringValue(ContactListContactItem.CONTACTITEM_NAME).toLowerCase();
		return lowerText;
	}
	
	// Returns color for contact name
	public int getTextColor()
	{
		if (getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_IS_TEMP)) return 0x808080;
		return 
		getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_HAS_CHAT) 
				? Options.getSchemeColor(Options.CLRSCHHEME_BLUE)
				: Options.getSchemeColor(Options.CLRSCHHEME_TEXT); 
	}
	
	// Returns font style for contact name 
	public int getFontStyle()
	{
		return getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_HAS_CHAT) ? Font.STYLE_BOLD : Font.STYLE_PLAIN;
	}

	// Returns imaghe index for contact
	public int getImageIndex()
	{
		int tempIndex = -1;
		
		if (isMessageAvailable(MESSAGE_PLAIN)) tempIndex = 8;
		else if (isMessageAvailable(MESSAGE_URL)) tempIndex = 9;
		else if (isMessageAvailable(MESSAGE_AUTH_REQUEST)) tempIndex = 11;
		else if (isMessageAvailable(MESSAGE_SYS_NOTICE) || getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_NO_AUTH)) tempIndex = 10;
		else tempIndex = getStatusImageIndex(getLongValue(ContactListContactItem.CONTACTITEM_STATUS));
		return tempIndex;
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
		ContactList.STATUS_ONLINE
	};
	
	private final static int[] imageIndexes = { 0, 1, 2, 3, 4, 5, 6, 7 };
	
	private final static String[] statusStrings = 
	{
		"status_away",
		"status_chat",
		"status_dnd",
		"status_invisible",
		"status_na",
		"status_occupied",
		"status_offline",
		"status_online"
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
			   getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_IS_TEMP); 
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
		Jimm.jimm.getChatHistoryRef().addMessage(getStringValue(ContactListContactItem.CONTACTITEM_UIN),message,this);
	}

	public synchronized void resetUnreadMessages()
	{
		this.setIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES,0);
		this.setIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES,0);
		this.setIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES,0);
	}

	// Delete the chat history
	public void deleteChatHistory()
	{
		Jimm.jimm.getChatHistoryRef().chatHistoryDelete(this.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
	}

	// Checks whether some other object is equal to this one
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ContactListContactItem)) return (false);
		ContactListContactItem ci = (ContactListContactItem) obj;
		return (this.getStringValue(ContactListContactItem.CONTACTITEM_UIN).equals(ci.getStringValue(ContactListContactItem.CONTACTITEM_UIN)) && (this.getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_IS_TEMP) == ci.getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_IS_TEMP)));
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

	public void onCursorMove(VirtualList sender) {}
	public void onItemSelected(VirtualList sender) {}
	public void onKeyPress(VirtualList sender, int keyCode,int type)
	{
		if (type == VirtualList.KEY_PRESSED)
		{
			String currUin;
			switch (sender.getGameAction(keyCode))
			{
			case Canvas.LEFT:
				currUin = ContactList.showNextPrevChat(false);
				Jimm.jimm.getChatHistoryRef().calcCounter(currUin);
				break;
				
			case Canvas.RIGHT:
				currUin = ContactList.showNextPrevChat(true);
				Jimm.jimm.getChatHistoryRef().calcCounter(currUin);
				break;
			}
		}

		switch (keyCode)
		{
		case Canvas.KEY_NUM0:
			callHotkeyAction(Options.getIntOption(Options.OPTION_EXT_CLKEY0), type);
			break;
			
		case Canvas.KEY_NUM4:
			callHotkeyAction(Options.getIntOption(Options.OPTION_EXT_CLKEY4), type);
			break;
			
		case Canvas.KEY_NUM6:
			callHotkeyAction(Options.getIntOption(Options.OPTION_EXT_CLKEY6), type);
			break;
			
		case Canvas.KEY_STAR:
			callHotkeyAction(Options.getIntOption(Options.OPTION_EXT_CLKEYSTAR), type);
			break;
			
		case Canvas.KEY_POUND:
			callHotkeyAction(Options.getIntOption(Options.OPTION_EXT_CLKEYPOUND), type);
			break;
			
		// #sijapp cond.if target is "SIEMENS2"#
		case -11:
			// This means the CALL button was pressed...
			callHotkeyAction(Options.getIntOption(Options.OPTION_EXT_CLKEYCALL), type);
			break;
		// #sijapp cond.end#
		}	
	}

	public void checkForInvis()
	{
		VisibilityCheckerAction act = new VisibilityCheckerAction(getStringValue(ContactListContactItem.CONTACTITEM_UIN), getStringValue(ContactListContactItem.CONTACTITEM_NAME));
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
	
	//#sijapp cond.if modules_HISTORY is "true" #
	public void showHistory()
	{
		HistoryStorage.showHistoryList(getStringValue(ContactListContactItem.CONTACTITEM_UIN), getStringValue(ContactListContactItem.CONTACTITEM_NAME));
	}
	//#sijapp cond.end#
	
	public void showInfo()
	{
		// Reqeust user information
		// Display splash canvas
		SplashCanvas.setMessage(ResourceBundle.getString("wait"));
		SplashCanvas.setProgress(0);
		Jimm.display.setCurrent(Jimm.jimm.getSplashCanvasRef());

		// Request info from server
		RequestInfoAction act1 = new RequestInfoAction(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
		try
		{
			Icq.requestAction(act1);
		}
		catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical()) return;
		}

		// Start timer
		Jimm.jimm.getTimerRef().schedule(new SplashCanvas.RequestInfoTimerTask(
				act1), 1000, 1000);
	}
	
	public void newMessage()
	{
		if (menuList == null) this.activate();
		menuList.setSelectedIndex(0, true);
		writeMessage(null);
	} 
	
	private static long pressedTime;
	private void callHotkeyAction(int actionNum, int keyType)
	{
		if (keyType == VirtualList.KEY_PRESSED)
		{
			pressedTime = System.currentTimeMillis();
			switch (actionNum)
			{
				case Options.HOTKEY_INVIS:
					this.checkForInvis();
					break;
					
				// #sijapp cond.if modules_HISTORY is "true" #
				case Options.HOTKEY_HISTORY:
					this.showHistory();
					break;
				// #sijapp cond.end#
				
				case Options.HOTKEY_INFO:
					this.showInfo();
					break;
					
				case Options.HOTKEY_NEWMSG:
					newMessage();
					break;
					
				case Options.HOTKEY_OPTIONS:
					Options.optionsForm.activate();
					break;
					
				case Options.HOTKEY_MENU:
					MainMenu.activate();
					break;
					
				case Options.HOTKEY_LOCK:
					SplashCanvas.lock();
					break;
				
				// #sijapp cond.if target is "MIDP2"#
				case Options.HOTKEY_MINIMIZE:
					Jimm.setMinimized(true);
					break;
				// #sijapp cond.end#
			}
		}
		else if ((keyType == VirtualList.KEY_REPEATED) || (keyType == VirtualList.KEY_RELEASED))
		{
			if (pressedTime == -1) return;
			long diff = System.currentTimeMillis()-pressedTime;
			if ((actionNum == Options.HOTKEY_LOCK) && (diff > 900))
			{
				pressedTime = -1;
				SplashCanvas.lock();
			}
		}
	}

	/** ************************************************************************* */
	/** ************************************************************************* */
	/** ************************************************************************* */

	static String lastAnsUIN = new String();
	static boolean repliedWithQuota = false;

	
	//private class Menu implements CommandListener
	//{
		final public static int MSGBS_DELETECONTACT = 1; 
		final public static int MSGBS_REMOVEME = 2;
		
		// Shows new message form 
		private void writeMessage(String initText)
		{
			// If user want reply with quotation 
			if (initText != null) messageTextbox.setString(initText);
			
			// Keep old text if press "cancel" while last edit 
			else if ( !lastAnsUIN.equals(getStringValue(ContactListContactItem.CONTACTITEM_UIN)) ) messageTextbox.setString(null);
			
			// Display textbox for entering messages
			messageTextbox.setTitle(ResourceBundle.getString("message")+" "+ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_NAME));
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
					Alert alert = new Alert( ResourceBundle.getString("text_too_long") );
					Jimm.display.setCurrent(alert, Jimm.display.getCurrent());
				}
			}
            
            // Menu item has been selected
            else if ((c == List.SELECT_COMMAND) 
            //#sijapp cond.if target is "MOTOROLA"#
            || (c == selectCommand)
            // #sijapp cond.end#
            )
            {
                switch(eventList[menuList.getSelectedIndex()])
                {
                case USER_MENU_MESSAGE: 
                    // Send plain message
                	writeMessage(null);
                    break;
                    
                case USER_MENU_QUOTA:
                    // Send plain message with quotation
                	menuList.setSelectedIndex(0, true);
                	repliedWithQuota = true;
                	writeMessage(JimmUI.getClipBoardText());
                    break;
                    
                case USER_MENU_URL:
                    // Send URL message
                    // Reset and display textbox for entering messages
                    messageTextbox.setString(null);
                    messageTextbox.setTitle(ResourceBundle.getString("send_url"));
                    clearMessBoxCommands();
                    messageTextbox.addCommand(textboxOkCommand);
                    messageTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(messageTextbox);
                    break;
                    
                case USER_MENU_STATUS_MESSAGE:
                    // Send a status message request message
                    if (!((ContactListContactItem.this.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_ONLINE) || (ContactListContactItem.this.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_OFFLINE) || (ContactListContactItem.this.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_INVISIBLE)))
                    {
                            int msgType;
                        // Send a status message request message
                        if (ContactListContactItem.this.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_AWAY)
                            msgType = Message.MESSAGE_TYPE_AWAY;
                        else if (ContactListContactItem.this.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) ==  ContactList.STATUS_OCCUPIED)
                            msgType = Message.MESSAGE_TYPE_OCC;
                        else if (ContactListContactItem.this.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_DND)
                            msgType = Message.MESSAGE_TYPE_DND;
                        else if (ContactListContactItem.this.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_CHAT)
                            msgType = Message.MESSAGE_TYPE_FFC;
                        else
                            msgType = Message.MESSAGE_TYPE_AWAY;
    
                        PlainMessage awayReq = new PlainMessage(Options.getStringOption(Options.OPTION_UIN), ContactListContactItem.this,msgType, new Date(), "");
                        
                        SendMessageAction act = new SendMessageAction(awayReq);
                        try
                        {
                        	Icq.requestAction(act);
    
                        } catch (JimmException e)
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
                    if (ContactListContactItem.this.getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT) < 8)
                    {
                        JimmException.handleException(new JimmException(190, 0, true));
                    }
                    else
                    {
                        ft = new FileTransfer(FileTransfer.FT_TYPE_FILE_BY_NAME,ContactListContactItem.this);
                        ft.startFT(); 
                    }
                    break;
                    
                 // #sijapp cond.if target isnot "MOTOROLA" #
                case USER_MENU_CAM_TRANS:
                    // Send a filetransfer with a camera image
                    // We can only make file transfers with ICQ clients prot V8 and up
                    if (ContactListContactItem.this.getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT) < 8)
                    {
                        JimmException.handleException(new JimmException(190, 0, true));
                    }
                    else
                    {
                        ft = new FileTransfer(FileTransfer.FT_TYPE_CAMERA_SNAPSHOT,ContactListContactItem.this);
                        ft.startFT(); 
                    }
                    break;
                // #sijapp cond.end#
                // #sijapp cond.end#
                // #sijapp cond.end#
                    
                case USER_MENU_USER_REMOVE:
                	JimmUI.messageBox
                    (
                        ResourceBundle.getString("remove")+"?",
                        ResourceBundle.getString("remove")+" "+ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_NAME)+"?",
                        JimmUI.MESBOX_OKCANCEL,
                        this,
                        MSGBS_DELETECONTACT
                    );
                    break;
                    
                case USER_MENU_REMOVE_ME:
                    // Remove me from other users contact list
                	
                	JimmUI.messageBox
                    (
                    	ResourceBundle.getString("remove_me")+"?",
						ResourceBundle.getString("remove_me_from")+ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_NAME)+"?",
						JimmUI.MESBOX_OKCANCEL,
						this,
						MSGBS_REMOVEME
                    );
                    break;
                    
                case USER_MENU_RENAME:
                    // Rename the contact local and on the server
                    // Reset and display textbox for entering name
                    messageTextbox.setTitle(ResourceBundle.getString("rename"));
                    messageTextbox.setString(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_NAME));
                    clearMessBoxCommands();
                    messageTextbox.addCommand(renameOkCommand);
                    messageTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(messageTextbox);
                    break;
                    
                // #sijapp cond.if modules_HISTORY is "true" #                    
                case USER_MENU_HISTORY:
                    // Stored history
					HistoryStorage.showHistoryList(getStringValue(ContactListContactItem.CONTACTITEM_UIN),getStringValue(ContactListContactItem.CONTACTITEM_NAME));
                    break;
                // #sijapp cond.end#
                    
                case USER_MENU_USER_INFO:
                	showInfo();
                	break;
                    
                // Show Timeing info and DC info 
                case USER_MENU_LOCAL_INFO:
                    Alert info = new Alert(ResourceBundle.getString("local_info"));
                    StringBuffer buf = new StringBuffer();
                    final String clrf = "\n";
                    if (getLongValue(ContactListContactItem.CONTACTITEM_SIGNON) > 0) {
                    	Date signon = new Date(this.getLongValue(ContactListContactItem.CONTACTITEM_SIGNON));
                    	buf.append(ResourceBundle.getString("li_signon_time")+": "+Util.getDateString(false,signon)+"\n");
                    }
                    if (getLongValue(ContactListContactItem.CONTACTITEM_ONLINE) > 0)
					{
						long online = this.getLongValue(ContactListContactItem.CONTACTITEM_ONLINE);
						buf.append(ResourceBundle.getString("li_online_time") + ": ");
						if ((online / 86400) != 0)
						{
							buf.append(online / 86400 + ResourceBundle.getString("days") +" ");
							online = online % 86400;
						}
						if ((online / 3600)  != 0)
						{
							buf.append(online / 3600 + ResourceBundle.getString("hours") +" ");
							online = online % 3600;
						}
						buf.append(online / 60 +  ResourceBundle.getString("minutes") +"\n");
					}
                    if (getIntValue(ContactListContactItem.CONTACTITEM_IDLE) > 0)
                    {
                    	int idleTime = this.getIntValue(ContactListContactItem.CONTACTITEM_IDLE);
                    	buf.append(ResourceBundle.getString("li_idle_time")+": ");
						if ((idleTime / 60) != 0)
							buf.append(idleTime/60+"h ");
                    	buf.append(idleTime%60+ResourceBundle.getString("minutes") +"\n");
                    }
                    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                    // #sijapp cond.if modules_FILES is "true"#    
                    buf.append("DC typ: ")     .append(this.getIntValue(ContactListContactItem.CONTACTITEM_DC_TYPE)).append(clrf)
					   .append("ICQ version: ").append(this.getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT)).append(clrf)
					   .append("Int IP: ")     .append(Util.ipToString(this.getByteArrayValue(ContactListContactItem.CONTACTITEM_INTERNAL_IP))).append(clrf)
					   .append("Ext IP: ")     .append(Util.ipToString(this.getByteArrayValue(ContactListContactItem.CONTACTITEM_EXTERNAL_IP))).append(clrf)
					   .append("Port: ")       .append(this.getStringValue(ContactListContactItem.CONTACTITEM_DC_PORT)).append(clrf);
                    // #sijapp cond.end#
                    // #sijapp cond.end# 
                    
                    info.setString(buf.toString());
                    info.setTimeout(Alert.FOREVER);
                    
                    Jimm.display.setCurrent(info);
                    break;   
                    
                case USER_MENU_REQU_AUTH:
                    // Request auth
                	
                	this.setBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_REQU_REASON,true);
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
				MainMenu.addUserOrGroupCmd(this.getStringValue(ContactListContactItem.CONTACTITEM_UIN),true);
			}
			
			// User adds selected message to history
			//#sijapp cond.if modules_HISTORY is "true" #
			else if (c == addToHistoryCommand)
			{
				Jimm.jimm.getChatHistoryRef().addTextToHistory(getStringValue(ContactListContactItem.CONTACTITEM_UIN));
			}
			//#sijapp cond.end#
			
			// "Copy text" command selected
			else if (c == copyTextCommand)
			{
				Jimm.jimm.getChatHistoryRef().copyText(getStringValue(ContactListContactItem.CONTACTITEM_UIN));
				getCurrDisplay().addCommand(replWithQuotaCommand);
			}
			
			// User wants to rename Contact
			else if (c == renameOkCommand)
			{
				ContactListContactItem.this.setStringValue(ContactListContactItem.CONTACTITEM_NAME,messageTextbox.getString());
				try 
				{
					// Save ContactList
					ContactList.save();
					
					// Try to save ContactList to server
					UpdateContactListAction action = new UpdateContactListAction(ContactListContactItem.this,UpdateContactListAction.ACTION_RENAME);
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
				
				Jimm.jimm.getChatHistoryRef().contactRenamed(getStringValue(ContactListContactItem.CONTACTITEM_UIN),getStringValue(ContactListContactItem.CONTACTITEM_NAME));
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
						this.activate();
					}


					// Send plain message
					if ((eventList[menuList.getSelectedIndex()] == USER_MENU_MESSAGE) 
							&& !messageTextbox.getString().equals(""))
					{
                        // Construct plain message object and request new SendMessageAction
						// Add the new message to the chat history
						PlainMessage plainMsg = new PlainMessage(Options.getStringOption(Options.OPTION_UIN),

					    ContactListContactItem.this,Message.MESSAGE_TYPE_NORM, new Date(), messageTextbox.getString());
						SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
						try
						{
							Icq.requestAction(sendMsgAct);
						} catch (JimmException e)
						{
							ContactList.activate(JimmException.handleException(e));
                            if (e.isCritical()) return;
						}
                        Jimm.jimm.getChatHistoryRef().addMyMessage(getStringValue(ContactListContactItem.CONTACTITEM_UIN),plainMsg.getText(),plainMsg.getDate(),getStringValue(ContactListContactItem.CONTACTITEM_NAME));
                        
                        // #sijapp cond.if modules_HISTORY is "true" #
                        if ( Options.getBooleanOption(Options.OPTION_HISTORY) )
                            HistoryStorage.addText(getStringValue(ContactListContactItem.CONTACTITEM_UIN), plainMsg.getText(), (byte)1, ResourceBundle.getString("me"), plainMsg.getDate());
                        // #sijapp cond.end#
                            
                        
						// Return to chat or menu
						this.activate();
						
						// Clear text in messageTextbox
						messageTextbox.setString(null);
						
						// Clear clipboard
						if (repliedWithQuota) JimmUI.clearClipBoardText();
						getCurrDisplay().removeCommand(replWithQuotaCommand);
						repliedWithQuota = false;
					}
					
					// Send URL message (continue creation)
					else if (eventList[menuList.getSelectedIndex()] == USER_MENU_URL)
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
						this.activate();
					}

					// Construct URL message object and request new
					// SendMessageAction
					UrlMessage urlMsg = new UrlMessage(Options.getStringOption(Options.OPTION_UIN), ContactListContactItem.this, Message.MESSAGE_TYPE_NORM,
							new Date(), urlTextbox.getString(), messageTextbox.getString());
					SendMessageAction sendMsgAct = new SendMessageAction(urlMsg);
					try
					{
						Icq.requestAction(sendMsgAct);
					} catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) return;
					}

					// Return to contact list
					this.activate();

				}
				// Reason has been entered
				else if (d == reasonTextbox)
				{
					
					SystemNotice notice;

					// Decrease the number of handled auth requests by one
					if (!getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_REQU_REASON)) 
						setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS,getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS)-1);

					// If or if not a reason was entered
					// Though this box is used twice (reason for auth request and auth repley)
					// we have to distinguish what we wanna do requReason is used for that
					if (reasonTextbox.getString().length() < 1)
					{
						if (getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_REQU_REASON))
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_REQUAUTH, ContactListContactItem.this
									.getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, "");
						else
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, ContactListContactItem.this
									.getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, "");
					} else
					{
						if (getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_REQU_REASON))
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_REQUAUTH, ContactListContactItem.this
									.getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, reasonTextbox.getString());
						else
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, ContactListContactItem.this
									.getStringValue(ContactListContactItem.CONTACTITEM_UIN), false, reasonTextbox.getString());
					}

					// Assemble the sysNotAction and request it
					SysNoticeAction sysNotAct = new SysNoticeAction(notice);
					UpdateContactListAction updateAct = new UpdateContactListAction(ContactListContactItem.this, UpdateContactListAction.ACTION_ADD);

					try
					{
						Icq.requestAction(sysNotAct);
						if (ContactListContactItem.this.getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_IS_TEMP)) Icq.requestAction(updateAct);
					} catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) return;
					}
					setBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_REQU_REASON,false);
					ContactList.activate();
				}
			}
			
			// user select Ok in delete contact message box
			else if (JimmUI.isMsgBoxCommand(c, MSGBS_DELETECONTACT) == JimmUI.CMD_OK)
			{
				Icq.delFromContactList(ContactListContactItem.this);
				//#sijapp cond.if modules_HISTORY is "true" #
				HistoryStorage.clearHistory(getStringValue(ContactListContactItem.CONTACTITEM_UIN));
				//#sijapp cond.end# 
			}
			
			// user select CANCEL in delete contact message box
			else if (JimmUI.isMsgBoxCommand(c, MSGBS_DELETECONTACT) == JimmUI.CMD_CANCEL)
			{
				this.activate();
			}

			// user select Ok in delete me message box
			else if (JimmUI.isMsgBoxCommand(c, MSGBS_REMOVEME) == JimmUI.CMD_OK)
			{
				RemoveMeAction remAct = new RemoveMeAction(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN));

				try
				{
					Icq.requestAction(remAct);
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
				ContactList.activate();
			}
			
			// user select CANCEL in delete contact message box
			else if (JimmUI.isMsgBoxCommand(c, MSGBS_REMOVEME) == JimmUI.CMD_CANCEL)
			{
				this.activate();
			}

			// Textbox has been canceled
			else if (c == textboxCancelCommand)
			{
				this.activate();
			}
			// Menu should be activated
			else if (c == addMenuCommand)
			{
				menuList.setTitle(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_NAME));
				menuList.setSelectedIndex(0, true);
				menuList.setCommandListener(this);
				Jimm.display.setCurrent(menuList);
			}
			
			// Delete chat history
			else if (c == deleteChatCommand)
			{
				ContactListContactItem.this.deleteChatHistory();
				ContactListContactItem.this.setBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_HAS_CHAT,false);
				ContactList.activate();
			}
			
			//Grant authorisation
			else if (c == grantAuthCommand)
			{
				setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS,getIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS)-1);
				SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, ContactListContactItem.this
						.getStringValue(ContactListContactItem.CONTACTITEM_UIN), true, "");
				SysNoticeAction sysNotAct = new SysNoticeAction(notice);
				try
				{
					Icq.requestAction(sysNotAct);
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
				this.activate();
			}
			//Deny authorisation OR request authorisation
			else if (c == denyAuthCommand || c == reqAuthCommand)
			{
				// Reset and display textbox for entering deney reason
				if (c == reqAuthCommand) setBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_REQU_REASON,true);
				if (c == reqAuthCommand)
					reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
				else
					reasonTextbox.setString(null);
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
			else if ( Emotions.isMyOkCommand(c) )
			{
				// #sijapp cond.if target is "MOTOROLA"#
				//caretPos = messageTextbox.getString().length();
				// #sijapp cond.end#
				
				messageTextbox.insert
				(
					new String(" ").concat(Emotions.getSelectedEmotion()).concat(" "),
					caretPos
				);
			}
			// #sijapp cond.end#

		}
		
		static private int caretPos;
		
		VirtualList getCurrDisplay()
		{
			return Jimm.jimm.getChatHistoryRef().getChatHistoryAt(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
		}
		
		// Activates the contact item menu
		public void activate()
		{
			currentUin = new String(getStringValue(ContactListContactItem.CONTACTITEM_UIN));
			
			//#sijapp cond.if modules_HISTORY is "true" #
			Jimm.jimm.getChatHistoryRef().fillFormHistory(getStringValue(ContactListContactItem.CONTACTITEM_UIN), getStringValue(ContactListContactItem.CONTACTITEM_NAME));
			//#sijapp cond.end#

			
			// Display chat history
			if (ContactListContactItem.this.getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_HAS_CHAT))
			{
				initList(ContactListContactItem.this.getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_NO_AUTH), this);
				VirtualList msgDisplay = getCurrDisplay();
                msgDisplay.removeCommand(addUrsCommand);
				msgDisplay.removeCommand(grantAuthCommand);
				msgDisplay.removeCommand(denyAuthCommand);
				msgDisplay.removeCommand(reqAuthCommand);
				msgDisplay.removeCommand(replWithQuotaCommand);
				msgDisplay.addCommand(copyTextCommand);
				msgDisplay.addCommand(msgCloseCommand);
				msgDisplay.addCommand(msgReplyCommand);
				msgDisplay.addCommand(deleteChatCommand);
				msgDisplay.addCommand(addMenuCommand);
				//#sijapp cond.if modules_HISTORY is "true" #
				msgDisplay.removeCommand(addToHistoryCommand);
				if ( !Options.getBooleanOption(Options.OPTION_HISTORY) )
					msgDisplay.addCommand(addToHistoryCommand);
				//#sijapp cond.end#
				if (ContactListContactItem.this.isMessageAvailable(ContactListContactItem.MESSAGE_AUTH_REQUEST))
				{
					msgDisplay.addCommand(grantAuthCommand);
					msgDisplay.addCommand(denyAuthCommand);
				}
				
				if (JimmUI.getClipBoardText() != null) msgDisplay.addCommand(replWithQuotaCommand);
				
				if (ContactListContactItem.this.getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_NO_AUTH)) msgDisplay.addCommand(reqAuthCommand);
				msgDisplay.setCommandListener(this);
				msgDisplay.setVLCommands(this);
				if (getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_IS_TEMP) && !getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_NO_AUTH)) 
                    msgDisplay.addCommand(addUrsCommand);
				Jimm.jimm.getChatHistoryRef().UpdateCaption(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
				// Display history
				ContactListContactItem.this.resetUnreadMessages();
				Jimm.display.setCurrent(msgDisplay);
				// #sijapp cond.if target is "MOTOROLA"#
				LightControl.flash(false);
				// #sijapp cond.end#

			}
			// Display menu
			else
			{
				initList(ContactListContactItem.this.getBooleanValue(ContactListContactItem.CONTACTITEM_VALUE_NO_AUTH), this);
                menuList.setTitle(ContactListContactItem.this.getStringValue(ContactListContactItem.CONTACTITEM_NAME));
				menuList.setSelectedIndex(0, true);
				menuList.setCommandListener(this);
				Jimm.display.setCurrent(menuList);
				// #sijapp cond.if target is "MOTOROLA"#
				LightControl.flash(true);
				// #sijapp cond.end#
			}
		}
	//}

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

	//#sijapp cond.if target is "MOTOROLA"#
	// Select command
	private static Command selectCommand = new Command(ResourceBundle.getString("select"), Command.OK, 2);
	//#sijapp cond.end#		

    // Message close command
    private static Command msgCloseCommand;
    
    // Message reply command
    private static Command msgReplyCommand;
    
    private static Command replWithQuotaCommand = new Command(ResourceBundle.getString("quote"), Command.ITEM, 3);
    
    private static Command copyTextCommand = new Command(ResourceBundle.getString("copy_text"), Command.ITEM, 4);

	// Add temporary user to contact list
	private static Command addUrsCommand = new Command(ResourceBundle.getString("add_user"), Command.ITEM, 5);
	
	// Add selected message to history 
	//#sijapp cond.if modules_HISTORY is "true" #
	private static Command addToHistoryCommand  = new Command(ResourceBundle.getString("add_to_history"), Command.ITEM, 6);
	//#sijapp cond.end#

	//Show the message menu
	private static Command addMenuCommand = new Command(ResourceBundle.getString("user_menu"), Command.ITEM, 7);

	//Delete Chat History
	private static Command deleteChatCommand = new Command(ResourceBundle.getString("delete_chat"), Command.ITEM, 8);

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
        // Size of the event list equals last entry number
        eventList = new int[USER_MENU_LAST_ITEM];
        menuList = new List("", List.IMPLICIT);
        
        // #sijapp cond.if target is "MOTOROLA"#
        menuList.addCommand(selectCommand);
        // #sijapp cond.end#
        menuList.addCommand(backCommand);
        
        // Add the needed elements to the event list
        eventList[menuList.append(ResourceBundle.getString("send_message"), null)] = USER_MENU_MESSAGE;
        
        if (JimmUI.getClipBoardText() != null)
        	eventList[menuList.append(ResourceBundle.getString("quote"), null)] = USER_MENU_QUOTA;
        
        eventList[menuList.append(ResourceBundle.getString("send_url"), null)]     = USER_MENU_URL;
        
        if (showAuthItem)
            eventList[menuList.append(ResourceBundle.getString("requauth"), null)] = USER_MENU_REQU_AUTH;
        
        if (item.getLongValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_OFFLINE)
        {
        	eventList[menuList.append(ResourceBundle.getString("invisible_check"), null)] = USER_MENU_INVIS_CHECK;
        }
        else
        {
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
        
        eventList[menuList.append(ResourceBundle.getString("remove"), null)]       = USER_MENU_USER_REMOVE;
        eventList[menuList.append(ResourceBundle.getString("remove_me"), null)]    = USER_MENU_REMOVE_ME;
        eventList[menuList.append(ResourceBundle.getString("rename"), null)]       = USER_MENU_RENAME;
        // #sijapp cond.if modules_HISTORY is "true" #
        eventList[menuList.append(ResourceBundle.getString("history"), null)]      = USER_MENU_HISTORY;
        // #sijapp cond.end#
        eventList[menuList.append(ResourceBundle.getString("info"), null)]         = USER_MENU_USER_INFO;
        eventList[menuList.append(ResourceBundle.getString("local_info"), null)]   = USER_MENU_LOCAL_INFO;
	}
	 
	static private Displayable getCurrDisplayable(String uin)
	{
		Displayable vis = null;
		if (messageTextbox.isShown()) vis = messageTextbox;
		else if (Jimm.jimm.getChatHistoryRef().chatHistoryShown(uin)) vis = Jimm.jimm.getChatHistoryRef().getChatHistoryAt(uin);
		else if (menuList != null) if (menuList.isShown()) vis = menuList;
		return vis;
	}
	
	// Shows popup window with text of received message
	static public void showPopupWindow(String uin, String name, String text)
	{
		if (Options.getBooleanOption(Options.OPTION_POPUP_WIN) == false) return;
		if (Jimm.jimm.getChatHistoryRef().chatHistoryShown(uin)) return;
		
		if (Jimm.display.getCurrent() instanceof Alert) return;
		
		// #sijapp cond.if target is "MIDP2"#
		String oldText = messageTextbox.isShown() ? messageTextbox.getString() : null;
		// #sijapp cond.end#
	
		Alert alert = new Alert(uin, "["+name+"]\n\n"+text, null, null);
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
		msgReplyCommand = new Command(ResourceBundle.getString("reply"),Command.OK, 2);
		renameOkCommand = new Command(ResourceBundle.getString("ok"),Command.OK, 2);
	    //#sijapp cond.else#
		msgCloseCommand = new Command(ResourceBundle.getString("close"),Command.BACK, 2);
		msgReplyCommand = new Command(ResourceBundle.getString("reply"),Command.OK, 1);
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
	
	static String getCaption(Displayable ctrl)
	{
		if (ctrl == null) return null;
		String result = null;
		if (ctrl instanceof VirtualList)
		{
			VirtualList vl = (VirtualList)ctrl;
			// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			result = vl.getTitle();
			// #sijapp cond.else#
			result = vl.getCaption();
			// #sijapp cond.end#
		}
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else result = ctrl.getTitle();
		// #sijapp cond.end#
		
		return result;
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
		this.oldText = getCaption(displ);
		this.counter = 8;
	}
	
	public void run()
	{
		if ((counter != 0) && displ.isShown())
		{
			setCaption(displ, ((counter&1) == 0) ? text : " ");
			counter--;
		}
		else
		{
			setCaption(displ, oldText);
			cancel();
		}
	}
	
	public void restoreCaption()
	{
		setCaption(displ, oldText);
	}
	
	static public void setCaption(Displayable ctrl, String caption)
	{
		if (ctrl instanceof VirtualList)
		{
			VirtualList vl = (VirtualList)ctrl;
			// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			vl.setTitle(caption);
			// #sijapp cond.else#
			vl.setCaption(caption);
			// #sijapp cond.end#
		}
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else ctrl.setTitle(caption);
		// #sijapp cond.end#
	}
	
	static String getCaption(Displayable ctrl)
	{
		if (ctrl == null) return null;
		String result = null;
		if (ctrl instanceof VirtualList)
		{
			VirtualList vl = (VirtualList)ctrl;
			// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			result = vl.getTitle();
			// #sijapp cond.else#
			result = vl.getCaption();
			// #sijapp cond.end#
		}
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else result = ctrl.getTitle();
		// #sijapp cond.end#
		
		return result;
	}
	
}
