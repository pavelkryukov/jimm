/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-08  Jimm Project

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
	private int messId;

	public MessData(boolean incoming, long time, int textOffset,
			boolean contains_url, int messId)
	{
		this.messId = messId;
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
	
	public int getMessId()
	{
		return messId;
	}

	//#sijapp cond.if target != "DEFAULT"#
	public boolean isURL()
	{
		return (rowData & 0x8000000) != 0;
	}
	//#sijapp cond.end#
}

class ChatTextList implements VirtualListCommands, CommandListener, JimmScreen
{
	// UI modes
	final public static int UI_MODE_NONE = 0;
	final public static int UI_MODE_DEL_CHAT = 1;
	
	// Chat
	TextList textList;

	private static final Command cmdMsgReply = new Command(ResourceBundle.getString("reply", ResourceBundle.FLAG_ELLIPSIS), Command.OK, 1);
	private static final Command cmdCloseChat = new Command(ResourceBundle.getString("close"), Jimm.cmdBack, 2);
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
	ContactItem contact;

	private Vector messData = new Vector();

	private int messTotalCounter = 0;

	private long lastMsgTime = 0;

	private boolean lastDirection;

	ChatTextList(String name, ContactItem contact)
	{
		textList = new TextList(null);

		textList.setMode(VirtualList.CURSOR_MODE_DISABLED);
		textList.setFontSize(Options.getBoolean(Options.OPTION_CHAT_SMALL_FONT) ? VirtualList.SMALL_FONT : VirtualList.MEDIUM_FONT);

		this.contact = contact;
		ChatName = name;

		textList.setVLCommands(this);
	}
	
	public ContactItem getContact()
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
		
//#sijapp cond.if target!="RIM" & target!="DEFAULT"#
		textList.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#
		
		textList.addCommandEx(cmdMsgReply, VirtualList.MENU_TYPE_RIGHT);
		textList.addCommandEx(cmdDelChat, VirtualList.MENU_TYPE_RIGHT);
		textList.addCommandEx(cmdCopyText, VirtualList.MENU_TYPE_RIGHT);
		
