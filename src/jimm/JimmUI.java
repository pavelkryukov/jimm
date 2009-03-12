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
import DrawControls.ImageList;
import DrawControls.TextList;
import DrawControls.VirtualList;

public class JimmUI implements CommandListener
{
	static private Vector lastScreens = new Vector();
	static private TextList msgBoxList;
	
	public static void setLastScreen(JimmScreen screen, boolean isRoot)
	{
		synchronized (lastScreens)
		{
			if (isRoot) lastScreens.removeAllElements();
			else lastScreens.removeElement(screen);
			lastScreens.addElement(screen);
		}
	}
	
	public static void removeScreen(JimmScreen screen)
	{
		synchronized (lastScreens)
		{
			lastScreens.removeElement(screen);
		}
	}
	
	public static void backToLastScreen()
	{
		synchronized (lastScreens)
		{
			if (lastScreens.size() == 0) MainMenu.activateMenu();
			else
			{
				for (;;)
				{
					JimmScreen screen = (JimmScreen)lastScreens.lastElement();
					lastScreens.removeElement(screen);
					
					if (lastScreens.size() == 0 || !screen.isScreenActive()) 
					{
						//System.out.println("lastScreens.size()="+lastScreens.size()+", screen.isScreenActive()="+screen.isScreenActive()+", screen="+screen.getClass().getName());
						screen.activate();
						break;
					}
				} 
			}
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
	public final static Command cmdSave = new Command(ResourceBundle.getString("save"), Command.ITEM, 1);
	public final static Command cmdOk = new Command(ResourceBundle.getString("ok"), Command.OK, 1);
	public final static Command cmdCancel = new Command(ResourceBundle.getString("cancel"), Jimm.cmdBack, 5);
	public final static Command cmdYes = new Command(ResourceBundle.getString("yes"), Command.OK, 1);
	public final static Command cmdNo = new Command(ResourceBundle.getString("no"), Command.CANCEL, 2);
	public final static Command cmdFind = new Command(ResourceBundle.getString("find"), Command.OK, 1);
	public final static Command cmdBack = new Command(ResourceBundle.getString("back"), Jimm.cmdBack, 1);
	public final static Command cmdCopyText = new Command(ResourceBundle.getString("copy_text"), Command.ITEM, 3);
	public final static Command cmdCopyAll = new Command(ResourceBundle.getString("copy_all_text"), Command.ITEM, 4);
	public final static Command cmdEdit = new Command(ResourceBundle.getString("edit"), Command.ITEM, 1);
	public final static Command cmdMenu = new Command(ResourceBundle.getString("menu"), Command.ITEM, 1);
	public final static Command cmdSelect = new Command(ResourceBundle.getString("select"), Command.OK, 2);
	public final static Command cmdSelect2 = new Command(ResourceBundle.getString("select"), Command.ITEM, 2);
	public final static Command cmdSend = new Command(ResourceBundle.getString("send"), Command.OK, 1);
	public final static Command cmdList = new Command(ResourceBundle.getString("contact_list"), Command.ITEM, 1);
	public final static Command cmdInfo = new Command(ResourceBundle.getString("info"), Command.ITEM, 10);
	
	//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #
	public final static Command cmdInsertEmo = new Command(ResourceBundle.getString("insert_emotion"), Command.ITEM, 3);
	//#sijapp cond.end#
	
	public final static Command cmdInsTemplate = new Command(ResourceBundle.getString("templates"), Command.ITEM, 4);	
	
	//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
	public final static Command cmdGotoURL = new Command(ResourceBundle.getString("goto_url"), Command.ITEM, 9);
	//#sijapp cond.end#
	
	private final static Command cmdClearText = new Command(ResourceBundle.getString("clear"), Command.ITEM, 5);

	static private CommandListener listener;
	static private Hashtable commands = new Hashtable();
	static private JimmUI _this;
	
	// Misc constants
	final private static int GROUP_SELECTOR_MOVE_TAG = 4; 

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
		Jimm.aaUserActivity();
		
		if (isControlActive(tlStatusMessage))
		{
			tlStatusMessage = null;
			statusMessCI = null;
			backToLastScreen();
		}
		
		else if (isControlActive(loadErrorTextList))
		{
			MainMenu.activateMenu();
		}
		
		else if (isControlActive(removeContactMessageBox))
		{
			if (c == cmdOk) menuRemoveContactSelected();
			else backToLastScreen();
			removeContactMessageBox = null;
		}
		
		else if ((renameTextbox != null) && (d == renameTextbox))
		{
			if (c == cmdOk) menuRenameSelected();
			backToLastScreen();
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
			else
			{
				backToLastScreen();
				tlContactMenu = null;
				clciContactMenu = null;
			}
		}
		
		else if ((authTextbox != null) && (d == authTextbox))
		{
			if (c == cmdSend)
			{
				SystemNotice notice = null;
				boolean authRequested = false;

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
							authContactItem.getStringValue(ContactItem.CONTACTITEM_UIN),
							false, reasonText);
					break;
				case AUTH_TYPE_REQ_AUTH:
					notice = new SystemNotice(
							SystemNotice.SYS_NOTICE_REQUAUTH,
							authContactItem.getStringValue(ContactItem.CONTACTITEM_UIN),
							false, reasonText);
					authRequested = true;
					break;
				}
				
				/* Assemble the sysNotAction and request it */
				SysNoticeAction sysNotAct = new SysNoticeAction(notice);
				UpdateContactListAction updateAct = new UpdateContactListAction(
						authContactItem, UpdateContactListAction.ACTION_REQ_AUTH);

				try
				{
					Icq.requestAction(sysNotAct);
					
					if (authContactItem.getBooleanValue(ContactItem.CONTACTITEM_IS_TEMP))
						Icq.requestAction(updateAct);
					
					ChatHistory.rebuildMenu(authContactItem);
					
					if (authRequested) 
						authContactItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, false);
					
					authContactItem.setBooleanValue(ContactItem.CONTACTITEM_B_AUTREQUESTS, false);
				} 
				catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
			}

			boolean activated = ChatHistory.activateIfExists(authContactItem);
			if (!activated) JimmUI.backToLastScreen(); 

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
			URLList = null;
			
