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

import java.util.*;

import javax.microedition.lcdui.*;

import jimm.comm.Icq;
import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SearchAction;
import jimm.comm.SysNoticeAction;
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

class MessData
{
	private long time;

	private int rowData;

	public MessData(boolean incoming, long time, int textOffset,
			boolean contains_url)
	{
		this.time = time;
		this.rowData = (textOffset & 0xFFFFFF) | (contains_url ? 0x8000000 : 0)
				| (incoming ? 0x4000000 : 0);
	}

	public boolean getIncoming()
	{
		return (rowData & 0x4000000) != 0;
	}

	public long getTime()
	{
		return time;
	}

	public int getOffset()
	{
		return (rowData & 0xFFFFFF);
	}

	//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
	public boolean isURL()
	{
		return (rowData & 0x8000000) != 0;
	}
	//#sijapp cond.end#
}

class ChatTextList implements VirtualListCommands, CommandListener
{
	// UI modes
	final public static int UI_MODE_NONE = 0;
	final public static int UI_MODE_DEL_CHAT = 1;
	
	// Chat
	TextList textList;

	private static final Command cmdMsgReply = new Command(ResourceBundle.getString("reply", ResourceBundle.FLAG_ELLIPSIS), Command.OK, 1);
	private static final Command cmdCloseChat = new Command(ResourceBundle.getString("close"), Command.BACK, 2);
	private static final Command cmdCopyText = new Command(ResourceBundle.getString("copy_text"), Command.ITEM, 4);
	private static final Command cmdReplWithQuota = new Command(ResourceBundle.getString("quote", ResourceBundle.FLAG_ELLIPSIS), Command.ITEM, 3);
	private static final Command cmdAddUrs = new Command(ResourceBundle.getString("add_user", ResourceBundle.FLAG_ELLIPSIS), Command.ITEM, 5);
	private static final Command cmdDenyAuth = new Command(ResourceBundle.getString("deny"), Command.CANCEL, 1);
	
	/* Request authorisation from a contact */
	private static final Command cmdReqAuth = new Command(ResourceBundle.getString("requauth"), Command.ITEM, 1);	
	
	/* Grand authorisation a for authorisation asking contact */
	private static final  Command cmdGrantAuth = new Command(ResourceBundle.getString("grant"), Command.ITEM, 1);

	//#sijapp cond.if modules_HISTORY is "true" #
	private static final  Command cmdAddToHistory = new Command(ResourceBundle.getString("add_to_history"), Command.ITEM, 6);
	//#sijapp cond.end#

	// Delete Chat History
	private static final  Command cmdDelChat = new Command(ResourceBundle.getString("delete_chat", ResourceBundle.FLAG_ELLIPSIS), Command.ITEM, 8);
	
	/* Show the message menu */
	private static final Command cmdContactMenu = new Command(ResourceBundle.getString("user_menu"), Command.ITEM, 7);
	
	public String ChatName;
	ContactListContactItem contact;

	private Vector messData = new Vector();

	private int messTotalCounter = 0;
	
	private static int currentUiMode; 

	ChatTextList(String name, ContactListContactItem contact)
	{
		textList = new TextList(null);

		textList.setMode(TextList.MODE_TEXT);
		textList.setFontSize(Options.getBoolean(Options.OPTION_CHAT_SMALL_FONT) ? TextList.SMALL_FONT : TextList.MEDIUM_FONT);

		this.contact = contact;
		ChatName = name;
		JimmUI.setColorScheme(textList, true);

		textList.setVLCommands(this);
	}
	
	public ContactListContactItem getContact()
	{
		return contact;
	}
	
	public Object getUIControl()
	{
		return textList;
	}
	
	void buildMenu()
	{
		textList.removeAllCommands();
		textList.addCommandEx(cmdMsgReply, VirtualList.MENU_TYPE_LEFT_BAR);
		textList.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
		textList.addCommandEx(cmdCloseChat, VirtualList.MENU_TYPE_RIGHT);
		textList.addCommandEx(cmdDelChat, VirtualList.MENU_TYPE_RIGHT);
		textList.addCommandEx(cmdCopyText, VirtualList.MENU_TYPE_RIGHT);
		
		if (contact.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP)) 
			textList.addCommandEx(cmdAddUrs, VirtualList.MENU_TYPE_RIGHT);
		
