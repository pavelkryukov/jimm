/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-06  Jimm Project

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
 File: src/jimm/JimmUI.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Igor Palkin, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import jimm.comm.*;
import jimm.util.ResourceBundle;
import DrawControls.TextList;
import DrawControls.VirtualList;

public class JimmUI implements CommandListener
{
	// Last screen constants
	static private Object lastScreen;
	
	public static void setLastScreen(Object screen)
	{
		lastScreen = screen;
	}
	
	public static void backToLastScreen()
	{
		selectScreen(lastScreen);
	}
	
	public static void selectScreen(Object screen)
	{
		if (screen instanceof VirtualList)
		{
			VirtualList vl = (VirtualList)screen;
			vl.activate(Jimm.display);
		}
		else if (screen instanceof Displayable)
		{
			Jimm.display.setCurrent((Displayable)screen);
		}
		else
		{
			MainMenu.activate();
		}
	}
	
	// Commands codes
	final public static int CMD_OK = 1;
	final public static int CMD_CANCEL = 2;
	final public static int CMD_YES = 3;
	final public static int CMD_NO = 4;
	final public static int CMD_FIND = 5;
	final public static int CMD_BACK = 6;

	// Commands
	public final static Command cmdOk = new Command(ResourceBundle
			.getString("ok"), Command.OK, 1);

	public final static Command cmdCancel = new Command(ResourceBundle
			.getString("cancel"), Command.BACK, 1);

	public final static Command cmdYes = new Command(ResourceBundle
			.getString("yes"), Command.OK, 1);

	public final static Command cmdNo = new Command(ResourceBundle
			.getString("no"), Command.CANCEL, 2);

	public final static Command cmdFind = new Command(ResourceBundle
			.getString("find"), Command.OK, 1);

	public final static Command cmdBack = new Command(ResourceBundle
			.getString("back"), Command.BACK, 1);

	public final static Command cmdCopyText = new Command(ResourceBundle
			.getString("copy_text"), Command.ITEM, 3);

	public final static Command cmdCopyAll = new Command(ResourceBundle
			.getString("copy_all_text"), Command.ITEM, 4);

	public final static Command cmdEdit = new Command(ResourceBundle
			.getString("edit"), Command.ITEM, 1);
	
	public final static Command cmdMenu = new Command(ResourceBundle
			.getString("menu"), Command.ITEM, 1);
	
	public final static Command cmdSelect = new Command(ResourceBundle
			.getString("select"), Command.OK, 2);
	
	public final static Command cmdSend = new Command(ResourceBundle
			.getString("send"), Command.OK, 1);
	
	//#sijapp cond.if modules_SMILES is "true" #
	public final static Command cmdInsertEmo = new Command(ResourceBundle
			.getString("insert_emotion"), Command.ITEM, 3);
	//#sijapp cond.end#
	
	public final static Command cmdInsTemplate = new Command(ResourceBundle
			.getString("templates"), Command.ITEM, 4);	
	
	//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
	public final static Command cmdGotoURL = new Command(ResourceBundle.getString("goto_url"), Command.ITEM, 9);
	//#sijapp cond.end#
	
	private final static Command cmdClearText = new Command(ResourceBundle
			.getString("clear"), Command.ITEM, 5);

	static private CommandListener listener;
	static private Hashtable commands = new Hashtable();
	static private JimmUI _this;

	// Associate commands and commands codes 
	static
	{
		commands.put(cmdOk, new Integer(CMD_OK));
		commands.put(List.SELECT_COMMAND, new Integer(CMD_OK));
		commands.put(cmdCancel, new Integer(CMD_CANCEL));
		commands.put(cmdYes, new Integer(CMD_YES));
		commands.put(cmdNo, new Integer(CMD_NO));
		commands.put(cmdFind, new Integer(CMD_FIND));
		commands.put(cmdBack, new Integer(CMD_BACK));
	}

	JimmUI()
	{
		_this = this;
	}

	// Returns commands index of command
	public static int getCommandIdx(Command command)
	{
		Object result = commands.get(command);
		return (result == null) ? -1 : ((Integer) result).intValue();
	}

	// Place "object = null;" code here:
	private static void clearAll()
	{
		msgForm = null;
		aboutTextList = null;
		System.gc();
	}