			backToLastScreen();
			return;
		}
		//#sijapp cond.end#
		
		else if ((messageTextbox != null) && (d == messageTextbox))
		{
			sendTypeingNotify(false);
			if (c == cmdCancel)
			{
				backToLastScreen();
			}
			else if (c == cmdSend)
			{
				switch (textMessCurMode)
				{
				case EDITOR_MODE_MESSAGE:
					String messText = messageTextbox.getString();

					if (messText.length() != 0)
					{
						sendMessage(messText, textMessReceiver);
						if (Jimm.getPhoneVendor() == Jimm.PHONE_SONYERICSSON)
						{
							messageTextbox = null;
						}
						else
						{
							messageTextbox.setString(null);
						}
					}
					boolean activated = ChatHistory.activateIfExists(textMessReceiver);
					if (!activated) JimmUI.backToLastScreen();
					break;
				}
			}
			
			//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #
			else if (c == cmdInsertEmo)
			{
				Emotions.selectEmotion(messageTextbox);
			}
			//#sijapp cond.end#
			
			else if (c == cmdInsTemplate)
			{
				Templates.selectTemplate(messageTextbox);
			}
			
			else if (c == cmdClearText)
			{
				messageTextbox.setString(new String());
			}
		}
		
		// "About" -> "Back"
		else if (JimmUI.isControlActive(aboutTextList))
		{
			if (c == cmdBack)
			{
				MainMenu.activateMenu();
				aboutTextList = null;
			}
//#sijapp cond.if target isnot "DEFAULT"#			
			else if (c == cmdSelect)
			{
				if (aboutTextList.getCurrTextIndex() == ABOUT_JIMM_WAP)
				{
					try
					{					
						Jimm.jimm.platformRequest("http://jimm.org/wap/");
					}
					catch (Exception e) {}
				}
			}
//#sijapp cond.end#		
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
				EditInfo.showEditForm(last_user_info);
			}

			// "User info" -> "Copy text, Copy all"
			else if ((c == cmdCopyText) || (c == cmdCopyAll))
			{
				JimmUI.setClipBoardText("[" + getCaption(infoTextList) + "]\n"
						+ infoTextList.getCurrText(0, (c == cmdCopyAll)));
			}
		}

		// "Selector"
		else if (isControlActive(lstSelector))
		{
			lastSelectedItemIndex = lstSelector.getCurrTextIndex();
			
			// User have selected new group for contact
			if ((curScreenTag == GROUP_SELECTOR_MOVE_TAG) && ((c == cmdOk) || (c == List.SELECT_COMMAND)))
			{
				int currGroupId = clciContactMenu.getIntValue(ContactItem.CONTACTITEM_GROUP);
				int newGroupId = groupList[JimmUI.getLastSelIndex()];
				ContactListGroupItem oldGroup = ContactList.getGroupById(currGroupId);
				ContactListGroupItem newGroup = ContactList.getGroupById(newGroupId);

				UpdateContactListAction act = new UpdateContactListAction(clciContactMenu, oldGroup, newGroup);

				try
				{
					Icq.requestAction(act);
					SplashCanvas.addTimerTask("wait", act, false);
				}
				catch (JimmException e)
				{
					JimmException.handleException(e);
				}
				return;
			}
			else listener.commandAction(c, d); 

			lstSelector = null;
			listener = null;

			curScreenTag = -1;
		}

		// Message box
		else if ((msgForm != null) && (d == msgForm))
		{
			listener.commandAction(c, d);
			msgForm = null;
			curScreenTag = -1;
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
	
	public static int getCurScreenTag()
	{
		if ((msgForm != null) && (msgForm.isShown())) return curScreenTag;
		if (isControlActive(msgBoxList)) return curScreenTag;
		if (isControlActive(lstSelector)) return curScreenTag;
		return -1;
	}

	/////////////////////////
	//                     // 
	//     Message Box     //
	//                     //
	/////////////////////////
	static private Form msgForm;

	static private int curScreenTag = -1;

	public static int getCommandType(Command testCommand, int testTag)
	{
		return (curScreenTag == testTag) ? getCommandIdx(testCommand) : -1;
	}

	final public static int MESBOX_YESNO    = 1;
	final public static int MESBOX_OKCANCEL = 2;
	final public static int MESBOX_OK       = 3;

	static public void messageBox(String cap, String text, int type,
			CommandListener listener, int tag)
	{
		clearAll();

		curScreenTag = tag;
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
			
		case MESBOX_OK:
			msgForm.addCommand(cmdOk);
			msgForm.addCommand(cmdCancel);
			break;
		}

		JimmUI.listener = listener;
		msgForm.setCommandListener(_this);
		Jimm.display.setCurrent(msgForm);
		Jimm.setBkltOn(true);
	}
	
	static private TextList showMessageBox(String cap, String text, int type)
	{
		msgBoxList = new TextList(cap);
		msgBoxList.setMode(VirtualList.CURSOR_MODE_DISABLED);
		setColorScheme(msgBoxList, false, -1, true);
		msgBoxList.setFontSize(Font.SIZE_LARGE);
		msgBoxList.addBigText(text, msgBoxList.getTextColor(), Font.STYLE_PLAIN, -1);
		
		switch (type)
		{
		case MESBOX_YESNO:
			msgBoxList.addCommandEx(cmdYes, VirtualList.MENU_TYPE_RIGHT_BAR);
			msgBoxList.addCommandEx(cmdNo, VirtualList.MENU_TYPE_LEFT_BAR);
			break;

		case MESBOX_OKCANCEL:
			msgBoxList.addCommandEx(cmdOk, VirtualList.MENU_TYPE_RIGHT_BAR);
			msgBoxList.addCommandEx(cmdCancel, VirtualList.MENU_TYPE_LEFT_BAR);
			break;
		}
		
		msgBoxList.setCommandListener(_this);
		msgBoxList.activate(Jimm.display);
		return msgBoxList; 
	}

	//////////////////////////////////////////////////////////////////////////////
	private static TextList aboutTextList;

	// String for recent version
	static private String version;
	static private String betaVersion;
	static private final int ABOUT_JIMM_WAP = 1000;

	static public void about()
	{
		System.gc();
		long freeMem = Runtime.getRuntime().freeMemory() / 1024;
		
		if (aboutTextList == null)
			aboutTextList = new TextList(null);

		aboutTextList.lock();
		aboutTextList.clear();
		aboutTextList.setMode(VirtualList.CURSOR_MODE_DISABLED);
		setColorScheme(aboutTextList, false, -1, true);

		aboutTextList.setCaption(ResourceBundle.getString("about"));
		
		aboutTextList.addBigText(
				ResourceBundle.getString("about_cap"), 
				Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING, -1), 
				Font.STYLE_BOLD, 
				-1
		);
		aboutTextList.doCRLF(-1);

		String commaAndSpace = ", ";

		StringBuffer str = new StringBuffer();
		str.append(" ").append(ResourceBundle.getString("about_info")).append("\n");
		
		aboutTextList.addBigText(str.toString(), -1, Font.STYLE_PLAIN, -1);
		aboutTextList.doCRLF(-1);
		
