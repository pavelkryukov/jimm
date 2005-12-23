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
import javax.microedition.lcdui.*;

import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SystemNotice;
import jimm.comm.UrlMessage;
import jimm.comm.Util;
import jimm.util.ResourceBundle;
//#sijapp cond.if modules_HISTORY is "true" #
import jimm.HistoryStorage;
//#sijapp cond.end#
import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;

// #sijapp cond.if target is "SIEMENS2"#
class ChatItem extends CustomItem
{
	private int height, width;
	private TextList textList;
	
	public ChatItem(int width, int height)
	{
		super(new String());
		this.height   = height;
		this.width    = width;
	}
	
	void setTextList(TextList textList)
	{
		this.textList = textList;
	}
	
	protected int getMinContentHeight()
	{
		return height;
	}
	
	protected int getMinContentWidth()
	{
		return width;
	}
	
	protected int getPrefContentHeight(int width)
	{
		return height;
	}
	
	protected int getPrefContentWidth(int height)
	{
		return width;
	}
	
	protected void paint(Graphics g, int w, int h)
	{
		textList.setForcedSize(w, h);
		textList.paintAllOnGraphics(g);
	}
	
	protected void keyPressed(int keyCode)
	{
		textList.doKeyreaction(keyCode, VirtualList.KEY_PRESSED);
		repaint();
	}
	
	protected void keyRepeated(int keyCode)
	{
		textList.doKeyreaction(keyCode, VirtualList.KEY_REPEATED);
		repaint();
	}
	
	protected void keyReleased(int keyCode)
	{
		textList.doKeyreaction(keyCode, VirtualList.KEY_RELEASED);
		repaint();
	}
	
	protected void showNotify()
	{
		ChatTextList.updateChatHeight(true);
	}
	
	void setHeight(int value)
	{
		height = value;
		invalidate();
	}
	
	void updateContents()
	{
		repaint();
	}
}
// #sijapp cond.end#

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