		if (JimmUI.getClipBoardText() != null) textList.addCommandEx(cmdReplWithQuota, VirtualList.MENU_TYPE_RIGHT);
		
		//#sijapp cond.if modules_HISTORY is "true" #
		if (!Options.getBoolean(Options.OPTION_HISTORY)) textList.addCommandEx(cmdAddToHistory, VirtualList.MENU_TYPE_RIGHT);
		//#sijapp cond.end#
		
		if (contact.getBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH))
			textList.addCommandEx(cmdReqAuth, VirtualList.MENU_TYPE_RIGHT);
		
		textList.addCommandEx(cmdContactMenu, VirtualList.MENU_TYPE_RIGHT);
		
		checkTextForURL();
		checkForAuthReply();
		
		textList.setCommandListener(this);
	}
	
	public boolean isVisible()
	{
		if (textList != null) return textList.isActive();
		return false;
	}
	
	private Object getVisibleObject()
	{
		return textList;
	}
	
	public void commandAction(Command c, Displayable d)
	{
		/* User selected chat to delete */
		if ((currentUiMode == UI_MODE_DEL_CHAT) && (c == JimmUI.cmdOk))
		{
			int delType = -1;

			switch (JimmUI.getLastSelIndex())
			{
			case 0:
				delType = ChatHistory.DEL_TYPE_CURRENT;
				break;
			case 1:
				delType = ChatHistory.DEL_TYPE_ALL_EXCEPT_CUR;
				break;
			case 2:
				delType = ChatHistory.DEL_TYPE_ALL;
				break;
			}

			ChatHistory.chatHistoryDelete(contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN), delType);
			ContactList.activate();
			return;
		}
		
		/* Write new message */
		else if (c == cmdMsgReply)
		{
			JimmUI.writeMessage(contact, null);
		}
		
		/* Close current chat */
		else if (c == cmdCloseChat)
		{
			contact.resetUnreadMessages();
			ContactList.activate();
		}
		
		/* Delete current chat */
		else if (c == cmdDelChat)
		{
			currentUiMode = UI_MODE_DEL_CHAT;
			JimmUI.showSelector("delete_chat", JimmUI.stdSelector, this, UI_MODE_DEL_CHAT, true);
		}
		
		/* Copy selected text to clipboard */
		else if (c == cmdCopyText)
		{
			ChatHistory.copyText(contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN), ChatName);
			textList.addCommandEx(cmdReplWithQuota, VirtualList.MENU_TYPE_RIGHT);
		}
		
		/* Reply with quotation */
		else if (c == cmdReplWithQuota)
		{
			JimmUI.writeMessage(contact, JimmUI.getClipBoardText());
		}
		
		/* Open URL in web brouser */
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else if (c == JimmUI.cmdGotoURL)
		{
			JimmUI.gotoURL(textList.getCurrText(0, false), getVisibleObject());
		}
		//#sijapp cond.end#
		
		/* Add temporary user to contact list */
		else if (c == cmdAddUrs)
		{
			Search search = new Search(true);
			String data[] = new String[Search.LAST_INDEX];
			data[Search.UIN] = contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN);

			SearchAction act = new SearchAction(search, data, SearchAction.CALLED_BY_ADDUSER);

			try
			{
				Icq.requestAction(act);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
			}

			SplashCanvas.addTimerTask("wait", act, false);
		}
		
		/* Add selected text to history */
		//#sijapp cond.if modules_HISTORY is "true" #
		else if (c == cmdAddToHistory)
		{
			int textIndex = textList.getCurrTextIndex();

			MessData data = (MessData) getMessData().elementAt(textIndex);

			String text = textList.getCurrText(data.getOffset(), false);
			if (text == null)
				return;

			String uin = contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
			HistoryStorage.addText(uin, text, data.getIncoming() ? (byte) 0
					: (byte) 1, data.getIncoming() ? ChatName : ResourceBundle
					.getString("me"), data.getTime());
		}
		//#sijapp cond.end#
		
		/* Grant authorization */
		else if (c == cmdGrantAuth)
		{
			contact.setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS, 0);
			SystemNotice notice = new SystemNotice(
					SystemNotice.SYS_NOTICE_AUTHORISE,
					contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN),
					true, "");
			SysNoticeAction sysNotAct = new SysNoticeAction(notice);
			try
			{
				Icq.requestAction(sysNotAct);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}
		}
		
		/* Deny authorization */
		else if (c == cmdDenyAuth)
		{
			JimmUI.authMessage(JimmUI.AUTH_TYPE_DENY, contact, "reason", null);
		}
		
		/* Request autorization */
		else if (c == cmdReqAuth)
		{
			JimmUI.authMessage(JimmUI.AUTH_TYPE_REQ_AUTH, contact, "requauth", "plsauthme");
		}
		
		/* Show contact menu */
		else if (c == cmdContactMenu)
		{
			JimmUI.showContactMenu(contact);
		}
	}
	

	static int getInOutColor(boolean incoming)
	{
		return incoming 
			? Options.getSchemeColor(Options.CLRSCHHEME_INCOMING)
			: Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING);
	}

	Vector getMessData()
	{
		return messData;
	}

	public void vlCursorMoved(VirtualList sender)
	{
		checkTextForURL();
	}
	
	void checkTextForURL()
	{
		//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
		textList.removeCommandEx(JimmUI.cmdGotoURL);
		int messIndex = textList.getCurrTextIndex();
		if (messIndex != -1)
		{
			MessData md = (MessData) getMessData().elementAt(messIndex);
			if (md.isURL()) textList.addCommandEx(JimmUI.cmdGotoURL, VirtualList.MENU_TYPE_RIGHT);
		}
		//#sijapp cond.end#
	}
	
	void checkForAuthReply()
	{
		if (contact.isMessageAvailable(ContactListContactItem.MESSAGE_AUTH_REQUEST))
		{
			textList.addCommandEx(cmdGrantAuth, VirtualList.MENU_TYPE_RIGHT);
			textList.addCommandEx(cmdDenyAuth, VirtualList.MENU_TYPE_RIGHT);
		}
	}
	
	public void setImage(Image img)
	{
		textList.setCapImage(img);
	}

	public void vlItemClicked(VirtualList sender)
	{
	}

	public void vlKeyPress(VirtualList sender, int keyCode, int type)
	{
		try
		// getGameAction can raise exception
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

			JimmUI.execHotKey(contact, keyCode, type);
		} catch (Exception e)
		{
			// do nothing
		}
	}

	//#sijapp cond.if target isnot "DEFAULT"#
	public void BeginTyping(boolean type)
	{
		textList.repaint();
	}

	//#sijapp cond.end#

	void addTextToForm(String from, String message, String url, long time,
			boolean red, boolean offline)
	{
		int texOffset = 0;

		textList.lock();
	
		int lastSize = textList.getSize();
		
		StringBuffer messHeader = new StringBuffer();
		
		if (Options.getBoolean(Options.OPTION_SHOW_MESS_ICON))
		{
			textList.addImage(ContactList.imageList.elementAt(8), "", messTotalCounter);
			messHeader.append(' ');
		}
		
		if (Options.getBoolean(Options.OPTION_SHOW_NICK))
		{
			messHeader.append(from);
			messHeader.append(' ');
		}
		
		if (Options.getBoolean(Options.OPTION_SHOW_MESS_DATE))
		{
			messHeader.append('(');
			messHeader.append(Util.getDateString(!offline, time));
			messHeader.append(')');
		}
		
		if (messHeader.length() != 0) messHeader.append(": ");
		
		if (messHeader.length() != 0)
		{
			textList.addBigText(messHeader.toString(), getInOutColor(red), Font.STYLE_BOLD, messTotalCounter);
			if (offline || Options.getBoolean(Options.OPTION_SHOW_MESS_CLRF)) textList.doCRLF(messTotalCounter);
			String restoredHeadText = textList.getTextByIndex(0, false, messTotalCounter);
			texOffset = restoredHeadText.length();
		}
		else texOffset = 0;

		if (url.length() > 0)
		{
			textList.addBigText(ResourceBundle.getString("url") + ": " + url,
					0x00FF00, Font.STYLE_PLAIN, messTotalCounter);
		}

		int textColor = Options.getBoolean(Options.OPTION_MESS_COLORED_TEXT) ? getInOutColor(red) : textList.getTextColor(); 
		JimmUI.addMessageText(textList, message, textColor, messTotalCounter);

		boolean contains_url = false;
		//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
		if (Util.parseMessageForURL(message) != null)
		{
			contains_url = true;
			if (texOffset == 1)
				textList.addCommandEx(JimmUI.cmdGotoURL, VirtualList.MENU_TYPE_RIGHT);
		}
		//#sijapp cond.end#
		getMessData().addElement(
				new MessData(red, time, texOffset, contains_url));
		messTotalCounter++;

		textList.setTopItem(lastSize);
		textList.unlock();
	}

	public void activate(boolean initChat, boolean resetText)
	{
		currentUiMode = UI_MODE_NONE;
		textList.activate(Jimm.display);
		JimmUI.setLastScreen(textList);
		ChatHistory.currentChat = this;
	}
}

