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
 File: src/jimm/ChatHistory.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Artyomov Denis
 *******************************************************************************/

package jimm;

import java.util.Enumeration;
import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Canvas;

import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SystemNotice;
import jimm.comm.UrlMessage;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import DrawControls.TextList;

class ChatTextList extends TextList
{
	public String ChatName;
	ChatTextList(String name)
	{
		super
		(
			null, 
			TextList.getDefCapColor(),
			TextList.getDefCapFontColor(),
			TextList.getDefBackColor(),
			Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_CHAT_SMALL_FONT)
			   ? TextList.SMALL_FONT : TextList.MEDIUM_FONT,
			TextList.SEL_NONE
		);
		ChatName = name;
			Jimm.setColorScheme(this);
	}

	protected void userPressKey(int keyCode)
	{
		switch (getGameAction(keyCode))
		{
		case Canvas.LEFT:
			Jimm.jimm.getChatHistoryRef().incCounter(false);
			Jimm.jimm.getContactListRef().showNextPrevChat(false);
			break;
			
		case Canvas.RIGHT:
			Jimm.jimm.getChatHistoryRef().incCounter(true);
			Jimm.jimm.getContactListRef().showNextPrevChat(true);
			
			break;
		}
	}

}

public class ChatHistory
{
	private Hashtable historyTable;
	private int counter;
	

	public ChatHistory()
	{
		historyTable = new Hashtable();
		counter = 1;
	}

	// Adds a message to the message display
	protected synchronized void addMessage(String uin,Message message,ContactListContactItem contact)
	{
		if (!historyTable.containsKey(uin))
			newChatForm(uin,contact.getName());

		TextList msgDisplay = (TextList) historyTable.get(uin);

		if (message instanceof PlainMessage)
		{
			PlainMessage plainMsg = (PlainMessage) message;
			if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_PLAIN);
			this.addTextToForm(uin,contact.getName(), plainMsg.getText(), "", plainMsg.getDate(), true);

			// #sijapp cond.if modules_HISTORY is "true" #
			if ( Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_HISTORY) )
				Jimm.jimm.getHistory().addText(contact.getUin(), plainMsg.getText(), (byte)0, contact.getName());
			// #sijapp cond.end#	
		}
		if (message instanceof UrlMessage)
		{
			UrlMessage urlMsg = (UrlMessage) message;
			if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_URL);
			this.addTextToForm(uin,contact.getName(), urlMsg.getText(), urlMsg.getUrl(), urlMsg.getDate(), false);
		}
		if (message instanceof SystemNotice)
		{
			SystemNotice notice = (SystemNotice) message;
			if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_SYS_NOTICE);

			if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_YOUWEREADDED)
			{
				this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("youwereadded")
						+ notice.getSndrUin(), "", notice.getDate(), false);
			} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ)
			{
				contact.increaseMessageCount(ContactListContactItem.MESSAGE_AUTH_REQUEST);
				this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), notice.getSndrUin()
						+ ResourceBundle.getString("wantsyourauth") + notice.getReason(), "", notice.getDate(), false);
			} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY)
			{
				if (notice.isAUTH_granted())
				{
					contact.setBoolValue(ContactListContactItem.VALUE_NO_AUTH,false);
					this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("grantedby")
							+ notice.getSndrUin() + ".", "", notice.getDate(), false);
				} else if (notice.getReason() != null)
					this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
							+ notice.getSndrUin() + ". " + ResourceBundle.getString("reason") + ": " + notice.getReason(),
							"", notice.getDate(), false);
				else
					this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
							+ notice.getSndrUin() + ". " + ResourceBundle.getString("noreason"), "", notice.getDate(),
							false);
			}
		}
	}
	
	protected synchronized void addMyMessage(String uin, String message, Date time, String ChatName)
	{
		if (!historyTable.containsKey(uin))
			newChatForm(uin,ChatName);

		addTextToForm(uin,ResourceBundle.getString("me"),message,"",time,false);
	}
	
	// Add text to message form
	synchronized private void addTextToForm(String uin,String from, String message, String url, Date time, boolean red)
	{

		TextList msgDisplay = (TextList) historyTable.get(uin);

		msgDisplay.lock();
		int lastSize = msgDisplay.getItemCount();
		msgDisplay.addBigText
		(
		  from + " (" + Util.getDateString(true) + "):",
		  red ? 0xFF0000 : Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_BLUE), 
		  Font.STYLE_BOLD
		);

		if (url.length() > 0)
		{
			msgDisplay.addBigText(ResourceBundle.getString("url")+": "+url, 0x00FF00, Font.STYLE_PLAIN);
		}

		msgDisplay.addBigText(message, msgDisplay.getTextColor(), Font.STYLE_PLAIN);
		
		msgDisplay.setTopItem(lastSize);		
		msgDisplay.unlock();
	}

	// Returns the chat history form at the given nr
	public TextList getChatHistoryAt(String uin)
	{
		if (historyTable.containsKey(uin))
			return (TextList) historyTable.get(uin);
		else
			return new TextList("Error");
	}

	// Delete the chat hisotry to uin
	public void chatHistoryDelete(String uin)
	{
		historyTable.remove(uin);
		int total = historyTable.size();
		if (total > 0) counter = ((counter--) % total) + 1;
	}

	// Returns if the chat history at the given number is shown
	public boolean chatHistoryShown(String uin)
	{
		if (historyTable.containsKey(uin))
		{
			TextList temp = (TextList)historyTable.get(uin);
			return temp.isShown();
		}
		else
			return false;
	}

	// Returns the size of the chat histore at number nr
	public int chatHistorySize(String uin)
	{
		if (historyTable.containsKey(uin))
		{
			TextList temp = (TextList) historyTable.get(uin);
			return temp.getItemCount();
		} else
			return -1;
	}

	// Return the size of the History Hash
	public int chatHistoryTableSize(String uin)
	{
		return historyTable.size();
	}

	// Creates a new chat form and returns the index number of it in the vector
	public int newChatForm(String uin,String name)
	{
		ChatTextList chatForm = new ChatTextList(name);
		historyTable.put(uin,chatForm);
		UpdateCaption(uin);
		return historyTable.size();
	}

	public void UpdateCaption(String uin)
	{
		ChatTextList temp = (ChatTextList) this.historyTable.get(uin);
		// Calculate the title for the chatdisplay.
		String Title = temp.ChatName+" ("+counter+"/"+this.historyTable.size()+")";
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		temp.setFullScreenMode(false);
		temp.setTitle(Title);
		// #sijapp cond.else#
		temp.setCaption(Title);
		// #sijapp cond.end# */
		
	}

	public void setColorScheme()
	{
		int count = historyTable.size();
		Enumeration AllChats = historyTable.elements();
		while (AllChats.hasMoreElements())
			Jimm.setColorScheme((ChatTextList)AllChats.nextElement());
	}
	// Sets the counter for the ChatHistory
	public void incCounter(boolean up)
	{
	    if (up)
	        counter =((counter++) % historyTable.size())+1;
	    else
	        counter =((counter--) % historyTable.size())+1;
	}
	public int getCounter()
	{
		return counter;
	}

}