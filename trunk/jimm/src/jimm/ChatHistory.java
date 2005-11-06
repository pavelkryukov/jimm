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
 Author(s): Andreas Rossbacher, Artyomov Denis, Dmitry Tunin
 *******************************************************************************/

package jimm;

import java.util.Enumeration;
import java.util.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Canvas;

import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SystemNotice;
import jimm.comm.UrlMessage;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;

class MessData
{
	private boolean incoming;
	private Date time;
	private String from;
	private int textOffset;
	
	public MessData(boolean incoming, Date time, String from, int textOffset)
	{
		this.incoming   = incoming;
		this.time       = time;
		this.from       = from;
		this.textOffset = textOffset;
	}
		
	public boolean getIncoming() { return incoming; }
	public String getFrom() { return from; }
	public Date getTime() { return time; }
	public int getOffset() { return textOffset; }
}

class ChatTextList extends TextList implements VirtualListCommands
{
	public String ChatName;
	private Vector messData = new Vector();
	private int messTotalCounter = 0;
	
	ChatTextList(String name)
	{
		super(null);
		
		this.setCursorMode(TextList.SEL_NONE);
		this.setFontSize
		(
			Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_CHAT_SMALL_FONT)
				   ? TextList.SMALL_FONT : TextList.MEDIUM_FONT
		);
		
		ChatName = name;
		JimmUI.setColorScheme(this);
		
		setVLCommands(this);
	}

	Vector getMessData()
	{
		return messData; 
	}
	
	public void onCursorMove(VirtualList sender) {}
	public void onItemSelected(VirtualList sender) {}

	public void onKeyPress(VirtualList sender, int keyCode)
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
	
	void addTextToForm(String from, String message, String url, Date time, boolean red, boolean offline)
	{
		int texOffset;
		
		lock();
		int lastSize = getSize();
		addBigText
		(
			from + " (" + Util.getDateString(!offline, time) + "): ",
			red ? 0xFF0000 : Jimm.jimm.getOptionsRef().getSchemeColor(Options.CLRSCHHEME_BLUE), 
			Font.STYLE_BOLD,
			messTotalCounter
		);
		doCRLF(messTotalCounter);

		if (url.length() > 0)
		{
			addBigText
			(
				ResourceBundle.getString("url")+": "+url, 
				0x00FF00, 
				Font.STYLE_PLAIN, messTotalCounter
			);
		}
		
		texOffset = getSize()-lastSize;
		
		//#sijapp cond.if modules_SMILES is "true" #
		Jimm.jimm.getEmotionsRef().addTextWithEmotions(this, message, Font.STYLE_PLAIN, getTextColor(), messTotalCounter);
		//#sijapp cond.else#
		addBigText(message, getTextColor(), Font.STYLE_PLAIN, messTotalCounter);
		//#sijapp cond.end#
		doCRLF(messTotalCounter);
		
		setTopItem(lastSize);		
		unlock();
		getMessData().addElement( new MessData(red, time, from, texOffset) );
		
		messTotalCounter++;

	}
}

public class ChatHistory
{
	private Hashtable historyTable;
	private int counter;

	// Adds selected message to history
	//#sijapp cond.if modules_HISTORY is "true" #
	public void addTextToHistory(String uin)
	{
		ChatTextList list = getChatHistoryAt(uin);
		int textIndex = list.getCurrTextIndex();
		String text = list.getCurrText(1);
		if (text == null) return;
		
		MessData data = (MessData)list.getMessData().elementAt(textIndex);
		Jimm.jimm.getHistory().addText
		(
			uin, 
			text, 
			data.getIncoming() ? (byte)0 : (byte)1, 
			data.getFrom(), 
			data.getTime()
		);
	}
	//#sijapp cond.end#

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
		
		boolean offline = message.getOffline();

		if (message instanceof PlainMessage)
		{
			PlainMessage plainMsg = (PlainMessage) message;
			if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_PLAIN);
			this.addTextToForm(uin,contact.getName(), plainMsg.getText(), "", plainMsg.getDate(), true, offline);