//#sijapp cond.if target isnot "DEFAULT"#
		JimmUI.addTextListItem(aboutTextList, "about_visit_wap", JimmUI.eventUrlMessageImg, ABOUT_JIMM_WAP, true, -1, Font.STYLE_BOLD);
		aboutTextList.doCRLF(-1);
		aboutTextList.addCommandEx(cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#
		
		str.setLength(0);
		str	.append(ResourceBundle.getString("midp_info"))
			.append(": ").append(Jimm.microeditionPlatform);

		if (Jimm.microeditionProfiles != null)
			str.append(commaAndSpace).append(Jimm.microeditionProfiles);
		
		String locale = System.getProperty("microedition.locale");
		if (locale != null) str.append(commaAndSpace).append(locale);

		str	.append("\n\n").append(ResourceBundle.getString("free_heap"))
			.append(": ").append(freeMem).append("kb\n")
			.append(ResourceBundle.getString("total_mem")).append(": ")
			.append(Runtime.getRuntime().totalMemory() / 1024).append("kb\n\n")
			.append(ResourceBundle.getString("latest_ver")).append(":\n");
		
		aboutTextList.addBigText(str.toString(), -1, Font.STYLE_PLAIN, -1);
		
		internalShowLastVers();

		aboutTextList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
		aboutTextList.setCommandListener(_this);
		aboutTextList.unlock();
		
		aboutTextList.activate(Jimm.display);
		
		// request last jimm version
		if (version == null && betaVersion == null) Threads.requestLastJimmVers();
	}
	
	static private String readHttpContentFirstString(String url)
	{
		HttpConnection httemp = null;
		InputStream istemp = null;
		String result = null;
		
		try
		{
			httemp = (HttpConnection) Connector.open(url);
			if (httemp.getResponseCode() != HttpConnection.HTTP_OK) throw new IOException();
			istemp = httemp.openInputStream();
			byte[] versRaw_ = new byte[(int) httemp.getLength()];
			istemp.read(versRaw_, 0, versRaw_.length);
			result = new String(versRaw_);
		} catch (SecurityException e)
		{
			JimmException f = new JimmException(119, 1, true);
			JimmException.handleException(f);
		}
		catch (Exception e) {}
		finally
		{
			if (istemp != null) try {istemp.close();} catch (Exception e) {}
			if (httemp != null) try {httemp.close();} catch (Exception e) {}
		}
		
		if (result != null)
		{
			int pos = result.indexOf("\r");
			if (pos != -1) pos = result.indexOf("\n");
			if (pos != -1) result = result.substring(0, pos); 
		}
		return result;
	}
	
	static public void internalReqLastVersThread()
	{
		Thread.yield();
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		if (version == null)
			version = readHttpContentFirstString("http://www.jimm.org/current_ver");
		
		if (betaVersion == null)
			betaVersion = readHttpContentFirstString("http://www.jimm.org/nightly/current-nightly");
		
		MainThread.showLastJimmVers();
	}
	
	static private void addVersString(String result, String name)
	{
		if (result == null) return;
		aboutTextList.addBigText(ResourceBundle.getString(name), aboutTextList.getTextColor(), Font.STYLE_PLAIN, -1);
		aboutTextList.addBigText(": ", aboutTextList.getTextColor(), Font.STYLE_PLAIN, -1);
		aboutTextList.addBigText(result, aboutTextList.getTextColor(), Font.STYLE_BOLD, -1);
		aboutTextList.doCRLF(-1);
	}
	
	static public void internalShowLastVers()
	{
		if (aboutTextList != null)
		{
			addVersString(version,     "version_stable");
			addVersString(betaVersion, "version_beta");
		}
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
		char chr;
		for (int i = 0; i < size; i++)
		{
			chr = text.charAt(i);
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

	static public final int INDEXED_COLOR_INCOMING = 0x01000000;
	static public final int INDEXED_COLOR_OUTGOING = 0x02000000;
	
	static public void setColorScheme(VirtualList vl, boolean setFullScreen, int theme, boolean changeFont)
	{
		if (vl == null) return;

		int cursorAlpha, menuAlpha;
		
//#sijapp cond.if target="DEFAULT"#
		cursorAlpha = 255;
		menuAlpha = 255;
//#sijapp cond.else#
		if (Jimm.display.numAlphaLevels() > 2)
		{
			cursorAlpha = 255-Options.getInt(Options.OPTION_CURSOR_ALPHA);
			menuAlpha = 255-Options.getInt(Options.OPTION_MENU_ALPHA);
		}
		else cursorAlpha = menuAlpha = 255; 
//#sijapp cond.end#		

		vl.setColors
		(
			Options.getSchemeColor(Options.CLRSCHHEME_CAP_TEXT, theme), 
			Options.getSchemeColor(Options.CLRSCHHEME_CAP, theme), 
			Options.getSchemeColor(Options.CLRSCHHEME_BACK, theme), 
			Options.getSchemeColor(Options.CLRSCHHEME_CURS, theme), 
			Options.getSchemeColor(Options.CLRSCHHEME_TEXT, theme),
			Options.getSchemeColor(Options.CLRSCHHEME_CURS_FRAME, theme), 
			cursorAlpha,
			menuAlpha
		);
		
		if (vl instanceof TextList)
		{
			TextList tl = (TextList)vl;
			tl.setIndexedColor(INDEXED_COLOR_INCOMING, Options.getSchemeColor(Options.CLRSCHHEME_INCOMING, theme));
			tl.setIndexedColor(INDEXED_COLOR_OUTGOING, Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING, theme));
		}
		
		if (setFullScreen) 
			vl.setFullScreen(Options.getBoolean(Options.OPTION_FULL_SCREEN));
		else
			vl.setFullScreen(false);
		
		if (changeFont)
			vl.setFontSize(Options.getBoolean(Options.OPTION_SMALL_FONT) ? Font.SIZE_SMALL : Font.SIZE_MEDIUM);
	}


	/************************************************************************/
	/************************************************************************/
	/************************************************************************/

	///////////////////
	//               //
	//    Hotkeys    //
	//               //
	///////////////////
	static public void execHotKey(ContactItem cItem, int keyCode,
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
			ContactItem item, int keyType)
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
				{
					requiestUserInfo
					(
						item.getStringValue(ContactItem.CONTACTITEM_UIN),
						item.getStringValue(ContactItem.CONTACTITEM_NAME),
						false
						//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
						,item.getBytesArray(ContactItem.CONTACTITEM_BUDDYICON_HASH)
						//  #sijapp cond.end#
					);
				}
				break;

			case Options.HOTKEY_NEWMSG:
				if (item != null) writeMessage(item, null);
				break;

			case Options.HOTKEY_USER_GROUPS:
				Options.setBoolean(Options.OPTION_USE_GROUPS, !Options.getBoolean(Options.OPTION_USE_GROUPS));
				Options.safeSave();
				ContactList.optionsChanged(true, false);
				ContactList.activateList();
				break;

			case Options.HOTKEY_ONOFF:
				if (Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE))
					Options.setBoolean(Options.OPTION_CL_HIDE_OFFLINE, false);
				else
					Options.setBoolean(Options.OPTION_CL_HIDE_OFFLINE, true);
				Options.safeSave();
				ContactList.optionsChanged(true, false);
				ContactList.activateList();
				break;

			case Options.HOTKEY_OPTIONS:
				Options.editOptions();
				break;

			case Options.HOTKEY_MENU:
				MainMenu.activateMenu();
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
				Options.safeSave();
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
				
			case Options.HOTKEY_REQ_SM:
				requestContactStatusMess(item);
				break;
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
	final public static int UI_UIN           = 0;
	final public static int UI_NICK          = 1;
	final public static int UI_NAME          = 2;
	final public static int UI_EMAIL         = 3;
	final public static int UI_CITY          = 4;
	final public static int UI_STATE         = 5;
	final public static int UI_PHONE         = 6;
	final public static int UI_FAX           = 7;
	final public static int UI_ADDR          = 8;
	final public static int UI_CPHONE        = 9;
	final public static int UI_AGE           = 10;
	final public static int UI_GENDER        = 11;
	final public static int UI_HOME_PAGE     = 12;
	final public static int UI_BDAY          = 13;
	final public static int UI_W_CITY        = 14;
	final public static int UI_W_STATE       = 15;
	final public static int UI_W_PHONE       = 16;
	final public static int UI_W_FAX         = 17;
	final public static int UI_W_ADDR        = 18;
	final public static int UI_W_NAME        = 19;
	final public static int UI_W_DEP         = 20;
	final public static int UI_W_POS         = 21;
	final public static int UI_ABOUT         = 22;
	final public static int UI_INETRESTS1_T  = 23;
	final public static int UI_INETRESTS1_V  = 24;
	final public static int UI_INETRESTS2_T  = 25;
	final public static int UI_INETRESTS2_V  = 26;
	final public static int UI_INETRESTS3_T  = 27;
	final public static int UI_INETRESTS3_V  = 28;
	final public static int UI_INETRESTS4_T  = 29;
	final public static int UI_INETRESTS4_V  = 30;
	final public static int UI_AUTH          = 31;
	final public static int UI_STATUS        = 32;
	final public static int UI_ICQ_CLIENT    = 33;
	final public static int UI_SIGNON        = 34;
	final public static int UI_ONLINETIME    = 35;
	final public static int UI_IDLE_TIME     = 36;
	final public static int UI_REG_DATE      = 37;
	final public static int UI_ICQ_VERS      = 38;
	final public static int UI_INT_IP        = 39;
	final public static int UI_EXT_IP        = 40;
	final public static int UI_PORT          = 41;
	final public static int UI_UIN_LIST      = 42;
	final public static int UI_FIRST_NAME    = 43;
	final public static int UI_LAST_NAME     = 44;
	final public static int UI_ONLINE_STATUS = 45;
	final public static int UI_XSTATUS       = 46;
	final public static int UI_CAPS          = 47;

	//////
	final public static int UI_LAST_ID = 48;

	static private int uiBigTextIndex;

	static private String uiSectName = null;
	
	static private void addToTextList(String str, String langStr, TextList list, boolean translate)
	{
		if (uiSectName != null)
		{
			list.addBigText(ResourceBundle.getString(uiSectName),
					list.getTextColor(), Font.STYLE_BOLD, -1).doCRLF(-1);
			uiSectName = null;
		}

		list.addBigText(
			(translate ? ResourceBundle.getString(langStr) : langStr) + ": ",
			list.getTextColor(), Font.STYLE_PLAIN, uiBigTextIndex)
		.addBigText(str,
				Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING, -1),
				Font.STYLE_PLAIN, uiBigTextIndex)
		.doCRLF(uiBigTextIndex);
		uiBigTextIndex++;
	}

	static private void addToTextList(int index, String[] data, String langStr,
			TextList list, boolean translate)
	{
		String str = data[index];
		if (str == null) return;
		if (str.length() == 0) return;
		addToTextList(str, langStr, list, translate);
	}
	
	static private void fillInterests(int type, int value, String[] data, TextList list)
	{
		String strType = data[type];
		String strValue = data[value];
		if (strType == null) return;
		if (strValue == null) strValue = new String();
		String interestName = (String)Icq.interests.get(strType);
		if (interestName == null) return;
		addToTextList(strValue, interestName, list, false);
	}

	static public void fillUserInfo(String[] data, TextList list)
	{
		uiSectName = "main_info";
		addToTextList(UI_UIN_LIST, data, "uin", list, true);
		addToTextList(UI_NICK, data, "nick", list, true);
		addToTextList(UI_NAME, data, "name", list, true);
		addToTextList(UI_GENDER, data, "gender", list, true);
		addToTextList(UI_AGE, data, "age", list, true);
		addToTextList(UI_EMAIL, data, "email", list, true);
		if (data[UI_AUTH] != null)
			addToTextList(data[UI_AUTH].equals("1") ? ResourceBundle
					.getString("yes") : ResourceBundle.getString("no"), "auth",
					list, true);
		addToTextList(UI_BDAY, data, "birth_day", list, true);
		addToTextList(UI_CPHONE, data, "cell_phone", list, true);
		addToTextList(UI_HOME_PAGE, data, "home_page", list, true);
		addToTextList(UI_ABOUT, data, "notes", list, true);

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
					.addImage(JimmUI.imageList.elementAt(imgIndex),
							null, uiBigTextIndex).doCRLF(uiBigTextIndex);
			uiBigTextIndex++;
		}
		
		uiSectName = "interests";
		fillInterests(UI_INETRESTS1_T, UI_INETRESTS1_V, data, list);
		fillInterests(UI_INETRESTS2_T, UI_INETRESTS2_V, data, list);
		fillInterests(UI_INETRESTS3_T, UI_INETRESTS3_V, data, list);
		fillInterests(UI_INETRESTS4_T, UI_INETRESTS4_V, data, list);

		uiSectName = "home_info";
		addToTextList(UI_CITY, data, "city", list, true);
		addToTextList(UI_STATE, data, "state", list, true);
		addToTextList(UI_ADDR, data, "addr", list, true);
		addToTextList(UI_PHONE, data, "phone", list, true);
		addToTextList(UI_FAX, data, "fax", list, true);

		uiSectName = "work_info";
		addToTextList(UI_W_NAME, data, "title", list, true);
		addToTextList(UI_W_DEP, data, "depart", list, true);
		addToTextList(UI_W_POS, data, "position", list, true);
		addToTextList(UI_W_CITY, data, "city", list, true);
		addToTextList(UI_W_STATE, data, "state", list, true);
		addToTextList(UI_W_ADDR, data, "addr", list, true);
		addToTextList(UI_W_PHONE, data, "phone", list, true);
		addToTextList(UI_W_FAX, data, "fax", list, true);

		uiSectName = "icq_client";
		addToTextList(UI_ICQ_CLIENT, data, "icq_client", list, true);
		addToTextList(UI_CAPS, data, "cli_caps", list, true);
		
		uiSectName = "dc_info";
		addToTextList(UI_ONLINE_STATUS, data, "status", list, true);
		addToTextList(UI_XSTATUS, data, "xstatus", list, true);
		addToTextList(UI_REG_DATE, data, "li_reg_date", list, true);
		addToTextList(UI_SIGNON, data, "li_signon_time", list, true);
		addToTextList(UI_ONLINETIME, data, "li_online_time", list, true);
		addToTextList(UI_IDLE_TIME, data, "li_idle_time", list, true);
		addToTextList(UI_ICQ_VERS, data, "ICQ version", list, true);
		addToTextList(UI_INT_IP, data, "Int IP", list, true);
		addToTextList(UI_EXT_IP, data, "Ext IP", list, true);
		addToTextList(UI_PORT, data, "Port", list, true);
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	static private TextList infoTextList = null;

	//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
	static public void updateActiveUserInfo(String uin)
	{
		if (infoTextList != null)
		{
			if (infoTextList.isActive())
			{
				try
				{
					ContactItem cItem = ContactList.getItembyUIN(uin);
					Image image = cItem.getImage(ContactItem.CONTACTITEM_BUDDYICON);
					if (image != null)
					{
						infoTextList.insertImage(image, null, -1, 0).doCRLF(-1);
						infoTextList.repaint();
					}
					image = null;
				} catch (Exception ignore) {/* Do nothing */}
			}
		}
	}
	//  #sijapp cond.end#

	static public void requiestUserInfo(String uin, String name, boolean allowToEdit
		//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
		, byte[] hash
		//  #sijapp cond.end#
		)
	{
		infoTextList = getInfoTextList(uin, false);
		infoTextList.setCommandListener(_this);

		if (Icq.isConnected())
		{
			if (allowToEdit)
				infoTextList.addCommandEx(cmdEdit, VirtualList.MENU_TYPE_RIGHT);

			//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
			if (hash != null) {
				ContactItem cItem = ContactList.getItembyUIN(uin);
				if (cItem.iconReady())
				{
					RequestBuddyIconAction act = new RequestBuddyIconAction(uin,hash);
					try
					{
						Icq.requestAction(act);
					} catch (JimmException e)
					{
						System.out.println ("Exception!");
							JimmException.handleException(e);
						if (e.isCritical())
							return;
					}
					SplashCanvas.addTimerTask(act);
				}
			}
			//  #sijapp cond.end#

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
			data[JimmUI.UI_UIN] = uin;
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
		//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
		try
		{
			ContactItem cItem = ContactList.getItembyUIN(data[JimmUI.UI_UIN]);
			Image image = cItem.getImage(ContactItem.CONTACTITEM_BUDDYICON);
			if (image != null) infoTextList.insertImage(image, null, -1, 0).doCRLF(-1);
			image = null;
		} catch (Exception ignore) {/*Do nothing*/}
		//  #sijapp cond.end#

		JimmUI.fillUserInfo(data, infoTextList);
		infoTextList.removeCommandEx(cmdCancel);
		infoTextList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
//#sijapp cond.if target!="RIM"#		
		infoTextList.addCommandEx(cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#		
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

		JimmUI.setColorScheme(infoTextList, false, -1, true);
		infoTextList.setMode(VirtualList.CURSOR_MODE_DISABLED);

		if (addCommands)
		{
			infoTextList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
//#sijapp cond.if target!="RIM"#
			infoTextList.addCommandEx(cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#			
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

	static private TextList lstSelector;

	static private int lastSelectedItemIndex;

	static public void showSelector(String caption, String[] elements,
			CommandListener listener, int tag, boolean translateWords)
	{
		curScreenTag = tag;
		lstSelector = new TextList (ResourceBundle.getString(caption));
		JimmUI.setColorScheme(lstSelector, false, -1, true);
		lstSelector.setCyclingCursor(true);
		lstSelector.setMode(VirtualList.CURSOR_MODE_DISABLED);
		lstSelector.setFontSize(Font.SIZE_LARGE);
		for (int i = 0; i < elements.length; i++) JimmUI.addTextListItem(lstSelector, elements[i], null, i, translateWords, -1, Font.STYLE_PLAIN);
		lstSelector.addCommandEx(cmdOk, VirtualList.MENU_TYPE_RIGHT_BAR);
		lstSelector.addCommandEx(cmdCancel, VirtualList.MENU_TYPE_LEFT_BAR);
		lstSelector.setCommandListener(_this);
		JimmUI.listener = listener;
		lstSelector.activate(Jimm.display);
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
		//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #
		Emotions.addTextWithEmotions(textList, text, Font.STYLE_PLAIN, color, messTotalCounter);
		//#sijapp cond.else#
		//#		textList.addBigText(text, textList.getTextColor(), Font.STYLE_PLAIN, messTotalCounter);
		//#sijapp cond.end#
		textList.doCRLF(messTotalCounter);
	}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
	
	final public static ImageList imageList     = new ImageList();
	final public static ImageList xStatusImages = new ImageList();
	
	final public static Image statusEvilImg;
	final public static Image statusDepressionImg;
	final public static Image statusHomeImg;
	final public static Image statusWorkImg;
	final public static Image statusLunchImg;
	final public static Image statusAwayImg;
	final public static Image statusChatImg;
	final public static Image statusDndImg;
	final public static Image statusInvisibleImg;
	final public static Image statusNaImg;
	final public static Image statusOccupiedImg;
	final public static Image statusOfflineImg;
	final public static Image statusOnlineImg;
	final public static Image eventPlainMessageImg;
	final public static Image eventUrlMessageImg;
	final public static Image eventSystemNoticeImg;
	final public static Image eventSysActionImg;
	final public static Image imgTyping;
	final public static Image imgMessDeliv;
	final public static Image imgNoAuth;
	
	final public static StatusInfo[] statusInfos;
	
	static
	{
		try
		{
			/* reads and divides image "icons.png" to several icons */
//#sijapp cond.if target="MIDP2" | target="SIEMENS2"#
			imageList.setScale(Options.getInt(Options.OPTION_IMG_SCALE));
//#sijapp cond.end#
			imageList.load("/icons.png", -1, -1, -1, Jimm.getPhoneVendor() == Jimm.PHONE_NOKIA);
		} 
		catch (Exception e) {}
		
		statusEvilImg        = imageList.elementAt(8);
		statusDepressionImg  = imageList.elementAt(9);
		statusHomeImg        = imageList.elementAt(10);
		statusWorkImg        = imageList.elementAt(11);
		statusLunchImg       = imageList.elementAt(12);
		statusAwayImg        = imageList.elementAt(0);
		statusChatImg        = imageList.elementAt(1);
		statusDndImg         = imageList.elementAt(2);
		statusInvisibleImg   = imageList.elementAt(3);
		statusNaImg          = imageList.elementAt(4);
		statusOccupiedImg    = imageList.elementAt(5);
		statusOfflineImg     = imageList.elementAt(6);
		statusOnlineImg      = imageList.elementAt(7);
		eventPlainMessageImg = imageList.elementAt(13);
		eventUrlMessageImg   = imageList.elementAt(14);
		eventSystemNoticeImg = imageList.elementAt(15);
		eventSysActionImg    = imageList.elementAt(16);
		imgTyping            = imageList.elementAt(17);
		imgMessDeliv         = imageList.elementAt(18);
		imgNoAuth            = imageList.elementAt(19);
		
		
		try
		{
//#sijapp cond.if target="MIDP2" | target="SIEMENS2"#
			xStatusImages.setScale(Options.getInt(Options.OPTION_IMG_SCALE));
//#sijapp cond.end#
			xStatusImages.load("/xstatus.png", -1, -1, -1, Jimm.getPhoneVendor() == Jimm.PHONE_NOKIA);
		} catch (Exception e) {}
		
		
		Vector data = new Vector(); 
		
		/* Normal statuses */
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_ONLINE,     "status_online",     statusOnlineImg,     StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_CHAT,       "status_chat",       statusChatImg,       StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_AWAY,       "status_away",       statusAwayImg,       StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_NA,         "status_na",         statusNaImg,         StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_OCCUPIED,   "status_occupied",   statusOccupiedImg,   StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_DND,        "status_dnd",        statusDndImg,        StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_INVISIBLE,  "status_invisible",  statusInvisibleImg,  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_INVIS_ALL,  "status_invis_all",  statusInvisibleImg,  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_EVIL,       "status_evil",       statusEvilImg,       StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_DEPRESSION, "status_depression", statusDepressionImg, StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_HOME,       "status_home",       statusHomeImg,       StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_WORK,       "status_work",       statusWorkImg,       StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_LUNCH,      "status_lunch",      statusLunchImg,      StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_STATUS, ContactList.STATUS_OFFLINE,    "status_offline",    statusOfflineImg,    0);
		
		/* Extended statuses */
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS,   -2, "xstatus_none",            null,                        StatusInfo.FLAG_IN_MENU);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x17, "xstatus_angry",           xStatusImages.elementAt(0),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x01, "xstatus_duck",            xStatusImages.elementAt(1),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x02, "xstatus_tired",           xStatusImages.elementAt(2),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x03, "xstatus_party",           xStatusImages.elementAt(3),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x04, "xstatus_beer",            xStatusImages.elementAt(4),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x05, "xstatus_thinking",        xStatusImages.elementAt(5),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x06, "xstatus_eating",          xStatusImages.elementAt(6),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x07, "xstatus_tv",              xStatusImages.elementAt(7),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x08, "xstatus_friends",         xStatusImages.elementAt(8),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x09, "xstatus_coffee",          xStatusImages.elementAt(9),  StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x0A, "xstatus_music",           xStatusImages.elementAt(10), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x0B, "xstatus_business",        xStatusImages.elementAt(11), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x0C, "xstatus_camera",          xStatusImages.elementAt(12), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x0D, "xstatus_funny",           xStatusImages.elementAt(13), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x0E, "xstatus_phone",           xStatusImages.elementAt(14), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x0F, "xstatus_games",           xStatusImages.elementAt(15), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x10, "xstatus_college",         xStatusImages.elementAt(16), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
		addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x00, "xstatus_shopping",        xStatusImages.elementAt(17), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x11, "xstatus_sick",            xStatusImages.elementAt(18), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x12, "xstatus_sleeping",        xStatusImages.elementAt(19), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x13, "xstatus_surfing",         xStatusImages.elementAt(20), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x14, "xstatus_internet",        xStatusImages.elementAt(21), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x15, "xstatus_engineering",     xStatusImages.elementAt(22), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x16, "xstatus_typing",          xStatusImages.elementAt(23), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_STD|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x18, "xstatus_unk",             xStatusImages.elementAt(24), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x19, "xstatus_ppc",             xStatusImages.elementAt(25), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x1A, "xstatus_mobile",          xStatusImages.elementAt(26), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x1B, "xstatus_man",             xStatusImages.elementAt(27), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x1C, "xstatus_wc",              xStatusImages.elementAt(28), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x1D, "xstatus_question",        xStatusImages.elementAt(29), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x1E, "xstatus_way",             xStatusImages.elementAt(30), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x1F, "xstatus_heart",           xStatusImages.elementAt(31), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x20, "xstatus_cigarette",       xStatusImages.elementAt(32), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x21, "xstatus_sex",             xStatusImages.elementAt(33), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x22, "xstatus_rambler_search",  xStatusImages.elementAt(34), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
        addStatusInfo(data, StatusInfo.TYPE_X_STATUS, 0x23, "xstatus_rambler_journal", xStatusImages.elementAt(35), StatusInfo.FLAG_IN_MENU|StatusInfo.FLAG_HAVE_DESCR);
		
		statusInfos = new StatusInfo[data.size()];
		data.copyInto(statusInfos);
	}
	
	public static StatusInfo findStatus(int type, int value)
	{
		for (int i = 0; i < statusInfos.length; i++)
		{
			StatusInfo info = statusInfos[i];
			if (info.getType() == type && info.getValue() == value) return info; 
		}
		return null;
	}
	
	private static void addStatusInfo(Vector vct, int type, int value, String text, Image image, int flags)
	{
		vct.addElement(new StatusInfo(type, value, ResourceBundle.getString(text), image, flags));
	}
	
	public static final int SHOW_STATUSES_NAME  = 1 << 0;
	public static final int SHOW_STATUSES_DESCR = 1 << 1;
	
	public static void fillStatusesInList(TextList list, int type, int flags, int showFlags)
	{
		boolean showDescr = (showFlags&SHOW_STATUSES_DESCR) != 0;
		boolean showName  = (showFlags&SHOW_STATUSES_NAME)  != 0;
		
		for (int i = 0; i < statusInfos.length; i++)
		{
			StatusInfo info = statusInfos[i];
			if (info.getType() != type) continue;
			if ((info.getFlags()&flags) == 0) continue;
			
			if (showName)
			{
				addTextListItem
				(
					list, 
					info.getText(), 
					info.getImage(), 
					info.getValue(), 
					true, 
					-1, 
					showDescr||((info.getFlags()&StatusInfo.FLAG_STD) != 0) ? Font.STYLE_BOLD : Font.STYLE_PLAIN
				);
			}
			
			if (showDescr)
			{
				int value = info.getValue();
				String descr = Options.getStatusString(type, value);
				if (descr == null) descr = info.getText();
				addTextListItem
				(
					list, 
					descr, 
					(!showName) ? info.getImage() : null, 
					info.getValue(), 
					true, 
					-1, 
					(!showName)&&((info.getFlags()&StatusInfo.FLAG_STD) != 0) ? Font.STYLE_BOLD : Font.STYLE_PLAIN
				);
			}
		}
	}

	
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
	
	
	public static final int SHS_TYPE_ALL = 1;
	public static final int SHS_TYPE_EMPTY = 2;
	
	public static TextList showGroupSelector(String caption, CommandListener listener, int type, int excludeGroupId)
	{
		TextList tlGroups = new TextList (ResourceBundle.getString(caption));
		
		tlGroups.lock();
		JimmUI.setColorScheme(tlGroups, false, -1, true);
		tlGroups.setMode(VirtualList.CURSOR_MODE_DISABLED);
		
		tlGroups.addCommandEx(cmdOk, VirtualList.MENU_TYPE_RIGHT_BAR);
		tlGroups.addCommandEx(cmdCancel, VirtualList.MENU_TYPE_LEFT_BAR);
		
		ContactListGroupItem[] groups = ContactList.getGroupItems();
		
		boolean found = false;
		int id;
		for (int i = 0; i < groups.length; i++)
		{
			id = groups[i].getId();
			if ((id == excludeGroupId) || (id == 0x0000)) continue;
			
			if (type == SHS_TYPE_EMPTY)
			{
				ContactItem[] cItems = ContactList.getGroupItems(id);
				if (cItems.length != 0)continue;
			}
			
			addTextListItem(tlGroups, groups[i].getName(), null, id, false, -1, Font.STYLE_PLAIN);
			found = true;
		}
		
		if (!found)
			addTextListItem(tlGroups, "No groups found", null, -1, false, -1, Font.STYLE_PLAIN);
		
		tlGroups.setCommandListener(listener);
		tlGroups.activate(Jimm.display);
		
		tlGroups.unlock();

		return tlGroups;
	}

	public static int[] showGroupSelector(String caption, int tag,
			CommandListener listener, int type, int excludeGroupId)
	{
		ContactListGroupItem[] groups = ContactList.getGroupItems();
		String[] groupNamesTmp = new String[groups.length];
		int[] groupIdsTmp = new int[groups.length];

		int index = 0;
		int groupId;
		ContactItem[] cItems;
		for (int i = 0; i < groups.length; i++)
		{
			groupId = groups[i].getId();
			if (groupId == excludeGroupId)
				continue;
			switch (type)
			{
			case SHS_TYPE_EMPTY:
				cItems = ContactList.getGroupItems(groupId);
				if (cItems.length != 0)
					continue;
				break;
			}

			groupIdsTmp[index] = groupId;
			groupNamesTmp[index] = groups[i].getName();
			index++;
		}
		cItems = null;

		if (index == 0)
		{
			Alert alert = new Alert("", ResourceBundle.getString("no_availible_groups"), null, AlertType.INFO);
			alert.setTimeout(Alert.FOREVER);
			Jimm.display.setCurrent(alert);
			Jimm.setBkltOn(false);
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
	
	public static void addTextListItem(TextList list, String text, Image image, int value, boolean translate, int textColor, int fontStyle)
	{
		if (image != null) list.addImage(image, null, value);
		String textToAdd = translate ? ResourceBundle.getString(text) : text;
		if (textColor == -1) textColor = list.getTextColor();
		else if (textColor == -2) textColor = Options.getSchemeColor(Options.CLRSCHHEME_OUTGOING, -1);
		if (fontStyle == -1) fontStyle = Font.STYLE_PLAIN;
		list.addBigText((image != null) ? (" "+textToAdd) : textToAdd, textColor, fontStyle, value);
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
	private static ContactItem textMessReceiver;
	
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
	public static void writeMessage(ContactItem receiver, String initText)
	{
		String formCap = Options.getBoolean(Options.OPTION_FULL_TEXTBOX) ? null
				 : receiver.getStringValue(ContactItem.CONTACTITEM_NAME)+" - "+ResourceBundle.getString("message");
		
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		if (messageTextbox == null)
			messageTextbox = new TextBox(formCap, null, MAX_EDITOR_TEXT_SIZE, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
		else
			messageTextbox.setTitle(formCap);

//#sijapp cond.else#
		messageTextbox = new TextBox(formCap, null, MAX_EDITOR_TEXT_SIZE, TextField.ANY);
//#sijapp cond.end#
		
		textMessReceiver = receiver;
		textMessCurMode = EDITOR_MODE_MESSAGE;
		
		removeTextMessageCommands();
		messageTextbox.addCommand(cmdSend);
		messageTextbox.addCommand(cmdCancel);
		messageTextbox.addCommand(cmdClearText);

		//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #
		messageTextbox.addCommand(cmdInsertEmo);
		//#sijapp cond.end#
		
		messageTextbox.addCommand(cmdInsTemplate);
		
		if (initText != null) messageTextbox.setString(initText);
		messageTextbox.setCommandListener(_this);
		Jimm.display.setCurrent(messageTextbox);
		Jimm.setBkltOn(true);
		
		sendTypeingNotify(true);
	}
	
	private static void sendTypeingNotify(boolean value)
	{
		//#sijapp cond.if target isnot "DEFAULT"#
		if 
		(      (Options.getInt(Options.OPTION_TYPING_MODE) > 0)
			&& textMessReceiver.hasCapability(Icq.CAPF_TYPING)
			&& ((Options.getLong(Options.OPTION_ONLINE_STATUS) != ContactList.STATUS_INVISIBLE)
			&& (Options.getLong(Options.OPTION_ONLINE_STATUS) != ContactList.STATUS_INVIS_ALL))
		){
			try
			{
				System.out.println("sendTypeingNotify()");
				Icq.beginTyping(textMessReceiver.getStringValue(ContactItem.CONTACTITEM_UIN), value);
			} catch (JimmException e)
			{}
		}
		//#sijapp cond.end#
	}

	public static void sendMessage(String text, ContactItem textMessReceiver)
	{
		/* Construct plain message object and request new SendMessageAction
		 Add the new message to the chat history */

		if (text == null)
			return;
		if (text.length() == 0)
			return;

		PlainMessage plainMsg = new PlainMessage(Options.getString(Options.OPTION_UIN), textMessReceiver, Message.MESSAGE_TYPE_NORM, Util.createCurrentDate(false), text);

		SendMessageAction sendMsgAct = new SendMessageAction(plainMsg);
		
		int messId = sendMsgAct.getMessId();
		ChatHistory.addMyMessage(textMessReceiver, text, plainMsg.getNewDate(), textMessReceiver.getStringValue(ContactItem.CONTACTITEM_NAME), messId);
		
		try
		{
			Icq.requestAction(sendMsgAct);
		} catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical())
				return;
		}

		//#sijapp cond.if modules_HISTORY is "true" #
		if (Options.getBoolean(Options.OPTION_HISTORY))
			HistoryStorage.addText(textMessReceiver.getStringValue(ContactItem.CONTACTITEM_UIN),
					text, (byte) 1, ResourceBundle.getString("me"), plainMsg
							.getNewDate());
		//#sijapp cond.end#
	}
	
	/////////////////////////////////////////////////////

	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	private static TextList URLList;
	
	public static void gotoURL(String msg)
	{
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
			URLList.addCommandEx(cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
			URLList.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
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
	private static ContactItem authContactItem;

	public static final int AUTH_TYPE_DENY = 10001;
	public static final int AUTH_TYPE_REQ_AUTH = 10002;
	
	public static void authMessage(int authType, ContactItem contactItem, String caption, String text)
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
		Jimm.setBkltOn(true);
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
	private static final int USER_MENU_MOVE_TO_GROUP = 15;
	private static final int USER_MENU_TO_IGN_LIST = 16;
	private static final int USER_MENU_REM_IGN_LIST = 17;
	private static final int USER_MENU_TO_INV_LIST = 18;
	private static final int USER_MENU_REM_INV_LIST = 19;
	private static final int USER_MENU_TO_VIS_LIST = 20;
	private static final int USER_MENU_REM_VIS_LIST = 21;
	private static final int USER_MENU_FILE_TRANS_LINK = 22;
	
	private static TextList tlContactMenu;
	private static ContactItem clciContactMenu;
	private static TextList removeContactMessageBox;
	private static TextList removeMeMessageBox;
	private static TextBox renameTextbox;
	
	public static boolean isContactMenuActive(ContactItem contact)
	{
		return (clciContactMenu == contact && isControlActive(tlContactMenu));
	}
	
	public static void showContactMenu(ContactItem contact)
	{
		clciContactMenu = contact;
	
		tlContactMenu = new TextList(ResourceBundle.getString("user_menu"));
		JimmUI.setColorScheme(tlContactMenu, false, -1, true);
		tlContactMenu.setMode(VirtualList.CURSOR_MODE_DISABLED);
		tlContactMenu.activate(Jimm.display);
		tlContactMenu.addCommandEx(cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
		tlContactMenu.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
		tlContactMenu.setCommandListener(_this);
		tlContactMenu.setAlwaysShowCursor(true);
		
		long status = contact.getIntValue(ContactItem.CONTACTITEM_STATUS);
		
		addTextListItem(tlContactMenu, "group_message", null, -1, true, -2, Font.STYLE_BOLD);
		
		if (Icq.isConnected())
		{
			addTextListItem(tlContactMenu, "send_message", null, USER_MENU_MESSAGE, true, -1, Font.STYLE_PLAIN);
			
			if (JimmUI.getClipBoardText() != null)
				addTextListItem(tlContactMenu, "quote", null, USER_MENU_QUOTA, true, -1, Font.STYLE_PLAIN);
			
			if (contact.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH))
				addTextListItem(tlContactMenu, "requauth", null, USER_MENU_REQU_AUTH, true, -1, Font.STYLE_PLAIN);
		}
		
		//#sijapp cond.if modules_HISTORY is "true" #
		addTextListItem(tlContactMenu, "history", null, USER_MENU_HISTORY, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#
		
		addTextListItem(tlContactMenu, "group_info", null, -1, true, -2, Font.STYLE_BOLD);
		
		addTextListItem(tlContactMenu, "info", null, USER_MENU_USER_INFO, true, -1, Font.STYLE_PLAIN);
		if (status != ContactList.STATUS_OFFLINE)
			addTextListItem(tlContactMenu, "dc_info", null, USER_MENU_LOCAL_INFO, true, -1, Font.STYLE_PLAIN);
		
		if (Icq.isConnected())
		{
			StatusInfo si = JimmUI.findStatus(StatusInfo.TYPE_STATUS, (int)status);
			if (si != null && si.testFlag(StatusInfo.FLAG_HAVE_DESCR))
				addTextListItem(tlContactMenu, "reqstatmsg", null, USER_MENU_STATUS_MESSAGE, true, -1, Font.STYLE_PLAIN);		
			
//#sijapp cond.if (target="MIDP2"|target="MOTOROLA"|target="SIEMENS2"|target="RIM")&modules_FILES="true"#
			addTextListItem(tlContactMenu, "ft_caption", null, -1, true, -2, Font.STYLE_BOLD);
			if (((status != ContactList.STATUS_OFFLINE) 
					&& contact.getIntValue(ContactItem.CONTACTITEM_ICQ_PROT) >= 8) ||
					(Options.getInt(Options.OPTION_FT_MODE) == Options.FS_MODE_WEB))
			{
				addTextListItem(tlContactMenu, "ft_name", null, USER_MENU_FILE_TRANS, true, -1, Font.STYLE_PLAIN);
				
				if (lastFileTransferLink != null)
					addTextListItem(tlContactMenu, "ft_link", null, USER_MENU_FILE_TRANS_LINK, true, -1, Font.STYLE_PLAIN); 
				
//#sijapp cond.if target isnot "MOTOROLA"#
				if (System.getProperty("video.snapshot.encodings") != null)
				{
					addTextListItem(tlContactMenu, "ft_cam", null, USER_MENU_CAM_TRANS, true, -1, Font.STYLE_PLAIN);
				}
//#sijapp cond.end#
			}
//#sijapp cond.end#
			
			addTextListItem(tlContactMenu, "group_lists", null, -1, true, -2, Font.STYLE_BOLD);
			addTextListItem(tlContactMenu, "remove", null, USER_MENU_USER_REMOVE, true, -1, Font.STYLE_PLAIN);
			addTextListItem(tlContactMenu, "remove_me", null, USER_MENU_REMOVE_ME, true, -1, Font.STYLE_PLAIN);
			if (contact.getIntValue(ContactItem.CONTACTITEM_GROUP) != 0x0000) {
				addTextListItem(tlContactMenu, "rename", null, USER_MENU_RENAME, true, -1, Font.STYLE_PLAIN);
				addTextListItem(tlContactMenu, "move_to_group", null, USER_MENU_MOVE_TO_GROUP, true, -1, Font.STYLE_PLAIN);
			}
			if (contact.getIntValue(ContactItem.CONTACTITEM_IGN_ID) != 0)
				addTextListItem(tlContactMenu, "privacy_rem_ign", null, USER_MENU_REM_IGN_LIST, true, -1, Font.STYLE_PLAIN);
			else 
				addTextListItem(tlContactMenu, "privacy_to_ign", null, USER_MENU_TO_IGN_LIST, true, -1, Font.STYLE_PLAIN);
		
			if (contact.getIntValue(ContactItem.CONTACTITEM_INV_ID) != 0)
				addTextListItem(tlContactMenu, "privacy_rem_inv", null, USER_MENU_REM_INV_LIST, true, -1, Font.STYLE_PLAIN);
			else 
				addTextListItem(tlContactMenu, "privacy_to_inv", null, USER_MENU_TO_INV_LIST, true, -1, Font.STYLE_PLAIN);

			if (contact.getIntValue(ContactItem.CONTACTITEM_VIS_ID) != 0)
				addTextListItem(tlContactMenu, "privacy_rem_vis", null, USER_MENU_REM_VIS_LIST, true, -1, Font.STYLE_PLAIN);
			else 
				addTextListItem(tlContactMenu, "privacy_to_vis", null, USER_MENU_TO_VIS_LIST, true, -1, Font.STYLE_PLAIN);
		}
		
		tlContactMenu.selectFirstTextIndex();
		
		JimmUI.setLastScreen(contact, false);
	}
	
	private static int[] groupList;
	
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
			requestContactStatusMess(clciContactMenu);
			break;

			//#sijapp cond.if target="MIDP2" | target="MOTOROLA" | target="SIEMENS2" | target="RIM"#
			//#sijapp cond.if modules_FILES is "true"#
			case USER_MENU_FILE_TRANS:
				/* Send a filetransfer with a file given by path */
				{
					FileTransfer ft = new FileTransfer(FileTransfer.FT_TYPE_FILE_BY_NAME, clciContactMenu);
					ft.startFT();
				}
				break;
				
			case USER_MENU_FILE_TRANS_LINK:
				sendMessage(lastFileTransferLink, clciContactMenu);
				ChatHistory.activateIfExists(clciContactMenu);
				break;

			//#sijapp cond.if target isnot "MOTOROLA" #
			case USER_MENU_CAM_TRANS:
				/* Send a filetransfer with a camera image
				 We can only make file transfers with ICQ clients prot V8 and up */
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
					ResourceBundle.getString("remove") + " " + clciContactMenu.getStringValue(ContactItem.CONTACTITEM_NAME) + "?", 
					JimmUI.MESBOX_OKCANCEL
				);
				break;
				
			case USER_MENU_REMOVE_ME: /* Remove me from other users contact list */
				removeMeMessageBox = showMessageBox
				(
					ResourceBundle.getString("remove_me") + "?",
					ResourceBundle.getString("remove_me_from") + clciContactMenu.getStringValue(ContactItem.CONTACTITEM_NAME) + "?", 
					JimmUI.MESBOX_OKCANCEL
				); 
				break;
				
			case USER_MENU_RENAME:
				renameTextbox = new TextBox
				(
					ResourceBundle.getString("rename"),
					clciContactMenu.getStringValue(ContactItem.CONTACTITEM_NAME),
					64,
					TextField.ANY
				);
				renameTextbox.addCommand(cmdOk);
				renameTextbox.addCommand(cmdCancel);
				renameTextbox.setCommandListener(_this);
				Jimm.display.setCurrent(renameTextbox);
				Jimm.setBkltOn(true);
				break;
				
			case USER_MENU_USER_INFO:
				JimmUI.requiestUserInfo
				(
					clciContactMenu.getStringValue(ContactItem.CONTACTITEM_UIN), 
					clciContactMenu.getStringValue(ContactItem.CONTACTITEM_NAME), 
					false
					//#sijapp cond.if target!="DEFAULT" & modules_AVATARS="true"#
					,clciContactMenu.getBytesArray(ContactItem.CONTACTITEM_BUDDYICON_HASH)
					//  #sijapp cond.end#
				);
				break;
				
			case USER_MENU_LOCAL_INFO:
				showClientInfo(clciContactMenu);
				break;
				
			case USER_MENU_MOVE_TO_GROUP:
				groupList = showGroupSelector
				(
					"move_to_group", 
					GROUP_SELECTOR_MOVE_TAG, 
					_this, 
					SHS_TYPE_ALL, 
					clciContactMenu.getIntValue(ContactItem.CONTACTITEM_GROUP)
				);
				break;
				
			case USER_MENU_TO_IGN_LIST:
			case USER_MENU_REM_IGN_LIST:
			case USER_MENU_TO_INV_LIST:
			case USER_MENU_REM_INV_LIST:
			case USER_MENU_TO_VIS_LIST:
			case USER_MENU_REM_VIS_LIST:
				processPrivateRequest(index);
				break;
				
			//#sijapp cond.if modules_HISTORY is "true" #
			case USER_MENU_HISTORY:
				HistoryStorage.showHistoryList
				(
					clciContactMenu.getStringValue(ContactItem.CONTACTITEM_UIN), 
					clciContactMenu.getStringValue(ContactItem.CONTACTITEM_NAME)
				);
				break;
			//#sijapp cond.end#
		}
	}
	
	private static void processPrivateRequest(int commandId)
	{
		int ingnoreId = clciContactMenu.getIntValue(ContactItem.CONTACTITEM_IGN_ID);
		int visibleId = clciContactMenu.getIntValue(ContactItem.CONTACTITEM_VIS_ID);
		int invisId = clciContactMenu.getIntValue(ContactItem.CONTACTITEM_INV_ID);
		int ingnoreNew = ingnoreId;
		int visibleNew = visibleId;
		int invisNew = invisId;
		String uin = clciContactMenu.getStringValue(ContactItem.CONTACTITEM_UIN);
		
		switch (commandId)
		{
		case USER_MENU_TO_IGN_LIST:
			ingnoreNew = ContactList.generateNewIdForBuddy();
			break;
			
		case USER_MENU_REM_IGN_LIST:
			ingnoreNew = 0;
			break;
			
		case USER_MENU_TO_INV_LIST:
			invisNew = ContactList.generateNewIdForBuddy();
			visibleNew = 0;
			break;
			
		case USER_MENU_REM_INV_LIST:
			invisNew = 0;
			break;
			
		case USER_MENU_TO_VIS_LIST:
			visibleNew = ContactList.generateNewIdForBuddy();
			invisNew = 0;
			break;
			
		case USER_MENU_REM_VIS_LIST:
			visibleNew = 0;
			break;
		}
		
		try
		{
			Icq.sendCLI_ADDSTART();
			processPrivateChange(ingnoreId, ingnoreNew, 0x000E, uin);
			processPrivateChange(visibleId, visibleNew, 0x0002, uin);
			processPrivateChange(invisId, invisNew, 0x0003, uin);
			Icq.sendCLI_ADDEND();
			
			clciContactMenu.setIntValue(ContactItem.CONTACTITEM_IGN_ID, ingnoreNew);
			clciContactMenu.setIntValue(ContactItem.CONTACTITEM_VIS_ID, visibleNew);
			clciContactMenu.setIntValue(ContactItem.CONTACTITEM_INV_ID, invisNew);
			
			ContactList.safeSave();
			JimmUI.backToLastScreen();
		}
		catch (JimmException e)
		{
			try { Icq.sendCLI_ADDEND(); } catch (Exception e2) {}
			JimmException.handleException(e);
		}
	}
	
	private static void processPrivateChange(int oldId, int newId, int type, String uin) throws JimmException
	{
		if (oldId == newId) return;
		if ((oldId != 0) && (newId != 0)) throw new JimmException(230, 1);
		if (oldId != 0) Icq.sendProcessBuddy(Icq.PROCESS_BUDDY_DELETE, uin, oldId, 0, type);
		if (newId != 0) Icq.sendProcessBuddy(Icq.PROCESS_BUDDY_ADD, uin, newId, 0, type);
	}
	
	public static void showClientInfo(ContactItem cItem)
	{
		StatusInfo statusInfo;
		
		TextList tlist = JimmUI.getInfoTextList(
				cItem.getStringValue(ContactItem.CONTACTITEM_UIN), true);
		String[] clInfoData = new String[JimmUI.UI_LAST_ID];

		/* Status */
		statusInfo = JimmUI.findStatus(StatusInfo.TYPE_STATUS, cItem.getIntValue(ContactItem.CONTACTITEM_STATUS));
		clInfoData[JimmUI.UI_ONLINE_STATUS] = (statusInfo != null) ? statusInfo.getText() : null;
		
		/* X-status */
		int xStatus = cItem.getIntValue(ContactItem.CONTACTITEM_XSTATUS);
		statusInfo = JimmUI.findStatus(StatusInfo.TYPE_X_STATUS, xStatus);
		String rcvdXMsg = cItem.getStringValue(ContactItem.CONTACTITEM_XSTATUSMSG);

		String stText = (statusInfo != null) ? ResourceBundle.getString(statusInfo.getText()) : "";

		if (rcvdXMsg != null)
		{
			clInfoData[JimmUI.UI_XSTATUS] = ((rcvdXMsg.length() > 0) ? stText+" ("+rcvdXMsg+")" : stText);
		}
		else
		{
			clInfoData[JimmUI.UI_XSTATUS] = stText;
		}
		
		/* registration date */
		long regDate = cItem.getIntValue(ContactItem.CONTACTITEM_REG);
		if (regDate > 0)
			clInfoData[JimmUI.UI_REG_DATE] = Util
					.getDateString(false, regDate);

		/* sign on time */
		long signonTime = cItem.getIntValue(ContactItem.CONTACTITEM_SIGNON);
		if (signonTime > 0)
			clInfoData[JimmUI.UI_SIGNON] = Util
					.getDateString(false, signonTime);

		/* online time */
		long onlineTime = cItem.getIntValue(ContactItem.CONTACTITEM_ONLINE);
		if (onlineTime > 0)
			clInfoData[JimmUI.UI_ONLINETIME] = Util
					.longitudeToString(onlineTime);

		/* idle time */
		int idleTime = cItem.getIntValue(ContactItem.CONTACTITEM_IDLE);
		if (idleTime > 0)
			clInfoData[JimmUI.UI_IDLE_TIME] = Util.longitudeToString(idleTime);
		
		//#sijapp cond.if (target="MIDP2" | target="MOTOROLA" | target="SIEMENS2") & modules_FILES="true"#

		/* Client version */
		int clientVers = cItem.getIntValue(ContactItem.CONTACTITEM_CLIENT);
		if (clientVers != Icq.CLI_NONE)
			clInfoData[JimmUI.UI_ICQ_CLIENT] = Icq.getClientString((byte) clientVers)
					+ " " + cItem.getStringValue(ContactItem.CONTACTITEM_CLIVERSION);

		/* ICQ protocol version */
		clInfoData[JimmUI.UI_ICQ_VERS] = Integer
				.toString(cItem.getIntValue(ContactItem.CONTACTITEM_ICQ_PROT));

		/* Internal IP */
		clInfoData[JimmUI.UI_INT_IP] = Util
				.ipToString(cItem.getBytesArray(ContactItem.CONTACTITEM_INTERNAL_IP));

		/* External IP */
		clInfoData[JimmUI.UI_EXT_IP] = Util
				.ipToString(cItem.getBytesArray(ContactItem.CONTACTITEM_EXTERNAL_IP));

		/* Port */
		int port = cItem.getIntValue(ContactItem.CONTACTITEM_DC_PORT);
		if (port != 0)
			clInfoData[JimmUI.UI_PORT] = Integer.toString(port);
		
		StringBuffer capsStr = new StringBuffer("\n");
		int caps = cItem.getIntValue(ContactItem.CONTACTITEM_CAPABILITIES);
		if ((caps & Icq.CAPF_AIM_SERVERRELAY) != 0) capsStr.append("Type-2 messages\n");
		if ((caps & Icq.CAPF_UTF8_INTERNAL)   != 0) capsStr.append("Utf-8 messages\n");
		if ((caps & Icq.CAPF_RICHTEXT)        != 0) capsStr.append("Rich text messages\n");
		if ((caps & Icq.CAPF_HTMLMESSAGES)    != 0) capsStr.append("Html messages\n");
		if ((caps & Icq.CAPF_AIMICON)         != 0) capsStr.append("AIM Icon\n");
		if ((caps & Icq.CAPF_AIMCHAT)         != 0) capsStr.append("AIM Chat\n");
		if ((caps & Icq.CAPF_XTRAZ)           != 0) capsStr.append("X-traz\n");
		if ((caps & Icq.CAPF_AIMFILE)         != 0) capsStr.append("AIM File\n");
		if ((caps & Icq.CAPF_AIMIMIMAGE)      != 0) capsStr.append("AIM Image\n");
		if ((caps & Icq.CAPF_AVATAR)          != 0) capsStr.append("Avatars\n");
		if ((caps & Icq.CAPF_DIRECT)          != 0) capsStr.append("Direct connect\n");
		if ((caps & Icq.CAPF_TYPING)          != 0) capsStr.append("Typing notify\n");
		if ((caps & Icq.CAPF_AUDIO)           != 0) capsStr.append("Audio\n");
		if ((caps & Icq.CAPF_VIDEO)           != 0) capsStr.append("Video\n");
		clInfoData[UI_CAPS] = capsStr.toString(); 
		
		//#sijapp cond.end#

		JimmUI.fillUserInfo(clInfoData, tlist);
		JimmUI.showInfoTextList(tlist);
	}

	private static void menuRemoveContactSelected()
	{
		System.out.println("clciContactMenu="+clciContactMenu);
		String uin = clciContactMenu.getStringValue(ContactItem.CONTACTITEM_UIN);
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
		RemoveMeAction remAct = new RemoveMeAction(clciContactMenu.getStringValue(ContactItem.CONTACTITEM_UIN));

		try
		{
			Icq.requestAction(remAct);
		} 
		catch (JimmException e)
		{
			JimmException.handleException(e);
			if (e.isCritical()) return;
		}
		JimmUI.backToLastScreen();
	}
	
	private static void menuRenameSelected()
	{
		String newName = renameTextbox.getString();
		if ((newName == null) || (newName.length() == 0)) return;
		clciContactMenu.rename(newName);
		renameTextbox.setString(null);
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
		
		if (flashTimerTask != null)
		{
			flashTimerTask.flashRestoreOldCaption();
			flashTimerTask.cancel();
		}
		flashTimerTask = new TimerTasks(control, text, (type == TimerTasks.TYPE_FLASH) ? 10 : 0, type);
		int interval = (type == TimerTasks.TYPE_FLASH) ? 500 : 300;
		Jimm.getTimerRef().schedule(flashTimerTask, interval, interval);
	}
	
	public static void showCreepingLine(Object control, String text, ContactItem cItem)
	{
		if ((text == null) || (control == null))
		if (!Options.getBoolean(Options.OPTION_CREEPING_LINE)) return;
		ChatTextList curChat = ChatHistory.getCurrent(); 
		if ((curChat != null) && (curChat.getUIControl() == control) && (curChat.isVisible())) return;
		String name = cItem.getStringValue(ContactItem.CONTACTITEM_NAME);
		String creepingText = name+": "+text;
		showCapText(control, creepingText, TimerTasks.TYPE_CREEPING);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static void startTaskForTimeString()
	{
		TimerTask task = new TimerTask() 
		{
			public void run()
			{
				MainThread.showTime();
			}
		};
		new Timer().schedule(task, 500, 10000);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private static String lastFileTransferLink = null;
	
	public static void setLastFileTransferLink(String value)
	{
		lastFileTransferLink = value;
		System.out.println("setLastFileTransferLink, value="+value);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static TextList loadErrorTextList;
	public static void showLoadError(String text)
	{
		loadErrorTextList = new TextList("Load error");
		loadErrorTextList.setMode(VirtualList.CURSOR_MODE_DISABLED);
		setColorScheme(loadErrorTextList, false, -1, true);
		loadErrorTextList.addBigText(text, 0xFF0000, Font.STYLE_PLAIN, -1);
		loadErrorTextList.addCommandEx(cmdOk, VirtualList.MENU_TYPE_RIGHT_BAR);
		loadErrorTextList.setCommandListener(_this);
		loadErrorTextList.activate(Jimm.display);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////
	//                            //
	//    Status message stuff    //
	//                            //
	////////////////////////////////
	
	private static TextList tlStatusMessage;
	private static ContactItem statusMessCI;
	
	/* Send a status message request message */
	public static void requestContactStatusMess(ContactItem ci)
	{
		int status = ci.getIntValue(ContactItem.CONTACTITEM_STATUS);
		
		statusMessCI = ci;
		tlStatusMessage = new TextList(ResourceBundle.getString("status_message"));
		setColorScheme(tlStatusMessage, false, -1, true);
		addTextListItem(tlStatusMessage, "wait", null, -1, true, 0x808080, Font.STYLE_PLAIN);
		tlStatusMessage.addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
		tlStatusMessage.setCommandListener(_this);
		tlStatusMessage.activate(Jimm.display);
		
		int msgType = Message.MESSAGE_TYPE_AWAY;
		
		switch (status)
		{
		case ContactList.STATUS_AWAY:       msgType = Message.MESSAGE_TYPE_AWAY; break;
		case ContactList.STATUS_OCCUPIED:   msgType = Message.MESSAGE_TYPE_OCC; break;
		case ContactList.STATUS_DND:        msgType = Message.MESSAGE_TYPE_DND; break;
		case ContactList.STATUS_CHAT:       msgType = Message.MESSAGE_TYPE_FFC; break;
		case ContactList.STATUS_NA:         msgType = Message.MESSAGE_TYPE_NA; break;
		case ContactList.STATUS_EVIL:       msgType = Message.MESSAGE_TYPE_EVIL; break;
		case ContactList.STATUS_DEPRESSION: msgType = Message.MESSAGE_TYPE_DEPRESSION; break;
		case ContactList.STATUS_HOME:       msgType = Message.MESSAGE_TYPE_HOME; break;
		case ContactList.STATUS_WORK:       msgType = Message.MESSAGE_TYPE_WORK; break;
		case ContactList.STATUS_LUNCH:      msgType = Message.MESSAGE_TYPE_LUNCH; break;
		}

		PlainMessage awayReq = new PlainMessage(
				Options.getString(Options.OPTION_UIN), statusMessCI, msgType, Util.createCurrentDate(false), "");

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
	
	static public void showStatusMessage(String message, String uin)
	{
		if (tlStatusMessage == null || statusMessCI == null) return;
		if ( !statusMessCI.getStringValue(ContactItem.CONTACTITEM_UIN).equals(uin) ) return;
		
		tlStatusMessage.lock();
		tlStatusMessage.clear();
		
		addTextListItem(tlStatusMessage, statusMessCI.getStringValue(ContactItem.CONTACTITEM_NAME), null, -1, false, -1, Font.STYLE_BOLD);
		StatusInfo statInfo = JimmUI.findStatus(StatusInfo.TYPE_STATUS, statusMessCI.getIntValue(ContactItem.CONTACTITEM_STATUS));
		String status = (statInfo != null) ? statInfo.getText() : new String();
		addTextListItem(tlStatusMessage, status, null, -1, false, -1, Font.STYLE_PLAIN);
		addTextListItem(tlStatusMessage, message, null, -1, false, -2, Font.STYLE_PLAIN);
		
		tlStatusMessage.unlock();
	}
	
}