class ChatTextList implements VirtualListCommands
                             // #sijapp cond.if target is "SIEMENS2"#
                             ,ItemStateListener
                             ,ItemCommandListener
                             // #sijapp cond.end#
{
	// #sijapp cond.if target is "SIEMENS2"#
	static public Form form;
	static public TextField textLine;
	static public ChatItem chatItem;
	static private Command cmdSend; 
	
	static
	{
		form = new Form(null);
		textLine = new TextField(null, null, 1000, TextField.ANY);
		chatItem = new ChatItem(form.getWidth()-4, 10);
		cmdSend = new Command(ResourceBundle.getString("send"), Command.OK, 0);
		form.append(chatItem);
		form.append(textLine);
		
		textLine.addCommand(cmdSend);
	}
	// #sijapp cond.end#
	
	TextList textList;
	public String ChatName, uin;
	private Vector messData = new Vector();
	private int messTotalCounter = 0;
	
	ChatTextList(String name, String uin)
	{
		textList = new TextList(null);
		
		textList.setCursorMode(TextList.SEL_NONE);
		textList.setFontSize
		(
			Options.getBooleanOption(Options.OPTION_CHAT_SMALL_FONT)
				   ? TextList.SMALL_FONT : TextList.MEDIUM_FONT
		);
		
		this.uin = uin;
		ChatName = name;
		JimmUI.setColorScheme(textList);
		
		textList.setVLCommands(this);
	}
	
	public Displayable getDisplayable()
	{
		// #sijapp cond.if target is "SIEMENS2"#
		return Options.getBooleanOption(Options.OPTION_CLASSIC_CHAT) ? (Displayable)form : (Displayable)textList;
		// #sijapp cond.else#
		return textList;
		// #sijapp cond.end#
	}
	
	static int getInOutColor(boolean incoming)
	{
		return incoming ? 
		       0xFF0000 : 
		       Options.getSchemeColor(Options.CLRSCHHEME_BLUE);
	}
	

	Vector getMessData()
	{
		return messData; 
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
				ChatHistory.calcCounter(currUin);
				return;
				
			case Canvas.RIGHT:
				currUin = ContactList.showNextPrevChat(true);
				ChatHistory.calcCounter(currUin);
				return;
			}
		}

		JimmUI.execHotKey(ContactList.getItembyUIN(uin), keyCode, type);
	}
	
	
	void addTextToForm(String from, String message, String url, Date time, boolean red, boolean offline)
	{
		int texOffset;
		
		textList.lock();
		int lastSize = textList.getSize();
		textList.addBigText
		(
			from + " (" + Util.getDateString(!offline, time) + "): ",
			getInOutColor(red), 
			Font.STYLE_BOLD,
			messTotalCounter
		);
		textList.doCRLF(messTotalCounter);

		if (url.length() > 0)
		{
			textList.addBigText
			(
				ResourceBundle.getString("url")+": "+url, 
				0x00FF00, 
				Font.STYLE_PLAIN, messTotalCounter
			);
		}
		
		texOffset = textList.getSize()-lastSize;
		
		//#sijapp cond.if modules_SMILES is "true" #
		Emotions.addTextWithEmotions(textList, message, Font.STYLE_PLAIN, textList.getTextColor(), messTotalCounter);
		//#sijapp cond.else#
		textList.addBigText(message, textList.getTextColor(), Font.STYLE_PLAIN, messTotalCounter);
		//#sijapp cond.end#
		textList.doCRLF(messTotalCounter);
		
		textList.setTopItem(lastSize);		
		textList.unlock();
		
		// #sijapp cond.if target is "SIEMENS2"#
		chatItem.updateContents();
		//#sijapp cond.end#
		
		getMessData().addElement( new MessData(red, time, from, texOffset) );
		
		messTotalCounter++;
	}
	
	public void activate(boolean initChat)
	{
		//#sijapp cond.if target is "SIEMENS2"#
		if ( Options.getBooleanOption(Options.OPTION_CLASSIC_CHAT) )
		{
			form.setItemStateListener(this);
			textLine.setItemCommandListener(this);
			chatItem.setTextList(textList);
			chatItem.updateContents();
			Jimm.display.setCurrent(form);
			System.out.println("activate "+initChat);
			if (initChat) Jimm.display.setCurrentItem(textLine);
		}
		else Jimm.display.setCurrent(textList); 
		//#sijapp cond.else#
		Jimm.display.setCurrent(textList);
		//#sijapp cond.end#
	}
	
	//#sijapp cond.if target is "SIEMENS2"#
	private static int lastHeight = -1;
	public void itemStateChanged(Item item)
	{
		if (item == textLine) updateChatHeight(false);
	}
	
	static void updateChatHeight(boolean force)
	{
		int height = form.getHeight()-textLine.getPreferredHeight()-4;
		if (lastHeight != height)
		{
			chatItem.setHeight(height);
			lastHeight = height; 
		}
	}
	
	public void commandAction(Command c, Item item)
	{
		if (c == cmdSend)
		{
			ContactListContactItem cItem = ContactList.getItembyUIN(uin);
			cItem.sendMessage(textLine.getString());
			textLine.setString(new String());
			updateChatHeight(true);
		}
	}
	
	//#sijapp cond.end#
}

public class ChatHistory
{
	static private Hashtable historyTable;
	static private int counter;

	// Adds selected message to history
	//#sijapp cond.if modules_HISTORY is "true" #
	static public void addTextToHistory(String uin)
	{
		ChatTextList list = getChatHistoryAt(uin);
		int textIndex = list.textList.getCurrTextIndex();
		String text = list.textList.getCurrText(1);
		if (text == null) return;
		
		MessData data = (MessData)list.getMessData().elementAt(textIndex);
		HistoryStorage.addText
		(
			uin, 
			text, 
			data.getIncoming() ? (byte)0 : (byte)1, 
			data.getFrom(), 
			data.getTime()
		);
	}
	//#sijapp cond.end#

	static
	{
		historyTable = new Hashtable();
		counter = 1;
	}

