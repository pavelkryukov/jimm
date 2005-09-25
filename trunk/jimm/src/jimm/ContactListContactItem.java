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
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.util.Date;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import java.util.*;

import jimm.JimmUI;
import jimm.comm.*;
import jimm.util.ResourceBundle;
// #sijapp cond.if target is "MOTOROLA"#
import DrawControls.*;
// #sijapp cond.end#


public class ContactListContactItem extends ContactListItem implements CommandListener
{

	// No capability
	public static final int CAP_NO_INTERNAL = 0x00000000;

	// Client unterstands type-2 messages
	public static final int CAP_AIM_SERVERRELAY_INTERNAL = 0x00000001;

	// Client unterstands UTF-8 messages
	public static final int CAP_UTF8_INTERNAL = 0x00000002;
	
	// Message types
	public static final int MESSAGE_PLAIN		= 1;
	public static final int MESSAGE_URL		  = 2;
	public static final int MESSAGE_SYS_NOTICE   = 3;
	public static final int MESSAGE_AUTH_REQUEST = 4;
	
	// Value types
	public static final int VALUE_ADDED 		 = 5;
	public static final int VALUE_NO_AUTH		 = 6;
	public static final int VALUE_CHAT_SHOWN	 = 7;
	public static final int VALUE_IS_TEMP 		 = 8;
	public static final int VALUE_HAS_CHAT		 = 9;

	/** ************************************************************************* */

	// Persistent variables
	private int id;
	private int group;
	private String uin;
	private String name;
	private boolean noAuth;

	// Transient variables
	private boolean temporary;
	private boolean requReason;
	private boolean added;
	private long status;
	private int capabilities;
	
	
	// Message count
	private int plainMessages;
	private int urlMessages;
	private int sysNotices;
	private int authRequest;
	
	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// #sijapp cond.if modules_FILES is "true"#
	// DC values
	private byte[] internalIP;
	private byte[] externalIP;
	private String dcPort;
	private int dcType;
	private int icqProt;
	private long authCookie;
	// #sijapp cond.end#
	// #sijapp cond.end#
	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// #sijapp cond.if modules_FILES is "true"#
	private FileTransferMessage ftm;
	private FileTransfer ft;
	//  #sijapp cond.end#
	//  #sijapp cond.end#

	// Constructor for an existing contact item
	public ContactListContactItem(int id, int group, String uin, String name, boolean noAuth, boolean added)
	{
		this.id = id;
		this.group = group;
		this.uin = new String(uin);
		this.name = new String(name);
		this.noAuth = noAuth;
		this.temporary = false;
		this.added = added;
		this.status = ContactList.STATUS_OFFLINE;
		this.capabilities = ContactListContactItem.CAP_NO_INTERNAL;
		this.plainMessages = 0;
		this.urlMessages = 0;
		this.sysNotices = 0;
		this.authRequest = 0;
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#
		this.internalIP = new byte[4];
		this.externalIP = new byte[4];
		this.dcPort = "";
		this.dcType = -1;
		this.icqProt = 0;
		this.authCookie = 0;
		// #sijapp cond.end#
		// #sijapp cond.end#
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#
		this.ft = null;
		//  #sijapp cond.end# 
		//  #sijapp cond.end# 
		this.requReason = false;
	}
	
	// Constructor for a new contact item
	public ContactListContactItem(int group, String uin, String name, boolean noAuth, boolean added)
	{
		this.id = Util.createRandomId();
		this.group = group;
		this.uin = new String(uin);
		this.name = new String(name);
		this.noAuth = noAuth;
		this.temporary = false;
		this.added = added;
		this.status = ContactList.STATUS_OFFLINE;
		this.capabilities = ContactListContactItem.CAP_NO_INTERNAL;
		this.plainMessages = 0;
		this.urlMessages = 0;
		this.sysNotices = 0;
		this.authRequest = 0;
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		// #sijapp cond.if modules_FILES is "true"#
		this.internalIP = new byte[4];
		this.externalIP = new byte[4];
		this.dcPort = "";
		this.dcType = -1;
		this.icqProt = 0;
		this.authCookie = 0;
		this.ft = null;
		// #sijapp cond.end#
		// #sijapp cond.end#
		this.requReason = false;
	}
	


