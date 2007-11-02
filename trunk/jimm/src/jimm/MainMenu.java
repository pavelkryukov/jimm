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
import jimm.comm.SearchAction;
import jimm.comm.Action;
import jimm.comm.UpdateContactListAction;
import jimm.util.ResourceBundle;
import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;

public class MainMenu implements CommandListener
{
	private static final int TAG_EXIT = 1;
	private static final int TAG_RENAME_GROUPS = 2;
	private static final int TAG_DELETE_GROUPS = 3;
	private static final int TAG_CL = 4;
	
	private static final int SELECT_STATUS = 1;
	private static final int SELECT_XSTATUS = 2;
	
	private static int statusSelection = 0;

	private static MainMenu _this;

	/* Static constants for menu actios */
	private static final int MENU_CONNECT = 1;

	private static final int MENU_DISCONNECT = 2;

	private static final int MENU_LIST = 3;

	private static final int MENU_OPTIONS = 4;

	private static final int MENU_TRAFFIC = 5;

	private static final int MENU_KEYLOCK = 6;

	private static final int MENU_STATUS = 7;
	private static final int MENU_XSTATUS = 8;

	private static final int MENU_GROUPS = 9;

	private static final int MENU_ABOUT = 10;

	private static final int MENU_MINIMIZE = 11;

	private static final int MENU_SOUND = 12;

	private static final int MENU_MYSELF = 13;

	private static final int MENU_EXIT = 14; /* Exit has to be biggest element cause it also marks the size */

	/* Send command */
	private static Command sendCommand = new Command(ResourceBundle
			.getString("send"), Command.OK, 1);

	//#sijapp cond.if target is "MOTOROLA" #
	//#	private static Command exitCommand = new Command(ResourceBundle.getString("exit_button"), Command.EXIT, 1);
	//#sijapp cond.end#

	/* List for selecting a online status */
	//private static List statusList;
	private static TextList statusList;
	
	private static List groupActList;

	/////////////////////////////////////////////////////////////////
	private static final int STATUS_NONE = 0;

	private static final int STATUS_ADD_CONTACT = 1;

	private static final int STATUS_ADD_GROUP = 2;

	private static final int STATUS_RENAME_GROUP = 3;

	private static int status = STATUS_NONE;

	/** ************************************************************************* */

	/* Visual list */
	static private TextList list = new TextList(ResourceBundle.getString("menu"));

	/* Groups list */
	static private int[] groupIds;

	/* Form for the adding users dialog */
	static public Form textBoxForm;

	/* Text box for adding users to the contact list */
	static private TextField uinTextField;

	/* Textbox for  Status messages */
	static private TextBox statusMessage;

	static
	{
		list.setCursorMode(VirtualList.SEL_NONE);
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
		JimmUI.setColorScheme(list, false);
		
		boolean connected = Icq.isConnected();
			
		list.lock();
		
		int lastIndex = list.getCurrTextIndex();
		
		list.removeAllCommands();
		list.clear();
		
		if (connected)
		{
			JimmUI.addTextListItem(list, "keylock_enable", null, MENU_KEYLOCK);
			JimmUI.addTextListItem(list, "disconnect", null, MENU_DISCONNECT);
		}
		else
		{
			JimmUI.addTextListItem(list, "connect", null, MENU_CONNECT);
		}
		
		JimmUI.addTextListItem(list, "set_status", getStatusImage(), MENU_STATUS);
		JimmUI.addTextListItem(list, "set_xstatus", ContactList.xStatusImages.elementAt(Options.getInt(Options.OPTION_XSTATUS)), MENU_XSTATUS);
		
		if (connected)
		{
			JimmUI.addTextListItem(list, "manage_contact_list", null, MENU_GROUPS);
			JimmUI.addTextListItem(list, "myself", null, MENU_MYSELF);
		}
		
		JimmUI.addTextListItem(list, "contact_list", null, MENU_LIST);
		JimmUI.addTextListItem(list, "options_lng",  null, MENU_OPTIONS);
		
		//#sijapp cond.if target isnot "DEFAULT"#
		JimmUI.addTextListItem(list, getSoundValue(Options.getBoolean(Options.OPTION_SILENT_MODE)), null, MENU_SOUND);
		//#sijapp cond.end#    	
			
		//#sijapp cond.if modules_TRAFFIC is "true" #
		JimmUI.addTextListItem(list, "traffic_lng", null, MENU_TRAFFIC);
		//#sijapp cond.end#
			
		JimmUI.addTextListItem(list, "about", null, MENU_ABOUT);
		//#sijapp cond.if target is "MIDP2" #
		if (Jimm.is_phone_SE()) JimmUI.addTextListItem(list, "minimize", null, MENU_MINIMIZE);
		//#sijapp cond.end#
		JimmUI.addTextListItem(list, "exit", null, MENU_EXIT);

		list.addCommandEx(JimmUI.cmdSelect, VirtualList.MENU_TYPE_LEFT_BAR);
		if (connected) list.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
			
		list.selectTextByIndex(lastIndex);
			
		list.unlock();
			
		list.setCommandListener(_this);
	}

