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
import java.util.Calendar;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.RequestInfoAction;
import jimm.comm.SendMessageAction;
import jimm.comm.SystemNotice;
import jimm.comm.SysNoticeAction;
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
    private Form msgDisplay;

    // Message count
    private int outgoingPlainMessagesCnt;
    private int plainMessages;
    private int urlMessages;
    private int sysNotices;
    private int authRequest;

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
        this.msgDisplay = new Form(ContactListContactItem.this.getName());
        this.outgoingPlainMessagesCnt = 0;
        this.plainMessages = 0;
        this.urlMessages = 0;
        this.sysNotices = 0;
        this.authRequest = 0;
        this.menu = new Menu();
        this.requReason = false;
    }

    // Returns a string representing the object
    public String toString()
    {
        String rep = new String();
        rep = "ID: 0x" + Integer.toHexString(id) + " Group: 0x" + Integer.toHexString(group) + " UIN: " + uin
                + " Name: " + name + " Temporary: " + temporary + "NoAuth: "+noAuth;
        return rep;
    }

    // Returns if added to the contact list
    public boolean added()
    {
        return (this.added);
    }

    // Sets the contact item added
    public void setAdded(boolean added)
    {
        this.added = added;
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

    // Retrun the staate of authorisation
    public boolean noAuth()
    {
        return (noAuth);
    }

    // Sets stat auf authorisation
    public void setNoAuth(boolean noAuth)
    {
        this.noAuth = noAuth;
    }

    // Returns true if this is a contact which is on the local list
    public boolean isTemporary()
    {
        return (this.temporary);
    }

    // Permanent / temporary contact entry
    public void setTemporary(boolean temporary)
    {
        this.temporary = temporary;
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

    // Returns true if the next available message is a plain message
    // Returns false if no message at all is available, or if the next available
    // message is an URL message or plain message
    protected synchronized boolean isPlainMessageAvailable()
    {
        return (this.plainMessages > 0);
    }

    // Returns true if the next available message is an URL message
    // Returns false if no message at all is available, or if the next available
    // message is an plain message or system notice
    protected synchronized boolean isUrlMessageAvailable()
    {
        return (this.urlMessages > 0);
    }

    // Returns true if the next available message is a sys notice
    // Returns false if no message at all is available, or if the next available
    // message is an URL message plain message
    protected synchronized boolean isSysNoticeAvailable()
    {
        return (this.sysNotices > 0);
    }

    // Returns
    protected synchronized boolean isunasweredAuthRequest()
    {
        return (this.authRequest > 0);
    }

    // Adds a message to the message display
    protected synchronized void addMessage(Message message)
    {
        if (message instanceof PlainMessage)
        {
            PlainMessage plainMsg = (PlainMessage) message;
            if (!msgDisplay.isShown()) plainMessages++;
            this.addTextToForm(this.getName(), plainMsg.getText(), "", plainMsg.getDate(), true);
        }
        if (message instanceof UrlMessage)
        {
            UrlMessage urlMsg = (UrlMessage) message;
            if (!msgDisplay.isShown()) urlMessages++;
            this.addTextToForm(this.getName(), urlMsg.getText(), urlMsg.getUrl(), urlMsg.getDate(), false);
        }
        if (message instanceof SystemNotice)
        {
            SystemNotice notice = (SystemNotice) message;
            if (!msgDisplay.isShown()) sysNotices++;

            if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_YOUWEREADDED)
            {
                this.addTextToForm(ResourceBundle.getString("sysnotice"), ResourceBundle.getString("youwereadded")
                        + notice.getSndrUin(), "", notice.getDate(), false);
            } else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ)
            {
                authRequest++;
                this.addTextToForm(ResourceBundle.getString("sysnotice"), notice.getSndrUin()
                        + ResourceBundle.getString("wantsyourauth") + notice.getReason(), "", notice.getDate(), false);
            } else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY)
            {
                if (notice.isAUTH_granted())
                {
                    this.setNoAuth(false);
                    this.addTextToForm(ResourceBundle.getString("sysnotice"), ResourceBundle.getString("grantedby")
                            + notice.getSndrUin() + ".", "", notice.getDate(), false);
                    Jimm.jimm.getContactListRef().refreshVisibleList(true);
                } else if (notice.getReason() != null)
                    this.addTextToForm(ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
                            + notice.getSndrUin() + ". " + ResourceBundle.getString("reason") + ": " + notice.getReason(),
                            "", notice.getDate(), false);
                else
                    this.addTextToForm(ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
                            + notice.getSndrUin() + ". " + ResourceBundle.getString("noreason"), "", notice.getDate(),
                            false);
            }
        }
    }

    // Add text to message form
    public void addTextToForm(String from, String message, String url, Date time, boolean red)
    {
        Calendar stamp = Calendar.getInstance();
        stamp.setTime(time);

        Font prequelFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        // #sijapp cond.if target is "MIDP2"#
        Image prequel = Image.createImage(msgDisplay.getWidth(), prequelFont.getHeight() + 2);
        Font textFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        // #sijapp cond.else#
        Image prequel = Image.createImage(120, prequelFont.getHeight() + 2);
        // #sijapp cond.end#
        Graphics g = prequel.getGraphics();
        if (!red)
            g.setColor(0, 0, 255);
        else
            g.setColor(255, 0, 0);
        g.setFont(prequelFont);
        g.drawString(from + " (" + stamp.get(Calendar.HOUR_OF_DAY) + ":" + Util.makeTwo(stamp.get(Calendar.MINUTE)) + "):", 0, 10,
                Graphics.BASELINE | Graphics.LEFT);
        Image copy = Image.createImage(prequel);

        msgDisplay.append(new ImageItem(null, copy, ImageItem.LAYOUT_LEFT + ImageItem.LAYOUT_NEWLINE_BEFORE
                + ImageItem.LAYOUT_NEWLINE_AFTER, null));
        if (url.length() > 0)
        {
            StringItem urlItem = new StringItem(null,ResourceBundle.getString("url")+": "+url);
//          #sijapp cond.if target is "MIDP2"#
            urlItem.setFont(textFont);
//          #sijapp cond.end#
            msgDisplay.append(urlItem);
        }
        StringItem messageItem = new StringItem(null, message);
//      #sijapp cond.if target is "MIDP2"#
        messageItem.setFont(textFont);
//      #sijapp cond.end#
        msgDisplay.append(messageItem);
    }

    // Returns if the chat hisotry screen is shown
    public boolean chatShown()
    {
        return msgDisplay.isShown();
    }

    // Returns if the contact item has a chat runnig
    public boolean hasChat()
    {
        if (msgDisplay.size() == 0)
            return false;
        else
            return true;
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
        while (msgDisplay.size() > 0)
            msgDisplay.delete(0);
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
        return (this.uin.equals(ci.getUin()) && (this.temporary == ci.isTemporary()));
    }

    // Compare two contact items (sort by status/nick)
    public int compareTo(ContactListContactItem cItem2)
    {

        // Get status and temporary flag
        long cItem1Status = this.getStatus();
        boolean cItem1IsTemporary = this.isTemporary();
        long cItem2Status = cItem2.getStatus();
        boolean cItem2IsTemporary = cItem2.isTemporary();

        // Compare, return negative int if cItem1 < cItem2, 0 if cItem1 ==
        // cItem2, or positive int if cItem1 > cItem2
        if ((cItem1Status != ContactList.STATUS_OFFLINE) && !cItem1IsTemporary)
        {
            if (((cItem2Status != ContactList.STATUS_OFFLINE) && !cItem2IsTemporary)
                    || (Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_CL_SORT_BY) != 0))
            {
                return (this.getName().compareTo(cItem2.getName()));
            } else
            {
                return (-1);
            }
        } else if ((cItem1Status == ContactList.STATUS_OFFLINE) && !cItem1IsTemporary)
        {
            if ((cItem2Status != ContactList.STATUS_OFFLINE) && !cItem2IsTemporary
                    && (Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_CL_SORT_BY) == 0))
            {
                return (1);
            } else if (((cItem2Status == ContactList.STATUS_OFFLINE) && !cItem2IsTemporary)
                    || (Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_CL_SORT_BY) != 0))
            {
                return (this.getName().compareTo(cItem2.getName()));
            } else
            {
                return (-1);
            }
        } else if (cItem1IsTemporary)
        {
            if (cItem2IsTemporary)
            {
                return (this.getName().compareTo(cItem2.getName()));
            } else
            {
                return (1);
            }
        } else
        {
            return (0);
        }

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
                if (!Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_KEEPCHAT))
                {
                    ContactListContactItem.this.deleteChatHistory();
                }
                Jimm.jimm.getContactListRef().update(ContactListContactItem.this.getUin());
                Jimm.jimm.getContactListRef().activate();
            }
            // User wants to send a reply
            else if (c == MenuUtil.msgReplyCommand)
            {

                if (!Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_KEEPCHAT))
                {
                    ContactListContactItem.this.deleteChatHistory();
                }

                // Select first list element (new message)
                MenuUtil.menuList.setSelectedIndex(0, true);

                // Reset and display textbox for entering messages
                MenuUtil.messageTextbox.setString(null);
                MenuUtil.messageTextbox.removeCommand(MenuUtil.textboxOkCommand);
                MenuUtil.messageTextbox.addCommand(MenuUtil.textboxSendCommand);
                MenuUtil.messageTextbox.setCommandListener(this);
                Jimm.display.setCurrent(MenuUtil.messageTextbox);

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

                    Jimm.jimm.getIcqRef().delFromContactList(ContactListContactItem.this);

                    break;

                case 4:
                    // Request auth

                    requReason = true;
                    MenuUtil.reasonTextbox.setString(ResourceBundle.getString("plsauthme"));
                    MenuUtil.reasonTextbox.removeCommand(MenuUtil.textboxOkCommand);
                    MenuUtil.reasonTextbox.addCommand(MenuUtil.textboxSendCommand);
                    MenuUtil.reasonTextbox.setCommandListener(this);
                    Jimm.display.setCurrent(MenuUtil.reasonTextbox);

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

                        if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_KEEPCHAT))
                        	ContactListContactItem.this.addTextToForm(ResourceBundle.getString("me"),plainMsg.getText(),"",plainMsg.getDate(),false);
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
                        Jimm.jimm.getContactListRef().activate();

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

                    if (!Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_KEEPCHAT))
                    {
                        ContactListContactItem.this.deleteChatHistory();
                    }

                    // Return to contact list
                    Jimm.jimm.getContactListRef().update();
                    Jimm.jimm.getContactListRef().activate();

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
                        if (ContactListContactItem.this.isTemporary()) Jimm.jimm.getIcqRef().requestAction(updateAct);
                    } catch (JimmException e)
                    {
                        JimmException.handleException(e);
                        if (e.isCritical()) return;
                    }
                    requReason = false;
                    Jimm.jimm.getContactListRef().activate();
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
            if (ContactListContactItem.this.hasChat())
            {
                msgDisplay.removeCommand(MenuUtil.grantAuthCommand);
                msgDisplay.removeCommand(MenuUtil.denyAuthCommand);
                msgDisplay.removeCommand(MenuUtil.reqAuthCommand);
                msgDisplay.addCommand(MenuUtil.msgCloseCommand);
                msgDisplay.addCommand(MenuUtil.msgReplyCommand);
                msgDisplay.addCommand(MenuUtil.deleteChatCommand);
                msgDisplay.addCommand(MenuUtil.addMenuCommand);
                if (ContactListContactItem.this.isunasweredAuthRequest())
                {
                    msgDisplay.addCommand(MenuUtil.grantAuthCommand);
                    msgDisplay.addCommand(MenuUtil.denyAuthCommand);
                }
                if (ContactListContactItem.this.noAuth()) msgDisplay.addCommand(MenuUtil.reqAuthCommand);
                msgDisplay.setCommandListener(this);
                // Display history
                ContactListContactItem.this.resetUnreadMessages();
                Jimm.display.setCurrent(msgDisplay);

            }
            // Display menu
            else
            {
                MenuUtil.menuList.setTitle(ContactListContactItem.this.name);
                if (MenuUtil.menuList.size() > 4) MenuUtil.menuList.delete(4);
                if (ContactListContactItem.this.noAuth())
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

        // Abort command
        private static Command backCommand = new Command(ResourceBundle.getString("back"),
                Command.BACK, 2);

        // Message close command
        private static Command msgCloseCommand = new Command(ResourceBundle.getString("close"),
                Command.BACK, 3);

        // Message close and reply command
        private static Command msgReplyCommand = new Command(ResourceBundle.getString("reply"),
                Command.OK, 2);

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
            MenuUtil.menuList.append(ResourceBundle.getString("info"), null);
            MenuUtil.menuList.append(ResourceBundle.getString("remove"), null);

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
