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


  /****************************************************************************/


	// Persistent variables
	private int id;
	private int group;
	private String uin;
	private String name;

	// Transient variables
	private boolean temporary;
	private long status;
  private int capabilities;
	private Form msgDisplay;

	// Message count
	private int outgoingPlainMessagesCnt;
	private int plainMessages;
	private int urlMessages;

	// Menu
	private Menu menu;

  // Constructor
  public ContactListContactItem(int id, int group, String uin, String name)
  {
    this.id = id;
    this.group = group;
    this.uin = new String(uin);
    this.name = new String(name);
    this.temporary = false;
    this.status = ContactList.STATUS_OFFLINE;
    this.capabilities = ContactListContactItem.CAP_NO_INTERNAL;
    this.msgDisplay = new Form(ContactListContactItem.this.getName());
    this.outgoingPlainMessagesCnt = 0;
    this.plainMessages = 0;
    this.urlMessages = 0;
    this.menu = new Menu();
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


	// Returns true if this is an temporary contact
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
  // message is an URL message
  protected synchronized boolean isPlainMessageAvailable()
  {
    return (this.plainMessages > 0);
  }


  // Returns true if the next available message is an URL message
  // Returns false if no message at all is available, or if the next available
  // message is an URL message
  protected synchronized boolean isUrlMessageAvailable()
  {
    return (this.urlMessages > 0);
  }


	// Adds a message to the message display
	protected synchronized void addMessage(Message message) {
		Calendar stamp = Calendar.getInstance();
		stamp.setTime(message.getDate());
		Font prequelFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
		Image prequel = Image.createImage(120, prequelFont.getHeight() + 2);
		Graphics g = prequel.getGraphics();
		g.setColor(255, 0, 0);
		g.setFont(prequelFont);
		g.drawString(this.getName() + " (" + stamp.get(Calendar.HOUR_OF_DAY) + ":" + Util.makeTwo(stamp.get(Calendar.MINUTE)) + "):", 0, 10, Graphics.BASELINE | Graphics.LEFT);
		Image copy = Image.createImage(prequel);
		msgDisplay.append(new ImageItem(null, copy, ImageItem.LAYOUT_LEFT + ImageItem.LAYOUT_NEWLINE_BEFORE + ImageItem.LAYOUT_NEWLINE_AFTER, null));
		if (message instanceof PlainMessage)
		{
			PlainMessage plainMsg = (PlainMessage) message;
			if (!msgDisplay.isShown())
				plainMessages++;
			msgDisplay.append(new StringItem(null, plainMsg.getText()));
		}
		if (message instanceof UrlMessage)
		{
			UrlMessage urlMsg = (UrlMessage) message;
			if (!msgDisplay.isShown())
				urlMessages++;
			msgDisplay.append(new StringItem(null, "URL: "+urlMsg.getUrl()));
		    msgDisplay.append(new StringItem(null, urlMsg.getText()));
		}
	}

	// Returns if the chat hisotry screen is shown
	public boolean chatShown(){
		return msgDisplay.isShown();
	}

	// Returns if the contact item has a chat runnig
	public boolean hasChat(){
		if (msgDisplay.size() == 0)
			return false;
	    else
	    	return true;
	}

	public synchronized void resetUnreadMessages(){
		plainMessages=0;
		urlMessages=0;
	}

	// Delete the chat history
	public void deleteChatHistory(){
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

		// Compare, return negative int if cItem1 < cItem2, 0 if cItem1 == cItem2, or positive int if cItem1 > cItem2
		if ((cItem1Status != ContactList.STATUS_OFFLINE) && !cItem1IsTemporary)
		{
			if (((cItem2Status != ContactList.STATUS_OFFLINE) && !cItem2IsTemporary) || (Jimm.jimm.getOptionsRef().getClSortBy() != 0) )
			{
				return (this.getName().compareTo(cItem2.getName()));
			}
			else
			{
				return (-1);
			}
		}
		else if ((cItem1Status == ContactList.STATUS_OFFLINE) && !cItem1IsTemporary)
		{
			if ((cItem2Status != ContactList.STATUS_OFFLINE) && !cItem2IsTemporary && (Jimm.jimm.getOptionsRef().getClSortBy() == 0))
			{
				return (1);
			}
			else if (((cItem2Status == ContactList.STATUS_OFFLINE) && !cItem2IsTemporary)|| (Jimm.jimm.getOptionsRef().getClSortBy() != 0))
			{
				return (this.getName().compareTo(cItem2.getName()));
			}
			else
			{
				return (-1);
			}
		}
		else if (cItem1IsTemporary)
		{
			if (cItem2IsTemporary)
			{
				return (this.getName().compareTo(cItem2.getName()));
			}
			else
			{
				return (1);
			}
		}
		else
		{
			return (0);
		}

	}


	/****************************************************************************/
	/****************************************************************************/
	/****************************************************************************/


	private class Menu implements CommandListener
	{

		// Command listener
		public void commandAction(Command c, Displayable d)
		{

			// Return to contact list
			if (c == MenuUtil.backCommand)
			{
				Jimm.jimm.getContactListRef().activate();
			}
			// Message has been closed
			else if (c == MenuUtil.msgCloseCommand)
			{
				if (!Jimm.jimm.getOptionsRef().keepChat()){
					ContactListContactItem.this.deleteChatHistory();
				}
				Jimm.jimm.getContactListRef().update(ContactListContactItem.this.getUin());
				Jimm.jimm.getContactListRef().activate();
			}
			// User wants to send a reply
			else if (c == MenuUtil.msgReplyCommand)
			{

				if (!Jimm.jimm.getOptionsRef().keepChat()){
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
					case 0:   // Send plain message

						// Reset and display textbox for entering messages
						MenuUtil.messageTextbox.setString(null);
						MenuUtil.messageTextbox.removeCommand(MenuUtil.textboxOkCommand);
						MenuUtil.messageTextbox.addCommand(MenuUtil.textboxSendCommand);
						MenuUtil.messageTextbox.setCommandListener(this);
						Jimm.display.setCurrent(MenuUtil.messageTextbox);

						break;
					case 1:   // Send URL message

						// Reset and display textbox for entering messages
						MenuUtil.messageTextbox.setString(null);
						MenuUtil.messageTextbox.removeCommand(MenuUtil.textboxSendCommand);
						MenuUtil.messageTextbox.addCommand(MenuUtil.textboxOkCommand);
						MenuUtil.messageTextbox.setCommandListener(this);
						Jimm.display.setCurrent(MenuUtil.messageTextbox);

						break;
					case 2:   // Info

						// Display splash canvas
						SplashCanvas wait1 = Jimm.jimm.getSplashCanvasRef();
						wait1.setMessage(ResourceBundle.getString("jimm.res.Text", "wait"));
						wait1.setProgress(0);
						Jimm.display.setCurrent(wait1);

						// Request info from server
						RequestInfoAction act1 = new RequestInfoAction(ContactListContactItem.this.getUin());
						try
						{
							Jimm.jimm.getIcqRef().requestAction(act1);
						}
						catch (JimmException e)
						{
							JimmException.handleException(e);
							if (e.isCritical()) return;
						}

						// Start timer
						Jimm.jimm.getTimerRef().schedule(new SplashCanvas.RequestInfoTimerTask(act1), 1000, 1000);

						break;
					case 3:   // Remove

						// Check whether contact item is temporary
						if (ContactListContactItem.this.isTemporary())
						{

							// Remove this temporary contact item
							Jimm.jimm.getContactListRef().removeContactItem(ContactListContactItem.this);

							if (!Jimm.jimm.getOptionsRef().keepChat()){
								ContactListContactItem.this.deleteChatHistory();
							}

							// Update and activate contact list
							Jimm.jimm.getContactListRef().update();
							Jimm.jimm.getContactListRef().activate();

						}
						else
						{

							// Display splash canvas
							SplashCanvas wait2 = Jimm.jimm.getSplashCanvasRef();
							wait2.setMessage(ResourceBundle.getString("jimm.res.Text", "wait"));
							wait2.setProgress(0);
							Jimm.display.setCurrent(wait2);

							// Request contact item removal
							UpdateContactListAction act2 = new UpdateContactListAction(ContactListContactItem.this);
							try
							{
								Jimm.jimm.getIcqRef().requestAction(act2);
							}
							catch (JimmException e)
							{
								JimmException.handleException(e);
								if (e.isCritical()) return;
							}

							// Start timer
							Jimm.jimm.getTimerRef().schedule(new SplashCanvas.UpdateContactListTimerTask(act2), 1000, 1000);

						}

						break;
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
						// Append advertisement?
						boolean appendAdvertisement = false;
						switch (Jimm.jimm.getOptionsRef().getDisplayAdvertisementMode())
						{
							case 0:
								appendAdvertisement = true;
								ContactListContactItem.this.outgoingPlainMessagesCnt++;
								break;
							case 1:
								appendAdvertisement = (ContactListContactItem.this.outgoingPlainMessagesCnt == 0);
								ContactListContactItem.this.outgoingPlainMessagesCnt++;
								break;
						}

						// Construct plain message object and request new SendMessageAction
						// Add the new message to the chat history
            			PlainMessage plainMsg = new PlainMessage(Jimm.jimm.getIcqRef().getUin(), ContactListContactItem.this, new Date(), MenuUtil.messageTextbox.getString(), appendAdvertisement);

						if (Jimm.jimm.getOptionsRef().keepChat()) {

							Calendar stamp = Calendar.getInstance();
							stamp.setTime(plainMsg.getDate());

							Font prequelFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
							Image prequel = Image.createImage(80, prequelFont.getHeight() + 2);
							Graphics g = prequel.getGraphics();
							g.setColor(0, 0, 255);
							g.setFont(prequelFont);
							g.drawString(ResourceBundle.getString("jimm.res.Text", "me") + " (" + stamp.get(Calendar.HOUR_OF_DAY) + ":" + Util.makeTwo(stamp.get(Calendar.MINUTE)) + "):", 0, 10, Graphics.BASELINE | Graphics.LEFT);
							Image copy = Image.createImage(prequel);

							msgDisplay.append(new ImageItem(null, copy, ImageItem.LAYOUT_LEFT + ImageItem.LAYOUT_NEWLINE_BEFORE + ImageItem.LAYOUT_NEWLINE_AFTER, null));
							msgDisplay.append(new StringItem(null, plainMsg.getText()));
						}
						SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
						try
						{
							Jimm.jimm.getIcqRef().requestAction(sendMsgAct);
						}
						catch (JimmException e)
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

					// Construct URL message object and request new SendMessageAction
          UrlMessage urlMsg = new UrlMessage(Jimm.jimm.getIcqRef().getUin(), ContactListContactItem.this, new Date(), MenuUtil.urlTextbox.getString(), MenuUtil.messageTextbox.getString());
					SendMessageAction sendMsgAct = new SendMessageAction(urlMsg);
					try
					{
						Jimm.jimm.getIcqRef().requestAction(sendMsgAct);
					}
					catch (JimmException e)
					{
						JimmException.handleException(e);
						if (e.isCritical()) return;
					}

					if (!Jimm.jimm.getOptionsRef().keepChat()){
						ContactListContactItem.this.deleteChatHistory();
					}

					// Return to contact list
					Jimm.jimm.getContactListRef().update();
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

		}


		// Activates the contact item menu
		public void activate() {

			// Display chat history
			if (ContactListContactItem.this.hasChat()) {
				msgDisplay.addCommand(MenuUtil.msgCloseCommand);
				msgDisplay.addCommand(MenuUtil.msgReplyCommand);
				msgDisplay.addCommand(MenuUtil.deleteChatCommand);
				msgDisplay.addCommand(MenuUtil.addMenuCommand);
				msgDisplay.setCommandListener(this);
				// Display history
				ContactListContactItem.this.resetUnreadMessages();
				Jimm.display.setCurrent(msgDisplay);

			}
			// Display menu
			else {
				MenuUtil.menuList.setTitle(ContactListContactItem.this.name);
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


		// Abort command
		private static Command backCommand = new Command(ResourceBundle.getString("jimm.res.Text", "back"), Command.BACK, 1);


		// Message close command
		private static Command msgCloseCommand = new Command(ResourceBundle.getString("jimm.res.Text", "close"), Command.BACK, 2);


		// Message close and reply command
		private static Command msgReplyCommand = new Command(ResourceBundle.getString("jimm.res.Text", "reply"), Command.OK, 1);

		//Show the message menu
		private static Command addMenuCommand = new Command(ResourceBundle.getString("jimm.res.Text", "user_menu"), Command.OK, 3);

		//Delete Chat History
		private static Command deleteChatCommand = new Command(ResourceBundle.getString("jimm.res.Text", "delete_chat"), Command.BACK, 4);

		// Textbox OK command
		private static Command textboxOkCommand = new Command(ResourceBundle.getString("jimm.res.Text", "ok"), Command.OK, 1);


		// Textbox Send command
		private static Command textboxSendCommand = new Command(ResourceBundle.getString("jimm.res.Text", "send"), Command.OK, 1);


		// Textbox cancel command
		private static Command textboxCancelCommand = new Command(ResourceBundle.getString("jimm.res.Text", "cancel"), Command.CANCEL, 2);


		// Initializer
		static
		{

			// Initialize the menu list
			MenuUtil.menuList = new List("set", Choice.IMPLICIT);
			MenuUtil.menuList.append(ResourceBundle.getString("jimm.res.Text", "send_message"), null);
			MenuUtil.menuList.append(ResourceBundle.getString("jimm.res.Text", "send_url"), null);
			MenuUtil.menuList.append(ResourceBundle.getString("jimm.res.Text", "info"), null);
			MenuUtil.menuList.append(ResourceBundle.getString("jimm.res.Text", "remove"), null);
			MenuUtil.menuList.addCommand(MenuUtil.backCommand);

			// Initialize the textbox for entering messages
			MenuUtil.messageTextbox = new TextBox(ResourceBundle.getString("jimm.res.Text", "message"), null, 1000, TextField.ANY);
			MenuUtil.messageTextbox.addCommand(MenuUtil.textboxCancelCommand);

			// Initialize the textbox for entering URLs
			MenuUtil.urlTextbox = new TextBox(ResourceBundle.getString("jimm.res.Text", "url"), null, 1000, TextField.URL);
			MenuUtil.urlTextbox.addCommand(MenuUtil.textboxCancelCommand);
			MenuUtil.urlTextbox.addCommand(MenuUtil.textboxSendCommand);

		}


	}


}