	/* Displays the given alert and activates the main menu afterwards */
	static public void activate(Alert alert)
	{
		status = STATUS_NONE;
		MainMenu.build();
		MainMenu.list.activate(Jimm.display, alert);
	}

	/* Activates the main menu */
	static public void activate()
	{
		MainMenu.build();
		MainMenu.list.activate(Jimm.display);
		JimmUI.setLastScreen(MainMenu.list);
	}

	/* Show form for adding user */
	static public void showTextBoxForm(String caption, String label,
			String text, int fieldType)
	{
		textBoxForm = new Form(ResourceBundle.getString(caption));
		uinTextField = new TextField(ResourceBundle.getString(label), text, 16, fieldType);
		textBoxForm.append(uinTextField);

		textBoxForm.addCommand(sendCommand);
		textBoxForm.addCommand(JimmUI.cmdBack);
		textBoxForm.setCommandListener(_this);
		Jimm.display.setCurrent(textBoxForm);
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
		statusList.setCursorMode(TextList.SEL_NONE);
		statusList.lock();
		JimmUI.addTextListItem(statusList, "status_online", ContactList.statusOnlineImg,    ContactList.STATUS_ONLINE);
		JimmUI.addTextListItem(statusList, "status_chat", ContactList.statusChatImg,      ContactList.STATUS_CHAT);
		JimmUI.addTextListItem(statusList, "status_away", ContactList.statusAwayImg,      ContactList.STATUS_AWAY);
		JimmUI.addTextListItem(statusList, "status_na", ContactList.statusNaImg,        ContactList.STATUS_NA);
		JimmUI.addTextListItem(statusList, "status_occupied", ContactList.statusOccupiedImg,  ContactList.STATUS_OCCUPIED);
		JimmUI.addTextListItem(statusList, "status_dnd", ContactList.statusDndImg,       ContactList.STATUS_DND);
		JimmUI.addTextListItem(statusList, "status_invisible", ContactList.statusInvisibleImg, ContactList.STATUS_INVISIBLE);
		JimmUI.addTextListItem(statusList, "status_invis_all", ContactList.statusInvisibleImg, ContactList.STATUS_INVIS_ALL);
		statusList.unlock();
		statusSelection = SELECT_STATUS;
	}
	