	// Retruns boolean value by property
	public boolean returnBoolValue(int value)
	{
		switch (value)
		{
		case VALUE_ADDED: return (this.added);
		case VALUE_NO_AUTH: return (noAuth);
		case VALUE_CHAT_SHOWN: return Jimm.jimm.getChatHistoryRef().chatHistoryShown(this.uin);
		case VALUE_IS_TEMP: return (this.temporary);
		case VALUE_HAS_CHAT:  return Jimm.jimm.getChatHistoryRef().chatHistoryExists(this.uin); 
		default: return false;
		}
	}
	
	// Sets boolean value by property
	public void setBoolValue(int value,boolean bool_value)
	{
		switch (value)
		{
		case VALUE_ADDED: this.added = bool_value; break;
		case VALUE_NO_AUTH: this.noAuth = bool_value; break;
		case VALUE_IS_TEMP: this.temporary = bool_value;
		}
	}
	
	// Returns the contact item id
	public int getId()
	{
		return (this.id);
	}

	// Sets the contact item id
	public void setId(int id)
	{
		this.id = id;
	}

	// Returns the group item id to which this contact item belongs
	public int getGroup()
	{
		return (this.group);
	}

	// Sets the group item id to which this contact item belongs
	public void setGroup(int group)
	{
		this.group = group;
	}

	// Returns the uin of this contact item
	public String getUin()
	{
		return (new String(this.uin));
	}

	// Sets the uin of this contact item
	public void setUin(String uin)
	{
		this.uin = new String(uin);
	}

	// Returns the name of this contact item
	public String getName()
	{
		return (new String(this.name));
	}

	// Sets the name of this contact item
	public void setName(String name)
	{
		this.name = new String(name);
	}
	
	// Returns the status of this contact item
	public long getStatus()
	{
		return (this.status);
	}

	// Sets the status of this contact item
	public void setStatus(long status)
	{
		this.status = Util.translateStatusReceived(status);
	}

	// Returns true if client supports given capability
	public boolean hasCapability(int capability)
	{
		return ((capability & this.capabilities) != 0x00000000);
	}

	// Sets client capabilities
	public void setCapabilities(int capabilities)
	{
		this.capabilities = capabilities;
	}
	
	public String getText()
	{
		return name;
	}
	
	private String lowerText = null;
	
	public String getLowerText()
	{
		if (lowerText == null) lowerText = name.toLowerCase();
		return lowerText;
	}
	
	// Returns true if contact must be shown even user offline
	// and "hide offline" is on
	protected boolean mustBeShownAnyWay()
	{
		return (plainMessages > 0) ||
			   (urlMessages > 0)   || 
			   (sysNotices > 0)	||
			   (authRequest > 0)   || 
			   temporary; 
	}
	
	// Returns total count of all unread messages (messages, sys notices, urls, auths)
	protected int getUnreadMessCount()
	{
		return plainMessages+urlMessages+sysNotices+authRequest;
	}
	
