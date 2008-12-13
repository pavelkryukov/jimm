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
 File: src/jimm/MainMenu.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Denis Artemov
 *******************************************************************************/

package jimm;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDletStateChangeException;

import jimm.comm.Icq;
import jimm.util.ResourceBundle;
import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.ImageList;

public class MainMenu implements CommandListener, JimmScreen
{
	private static final int TAG_EXIT = 1;
	
	private static final int SELECT_STATUS = 1;
	private static final int SELECT_XSTATUS = 2;
	
	private static int statusSelection = 0;

	private static MainMenu _this;

	/* Static constants for menu actios */
	private static final int MENU_CONNECT       = 1;
	private static final int MENU_DISCONNECT    = 2;
	private static final int MENU_LIST          = 3;
	private static final int MENU_PHONEBOOK     = 4;
	private static final int MENU_OPTIONS       = 5;
	private static final int MENU_TRAFFIC       = 6;
	private static final int MENU_KEYLOCK       = 7;
	private static final int MENU_STATUS        = 8;
	private static final int MENU_XSTATUS       = 9;
	private static final int MENU_ABOUT         = 10;
	private static final int MENU_MINIMIZE      = 11;
	private static final int MENU_SOUND         = 12;
	private static final int MENU_EXIT          = 14;
	private static final int MENU_DEBUG_LOG     = 15;

	final public static ImageList menuIcons;

	/* List for selecting a online status */
	private static TextList statusList;

	/* Visual list */
	static private TextList list = new TextList(ResourceBundle.getString("menu"));

	/* Form for the adding users dialog */
	static public Form textBoxForm;

	/* Textbox for  Status messages */
	static private TextBox statusMessage;

	static
	{
		list.setMode(VirtualList.CURSOR_MODE_DISABLED);
		menuIcons = new ImageList();
		
		try
		{
//#sijapp cond.if target="MIDP2"#
			menuIcons.setScale(Options.getInt(Options.OPTION_IMG_SCALE));
//#sijapp cond.end#
			menuIcons.load("/micons.png", -1, -1, -1, Jimm.getPhoneVendor() == Jimm.PHONE_NOKIA);
		} catch (Exception e) {}
	}
	
	public MainMenu()
	{
		_this = this;
	}

	static private Image getStatusImage()
	{
		long cursStatus = Options.getLong(Options.OPTION_ONLINE_STATUS);
		StatusInfo statInfo = JimmUI.findStatus(StatusInfo.TYPE_STATUS, (int)cursStatus);
		return (statInfo != null) ? statInfo.getImage() : null; 
	}

	static private Image getXStatusImage()
	{
		int cursStatus = Options.getInt(Options.OPTION_XSTATUS);
		StatusInfo statInfo = JimmUI.findStatus(StatusInfo.TYPE_X_STATUS, (int)cursStatus);
		return (statInfo != null) ? statInfo.getImage() : null; 
	}