    private static final String[] xstatus = {
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

	private static void initXStatusList()
	{
		statusList = new TextList(ResourceBundle.getString("set_xstatus"));
		statusList.setCursorMode(TextList.SEL_NONE);
		statusList.lock();
		for (int i = 0; i < xstatus.length; i++)
			JimmUI.addTextListItem(statusList, xstatus[i], ContactList.xStatusImages.elementAt(i-1), i);
		
		statusList.unlock();
		statusSelection = SELECT_XSTATUS;
	}

	
	/* Command listener */
	public void commandAction(Command c, Displayable d)
	{
		//#sijapp cond.if target is "MOTOROLA" #
		/* Exit by soft button */
		//#		if (c == MainMenu.exitCommand)
		//#		{
		//#			doExit(false);
		//#			return;
		//#		}
		//#sijapp cond.end#
		if ((groupActList != null) && (d == groupActList))
		{
			if ((c == JimmUI.cmdSelect) || (c == List.SELECT_COMMAND))
				CLManagementItemSelected(groupActList.getSelectedIndex());
			else if (c == JimmUI.cmdBack)
				activate();
			return;
		}

		if (JimmUI.getCommandType(c, TAG_RENAME_GROUPS) == JimmUI.CMD_OK)
		{
			String groupName = ContactList.getGroupById(
					groupIds[JimmUI.getLastSelIndex()]).getName();
			showTextBoxForm("rename_group", "group_name", groupName,
					TextField.ANY);
		}

		else if (JimmUI.getCommandType(c, TAG_DELETE_GROUPS) == JimmUI.CMD_OK)
		{
			UpdateContactListAction deleteGroupAct = new UpdateContactListAction(
					ContactList
							.getGroupById(groupIds[JimmUI.getLastSelIndex()]),
					UpdateContactListAction.ACTION_DEL);
			try
			{
				Icq.requestAction(deleteGroupAct);
				SplashCanvas.addTimerTask("wait", deleteGroupAct, false);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
			}
		}

		// Return to works screen after canceling status selection 
		else if ((c == JimmUI.cmdBack) && JimmUI.isControlActive(statusList))
		{
			Jimm.showWorkScreen();
			statusList = null;
		} 
		
		// Activate contact list after pressing "back" menu
		else if ((c == JimmUI.cmdBack) && JimmUI.isControlActive(list))
		{
			ContactList.activate();
		}
		
		else if ((c == sendCommand) && (d == MainMenu.textBoxForm) && (MainMenu.textBoxForm != null))
		{
			Action act = null;

			switch (status)
			{
			case STATUS_ADD_GROUP:
				ContactListGroupItem newGroup = new ContactListGroupItem(uinTextField.getString());
				act = new UpdateContactListAction(newGroup, UpdateContactListAction.ACTION_ADD);
				break;

			case STATUS_RENAME_GROUP:
				ContactListGroupItem group = ContactList.getGroupById(groupIds[JimmUI.getLastSelIndex()]);
				group.setName(uinTextField.getString());
				ContactList.safeSave();
				act = new UpdateContactListAction(group, UpdateContactListAction.ACTION_RENAME);
				break;

			case STATUS_ADD_CONTACT:
				Search search = new Search(true);
				String data[] = new String[Search.LAST_INDEX];
				data[Search.UIN] = uinTextField.getString();
				act = new SearchAction(search, data,
						SearchAction.CALLED_BY_ADDUSER);
				break;
			}

			status = STATUS_NONE;

			try
			{
				Icq.requestAction(act);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical())
					return;
			}
			// Start timer
			SplashCanvas.addTimerTask("wait", act, false);
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
			
				JimmUI.setColorScheme(statusList, false);
				
				MainMenu.statusList.setCommandListener(_this);
				MainMenu.statusList.addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
				MainMenu.statusList.addCommandEx(JimmUI.cmdSelect, VirtualList.MENU_TYPE_LEFT_BAR);
				MainMenu.statusList.activate(Jimm.display);
			
				break;

			case MENU_GROUPS:

				groupActList = new List(ResourceBundle.getString("manage_contact_list"), List.IMPLICIT);

				//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				groupActList.deleteAll();
				//#sijapp cond.else#
				while (groupActList.size() != 0) groupActList.delete(0);
				//#sijapp cond.end#
				
				
				groupActList.append(ResourceBundle.getString("add_user",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("search_user",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("add_group",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("rename_group",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.append(ResourceBundle.getString("del_group",
						ResourceBundle.FLAG_ELLIPSIS), null);
				groupActList.setCommandListener(_this);
				groupActList.addCommand(JimmUI.cmdBack);
				groupActList.addCommand(JimmUI.cmdSelect);
				Jimm.display.setCurrent(groupActList);

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

			case MENU_MYSELF:
				JimmUI.requiestUserInfo(Options.getString(Options.OPTION_UIN),
						"");
				break;

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

		/* Contact list management group */
		else if (JimmUI.getCommandType(c, TAG_CL) == JimmUI.CMD_OK)
			CLManagementItemSelected(JimmUI.getLastSelIndex());
	}
	
	private void userSelectStatus()
	{
		boolean activateMenu = false;
		switch (statusSelection)
		{
		case SELECT_STATUS:
			int onlineStatus = statusList.getCurrTextIndex();
			Options.setLong(Options.OPTION_ONLINE_STATUS, onlineStatus);

			if (Icq.isConnected())
			{
				try
				{
					Icq.setOnlineStatus(onlineStatus);
				} catch (JimmException e)
				{
					JimmException.handleException(e);
					if (e.isCritical()) return;
				}
			}
			
			if ((onlineStatus != ContactList.STATUS_INVISIBLE)
					&& (onlineStatus != ContactList.STATUS_INVIS_ALL)
					&& (onlineStatus != ContactList.STATUS_ONLINE)
					&& (onlineStatus != ContactList.STATUS_CHAT))
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
			} 
			else activateMenu = true;
			break;
	
		case SELECT_XSTATUS:
			Options.setInt(Options.OPTION_XSTATUS, statusList.getCurrTextIndex()-1);
			if (Icq.isConnected())
			{
				try
				{
					Icq.sendUserUnfoPacket();
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

	private void CLManagementItemSelected(int index)
	{
		switch (index)
		{
		case 0: /* Add user */
			status = STATUS_ADD_CONTACT;
			showTextBoxForm("add_user", "uin", null, TextField.NUMERIC);
			break;

		case 1: /* Search for User */
			Search searchf = new Search(false);
			searchf.getSearchForm().activate(Search.SearchForm.ACTIV_JUST_SHOW);
			break;

		case 2: /* Add group */
			status = STATUS_ADD_GROUP;
			showTextBoxForm("add_group", "group_name", null, TextField.ANY);
			break;

		case 3: /* Rename group */
			status = STATUS_RENAME_GROUP;
			groupIds = JimmUI.showGroupSelector("rename_group",
					TAG_RENAME_GROUPS, this, JimmUI.SHS_TYPE_ALL, -1);
			break;

		case 4: /* Delete group */
			groupIds = JimmUI.showGroupSelector("del_group", TAG_DELETE_GROUPS,
					this, JimmUI.SHS_TYPE_EMPTY, -1);
			break;
		}
	}

}