	// Returns color for contact name
	public int getTextColor()
	{
		if (temporary) return 0x808080;
		return 
			returnBoolValue(VALUE_HAS_CHAT) 
				? Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_BLUE)
				: Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_TEXT); 
	}
	
	// Returns font style for contact name 
	public int getFontStyle()
	{
		return returnBoolValue(VALUE_HAS_CHAT) ? Font.STYLE_BOLD : Font.STYLE_PLAIN;
	}
	

	// Returns imaghe index for contact
	public int getImageIndex()
	{
		int tempIndex = -1;
		
		if (isMessageAvailable(MESSAGE_PLAIN)) tempIndex = 8;
		else if (isMessageAvailable(MESSAGE_URL)) tempIndex = 9;
		else if (isMessageAvailable(MESSAGE_AUTH_REQUEST)) tempIndex = 11;
		else if (isMessageAvailable(MESSAGE_SYS_NOTICE) || returnBoolValue(VALUE_NO_AUTH)) tempIndex = 10;
		else tempIndex = getStatusImageIndex(status);
		return tempIndex;
	}
	
	public static int getStatusImageIndex(long status)
	{
		if (status == ContactList.STATUS_AWAY)	  return 0;
		else if (status == ContactList.STATUS_CHAT)	 return  1;
		else if (status == ContactList.STATUS_DND)	   return 2;
		else if (status == ContactList.STATUS_INVISIBLE) return 3;
		else if (status == ContactList.STATUS_NA)		return 4;
		else if (status == ContactList.STATUS_OCCUPIED)  return 5;
		else if (status == ContactList.STATUS_OFFLINE)   return 6;
		else if (status == ContactList.STATUS_ONLINE)	return  7;
		return -1;
	}
	
	// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// #sijapp cond.if modules_FILES is "true"#
	// Sets the DC values
	public void setDCValues(byte[] _internalIP,String _dcPort,int _dcType,int _icqProt,long _authCookie)
	{
		this.internalIP = _internalIP;
		this.dcPort = _dcPort;
		this.dcType = _dcType;
		this.icqProt = _icqProt;
		this.authCookie = _authCookie;
	}
	
	// Update ip and port values
	public void updateIPsandPort(byte[] _ip,byte[] _extIP,String _port)
	{
		this.internalIP = _ip;
		this.externalIP = _extIP;
		this.dcPort = _port;
	}
	
	// Return the DC auth cookie
	public long getDCAuthCookie()
	{
		return this.authCookie;
	}
	
	// Return ICQ version of this ContactListContactITem
	public int getICQVersion()
	{
		return this.icqProt;
	}
	
	// Return IP
	public byte[] getInternalIP()
	{
		return (this.internalIP);
	}
	
	// Return IP
	public byte[] getExternalIP()
	{
		return (this.externalIP);
	}
	
	// Return IP
	public int getDCType()
	{
		return (this.dcType);
	}
	
	// Return port
	public String getPort()
	{
		return (this.dcPort);
	}
	
	// Returns the fileTransfer Object of this contact
	public FileTransfer getFT()
	{
		return this.ft;
	}
	
	// Returns the FileTransferMessage of this contact
	public FileTransferMessage getFTM()
	{
		return this.ftm;
	}
	
	// Set the FileTransferMessage of this contact
	public void setFTM(FileTransferMessage _ftm)
	{
		this.ftm = _ftm;
	}	
	// #sijapp cond.end#
	// #sijapp cond.end#

	
	// Returns true if the next available message is a message of given type
	// Returns false if no message at all is available, or if the next available
	// message is of another type
	protected synchronized boolean isMessageAvailable(int type)
	{
		switch (type)
		{
			case MESSAGE_PLAIN:		return (this.plainMessages > 0);
			case MESSAGE_URL:		  return (this.urlMessages > 0); 
			case MESSAGE_SYS_NOTICE:   return (this.sysNotices > 0);
			case MESSAGE_AUTH_REQUEST: return (this.authRequest > 0); 
		}
		return (this.plainMessages > 0);
	}
	
	// Increases the mesage count
	protected synchronized void increaseMessageCount(int type)
	{ 
		switch (type)
		{
			case MESSAGE_PLAIN:		this.plainMessages++; break;
			case MESSAGE_URL:		  this.urlMessages++; break;
			case MESSAGE_SYS_NOTICE:   this.sysNotices++; break;
			case MESSAGE_AUTH_REQUEST: this.authRequest++;
		}
	}

	// Adds a message to the message display
	protected synchronized void addMessage(Message message)
	{
		Jimm.jimm.getChatHistoryRef().addMessage(uin,message,this);
	}

	public synchronized void resetUnreadMessages()
	{
		plainMessages = 0;
		urlMessages = 0;
		sysNotices = 0;
	}

	// Delete the chat history
	public void deleteChatHistory()
	{
		Jimm.jimm.getChatHistoryRef().chatHistoryDelete(uin);
	}

	// Activates the contact item menu
	public void activateMenu()
	{
		this.activate();
	}

	// Checks whether some other object is equal to this one
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ContactListContactItem)) return (false);
		ContactListContactItem ci = (ContactListContactItem) obj;
		return (this.uin.equals(ci.getUin()) && (this.temporary == ci.returnBoolValue(VALUE_IS_TEMP)));
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
			else if ( !lastAnsUIN.equals(getUin()) ) messageTextbox.setString(null);
			
			// Display textbox for entering messages
			messageTextbox.setTitle(ResourceBundle.getString("message")+" "+ContactListContactItem.this.getName());
			messageTextbox.addCommand(textboxSendCommand);
			messageTextbox.setCommandListener(this);
			Jimm.display.setCurrent(messageTextbox);
			lastAnsUIN = getUin();
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
				Jimm.jimm.getContactListRef().activate();
			}
			// Message has been closed
			else if (c == msgCloseCommand)
			{
				Jimm.jimm.getContactListRef().activate();
			}
			// User wants to send a reply
			else if ((c == msgReplyCommand) || (c == replWithQuotaCommand))
			{
				repliedWithQuota = (c == replWithQuotaCommand); 
				
				// Select first list element (new message)
				menuList.setSelectedIndex(0, true);
				
				// Show message form
				writeMessage(repliedWithQuota ? JimmUI.getClipBoardText() : null);
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
                    messageTextbox.addCommand(textboxOkCommand);
                    messageTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(messageTextbox);
                    break;
                    
                case USER_MENU_STATUS_MESSAGE:
                    // Send a status message request message
                    if (!((ContactListContactItem.this.getStatus() == ContactList.STATUS_ONLINE) || (ContactListContactItem.this.getStatus() == ContactList.STATUS_OFFLINE) || (ContactListContactItem.this.getStatus() == ContactList.STATUS_INVISIBLE)))
                    {
                            int msgType;
                        // Send a status message request message
                        if (ContactListContactItem.this.getStatus() == ContactList.STATUS_AWAY)
                            msgType = Message.MESSAGE_TYPE_AWAY;
                        else if (ContactListContactItem.this.getStatus() ==  ContactList.STATUS_OCCUPIED)
                            msgType = Message.MESSAGE_TYPE_OCC;
                        else if (ContactListContactItem.this.getStatus() == ContactList.STATUS_DND)
                            msgType = Message.MESSAGE_TYPE_DND;
                        else if (ContactListContactItem.this.getStatus() == ContactList.STATUS_CHAT)
                            msgType = Message.MESSAGE_TYPE_FFC;
                        else
                            msgType = Message.MESSAGE_TYPE_AWAY;
    
                        PlainMessage awayReq = new PlainMessage(Jimm.jimm.getIcqRef().getUin(), ContactListContactItem.this,msgType, new Date(), "");
                        SendMessageAction act = new SendMessageAction(awayReq);
                        try
                        {
                            Jimm.jimm.getIcqRef().requestAction(act);
    
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
                    if (ContactListContactItem.this.getICQVersion() < 8)
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
                    if (ContactListContactItem.this.getICQVersion() < 8)
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
                        ResourceBundle.getString("remove")+" "+ContactListContactItem.this.getName()+"?",
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
						ResourceBundle.getString("remove_me_from")+ContactListContactItem.this.getName()+"?",
						JimmUI.MESBOX_OKCANCEL,
						this,
						MSGBS_REMOVEME
                    );
                    break;
                    
                case USER_MENU_RENAME:
                    // Rename the contact local and on the server
                    // Reset and display textbox for entering name
                    messageTextbox.setTitle(ResourceBundle.getString("rename"));
                    messageTextbox.setString(ContactListContactItem.this.getName());
                    messageTextbox.addCommand(renameOkCommand);
                    messageTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(messageTextbox);
                    break;
                    
                // #sijapp cond.if modules_HISTORY is "true" #                    
                case USER_MENU_HISTORY:
                    // Stored history
                    Jimm.jimm.getHistory().showHistoryList(getUin(), getName());
                    System.out.println("History start: GOOD :)");
                    break;
                // #sijapp cond.end#
                    
                case USER_MENU_USER_INFO:
                    // Reqeust user information
                    // Display splash canvas
                    SplashCanvas wait1 = Jimm.jimm.getSplashCanvasRef();
                    wait1.setMessage(ResourceBundle.getString("wait"));
                    wait1.setProgress(0);
                    Jimm.display.setCurrent(wait1);

                    // Request info from server
                    RequestInfoAction act1 = new RequestInfoAction(ContactListContactItem.this.getUin());
                    try
                    {
                        Jimm.jimm.getIcqRef().requestAction(act1);
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) return;
                    }

                    // Start timer
                    Jimm.jimm.getTimerRef().schedule(new SplashCanvas.RequestInfoTimerTask(act1), 1000, 1000);
                    break;
                    
                // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                // #sijapp cond.if modules_FILES is "true"#                     
                case USER_MENU_DC_INFO:
                    Alert info = new Alert("DC Infos");
                    StringBuffer buf = new StringBuffer();
                    final String clrf = "\n";
                    
                    buf.append("DC typ: ")     .append(ContactListContactItem.this.getDCType())                     .append(clrf)
					   .append("ICQ version: ").append(ContactListContactItem.this.getICQVersion())                 .append(clrf)
					   .append("Int IP: ")     .append(Util.ipToString(ContactListContactItem.this.getInternalIP())).append(clrf)
					   .append("Ext IP: ")     .append(Util.ipToString(ContactListContactItem.this.getExternalIP())).append(clrf)
					   .append("Port: ")       .append(ContactListContactItem.this.getPort())                       .append(clrf);
                    
                    info.setString(buf.toString());
                    info.setTimeout(Alert.FOREVER);
                    
                    Jimm.display.setCurrent(info);
                    break;
                // #sijapp cond.end#
                // #sijapp cond.end#    
                    
                case USER_MENU_REQU_AUTH:
                    // Request auth
                    requReason = true;
                    reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
                    reasonTextbox.removeCommand(textboxOkCommand);
                    reasonTextbox.addCommand(textboxSendCommand);
                    reasonTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(reasonTextbox);
                    break;
                }
            }
			
			// User wants to add temporary contact
			else if (c == addUrsCommand)
			{
				Jimm.jimm.getMainMenuRef().addUserOrGroupCmd(uin,true);
			}
			
			// User adds selected message to history
			//#sijapp cond.if modules_HISTORY is "true" #
			else if (c == addToHistoryCommand)
			{
				Jimm.jimm.getChatHistoryRef().addTextToHistory(uin);
			}
			//#sijapp cond.end#
			
			// "Copy text" command selected
			else if (c == copyTextCommand)
			{
				DrawControls.TextList list = Jimm.jimm.getChatHistoryRef().getChatHistoryAt(uin);
				JimmUI.setClipBoardText(list.getCurrText(1));
				getCurrDisplay().addCommand(replWithQuotaCommand);
			}
			
			// User wants to rename Contact
			else if (c == renameOkCommand)
			{
				messageTextbox.removeCommand(renameOkCommand);
				ContactListContactItem.this.setName(messageTextbox.getString());
				try 
				{
					// Save ContactList
					Jimm.jimm.getContactListRef().save();
					
					// Try to save ContactList to server
					UpdateContactListAction action = new UpdateContactListAction(ContactListContactItem.this,UpdateContactListAction.ACTION_RENAME);
					Jimm.jimm.getIcqRef().requestAction(action);
				}
				catch (JimmException je)
				{
					if (je.isCritical()) return;
				}
				catch (Exception e)
				{
					// Do nothing
				}
				
				messageTextbox.setTitle(ResourceBundle.getString("message"));
				Jimm.jimm.getContactListRef().activate();
			}
			// Textbox has been closed
			else if ((c == textboxOkCommand) || (c == textboxSendCommand))
			{
				// Message has been entered
				if (d == messageTextbox)
				{
					messageTextbox.removeCommand(textboxOkCommand);
					messageTextbox.removeCommand(textboxSendCommand);
					
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
						PlainMessage plainMsg = new PlainMessage(Jimm.jimm.getIcqRef().getUin(),
					    ContactListContactItem.this,Message.MESSAGE_TYPE_NORM, new Date(), messageTextbox.getString());
						
						Jimm.jimm.getChatHistoryRef().addMyMessage(uin,plainMsg.getText(),plainMsg.getDate(),name);
						
						// #sijapp cond.if modules_HISTORY is "true" #
						if ( Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_HISTORY) )
							Jimm.jimm.getHistory().addText(getUin(), plainMsg.getText(), (byte)1, ResourceBundle.getString("me"), plainMsg.getDate());
						// #sijapp cond.end#
							
						SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
						try
						{
							Jimm.jimm.getIcqRef().requestAction(sendMsgAct);
						} catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) return;
						}
						
						// Return to contact list
						this.activate();
						
						// Clear text in messageTextbox
						messageTextbox.setString(null);
						
						// Clear clipboard
						if (repliedWithQuota) JimmUI.setClipBoardText(null);
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
					UrlMessage urlMsg = new UrlMessage(Jimm.jimm.getIcqRef().getUin(), ContactListContactItem.this, Message.MESSAGE_TYPE_NORM,
							new Date(), urlTextbox.getString(), messageTextbox.getString());
					SendMessageAction sendMsgAct = new SendMessageAction(urlMsg);
					try
					{
						Jimm.jimm.getIcqRef().requestAction(sendMsgAct);
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
					if (!requReason) ContactListContactItem.this.authRequest -= 1;

					// If or if not a reason was entered
					// Though this box is used twice (reason for auth request and auth repley)
					// we have to distinguish what we wanna do requReason is used for that
					if (reasonTextbox.getString().length() < 1)
					{
						if (requReason)
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_REQUAUTH, ContactListContactItem.this
									.getUin(), false, "");
						else
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, ContactListContactItem.this
									.getUin(), false, "");
					} else
					{
						if (requReason)
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_REQUAUTH, ContactListContactItem.this
									.getUin(), false, reasonTextbox.getString());
						else
							notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, ContactListContactItem.this
									.getUin(), false, reasonTextbox.getString());
					}

					// Assemble the sysNotAction and request it
					SysNoticeAction sysNotAct = new SysNoticeAction(notice);
					UpdateContactListAction updateAct = new UpdateContactListAction(ContactListContactItem.this, UpdateContactListAction.ACTION_ADD);

					try
					{
						Jimm.jimm.getIcqRef().requestAction(sysNotAct);
						if (ContactListContactItem.this.returnBoolValue(VALUE_IS_TEMP)) Jimm.jimm.getIcqRef().requestAction(updateAct);
					} catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) return;
					}
					requReason = false;
					Jimm.jimm.getContactListRef().activate();
				}
			}
			
			// user select Ok in delete contact message box
			else if (JimmUI.isMsgBoxCommand(c, MSGBS_DELETECONTACT) == JimmUI.CMD_OK)
			{
				Jimm.jimm.getIcqRef().delFromContactList(ContactListContactItem.this);
				//#sijapp cond.if modules_HISTORY is "true" #
				Jimm.jimm.getHistory().clearHistory(uin);
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
				RemoveMeAction remAct = new RemoveMeAction(ContactListContactItem.this.getUin());

				try
				{
					Jimm.jimm.getIcqRef().requestAction(remAct);
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
				Jimm.jimm.getContactListRef().activate();
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
				menuList.setTitle(ContactListContactItem.this.name);
				menuList.setSelectedIndex(0, true);
				menuList.setCommandListener(this);
				Jimm.display.setCurrent(menuList);
			}
			
			// Delete chat history
			else if (c == deleteChatCommand)
			{
				ContactListContactItem.this.deleteChatHistory();
				Jimm.jimm.getContactListRef().activate();
			}
			
			//Grant authorisation
			else if (c == grantAuthCommand)
			{
				ContactListContactItem.this.authRequest -= 1;
				SystemNotice notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, ContactListContactItem.this
						.getUin(), true, "");
				SysNoticeAction sysNotAct = new SysNoticeAction(notice);
				try
				{
					Jimm.jimm.getIcqRef().requestAction(sysNotAct);
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
				if (c == reqAuthCommand) requReason = true;
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
			else if (c == insertEmotionCommand)
			{
				Jimm.jimm.getEmotionsRef().selectEmotion(this, messageTextbox);
			}
			
			// User select a emotion
			else if ( Jimm.jimm.getEmotionsRef().isMyOkCommand(c) )
			{
				int insertPos;

				// #sijapp cond.if target is "MOTOROLA"#
				insertPos = messageTextbox.getString().length();
				// #sijapp cond.else#
				insertPos = messageTextbox.getCaretPosition();
				// #sijapp cond.end#
				
				messageTextbox.insert
				(
					new String(" ").concat(Jimm.jimm.getEmotionsRef().getSelectedEmotion()).concat(" "),
					insertPos
				);
			}
		}
		
		Displayable getCurrDisplay()
		{
			return Jimm.jimm.getChatHistoryRef().getChatHistoryAt(ContactListContactItem.this.uin);
		}
		
		// Activates the contact item menu
		public void activate()
		{
			// Display chat history
			if (ContactListContactItem.this.returnBoolValue(VALUE_HAS_CHAT))
			{
				initList(ContactListContactItem.this.returnBoolValue(VALUE_NO_AUTH));
				Displayable msgDisplay = getCurrDisplay();
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
				if ( !Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_HISTORY) )
					msgDisplay.addCommand(addToHistoryCommand);
				//#sijapp cond.end#
				if (ContactListContactItem.this.isMessageAvailable(ContactListContactItem.MESSAGE_AUTH_REQUEST))
				{
					msgDisplay.addCommand(grantAuthCommand);
					msgDisplay.addCommand(denyAuthCommand);
				}
				
				if (JimmUI.getClipBoardText() != null) msgDisplay.addCommand(replWithQuotaCommand);
				
				if (ContactListContactItem.this.returnBoolValue(VALUE_NO_AUTH)) msgDisplay.addCommand(reqAuthCommand);
				msgDisplay.setCommandListener(this);
				if (temporary && !noAuth) 
                    msgDisplay.addCommand(addUrsCommand);
				Jimm.jimm.getChatHistoryRef().UpdateCaption(ContactListContactItem.this.uin);
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
				initList(ContactListContactItem.this.returnBoolValue(VALUE_NO_AUTH));
                menuList.setTitle(ContactListContactItem.this.name);
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
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#        
    private static final int USER_MENU_DC_INFO          = 11;
    // #sijapp cond.end#
    // #sijapp cond.end#  
    private static final int USER_MENU_USER_INFO        = 12;
    private static final int USER_MENU_QUOTA            = 14;
    
    private static final int USER_MENU_LAST_ITEM        = 15; // YOU NEED TO CHANGE IT!

    
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
    private static Command msgCloseCommand
    //#sijapp cond.if target is "MOTOROLA"#
    = new Command(ResourceBundle.getString("close"),Command.BACK, 2);
    //#sijapp cond.else#
    = new Command(ResourceBundle.getString("close"),Command.BACK, 2);
    //#sijapp cond.end#
    
    // Message reply command
    private static Command msgReplyCommand
    //#sijapp cond.if target is "MOTOROLA"#
    = new Command(ResourceBundle.getString("reply"),Command.OK, 2);
    //#sijapp cond.else#
    = new Command(ResourceBundle.getString("reply"),Command.OK, 1);
    //#sijapp cond.end#
    
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
	private static Command textboxCancelCommand = new Command(ResourceBundle.getString("cancel"), Command.CANCEL, 3);

	// Grand authorisation a for authorisation asking contact
	private static Command grantAuthCommand = new Command(ResourceBundle.getString("grant"), Command.OK, 1);

	// Deny authorisation a for authorisation asking contact
	private static Command denyAuthCommand = new Command(ResourceBundle.getString("deny"), Command.CANCEL, 1);

	// Request authorisation from a contact
	private static Command reqAuthCommand = new Command(ResourceBundle.getString("requauth"), Command.ITEM, 1);

	// Insert imotion (smile) in text
	private static Command insertEmotionCommand = new Command(ResourceBundle.getString("insert_emotion"), Command.ITEM, 3);
    
    // Rename a contact
    private static Command renameOkCommand

    //#sijapp cond.if target is "MOTOROLA"#
    = new Command(ResourceBundle.getString("ok"),Command.BACK, 2);
    // #sijapp cond.else#
    = new Command(ResourceBundle.getString("rename"),Command.BACK, 2);
    //#sijapp cond.end#
    
	static void initList(boolean showAuthItem)
	{
        // Size of the event list equals last entry number
        eventList = new int[USER_MENU_LAST_ITEM];
        menuList = new List("",List.IMPLICIT);
        
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
        eventList[menuList.append(ResourceBundle.getString("reqstatmsg"), null)]   = USER_MENU_STATUS_MESSAGE;
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_FILES is "true"#
        eventList[menuList.append(ResourceBundle.getString("ft_name"), null)]      = USER_MENU_FILE_TRANS;
        // #sijapp cond.if target isnot "MOTOROLA"#
        eventList[menuList.append(ResourceBundle.getString("ft_cam"), null)]       = USER_MENU_CAM_TRANS;
        // #sijapp cond.end#
        // #sijapp cond.end#
        // #sijapp cond.end#
        eventList[menuList.append(ResourceBundle.getString("remove"), null)]       = USER_MENU_USER_REMOVE;
        eventList[menuList.append(ResourceBundle.getString("remove_me"), null)]    = USER_MENU_REMOVE_ME;
        eventList[menuList.append(ResourceBundle.getString("rename"), null)]       = USER_MENU_RENAME;
        // #sijapp cond.if modules_HISTORY is "true" #
        eventList[menuList.append(ResourceBundle.getString("history"), null)]      = USER_MENU_HISTORY;
        // #sijapp cond.end#
        eventList[menuList.append(ResourceBundle.getString("info"), null)]         = USER_MENU_USER_INFO;
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        // #sijapp cond.if modules_FILES is "true"#
        eventList[menuList.append(ResourceBundle.getString("dc_info"), null)]      = USER_MENU_DC_INFO;
        // #sijapp cond.end#
        // #sijapp cond.end#            
	}
	
	// Initializer
	static
	{
		// Initialize the textbox for entering messages
		messageTextbox = new TextBox(ResourceBundle.getString("message"), null, 1000, TextField.ANY);
		messageTextbox.addCommand(textboxCancelCommand);
		messageTextbox.addCommand(insertEmotionCommand);

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