			// #sijapp cond.if modules_HISTORY is "true" #
			if ( Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_HISTORY) )
				Jimm.jimm.getHistory().addText(contact.getUin(), plainMsg.getText(), (byte)0, contact.getName(), plainMsg.getDate());
			// #sijapp cond.end#	
			
			ContactListContactItem.messageReceived(uin, plainMsg.getText());
		}
		if (message instanceof UrlMessage)
		{
			UrlMessage urlMsg = (UrlMessage) message;
			if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_URL);
			this.addTextToForm(uin,contact.getName(), urlMsg.getText(), urlMsg.getUrl(), urlMsg.getDate(), false, offline);
		}
		if (message instanceof SystemNotice)
		{
			SystemNotice notice = (SystemNotice) message;
			if (!msgDisplay.isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_SYS_NOTICE);

			if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_YOUWEREADDED)
			{
				this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("youwereadded")
						+ notice.getSndrUin(), "", notice.getDate(), false, offline);
			} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ)
			{
				contact.increaseMessageCount(ContactListContactItem.MESSAGE_AUTH_REQUEST);
				this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), notice.getSndrUin()
						+ ResourceBundle.getString("wantsyourauth") + notice.getReason(), "", notice.getDate(), false, offline);
			} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY)
			{
				if (notice.isAUTH_granted())
				{
					contact.setBoolValue(ContactListContactItem.VALUE_NO_AUTH,false);
					this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("grantedby")
							+ notice.getSndrUin() + ".", "", notice.getDate(), false, offline);
				} else if (notice.getReason() != null)
					this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
							+ notice.getSndrUin() + ". " + ResourceBundle.getString("reason") + ": " + notice.getReason(),
							"", notice.getDate(), false, offline);
				else
					this.addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
							+ notice.getSndrUin() + ". " + ResourceBundle.getString("noreason"), "", notice.getDate(),
							false, offline);
			}
		}
	}
	
	protected synchronized void addMyMessage(String uin, String message, Date time, String ChatName)
	{
		if (!historyTable.containsKey(uin))
			newChatForm(uin,ChatName);

		addTextToForm(uin,ResourceBundle.getString("me"),message,"",time, false, false);
	}
	
	// Add text to message form
	synchronized private void addTextToForm(String uin,String from, String message, String url, Date time, boolean red, boolean offline)
	{
		ChatTextList msgDisplay = (ChatTextList) historyTable.get(uin);

		msgDisplay.addTextToForm(from, message, url, time, red, offline);
	}
	
	public void copyText(String uin)
	{
		ChatTextList list = getChatHistoryAt(uin);
		int messIndex = list.getCurrTextIndex();
		MessData md = (MessData)list.getMessData().elementAt(messIndex);
		
		JimmUI.setClipBoardText
		(
			md.getIncoming(),
			Util.getDateString(false, md.getTime()),
			md.getFrom(),
			list.getCurrText(md.getOffset())
		);
	}

	// Returns the chat history form at the given uin
	public ChatTextList getChatHistoryAt(String uin)
	{
		if (historyTable.containsKey(uin))
			return (ChatTextList) historyTable.get(uin);
		else
			return new ChatTextList("Error");
	}

	// Delete the chat history for uin
	public void chatHistoryDelete(String uin)
	{
		historyTable.remove(uin);
		if (counter > 1) incCounter(false);
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

	// Returns true if chat history exists for this uin
	public boolean chatHistoryExists(String uin)
	{
		return historyTable.containsKey(uin);
	}

	
	// Creates a new chat form
	private void newChatForm(String uin,String name)
	{
		ChatTextList chatForm = new ChatTextList(name);
		historyTable.put(uin,chatForm);
		UpdateCaption(uin);
	}
	
	public void contactRenamed(String uin, String newName)
	{
		ChatTextList temp = (ChatTextList) this.historyTable.get(uin);
		if (temp == null) return;
		temp.ChatName = newName;
		UpdateCaption(uin);
	}

	public void UpdateCaption(String uin)
	{
		ChatTextList temp = (ChatTextList) this.historyTable.get(uin);
		// Calculate the title for the chatdisplay.
		String Title = temp.ChatName+" ("+counter+"/"+historyTable.size()+")";
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		temp.setFullScreenMode(false);
		temp.setTitle(Title);
		// #sijapp cond.else#
		temp.setCaption(Title);
		// #sijapp cond.end#
		
	}

	public void setColorScheme()
	{
		Enumeration AllChats = historyTable.elements();
		while (AllChats.hasMoreElements())
			JimmUI.setColorScheme((ChatTextList)AllChats.nextElement());
	}
	
	// Sets the counter for the ChatHistory
    public void incCounter(boolean up)
    {
        counter = (counter + (up ? +1 : -1)) % historyTable.size();
    }

}