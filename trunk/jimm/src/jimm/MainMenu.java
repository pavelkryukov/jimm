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
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDletStateChangeException;

import jimm.comm.Icq;
import jimm.util.ResourceBundle;
import DrawControls.TextList;
import DrawControls.VirtualList;

public class MainMenu implements CommandListener
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
	private static final int MENU_OPTIONS       = 4;
	private static final int MENU_TRAFFIC       = 5;
	private static final int MENU_KEYLOCK       = 6;
	private static final int MENU_STATUS        = 7;
	private static final int MENU_XSTATUS       = 8;
	private static final int MENU_ABOUT         = 10;
	private static final int MENU_MINIMIZE      = 11;
	private static final int MENU_SOUND         = 12;
	private static final int MENU_EXIT          = 14;

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
	}
	
	public MainMenu()
	{
		_this = this;
	}

	static private Image getStatusImage()
	{
		long cursStatus = Options.getLong(Options.OPTION_ONLINE_STATUS);
		int imageIndex = JimmUI.getStatusImageIndex(cursStatus);
		return ContactList.getImageList().elementAt(imageIndex);
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
			JimmUI.addTextListItem(list, "keylock_enable", null, MENU_KEYLOCK, true, -1, Font.STYLE_PLAIN);
			JimmUI.addTextListItem(list, "disconnect", null, MENU_DISCONNECT, true, -1, Font.STYLE_PLAIN);
		}
		else
		{
			JimmUI.addTextListItem(list, "connect", null, MENU_CONNECT, true, -1, Font.STYLE_PLAIN);
		}
		
		JimmUI.addTextListItem(list, "set_status", getStatusImage(), MENU_STATUS, true, -1, Font.STYLE_PLAIN);
		JimmUI.addTextListItem(list, "set_xstatus", ContactList.xStatusImages.elementAt(Options.getInt(Options.OPTION_XSTATUS)), MENU_XSTATUS, true, -1, Font.STYLE_PLAIN);
		
		if (ContactList.getSize() != 0)
			JimmUI.addTextListItem(list, "contact_list", null, MENU_LIST, true, -1, Font.STYLE_PLAIN);
		
		JimmUI.addTextListItem(list, "options_lng",  null, MENU_OPTIONS, true, -1, Font.STYLE_PLAIN);
		
		//#sijapp cond.if target isnot "DEFAULT"#
		JimmUI.addTextListItem(list, getSoundValue(Options.getBoolean(Options.OPTION_SILENT_MODE)), null, MENU_SOUND, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#    	
			
		//#sijapp cond.if modules_TRAFFIC is "true" #
		JimmUI.addTextListItem(list, "traffic_lng", null, MENU_TRAFFIC, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#
			
		JimmUI.addTextListItem(list, "about", null, MENU_ABOUT, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.if target is "MIDP2" #
		if (Jimm.is_phone_SE()) JimmUI.addTextListItem(list, "minimize", null, MENU_MINIMIZE, true, -1, Font.STYLE_PLAIN);
		//#sijapp cond.end#
		JimmUI.addTextListItem(list, "exit", null, MENU_EXIT, true, -1, Font.STYLE_PLAIN);

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
	static public void activate()
	{
		Jimm.aaUserActivity();
		MainMenu.build();
		MainMenu.list.activate(Jimm.display);
		JimmUI.setLastScreen(MainMenu.list);
	}
	
	public static TextList getUIConrol()
	{
		return MainMenu.list;
	}

	private void doExit(boolean anyway)
	{
		if (!anyway && ContactList.getUnreadMessCount() > 0)
		{
			JimmUI.messageBox(ResourceBundle.getString("attention"),
					ResourceBundle.getString("have_unread_mess"),
					JimmUI.MESBOX_YESNO, _this, TAG_EXIT);
		} else
		{
			Icq.connecting = false;
			Icq.disconnect();
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
	
	private static void initStatusList()
	{
		statusList = new TextList(ResourceBundle.getString("set_status"));
		statusList.setMode(TextList.CURSOR_MODE_DISABLED);
		statusList.setCyclingCursor(true);
		statusList.lock();
		JimmUI.setColorScheme(statusList, false, -1, true);
		JimmUI.addTextListItem(statusList, "status_online",     ContactList.statusOnlineImg,     ContactList.STATUS_ONLINE,     true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_chat",       ContactList.statusChatImg,       ContactList.STATUS_CHAT,       true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_away",       ContactList.statusAwayImg,       ContactList.STATUS_AWAY,       true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_na",         ContactList.statusNaImg,         ContactList.STATUS_NA,         true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_occupied",   ContactList.statusOccupiedImg,   ContactList.STATUS_OCCUPIED,   true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_dnd",        ContactList.statusDndImg,        ContactList.STATUS_DND,        true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_invisible",  ContactList.statusInvisibleImg,  ContactList.STATUS_INVISIBLE,  true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_invis_all",  ContactList.statusInvisibleImg,  ContactList.STATUS_INVIS_ALL,  true, -1, Font.STYLE_BOLD);
		JimmUI.addTextListItem(statusList, "status_evil",       ContactList.statusEvilImg,	     ContactList.STATUS_EVIL,       true, -1, Font.STYLE_PLAIN);
		JimmUI.addTextListItem(statusList, "status_depression",	ContactList.statusDepressionImg, ContactList.STATUS_DEPRESSION, true, -1, Font.STYLE_PLAIN);
		JimmUI.addTextListItem(statusList, "status_home",       ContactList.statusHomeImg,	     ContactList.STATUS_HOME,       true, -1, Font.STYLE_PLAIN);
		JimmUI.addTextListItem(statusList, "status_work",       ContactList.statusWorkImg,	     ContactList.STATUS_WORK,       true, -1, Font.STYLE_PLAIN);
		JimmUI.addTextListItem(statusList, "status_lunch",      ContactList.statusLunchImg,	     ContactList.STATUS_LUNCH,      true, -1, Font.STYLE_PLAIN);
		statusList.unlock();
		statusSelection = SELECT_STATUS;
	}
	
	private static void initXStatusList()
	{
		statusList = new TextList(ResourceBundle.getString("set_xstatus"));
		statusList.setMode(TextList.CURSOR_MODE_DISABLED);
		statusList.setCyclingCursor(true);
		statusList.lock();
		JimmUI.setColorScheme(statusList, false, -1, true);
		for (int s = 0; s < 2; s++)
		{
			for (int i = 0; i < JimmUI.xStatusStrings.length; i++)
			{
				int xstatus = i-1;
				boolean std = Icq.isXStatusStd(xstatus);
				if ((std ? 0 : 1) == s)
					JimmUI.addTextListItem
					(
						statusList, 
						JimmUI.xStatusStrings[i], 
						ContactList.xStatusImages.elementAt(xstatus), 
						i, true, -1, 
						std ? Font.STYLE_BOLD : Font.STYLE_PLAIN
					);
			}
		}
		
		statusList.unlock();
		statusSelection = SELECT_XSTATUS;
	}
	
	/* Command listener */
	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
		// Return to works screen after canceling 
		if ((c == JimmUI.cmdBack) && (JimmUI.isControlActive(statusList) || JimmUI.isControlActive(list)))
		{
			Jimm.showWorkScreen();
			statusList = null;
		}
		
		// Activate contact list after pressing "List" menu
		else if (c == JimmUI.cmdList)
		{
			ContactList.activate();
		}

		/* User select OK in exit questiom message box */
		else if (JimmUI.getCommandType(c, TAG_EXIT) == JimmUI.CMD_YES)
		{
			doExit(true);
		}

		/* User select CANCEL in exit questiom message box */
		else if (JimmUI.getCommandType(c, TAG_EXIT) == JimmUI.CMD_NO)
		{
			ContactList.activate();
		}

		/* Menu item has been selected */
		else if (((c == List.SELECT_COMMAND) || (c == JimmUI.cmdSelect)) && JimmUI.isControlActive(list))
		{
			int selectedIndex = MainMenu.list.getCurrTextIndex();
			switch (selectedIndex)
			{
			case MENU_CONNECT:
				/* Connect */
				Icq.reconnect_attempts = Options
						.getInt(Options.OPTION_RECONNECT_NUMBER);
				ContactList.beforeConnect();
				Icq.connect();
				break;

			case MENU_DISCONNECT:
				/* Disconnect */
				Icq.connecting = false;
				Icq.disconnect();
				Thread.yield();
				/* Show the main menu */
				MainMenu.activate();
				break;

			case MENU_LIST:
				/* ContactList */
				ContactList.activate();
				break;

			case MENU_KEYLOCK:
				/* Enable keylock */
				SplashCanvas.lock();
				break;

			case MENU_STATUS: /* Set status */
			case MENU_XSTATUS: /* Set xstatus */
				int stValue;
				
				if (selectedIndex == MENU_STATUS)
				{
					initStatusList();
					stValue = (int)Options.getLong(Options.OPTION_ONLINE_STATUS);
				}
				else
				{
					initXStatusList();
					stValue = Options.getInt(Options.OPTION_XSTATUS)+1;
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
				JimmUI.about(null);
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
				break;
			//#sijapp cond.end#				

			case MENU_EXIT:
				/* Exit */
				doExit(false);
				break;
			}
		}

		/* Online status has been selected */
		else if (((c == List.SELECT_COMMAND) || (c == JimmUI.cmdSelect)) && JimmUI.isControlActive(statusList))
		{
			userSelectStatus();
		} 
		else if ((c == JimmUI.cmdSelect) && (d == statusMessage))
		{
			Options.setString(Options.OPTION_STATUS_MESSAGE, statusMessage.getString());
			Options.safe_save();
			/* Active MM/CL */
			if (Icq.isConnected())
			{
				ContactList.activate();
			} else
			{
				MainMenu.activate();
			}
		}
	}
	
	private void userSelectStatus()
	{
		boolean activateMenu = false;
		switch (statusSelection)
		{
		case SELECT_STATUS:
			int onlineStatus = statusList.getCurrTextIndex();
			long lastStatus = Options.getLong(Options.OPTION_ONLINE_STATUS);
			Options.setLong(Options.OPTION_ONLINE_STATUS, onlineStatus);

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
			
			if ((onlineStatus != ContactList.STATUS_INVISIBLE)
					&& (onlineStatus != ContactList.STATUS_INVIS_ALL)
					&& (onlineStatus != ContactList.STATUS_ONLINE)
					&& (onlineStatus != ContactList.STATUS_CHAT)
					&& (onlineStatus != ContactList.STATUS_EVIL)
					&& (onlineStatus != ContactList.STATUS_DEPRESSION)
					&& (onlineStatus != ContactList.STATUS_HOME)
					&& (onlineStatus != ContactList.STATUS_WORK)
					&& (onlineStatus != ContactList.STATUS_LUNCH))
			{
				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				    statusMessage = new TextBox(ResourceBundle
					.getString("status_message"), Options
					.getString(Options.OPTION_STATUS_MESSAGE), 255,
					TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
				//#sijapp cond.else#
				//#				statusMessage = new TextBox(ResourceBundle.getString("status_message"), Options.getString(Options.OPTION_STATUS_MESSAGE), 255, TextField.ANY);
				//#sijapp cond.end#

				statusMessage.addCommand(JimmUI.cmdSelect);
				statusMessage.setCommandListener(_this);
				Jimm.display.setCurrent(statusMessage);
				Jimm.setBkltOn(true);
			} 
			else activateMenu = true;
			break;
	
		case SELECT_XSTATUS:
			int xStatus = statusList.getCurrTextIndex()-1;
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
			activateMenu = true;
			break;
		}
		Options.safe_save();
		statusList = null;
		
		if (activateMenu) /* Active MM/CL */
		{
			if (Icq.isConnected()) ContactList.activate();
			else MainMenu.activate();
		}
	}
}