public class ChatHistory
{
	static private ChatHistory _this;
	static private Hashtable historyTable;

	static private int counter;

	public ChatHistory()
	{
		_this = this;
	}

	static
	{
		historyTable = new Hashtable();
		counter = 1;
	}

	/* Adds a message to the message display */
	static protected void addMessage(ContactListContactItem contact, Message message)
	{
		synchronized (_this)
		{
			String uin = contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
			if (!historyTable.containsKey(uin))
				newChatForm(contact, contact.getStringValue(ContactListContactItem.CONTACTITEM_NAME));
			
			ChatTextList chat = (ChatTextList) historyTable.get(uin);

			boolean offline = message.getOffline();
			
			boolean visible = chat.isVisible(); 

			if (message instanceof PlainMessage)
			{
				PlainMessage plainMsg = (PlainMessage) message;

				if (!visible) contact.increaseMessageCount(ContactListContactItem.MESSAGE_PLAIN);

				addTextToForm(uin, contact
						.getStringValue(ContactListContactItem.CONTACTITEM_NAME),
						plainMsg.getText(), "", plainMsg.getNewDate(), true,
						offline);
				
				//#sijapp cond.if modules_HISTORY is "true" #
				if (Options.getBoolean(Options.OPTION_HISTORY))
					HistoryStorage
							.addText(
									uin,
									plainMsg.getText(),
									(byte) 0,
									contact
											.getStringValue(ContactListContactItem.CONTACTITEM_NAME),
									plainMsg.getNewDate());
				//#sijapp cond.end#
				
				if (!offline)
				{
					/* Show creeping line */
					JimmUI.showCreepingLine(JimmUI.getCurrentScreen(), plainMsg.getText(), contact);
				}
			} 
			else if (message instanceof UrlMessage)
			{
				UrlMessage urlMsg = (UrlMessage) message;
				if (!chat.isVisible()) contact .increaseMessageCount(ContactListContactItem.MESSAGE_URL);
				addTextToForm(uin, contact
						.getStringValue(ContactListContactItem.CONTACTITEM_NAME),
						urlMsg.getText(), urlMsg.getUrl(), urlMsg.getNewDate(),
						false, offline);
			} else if (message instanceof SystemNotice)
			{
				SystemNotice notice = (SystemNotice) message;
				if (!visible)
					contact
							.increaseMessageCount(ContactListContactItem.MESSAGE_SYS_NOTICE);

				if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_YOUWEREADDED)
				{
					addTextToForm(uin, ResourceBundle.getString("sysnotice"),
							ResourceBundle.getString("youwereadded")
									+ notice.getSndrUin(), "", notice.getNewDate(),
							false, offline);
				} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ)
				{
					contact
							.increaseMessageCount(ContactListContactItem.MESSAGE_AUTH_REQUEST);
					addTextToForm(uin, ResourceBundle.getString("sysnotice"),
							notice.getSndrUin()
									+ ResourceBundle.getString("wantsyourauth")
									+ notice.getReason(), "", notice.getNewDate(),
							false, offline);
				} else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY)
				{
					if (notice.isAUTH_granted())
					{
						contact.setBooleanValue(
								ContactListContactItem.CONTACTITEM_NO_AUTH, false);
						addTextToForm(uin, ResourceBundle.getString("sysnotice"),
								ResourceBundle.getString("grantedby")
										+ notice.getSndrUin() + ".", "", notice
										.getNewDate(), false, offline);
					} else if (notice.getReason() != null)
						addTextToForm(uin, ResourceBundle.getString("sysnotice"),
								ResourceBundle.getString("denyedby")
										+ notice.getSndrUin() + ". "
										+ ResourceBundle.getString("reason") + ": "
										+ notice.getReason(), "", notice
										.getNewDate(), false, offline);
					else
						addTextToForm(uin, ResourceBundle.getString("sysnotice"),
								ResourceBundle.getString("denyedby")
										+ notice.getSndrUin() + ". "
										+ ResourceBundle.getString("noreason"), "",
								notice.getNewDate(), false, offline);
				}
			}
			chat.checkTextForURL();
			chat.checkForAuthReply();
		}
	}

	static protected synchronized void addMyMessage(ContactListContactItem contact, String message,
			long time, String ChatName)
	{
		String uin = contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
		if (!historyTable.containsKey(uin)) newChatForm(contact, ChatName);
		addTextToForm(uin, ResourceBundle.getString("me"), message, "", time, false, false);
	}

	// Add text to message form
	static synchronized private void addTextToForm(String uin, String from,
			String message, String url, long time, boolean red, boolean offline)
	{
		ChatTextList msgDisplay = (ChatTextList) historyTable.get(uin);

		msgDisplay.addTextToForm(from, message, url, time, red, offline);
	}

	static private MessData getCurrentMessData(String uin)
	{
		ChatTextList list = getChatHistoryAt(uin);
		int messIndex = list.textList.getCurrTextIndex();
		if (messIndex == -1)
			return null;
		MessData md = (MessData) list.getMessData().elementAt(messIndex);
		return md;
	}

	static public String getCurrentMessage(String uin)
	{
		return getChatHistoryAt(uin).textList.getCurrText(getCurrentMessData(uin).getOffset(), false);
	}

	static public void copyText(String uin, String from)
	{
		ChatTextList list = getChatHistoryAt(uin);
		int messIndex = list.textList.getCurrTextIndex();
		if (messIndex == -1) return;
		MessData md = (MessData) list.getMessData().elementAt(messIndex);

		JimmUI.setClipBoardText(md.getIncoming(), Util.getDateString(false, md
				.getTime()), md.getIncoming() ? from : ResourceBundle
				.getString("me"), getCurrentMessage(uin));
	}

	// Returns the chat history form at the given uin
	static public ChatTextList getChatHistoryAt(String uin)
	{
		if (historyTable.containsKey(uin))
			return (ChatTextList) historyTable.get(uin);
		else
			return null;
	}

	final static public int DEL_TYPE_CURRENT = 1;

	final static public int DEL_TYPE_ALL_EXCEPT_CUR = 2;

	final static public int DEL_TYPE_ALL = 3;

	static public void chatHistoryDelete(String uin)
	{
		ContactListContactItem cItem = ContactList.getItembyUIN(uin);
		historyTable.remove(uin);

		cItem.setBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT,
				false);
		cItem.setIntValue(ContactListContactItem.CONTACTITEM_PLAINMESSAGES, 0);
		cItem.setIntValue(ContactListContactItem.CONTACTITEM_URLMESSAGES, 0);
		cItem.setIntValue(ContactListContactItem.CONTACTITEM_SYSNOTICES, 0);
	}

	// Delete the chat history for uin
	static public void chatHistoryDelete(String uin, int delType)
	{
		switch (delType)
		{
		case DEL_TYPE_CURRENT:
			chatHistoryDelete(uin);
			break;

		case DEL_TYPE_ALL_EXCEPT_CUR:
		case DEL_TYPE_ALL:
			Enumeration AllChats = historyTable.keys();
			while (AllChats.hasMoreElements())
			{
				String key = (String) AllChats.nextElement();
				if ((delType == DEL_TYPE_ALL_EXCEPT_CUR) && (key.equals(uin)))
					continue;
				chatHistoryDelete(key);
			}
			break;
		}
	}

	// Returns if the chat history at the given number is shown
	static public boolean chatHistoryShown(String uin)
	{
		if (historyTable.containsKey(uin))
		{
			ChatTextList temp = (ChatTextList) historyTable.get(uin);
			return temp.isVisible();
		} else
			return false;
	}

	// Returns true if chat history exists for this uin
	static public boolean chatHistoryExists(String uin)
	{
		return historyTable.containsKey(uin);
	}

	// Creates a new chat form
	static private void newChatForm(ContactListContactItem contact, String name)
	{
		ChatTextList chatForm = new ChatTextList(name, contact);
		String uin = contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
		historyTable.put(uin, chatForm);
		UpdateCaption(uin);
		ContactList.getItembyUIN(uin).setBooleanValue(
				ContactListContactItem.CONTACTITEM_HAS_CHAT, true); ///
		//#sijapp cond.if modules_HISTORY is "true" #
		fillFormHistory(contact);
		//#sijapp cond.end#
	}

	// fill chat with last history lines
	//#sijapp cond.if modules_HISTORY is "true" #
	final static private int MAX_HIST_LAST_MESS = 5;

	static public void fillFormHistory(ContactListContactItem contact)
	{
		String name = contact.getStringValue(ContactListContactItem.CONTACTITEM_NAME);
		String uin = contact.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
		if (Options.getBoolean(Options.OPTION_SHOW_LAST_MESS))
		{
			int recCount = HistoryStorage.getRecordCount(uin);
			if (recCount == 0)
				return;

			if (!chatHistoryExists(uin)) newChatForm(contact, name);
			ChatTextList chatForm = (ChatTextList) historyTable.get(uin);
			if (chatForm.textList.getSize() != 0)
				return;

			int insSize = (recCount > MAX_HIST_LAST_MESS) ? MAX_HIST_LAST_MESS
					: recCount;
			for (int i = recCount - insSize; i < recCount; i++)
			{
				CachedRecord rec = HistoryStorage.getRecord(uin, i);
				chatForm.textList.addBigText("[" + rec.from + " " + rec.date
						+ "]", ChatTextList.getInOutColor(rec.type == 0),
						Font.STYLE_PLAIN, -1);
				chatForm.textList.doCRLF(-1);

				//#sijapp cond.if modules_SMILES is "true" #
				Emotions.addTextWithEmotions(chatForm.textList, rec.text,
						Font.STYLE_PLAIN, 0x808080, -1);
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
		if (temp == null)
			return;
		temp.ChatName = newName;
		UpdateCaption(uin);
	}

	static public void UpdateCaption(String uin)
	{
		calcCounter(uin);
		ChatTextList temp = (ChatTextList) historyTable.get(uin);
		// Calculate the title for the chatdisplay.
		String Title = temp.ChatName + " (" + counter + "/"
				+ historyTable.size() + ")";
		temp.textList.setCaption(Title);
	}

	static public void setColorScheme()
	{
		Enumeration AllChats = historyTable.elements();
		while (AllChats.hasMoreElements())
			JimmUI
					.setColorScheme(((ChatTextList) AllChats.nextElement()).textList, false);
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
	
	public static boolean activateIfExists(ContactListContactItem item)
	{
		if (item == null) return false;
		
		ChatTextList chat = getChatHistoryAt(item.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
		if (chat != null)
		{
			chat.buildMenu();
			chat.activate(false, false);
		}
		return (chat != null);
	}
	
	static ChatTextList currentChat;
	
	public static ChatTextList getCurrent()
	{
		return currentChat;
	}
}