	/* Builds the main menu (visual list) */
	public static void build()
	{
		JimmUI.setColorScheme(list, false, -1, true);
		
		boolean connected = Icq.isConnected();
			
		list.lock();
		
		int lastIndex = list.getCurrTextIndex();
		
		list.removeAllCommands();
		list.clear();
		
		if (connected)
		{
			JimmUI.addTextListItem(list, "keylock_enable", menuIcons.elementAt(3), MENU_KEYLOCK, true, -1, Font.STYLE_PLAIN);
			JimmUI.addTextListItem(list, "disconnect", menuIcons.elementAt(1), MENU_DISCONNECT, true, -1, Font.STYLE_PLAIN);
		}
		else
		{
			JimmUI.addTextListItem(list, "connect", menuIcons.elementAt(0), MENU_CONNECT, true, -1, Font.STYLE_PLAIN);
		}
		
		JimmUI.addTextListItem(list, "set_status", getStatusImage(), MENU_STATUS, true, -1, Font.STYLE_PLAIN);
		JimmUI.addTextListItem(list, "set_xstatus", getXStatusImage(), MENU_XSTATUS, true, -1, Font.STYLE_PLAIN);
		
		if (ContactList.getSize() != 0)
			JimmUI.addTextListItem(list, "contact_list", menuIcons.elementAt(2), MENU_LIST, true, -1, Font.STYLE_PLAIN);
		
		//#sijapp cond.if modules_PIM is "true" #
		JimmUI.addTextListItem(list, "phone_book", menuIcons.elementAt(33), MENU_PHONEBOOK, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#    	

		JimmUI.addTextListItem(list, "options_lng",  menuIcons.elementAt(4), MENU_OPTIONS, true, -1, Font.STYLE_PLAIN);
		
		//#sijapp cond.if target isnot "DEFAULT"#
		boolean silentMode = Options.getBoolean(Options.OPTION_SILENT_MODE);
		JimmUI.addTextListItem(list, getSoundValue(silentMode), silentMode ? menuIcons.elementAt(6) : menuIcons.elementAt(5), MENU_SOUND, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#    	
			
		//#sijapp cond.if modules_TRAFFIC is "true" #
		JimmUI.addTextListItem(list, "traffic_lng", menuIcons.elementAt(7), MENU_TRAFFIC, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#
			
		JimmUI.addTextListItem(list, "about", menuIcons.elementAt(8), MENU_ABOUT, true, -1, Font.STYLE_PLAIN);

		//#sijapp cond.if modules_DEBUGLOG is "true" #
		JimmUI.addTextListItem(list, "** Debug Log **", null, MENU_DEBUG_LOG, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#
		
		//#sijapp cond.if target is "MIDP2" #
		if (Jimm.getPhoneVendor() == Jimm.PHONE_SONYERICSSON) 
			JimmUI.addTextListItem(list, "minimize", menuIcons.elementAt(9), MENU_MINIMIZE, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#
		
		JimmUI.addTextListItem(list, "exit", menuIcons.elementAt(10), MENU_EXIT, true, -1, Font.STYLE_PLAIN);

		list.addCommandEx(JimmUI.cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
		if (ContactList.getSize() != 0)
			list.addCommandEx(JimmUI.cmdList, VirtualList.MENU_TYPE_LEFT_BAR);

		list.selectTextByIndex(lastIndex);
		
		list.setCyclingCursor(true);
			
		list.unlock();
			
		list.setCommandListener(_this);
	}

	/* Displays the given alert and activates the main menu afterwards */
	static public void activate(Alert alert)
	{
		MainMenu.build();
		MainMenu.list.activate(Jimm.display, alert);
	}

	/* Activates the main menu */
	static public void activateMenu()
	{
		Jimm.aaUserActivity();
		MainMenu.build();
		MainMenu.list.activate(Jimm.display);
		JimmUI.setLastScreen(_this, true);
	}
	
	public void activate()
	{
		MainMenu.activateMenu();
	}
	
	public boolean isScreenActive()
	{
		return JimmUI.isControlActive(list);
	}
	
	public static TextList getUIConrol()
	{
		return MainMenu.list;
	}

	private void doExit(boolean anyway)
	{
		if (!anyway && ContactList.getUnreadMessCount())
		{
			JimmUI.messageBox(ResourceBundle.getString("attention"),
					ResourceBundle.getString("have_unread_mess"),
					JimmUI.MESBOX_YESNO, _this, TAG_EXIT);
		} else
		{
			Icq.disconnect(false);
			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e1)
			{
				/* Do nothing */
			}
			/* Exit app */
			try
			{
				Jimm.jimm.destroyApp(true);
			} catch (MIDletStateChangeException e)
			{ /* Do nothing */
			}
		}
	}

	//#sijapp cond.if target isnot "DEFAULT" #	
	static private String getSoundValue(boolean value)
	{
		return ResourceBundle.getString(value ? "#sound_on" : "#sound_off");
	}

	//#sijapp cond.end#	
	
	private static void initStatusList(String caption, int type, int afterStatus, int showMode)
	{
		statusList = new TextList(ResourceBundle.getString(caption));
		statusList.setMode(VirtualList.CURSOR_MODE_DISABLED);
		statusList.setCyclingCursor(true);
		statusList.lock();
		JimmUI.setColorScheme(statusList, false, -1, true);
		JimmUI.fillStatusesInList(statusList, type, StatusInfo.FLAG_IN_MENU, showMode);
		statusList.unlock();
		statusSelection = afterStatus;
	}
	
	/* Command listener */
	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
		// Cancel status text selection 
		if (c == JimmUI.cmdCancel)
		{
			JimmUI.backToLastScreen();
		}
		
		// Return to works screen after canceling 
		else if ((c == JimmUI.cmdBack) && (JimmUI.isControlActive(statusList) || JimmUI.isControlActive(list)))
		{
			JimmUI.backToLastScreen();
			statusList = null;
		}
		
		// Activate contact list after pressing "List" menu
		else if (c == JimmUI.cmdList)
		{
			ContactList.activateList();
		}

		/* User select OK in exit questiom message box */
		else if (JimmUI.getCommandType(c, TAG_EXIT) == JimmUI.CMD_YES)
		{
			doExit(true);
		}

		/* User select CANCEL in exit questiom message box */
		else if (JimmUI.getCommandType(c, TAG_EXIT) == JimmUI.CMD_NO)
		{
			JimmUI.backToLastScreen();
		}

		/* Menu item has been selected */
		else if (((c == List.SELECT_COMMAND) || (c == JimmUI.cmdSelect)) && JimmUI.isControlActive(list))
		{
			int selectedIndex = MainMenu.list.getCurrTextIndex();
			switch (selectedIndex)
			{
			case MENU_CONNECT:
				/* Connect */
				Icq.reconnect_attempts = (Options.getBoolean(Options.OPTION_RECONNECT)) ?
							Options.getInt(Options.OPTION_RECONNECT_NUMBER) : 0;
				ContactList.beforeConnect();
				SplashCanvas.setLastErrCode(null);
				Icq.connect();
				break;

			case MENU_DISCONNECT:
				/* Disconnect */
				Icq.disconnect(true);
				Thread.yield();
				/* Show the main menu */
				MainMenu.activateMenu();
				break;

			case MENU_LIST:
				/* ContactList */
				ContactList.activateList();
				break;

			//#sijapp cond.if modules_PIM is "true" #
			case MENU_PHONEBOOK:
				/* PhoneBook */
				PhoneBook.activate(false);
				break;
			//#sijapp cond.end#    	

			case MENU_KEYLOCK:
				/* Enable keylock */
				SplashCanvas.lock();
				break;

			case MENU_STATUS: /* Set status */
			case MENU_XSTATUS: /* Set xstatus */
				int stValue;
				
				if (selectedIndex == MENU_STATUS)
				{
					initStatusList("set_status", StatusInfo.TYPE_STATUS, SELECT_STATUS, JimmUI.SHOW_STATUSES_NAME);
					stValue = (int)Options.getLong(Options.OPTION_ONLINE_STATUS);
				}
				else
				{
					initStatusList("set_xstatus", StatusInfo.TYPE_X_STATUS, SELECT_XSTATUS, JimmUI.SHOW_STATUSES_DESCR);
					stValue = Options.getInt(Options.OPTION_XSTATUS);
				}
				
				MainMenu.statusList.selectTextByIndex(stValue);
				MainMenu.statusList.setCommandListener(_this);
				MainMenu.statusList.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
				MainMenu.statusList.addCommandEx(JimmUI.cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
				MainMenu.statusList.activate(Jimm.display);
				break;

			case MENU_OPTIONS:
				/* Options */
				Options.editOptions();
				break;

			//#sijapp cond.if modules_TRAFFIC is "true" #
			case MENU_TRAFFIC:
				/* Traffic */
				Traffic.trafficScreen.activate();
				break;
			//#sijapp cond.end #

			case MENU_ABOUT:
				// Display an info
				JimmUI.about();
				break;

			//#sijapp cond.if target is "MIDP2"#
			case MENU_MINIMIZE:
				/* Minimize Jimm (if supported) */
				Jimm.setMinimized(true);
				break;
			//#sijapp cond.end#

			//#sijapp cond.if target isnot "DEFAULT" #
			case MENU_SOUND:
				ContactList.changeSoundMode(Icq.isConnected());
				build();
				break;
			//#sijapp cond.end#				

			case MENU_EXIT:
				/* Exit */
				doExit(false);
				break;
				
			//#sijapp cond.if modules_DEBUGLOG is "true" #
			case MENU_DEBUG_LOG:
				DebugLog.activate();
				break;
			//#sijapp cond.end#
			}
		}

		/* Online status has been selected */
		else if (((c == List.SELECT_COMMAND) || (c == JimmUI.cmdSelect)) && JimmUI.isControlActive(statusList))
		{
			userSelectStatus();
		} 
		else if ((c == JimmUI.cmdSelect) && (d == statusMessage))
		{
			int onlineStatus = statusList.getCurrTextIndex();
			Options.setStatusString(StatusInfo.TYPE_STATUS, onlineStatus, statusMessage.getString());
			Options.saveStatusStringsByType(StatusInfo.TYPE_STATUS);
			setStatus(false);
			JimmUI.backToLastScreen();
			statusList = null;
		}
	}
	
	private static void setStatus(boolean save)
	{
		int onlineStatus = statusList.getCurrTextIndex();
		Options.setLong(Options.OPTION_ONLINE_STATUS, onlineStatus);
		if (save) Options.safeSave(); 
		if (Icq.isConnected())
		{
			try
			{
				Icq.setOnlineStatus(onlineStatus, 255);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}
		}
	}
	
	private void userSelectStatus()
	{
		switch (statusSelection)
		{
		case SELECT_STATUS:
		{
			int onlineStatus = statusList.getCurrTextIndex();
			
			StatusInfo info = JimmUI.findStatus(StatusInfo.TYPE_STATUS, onlineStatus);
			
			if (info != null && info.testFlag(StatusInfo.FLAG_HAVE_DESCR))
			{
				String statMessage = Options.getStatusString(StatusInfo.TYPE_STATUS, onlineStatus);
				
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target is "RIM"#
				    statusMessage = new TextBox(info.getText(), statMessage, 255, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
//#sijapp cond.else#
				    statusMessage = new TextBox(info.getText(), statMessage, 255, TextField.ANY);
//#sijapp cond.end#

			    statusMessage.addCommand(JimmUI.cmdCancel);
				statusMessage.addCommand(JimmUI.cmdSelect);
				statusMessage.setCommandListener(_this);
				Jimm.display.setCurrent(statusMessage);
				Jimm.setBkltOn(true);
				return;
			}
			else
			{
				setStatus(true);
			}
		}
			break;
	
		case SELECT_XSTATUS:
			int xStatus = statusList.getCurrTextIndex();
			if (xStatus == -2) xStatus = -1;
			Options.setInt(Options.OPTION_XSTATUS, xStatus);
			if (Icq.isConnected())
			{
				try
				{
					Icq.sendUserUnfoPacket();
					Icq.setOnlineStatus (-1, xStatus);
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
			}
			Options.safeSave();
			break;
		}
		
		JimmUI.backToLastScreen();
	}
}
