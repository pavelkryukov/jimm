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
 File: src/jimm/ContactListContactItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.util.Date;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

// #sijapp cond.if target is "MIDP2"#
import jimm.comm.FileTransferMessage;
// #sijapp cond.end#
import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.RequestInfoAction;
import jimm.comm.SendMessageAction;
import jimm.comm.SysNoticeAction;
import jimm.comm.SystemNotice;
import jimm.comm.UpdateContactListAction;
import jimm.comm.UrlMessage;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class ContactListContactItem extends ContactListItem
{

    // No capability
    public static final int CAP_NO_INTERNAL = 0x00000000;

    // Client unterstands type-2 messages
    public static final int CAP_AIM_SERVERRELAY_INTERNAL = 0x00000001;

    // Client unterstands UTF-8 messages
    public static final int CAP_UTF8_INTERNAL = 0x00000002;
    
    // Message types
    public static final int MESSAGE_PLAIN        = 1;
    public static final int MESSAGE_URL          = 2;
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
    private int chatHistoryDisplayNr;
    
    
    // Message count
    private int outgoingPlainMessagesCnt;
    private int plainMessages;
    private int urlMessages;
    private int sysNotices;
    private int authRequest;
    
    //  #sijapp cond.if target is "MIDP2"#
    // DC values
    private byte[] internalIP;
    private byte[] externalIP;
    private String dcPort;
    private int dcType;
    private int icqProt;
    private long authCookie;
    private FileTransferMessage ftm;
    private FileTransfer ft;
    //  #sijapp cond.end#
    
    // Menu
    private Menu menu;

    // Constructor
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
        this.chatHistoryDisplayNr = -1;
        this.outgoingPlainMessagesCnt = 0;
        this.plainMessages = 0;
        this.urlMessages = 0;
        this.sysNotices = 0;
        this.authRequest = 0;
        //  #sijapp cond.if target is "MIDP2"#
        this.internalIP = new byte[4];
        this.externalIP = new byte[4];
        this.dcPort = "";
        this.dcType = -1;
        this.icqProt = 0;
        this.authCookie = 0;
        this.ft = null;
        //  #sijapp cond.end#
        this.menu = new Menu();
        this.requReason = false;
    }
    
    protected void copyChatHistory(ContactListContactItem src)
    {
    	chatHistoryDisplayNr = src.chatHistoryDisplayNr;
    }

    // Retruns boolean value by property
    public boolean returnBoolValue(int value)
    {
        switch (value)
        {
        case VALUE_ADDED: return (this.added);
        case VALUE_NO_AUTH: return (noAuth);
        case VALUE_CHAT_SHOWN: return Jimm.jimm.getChatHistoryRef().chatHistoryShown(chatHistoryDisplayNr);
        case VALUE_IS_TEMP: return (this.temporary);
        case VALUE_HAS_CHAT: if (Jimm.jimm.getChatHistoryRef().chatHistorySize(chatHistoryDisplayNr) > 0)
            					return true;
        					 else
        					     return false;
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
    
    // Returns true if contact must be shown even user offline
    // and "hide offline" is on
    protected boolean mustBeShownAnyWay()
    {
    	return (plainMessages > 0) ||
		       (urlMessages > 0)   || 
			   (sysNotices > 0)    ||
			   (authRequest > 0)   || 
			   temporary; 
    }
    
    public int getTextColor()
    {
    	return temporary ? 0x808080 : 0x000000; 
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

    	if ((tempIndex != -1) && (returnBoolValue(VALUE_HAS_CHAT)))
    	{
    		tempIndex += ContactList.getImagesCount();
    	}
    	
        return tempIndex;
    }
    
    public static int getStatusImageIndex(long status)
    {
        if (status == ContactList.STATUS_AWAY)      return 0;
        else if (status == ContactList.STATUS_CHAT)     return  1;
        else if (status == ContactList.STATUS_DND)       return 2;
        else if (status == ContactList.STATUS_INVISIBLE) return 3;
        else if (status == ContactList.STATUS_NA)        return 4;
        else if (status == ContactList.STATUS_OCCUPIED)  return 5;
        else if (status == ContactList.STATUS_OFFLINE)   return 6;
        else if (status == ContactList.STATUS_ONLINE)    return  7;
        return -1;
    }
    
    //  #sijapp cond.if target is "MIDP2"#
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
    
    // Returns true if the next available message is a message of given type
    // Returns false if no message at all is available, or if the next available
    // message is of another type
    protected synchronized boolean isMessageAvailable(int type)
    {
        switch (type)
        {
        	case MESSAGE_PLAIN:        return (this.plainMessages > 0);
        	case MESSAGE_URL:          return (this.urlMessages > 0); 
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
        	case MESSAGE_PLAIN:        this.plainMessages++; break;
        	case MESSAGE_URL:          this.urlMessages++; break;
        	case MESSAGE_SYS_NOTICE:   this.sysNotices++; break;
        	case MESSAGE_AUTH_REQUEST: this.authRequest++;
        }
    }

    // Adds a message to the message display
    protected synchronized void addMessage(Message message)
    {
        if(chatHistoryDisplayNr != -1)
            Jimm.jimm.getChatHistoryRef().addMessage(chatHistoryDisplayNr,message,this);
        else
        {
            chatHistoryDisplayNr = Jimm.jimm.getChatHistoryRef().newChatForm(name);
            addMessage(message);
        }
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
        while (Jimm.jimm.getChatHistoryRef().chatHistorySize(chatHistoryDisplayNr) > 0)
            Jimm.jimm.getChatHistoryRef().chatHistoryDelete(chatHistoryDisplayNr);
    }

    // Activates the contact item menu
    public void activateMenu()
    {
        this.menu.activate();
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

    private class Menu implements CommandListener
    {

        // Command listener
        public void commandAction(Command c, Displayable d)
        {
			
            // Return to contact list
            if (c == MenuUtil.backCommand)
            {
                ContactListContactItem.this.resetUnreadMessages();
                Jimm.jimm.getContactListRef().activate();
            }
            // Message has been closed
            else if (c == MenuUtil.msgCloseCommand)
            {
                Jimm.jimm.getContactListRef().update(ContactListContactItem.this.getUin());
                Jimm.jimm.getContactListRef().activate();
            }
            // User wants to send a reply
            else if (c == MenuUtil.msgReplyCommand)
            {
                // Select first list element (new message)
                MenuUtil.menuList.setSelectedIndex(0, true);

                // Reset and display textbox for entering messages
                MenuUtil.messageTextbox.setString(null);
                MenuUtil.messageTextbox.removeCommand(MenuUtil.textboxOkCommand);
                MenuUtil.messageTextbox.addCommand(MenuUtil.textboxSendCommand);
                MenuUtil.messageTextbox.setCommandListener(this);
                Jimm.display.setCurrent(MenuUtil.messageTextbox);

            }
            
            // User wants to add temporary contact
            else if (c == MenuUtil.addUrsCommand)
            {
            	MainMenu.addUserCmd(uin);
            }
            
            // Menu item has been selected
            else if (c == List.SELECT_COMMAND)
            {

                switch (MenuUtil.menuList.getSelectedIndex())
                {
                case 0:
                    // Send plain message

                    // Reset and display textbox for entering messages
                    MenuUtil.messageTextbox.setString(null);
                    MenuUtil.messageTextbox.removeCommand(MenuUtil.textboxOkCommand);
                    MenuUtil.messageTextbox.addCommand(MenuUtil.textboxSendCommand);
                    MenuUtil.messageTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(MenuUtil.messageTextbox);

                    break;
                case 1:
                    // Send URL message

                    // Reset and display textbox for entering messages
                    MenuUtil.messageTextbox.setString(null);
                    MenuUtil.messageTextbox.removeCommand(MenuUtil.textboxSendCommand);
                    MenuUtil.messageTextbox.addCommand(MenuUtil.textboxOkCommand);
                    MenuUtil.messageTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(MenuUtil.messageTextbox);

                    break;
                // #sijapp cond.if target is "MIDP2"#
                case 2:
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
                        
                case 3:
                    // Send a filetransfer with a camera image
                    // System.out.println("FileTransfer: cam");
                    
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
                
                case 4:
                    // Info

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
                case 5:
                    // Remove
                    MenuUtil.deleteUserAlert = new Alert(ResourceBundle.getString("remove")+"?",ResourceBundle.getString("remove")+" "+ContactListContactItem.this.getName()+"?",null,AlertType.CONFIRMATION);
                    MenuUtil.deleteUserAlert.addCommand(MenuUtil.textboxOkCommand);
                    MenuUtil.deleteUserAlert.addCommand(MenuUtil.textboxCancelCommand);
                    MenuUtil.deleteUserAlert.setCommandListener(this);
                    Jimm.display.setCurrent(MenuUtil.deleteUserAlert);
                    
                    break;
                    
                case 6:
                    
                    // DC Info
                    Alert info = new Alert("DC Infos");
                    info.setString("DC typ: "+ContactListContactItem.this.getDCType()+"\n"+
                                   "ICQ version: "+ContactListContactItem.this.getICQVersion()+"\n"+
                                   "Int IP: "+Util.ipToString(ContactListContactItem.this.getInternalIP())+"\n"+
                                   "Ext IP: "+Util.ipToString(ContactListContactItem.this.getExternalIP())+"\n"+
                                   "Port: "+ContactListContactItem.this.getPort()+"\n");
                    info.setTimeout(Alert.FOREVER);
                    Jimm.display.setCurrent(info);
                    
                    break;

                case 7:
                    // Request auth
                    requReason = true;
                    MenuUtil.reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
                    MenuUtil.reasonTextbox.removeCommand(MenuUtil.textboxOkCommand);
                    MenuUtil.reasonTextbox.addCommand(MenuUtil.textboxSendCommand);
                    MenuUtil.reasonTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(MenuUtil.reasonTextbox);
                // #sijapp cond.else#
                case 2:
                    // Info

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
                case 3:
                    // Remove
                    MenuUtil.deleteUserAlert = new Alert(ResourceBundle.getString("remove")+"?",ResourceBundle.getString("remove")+" "+ContactListContactItem.this.getName()+"?",null,AlertType.CONFIRMATION);
                    MenuUtil.deleteUserAlert.addCommand(MenuUtil.textboxOkCommand);
                    MenuUtil.deleteUserAlert.addCommand(MenuUtil.textboxCancelCommand);
                    MenuUtil.deleteUserAlert.setCommandListener(this);
                    Jimm.display.setCurrent(MenuUtil.deleteUserAlert);
                    
                    break;
                    
                case 4:
                    // Request auth
                    requReason = true;
                    MenuUtil.reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
                    MenuUtil.reasonTextbox.removeCommand(MenuUtil.textboxOkCommand);
                    MenuUtil.reasonTextbox.addCommand(MenuUtil.textboxSendCommand);
                    MenuUtil.reasonTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(MenuUtil.reasonTextbox);
                // #sijapp cond.end#
                }

            }
            // Textbox has been closed
            else if ((c == MenuUtil.textboxOkCommand) || (c == MenuUtil.textboxSendCommand))
            {

                // Message has been entered
                if (d == MenuUtil.messageTextbox)
                {
                    
                    // Abort if nothing has been entered
                    if (MenuUtil.messageTextbox.getString().length() < 1)
                    {
                        this.activate();
                    }

                    // Send plain message
                    if (MenuUtil.menuList.getSelectedIndex() == 0 && !MenuUtil.messageTextbox.getString().equals(""))
                    {
                        // Construct plain message object and request new
                        // SendMessageAction
                        // Add the new message to the chat history
                        PlainMessage plainMsg = new PlainMessage(Jimm.jimm.getIcqRef().getUin(),
                                ContactListContactItem.this, new Date(), MenuUtil.messageTextbox.getString());
                            if (chatHistoryDisplayNr != -1)
                                Jimm.jimm.getChatHistoryRef().addTextToForm(ContactListContactItem.this.chatHistoryDisplayNr,ResourceBundle.getString("me"),plainMsg.getText(),"",plainMsg.getDate(),false);
                            else
	                        {
	                            chatHistoryDisplayNr = Jimm.jimm.getChatHistoryRef().newChatForm(name);
	                            Jimm.jimm.getChatHistoryRef().addTextToForm(ContactListContactItem.this.chatHistoryDisplayNr,ResourceBundle.getString("me"),plainMsg.getText(),"",plainMsg.getDate(),false);
	                        }
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
                        Jimm.jimm.getContactListRef().update(ContactListContactItem.this.getUin());
                        this.activate();

                    }
                    // Send URL message (continue creation)
                    else if (MenuUtil.menuList.getSelectedIndex() == 1)
                    {

                        // Reset and display textbox for entering URLs
                        MenuUtil.urlTextbox.setString(null);
                        MenuUtil.urlTextbox.setCommandListener(this);
                        Jimm.display.setCurrent(MenuUtil.urlTextbox);

                    }

                }
                // URL has been entered
                else if (d == MenuUtil.urlTextbox)
                {

                    // Abort if nothing has been entered
                    if (MenuUtil.urlTextbox.getString().length() < 1)
                    {
                        this.activate();
                    }

                    // Construct URL message object and request new
                    // SendMessageAction
                    UrlMessage urlMsg = new UrlMessage(Jimm.jimm.getIcqRef().getUin(), ContactListContactItem.this,
                            new Date(), MenuUtil.urlTextbox.getString(), MenuUtil.messageTextbox.getString());
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
                    Jimm.jimm.getContactListRef().update();
                    this.activate();

                }
                // Reason has been entered
                else if (d == MenuUtil.reasonTextbox)
                {
                    
                    SystemNotice notice;

                    // Decrease the number of handled auth requests by one
                    if (!requReason) ContactListContactItem.this.authRequest -= 1;

                    // If or if not a reason was entered
                    // Though this box is used twice (reason for auth request
                    // and auth repley)
                    // we have to distinguish what we wanna do requReason is
                    // used for that
                    if (MenuUtil.reasonTextbox.getString().length() < 1)
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
                                    .getUin(), false, MenuUtil.reasonTextbox.getString());
                        else
                            notice = new SystemNotice(SystemNotice.SYS_NOTICE_AUTHORISE, ContactListContactItem.this
                                    .getUin(), false, MenuUtil.reasonTextbox.getString());
                    }

                    // Assemble the sysNotAction and request it
                    SysNoticeAction sysNotAct = new SysNoticeAction(notice);
                    UpdateContactListAction updateAct = new UpdateContactListAction(ContactListContactItem.this, true);

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
                // If user interaction deleteUserAlert is shown.
                else if (d == MenuUtil.deleteUserAlert)
                {
                    Jimm.jimm.getIcqRef().delFromContactList(ContactListContactItem.this);
                }

            }
            // Textbox has been canceled
            else if (c == MenuUtil.textboxCancelCommand)
            {
                this.activate();
            }
            // Menu should be activated
            else if (c == MenuUtil.addMenuCommand)
            {
                MenuUtil.menuList.setTitle(ContactListContactItem.this.name);
                MenuUtil.menuList.setSelectedIndex(0, true);
                MenuUtil.menuList.setCommandListener(this);
                Jimm.display.setCurrent(MenuUtil.menuList);
            }
            // Delete chat history
            else if (c == MenuUtil.deleteChatCommand)
            {
                ContactListContactItem.this.deleteChatHistory();
                Jimm.jimm.getContactListRef().update(ContactListContactItem.this.getUin());
                Jimm.jimm.getContactListRef().activate();
            }
            //Grant authorisation
            else if (c == MenuUtil.grantAuthCommand)
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
            else if (c == MenuUtil.denyAuthCommand || c == MenuUtil.reqAuthCommand)
            {
                // Reset and display textbox for entering deney reason
                if (c == MenuUtil.reqAuthCommand) requReason = true;
                if (c == MenuUtil.reqAuthCommand)
                    MenuUtil.reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
                else
                    MenuUtil.reasonTextbox.setString(null);
                MenuUtil.reasonTextbox.removeCommand(MenuUtil.textboxOkCommand);
                MenuUtil.reasonTextbox.addCommand(MenuUtil.textboxSendCommand);
                MenuUtil.reasonTextbox.setCommandListener(this);
                Jimm.display.setCurrent(MenuUtil.reasonTextbox);
            }
        }
        
        // Activates the contact item menu
        public void activate()
        {

            // Display chat history
            if (ContactListContactItem.this.returnBoolValue(VALUE_HAS_CHAT))
            {
                Displayable msgDisplay = Jimm.jimm.getChatHistoryRef().getChatHistoryAt(ContactListContactItem.this.chatHistoryDisplayNr);
                
                msgDisplay.removeCommand(MenuUtil.addUrsCommand);
                msgDisplay.removeCommand(MenuUtil.grantAuthCommand);
                msgDisplay.removeCommand(MenuUtil.denyAuthCommand);
                msgDisplay.removeCommand(MenuUtil.reqAuthCommand);
                msgDisplay.addCommand(MenuUtil.msgCloseCommand);
                msgDisplay.addCommand(MenuUtil.msgReplyCommand);
                msgDisplay.addCommand(MenuUtil.deleteChatCommand);
                msgDisplay.addCommand(MenuUtil.addMenuCommand);
                if (ContactListContactItem.this.isMessageAvailable(ContactListContactItem.MESSAGE_AUTH_REQUEST))
                {
                    msgDisplay.addCommand(MenuUtil.grantAuthCommand);
                    msgDisplay.addCommand(MenuUtil.denyAuthCommand);
                }
                if (ContactListContactItem.this.returnBoolValue(VALUE_NO_AUTH)) msgDisplay.addCommand(MenuUtil.reqAuthCommand);
                msgDisplay.setCommandListener(this);
                
                if (temporary && !noAuth) msgDisplay.addCommand(MenuUtil.addUrsCommand);
                
                // Display history
                ContactListContactItem.this.resetUnreadMessages();
                Jimm.display.setCurrent(msgDisplay);

            }
            // Display menu
            else
            {
                MenuUtil.menuList.setTitle(ContactListContactItem.this.name);
                //  #sijapp cond.if target is "MIDP2"#
                if (MenuUtil.menuList.size() > 7) MenuUtil.menuList.delete(7);
                //  #sijapp cond.else#
                if (MenuUtil.menuList.size() > 4) MenuUtil.menuList.delete(4);
                //  #sijapp cond.end#
                if (ContactListContactItem.this.returnBoolValue(VALUE_NO_AUTH))
                        MenuUtil.menuList.append(ResourceBundle.getString("requauth"), null);
                MenuUtil.menuList.setSelectedIndex(0, true);
                MenuUtil.menuList.setCommandListener(this);
                Jimm.display.setCurrent(MenuUtil.menuList);
            }
        }
    }

    /****************************************************************************/
    /****************************************************************************/
    /****************************************************************************/

    private static class MenuUtil
    {

        // Menu list
        private static List menuList;

        // Textbox for entering messages
        private static TextBox messageTextbox;

        // Textbox for entering URLs
        private static TextBox urlTextbox;

        // Textbox for entering a reason
        private static TextBox reasonTextbox;
        
        // Alert for security question for user delete
        private static Alert deleteUserAlert;

        // Abort command
        private static Command backCommand = new Command(ResourceBundle.getString("back"),
                Command.BACK, 2);

        // Message close command
        private static Command msgCloseCommand = new Command(ResourceBundle.getString("close"),
                Command.BACK, 3);

        // Message close and reply command
        private static Command msgReplyCommand = new Command(ResourceBundle.getString("reply"),
                Command.OK, 2);
        
        // Add temporary user to contact list
        private static Command addUrsCommand = new Command(ResourceBundle.getString("add_user"), Command.OK, 2);

        //Show the message menu
        private static Command addMenuCommand = new Command(ResourceBundle.getString("user_menu"),
                Command.OK, 4);

        //Delete Chat History
        private static Command deleteChatCommand = new Command(ResourceBundle.getString("delete_chat"), Command.BACK, 5);

        // Textbox OK command
        private static Command textboxOkCommand = new Command(ResourceBundle.getString("ok"),
                Command.OK, 2);

        // Textbox Send command
        private static Command textboxSendCommand = new Command(ResourceBundle.getString("send"),
                Command.OK, 2);

        // Textbox cancel command
        private static Command textboxCancelCommand = new Command(ResourceBundle.getString("cancel"),
                Command.CANCEL, 3);

        // Grand authorisation a for authorisation asking contact
        private static Command grantAuthCommand = new Command(ResourceBundle.getString("grant"),
                Command.OK, 1);

        // Deny authorisation a for authorisation asking contact
        private static Command denyAuthCommand = new Command(ResourceBundle.getString("deny"),
                Command.CANCEL, 1);

        // Request authorisation from a contact
        private static Command reqAuthCommand = new Command(ResourceBundle.getString("requauth"),
                Command.OK, 1);
        
        // Initializer
        static
        {

            // Initialize the menu list
            MenuUtil.menuList = new List("set", Choice.IMPLICIT);
            MenuUtil.menuList.append(ResourceBundle.getString("send_message"), null);
            MenuUtil.menuList.append(ResourceBundle.getString("send_url"), null);
            // #sijapp cond.if target is "MIDP2"#
            MenuUtil.menuList.append(ResourceBundle.getString("ft_name"),null);
            MenuUtil.menuList.append(ResourceBundle.getString("ft_cam"),null);
            // #sijapp cond.end#
            MenuUtil.menuList.append(ResourceBundle.getString("info"), null);
            MenuUtil.menuList.append(ResourceBundle.getString("remove"), null);
            // #sijapp cond.if target is "MIDP2"#
            MenuUtil.menuList.append("DC Info", null);
            // #sijapp cond.end#

            MenuUtil.menuList.addCommand(MenuUtil.backCommand);

            // Initialize the textbox for entering messages
            MenuUtil.messageTextbox = new TextBox(ResourceBundle.getString("message"), null, 1000,
                    TextField.ANY);
            MenuUtil.messageTextbox.addCommand(MenuUtil.textboxCancelCommand);

            // Initialize the textbox for entering URLs
            MenuUtil.urlTextbox = new TextBox(ResourceBundle.getString("url"), null, 1000,
                    TextField.URL);
            MenuUtil.urlTextbox.addCommand(MenuUtil.textboxCancelCommand);
            MenuUtil.urlTextbox.addCommand(MenuUtil.textboxSendCommand);

            // Initialize the textfor for entering reasons
            MenuUtil.reasonTextbox = new TextBox(ResourceBundle.getString("reason"), null, 1000,
                    TextField.ANY);
            MenuUtil.reasonTextbox.addCommand(MenuUtil.textboxCancelCommand);
            MenuUtil.reasonTextbox.addCommand(MenuUtil.textboxSendCommand);

        }

    }

}