	public void commandAction(Command c, Displayable d)
	{
		if (isControlActive(removeContactMessageBox))
		{
			if (c == cmdOk) menuRemoveContactSelected();
			else backToLastScreen();
			removeContactMessageBox = null;
		}
		
		else if ((renameTextbox != null) && (d == renameTextbox))
		{
			if (c == cmdOk) menuRenameSelected();
			else backToLastScreen();
			renameTextbox = null;
		}
		
		else if (isControlActive(removeMeMessageBox))
		{
			if (c == cmdOk) menuRemoveMeSelected();
			else backToLastScreen();
			removeMeMessageBox = null;
		}
		
		else if (isControlActive(tlContactMenu))
		{
			if (c == cmdSelect) contactMenuSelected(tlContactMenu.getCurrTextIndex());
			else backToLastScreen();
			tlContactMenu = null;
			clciContactMenu = null;
		}
		
		else if ((authTextbox != null) && (d == authTextbox))
		{
			if (c == cmdSend)
			{
				SystemNotice notice = null;

				/* If or if not a reason was entered
				 Though this box is used twice (reason for auth request and auth repley)
				 we have to distinguish what we wanna do requReason is used for that */
				String textBoxText = authTextbox.getString();
				String reasonText = (textBoxText == null || textBoxText.length() < 1) ? "" : textBoxText;
				
				switch (authType)
				{
				case AUTH_TYPE_DENY:
					notice = new SystemNotice(
							SystemNotice.SYS_NOTICE_AUTHORISE,
							authContactItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN),
							false, reasonText);
					break;
				case AUTH_TYPE_REQ_AUTH:
					notice = new SystemNotice(
							SystemNotice.SYS_NOTICE_REQUAUTH,
							authContactItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN),
							false, reasonText);
					break;
				}
				
				/* Assemble the sysNotAction and request it */
				SysNoticeAction sysNotAct = new SysNoticeAction(notice);
				UpdateContactListAction updateAct = new UpdateContactListAction(
						authContactItem, UpdateContactListAction.ACTION_REQ_AUTH);

				try
				{
					Icq.requestAction(sysNotAct);
					if (authContactItem.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP))
						Icq.requestAction(updateAct);
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical())
						return;
				}
				
				authContactItem.setIntValue(ContactListContactItem.CONTACTITEM_AUTREQUESTS, 0);
				
			}

			boolean activated = ChatHistory.activateIfExists(authContactItem);
			if (!activated) ContactList.activate();

			authTextbox = null;
			authContactItem = null;
			
			return;
		}
		
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		if (JimmUI.isControlActive(URLList))
		{
			if (c == cmdSelect)
			{
				try
				{
					Jimm.jimm.platformRequest(URLList.getCurrText(0, false));
				} catch (Exception e) {}
			}
			selectScreen(lastScreenBeforeUrlSelect);
			URLList = null;
			lastScreenBeforeUrlSelect = null;
			return;
		}
		//#sijapp cond.end#
		
		else if ((messageTextbox != null) && (d == messageTextbox))
		{
			if (c == cmdCancel) backToLastScreen();
			else if (c == cmdSend)
			{
				switch (textMessCurMode)
				{
				case EDITOR_MODE_MESSAGE:
					String messText = messageTextbox.getString();

					if (messText.length() != 0)
					{
						sendMessage(messText, textMessReceiver);
						messageTextbox.setString(null);
						boolean activated = ChatHistory.activateIfExists(textMessReceiver);
						if (!activated) backToLastScreen();
					}
					break;
				}
			}
			
			//#sijapp cond.if modules_SMILES is "true" #
			else if (c == cmdInsertEmo)
			{
				Emotions.selectEmotion(messageTextbox, messageTextbox);
			}
			//#sijapp cond.end#
			
			else if (c == cmdInsTemplate)
			{
				Templates.selectTemplate(messageTextbox, messageTextbox);
			}
			
			else if (c == cmdClearText)
			{
				messageTextbox.setString(new String());
			}
		}
		
		// "About" -> "Back"
		else if (JimmUI.isControlActive(aboutTextList) && (c == cmdBack))
		{
			synchronized (_this)
			{
				MainMenu.activate();
				aboutTextList = null;
			}
		}

		// "User info"
		else if (JimmUI.isControlActive(infoTextList))
		{
			// "User info" -> "Cancel, Back"
			if ((c == cmdCancel) || (c == cmdBack))
			{
				backToLastScreen();
			}

			if (c == cmdEdit)
			{
				EditInfo.showEditForm(last_user_info, Jimm.display.getCurrent());
			}

			// "User info" -> "Copy text, Copy all"
			else if ((c == cmdCopyText) || (c == cmdCopyAll))
			{
				JimmUI.setClipBoardText("[" + getCaption(infoTextList) + "]\n"
						+ infoTextList.getCurrText(0, (c == cmdCopyAll)));
			}
		}

		// "Selector"
		else if ((lstSelector != null) && (d == lstSelector))
		{
			lastSelectedItemIndex = lstSelector.getSelectedIndex();

			// "Selector" -> "Cancel"
			if (c == cmdCancel)
			{
				backToLastScreen();
			}

			// "Selector" -> "Ok"
			else if ((c == cmdOk) || (c == List.SELECT_COMMAND))
				listener.commandAction(cmdOk, d);

			lstSelector = null;
			listener = null;

			actionTag = -1;
		}

		// Message box
		else if ((msgForm != null) && (d == msgForm))
		{
			listener.commandAction(c, d);
			msgForm = null;
			actionTag = -1;
		}
	}

	public static void setCaption(Object ctrl, String caption)
	{
		if (ctrl instanceof VirtualList)
		{
			VirtualList vl = (VirtualList) ctrl;
			vl.setCaption(caption);
		}
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else if (ctrl instanceof Displayable)
		{
			((Displayable)ctrl).setTitle(caption);
		}
		//#sijapp cond.end#
	}

	public static String getCaption(Object ctrl)
	{
		if (ctrl == null)
			return null;
		String result = null;
		if (ctrl instanceof VirtualList)
		{
			VirtualList vl = (VirtualList) ctrl;
			result = vl.getCaption();
		}
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		else if (ctrl instanceof Displayable)
		{
			result = ((Displayable)ctrl).getTitle();
		}
		//#sijapp cond.end#

		return result;
	}

	/////////////////////////
	//                     // 
	//     Message Box     //
	//                     //
	/////////////////////////
	static private Form msgForm;

	static private int actionTag = -1;

	public static int getCommandType(Command testCommand, int testTag)
	{
		return (actionTag == testTag) ? getCommandIdx(testCommand) : -1;
	}

	final public static int MESBOX_YESNO = 1;

	final public static int MESBOX_OKCANCEL = 2;

	static public void messageBox(String cap, String text, int type,
			CommandListener listener, int tag)
	{
		clearAll();

		actionTag = tag;
		msgForm = new Form(cap);
		msgForm.append(text);

		switch (type)
		{
		case MESBOX_YESNO:
			msgForm.addCommand(cmdYes);
			msgForm.addCommand(cmdNo);
			break;

		case MESBOX_OKCANCEL:
			msgForm.addCommand(cmdOk);
			msgForm.addCommand(cmdCancel);
			break;
		}

		JimmUI.listener = listener;
		msgForm.setCommandListener(_this);
		Jimm.display.setCurrent(msgForm);
	}
	
	static private TextList showMessageBox(String cap, String text, int type)
	{
		TextList list = new TextList(cap);
		list.setCursorMode(TextList.SEL_NONE);
		setColorScheme(list, false);
		list.setFontSize(Font.SIZE_LARGE);
		list.addBigText(text, list.getTextColor(), Font.STYLE_PLAIN, -1);
		
		switch (type)
		{
		case MESBOX_YESNO:
			list.addCommandEx(cmdYes, TextList.MENU_TYPE_LEFT_BAR);
			list.addCommandEx(cmdNo, TextList.MENU_TYPE_RIGHT_BAR);
			break;

		case MESBOX_OKCANCEL:
			list.addCommandEx(cmdOk, TextList.MENU_TYPE_LEFT_BAR);
			list.addCommandEx(cmdCancel, TextList.MENU_TYPE_RIGHT_BAR);
			break;
		}
		
		list.setCommandListener(_this);
		list.activate(Jimm.display);
		return list; 
	}

	//////////////////////////////////////////////////////////////////////////////
	private static TextList aboutTextList;

	// String for recent version
	static private String version;

	static public void about(Displayable lastDisplayable_)
	{
		System.gc();
		long freeMem = Runtime.getRuntime().freeMemory() / 1024;
		
		if (aboutTextList == null)
			aboutTextList = new TextList(null);

		aboutTextList.lock();
		aboutTextList.clear();
		aboutTextList.setCursorMode(TextList.SEL_NONE);
		setColorScheme(aboutTextList, false);
		aboutTextList.setColors(0xffffff, 0x006fb1, 0x006fb1, 0x006fb1,
				0xffffff);

		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
		aboutTextList.setFontSize(Font.SIZE_MEDIUM);
		//#sijapp cond.else#
		//#		aboutTextList.setFontSize(Font.SIZE_SMALL);
		//#sijapp cond.end#

		aboutTextList.setCaption(ResourceBundle.getString("about"));

		String commaAndSpace = ", ";

		StringBuffer str = new StringBuffer();
		str.append(" ").append(ResourceBundle.getString("about_info")).append(
				"\n\n")

		.append(ResourceBundle.getString("midp_info")).append(": ").append(
				Jimm.microeditionPlatform);

		if (Jimm.microeditionProfiles != null)
			str.append(commaAndSpace).append(Jimm.microeditionProfiles);

		String locale = System.getProperty("microedition.locale");
		if (locale != null)
			str.append(commaAndSpace).append(locale);

		str.append("\n\n").append(ResourceBundle.getString("free_heap"))
				.append(": ").append(freeMem).append("kb\n").append(
						ResourceBundle.getString("total_mem")).append(": ")
				.append(Runtime.getRuntime().totalMemory() / 1024).append(
						"kb\n\n")
				.append(ResourceBundle.getString("latest_ver")).append(':');

		if (version != null)
			str.append(' ').append(version);
		else
			str.append(" ...");

		try
		{
			Image image = SplashCanvas.getSplashImage();
			aboutTextList.addBigText("\n", 0xffffff, Font.STYLE_PLAIN, -1)
					.addImage(image, null, -1).doCRLF(-1).addBigText(str.toString(), 0xffffff,
							Font.STYLE_PLAIN, -1);

			aboutTextList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
			aboutTextList.setCommandListener(_this);

			// Set the color sceme (background would not fit otherwise)
			aboutTextList.activate(Jimm.display);
		} catch (Exception e)
		{
		}

		aboutTextList.unlock();

		if (version == null)
			Jimm.getTimerRef().schedule(new GetVersionInfoTimerTask(), 2000);
	}

	//////////////////////
	//                  //
	//    Clipboard     //
	//                  //
	//////////////////////

	static private String clipBoardText;

	static private String insertQuotingChars(String text, String qChars)
	{
		StringBuffer result = new StringBuffer();
		int size = text.length();
		boolean wasNewLine = true;
		for (int i = 0; i < size; i++)
		{
			char chr = text.charAt(i);
			if (wasNewLine)
				result.append(qChars);
			result.append(chr);
			wasNewLine = (chr == '\n');
		}

		return result.toString();
	}

	static public String getClipBoardText()
	{
		return clipBoardText;
	}

	static public void setClipBoardText(String text)
	{
		clipBoardText = text;
	}

	static public void setClipBoardText(boolean incoming, String date,
			String from, String text)
	{
		StringBuffer sb = new StringBuffer();
		sb.append('[').append(from).append(' ').append(date).append(']')
				.append(CRLFstr).append(
						insertQuotingChars(text, incoming ? ">> " : "<< "));
		clipBoardText = sb.toString();
	}

	static public void clearClipBoardText()
	{
		clipBoardText = null;
	}

	final public static String CRLFstr = "\n";

	////////////////////////
	//                    //
	//    Color scheme    //
	//                    //
	////////////////////////

	static public void setColorScheme(VirtualList vl, boolean setFullScreen)
	{
		if (vl == null) return;

		vl.setColors
		(
			Options.getSchemeColor(Options.CLRSCHHEME_TEXT), 
			Options.getSchemeColor(Options.CLRSCHHEME_CAP), 
			Options.getSchemeColor(Options.CLRSCHHEME_BACK), 
			Options.getSchemeColor(Options.CLRSCHHEME_CURS), 
			Options.getSchemeColor(Options.CLRSCHHEME_TEXT)
		);
		
		if (setFullScreen) 
			vl.setFullScreen(Options.getBoolean(Options.OPTION_FULL_SCREEN));
		else
			vl.setFullScreen(false);
	}

	static public void setColorScheme()
	{
		//#sijapp cond.if modules_HISTORY is "true" #
		HistoryStorage.setColorScheme();
		//#sijapp cond.end#

		ChatHistory.setColorScheme();
		setColorScheme((VirtualList)ContactList.getVisibleContactListRef(), true);
	}

	/*****************************************************************************/
	/*****************************************************************************/
	/*****************************************************************************/

	// Waits until contact listupdate is completed
	public static class GetVersionInfoTimerTask extends TimerTask
	{
		// Try to get current Jimm version from Jimm server
		HttpConnection httemp;

		InputStream istemp;

		// Timer routine
		public void run()
		{
			try
			{
				httemp = (HttpConnection) Connector
						.open("http://www.jimm.org/en/current_ver");
				if (httemp.getResponseCode() != HttpConnection.HTTP_OK)
					throw new IOException();
				istemp = httemp.openInputStream();
				byte[] version_ = new byte[(int) httemp.getLength()];
				istemp.read(version_, 0, version_.length);
				version = new String(version_);
			} catch (Exception e)
			{
				e.printStackTrace();
				version = "Error: " + e.getMessage();
			}

			synchronized (_this)
			{
				if ((aboutTextList != null) && aboutTextList.isActive())
				{
					aboutTextList.addBigText(version, aboutTextList.getTextColor(), Font.STYLE_PLAIN, -1);
				}
			}
		}

	}

	/************************************************************************/
	/************************************************************************/
	/************************************************************************/

	///////////////////
	//               //
	//    Hotkeys    //
	//               //
	///////////////////
	static public void execHotKey(ContactListContactItem cItem, int keyCode,
			int type)
	{
		switch (keyCode)
		{
		case Canvas.KEY_NUM0:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEY0), cItem,
					type);
			break;
		case Canvas.KEY_NUM4:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEY4), cItem,
					type);
			break;

		case Canvas.KEY_NUM6:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEY6), cItem,
					type);
			break;

		case Canvas.KEY_STAR:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEYSTAR),
					cItem, type);
			break;

		case Canvas.KEY_POUND:
			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEYPOUND),
					cItem, type);
			break;

		//#sijapp cond.if target is "SIEMENS2"#
		//#		case -11:
		//#			// This means the CALL button was pressed...
		//#			execHotKeyAction(Options.getInt(Options.OPTION_EXT_CLKEYCALL), cItem, type);
		//#			break;
		//#sijapp cond.end#

		}
	}

	private static long lockPressedTime = -1;

	static private void execHotKeyAction(int actionNum,
			ContactListContactItem item, int keyType)
	{
		if (keyType == VirtualList.KEY_PRESSED)
		{
			lockPressedTime = System.currentTimeMillis();

			switch (actionNum)
			{

			//#sijapp cond.if modules_HISTORY is "true" #
			case Options.HOTKEY_HISTORY:
				if (item != null)
					item.showHistory();
				break;
			//#sijapp cond.end#

			case Options.HOTKEY_INFO:
				if (item != null)
					requiestUserInfo(
							item
									.getStringValue(ContactListContactItem.CONTACTITEM_UIN),
							item
									.getStringValue(ContactListContactItem.CONTACTITEM_NAME));
				break;

			case Options.HOTKEY_NEWMSG:
				if (item != null) writeMessage(item, null);
				break;

			case Options.HOTKEY_ONOFF:
				if (Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE))
					Options.setBoolean(Options.OPTION_CL_HIDE_OFFLINE, false);
				else
					Options.setBoolean(Options.OPTION_CL_HIDE_OFFLINE, true);
				Options.safe_save();
				ContactList.optionsChanged(true, false);
				ContactList.activate();
				break;

			case Options.HOTKEY_OPTIONS:
				Options.editOptions();
				break;

			case Options.HOTKEY_MENU:
				MainMenu.activate();
				break;

			//#sijapp cond.if target is "MIDP2"#
			case Options.HOTKEY_MINIMIZE:
				Jimm.setMinimized(true);
				break;
			//#sijapp cond.end#

			case Options.HOTKEY_CLI_INFO:
				if (item != null) showClientInfo(item);
				break;

			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			case Options.HOTKEY_FULLSCR:
				boolean fsValue = !Options
						.getBoolean(Options.OPTION_FULL_SCREEN);
				VirtualList.setFullScreenForCurrent(fsValue);
				Options.setBoolean(Options.OPTION_FULL_SCREEN, fsValue);
				Options.safe_save();
				//#sijapp cond.if target is "SIEMENS2"#
				//#sijapp cond.if modules_TRAFFIC is "true" #
				//#				ContactList.updateTitle(Traffic.getSessionTraffic());
				//#sijapp cond.else #
				//#				ContactList.updateTitle(0);
				//#sijapp cond.end#
				//#sijapp cond.end#
				break;
			//#sijapp cond.end#

			//#sijapp cond.if target isnot "DEFAULT" #
			case Options.HOTKEY_SOUNDOFF:
				ContactList.changeSoundMode(false);
				MainMenu.build();
				break;
			//#sijapp cond.end#
			}
		}

		else if ((keyType == VirtualList.KEY_REPEATED)
				|| (keyType == VirtualList.KEY_RELEASED))
		{
			if (lockPressedTime == -1)
				return;
			long diff = System.currentTimeMillis() - lockPressedTime;
			if ((actionNum == Options.HOTKEY_LOCK) && (diff > 900))
			{
				lockPressedTime = -1;
				SplashCanvas.lock();
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	//                                                                       //
	//               U S E R   A N D   C L I E N T   I N F O                 //
	//                                                                       //
	///////////////////////////////////////////////////////////////////////////

	// Information about the user
	final public static int UI_UIN = 0;

	final public static int UI_NICK = 1;

	final public static int UI_NAME = 2;

	final public static int UI_EMAIL = 3;

	final public static int UI_CITY = 4;

	final public static int UI_STATE = 5;

	final public static int UI_PHONE = 6;

	final public static int UI_FAX = 7;

	final public static int UI_ADDR = 8;

	final public static int UI_CPHONE = 9;

	final public static int UI_AGE = 10;

	final public static int UI_GENDER = 11;

	final public static int UI_HOME_PAGE = 12;

	final public static int UI_BDAY = 13;

	final public static int UI_W_CITY = 14;

	final public static int UI_W_STATE = 15;

	final public static int UI_W_PHONE = 16;

	final public static int UI_W_FAX = 17;

	final public static int UI_W_ADDR = 18;

	final public static int UI_W_NAME = 19;

	final public static int UI_W_DEP = 20;

	final public static int UI_W_POS = 21;

	final public static int UI_ABOUT = 22;

	final public static int UI_INETRESTS = 23;

	final public static int UI_AUTH = 24;

	final public static int UI_STATUS = 25;

	final public static int UI_ICQ_CLIENT = 26;

	final public static int UI_SIGNON = 27;

	final public static int UI_ONLINETIME = 28;

	final public static int UI_IDLE_TIME = 29;

	final public static int UI_ICQ_VERS = 31;

	final public static int UI_INT_IP = 32;

	final public static int UI_EXT_IP = 33;

	final public static int UI_PORT = 34;

	final public static int UI_UIN_LIST = 35;

	final public static int UI_FIRST_NAME = 36;

	final public static int UI_LAST_NAME = 37;

	//////
	final public static int UI_LAST_ID = 38;

	static private int uiBigTextIndex;

	static private String uiSectName = null;

	static private void addToTextList(String str, String langStr, TextList list)
	{
		if (uiSectName != null)
		{
			list.addBigText(ResourceBundle.getString(uiSectName),
					list.getTextColor(), Font.STYLE_BOLD, -1).doCRLF(-1);
			uiSectName = null;
		}

		list.addBigText(ResourceBundle.getString(langStr) + ": ",
				list.getTextColor(), Font.STYLE_PLAIN, uiBigTextIndex)
				.addBigText(str,
						Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING),
						Font.STYLE_PLAIN, uiBigTextIndex)
				.doCRLF(uiBigTextIndex);
		uiBigTextIndex++;
	}

	static private void addToTextList(int index, String[] data, String langStr,
			TextList list)
	{
		String str = data[index];
		if (str == null)
			return;
		if (str.length() == 0)
			return;

		addToTextList(str, langStr, list);
	}

	static public void fillUserInfo(String[] data, TextList list)
	{
		uiSectName = "main_info";
		addToTextList(UI_UIN_LIST, data, "uin", list);
		addToTextList(UI_NICK, data, "nick", list);
		addToTextList(UI_NAME, data, "name", list);
		addToTextList(UI_GENDER, data, "gender", list);
		addToTextList(UI_AGE, data, "age", list);
		addToTextList(UI_EMAIL, data, "email", list);
		if (data[UI_AUTH] != null)
			addToTextList(data[UI_AUTH].equals("1") ? ResourceBundle
					.getString("yes") : ResourceBundle.getString("no"), "auth",
					list);
		addToTextList(UI_BDAY, data, "birth_day", list);
		addToTextList(UI_CPHONE, data, "cell_phone", list);
		addToTextList(UI_HOME_PAGE, data, "home_page", list);
		addToTextList(UI_ABOUT, data, "notes", list);
		addToTextList(UI_INETRESTS, data, "interests", list);

		if (data[UI_STATUS] != null)
		{
			int stat = Integer.parseInt(data[UI_STATUS]);
			int imgIndex = 0;
			if (stat == 0)
				imgIndex = 6;
			else if (stat == 1)
				imgIndex = 7;
			else if (stat == 2)
				imgIndex = 3;
			list.addBigText(ResourceBundle.getString("status") + ": ",
					list.getTextColor(), Font.STYLE_PLAIN, uiBigTextIndex)
					.addImage(ContactList.getImageList().elementAt(imgIndex),
							null, uiBigTextIndex).doCRLF(uiBigTextIndex);
			uiBigTextIndex++;
		}

		uiSectName = "home_info";
		addToTextList(UI_CITY, data, "city", list);
		addToTextList(UI_STATE, data, "state", list);
		addToTextList(UI_ADDR, data, "addr", list);
		addToTextList(UI_PHONE, data, "phone", list);
		addToTextList(UI_FAX, data, "fax", list);

		uiSectName = "work_info";
		addToTextList(UI_W_NAME, data, "title", list);
		addToTextList(UI_W_DEP, data, "depart", list);
		addToTextList(UI_W_POS, data, "position", list);
		addToTextList(UI_W_CITY, data, "city", list);
		addToTextList(UI_W_STATE, data, "state", list);
		addToTextList(UI_W_ADDR, data, "addr", list);
		addToTextList(UI_W_PHONE, data, "phone", list);
		addToTextList(UI_W_FAX, data, "fax", list);

		uiSectName = "dc_info";
		addToTextList(UI_ICQ_CLIENT, data, "icq_client", list);
		addToTextList(UI_SIGNON, data, "li_signon_time", list);
		addToTextList(UI_ONLINETIME, data, "li_online_time", list);
		addToTextList(UI_IDLE_TIME, data, "li_idle_time", list);

		uiSectName = "DC";
		addToTextList(UI_ICQ_VERS, data, "ICQ version", list);
		addToTextList(UI_INT_IP, data, "Int IP", list);
		addToTextList(UI_EXT_IP, data, "Ext IP", list);
		addToTextList(UI_PORT, data, "Port", list);
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	static private TextList infoTextList = null;

	static public void requiestUserInfo(String uin, String name)
	{
		infoTextList = getInfoTextList(uin, false);
		infoTextList.setCommandListener(_this);

		if (Icq.isConnected())
		{
			if (uin == Options.getString(Options.OPTION_UIN))
				infoTextList.addCommandEx(cmdEdit, VirtualList.MENU_TYPE_RIGHT);

			RequestInfoAction act = new RequestInfoAction(uin, name);

			infoTextList.addCommandEx(cmdCancel, VirtualList.MENU_TYPE_LEFT_BAR);

			try
			{
				Icq.requestAction(act);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical())
					return;
			}

			infoTextList.add(ResourceBundle.getString("wait"));

			showInfoTextList(infoTextList);
		} else
		{
			String[] data = new String[JimmUI.UI_LAST_ID];
			data[JimmUI.UI_NICK] = name;
			data[JimmUI.UI_UIN_LIST] = uin;
			showUserInfo(data);
			showInfoTextList(infoTextList);
		}
	}

	static private String[] last_user_info;

	static public void showUserInfo(String[] data)
	{
		last_user_info = data;
		if (infoTextList == null) return;
		infoTextList.clear();
		JimmUI.fillUserInfo(data, infoTextList);
		infoTextList.removeCommandEx(cmdCancel);
		infoTextList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
		infoTextList.addCommandEx(cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
		infoTextList.addCommandEx(cmdCopyText, VirtualList.MENU_TYPE_RIGHT);
		infoTextList.addCommandEx(cmdCopyAll, VirtualList.MENU_TYPE_RIGHT);
	}

	static public TextList getInfoTextList(String caption, boolean addCommands)
	{
		infoTextList = new TextList(null);

		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
		infoTextList.setFontSize(Font.SIZE_MEDIUM);
		//#sijapp cond.else#
		//#		infoTextList.setFontSize(Font.SIZE_SMALL);
		//#sijapp cond.end#

		infoTextList.setCaption(caption);

		JimmUI.setColorScheme(infoTextList, false);
		infoTextList.setCursorMode(TextList.SEL_NONE);

		if (addCommands)
		{
			infoTextList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
			infoTextList.addCommandEx(cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
			infoTextList.addCommandEx(cmdCopyText, VirtualList.MENU_TYPE_RIGHT);
			infoTextList.addCommandEx(cmdCopyAll, VirtualList.MENU_TYPE_RIGHT);
			infoTextList.setCommandListener(_this);
		}

		return infoTextList;
	}

	static public void showInfoTextList(TextList list)
	{
		list.activate(Jimm.display);
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	static public String[] stdSelector = Util.explode("currect_contact" + "|"
			+ "all_contact_except_this" + "|" + "all_contacts", '|');

	static private List lstSelector;

	static private int lastSelectedItemIndex;

	static public void showSelector(String caption, String[] elements,
			CommandListener listener, int tag, boolean translateWords)
	{
		if (translateWords)
			for (int i = 0; i < elements.length; i++)
				elements[i] = ResourceBundle.getString(elements[i]);
		actionTag = tag;
		lstSelector = new List(ResourceBundle.getString(caption),
				List.IMPLICIT, elements, null);
		lstSelector.addCommand(cmdOk);
		lstSelector.addCommand(cmdCancel);
		lstSelector.setCommandListener(_this);
		JimmUI.listener = listener;
		Jimm.display.setCurrent(lstSelector);
	}

	static public int getLastSelIndex()
	{
		return lastSelectedItemIndex;
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	static public void addMessageText(TextList textList, String text, int color,
			int messTotalCounter)
	{
		//#sijapp cond.if modules_SMILES is "true" #
		Emotions.addTextWithEmotions(textList, text, Font.STYLE_PLAIN, color, messTotalCounter);
		//#sijapp cond.else#
		//#		textList.addBigText(text, textList.getTextColor(), Font.STYLE_PLAIN, messTotalCounter);
		//#sijapp cond.end#
		textList.doCRLF(messTotalCounter);
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	private final static long[] statuses =
	{ ContactList.STATUS_AWAY, ContactList.STATUS_CHAT, ContactList.STATUS_DND,
			ContactList.STATUS_INVISIBLE, ContactList.STATUS_NA,
			ContactList.STATUS_OCCUPIED, ContactList.STATUS_OFFLINE,
			ContactList.STATUS_ONLINE, ContactList.STATUS_INVIS_ALL, };

	//#sijapp cond.if target="MOTOROLA"#
	//#	public final static int[] st_colors =
	//#	{
	//#		0x00AACC,
	//#		0xFFFF00,
	//#		0xFF8888,
	//#		0xBBBBBB,
	//#		0xAA0088,
	//#		0xFFCC66,
	//#		0xFF0000,
	//#		0x00FF00,
	//#		0x888888,
	//#		0x0000FF
	//#	};
	//#sijapp cond.end#

	public final static int[] imageIndexes =
	{ 0, 1, 2, 3, 4, 5, 6, 7, 3 };

	private final static String[] statusStrings = Util.explode("status_away"
			+ "|" + "status_chat" + "|" + "status_dnd" + "|"
			+ "status_invisible" + "|" + "status_na" + "|" + "status_occupied"
			+ "|" + "status_offline" + "|" + "status_online" + "|"
			+ "status_invis_all", '|');

    public static final String[] xStatusStrings = {
    	"xstatus_none",
        "xstatus_angry",
        "xstatus_duck",
        "xstatus_tired",
        "xstatus_party",
        "xstatus_beer",
        "xstatus_thinking",
        "xstatus_eating",
        "xstatus_tv",
        "xstatus_friends",
        "xstatus_coffee",
        "xstatus_music",
        "xstatus_business",
        "xstatus_camera",
        "xstatus_funny",
        "xstatus_phone",
        "xstatus_games",
        "xstatus_college",
        "xstatus_shopping",
        "xstatus_sick",
        "xstatus_sleeping",
        "xstatus_surfing",
        "xstatus_internet",
        "xstatus_engineering",
        "xstatus_typing",
        "xstatus_unk",
        "xstatus_ppc",
        "xstatus_mobile",
        "xstatus_man",
        "xstatus_wc",
        "xstatus_question",
        "xstatus_way",
        "xstatus_heart",
        "xstatus_cigarette",
        "xstatus_sex",
        "xstatus_rambler_search",
        "xstatus_rambler_journal"
    };

	public static int getStatusIndex(long status)
	{
		for (int i = 0; i < statuses.length; i++)
			if (statuses[i] == status)
				return i;
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
		return (index == -1) ? null : ResourceBundle
				.getString(statusStrings[index]);
	}

	public static final int SHS_TYPE_ALL = 1;

	public static final int SHS_TYPE_EMPTY = 2;

	public static int[] showGroupSelector(String caption, int tag,
			CommandListener listener, int type, int excludeGroupId)
	{
		ContactListGroupItem[] groups = ContactList.getGroupItems();
		String[] groupNamesTmp = new String[groups.length];
		int[] groupIdsTmp = new int[groups.length];

		int index = 0;
		for (int i = 0; i < groups.length; i++)
		{
			int groupId = groups[i].getId();
			if (groupId == excludeGroupId)
				continue;
			switch (type)
			{
			case SHS_TYPE_EMPTY:
				ContactListContactItem[] cItems = ContactList
						.getGroupItems(groupId);
				if (cItems.length != 0)
					continue;
				break;
			}

			groupIdsTmp[index] = groupId;
			groupNamesTmp[index] = groups[i].getName();
			index++;
		}

		if (index == 0)
		{
			Alert alert = new Alert("", ResourceBundle.getString("no_availible_groups"), null, AlertType.INFO);
			alert.setTimeout(Alert.FOREVER);
			Jimm.display.setCurrent(alert);
			return null;
		}

		String[] groupNames = new String[index];
		int[] groupIds = new int[index];

		System.arraycopy(groupIdsTmp, 0, groupIds, 0, index);
		System.arraycopy(groupNamesTmp, 0, groupNames, 0, index);

		showSelector(ResourceBundle.getString(caption), groupNames, listener,
				tag, false);

		return groupIds;
	}
	
	///////
	
	public static void addTextListItem(TextList list, String text, Image image, int value)
	{
		if (image != null) list.addImage(image, null, value);
		list.addBigText(" "+ResourceBundle.getString(text), list.getTextColor(), Font.STYLE_PLAIN, value);
		list.doCRLF(value);
	}
	
	//////
	
	static public boolean isControlActive(VirtualList list)
	{
		if (list == null) return false;
		return list.isActive();
	}
	
	//
	// Text editor for messages
	//
	
	/* Size of text area for entering mesages */
	final public static int MAX_EDITOR_TEXT_SIZE = 2000;
	
	/* Textbox for entering messages */
	private static TextBox messageTextbox;
	
	/* receiver for text message */
	private static ContactListContactItem textMessReceiver;
	
	/* Modes constant for text editor */
	final private static int EDITOR_MODE_MESSAGE = 200001;
	
	/* Current text editor mode */
	private static int textMessCurMode; 
	
	private static void removeTextMessageCommands()
	{
		messageTextbox.removeCommand(cmdSend);
		messageTextbox.removeCommand(cmdCancel);
	}
	
	/* Write message */
	public static void writeMessage(ContactListContactItem receiver, String initText)
	{
		if (messageTextbox == null)
		{
			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			messageTextbox = new TextBox(ResourceBundle.getString("message"), null, MAX_EDITOR_TEXT_SIZE, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
			//#sijapp cond.else#
			messageTextbox = new TextBox(ResourceBundle.getString("message"), null, MAX_EDITOR_TEXT_SIZE, TextField.ANY);
			//#sijapp cond.end#
		}
		
		textMessReceiver = receiver;
		textMessCurMode = EDITOR_MODE_MESSAGE;
		
		removeTextMessageCommands();
		messageTextbox.addCommand(cmdSend);
		messageTextbox.addCommand(cmdCancel);
		messageTextbox.addCommand(cmdClearText);
		
		//#sijapp cond.if modules_SMILES is "true" #
		messageTextbox.addCommand(cmdInsertEmo);
		//#sijapp cond.end#
		
		messageTextbox.addCommand(cmdInsTemplate);
		
		if (initText != null) messageTextbox.setString(initText);
		messageTextbox.setCommandListener(_this);
		Jimm.display.setCurrent(messageTextbox);
	}

	public static void sendMessage(String text, ContactListContactItem textMessReceiver)
	{
		/* Construct plain message object and request new SendMessageAction
		 Add the new message to the chat history */

		if (text == null)
			return;
		if (text.length() == 0)
			return;

		PlainMessage plainMsg = new PlainMessage(Options.getString(Options.OPTION_UIN), textMessReceiver, Message.MESSAGE_TYPE_NORM, Util.createCurrentDate(false), text);

		SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
		try
		{
			Icq.requestAction(sendMsgAct);
		} catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical())
				return;
		}
		ChatHistory.addMyMessage(textMessReceiver, text, plainMsg.getNewDate(), textMessReceiver.getStringValue(ContactListContactItem.CONTACTITEM_NAME));

		//#sijapp cond.if modules_HISTORY is "true" #
		if (Options.getBoolean(Options.OPTION_HISTORY))
			HistoryStorage.addText(textMessReceiver.getStringValue(ContactListContactItem.CONTACTITEM_UIN),
					text, (byte) 1, ResourceBundle.getString("me"), plainMsg
							.getNewDate());
		//#sijapp cond.end#
	}
	
	/////////////////////////////////////////////////////

	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	private static TextList URLList;
	private static Object lastScreenBeforeUrlSelect;
	
	public static void gotoURL(String msg, Object lastScreen)
	{
		lastScreenBeforeUrlSelect = lastScreen;
		Vector v = Util.parseMessageForURL(msg);
		if (v.size() == 1)
		{
			try
			{
				Jimm.jimm.platformRequest((String) v.elementAt(0));
			} catch (Exception e) {}
		}
		else
		{
			URLList = JimmUI.getInfoTextList(ResourceBundle.getString("goto_url"), false);
			URLList.addCommandEx(cmdSelect, VirtualList.MENU_TYPE_RIGHT);
			URLList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_RIGHT);
			URLList.setCommandListener(_this);
			for (int i = 0; i < v.size(); i++)
			{
				URLList.addBigText((String) v.elementAt(i), URLList.getTextColor(),
						Font.STYLE_PLAIN, i).doCRLF(i);
			}
			JimmUI.showInfoTextList(URLList);
		}
	}
	//#sijapp cond.end#
	
	///////////////////////////////////////////////////////////
	
	private static int authType;
	private static TextBox authTextbox;
	private static ContactListContactItem authContactItem;

	public static final int AUTH_TYPE_DENY = 10001;
	public static final int AUTH_TYPE_REQ_AUTH = 10002;
	
	public static void authMessage(int authType, ContactListContactItem contactItem, String caption, String text)
	{
		JimmUI.authType = authType;
		authContactItem = contactItem;
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		authTextbox = new TextBox(ResourceBundle.getString(caption), ResourceBundle.getString(text), 500, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
		//#sijapp cond.else#
		authTextbox = new TextBox(ResourceBundle.getString(caption), ResourceBundle.getString(text), 500, TextField.ANY);
		//#sijapp cond.end#

		
		authTextbox.addCommand(cmdSend);
		authTextbox.addCommand(cmdCancel);
		authTextbox.setCommandListener(_this);
		Jimm.display.setCurrent(authTextbox);
	}
	
	/////////////////////////////////////////////////////////////
	
	private static final int USER_MENU_MESSAGE = 1;
	private static final int USER_MENU_STATUS_MESSAGE = 3;
	private static final int USER_MENU_REQU_AUTH = 4;
	private static final int USER_MENU_FILE_TRANS = 5;
	private static final int USER_MENU_CAM_TRANS = 6;
	private static final int USER_MENU_USER_REMOVE = 7;
	private static final int USER_MENU_REMOVE_ME = 8;
	private static final int USER_MENU_RENAME = 9;
    private static final int USER_MENU_HISTORY = 10;
	private static final int USER_MENU_LOCAL_INFO = 11;
	private static final int USER_MENU_USER_INFO = 12;
	private static final int USER_MENU_QUOTA = 14;
	
	private static TextList tlContactMenu;
	private static ContactListContactItem clciContactMenu;
	private static TextList removeContactMessageBox;
	private static TextList removeMeMessageBox;
	private static TextBox renameTextbox;
	
	public static void showContactMenu(ContactListContactItem contact)
	{
		clciContactMenu = contact; 
		tlContactMenu = new TextList(ResourceBundle.getString("user_menu"));
		JimmUI.setColorScheme(tlContactMenu, false);
		tlContactMenu.setCursorMode(VirtualList.SEL_NONE);
		tlContactMenu.activate(Jimm.display);
		tlContactMenu.addCommandEx(cmdSelect, VirtualList.MENU_TYPE_LEFT_BAR);
		tlContactMenu.addCommandEx(cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
		tlContactMenu.setCommandListener(_this);
		
		long status = contact.getIntValue(ContactListContactItem.CONTACTITEM_STATUS);
		
		if (contact.getBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH))
			addTextListItem(tlContactMenu, "requauth", null, USER_MENU_REQU_AUTH);
		
		addTextListItem(tlContactMenu, "send_message", null, USER_MENU_MESSAGE);
		
		if (JimmUI.getClipBoardText() != null)
			addTextListItem(tlContactMenu, "quote", null, USER_MENU_QUOTA);
		
		addTextListItem(tlContactMenu, "info", null, USER_MENU_USER_INFO);
		
		if ((status != ContactList.STATUS_ONLINE)
				&& (status != ContactList.STATUS_OFFLINE)
				&& (status != ContactList.STATUS_INVISIBLE))
			addTextListItem(tlContactMenu, "reqstatmsg", null, USER_MENU_STATUS_MESSAGE);		

		
		//#sijapp cond.if modules_FILES is "true"#
		//if ((status != ContactList.STATUS_OFFLINE) 
		//		&& contact.getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT) >= 8)
		{
			addTextListItem(tlContactMenu, "ft_name", null, USER_MENU_FILE_TRANS);
			//#sijapp cond.if target isnot "MOTOROLA"#
			addTextListItem(tlContactMenu, "ft_cam", null, USER_MENU_CAM_TRANS);
			//#sijapp cond.end#
		}
		//#sijapp cond.end#
		
		addTextListItem(tlContactMenu, "remove", null, USER_MENU_USER_REMOVE);
		addTextListItem(tlContactMenu, "remove_me", null, USER_MENU_REMOVE_ME);
		addTextListItem(tlContactMenu, "rename", null, USER_MENU_RENAME);

		//#sijapp cond.if modules_HISTORY is "true" #
		addTextListItem(tlContactMenu, "history", null, USER_MENU_HISTORY);
		//#sijapp cond.end#

		if (status != ContactList.STATUS_OFFLINE)
			addTextListItem(tlContactMenu, "dc_info", null, USER_MENU_LOCAL_INFO);
		
	}
	
	private static void contactMenuSelected(int index)
	{
		switch (index)
		{
		case USER_MENU_REQU_AUTH:
			JimmUI.authMessage(JimmUI.AUTH_TYPE_REQ_AUTH, clciContactMenu, "requauth", "plsauthme");
			break;

		case USER_MENU_MESSAGE:
			writeMessage(clciContactMenu, null);
			break;
			
		case USER_MENU_QUOTA:
			writeMessage(clciContactMenu, JimmUI.getClipBoardText());
			break;
			
		case USER_MENU_STATUS_MESSAGE:
			long status = clciContactMenu.getIntValue(ContactListContactItem.CONTACTITEM_STATUS);
			if (!((status == ContactList.STATUS_ONLINE)
					|| (status == ContactList.STATUS_OFFLINE) || (status == ContactList.STATUS_INVISIBLE)))
			{
				int msgType;
				/* Send a status message request message */
				if (status == ContactList.STATUS_AWAY)
					msgType = Message.MESSAGE_TYPE_AWAY;
				else if (status == ContactList.STATUS_OCCUPIED)
					msgType = Message.MESSAGE_TYPE_OCC;
				else if (status == ContactList.STATUS_DND)
					msgType = Message.MESSAGE_TYPE_DND;
				else if (status == ContactList.STATUS_CHAT)
					msgType = Message.MESSAGE_TYPE_FFC;
				else if (status == ContactList.STATUS_NA)
					msgType = Message.MESSAGE_TYPE_NA;
				else
					msgType = Message.MESSAGE_TYPE_AWAY;

				PlainMessage awayReq = new PlainMessage(Options.getString(Options.OPTION_UIN),
						clciContactMenu, msgType, Util
								.createCurrentDate(false), "");

				SendMessageAction act = new SendMessageAction(awayReq);
				try
				{
					Icq.requestAction(act);

				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical())
						return;
				}
			}
			break;
			

			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			//#sijapp cond.if modules_FILES is "true"#                    
			case USER_MENU_FILE_TRANS:
				/* Send a filetransfer with a file given by path */
				{
					FileTransfer ft = new FileTransfer(FileTransfer.FT_TYPE_FILE_BY_NAME, clciContactMenu);
					ft.startFT();
				}
				break;

			//#sijapp cond.if target isnot "MOTOROLA" #
			case USER_MENU_CAM_TRANS:
				/* Send a filetransfer with a camera image
				 We can only make file transfers with ICQ clients prot V8 and up */
				if (clciContactMenu.getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT) < 8)
				{
					JimmException.handleException(new JimmException(190, 0, true));
				} else
				{
					FileTransfer ft = new FileTransfer(FileTransfer.FT_TYPE_CAMERA_SNAPSHOT, clciContactMenu);
					ft.startFT();
				}
				break;
			//#sijapp cond.end#
			//#sijapp cond.end#
			//#sijapp cond.end#
			
			case USER_MENU_USER_REMOVE:
				removeContactMessageBox = showMessageBox
				(
					ResourceBundle.getString("remove") + "?", 
					ResourceBundle.getString("remove") + " " + clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_NAME) + "?", 
					JimmUI.MESBOX_OKCANCEL
				);
				break;
				
			case USER_MENU_REMOVE_ME: /* Remove me from other users contact list */
				removeMeMessageBox = showMessageBox
				(
					ResourceBundle.getString("remove_me") + "?",
					ResourceBundle.getString("remove_me_from") + clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_NAME) + "?", 
					JimmUI.MESBOX_OKCANCEL
				); 
				break;
				
			case USER_MENU_RENAME:
				renameTextbox = new TextBox
				(
					ResourceBundle.getString("rename"),
					clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_NAME),
					64,
					TextField.ANY
				);
				renameTextbox.addCommand(cmdOk);
				renameTextbox.addCommand(cmdCancel);
				renameTextbox.setCommandListener(_this);
				Jimm.display.setCurrent(renameTextbox);
				break;
				
			case USER_MENU_USER_INFO:
				JimmUI.requiestUserInfo
				(
					clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_UIN), 
					clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_NAME)
				);
				break;
				
			case USER_MENU_LOCAL_INFO:
				showClientInfo(clciContactMenu);
				break;
				
			//#sijapp cond.if modules_HISTORY is "true" #
			case USER_MENU_HISTORY:
				HistoryStorage.showHistoryList
				(
					clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_UIN), 
					clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_NAME)
				);
				break;
			//#sijapp cond.end#
		}
	}
	
	public static void showClientInfo(ContactListContactItem cItem)
	{
		TextList tlist = JimmUI.getInfoTextList(
				cItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN), true);
		String[] clInfoData = new String[JimmUI.UI_LAST_ID];

		/* sign on time */
		long signonTime = cItem.getIntValue(ContactListContactItem.CONTACTITEM_SIGNON);
		if (signonTime > 0)
			clInfoData[JimmUI.UI_SIGNON] = Util
					.getDateString(false, signonTime);

		/* online time */
		long onlineTime = cItem.getIntValue(ContactListContactItem.CONTACTITEM_ONLINE);
		if (onlineTime > 0)
			clInfoData[JimmUI.UI_ONLINETIME] = Util
					.longitudeToString(onlineTime);

		/* idle time */
		int idleTime = cItem.getIntValue(ContactListContactItem.CONTACTITEM_IDLE);
		if (idleTime > 0)
			clInfoData[JimmUI.UI_IDLE_TIME] = Util.longitudeToString(idleTime);
		
		//#sijapp cond.if (target="MIDP2" | target="MOTOROLA" | target="SIEMENS2") & modules_FILES="true"#

		/* Client version */
		int clientVers = cItem.getIntValue(ContactListContactItem.CONTACTITEM_CLIENT);
		if (clientVers != Icq.CLI_NONE)
			clInfoData[JimmUI.UI_ICQ_CLIENT] = Icq.getClientString((byte) clientVers)
					+ " " + cItem.getStringValue(ContactListContactItem.CONTACTITEM_CLIVERSION);

		/* ICQ protocol version */
		clInfoData[JimmUI.UI_ICQ_VERS] = Integer
				.toString(cItem.getIntValue(ContactListContactItem.CONTACTITEM_ICQ_PROT));

		/* Internal IP */
		clInfoData[JimmUI.UI_INT_IP] = Util
				.ipToString(cItem.getIPValue(ContactListContactItem.CONTACTITEM_INTERNAL_IP));

		/* External IP */
		clInfoData[JimmUI.UI_EXT_IP] = Util
				.ipToString(cItem.getIPValue(ContactListContactItem.CONTACTITEM_EXTERNAL_IP));

		/* Port */
		int port = cItem.getIntValue(ContactListContactItem.CONTACTITEM_DC_PORT);
		if (port != 0)
			clInfoData[JimmUI.UI_PORT] = Integer.toString(port);
		//#sijapp cond.end#

		JimmUI.fillUserInfo(clInfoData, tlist);
		JimmUI.showInfoTextList(tlist);
	}

	private static void menuRemoveContactSelected()
	{
		String uin = clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
		ChatHistory.chatHistoryDelete(uin);
		boolean ok = Icq.delFromContactList(clciContactMenu);
		if (ok)
		{
			//#sijapp cond.if modules_HISTORY is "true" #
			HistoryStorage.clearHistory(uin);
			//#sijapp cond.end#
		}
	}
	
	private static void menuRemoveMeSelected()
	{
		RemoveMeAction remAct = new RemoveMeAction(clciContactMenu.getStringValue(ContactListContactItem.CONTACTITEM_UIN));

		try
		{
			Icq.requestAction(remAct);
		} 
		catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical()) return;
		}
		ContactList.activate();
	}
	
	private static void menuRenameSelected()
	{
		String newName = renameTextbox.getString();
		if ((newName == null) || (newName.length() == 0)) return;
		clciContactMenu.rename(newName);
		messageTextbox.setString(null);
	}
	
	////////////////////////////////
	
	public static Object getCurrentScreen()
	{
		if (VirtualList.getCurrent() != null) return VirtualList.getCurrent();
		Displayable disp = Jimm.display.getCurrent();
		if ((disp == null) || (disp instanceof Canvas)) return null;
		return disp;
	}
	
	private static TimerTasks flashTimerTask;
	
	public static void showCapText(Object control, String text, int type)
	{
		if ((text == null) || (control == null) || (control instanceof Canvas)) return;
		if (!Options.getBoolean(Options.OPTION_CREEPING_LINE)) return;
		if ((type == TimerTasks.TYPE_FLASH) && 
			(flashTimerTask != null) && 
			!flashTimerTask.isCanceled() && 
			(flashTimerTask.getType() == TimerTasks.TYPE_CREEPING)) return;
		
		if (flashTimerTask != null) flashTimerTask.flashRestoreOldCaption();
		flashTimerTask = new TimerTasks(control, text, (type == TimerTasks.TYPE_FLASH) ? 10 : 0, type);
		int interval = (type == TimerTasks.TYPE_FLASH) ? 500 : 300;
		Jimm.getTimerRef().schedule(flashTimerTask, interval, interval);
	}
	
	public static void showCreepingLine(Object control, String text, ContactListContactItem cItem)
	{
		if ((text == null) || (control == null))
		if (!Options.getBoolean(Options.OPTION_CREEPING_LINE)) return;
		ChatTextList curChat = ChatHistory.getCurrent(); 
		if ((curChat != null) && (curChat.getUIControl() == control) && (curChat.isVisible())) return;
		String name = cItem.getStringValue(ContactListContactItem.CONTACTITEM_NAME);
		String creepingText = name+": "+text;
		showCapText(control, creepingText, TimerTasks.TYPE_CREEPING);
	}
	
	//////////////////////////////////
	
}