	// Adds a message to the message display
	static protected synchronized void addMessage(String uin,Message message,ContactListContactItem contact)
	{
		if (!historyTable.containsKey(uin))
			newChatForm(uin,contact.getStringValue(ContactListContactItem.CONTACTITEM_NAME));
		
		ChatTextList chat = (ChatTextList)historyTable.get(uin);

		boolean offline = message.getOffline();

		if (message instanceof PlainMessage)
		{
			PlainMessage plainMsg = (PlainMessage) message;
			
			if (!chat.getDisplayable().isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_PLAIN);
			
			addTextToForm(uin,contact.getStringValue(ContactListContactItem.CONTACTITEM_NAME), plainMsg.getText(), "", plainMsg.getDate(), true, offline);
			
			// #sijapp cond.if modules_HISTORY is "true" #
			if ( Options.getBooleanOption(Options.OPTION_HISTORY) )
				HistoryStorage.addText(contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN), plainMsg.getText(), (byte)0, contact.getStringValue(ContactListContactItem.CONTACTITEM_NAME), plainMsg.getDate());
			// #sijapp cond.end#
			
			if ( !message.getOffline() )
				ContactListContactItem.showPopupWindow(uin, contact.getStringValue(ContactListContactItem.CONTACTITEM_NAME), plainMsg.getText());
		}
		if (message instanceof UrlMessage)
		{
			UrlMessage urlMsg = (UrlMessage) message;
			if (!chat.getDisplayable().isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_URL);
			addTextToForm(uin,contact.getStringValue(ContactListContactItem.CONTACTITEM_NAME), urlMsg.getText(), urlMsg.getUrl(), urlMsg.getDate(), false, offline);
		}
		if (message instanceof SystemNotice)
		{
			SystemNotice notice = (SystemNotice) message;
			if (!chat.getDisplayable().isShown()) contact.increaseMessageCount(ContactListContactItem.MESSAGE_SYS_NOTICE);

			if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_YOUWEREADDED)
			{
				addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("youwereadded")
						+ notice.getSndrUin(), "", notice.getDate(), false, offline);
			} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ)
			{
				contact.increaseMessageCount(ContactListContactItem.MESSAGE_AUTH_REQUEST);
				addTextToForm(uin,ResourceBundle.getString("sysnotice"), notice.getSndrUin()
						+ ResourceBundle.getString("wantsyourauth") + notice.getReason(), "", notice.getDate(), false, offline);
			} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY)
			{
				if (notice.isAUTH_granted())
				{
					contact.setBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH,false);
					addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("grantedby")
							+ notice.getSndrUin() + ".", "", notice.getDate(), false, offline);
				} else if (notice.getReason() != null)
					addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
							+ notice.getSndrUin() + ". " + ResourceBundle.getString("reason") + ": " + notice.getReason(),
							"", notice.getDate(), false, offline);
				else
					addTextToForm(uin,ResourceBundle.getString("sysnotice"), ResourceBundle.getString("denyedby")
							+ notice.getSndrUin() + ". " + ResourceBundle.getString("noreason"), "", notice.getDate(),
							false, offline);
			}
		}
	}
	
	static protected synchronized void addMyMessage(String uin, String message, Date time, String ChatName)
	{
		if (!historyTable.containsKey(uin))
			newChatForm(uin,ChatName);

		addTextToForm(uin,ResourceBundle.getString("me"),message,"",time, false, false);
	}
	
	// Add text to message form
	static synchronized private void addTextToForm(String uin,String from, String message, String url, Date time, boolean red, boolean offline)
	{
		ChatTextList msgDisplay = (ChatTextList) historyTable.get(uin);

		msgDisplay.addTextToForm(from, message, url, time, red, offline);
	}
	
	static public void copyText(String uin)
	{
		ChatTextList list = getChatHistoryAt(uin);
		int messIndex = list.textList.getCurrTextIndex();
		MessData md = (MessData)list.getMessData().elementAt(messIndex);
		
		JimmUI.setClipBoardText
		(
			md.getIncoming(),
			Util.getDateString(false, md.getTime()),
			md.getFrom(),
			list.textList.getCurrText(md.getOffset())
		);
	}

	// Returns the chat history form at the given uin
	static public ChatTextList getChatHistoryAt(String uin)
	{
		if (historyTable.containsKey(uin))
			return (ChatTextList) historyTable.get(uin);
		else
			return new ChatTextList("Error", null);
	}

	// Delete the chat history for uin
	static public void chatHistoryDelete(String uin)
	{
		historyTable.remove(uin);
	}

	// Returns if the chat history at the given number is shown
	static public boolean chatHistoryShown(String uin)
	{
		if (historyTable.containsKey(uin))
		{
			ChatTextList temp = (ChatTextList)historyTable.get(uin);
			return temp.getDisplayable().isShown();
		}
		else
			return false;
	}

	// Returns true if chat history exists for this uin
	static public boolean chatHistoryExists(String uin)
	{
		return historyTable.containsKey(uin);
	}

	
	// Creates a new chat form
	static private void newChatForm(String uin, String name)
	{
		ChatTextList chatForm = new ChatTextList(name, uin);
		historyTable.put(uin,chatForm);
		UpdateCaption(uin);
		ContactList.getItembyUIN(uin).setBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT,true); ///
		//#sijapp cond.if modules_HISTORY is "true" #
		fillFormHistory(uin, name);
		//#sijapp cond.end#
	}
	
	// fill chat with last history lines
	//#sijapp cond.if modules_HISTORY is "true" #
	final static private int MAX_HIST_LAST_MESS = 5;
	static public void fillFormHistory(String uin, String name)
	{
		if (Options.getBooleanOption(Options.OPTION_SHOW_LAST_MESS))
		{
			int recCount = HistoryStorage.getRecordCount(uin);
			if (recCount == 0) return;
			 
			if (!chatHistoryExists(uin)) newChatForm(uin, name);
			ChatTextList chatForm = (ChatTextList) historyTable.get(uin);
			if (chatForm.textList.getSize() != 0) return;
			
			int insSize = (recCount > MAX_HIST_LAST_MESS) ? MAX_HIST_LAST_MESS : recCount;  
			for (int i = recCount-insSize; i < recCount; i++)
			{
				CachedRecord rec = HistoryStorage.getRecord(uin, i);
				chatForm.textList.addBigText
				(
					"["+rec.from+" "+rec.date+"]", 
					ChatTextList.getInOutColor(rec.type == 0),
					Font.STYLE_PLAIN,
					-1
				);
				chatForm.textList.doCRLF(-1);
				
				//#sijapp cond.if modules_SMILES is "true" #
				Emotions.addTextWithEmotions(chatForm.textList, rec.text, Font.STYLE_PLAIN, 0x808080, -1);
				//#sijapp cond.else#
				chatForm.textList.addBigText(rec.text, 0x808080, Font.STYLE_PLAIN, -1);
				//#sijapp cond.end#
				chatForm.textList.doCRLF(-1);
			}
		}
	}
	//#sijapp cond.end#
	
	static public void contactRenamed(String uin, String newName)
	{
		ChatTextList temp = (ChatTextList) historyTable.get(uin);
		if (temp == null) return;
		temp.ChatName = newName;
		UpdateCaption(uin);
	}

	static public void UpdateCaption(String uin)
	{
		calcCounter(uin);
		ChatTextList temp = (ChatTextList) historyTable.get(uin);
		// Calculate the title for the chatdisplay.
		String Title = temp.ChatName+" ("+counter+"/"+historyTable.size()+")";
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		temp.textList.setFullScreenMode(false);
		temp.getDisplayable().setTitle(Title);
		// #sijapp cond.else#
		temp.textList.setCaption(Title);
		// #sijapp cond.end#
		
	}

	static public void setColorScheme()
	{
		Enumeration AllChats = historyTable.elements();
		while (AllChats.hasMoreElements())
			JimmUI.setColorScheme(((ChatTextList)AllChats.nextElement()).textList);
	}
	
	// Sets the counter for the ChatHistory
	static public void calcCounter(String curUin)
    {
		if (curUin == null) return;
		Enumeration AllChats = historyTable.elements();
		Object chat = historyTable.get(curUin);
		counter = 1;
		while (AllChats.hasMoreElements())
		{
			if (AllChats.nextElement() == chat) break;
			counter++;
		}
    }
}