		if (contact.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP)) 
			textList.addCommandEx(cmdAddUrs, VirtualList.MENU_TYPE_RIGHT);
		
		if (JimmUI.getClipBoardText() != null) textList.addCommandEx(cmdReplWithQuota, VirtualList.MENU_TYPE_RIGHT);
		
		//#sijapp cond.if modules_HISTORY is "true" #
		if (!Options.getBoolean(Options.OPTION_HISTORY)) textList.addCommandEx(cmdAddToHistory, VirtualList.MENU_TYPE_RIGHT);
		//#sijapp cond.end#
		
		if (contact.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH))
			textList.addCommandEx(cmdReqAuth, VirtualList.MENU_TYPE_RIGHT);
		
		textList.addCommandEx(cmdContactMenu, VirtualList.MENU_TYPE_RIGHT);
		
		textList.addCommandEx(cmdCloseChat, VirtualList.MENU_TYPE_LEFT_BAR);
		
		checkTextForURL();
		checkForAuthReply();
		
		textList.setCommandListener(this);
	}
	
	public boolean isVisible()
	{
		if (textList != null) return textList.isActive();
		return false;
	}
	
	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
		/* User selected chat to delete */
		if (JimmUI.getCurScreenTag() == UI_MODE_DEL_CHAT)
		{
			if (c == JimmUI.cmdOk)
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

				ChatHistory.chatHistoryDelete(contact.getStringValue(ContactItem.CONTACTITEM_UIN), delType);
				ContactList.activateList();
				return;
			}
			else activate();
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
			JimmUI.backToLastScreen();
		}
		
		/* Delete current chat */
		else if (c == cmdDelChat)
		{
			JimmUI.showSelector("delete_chat", JimmUI.stdSelector, this, UI_MODE_DEL_CHAT, true);
		}
		
		/* Copy selected text to clipboard */
		else if (c == cmdCopyText)
		{
			ChatHistory.copyText(contact.getStringValue(ContactItem.CONTACTITEM_UIN), ChatName);
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
			JimmUI.gotoURL(textList.getCurrText(0, false));
		}
		//#sijapp cond.end#
		
		/* Add temporary user to contact list */
		else if (c == cmdAddUrs)
		{
			Search search = new Search();
			String data[] = new String[Search.LAST_INDEX];
			data[Search.UIN] = contact.getStringValue(ContactItem.CONTACTITEM_UIN);

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

			String uin = contact.getStringValue(ContactItem.CONTACTITEM_UIN);
			HistoryStorage.addText(uin, text, data.getIncoming() ? (byte) 0
					: (byte) 1, data.getIncoming() ? ChatName : ResourceBundle
					.getString("me"), data.getTime());
		}
		//#sijapp cond.end#
		
		/* Grant authorization */
		else if (c == cmdGrantAuth)
		{
			SystemNotice notice = new SystemNotice(
					SystemNotice.SYS_NOTICE_AUTHORISE,
					contact.getStringValue(ContactItem.CONTACTITEM_UIN),
					true, "");
			SysNoticeAction sysNotAct = new SysNoticeAction(notice);
			try
			{
				Icq.requestAction(sysNotAct);
			
				contact.setBooleanValue(ContactItem.CONTACTITEM_B_AUTREQUESTS, false);
				buildMenu();
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
			? JimmUI.INDEXED_COLOR_INCOMING
			: JimmUI.INDEXED_COLOR_OUTGOING;
	}

	Vector getMessData()
	{
		return messData;
	}

	public void vlCursorMoved(VirtualList sender)
	{
		checkTextForURL();
	}
	
	public void vlItemClicked(VirtualList sender) {}
	
	public void vlKeyPress(VirtualList sender, int keyCode, int type) 
	{
		if (VirtualList.isNumpadKey(keyCode) || type != VirtualList.KEY_PRESSED) return;
		
		try
		{
			switch (sender.getGameAction(keyCode))
			{
			case Canvas.LEFT:
				ChatHistory.selectNextChat(false);
				break;
			case Canvas.RIGHT:
				ChatHistory.selectNextChat(true);
				break;
			}
		} 
		catch (Exception e) { /* do nothing */ }		
	}
	
	void checkTextForURL()
	{
//#sijapp cond.if target != "DEFAULT"#
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
		if (contact.getBooleanValue(ContactItem.CONTACTITEM_B_AUTREQUESTS))
		{
			textList.addCommandEx(cmdGrantAuth, VirtualList.MENU_TYPE_RIGHT);
			textList.addCommandEx(cmdDenyAuth, VirtualList.MENU_TYPE_RIGHT);
		}
	}
	
	public void setImages(Image[] images)
	{
		textList.setCapImage(images);
	}

//#sijapp cond.if target != "DEFAULT"#
	public void BeginTyping(boolean type)
	{
		textList.repaint();
	}
//#sijapp cond.end#

	boolean inOneMinute (long time1, long time2) {

		return ( ((time1 / 60) % 60 == (time2 / 60) % 60) ? true : false );

	}

	void addTextToForm(String from, String message, String url, long time,
			boolean red, boolean offline, int messId)
	{
		int texOffset = 0;
		boolean deliveryReqOn = Options.getBoolean(Options.OPTION_DELIV_MES_INFO); 

		textList.lock();
	
		boolean shortMsg = (inOneMinute (lastMsgTime, time)) 
			&& (lastDirection == red) 
			&& !deliveryReqOn;

		int lastSize = textList.getSize();
		
		StringBuffer messHeader = new StringBuffer();
		
		if (((Options.getBoolean(Options.OPTION_SHOW_MESS_ICON)) && (!shortMsg)) || deliveryReqOn)
		{
			textList.addImage(JimmUI.eventPlainMessageImg, "", messTotalCounter);
			messHeader.append(' ');
		}
		
		if ((Options.getBoolean(Options.OPTION_SHOW_NICK)) && (!shortMsg))
		{
			messHeader.append(from);
			messHeader.append(' ');
		}
		
		if ((Options.getBoolean(Options.OPTION_SHOW_MESS_DATE)) && (!shortMsg))
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
				new MessData(red, time, texOffset, contains_url, messId));
		messTotalCounter++;
		lastMsgTime = (shortMsg) ? lastMsgTime : time;
		lastDirection = red;

		textList.setTopItem(lastSize);
		textList.unlock();
	}

	public void activate()
	{
		buildMenu();
		JimmUI.setColorScheme(textList, true, -1, false);
		textList.activate(Jimm.display);
		ChatHistory.currentChat = this;
		ChatHistory.updateCaption_Internal(this);
		contact.resetUnreadMessages();
		contact.setStatusImage();
		JimmUI.setLastScreen(this, false);
		JimmUI.removeScreen(contact); 
	}
	
	public boolean isScreenActive()
	{
		System.out.println(textList.isActive());
		return textList.isActive();
	}
	
	public void messageIsDelivered(int messId)
	{
		MessData data;
		boolean ok;
		for (int i = messData.size()-1; i >= 0; i--)
		{
			data = (MessData)messData.elementAt(i);
			if (data.getMessId() == messId)
			{
				ok = textList.replaceImages(i, JimmUI.eventPlainMessageImg, JimmUI.imgMessDeliv);
				if (ok) textList.repaint();
				break;
			}
		}
	}
}

public class ChatHistory
{
	static private ChatHistory _this;
	static private Hashtable historyTable;
	static private Vector historyUins = new Vector();

	public ChatHistory()
	{
		_this = this;
	}

	static
	{
		historyTable = new Hashtable();
	}

	/* Adds a message to the message display */
	static protected void addMessage(ContactItem contact, Message message)
	{
		MainThread.showTime();
		
		synchronized (_this)
		{
			String uin = contact.getStringValue(ContactItem.CONTACTITEM_UIN);
			if (!historyTable.containsKey(uin))
				newChatForm(contact, contact.getStringValue(ContactItem.CONTACTITEM_NAME));
			
			ChatTextList chat = (ChatTextList) historyTable.get(uin);

			boolean offline = message.getOffline();
			
			boolean visible = chat.isVisible(); 

			if (message instanceof PlainMessage)
			{
				PlainMessage plainMsg = (PlainMessage) message;

				if (!visible) contact.setBooleanValue(ContactItem.CONTACTITEM_B_PLMESSAGES, true);

				addTextToForm(uin, contact
						.getStringValue(ContactItem.CONTACTITEM_NAME),
						plainMsg.getText(), "", plainMsg.getNewDate(), true,
						offline, -1);
				
				//#sijapp cond.if modules_HISTORY is "true" #
				if (Options.getBoolean(Options.OPTION_HISTORY))
					HistoryStorage.addText
					(
						uin,
						plainMsg.getText(),
						(byte) 0,
						contact.getStringValue(ContactItem.CONTACTITEM_NAME),
						plainMsg.getNewDate()
					);
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
				if (!chat.isVisible()) 
					contact.setBooleanValue(ContactItem.CONTACTITEM_B_URLMESSAGES, true);
				
				addTextToForm(uin, contact
						.getStringValue(ContactItem.CONTACTITEM_NAME),
						urlMsg.getText(), urlMsg.getUrl(), urlMsg.getNewDate(),
						false, offline, -1);
			} 
			else if (message instanceof SystemNotice)
			{
				SystemNotice notice = (SystemNotice) message;
				if (!visible)
					contact.setBooleanValue(ContactItem.CONTACTITEM_B_SYSNOTICES, true);

				if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_YOUWEREADDED)
				{
					addTextToForm(uin, ResourceBundle.getString("sysnotice"),
							ResourceBundle.getString("youwereadded")
									+ notice.getSndrUin(), "", notice.getNewDate(),
							false, offline, -1);
				} 
				else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREQ)
				{
					contact.setBooleanValue(ContactItem.CONTACTITEM_B_AUTREQUESTS, true);
					
					addTextToForm(uin, ResourceBundle.getString("sysnotice"),
							notice.getSndrUin()
									+ ResourceBundle.getString("wantsyourauth")
									+ notice.getText(), "", notice.getNewDate(),
							false, offline, -1);
				} 
				else if (notice.getSysnotetype() == SystemNotice.SYS_NOTICE_AUTHREPLY)
				{
					if (notice.isAUTH_granted())
					{
						contact.setBooleanValue(
								ContactItem.CONTACTITEM_NO_AUTH, false);
						addTextToForm(uin, ResourceBundle.getString("sysnotice"),
								ResourceBundle.getString("grantedby")
										+ notice.getSndrUin() + ".", "", notice
										.getNewDate(), false, offline, -1);
					} else if (notice.getText() != null)
						addTextToForm(uin, ResourceBundle.getString("sysnotice"),
								ResourceBundle.getString("denyedby")
										+ notice.getSndrUin() + ". "
										+ ResourceBundle.getString("reason") + ": "
										+ notice.getText(), "", notice
										.getNewDate(), false, offline, -1);
					else
						addTextToForm(uin, ResourceBundle.getString("sysnotice"),
								ResourceBundle.getString("denyedby")
										+ notice.getSndrUin() + ". "
										+ ResourceBundle.getString("noreason"), "",
								notice.getNewDate(), false, offline, -1);
				}
				chat.buildMenu();
			}
			chat.checkTextForURL();
			chat.checkForAuthReply();
		}
	}

	static protected synchronized void addMyMessage(ContactItem contact, String message,
			long time, String ChatName, int messId)
	{
		String uin = contact.getStringValue(ContactItem.CONTACTITEM_UIN);
		if (!historyTable.containsKey(uin)) newChatForm(contact, ChatName);
		addTextToForm(uin, ResourceBundle.getString("me"), message, "", time, false, false, messId);
	}

	// Add text to message form
	static synchronized private void addTextToForm(String uin, String from,
			String message, String url, long time, boolean red, boolean offline, int messId)
	{
		ChatTextList msgDisplay = (ChatTextList) historyTable.get(uin);

		msgDisplay.addTextToForm(from, message, url, time, red, offline, messId);
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
		ContactItem cItem = ContactList.getItembyUIN(uin);
		historyTable.remove(uin);
		historyUins.removeElement(uin);

		cItem.setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, false);
		cItem.resetUnreadMessages();
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
			String key;
			while (AllChats.hasMoreElements())
			{
				key = (String) AllChats.nextElement();
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
	static private void newChatForm(ContactItem contact, String name)
	{
		ChatTextList chatForm = new ChatTextList(name, contact);
		String uin = contact.getStringValue(ContactItem.CONTACTITEM_UIN);
		historyTable.put(uin, chatForm);
		Enumeration chats = historyTable.elements();
		ChatTextList chat;
		while (chats.hasMoreElements())
		{
			chat = (ChatTextList)chats.nextElement();
			if (chatForm != chat) updateCaption_Internal(chat);
		}
		chat = null;		
		historyUins.addElement(uin);
		
		ContactList.getItembyUIN(uin).setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, true);
//#sijapp cond.if modules_HISTORY is "true" #
		fillFormHistory(contact);
//#sijapp cond.end#
	}
	
	static public void updateChatIfExists(ContactItem contact)
	{
		String uin = contact.getStringValue(ContactItem.CONTACTITEM_UIN);
		ChatTextList chat = (ChatTextList) historyTable.get(uin);
		if (chat == null) return;
		chat.contact = contact;
	}

	// fill chat with last history lines
	//#sijapp cond.if modules_HISTORY is "true" #
	final static private int MAX_HIST_LAST_MESS = 5;

	static public void fillFormHistory(ContactItem contact)
	{
		String name = contact.getStringValue(ContactItem.CONTACTITEM_NAME);
		String uin = contact.getStringValue(ContactItem.CONTACTITEM_UIN);
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
			CachedRecord rec;
			for (int i = recCount - insSize; i < recCount; i++)
			{
				rec = HistoryStorage.getRecord(uin, i);
				chatForm.textList.addBigText("[" + rec.from + " " + rec.date
						+ "]", ChatTextList.getInOutColor(rec.type == 0),
						Font.STYLE_PLAIN, -1);
				chatForm.textList.doCRLF(-1);

				//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #
				Emotions.addTextWithEmotions(chatForm.textList, rec.text,
						Font.STYLE_PLAIN, 0x808080, -1);
				//#sijapp cond.else#
				chatForm.textList.addBigText(rec.text, 0x808080, Font.STYLE_PLAIN, -1);
				//#sijapp cond.end#
				chatForm.textList.doCRLF(-1);
			}
			rec = null;
		}
	}

	//#sijapp cond.end#

	static public void contactRenamed(String uin, String newName)
	{
		ChatTextList temp = (ChatTextList) historyTable.get(uin);
		if (temp == null) return;
		temp.ChatName = newName;
		updateCaption_Internal(temp);
	}

	static protected void updateCaption_Internal(ChatTextList current)
	{
		String uin = current.contact.getStringValue(ContactItem.CONTACTITEM_UIN);
		ChatTextList temp = (ChatTextList) historyTable.get(uin);
		int size = historyTable.size();
		String title = null;
		if (size != 1)
		{
			int index = historyUins.indexOf(uin)+1;
			title = temp.ChatName + " (" + index + "/" + size + ")";
		}
		else title = temp.ChatName;
		temp.textList.setCaption(title);
	}		
	
	static public boolean selectNextChat(boolean next)
	{
		if (currentChat == null) return false;
		if (!currentChat.isVisible()) return false;
		
		if (historyTable.size() <= 1) return false;
		String curUin = currentChat.contact.getStringValue(ContactItem.CONTACTITEM_UIN);
		Enumeration keys = historyTable.keys();
		Vector uins = new Vector(); 
		while (keys.hasMoreElements()) uins.addElement(keys.nextElement());
		int curIndex = uins.indexOf(curUin);
		if (next) curIndex++;
		else curIndex--;
		if (curIndex < 0) curIndex = uins.size()-1;
		if (curIndex >= uins.size()) curIndex = 0;
		JimmUI.removeScreen(currentChat);
		ChatTextList newChat = (ChatTextList)historyTable.get(uins.elementAt(curIndex));
		newChat.activate();
		return true;
	}
	
	public static void rebuildMenu(ContactItem item)
	{
		ChatTextList chat = getChatHistoryAt(item.getStringValue(ContactItem.CONTACTITEM_UIN));
		if (chat != null) chat.buildMenu();
	}
	
	public static boolean activateIfExists(ContactItem item)
	{
		if (item == null) return false;
		ChatTextList chat = getChatHistoryAt(item.getStringValue(ContactItem.CONTACTITEM_UIN));
		if (chat != null) chat.activate();
		return (chat != null);
	}
	
	static ChatTextList currentChat;
	
	public static ChatTextList getCurrent()
	{
		return currentChat;
	}
	
	public static void messageIsDelivered(String uin, int messId)
	{
		ChatTextList msgDisplay = (ChatTextList) historyTable.get(uin);
		if (msgDisplay == null) return;
		msgDisplay.messageIsDelivered(messId);
	}
}
